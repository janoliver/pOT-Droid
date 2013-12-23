package com.mde.potdroid.helpers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.mde.potdroid.fragments.TopicFragment;

/**
 * The Javascript interface for the Topic Webviews, extended from the Bender js interface.
 * Provides some more API functions.
 */
public class TopicJSInterface extends BenderJSInterface
{

    // this is the post that is currently visible
    private Integer mCurrentVisiblePost;

    // a reference to the topic fragment
    private TopicFragment mTopicFragment;

    public TopicJSInterface(WebView wv, Activity cx, TopicFragment fragment) {
        super(wv, cx);

        mCurrentVisiblePost = 0;
        mTopicFragment = fragment;
    }

    /**
     * When scrolled over a post, register it
     *
     * @param id the Post id
     */
    @JavascriptInterface
    public void registerScroll(int id) {
        mCurrentVisiblePost = id;
    }

    /**
     * Get the last or currently visible post id
     *
     * @return post id
     */
    @JavascriptInterface
    public int getScroll() {
        return mCurrentVisiblePost;
    }

    /**
     * Check if images should be loaded right now
     *
     * @return true if yes
     */
    @JavascriptInterface
    public boolean isLoadImages() {
        return mSettings.downloadImages();
    }

    /**
     * Opens the dialog presenting some more functions for the Post
     *
     * @param post_id the post id
     */
    @JavascriptInterface
    public void openTopicMenu(int post_id) {
        mTopicFragment.showPostDialog(post_id);
    }

    /**
     * Open the FormFragment to edit the post
     *
     * @param post_id the post id to edit
     */
    @JavascriptInterface
    public void editPost(int post_id) {
        mTopicFragment.editPost(post_id);
    }

    /**
     * Open the FormFragment to answer with a quote
     *
     * @param post_id the post id to quote
     */
    @JavascriptInterface
    public void quotePost(int post_id) {
        mTopicFragment.quotePost(post_id);
    }

    /**
     * Open the post in a webbrowser
     *
     * @param post_id the post id
     */
    @JavascriptInterface
    public void linkPost(int post_id) {
        mTopicFragment.linkPost(post_id);
    }

    /**
     * Bookmark a post
     *
     * @param post_id the post id to bookmark
     */
    @JavascriptInterface
    public void bookmarkPost(int post_id) {
        mTopicFragment.bookmarkPost(post_id, null);
    }

    /**
     * Send an intent and open the url URL
     *
     * @param url url to open
     */
    @JavascriptInterface
    public void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        mActivity.startActivity(i);
    }

}
