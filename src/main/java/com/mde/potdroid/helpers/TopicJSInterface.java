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
public class TopicJSInterface extends BenderJSInterface {

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
     * Show an error
     *
     * @param msg the message to display
     */
    @JavascriptInterface
    public void error(final String msg) {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.showError(msg);
            }
        });
    }

    /**
     * Show a success message
     *
     * @param msg the message to display
     */
    @JavascriptInterface
    public void success(final String msg) {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.showSuccess(msg);
            }
        });
    }

    /**
     * Show an information
     *
     * @param msg the message to display
     */
    @JavascriptInterface
    public void info(final String msg) {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.showInfo(msg);
            }
        });
    }

    /**
     * Loads an image in the background, and, if done, inserts it into the webview
     */
    @JavascriptInterface
    public void loadImage(final String url, final String id) {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.loadImage(url, id);
            }
        });
    }

    /**
     * Loads an image in the background, and, if done, inserts it into the webview
     */
    @JavascriptInterface
    public boolean isCached(final String url) {
        return mTopicFragment.isImageCached(url);
    }

    /**
     * Return the menu status
     *
     * @return 1 -> show always, 2 -> show icon, 3 -> orientation dependent
     */
    @JavascriptInterface
    public int getShowMenu() {
        return mSettings.showMenu();
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
     * Check if images should be loaded right now
     *
     * @return true if yes
     */
    @JavascriptInterface
    public boolean isLoadVideos() {
        return mSettings.downloadVideos();
    }

    /**
     * Check if old posts should be darkened
     *
     * @return true if yes
     */
    @JavascriptInterface
    public boolean isDarkenOldPosts() {
        return mSettings.darkenOldPosts();
    }

    /**
     * Check if new posts should be marked
     *
     * @return true if yes
     */
    @JavascriptInterface
    public boolean isMarkNewPosts() {
        return mSettings.markNewPosts();
    }

    /**
     * Check if User is logged in
     *
     * @return true if yes
     */
    @JavascriptInterface
    public boolean isLoggedIn() {
        return Utils.isLoggedIn();
    }

    /**
     * Check if this is the last Page of the topic
     *
     * @return true if yes
     */
    @JavascriptInterface
    public boolean isLastPage() {
        return mTopicFragment.isLastPage();
    }

    /**
     * Check if this is the first Page of the topic
     *
     * @return true if yes
     */
    @JavascriptInterface
    public boolean isFirstPage() {
        return mTopicFragment.isFirstPage();
    }

    /**
     * Open the reply form
     */
    @JavascriptInterface
    public void replyPost() {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.replyPost();
            }
        });
    }

    /**
     * Opens the dialog presenting some more functions for the Post
     *
     * @param post_id the post id
     */
    @JavascriptInterface
    public void openTopicMenu(final int post_id) {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.showPostDialog(post_id);
            }
        });
    }

    /**
     * Open the FormFragment to edit the post
     *
     * @param post_id the post id to edit
     */
    @JavascriptInterface
    public void editPost(final int post_id) {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.editPost(post_id);
            }
        });
    }

    /**
     * Open the FormFragment to answer with a quote
     *
     * @param post_id the post id to quote
     */
    @JavascriptInterface
    public void quotePost(final int post_id) {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.quotePost(post_id);
            }
        });
    }

    /**
     * Open the post in a webbrowser
     *
     * @param post_id the post id
     */
    @JavascriptInterface
    public void linkPost(final int post_id) {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.linkPost(post_id);
            }
        });
    }

    /**
     * send a PM to the author
     *
     * @param post_id the post id
     */
    @JavascriptInterface
    public void pmAuthor(final int post_id) {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.pmToAuthor(post_id);
            }
        });
    }

    /**
     * Bookmark a post
     *
     * @param post_id the post id to bookmark
     */
    @JavascriptInterface
    public void bookmarkPost(final int post_id) {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.bookmarkPost(post_id, null);
            }
        });
    }

    /**
     * Paginate: Go to first page
     */
    @JavascriptInterface
    public void frwd() {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.goToFirstPage();
            }
        });
    }

    /**
     * Paginate: Go to previous page
     */
    @JavascriptInterface
    public void rwd() {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.goToPrevPage();
            }
        });
    }

    /**
     * Paginate: refresh
     */
    @JavascriptInterface
    public void refresh() {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.refreshPage();
            }
        });
    }

    /**
     * Paginate: Go to next page
     */
    @JavascriptInterface
    public void fwd() {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.goToNextPage();
            }
        });
    }

    /**
     * Paginate: Go to last page
     */
    @JavascriptInterface
    public void ffwd() {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.goToLastPage();
            }
        });
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

    public void unveil() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mWebView.loadUrl("javascript:unveil();");
            }
        });
    }

    public void scrollToLastOwnPost() {
        if(mSettings.hasUsername() && mSettings.getUserId() > 0)
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    mWebView.loadUrl(String.format("javascript:scrollToLastPostByUID(%d);",
                            mSettings.getUserId()));
                }
            });
    }

    public void loadAllImages() {
        mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    mWebView.loadUrl("javascript:loadAllImages();");
                }
        });
    }

    public void displayImage(final String url, final String disk_path, final String id) {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mWebView.loadUrl(String.format("javascript:displayImage('%s', '%s', '%s');",
                        url, disk_path, id));
            }
        });
    }

}
