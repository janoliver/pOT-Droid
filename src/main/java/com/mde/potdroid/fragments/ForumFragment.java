package com.mde.potdroid.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;
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
import java.util.ArrayList;
import java.util.List;

/**
 * The Forum list fragment. It shows an ExpandableList with Categories as groups and
 * boards as children. The loading of the xml is done via an AsyncTaskLoader which
 * preserves data on configuration changes.
 */
public class ForumFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Forum> {

    private Forum mForum;

    private ForumListAdapter mListAdapter;
    private RecyclerView mListView;

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

        List<CategoryItem> items = new ArrayList<>();
        mListAdapter = new ForumListAdapter(items);

        mListView = (RecyclerView) v.findViewById(R.id.forum_list_content);
        mListView.setAdapter(mListAdapter);
        mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //mListView.setGroupIndicator(null);
        mListView.setAdapter(mListAdapter);
        /**/

        getActionbar().setTitle(R.string.title_forum);

        return v;

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

        menu.findItem(R.id.refresh).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_refresh));
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
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
            List<CategoryItem> items = new ArrayList<>();
            for(Category c : mForum.getCategories())
                items.add(new CategoryItem(c));

            mListAdapter = new ForumListAdapter(items);
            mListView.setAdapter(mListAdapter);
        } else {
            showError(getString(R.string.msg_loading_error));
        }
    }

    public class CategoryItem implements ParentListItem {

        /* Create an instance variable for your list of children */
        private Category mCategory;

        public CategoryItem(Category category) {
            mCategory = category;
        }

        public Category getCategory() {
            return mCategory;
        }

        @Override
        public List<?> getChildItemList() {
            return mCategory.getBoards();
        }

        @Override
        public boolean isInitiallyExpanded() {
            return false;
        }
    }

    public static class CategoryViewHolder extends ParentViewHolder {

        RelativeLayout mContainer;
        TextView mTextDescription;
        TextView mTextName;

        public CategoryViewHolder(View view) {
            super(view);

            mContainer = (RelativeLayout) view.findViewById(R.id.container);
            mTextDescription = (TextView) view.findViewById(R.id.text_description);
            mTextName = (TextView) view.findViewById(R.id.text_name);
        }

    }

    public static class BoardViewHolder extends ChildViewHolder {

        RelativeLayout mContainer;
        TextView mTextDescription;
        TextView mTextLastPost;
        TextView mTextName;

        public BoardViewHolder(View view) {
            super(view);

            mContainer = (RelativeLayout) view.findViewById(R.id.container);
            mTextDescription = (TextView) view.findViewById(R.id.text_description);
            mTextName = (TextView) view.findViewById(R.id.text_name);
            mTextLastPost = (TextView) view.findViewById(R.id.last_post);
        }

    }

    @Override
    public void onLoaderReset(Loader<Forum> loader) {
        hideLoadingAnimation();
    }

    public class ForumListAdapter extends ExpandableRecyclerAdapter<CategoryViewHolder, BoardViewHolder> {

        public ForumListAdapter(List<? extends ParentListItem> parentItemList) {
            super(parentItemList);
        }

        @Override
        public CategoryViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
            View view = getInflater().inflate(R.layout.listitem_category, parentViewGroup, false);
            return new CategoryViewHolder(view);
        }

        @Override
        public BoardViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
            View view = getInflater().inflate(R.layout.listitem_forum, childViewGroup, false);
            return new BoardViewHolder(view);
        }

        @Override
        public void onBindParentViewHolder(CategoryViewHolder parentViewHolder, int position, ParentListItem parentListItem) {
            CategoryItem category = (CategoryItem) parentListItem;

            parentViewHolder.mTextName.setText(category.getCategory().getName());
            parentViewHolder.mTextDescription.setText(category.getCategory().getDescription());
        }

        @Override
        public void onBindChildViewHolder(BoardViewHolder childViewHolder, int position, Object item) {
            final Board board = (Board) item;
            childViewHolder.mTextName.setText(board.getName());
            childViewHolder.mTextDescription.setText(board.getDescription());

            if(board.getLastPost() != null) {
                String time = new SimpleDateFormat(getContext()
                        .getString(R.string.default_time_format)).format(board.getLastPost().getDate());
                Spanned lastpost_text = Html.fromHtml(String.format(
                        getContext().getString(R.string.last_post), board.getLastPost().getAuthor().getNick(), time));
                childViewHolder.mTextLastPost.setText(lastpost_text);
            }

            childViewHolder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int bid = board.getId();

                    Intent intent = new Intent(getBaseActivity(), BoardActivity.class);
                    intent.putExtra(BoardFragment.ARG_ID, bid);
                    intent.putExtra(BoardFragment.ARG_PAGE, 1);
                    startActivity(intent);
                }
            });

            childViewHolder.mContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    new MaterialDialog.Builder(getActivity())
                            .content(R.string.action_add_favorite_board)
                            .positiveText("Ok")
                            .negativeText("Abbrechen")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    DatabaseWrapper db = new DatabaseWrapper(getActivity());
                                    db.addBoard(board);
                                    showSuccess(R.string.msg_marked_favorite);
                                }
                            })
                            .show();

                    return true;
                }
            });
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
