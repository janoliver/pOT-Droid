package com.mde.potdroid.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.LoginPreference;
import com.mde.potdroid.helpers.LogoutPreference;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.services.MessagePollingService;
import com.mde.potdroid.views.LoginDialog;
import com.mde.potdroid.views.LogoutDialog;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.io.*;
import java.util.Map;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SettingsWrapper mSettings;

    private static final int NOTIFICATION_SOUND_REQUEST_CODE = 1;
    private static final int DOWNLOAD_DIRECTORY_CODE = 2;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {

        mSettings = new SettingsWrapper(getActivity());

        getActivity().setTitle(R.string.subtitle_settings);

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();

        // set some custom descriptions programmatically
        setPreferenceDescription(SettingsWrapper.PREF_KEY_THEME);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_USERNAME);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_LOAD_BENDERS);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_SHOW_BENDERS);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_LOAD_IMAGES);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_LOAD_GIFS);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_LOAD_VIDEOS);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_SHOW_MENU);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_POLL_MESSAGES);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_MATA);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_START_ACTIVITY);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_START_FORUM);
        setPreferenceDescription(SettingsWrapper.PREF_KEY_FAB);
        setPreferenceDescription(SettingsWrapper.PREF_DOWNLOAD_DIRECTORY);

        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Let's do something when a preference value changes
        setPreferenceDescription(key);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;

        if (preference instanceof LoginPreference) {
            dialogFragment = LoginDialog.newInstance(preference.getKey());
            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(this.getFragmentManager(),
                        "android.support.v7.preference.PreferenceFragment.DIALOG");
            }
        } else if (preference instanceof LogoutPreference) {
            dialogFragment = LogoutDialog.newInstance(preference.getKey());
            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(this.getFragmentManager(),
                        "android.support.v7.preference.PreferenceFragment.DIALOG");
            }
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    private void setPreferenceDescription(String key) {
        Preference preference = findPreference(key);

        // if list preference, set the chosen value as description
        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            preference.setSummary(listPref.getEntry());
        } else if (preference instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) preference;
            preference.setSummary(editTextPref.getText());
        }

        if (preference != null && key.equals(SettingsWrapper.PREF_DOWNLOAD_DIRECTORY)) {
            preference.setSummary(mSettings.getMediaDownloadPath());
        }

        if (key.equals(SettingsWrapper.PREF_KEY_USERNAME)) {

            // set the description of the login state and enable/disable
            // the logout button.
            LoginPreference loginPreference = (LoginPreference) findPreference(SettingsWrapper
                    .PREF_KEY_LOGIN);
            LogoutPreference logoutPreference = (LogoutPreference) findPreference(SettingsWrapper
                    .PREF_KEY_LOGOUT);

            if (loginPreference != null && logoutPreference != null) {
                if (Utils.isLoggedIn()) {
                    loginPreference.setSummary(String.format("%s %s", getString(R.string
                            .pref_state_loggedin), mSettings.getUsername()));
                    logoutPreference.setEnabled(true);
                    loginPreference.setEnabled(false);
                } else {
                    loginPreference.setSummary(getString(R.string.pref_state_notloggedin));
                    logoutPreference.setEnabled(false);
                    loginPreference.setEnabled(true);
                }
            }

        } else if (key.equals(SettingsWrapper.PREF_KEY_POLL_MESSAGES)) {

            // the polling preference
            Intent pollServiceIntent = new Intent(getActivity(), MessagePollingService.class);
            if (mSettings.pollMessagesInterval() == 0) {
                if (isPollingServiceRunning())
                    getActivity().stopService(pollServiceIntent);
            } else {
                if (!isPollingServiceRunning())
                    getActivity().startService(pollServiceIntent);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(SettingsWrapper.PREF_KEY_NOTIFICATION_SOUND)) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

            String existingValue = mSettings.getNotificationSoundURI(); // TODO
            if (existingValue != null) {
                if (existingValue.length() == 0) {
                    // Select "Silent"
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                }
            } else {
                // No ringtone has been selected, set to the default
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
            }

            startActivityForResult(intent, NOTIFICATION_SOUND_REQUEST_CODE);
            return true;

        }

        if (preference.getKey().equals(SettingsWrapper.PREF_EXPORT_SETTINGS)) {
            File f = new File(getContext().getExternalFilesDir(null), "settings");
            if (saveSharedPreferencesToFile(f)) {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Snackbar snackbar = Snackbar
                                .make(getActivity().findViewById(android.R.id.content), R.string.msg_export_success, Snackbar.LENGTH_LONG);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(Utils.getColorByAttr(getActivity(), R.attr.bbSuccessColor));
                        TextView tv = (TextView) snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        snackbar.show();
                    }
                });
            } else {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Snackbar snackbar = Snackbar
                                .make(getActivity().findViewById(android.R.id.content), R.string.msg_export_error, Snackbar.LENGTH_LONG);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(Utils.getColorByAttr(getActivity(), R.attr.bbErrorColor));
                        TextView tv = (TextView) snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        snackbar.show();
                    }
                });
            }
            return true;
        }

        if (preference.getKey().equals(SettingsWrapper.PREF_IMPORT_SETTINGS)) {
            File f = new File(getContext().getExternalFilesDir(null), "settings");
            if (f.exists() && loadSharedPreferencesFromFile(f)) {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Snackbar snackbar = Snackbar
                                .make(getActivity().findViewById(android.R.id.content), R.string.msg_import_success, Snackbar.LENGTH_LONG)
                                .setAction("Neu starten", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = getActivity().getPackageManager()
                                                .getLaunchIntentForPackage(getActivity().getPackageName());
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        getContext().startActivity(i);
                                    }
                                });
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(Utils.getColorByAttr(getActivity(), R.attr.bbSuccessColor));
                        TextView tv = (TextView) snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        snackbar.show();
                    }
                });
            } else {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Snackbar snackbar = Snackbar
                                .make(getActivity().findViewById(android.R.id.content), R.string.msg_import_error, Snackbar.LENGTH_LONG);
                        View snackBarView = snackbar.getView();
                        TextView tv = (TextView) snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        snackBarView.setBackgroundColor(Utils.getColorByAttr(getActivity(), R.attr.bbErrorColor));
                        snackbar.show();
                    }
                });
            }
            return true;
        }

        if (preference.getKey().equals(SettingsWrapper.PREF_DOWNLOAD_DIRECTORY)) {

            Intent i = new Intent(getActivity(), FilePickerActivity.class);

            // Set these depending on your use case. These are the defaults.
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

            startActivityForResult(i, DOWNLOAD_DIRECTORY_CODE);

            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NOTIFICATION_SOUND_REQUEST_CODE && data != null) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = p.edit();

            if (ringtone != null) {
                editor.putString(SettingsWrapper.PREF_KEY_NOTIFICATION_SOUND, ringtone.toString());
            } else {
                // "Silent" was selected
                editor.putString(SettingsWrapper.PREF_KEY_NOTIFICATION_SOUND, "");
            }

            editor.apply();
        } else if (requestCode == DOWNLOAD_DIRECTORY_CODE && resultCode == Activity.RESULT_OK) {
            File f = new File(data.getData().getPath());
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = p.edit();

            if(f.exists() && f.isDirectory())
                editor.putString(SettingsWrapper.PREF_DOWNLOAD_DIRECTORY, data.getData().getPath());

            editor.apply();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    // check if the MessagePollingService is running.

    private boolean isPollingServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer
                .MAX_VALUE)) {
            if (MessagePollingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean saveSharedPreferencesToFile(File dst) {
        boolean res = false;
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(dst));
            SharedPreferences pref =
                    PreferenceManager.getDefaultSharedPreferences(getContext());
            output.writeObject(pref.getAll());

            res = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    @SuppressWarnings({"unchecked"})
    private boolean loadSharedPreferencesFromFile(File src) {
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            prefEdit.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                else if (v instanceof Float)
                    prefEdit.putFloat(key, ((Float) v).floatValue());
                else if (v instanceof Integer)
                    prefEdit.putInt(key, ((Integer) v).intValue());
                else if (v instanceof Long)
                    prefEdit.putLong(key, ((Long) v).longValue());
                else if (v instanceof String)
                    prefEdit.putString(key, ((String) v));
            }
            prefEdit.commit();
            res = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }
}
