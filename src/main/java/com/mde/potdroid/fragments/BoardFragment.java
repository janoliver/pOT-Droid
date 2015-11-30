package com.mde.potdroid.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mde.potdroid.EditorActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.TopicActivity;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.helpers.DatabaseWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Bookmark;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;
import com.mde.potdroid.parsers.BoardParser;
import com.mde.potdroid.views.IconDrawable;
import com.mde.potdroid.views.IconView;
import org.apache.http.Header;
import uk.co.ribot.easyadapter.EasyRecyclerAdapter;
import uk.co.ribot.easyadapter.ItemViewHolder;
import uk.co.ribot.easyadapter.PositionInfo;
import uk.co.ribot.easyadapter.annotations.LayoutId;
import uk.co.ribot.easyadapter.annotations.ViewId;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * The Board Fragment, which contains a list of Topics.
 */
public class BoardFragment extends PaginateFragment implements LoaderManager.LoaderCallbacks<Board> {

    // the tags of the fragment arguments
    public static final String ARG_ID = "board_id";
    public static final String ARG_PAGE = "page";

    // the board object
    private Board mBoard;

    private RecyclerView mListView;

    EasyRecyclerAdapter<Topic> mListAdapter;

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

        TopicViewHolder.TopicListener listener = new TopicViewHolder.TopicListener() {
            @Override
            public void onClick(Topic t) {
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

            @Override
            public boolean onLongClick(Topic t) {
                Intent intent = new Intent(getBaseActivity(), TopicActivity.class);
                intent.putExtra(TopicFragment.ARG_TOPIC_ID, t.getId());
                intent.putExtra(TopicFragment.ARG_PAGE, 1);
                startActivity(intent);
                return true;
            }
        };


        mListAdapter = new EasyRecyclerAdapter<>(getActivity(), TopicViewHolder.class,
                new ArrayList<Topic>(), listener);

        mListView = (RecyclerView) v.findViewById(R.id.forum_list_content);
        mListView.setAdapter(mListAdapter);
        mListView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getBaseActivity());
        mListView.setLayoutManager(mLayoutManager);

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setImageDrawable(IconDrawable.getIconDrawable(getActivity(), R.string.icon_pencil));

        if (Utils.isLoggedIn() && mSettings.isShowFAB() && !mSettings.isBottomToolbar()) {
            fab.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    newThread();
                }
            });
        } else {
            fab.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        if (mBoard == null)
            startLoader(this);

        if (mSettings.isBottomToolbar()) {
            getWriteButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newThread();
                }
            });
        }
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
        else
            menu.findItem(R.id.new_thread).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_pencil));
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
    public void onRefresh() {
        super.onRefresh();
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

            // generate subtitle and set title and subtitle of the actionbar
            Spanned subtitle = Html.fromHtml(String.format(getString(
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

    @LayoutId(R.layout.listitem_thread)
    public static class TopicViewHolder extends ItemViewHolder<Topic> {

        @ViewId(R.id.container)
        RelativeLayout mContainer;

        @ViewId(R.id.title)
        TextView mTextTitle;

        @ViewId(R.id.subtitle)
        TextView mTextSubtitle;

        @ViewId(R.id.pages)
        TextView mTextPages;

        @ViewId(R.id.author)
        TextView mTextAuthor;

        @ViewId(R.id.icon_pinned)
        IconView mIconPinned;

        @ViewId(R.id.icon_locked)
        IconView mIconLock;

        @ViewId(R.id.icon_bookmarked)
        IconView mIconBookmark;

        public TopicViewHolder(View view) {
            super(view);
        }

        @Override
        public void onSetValues(Topic t, PositionInfo positionInfo) {
            mTextTitle.setText(t.getTitle());
            if (t.isClosed())
                mTextTitle.setPaintFlags(mTextTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                mTextTitle.setPaintFlags(mTextTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

            mTextSubtitle.setText(t.getSubTitle());

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

            String time = new SimpleDateFormat(fmt).format(displayPost.getDate());
            mTextAuthor.setText(Html.fromHtml(String.format(
                    getContext().getString(R.string.thread_lastpost), displayPost.getAuthor().getNick(), time)));


            if (t.getIconId() != null) {
                try {
                    Drawable d = Utils.getIcon(getContext(), t.getIconId());
                    d.setBounds(0, 0, (int) mTextTitle.getTextSize(), (int) mTextTitle.getTextSize());
                    mTextTitle.setCompoundDrawables(d, null, null, null);
                } catch (IOException e) {
                    Utils.printException(e);
                }
            }

            Spanned pages_content = Html.fromHtml(String.format(getContext().getString(
                            R.string.topic_additional_information),
                    t.getNumberOfPosts(), t.getNumberOfPages()));
            mTextPages.setText(pages_content);

            // all important topics get a different background.
            // the padding stuff is apparently an android bug...
            // see http://stackoverflow.com/questions/5890379
            if (t.isSticky() || t.isImportant() || t.isAnnouncement() || t.isGlobal()) {
                View v = mContainer.findViewById(R.id.container);
                int padding_top = v.getPaddingTop();
                int padding_bottom = v.getPaddingBottom();
                int padding_right = v.getPaddingRight();
                int padding_left = v.getPaddingLeft();

                v.setBackgroundResource(Utils.getDrawableResourceIdByAttr(getContext(), R.attr.bbBackgroundListActive));
                v.setPadding(padding_left, padding_top, padding_right, padding_bottom);
            } else {
                View v = mContainer.findViewById(R.id.container);
                int padding_top = v.getPaddingTop();
                int padding_bottom = v.getPaddingBottom();
                int padding_right = v.getPaddingRight();
                int padding_left = v.getPaddingLeft();

                v.setBackgroundResource(Utils.getDrawableResourceIdByAttr(getContext(), R.attr.bbBackgroundList));
                v.setPadding(padding_left, padding_top, padding_right, padding_bottom);
            }

            if (!t.isSticky()) {
                mIconPinned.setVisibility(View.GONE);
            } else {
                mIconPinned.setVisibility(View.VISIBLE);
            }

            if (!t.isClosed()) {
                mIconLock.setVisibility(View.GONE);
            } else {
                mIconLock.setVisibility(View.VISIBLE);
            }

            DatabaseWrapper db = new DatabaseWrapper(getContext());
            if (Utils.isLoggedIn() && !db.isBookmark(t)) {
                mIconBookmark.setVisibility(View.GONE);
            } else {
                mIconBookmark.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onSetListeners() {
            mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Get your custom listener and call the method.
                    TopicListener listener = getListener(TopicListener.class);
                    if (listener != null) {
                        listener.onClick(getItem());
                    }
                }
            });

            mContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Get your custom listener and call the method.
                    TopicListener listener = getListener(TopicListener.class);
                    if (listener != null) {
                        listener.onLongClick(getItem());
                    }
                    return true;
                }
            });
        }

        public interface TopicListener {
            public void onClick(Topic t);

            public boolean onLongClick(Topic t);

        }
    }


}