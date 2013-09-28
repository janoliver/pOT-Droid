package com.mde.potdroid3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Window;
import com.mde.potdroid3.fragments.SidebarFragment;

public class BaseActivity extends Activity {

    protected SharedPreferences mSettings;
    protected Bundle mExtras;
    protected SidebarFragment mSidebar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mExtras = getIntent().getExtras();

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(getLayout());

        mSidebar = (SidebarFragment)getFragmentManager().findFragmentByTag("sidebar");
        if(mSidebar == null)
            mSidebar = SidebarFragment.newInstance();

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerListener(mSidebar);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.sidebar_container, mSidebar, "sidebar")
                    .commit();
        }

    }

    protected int getLayout() {
        return R.layout.layout_activity_single_fragment;
    }

}

