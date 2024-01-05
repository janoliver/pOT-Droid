package com.mde.potdroid.helpers;

import android.content.Context;
import androidx.preference.DialogPreference;
import android.util.AttributeSet;


public class LogoutPreference extends DialogPreference {
    public LogoutPreference(Context context) {
        super(context, null);
    }

    public LogoutPreference(Context context, AttributeSet attrs) {
        super(context, attrs, androidx.preference.R.attr.dialogPreferenceStyle);
    }

    public LogoutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LogoutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int getDialogLayoutResource() {
        return 0;
    }

}
