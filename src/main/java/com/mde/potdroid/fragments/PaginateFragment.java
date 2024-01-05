package com.mde.potdroid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.*;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.mde.potdroid.BoardActivity;
import com.mde.potdroid.BookmarkActivity;
import com.mde.potdroid.ForumActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;

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
    private ImageButton mRefreshButton;
    private ImageButton mHomeButton;
    private ImageButton mFwdButton;
    private ImageButton mFfwdButton;
    private ImageButton mRwdButton;
    private ImageButton mFrwdButton;
    private ImageButton mWriteButton;

    public abstract void goToFirstPage();

    public abstract void goToLastPage();

    public abstract void goToNextPage();

    public abstract void nextButtonLongClick();

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

        if (mSettings.isBottomToolbar()) {
            getBaseActivity().enableBottomToolbar();

            mRefreshButton = (ImageButton) getBaseActivity().getBottomToolbar().findViewById(R.id.button_refresh);
            mFwdButton = (ImageButton) getBaseActivity().getBottomToolbar().findViewById(R.id.button_fwd);
            mFfwdButton = (ImageButton) getBaseActivity().getBottomToolbar().findViewById(R.id.button_ffwd);
            mRwdButton = (ImageButton) getBaseActivity().getBottomToolbar().findViewById(R.id.button_rwd);
            mFrwdButton = (ImageButton) getBaseActivity().getBottomToolbar().findViewById(R.id.button_frwd);
            mWriteButton = (ImageButton) getBaseActivity().getBottomToolbar().findViewById(R.id.button_write);
            mHomeButton = (ImageButton) getBaseActivity().getBottomToolbar().findViewById(R.id.button_home);

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
            mHomeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToHomePage();
                }
            });

            mFwdButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    nextButtonLongClick();
                    return true;
                }
            });

            if(!Utils.isLoggedIn())
                mWriteButton.setVisibility(View.GONE);
        }

        refreshPaginateLayout();
    }

    public void goToHomePage() {
        Intent intent;
        if (mSettings.getStartActivity() == SettingsWrapper.START_FORUM) {
            intent = new Intent(getBaseActivity(), BoardActivity.class);
            intent.putExtra(BoardFragment.ARG_ID, mSettings.getStartForum());
            intent.putExtra(BoardFragment.ARG_PAGE, 1);
        } else if (mSettings.getStartActivity() == SettingsWrapper.START_BOOKMARKS && Utils.isLoggedIn()) {
            intent = new Intent(getBaseActivity(), BookmarkActivity.class);
        } else {
            intent = new Intent(getBaseActivity(), ForumActivity.class);
        }

        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (mSettings.isBottomToolbar())
            return;

        inflater.inflate(R.menu.actionmenu_paginate, menu);

        MenuItem prev_item = menu.findItem(R.id.prev);
        MenuItem next_item = menu.findItem(R.id.next);

        if (isFirstPage()) {
            prev_item.setVisible(false);
        }

        if (isLastPage()) {
            next_item.setVisible(false);
        }

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
        if (mSettings.isSwipeToPaginate())
            v.setOnTouchListener(new PaginateDragListener());
    }

    public void refreshPaginateLayout() {
        getBaseActivity().invalidateOptionsMenu();

        if (mSettings.isBottomToolbar()) {
            if (isFirstPage()) {
                Utils.setImageButtonEnabled(false, mRwdButton);
                Utils.setImageButtonEnabled(false, mFrwdButton);
            } else {
                Utils.setImageButtonEnabled(true, mRwdButton);
                Utils.setImageButtonEnabled(true, mFrwdButton);
            }

            if (isLastPage()) {
                Utils.setImageButtonEnabled(false, mFwdButton);
                Utils.setImageButtonEnabled(false, mFfwdButton);
            } else {
                Utils.setImageButtonEnabled(true, mFwdButton);
                Utils.setImageButtonEnabled(true, mFfwdButton);
            }
        }

        mFastscrollLayout = getBaseActivity().getFastscrollLayout();
        mUpButton = (FloatingActionButton) mFastscrollLayout.findViewById(R.id.button_up);
        mDownButton = (FloatingActionButton) mFastscrollLayout.findViewById(R.id.button_down);
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
        if (mDownButton.getVisibility() == View.VISIBLE)
            mDownButton.setVisibility(View.GONE);
    }

    public void showDownButton() {
        mUpHandler.removeCallbacks(mUpRunnable);
        mDownHandler.removeCallbacks(mDownRunnable);
        mDownHandler.postDelayed(mDownRunnable, 1500);

        if (mDownButton.getVisibility() == View.GONE) {
            mDownButton.setVisibility(View.VISIBLE);
        }
    }

    public void hideUpButton() {
        if (mUpButton.getVisibility() == View.VISIBLE)
            mUpButton.setVisibility(View.GONE);
    }

    public void showUpButton() {
        mDownHandler.removeCallbacks(mDownRunnable);
        mUpHandler.removeCallbacks(mUpRunnable);
        mUpHandler.postDelayed(mUpRunnable, 1500);

        if (mUpButton.getVisibility() == View.GONE)
            mUpButton.setVisibility(View.VISIBLE);
    }

    public ImageButton getWriteButton() {
        return mWriteButton;
    }

    public void disableFfwd() {
        Utils.setImageButtonEnabled(false, mFfwdButton);
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
            if (!mSwipeEnabled)
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
