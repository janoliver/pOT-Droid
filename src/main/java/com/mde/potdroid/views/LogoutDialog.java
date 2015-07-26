package com.mde.potdroid.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import com.mde.potdroid.R;
import com.mde.potdroid.fragments.BaseFragment;
import com.mde.potdroid.helpers.SettingsWrapper;

/**
 * Simple PreferenceDialog that deletes the stored cookie and username/userid for the user.
 * This is equal to a "logout" action.
 */
public class LogoutDialog extends DialogPreference {

    private Activity mContext;
    private SettingsWrapper mSettingsWrapper;

    public LogoutDialog(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = (Activity)context;
        mSettingsWrapper = new SettingsWrapper(mContext);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
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
        }
    }

}