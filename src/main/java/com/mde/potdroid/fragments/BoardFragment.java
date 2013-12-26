package com.mde.potdroid.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mde.potdroid.R;
import com.mde.potdroid.TopicActivity;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.helpers.DatabaseWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Topic;
import com.mde.potdroid.parsers.BoardParser;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * The Board Fragment, which contains a list of Topics.
 */
public class BoardFragment extends PaginateFragment implements LoaderManager.LoaderCallbacks<Board>
{

    // the tags of the fragment arguments
    public static final String ARG_ID = "board_id";
    public static final String ARG_PAGE = "page";
    // the board object
    private Board mBoard;
    // the topic list adapter
    private BoardListAdapter mListAdapter;
    // bookmark database handler
    private DatabaseWrapper mDatabase;

    /**
     * Returns an instance of the BoardFragment and sets required parameters as Arguments
     *
     * @param board_id the id of the board
     * @param page the currently visible page
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

        mListAdapter = new BoardListAdapter();
        ListView mListView = (ListView) v.findViewById(R.id.list_content);
        mListView.setAdapter(mListAdapter);

        // clicking on a topic leads to the topicactivity
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseActivity(), TopicActivity.class);
                intent.putExtra(TopicFragment.ARG_TOPIC_ID, mBoard.getTopics().get(position)
                        .getId());
                intent.putExtra(TopicFragment.ARG_PAGE, 1);
                startActivity(intent);
            }
        });

        mDatabase = new DatabaseWrapper(getActivity());

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getBaseActivity().enableLeftSidebar();

        if (mBoard == null)
            startLoader(this);
    }

    @Override
    public Loader<Board> onCreateLoader(int id, Bundle args) {
        int page = getArguments().getInt(ARG_PAGE, 1);
        int bid = getArguments().getInt(ARG_ID, 0);

        showLoadingAnimation();

        return new AsyncContentLoader(getBaseActivity(), page, bid);
    }

    @Override
    public void onLoadFinished(Loader<Board> loader, Board data) {
        hideLoadingAnimation();

        if (data != null) {
            mBoard = data;

            // refresh the list
            mListAdapter.notifyDataSetChanged();

            // refresh the OptionsMenu, because of new pagination possibilities
            getBaseActivity().supportInvalidateOptionsMenu();

            // generate subtitle and set title and subtitle of the actionbar
            Spanned subtitle = Html.fromHtml(String.format(getString(
                    R.string.paginate_page_indicator), mBoard.getPage(),
                    mBoard.getNumberOfPages()));

            getActionbar().setTitle(mBoard.getName());
            getActionbar().setSubtitle(subtitle);

        } else {
            showError(getString(R.string.loading_error));
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
    public boolean isFirstPage() {
        return mBoard == null || mBoard.getPage() == 1;
    }

    @Override
    public void refreshPage() {
        restartLoader(this);
    }

    /**
     * The content loader
     */
    static class AsyncContentLoader extends AsyncHttpLoader<Board>
    {

        AsyncContentLoader(Context cx, int page, int board_id) {
            super(cx, BoardParser.getUrl(board_id, page));
        }

        @Override
        public Board processNetworkResponse(String response) {
            try {
                BoardParser parser = new BoardParser();
                return parser.parse(response);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    private class BoardListAdapter extends BaseAdapter
    {

        public int getCount() {
            if (mBoard == null)
                return 0;
            return mBoard.getTopics().size();
        }

        public Object getItem(int position) {
            return mBoard.getTopics().get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = getInflater().inflate(R.layout.listitem_thread, null);
            Topic t = (Topic) getItem(position);

            // set the name, striked if closed
            TextView title = (TextView) row.findViewById(R.id.title);
            title.setText(t.getTitle());
            if (t.isClosed())
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            // set the subtitle
            TextView subtitle = (TextView) row.findViewById(R.id.subtitle);
            subtitle.setText(t.getSubTitle());

            // pages information
            TextView pages = (TextView) row.findViewById(R.id.pages);
            Spanned pages_content = Html.fromHtml(String.format(getString(
                    R.string.topic_additional_information),
                    t.getNumberOfPosts(), t.getNumberOfPages()));
            pages.setText(pages_content);

            // date
            TextView date = (TextView) row.findViewById(R.id.date);
            String ds = new SimpleDateFormat(getString(R.string.standard_time_format))
                    .format(t.getFirstPost().getDate());
            date.setText(ds);

            // icon
            if (t.getIconId() != null) {
                try {
                    Drawable d = Utils.getIcon(getActivity(), t.getIconId());
                    d.setBounds(0, 0, 13 * (int) mDensity, 13 * (int) mDensity);
                    title.setCompoundDrawables(d, null, null, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // all important topics get a different background.
            // the padding stuff is apparently an android bug...
            // see http://stackoverflow.com/questions/5890379
            if (t.isSticky() || t.isImportant() || t.isAnnouncement() || t.isGlobal()) {
                View v = row.findViewById(R.id.container);
                int padding_top = v.getPaddingTop();
                int padding_bottom = v.getPaddingBottom();
                int padding_right = v.getPaddingRight();
                int padding_left = v.getPaddingLeft();

                v.setBackgroundResource(R.drawable.sidebar_button_background);
                v.setPadding(padding_left, padding_top, padding_right, padding_bottom);
            }

            if (!t.isSticky()) {
                row.findViewById(R.id.icon_pinned).setVisibility(View.INVISIBLE);
            }

            if (!t.isClosed()) {
                row.findViewById(R.id.icon_locked).setVisibility(View.INVISIBLE);
            }

            if (!mDatabase.isBookmark(t)) {
                row.findViewById(R.id.icon_bookmarked).setVisibility(View.INVISIBLE);
            }

            return row;
        }
    }

}
