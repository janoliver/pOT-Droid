package com.mde.potdroid3.fragments;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.mde.potdroid3.*;
import com.mde.potdroid3.models.Bookmark;
import com.mde.potdroid3.models.BookmarkList;

public class SidebarFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<BookmarkList> {

    private BookmarkList mBookmarkList;
    private BookmarkListAdapter mListAdapter;
    private ListView mListView;
    protected Button mBookmarkRefreshButton;
    private Boolean mDirty = true;

    public static SidebarFragment newInstance() {
        return new SidebarFragment();
    }

    protected int getLayout() {
        return R.layout.layout_sidebar;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
        mBookmarkList = new BookmarkList(getActivity());

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = super.onCreateView(inflater, container, saved);

        mListAdapter = new BookmarkListAdapter();
        mListView = (ListView)v.findViewById(R.id.listview_bookmarks);
        mListView.setAdapter(mListAdapter);
        mListView.setEmptyView(v.findViewById(R.id.empty_bookmarks_text));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TopicActivity.class);
                intent.putExtra("post_id", mBookmarkList.getUnreadBookmarks().get(position).getLastPost().getId());
                intent.putExtra("thread_id", mBookmarkList.getUnreadBookmarks().get(position).getThread().getId());
                startActivity(intent);
            }
        });

        mBookmarkRefreshButton = (Button) v.findViewById(R.id.refresh_bookmarks);
        mBookmarkRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // replace the button's icon with a rotating one
                restartLoader(SidebarFragment.this);
            }
        });

        ImageButton home = (ImageButton) v.findViewById(R.id.button_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ForumActivity.class);
                startActivity(intent);
            }
        });

        ImageButton preferences = (ImageButton) v.findViewById(R.id.button_preferences);
        preferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        ImageButton bookmarks = (ImageButton) v.findViewById(R.id.button_bookmarks);
        bookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BookmarkActivity.class);
                startActivity(intent);
            }
        });

        ImageButton pm = (ImageButton) v.findViewById(R.id.button_pm);
        pm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go to foren overview
            }
        });

        return v;

    }

    @Override
    public void showLoadingAnimation() {
        mBookmarkRefreshButton.setEnabled(false);
        getView().findViewById(R.id.separator).setVisibility(View.GONE);
        getView().findViewById(R.id.update_progress).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingAnimation() {
        mBookmarkRefreshButton.setEnabled(true);
        getView().findViewById(R.id.separator).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.update_progress).setVisibility(View.GONE);
    }

    public void refreshBookmarks() {
        restartLoader(this);
    }

    @Override
    public Loader<BookmarkList> onCreateLoader(int id, Bundle args) {
        mDirty = false;
        showLoadingAnimation();
        return new BookmarkFragment.AsyncContentLoader(getActivity(), mBookmarkList);
    }

    @Override
    public void onLoadFinished(Loader<BookmarkList> loader, BookmarkList data) {
        hideLoadingAnimation();
        if(data != null) {
            mBookmarkList = data;
            mListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<BookmarkList> loader) {}

    public boolean isDirty() {
        return mDirty;
    }

    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    @Override
    public void onResume() {
        super.onResume();

        mListAdapter.notifyDataSetChanged();
    }

    private class BookmarkListAdapter extends BaseAdapter {

        public int getCount() {
            if(mBookmarkList == null)
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
            View row = mInflater.inflate(R.layout.listitem_sidebar_bookmark, null);
            Bookmark b = (Bookmark)getItem(position);

            // set the name, striked if closed
            TextView title = (TextView) row.findViewById(R.id.name);
            title.setText(b.getThread().getTitle());
            if(b.getThread().isClosed())
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            TextView newposts = (TextView) row.findViewById(R.id.newposts);
            newposts.setText(b.getNumberOfNewPosts().toString());

            return row;
        }
    }


}
