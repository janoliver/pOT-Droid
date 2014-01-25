package com.mde.potdroid.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * This Service starts and stops the Alarm of the Message polling
 */
public class MessagePollingService extends Service {

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