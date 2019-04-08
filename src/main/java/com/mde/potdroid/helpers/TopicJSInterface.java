package com.mde.potdroid.helpers;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.Keep;
import android.util.DisplayMetrics;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.mde.potdroid.fragments.TopicFragment;

/**
 * The Javascript interface for the Topic Webviews, extended from the Bender js interface.
 * Provides some more API functions.
 */
@Keep
public class TopicJSInterface extends BenderJSInterface {

    // this is the post that is currently visible
    private Integer mCurrentVisiblePost;

    // a reference to the topic fragment
    private TopicFragment mTopicFragment;

    private boolean mLightboxOpen = false;

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
     * Opens an image or video in the Media Activity
     */
    @JavascriptInterface
    public void zoom(final String url, final String type) {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.zoomImage(url, type);
            }
        });
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
    public boolean isLoadGifs() {
        return mSettings.downloadGifs();
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
     * Check if swipe to refresh is on
     *
     * @return true if yes
     */
    @JavascriptInterface
    public boolean isPullUpToRefresh() {
        return mSettings.isSwipeToRefreshTopic();
    }

    /**
     * Check if the topic end indicator should be shown
     *
     * @return true if yes
     */
    @JavascriptInterface
    public boolean isShowEndIndicator() {
        return mSettings.isShowEndIndicator();
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

    @JavascriptInterface
    public int getToolBarHeightInDp() {
        Resources resources = mTopicFragment.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float pxHeight = mTopicFragment.getBaseActivity().getToolbar().getHeight();
        return (int) (pxHeight / metrics.densityDpi * 160f);
    }

    @JavascriptInterface
    public boolean isOverlayToolbars() {
        return mTopicFragment.getBaseActivity().getOverlayToolbars();
    }

    @JavascriptInterface
    public boolean isBottomToolbar() {
        return mSettings.isBottomToolbar();
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
     * Store the post
     *
     * @param post_id the post id to edit
     */
    @JavascriptInterface
    public void savePost(final int post_id) {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.savePost(post_id);
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
     * copy the link to the post to clipboard
     *
     * @param post_id the post id
     */
    @JavascriptInterface
    public void copyPostLink(final int post_id) {
        mTopicFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mTopicFragment.clipboardPostUrl(post_id);
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

    public void tobottom() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mWebView.loadUrl("javascript:scrollToBottom();");
            }
        });
    }

    public void scrollToLastOwnPost() {
        if (mSettings.hasUsername() && mSettings.getUserId() > 0)
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
                mWebView.loadUrl("javascript:loadAllImages();loadAllGifs();loadAllVideos();");
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

    @JavascriptInterface
    public void displayImageLoader(final String id) {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mWebView.loadUrl(String.format("javascript:displayImageLoader('%s');", id));
            }
        });
    }

    @JavascriptInterface
    public int getShowMenu() {
        return mSettings.showMenu();
    }

    @JavascriptInterface
    public boolean isLandscape() {
        return mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }


    public void scrollToBottom() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mWebView.loadUrl("javascript:scrollToBottom();");
            }
        });
    }

    public void scrollToTop() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mWebView.loadUrl("javascript:scrollToTop();");
            }
        });
    }

    public void configurationChange(final boolean landscape) {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mWebView.loadUrl(String.format("javascript:changeConfiguration(%d);", landscape ? 1 : 0));
            }
        });
    }

}
