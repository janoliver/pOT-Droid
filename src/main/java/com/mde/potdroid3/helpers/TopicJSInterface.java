package com.mde.potdroid3.helpers;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.mde.potdroid3.models.User;

public class TopicJSInterface {
    private WebView mWebView;
    private BenderHandler mBenderHandler;
    private Activity mActivty;

    public TopicJSInterface(WebView wv, Activity cx) {
        mWebView = wv;
        mActivty = cx;
        mBenderHandler = new BenderHandler(mActivty);
    }

    /**
     * Shows a toast issued from within the webviews javascript.
     */
    @JavascriptInterface
    public void log(String msg) {
        Utils.log(msg);
    }

    @JavascriptInterface
    public boolean isBenderEnabled() {
        return true;
    }

    @JavascriptInterface
    public String getBenderUrl(int user_id, String avatar_file, int avatar_id) {
        User u = new User(user_id);
        u.setAvatarFile(avatar_file);
        u.setAvatarId(avatar_id);

        return mBenderHandler.getAvatar(u, new JSInterfaceListener(mWebView, mActivty));
    }

    public class JSInterfaceListener {
        private WebView mWebView;
        private Activity mContext;

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
