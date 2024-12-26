package com.mde.potdroid;

import android.app.Application;

import androidx.emoji.bundled.BundledEmojiCompatConfig;
import androidx.emoji.text.EmojiCompat;


public class PotDroidApplication extends Application {
    public void onCreate() {
        super.onCreate();
        EmojiCompat.init(new BundledEmojiCompatConfig(getApplicationContext()));
    }
}
