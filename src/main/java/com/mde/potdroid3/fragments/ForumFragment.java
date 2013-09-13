package com.mde.potdroid3.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import com.mde.potdroid3.BoardActivity;
import com.mde.potdroid3.BookmarkActivity;
import com.mde.potdroid3.R;
import com.mde.potdroid3.SettingsActivity;
import com.mde.potdroid3.models.Board;
import com.mde.potdroid3.models.Category;
import com.mde.potdroid3.models.Forum;
import com.mde.potdroid3.parsers.ForumParser;

import java.io.InputStream;

public class ForumFragment extends BaseFragment {

    private Forum mForum = null;
    private ForumListAdapter mListAdapter = null;
    private ExpandableListView mListView = null;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListAdapter = new ForumListAdapter();
        mListView = (ExpandableListView)getView().findViewById(R.id.list_content);

        mListView.setGroupIndicator(null);
        mListView.setAdapter(mListAdapter);
        mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
        {
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
            {
                Intent intent = new Intent(getActivity(), BoardActivity.class);
                intent.putExtra("board_id", mForum.getCategories().get(groupPosition).getBoards().get(childPosition).getId());
                intent.putExtra("page", 1);
                startActivity(intent);
                return true;
            }
        });

        new BaseLoaderTask().execute((Void[]) null);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_forum, menu);
        //menu.setGroupVisible(R.id.loggedin, mObjectManager.isLoggedIn());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.refresh:
                new BaseLoaderTask().execute((Void[]) null);
                return true;
            case R.id.bookmarks:
                intent = new Intent(getActivity(), BookmarkActivity.class);
                startActivity(intent);
                return true;
            case R.id.preferences:
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected int getLayout() {
        return R.layout.layout_expandablelist_container;
    }

    public class ForumListAdapter extends BaseExpandableListAdapter {

        public Object getChild(int groupPosition, int childPosition) {
            return mForum.getCategories().get(groupPosition).getBoards().get(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            if(mForum.getCategories().get(groupPosition).getBoards() == null)
                return 0;
            return mForum.getCategories().get(groupPosition).getBoards().size();
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            View row = mInflater.inflate(R.layout.listitem_forum, null);

            TextView name = (TextView) row.findViewById(R.id.name);
            TextView descr = (TextView) row.findViewById(R.id.description);

            Board b = (Board)getChild(groupPosition, childPosition);

            name.setText(b.getName());
            descr.setText(b.getDescription());

            return (row);

        }

        public Object getGroup(int groupPosition) {
            return mForum.getCategories().get(groupPosition);
        }

        public int getGroupCount() {
            if(mForum == null)
                return 0;
            return mForum.getCategories().size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                                 ViewGroup parent) {
            View row = mInflater.inflate(R.layout.listitem_category, null);

            TextView name = (TextView) row.findViewById(R.id.name);
            TextView descr = (TextView) row.findViewById(R.id.description);

            Category cat = (Category)getGroup(groupPosition);

            name.setText(cat.getName());
            descr.setText(cat.getDescription());

            return (row);
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

    }


    class BaseLoaderTask extends AsyncTask<Void, Void, Forum> {

        @Override
        protected void onPreExecute() {
            showLoader();
        }

        @Override
        protected Forum doInBackground(Void... params) {
            try {
                InputStream xml = mNetwork.getDocument(Forum.Xml.URL);
                ForumParser parser = new ForumParser();
                return parser.parse(xml);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Forum forum) {
            if(forum != null) {
                mForum = forum;
                mListAdapter.notifyDataSetChanged();
            }
            hideLoader();
        }
    }

}
