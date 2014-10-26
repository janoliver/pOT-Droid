package com.mde.potdroid;

import android.app.Application;
import com.mde.potdroid.helpers.PersistentCookieStore;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

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

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .build();

        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .diskCache(new LimitedAgeDiscCache(
                        StorageUtils.getCacheDirectory(getApplicationContext()),
                        null,
                        new FileNameGenerator() {
                            @Override
                            public String generate(String imageUri) {
                                String filename = imageUri.substring(imageUri.lastIndexOf('/') + 1, imageUri.length());
                                String basename = filename.substring(0, filename.lastIndexOf('.'));
                                String extension = filename.substring(filename.lastIndexOf('.'));
                                return basename.replaceAll("\\W+", "") + extension;
                            }
                        },
                        3600 * 24 * 7))
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);
    }
}
