package com.mde.potdroid.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.mde.potdroid.EditorActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.TopicActivity;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.helpers.DatabaseWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.helpers.ptr.SwipyRefreshLayoutDirection;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Bookmark;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;
import com.mde.potdroid.parsers.BoardParser;
import org.apache.http.Header;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * The Board Fragment, which contains a list of Topics.
 */
public class BoardFragment extends PaginateFragment implements LoaderManager.LoaderCallbacks<Board>, ObservableScrollViewCallbacks {

    // the tags of the fragment arguments
    public static final String ARG_ID = "board_id";
    public static final String ARG_PAGE = "page";

    // the board object
    private Board mBoard;

    private ObservableRecyclerView mListView;

    TopicListAdapter mListAdapter;

    private LinearLayoutManager mLayoutManager;

    /**
     * Returns an instance of the BoardFragment and sets required parameters as Arguments
     *
     * @param board_id the id of the board
     * @param page     the currently visible page
     * @return BoardFragment object
     */
    public static BoardFragment newInstance(int board_id, int page) {
        BoardFragment f = new BoardFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt(ARG_ID, board_id);
        args.putInt(ARG_PAGE, page);

        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_board, container, false);


        mListAdapter = new TopicListAdapter(new ArrayList<Topic>());

        mListView = (ObservableRecyclerView) v.findViewById(R.id.forum_list_content);
        mListView.setScrollViewCallbacks(this);

        mListView.setAdapter(mListAdapter);
        mListView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getBaseActivity());
        mListView.setLayoutManager(mLayoutManager);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        if (mBoard == null)
            startLoader(this);
        else
            mListAdapter.setItems(mBoard.getFilteredTopics(getActivity()));

        if (mSettings.isBottomToolbar()) {
            getWriteButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newThread();
                }
            });
        }

        if(mSettings.isBottomToolbar() && mSettings.isDisableFfwdBoard())
            disableFfwd();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_thread:
                // reload content
                newThread();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_board, menu);
        if (!Utils.isLoggedIn() || mSettings.isBottomToolbar())
            menu.findItem(R.id.new_thread).setVisible(false);
    }

    /**
     * Open the form for a new thread
     */
    public void newThread() {
        if (mBoard == null)
            return;

        Intent intent = new Intent(getBaseActivity(), EditorActivity.class);
        intent.putExtra(EditorFragment.ARG_MODE, EditorFragment.MODE_THREAD);
        intent.putExtra(EditorFragment.ARG_BOARD_ID, mBoard.getId());
        intent.putExtra(EditorFragment.ARG_TOKEN, mBoard.getNewthreadtoken());

        startActivityForResult(intent, EditorFragment.MODE_THREAD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == EditorFragment.MODE_THREAD) {
            if (resultCode == Activity.RESULT_OK) {
                Intent intent = new Intent(getBaseActivity(), TopicActivity.class);
                intent.putExtra(TopicFragment.ARG_TOPIC_ID, data.getExtras().getInt(EditorFragment.ARG_TOPIC_ID));
                intent.putExtra(TopicFragment.ARG_PAGE, 1);
                startActivity(intent);
            }
        }
    }

    @Override
    public Loader<Board> onCreateLoader(int id, Bundle args) {
        int page = getArguments().getInt(ARG_PAGE, 1);
        int bid = getArguments().getInt(ARG_ID, 0);

        showLoadingAnimation();
        setSwipeEnabled(false);

        return new AsyncContentLoader(getBaseActivity(), page, bid);
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        super.onRefresh(direction);
        restartLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Board> loader, Board data) {
        hideLoadingAnimation();

        if (data != null) {
            mBoard = data;

            // refresh the list
            mListAdapter.setItems(mBoard.getFilteredTopics(getActivity()));

            // refresh the OptionsMenu, because of new pagination possibilities
            //getBaseActivity().supportInvalidateOptionsMenu();
            refreshPaginateLayout();

            if(mSettings.isBottomToolbar() && mSettings.isDisableFfwdBoard())
                disableFfwd();

            // generate subtitle and set title and subtitle of the actionbar
            Spanned subtitle = Utils.fromHtml(String.format(getString(
                            R.string.subtitle_paginate), mBoard.getPage(),
                    mBoard.getNumberOfPages()));

            getActionbar().setTitle(mBoard.getName());
            getActionbar().setSubtitle(subtitle);
            setSwipeEnabled(true);

            // scroll to top
            mLayoutManager.scrollToPositionWithOffset(0, 0);

        } else {
            showError(getString(R.string.msg_loading_error));
        }
    }

    @Override
    public void onLoaderReset(Loader<Board> loader) {
        hideLoadingAnimation();
    }

    public void goToNextPage() {
        // whether there is a next page was already checked in onCreateOptionsMenu
        getArguments().putInt(ARG_PAGE, mBoard.getPage() + 1);
        restartLoader(this);
    }

    @Override
    public void nextButtonLongClick() {
        goToLastPage();
    }

    public void goToPrevPage() {
        // whether there is a previous page was already checked in onCreateOptionsMenu
        getArguments().putInt(ARG_PAGE, mBoard.getPage() - 1);
        restartLoader(this);
    }

    public void goToLastPage() {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt(ARG_PAGE, mBoard.getNumberOfPages());
        restartLoader(this);
    }

    public void goToFirstPage() {
        // whether there is a previous page was already checked in onCreateOptionsMenu
        getArguments().putInt(ARG_PAGE, 1);
        restartLoader(this);
    }

    @Override
    public boolean isLastPage() {
        return mBoard == null || mBoard.isLastPage();
    }

    @Override
    public ViewGroup getSwipeView() {
        return mListView;
    }

    @Override
    public boolean isFirstPage() {
        return mBoard == null || mBoard.getPage() == 1;
    }

    public void refreshPage() {
        restartLoader(this);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    /**
     * The content loader
     */
    static class AsyncContentLoader extends AsyncHttpLoader<Board> {

        AsyncContentLoader(Context cx, int page, int board_id) {
            super(cx, BoardParser.getUrl(board_id, page));
        }

        @Override
        public Board processNetworkResponse(String response) {
            try {
                BoardParser parser = new BoardParser();
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


    public class TopicListAdapter extends RecyclerView.Adapter<TopicListAdapter.ViewHolder> {
        private ArrayList<Topic> mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {

            public FrameLayout mRoot;
            public RelativeLayout mContainer;
            public TextView mTextTitle;
            public TextView mTextSubTitle;
            public TextView mTextPages;
            public TextView mTextAuthor;
            public ImageView mIconPinned;
            public ImageView mIconLock;
            public ImageView mIconBookmark;

            public ViewHolder(FrameLayout container) {
                super(container);
                mRoot = container;
                mContainer = (RelativeLayout) container.findViewById(R.id.container);
                mTextTitle = (TextView)mContainer.findViewById(R.id.title);
                mTextSubTitle = (TextView)mContainer.findViewById(R.id.subtitle);
                mTextPages = (TextView)mContainer.findViewById(R.id.pages);
                mTextAuthor = (TextView)mContainer.findViewById(R.id.author);
                mIconPinned = (ImageView)mContainer.findViewById(R.id.icon_pinned);
                mIconLock = (ImageView)mContainer.findViewById(R.id.icon_locked);
                mIconBookmark = (ImageView)mContainer.findViewById(R.id.icon_bookmarked);
            }
        }

        public TopicListAdapter(ArrayList<Topic> data) {
            mDataset = data;
        }

        public void setItems(ArrayList<Topic> data) {
            mDataset = data;
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @Override
        public TopicListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            FrameLayout v = (FrameLayout)LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_thread, parent, false);

            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final Topic t = mDataset.get(position);

            holder.mTextTitle.setText(t.getTitle());
            if (t.isClosed())
                holder.mTextTitle.setPaintFlags(holder.mTextTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                holder.mTextTitle.setPaintFlags(holder.mTextTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

            holder.mTextSubTitle.setText(t.getSubTitle());

            // lastpost
            Post displayPost;

            if (t.getLastPost() != null) {
                displayPost = t.getLastPost();
            } else {
                displayPost = t.getFirstPost();
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(displayPost.getDate());
            Calendar today = Calendar.getInstance();
            String fmt = "dd.MM.yyyy, HH:mm";
            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                fmt = "HH:mm";
            } else if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                fmt = "dd.MM., HH:mm";
            }

            String time = Utils.getFormattedTime(fmt, displayPost.getDate());

            holder.mTextAuthor.setText(Utils.fromHtml(String.format(
                    getContext().getString(R.string.thread_lastpost), displayPost.getAuthor().getNick(), time)));


            if (t.getIconId() != null) {
                try {
                    Drawable d = Utils.getIcon(getContext(), t.getIconId());
                    d.setBounds(0, 0, (int) holder.mTextTitle.getTextSize(), (int) holder.mTextTitle.getTextSize());
                    holder.mTextTitle.setCompoundDrawables(d, null, null, null);
                } catch (IOException e) {
                    holder.mTextTitle.setCompoundDrawables(null,null,null, null);
                    Utils.printException(e);
                }
            } else {
                holder.mTextTitle.setCompoundDrawables(null,null,null, null);
            }

            Spanned pages_content = Utils.fromHtml(String.format(getContext().getString(
                    R.string.topic_additional_information),
                    t.getNumberOfPosts(), t.getNumberOfPages()));
            holder.mTextPages.setText(pages_content);

            if (t.isSticky() || t.isImportant() || t.isAnnouncement() || t.isGlobal()) {
                holder.mRoot.setBackgroundColor(Utils.getColorByAttr(getContext(), R.attr.bbDarkerItemBackground));
            } else {
                holder.mRoot.setBackgroundColor(Utils.getColorByAttr(getContext(), R.attr.bbItemBackground));
            }

            if (!t.isSticky()) {
                holder.mIconPinned.setVisibility(View.GONE);
            } else {
                holder.mIconPinned.setVisibility(View.VISIBLE);
            }

            if (!t.isClosed()) {
                holder.mIconLock.setVisibility(View.GONE);
            } else {
                holder.mIconLock.setVisibility(View.VISIBLE);
            }

            DatabaseWrapper db = new DatabaseWrapper(getContext());
            if (Utils.isLoggedIn() && !db.isBookmark(t)) {
                holder.mIconBookmark.setVisibility(View.GONE);
            } else {
                holder.mIconBookmark.setVisibility(View.VISIBLE);
            }

            holder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getBaseActivity(), TopicActivity.class);
                    Bookmark b = mDatabase.getBookmarkByTopic(t);
                    if (b != null) {
                        intent.putExtra(TopicFragment.ARG_POST_ID, b.getLastPost().getId());
                    } else {
                        intent.putExtra(TopicFragment.ARG_PAGE, t.getNumberOfPages());
                    }
                    intent.putExtra(TopicFragment.ARG_TOPIC_ID, t.getId());
                    startActivity(intent);
                }
            });

            holder.mContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent intent = new Intent(getBaseActivity(), TopicActivity.class);
                    intent.putExtra(TopicFragment.ARG_TOPIC_ID, t.getId());
                    intent.putExtra(TopicFragment.ARG_PAGE, 1);
                    startActivity(intent);
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