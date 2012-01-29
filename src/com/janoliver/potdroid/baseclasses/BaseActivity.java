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

package com.janoliver.potdroid.baseclasses;

import android.app.Activity;
import android.os.Bundle;

import com.janoliver.potdroid.helpers.ObjectManager;
import com.janoliver.potdroid.helpers.PotExceptionHandler;
import com.janoliver.potdroid.helpers.PotUtils;
import com.janoliver.potdroid.helpers.WebsiteInteraction;

/**
 * The Acitivty base class. ATM only takes care of some member variables.
 */
public abstract class BaseActivity extends Activity {

    protected WebsiteInteraction mWebsiteInteraction;
    protected ObjectManager      mObjectManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // set our own exception handler
        Thread.setDefaultUncaughtExceptionHandler(new PotExceptionHandler(
                PotUtils.SDCARD_ERRLOG_LOCATION, null));
        
        super.onCreate(savedInstanceState);

        mWebsiteInteraction = PotUtils.getWebsiteInteractionInstance(this);
        mObjectManager      = PotUtils.getObjectManagerInstance(this);
    }
}
