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
import com.mde.potdroid.models.Bookmark;
import com.mde.potdroid.models.BookmarkList;
import com.mde.potdroid.parsers.BookmarkParser;
import com.mde.potdroid.views.IconButton;
import uk.co.ribot.easyadapter.EasyRecyclerAdapter;
import uk.co.ribot.easyadapter.ItemViewHolder;
import uk.co.ribot.easyadapter.PositionInfo;
import uk.co.ribot.easyadapter.annotations.LayoutId;
import uk.co.ribot.easyadapter.annotations.ViewId;

import java.util.ArrayList;

/**
 * This is the Sidebar containing a list of unread Bookmarks and the navigation.
 */
public class SidebarBookmarksFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<BookmarkParser.BookmarksContainer> {

    // the bookmark list and adapter
    private BookmarkList mBookmarkList;
    private EasyRecyclerAdapter<Bookmark>  mListAdapter;
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

        BookmarkViewHolder.BookmarkListener listener = new BookmarkViewHolder.BookmarkListener() {
            @Override
            public void onClick(Bookmark t) {
                Intent intent = new Intent(getBaseActivity(), TopicActivity.class);
                intent.putExtra(TopicFragment.ARG_POST_ID, t.getLastPost().getId());
                intent.putExtra(TopicFragment.ARG_TOPIC_ID, t.getThread().getId());
                startActivity(intent);
            }
        };

        mEmptyListView = (TextView) v.findViewById(R.id.empty_bookmarks_text);


        mListAdapter = new EasyRecyclerAdapter<>(getActivity(), BookmarkViewHolder.class,
                new ArrayList<Bookmark>(), listener);
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

        if(!Utils.isLoggedIn()) {
            mPmButton.disable();
            mBookmarksButton.disable();
        }

        IconButton refresh = (IconButton) v.findViewById(R.id.button_refresh);
        if(!mSettings.isSwipeToRefresh()) {
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
    public void onRefresh() {
        super.onRefresh();
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

        if(success != null && success.getException() != null) {
            if(success.getException() instanceof Utils.NotLoggedInException) {
                Utils.setNotLoggedIn();
                mBookmarkList.clearBookmarksCache();
                setNewBookmarks();
                showError(getString(R.string.notloggedin));
                TextView indicator = (TextView)getView().findViewById(R.id.empty_bookmarks_text);
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
        if(mSettings.isReloadBookmarksOnSidebarOpen())
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
        mListAdapter.setItems(mBookmarkList.getUnreadBookmarks());

        if(mBookmarkList.getUnreadBookmarks().isEmpty())
            mEmptyListView.setVisibility(View.VISIBLE);
        else
            mEmptyListView.setVisibility(View.GONE);
    }

    @LayoutId(R.layout.listitem_sidebar_bookmark)
    public static class BookmarkViewHolder extends ItemViewHolder<Bookmark> {

        @ViewId(R.id.container)
        RelativeLayout mContainer;

        @ViewId(R.id.name)
        TextView mTextTitle;

        @ViewId(R.id.newposts)
        TextView mTextNewposts;

        public BookmarkViewHolder(View view) {
            super(view);
        }

        @Override
        public void onSetValues(Bookmark b, PositionInfo positionInfo) {
            mTextTitle.setText(b.getThread().getTitle());
            if (b.getThread().isClosed())
                mTextTitle.setPaintFlags(mTextTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            mTextNewposts.setText(b.getNumberOfNewPosts().toString());
        }

        @Override
        public void onSetListeners() {
            mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Get your custom listener and call the method.
                    BookmarkListener listener = getListener(BookmarkListener.class);
                    if (listener != null) {
                        listener.onClick(getItem());
                    }
                }
            });

        }

        public interface BookmarkListener {
            void onClick(Bookmark bookmark);
        }
    }


}
