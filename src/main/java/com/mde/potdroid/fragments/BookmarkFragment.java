package com.mde.potdroid.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.mde.potdroid.R;
import com.mde.potdroid.TopicActivity;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.helpers.Network;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.Bookmark;
import com.mde.potdroid.models.BookmarkList;
import com.mde.potdroid.parsers.BookmarkParser;
import com.mde.potdroid.views.IconDrawable;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.http.Header;

import java.io.IOException;

/**
 * The fragment that displays the list of bookmarks
 */
public class BookmarkFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<BookmarkParser.BookmarksContainer> {

    // the bookmark list, Listview and adapter
    private BookmarkList mBookmarkList;
    private BookmarkListAdapter mListAdapter;

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

        mBookmarkList = new BookmarkList(getBaseActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_bookmarks, container, false);

        mListAdapter = new BookmarkListAdapter();
        ListView listView = (ListView) v.findViewById(R.id.forum_list_content);
        listView.setAdapter(mListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseActivity(), TopicActivity.class);
                intent.putExtra(TopicFragment.ARG_POST_ID, mBookmarkList.getBookmarks()
                        .get(position).getLastPost().getId());
                intent.putExtra(TopicFragment.ARG_TOPIC_ID, mBookmarkList.getBookmarks()
                        .get(position).getThread().getId());
                startActivity(intent);
            }
        });

        registerForContextMenu(listView);

        getActionbar().setTitle(R.string.title_bookmarks);

        return v;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        startLoader(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_bookmarks, menu);

        menu.findItem(R.id.refresh).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_refresh));
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
        MenuInflater inflater = getBaseActivity().getMenuInflater();
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
                final String url = Utils.getAsyncUrl(
                        String.format("remove-bookmark.php?BMID=%s&token=%s", b.getId(), b.getRemovetoken()));

                showLoadingAnimation();

                Network network = new Network(getActivity());
                network.get(url, new Callback() {
                    @Override
                    public void onResponse(Response response) {
                        getBaseActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                showSuccess(R.string.msg_bookmark_removed);
                                hideLoadingAnimation();
                                restartLoader(BookmarkFragment.this);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Request request, IOException error) {
                        hideLoadingAnimation();
                    }
                });

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        restartLoader(this);
    }

    @Override
    public Loader<BookmarkParser.BookmarksContainer> onCreateLoader(int id, Bundle args) {
        AsyncContentLoader l = new AsyncContentLoader(getBaseActivity());
        showLoadingAnimation();
        return l;
    }

    @Override
    public void onLoadFinished(Loader<BookmarkParser.BookmarksContainer> loader,
                               BookmarkParser.BookmarksContainer success) {
        hideLoadingAnimation();

        if(success != null && success.getException() != null) {
            if(success.getException() instanceof Utils.NotLoggedInException) {
                Utils.setNotLoggedIn();
                mBookmarkList.clearBookmarksCache();
                showError(getString(R.string.notloggedin));
                mListAdapter.notifyDataSetChanged();
            }
        } else if (success != null) {

            mBookmarkList.refresh(success.getBookmarks(), success.getNumberOfNewPosts());
            mListAdapter.notifyDataSetChanged();
            Spanned subtitle = Html.fromHtml(String.format(getString(R.string.subtitle_bookmarks),
                    success.getNumberOfNewPosts()));
            getActionbar().setSubtitle(subtitle);

        } else {
             showError(getString(R.string.msg_loading_error));
        }
    }

    @Override
    public void onLoaderReset(Loader<BookmarkParser.BookmarksContainer> loader) {
        hideLoadingAnimation();
    }

    private class BookmarkListAdapter extends BaseAdapter {

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
                int padding_top = v.getPaddingTop();
                int padding_bottom = v.getPaddingBottom();
                int padding_right = v.getPaddingRight();
                int padding_left = v.getPaddingLeft();

                v.setBackgroundResource(Utils.getDrawableResourceIdByAttr(getActivity(), R.attr.bbBackgroundListActive));
                v.setPadding(padding_left, padding_top, padding_right, padding_bottom);
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

    static class AsyncContentLoader extends AsyncHttpLoader<BookmarkParser.BookmarksContainer> {

        AsyncContentLoader(Context cx) {
            super(cx, BookmarkParser.URL);
        }

        @Override
        public BookmarkParser.BookmarksContainer processNetworkResponse(String response) {
            Utils.log(response);
            try {
                BookmarkParser parser = new BookmarkParser();
                return parser.parse(response);
            } catch (Exception e) {
                BookmarkParser.BookmarksContainer c = new BookmarkParser.BookmarksContainer();
                Utils.setNotLoggedIn();
                Utils.log("not logged in!");
                c.setException(e);
                return c;
            }
        }

        @Override
        protected void onNetworkFailure(int statusCode, Header[] headers,
                                        String responseBody, Throwable error) {

            Utils.printException(error);
            deliverResult(null);
        }
    }

}
