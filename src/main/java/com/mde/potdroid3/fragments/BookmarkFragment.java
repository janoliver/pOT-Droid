package com.mde.potdroid3.fragments;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.mde.potdroid3.ForumActivity;
import com.mde.potdroid3.R;
import com.mde.potdroid3.SettingsActivity;
import com.mde.potdroid3.TopicActivity;
import com.mde.potdroid3.helpers.Network;
import com.mde.potdroid3.models.Bookmark;
import com.mde.potdroid3.models.BookmarkList;
import com.mde.potdroid3.parsers.BookmarkParser;

import java.io.InputStream;

public class BookmarkFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<BookmarkList> {

    private BookmarkList mBookmarkList;
    private BookmarkListAdapter mListAdapter;
    private ListView mListView;

    public static BookmarkFragment newInstance(int board_id, int page) {
        return new BookmarkFragment();
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListAdapter = new BookmarkListAdapter();
        mListView = (ListView)getView().findViewById(R.id.list_content);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TopicActivity.class);
                intent.putExtra("post_id", mBookmarkList.getBookmarks().get(position).getLastPost().getId());
                intent.putExtra("thread_id", mBookmarkList.getBookmarks().get(position).getThread().getId());
                startActivity(intent);
            }
        });

        startLoader(this);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_bookmarks, menu);
        //menu.setGroupVisible(R.id.loggedin, mObjectManager.isLoggedIn());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                restartLoader(this);
                return true;
            case R.id.preferences:
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.forumact:
                intent = new Intent(getActivity(), ForumActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<BookmarkList> onCreateLoader(int id, Bundle args) {
        AsyncContentLoader l = new AsyncContentLoader(getActivity(), mNetwork);
        showLoader();
        return l;
    }

    @Override
    public void onLoadFinished(Loader<BookmarkList> loader, BookmarkList data) {
        hideLoader();
        if(data != null) {
            mBookmarkList = data;
            mListAdapter.notifyDataSetChanged();
        } else {
            showError("Fehler beim Laden der Daten.");
        }
    }

    @Override
    public void onLoaderReset(Loader<BookmarkList> loader) {
        hideLoader();
    }

    protected int getLayout() {
        return R.layout.layout_list_container;
    }

    private class BookmarkListAdapter extends BaseAdapter {

        public int getCount() {
            if(mBookmarkList == null)
                return 0;
            return mBookmarkList.getBookmarks().size();
        }

        public Object getItem(int position) {
            return mBookmarkList.getBookmarks().get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = mInflater.inflate(R.layout.listitem_thread, null);
            Bookmark b = (Bookmark)getItem(position);

            // set the name, striked if closed
            TextView title = (TextView) row.findViewById(R.id.title);
            title.setText(b.getThread().getTitle());
            if(b.getThread().isClosed())
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            return row;
        }
    }

    static class AsyncContentLoader extends AsyncTaskLoader<BookmarkList> {
        private Network mNetwork;

        AsyncContentLoader(Context cx, Network network) {
            super(cx);
            mNetwork = network;
        }

        @Override
        public BookmarkList loadInBackground() {
            try {
                InputStream xml = mNetwork.getDocument(BookmarkList.Xml.getUrl());
                BookmarkParser parser = new BookmarkParser();
                return parser.parse(xml);
            } catch (Exception e) {
                return null;
            }
        }

    }


}
