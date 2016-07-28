package com.mde.potdroid;

import android.app.Application;
import com.mde.potdroid.helpers.ImageHandler;


public class PotDroidApplication extends Application {
    private ImageHandler mCachedPictureHandler;
    private ImageHandler mCachedBenderHandler;

    public void onCreate() {
        super.onCreate();

    }
}
