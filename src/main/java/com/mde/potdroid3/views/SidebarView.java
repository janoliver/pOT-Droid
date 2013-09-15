package com.mde.potdroid3.views;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by oli on 9/15/13.
 */
public class SidebarView extends DrawerLayout {
    public SidebarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SidebarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SidebarView(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean ret = super.onInterceptTouchEvent(ev);
        // always false and event will be send to each view
        if(isDrawerOpen(this))
            return false;
        else
            return ret;
    }
}
