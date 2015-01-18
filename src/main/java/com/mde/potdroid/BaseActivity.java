package com.mde.potdroid;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.mde.potdroid.fragments.BoardFragment;
import com.mde.potdroid.fragments.SidebarBoardsFragment;
import com.mde.potdroid.fragments.SidebarBookmarksFragment;
import com.mde.potdroid.helpers.CustomExceptionHandler;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.views.UpdateInfoDialog;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * The Class all activities should extend. It mainly handles the sidebar(s)
 */
public class BaseActivity extends ActionBarActivity {

    protected static final String TAG_SIDEBAR_BOOKMARKS = "sidebar-bookmarks";
    protected static final String TAG_SIDEBAR_BOARDS = "sidebar-boards";
    protected SettingsWrapper mSettings;
    protected Bundle mExtras;
    protected SidebarBookmarksFragment mBookmarksSidebar;
    protected SidebarBoardsFragment mBoardsSidebar;
    protected DrawerLayout mDrawerLayout;
    protected Toolbar mToolbar;
    protected RelativeLayout mBottomToolbar;
    protected LinearLayout mFastscrollLayout;
    protected FrameLayout mContentView;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected boolean mOverlayToolbars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // load default preference values from xml
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        mSettings = new SettingsWrapper(this);

        setTheme(mSettings.getTheme());

        super.onCreate(savedInstanceState);

        // register an application context singleton in the Utils class.
        Utils.setApplicationContext(getApplicationContext());

        mExtras = getIntent().getExtras();

        // debug mode. We write exceptions to the SDCard with a custom default exceptionhandler
        if (mSettings.isDebug()) {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
            }
        }

        // see getLayout function. We implement it as a function
        // so it can be overridden for a custom layout.
        if (mSettings.isFixedSidebar())
            setContentView(R.layout.main_fixedsidebar);
        else
            setContentView(R.layout.main);

        // tinted statusbar
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(Utils.getColorByAttr(this, R.attr.colorPrimary));

        }

        mContentView = (FrameLayout) findViewById(R.id.content);

        // find our drawerlayout. If it does not exist, we are in large mode.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // this is for fast scrolling. It is usually hidden, but PaginateFragments
        // show it if necessary.
        mFastscrollLayout = (LinearLayout) findViewById(R.id.fastscroll_view);

        // our toolbar (the new ActionBar)
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mBottomToolbar = (RelativeLayout) findViewById(R.id.bottom_toolbar);

        setSupportActionBar(mToolbar);

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
                this, mDrawerLayout, mToolbar, R.string.open_drawer, R.string.close_drawer) {

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
        if (!Utils.isLoggedIn() ||
                mSettings.isFixedSidebar() ||
                mSettings.getMataAction() != SettingsWrapper.START_SIDEBAR) {

            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerToggle.setHomeAsUpIndicator(getV7DrawerToggleDelegate().getThemeUpIndicator());
            mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSettings.getMataAction() == SettingsWrapper.START_FORUM) {
                        Intent intent = new Intent(BaseActivity.this, BoardActivity.class);
                        intent.putExtra(BoardFragment.ARG_ID, mSettings.getMataForum());
                        intent.putExtra(BoardFragment.ARG_PAGE, 1);
                        startActivity(intent);
                    } else if (mSettings.getMataAction() == SettingsWrapper.START_BOOKMARKS &&
                            Utils.isLoggedIn()) {
                        Intent intent = new Intent(BaseActivity.this, BookmarkActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(BaseActivity.this, ForumActivity.class);
                        startActivity(intent);
                    }
                }
            });
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
        return mToolbar;
    }

    public RelativeLayout getBottomToolbar() {
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

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

}

