package com.mde.potdroid.helpers.ptr;

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import com.mde.potdroid.R;


/**
 * The SwipeRefreshLayout should be used whenever the user can refresh the
 * contents of a view via a vertical swipe gesture. The activity that
 * instantiates this view should add an OnRefreshListener to be notified
 * whenever the swipe to refresh gesture is completed. The SwipeRefreshLayout
 * will notify the listener each and every time the gesture is completed again;
 * the listener is responsible for correctly determining when to actually
 * initiate a refresh of its content. If the listener determines there should
 * not be a refresh, it must call setRefreshing(false) to cancel any visual
 * indication of a refresh. If an activity wishes to show just the progress
 * animation, it should call setRefreshing(true). To disable the gesture and
 * progress animation, call setEnabled(false) on the view.
 * <p>
 * This layout should be made the parent of the view that will be refreshed as a
 * result of the gesture and can only support one direct child. This view will
 * also be made the target of the gesture and will be forced to match both the
 * width and the height supplied in this layout. The SwipeRefreshLayout does not
 * provide accessibility events; instead, a menu item must be provided to allow
 * refresh of the content wherever this gesture is used.
 * </p>
 */
public class SwipyRefreshLayout extends ViewGroup {

    public static final String TAG = "SwipyRefreshLayout";

    private static final float MAX_SWIPE_DISTANCE_FACTOR = .4f;
    private static final int REFRESH_TRIGGER_DISTANCE = 90;

    // Maps to ProgressBar.Large style
    public static final int LARGE = MaterialProgressDrawable.LARGE;
    // Maps to ProgressBar default style
    public static final int DEFAULT = MaterialProgressDrawable.DEFAULT;

    private static final String LOG_TAG = SwipyRefreshLayout.class.getSimpleName();

    private static final int MAX_ALPHA = 255;
    private static final int STARTING_PROGRESS_ALPHA = (int) (.3f * MAX_ALPHA);

    private static final int CIRCLE_DIAMETER = 40;
    private static final int CIRCLE_DIAMETER_LARGE = 56;

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final int INVALID_POINTER = -1;
    private static final float DRAG_RATE = .7f;

    // Max amount of circle that can be filled by progress during swipe gesture,
    // where 1.0 is a full circle
    private static final float MAX_PROGRESS_ANGLE = .8f;

    private static final int SCALE_DOWN_DURATION = 150;

    private static final int ALPHA_ANIMATION_DURATION = 300;

    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;

    private static final int ANIMATE_TO_START_DURATION = 200;

    // Default background for the progress spinner
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    // Default offset in dips from the top of the view to where the progress spinner should stop
    private static final int DEFAULT_CIRCLE_TARGET = 64;

    private View mTarget; // the target of the gesture
    private View mChildContainer; // the target of the gesture
    private SwipyRefreshLayoutDirection mDirection;
    private boolean mBothDirection;
    private OnRefreshListener mListener;
    private boolean mRefreshing = false;
    private int mTouchSlop;
    private float mTotalDragDistance = -1;
    private int mMediumAnimationDuration;
    private int mCurrentTargetOffsetTop;
    // Whether or not the starting offset has been determined.
    private boolean mOriginalOffsetCalculated = false;

    private float mInitialMotionY;
    private float mInitialDownY;
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;
    // Whether this item is scaled up rather than clipped
    private boolean mScale;

    // Target is returning to its start offset because it was cancelled or a
    // refresh was triggered.
    private boolean mReturningToStart;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };

    private CircleImageView mCircleView;
    private int mCircleViewIndex = -1;

    protected int mFrom;

    private float mStartingScale;

    protected int mOriginalOffsetTop;

    private MaterialProgressDrawable mProgress;

    private Animation mScaleAnimation;

    private Animation mScaleDownAnimation;

    private Animation mAlphaStartAnimation;

    private Animation mAlphaMaxAnimation;

    private Animation mScaleDownToStartAnimation;

    private float mSpinnerFinalOffset;

    private boolean mNotify;

    private int mCircleWidth;

    private int mCircleHeight;

    int mAdapterViewId;

    // Whether the client has set a custom starting position;
    private boolean mUsingCustomStart;

    private AnimationListener mRefreshListener = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mRefreshing) {
                // Make sure the progress view is fully visible
                mProgress.setAlpha(MAX_ALPHA);
                mProgress.start();
                if (mNotify) {
                    if (mListener != null) {
                        mListener.onRefresh(mDirection);
                    }
                }
            } else {
                mProgress.stop();
                mCircleView.setVisibility(View.GONE);
                setColorViewAlpha(MAX_ALPHA);
                // Return the circle to its start position
                if (mScale) {
                    setAnimationProgress(0 /* animation complete and view is hidden */);
                } else {
                    setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentTargetOffsetTop,
                            true /* requires update */);
                }
            }
            mCurrentTargetOffsetTop = mCircleView.getTop();
        }
    };

    private void setColorViewAlpha(int targetAlpha) {
        mCircleView.getBackground().setAlpha(targetAlpha);
        mProgress.setAlpha(targetAlpha);
    }

    /**
     * The refresh indicator starting and resting position is always positioned
     * near the top of the refreshing content. This position is a consistent
     * location, but can be adjusted in either direction based on whether or not
     * there is a toolbar or actionbar present.
     *
     * @param scale Set to true if there is no view at a higher z-order than
     *            where the progress spinner is set to appear.
     * @param start The offset in pixels from the top of this view at which the
     *            progress spinner should appear.
     * @param end The offset in pixels from the top of this view at which the
     *            progress spinner should come to rest after a successful swipe
     *            gesture.
     */

    public void setProgressViewOffset(boolean scale, int start, int end) {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();

        mScale = scale;
        mCircleView.setVisibility(View.GONE);

        /*switch (mDirection) {
            case BOTTOM:
                mCurrentTargetOffsetTop = mOriginalOffsetTop = getMeasuredHeight() - end;
                break;
            case TOP:
            default:
                mCurrentTargetOffsetTop = mOriginalOffsetTop = -mCircleView.getMeasuredHeight() + start;
                break;
        }*/
        mCurrentTargetOffsetTop = mOriginalOffsetTop = -mCircleView.getMeasuredHeight() + start;

        mSpinnerFinalOffset = DEFAULT_CIRCLE_TARGET * metrics.density + end;

        mUsingCustomStart = true;
        mCircleView.invalidate();

    }

    public void addProgressViewOffset(int start, int end) {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();

        mCircleView.setVisibility(View.GONE);

        /*switch (mDirection) {
            case BOTTOM:
                mCurrentTargetOffsetTop = mOriginalOffsetTop = getMeasuredHeight() - end;
                break;
            case TOP:
            default:
                mCurrentTargetOffsetTop = mOriginalOffsetTop = -mCircleView.getMeasuredHeight() + start;
                break;
        }*/
        mCurrentTargetOffsetTop = mOriginalOffsetTop = -mCircleView.getMeasuredHeight() + start;

        mSpinnerFinalOffset = DEFAULT_CIRCLE_TARGET * metrics.density + end;

        mUsingCustomStart = true;
        mCircleView.invalidate();
    }

    /**
     * The refresh indicator resting position is always positioned near the top
     * of the refreshing content. This position is a consistent location, but
     * can be adjusted in either direction based on whether or not there is a
     * toolbar or actionbar present.
     *
     * @param scale Set to true if there is no view at a higher z-order than
     *            where the progress spinner is set to appear.
     * @param end The offset in pixels from the top of this view at which the
     *            progress spinner should come to rest after a successful swipe
     *            gesture.
     */

    public void setProgressViewEndTarget(boolean scale, int end) {
        mSpinnerFinalOffset = end;
        mScale = scale;
        mCircleView.invalidate();
    }

    /**
     * One of DEFAULT, or LARGE.
     */
    public void setSize(int size) {
        if (size != MaterialProgressDrawable.LARGE && size != MaterialProgressDrawable.DEFAULT) {
            return;
        }
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (size == MaterialProgressDrawable.LARGE) {
            mCircleHeight = mCircleWidth = (int) (CIRCLE_DIAMETER_LARGE * metrics.density);
        } else {
            mCircleHeight = mCircleWidth = (int) (CIRCLE_DIAMETER * metrics.density);
        }
        // force the bounds of the progress circle inside the circle view to
        // update by setting it to null before updating its size and then
        // re-setting it
        mCircleView.setImageDrawable(null);
        mProgress.updateSizes(size);
        mCircleView.setImageDrawable(mProgress);
    }

    /**
     * Simple constructor to use when creating a SwipeRefreshLayout from code.
     *
     * @param context
     */
    public SwipyRefreshLayout(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating SwipeRefreshLayout from XML.
     *
     * @param context
     * @param attrs
     */
    public SwipyRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mMediumAnimationDuration = getResources().getInteger(
                android.R.integer.config_mediumAnimTime);

        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();

        final TypedArray a2 = context.obtainStyledAttributes(attrs, R.styleable.SwipyRefreshLayout);
        mAdapterViewId = a2.getResourceId(R.styleable.SwipyRefreshLayout_adapter_view, -1);
        SwipyRefreshLayoutDirection direction
                = SwipyRefreshLayoutDirection.getFromInt(a2.getInt(R.styleable.SwipyRefreshLayout_srl_direction, 0));
        if (direction != SwipyRefreshLayoutDirection.BOTH) {
            mDirection = direction;
            mBothDirection = false;
        } else {
            mDirection = SwipyRefreshLayoutDirection.TOP;
            mBothDirection = true;
        }
        a2.recycle();

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mCircleWidth = (int) (CIRCLE_DIAMETER * metrics.density);
        mCircleHeight = (int) (CIRCLE_DIAMETER * metrics.density);

        createProgressView();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        // the absolute offset has to take into account that the circle starts at an offset
        mSpinnerFinalOffset = DEFAULT_CIRCLE_TARGET * metrics.density;

    }

    protected int getChildDrawingOrder(int childCount, int i) {
        if (mCircleViewIndex < 0) {
            return i;
        } else if (i == childCount - 1) {
            // Draw the selected child last
            return mCircleViewIndex;
        } else if (i >= mCircleViewIndex) {
            // Move the children after the selected child earlier one
            return i + 1;
        } else {
            // Keep the children before the selected child the same
            return i;
        }
    }

    private void createProgressView() {
        mCircleView = new CircleImageView(getContext(), CIRCLE_BG_LIGHT, CIRCLE_DIAMETER / 2);
        mProgress = new MaterialProgressDrawable(getContext(), this);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        mCircleView.setImageDrawable(mProgress);
        mCircleView.setVisibility(View.GONE);
        addView(mCircleView);
    }

    /**
     * Set the listener to be notified when a refresh is triggered via the swipe
     * gesture.
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    /**
     * Pre API 11, alpha is used to make the progress circle appear instead of scale.
     */
    private boolean isAlphaUsedForScale() {
        return android.os.Build.VERSION.SDK_INT < 11;
    }

    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     *
     * @param refreshing Whether or not the view should show refresh progress.
     */
    public void setRefreshing(boolean refreshing) {
        if (refreshing && mRefreshing != refreshing) {
            // scale and show
            mRefreshing = refreshing;
            int endTarget = 0;
            if (!mUsingCustomStart) {
                /*switch (mDirection) {
                    case BOTTOM:
                        endTarget = getMeasuredHeight() - (int) (mSpinnerFinalOffset);
                        break;
                    case TOP:
                    default:
                        endTarget = (int) (mSpinnerFinalOffset - Math.abs(mOriginalOffsetTop));
                        break;
                }*/
                endTarget = (int) (mSpinnerFinalOffset - Math.abs(mOriginalOffsetTop));
            } else {
                endTarget = (int) mSpinnerFinalOffset;
            }
            setTargetOffsetTopAndBottom(endTarget - mCurrentTargetOffsetTop,
                    true /* requires update */);
            mNotify = false;
            startScaleUpAnimation(mRefreshListener);
        } else {
            setRefreshing(refreshing, false /* notify */);
        }
    }

    private void startScaleUpAnimation(AnimationListener listener) {
        mCircleView.setVisibility(View.VISIBLE);
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            // Pre API 11, alpha is used in place of scale up to show the
            // progress circle appearing.
            // Don't adjust the alpha during appearance otherwise.
            mProgress.setAlpha(MAX_ALPHA);
        }
        mScaleAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setAnimationProgress(interpolatedTime);
            }
        };
        mScaleAnimation.setDuration(mMediumAnimationDuration);
        if (listener != null) {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleAnimation);
    }

    /**
     * Pre API 11, this does an alpha animation.
     *
     * @param progress
     */
    private void setAnimationProgress(float progress) {
        if (isAlphaUsedForScale()) {
            setColorViewAlpha((int) (progress * MAX_ALPHA));
        } else {
            ViewCompat.setScaleX(mCircleView, progress);
            ViewCompat.setScaleY(mCircleView, progress);
        }
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            ensureChildContainer();
            mRefreshing = refreshing;
            if (mRefreshing) {
                animateOffsetToCorrectPosition(mCurrentTargetOffsetTop, mRefreshListener);
            } else {
                startScaleDownAnimation(mRefreshListener);
            }
        }
    }

    private void startScaleDownAnimation(AnimationListener listener) {
        mScaleDownAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setAnimationProgress(1 - interpolatedTime);
            }
        };
        mScaleDownAnimation.setDuration(SCALE_DOWN_DURATION);
        mCircleView.setAnimationListener(listener);
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleDownAnimation);
    }

    private void startProgressAlphaStartAnimation() {
        mAlphaStartAnimation = startAlphaAnimation(mProgress.getAlpha(), STARTING_PROGRESS_ALPHA);
    }

    private void startProgressAlphaMaxAnimation() {
        mAlphaMaxAnimation = startAlphaAnimation(mProgress.getAlpha(), MAX_ALPHA);
    }

    private Animation startAlphaAnimation(final int startingAlpha, final int endingAlpha) {
        // Pre API 11, alpha is used in place of scale. Don't also use it to
        // show the trigger point.
        if (mScale && isAlphaUsedForScale()) {
            return null;
        }
        Animation alpha = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                mProgress
                        .setAlpha((int) (startingAlpha + ((endingAlpha - startingAlpha)
                                * interpolatedTime)));
            }
        };
        alpha.setDuration(ALPHA_ANIMATION_DURATION);
        // Clear out the previous animation listeners.
        mCircleView.setAnimationListener(null);
        mCircleView.clearAnimation();
        mCircleView.startAnimation(alpha);
        return alpha;
    }

    /**
     * Set the background color of the progress spinner disc.
     *
     * @param colorRes Resource id of the color.
     */
    public void setProgressBackgroundColor(int colorRes) {
        mCircleView.setBackgroundColor(colorRes);
        mProgress.setBackgroundColor(getResources().getColor(colorRes));
    }

    /**
     * @deprecated Use {@link #setColorSchemeResources(int...)}
     */
    @Deprecated
    public void setColorScheme(int... colors) {
        setColorSchemeResources(colors);
    }

    /**
     * Set the color resources used in the progress animation from color resources.
     * The first color will also be the color of the bar that grows in response
     * to a user swipe gesture.
     *
     * @param colorResIds
     */
    public void setColorSchemeResources(int... colorResIds) {
        final Resources res = getResources();
        int[] colorRes = new int[colorResIds.length];
        for (int i = 0; i < colorResIds.length; i++) {
            colorRes[i] = res.getColor(colorResIds[i]);
        }
        setColorSchemeColors(colorRes);
    }

    /**
     * Set the colors used in the progress animation. The first
     * color will also be the color of the bar that grows in response to a user
     * swipe gesture.
     *
     * @param colors
     */
    public void setColorSchemeColors(int... colors) {
        ensureTarget();
        ensureChildContainer();
        mProgress.setColorSchemeColors(colors);
    }

    /**
     * @return Whether the SwipeRefreshWidget is actively showing refresh
     * progress.
     */
    public boolean isRefreshing() {
        return mRefreshing;
    }

    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            mTarget = findViewById(mAdapterViewId);
            if(mTarget == null) {
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    if (!child.equals(mCircleView)) {
                        mTarget = child;
                        break;
                    }
                }
            }
        }
        if (mTotalDragDistance == -1) {
            if (getParent() != null && ((View) getParent()).getHeight() > 0) {
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                mTotalDragDistance = (int) Math.min(
                        ((View) getParent()).getHeight() * MAX_SWIPE_DISTANCE_FACTOR,
                        REFRESH_TRIGGER_DISTANCE * metrics.density);
            }
        }
    }

    private void ensureChildContainer() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mChildContainer == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mCircleView)) {
                    mChildContainer = child;
                    break;
                }
            }
        }
    }

    /**
     * Set the distance to trigger a sync in dips
     *
     * @param distance
     */
    public void setDistanceToTriggerSync(int distance) {
        mTotalDragDistance = distance;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        if (mTarget == null) {
            ensureTarget();
        }
        if(mChildContainer == null) {
            ensureChildContainer();
        }
        if (mTarget == null || mChildContainer == null) {
            return;
        }
        final View child = mChildContainer;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        int circleWidth = mCircleView.getMeasuredWidth();
        int circleHeight = mCircleView.getMeasuredHeight();
        mCircleView.layout((width / 2 - circleWidth / 2), mCurrentTargetOffsetTop,
                (width / 2 + circleWidth / 2), mCurrentTargetOffsetTop + circleHeight);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mChildContainer == null) {
            ensureTarget();
        }
        if (mChildContainer == null) {
            return;
        }
        mChildContainer.measure(MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        mCircleView.measure(MeasureSpec.makeMeasureSpec(mCircleWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mCircleHeight, MeasureSpec.EXACTLY));
        if (!mUsingCustomStart && !mOriginalOffsetCalculated) {
            mOriginalOffsetCalculated = true;

            switch (mDirection) {
                case BOTTOM:
                    mCurrentTargetOffsetTop = mOriginalOffsetTop = getMeasuredHeight();
                    break;
                case TOP:
                default:
                    mCurrentTargetOffsetTop = mOriginalOffsetTop = -mCircleView.getMeasuredHeight();
                    break;
            }
        }
        mCircleViewIndex = -1;
        // Get the index of the circleview.
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mCircleView) {
                mCircleViewIndex = index;
                break;
            }
        }
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }
//    public boolean canChildScrollUp() {
//        if (android.os.Build.VERSION.SDK_INT < 14) {
//            if (mTarget instanceof AbsListView) {
//                final AbsListView absListView = (AbsListView) mTarget;
//                if (absListView.getLastVisiblePosition() + 1 == absListView.getCount()) {
//                    int lastIndex = absListView.getLastVisiblePosition() - absListView.getFirstVisiblePosition();
//
//                    boolean res = absListView.getChildAt(lastIndex).getBottom() == absListView.getPaddingBottom();
//
//                    return res;
//                }
//                return true;
//            } else {
//                return mTarget.getScrollY() > 0;
//            }
//        } else {
//            return ViewCompat.canScrollVertically(mTarget, 1);
//        }
//    }


    public boolean canChildScrollDown() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                try {
                    if (absListView.getCount() > 0) {
                        if (absListView.getLastVisiblePosition() + 1 == absListView.getCount()) {
                            int lastIndex = absListView.getLastVisiblePosition() - absListView.getFirstVisiblePosition();
                            return absListView.getChildAt(lastIndex).getBottom() == absListView.getPaddingBottom();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return true;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, 1);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();
        ensureChildContainer();

        final int action = MotionEventCompat.getActionMasked(ev);

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        switch (mDirection) {
            case BOTTOM:
                if (!isEnabled() || mReturningToStart || (!mBothDirection && canChildScrollDown()) || mRefreshing) {
                    // Fail fast if we're not in a state where a swipe is possible
                    return false;
                }
                break;
            case TOP:
            default:
                if (!isEnabled() || mReturningToStart || (!mBothDirection && canChildScrollUp()) || mRefreshing) {
                    // Fail fast if we're not in a state where a swipe is possible
                    return false;
                }
                break;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCircleView.getTop(), true);
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                mInitialDownY = initialDownY;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }

                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                if (mBothDirection) {
                    if (y > mInitialDownY) {
                        setRawDirection(SwipyRefreshLayoutDirection.TOP);
                    } else if (y < mInitialDownY) {
                        setRawDirection(SwipyRefreshLayoutDirection.BOTTOM);
                    }
                    if ((mDirection == SwipyRefreshLayoutDirection.BOTTOM && canChildScrollDown())
                            || (mDirection == SwipyRefreshLayoutDirection.TOP && canChildScrollUp())) {
                        mInitialDownY = y;
                        return false;
                    }
                }
                float yDiff;
                switch (mDirection) {
                    case BOTTOM:
                        yDiff = mInitialDownY - y;
                        break;
                    case TOP:
                    default:
                        yDiff = y - mInitialDownY;
                        break;
                }
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    switch (mDirection) {
                        case BOTTOM:
                            mInitialMotionY = mInitialDownY - mTouchSlop;
                            break;
                        case TOP:
                        default:
                            mInitialMotionY = mInitialDownY + mTouchSlop;
                            break;
                    }
                    mIsBeingDragged = true;
                    mProgress.setAlpha(STARTING_PROGRESS_ALPHA);
                }
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // Nope.
    }

    private boolean isAnimationRunning(Animation animation) {
        return animation != null && animation.hasStarted() && !animation.hasEnded();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            final int action = MotionEventCompat.getActionMasked(ev);

            if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
                mReturningToStart = false;
            }

            switch (mDirection) {
                case BOTTOM:
                    if (!isEnabled() || mReturningToStart || canChildScrollDown() || mRefreshing) {
                        // Fail fast if we're not in a state where a swipe is possible
                        return false;
                    }
                    break;
                case TOP:
                default:
                    if (!isEnabled() || mReturningToStart || canChildScrollUp() || mRefreshing) {
                        // Fail fast if we're not in a state where a swipe is possible
                        return false;
                    }
                    break;
            }

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    mIsBeingDragged = false;
                    break;

                case MotionEvent.ACTION_MOVE: {
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    if (pointerIndex < 0) {
                        return false;
                    }

                    final float y = MotionEventCompat.getY(ev, pointerIndex);

                    float overscrollTop;
                    switch (mDirection) {
                        case BOTTOM:
                            overscrollTop = (mInitialMotionY - y) * DRAG_RATE;
                            break;
                        case TOP:
                        default:
                            overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                            break;
                    }
                    if (mIsBeingDragged) {
                        mProgress.showArrow(true);
                        float originalDragPercent = overscrollTop / mTotalDragDistance;
                        if (originalDragPercent < 0) {
                            return false;
                        }
                        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
                        float adjustedPercent = (float) Math.max(dragPercent - .4, 0) * 5 / 3;
                        float extraOS = Math.abs(overscrollTop) - mTotalDragDistance;
                        float slingshotDist = mUsingCustomStart ? mSpinnerFinalOffset
                                - mOriginalOffsetTop : mSpinnerFinalOffset;
                        float tensionSlingshotPercent = Math.max(0,
                                Math.min(extraOS, slingshotDist * 2) / slingshotDist);
                        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                                (tensionSlingshotPercent / 4), 2)) * 2f;
                        float extraMove = (slingshotDist) * tensionPercent * 2;

                        int targetY = mOriginalOffsetTop + (int) ((slingshotDist * dragPercent) + extraMove);
                        /*int targetY;
                        if (mDirection == SwipyRefreshLayoutDirection.TOP) {
                            targetY = mOriginalOffsetTop + (int) ((slingshotDist * dragPercent) + extraMove);
                        } else {
                            targetY = mOriginalOffsetTop - (int) ((slingshotDist * dragPercent) + extraMove);
                        }*/
                        // where 1.0f is a full circle
                        if (mCircleView.getVisibility() != View.VISIBLE) {
                            mCircleView.setVisibility(View.VISIBLE);
                        }
                        if (!mScale) {
                            ViewCompat.setScaleX(mCircleView, 1f);
                            ViewCompat.setScaleY(mCircleView, 1f);
                        }
                        if (overscrollTop < mTotalDragDistance) {
                            if (mScale) {
                                setAnimationProgress(overscrollTop / mTotalDragDistance);
                            }
                            if (mProgress.getAlpha() > STARTING_PROGRESS_ALPHA
                                    && !isAnimationRunning(mAlphaStartAnimation)) {
                                // Animate the alpha
                                startProgressAlphaStartAnimation();
                            }
                            float strokeStart = (float) (adjustedPercent * .8f);
                            mProgress.setStartEndTrim(0f, Math.min(MAX_PROGRESS_ANGLE, strokeStart));
                            mProgress.setArrowScale(Math.min(1f, adjustedPercent));
                        } else {
                            if (mProgress.getAlpha() < MAX_ALPHA
                                    && !isAnimationRunning(mAlphaMaxAnimation)) {
                                // Animate the alpha
                                startProgressAlphaMaxAnimation();
                            }
                        }
                        float rotation = (-0.25f + .4f * adjustedPercent + tensionPercent * 2) * .5f;
                        mProgress.setProgressRotation(rotation);
                        setTargetOffsetTopAndBottom(targetY - mCurrentTargetOffsetTop,
                                true /* requires update */);
                    }
                    break;
                }
                case MotionEventCompat.ACTION_POINTER_DOWN: {
                    final int index = MotionEventCompat.getActionIndex(ev);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                    break;
                }

                case MotionEventCompat.ACTION_POINTER_UP:
                    onSecondaryPointerUp(ev);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    if (mActivePointerId == INVALID_POINTER) {
                        if (action == MotionEvent.ACTION_UP) {
                        }
                        return false;
                    }
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);

                    float overscrollTop;
                    switch (mDirection) {
                        case BOTTOM:
                            overscrollTop = (mInitialMotionY - y) * DRAG_RATE;
                            break;
                        case TOP:
                        default:
                            overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                            break;
                    }
                    mIsBeingDragged = false;
                    if (overscrollTop > mTotalDragDistance) {
                        setRefreshing(true, true /* notify */);
                    } else {
                        // cancel refresh
                        mRefreshing = false;
                        mProgress.setStartEndTrim(0f, 0f);
                        AnimationListener listener = null;
                        if (!mScale) {
                            listener = new AnimationListener() {

                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    if (!mScale) {
                                        startScaleDownAnimation(null);
                                    }
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }

                            };
                        }
                        animateOffsetToStartPosition(mCurrentTargetOffsetTop, listener);
                        mProgress.showArrow(false);
                    }
                    mActivePointerId = INVALID_POINTER;
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "An exception occured during SwipyRefreshLayout onTouchEvent " + e.toString());
        }

        return true;
    }

    private void animateOffsetToCorrectPosition(int from, AnimationListener listener) {
        mFrom = from;
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mAnimateToCorrectPosition);
    }

    private void animateOffsetToStartPosition(int from, AnimationListener listener) {
        if (mScale) {
            // Scale the item back down
            startScaleDownReturnToStartAnimation(from, listener);
        } else {
            mFrom = from;
            mAnimateToStartPosition.reset();
            mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
            mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
            if (listener != null) {
                mCircleView.setAnimationListener(listener);
            }
            mCircleView.clearAnimation();
            mCircleView.startAnimation(mAnimateToStartPosition);
        }
    }

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = 0;
            int endTarget = 0;
            if (!mUsingCustomStart) {
                /*switch (mDirection) {
                    case BOTTOM:
                        endTarget = getMeasuredHeight() - (int) (mSpinnerFinalOffset);
                        break;
                    case TOP:
                    default:
                        endTarget = (int) (mSpinnerFinalOffset - Math.abs(mOriginalOffsetTop));
                        break;
                }*/
                endTarget = (int) (mSpinnerFinalOffset - Math.abs(mOriginalOffsetTop));
            } else {
                endTarget = (int) mSpinnerFinalOffset;
            }
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mCircleView.getTop();
            setTargetOffsetTopAndBottom(offset, false /* requires update */);
        }
    };

    private void moveToStart(float interpolatedTime) {
        int targetTop = 0;
        targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mCircleView.getTop();
        setTargetOffsetTopAndBottom(offset, false /* requires update */);
    }

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private void startScaleDownReturnToStartAnimation(int from,
                                                      AnimationListener listener) {
        mFrom = from;
        if (isAlphaUsedForScale()) {
            mStartingScale = mProgress.getAlpha();
        } else {
            mStartingScale = ViewCompat.getScaleX(mCircleView);
        }
        mScaleDownToStartAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                float targetScale = (mStartingScale + (-mStartingScale * interpolatedTime));
                setAnimationProgress(targetScale);
                moveToStart(interpolatedTime);
            }
        };
        mScaleDownToStartAnimation.setDuration(SCALE_DOWN_DURATION);
        if (listener != null) {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleDownToStartAnimation);
    }

    private void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
        mCircleView.bringToFront();
        mCircleView.offsetTopAndBottom(offset);

//        switch (mDirection) {
//            case BOTTOM:
//                mCurrentTargetOffsetTop = getMeasuredHeight() - mCircleView.getMeasuredHeight();
//                break;
//            case TOP:
//            default:
//                mCurrentTargetOffsetTop  = mCircleView.getTop();
//                break;
//        }
        mCurrentTargetOffsetTop = mCircleView.getTop();
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    /**
     * Classes that wish to be notified when the swipe gesture correctly
     * triggers a refresh should implement this interface.
     */
    public interface OnRefreshListener {
        public void onRefresh(SwipyRefreshLayoutDirection direction);
    }

    public SwipyRefreshLayoutDirection getDirection() {
        return mBothDirection ? SwipyRefreshLayoutDirection.BOTH : mDirection;
    }

    public void setDirection(SwipyRefreshLayoutDirection direction) {
        if (direction == SwipyRefreshLayoutDirection.BOTH) {
            mBothDirection = true;
        } else {
            mBothDirection = false;
            mDirection = direction;
        }

        /*switch (mDirection) {
            case BOTTOM:
                mCurrentTargetOffsetTop = mOriginalOffsetTop = getMeasuredHeight();
                break;
            case TOP:
            default:
                mCurrentTargetOffsetTop = mOriginalOffsetTop = -mCircleView.getMeasuredHeight();
                break;
        }*/
        mCurrentTargetOffsetTop = mOriginalOffsetTop = -mCircleView.getMeasuredHeight();
    }

    // only TOP or Bottom
    private void setRawDirection(SwipyRefreshLayoutDirection direction) {
        if (mDirection == direction) {
            return;
        }

        mDirection = direction;
        /*
        switch (mDirection) {
            case BOTTOM:
                mCurrentTargetOffsetTop = mOriginalOffsetTop = getMeasuredHeight();
                break;
            case TOP:
            default:
                mCurrentTargetOffsetTop = mOriginalOffsetTop = -mCircleView.getMeasuredHeight();
                break;
        }*/
        mCurrentTargetOffsetTop = mOriginalOffsetTop = -mCircleView.getMeasuredHeight();
    }

    public CircleImageView getCircleView() {
        return mCircleView;
    }
}