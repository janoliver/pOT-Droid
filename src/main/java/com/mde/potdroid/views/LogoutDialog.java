package com.mde.potdroid.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import com.mde.potdroid.R;
import com.mde.potdroid.fragments.BaseFragment;
import com.mde.potdroid.helpers.SettingsWrapper;

public class LogoutDialog extends PreferenceDialogFragmentCompat {

    private Activity mContext;
    private SettingsWrapper mSettingsWrapper;


    public static LogoutDialog newInstance(String key) {
        final LogoutDialog fragment = new LogoutDialog();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mSettingsWrapper = new SettingsWrapper(getContext());
    }

    @Override
    public void onClick(final DialogInterface dialog, int which) {
        if(which == Dialog.BUTTON_POSITIVE) {
            mSettingsWrapper.clearCookie();
            mSettingsWrapper.clearUsername();
            mSettingsWrapper.clearUserId();
            Snackbar snackbar = Snackbar
                    .make(mContext.findViewById(android.R.id.content), R.string.msg_logout_success, Snackbar.LENGTH_LONG)
                    .setAction("Neu starten", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = mContext.getPackageManager()
                                    .getLaunchIntentForPackage(mContext.getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            getContext().startActivity(i);
                        }
                    });
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(BaseFragment.COLOR_SUCCESS);
            snackbar.show();
        } else {
            dialog.dismiss();
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
    }

}