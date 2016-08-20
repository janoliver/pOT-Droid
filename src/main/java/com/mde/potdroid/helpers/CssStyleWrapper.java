package com.mde.potdroid.helpers;

import android.content.Context;
import android.graphics.Color;
import com.mde.potdroid.R;

import java.util.Locale;

public class CssStyleWrapper {

    private Context mContext;

    public CssStyleWrapper(Context cx) {
        mContext = cx;
    }

    public String getWindowBackgroundColor() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbTopicBackgroundColor));
    }

    public String getPostBackgroundColor() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbPostBackgroundColor));
    }

    public String getHeaderBackgroundColor() {
        return cf(Utils.getColorByAttr(mContext,  R.attr.bbPostHeaderBackgroundColor));
    }

    public String getTextColorPrimary() {
        return cf(Utils.getColorByAttr(mContext,  R.attr.bbTextColorPrimary));
    }

    public String getTextColorSecondary() {
        return cf(Utils.getColorByAttr(mContext,  R.attr.bbTextColorSecondary));
    }

    public String getTextColorTertiary() {
        return cf(Utils.getColorByAttr(mContext,  R.attr.bbTextColorTertiary));
    }

    public String getErrorColor() {
        return cf(Utils.getColorByAttr(mContext,  R.attr.bbErrorColor));
    }

    public String getQuoteBackgroundColor() {
        return cf(Utils.getColorByAttr(mContext,  R.attr.bbQuoteBackgroundColor));
    }

    public String getQuoteHeaderColor() {
        return cf(Utils.getColorByAttr(mContext,  R.attr.bbQuoteHeaderColor));
    }

    public String getMediaBackgroundColor() {
        return cf(Utils.getColorByAttr(mContext,  R.attr.bbMediaBackgroundColor));
    }

    public String getMediaForegroundColor() {
        return cf(Utils.getColorByAttr(mContext,  R.attr.bbMediaForegroundColor));
    }

    public String getNewPostGlowColor() {
        return cf(Utils.getColorByAttr(mContext,  R.attr.bbNewPostGlowColor));
    }

    public String cf(int c) {
        return String.format(Locale.US ,"rgba(%d, %d, %d, %.2f)", Color.red(c), Color.green(c), Color.blue(c), Color.alpha(c) / 255.);
    }
}
