package com.mde.potdroid;

import android.app.Application;
import com.mde.potdroid.helpers.PersistentCookieStore;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.net.*;


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
                                try {
                                    URL url = new URL(imageUri);
                                    String filename = url.getPath().substring(url.getPath().lastIndexOf('/') + 1, url.getPath().length());
                                    String basename = filename.substring(0, filename.lastIndexOf('.'));
                                    String extension = filename.substring(filename.lastIndexOf('.'));
                                    return basename.replaceAll("\\W+", "") + extension;

                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                    return imageUri;
                                }
                            }
                        },
                        3600 * 24 * 7))
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);
    }
}
