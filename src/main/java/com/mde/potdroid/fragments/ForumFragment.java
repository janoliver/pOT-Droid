package com.mde.potdroid.fragments;

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
import com.mde.potdroid.BoardActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.helpers.DatabaseWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Category;
import com.mde.potdroid.models.Forum;
import com.mde.potdroid.parsers.ForumParser;
import com.mde.potdroid.views.IconDrawable;
import org.apache.http.Header;

import java.text.SimpleDateFormat;

/**
 * The Forum list fragment. It shows an ExpandableList with Categories as groups and
 * boards as children. The loading of the xml is done via an AsyncTaskLoader which
 * preserves data on configuration changes.
 */
public class ForumFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Forum> {

    private Forum mForum;

    private ForumListAdapter mListAdapter;
    private ExpandableListView mListView;

    /**
     * Return new instance of ForumFragment. Although this fragment has no parameters,
     * We provide this method for consistency.
     *
     * @return ForumFragment
     */
    public static ForumFragment newInstance() {
        return new ForumFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_forum, container, false);

        mListAdapter = new ForumListAdapter();
        mListView = (ExpandableListView) v.findViewById(R.id.list_content);

        mListView.setGroupIndicator(null);
        mListView.setAdapter(mListAdapter);
        mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                        int childPosition, long id) {

                int bid = mForum.getCategories()
                        .get(groupPosition).getBoards().get(childPosition).getId();

                Intent intent = new Intent(getBaseActivity(), BoardActivity.class);
                intent.putExtra(BoardFragment.ARG_ID, bid);
                intent.putExtra(BoardFragment.ARG_PAGE, 1);
                startActivity(intent);

                return true;
            }
        });

        registerForContextMenu(mListView);

        getActionbar().setTitle(R.string.title_forum);

        return v;

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getBaseActivity().getMenuInflater();
        inflater.inflate(R.menu.contextmenu_board, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() != R.id.forum)
            return false;

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            // so far, one can only delete a bookmark through the context menu
            case R.id.add:
                DatabaseWrapper db = new DatabaseWrapper(getActivity());
                Board b = mForum.
                        getCategories().get(mListView.getPackedPositionGroup(info.packedPosition)).
                        getBoards().get(mListView.getPackedPositionChild(info.packedPosition));

                db.addBoard(b);
                showSuccess(R.string.msg_marked_favorite);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mForum == null)
            startLoader(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_forum, menu);

        MenuItem refresh = menu.findItem(R.id.refresh);
        refresh.setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_refresh));
    }

    @Override
    public void onRefreshStarted(View view) {
        super.onRefreshStarted(view);
        restartLoader(this);
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
        AsyncContentLoader l = new AsyncContentLoader(getBaseActivity());
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
            showError(getString(R.string.msg_loading_error));
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
            String time = new SimpleDateFormat(getString(R.string.default_time_format)).format(b
                    .getLastPost().getDate());
            Spanned lastpost_text = Html.fromHtml(String.format(
                    getString(R.string.last_post), b.getLastPost().getAuthor().getNick(), time));
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

            return row;
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
            super(cx, ForumParser.URL);
        }

        @Override
        protected Forum processNetworkResponse(String response) {
            try {
                ForumParser parser = new ForumParser();
                return parser.parse(response);
            } catch (Exception e) {
                Utils.printException(e);
                return null;
            }
        }

        @Override
        protected void onNetworkFailure(int statusCode, Header[] headers,
                                        String responseBody, Throwable error) {

            Utils.printException(error);
            deliverResult(null);
        }

    }

}
