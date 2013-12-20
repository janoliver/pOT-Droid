package com.mde.potdroid.helpers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.mde.potdroid.fragments.TopicFragment;

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

    @JavascriptInterface
    public void editPost(int post_id) {
        mTopicFragment.editPost(post_id);
    }

    @JavascriptInterface
    public void quotePost(int post_id) {
        mTopicFragment.quotePost(post_id);
    }

    @JavascriptInterface
    public void linkPost(int post_id) {
        mTopicFragment.linkPost(post_id, null);
    }

    @JavascriptInterface
    public void bookmarkPost(int post_id) {
        mTopicFragment.bookmarkPost(post_id, null);
    }

    @JavascriptInterface
    public void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        mActivity.startActivity(i);
    }

}
