package com.mde.potdroid.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * This is a button that uses the font awesome font face.
 */
public class IconButton extends Button {

    private static Typeface mFont;

    public static Typeface getTypeface(Context context, String typeface) {
        if (mFont == null)
            mFont = Typeface.createFromAsset(context.getAssets(), typeface);

        return mFont;
    }

    public IconButton(Context context) {
        super(context);
        initTypeface();
    }

    public IconButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTypeface();
    }

    public IconButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initTypeface();
    }

    protected void initTypeface() {
        setTypeface(getTypeface(getContext(), "font-awesome/fonts/fontawesome-webfont.ttf"));
        setTextColor(getTextColors().withAlpha(204));
    }

    public void disable() {
        setEnabled(false);
        setTextColor(getTextColors().withAlpha(76));
    }

    public void enable() {
        setEnabled(true);
        setTextColor(getTextColors().withAlpha(204));
    }

    public void setColor(int color) {
        setTextColor(color);
        if(isEnabled())
            setTextColor(getTextColors().withAlpha(204));
        else
            setTextColor(getTextColors().withAlpha(76));

    }

}
