package com.mde.potdroid;

import android.app.Application;
import com.mde.potdroid.helpers.PersistentCookieStore;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

/**
 * Created by oli on 8/23/14.
 */
public class PotDroidApplication extends Application {
    public void onCreate() {
        super.onCreate();

        // enable cookies
        CookieManager cookieManager = new CookieManager(
                new PersistentCookieStore(getApplicationContext()), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }
}
