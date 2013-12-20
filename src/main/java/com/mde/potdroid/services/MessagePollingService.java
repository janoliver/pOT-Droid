package com.mde.potdroid.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by oli on 12/5/13.
 */
public class MessagePollingService extends Service
{
    MessagePollingAlarm alarm = new MessagePollingAlarm();

    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        alarm.setAlarm(this);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        alarm.cancelAlarm(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}