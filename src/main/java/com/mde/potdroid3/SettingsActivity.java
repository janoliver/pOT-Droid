package com.mde.potdroid3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import com.mde.potdroid3.helpers.SettingsWrapper;
import com.mde.potdroid3.services.MessagePollingService;
import com.mde.potdroid3.views.LoginDialog;
import com.mde.potdroid3.views.LogoutDialog;

import java.util.Arrays;

public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SettingsWrapper mSettings;

    private LoginDialog mLoginPreference;
    private LogoutDialog mLogoutPreference;
    private CheckBoxPreference mShowBendersPreference;
    private ListPreference mLoadBendersPreference;
    private ListPreference mLoadImagesPreference;
    private ListPreference mPollMessagesPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = new SettingsWrapper(this);

        // set a subtitle
        getActionBar().setSubtitle(getString(R.string.subtitle_settings));

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // store preferences
        mLoginPreference = (LoginDialog) getPreferenceScreen().findPreference(SettingsWrapper.PREF_KEY_LOGIN);
        mLogoutPreference = (LogoutDialog) getPreferenceScreen().findPreference(SettingsWrapper.PREF_KEY_LOGOUT);
        mShowBendersPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(SettingsWrapper.PREF_KEY_SHOW_BENDERS);
        mLoadBendersPreference = (ListPreference) getPreferenceScreen().findPreference(SettingsWrapper.PREF_KEY_LOAD_BENDERS);
        mLoadImagesPreference = (ListPreference) getPreferenceScreen().findPreference(SettingsWrapper.PREF_KEY_LOAD_IMAGES);
        mPollMessagesPreference = (ListPreference) getPreferenceScreen().findPreference(SettingsWrapper.PREF_KEY_POLL_MESSAGES);

        //@TODO: Reload on theme change...
    }

    @Override
    public void onResume() {
        super.onResume();

        setPreferenceDescription(SettingsWrapper.PREF_KEY_USERNAME);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_SHOW_BENDERS);
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
        if (key.equals(SettingsWrapper.PREF_KEY_USERNAME)) {
            if(mSettings.hasUsername()) {
                mLoginPreference.setSummary(getString(R.string.pref_state_loggedin)
                        + mSettings.getUsername());
                mLogoutPreference.setEnabled(true);
            } else {
                mLoginPreference.setSummary(getString(R.string.pref_state_notloggedin));
                mLogoutPreference.setEnabled(false);
            }
        } else if (key.equals(SettingsWrapper.PREF_KEY_SHOW_BENDERS)) {
            mShowBendersPreference.setSummary(
                    mSettings.showBenders() ?
                            getString(R.string.pref_state_benders_shown) :
                            getString(R.string.pref_state_benders_notshown));
        } else if (key.equals(SettingsWrapper.PREF_KEY_LOAD_IMAGES)) {
            if(mSettings.loadImages().equals("0"))
                mLoadImagesPreference.setSummary(getString(R.string.pref_state_images_never));
            if(mSettings.loadImages().equals("1"))
                mLoadImagesPreference.setSummary(getString(R.string.pref_state_images_wifi));
            if(mSettings.loadImages().equals("2"))
                mLoadImagesPreference.setSummary(getString(R.string.pref_state_images_always));
        } else if (key.equals(SettingsWrapper.PREF_KEY_LOAD_BENDERS)) {
            if(mSettings.loadBenders().equals("0"))
                mLoadBendersPreference.setSummary(getString(R.string.pref_state_benders_never));
            if(mSettings.loadBenders().equals("1"))
                mLoadBendersPreference.setSummary(getString(R.string.pref_state_benders_wifi));
            if(mSettings.loadBenders().equals("2"))
                mLoadBendersPreference.setSummary(getString(R.string.pref_state_benders_always));
        } else if (key.equals(SettingsWrapper.PREF_KEY_POLL_MESSAGES)) {
            Intent pollServiceIntent = new Intent(SettingsActivity.this,
                    MessagePollingService.class);

            if(mSettings.pollMessagesInterval() == 0) {
                stopService(pollServiceIntent);
            } else {
                startService(pollServiceIntent);
            }

            String[] values = getResources().getStringArray(R.array.pref_poll_messages_values);
            String[] entries = getResources().getStringArray(R.array.pref_poll_messages_entries);

            int index = Arrays.asList(values).indexOf(mSettings.pollMessagesInterval() + "");
            mPollMessagesPreference.setSummary(entries[index]);

        }
    }


}