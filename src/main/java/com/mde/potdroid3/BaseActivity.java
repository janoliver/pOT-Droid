package com.mde.potdroid3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import com.mde.potdroid3.fragments.FormFragment;
import com.mde.potdroid3.fragments.SidebarFragment;

public class BaseActivity extends Activity implements DrawerLayout.DrawerListener {

    protected SharedPreferences mSettings;
    protected Bundle mExtras;
    protected SidebarFragment mSidebar;
    protected FormFragment mRightSidebar;
    protected DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mExtras = getIntent().getExtras();

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(getLayout());

        // find sidebar fragment
        mSidebar = (SidebarFragment)getFragmentManager().findFragmentByTag("sidebar");
        if(mSidebar == null)
            mSidebar = SidebarFragment.newInstance();

        // if there is a right sidebar, i.e., the editor sidebar, take care of it
        if(hasRightSidebar()) {
            mRightSidebar = (FormFragment)getFragmentManager().findFragmentByTag("sidebar_right");
            if(mRightSidebar == null)
                mRightSidebar = FormFragment.newInstance();
        }

        // find our drawerlayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);

        // add the fragments
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.sidebar_container, mSidebar, "sidebar")
                    .commit();

            if(hasRightSidebar()) {
                getFragmentManager().beginTransaction()
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

