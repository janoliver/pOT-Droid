package com.mde.potdroid.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.loopj.android.http.*;
import org.apache.http.Header;
import org.apache.http.cookie.Cookie;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Network functionality class. Provides convenience methods for Get and Post
 * requests, login and some URLs.
 */
public class Network {

    // this is the AsyncHttpClient we use for the network interaction
    private AsyncHttpClient mHttpClient = new AsyncHttpClient();

    public static final int DEFAULT_TIMEOUT = 20 * 1000;

    // a reference to the Context
    private Context mContext;

    // A reference to the Settings
    private SettingsWrapper mSettings;

    // some URLs.
    public static final String BASE_URL = "http://forum.mods.de/bb/";
    public static final String LOGIN_URL = "http://login.mods.de/";
    public static final String BOARD_URL_POST = "newreply.php";
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
        mContext    = context;
        mSettings   = new SettingsWrapper(mContext);
        mHttpClient.setUserAgent(mSettings.getUserAgent());
        mHttpClient.addHeader("Accept-Encoding", "gzip");
        mHttpClient.setTimeout(DEFAULT_TIMEOUT);

        if (mSettings.hasLoginCookie()) {
            PersistentCookieStore cStore = new PersistentCookieStore(mContext);
            cStore.addCookie(mSettings.getLoginCookie());
            mHttpClient.setCookieStore(cStore);
        }
    }

    /**
     * Get a xml document from the mods.de api
     */
    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        mHttpClient.get(getAbsoluteUrl(url), params, responseHandler);
    }

    /**
     * Post to the mods.de website.
     */
    public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        mHttpClient.post(getAbsoluteUrl(url), params, responseHandler);
    }

    /**
     * Try to cancel the current loading of the httpclient
     */
    public void cancelLoad() {
        mHttpClient.cancelRequests(mContext, true);
    }

    /**
     * Given username and password, try to login. We do not use AsyncHttpLoader here
     * because we generate new user agents and do not use cookie storage here.
     * @param username the username
     * @param password the password
     * @param callback the callback instance
     */
    public void login(String username, String password, final LoginCallback callback) {

        // first, create new random user agent, since the mde login system partly
        // works with User agents
        mSettings.generateUniqueUserAgent();
        mHttpClient.setUserAgent(mSettings.getUserAgent());

        // add login data
        RequestParams params = new RequestParams();
        if (username.equals("") || password.equals(""))
            callback.onFailure();

        params.put("login_username", username);
        params.put("login_password", password);
        params.put("login_lifetime", COOKIE_LIFETIME);

        mHttpClient.post(LOGIN_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Pattern pattern = Pattern.compile("http://forum.mods.de/SSO.php\\?UID=([0-9]+)[^']*");

                // check if the login worked, e.g. one was redirected to SSO.php..
                Matcher m = pattern.matcher(new String(responseBody));

                if (m.find()) {
                    // set user id
                    mSettings.setUserId(Integer.valueOf(m.group(1)));

                    final PersistentCookieStore cStore = new PersistentCookieStore(mContext);
                    mHttpClient.setCookieStore(cStore);
                    mHttpClient.get(m.group(0), null, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            for (Cookie cookie : cStore.getCookies()) {
                                if (cookie.getName().equals("MDESID")) {
                                    mSettings.setLoginCookie(cookie);
                                    callback.onSuccess();
                                    return;
                                }
                            }
                            callback.onFailure();
                        }
                    });
                } else {
                    callback.onFailure();
                }
            }

            @Override
            public void onFinish() {
                callback.onStop();
            }

            @Override
            public void onStart() {
                callback.onStart();
            }
        });
    }


    /**
     * Returns the state of the network connection
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
     * @param relativeUrl the URL to shape
     * @return the shaped url
     */
    public static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    /**
     * Given a URL relative to /async, attach async/
     * @param relativeUrl the URL to shape
     * @return the shaped url
     */
    public static String getAsyncUrl(String relativeUrl) {
        return ASYNC_URL + relativeUrl;
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
