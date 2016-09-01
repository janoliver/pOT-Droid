package com.mde.potdroid;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.mde.potdroid.fragments.SidebarBoardsFragment;
import com.mde.potdroid.fragments.SidebarBookmarksFragment;
import com.mde.potdroid.helpers.CustomExceptionHandler;
import com.mde.potdroid.helpers.DatabaseWrapper;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.views.UpdateInfoDialog;

/**
 * The Class all activities should extend. It mainly handles the sidebar(s)
 */
public class BaseActivity extends AppCompatActivity {

    // the fragment tags
    protected static final String TAG_SIDEBAR_BOOKMARKS = "sidebar-bookmarks";
    protected static final String TAG_SIDEBAR_BOARDS = "sidebar-boards";

    // these are some variables to be available in inheriting classes. Access to settings,
    // database, Extras and so on.
    protected SettingsWrapper mSettings;
    protected Bundle mExtras;
    protected DatabaseWrapper mDatabase;

    // the two sidebars and the toolbars
    protected SidebarBookmarksFragment mBookmarksSidebar;
    protected SidebarBoardsFragment mBoardsSidebar;
    protected Toolbar mTopToolbar;
    protected Toolbar mBottomToolbar;

    // layout
    protected DrawerLayout mDrawerLayout;
    protected LinearLayout mFastscrollLayout;
    protected FrameLayout mContentView;
    protected ActionBarDrawerToggle mDrawerToggle;

    // determine the overlay behaviour
    protected boolean mOverlayToolbars;

    protected ExternalPermissionCallback mPermissionCallback;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // load default preference values from xml
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mSettings = new SettingsWrapper(this);

        setTheme(mSettings.getTheme());

        // all of the above calls should be done before calling the super class's
        // onCreate method.
        super.onCreate(savedInstanceState);

        // register an application context singleton in the Utils class.
        Utils.setApplicationContext(getApplicationContext());

        mExtras = getIntent().getExtras();
        mDatabase = new DatabaseWrapper(this);

        // debug mode. We write exceptions to the SDCard with a custom default exceptionhandler
        if (mSettings.isDebug()) {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
            }
        }

        // determine how to lay out the views
        if (mSettings.isFixedSidebar())
            setContentView(R.layout.main_fixedsidebar);
        else
            setContentView(R.layout.main);


        mContentView = (FrameLayout) findViewById(R.id.content);

        // find our drawerlayout. If it does not exist, we are in large mode.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // this is for fast scrolling. It is usually hidden, but PaginateFragments
        // show it if necessary.
        mFastscrollLayout = (LinearLayout) findViewById(R.id.fastscroll_view);

        // our toolbar (the new ActionBar)
        mTopToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mBottomToolbar = (Toolbar) findViewById(R.id.bottom_toolbar);

        setSupportActionBar(mTopToolbar);

        setupDrawerToggle();

        // find or create the left sidebar fragment
        mBookmarksSidebar = (SidebarBookmarksFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_SIDEBAR_BOOKMARKS);
        if (mBookmarksSidebar == null)
            mBookmarksSidebar = SidebarBookmarksFragment.newInstance();

        mBoardsSidebar = (SidebarBoardsFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_SIDEBAR_BOARDS);
        if (mBoardsSidebar == null)
            mBoardsSidebar = SidebarBoardsFragment.newInstance();

        // add the fragments
        if (savedInstanceState == null) {

            int bookmarks_sidebar_target = R.id.sidebar_container_bookmarks;
            int boards_sidebar_target = R.id.sidebar_container_boards;
            int boards_gravity = Gravity.RIGHT;

            // swap the sidebar gravity if swapped sidebars is set to on
            if (!mSettings.isFixedSidebar() && mSettings.isSwappedSidebars()) {
                bookmarks_sidebar_target = R.id.sidebar_container_boards;
                boards_sidebar_target = R.id.sidebar_container_bookmarks;
                boards_gravity = Gravity.LEFT;
            }

            getSupportFragmentManager().beginTransaction().add(
                    bookmarks_sidebar_target, mBookmarksSidebar, TAG_SIDEBAR_BOOKMARKS).commit();

            getSupportFragmentManager().beginTransaction().add(
                    boards_sidebar_target, mBoardsSidebar, TAG_SIDEBAR_BOARDS).commit();

            if (!mSettings.isBoardBookmarks())
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, boards_gravity);

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

    public void setupDrawerToggle() {
        // create the drawer toggle with the listeners
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mTopToolbar, R.string.open_drawer, R.string.close_drawer) {

            public void onDrawerClosed(View view) {
            }

            public void onDrawerOpened(View view) {
                // if the left sidebar is opened, refresh bookmarks
                if (view.getId() == R.id.sidebar_container_bookmarks) {
                    if (mBookmarksSidebar.isDirty())
                        mBookmarksSidebar.refreshBookmarks();
                }

                // if the right sidebar is opened, refresh boards
                if (view.getId() == R.id.sidebar_container_boards) {
                    if (mBoardsSidebar.isDirty())
                        mBoardsSidebar.refreshBoards();
                }
            }
        };

        // if user is not logged in OR no sidebar is in the drawer layout OR the user setting is
        // NOT to open the sidebar on drawertoggle click, then disable it.
        if (!Utils.isLoggedIn() || mSettings.isFixedSidebar()) {

            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerToggle.setHomeAsUpIndicator(null);
        }

        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public SidebarBoardsFragment getRightSidebarFragment() {
        return mBoardsSidebar;
    }

    public SidebarBookmarksFragment getLeftSidebarFragment() {
        return mBookmarksSidebar;
    }

    public Toolbar getToolbar() {
        return mTopToolbar;
    }

    public Toolbar getBottomToolbar() {
        return mBottomToolbar;
    }

    public LinearLayout getFastscrollLayout() {
        return mFastscrollLayout;
    }

    public FrameLayout getContentView() {
        return mContentView;
    }

    public void setOverlayToolbars() {
        mOverlayToolbars = true;
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
        p.addRule(RelativeLayout.BELOW, 0);
        p.addRule(RelativeLayout.ABOVE, 0);
        mContentView.setLayoutParams(p);
    }

    public boolean getOverlayToolbars() {
        return mOverlayToolbars;
    }

    public void setSolidToolbars() {
        mOverlayToolbars = false;
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
        p.addRule(RelativeLayout.BELOW, R.id.main_toolbar);

        if(mSettings.isBottomToolbar())
            p.addRule(RelativeLayout.ABOVE, R.id.bottom_toolbar);
        mContentView.setLayoutParams(p);
    }

    public void disableBottomToolbar() {
        getBottomToolbar().setVisibility(View.GONE);
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
        p.addRule(RelativeLayout.ABOVE, 0);
        mContentView.setLayoutParams(p);
    }

    public void enableBottomToolbar() {
        getBottomToolbar().setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
        p.addRule(RelativeLayout.ABOVE, R.id.bottom_toolbar);
        mContentView.setLayoutParams(p);
    }

    public void hideFastscrollView() {
        mFastscrollLayout.setVisibility(View.GONE);
    }

    public void showFastscrollView() {
        mFastscrollLayout.setVisibility(View.VISIBLE);
    }

    public void closeLeftDrawer() {
        if (mDrawerLayout != null && !mSettings.isFixedSidebar())
            mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    public void closeRightDrawer() {
        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mPermissionCallback.granted();
                } else {
                    mPermissionCallback.denied();
                }
                return;
            }
        }
    }


    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public void verifyStoragePermissions(Activity activity, ExternalPermissionCallback callback) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        mPermissionCallback = callback;

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        } else {
            mPermissionCallback.granted();
        }
    }

    public interface ExternalPermissionCallback {
        void granted();
        void denied();
    }
}

