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
    protected SidebarFragment mSidebar;
    protected FormFragment mRightSidebar;
    protected DrawerLayout mDrawerLayout;

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
        setContentView(getLayout());

        // sidebars are only needed when logged in.
        if(Utils.isLoggedIn()) {

            // find or create the left sidebar fragment
            mSidebar = (SidebarFragment) getSupportFragmentManager().findFragmentByTag(TAG_SIDEBAR_LEFT);
            if (mSidebar == null)
                mSidebar = SidebarFragment.newInstance();

            // if there is a right sidebar, i.e., the editor sidebar, take care of it
            if (hasRightSidebar()) {
                mRightSidebar = (FormFragment) getSupportFragmentManager()
                        .findFragmentByTag(TAG_SIDEBAR_RIGHT);
                if (mRightSidebar == null)
                    mRightSidebar = FormFragment.newInstance();
            }

            // find our drawerlayout
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerLayout.setDrawerListener(this);

            // add the fragments
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.sidebar_container, mSidebar, TAG_SIDEBAR_LEFT)
                        .commit();

                if (hasRightSidebar()) {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.sidebar_container_right, mRightSidebar, TAG_SIDEBAR_RIGHT)
                            .commit();
                }
            }

        }
    }

    public boolean isRightSidebarOpen() {
        return mDrawerLayout.isDrawerOpen(Gravity.RIGHT);
    }

    protected boolean hasRightSidebar() {
        return findViewById(R.id.sidebar_container_right) != null;
    }

    public SidebarFragment getSidebar() {
        return mSidebar;
    }

    public FormFragment getRightSidebar() {
        return mRightSidebar;
    }

    public void openRightSidebar() {
        mDrawerLayout.openDrawer(Gravity.RIGHT);
    }

    public void closeRightSidebar() {
        mDrawerLayout.closeDrawer(Gravity.RIGHT);
    }

    public void openLeftSidebar() {
        mDrawerLayout.openDrawer(Gravity.RIGHT);
    }

    protected int getLayout() {
        if(!Utils.isLoggedIn())
            return R.layout.layout_no_sidebar;
        return R.layout.layout_sidebar_l;
    }

    @Override
    public void onDrawerSlide(View view, float v) {
    }

    @Override
    public void onDrawerOpened(View view) {
        // if the left sidebar is opened, refresh bookmarks
        if (view.getId() == R.id.sidebar_container) {
            if (mSidebar.isDirty())
                mSidebar.refreshBookmarks();
        }

    }

    @Override
    public void onDrawerClosed(View view) {

    }

    @Override
    public void onDrawerStateChanged(int i) {

    }
}

