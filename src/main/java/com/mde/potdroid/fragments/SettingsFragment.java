package com.mde.potdroid.fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.LoginPreference;
import com.mde.potdroid.helpers.LogoutPreference;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.services.MessagePollingService;
import com.mde.potdroid.views.LoginDialog;
import com.mde.potdroid.views.LogoutDialog;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;


public class SettingsFragment extends PreferenceFragmentCompatDividers implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SettingsWrapper mSettings;

    private static final int NOTIFICATION_SOUND_REQUEST_CODE = 1;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {

        mSettings = new SettingsWrapper(getActivity());
        //boolean def = mSettings.isFixedSidebar();

        getActivity().setTitle(R.string.subtitle_settings);

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);
        /*CheckBoxPreference preference = (CheckBoxPreference) findPreference("pref_fixed_sidebar");
        preference.setChecked(def);
        preference.setDefaultValue(def);*/
    }

    @Override
    public void onResume() {
        super.onResume();

        // set some custom descriptions programmatically
        setPreferenceDescription(SettingsWrapper.PREF_KEY_THEME);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_THEME_DARK_VARIANT);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_USERNAME);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_LOAD_BENDERS);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_SHOW_BENDERS);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_LOAD_IMAGES);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_SHOW_MENU);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_LOAD_VIDEOS);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_POLL_MESSAGES);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_MATA);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_MATA_FORUM);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_START_ACTIVITY);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_START_FORUM);

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

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;

        if (preference instanceof LoginPreference) {
            dialogFragment = LoginDialog.newInstance(preference.getKey());
            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(this.getFragmentManager(),
                        "android.support.v7.preference.PreferenceFragment.DIALOG");
            }
        } else if (preference instanceof LogoutPreference) {
            dialogFragment = LogoutDialog.newInstance(preference.getKey());
            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(this.getFragmentManager(),
                        "android.support.v7.preference.PreferenceFragment.DIALOG");
            }
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
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
            LoginPreference loginPreference = (LoginPreference) findPreference(SettingsWrapper
                    .PREF_KEY_LOGIN);
            LogoutPreference logoutPreference = (LogoutPreference) findPreference(SettingsWrapper
                    .PREF_KEY_LOGOUT);

            if(loginPreference != null && logoutPreference != null) {
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
            }

        } else if (key.equals(SettingsWrapper.PREF_KEY_POLL_MESSAGES)) {

            // the polling preference
            Intent pollServiceIntent = new Intent(getActivity(), MessagePollingService.class);
            if (mSettings.pollMessagesInterval() == 0) {
                if (isPollingServiceRunning())
                    getActivity().stopService(pollServiceIntent);
            } else {
                if (!isPollingServiceRunning())
                    getActivity().startService(pollServiceIntent);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(SettingsWrapper.PREF_KEY_NOTIFICATION_SOUND)) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

            String existingValue = mSettings.getNotificationSoundURI(); // TODO
            if (existingValue != null) {
                if (existingValue.length() == 0) {
                    // Select "Silent"
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                }
            } else {
                // No ringtone has been selected, set to the default
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
            }

            startActivityForResult(intent, NOTIFICATION_SOUND_REQUEST_CODE);
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NOTIFICATION_SOUND_REQUEST_CODE && data != null) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = p.edit();

            if (ringtone != null) {
                editor.putString(SettingsWrapper.PREF_KEY_NOTIFICATION_SOUND, ringtone.toString());
            } else {
                // "Silent" was selected
                editor.putString(SettingsWrapper.PREF_KEY_NOTIFICATION_SOUND, "");
            }

            editor.commit();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // check if the MessagePollingService is running.
    private boolean isPollingServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer
                .MAX_VALUE)) {
            if (MessagePollingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
