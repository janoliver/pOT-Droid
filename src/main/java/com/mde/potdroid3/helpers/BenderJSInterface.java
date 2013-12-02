package com.mde.potdroid3.helpers;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.mde.potdroid3.models.User;

public class BenderJSInterface {
    protected WebView mWebView;
    protected BenderHandler mBenderHandler;
    protected Activity mActivity;
    protected SettingsWrapper mSettings;

    public BenderJSInterface(WebView wv, Activity cx) {
        mWebView = wv;
        mActivity = cx;
        mBenderHandler = new BenderHandler(mActivity);
        mSettings = new SettingsWrapper(cx);
    }

    @JavascriptInterface
    public void log(String msg) {
        Utils.log(msg);
    }

    @JavascriptInterface
    public boolean isBenderEnabled() {
        return mSettings.showBenders();
    }

    @JavascriptInterface
    public String getBenderUrl(int user_id, String avatar_file, int avatar_id) {
        User u = new User(user_id);
        u.setAvatarFile(avatar_file);
        u.setAvatarId(avatar_id);

        return mBenderHandler.getAvatar(u, new JSInterfaceListener(mWebView, mActivity));
    }

    public class JSInterfaceListener {
        protected WebView mWebView;
        protected Activity mContext;

        public JSInterfaceListener(WebView wv, Activity cx) {
            mWebView = wv;
            mContext = cx;
        }

        public void updateBender(final int user_id) {
            mContext.runOnUiThread(new Runnable() {
                public void run() {
                    mWebView.loadUrl("javascript:loadBender(" + user_id + ");");
                }
            });

        }
    }

}
