package com.mde.potdroid.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * This is a button that uses the font awesome font face.
 */
public class IconView extends TextView {

    private static Typeface mFont;

    public static Typeface getTypeface(Context context, String typeface) {
        if (mFont == null)
            mFont = Typeface.createFromAsset(context.getAssets(), typeface);

        return mFont;
    }

    public IconView(Context context) {
        super(context);
        initTypeface();
    }

    public IconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTypeface();
    }

    public IconView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initTypeface();
    }

    @TargetApi(21)
    public IconView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initTypeface();
    }

    protected void initTypeface() {
        setTypeface(getTypeface(getContext(), "font-awesome/fonts/fontawesome-webfont.ttf"));
    }
}
