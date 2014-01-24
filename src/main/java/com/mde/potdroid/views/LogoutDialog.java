package com.mde.potdroid.views;

import android.app.Activity;
import android.content.Context;
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

    private Context mContext;
    private SettingsWrapper mSettingsWrapper;

    public LogoutDialog(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mSettingsWrapper = new SettingsWrapper(mContext);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            mSettingsWrapper.clearCookie();
            mSettingsWrapper.clearUsername();
            mSettingsWrapper.clearUserId();
            Crouton.makeText((Activity)mContext, R.string.logout_success, Style.CONFIRM);
        }
    }

}