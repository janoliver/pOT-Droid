package com.mde.potdroid.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.LinearLayout;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.views.IconButton;
import com.mde.potdroid.views.IconDrawable;
import com.melnykov.fab.FloatingActionButton;

/**
 * This Fragment extends BaseFragment and provides some more methods and an interface
 * for those Fragments who have pagination functionality.
 */
abstract public class PaginateFragment extends BaseFragment {

    private LinearLayout mFastscrollLayout;
    private FloatingActionButton mUpButton;
    private FloatingActionButton mDownButton;
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
    private boolean mSwipeEnabled = true;
    private IconButton mRefreshButton;
    private IconButton mFwdButton;
    private IconButton mFfwdButton;
    private IconButton mRwdButton;
    private IconButton mFrwdButton;
    private IconButton mWriteButton;

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


        if (getSwipeView() != null && mSettings.isSwipeToPaginate())
            getSwipeView().setOnTouchListener(new PaginateDragListener());

        if(mSettings.isBottomToolbar()) {
            getBaseActivity().enableBottomToolbar();

            mRefreshButton = (IconButton) getBaseActivity().getBottomToolbar().findViewById(R.id.button_refresh);
            mFwdButton = (IconButton) getBaseActivity().getBottomToolbar().findViewById(R.id.button_fwd);
            mFfwdButton = (IconButton) getBaseActivity().getBottomToolbar().findViewById(R.id.button_ffwd);
            mRwdButton = (IconButton) getBaseActivity().getBottomToolbar().findViewById(R.id.button_rwd);
            mFrwdButton = (IconButton) getBaseActivity().getBottomToolbar().findViewById(R.id.button_frwd);
            mWriteButton = (IconButton) getBaseActivity().getBottomToolbar().findViewById(R.id.button_write);

            mRefreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshPage();
                }
            });
            mFwdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToNextPage();
                }
            });
            mFfwdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToLastPage();
                }
            });
            mRwdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToPrevPage();
                }
            });
            mFrwdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToFirstPage();
                }
            });

            if(!Utils.isLoggedIn())
                mWriteButton.setVisibility(View.GONE);
        }

        refreshPaginateLayout();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if(mSettings.isBottomToolbar())
            return;

        inflater.inflate(R.menu.actionmenu_paginate, menu);
        menu.findItem(R.id.refresh).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_refresh));

        Drawable prev_icon = IconDrawable.getIconDrawable(getActivity(), R.string.icon_backward);
        MenuItem prev_item = menu.findItem(R.id.prev);
        Drawable next_icon = IconDrawable.getIconDrawable(getActivity(), R.string.icon_forward);
        MenuItem next_item = menu.findItem(R.id.next);

        if(isFirstPage()) {
            prev_item.setVisible(false);
            //prev_icon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }

        if(isLastPage()) {
            next_item.setVisible(false);
        }

        prev_item.setIcon(prev_icon);
        next_item.setIcon(next_icon);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                // reload content
                refreshPage();
                return true;
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

    public void setSwipeEnabled(boolean enabled) {
        mSwipeEnabled = enabled;
    }

    public void setSwipeTarget(View v) {
        if(mSettings.isSwipeToPaginate())
            v.setOnTouchListener(new PaginateDragListener());
    }

    public void refreshPaginateLayout() {
        getBaseActivity().invalidateOptionsMenu();

        if(mSettings.isBottomToolbar()) {
            if(isFirstPage()) {
                mRwdButton.disable();
                mFrwdButton.disable();
            } else {
                mRwdButton.enable();
                mFrwdButton.enable();
            }

            if(isLastPage()) {
                mFwdButton.disable();
                mFfwdButton.disable();
            } else {
                mFwdButton.enable();
                mFfwdButton.enable();
            }
        }

        mFastscrollLayout = getBaseActivity().getFastscrollLayout();
        mUpButton = (FloatingActionButton) mFastscrollLayout.findViewById(R.id.button_up);
        mDownButton = (FloatingActionButton) mFastscrollLayout.findViewById(R.id.button_down);

        mUpButton.setImageDrawable(IconDrawable.getIconDrawable(getActivity(), R.string.icon_chevron_up));
        mDownButton.setImageDrawable(IconDrawable.getIconDrawable(getActivity(), R.string.icon_chevron_down));
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

    public IconButton getmWriteButton() {
        return mWriteButton;
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
        private boolean mSwiping;

        public PaginateDragListener() {
            super();

            final ViewConfiguration vc = ViewConfiguration.get(getActivity());
            swipeMinDistance = vc.getScaledPagingTouchSlop() * 7;
            swipeMaxOffPath = vc.getScaledTouchSlop() * 4;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(!mSwipeEnabled)
                return false;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                start_x = event.getX();
                start_y = event.getY();
                mSwiping = true;
            } else if (mSwiping && event.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = start_x - event.getX();
                float dy = Math.abs(start_y - event.getY());

                if (dx > swipeMinDistance && !isLastPage() && dy < swipeMaxOffPath) {
                    goToNextPage();
                    mSwiping = false;
                }
                if (dx < -swipeMinDistance && !isFirstPage() && dy < swipeMaxOffPath) {
                    goToPrevPage();
                    mSwiping = false;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                start_x = event.getX();
                start_y = event.getY();
                mSwiping = false;
            }
            return false;
        }
    }

}
