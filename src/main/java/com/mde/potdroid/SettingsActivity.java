package com.mde.potdroid;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import com.mde.potdroid.fragments.SettingsFragment;
import com.mde.potdroid.helpers.CustomExceptionHandler;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

/**
 * Settings Activity. Since the support lib does not contain a PreferenceFragment,
 * we use the legacy PreferenceActivity.
 */
public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback {
    protected SettingsWrapper mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.Theme_pOTSettings);

        super.onCreate(savedInstanceState);

        mSettings = new SettingsWrapper(this);
        setContentView(R.layout.settings_container);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // register an application context singleton in the Utils class.
        Utils.setApplicationContext(getApplicationContext());

        // debug mode. We write exceptions to the SDCard with a custom default exceptionhandler
        if (true || mSettings.isDebug()) {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
            }
        }

        if (savedInstanceState == null) {
            SettingsFragment fragment = new SettingsFragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment, fragment);
            ft.commit();
        }
    }

    @Override
    public boolean onPreferenceStartScreen(android.support.v7.preference.PreferenceFragmentCompat caller, PreferenceScreen preferenceScreen) {
        SettingsFragment fragment = new SettingsFragment();

        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
        fragment.setArguments(args);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.fragment, fragment, preferenceScreen.getKey());
        ft.addToBackStack(preferenceScreen.getKey());
        ft.commit();

        return true;
    }
}