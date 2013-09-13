package com.mde.potdroid3.fragments;

import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
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
import com.mde.potdroid3.models.Bookmark;
import com.mde.potdroid3.models.BookmarkList;
import com.mde.potdroid3.parsers.BookmarkParser;

import java.io.InputStream;

public class BookmarkFragment extends BaseFragment {

    private BookmarkList mBookmarkList = null;
    private BookmarkListAdapter mListAdapter = null;
    private ListView mListView = null;

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
                intent.putExtra("page", 1);
                startActivity(intent);
            }
        });

        new BaseLoaderTask().execute((Void[]) null);

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
                new BaseLoaderTask().execute((Void[]) null);
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

    class BaseLoaderTask extends AsyncTask<Void, Void, BookmarkList> {

        @Override
        protected void onPreExecute() {
            showLoader();
        }

        @Override
        protected BookmarkList doInBackground(Void... params) {

            try {
                InputStream xml = mNetwork.getDocument(BookmarkList.Xml.getUrl());
                BookmarkParser parser = new BookmarkParser();
                return parser.parse(xml);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(BookmarkList list) {
            if(list != null) {
                mBookmarkList = list;
                mListAdapter.notifyDataSetChanged();
            }
            hideLoader();
        }
    }


}
