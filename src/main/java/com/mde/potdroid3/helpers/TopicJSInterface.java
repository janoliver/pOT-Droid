package com.mde.potdroid3.helpers;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.mde.potdroid3.fragments.TopicFragment;

public class TopicJSInterface extends BenderJSInterface {
    private Integer mCurrentVisiblePost;
    private TopicFragment mTopicFragment;

    public TopicJSInterface(WebView wv, Activity cx, TopicFragment fragment) {
        super(wv, cx);

        mCurrentVisiblePost = 0;
        mTopicFragment = fragment;
    }

    @JavascriptInterface
    public void registerScroll(int id) {
        mCurrentVisiblePost = id;
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

}
