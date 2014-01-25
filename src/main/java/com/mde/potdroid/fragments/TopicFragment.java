package com.mde.potdroid.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.view.*;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mde.potdroid.EditorActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.*;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;
import com.mde.potdroid.parsers.TopicParser;
import com.mde.potdroid.views.IconDrawable;
import com.mde.potdroid.views.PostActionsDialog;
import org.apache.http.Header;

import java.util.LinkedList;

/**
 * This Fragment displays a Topic in a WebView. Since the WebView has a memory leak,
 * we have to work around that by adding and deleting it in onPause and onResume. This sucks,
 * I know, but LOLANDROID!
 */
public class TopicFragment extends PaginateFragment implements LoaderManager.LoaderCallbacks<Topic> {

    public static final String ARG_TOPIC_ID = "thread_id";
    public static final String ARG_POST_ID = "post_id";
    public static final String ARG_PAGE = "page";

    // the topic Object
    private Topic mTopic;

    // the webview which is attached to the WebContainer
    private WebView mWebView;

    // the webcontainer
    private FrameLayout mWebContainer;

    // we need to invoke some functions on this one outside of the
    // webview initialization, so keep a reference here.
    private TopicJSInterface mJsInterface;

    // singleton and state indicator for the Kitkat bug workaround
    public static LinkedList<TopicFragment> mWebViewHolder = new LinkedList<TopicFragment>();
    public boolean mDestroyed;


    /**
     * Create a new instance of TopicFragment and set the arguments
     *
     * @param thread_id the thread id of the topic
     * @param page      the displayed page of the topic
     * @param post_id   the post id of the current post
     * @return TopicFragment instance
     */
    public static TopicFragment newInstance(int thread_id, int page, int post_id) {
        TopicFragment f = new TopicFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt(ARG_TOPIC_ID, thread_id);
        args.putInt(ARG_PAGE, page);
        args.putInt(ARG_POST_ID, post_id);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        if (mTopic == null)
            startLoader(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_topic, container, false);

        mWebContainer = (FrameLayout) v.findViewById(R.id.web_container);

        setupWebView();

        // this is a hotfix for the Kitkat Webview memory leak. We destroy the webview
        // of some former TopicFragment, which will be restored on onResume. .
        if (Utils.isKitkat()) {
            mWebViewHolder.add(this);
            if (mWebViewHolder.size() > 3) {
                TopicFragment fragment = mWebViewHolder.removeFirst();
                if (fragment != null)
                    fragment.destroyWebView();
            }
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_topic, menu);

        menu.findItem(R.id.new_message).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_pencil));
        menu.findItem(R.id.load_images).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_picture));
        menu.findItem(R.id.unveil).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_eye_open));
        menu.findItem(R.id.last_own_post).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_search));

        if (!Utils.isLoggedIn()) {
            menu.setGroupVisible(R.id.loggedout_topic, false);
        } else {
            menu.setGroupVisible(R.id.loggedout_topic, true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_message:
                replyPost();
                return true;
            case R.id.unveil:
                mJsInterface.unveil();
                return true;
            case R.id.last_own_post:
                mJsInterface.scrollToLastOwnPost();
                return true;
            case R.id.load_images:
                mJsInterface.loadAllImages();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Set up the webview programmatically, to workaround the kitkat memory leak.
     */
    public void setupWebView() {

        mDestroyed = false;

        // create a webview
        mWebView = new WebView(getBaseActivity());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.setBackgroundColor(0x00000000);

        mJsInterface = new TopicJSInterface(mWebView, getBaseActivity(), this);
        mJsInterface.registerScroll(getArguments().getInt(ARG_POST_ID, 0));

        // 2.3 has a bug that prevents adding JS interfaces.
        // see here: http://code.google.com/p/android/issues/detail?id=12987
        if (!Utils.isGingerbread()) {
            mWebView.addJavascriptInterface(mJsInterface, "api");
        } else {
            showInfo(R.string.msg_error_gb);
        }

        mWebContainer.addView(mWebView);

        registerForContextMenu(mWebView);

        if (mTopic != null) {
            mWebView.loadDataWithBaseURL("file:///android_asset/",
                    mTopic.getHtmlCache(), "text/html", Network.ENCODING_UTF8, null);
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

        if (mWebView != null && !mDestroyed) {

            mWebView.destroy();
            mWebView = null;

            mWebContainer.removeAllViews();

            mDestroyed = true;
        }
    }

    @Override
    public Loader<Topic> onCreateLoader(int id, Bundle args) {
        int page = getArguments().getInt(ARG_PAGE, 1);
        int tid = getArguments().getInt(ARG_TOPIC_ID, 0);
        int pid = getArguments().getInt(ARG_POST_ID, 0);

        showLoadingAnimation();

        return new AsyncContentLoader(getBaseActivity(), page, tid, pid);
    }

    @Override
    public void onLoadFinished(Loader<Topic> loader, Topic data) {
        hideLoadingAnimation();

        if (mWebView != null && data != null) {
            // update the topic data
            mTopic = data;

            // Refresh the bookmarks after the topic loaded
            getBaseActivity().getLeftSidebarFragment().refreshBookmarks();

            // update html
            mWebView.loadDataWithBaseURL("file:///android_asset/",
                    mTopic.getHtmlCache(), "text/html", Network.ENCODING_UTF8, null);

            // set title and subtitle of the ActionBar and reload the OptionsMenu
            Spanned subtitleText = Html.fromHtml(getString(R.string.subtitle_paginate,
                    mTopic.getPage(), mTopic.getNumberOfPages()));

            getBaseActivity().supportInvalidateOptionsMenu();
            getActionbar().setTitle(mTopic.getTitle());
            getActionbar().setSubtitle(subtitleText);

        } else {
            showError(getString(R.string.msg_loading_error));
        }
    }

    public Topic getTopic() {
        return mTopic;
    }

    @Override
    public void onLoaderReset(Loader<Topic> loader) {
        hideLoadingAnimation();
    }

    public void goToNextPage() {
        // whether there is a next page was checked in onCreateOptionsMenu
        getArguments().putInt(ARG_PAGE, mTopic.getPage() + 1);
        getArguments().remove(ARG_POST_ID);
        mJsInterface.registerScroll(0);
        restartLoader(this);
    }

    public void goToPrevPage() {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt(ARG_PAGE, mTopic.getPage() - 1);
        getArguments().remove(ARG_POST_ID);
        mJsInterface.registerScroll(0);
        restartLoader(this);
    }

    public void goToFirstPage() {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt(ARG_PAGE, 1);
        getArguments().remove(ARG_POST_ID);
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

    @Override
    public void refreshPage() {
        restartLoader(this);
    }

    @Override
    public void goToLastPage() {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt(ARG_PAGE, mTopic.getNumberOfPages());
        getArguments().remove(ARG_POST_ID);
        mJsInterface.registerScroll(0);
        restartLoader(this);
    }

    public void goToLastPost(int pid) {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt(ARG_POST_ID, pid);
        getArguments().remove(ARG_PAGE);
        mJsInterface.registerScroll(pid);
        restartLoader(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo
            menuInfo) {
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

    /**
     * Shows a dialog with options for a single post (answer, quote, etc.)
     *
     * @param post_id the PID
     */
    public void showPostDialog(int post_id) {
        PostActionsDialog menu = PostActionsDialog.getInstance(post_id);
        menu.setTargetFragment(this, 0);
        menu.show(getBaseActivity().getSupportFragmentManager(), PostActionsDialog.TAG);
    }

    static class AsyncContentLoader extends AsyncHttpLoader<Topic> {

        AsyncContentLoader(Context cx, int page, int thread_id, int post_id) {
            super(cx, TopicParser.getUrl(thread_id, page, post_id));
        }

        @Override
        public Topic processNetworkResponse(String response) {
            try {
                TopicParser parser = new TopicParser();
                Topic t = parser.parse(response);

                TopicBuilder b = new TopicBuilder(getContext());
                t.setHtmlCache(b.parse(t));

                return t;
            } catch (Exception e) {
                Utils.printException(e);
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

    /**
     * Open the form and insert the quoted post
     *
     * @param id The post id to quote
     */
    public void quotePost(final int id) {
        Post p = mTopic.getPostById(id);

        if (mTopic.isClosed())
            showInfo(R.string.msg_topic_closed);

        String text = String.format(getString(R.string.quote),
                mTopic.getId(), p.getId(), p.getAuthor().getNick(), p.getText());

        Intent intent = new Intent(getBaseActivity(), EditorActivity.class);
        intent.putExtra(EditorFragment.ARG_TOKEN, mTopic.getNewreplytoken());
        intent.putExtra(EditorFragment.ARG_MODE, EditorFragment.MODE_REPLY);
        intent.putExtra(EditorFragment.ARG_TOPIC_ID, mTopic.getId());
        intent.putExtra(EditorFragment.ARG_TEXT, text);

        startActivityForResult(intent, EditorFragment.MODE_REPLY);
    }

    /**
     * Open the form for reply
     */
    public void replyPost() {
        if (mTopic.isClosed())
            showInfo(R.string.msg_topic_closed);

        Intent intent = new Intent(getBaseActivity(), EditorActivity.class);
        intent.putExtra(EditorFragment.ARG_MODE, EditorFragment.MODE_REPLY);
        intent.putExtra(EditorFragment.ARG_TOPIC_ID, mTopic.getId());
        intent.putExtra(EditorFragment.ARG_TOKEN, mTopic.getNewreplytoken());

        startActivityForResult(intent, EditorFragment.MODE_REPLY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == EditorFragment.MODE_REPLY) {
            if (resultCode == Activity.RESULT_OK) {
                goToLastPost(data.getExtras().getInt(ARG_POST_ID));
                showSuccess(R.string.msg_answer_created);
            }
        } else if (requestCode == EditorFragment.MODE_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                refreshPage();
                showSuccess(R.string.msg_post_edited);
            }
        }
    }

    /**
     * Open the form and insert the content of a post to edit it.
     *
     * @param id the PID
     */
    public void editPost(final int id) {
        Post p = mTopic.getPostById(id);

        SettingsWrapper settings = new SettingsWrapper(getBaseActivity());

        if (p.getAuthor().getId() == settings.getUserId()) {

            Intent intent = new Intent(getBaseActivity(), EditorActivity.class);
            intent.putExtra(EditorFragment.ARG_TOKEN, p.getEdittoken());
            intent.putExtra(EditorFragment.ARG_MODE, EditorFragment.MODE_EDIT);
            intent.putExtra(EditorFragment.ARG_TOPIC_ID, mTopic.getId());
            intent.putExtra(EditorFragment.ARG_POST_ID, p.getId());
            intent.putExtra(EditorFragment.ARG_TITLE, p.getTitle());
            intent.putExtra(EditorFragment.ARG_TEXT, p.getText());
            intent.putExtra(EditorFragment.ARG_ICON, p.getIconId());

            startActivityForResult(intent, EditorFragment.MODE_EDIT);
        } else {
            showError(R.string.msg_post_notyours);
        }
    }

    /**
     * Add a bookmark to a post
     *
     * @param id the PID
     * @param d  the Dialog to close, if successful and Dialog exists
     */
    public void bookmarkPost(final int id, final Dialog d) {
        Post p = mTopic.getPostById(id);

        final String url = Network.getAsyncUrl(
                String.format("set-bookmark.php?PID=%d&token=%s", p.getId(), p.getBookmarktoken()));

        Network network = new Network(getActivity());
        network.get(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                showSuccess(R.string.msg_bookmark_added);
                if (d != null)
                    d.cancel();
            }
        });
    }

    /**
     * Open the post in a browser
     *
     * @param id the PID
     */
    public void linkPost(final int id) {
        Post p = mTopic.getPostById(id);

        String url = Network.getAbsoluteUrl(
                String.format("thread.php?PID=%d&TID=%d#reply_%d", p.getId(), mTopic.getId(), p.getId()));

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}
