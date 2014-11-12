package com.mde.potdroid;

import android.app.Application;
import android.content.Context;
import com.mde.potdroid.helpers.CacheContentProvider;
import com.mde.potdroid.helpers.PersistentCookieStore;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;


public class PotDroidApplication extends Application {
    public void onCreate() {
        super.onCreate();

        // enable cookies
        CookieManager cookieManager = new CookieManager(
                new PersistentCookieStore(getApplicationContext()), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        initImageLoader(getApplicationContext());
    }

    public static void initImageLoader(Context c) {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .build();

        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(c)
                .diskCacheFileNameGenerator(new CacheContentProvider.HashFileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);
    }
}
