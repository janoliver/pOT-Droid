package com.mde.potdroid.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.mde.potdroid.BaseActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.helpers.BenderJSInterface;
import com.mde.potdroid.helpers.MessageBuilder;
import com.mde.potdroid.helpers.Network;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.Message;
import com.mde.potdroid.parsers.MessageParser;

import java.util.LinkedList;

/**
 * This Fragment displays a PM Message in a WebView. Since the WebView has a memory leak,
 * we have to work around that by adding and deleting it in onPause and onResume. This sucks,
 * I know, but LOLANDROID!
 */
public class MessageFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Message>
{

    // the message object
    Message mMessage;

    // the tags of the fragment arguments
    public static final String ARG_ID = "message_id";

    // we store a reference to the BaseActivity for API purposes
    private BaseActivity mActivity;

    // the webview
    private WebView mWebView;
    private FrameLayout mWebContainer;

    // singleton and state indicator for the Kitkat bug workaround
    public static LinkedList<MessageFragment> mWebViewHolder = new LinkedList<MessageFragment>();
    public boolean mDestroyed;

    /**
     * Returns a new instance of the MessageFragment and sets the ID argument
     *
     * @param message_id the ID of the PM message
     * @return The MessageFragment
     */
    public static MessageFragment newInstance(int message_id) {
        MessageFragment f = new MessageFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt(ARG_ID, message_id);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_message, container, false);
        mWebContainer = (FrameLayout) v.findViewById(R.id.web_container);
        mActivity = (BaseActivity) getSupportActivity();

        setupWebView();

        // this is a hotfix for the Kitkat Webview memory leak. We destroy the webview
        // of some former TopicFragment, which will be restored on onResume. .
        if(Utils.isKitkat()) {
            mWebViewHolder.add(this);
            if(mWebViewHolder.size() > 3)
                mWebViewHolder.removeFirst().destroyWebView();
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActionbar().setTitle(R.string.loading_message);

        if (mMessage == null)
            startLoader(this);
    }

    public void setupWebView() {

        mDestroyed = false;

        mWebView = new WebView(mActivity);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.loadData("", "text/html", "utf-8");
        mWebView.setBackgroundColor(0x00000000);

        BenderJSInterface mJsInterface = new BenderJSInterface(mWebView, getSupportActivity());

        // 2.3 has a bug that prevents adding JS interfaces.
        // see here: http://code.google.com/p/android/issues/detail?id=12987
        if (!Utils.isGingerbread()) {
            mWebView.addJavascriptInterface(mJsInterface, "api");
        } else {
            Utils.toast(mActivity, getString(R.string.error_gingerbread_js));
        }

        mWebContainer.addView(mWebView);

        if (mMessage != null) {
            mWebView.loadDataWithBaseURL("file:///android_asset/",
                    mMessage.getHtmlCache(), "text/html", Network.ENCODING_UTF8, null);
        } else {
            mWebView.loadData("", "text/html", Network.ENCODING_UTF8);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mDestroyed && Utils.isKitkat()) {
            setupWebView();
        }
    }

    /**
     * Destroys and detaches the webview.
     */
    public void destroyWebView() {

        mWebView.destroy();
        mWebView = null;

        mWebContainer.removeAllViews();

        mDestroyed = true;
    }

    @Override
    public Loader<Message> onCreateLoader(int id, Bundle args) {
        int mid = getArguments().getInt(ARG_ID, 0);
        AsyncContentLoader l = new AsyncContentLoader(getSupportActivity(), mid);
        showLoadingAnimation();

        return l;
    }

    @Override
    public void onLoadFinished(Loader<Message> loader, Message data) {
        hideLoadingAnimation();

        if (data != null) {

            // update the topic data
            mMessage = data;

            mWebView.loadDataWithBaseURL("file:///android_asset/",
                    mMessage.getHtmlCache(), "text/html", Network.ENCODING_UTF8, null);

            // generate and set title and subtitle
            Spanned subtitle = Html.fromHtml(String.format(getString(R.string.message_subtitle),
                    mMessage.isOutgoing() ? "an" : "von", mMessage.getFrom().getNick()));
            getActionbar().setTitle(mMessage.getTitle());
            getActionbar().setSubtitle(subtitle);

            // populate right sidebar
            mActivity.getRightSidebar().setIsMessage(mMessage);

        } else {
            showError(getString(R.string.loading_error));
        }
    }

    @Override
    public void onLoaderReset(Loader<Message> loader) {
        hideLoadingAnimation();
    }

    static class AsyncContentLoader extends AsyncHttpLoader<Message>
    {

        private Integer mMessageId;

        AsyncContentLoader(Context cx, Integer message_id) {
            super(cx, MessageParser.getUrl(message_id), GET, null, Network.ENCODING_ISO);

            mMessageId = message_id;
        }

        @Override
        public Message processNetworkResponse(String response) {
            try {
                MessageParser parser = new MessageParser();
                Message m = parser.parse(response, mMessageId);
                MessageBuilder b = new MessageBuilder(getContext());
                m.setHtmlCache(b.parse(m));
                return m;
            } catch (Exception e) {
                return null;
            }
        }
    }

}
