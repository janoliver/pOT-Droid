package com.mde.potdroid;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;

import com.mde.potdroid.fragments.FormFragment;
import com.mde.potdroid.fragments.SidebarFragment;
import com.mde.potdroid.helpers.CustomExceptionHandler;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;

/**
 * The Class all activities should extend. It mainly handles the sidebar(s)
 */
public class BaseActivity extends ActionBarActivity implements DrawerLayout.DrawerListener
{

    protected static final String TAG_SIDEBAR_LEFT = "sidebar-left";
    protected static final String TAG_SIDEBAR_RIGHT = "sidebar-right";
    protected SettingsWrapper mSettings;
    protected Bundle mExtras;
    protected SidebarFragment mLeftSidebar;
    protected FormFragment mRightSidebar;
    protected DrawerLayout mDrawerLayout;
    protected Boolean mDualPane = false;

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

        // find out, if we are in dual pane mode
        if(findViewById(R.id.dual_pane_container) != null)
            mDualPane = true;

        // see getLayout function. We implement it as a function
        // so it can be overridden for a custom layout.
        setContentView(R.layout.main);

        // find our drawerlayout. If it does not exist, we are in large mode.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);

        // find or create the left sidebar fragment
        mLeftSidebar = (SidebarFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_SIDEBAR_LEFT);
        if (mLeftSidebar == null)
            mLeftSidebar = SidebarFragment.newInstance();

        mRightSidebar = (FormFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_SIDEBAR_RIGHT);
        if (mRightSidebar == null)
            mRightSidebar = FormFragment.newInstance();

        // add the fragments
        if (savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.sidebar_container_left, mLeftSidebar, TAG_SIDEBAR_LEFT).commit();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.sidebar_container_right, mRightSidebar, TAG_SIDEBAR_RIGHT).commit();
        }

        // disable the fragments first, the extending activities must enable them
        // as needed.
        disableLeftSidebar();
        disableRightSidebar();

    }

    @Override
    public void onDrawerSlide(View view, float v) {}

    @Override
    public void onDrawerOpened(View view) {
        // if the left sidebar is opened, refresh bookmarks
        if (view.getId() == R.id.sidebar_container_left) {
            if (mLeftSidebar.isDirty())
                mLeftSidebar.refreshBookmarks();
        }

    }

    @Override
    public void onDrawerClosed(View view) {}

    @Override
    public void onDrawerStateChanged(int i) {}

    public void closeRightSidebar() {
        mDrawerLayout.closeDrawer(Gravity.RIGHT);
    }

    public void closeLeftSidebar() {
        mDrawerLayout.closeDrawer(Gravity.RIGHT);
    }

    public void enableLeftSidebar() {
        if (Utils.isLoggedIn())
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.LEFT);
    }

    public void enableRightSidebar() {
        if (Utils.isLoggedIn())
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
    }

    public void disableLeftSidebar() {
        if (Utils.isLoggedIn())
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);
    }

    public void disableRightSidebar() {
        if (Utils.isLoggedIn())
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
    }

    public FormFragment getRightSidebarFragment() {
        return mRightSidebar;
    }

    public SidebarFragment getLeftSidebarFragment() {
        return mLeftSidebar;
    }

    public void openLeftSidebar() {
        mDrawerLayout.openDrawer(Gravity.RIGHT);
    }

    public void openRightSidebar() {
        mDrawerLayout.openDrawer(Gravity.RIGHT);
    }

    public Boolean isDualPane() {
        return mDualPane;
    }
}

