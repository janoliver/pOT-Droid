package com.mde.potdroid3.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.mde.potdroid3.*;
import com.mde.potdroid3.helpers.Network;
import com.mde.potdroid3.models.Bookmark;
import com.mde.potdroid3.models.BookmarkList;

public class SidebarFragment extends Fragment implements LoaderManager.LoaderCallbacks<BookmarkList> {

    private BookmarkList mBookmarkList;
    private BookmarkListAdapter mListAdapter;
    private ListView mListView;
    protected LayoutInflater mInflater;
    protected Network mNetwork;
    protected ImageView mBookmarkLoadingIcon;
    protected Button mBookmarkRefreshButton;

    public static SidebarFragment newInstance() {
        return new SidebarFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        mNetwork = new Network(getActivity());
        mBookmarkList = new BookmarkList(getActivity());
        return inflater.inflate(R.layout.layout_sidebar, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBookmarkLoadingIcon = (ImageView)getView().findViewById(R.id.refresh_bookmarks_icon);

        mListAdapter = new BookmarkListAdapter();
        mListView = (ListView)getView().findViewById(R.id.listview_bookmarks);
        mListView.setAdapter(mListAdapter);
        mListView.setEmptyView(getView().findViewById(R.id.empty_bookmarks_text));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TopicActivity.class);
                intent.putExtra("post_id", mBookmarkList.getUnreadBookmarks().get(position).getLastPost().getId());
                intent.putExtra("thread_id", mBookmarkList.getUnreadBookmarks().get(position).getThread().getId());
                startActivity(intent);
            }
        });

        mBookmarkRefreshButton = (Button) getView().findViewById(R.id.refresh_bookmarks);
        mBookmarkRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // replace the button's icon with a rotating one
                restartLoader(SidebarFragment.this);
            }
        });

        ImageButton home = (ImageButton) getView().findViewById(R.id.button_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ForumActivity.class);
                startActivity(intent);
            }
        });

        ImageButton preferences = (ImageButton) getView().findViewById(R.id.button_preferences);
        preferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        ImageButton bookmarks = (ImageButton) getView().findViewById(R.id.button_bookmarks);
        bookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BookmarkActivity.class);
                startActivity(intent);
            }
        });

        ImageButton pm = (ImageButton) getView().findViewById(R.id.button_pm);
        pm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go to foren overview
            }
        });



        startLoader(this);

    }

    public void showBookmarkLoadingAnimation() {
        mBookmarkRefreshButton.setEnabled(false);
        Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.clockwise_rotation);
        mBookmarkLoadingIcon.startAnimation(rotation);
    }

    public void hideBookmarkLoadingAnimation() {
        mBookmarkRefreshButton.setEnabled(true);
        mBookmarkLoadingIcon.clearAnimation();
    }

    @Override
    public Loader<BookmarkList> onCreateLoader(int id, Bundle args) {
        showBookmarkLoadingAnimation();
        return new BookmarkFragment.AsyncContentLoader(getActivity(), mBookmarkList);
    }

    @Override
    public void onLoadFinished(Loader<BookmarkList> loader, BookmarkList data) {
        hideBookmarkLoadingAnimation();
        if(data != null) {
            mBookmarkList = data;
            mListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<BookmarkList> loader) {
        // empty
    }

    /**
     * Start the content loader
     */
    public void startLoader(LoaderManager.LoaderCallbacks l) {
        getLoaderManager().initLoader(0, null, l).forceLoad();
    }

    /**
     * Restart the content loader
     */
    public void restartLoader(LoaderManager.LoaderCallbacks l) {
        getLoaderManager().restartLoader(0, null, l).forceLoad();
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
