package com.mde.potdroid3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class BaseActivity extends Activity {

    protected SharedPreferences mSettings;
    protected Bundle mExtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mExtras = getIntent().getExtras();

//        String theme = mSettings.getString("theme", Utils.THEME_LIGHT);
//        if(theme == Utils.THEME_LIGHT)   this.setTheme(R.style.PotDroidLight);
//        if(theme == Utils.THEME_DARK)    this.setTheme(R.style.PotDroidDark);

        setContentView(R.layout.layout_activity_single_fragment);

    }
}

