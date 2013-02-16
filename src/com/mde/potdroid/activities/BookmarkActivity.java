/*
 * Copyright (C) 2012 mods.de community 
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

package com.mde.potdroid.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.ContextMenu;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.FavouritesDatabase;
import com.mde.potdroid.helpers.PotNotification;
import com.mde.potdroid.helpers.PotUtils;
import com.mde.potdroid.models.Bookmark;

/**
 * This Activity shows the bookmark list and handles all it's actions.
 */
public class BookmarkActivity extends BaseActivity {
    

    private ListView mListView;
    private DataHandler mDataHandler;
    
    /**
     * mFavouritesDatabase is the sqlite database for the favourites among
     * the bookmarks
     */
    private FavouritesDatabase mFavouritesDatabase;

    /**
     * Starting point of the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_bookmarks);
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(null);
        
        // create the favourites database
        mFavouritesDatabase = new FavouritesDatabase(this);
        
        // check the login status and redirect in case the user is not logged in.
        if (!mObjectManager.isLoggedIn()) {
            finish();
            Intent intent = new Intent(BookmarkActivity.this, ForumActivity.class);
            intent.putExtra("noredirect", true);
            startActivityForResult(intent, 1);
            return;
        }

        // initialize the data handler
        mDataHandler = (DataHandler)mFragmentManager.findFragmentByTag("data");
        if (mDataHandler == null) {
            mDataHandler = new DataHandler();
            mFragmentManager.beginTransaction().add(mDataHandler, "data").commit();
            
            // initialize the data
            mDataHandler.mBookmarks  = null;
            
            refresh();
        } else {
            fillView();
        }
        
        // register context menu and the clicklistener
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openThread((Bookmark)mDataHandler.mBookmarks[position], false, true);
            }
        });
    }
    
    /**
     * This fragment handles the data 
     */
    public static class DataHandler extends FragmentBase {
        private Bookmark[] mBookmarks;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
    
    /**
     * After having downloaded the data, fill the view
     */
    private void fillView() {
        BookmarkViewAdapter adapter = new BookmarkViewAdapter(BookmarkActivity.this);
        mListView.setAdapter(adapter);
        setTitle("Bookmarks (" + mObjectManager.getUnreadBookmarks() + " neue Posts)");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.forumact:
            goToForumActivity();
            return true;
        case R.id.preferences:
            goToPreferencesActivityPot();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.actionmenu_bookmarks, menu);
        if (mObjectManager.isLoggedIn()) {
            menu.setGroupVisible(R.id.loggedin, true);
        } else {
            menu.setGroupVisible(R.id.loggedin, false);
        }
        return true;
    }

    /**
     * Open a thread after a click on a bookmark.
     */
    public void openThread(Bookmark bm, Boolean lastPage, Boolean scroll) {
        Intent intent = new Intent(BookmarkActivity.this, TopicActivity.class);
        intent.putExtra("TID", bm.getThread().getId());
        
        if (scroll) {
            intent.putExtra("PID", bm.getLastPost().getId());
        }

        if (lastPage) {
            intent.putExtra("page", bm.getThread().getLastPage());
        }

        startActivity(intent);
    }
    
    /**
     * context menu creator
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.context_bookmark, menu);
        
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        if(mFavouritesDatabase.isFavourite((Bookmark)mDataHandler.mBookmarks[(int) info.id])) {
            menu.removeItem(R.id.add_favourite);
        } else {
            menu.removeItem(R.id.delete_favourite);
        }
    }
    
    /**
     * context menu item selected.
     * removebookmark could show a loading animation.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        ImageView star = (ImageView) info.targetView.findViewById(R.id.favourite);
        switch (item.getItemId()) {
        case R.id.first_page:
            openThread((Bookmark)mDataHandler.mBookmarks[(int) info.id], false, false);
            return true;
        case R.id.last_page:
            openThread((Bookmark)mDataHandler.mBookmarks[(int) info.id], true, false);
            return true;
        case R.id.removebookmark:
            // bookmark
            Bookmark bm = (Bookmark)mDataHandler.mBookmarks[(int) info.id];
            final String url = PotUtils.ASYNC_URL + "remove-bookmark.php?BMID=" + bm.getId()
                    + "&token=" + bm.getRemovetoken();
            new Thread(new Runnable() {
                public void run() {
                    mWebsiteInteraction.callPage(url);
                    BookmarkActivity.this.refresh();
                }
            }).start();
            return true;
        case R.id.add_favourite:
            mFavouritesDatabase.addFavourite((Bookmark)mDataHandler.mBookmarks[(int) info.id]);
            star.setVisibility(View.VISIBLE);
            return true;
        case R.id.delete_favourite:
            mFavouritesDatabase.deleteFavourite((Bookmark)mDataHandler.mBookmarks[(int) info.id]);
            star.setVisibility(View.INVISIBLE);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    /**
     * refreshes the activity
     */
    @Override
    public void refresh() {
        new PrepareAdapter().execute((Void[]) null);
    }

    /**
     * Custom view adapter for the ListView items
     */
    class BookmarkViewAdapter extends ArrayAdapter<Bookmark> {
        Activity context;

        BookmarkViewAdapter(Activity context) {
            super(context, R.layout.listitem_thread, R.id.name, mDataHandler.mBookmarks);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();

            View row = inflater.inflate(R.layout.listitem_bookmark, null);
            Bookmark bm = (Bookmark)mDataHandler.mBookmarks[position];
            ImageView star = (ImageView) row.findViewById(R.id.favourite);
            TextView name = (TextView) row.findViewById(R.id.name);
            TextView descr = (TextView) row.findViewById(R.id.description);
            TextView important = (TextView) row.findViewById(R.id.important);
            
            if(mSettings.getBoolean("notifications",false)
                    && BookmarkActivity.this.mFavouritesDatabase.isFavourite(bm))
                star.setVisibility(View.VISIBLE);
            name.setText(bm.getThread().getTitle());
            
            Spanned content = Html.fromHtml("Neue Posts: <b>"+bm.getNumberOfNewPosts()+"</b>"
                    + "   Seiten: <b>"+bm.getThread().getLastPage()+"</b>");
            descr.setText(content);
            
            if (bm.getNumberOfNewPosts() > 0)
                important.setBackgroundResource(R.color.darkred);

            return (row);
        }
    }

    /**
     * This async task shows a loader and updates the bookmark object.
     * When it is finished, the loader is hidden.
     */
    class PrepareAdapter extends AsyncTask<Void, Void, Exception> {
        ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = new PotNotification(BookmarkActivity.this, this, true);
            mDialog.setMessage("Lade...");
            mDialog.show();
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                mDataHandler.mBookmarks = mObjectManager.getBookmarks();
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception e) {
            if(e == null) {
                fillView();
                mDialog.dismiss();
                mLeftMenu.refresh();
            } else {
                Toast.makeText(BookmarkActivity.this, "Verbindungsfehler!", Toast.LENGTH_LONG).show();
                mDialog.dismiss();
                e.printStackTrace();
            }
        }
    }
}