package com.mde.potdroid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.BoardActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.DatabaseWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.helpers.ptr.SwipyRefreshLayoutDirection;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Forum;

import java.util.ArrayList;

/**
 * This is the Sidebar containing a list of favorite Boards
 */
public class SidebarBoardsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Forum> {

    private ArrayList<Board> mBoards;
    private BoardListAdapter mListAdapter;
    private DatabaseWrapper mDatabase;
    private TextView mEmptyListView;

    // this member indicates, whether the view is "dirty" and should be refreshed.
    private Boolean mDirty = true;

    /**
     * Return new instance of SidebarRightFragment. Although this fragment has no parameters,
     * We provide this method for consistency.
     *
     * @return SidebarLeftFragment
     */
    public static SidebarBoardsFragment newInstance() {
        return new SidebarBoardsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = new DatabaseWrapper(getActivity());
        mBoards = mDatabase.getBoards();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_sidebar_right, container, false);

        mEmptyListView = (TextView) v.findViewById(R.id.empty_bookmarks_text);
        mListAdapter = new BoardListAdapter(new ArrayList<Board>());
        setNewBoards();

        RecyclerView listView = (RecyclerView) v.findViewById(R.id.listview_boards);
        listView.setAdapter(mListAdapter);
        listView.setLayoutManager(new LinearLayoutManager(getBaseActivity()));

        Button refreshButton = (Button) v.findViewById(R.id.refresh_boards);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartLoader(SidebarBoardsFragment.this);
            }
        });

        ImageButton refresh = (ImageButton) v.findViewById(R.id.button_refresh);
        if(!mSettings.isSwipeToRefresh()) {
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restartLoader(SidebarBoardsFragment.this);
                }
            });
            refresh.setVisibility(View.VISIBLE);
        }

        return v;
    }

    private void setNewBoards() {
        mListAdapter.setItems(mBoards);

        if(mBoards.isEmpty())
            mEmptyListView.setVisibility(View.VISIBLE);
        else
            mEmptyListView.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        super.onRefresh(direction);
        restartLoader(this);
    }

    @Override
    public int getNotificationParent() {
        return R.id.forums_holder;
    }

    public void refreshBoards() {
        restartLoader(this);
    }

    @Override
    public Loader<Forum> onCreateLoader(int id, Bundle args) {
        mDirty = false;
        showLoadingAnimation();
        return new ForumFragment.AsyncContentLoader(getBaseActivity());
    }

    @Override
    public void onLoadFinished(Loader<Forum> loader, Forum success) {
        hideLoadingAnimation();
        if (success != null) {
            mDatabase.refreshBoards(success.getBoards());
            mBoards = mDatabase.getBoards();
            setNewBoards();
            //mListAdapter.notifyDataSetChanged();
        } else {
            showError(R.string.msg_loading_error);
        }
    }

    @Override
    public void onLoaderReset(Loader<Forum> loader) {
        hideLoadingAnimation();
    }

    /**
     * Is the sidebar dirty? *rr*
     *
     * @return true if it was not reloaded since it was attached, false otherwise
     */
    public Boolean isDirty() {
        return mDirty;
    }

    @Override
    public void onResume() {
        super.onResume();
        mListAdapter.notifyDataSetChanged();
    }

    public class BoardListAdapter extends RecyclerView.Adapter<BoardListAdapter.ViewHolder> {
        private ArrayList<Board> mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {

            public LinearLayout mContainer;
            public TextView mTextTitle;
            public TextView mTextLastPost;
            public TextView mTextDescription;

            public ViewHolder(LinearLayout container) {
                super(container);
                mContainer = container;
                mTextTitle = (TextView)mContainer.findViewById(R.id.name);
                mTextLastPost = (TextView)mContainer.findViewById(R.id.last_post);
                mTextDescription = (TextView)mContainer.findViewById(R.id.text_description);
            }
        }

        public BoardListAdapter(ArrayList<Board> data) {
            mDataset = data;
        }

        public void setItems(ArrayList<Board> data) {
            mDataset = data;
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @Override
        public BoardListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            LinearLayout v = (LinearLayout)LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_sidebar_board, parent, false);

            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final Board b = mDataset.get(position);

            holder.mTextTitle.setText(b.getName());
            Spanned lastpost_text = Utils.fromHtml(String.format(
                    getContext().getString(R.string.strong), b.getLastPost().getTopic().getTitle()));
            holder.mTextDescription.setText(lastpost_text);

            String time = Utils.getFormattedTime(getString(R.string.default_time_format), b.getLastPost().getDate());

            Spanned lastpost_text_line2 = Utils.fromHtml(String.format(
                    getContext().getString(R.string.last_post_sidebar), b.getLastPost().getAuthor().getNick(), time));
            holder.mTextLastPost.setText(lastpost_text_line2);

            holder.mContainer.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getBaseActivity(), BoardActivity.class);
                    intent.putExtra(BoardFragment.ARG_ID, b.getId());
                    startActivity(intent);
                }
            });

            holder.mContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new MaterialDialog.Builder(getActivity())
                            .content(R.string.action_remove_bookmark)
                            .positiveText("Ok")
                            .negativeText("Abbrechen")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    DatabaseWrapper db = new DatabaseWrapper(getActivity());
                                    db.removeBoard(b);
                                    showSuccess(R.string.msg_remove_success);
                                    refreshBoards();
                                }
                            })
                            .show();

                    return true;
                }
            });

        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

}
