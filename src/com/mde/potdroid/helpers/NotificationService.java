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

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private Notification        mPostNotification;
    private Notification        mPmNotification;
    private Timer               mTimer;
    private SharedPreferences   mSettings;
    private Integer             mStartId;
    private PendingIntent       mContentIntent;
    private ObjectManager       mObjectManager;
    
    private int                 mPostsUnread = 0;
    private int                 mPmUnread = 0;
    
    // this is the database helper for the favourites.
    private FavouritesDatabase  mFavouritesDatabase;
    
    private static final Integer NOTIFICATION_ID   = 1;
    private static final String  NOTIFICATION_NEWPOST = "pOT Droid: Neue Posts";
    private static final String  NOTIFICATION_NEWPM = "pOT Droid: Neue PM";
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
        
        mPostNotification        = new Notification(R.drawable.icon, 
                NOTIFICATION_NEWPOST, System.currentTimeMillis());
        mPostNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        
        mPmNotification        = new Notification(R.drawable.icon, 
                NOTIFICATION_NEWPM, System.currentTimeMillis());
        mPmNotification.flags |= Notification.FLAG_AUTO_CANCEL;
    }

    /**
     * This method is called when the service is started (e.g. when the checkbox
     * in the potdroid settings page is activated. It starts a timer that checks
     * for new posts each interval, where interval is also defined in the settings.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartId = startId;
        
        Long interval = Long.valueOf(mSettings.getString("notificationrefresh","120"));
        mTimer.schedule( new TimerTask() {
            @Override
            public void run() {
                // the new pm stuff
                if(mSettings.getBoolean("newPmNotifications",false)) {
                    int unread = checkNewPm();
                   
                    if(unread > mPmUnread) {
                        mPmUnread = unread;
                        showNewPmNotification(unread + " ungelesene PMs!");
                    }
                    else if(unread == 0)
                        hideNewPmNotification();
                }
                
                // the new post stuff
                if(mSettings.getBoolean("newPostsNotifications",false)) {
                    int unread = checkNewPosts();
                   
                    if(unread > mPostsUnread) {
                        mPostsUnread = unread;
                        showNewPostNotification(unread + " neue Posts!");
                    } else if(unread == 0)
                        hideNewPostNotification();
                }
                
            }
        }, 0, interval * 1000);
        
        return Service.START_STICKY;
    }
    
    /**
     * Update the notification with the message msg.
     */
    private void showNewPostNotification(String msg) {
        mPostNotification.setLatestEventInfo(getApplicationContext(), CONTENT_TITLE, msg, mContentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mPostNotification);
    }
    
    /**
     * hide the notification
     */
    private void hideNewPostNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
    
    /**
     * Update the notification with the message msg.
     */
    private void showNewPmNotification(String msg) {
        mPmNotification.setLatestEventInfo(getApplicationContext(), CONTENT_TITLE, msg, 
                PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0));
        mNotificationManager.notify(NOTIFICATION_ID, mPmNotification);
    }
    
    /**
     * hide the notification
     */
    private void hideNewPmNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
    
    /**
     * Check for new posts (there might be more notifyable stuff in the futuer.
     * Depends on enos, if there is going to be a really cool feature. :)
     */
    private Integer checkNewPosts() {
        int unread = 0;
        
        try {
            Bookmark[] bookmarks = mObjectManager.getBookmarks();
        
            // remove bookmark entries from database that aren't bookmarks anymore
            mFavouritesDatabase.cleanFavourites(bookmarks);
        
            for(Bookmark b : bookmarks) {
                if(mFavouritesDatabase.isFavourite(b))
                    unread += b.getNumberOfNewPosts();
            }
        } catch (Exception e) {}
        return unread;
    }
    
    /**
     * Check for new posts (there might be more notifyable stuff in the futuer.
     * Depends on enos, if there is going to be a really cool feature. :)
     */
    private Integer checkNewPm() {
        int unread = 0;
        String html = PotUtils.getWebsiteInteractionInstance(this).callPage("http://forum.mods.de/bb");
        Pattern pattern = Pattern.compile("<span class=\"infobar_newpm\">([0-9]+)</span>");
        Matcher m = pattern.matcher(html);
        
        if (m.find()) {
            unread = Integer.valueOf(m.group(1));
        }
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
        hideNewPostNotification();
        hideNewPmNotification();
        mTimer.cancel();
        mFavouritesDatabase.close();
        
        // this might be unneeded. Just in case...
        if(!mSettings.getBoolean("notifications", false)) {
            stopSelf(mStartId);
        }
    }
    
    
    
}