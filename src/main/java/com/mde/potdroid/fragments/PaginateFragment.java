package com.mde.potdroid.fragments;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.LinearLayout;
import com.mde.potdroid.R;
import com.mde.potdroid.views.IconButton;
import com.mde.potdroid.views.IconDrawable;

/**
 * This Fragment extends BaseFragment and provides some more methods and an interface
 * for those Fragments who have pagination functionality.
 */
abstract public class PaginateFragment extends BaseFragment {

    private LinearLayout mFastscrollLayout;
    private IconButton mUpButton;
    private IconButton mDownButton;
    private Handler mDownHandler = new Handler();
    private Runnable mDownRunnable = new Runnable() {
        @Override
        public void run() {
            hideDownButton();
        }
    };
    private Handler mUpHandler = new Handler();
    private Runnable mUpRunnable = new Runnable() {
        @Override
        public void run() {
            hideUpButton();
        }
    };


    public abstract void goToFirstPage();

    public abstract void goToLastPage();

    public abstract void goToNextPage();

    public abstract void refreshPage();

    public abstract void goToPrevPage();

    public abstract boolean isFirstPage();

    public abstract boolean isLastPage();

    public abstract ViewGroup getSwipeView();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        refreshPaginateLayout();

        if (getSwipeView() != null && mSettings.isSwipeToPaginate())
            getSwipeView().setOnTouchListener(new PaginateDragListener());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_paginate, menu);

        Drawable prev_icon = IconDrawable.getIconDrawable(getActivity(), R.string.icon_backward);
        MenuItem prev_item = menu.findItem(R.id.prev);
        Drawable next_icon = IconDrawable.getIconDrawable(getActivity(), R.string.icon_forward);
        MenuItem next_item = menu.findItem(R.id.next);

        if(isFirstPage()) {
            prev_item.setEnabled(false);
            prev_icon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }

        if(isLastPage()) {
            next_item.setEnabled(false);
            next_icon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }

        prev_item.setIcon(prev_icon);
        next_item.setIcon(next_icon);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.prev:
                // reload content
                goToPrevPage();
                return true;
            case R.id.next:
                // reload content
                goToNextPage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setSwipeTarget(View v) {
        if(mSettings.isSwipeToPaginate())
            v.setOnTouchListener(new PaginateDragListener());
    }

    public void refreshPaginateLayout() {
        getBaseActivity().invalidateOptionsMenu();

        mFastscrollLayout = getBaseActivity().getFastscrollLayout();
        mUpButton = (IconButton) mFastscrollLayout.findViewById(R.id.button_up);
        mDownButton = (IconButton) mFastscrollLayout.findViewById(R.id.button_down);

    }

    public void enableFastScroll(final FastScrollListener listener) {
        getBaseActivity().showFastscrollView();

        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onUpButtonClicked();
            }
        });
        mDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDownButtonClicked();
            }
        });
    }

    public void hideDownButton() {
        if(mDownButton.getVisibility() == View.VISIBLE)
            mDownButton.setVisibility(View.GONE);
    }

    public void showDownButton() {
        mUpHandler.removeCallbacks(mUpRunnable);
        mDownHandler.removeCallbacks(mDownRunnable);
        mDownHandler.postDelayed(mDownRunnable, 1500);

        if(mDownButton.getVisibility() == View.GONE) {
            mDownButton.setVisibility(View.VISIBLE);
        }
    }

    public void hideUpButton() {
        if(mUpButton.getVisibility() == View.VISIBLE)
            mUpButton.setVisibility(View.GONE);
    }

    public void showUpButton() {
        mDownHandler.removeCallbacks(mDownRunnable);
        mUpHandler.removeCallbacks(mUpRunnable);
        mUpHandler.postDelayed(mUpRunnable, 1500);

        if(mUpButton.getVisibility() == View.GONE)
            mUpButton.setVisibility(View.VISIBLE);
    }

    public interface FastScrollListener {
        public void onUpButtonClicked();

        public void onDownButtonClicked();
    }

    class PaginateDragListener implements View.OnTouchListener {
        private float start_x;
        private float start_y;
        private int swipeMinDistance;
        private int swipeMaxOffPath;

        public PaginateDragListener() {
            super();

            final ViewConfiguration vc = ViewConfiguration.get(getActivity());
            swipeMinDistance = vc.getScaledPagingTouchSlop() * 10;
            swipeMaxOffPath = vc.getScaledTouchSlop() * 4;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                start_x = event.getX();
                start_y = event.getY();
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = start_x - event.getX();
                float dy = Math.abs(start_y - event.getY());

                if (dx > swipeMinDistance && !isLastPage() && dy < swipeMaxOffPath)
                    goToNextPage();
                if (dx < -swipeMinDistance && !isFirstPage() && dy < swipeMaxOffPath)
                    goToPrevPage();
            }
            return false;
        }
    }

}
