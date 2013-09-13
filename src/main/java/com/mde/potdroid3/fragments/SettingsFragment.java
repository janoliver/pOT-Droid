package com.mde.potdroid3.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import com.mde.potdroid3.R;
import com.mde.potdroid3.helpers.SettingsWrapper;
import com.mde.potdroid3.views.LoginDialog;
import com.mde.potdroid3.views.LogoutDialog;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SettingsWrapper mSettings;

    private LoginDialog mLoginPreference;
    private LogoutDialog mLogoutPreference;
    private CheckBoxPreference mShowBendersPreference;
    private ListPreference mLoadBendersPreference;
    private ListPreference mLoadImagesPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = new SettingsWrapper(getActivity());

        // set a subtitle
        getActivity().getActionBar().setSubtitle(getActivity().getString(R.string.subtitle_settings));

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // store preferences
        mLoginPreference = (LoginDialog) getPreferenceScreen().findPreference(SettingsWrapper.PREF_KEY_LOGIN);
        mLogoutPreference = (LogoutDialog) getPreferenceScreen().findPreference(SettingsWrapper.PREF_KEY_LOGOUT);
        mShowBendersPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(SettingsWrapper.PREF_KEY_SHOW_BENDERS);
        mLoadBendersPreference = (ListPreference) getPreferenceScreen().findPreference(SettingsWrapper.PREF_KEY_LOAD_BENDERS);
        mLoadImagesPreference = (ListPreference) getPreferenceScreen().findPreference(SettingsWrapper.PREF_KEY_LOAD_IMAGES);

        //@TODO: Reload on theme change...
    }

    @Override
    public void onResume() {
        super.onResume();

        setPreferenceDescription(SettingsWrapper.PREF_KEY_USERNAME);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_SHOW_BENDERS);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_LOAD_BENDERS);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_LOAD_IMAGES);

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
                mLoginPreference.setSummary(getActivity().getString(R.string.pref_state_loggedin)
                        + mSettings.getUsername());
                mLogoutPreference.setEnabled(true);
            } else {
                mLoginPreference.setSummary(getActivity().getString(R.string.pref_state_notloggedin));
                mLogoutPreference.setEnabled(false);
            }
        } else if (key.equals(SettingsWrapper.PREF_KEY_SHOW_BENDERS)) {
            mShowBendersPreference.setSummary(
                    mSettings.showBenders() ?
                    getActivity().getString(R.string.pref_state_benders_shown) :
                    getActivity().getString(R.string.pref_state_benders_notshown));
        } else if (key.equals(SettingsWrapper.PREF_KEY_LOAD_IMAGES)) {
            if(mSettings.loadImages().equals("0"))
                mLoadImagesPreference.setSummary(getActivity().getString(R.string.pref_state_images_never));
            if(mSettings.loadImages().equals("1"))
                mLoadImagesPreference.setSummary(getActivity().getString(R.string.pref_state_images_wifi));
            if(mSettings.loadImages().equals("2"))
                mLoadImagesPreference.setSummary(getActivity().getString(R.string.pref_state_images_always));
        } else if (key.equals(SettingsWrapper.PREF_KEY_LOAD_BENDERS)) {
            if(mSettings.loadBenders().equals("0"))
                mLoadBendersPreference.setSummary(getActivity().getString(R.string.pref_state_benders_never));
            if(mSettings.loadBenders().equals("1"))
                mLoadBendersPreference.setSummary(getActivity().getString(R.string.pref_state_benders_wifi));
            if(mSettings.loadBenders().equals("2"))
                mLoadBendersPreference.setSummary(getActivity().getString(R.string.pref_state_benders_always));
        }
    }
}
