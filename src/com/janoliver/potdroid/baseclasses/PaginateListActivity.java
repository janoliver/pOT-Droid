/*
 * Copyright (C) 2011 Jan Oliver Oelerich <janoliver@oelerich.org>
 *
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this software, and changing it is allowed as long as the 
 * name is changed.
 *
 *           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *  0. You just DO WHAT THE FUCK YOU WANT TO. 
 */

package com.janoliver.potdroid.baseclasses;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.janoliver.potdroid.R;

/**
 * @author oli
 * 
 *         This class should be inherited by activities that need pagination
 *         functionality. It enables fling detection to navigate back and forth.
 *         Also, it adds two buttons for this purpose to the option menu. Two
 *         methods are abstract and have to be implemented by classes inheriting
 *         this class.
 */
abstract class PaginateListActivity extends BaseListActivity {

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    protected GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gesture detection
        gestureDetector = new GestureDetector(new GestureDetection());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };

        // set view
        lv.setOnTouchListener(gestureListener);
    }

    // overwrite volume button behaviour
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("volumeControl", false)) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                showPreviousPage();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                showNextPage();
                return true;
            } else
                return super.onKeyDown(keyCode, event);
        } else
            return super.onKeyDown(keyCode, event);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("volumeControl", false)) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                return true;
            } else
                return super.onKeyDown(keyCode, event);
        } else
            return super.onKeyDown(keyCode, event);
    }

    class GestureDetection extends SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    PaginateListActivity.this.showNextPage();
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    PaginateListActivity.this.showPreviousPage();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.iconmenu_paginate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.next:
            showNextPage();
            return true;
        case R.id.previous:
            showPreviousPage();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    abstract void showNextPage();

    abstract void showPreviousPage();

}
