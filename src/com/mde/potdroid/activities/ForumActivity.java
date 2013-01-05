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
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mde.potdroid.R;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Category;

/**
 * In this activity, the forum and the containing categories are shown.
 */
public class ForumActivity extends BaseActivity {
    /**
     * mCats is an array of the forum's categories. It is filled after
     * PrepareAdapter was executed.
     */
    private Category[] mCats;
    private ExpandableListView mListView;

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
        
        // prepare the views
        setContentView(R.layout.activity_forum);
        mListView = (ExpandableListView) findViewById(R.id.list);
        mListView.setGroupIndicator(null);
        mListView.setOnChildClickListener(new OnChildClickListener() 
        {               
            public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id) 
            { 
                Intent intent = new Intent(ForumActivity.this, BoardActivity.class);
                intent.putExtra("BID", mCats[groupPosition].getBoards()[childPosition].getId());
                intent.putExtra("CID", mCats[groupPosition].getId());
                intent.putExtra("page", 1);
                startActivity(intent);
                return true;
            }
        });
        
        // load the data and display it
        try {
            mCats = mObjectManager.getForum().getCategories();
            ForumListAdapter adapter = new ForumListAdapter(ForumActivity.this);
            mListView.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(ForumActivity.this, "Ladefehler.", Toast.LENGTH_LONG).show();
        } 
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // refresh the leftmenu stuff.
        refreshLeftMenu();
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
        case R.id.bookmarks:
            goToBookmarkActivity();
            return true;
        case R.id.refresh:
            refresh();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.actionmenu_forum, menu);
        if (mObjectManager.isLoggedIn()) {
            menu.setGroupVisible(R.id.loggedin, true);
        } else {
            menu.setGroupVisible(R.id.loggedin, false);
        }
        return true;
    }
    
    /**
     * Should be implemented someday.
     */
    @Override
    public void refresh() {};
    
    public class ForumListAdapter extends BaseExpandableListAdapter {
        Activity mContext;
        
        public ForumListAdapter(Activity context) {
            mContext = context;
        }
        
        public Object getChild(int groupPosition, int childPosition) {
            return mCats[groupPosition].getBoards()[childPosition];
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return mCats[groupPosition].getBoards().length;
        }
        
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            View row = inflater.inflate(R.layout.listitem_forum, null);

            TextView name = (TextView) row.findViewById(R.id.name);
            TextView descr = (TextView) row.findViewById(R.id.description);
            
            Board b = mCats[groupPosition].getBoards()[childPosition];
            
            name.setText(b.getName());
            descr.setText(b.getDescription());
            
            return (row);
            
        }

        public Object getGroup(int groupPosition) {
            return mCats[groupPosition];
        }

        public int getGroupCount() {
            return mCats.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            View row = inflater.inflate(R.layout.listitem_category, null);

            TextView name = (TextView) row.findViewById(R.id.name);
            TextView descr = (TextView) row.findViewById(R.id.description);
            
            name.setText(mCats[groupPosition].getName());
            descr.setText(mCats[groupPosition].getDescription());
            

            return (row);
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

    }
}