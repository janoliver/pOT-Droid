package com.mde.potdroid.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import com.mde.potdroid.R;
import com.mde.potdroid.fragments.BaseFragment;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.ActionClickListener;

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
            SnackbarManager.show(
                    Snackbar.with((Activity)mContext)
                            .text(R.string.msg_logout_success)
                            .actionLabel("Neu starten")
                            .type(SnackbarType.MULTI_LINE)
                            .actionListener(new ActionClickListener() {
                                @Override
                                public void onActionClicked(Snackbar snackbar) {
                                    Intent i = mContext.getPackageManager()
                                            .getLaunchIntentForPackage(mContext.getPackageName());
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    getContext().startActivity(i);
                                }
                            })
                            .color(BaseFragment.COLOR_SUCCESS), (Activity) getContext());
        }
    }

}