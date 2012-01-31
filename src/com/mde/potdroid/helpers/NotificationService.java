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

import com.janoliver.potdroid.R;
import com.mde.potdroid.activities.BookmarkActivity;
import com.mde.potdroid.helpers.ObjectManager.ParseErrorException;
import com.mde.potdroid.models.Bookmark;

public class NotificationService extends Service {

    private NotificationManager mNotificationManager;
    private Notification        mNotification;
    private Timer               mTimer;
    private SharedPreferences   mSettings;
    private Integer             mStartId;
    private PendingIntent       mContentIntent;
    private ObjectManager       mObjectManager;
    
    private FavouritesDatabase  mFavouritesDatabase;
    
    private static final Integer NOTIFICATION_ID   = 1;
    private static final String  NOTIFICATION_TEXT = "pOT Droid: Neue Posts";
    private static final String  CONTENT_TITLE     = "pOT Droid";
    

    /** 
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
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PotUtils.log("Notification started.");
        
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
    
    private void showNotification(String msg) {
        mNotification.setLatestEventInfo(getApplicationContext(), CONTENT_TITLE, msg, mContentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }
    
    private void hideNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
    
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
        } catch (ParseErrorException e) {}
        return unread;
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     */
    @Override
    public void onDestroy() {
        hideNotification();
        mTimer.cancel();
        
        if(!mSettings.getBoolean("notifications", false)) {
            stopSelf(mStartId);
        }
    }
    
    
    
}