package com.mde.potdroid.helpers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.mde.potdroid.fragments.MessageFragment;
import com.mde.potdroid.fragments.TopicFragment;

/**
 * The Javascript interface for the Topic Webviews, extended from the Bender js interface.
 * Provides some more API functions.
 */
public class MessageJSInterface extends BenderJSInterface
{

    // a reference to the topic fragment
    private MessageFragment mMessageFragment;

    public MessageJSInterface(WebView wv, Activity cx, MessageFragment fragment) {
        super(wv, cx);

        mMessageFragment = fragment;
    }

    /**
     * Open the reply form
     */
    @JavascriptInterface
    public void replyPost() {
        mMessageFragment.getBaseActivity().runOnUiThread(new Runnable() {
            public void run() {
                mMessageFragment.replyPost();
            }
        });
    }

}
