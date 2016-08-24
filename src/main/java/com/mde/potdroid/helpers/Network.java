package com.mde.potdroid.helpers;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import okhttp3.*;
import okhttp3.internal.JavaNetCookieJar;
import okio.BufferedSink;
import okio.Okio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Network functionality class. Provides convenience methods for Get and Post
 * requests, login and some URLs.
 */
public class Network {
    private static OkHttpClient mHttpClient;

    // this is the AsyncHttpClient we use for the network interaction
    private Headers mHeaders;

    // a reference to the Context
    private Context mContext;

    // A reference to the Settings
    private SettingsWrapper mSettings;

    public static final String LOGIN_URL = "http://login.mods.de/";

    // the User agent template
    public static final String UAGENT_TPL = "okhttp3/potdroid-%1$s";

    // the only two required encodings
    public static final String ENCODING_UTF8 = "UTF-8";
    public static final String ENCODING_ISO = "ISO-8859-15";

    // the cookie lifetime is set to one year
    public static final String COOKIE_LIFETIME = "31536000";

    public Network(Context context) {
        mContext = context;
        mSettings = new SettingsWrapper(mContext);

        ensureHttpClient();
    }

    public void ensureHttpClient() {
        if (mHttpClient == null) {
            CookieHandler cookieHandler = new CookieManager(
                    new PersistentCookieStore(mContext), CookiePolicy.ACCEPT_ALL);
            mHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(mSettings.getConnectionTimeout(), TimeUnit.SECONDS)
                    .readTimeout(mSettings.getConnectionTimeout(), TimeUnit.SECONDS)
                    .cookieJar(new JavaNetCookieJar(cookieHandler))
                    .build();
        }

        mHeaders = new Headers.Builder()
                .add("User-Agent", mSettings.getUserAgent())
                .build();
    }

    public OkHttpClient getHttpClient() {
        ensureHttpClient();
        return mHttpClient;
    }

    /**
     * Get a xml document from the mods.de api
     */
    public Call get(String url, Callback responseHandler) {
        ensureHttpClient();
        Request request = new Request.Builder()
                .url(Utils.getAbsoluteUrl(url))
                .headers(mHeaders)
                .build();
        Call c = mHttpClient.newCall(request);
        c.enqueue(responseHandler);
        return c;
    }

    /**
     * Get a xml document from the mods.de api
     */
    public Call post(String url, RequestBody params, Callback responseHandler) {
        ensureHttpClient();
        Request request = new Request.Builder()
                .url(Utils.getAbsoluteUrl(url))
                .headers(mHeaders)
                .post(params)
                .build();

        Call c = mHttpClient.newCall(request);
        c.enqueue(responseHandler);
        return c;
    }

    public static void logout(Context cx) {
        PersistentCookieStore p = new PersistentCookieStore(cx);
        p.removeAll();

        SettingsWrapper s = new SettingsWrapper(cx);
        s.clearUsername();
        s.clearUserId();

        // delete httpclient singleton
        mHttpClient = null;
    }

    /**
     * Given username and password, try to login. We do not use AsyncHttpLoader here
     * because we generate new user agents and do not use cookie storage here.
     *
     * @param username the username
     * @param password the password
     * @param callback the callback instance
     */
    public void login(String username, String password, final LoginCallback callback) {

        // first, create new random user agent, since the mde login system partly
        // works with User agents
        mSettings.generateUniqueUserAgent();
        mHeaders = new Headers.Builder()
                .add("User-Agent", mSettings.getUserAgent())
                .build();

        if (username.equals("") || password.equals(""))
            callback.onFailure();

        // add login data
        RequestBody formBody = new com.mde.potdroid.helpers.FormEncodingBuilder(ENCODING_ISO)
                .add("login_username", username)
                .add("login_password", password)
                .add("login_lifetime", COOKIE_LIFETIME)
                .build();

        post(LOGIN_URL, formBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Utils.printException(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);

                Pattern pattern = Pattern.compile("http://forum.mods.de/SSO.php\\?UID=([0-9]+)[^']*");

                // check if the login worked, e.g. one was redirected to SSO.php..
                Matcher m = pattern.matcher(response.body().string());

                if (m.find()) {
                    // set user id
                    mSettings.setUserId(Integer.valueOf(m.group(1)));

                    get(m.group(0), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            callback.onFailure();
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            // delete httpclient singleton
                            mHttpClient = null;

                            // do nothing, cookie was hopefully saved... :)
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                public void run() {
                                    callback.onSuccess();
                                }
                            });
                        }

                    });
                } else {
                    callback.onFailure();
                }
            }

        });

    }

    public static void downloadFile(final Context cx, final Uri uri, final File dir, final DownloadCallback callback) {
        final File file = new File(dir, uri.getLastPathSegment());

        Network network = new Network(cx.getApplicationContext());
        Request request = new Request.Builder().url(uri.toString()).build();
        network.getHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(uri, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                BufferedSink sink = null;
                FileOutputStream fo = null;
                try {
                    fo = new FileOutputStream(file);
                    sink = Okio.buffer(Okio.sink(fo));
                    sink.writeAll(response.body().source());
                    sink.close();

                    callback.onSuccess(uri, file);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (sink != null)
                            sink.close();
                        if (fo != null)
                            fo.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    response.body().close();
                }

            }
        });
    }


    /**
     * A callback Class for the login function.
     */
    public interface LoginCallback {

        /**
         * Called on login success
         */
        void onSuccess();

        /**
         * Called on login failure
         */
        void onFailure();

    }


    /**
     * A callback Class for the login function.
     */
    public interface DownloadCallback {

        /**
         * Called on login success
         */
        void onSuccess(final Uri uri, final File download);

        /**
         * Called on login failure
         */
        void onFailure(final Uri uri, Exception e);

    }

}
