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

package com.mde.potdroid.baseclasses;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.mde.potdroid.R;
import com.mde.potdroid.activities.BoardActivity;
import com.mde.potdroid.activities.BookmarkActivity;
import com.mde.potdroid.activities.ForumActivity;
import com.mde.potdroid.activities.PreferenceActivityPot;
import com.mde.potdroid.helpers.ObjectManager;
import com.mde.potdroid.helpers.PotUtils;
import com.mde.potdroid.helpers.WebsiteInteraction;

/**
 * Base Activity class for all activities that are/use ListActivity. Defines
 * some more stuff than BaseActivity
 */
public abstract class BaseListActivity extends ListActivity {

    protected ListView           mListView;
    protected WebsiteInteraction mWebsiteInteraction;
    protected ObjectManager      mObjectManager;
    protected SharedPreferences  mSettings;
    protected Bundle             mExtras;
    
    private final int THEME_LIGHT = 0;
    private final int THEME_DARK  = 1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // set the theme
        int theme = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(
                "theme", "0"));
        if(theme == THEME_LIGHT)
            this.setTheme(R.style.PotLight);
        if(theme == THEME_DARK)
            this.setTheme(R.style.PotDark);
        
        // this has to be called _after_ setting the theme. God knows, why.
        super.onCreate(savedInstanceState);
        
        mListView = getListView();
        mListView.setFastScrollEnabled(true);
        mWebsiteInteraction = PotUtils.getWebsiteInteractionInstance(this);
        mObjectManager      = PotUtils.getObjectManagerInstance(this);
        mSettings           = PreferenceManager.getDefaultSharedPreferences(this);
        mExtras             = getIntent().getExtras();
    }
    
    /**
     * Override the search button
     */
    @Override
    public boolean onSearchRequested() {
        goToPresetActivity();
        return false;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.iconmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case android.R.id.home:
            goToPresetActivity();
            return true;
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

    protected void goToPreferencesActivityPot() {
        Intent intent = new Intent(this, PreferenceActivityPot.class);
        startActivity(intent);
    }

    protected void goToForumActivity() {
        Intent intent = new Intent(this, ForumActivity.class);
        intent.putExtra("noredirect", true);
        startActivity(intent);
    }

    protected void goToBookmarkActivity() {
        Intent intent = new Intent(this, BookmarkActivity.class);
        startActivity(intent);
    }
    
    protected void goToPresetActivity() {
        // app icon in action bar clicked
        int loc = Integer.valueOf(mSettings.getString("mataloc", "0"));
        switch (loc) {
        case 0:
            goToBookmarkActivity();
            break;
        case 2:
            Intent intent = new Intent(this, BoardActivity.class);
            intent.putExtra("BID", 14);
            intent.putExtra("page", 1);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            break;
        case 1:
        default:
            goToForumActivity();
            break;
        }
    }

    public abstract void refresh();
}
