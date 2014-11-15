package com.mde.potdroid.views;


import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.TypedValue;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.Utils;

public class IconDrawable extends Drawable {
    private String mText;
    private TextPaint mPaint;
    private Rect mRect;
    private static Typeface mFont;

    public static Typeface getTypeface(Context context, String typeface) {
        if (mFont == null)
            mFont = Typeface.createFromAsset(context.getAssets(), typeface);

        return mFont;
    }

    public static int getDefaultColor(Context cx) {
        return Utils.getColorByAttr(cx, R.attr.bbToolbarColor);
    }

    public static IconDrawable getIconDrawable(Context cx, int icon_string) {
        return getIconDrawable(cx, icon_string, 24, getDefaultColor(cx), 204);
    }

    public static IconDrawable getIconDrawable(Context cx, int icon_string, int text_size) {
        return getIconDrawable(cx, icon_string, text_size, getDefaultColor(cx), 204);
    }

    public static IconDrawable getIconDrawable(Context cx,
                                               int icon_string, int text_size, int color) {
        return getIconDrawable(cx, icon_string, text_size, color, 204);
    }

    public static IconDrawable getIconDrawable(Context cx,
                                               int icon_string, int text_size, int color, int alpha) {
        IconDrawable d = new IconDrawable(cx);

        d.setText(cx.getString(icon_string));
        d.setColor(color);
        d.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                text_size, cx.getResources().getDisplayMetrics()));
        d.setAlpha(alpha);

        return d;
    }

    public IconDrawable(Context cx) {
        mPaint = new TextPaint();
        mRect = new Rect();

        mPaint.setTypeface(getTypeface(cx, "font-awesome/fonts/fontawesome-webfont.ttf"));
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.LEFT);
    }

    public void setText(String text) {
        mText = text != null ? text : "";
        invalidateSelf();
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        invalidateSelf();
    }

    public void setTextSize(float size) {
        mPaint.setTextSize(size);
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void invalidateSelf() {
        super.invalidateSelf();

        mPaint.getTextBounds(mText, 0, mText.length(), mRect);
    }

    @Override
    public int getIntrinsicWidth() {
        return mRect.width();
    }

    @Override
    public int getIntrinsicHeight() {
        return mRect.height();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawText(mText, -mRect.left, -mRect.top, mPaint);
    }
}

