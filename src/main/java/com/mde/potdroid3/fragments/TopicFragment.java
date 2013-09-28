package com.mde.potdroid3.fragments;

import android.app.Activity;
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

public class TopicFragment extends PaginateFragment
        implements LoaderManager.LoaderCallbacks<Topic> {

    private Topic mTopic;
    private WebView mWebView;
    private TopicJSInterface mJsInterface;
    private OnContentLoadedListener mCallback;

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnContentLoadedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnContentLoadedListener");
        }
    }

    @Override
    public Loader<Topic> onCreateLoader(int id, Bundle args) {
        int page = getArguments().getInt("page", 1);
        int tid = getArguments().getInt("thread_id", 0);
        int pid = getArguments().getInt("post_id", 0);
        AsyncContentLoader l = new AsyncContentLoader(getActivity(), mNetwork, page, tid, pid);
        showLoadingAnimation();

        return l;
    }

    @Override
    public void onLoadFinished(Loader<Topic> loader, Topic data) {
        hideLoadingAnimation();

        if(data != null) {

            // update the topic data
            mTopic = data;

            // update html
            mWebView.loadDataWithBaseURL("file:///android_asset/", mTopic.getHtmlCache(),
                    "text/html", "UTF-8", null);

            // set title and subtitle of the ActionBar and reload the OptionsMenu
            Spanned subtitleText = Html.fromHtml("Seite <b>"
                    + mTopic.getPage()
                    + "</b> von <b>"
                    + mTopic.getNumberOfPages()
                    + "</b>");

            getActivity().invalidateOptionsMenu();
            getActivity().getActionBar().setTitle(mTopic.getTitle());
            getActivity().getActionBar().setSubtitle(subtitleText);

            // call the onLoaded function
            mCallback.onContentLoaded();

        } else {
            showError("Fehler beim Laden der Daten.");
        }
    }

    @Override
    public void onLoaderReset(Loader<Topic> loader) {
        hideLoadingAnimation();
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

    @Override
    public boolean isLastPage() {
        return mTopic == null || mTopic.isLastPage();
    }

    @Override
    public boolean isFirstPage() {
        return mTopic == null || mTopic.getPage() == 1;
    }

    public void goToLastPage() {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt("page", mTopic.getNumberOfPages());
        getArguments().remove("post_id");
        mJsInterface.registerScroll(0);
        restartLoader(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        // long touch is only resolved if it happened on an image. If so, we
        // offer to open the image with an image application
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
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.nav_refresh:
                restartLoader(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    protected int getLayout() {
        return R.layout.layout_topic;
    }

    /**
     * The Activity must implement this listener. It will get informed
     * when the topic is finished loading, so it can trigger the refresh
     * of the BookmarksList in the sidebar.
     */
    public interface OnContentLoadedListener {
        public void onContentLoaded();
    }

    /**
     * Takes care of loading the topic XML asynchroneously.
     */
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
