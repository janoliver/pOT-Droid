package com.mde.potdroid.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.mde.potdroid.R;
import com.mde.potdroid.views.IconButton;

/**
 * This Fragment extends BaseFragment and provides some more methods and an interface
 * for those Fragments who have pagination functionality.
 */
abstract public class PaginateFragment extends BaseFragment {

    private LinearLayout mPaginateLayout;
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

    public void setSwipeTarget(View v) {
        if(mSettings.isSwipeToPaginate())
            v.setOnTouchListener(new PaginateDragListener());
    }

    public void refreshPaginateLayout() {
        mPaginateLayout = getBaseActivity().getPaginateLayout();
        mFastscrollLayout = getBaseActivity().getFastscrollLayout();
        mUpButton = (IconButton) mFastscrollLayout.findViewById(R.id.button_up);
        mDownButton = (IconButton) mFastscrollLayout.findViewById(R.id.button_down);

        if (!mSettings.isShowPaginateToolbar()) {
            mPaginateLayout.setVisibility(View.GONE);
            return;
        }

        IconButton fwdButton = (IconButton) mPaginateLayout.findViewById(R.id.button_fwd);
        IconButton ffwdButton = (IconButton) mPaginateLayout.findViewById(R.id.button_ffwd);
        IconButton rwdButton = (IconButton) mPaginateLayout.findViewById(R.id.button_rwd);
        IconButton frwdButton = (IconButton) mPaginateLayout.findViewById(R.id.button_frwd);
        IconButton refreshButton = (IconButton) mPaginateLayout.findViewById(R.id.button_refresh);

        fwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNextPage();
            }
        });

        ffwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLastPage();
            }
        });

        rwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPrevPage();
            }
        });

        frwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFirstPage();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPage();
            }
        });

        boolean anyVisible = false;

        // only show the paginate buttons if there are before or after the current
        if (isLastPage()) {
            fwdButton.setVisibility(View.INVISIBLE);
            ffwdButton.setVisibility(View.INVISIBLE);
        } else {
            anyVisible = true;
            fwdButton.setVisibility(View.VISIBLE);
            ffwdButton.setVisibility(View.VISIBLE);
        }

        if (isFirstPage()) {
            rwdButton.setVisibility(View.INVISIBLE);
            frwdButton.setVisibility(View.INVISIBLE);
        } else {
            anyVisible = true;
            rwdButton.setVisibility(View.VISIBLE);
            frwdButton.setVisibility(View.VISIBLE);
        }

        if (anyVisible) {
            getBaseActivity().showPaginateView();
        } else {
            getBaseActivity().hidePaginateView();
        }

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
        private float min_distance;

        public PaginateDragListener() {
            super();

            DisplayMetrics dm = getResources().getDisplayMetrics();
            min_distance = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, dm);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                start_x = event.getX();
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = start_x - event.getX();

                if (dx > min_distance && !isLastPage())
                    goToNextPage();
                if (dx < -min_distance && !isFirstPage())
                    goToPrevPage();
            }
            return false;
        }
    }

}
