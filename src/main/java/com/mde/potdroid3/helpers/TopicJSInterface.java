package com.mde.potdroid3.helpers;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.mde.potdroid3.fragments.TopicFragment;
import com.mde.potdroid3.models.User;

public class TopicJSInterface {
    private WebView mWebView;
    private BenderHandler mBenderHandler;
    private Activity mActivity;
    private Integer mCurrentVisiblePost;
    private SettingsWrapper mSettings;
    private TopicFragment mTopicFragment;

    public TopicJSInterface(WebView wv, Activity cx, TopicFragment fragment) {
        mWebView = wv;
        mActivity = cx;
        mBenderHandler = new BenderHandler(mActivity);
        mCurrentVisiblePost = 0;
        mSettings = new SettingsWrapper(cx);
        mTopicFragment = fragment;
    }

    @JavascriptInterface
    public void registerScroll(int id) {
        mCurrentVisiblePost = id;
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

    @JavascriptInterface
    public int getScroll() {
        return mCurrentVisiblePost;
    }

    @JavascriptInterface
    public boolean isLoadImages() {
        return mSettings.downloadImages();
    }

    @JavascriptInterface
    public void openTopicMenu(int post_id) {
        mTopicFragment.showPostDialog(post_id);
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
