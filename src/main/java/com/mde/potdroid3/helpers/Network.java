package com.mde.potdroid3.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Created by oli on 5/26/13.
 */
public class Network {

    private DefaultHttpClient mHttpClient;
    private Context mContext;
    private SettingsWrapper mSettings;

    public static final String UAGENT_BASE = "Apache-HttpClient/potdroid";
    public static final String UAGENT_TAIL = "potdroid";

    public static final int NETWORK_NONE = 0;
    public static final int NETWORK_WIFI = 1;
    public static final int NETWORK_ELSE = 2;

    public Network(Context context) {
        mContext    = context;
        mSettings   = new SettingsWrapper(mContext);
        mHttpClient = new DefaultHttpClient();
        mHttpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, mSettings.getUserAgent());

        if (mSettings.hasLoginCookie()) {
            mHttpClient.getCookieStore().addCookie(mSettings.getLoginCookie());
        }
    }

    /**
     * Get a xml document from the mods.de api
     * @throws NoConnectionException
     */
    public InputStream getDocument(String url) throws NoConnectionException {
        Document document;

        // no internet connection...
        if (getConnectionType(mContext) == 0) {
            throw new NoConnectionException();
        }

        try {
            // get the input stream from fetchContent().
            // return null if the fetching of the document failed
            HttpGet request = new HttpGet(url);
            request.addHeader("Accept-Encoding", "gzip");

            HttpResponse response = mHttpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if ((entity == null) || !entity.isStreaming()) {
                throw new NoConnectionException();
            }

            // get the content input stream and take care of gzip encoding
            InputStream instream = entity.getContent();
            Header contentEncoding = response.getFirstHeader("Content-Encoding");
            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                instream = new GZIPInputStream(instream);
            }

            return instream;
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoConnectionException();
        }
    }

    // login
    public Boolean login(String username, String password) throws Exception {

        // first, create new user agent
        // and recreate the httpclient
        mSettings.generateUniqueUserAgent();

        mHttpClient = new DefaultHttpClient();
        mHttpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, mSettings.getUserAgent());

        // add login data
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if (username.equals("") || password.equals("")) {
            return false;
        }
        nvps.add(new BasicNameValuePair("login_username", username));
        nvps.add(new BasicNameValuePair("login_password", password));
        nvps.add(new BasicNameValuePair("login_lifetime", Utils.COOKIE_LIFETIME));

        // create the request
        HttpPost httpost = new HttpPost(Utils.LOGIN_URL);
        httpost.setEntity(new UrlEncodedFormEntity(nvps, Utils.DEFAULT_ENCODING));

        // execute the form
        HttpResponse response = mHttpClient.execute(httpost);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity()
                .getContent(), Utils.DEFAULT_ENCODING));

        // fetch the result of the http request and save it as a string
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        String input = sb.toString();

        // check if the login worked, e.g. one was redirected to SSO.php..
        Pattern pattern = Pattern.compile("http://forum.mods.de/SSO.php\\?UID=([0-9]+)[^']*");
        Matcher m = pattern.matcher(input);

        if (m.find()) {
            // set user id
            mSettings.setUserId( Integer.valueOf(m.group(1)));

            // url for the setcookie found, send a request
            HttpGet cookieUrl = new HttpGet(m.group(0));

            mHttpClient.execute(cookieUrl);

            // store cookie data
            List<Cookie> cookies = mHttpClient.getCookieStore().getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("MDESID")) {
                    mSettings.setLoginCookie(cookie);
                }
            }
            return true;
        }

        return false;
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
     * Sends a Post request to the website and return the PID of the new/edited post.
     */
    public int sendPost(String url, List<NameValuePair> params) {
        HttpPost httppost = new HttpPost(url);
        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, Utils.DEFAULT_ENCODING));
            HttpResponse response = mHttpClient.execute(httppost);

            // find out the PID of the new post
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent(), Utils.DEFAULT_ENCODING));

            // fetch the result of the http request and save it as a string
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String input = sb.toString();

            Pattern pattern = Pattern.compile("thread.php\\?TID=([0-9]+)&temp=[0-9]+&PID=([0-9]+)");
            Matcher m = pattern.matcher(input);

            if (m.find())
                return Integer.parseInt(m.group(2));

            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    /**
     * This function just makes a normal get request and returns the html
     * result as a string
     */
    public String callPage(String url) {
        try {
            InputStream instream = getDocument(url);

            BufferedReader reader = new BufferedReader(new InputStreamReader(instream, Utils.DEFAULT_ENCODING));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");

            return sb.toString();
        } catch (Exception e) {
            return "";
        }
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
}
