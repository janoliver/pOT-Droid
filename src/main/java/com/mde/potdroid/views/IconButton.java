package com.mde.potdroid.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * This is a button that uses the font awesome font face.
 */
public class IconButton extends Button {

    public IconButton(Context context) {
        super(context);
    }

    public IconButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void initTypeface() {
        Typeface font = Typeface.createFromAsset( getContext().getAssets(),
                "font-awesome/fonts/fontawesome-webfont.ttf" );
        setTypeface(font);
    }

}
