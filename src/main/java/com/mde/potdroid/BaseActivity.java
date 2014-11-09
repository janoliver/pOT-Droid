package com.mde.potdroid;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import com.mde.potdroid.fragments.BoardFragment;
import com.mde.potdroid.fragments.SidebarLeftFragment;
import com.mde.potdroid.fragments.SidebarRightFragment;
import com.mde.potdroid.helpers.CustomExceptionHandler;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.views.SwipeRefreshLayout;
import com.mde.potdroid.views.UpdateInfoDialog;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

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
    protected Toolbar mToolbar;
    protected SmoothProgressBar mProgressbar;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected LinearLayout mPaginateLayout;
    protected ActionBarDrawerToggle mDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // this must be called first to fix a crash in API 7
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

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

        mPaginateLayout = (LinearLayout) findViewById(R.id.paginate_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content);
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mProgressbar = (SmoothProgressBar) findViewById(R.id.progressbar);
        setSupportActionBar(mToolbar);
        setUpActionBar();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.none, R.string.none) {

            public void onDrawerClosed(View view) {}

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
        if(mSettings.getMataAction() == SettingsWrapper.START_SIDEBAR) {
            // Pass the event to ActionBarDrawerToggle, if it returns
            // true, then it has handled the app icon touch event
            return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
        } else {
            switch (item.getItemId())
            {
                case android.R.id.home:
                    if(mSettings.getMataAction() == SettingsWrapper.START_FORUM) {
                        Intent intent = new Intent(this, BoardActivity.class);
                        intent.putExtra(BoardFragment.ARG_ID, mSettings.getMataForum());
                        intent.putExtra(BoardFragment.ARG_PAGE, 1);
                        startActivity(intent);
                    } else if(mSettings.getMataAction() == SettingsWrapper.START_BOOKMARKS &&
                            Utils.isLoggedIn()) {
                        Intent intent = new Intent(this, BookmarkActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, ForumActivity.class);
                        intent.putExtra("overview", true);
                        startActivity(intent);
                    }
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.clearCroutonsForActivity(this);
    }

    public void setUpActionBar() {

        if (Utils.isLoggedIn()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);
        }
    }

    public SidebarRightFragment getRightSidebarFragment() {
        return mRightSidebar;
    }

    public SidebarLeftFragment getLeftSidebarFragment() {
        return mLeftSidebar;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public SmoothProgressBar getProgressbar() {
        return mProgressbar;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    public LinearLayout getPaginateLayout() {
        return mPaginateLayout;
    }

}

