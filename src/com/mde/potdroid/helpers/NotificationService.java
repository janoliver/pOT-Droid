/*
 * Copyright (C) 2012 mods.de community 
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

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.mde.potdroid.R;
import com.mde.potdroid.activities.BookmarkActivity;
import com.mde.potdroid.models.Bookmark;

/**
 * This class provides the service, that checks for notifyable events and
 * in case it finds one, notifys the user. So far, only the favourited bookmarks
 * are checked. 
 * 
 * The magic happens in checkNewPosts() that returns an integer of new posts.
 * If it is > 0, a notification is posted.
 */
public class NotificationService extends Service {

    private NotificationManager mNotificationManager;
    private Notification        mNotification;
    private Timer               mTimer;
    private SharedPreferences   mSettings;
    private Integer             mStartId;
    private PendingIntent       mContentIntent;
    private ObjectManager       mObjectManager;
    
    // this is the database helper for the favourites.
    private FavouritesDatabase  mFavouritesDatabase;
    
    private static final Integer NOTIFICATION_ID   = 1;
    private static final String  NOTIFICATION_TEXT = "pOT Droid: Neue Posts";
    private static final String  CONTENT_TITLE     = "pOT Droid";
    

    /** 
     * Create the Service. We prepare the notification here.
     */
    @Override
    public void onCreate() {
        // create members
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mSettings            = PreferenceManager.getDefaultSharedPreferences(this);
        mTimer               = new Timer();
        mObjectManager       = PotUtils.getObjectManagerInstance(this);
        mFavouritesDatabase  = new FavouritesDatabase(this);
        
        // prepare the notification
        Intent notificationIntent = new Intent(this, BookmarkActivity.class);
        mContentIntent       = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mNotification        = new Notification(R.drawable.icon, 
                NOTIFICATION_TEXT, System.currentTimeMillis());
        mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
    }

    /**
     * This method is called when the service is started (e.g. when the checkbox
     * in the potdroid settings page is activated. It starts a timer that checks
     * for new posts each interval, where interval is also defined in the settings.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartId = startId;
        
        Long interval = new Long(mSettings.getString("notificationrefresh","120"));
        mTimer.schedule( new TimerTask() {
            public void run() {
                int unread = checkNewPosts();
                
                if(unread > 0) {
                    showNotification(unread + " neue Posts!");
                } else {
                    hideNotification();
                }
            }
        }, 0, interval * 1000);
        
        return Service.START_STICKY;
    }
    
    /**
     * Update the notification with the message msg.
     */
    private void showNotification(String msg) {
        mNotification.setLatestEventInfo(getApplicationContext(), CONTENT_TITLE, msg, mContentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }
    
    /**
     * hide the notification
     */
    private void hideNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
    
    /**
     * Check for new posts (there might be more notifyable stuff in the futuer.
     * Depends on enos, if there is going to be a really cool feature. :)
     */
    private Integer checkNewPosts() {
        int unread = 0;
        
        try {
            Map<Integer, Bookmark> bookmarks = mObjectManager.getBookmarks();
        
            // remove bookmark entries from database that aren't bookmarks anymore
            mFavouritesDatabase.cleanFavourites(bookmarks);
        
            for(Bookmark b : bookmarks.values()) {
                if(mFavouritesDatabase.isFavourite(b))
                    unread += b.getNumberOfNewPosts();
            }
        } catch (Exception e) {}
        return unread;
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * Before we stop the service, we want to cancel the timer and hide the 
     * notification.
     */
    @Override
    public void onDestroy() {
        hideNotification();
        mTimer.cancel();
        mFavouritesDatabase.close();
        
        // this might be unneeded. Just in case...
        if(!mSettings.getBoolean("notifications", false)) {
            stopSelf(mStartId);
        }
    }
    
    
    
}