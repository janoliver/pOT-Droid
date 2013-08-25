package com.mde.potdroid3.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.mde.potdroid3.R;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set a subtitle
        getActivity().getActionBar().setSubtitle("Settings");

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        //@TODO: Reload on theme change...
    }

}
