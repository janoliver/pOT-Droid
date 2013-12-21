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

public class BaseActivity extends ActionBarActivity implements DrawerLayout.DrawerListener {

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

        mSettings = new SettingsWrapper(this);
        mExtras = getIntent().getExtras();

        // debug mode. We write exceptions to the SDCard with a custom default exceptionhandler
        if(mSettings.isDebug()) {
            if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
            }
        }

        setContentView(getLayout());

        // find sidebar fragment
        mSidebar = (SidebarFragment)getSupportFragmentManager().findFragmentByTag("sidebar");
        if(mSidebar == null)
            mSidebar = SidebarFragment.newInstance();

        // if there is a right sidebar, i.e., the editor sidebar, take care of it
        if(hasRightSidebar()) {
            mRightSidebar = (FormFragment)getSupportFragmentManager().findFragmentByTag("sidebar_right");
            if(mRightSidebar == null)
                mRightSidebar = FormFragment.newInstance();
        }

        // find our drawerlayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);

        // add the fragments
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.sidebar_container, mSidebar, "sidebar")
                    .commit();

            if(hasRightSidebar()) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.sidebar_container_right, mRightSidebar, "sidebar_right")
                        .commit();
            }
        }
    }

    public boolean isRightDrawerOpen() {
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
        return R.layout.layout_activity_single_fragment;
    }

    @Override
    public void onDrawerSlide(View view, float v) {}

    @Override
    public void onDrawerOpened(View view) {
        // if the left sidebar is opened, refresh bookmarks
        if(view.getId() == R.id.sidebar_container) {
            if(mSidebar.isDirty())
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

