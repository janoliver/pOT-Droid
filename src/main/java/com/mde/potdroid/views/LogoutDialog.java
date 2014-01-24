package com.mde.potdroid.views;

import android.app.Activity;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.SettingsWrapper;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Simple PreferenceDialog that deletes the stored cookie and username/userid for the user.
 * This is equal to a "logout" action.
 */
public class LogoutDialog extends DialogPreference
{

    private Activity mActivity;
    private SettingsWrapper mSettingsWrapper;

    public LogoutDialog(Activity activity, AttributeSet attrs) {
        super(activity, attrs);

        mActivity = activity;
        mSettingsWrapper = new SettingsWrapper(mActivity);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            mSettingsWrapper.clearCookie();
            mSettingsWrapper.clearUsername();
            mSettingsWrapper.clearUserId();
            Crouton.makeText(mActivity, R.string.logout_success, Style.CONFIRM);
        }
    }

}