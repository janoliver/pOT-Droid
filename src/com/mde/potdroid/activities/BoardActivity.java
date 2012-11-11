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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mde.potdroid.R;
import com.mde.potdroid.baseclasses.BaseListActivity;
import com.mde.potdroid.helpers.PotNotification;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Topic;

/**
 * This is the BoardView, showing the content of a specific mBoard. It extends
 * PaginateListActivity
 */
public class BoardActivity extends BaseListActivity {
    
    /**
     * mThreads is an array of all the threads currently visible on the page
     * mPage in the board mBoard.
     */
    private Topic[] mThreads;
    private Integer mPage;
    private Board   mBoard;
    
    /**
     * Starting point of the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // the URL handler
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            Uri uri = Uri.parse(getIntent().getDataString());
            Integer board_id = Integer.valueOf(uri.getQueryParameter("BID"));
            mPage = 1;
            mBoard = mObjectManager.getBoard(board_id);
        } else {
            // create Board object
            mPage  = mExtras.getInt("page");
            mBoard = mObjectManager.getBoard(mExtras.getInt("BID"));
        }

        // update category information and get thread list
        // was only the orientation changed?
        setListAdapter(null);
        final Board stateSaved = (Board) getLastNonConfigurationInstance();
        if (stateSaved == null) {
            new PrepareAdapter().execute((Void[]) null);
        } else {
            mBoard   = stateSaved;
            mThreads = mBoard.getTopics().get(mExtras.getInt("page"));
            fillView();
        }

        // setup listview
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    openThread(mThreads[position - 1], true);
                }
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){ 
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) { 
                if (position > 0) {
                    openThread(mThreads[position - 1], false);
                }
                return false; 
            } 
        });
    } 

    /**
     * Needed for orientation change
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        final Board stateSaved = mBoard;
        return stateSaved;
    }

    /**
     * After having downloaded the data, fill the view
     */
    private void fillView() {
        CategoryViewAdapter adapter = new CategoryViewAdapter(BoardActivity.this);
        mListView.addHeaderView(getHeaderView());
        mListView.setAdapter(adapter);
        setTitle("Board: " + mBoard.getName());
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
     * Returns the header view for the list.
     */
    public View getHeaderView() {
        LayoutInflater inflater = this.getLayoutInflater();
        View row = inflater.inflate(R.layout.header_general, null);

        TextView descr = (TextView) row.findViewById(R.id.pagetext);
        descr.setText("Seite " + mPage + "/" + mBoard.getNumberOfPages());

        TextView loggedin = (TextView) row.findViewById(R.id.loggedin);
        loggedin.setText(mObjectManager.isLoggedIn() ? "Hallo "
                + mObjectManager.getCurrentUser().getNick() : "nicht eingeloggt");

        return (row);
    }
    
    /**
     * options menu creator
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.iconmenu_paginate, menu);
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
        Intent intent = new Intent(BoardActivity.this, BoardActivity.class);
        intent.putExtra("CID", mBoard.getCategory().getId());
        intent.putExtra("page", mPage);
        intent.putExtra("BID", mBoard.getId());
        startActivity(intent);
    }
    
    /**
     * Shows the next page. This could maybe be changed into an intent...
     */
    public void showNextPage() {
        if (mPage < mBoard.getNumberOfPages()) {
            
            try {
                mBoard = mObjectManager.getBoardByPage(mBoard.getId(), mPage+1);
                mPage++;
                refresh();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        } else if (mPage > 1) {
            try {
                mBoard = mObjectManager.getBoardByPage(mBoard.getId(), 1);
                mPage  = 1;
                refresh();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        } else {
            Toast.makeText(this, "Keine weiteren Seiten vorhanden", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Shows the previous page. This could maybe be changed into an intent...
     */
    public void showPreviousPage() {
        if (mPage > 1) {
            
            try {
                mBoard = mObjectManager.getBoardByPage(mBoard.getId(), mPage-1);
                mPage--;
                refresh();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        } else if (mBoard.getNumberOfPages() > 1) {
            try {
                mBoard = mObjectManager.getBoardByPage(mBoard.getId(), mBoard.getNumberOfPages());
                mPage  = mBoard.getNumberOfPages();
                refresh();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
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
            super(context, R.layout.listitem_thread, R.id.name, mThreads);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View row = inflater.inflate(R.layout.listitem_thread, null);

            TextView name = (TextView) row.findViewById(R.id.name);
            name.setText(mThreads[position].getTitle());
            TextView descr = (TextView) row.findViewById(R.id.description);
            descr.setText(mThreads[position].getSubTitle());
            TextView important = (TextView) row.findViewById(R.id.important);
            if (mThreads[position].isImportant()) {
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
                mObjectManager.getBoardByPage(mBoard.getId(), mPage);
                mThreads = mBoard.getTopics().get(mPage);
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
            } else {
                Toast.makeText(BoardActivity.this, "Verbindungsfehler!", Toast.LENGTH_LONG).show();
                mDialog.dismiss();
                e.printStackTrace();
            }
        }
    }
}