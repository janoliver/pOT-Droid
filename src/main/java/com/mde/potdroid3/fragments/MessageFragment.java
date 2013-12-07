package com.mde.potdroid3.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.mde.potdroid3.BaseActivity;
import com.mde.potdroid3.R;
import com.mde.potdroid3.helpers.AsyncHttpLoader;
import com.mde.potdroid3.helpers.BenderJSInterface;
import com.mde.potdroid3.helpers.MessageBuilder;
import com.mde.potdroid3.models.Message;
import com.mde.potdroid3.parsers.MessageParser;

public class MessageFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<Message> {

    private Message mMessage;
    private BaseActivity mActivity;
    private WebView mWebView;
    private BenderJSInterface mJsInterface;

    public static MessageFragment newInstance(int message_id) {
        MessageFragment f = new MessageFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("message_id", message_id);
        f.setArguments(args);

        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = super.onCreateView(inflater, container, saved);
        mWebView = (WebView)v.findViewById(R.id.message_webview);
        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = (BaseActivity) getSupportActivity();

        mJsInterface = new BenderJSInterface(mWebView, getSupportActivity());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.addJavascriptInterface(mJsInterface, "api");
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.loadData("", "text/html", "utf-8");
        mWebView.setBackgroundColor(0x00000000);

        getActionbar().setTitle("Lade Nachricht");

        // load the content
        startLoader(this);

    }

    @Override
    public Loader<Message> onCreateLoader(int id, Bundle args) {
        int mid = getArguments().getInt("message_id", 0);
        AsyncContentLoader l = new AsyncContentLoader(getSupportActivity(), mid);
        showLoadingAnimation();

        return l;
    }

    @Override
    public void onLoadFinished(Loader<Message> loader, Message data) {
        hideLoadingAnimation();

        if(data != null) {

            // update the topic data
            mMessage = data;

            mWebView.loadDataWithBaseURL("file:///android_asset/", mMessage.getHtmlCache(),
                    "text/html", "UTF-8", null);

            getActionbar().setTitle(mMessage.getTitle());
            getActionbar().setSubtitle(Html.fromHtml((mMessage.isOutgoing() ? "an" : "von")
                + " <b>" + mMessage.getFrom().getNick() + "</b>"));

            // populate right sidebar
            mActivity.getRightSidebar().setIsMessage(mMessage);

        } else {
            showError("Fehler beim Laden der Daten.");
        }
    }

    @Override
    public void onLoaderReset(Loader<Message> loader) {
        hideLoadingAnimation();
    }

    protected int getLayout() {
        return R.layout.layout_message;
    }

    static class AsyncContentLoader extends AsyncHttpLoader<Message> {
        private Integer mMessageId;

        AsyncContentLoader(Context cx, Integer message_id) {
            super(cx, Message.Html.getUrl(message_id), GET, null, "ISO-8859-15");

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
