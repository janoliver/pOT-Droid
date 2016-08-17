package com.mde.potdroid.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.BoardActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.helpers.DatabaseWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.helpers.ptr.SwipyRefreshLayoutDirection;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Category;
import com.mde.potdroid.models.Forum;
import com.mde.potdroid.parsers.ForumParser;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import org.apache.http.Header;

/**
 * The Forum list fragment. It shows an ExpandableList with Categories as groups and
 * boards as children. The loading of the xml is done via an AsyncTaskLoader which
 * preserves data on configuration changes.
 */
public class ForumFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Forum> {

    private Forum mForum;

    private SectionedRecyclerViewAdapter mListAdapter;
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

        mListAdapter = new SectionedRecyclerViewAdapter();

        mListView = (RecyclerView) v.findViewById(R.id.forum_list_content);
        mListView.setAdapter(mListAdapter);
        mListView.setLayoutManager(new LinearLayoutManager(getActivity()));

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
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        super.onRefresh(direction);
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
            for(Category c : mForum.getCategories())
                mListAdapter.addSection(new CategorySection(c));

            mListAdapter.notifyDataSetChanged();
        } else {
            showError(getString(R.string.msg_loading_error));
        }
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        FrameLayout mRoot;
        RelativeLayout mContainer;
        TextView mTextDescription;
        TextView mTextName;

        public CategoryViewHolder(View view) {
            super(view);

            mRoot = (FrameLayout) view;
            mContainer = (RelativeLayout) view.findViewById(R.id.container);
            mTextDescription = (TextView) view.findViewById(R.id.text_description);
            mTextName = (TextView) view.findViewById(R.id.text_name);
        }

    }

    @Override
    public void onLoaderReset(Loader<Forum> loader) {
        hideLoadingAnimation();
    }

    class ForumViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout mContainer;
        TextView mTextDescription;
        TextView mTextLastPost;
        TextView mTextName;

        public ForumViewHolder(View view) {
            super(view);

            mContainer = (RelativeLayout) view.findViewById(R.id.container);
            mTextDescription = (TextView) view.findViewById(R.id.text_description);
            mTextName = (TextView) view.findViewById(R.id.text_name);
            mTextLastPost = (TextView) view.findViewById(R.id.last_post);
        }

        public void bindTo(final Board board) {
            mTextName.setText(board.getName());
            mTextDescription.setText(board.getDescription());

            if(board.getLastPost() != null) {
                String time = Utils.getFormattedTime(getContext()
                        .getString(R.string.default_time_format), board.getLastPost().getDate());
                Spanned lastpost_text = Utils.fromHtml(String.format(
                        getContext().getString(R.string.last_post), board.getLastPost().getAuthor().getNick(), time));
                mTextLastPost.setText(lastpost_text);
            }

            mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int bid = board.getId();

                    Intent intent = new Intent(getBaseActivity(), BoardActivity.class);
                    intent.putExtra(BoardFragment.ARG_ID, bid);
                    intent.putExtra(BoardFragment.ARG_PAGE, 1);
                    startActivity(intent);
                }
            });

            mContainer.setOnLongClickListener(new View.OnLongClickListener() {
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

    class CategorySection extends StatelessSection {

        Category mCategory;
        boolean expanded = false;

        public CategorySection(Category c) {
            // call constructor with layout resources for this Section header and items
            super(R.layout.listitem_category, R.layout.listitem_forum);

            mCategory = c;
        }

        @Override
        public int getContentItemsTotal() {
            return expanded? mCategory.getBoards().size() : 0;
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            // return a custom instance of ViewHolder for the items of this section
            return new ForumViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            ForumViewHolder itemHolder = (ForumViewHolder) holder;

            // bind your view here
            itemHolder.bindTo(mCategory.getBoards().get(position));
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new CategoryViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            final CategoryViewHolder headerHolder = (CategoryViewHolder) holder;

            headerHolder.mTextName.setText(mCategory.getName());
            headerHolder.mTextDescription.setText(mCategory.getDescription());

            headerHolder.mRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expanded = !expanded;
                    mListAdapter.notifyDataSetChanged();
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
