package com.mde.potdroid.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.mde.potdroid.BoardActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Category;
import com.mde.potdroid.models.Forum;
import com.mde.potdroid.parsers.ForumParser;

import java.text.SimpleDateFormat;

/**
 * The Forum list fragment. It shows an ExpandableList with Categories as groups and
 * boards as children. The loading of the xml is done via an AsyncTaskLoader which
 * preserves data on configuration changes.
 */
public class ForumFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Forum>
{

    private Forum mForum;
    private ForumListAdapter mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_forum, container, false);

        mListAdapter = new ForumListAdapter();
        ExpandableListView mListView = (ExpandableListView) v.findViewById(R.id.list_content);

        mListView.setGroupIndicator(null);
        mListView.setAdapter(mListAdapter);
        mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
        {
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                        int childPosition, long id) {
                // go to a board
                Intent intent = new Intent(getSupportActivity(), BoardActivity.class);
                intent.putExtra(BoardFragment.ARG_ID, mForum.getCategories()
                        .get(groupPosition).getBoards().get(childPosition).getId());
                intent.putExtra(BoardFragment.ARG_PAGE, 1);
                startActivity(intent);
                return true;
            }
        });

        getActionbar().setTitle(R.string.forum_title);

        return v;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        if (mForum == null)
            startLoader(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_forum, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.refresh:
                // reload content
                restartLoader(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        if (data != null) {
            mForum = data;
            mListAdapter.notifyDataSetChanged();
        } else {
            showError(getString(R.string.loading_error));
        }
    }

    @Override
    public void onLoaderReset(Loader<Forum> loader) {
        hideLoadingAnimation();
    }

    public class ForumListAdapter extends BaseExpandableListAdapter
    {

        public Object getChild(int groupPosition, int childPosition) {
            return mForum.getCategories().get(groupPosition).getBoards().get(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            if (mForum.getCategories().get(groupPosition).getBoards() == null)
                return 0;
            return mForum.getCategories().get(groupPosition).getBoards().size();
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            View row = getInflater().inflate(R.layout.listitem_forum, null);

            Board b = (Board) getChild(groupPosition, childPosition);

            TextView name = (TextView) row.findViewById(R.id.text_name);
            name.setText(b.getName());

            TextView descr = (TextView) row.findViewById(R.id.text_description);
            descr.setText(b.getDescription());

            TextView lastpost = (TextView) row.findViewById(R.id.last_post);
            String time = new SimpleDateFormat(getString(R.string.standard_time_format)).format(b
                    .getLastPost().getDate());
            Spanned lastpost_text = Html.fromHtml(String.format(
                    getString(R.string.last_post, b.getLastPost().getAuthor().getNick(), time)));
            lastpost.setText(lastpost_text);

            return (row);

        }

        public Object getGroup(int groupPosition) {
            return mForum.getCategories().get(groupPosition);
        }

        public int getGroupCount() {
            if (mForum == null)
                return 0;
            return mForum.getCategories().size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                                 ViewGroup parent) {
            View row = getInflater().inflate(R.layout.listitem_category, null);

            Category cat = (Category) getGroup(groupPosition);

            TextView name = (TextView) row.findViewById(R.id.text_name);
            name.setText(cat.getName());

            TextView descr = (TextView) row.findViewById(R.id.text_description);
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

    static class AsyncContentLoader extends AsyncHttpLoader<Forum>
    {

        AsyncContentLoader(Context cx) {
            super(cx, ForumParser.URL);
        }

        @Override
        public Forum processNetworkResponse(String response) {
            try {
                ForumParser parser = new ForumParser();
                return parser.parse(response);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
