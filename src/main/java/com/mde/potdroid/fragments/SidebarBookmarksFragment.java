package com.mde.potdroid.fragments;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mde.potdroid.*;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.helpers.ptr.SwipyRefreshLayoutDirection;
import com.mde.potdroid.models.Bookmark;
import com.mde.potdroid.models.BookmarkList;
import com.mde.potdroid.parsers.BookmarkParser;
import com.mde.potdroid.views.IconButton;

import java.util.ArrayList;

/**
 * This is the Sidebar containing a list of unread Bookmarks and the navigation.
 */
public class SidebarBookmarksFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<BookmarkParser.BookmarksContainer> {

    // the bookmark list and adapter
    private BookmarkList mBookmarkList;
    private BookmarkListAdapter mListAdapter;
    private TextView mEmptyListView;
    private IconButton mPmButton;
    private IconButton mBookmarksButton;

    // this member indicates, whether the view is "dirty" and should be refreshed.
    private Boolean mDirty = true;

    /**
     * Return new instance of SidebarLeftFragment. Although this fragment has no parameters,
     * We provide this method for consistency.
     *
     * @return SidebarLeftFragment
     */
    public static SidebarBookmarksFragment newInstance() {
        return new SidebarBookmarksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBookmarkList = new BookmarkList(getBaseActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_sidebar_left, container, false);

        mEmptyListView = (TextView) v.findViewById(R.id.empty_bookmarks_text);

        mListAdapter = new BookmarkListAdapter(new ArrayList<Bookmark>());
        setNewBookmarks();

        RecyclerView listView = (RecyclerView) v.findViewById(R.id.listview_bookmarks);
        listView.setAdapter(mListAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseActivity());
        listView.setLayoutManager(layoutManager);

        IconButton home = (IconButton) v.findViewById(R.id.button_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseActivity(), ForumActivity.class);
                startActivity(intent);
            }
        });

        IconButton preferences = (IconButton) v.findViewById(R.id.button_preferences);
        preferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        mBookmarksButton = (IconButton) v.findViewById(R.id.button_bookmarks);
        mBookmarksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseActivity(), BookmarkActivity.class);
                startActivity(intent);
            }
        });

        mPmButton = (IconButton) v.findViewById(R.id.button_pm);
        mPmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseActivity(), MessageListActivity.class);
                startActivity(intent);
            }
        });

        if (!Utils.isLoggedIn()) {
            mPmButton.disable();
            mBookmarksButton.disable();
        }

        IconButton refresh = (IconButton) v.findViewById(R.id.button_refresh);
        if (!mSettings.isSwipeToRefresh()) {
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshBookmarks();
                }
            });
            refresh.setVisibility(View.VISIBLE);
        }

        return v;

    }

    @Override
    public int getNotificationParent() {
        return R.id.bookmark_list_holder;
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        super.onRefresh(direction);
        restartLoader(this);
    }

    public void refreshBookmarks() {
        if (Utils.isLoggedIn())
            restartLoader(this);
    }

    @Override
    public Loader<BookmarkParser.BookmarksContainer> onCreateLoader(int id, Bundle args) {
        mDirty = false;
        showLoadingAnimation();
        return new BookmarkFragment.AsyncContentLoader(getBaseActivity());
    }

    @Override
    public void onLoadFinished(Loader<BookmarkParser.BookmarksContainer> loader,
                               BookmarkParser.BookmarksContainer success) {
        hideLoadingAnimation();

        if (success != null && success.getException() != null) {
            if (success.getException() instanceof Utils.NotLoggedInException) {
                Utils.setNotLoggedIn();
                mBookmarkList.clearBookmarksCache();
                setNewBookmarks();
                showError(getString(R.string.notloggedin));
                TextView indicator = (TextView) getView().findViewById(R.id.empty_bookmarks_text);
                indicator.setText(R.string.notloggedin);

                mPmButton.disable();
                mBookmarksButton.disable();
            }
        } else if (success != null) {

            mBookmarkList.refresh(success.getBookmarks(), success.getNumberOfNewPosts());
            setNewBookmarks();

        } else {
            showError(getString(R.string.msg_loading_error));
        }

        // if the setting to refresh bookmarks on sidebar open is set to true,
        // we immediately set dirty again so the bookmarks become updated
        if (mSettings.isReloadBookmarksOnSidebarOpen())
            mDirty = true;
    }

    @Override
    public void onLoaderReset(Loader<BookmarkParser.BookmarksContainer> loader) {
        hideLoadingAnimation();
    }

    /**
     * Is the sidebar dirty? *rr*
     *
     * @return true if it was not reloaded since it was attached, false otherwise
     */
    public Boolean isDirty() {
        return mDirty;
    }

    @Override
    public void onResume() {
        super.onResume();
        setNewBookmarks();
    }

    private void setNewBookmarks() {

        mListAdapter.setItems(mBookmarkList.getUnreadBookmarks(mSettings.isReadSidebar()));

        if (mListAdapter.getItemCount() == 0)
            mEmptyListView.setVisibility(View.VISIBLE);
        else
            mEmptyListView.setVisibility(View.GONE);
    }

    public class BookmarkListAdapter extends RecyclerView.Adapter<BookmarkListAdapter.ViewHolder> {
        private ArrayList<Bookmark> mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {

            public RelativeLayout mContainer;
            public TextView mTextTitle;
            public TextView mTextNewposts;

            public ViewHolder(RelativeLayout container) {
                super(container);
                mContainer = container;
                mTextTitle = (TextView)mContainer.findViewById(R.id.name);
                mTextNewposts = (TextView)mContainer.findViewById(R.id.newposts);
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
            RelativeLayout v = (RelativeLayout)LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_sidebar_bookmark, parent, false);

            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final Bookmark b = mDataset.get(position);

            holder.mTextTitle.setText(b.getThread().getTitle());
            if (b.getThread().isClosed())
                holder.mTextTitle.setPaintFlags(holder.mTextTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                holder.mTextTitle.setPaintFlags(holder.mTextTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

            holder.mTextNewposts.setText(b.getNumberOfNewPosts().toString());

            if (b.getNumberOfNewPosts() == 0)
                holder.mTextNewposts.setVisibility(View.GONE);
            else
                holder.mTextNewposts.setVisibility(View.VISIBLE);

            holder.mContainer.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getBaseActivity(), TopicActivity.class);
                    intent.putExtra(TopicFragment.ARG_POST_ID, b.getLastPost().getId());
                    intent.putExtra(TopicFragment.ARG_TOPIC_ID, b.getThread().getId());
                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }



}
