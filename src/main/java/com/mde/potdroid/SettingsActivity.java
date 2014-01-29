package com.mde.potdroid;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.services.MessagePollingService;
import com.mde.potdroid.views.LoginDialog;
import com.mde.potdroid.views.LogoutDialog;
import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * Settings Activity. Since the support lib does not contain a PreferenceFragment,
 * we use the legacy PreferenceActivity.
 */
public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SettingsWrapper mSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = new SettingsWrapper(this);

        setTitle(R.string.subtitle_settings);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        // set some custom descriptions programmatically
        setPreferenceDescription(SettingsWrapper.PREF_KEY_USERNAME);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_LOAD_BENDERS);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_SHOW_BENDERS);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_LOAD_IMAGES);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_POLL_MESSAGES);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_MATA);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_MATA_FORUM);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_START_ACTIVITY);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_START_FORUM);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Let's do something when a preference value changes
        setPreferenceDescription(key);
    }

    private void setPreferenceDescription(String key) {
        Preference preference = findPreference(key);

        // if list preference, set the chosen value as description
        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            preference.setSummary(listPref.getEntry());
        }


        if (key.equals(SettingsWrapper.PREF_KEY_USERNAME)) {

            // set the description of the login state and enable/disable
            // the logout button.
            LoginDialog loginPreference = (LoginDialog) findPreference(SettingsWrapper
                    .PREF_KEY_LOGIN);
            LogoutDialog logoutPreference = (LogoutDialog) findPreference(SettingsWrapper
                    .PREF_KEY_LOGOUT);

            if (Utils.isLoggedIn()) {
                loginPreference.setSummary(String.format("%s %s", getString(R.string
                        .pref_state_loggedin), mSettings.getUsername()));
                logoutPreference.setEnabled(true);
                loginPreference.setEnabled(false);
            } else {
                loginPreference.setSummary(getString(R.string.pref_state_notloggedin));
                logoutPreference.setEnabled(false);
                loginPreference.setEnabled(true);
            }
        } else if (key.equals(SettingsWrapper.PREF_KEY_POLL_MESSAGES)) {

            // the polling preference
            Intent pollServiceIntent = new Intent(SettingsActivity.this,
                    MessagePollingService.class);
            if (mSettings.pollMessagesInterval() == 0) {
                if (isPollingServiceRunning())
                    stopService(pollServiceIntent);
            } else {
                if (!isPollingServiceRunning())
                    startService(pollServiceIntent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Crouton.cancelAllCroutons();
    }

    // check if the MessagePollingService is running.
    private boolean isPollingServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer
                .MAX_VALUE)) {
            if (MessagePollingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}