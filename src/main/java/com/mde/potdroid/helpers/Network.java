package com.mde.potdroid.helpers;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import com.squareup.okhttp.*;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Network functionality class. Provides convenience methods for Get and Post
 * requests, login and some URLs.
 */
public class Network {
    private static OkHttpClient mHttpClient;

    private static final String CACHE_DIR = "cache";

    // this is the AsyncHttpClient we use for the network interaction
    private Headers mHeaders;

    public static final int DEFAULT_TIMEOUT = 60; // s

    // a reference to the Context
    private Context mContext;

    // A reference to the Settings
    private SettingsWrapper mSettings;

    // some URLs.
    public static final String BASE_URL = "http://forum.mods.de/bb/";
    public static final String LOGIN_URL = "http://login.mods.de/";
    public static final String BOARD_URL_POST = "newreply.php";
    public static final String BOARD_URL_THREAD = "newthread.php";
    public static final String BOARD_URL_EDITPOST = "editreply.php";
    public static final String ASYNC_URL = "async/";

    // the User agent template
    public static final String UAGENT_TPL = "Apache-HttpClient/potdroid-%1$s";

    public static final String ENCODING_UTF8 = "UTF-8";
    public static final String ENCODING_ISO = "ISO-8859-15";

    public static final String COOKIE_LIFETIME = "31536000";

    public static final int NETWORK_NONE = 0;
    public static final int NETWORK_WIFI = 1;
    public static final int NETWORK_ELSE = 2;

    public Network(Context context) {
        if(mHttpClient == null) {
            mHttpClient = new OkHttpClient();
            mHttpClient.setConnectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        }
        mContext = context;
        mSettings = new SettingsWrapper(mContext);
        initHttpClient();
    }

    void initHttpClient() {
        mHeaders = new Headers.Builder()
                .add("User-Agent", mSettings.getUserAgent())
                .build();
        mHttpClient.setCookieHandler(CookieHandler.getDefault());
    }

    /**
     * Get a xml document from the mods.de api
     */
    public void get(String url, Callback responseHandler) {
        Request request = new Request.Builder()
                .url(getAbsoluteUrl(url))
                .headers(mHeaders)
                .build();
        mHttpClient.newCall(request).enqueue(responseHandler);
    }

    /**
     * Get a xml document from the mods.de api
     */
    public void post(String url, RequestBody params, Callback responseHandler) {

        Request request = new Request.Builder()
                .url(getAbsoluteUrl(url))
                .headers(mHeaders)
                .post(params)
                .build();
        mHttpClient.newCall(request).enqueue(responseHandler);
    }

    /**
     * Try to cancel the current loading of the httpclient
     */
    public void cancelLoad() {
        mHttpClient.cancel(null);
    }

    /**
     * Change timeout
     */
    public void setTimeout(Integer seconds) {
        mHttpClient.setConnectTimeout(seconds, TimeUnit.SECONDS);
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
        initHttpClient();

        if (username.equals("") || password.equals(""))
            callback.onFailure();

        // add login data
        RequestBody formBody = new FormEncodingBuilder()
                .add("login_username", username)
                .add("login_password", password)
                .add("login_lifetime", COOKIE_LIFETIME)
                .build();

        post(LOGIN_URL, formBody, new Callback() {
            @Override
            public void onFailure(Request request, IOException throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Pattern pattern = Pattern.compile("http://forum.mods.de/SSO.php\\?UID=([0-9]+)[^']*");

                // check if the login worked, e.g. one was redirected to SSO.php..
                Matcher m = pattern.matcher(response.body().string());

                if (m.find()) {
                    // set user id
                    mSettings.setUserId(Integer.valueOf(m.group(1)));

                    get(m.group(0), new Callback() {
                        @Override
                        public void onFailure(Request request, IOException throwable) {
                            callback.onFailure();
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
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


    /**
     * Returns the state of the network connection
     *
     * @param context A context object
     * @return 0 -> not connected, 1 -> wifi, 2 -> else
     */
    public static int getConnectionType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null) {
            return NETWORK_NONE;
        }

        if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return NETWORK_WIFI;
        }

        return NETWORK_ELSE;
    }

    /**
     * Given a relative URL, return the absolute one to http://forum.mods.de/..
     *
     * @param relativeUrl the URL to shape
     * @return the shaped url
     */
    public static String getAbsoluteUrl(String relativeUrl) {
        if(relativeUrl.startsWith("http://"))
            return relativeUrl;
        return BASE_URL + relativeUrl;
    }

    /**
     * Given a URL relative to /async, attach async/
     *
     * @param relativeUrl the URL to shape
     * @return the shaped url
     */
    public static String getAsyncUrl(String relativeUrl) {
        if(relativeUrl.startsWith("http://"))
            return relativeUrl;
        return ASYNC_URL + relativeUrl;
    }

    public static File getCacheDir(Context context) {
        File ext_root = Environment.getExternalStorageDirectory();
        return new File(ext_root, "Android/data/" + context.getPackageName() + CACHE_DIR);
    }

    /**
     * A callback Class for the login function.
     */
    public interface LoginCallback {

        /**
         * Called on login success
         */
        abstract public void onSuccess();

        /**
         * Called on login failure
         */
        abstract public void onFailure();

        /**
         * Called before the request is started
         */
        abstract public void onStart();

        /**
         * Called after the request is finished.
         */
        abstract public void onStop();
    }

}
