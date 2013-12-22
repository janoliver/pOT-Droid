package com.mde.potdroid.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * This class provides access to the preferences of the App. All settings should be
 * retrieved through this class.
 */
public class SettingsWrapper {

    // The keys to the settings
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
    public static final String PREF_KEY_DEBUG = "pref_debug_mode";
    public static final String PREF_KEY_LOAD_BENDERS = "pref_load_benders";
    public static final String PREF_KEY_LOAD_IMAGES = "pref_load_images";
    public static final String PREF_KEY_POLL_MESSAGES = "pref_message_polling_interval";
    public static final String PREF_KEY_NOTIFICATION_VIBRATE = "pref_notification_vibrate";
    public static final String PREF_KEY_NOTIFICATION_SOUND = "pref_notification_sound";

    // some references
    private SharedPreferences mSharedPreferences;
    private Context mContext;

    public SettingsWrapper(Context cx) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(cx);
        mContext = cx;
    }

    /**
     * Do we show benders at all?
     * @return true, if benders should be shown
     */
    public Boolean showBenders() {
        return mSharedPreferences.getBoolean(PREF_KEY_SHOW_BENDERS, true);
    }

    /**
     * The interval for the PM polling
     * @return interval in seconds
     */
    public Integer pollMessagesInterval() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_POLL_MESSAGES, "0"));
    }

    /**
     * Never, always or only in Wifi benders
     * @return 0 -> never, 1 -> only wifi, 2 -> always
     */
    public String loadBenders() {
        return mSharedPreferences.getString(PREF_KEY_LOAD_BENDERS, "0");
    }

    /**
     * Never, always or only in Wifi images
     * @return 0 -> never, 1 -> only wifi, 2 -> always
     */
    public String loadImages() {
        return mSharedPreferences.getString(PREF_KEY_LOAD_IMAGES, "0");
    }

    /**
     * Check if, given the current network state, Benders should be loaded.
     * @return true if loaded
     */
    public Boolean downloadBenders() {
        String lb = mSharedPreferences.getString(PREF_KEY_LOAD_BENDERS, "0");
        return !(lb.equals("0") || (lb.equals("1") &&
                Network.getConnectionType(mContext) != Network.NETWORK_WIFI));
    }

    /**
     * Check if, given the current network state, Images should be loaded.
     * @return true if loaded
     */
    public Boolean downloadImages() {
        String lb = mSharedPreferences.getString(PREF_KEY_LOAD_IMAGES, "0");
        return !(lb.equals("0") || (lb.equals("1") &&
                Network.getConnectionType(mContext) != Network.NETWORK_WIFI));
    }

    /**
     * Set the settings username
     */
    public void setUsername(String username) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_KEY_USERNAME, username);
        editor.commit();
    }

    /**
     * Check if a username is stored in the settings
     * @return true if set
     */
    public Boolean hasUsername() {
        return mSharedPreferences.contains(PREF_KEY_USERNAME);
    }

    /**
     * Check if debug mode is switched on
     * @return true if switched on
     */
    public Boolean isDebug() {
        return mSharedPreferences.getBoolean(PREF_KEY_DEBUG, false);
    }

    /**
     * Check if notifications should vibrate
     * @return true if switched on
     */
    public Boolean isNotificationVibrate() {
        return mSharedPreferences.getBoolean(PREF_KEY_NOTIFICATION_VIBRATE, false);
    }

    /**
     * Get the URI to the notification sound
     * @return String uri of the sound
     */
    public String getNotificationSoundURI() {
        return mSharedPreferences.getString(PREF_KEY_NOTIFICATION_SOUND,
                Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    }

    /**
     * Get the username as stored in the settings
     * @return the username
     */
    public String getUsername() {
        return mSharedPreferences.getString(PREF_KEY_USERNAME, "");
    }

    /**
     * Clear the username from the settings.
     */
    public void clearUsername() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_KEY_USERNAME);
        editor.commit();
    }

    /**
     * Set the User ID in the sharedpreferences
     * @param id the User ID
     */
    public void setUserId(int id) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(PREF_KEY_USERID, id);
        editor.commit();
    }

    /**
     * Clear the user ID from the settings
     */
    public void clearUserId() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_KEY_USERID);
        editor.commit();
    }

    /**
     * Get the user id from the settings
     * @return user ID
     */
    public int getUserId() {
        return mSharedPreferences.getInt(PREF_KEY_USERID, 0);
    }

    /**
     * Check, if a login cookie is stored in the settings
     * @return true if one is present
     */
    public Boolean hasLoginCookie() {
        return mSharedPreferences.contains(PREF_KEY_COOKIE_NAME);
    }

    /**
     * Get a Login Cookie from the values stored in the settings.
     * @return the cookie
     */
    public BasicClientCookie getLoginCookie() {
        BasicClientCookie cookie = new BasicClientCookie(
                mSharedPreferences.getString(PREF_KEY_COOKIE_NAME, null),
                mSharedPreferences.getString(PREF_KEY_COOKIE_VALUE, null)
        );
        cookie.setPath(mSharedPreferences.getString(PREF_KEY_COOKIE_PATH, null));
        cookie.setDomain(mSharedPreferences.getString(PREF_KEY_COOKIE_URL, null));
        return cookie;
    }

    /**
     * Store a login cookie in the settings
     * @param cookie the cookie to store
     */
    public void setLoginCookie(Cookie cookie) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_KEY_COOKIE_NAME, cookie.getName());
        editor.putString(PREF_KEY_COOKIE_VALUE, cookie.getValue());
        editor.putString(PREF_KEY_COOKIE_URL, cookie.getDomain());
        editor.putString(PREF_KEY_COOKIE_PATH, cookie.getPath());
        editor.commit();
    }

    /**
     * Clear the login cookie from the settings
     */
    public void clearCookie() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_KEY_COOKIE_NAME);
        editor.remove(PREF_KEY_COOKIE_VALUE);
        editor.remove(PREF_KEY_COOKIE_URL);
        editor.remove(PREF_KEY_COOKIE_PATH);
        editor.commit();
    }

    /**
     * Get the user agent of the current user.
     * @return User agent
     */
    public String getUserAgent() {
        return String.format(Network.UAGENT_TPL, mSharedPreferences.getString(PREF_KEY_UAGENT, ""));
    }

    /**
     * generate a random string for the user agent and store it in the settings.
     */
    public void generateUniqueUserAgent() {
        SecureRandom random = new SecureRandom();
        String uAgent = new BigInteger(50, random).toString(32);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_KEY_UAGENT, uAgent);
        editor.commit();
    }
}
