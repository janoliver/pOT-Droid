package com.mde.potdroid.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.*;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import com.mde.potdroid.EditorActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.*;
import com.mde.potdroid.models.Message;
import com.mde.potdroid.parsers.MessageParser;
import com.mde.potdroid.views.IconDrawable;
import org.apache.http.Header;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

/**
 * This Fragment displays a PM Message in a WebView. Since the WebView has a memory leak,
 * we have to work around that by adding and deleting it in onPause and onResume. This sucks,
 * I know, but LOLANDROID!
 */
public class MessageFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<Message> {

    // the tags of the fragment arguments
    public static final String ARG_ID = "message_id";
    // singleton and state indicator for the Kitkat bug workaround
    public static LinkedList<MessageFragment> mWebViewHolder = new LinkedList<MessageFragment>();
    public boolean mDestroyed;
    // the message object
    Message mMessage;
    // the webview
    private WebView mWebView;
    private FrameLayout mWebContainer;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    /**
     * Destroys and detaches the webview.
     */
    public void destroyWebView() {

        if (mWebView != null && !mDestroyed) {

            mWebView.destroy();
            mWebView = null;

            mWebContainer.removeAllViews();

            mDestroyed = true;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActionbar().setTitle(R.string.msg_message_loading);

        if (mMessage == null)
            startLoader(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_message, menu);

        MenuItem replyMessage = menu.findItem(R.id.reply);
        replyMessage.setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_reply));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // only show the paginate buttons if there are before or after the current
        if (mMessage != null && mMessage.isSystem()) {
            menu.removeItem(R.id.reply);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.reply:

                Intent intent = new Intent(getBaseActivity(), EditorActivity.class);
                intent.putExtra(EditorFragment.ARG_MODE, EditorFragment.MODE_MESSAGE);
                intent.putExtra(EditorFragment.ARG_RCPT, mMessage.getFrom().getNick());

                String toQuote = Html.fromHtml(mMessage.getText()
                    .replaceAll("<img src='message-icons/[^']+' alt='([^']+)' class='smiley'>", "$1")).toString();

                // read in the message string as HTML, so <br> is converted into line
                // breaks and so on.
                BufferedReader bufReader = new BufferedReader(new StringReader(toQuote));

                // we need to build the message text using a string builder, so
                // we can prefix each line with a > (for quotes)
                String line;
                StringBuilder content = new StringBuilder();

                String ds = new SimpleDateFormat(getString(R.string.default_time_format)).format(mMessage.getDate());
                String quote_line = "> %s \n";
                content.append(String.format(getString(R.string.message_header),
                        mMessage.getFrom().getNick(), ds));
                try {
                    while ((line = bufReader.readLine()) != null)
                        content.append(String.format(quote_line, line));
                } catch (IOException e) {
                    // this will never occur.
                }

                intent.putExtra(EditorFragment.ARG_TITLE, String.format("%s%s",
                        mMessage.isReply() ? "Re: " : "", mMessage.getTitle()));
                intent.putExtra(EditorFragment.ARG_TEXT, content.toString());

                startActivityForResult(intent, EditorFragment.MODE_MESSAGE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == EditorFragment.MODE_MESSAGE) {
            if (resultCode == Activity.RESULT_OK) {
                showSuccess(R.string.msg_send_successful);
            }
        }
    }

    @Override
    public Loader<Message> onCreateLoader(int id, Bundle args) {
        int mid = getArguments().getInt(ARG_ID, 0);
        AsyncContentLoader l = new AsyncContentLoader(getBaseActivity(), mid);
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
            Spanned subtitle;
            if(mMessage.isSystem()) {
                subtitle = Html.fromHtml("<b>Systemnachricht</b>");
            } else {

                subtitle = Html.fromHtml(String.format(getString(R.string.subtitle_message),
                        mMessage.isOutgoing() ? "an" : "von", mMessage.getFrom().getNick()));
            }
            getActionbar().setTitle(Html.fromHtml(mMessage.getTitle()));
            getActionbar().setSubtitle(subtitle);

            getActivity().supportInvalidateOptionsMenu();

        } else {
            showError(getString(R.string.msg_loading_error));
        }
    }

    @Override
    public void onLoaderReset(Loader<Message> loader) {
        hideLoadingAnimation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_message, container, false);
        mWebContainer = (FrameLayout) v.findViewById(R.id.web_container);

        setupWebView();

        // this is a hotfix for the Kitkat Webview memory leak. We destroy the webview
        // of some former MessageFragment, which will be restored on onResume. .
        if (Utils.isKitkat()) {
            mWebViewHolder.add(this);
            if (mWebViewHolder.size() > 3) {
                MessageFragment fragment = mWebViewHolder.removeFirst();
                if (fragment != null)
                    fragment.destroyWebView();
            }
        }

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.main_toolbar);
        getBaseActivity().setSupportActionBar(toolbar);
        getBaseActivity().setUpActionBar();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mDestroyed && Utils.isKitkat()) {
            setupWebView();
        }
    }

    public void setupWebView() {

        mDestroyed = false;

        mWebView = new WebView(getBaseActivity());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.loadData("", "text/html", "utf-8");
        mWebView.setBackgroundColor(0x00000000);

        BenderJSInterface mJsInterface = new BenderJSInterface(mWebView, getBaseActivity());

        mWebView.addJavascriptInterface(mJsInterface, "api");

        mWebContainer.addView(mWebView);

        if (mMessage != null) {
            mWebView.loadDataWithBaseURL("file:///android_asset/",
                    mMessage.getHtmlCache(), "text/html", Network.ENCODING_UTF8, null);
        } else {
            mWebView.loadData("", "text/html", Network.ENCODING_UTF8);
        }
    }

    static class AsyncContentLoader extends AsyncHttpLoader<Message> {

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
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onNetworkFailure(int statusCode, Header[] headers,
                                        String responseBody, Throwable error) {

            Utils.printException(error);
            deliverResult(null);
        }
    }

}
