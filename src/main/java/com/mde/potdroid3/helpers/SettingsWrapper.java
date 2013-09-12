package com.mde.potdroid3.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by oli on 8/24/13.
 */
public class SettingsWrapper {

    public static final String PREF_KEY_LOGIN = "pref_login";
    public static final String PREF_KEY_LOGOUT = "pref_logout";
    public static final String PREF_KEY_USERNAME = "user_name";
    public static final String PREF_KEY_USERID = "user_id";
    public static final String PREF_KEY_UAGENT = "unique_uagent";
    public static final String PREF_KEY_COOKIE_NAME = "cookie_name";
    public static final String PREF_KEY_COOKIE_VALUE = "cookie_value";
    public static final String PREF_KEY_COOKIE_PATH = "cookie_path";
    public static final String PREF_KEY_COOKIE_URL = "cookie_url";
    public static final String PREF_KEY_SHOW_BENDERS = "pref_show_benders";
    public static final String PREF_KEY_LOAD_BENDERS = "pref_load_benders";
    public static final String PREF_KEY_LOAD_IMAGES = "pref_load_images";

    private SharedPreferences mSharedPreferences;
    private Context mContext;

    public SettingsWrapper(Context cx) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(cx);
        mContext = cx;
    }

    public Boolean showBenders() {
        return mSharedPreferences.getBoolean(PREF_KEY_SHOW_BENDERS, true);
    }

    public Boolean downloadBenders() {
        String lb = mSharedPreferences.getString(PREF_KEY_LOAD_BENDERS, "0");
        return !(lb.equals("0") || (lb.equals("1") && Network.getConnectionType(mContext) != Network.NETWORK_WIFI));
    }

    public Boolean downloadImages() {
        String lb = mSharedPreferences.getString(PREF_KEY_LOAD_IMAGES, "0");
        Utils.log(lb);
        return !(lb.equals("0") || (lb.equals("1") && Network.getConnectionType(mContext) != Network.NETWORK_WIFI));
    }

    public void setUsername(String username) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_KEY_USERNAME, username);
        editor.commit();
    }

    public Boolean hasUsername() {
        return mSharedPreferences.contains(PREF_KEY_USERNAME);
    }

    public String getUsername() {
        return mSharedPreferences.getString(PREF_KEY_USERNAME, "");
    }

    public void clearUsername() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_KEY_USERNAME);
        editor.commit();
    }

    public void setUserId(int id) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(PREF_KEY_USERID, id);
        editor.commit();
    }

    public void clearUserId() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_KEY_USERID);
        editor.commit();
    }

    public Boolean hasLoginCookie() {
        return mSharedPreferences.contains(PREF_KEY_COOKIE_NAME);
    }

    public BasicClientCookie getLoginCookie() {
        BasicClientCookie cookie = new BasicClientCookie(
                mSharedPreferences.getString(PREF_KEY_COOKIE_NAME, null),
                mSharedPreferences.getString(PREF_KEY_COOKIE_VALUE, null)
        );
        cookie.setPath(mSharedPreferences.getString(PREF_KEY_COOKIE_PATH, null));
        cookie.setDomain(mSharedPreferences.getString(PREF_KEY_COOKIE_URL, null));
        return cookie;
    }

    public void setLoginCookie(Cookie cookie) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_KEY_COOKIE_NAME, cookie.getName());
        editor.putString(PREF_KEY_COOKIE_VALUE, cookie.getValue());
        editor.putString(PREF_KEY_COOKIE_URL, cookie.getDomain());
        editor.putString(PREF_KEY_COOKIE_PATH, cookie.getPath());
        editor.commit();
    }

    public void clearCookie() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_KEY_COOKIE_NAME);
        editor.remove(PREF_KEY_COOKIE_VALUE);
        editor.remove(PREF_KEY_COOKIE_URL);
        editor.remove(PREF_KEY_COOKIE_PATH);
        editor.commit();
    }

    public String getUserAgent() {
        return Network.UAGENT_BASE + mSharedPreferences.getString(PREF_KEY_UAGENT, Network.UAGENT_TAIL);
    }

    public void generateUniqueUserAgent() {
        SecureRandom random = new SecureRandom();
        String uAgent = new BigInteger(50, random).toString(32);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_KEY_UAGENT, uAgent);
        editor.commit();
    }
}
