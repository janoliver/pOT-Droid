package com.mde.potdroid;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import com.mde.potdroid.fragments.SidebarLeftFragment;
import com.mde.potdroid.fragments.SidebarRightFragment;
import com.mde.potdroid.helpers.CustomExceptionHandler;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.views.UpdateInfoDialog;
import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * The Class all activities should extend. It mainly handles the sidebar(s)
 */
public class BaseActivity extends ActionBarActivity {

    protected static final String TAG_SIDEBAR_LEFT = "sidebar-left";
    protected static final String TAG_SIDEBAR_RIGHT = "sidebar-right";
    protected SettingsWrapper mSettings;
    protected Bundle mExtras;
    protected SidebarLeftFragment mLeftSidebar;
    protected SidebarRightFragment mRightSidebar;
    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // this must be called first to fix a crash in API 7
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        // register an application context singleton in the Utils class.
        Utils.setApplicationContext(getApplicationContext());

        mSettings = new SettingsWrapper(this);
        mExtras = getIntent().getExtras();

        // debug mode. We write exceptions to the SDCard with a custom default exceptionhandler
        if (mSettings.isDebug()) {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
            }
        }

        // see getLayout function. We implement it as a function
        // so it can be overridden for a custom layout.
        setContentView(R.layout.main);

        // find our drawerlayout. If it does not exist, we are in large mode.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.none, R.string.none) {

            public void onDrawerClosed(View view) {
            }

            public void onDrawerOpened(View view) {
                // if the left sidebar is opened, refresh bookmarks
                if (view.getId() == R.id.sidebar_container_left) {
                    if (mLeftSidebar.isDirty())
                        mLeftSidebar.refreshBookmarks();
                }

                // if the right sidebar is opened, refresh boards
                if (view.getId() == R.id.sidebar_container_right) {
                    if (mRightSidebar.isDirty())
                        mRightSidebar.refreshBoards();
                }
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // find or create the left sidebar fragment
        mLeftSidebar = (SidebarLeftFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_SIDEBAR_LEFT);
        if (mLeftSidebar == null)
            mLeftSidebar = SidebarLeftFragment.newInstance();

        mRightSidebar = (SidebarRightFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_SIDEBAR_RIGHT);
        if (mRightSidebar == null)
            mRightSidebar = SidebarRightFragment.newInstance();

        // add the fragments
        if (savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.sidebar_container_left, mLeftSidebar, TAG_SIDEBAR_LEFT).commit();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.sidebar_container_right, mRightSidebar, TAG_SIDEBAR_RIGHT).commit();
        }

        if (Utils.isLoggedIn()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);
        }

        // first start info
        SettingsWrapper settings = new SettingsWrapper(this);
        if (settings.isVersionUpdate(this)) {
            settings.registerVersion(this);

            UpdateInfoDialog d = new UpdateInfoDialog();
            d.show(getSupportFragmentManager(), "update_dialog");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.clearCroutonsForActivity(this);
    }

    public SidebarRightFragment getRightSidebarFragment() {
        return mRightSidebar;
    }

    public SidebarLeftFragment getLeftSidebarFragment() {
        return mLeftSidebar;
    }

}

