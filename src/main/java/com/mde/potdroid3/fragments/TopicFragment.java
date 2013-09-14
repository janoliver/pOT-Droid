package com.mde.potdroid3.fragments;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.mde.potdroid3.ForumActivity;
import com.mde.potdroid3.R;
import com.mde.potdroid3.SettingsActivity;
import com.mde.potdroid3.helpers.Network;
import com.mde.potdroid3.helpers.TopicBuilder;
import com.mde.potdroid3.helpers.TopicJSInterface;
import com.mde.potdroid3.models.Topic;
import com.mde.potdroid3.parsers.TopicParser;

import java.io.InputStream;

public class TopicFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<TopicFragment.TopicHtmlContainer> {

    private TopicHtmlContainer mTopicContainer;
    private WebView mWebView;
    private TopicJSInterface mJsInterface;

    public static TopicFragment newInstance(int thread_id, int page, int post_id) {
        TopicFragment f = new TopicFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("thread_id", thread_id);
        args.putInt("page", page);
        args.putInt("post_id", post_id);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mWebView = (WebView)getView().findViewById(R.id.topic_webview);
        mJsInterface = new TopicJSInterface(mWebView, getActivity());

        registerForContextMenu(mWebView);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.addJavascriptInterface(mJsInterface, "api");
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.loadData("", "text/html", "utf-8");
        mWebView.setBackgroundColor(0x00000000);

        startLoader(this);

    }

    @Override
    public Loader<TopicHtmlContainer> onCreateLoader(int id, Bundle args) {
        int page = getArguments().getInt("page", 1);
        int tid = getArguments().getInt("thread_id", 0);
        int pid = getArguments().getInt("post_id", 0);
        AsyncContentLoader l = new AsyncContentLoader(getActivity(), mNetwork, page, tid, pid);
        showLoader();
        return l;
    }

    @Override
    public void onLoadFinished(Loader<TopicHtmlContainer> loader, TopicHtmlContainer data) {
        hideLoader();

        if(data != null) {
            mTopicContainer = data;
            mWebView.loadDataWithBaseURL("file:///android_asset/",
                    mTopicContainer.getHtml(), "text/html", "UTF-8", null);
        } else {
            showError("Fehler beim Laden der Daten.");
        }
    }

    @Override
    public void onLoaderReset(Loader<TopicHtmlContainer> loader) {
        hideLoader();
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        WebView.HitTestResult hitTestResult = mWebView.getHitTestResult();
        if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
            hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(hitTestResult.getExtra()), "image/*");
            startActivity(intent);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_topic, menu);
        //menu.setGroupVisible(R.id.loggedin, mObjectManager.isLoggedIn());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                restartLoader(this);
                return true;
            case R.id.bookmarks:
                return true;
            case R.id.preferences:
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.forumact:
                intent = new Intent(getActivity(), ForumActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected int getLayout() {
        return R.layout.layout_topic;
    }

    /**
     * Container class so the html can be parsed by the loader. On orientation change,
     * we therefore do not have to wait for the TopicParser.
     */
    static class TopicHtmlContainer {
        private Topic mTopic;
        private String mHtml;

        public TopicHtmlContainer(Topic topic, String html) {
            mTopic = topic;
            mHtml = html;
        }

        public Topic getTopic() {
            return mTopic;
        }

        public String getHtml() {
            return mHtml;
        }
    }

    static class AsyncContentLoader extends AsyncTaskLoader<TopicHtmlContainer> {
        private Network mNetwork;
        private Integer mPage;
        private Integer mThreadId;
        private Integer mPostId;
        private Context mContext;

        AsyncContentLoader(Context cx, Network network, int page, int thread_id, int post_id) {
            super(cx);
            mContext = cx;
            mNetwork = network;
            mPage = page;
            mThreadId = thread_id;
            mPostId = post_id;
        }

        @Override
        public TopicHtmlContainer loadInBackground() {
            try {
                InputStream xml = mNetwork.getDocument(Topic.Xml.getUrl(mThreadId, mPage, mPostId));
                TopicParser parser = new TopicParser();

                Topic t = parser.parse(xml);
                TopicBuilder b = new TopicBuilder(mContext);

                return new TopicHtmlContainer(t, b.parse(t));

            } catch (Exception e) {
                return null;
            }
        }

    }


}
