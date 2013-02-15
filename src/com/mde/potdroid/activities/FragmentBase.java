package com.mde.potdroid.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.mde.potdroid.helpers.ObjectManager;
import com.mde.potdroid.helpers.PotUtils;
import com.mde.potdroid.helpers.WebsiteInteraction;

public class FragmentBase extends Fragment {

    protected WebsiteInteraction mWebsiteInteraction;
    protected ObjectManager      mObjectManager;
    protected SharedPreferences  mSettings;
    protected FragmentActivity   mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mActivity           = getActivity();
        mWebsiteInteraction = PotUtils.getWebsiteInteractionInstance(mActivity);
        mObjectManager      = PotUtils.getObjectManagerInstance(mActivity);
        mSettings           = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }
    
}
