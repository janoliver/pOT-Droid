package com.mde.potdroid.fragments;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.mde.potdroid.*;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.Bookmark;
import com.mde.potdroid.models.BookmarkList;
import com.mde.potdroid.parsers.BookmarkParser;
import com.mde.potdroid.views.IconButton;

/**
 * This is the Sidebar containing a list of unread Bookmarks and the navigation.
 */
public class SidebarLeftFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<BookmarkParser.BookmarksContainer> {

    // the bookmark list and adapter
    private BookmarkList mBookmarkList;
    private BookmarkListAdapter mListAdapter;

    // this member indicates, whether the view is "dirty" and should be refreshed.
    private Boolean mDirty = true;

    /**
     * Return new instance of SidebarLeftFragment. Although this fragment has no parameters,
     * We provide this method for consistency.
     *
     * @return SidebarLeftFragment
     */
    public static SidebarLeftFragment newInstance() {
        return new SidebarLeftFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBookmarkList = new BookmarkList(getBaseActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_sidebar_left, container, false);

        mListAdapter = new BookmarkListAdapter();

        ListView listView = (ListView) v.findViewById(R.id.listview_bookmarks);
        listView.setAdapter(mListAdapter);
        listView.setEmptyView(v.findViewById(R.id.empty_bookmarks_text));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseActivity(), TopicActivity.class);
                intent.putExtra(TopicFragment.ARG_POST_ID,
                        mBookmarkList.getUnreadBookmarks().get(position).getLastPost().getId());
                intent.putExtra(TopicFragment.ARG_TOPIC_ID,
                        mBookmarkList.getUnreadBookmarks().get(position).getThread().getId());
                startActivity(intent);
            }
        });

        // below are the navigation and bookmark refresh buttons

        Button bookmarkRefreshButton = (Button) v.findViewById(R.id.refresh_bookmarks);
        bookmarkRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartLoader(SidebarLeftFragment.this);
            }
        });

        IconButton home = (IconButton) v.findViewById(R.id.button_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseActivity(), ForumActivity.class);
                intent.putExtra("overview", true);
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

        IconButton bookmarks = (IconButton) v.findViewById(R.id.button_bookmarks);
        bookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseActivity(), BookmarkActivity.class);
                startActivity(intent);
            }
        });

        IconButton pm = (IconButton) v.findViewById(R.id.button_pm);
        pm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseActivity(), MessageListActivity.class);
                startActivity(intent);
            }
        });

        return v;

    }

    /**
     * We have a different loading animation in this fragment.
     */
    @Override
    public void showLoadingAnimation() {
        try {
            getView().findViewById(R.id.separator).setVisibility(View.GONE);
            getView().findViewById(R.id.update_progress).setVisibility(View.VISIBLE);
        } catch (NullPointerException e) {
            // the view was already detached. Never mind...
        }
    }

    /**
     * We have a different loading animation in this fragment.
     */
    @Override
    public void hideLoadingAnimation() {
        try {
            getView().findViewById(R.id.separator).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.update_progress).setVisibility(View.GONE);
        } catch (NullPointerException e) {
            // the view was already detached. Never mind...
        }
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
        if (success != null) {
            mBookmarkList.refresh(success.getBookmarks(), success.getNumberOfNewPosts());
            mListAdapter.notifyDataSetChanged();
        } else {
            showError(getString(R.string.msg_loading_error));
        }
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
        mListAdapter.notifyDataSetChanged();
    }

    private class BookmarkListAdapter extends BaseAdapter {

        public int getCount() {
            if (mBookmarkList == null)
                return 0;
            return mBookmarkList.getUnreadBookmarks().size();
        }

        public Object getItem(int position) {
            return mBookmarkList.getUnreadBookmarks().get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = getInflater().inflate(R.layout.listitem_sidebar_bookmark, null);
            Bookmark b = (Bookmark) getItem(position);

            // set the name, striked if closed
            TextView title = (TextView) row.findViewById(R.id.name);
            title.setText(b.getThread().getTitle());
            if (b.getThread().isClosed())
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            // set the number of new posts
            TextView newposts = (TextView) row.findViewById(R.id.newposts);
            newposts.setText(b.getNumberOfNewPosts().toString());

            return row;
        }
    }


}
