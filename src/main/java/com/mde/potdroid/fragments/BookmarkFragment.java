package com.mde.potdroid.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.mde.potdroid.R;
import com.mde.potdroid.TopicActivity;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.helpers.Network;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.helpers.ptr.SwipyRefreshLayoutDirection;
import com.mde.potdroid.models.Bookmark;
import com.mde.potdroid.models.BookmarkList;
import com.mde.potdroid.parsers.BookmarkParser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.apache.http.Header;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The fragment that displays the list of bookmarks
 */
public class BookmarkFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<BookmarkParser.BookmarksContainer> {

    // the bookmark list, Listview and adapter
    private BookmarkList mBookmarkList;
    private BookmarkListAdapter mListAdapter;

    /**
     * Return new instance of BookmarkFragment. Although this fragment has no parameters,
     * We provide this method for consistency.
     *
     * @return Bookmarkfragment
     */
    public static BookmarkFragment newInstance() {
        return new BookmarkFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mBookmarkList = new BookmarkList(getBaseActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_bookmarks, container, false);

        mListAdapter = new BookmarkListAdapter(new ArrayList<Bookmark>());
        ObservableRecyclerView listView = (ObservableRecyclerView) v.findViewById(R.id.forum_list_content);
        listView.setLayoutManager(new LinearLayoutManager(getBaseActivity()));
        listView.setAdapter(mListAdapter);
        
        getActionbar().setTitle(R.string.title_bookmarks);

        return v;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        startLoader(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_bookmarks, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                restartLoader(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        super.onRefresh(direction);
        restartLoader(this);
    }

    @Override
    public Loader<BookmarkParser.BookmarksContainer> onCreateLoader(int id, Bundle args) {
        AsyncContentLoader l = new AsyncContentLoader(getBaseActivity());
        showLoadingAnimation();
        return l;
    }

    @Override
    public void onLoadFinished(Loader<BookmarkParser.BookmarksContainer> loader,
                               BookmarkParser.BookmarksContainer success) {
        hideLoadingAnimation();

        if (success != null && success.getException() != null) {
            if (success.getException() instanceof Utils.NotLoggedInException) {
                Utils.setNotLoggedIn();
                mBookmarkList.clearBookmarksCache();
                showError(getString(R.string.notloggedin));
                mListAdapter.setItems(new ArrayList<Bookmark>());
            }
        } else if (success != null) {

            mBookmarkList.refresh(success.getBookmarks(), success.getNumberOfNewPosts());
            mListAdapter.setItems(mBookmarkList.getBookmarks());
            Spanned subtitle = Utils.fromHtml(String.format(getString(R.string.subtitle_bookmarks),
                    success.getNumberOfNewPosts()));
            getActionbar().setSubtitle(subtitle);

        } else {
            showError(getString(R.string.msg_loading_error));
        }
    }

    @Override
    public void onLoaderReset(Loader<BookmarkParser.BookmarksContainer> loader) {
        hideLoadingAnimation();
    }

    public class BookmarkListAdapter extends RecyclerView.Adapter<BookmarkListAdapter.ViewHolder> {
        private ArrayList<Bookmark> mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {

            public FrameLayout mRoot;
            public RelativeLayout mContainer;
            public TextView mTextTitle;
            public TextView mTextBoard;
            public TextView mTextPages;


            public ViewHolder(FrameLayout container) {
                super(container);
                mRoot = container;
                mContainer = (RelativeLayout) container.findViewById(R.id.container);
                mTextTitle = (TextView) mContainer.findViewById(R.id.title);
                mTextBoard = (TextView) mContainer.findViewById(R.id.board);
                mTextPages = (TextView) mContainer.findViewById(R.id.pages);
            }
        }

        public BookmarkListAdapter(ArrayList<Bookmark> data) {
            mDataset = data;
        }

        public void setItems(ArrayList<Bookmark> data) {
            mDataset = data;
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @Override
        public BookmarkListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            FrameLayout v = (FrameLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_bookmark, parent, false);

            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final Bookmark b = mDataset.get(position);


            // change the background color, if the bookmark has unread posts
            if (b.getNumberOfNewPosts() > 0) {
                holder.mRoot.setBackgroundColor(Utils.getColorByAttr(getActivity(), R.attr.bbDarkerItemBackground));
            } else {
                holder.mRoot.setBackgroundColor(Utils.getColorByAttr(getActivity(), R.attr.bbItemBackground));
            }

            // set the name, striked if closed
            TextView title = holder.mTextTitle;
            title.setText(b.getThread().getTitle());
            if (b.getThread().isClosed())
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            // set the name of the board
            TextView board = holder.mTextBoard;
            board.setText(b.getThread().getBoard().getName());

            // display the number of new posts
            Spanned description_content = Utils.fromHtml(String.format(getString(
                    R.string.new_posts_description),
                    b.getNumberOfNewPosts(), b.getThread().getNumberOfPages()));

            TextView description = holder.mTextPages;
            description.setText(description_content);

            holder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getBaseActivity(), TopicActivity.class);
                    intent.putExtra(TopicFragment.ARG_POST_ID, b.getLastPost().getId());
                    intent.putExtra(TopicFragment.ARG_TOPIC_ID, b.getThread().getId());
                    startActivity(intent);
                }
            });

            holder.mContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new MaterialDialog.Builder(getActivity())
                            .content(R.string.action_remove_bookmark)
                            .positiveText("Ok")
                            .negativeText("Abbrechen")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    final String url = Utils.getAsyncUrl(
                                            String.format("remove-bookmark.php?BMID=%s&token=%s", b.getId(), b.getRemovetoken()));

                                    showLoadingAnimation();

                                    Network network = new Network(getActivity());
                                    network.get(url, new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            hideLoadingAnimation();
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            getBaseActivity().runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    showSuccess(R.string.msg_bookmark_removed);
                                                    hideLoadingAnimation();
                                                    restartLoader(BookmarkFragment.this);
                                                }
                                            });
                                        }
                                    });
                                }
                            })
                            .show();
                    return true;
                }
            });

        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    static class AsyncContentLoader extends AsyncHttpLoader<BookmarkParser.BookmarksContainer> {

        AsyncContentLoader(Context cx) {
            super(cx, BookmarkParser.URL);
        }

        @Override
        public BookmarkParser.BookmarksContainer processNetworkResponse(String response) {
            try {
                BookmarkParser parser = new BookmarkParser();
                return parser.parse(response);
            } catch (Exception e) {
                BookmarkParser.BookmarksContainer c = new BookmarkParser.BookmarksContainer();
                Utils.setNotLoggedIn();
                c.setException(e);
                return c;
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
