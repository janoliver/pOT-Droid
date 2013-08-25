package com.mde.potdroid3.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by oli on 8/24/13.
 */
public class SettingsWrapper {

    private SharedPreferences mSharedPreferences;
    private Context mContext;

    public SettingsWrapper(Context cx) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(cx);
        mContext = cx;
    }

    public Boolean showBenders() {
        return mSharedPreferences.getBoolean("pref_show_benders", true);
    }

    public Boolean downloadBenders() {
        String lb = mSharedPreferences.getString("pref_load_benders", "0");
        return !(lb == "0" || (lb == "1" && Network.getConnectionType(mContext) != Network.NETWORK_WIFI));
    }
}
