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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.view.MenuItem;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.ObjectManager;
import com.mde.potdroid.helpers.PotUtils;
import com.mde.potdroid.helpers.WebsiteInteraction;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingActivity;

/**
 * The Acitivty base class. ATM only takes care of some member variables.
 */
public abstract class BaseActivity extends SlidingActivity {

    protected WebsiteInteraction mWebsiteInteraction;
    protected ObjectManager      mObjectManager;
    protected SharedPreferences  mSettings;
    protected Bundle             mExtras;
    protected LeftMenu           mLeftMenu;
    protected FragmentManager    mFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWebsiteInteraction = PotUtils.getWebsiteInteractionInstance(this);
        mObjectManager      = PotUtils.getObjectManagerInstance(this);
        mSettings           = PreferenceManager.getDefaultSharedPreferences(this);
        mExtras             = getIntent().getExtras();
        
        // set the theme
        int theme = Integer.valueOf(mSettings.getString("theme", "0"));
        if(theme == PotUtils.THEME_LIGHT)
            this.setTheme(R.style.PotLight);
        if(theme == PotUtils.THEME_DARK)
            this.setTheme(R.style.PotDark);
        
        // sliding menu
        SlidingMenu sm = getSlidingMenu();
        sm.setMode(SlidingMenu.LEFT);
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        
        setBehindContentView(R.layout.sidebar_frame);
        
        // get or create and attach the leftmenu fragment
        mFragmentManager = (FragmentManager)getSupportFragmentManager();

        // Check to see if we have retained the worker fragment.
        mLeftMenu = (LeftMenu)mFragmentManager.findFragmentByTag("lm");

        // If not retained (or first time running), we need to create it.
        if (mLeftMenu == null) {
            mLeftMenu = new LeftMenu();
            // Tell it who it is working with.
            
            mFragmentManager.beginTransaction().add(mLeftMenu, "lm").commit();
        }
        
        mFragmentManager.beginTransaction().add(R.id.leftmenu, mLeftMenu).commit();
        
        // actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSlidingActionBarEnabled(true);
        
    }
    
    @Override
    public void onBackPressed() {
        if (getSlidingMenu().isMenuShowing()) {
            getSlidingMenu().showContent();
        } else {
            super.onBackPressed();
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case android.R.id.home:
            getSlidingMenu().showMenu();
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
    
    public void refreshLeftMenu() {
        ((LeftMenu)getSupportFragmentManager().findFragmentById(R.id.leftmenu)).refresh();
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
