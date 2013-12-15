package com.mde.potdroid3.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.mde.potdroid3.R;
import com.mde.potdroid3.TopicActivity;
import com.mde.potdroid3.helpers.AsyncHttpLoader;
import com.mde.potdroid3.helpers.Utils;
import com.mde.potdroid3.models.Board;
import com.mde.potdroid3.models.Topic;
import com.mde.potdroid3.parsers.BoardParser;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class BoardFragment extends PaginateFragment
        implements LoaderManager.LoaderCallbacks<Board>  {

    private Board mBoard = null;
    private BoardListAdapter mListAdapter = null;
    private ListView mListView = null;
    private DisplayMetrics mDisplayMetrics;

    public static BoardFragment newInstance(int board_id, int page) {
        BoardFragment f = new BoardFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("board_id", board_id);
        args.putInt("page", page);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = super.onCreateView(inflater, container, saved);

        mListAdapter = new BoardListAdapter();
        mListView = (ListView)v.findViewById(R.id.list_content);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getSupportActivity(), TopicActivity.class);
                intent.putExtra("thread_id", mBoard.getTopics().get(position).getId());
                intent.putExtra("page", 1);
                startActivity(intent);
            }
        });

        mDisplayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        startLoader(this);
    }

    @Override
    public Loader<Board> onCreateLoader(int id, Bundle args) {
        int page = getArguments().getInt("page", 1);
        int bid = getArguments().getInt("board_id", 0);
        showLoadingAnimation();
        return new AsyncContentLoader(getSupportActivity(), page, bid);
    }

    @Override
    public void onLoadFinished(Loader<Board> loader, Board data) {
        hideLoadingAnimation();
        if(data != null) {
            mBoard = data;
            mListAdapter.notifyDataSetChanged();

            getSupportActivity().supportInvalidateOptionsMenu();

            Spanned subtitleText = Html.fromHtml("Seite <b>" + mBoard.getPage()
                    + "</b> von <b>" + mBoard.getNumberOfPages() + "</b>");

            getActionbar().setTitle(mBoard.getName());
            getActionbar().setSubtitle(subtitleText);
        } else {
            showError("Fehler beim Laden der Daten.");
        }
    }

    @Override
    public boolean isLastPage() {
        return mBoard == null || mBoard.isLastPage();
    }

    @Override
    public boolean isFirstPage() {
        return mBoard == null || mBoard.getPage() == 1;
    }

    public void goToNextPage() {
        // whether there is a next page was checked in onCreateOptionsMenu
        getArguments().putInt("page", mBoard.getPage()+1);
        restartLoader(this);
    }

    public void goToPrevPage() {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt("page", mBoard.getPage()-1);
        restartLoader(this);
    }

    public void goToFirstPage() {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt("page", 1);
        restartLoader(this);
    }

    public void goToLastPage() {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt("page", mBoard.getNumberOfPages());
        restartLoader(this);
    }

    @Override
    public void refreshPage() {
        restartLoader(this);
    }

    @Override
    public void onLoaderReset(Loader<Board> loader) {
        hideLoadingAnimation();
    }

    protected int getLayout() {
        return R.layout.layout_board;
    }

    private class BoardListAdapter extends BaseAdapter {

        public int getCount() {
            if(mBoard == null)
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
            View row = mInflater.inflate(R.layout.listitem_thread, null);
            Topic t = (Topic)getItem(position);

            // set the name, striked if closed
            TextView title = (TextView) row.findViewById(R.id.title);
            title.setText(t.getTitle());
            if(t.isClosed())
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            // set the subtitle
            TextView subtitle = (TextView) row.findViewById(R.id.subtitle);
            subtitle.setText(t.getSubTitle());

            // pages information
            TextView pages = (TextView) row.findViewById(R.id.pages);
            Spanned content = Html.fromHtml("<b>" + (t.getNumberOfPosts()+1) + "</b> Posts, <b>" +
                    t.getNumberOfPages() + "</b> Seiten");
            pages.setText(content);

            // date
            TextView date = (TextView) row.findViewById(R.id.date);
            String ds = new SimpleDateFormat("dd.MM.yyyy, HH:mm").format(t.getFirstPost().getDate());
            date.setText(ds);

            // icon
            ImageView icon = (ImageView) row.findViewById(R.id.icon);
            try {
                Drawable d = Utils.getDrawableFromAsset(getActivity(),
                        "thread-icons/icon"+ t.getIconId() +".png");
                d.setBounds(0,0,13*(int)mDisplayMetrics.density,13*(int)mDisplayMetrics.density);
                title.setCompoundDrawables(d, null, null, null);
            } catch (IOException e) {
                //icon.setVisibility(View.GONE);
            }

            // all important topics get a different background.
            // the padding stuff is apparently an android bug...
            // see http://stackoverflow.com/questions/5890379
            if(t.isSticky() || t.isImportant() || t.isAnnouncement() || t.isGlobal()) {
                View v = row.findViewById(R.id.container);
                int bottom = v.getPaddingBottom();
                int top = v.getPaddingTop();
                int right = v.getPaddingRight();
                int left = v.getPaddingLeft();
                v.setBackgroundResource(R.drawable.sidebar_button_background);
                v.setPadding(left, top, right, bottom);
            }

            if(!t.isSticky()) {
                row.findViewById(R.id.icon_pinned).setVisibility(View.INVISIBLE);
            }

            if(!t.isClosed()) {
                row.findViewById(R.id.icon_locked).setVisibility(View.INVISIBLE);
            }

            return row;
        }
    }

    static class AsyncContentLoader extends AsyncHttpLoader<Board> {
        AsyncContentLoader(Context cx, int page, int board_id) {
            super(cx, Board.Xml.getUrl(board_id, page));
        }

        @Override
        public Board processNetworkResponse(String response) {
            try {
                BoardParser parser = new BoardParser();
                return parser.parse(response);
            } catch (Exception e) {
                return null;
            }
        }

    }

}
