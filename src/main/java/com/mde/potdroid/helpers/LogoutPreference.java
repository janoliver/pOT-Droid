package com.mde.potdroid.helpers;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import com.mde.potdroid.R;


public class LogoutPreference extends DialogPreference {
    public LogoutPreference(Context context) {
        super(context, null);
    }

    public LogoutPreference(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.dialogPreferenceStyle);
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
