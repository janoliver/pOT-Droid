package com.mde.potdroid.helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class that provides some static helper methods.
 */
public class Utils {

    // the logcat tag
    public static final String LOG_TAG = "pOT Droid";
    // some URLs.
    public static final String BASE_URL = "http://forum.mods.de/bb/";
    public static final String ASYNC_URL = "async/";
    public static final int NETWORK_NONE = 0;
    public static final int NETWORK_WIFI = 1;
    public static final int NETWORK_ELSE = 2;
    private static final String CACHE_DIR = "cache";
    // some static reference to any context for settings retrieval
    protected static Context mContext;

    /**
     * Get a drawable asset file
     *
     * @param cx      the context
     * @param strName the filename
     * @return Drawable asset
     * @throws IOException
     */
    public static Drawable getDrawableFromAsset(Context cx, String strName) throws IOException {
        AssetManager assetManager = cx.getAssets();
        InputStream istr = assetManager.open(strName);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        Drawable d = new BitmapDrawable(cx.getResources(), bitmap);
        return d;
    }

    /**
     * Get a drawable Icon from the assets folder
     *
     * @param cx      the context
     * @param icon_id the icon id
     * @return Drawable of the icon
     * @throws IOException
     */
    public static Drawable getIcon(Context cx, Integer icon_id) throws IOException {
        return getDrawableFromAsset(cx, String.format("thread-icons/icon%d.png", icon_id));
    }

    /**
     * Get a drawable Icon from the assets folder
     *
     * @param cx       the context
     * @param filename the icon filename
     * @return Drawable of the icon
     * @throws IOException
     */
    public static Drawable getIcon(Context cx, String filename) throws IOException {
        return getDrawableFromAsset(cx, "thread-icons/" + filename);
    }

    /**
     * Get a drawable Smiley from the assets folder
     *
     * @param cx       the context
     * @param filename the icon filename
     * @return Drawable of the icon
     * @throws IOException
     */
    public static Drawable getSmiley(Context cx, String filename) throws IOException {
        return getDrawableFromAsset(cx, "smileys/" + filename);
    }

    /**
     * Get a drawable asset file
     *
     * @param cx      the context
     * @param strName the filename
     * @return Bitmap asset
     * @throws IOException
     */
    public static Bitmap getBitmapFromAsset(Context cx, String strName) throws IOException {
        AssetManager assetManager = cx.getAssets();
        InputStream istr = assetManager.open(strName);
        return BitmapFactory.decodeStream(istr);
    }

    /**
     * Get a drawable Icon from the assets folder
     *
     * @param cx       the context
     * @param filename the icon filename
     * @return Drawable of the icon
     * @throws IOException
     */
    public static Bitmap getBitmapIcon(Context cx, String filename) throws IOException {
        return getBitmapFromAsset(cx, "thread-icons/" + filename);
    }

    /**
     * Get a drawable Icon from the assets folder
     *
     * @param cx the context
     * @param id the icon id
     * @return Drawable of the icon
     * @throws IOException
     */
    public static Bitmap getBitmapIcon(Context cx, Integer id) throws IOException {
        return getBitmapFromAsset(cx, String.format("thread-icons/icon%d.png", id));
    }

    /**
     * Check if the current device version is Kitkat (4.4.x)
     *
     * @return true if Kitkat
     */
    public static boolean isKitkat() {
        return android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.KITKAT;
    }

    /**
     * Check if the current device version is Kitkat (2.3.3)
     *
     * @return true if Kitkat
     */
    public static boolean isGingerbread() {
        return android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD ||
                android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD_MR1;
    }

    /**
     * Set the login state of the user to be not logged in
     */
    public static void setNotLoggedIn() {
        if (mContext == null)
            return;

        SettingsWrapper settings = new SettingsWrapper(mContext);
        settings.clearUserId();
        settings.clearCookie();
        settings.clearUsername();
    }

    public static boolean isLoggedIn() {
        if (mContext == null)
            return false;

        SettingsWrapper settings = new SettingsWrapper(mContext);
        return settings.hasUsername();
    }

    /**
     * Set the static context reference needed for some methods
     *
     * @param cx the context
     */
    public static void setApplicationContext(Context cx) {
        mContext = cx;
    }

    /**
     * Get the static application context
     *
     * @return the Context
     */
    public static Context getApplicationContext() {
        if (mContext == null)
            throw new NullPointerException("No application context saved.");
        return mContext;
    }

    public static void printException(Throwable e) {
        e.printStackTrace();

        SettingsWrapper s = new SettingsWrapper(getApplicationContext());
        if (s.isDebug())
            CustomExceptionHandler.writeExceptionToSdCard(e);
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

    public static class NotLoggedInException extends Exception {

    }

    public static int getColorByAttr(Context cx, int attr) {
        TypedValue typedValue = new TypedValue();
        cx.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    public static String getStringByAttr(Context cx, int attr) {
        TypedValue typedValue = new TypedValue();
        cx.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.string.toString();
    }

    public static int getDrawableResourceIdByAttr(Context cx, int attr) {
        TypedArray ta = cx.obtainStyledAttributes(new int[] { attr });
        int resid = ta.getResourceId(0, 0);
        ta.recycle();
        return resid;
    }

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Spanned fromHtml(String html_str) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html_str,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html_str);
        }
        return result;
    }

    public static String getFormattedTime(String format, Date date) {
        SimpleDateFormat f = new SimpleDateFormat(format);
        SettingsWrapper s = new SettingsWrapper(getApplicationContext());
        if(s.isUseGermanTimezone())
            f.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        return f.format(date);
    }
}
