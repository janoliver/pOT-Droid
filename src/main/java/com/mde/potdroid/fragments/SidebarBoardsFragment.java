package com.mde.potdroid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.BoardActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.DatabaseWrapper;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Forum;
import com.mde.potdroid.views.IconButton;
import uk.co.ribot.easyadapter.EasyRecyclerAdapter;
import uk.co.ribot.easyadapter.ItemViewHolder;
import uk.co.ribot.easyadapter.PositionInfo;
import uk.co.ribot.easyadapter.annotations.LayoutId;
import uk.co.ribot.easyadapter.annotations.ViewId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * This is the Sidebar containing a list of favorite Boards
 */
public class SidebarBoardsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Forum> {

    private ArrayList<Board> mBoards;
    private EasyRecyclerAdapter<Board> mListAdapter;
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

        BoardViewHolder.BoardListener listener = new BoardViewHolder.BoardListener() {
            @Override
            public void onClick(Board t) {
                Intent intent = new Intent(getBaseActivity(), BoardActivity.class);
                intent.putExtra(BoardFragment.ARG_ID, t.getId());
                startActivity(intent);
            }

            @Override
            public boolean onLongClick(final Board board) {
                new MaterialDialog.Builder(getActivity())
                        .content(R.string.action_remove_bookmark)
                        .positiveText("Ok")
                        .negativeText("Abbrechen")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                DatabaseWrapper db = new DatabaseWrapper(getActivity());
                                db.removeBoard(board);
                                showSuccess(R.string.msg_remove_success);
                                refreshBoards();
                            }
                        })
                        .show();

                return true;
            }
        };
        mEmptyListView = (TextView) v.findViewById(R.id.empty_bookmarks_text);
        mListAdapter = new EasyRecyclerAdapter<>(getActivity(), BoardViewHolder.class,
                new ArrayList<Board>(), listener);
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

        IconButton refresh = (IconButton) v.findViewById(R.id.button_refresh);
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
    public void onRefresh() {
        super.onRefresh();
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

    @LayoutId(R.layout.listitem_sidebar_board)
    public static class BoardViewHolder extends ItemViewHolder<Board> {

        @ViewId(R.id.container)
        LinearLayout mContainer;

        @ViewId(R.id.name)
        TextView mTextTitle;

        @ViewId(R.id.text_description)
        TextView mTextDescription;

        @ViewId(R.id.last_post)
        TextView mTextLastPost;

        public BoardViewHolder(View view) {
            super(view);
        }

        @Override
        public void onSetValues(Board b, PositionInfo positionInfo) {
            mTextTitle.setText(b.getName());
            Spanned lastpost_text = Html.fromHtml(String.format(
                    getContext().getString(R.string.strong), b.getLastPost().getTopic().getTitle()));
            mTextDescription.setText(lastpost_text);

            String time = new SimpleDateFormat(getContext().getString(R.string.default_time_format)).format(b
                    .getLastPost().getDate());
            Spanned lastpost_text_line2 = Html.fromHtml(String.format(
                    getContext().getString(R.string.last_post_sidebar), b.getLastPost().getAuthor().getNick(), time));
            mTextLastPost.setText(lastpost_text_line2);
        }

        @Override
        public void onSetListeners() {
            mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Get your custom listener and call the method.
                    BoardListener listener = getListener(BoardListener.class);
                    if (listener != null) {
                        listener.onClick(getItem());
                    }
                }
            });


            mContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Get your custom listener and call the method.
                    BoardListener listener = getListener(BoardListener.class);
                    if (listener != null) {
                        return listener.onLongClick(getItem());
                    }
                    return false;
                }
            });
        }

        public interface BoardListener {
            void onClick(Board board);
            boolean onLongClick(Board board);
        }
    }

}
