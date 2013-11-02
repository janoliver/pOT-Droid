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
import android.widget.*;
import com.mde.potdroid3.R;
import com.mde.potdroid3.TopicActivity;
import com.mde.potdroid3.helpers.Utils;
import com.mde.potdroid3.models.Bookmark;
import com.mde.potdroid3.models.BookmarkList;

public class BookmarkFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<Boolean> {

    private BookmarkList mBookmarkList;
    private BookmarkListAdapter mListAdapter;
    private ListView mListView;

    public static BookmarkFragment newInstance(int board_id, int page) {
        return new BookmarkFragment();
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setRetainInstance(true);

        mBookmarkList = new BookmarkList(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = super.onCreateView(inflater, container, saved);

        mListAdapter = new BookmarkListAdapter();
        mListView = (ListView)v.findViewById(R.id.list_content);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TopicActivity.class);
                intent.putExtra("post_id", mBookmarkList.getBookmarks().get(position).getLastPost().getId());
                intent.putExtra("thread_id", mBookmarkList.getBookmarks().get(position).getThread().getId());
                startActivity(intent);
            }
        });

        registerForContextMenu(mListView);

        return v;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        startLoader(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_bookmarks, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                restartLoader(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.contextmenu_bookmark, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                Bookmark b = mBookmarkList.getBookmarks().get((int) info.id);
                final String url = Utils.ASYNC_URL + "remove-bookmark.php?BMID=" + b.getId()
                        + "&token=" + b.getRemovetoken();
                new Thread(new Runnable() {
                    public void run() {
                        mNetwork.callPage(url);

                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getActivity(), "Bookmark entfernt.",
                                        Toast.LENGTH_SHORT).show();

                                restartLoader(BookmarkFragment.this);
                            }
                        });

                    }
                }).start();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        AsyncContentLoader l = new AsyncContentLoader(getActivity(), mBookmarkList);
        showLoadingAnimation();
        return l;
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
        hideLoadingAnimation();
        if(success) {
            mListAdapter.notifyDataSetChanged();
        } else {
            showError("Fehler beim Laden der Daten.");
        }
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {
        hideLoadingAnimation();
    }

    protected int getLayout() {
        return R.layout.layout_board;
    }

    private class BookmarkListAdapter extends BaseAdapter {

        public int getCount() {
            if(mBookmarkList == null)
                return 0;
            return mBookmarkList.getBookmarks().size();
        }

        public Object getItem(int position) {
            return mBookmarkList.getBookmarks().get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = mInflater.inflate(R.layout.listitem_bookmark, null);
            Bookmark b = (Bookmark)getItem(position);

            if(b.getNumberOfNewPosts() > 0)
                row.findViewById(R.id.container).setBackgroundColor(
                        getResources().getColor(R.color.bbstyle_darkblue)
                );

            // set the name, striked if closed
            TextView title = (TextView) row.findViewById(R.id.title);
            title.setText(b.getThread().getTitle());
            if(b.getThread().isClosed())
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            TextView board = (TextView) row.findViewById(R.id.board);
            board.setText(b.getThread().getBoard().getName());

            TextView description = (TextView) row.findViewById(R.id.description);
            Spanned content = Html.fromHtml("<b>" + b.getNumberOfNewPosts() + "</b> neue Post. <b>"
                    + b.getThread().getNumberOfPages() + "</b> Seiten");
            description.setText(content);

            return row;
        }
    }

    public static class AsyncContentLoader extends AsyncTaskLoader<Boolean> {
        private BookmarkList mBookmarkList;

        AsyncContentLoader(Context cx, BookmarkList list) {
            super(cx);
            mBookmarkList = list;
        }

        @Override
        public Boolean loadInBackground() {

            try {
                mBookmarkList.refresh();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

    }
}
