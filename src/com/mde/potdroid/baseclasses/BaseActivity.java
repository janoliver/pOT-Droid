/*
 * Copyright (C) 2011 Jan Oliver Oelerich <janoliver@oelerich.org>
 *
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this software, and changing it is allowed as long as the 
 * name is changed.
 *
 *           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *  0. You just DO WHAT THE FUCK YOU WANT TO. 
 */

package com.mde.potdroid.baseclasses;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.janoliver.potdroid.R;
import com.mde.potdroid.helpers.ObjectManager;
import com.mde.potdroid.helpers.PotExceptionHandler;
import com.mde.potdroid.helpers.PotUtils;
import com.mde.potdroid.helpers.WebsiteInteraction;

/**
 * The Acitivty base class. ATM only takes care of some member variables.
 */
public abstract class BaseActivity extends Activity {

    protected WebsiteInteraction mWebsiteInteraction;
    protected ObjectManager      mObjectManager;
    protected SharedPreferences  mSettings;
    protected Bundle             mExtras;

    private final int THEME_LIGHT = 0;
    private final int THEME_DARK  = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // set our own exception handler
        Thread.setDefaultUncaughtExceptionHandler(new PotExceptionHandler(
                PotUtils.SDCARD_ERRLOG_LOCATION, null));
        
        super.onCreate(savedInstanceState);

        mWebsiteInteraction = PotUtils.getWebsiteInteractionInstance(this);
        mObjectManager      = PotUtils.getObjectManagerInstance(this);
        mSettings           = PreferenceManager.getDefaultSharedPreferences(this);
        mExtras             = getIntent().getExtras();
        
        // set the theme
        int theme = new Integer(mSettings.getString("theme", "0"));
        if(theme == THEME_LIGHT)
            this.setTheme(R.style.PotLight);
        if(theme == THEME_DARK)
            this.setTheme(R.style.PotDark);
    }
}
