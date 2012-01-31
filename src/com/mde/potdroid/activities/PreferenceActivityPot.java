/*
 * Copyright (C) 2011 Jan Oliver Oelerich <janoliver@oelerich.org>
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

package com.mde.potdroid.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.janoliver.potdroid.R;
import com.mde.potdroid.helpers.NotificationService;
import com.mde.potdroid.helpers.PotUtils;

/**
 * Preference activity.
 */
public class PreferenceActivityPot extends PreferenceActivity {

    private SharedPreferences mSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // clear all caches
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        // create preferences
        addPreferencesFromResource(R.layout.preferences);

        // this is the login preference. It has to be built by ourself.
        Preference customPref = findPreference("loginPref");
        customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                // new implementation
                final Dialog dialog = new Dialog(PreferenceActivityPot.this);
                dialog.setContentView(R.layout.dialog_login);
                dialog.setTitle("mods.de Forum Login");
                dialog.setCancelable(true);

                // set values
                final EditText user = (EditText) dialog.findViewById(R.id.user_name);
                final String oldUser = mSettings.getString("user_name", "");
                user.setText(oldUser);
                final EditText pw = (EditText) dialog.findViewById(R.id.user_password);
                final String oldPw = mSettings.getString("user_password", "");
                pw.setText(oldPw);

                Button loginButton = (Button) dialog.findViewById(R.id.login);
                loginButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        SharedPreferences.Editor editor = mSettings.edit();

                        editor.putString("user_name", user.getText().toString());
                        editor.putString("user_password", pw.getText().toString());
                        editor.commit();
                        try {
                            if (PotUtils.getWebsiteInteractionInstance(PreferenceActivityPot.this)
                                    .login()) {
                                Toast.makeText(PreferenceActivityPot.this,
                                        "Erfolgreich eingeloggt", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                                PotUtils.clear();
                            } else {
                                // set pref values back
                                editor.putString("user_name", oldUser);
                                editor.putString("user_password", oldPw);
                                editor.commit();
                                Toast.makeText(PreferenceActivityPot.this, "Falsche Zugangsdaten!",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(PreferenceActivityPot.this, "ahhh", Toast.LENGTH_LONG);
                        }
                    }
                });
                dialog.show();

                return true;
            }

        });

        Preference aboutPref = findPreference("aboutPref");
        aboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                // new implementation
                final Dialog dialog = new Dialog(PreferenceActivityPot.this);
                dialog.setContentView(R.layout.dialog_about);
                dialog.setTitle("pOT Droid");
                dialog.setCancelable(true);

                TextView txt = (TextView) dialog.findViewById(R.id.text);
                // get about txt
                InputStream aboutTxt = null;
                try {
                    aboutTxt = PreferenceActivityPot.this.getResources().getAssets()
                            .open("about.txt");
                } catch (IOException e1) {
                }
                String aboutTxtStr = "";
                try {
                    aboutTxtStr = PotUtils.inputStreamToString(aboutTxt);
                } catch (IOException e) {
                }
                PreferenceActivityPot.this.getPackageManager();
                String version = "";
                try {
                    version = PreferenceActivityPot.this.getPackageManager().getPackageInfo(
                            getPackageName(), 0).versionName;
                } catch (NameNotFoundException e) {
                }

                aboutTxtStr = aboutTxtStr.replace("XVERSIONX", version);
                txt.setText(aboutTxtStr);

                Button cancelButton = (Button) dialog.findViewById(R.id.cancel);
                cancelButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        Random rand = new Random();
                        if (rand.nextInt(10) == 5) {
                            showPic();
                        }
                    }
                });
                dialog.show();

                return true;
            }

        });

        // callbacks for notification service
        Preference notificationOnOff = (Preference) findPreference("notifications");
        notificationOnOff.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference arg0, Object newValue) {
                Intent notificationServiceIntent = new Intent(PreferenceActivityPot.this,
                        NotificationService.class);
                if ((Boolean) newValue) {
                    startService(notificationServiceIntent);
                } else {
                    stopService(notificationServiceIntent);
                }
                return true;
            }
        });

    }

    private void showPic() {
        final Dialog dialog = new Dialog(PreferenceActivityPot.this);
        dialog.setContentView(R.layout.dialog_egg);
        dialog.setCancelable(true);

        Button cancelButton = (Button) dialog.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
