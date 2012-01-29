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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.janoliver.potdroid.helpers.ObjectManager.ParseErrorException;
import com.janoliver.potdroid.helpers.PotNotification;
import com.janoliver.potdroid.models.Board;
import com.janoliver.potdroid.models.Topic;

/**
 * This is the BoardView, showing the content of a specific mBoard. It extends
 * PaginateListActivity
 */
public class BoardActivity extends BaseListActivity {

    private Topic[] mThreads;
    private Integer mPage;
    private Board   mBoard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create Board object
        Bundle extras = getIntent().getExtras();
        mPage  = extras.getInt("page");
        mBoard = mObjectManager.getBoard(extras.getInt("BID"));

        // update category information and get thread list
        // was only the orientation changed?
        setListAdapter(null);
        final Board stateSaved = (Board) getLastNonConfigurationInstance();
        if (stateSaved == null) {
            new PrepareAdapter().execute((Void[]) null);
        } else {
            mBoard = stateSaved;
            mThreads = mBoard.getTopics().get(extras.getInt("page"));
            fillView();
        }

        // setup listview
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    openThread(mThreads[position - 1], false);
                }
            }
        });
    } 

    @Override
    public Object onRetainNonConfigurationInstance() {
        final Board stateSaved = mBoard;
        return stateSaved;
    }

    private void fillView() {

        CategoryViewAdapter adapter = new CategoryViewAdapter(BoardActivity.this);
        mListView.addHeaderView(getHeaderView());
        mListView.setAdapter(adapter);
        setTitle("Board: " + mBoard.getName());
    }

    /**
     * Opens a thread when a listitem is being clicked.
     * 
     * @param thread
     * @param lastPage
     */
    public void openThread(Topic thread, Boolean lastPage) {
        Intent intent = new Intent(BoardActivity.this, TopicActivity.class);
        intent.putExtra("TID", thread.getId());
        if (lastPage) {
            intent.putExtra("page", thread.getLastPage());
        }
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_thread, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Topic thread = mThreads[(int) info.id];
        switch (item.getItemId()) {
        case R.id.first_page:
            openThread(thread, false);
            return true;
        case R.id.last_page:
            openThread(thread, true);
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

        Integer lastPage = (int) java.lang.Math.ceil(mBoard.getNumberOfThreads()
                / (double) mBoard.getThreadsPerPage());

        TextView descr = (TextView) row.findViewById(R.id.pagetext);
        descr.setText("Seite " + mPage + "/" + lastPage);

        TextView loggedin = (TextView) row.findViewById(R.id.loggedin);
        loggedin.setText(mObjectManager.isLoggedIn() ? "Hallo "
                + mObjectManager.getCurrentUser().getNick() : "nicht eingeloggt");

        return (row);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.iconmenu_paginate, menu);
        return true;
    }

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

    public void showNextPage() {
        if ((mPage * mBoard.getThreadsPerPage()) < mBoard.getNumberOfThreads()) {
            
            try {
                mBoard = mObjectManager.getBoardByPage(mBoard.getId(), mPage+1);
                mPage++;
                refresh();
            } catch (ParseErrorException e) {
                e.printStackTrace();
            }
            
        } else if (mPage > 1) {
            try {
                mBoard = mObjectManager.getBoardByPage(mBoard.getId(), 1);
                mPage  = 1;
                refresh();
            } catch (ParseErrorException e) {
                e.printStackTrace();
            }
            
        } else {
            Toast.makeText(this, "Keine weiteren Seiten vorhanden", Toast.LENGTH_SHORT).show();
        }
    }

    public void showPreviousPage() {
        Integer lastPage = mBoard.getNumberOfPages();
        if (mPage > 1) {
            
            try {
                mBoard = mObjectManager.getBoardByPage(mBoard.getId(), mPage-1);
                mPage--;
                refresh();
            } catch (ParseErrorException e) {
                e.printStackTrace();
            }
            
        } else if (lastPage > 1) {
            try {
                mBoard = mObjectManager.getBoardByPage(mBoard.getId(), lastPage);
                mPage = lastPage;
                refresh();
            } catch (ParseErrorException e) {
                e.printStackTrace();
            }
            
        } else {
            Toast.makeText(this, "Keine weiteren Seiten vorhanden", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * @author oli
     * 
     *         Custom view adapter for the ListView items
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
    class PrepareAdapter extends AsyncTask<Void, Void, Void> {
        ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = new PotNotification(BoardActivity.this, this, true);
            mDialog.setMessage("Lade...");
            mDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mObjectManager.getBoardByPage(mBoard.getId(), mPage);
                mThreads = mBoard.getTopics().get(mPage);
            } catch (ParseErrorException e) {
                Toast.makeText(BoardActivity.this, "Verbindungsfehler!", Toast.LENGTH_LONG).show();
                this.cancel(true);
                mDialog.dismiss();
                e.printStackTrace();
            }
            
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            fillView();
            mDialog.dismiss();
        }
    }
}