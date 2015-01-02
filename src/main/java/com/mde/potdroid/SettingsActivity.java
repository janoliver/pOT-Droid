package com.mde.potdroid;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import com.mde.potdroid.fragments.SettingsFragment;
import com.mde.potdroid.helpers.CustomExceptionHandler;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;

/**
 * Settings Activity. Since the support lib does not contain a PreferenceFragment,
 * we use the legacy PreferenceActivity.
 */
public class SettingsActivity extends ActionBarActivity {
    protected SettingsWrapper mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSettings = new SettingsWrapper(this);

        setTheme(mSettings.getTheme());

        super.onCreate(savedInstanceState);

        // register an application context singleton in the Utils class.
        Utils.setApplicationContext(getApplicationContext());

        // debug mode. We write exceptions to the SDCard with a custom default exceptionhandler
        if (mSettings.isDebug()) {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
            }
        }

        SettingsFragment s = (SettingsFragment)getSupportFragmentManager().findFragmentByTag("settings");
        if (s == null)
            s = SettingsFragment.newInstance();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, s, "settings")
                    .commit();
        }
    }


}