package com.mde.potdroid.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.mde.potdroid.helpers.SettingsWrapper;

/**
 * A Broadcast Receiver that should be triggered after Boot. It starts the AlarmManager Service
 * of the PM Polling.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SettingsWrapper settings = new SettingsWrapper(context);

        // return, if polling is disabled
        if (settings.pollMessagesInterval().equals(0))
            return;

        Intent startServiceIntent = new Intent(context, MessagePollingService.class);
        context.startService(startServiceIntent);
    }
}