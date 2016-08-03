package com.mde.potdroid.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.*;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.mde.potdroid.EditorActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.*;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;
import com.mde.potdroid.parsers.TopicParser;
import com.mde.potdroid.views.*;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.apache.http.Header;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * This Fragment displays a Topic in a WebView. Since the WebView has a memory leak,
 * we have to work around that by adding and deleting it in onPause and onResume. This sucks,
 * I know, but LOLANDROID!
 */
public class TopicFragment extends PaginateFragment implements
        LoaderManager.LoaderCallbacks<Topic> {

    public static final String ARG_TOPIC_ID = "thread_id";
    public static final String ARG_POST_ID = "post_id";
    public static final String ARG_PAGE = "page";

    // the topic Object
    private Topic mTopic;
    private Topic mNextCache;

    // the webview which is attached to the WebContainer
    private ObservableWebView mWebView;
    private boolean mUserInteraction;
    private int mOldScroll;
    private FloatingActionButton mFab;

    // we need to invoke some functions on this one outside of the
    // webview initialization, so keep a reference here.
    private TopicJSInterface mJsInterface;
    private WebViewScrollCallbacks mWebViewScrollCallbacks = new WebViewScrollCallbacks();

    // images that appear
    public ArrayList<String> mImagesInThread = new ArrayList<String>();


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

        setRetainInstance(true);

        setHasOptionsMenu(true);

        setupWebView();

        if (!mSettings.isFixedSidebar()) {
            getBaseActivity().setOverlayToolbars();

            ViewTreeObserver vto = getBaseActivity().getToolbar().getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getBaseActivity().getToolbar().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    //mPullToRefreshLayout.setTopMargin(getBaseActivity().getToolbar().getHeight());
                }
            });
        }

        if (mTopic == null)
            startLoader(this);

        if (!mSettings.isSwipeToRefreshTopic())
            mPullToRefreshLayout.setEnabled(false);

        if (mSettings.isBottomToolbar()) {
            getWriteButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replyPost();
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_topic, container, false);

        mFab = (FloatingActionButton) v.findViewById(R.id.fab);
        mFab.setImageDrawable(IconDrawable.getIconDrawable(getActivity(), R.string.icon_pencil));
        mFab.hide();

        mWebView = (ObservableWebView) v.findViewById(R.id.topic_webview);

        if (Utils.isLoggedIn() && mSettings.isShowFAB() && !mSettings.isBottomToolbar()) {
            mFab.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    replyPost();
                }
            });
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 11)
            mWebView.onResume();
        else
            try {
                Class.forName("android.webkit.WebView").getMethod("onResume", (Class[]) null)
                        .invoke(mWebView, (Object[]) null);
            } catch (IllegalAccessException e) {
                Utils.printException(e);
            } catch (InvocationTargetException e) {
                Utils.printException(e);
            } catch (NoSuchMethodException e) {
                Utils.printException(e);
            } catch (ClassNotFoundException e) {
                Utils.printException(e);
            }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT >= 11)
            mWebView.onPause();
        else
            try {
                Class.forName("android.webkit.WebView").getMethod("onPause", (Class[]) null)
                        .invoke(mWebView, (Object[]) null);
            } catch (IllegalAccessException e) {
                Utils.printException(e);
            } catch (InvocationTargetException e) {
                Utils.printException(e);
            } catch (NoSuchMethodException e) {
                Utils.printException(e);
            } catch (ClassNotFoundException e) {
                Utils.printException(e);
            }

    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        super.onRefresh(direction);
        restartLoader(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        //ImageHandler ih = new ImageHandler(getActivity());
        //ih.clearCache();
    }

    /**
     * Set up the webview programmatically, to workaround the kitkat memory leak.
     */
    public void setupWebView() {

        if (mJsInterface == null) {
            mJsInterface = new TopicJSInterface(mWebView, getBaseActivity(), this);
            mJsInterface.registerScroll(getArguments().getInt(ARG_POST_ID, 0));
        }

        mWebView.setScrollViewCallbacks(mWebViewScrollCallbacks);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDefaultFontSize(mSettings.getDefaultFontSize());
        mWebView.setBackgroundColor(0x00000000);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setLoadWithOverviewMode(true);

        // broken on 2.3.3
        mWebView.addJavascriptInterface(mJsInterface, "api");

        registerForContextMenu(mWebView);

        mJsInterface.setWebView(mWebView);

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(i);
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getActivity().getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        displayContent();

        refreshTitleAndPagination();
    }

    public void registerScroll(int postId) {
        mJsInterface.registerScroll(postId);
    }

    public void displayContent() {
        // shouldn't be null :(
        if (mWebView == null)
            return;
        if (mTopic != null) {
            mWebView.loadDataWithBaseURL(
                    "file:///android_asset/",
                    mTopic.getHtmlCache(),
                    "text/html",
                    Network.ENCODING_UTF8,
                    null);
        } else {
            mWebView.loadData("", "text/html", Network.ENCODING_UTF8);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_topic, menu);

        menu.findItem(R.id.load_images).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_picture));
        menu.findItem(R.id.unveil).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_chevron_up));
        menu.findItem(R.id.tobottom).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_chevron_down));
        menu.findItem(R.id.topage).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_arrow_right));
        menu.findItem(R.id.last_own_post).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_search));

        if (!Utils.isLoggedIn() || mSettings.isBottomToolbar()) {
            menu.setGroupVisible(R.id.loggedout_topic, false);
            menu.findItem(R.id.new_reply).setVisible(false);
        } else {
            menu.setGroupVisible(R.id.loggedout_topic, true);
            menu.findItem(R.id.new_reply).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_pencil));
        }

        if (isLastPage())
            menu.findItem(R.id.preload_next).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.next:
                goToNextPage();
                return true;
            case R.id.new_reply:
                replyPost();
                return true;
            case R.id.unveil:
                mJsInterface.scrollToTop();
                return true;
            case R.id.tobottom:
                mJsInterface.scrollToBottom();
                return true;
            case R.id.topage:
                ChoosePageDialog d = ChoosePageDialog.getInstance(mTopic.getNumberOfPages());
                d.setTargetFragment(this, 0);
                d.show(getFragmentManager(), "pagedialog");
                return true;
            case R.id.last_own_post:
                mJsInterface.scrollToLastOwnPost();
                return true;
            case R.id.load_images:
                mJsInterface.loadAllImages();
                return true;
            case R.id.preload_next:
                startPreloadingNextPage();
                return true;
            case R.id.gallery:
                mJsInterface.showLightBox(getTopic().getMedia());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean backPressed() {
        if(mJsInterface.isLightbox()) {
            mJsInterface.closeLightbox();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Loader<Topic> onCreateLoader(int id, Bundle args) {

        int page = getArguments().getInt(ARG_PAGE, 1);
        int tid = getArguments().getInt(ARG_TOPIC_ID, 0);
        int pid = getArguments().getInt(ARG_POST_ID, 0);

        showLoadingAnimation();
        setSwipeEnabled(false);

        return new AsyncContentLoader(getBaseActivity(), args, page, tid, pid);
    }

    @Override
    public void onLoadFinished(Loader<Topic> loader, Topic data) {
        hideLoadingAnimation();

        if (data != null) {

            if (data.getIsCacheOnly()) {
                mNextCache = data;
                data.setIsCacheOnly(false);

                // display new 'next' button
                highlightNextButton();
            } else {
                mNextCache = null;

                // update the topic data
                mTopic = data;

                // Refresh the bookmarks after the topic loaded
                getBaseActivity().getLeftSidebarFragment().refreshBookmarks();

                displayContent();

                refreshTitleAndPagination();

                setSwipeEnabled(true);

                showFloatingActionButton();

            }

        } else {
            showError(getString(R.string.msg_loading_error));
        }
    }

    private void showFloatingActionButton() {
        if (Utils.isLoggedIn() && mSettings.isShowFAB() && !mSettings.isBottomToolbar())
            mFab.show();
    }

    public void refreshTitleAndPagination() {
        if (mTopic == null)
            return;

        // set title and subtitle of the ActionBar and reload the OptionsMenu
        Spanned subtitleText = Html.fromHtml(getString(R.string.subtitle_paginate,
                mTopic.getPage(), mTopic.getNumberOfPages()));

        //getBaseActivity().supportInvalidateOptionsMenu();
        getActionbar().setTitle(mTopic.getTitle());
        getActionbar().setSubtitle(subtitleText);


        enableFastScroll(new FastScrollListener() {
            @Override
            public void onUpButtonClicked() {
                //mWebView.scrollToTop();
                mJsInterface.scrollToTop();
            }

            @Override
            public void onDownButtonClicked() {
                mJsInterface.scrollToBottom();
                //mJsInterface.tobottom();
            }
        });

        setSwipeTarget(mWebView);

        if (!isLastPage())
            mPullToRefreshLayout.setEnabled(false);
        else if (mSettings.isSwipeToRefreshTopic())
            mPullToRefreshLayout.setEnabled(true);

        refreshPaginateLayout();

    }

    public Topic getTopic() {
        return mTopic;
    }

    @Override
    public void onLoaderReset(Loader<Topic> loader) {
        hideLoadingAnimation();
    }

    public void goToNextPage() {

        if (mNextCache != null) {
            mJsInterface.registerScroll(0);

            // update the topic data
            mTopic = mNextCache;
            mNextCache = null;

            // Refresh the bookmarks after the topic loaded
            getBaseActivity().getLeftSidebarFragment().refreshBookmarks();

            displayContent();

            refreshTitleAndPagination();

            setSwipeEnabled(true);

            showFloatingActionButton();
        } else {

            // whether there is a next page was checked in onCreateOptionsMenu
            getArguments().putInt(ARG_PAGE, mTopic.getPage() + 1);
            getArguments().remove(ARG_POST_ID);
            mJsInterface.registerScroll(0);
            restartLoader(this);
        }

    }

    @Override
    public void nextButtonLongClick() {
        startPreloadingNextPage();
    }

    public void startPreloadingNextPage() {
        getArguments().putInt(ARG_PAGE, mTopic.getPage() + 1);
        getArguments().remove(ARG_POST_ID);
        restartLoader(this, true);
    }

    public void goToPage(int page) {
        if (page != mTopic.getPage()) {
            getArguments().putInt(ARG_PAGE, page);
            getArguments().remove(ARG_POST_ID);
            mJsInterface.registerScroll(0);
            restartLoader(this);
        }
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

    public void refreshPage() {
        restartLoader(this);
    }

    @Override
    public ViewGroup getSwipeView() {
        return null;
    }

    @Override
    public void goToLastPage() {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt(ARG_PAGE, mTopic.getNumberOfPages());
        getArguments().remove(ARG_POST_ID);
        restartLoader(this);
    }

    public void goToLastPost(int pid) {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt(ARG_POST_ID, pid);
        getArguments().remove(ARG_PAGE);
        restartLoader(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo
            menuInfo) {

        WebView.HitTestResult hitTestResult = mWebView.getHitTestResult();
        if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            ImageActionsDialog imenu = ImageActionsDialog.getInstance(Uri.parse(hitTestResult.getExtra()));
            imenu.setTargetFragment(this, 0);
            imenu.show(getBaseActivity().getSupportFragmentManager(), ImageActionsDialog.TAG);
        } else if (hitTestResult.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
            LinkActionsDialog imenu = LinkActionsDialog.getInstance(Uri.parse(hitTestResult.getExtra()));
            imenu.setTargetFragment(this, 0);
            imenu.show(getBaseActivity().getSupportFragmentManager(), LinkActionsDialog.TAG);
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

    /**
     * Open the form and insert the quoted post
     *
     * @param id The post id to quote
     */
    public void quotePost(final int id) {
        Post p = mTopic.getPostById(id);

        String text = String.format(getString(R.string.quote),
                mTopic.getId(), p.getId(), p.getAuthor().getNick(), p.getText());

        // convert img to url
        text = text.replaceAll("\\[url=([^\\[\\]]+)\\]\\s*\\[img\\]\\s*([^\\[\\]]+)\\s*\\[/img\\]\\s*\\[/url\\]", "[url]$1[/url]");
        text = text.replaceAll("\\[img\\]\\s*([^\\[\\]]+)\\s*\\[/img\\]", "[url]$1[/url]");

        Intent intent = new Intent(getBaseActivity(), EditorActivity.class);
        intent.putExtra(EditorFragment.ARG_TOKEN, mTopic.getNewreplytoken());
        intent.putExtra(EditorFragment.ARG_MODE, EditorFragment.MODE_REPLY);
        intent.putExtra(EditorFragment.ARG_TOPIC_ID, mTopic.getId());
        intent.putExtra(EditorFragment.ARG_TEXT, text);
        intent.putExtra(EditorFragment.ARG_CLOSED, mTopic.isClosed());

        startActivityForResult(intent, EditorFragment.MODE_REPLY);
    }

    /**
     * Open the form for reply
     */
    public void replyPost() {
        if (mTopic == null)
            return;

        Intent intent = new Intent(getBaseActivity(), EditorActivity.class);
        intent.putExtra(EditorFragment.ARG_MODE, EditorFragment.MODE_REPLY);
        intent.putExtra(EditorFragment.ARG_TOPIC_ID, mTopic.getId());
        intent.putExtra(EditorFragment.ARG_TOKEN, mTopic.getNewreplytoken());
        intent.putExtra(EditorFragment.ARG_CLOSED, mTopic.isClosed());

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

        final String url = Utils.getAsyncUrl(
                String.format("set-bookmark.php?PID=%d&token=%s", p.getId(), p.getBookmarktoken()));

        showLoadingAnimation();

        Network network = new Network(getActivity());
        network.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //Utils.printException(error);
                hideLoadingAnimation();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // fail silently if the Activity is not present anymore
                if (getBaseActivity() == null)
                    return;

                getBaseActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        showSuccess(R.string.msg_bookmark_added);
                        mWebViewScrollCallbacks.toggleTopToolbar(true);
                    }
                });

                hideLoadingAnimation();
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

        String url = Utils.getAbsoluteUrl(
                String.format("thread.php?PID=%d&TID=%d#reply_%d", p.getId(), mTopic.getId(), p.getId()));

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));

        Intent chooser = Intent.createChooser(i, getString(R.string.choose_browser));
        if (i.resolveActivity(getBaseActivity().getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            showError(getString(R.string.no_browser_installed));
        }
    }

    @SuppressLint("NewApi")
    public void clipboardPostUrl(final int id) {
        Post p = mTopic.getPostById(id);

        String url = Utils.getAbsoluteUrl(
                String.format("thread.php?PID=%d&TID=%d#reply_%d", p.getId(), mTopic.getId(), p.getId()));

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(mTopic.getTitle(), url);
            clipboard.setPrimaryClip(clip);
        } else {
            android.text.ClipboardManager clipboard =
                    (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(url);
        }

        showSuccess("Link kopiert.");
    }

    /**
     * write a pm to the author
     *
     * @param id the PID
     */
    public void pmToAuthor(final int id) {
        Post p = mTopic.getPostById(id);

        Intent intent = new Intent(getBaseActivity(), EditorActivity.class);
        intent.putExtra(EditorFragment.ARG_MODE, EditorFragment.MODE_MESSAGE);
        intent.putExtra(EditorFragment.ARG_RCPT, p.getAuthor().getNick());
        startActivityForResult(intent, EditorFragment.MODE_MESSAGE);
    }

    public void loadImage(final String url, final String id) {
        ImageHandler ih = ImageHandler.getPictureHandler(getActivity());
        try {
            ih.retrieveImage(url, new ImageHandler.ImageHandlerCallback() {
                @Override
                public void onSuccess(String url, String path, boolean from_cache) {
                    mJsInterface.displayImage(url, path, id);
                }

                @Override
                public void onFailure(String url) {
                    mJsInterface.displayImageLoader(id);
                    showError(R.string.msg_img_loading_error);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class AsyncContentLoader extends AsyncHttpLoader<Topic> {

        // For some reason, getContext() returns an ApplicationContext object, which
        // is not sufficient for the TopicBuilder instance. So we store our own.
        private Context mContext;
        private Bundle mArgs;

        AsyncContentLoader(Context cx, Bundle args, int page, int thread_id, int post_id) {
            super(cx, TopicParser.getUrl(thread_id, page, post_id));
            mArgs = args;
            mContext = cx;

        }

        @Override
        public Topic processNetworkResponse(String response) {
            try {
                TopicParser parser = new TopicParser();
                final Topic t = parser.parse(response);
                final ArrayList<String> media = new ArrayList<>();

                TopicBuilder b = new TopicBuilder(mContext, new TopicBuilder.TagCallback() {
                    @Override
                    public void onTag(String tag, String content, List<String> args) {
                        if(tag.equals("img"))
                            media.add("I" + content);
                        else if(tag.equals("video"))
                            media.add("V" + content);
                    }
                });
                t.setHtmlCache(b.parse(t));
                t.setMedia(media);

                t.setIsCacheOnly(mArgs.getBoolean("cache", false));

                return t;
            } catch (Exception e) {
                Utils.printException(e);
                return null;
            }
        }

        @Override
        protected void onNetworkFailure(int statusCode, Header[] headers,
                                        String responseBody, Throwable error) {
            deliverResult(null);
        }
    }

    class WebViewScrollCallbacks implements ObservableScrollViewCallbacks {
        private boolean mIsScrolledBottom;
        private boolean mIsScrolledTop;
        private boolean mToolbarHidden;

        @Override
        public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
            if (!mUserInteraction)
                return;

            boolean scrollingDown = scrollY > mOldScroll;

            int wvContentLength = (int) Math.floor(mWebView.getContentHeight() * mWebView.getScale());

            mIsScrolledTop = getBaseActivity().getToolbar().getHeight() >= mWebView.getCurrentScrollY();
            mIsScrolledBottom = (wvContentLength - mWebView.getCurrentScrollY()) <=
                    (mWebView.getHeight() + 200);

            if (scrollingDown && !mIsScrolledTop && !mIsScrolledBottom) {
                hideToolbars();
            } else {
                showToolbars();
            }

            if ((scrollingDown && !mIsScrolledTop && !mIsScrolledBottom) ||
                    (!scrollingDown && mIsScrolledBottom)) {
                mFab.hide();
            } else {
                showFloatingActionButton();
            }

            if (mSettings.fastscroll()) {
                if (scrollingDown) {
                    hideUpButton();
                    showDownButton();
                } else {
                    showUpButton();
                    hideDownButton();
                }
            }

            mOldScroll = scrollY;
        }

        public void showToolbars() {
            if (mToolbarHidden) {
                toggleTopToolbar(true);
                toggleBottomToolbar(true);
            }
        }

        public void hideToolbars() {
            if (!mToolbarHidden && mSettings.dynamicToolbars() && !mSettings.isFixedSidebar()) {
                toggleTopToolbar(false);
                toggleBottomToolbar(false);
            }
        }

        public void toggleTopToolbar(final boolean show) {
            mToolbarHidden = !show;
            Toolbar t = getBaseActivity().getToolbar();
            int translation = show ? 0 : -t.getHeight();
            ViewPropertyAnimator.animate(t).translationY(translation).setDuration(200).start();
            //mPullToRefreshLayout.setTopMargin(translation + t.getHeight());
        }

        public void toggleBottomToolbar(final boolean show) {
            mToolbarHidden = !show;
            Toolbar t = getBaseActivity().getBottomToolbar();
            int translation = show ? 0 : t.getHeight();
            ViewPropertyAnimator.animate(t).translationY(translation).setDuration(200).start();
        }

        @Override
        public void onDownMotionEvent() {
            mUserInteraction = true;
        }

        @Override
        public void onUpOrCancelMotionEvent(ScrollState scrollState) {

        }
    }

}
