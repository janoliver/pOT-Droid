package com.mde.potdroid.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * A Broadcast Receiver that should be triggered after Boot. It starts the AlarmManager Service
 * of the PM Polling.
 */
public class BootBroadcastReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, MessagePollingService.class);
        context.startService(startServiceIntent);
    }
}