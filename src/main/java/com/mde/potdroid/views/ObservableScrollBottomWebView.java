package com.mde.potdroid.views;

import android.content.Context;
import android.util.AttributeSet;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;

public class ObservableScrollBottomWebView extends ObservableWebView
{
    private boolean mEnd;
    private boolean mStart;

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        int tek = (int) Math.floor(getContentHeight() * getScale());
        if(tek - getScrollY() == getHeight())
            mEnd = true;
        else
            mEnd = false;

        mStart = getScrollY() == 0;
    }

    public boolean isScrolledToBottom() {
        return mEnd;
    }

    public boolean isScrolledToTop() {
        return mStart;
    }

    public void scrollToBottom() {

        scrollTo(0, (int)(getContentHeight() * getScale()));
    }

    public void scrollToTop() {
        scrollTo(0, 0);
    }

    @SuppressWarnings("unused")
    public ObservableScrollBottomWebView(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public ObservableScrollBottomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public ObservableScrollBottomWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}