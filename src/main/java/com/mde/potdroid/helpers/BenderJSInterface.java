package com.mde.potdroid.helpers;

import android.app.Activity;
import androidx.annotation.Keep;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.mde.potdroid.models.User;

/**
 * The Javascript interface for WebViews that display Benders. Provides methods to
 * retrieve and Download Benders (via BenderHandler)
 */
@Keep
public class BenderJSInterface {

    // A reference to the WebView this interface is attached to
    protected WebView mWebView;

    // the BenderHandler instance
    protected BenderHandler mBenderHandler;

    // A reference to the containing activity
    protected Activity mActivity;

    // A reference to the SettingsWrapper instance
    protected SettingsWrapper mSettings;


    private static final String TAG = "JsInterface";


    public BenderJSInterface(WebView wv, Activity cx) {
        mWebView = wv;
        mActivity = cx;
        mBenderHandler = new BenderHandler(mActivity);
        mSettings = new SettingsWrapper(cx);
    }

    public void setWebView(WebView wv) {
        mWebView = wv;
    }

    /**
     * Log something to Logcat
     *
     * @param msg the message to log
     */
    @JavascriptInterface
    public void log(String msg) {
        Log.i(TAG, msg);
    }

    /**
     * Return if Benders are enabled at all
     *
     * @return true if enabled
     */
    @JavascriptInterface
    public boolean isBenderEnabled() {
        return mSettings.showBenders();
    }

    /**
     * Return if Benders can be downloaded
     *
     * @return true if enabled
     */
    @JavascriptInterface
    public boolean downloadBenders() {
        return mSettings.downloadBenders();
    }

    /**
     * Return the bender position
     *
     * @return 0 -> never, 1 -> always posthead, 2 -> always postbody, 3 -> orientation dependent
     */
    @JavascriptInterface
    public int getBenderPosition() {
        return mSettings.benderPosition();
    }

    /**
     * Return an url (filepath) to a bender
     *
     * @param user_id     the user id of the User whose Bender is requested
     * @param avatar_file The filename of the respective bender
     * @param avatar_id   the ID of the bender
     */
    @JavascriptInterface
    public void displayBender(final int user_id, String avatar_file, int avatar_id) {

        // this is needed, because we want to load asynchroneously.
        User u = new User(user_id);
        u.setAvatarFile(avatar_file);
        u.setAvatarId(avatar_id);

        // The Bender retrieval is asynchroneous, so upon success we
        // have to trigger the Javascript function to display it.
        mBenderHandler.getAvatar(u, new BenderHandler.BenderListener() {
            @Override
            public void onSuccess(final String path) {
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        mWebView.loadUrl("javascript:loadBender(" + user_id + ", '" + path + "');");
                    }
                });

            }

            @Override
            public void onFailure() {
            }
        });
    }

}
