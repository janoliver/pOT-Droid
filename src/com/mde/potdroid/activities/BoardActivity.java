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
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.PotNotification;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Topic;

/**
 * This is the BoardView, showing the content of a specific mBoard. It extends
 * PaginateListActivity
 */
public class BoardActivity extends BaseActivity {
    
    /**
     * mThreads is an array of all the threads currently visible on the page
     * mPage in the board mBoard.
     */
    
    private ListView mListView;
    private DataHandler mDataHandler;
    
    
    /**
     * Starting point of the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // prepare the views
        setContentView(R.layout.activity_board);
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(null);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openThread(mDataHandler.mThreads[position], true);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){ 
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) { 
                openThread(mDataHandler.mThreads[position], false);
                return false; 
            } 
        });

        // initialize the data handler
        mDataHandler = (DataHandler)mFragmentManager.findFragmentByTag("data");
        if (mDataHandler == null) {
            mDataHandler = new DataHandler();
            mFragmentManager.beginTransaction().add(mDataHandler, "data").commit();
            
            // initialize the data
            mDataHandler.mBoard  = mObjectManager.getBoard(mExtras.getInt("BID"));
            mDataHandler.mPage   = mExtras.getInt("page",1);
            
            refresh();
        } else {
            fillView();
        }
        
        
    }
    
    
    /**
     * This fragment handles the data 
     */
    public static class DataHandler extends FragmentBase {
        private Topic[] mThreads;
        private Integer mPage;
        private Board   mBoard;
        
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
        CategoryViewAdapter adapter = new CategoryViewAdapter(BoardActivity.this);
        mListView.setAdapter(adapter);
        setTitle(mDataHandler.mBoard.getName());
        getSupportActionBar().setSubtitle("Seite " + mDataHandler.mPage);
    }

    /**
     * Opens a thread when a listitem is being clicked.
     */
    public void openThread(Topic thread, Boolean lastPage) {
        Intent intent = new Intent(BoardActivity.this, TopicActivity.class);
        intent.putExtra("TID", thread.getId());
        if (lastPage) {
            intent.putExtra("page", thread.getLastPage());
        }
        startActivity(intent);
    }
    
    /**
     * options menu creator
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.actionmenu_board, menu);
        if (mObjectManager.isLoggedIn()) {
            menu.setGroupVisible(R.id.loggedin, true);
        } else {
            menu.setGroupVisible(R.id.loggedin, false);
        }
        return true;
    }
    
    /**
     * options menu item selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.next:
            showNextPage();
            return true;
        case R.id.previous:
            showPreviousPage();
            return true;
        default:
            return super.onOptionsItemSelected(item);
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
     * Shows the next page. This could maybe be changed into an intent...
     */
    public void showNextPage() {
        if (mDataHandler.mPage < mDataHandler.mBoard.getNumberOfPages()) {
            mDataHandler.mPage++;
            refresh();
        } else if (mDataHandler.mPage > 1) {
            mDataHandler.mPage  = 1;
            refresh();
        } else {
            Toast.makeText(this, "Keine weiteren Seiten vorhanden", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Shows the previous page. This could maybe be changed into an intent...
     */
    public void showPreviousPage() {
        if (mDataHandler.mPage > 1) {
            mDataHandler.mPage--;
            refresh();
        } else if (mDataHandler.mPage < mDataHandler.mBoard.getNumberOfPages()) {
            mDataHandler.mPage = mDataHandler.mBoard.getNumberOfPages();
            refresh();
        } else {
            Toast.makeText(this, "Keine weiteren Seiten vorhanden", Toast.LENGTH_SHORT).show();
        }
    }
    
    
    /**
     * Custom view adapter for the ListView items
     */
    class CategoryViewAdapter extends ArrayAdapter<Topic> {
        Activity context;

        CategoryViewAdapter(Activity context) {
            super(context, R.layout.listitem_thread, R.id.name, mDataHandler.mThreads);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View row = inflater.inflate(R.layout.listitem_thread, null);
            Topic t = mDataHandler.mThreads[position];

            TextView name = (TextView) row.findViewById(R.id.name);
            TextView descr = (TextView) row.findViewById(R.id.description);
            TextView lastpost = (TextView) row.findViewById(R.id.lastpost);
            TextView important = (TextView) row.findViewById(R.id.important);

            name.setText(t.getTitle());
            if(t.isClosed())
                name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            
            descr.setText(t.getSubTitle());
            Spanned content = Html.fromHtml("<b>"+t.getNumberOfPosts()+"</b> Posts auf <b>"+t.getLastPage()+"</b> Seiten");
            lastpost.setText(content);
            
            if (mDataHandler.mThreads[position].isImportant()) {
                important.setVisibility(View.GONE);
            } else if(mDataHandler.mThreads[position].isSticky()) {
                important.setBackgroundResource(R.color.darkred);
            }

            return (row);
        }
    }

    /**
     * This class starts an async task (opening another system thread) to
     * preload the view. It shows and handles the progressbar and the messages
     * to the user. The magic happens in the doInBackground() method.
     */
    class PrepareAdapter extends AsyncTask<Void, Void, Exception> {
        ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = new PotNotification(BoardActivity.this, this, true);
            mDialog.setMessage("Lade...");
            mDialog.show();
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                mObjectManager.getBoardByPage(mDataHandler.mBoard.getId(), mDataHandler.mPage);
                mDataHandler.mThreads = mDataHandler.mBoard.getTopics().get(mDataHandler.mPage);
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
                Toast.makeText(BoardActivity.this, "Verbindungsfehler!", Toast.LENGTH_LONG).show();
                mDialog.dismiss();
                e.printStackTrace();
            }
        }
    }
    
}