package com.mde.potdroid3.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.view.*;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import com.mde.potdroid3.BoardActivity;
import com.mde.potdroid3.R;
import com.mde.potdroid3.helpers.AsyncHttpLoader;
import com.mde.potdroid3.models.Board;
import com.mde.potdroid3.models.Category;
import com.mde.potdroid3.models.Forum;
import com.mde.potdroid3.parsers.ForumParser;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * The Forum list fragment. It shows an ExpandableList with Categories as groups and
 * boards as children. The loading of the xml is done via an AsyncTaskLoader which
 * preserves data on configuration changes.
 */
public class ForumFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Forum> {

    private Forum mForum;
    private ForumListAdapter mListAdapter;
    private ExpandableListView mListView;

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
                // go to a board
                Intent intent = new Intent(getSupportActivity(), BoardActivity.class);
                intent.putExtra("board_id", mForum.getCategories().get(groupPosition).getBoards().get(childPosition).getId());
                intent.putExtra("page", 1);
                startActivity(intent);
                return true;
            }
        });

        getActionbar().setTitle("Forenübersicht");

        // load the content
        startLoader(this);

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
                // reload content
                restartLoader(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected int getLayout() {
        return R.layout.layout_forum;
    }

    @Override
    public Loader<Forum> onCreateLoader(int id, Bundle args) {
        AsyncContentLoader l = new AsyncContentLoader(getSupportActivity());
        showLoadingAnimation();
        return l;
    }

    @Override
    public void onLoadFinished(Loader<Forum> loader, Forum data) {
        hideLoadingAnimation();
        if(data != null) {
            mForum = data;
            mListAdapter.notifyDataSetChanged();
        } else {
            showError("Fehler beim Laden der Daten.");
        }
    }

    @Override
    public void onLoaderReset(Loader<Forum> loader) {
        hideLoadingAnimation();
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

            TextView name = (TextView) row.findViewById(R.id.text_name);
            TextView descr = (TextView) row.findViewById(R.id.text_description);
            TextView lastpost = (TextView) row.findViewById(R.id.last_post);

            Board b = (Board)getChild(groupPosition, childPosition);

            name.setText(b.getName());
            descr.setText(b.getDescription());

            SimpleDateFormat f_date = new SimpleDateFormat("dd.MM.yy", Locale.GERMAN);
            SimpleDateFormat f_time = new SimpleDateFormat("HH:mm", Locale.GERMAN);
            Spanned lastpost_text = Html.fromHtml("Letzter Beitrag von <b>"
                    + b.getLastPost().getAuthor().getNick()
                    + "</b> am <b>" + f_date.format(b.getLastPost().getDate())
                    + "</b> um <b>" + f_time.format(b.getLastPost().getDate())
                    + "</b> Uhr");
            lastpost.setText(lastpost_text);

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

            TextView name = (TextView) row.findViewById(R.id.text_name);
            TextView descr = (TextView) row.findViewById(R.id.text_description);

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

    static class AsyncContentLoader extends AsyncHttpLoader<Forum> {
        AsyncContentLoader(Context cx) {
            super(cx, Forum.Xml.URL);
        }

        @Override
        public Forum processNetworkResponse(String response) {
            try {
                ForumParser parser = new ForumParser();
                return parser.parse(response);
            } catch (Exception e) {
                return null;
            }
        }
    }

}
