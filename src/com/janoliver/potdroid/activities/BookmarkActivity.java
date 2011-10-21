/*
 * Copyright (C) 2011 Jan Oliver Oelerich <janoliver@oelerich.org>
 *
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this software, and changing it is allowed as long as the 
 * name is changed.
 *
 *           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *  0. You just DO WHAT THE FUCK YOU WANT TO. 
 */

package com.janoliver.potdroid.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.janoliver.potdroid.R;
import com.janoliver.potdroid.baseclasses.BaseListActivity;
import com.janoliver.potdroid.helpers.PotNotification;
import com.janoliver.potdroid.helpers.PotUtils;
import com.janoliver.potdroid.models.Bookmark;
import com.janoliver.potdroid.models.Bookmarklist;

/**
 * This Activity shows the bookmark list and handles all it's actions.
 */
public class BookmarkActivity extends BaseListActivity {

    private Bookmark[] mBookmarks;
    private Bookmarklist mBookmarkList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!mWebsiteInteraction.loggedIn()) {
            finish();
            Intent intent = new Intent(BookmarkActivity.this, ForumActivity.class);
            intent.putExtra("noredirect", true);
            startActivityForResult(intent, 1);
            return;
        }

        // set view
        final Bookmarklist stateSaved = (Bookmarklist) getLastNonConfigurationInstance();
        if (stateSaved == null) {
            setListAdapter(null);
            new PrepareAdapter().execute((Void[]) null);
        } else {
            mBookmarkList = stateSaved;
            mBookmarks = mBookmarkList.getBookmarks();
            fillView();
        }
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    openThread(mBookmarks[position - 1], false, true);
                }
            }
        });
    }

    private void fillView() {
        BookmarkViewAdapter adapter = new BookmarkViewAdapter(BookmarkActivity.this);
        mListView.addHeaderView(getHeaderView());
        mListView.setAdapter(adapter);

        setTitle("Bookmarks (" + mBookmarkList.getUnread() + " neue Posts)");
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        final Bookmarklist stateSaved = mBookmarkList;
        return stateSaved;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Open a thread after a click on a bookmark.
     */
    public void openThread(Bookmark bm, Boolean lastPage, Boolean scroll) {
        Intent intent = new Intent(BookmarkActivity.this, TopicActivity.class);
        intent.putExtra("TID", bm.getThread().getId());
        
        if (scroll) {
            intent.putExtra("PID", bm.getLastPost());
        }

        if (lastPage) {
            intent.putExtra("page", bm.getThread().getLastPage());
        }

        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notifications", false)) {
            inflater.inflate(R.menu.context_bookmark, menu);
        } else {
            inflater.inflate(R.menu.context_bookmark, menu);
        } 
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.first_page:
            openThread(mBookmarks[(int) info.id], false, false);
            return true;
        case R.id.last_page:
            openThread(mBookmarks[(int) info.id], true, false);
            return true;
        case R.id.removebookmark:
            // bookmark
            Bookmark bm = mBookmarks[(int) info.id];
            final String url = PotUtils.ASYNC_URL + "remove-bookmark.php?BMID=" + bm.getId()
                    + "&token=" + bm.getRemovetoken();
            new Thread(new Runnable() {
                public void run() {
                    mWebsiteInteraction.callPage(url);

                    BookmarkActivity.this.refresh();

                }
            }).start();
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    /**
     * Returns the header view for the list.
     * 
     * @return View header
     */
    public View getHeaderView() {
        LayoutInflater inflater = this.getLayoutInflater();
        View row = inflater.inflate(R.layout.header_general, null);

        TextView descr = (TextView) row.findViewById(R.id.pagetext);
        descr.setText("Bookmarks: " + mBookmarkList.getNumberOfThreads());

        TextView loggedin = (TextView) row.findViewById(R.id.loggedin);
        loggedin.setText(mWebsiteInteraction.loggedIn() ? "Hallo "
                + mWebsiteInteraction.getUserName() : "nicht eingeloggt");

        return (row);
    }

    /**
     * refreshes the activity
     */
    @Override
    public void refresh() {
        Intent intent = new Intent(BookmarkActivity.this, BookmarkActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * @author oli Custom view adapter for the ListView items
     */
    class BookmarkViewAdapter extends ArrayAdapter<Bookmark> {
        Activity context;

        BookmarkViewAdapter(Activity context) {
            super(context, R.layout.listitem_thread, R.id.name, mBookmarks);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();

            View row = inflater.inflate(R.layout.listitem_bookmark, null);

            TextView name = (TextView) row.findViewById(R.id.name);
            name.setText(mBookmarks[position].getThread().getTitle());
            TextView descr = (TextView) row.findViewById(R.id.description);
            descr.setText("Neue Posts: " + mBookmarks[position].getNumberOfNewPosts());
            TextView important = (TextView) row.findViewById(R.id.important);
            if (mBookmarks[position].getNumberOfNewPosts() > 0) {
                important.setBackgroundResource(R.color.darkred);
            }

            return (row);
        }
    }

    /**
     * @author oli
     * 
     *         This class starts an async task (opening another system thread)
     *         to preload the view. It shows and handles the progressbar and the
     *         messages to the user. The magic happens in the doInBackground()
     *         method.
     */
    class PrepareAdapter extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new PotNotification(BookmarkActivity.this, this, true);
            dialog.setMessage("Lade...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mBookmarkList = new Bookmarklist();
            if (mBookmarkList.update(BookmarkActivity.this)) {
                mBookmarks = mBookmarkList.getBookmarks();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nix) {
            if (mBookmarks != null) {
                fillView();
            } else {
                Toast.makeText(BookmarkActivity.this, "Verbindungsfehler!", Toast.LENGTH_LONG)
                        .show();
            }
            dialog.dismiss();
        }
    }
}