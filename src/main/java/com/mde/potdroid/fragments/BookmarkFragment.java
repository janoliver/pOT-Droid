package com.mde.potdroid.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mde.potdroid.R;
import com.mde.potdroid.TopicActivity;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.helpers.Network;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.Bookmark;
import com.mde.potdroid.models.BookmarkList;
import com.mde.potdroid.parsers.BookmarkParser;

import org.apache.http.Header;

/**
 * The fragment that displays the list of bookmarks
 */
public class BookmarkFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<BookmarkParser.BookmarksContainer>
{

    // the bookmark list, Listview and adapter
    private BookmarkList mBookmarkList;
    private BookmarkListAdapter mListAdapter;
    private ListView mListView;

    /**
     * Return new instance of BookmarkFragment. Although this fragment has no parameters,
     * We provide this method for consistency.
     *
     * @return Bookmarkfragment
     */
    public static BookmarkFragment newInstance() {
        return new BookmarkFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mBookmarkList = new BookmarkList(getSupportActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_board, container, false);

        mListAdapter = new BookmarkListAdapter();
        mListView = (ListView) v.findViewById(R.id.list_content);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getSupportActivity(), TopicActivity.class);
                intent.putExtra(TopicFragment.ARG_POST_ID, mBookmarkList.getBookmarks()
                        .get(position).getLastPost().getId());
                intent.putExtra(TopicFragment.ARG_TOPIC_ID, mBookmarkList.getBookmarks()
                        .get(position).getThread().getId());
                startActivity(intent);
            }
        });

        registerForContextMenu(mListView);

        getActionbar().setTitle(R.string.bookmarks);

        return v;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mBookmarkList == null)
            startLoader(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_bookmarks, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        MenuInflater inflater = getSupportActivity().getMenuInflater();
        inflater.inflate(R.menu.contextmenu_bookmark, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            // so far, one can only delete a bookmark through the context menu
            case R.id.delete:
                Bookmark b = mBookmarkList.getBookmarks().get((int) info.id);
                final String url = Network.getAsyncUrl(
                        "remove-bookmark.php?BMID=" + b.getId() + "&token=" + b.getRemovetoken());

                showLoadingAnimation();

                Network network = new Network(getActivity());
                network.get(url, null, new AsyncHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Utils.toast(getSupportActivity(), getString(R.string.removed_bookmark));
                        hideLoadingAnimation();
                        restartLoader(BookmarkFragment.this);
                    }
                });

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public Loader<BookmarkParser.BookmarksContainer> onCreateLoader(int id, Bundle args) {
        AsyncContentLoader l = new AsyncContentLoader(getSupportActivity());
        showLoadingAnimation();
        return l;
    }

    @Override
    public void onLoadFinished(Loader<BookmarkParser.BookmarksContainer> loader,
                               BookmarkParser.BookmarksContainer success) {
        hideLoadingAnimation();

        if (success != null) {

            mBookmarkList.refresh(success.getBookmarks(), success.getNumberOfNewPosts());
            mListAdapter.notifyDataSetChanged();
            String subtitle = String.format(getString(R.string.x_unread_posts),
                    success.getNumberOfNewPosts());
            getActionbar().setSubtitle(subtitle);

        } else {
            showError(getString(R.string.loading_error));
        }
    }

    @Override
    public void onLoaderReset(Loader<BookmarkParser.BookmarksContainer> loader) {
        hideLoadingAnimation();
    }

    private class BookmarkListAdapter extends BaseAdapter
    {

        public int getCount() {
            if (mBookmarkList == null)
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
            View row = getInflater().inflate(R.layout.listitem_bookmark, null);
            Bookmark b = (Bookmark) getItem(position);

            // change the background color, if the bookmark has unread posts
            if (b.getNumberOfNewPosts() > 0) {
                View v = row.findViewById(R.id.container);
                v.setBackgroundResource(R.drawable.sidebar_button_background);
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),
                        v.getPaddingBottom());
            }

            // set the name, striked if closed
            TextView title = (TextView) row.findViewById(R.id.title);
            title.setText(b.getThread().getTitle());
            if (b.getThread().isClosed())
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            // set the name of the board
            TextView board = (TextView) row.findViewById(R.id.board);
            board.setText(b.getThread().getBoard().getName());

            // display the number of new posts
            Spanned description_content = Html.fromHtml(String.format(getString(
                    R.string.new_posts_description),
                    b.getNumberOfNewPosts(), b.getThread().getNumberOfPages()));

            TextView description = (TextView) row.findViewById(R.id.pages);
            description.setText(description_content);

            return row;
        }
    }

    static class AsyncContentLoader extends AsyncHttpLoader<BookmarkParser.BookmarksContainer>
    {

        AsyncContentLoader(Context cx) {
            super(cx, BookmarkParser.URL);
        }

        @Override
        public BookmarkParser.BookmarksContainer processNetworkResponse(String response) {
            try {
                BookmarkParser parser = new BookmarkParser();
                return parser.parse(response);
            } catch(Utils.NotLoggedInException e) {
                Utils.setNotLoggedIn();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
