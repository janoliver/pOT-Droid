package com.mde.potdroid;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.mde.potdroid.fragments.BoardFragment;
import com.mde.potdroid.fragments.SidebarLeftFragment;
import com.mde.potdroid.fragments.SidebarRightFragment;
import com.mde.potdroid.helpers.CustomExceptionHandler;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.views.UpdateInfoDialog;

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
    protected LinearLayout mPaginateLayout;
    protected LinearLayout mFastscrollLayout;
    protected FrameLayout mContentView;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected boolean mOverlayToolbars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        if(mSettings.isFixedSidebar())
            setContentView(R.layout.main_fixedsidebar);
        else
            setContentView(R.layout.main);

        mContentView = (FrameLayout) findViewById(R.id.content);

        // find our drawerlayout. If it does not exist, we are in large mode.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // this is for pagination. It is usually hidden, but PaginateFragments
        // show it if necessary.
        mPaginateLayout = (LinearLayout) findViewById(R.id.paginate_view);

        // this is for fast scrolling. It is usually hidden, but PaginateFragments
        // show it if necessary.
        mFastscrollLayout = (LinearLayout) findViewById(R.id.fastscroll_view);

        // our toolbar (the new ActionBar)
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        setupDrawerToggle();

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

            if(Utils.isLoggedIn() || !mSettings.isFixedSidebar())
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.sidebar_container_left, mLeftSidebar, TAG_SIDEBAR_LEFT).commit();
            else {
                findViewById(R.id.sidebar_container_left).setVisibility(View.GONE);
                View v = findViewById(R.id.wide_content);
                RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) v.getLayoutParams();
                p.addRule(RelativeLayout.LEFT_OF, 0);
                v.setLayoutParams(p);
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);
            }

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

    public void setupDrawerToggle() {
        // create the drawer toggle with the listeners
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.open_drawer, R.string.close_drawer) {

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

        // if user is not logged in OR no sidebar is in the drawer layout OR the user setting is
        // NOT to open the sidebar on drawertoggle click, then disable it.
        if(!Utils.isLoggedIn() || !
                mSettings.isFixedSidebar() ||
                mSettings.getMataAction() != SettingsWrapper.START_SIDEBAR) {

            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerToggle.setHomeAsUpIndicator(getV7DrawerToggleDelegate().getThemeUpIndicator());
            mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mSettings.getMataAction() == SettingsWrapper.START_FORUM) {
                        Intent intent = new Intent(BaseActivity.this, BoardActivity.class);
                        intent.putExtra(BoardFragment.ARG_ID, mSettings.getMataForum());
                        intent.putExtra(BoardFragment.ARG_PAGE, 1);
                        startActivity(intent);
                    } else if(mSettings.getMataAction() == SettingsWrapper.START_BOOKMARKS &&
                            Utils.isLoggedIn()) {
                        Intent intent = new Intent(BaseActivity.this, BookmarkActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(BaseActivity.this, ForumActivity.class);
                        intent.putExtra("overview", true);
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

    public SidebarRightFragment getRightSidebarFragment() {
        return mRightSidebar;
    }

    public SidebarLeftFragment getLeftSidebarFragment() {
        return mLeftSidebar;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public LinearLayout getPaginateLayout() {
        return mPaginateLayout;
    }

    public LinearLayout getFastscrollLayout() {
        return mFastscrollLayout;
    }

    public FrameLayout getContentView() {
        return mContentView;
    }

    public void setOverlayToolbars() {
        mOverlayToolbars = true;
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)mContentView.getLayoutParams();
        p.addRule(RelativeLayout.BELOW, 0);
        p.addRule(RelativeLayout.ABOVE, 0);
        mContentView.setLayoutParams(p);
    }

    public boolean getOverlayToolbars() {
        return mOverlayToolbars;
    }

    public void setSolidToolbars() {
        mOverlayToolbars = false;
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)mContentView.getLayoutParams();
        p.addRule(RelativeLayout.BELOW, R.id.main_toolbar);
        p.addRule(RelativeLayout.ABOVE, R.id.paginate_view);
        mContentView.setLayoutParams(p);
    }

    public void hidePaginateView() {
        if(!mOverlayToolbars) {
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
            p.addRule(RelativeLayout.ABOVE, 0);
            mContentView.setLayoutParams(p);
        }
        mPaginateLayout.setVisibility(View.GONE);
    }

    public void showPaginateView() {
        if(!mOverlayToolbars) {
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
            p.addRule(RelativeLayout.ABOVE, R.id.paginate_view);
            mContentView.setLayoutParams(p);
        }
        mPaginateLayout.setVisibility(View.VISIBLE);
    }

    public void hideFastscrollView() {
        mFastscrollLayout.setVisibility(View.GONE);
    }

    public void showFastscrollView() {
        mFastscrollLayout.setVisibility(View.VISIBLE);
    }

}

