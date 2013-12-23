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
import com.mde.potdroid.services.MessagePollingService;
import com.mde.potdroid.views.LoginDialog;
import com.mde.potdroid.views.LogoutDialog;

public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener
{

    private SettingsWrapper mSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = new SettingsWrapper(this);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        setPreferenceDescription(SettingsWrapper.PREF_KEY_USERNAME);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_LOAD_BENDERS);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_LOAD_IMAGES);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_POLL_MESSAGES);

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
        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            preference.setSummary(listPref.getEntry());
        }

        if (key.equals(SettingsWrapper.PREF_KEY_USERNAME)) {

            LoginDialog loginPreference = (LoginDialog) findPreference(SettingsWrapper
                    .PREF_KEY_LOGIN);
            LogoutDialog logoutPreference = (LogoutDialog) findPreference(SettingsWrapper
                    .PREF_KEY_LOGOUT);

            if (mSettings.hasUsername()) {
                loginPreference.setSummary(getString(R.string.pref_state_loggedin)
                        + mSettings.getUsername());
                logoutPreference.setEnabled(true);
            } else {
                loginPreference.setSummary(getString(R.string.pref_state_notloggedin));
                logoutPreference.setEnabled(false);
            }
        } else if (key.equals(SettingsWrapper.PREF_KEY_POLL_MESSAGES)) {

            Intent pollServiceIntent = new Intent(SettingsActivity.this,
                    MessagePollingService.class);
            if (mSettings.pollMessagesInterval() == 0) {
                if (isMyServiceRunning())
                    stopService(pollServiceIntent);
            } else {
                if (!isMyServiceRunning())
                    startService(pollServiceIntent);
            }
        }
    }

    private boolean isMyServiceRunning() {
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