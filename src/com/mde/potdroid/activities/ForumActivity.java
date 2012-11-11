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
import android.view.LayoutInflater;
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
import com.mde.potdroid.models.Category;
import com.mde.potdroid.models.Forum;

/**
 * In this activity, the forum and the containing categories are shown.
 */
public class ForumActivity extends BaseListActivity {
    /**
     * mCats is an array of the forum's categories. It is filled after
     * PrepareAdapter was executed.
     */
    private Category[] mCats;

    /**
     * Starting point of the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // redirect?
        if ((mExtras == null || !mExtras.getBoolean("noredirect", false))
                && !mSettings.getString("startActivity", "0").equals("0")) {
            
            finish();
            
            // redirect to bookmark?
            Intent intent = null;
            if(mSettings.getString("startActivity", "0").equals("1")) {
                intent = new Intent(ForumActivity.this, BookmarkActivity.class);
            } else if(mSettings.getString("startActivity", "0").equals("2")) {
                intent = new Intent(ForumActivity.this, BoardActivity.class);
                Integer board_id = Integer.valueOf(mSettings.getString("startForum", "14"));
                intent.putExtra("BID", board_id);
                intent.putExtra("page", 1);
            }
            startActivityForResult(intent, 1);
            return;
        }

        // the view
        setListAdapter(null); 

        // load the data and display it
        new PrepareAdapter().execute((Void[]) null);
        
        // set the touch listener
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Intent intent = new Intent(ForumActivity.this, CategoryActivity.class);
                    intent.putExtra("CID", mCats[position - 1].getId());
                    startActivity(intent);
                }
            }
        });
    }
    
    /**
     * After having downloaded the data, fill the view
     */
    private void fillView() {
        ForumViewAdapter adapter = new ForumViewAdapter(ForumActivity.this);
        mListView.addHeaderView(getHeaderView());
        mListView.setAdapter(adapter);
    }
    
    /**
     * Should be implemented someday.
     */
    @Override
    public void refresh() {};

    /**
     * Returns the header view for the list.
     */
    public View getHeaderView() {
        LayoutInflater inflater = this.getLayoutInflater();
        View row = inflater.inflate(R.layout.header_general, null);

        TextView descr = (TextView) row.findViewById(R.id.pagetext);
        descr.setText("Übersicht");

        TextView loggedin = (TextView) row.findViewById(R.id.loggedin);
        loggedin.setText(mObjectManager.isLoggedIn() ? "Hallo "
                + mObjectManager.getCurrentUser().getNick() : "nicht eingeloggt");

        return (row);
    }

    /**
     * Custom view adapter for the ListView items
     */
    class ForumViewAdapter extends ArrayAdapter<Category> {
        Activity context;

        ForumViewAdapter(Activity context) {
            super(context, R.layout.listitem_category, R.id.name, mCats);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View row = inflater.inflate(R.layout.listitem_category, null);

            TextView name = (TextView) row.findViewById(R.id.name);
            name.setText(mCats[position].getName());
            TextView descr = (TextView) row.findViewById(R.id.description);
            descr.setText(mCats[position].getDescription());

            return (row);
        }
    }

    /**
     * This async task shows a loader and updates the forum object.
     * When it is finished, the loader is hidden.
     */
    class PrepareAdapter extends AsyncTask<Void, Void, Exception> {
        ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = new PotNotification(ForumActivity.this, this, true);
            mDialog.setMessage("Lade...");
            mDialog.show();
        }

        @Override
        protected Exception doInBackground(Void... params) {
            Forum forum;
            try {
                forum = mObjectManager.getForum();
                mCats = forum.getCategories();
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
                Toast.makeText(ForumActivity.this, "Verbindungsfehler!", Toast.LENGTH_LONG).show();
                mDialog.dismiss();
                e.printStackTrace();
            }
        }
    }

}