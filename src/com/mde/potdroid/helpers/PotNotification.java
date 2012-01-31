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

package com.mde.potdroid.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;

/**
 * This class is to be used whenever the progressDialog is implemented to load
 * another activity. It extends it's features by a cancel button that dismisses
 * the dialog and finishes the activity to be loaded.
 */
public class PotNotification extends ProgressDialog {

    protected final Activity mActivity;
    private Boolean mFinishable;

    @SuppressWarnings("rawtypes")
    protected AsyncTask mThread;

    @SuppressWarnings("rawtypes")
    public PotNotification(Activity activity, AsyncTask thread, Boolean finishable) {
        super(activity);

        mActivity = activity;
        mThread = thread;
        mFinishable = finishable;

        // click listener for the cancel button
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                cancelTask();
            }
        };
        setButton(BUTTON_NEGATIVE, "Abbrechen", listener);

        setCancelable(false);

        // now we prevent the screen from changing the orientation until
        // it is destroyed
        int orientation = activity.getResources().getConfiguration().orientation;
        activity.setRequestedOrientation(orientation);

    }

    @Override
    protected void onStop() {
        // allow orientation changes again.
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public void cancelTask() {
        mThread.cancel(true);
        dismiss();
        if (mFinishable) {
            mActivity.finish();
        }
    }
}
