package com.mde.potdroid.helpers;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import com.mde.potdroid.R;


public class LoginPreference extends DialogPreference {
    public LoginPreference(Context context) {
        super(context, null);
    }

    public LoginPreference(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public LoginPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LoginPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.dialog_login;
    }

}
