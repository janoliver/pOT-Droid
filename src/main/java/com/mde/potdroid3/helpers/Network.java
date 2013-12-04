package com.mde.potdroid3.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.loopj.android.http.*;
import org.apache.http.Header;
import org.apache.http.cookie.Cookie;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oli on 5/26/13.
 */
public class Network {

    private AsyncHttpClient mHttpClient = new AsyncHttpClient();
    private Context mContext;
    private SettingsWrapper mSettings;

    public static final String BASE_URL = "http://forum.mods.de/bb/";
    public static final String LOGIN_URL = "http://login.mods.de/";
    public static final String BOARD_URL_POST = "newreply.php";
    public static final String BOARD_URL_EDITPOST = "editreply.php";
    public static final String ASYNC_URL = "async/";

    public static final String UAGENT_BASE = "Apache-HttpClient/potdroid";
    public static final String UAGENT_TAIL = "potdroid";


    public static final String COOKIE_LIFETIME = "31536000";

    public static final int NETWORK_NONE = 0;
    public static final int NETWORK_WIFI = 1;
    public static final int NETWORK_ELSE = 2;

    public Network(Context context) {
        mContext    = context;
        mSettings   = new SettingsWrapper(mContext);
        mHttpClient.setUserAgent(mSettings.getUserAgent());
        mHttpClient.addHeader("Accept-Encoding", "gzip");

        if (mSettings.hasLoginCookie()) {
            PersistentCookieStore cStore = new PersistentCookieStore(mContext);
            cStore.addCookie(mSettings.getLoginCookie());
            mHttpClient.setCookieStore(cStore);
        }
    }

    /**
     * Get a xml document from the mods.de api
     * @throws NoConnectionException
     */
    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        mHttpClient.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        mHttpClient.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public void cancelLoad() {
        mHttpClient.cancelRequests(mContext, true);
    }

    // login
    public void login(String username, String password, final LoginCallback callback) {

        // first, create new user agent
        // and recreate the httpclient
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

    // 0 -> not connected
    // 1 -> wifi
    // 2 -> else
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
     * This is the exception that is thrown when internet connection fails.
     */
    public class NoConnectionException extends Exception {
        private static final long serialVersionUID = 1L;

        public NoConnectionException() {
            super("No internet connection!");
        }
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public interface LoginCallback {
        abstract public void onSuccess();
        abstract public void onFailure();
        abstract public void onStart();
        abstract public void onStop();
    }

}
