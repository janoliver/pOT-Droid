package com.mde.potdroid3.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.*;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import com.mde.potdroid3.BaseActivity;
import com.mde.potdroid3.R;
import com.mde.potdroid3.helpers.Network;
import com.mde.potdroid3.helpers.TopicBuilder;
import com.mde.potdroid3.helpers.TopicJSInterface;
import com.mde.potdroid3.models.Post;
import com.mde.potdroid3.models.Topic;
import com.mde.potdroid3.parsers.TopicParser;

import java.io.InputStream;

public class TopicFragment extends PaginateFragment
        implements LoaderManager.LoaderCallbacks<Topic> {

    private Topic mTopic;
    private WebView mWebView;
    private TopicJSInterface mJsInterface;
    private BaseActivity mActivity;

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

        mActivity = (BaseActivity) getActivity();

        mWebView = (WebView)getView().findViewById(R.id.topic_webview);
        mJsInterface = new TopicJSInterface(mWebView, getActivity(), this);

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
            mActivity.getSidebar().refreshBookmarks();

            // populate right sidebar
            mActivity.getRightSidebar().setIsNewPost(mTopic.getId(), mTopic.getNewreplytoken());

        } else {
            showError("Fehler beim Laden der Daten.");
        }
    }

    @Override
    public void onLoaderReset(Loader<Topic> loader) {
        hideLoadingAnimation();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // only show the paginate buttons if there are before or after the current
        if(!isLastPage()) {
            menu.findItem(R.id.nav_next).setIcon(R.drawable.dark_navigation_fwd).setEnabled(true);
            menu.findItem(R.id.nav_lastpage).setIcon(R.drawable.dark_navigation_ffwd).setEnabled(true);
        }

        if(!isFirstPage()) {
            menu.findItem(R.id.nav_firstpage).setIcon(R.drawable.dark_navigation_frwd).setEnabled(true);
            menu.findItem(R.id.nav_previous).setIcon(R.drawable.dark_navigation_rwd).setEnabled(true);
        }

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

    public void showPostDialog(int post_id) {
        PostDialogFragment menu = new PostDialogFragment(post_id);
        menu.show(mActivity.getFragmentManager(), "postmenu");
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

    public class PostDialogFragment extends DialogFragment {
        private Integer mPostId;

        PostDialogFragment(int post_id) {
            super();

            mPostId = post_id;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View dialog_view = inflater.inflate(R.layout.dialog_post_actions, null);
            builder.setView(dialog_view)
                    .setTitle("Post Aktionen")
                    .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

            // Create the AlertDialog object and return it
            final Dialog d = builder.create();

            ImageButton quote_button = (ImageButton) dialog_view.findViewById(R.id.button_quote);
            quote_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    BaseActivity a = (BaseActivity)getActivity();
                    Post p = mTopic.getPostById(mPostId);

                    String text = text = "[quote=" + mTopic.getId() + "," + p.getId() + ",\""
                            + p.getAuthor().getNick() + "\"][b]\n" + p.getText() + "\n[/b][/quote]";

                    a.getRightSidebar().appendText(text);
                    a.openRightSidebar();

                    d.cancel();
                }
            });

            return d;
        }
    }

}
