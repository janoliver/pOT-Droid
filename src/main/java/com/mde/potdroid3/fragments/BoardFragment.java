package com.mde.potdroid3.fragments;

import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.mde.potdroid3.ForumActivity;
import com.mde.potdroid3.R;
import com.mde.potdroid3.SettingsActivity;
import com.mde.potdroid3.TopicActivity;
import com.mde.potdroid3.models.Board;
import com.mde.potdroid3.models.Topic;
import com.mde.potdroid3.parsers.BoardParser;

import java.io.InputStream;

public class BoardFragment extends BaseFragment {

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

        new BaseLoaderTask().execute((Void[]) null);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_board, menu);
        //menu.setGroupVisible(R.id.loggedin, mObjectManager.isLoggedIn());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                new BaseLoaderTask().execute((Void[]) null);
                return true;
            case R.id.bookmarks:
                return true;
            case R.id.preferences:
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.forumact:
                intent = new Intent(getActivity(), ForumActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    class BaseLoaderTask extends AsyncTask<Void, Void, Board> {

        @Override
        protected void onPreExecute() {
            showLoader();
        }

        @Override
        protected Board doInBackground(Void... params) {
            int page = getArguments().getInt("page", 1);
            int board_id = getArguments().getInt("board_id", 0);

            try {
                InputStream xml = mNetwork.getDocument(Board.Xml.getUrl(board_id, page));
                BoardParser parser = new BoardParser();
                return parser.parse(xml);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Board board) {
            if(board != null) {
                mBoard = board;
                mListAdapter.notifyDataSetChanged();
            }
            hideLoader();
        }
    }


}
