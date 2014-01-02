package com.mde.potdroid.helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class that provides some static helper methods.
 */
public class Utils
{

    // the logcat tag
    public static final String LOG_TAG = "pOT Droid";
    // some static reference to any context for settings retrieval
    protected static Context mContext;

    /**
     * Log something to logcat
     *
     * @param msg the message to log
     */
    public static void log(String msg) {
        Log.v(Utils.LOG_TAG, msg);
    }

    /**
     * Get a drawable asset file
     *
     * @param cx the context
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
     * @param cx the context
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
     * @param cx the context
     * @param filename the icon filename
     * @return Drawable of the icon
     * @throws IOException
     */
    public static Drawable getIcon(Context cx, String filename) throws IOException {
        return getDrawableFromAsset(cx, "thread-icons/" + filename);
    }

    /**
     * Get a drawable asset file
     *
     * @param cx the context
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
     * @param cx the context
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
     * Show a long toast
     *
     * @param cx the context
     * @param content the message to show in the toast
     */
    public static void toast(Context cx, String content) {
        Toast.makeText(cx, content, Toast.LENGTH_LONG).show();
    }

    /**
     * Check if the current device version is Gingerbread (2.3.x)
     *
     * @return true if GB
     */
    public static boolean isGingerbread() {
        return !(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD ||
                android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1);
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
     * @return the Context
     */
    public static Context getApplicationContext() {
        if(mContext == null)
            throw new NullPointerException("No application context saved.");
        return mContext;
    }

    public static class NotLoggedInException extends Exception
    {

    }
}
