package com.mde.potdroid3.fragments;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.*;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.mde.potdroid3.R;
import com.mde.potdroid3.helpers.Network;
import com.mde.potdroid3.helpers.TopicBuilder;
import com.mde.potdroid3.helpers.TopicJSInterface;
import com.mde.potdroid3.models.Topic;
import com.mde.potdroid3.parsers.TopicParser;

import java.io.InputStream;

public class TopicFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<Topic> {

    private Topic mTopic;
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

        // if there is a post_id from the bookmarks call, we set it as the currently
        // visible post.
        mJsInterface.registerScroll(getArguments().getInt("post_id", 0));

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
    public Loader<Topic> onCreateLoader(int id, Bundle args) {
        int page = getArguments().getInt("page", 1);
        int tid = getArguments().getInt("thread_id", 0);
        int pid = getArguments().getInt("post_id", 0);
        AsyncContentLoader l = new AsyncContentLoader(getActivity(), mNetwork, page, tid, pid);
        showLoader();
        return l;
    }

    @Override
    public void onLoadFinished(Loader<Topic> loader, Topic data) {
        hideLoader();

        // hide some buttons

        if(data != null) {
            mTopic = data;
            mWebView.loadDataWithBaseURL("file:///android_asset/",
                    mTopic.getHtmlCache(), "text/html", "UTF-8", null);

            getActivity().invalidateOptionsMenu();

            Spanned subtitleText = Html.fromHtml("Seite <b>" + mTopic.getPage()
                    + "</b> von <b>" + mTopic.getNumberOfPages() + "</b>");

            getActivity().getActionBar().setTitle(mTopic.getTitle());
            getActivity().getActionBar().setSubtitle(subtitleText);
        } else {
            showError("Fehler beim Laden der Daten.");
        }
    }

    @Override
    public void onLoaderReset(Loader<Topic> loader) {
        hideLoader();
    }

    public void goToNextPage() {
        // whether there is a next page was checked in onCreateOptionsMenu
        getArguments().putInt("page", mTopic.getPage()+1);
        getArguments().remove("post_id");
        mJsInterface.registerScroll(0);
        restartLoader(this);
    }

    public void goToPrevPage() {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt("page", mTopic.getPage()-1);
        getArguments().remove("post_id");
        mJsInterface.registerScroll(0);
        restartLoader(this);
    }

    public void goToFirstPage() {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt("page", 1);
        getArguments().remove("post_id");
        mJsInterface.registerScroll(0);
        restartLoader(this);
    }

    public void goToLastPage() {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt("page", mTopic.getNumberOfPages());
        getArguments().remove("post_id");
        mJsInterface.registerScroll(0);
        restartLoader(this);
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

        if(mTopic != null && !mTopic.isLastPage()) {
            menu.findItem(R.id.nav_lastpage).setEnabled(true);
            menu.findItem(R.id.nav_next).setEnabled(true);
        }

        if(mTopic != null && mTopic.getPage() > 1) {
            menu.findItem(R.id.nav_firstpage).setEnabled(true);
            menu.findItem(R.id.nav_previous).setEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.nav_refresh:
                restartLoader(this);
                return true;
            case R.id.nav_next:
                goToNextPage();
                return true;
            case R.id.nav_previous:
                goToPrevPage();
                return true;
            case R.id.nav_firstpage:
                goToFirstPage();
                return true;
            case R.id.nav_lastpage:
                goToLastPage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected int getLayout() {
        return R.layout.layout_topic;
    }

    static class AsyncContentLoader extends AsyncTaskLoader<Topic> {
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
        public Topic loadInBackground() {
            try {
                InputStream xml = mNetwork.getDocument(Topic.Xml.getUrl(mThreadId, mPage, mPostId));
                TopicParser parser = new TopicParser();

                Topic t = parser.parse(xml);
                TopicBuilder b = new TopicBuilder(mContext);
                t.setHtmlCache(b.parse(t));

                return t;

            } catch (Exception e) {
                return null;
            }
        }

    }


}
