package com.mde.potdroid.views;

import android.content.Context;
import android.util.AttributeSet;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;

public class ObservableScrollBottomWebView extends ObservableWebView
{
    private boolean mEnd;
    private boolean mStart;

    public ObservableScrollBottomWebView(final Context context)
    {
        super(context);
    }

    public ObservableScrollBottomWebView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ObservableScrollBottomWebView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    }

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
}