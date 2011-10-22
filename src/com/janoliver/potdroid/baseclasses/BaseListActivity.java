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

package com.janoliver.potdroid.baseclasses;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.janoliver.potdroid.R;
import com.janoliver.potdroid.activities.BookmarkActivity;
import com.janoliver.potdroid.activities.ForumActivity;
import com.janoliver.potdroid.activities.PreferenceActivityPot;
import com.janoliver.potdroid.helpers.PotExceptionHandler;
import com.janoliver.potdroid.helpers.PotUtils;
import com.janoliver.potdroid.helpers.WebsiteInteraction;

/**
 * Base Activity class for all activities that are/use ListActivity. Defines
 * some more stuff than BaseActivity
 */
public abstract class BaseListActivity extends ListActivity {

    protected ListView mListView;
    protected WebsiteInteraction mWebsiteInteraction;
    
    private final int THEME_LIGHT = 0;
    private final int THEME_DARK  = 1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // set our own exception handler
        Thread.setDefaultUncaughtExceptionHandler(new PotExceptionHandler(
                PotUtils.SDCARD_ERRLOG_LOCATION, null));
        
        // set the theme
        int theme = new Integer(PreferenceManager.getDefaultSharedPreferences(this).getString(
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

    public abstract void refresh();
}
