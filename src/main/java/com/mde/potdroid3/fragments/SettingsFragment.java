package com.mde.potdroid3.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import com.mde.potdroid3.R;
import com.mde.potdroid3.helpers.SettingsWrapper;
import com.mde.potdroid3.views.LoginDialog;
import com.mde.potdroid3.views.LogoutDialog;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SettingsWrapper mSettings;

    private LoginDialog mLoginPreference;
    private LogoutDialog mLogoutPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = new SettingsWrapper(getActivity());

        // set a subtitle
        getActivity().getActionBar().setSubtitle("Settings");

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // store preferences
        mLoginPreference = (LoginDialog) getPreferenceScreen().findPreference(SettingsWrapper.PREF_KEY_LOGIN);
        mLogoutPreference = (LogoutDialog) getPreferenceScreen().findPreference(SettingsWrapper.PREF_KEY_LOGOUT);

        //@TODO: Reload on theme change...
    }

    @Override
    public void onResume() {
        super.onResume();

        setPreferenceDescription(SettingsWrapper.PREF_KEY_USERNAME);

        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Let's do something when a preference value changes
        setPreferenceDescription(key);
    }

    private void setPreferenceDescription(String key) {
        if (key.equals(SettingsWrapper.PREF_KEY_USERNAME)) {
            if(mSettings.hasUsername()) {
                mLoginPreference.setSummary("Eingeloggt als: " + mSettings.getUsername());
                mLogoutPreference.setEnabled(true);
            } else {
                mLoginPreference.setSummary("Nicht eingeloggt!");
                mLogoutPreference.setEnabled(false);
            }
        }
    }
}
