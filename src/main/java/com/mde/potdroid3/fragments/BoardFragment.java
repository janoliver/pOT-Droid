package com.mde.potdroid3.fragments;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.mde.potdroid3.R;
import com.mde.potdroid3.TopicActivity;
import com.mde.potdroid3.helpers.Network;
import com.mde.potdroid3.models.Board;
import com.mde.potdroid3.models.Topic;
import com.mde.potdroid3.parsers.BoardParser;

import java.io.InputStream;

public class BoardFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Board>  {

    private Board mBoard = null;
    private BoardListAdapter mListAdapter = null;
    private ListView mListView = null;

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
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListAdapter = new BoardListAdapter();
        mListView = (ListView)getView().findViewById(R.id.list_content);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TopicActivity.class);
                intent.putExtra("thread_id", mBoard.getTopics().get(position).getId());
                intent.putExtra("page", 1);
                startActivity(intent);
            }
        });

        startLoader(this);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_board, menu);

        if(mBoard != null && !mBoard.isLastPage()) {
            menu.findItem(R.id.nav_lastpage).setEnabled(true);
            menu.findItem(R.id.nav_next).setEnabled(true);
        }

        if(mBoard != null && mBoard.getPage() > 1) {
            menu.findItem(R.id.nav_firstpage).setEnabled(true);
            menu.findItem(R.id.nav_previous).setEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.nav_refresh:
                restartLoader(this);
                return true;
            case R.id.nav_next:
                goToNextPage();
                return true;
            case R.id.nav_previous:
                goToPrevPage();
                return true;
            case R.id.nav_firstpage:
                goToFirstPage();
                return true;
            case R.id.nav_lastpage:
                goToLastPage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Board> onCreateLoader(int id, Bundle args) {
        int page = getArguments().getInt("page", 1);
        int bid = getArguments().getInt("board_id", 0);
        AsyncContentLoader l = new AsyncContentLoader(getActivity(), mNetwork, page, bid);
        showLoader();
        return l;
    }

    @Override
    public void onLoadFinished(Loader<Board> loader, Board data) {
        hideLoader();
        if(data != null) {
            mBoard = data;
            mListAdapter.notifyDataSetChanged();

            getActivity().invalidateOptionsMenu();

            Spanned subtitleText = Html.fromHtml("Seite <b>" + mBoard.getPage()
                    + "</b> von <b>" + mBoard.getNumberOfPages() + "</b>");

            getActivity().getActionBar().setTitle(mBoard.getName());
            getActivity().getActionBar().setSubtitle(subtitleText);
        } else {
            showError("Fehler beim Laden der Daten.");
        }
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
    public void onLoaderReset(Loader<Board> loader) {
        hideLoader();
    }

    protected int getLayout() {
        return R.layout.layout_list_container;
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

            // last post information
            //TextView lastpost = (TextView) row.findViewById(R.id.lastpost);
            //Spanned content = Html.fromHtml("<b>" + t.getNumberOfPosts() + "</b> Posts auf <b>" + t.getLastPage() + "</b> Seiten");
            //lastpost.setText(content);

            //TextView important = (TextView) row.findViewById(R.id.important);
            /*if (t.isImportant()) {
                important.setVisibility(View.GONE);
            } else if(t.isSticky()) {
                important.setBackgroundResource(R.color.darkred);
            }*/

            return row;
        }
    }

    static class AsyncContentLoader extends AsyncTaskLoader<Board> {
        private Network mNetwork;
        private Integer mPage;
        private Integer mBoardId;

        AsyncContentLoader(Context cx, Network network, int page, int board_id) {
            super(cx);
            mNetwork = network;
            mPage = page;
            mBoardId = board_id;
        }

        @Override
        public Board loadInBackground() {
            try {
                InputStream xml = mNetwork.getDocument(Board.Xml.getUrl(mBoardId, mPage));
                BoardParser parser = new BoardParser();
                return parser.parse(xml);
            } catch (Exception e) {
                return null;
            }
        }

    }

}
