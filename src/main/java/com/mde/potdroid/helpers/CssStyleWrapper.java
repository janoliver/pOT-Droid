package com.mde.potdroid.helpers;

import android.content.Context;
import android.graphics.Color;

public class CssStyleWrapper {

    private Context mContext;
    private SettingsWrapper mSettings;

    public CssStyleWrapper(Context cx) {
        mContext = cx;
        mSettings = new SettingsWrapper(cx);
    }

    public String getBackgroundColor() {
        return cf(Utils.getColorByAttr(mContext, android.R.attr.windowBackground));
    }

    public String cf(int c) {
        return String.format("rgba(%d, %d, %d, %d)", Color.red(c), Color.green(c), Color.blue(c), Color.alpha(c));
    }
}
