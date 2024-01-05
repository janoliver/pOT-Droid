package com.mde.potdroid.fragments;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.appcompat.app.ActionBar;
import android.util.TypedValue;
import android.view.*;
import android.widget.TextView;
import com.mde.potdroid.BaseActivity;
import com.mde.potdroid.ForumActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.SettingsActivity;
import com.mde.potdroid.helpers.DatabaseWrapper;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.helpers.ptr.SwipyRefreshLayout;
import com.mde.potdroid.helpers.ptr.SwipyRefreshLayoutDirection;

/**
 * The Base Fragment class that all Fragments should inherit. Provides some methods
 * for convenient access of objects and handles loading animations.
 */
public abstract class BaseFragment extends Fragment implements SwipyRefreshLayout.OnRefreshListener {

    // this is the ID of the content loader
    protected static final int CONTENT_LOADER_ID = 0;

    // the pulltorefresh instance
    protected SwipyRefreshLayout mPullToRefreshLayout;
    protected SettingsWrapper mSettings;
    protected DatabaseWrapper mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = new SettingsWrapper(getBaseActivity());
        mDatabase = new DatabaseWrapper(getBaseActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // the fragment instance should persist upon orientation changes.
        setRetainInstance(true);

        // Now find the PullToRefreshLayout to setup
        mPullToRefreshLayout = (SwipyRefreshLayout) getView().findViewById(R.id.ptr_holder);

        if (mPullToRefreshLayout != null) {
            mPullToRefreshLayout.setOnRefreshListener(this);
            mPullToRefreshLayout.setColorSchemeColors(Utils.getColorByAttr(
                    getActivity(), R.attr.bbProgressForeground));
            mPullToRefreshLayout.setProgressBackgroundColor(Utils.getDrawableResourceIdByAttr(
                    getActivity(), R.attr.bbProgressBackground));
        }

        if (!mSettings.isSwipeToRefresh())
            mPullToRefreshLayout.setEnabled(false);
    }

    /**
     * Get the actionbar reference
     *
     * @return ActionBar
     */
    public ActionBar getActionbar() {
        return getBaseActivity().getSupportActionBar();
    }

    /**
     * Get a layout inflater instance.
     *
     * @return LayoutInflater
     */
    public LayoutInflater getInflater() {
        return getBaseActivity().getLayoutInflater();
    }

    /**
     * Get the activity of the current fragment
     *
     * @return SupportActivity
     */
    public BaseActivity getBaseActivity() {
        return (BaseActivity) super.getActivity();
    }

    /**
     * Hides the loading message
     */
    public void hideLoadingAnimation() {
        if (mPullToRefreshLayout != null) {
            mPullToRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mPullToRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {

    }

    public int getNotificationParent() {
        return R.id.content;
    }

    /**
     * When detached, stop all loaders.
     */
    @Override
    public void onDetach() {
        super.onDetach();

        stopLoader();
    }

    /**
     * Get Actionbar height attribute
     */
    public int getActionbarHeight() {
        TypedValue typedValue = new TypedValue();
        getBaseActivity().getTheme().resolveAttribute(com.google.android.material.R.attr.actionBarSize, typedValue, true);
        int[] textSizeAttr = new int[]{com.google.android.material.R.attr.actionBarSize};
        int indexOfAttr = 0;
        TypedArray a = getBaseActivity().obtainStyledAttributes(typedValue.data, textSizeAttr);
        int abSize = a.getDimensionPixelSize(indexOfAttr, -1);
        a.recycle();
        return abSize;
    }

    /**
     * When the user is not logged in, we add preferences and home to
     * the Actionbar Menu
     *
     * @param menu     the menu
     * @param inflater the layout inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_base, menu);

        if (!Utils.isLoggedIn()) {
            menu.setGroupVisible(R.id.loggedout, true);
        } else {
            menu.setGroupVisible(R.id.loggedout, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.preferences:
                intent = new Intent(getBaseActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.forumact:
                intent = new Intent(getBaseActivity(), ForumActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Restart the content loader
     */
    public void restartLoader(LoaderManager.LoaderCallbacks l) {
        restartLoader(l, false);
    }

    /**
     * Restart the content loader
     */
    public void restartLoader(LoaderManager.LoaderCallbacks l, boolean cache) {
        Bundle args = new Bundle();
        args.putBoolean("cache", cache);
        getLoaderManager().restartLoader(CONTENT_LOADER_ID, args, l);
    }

    protected void showGenericNotification(String message, int c) {
        if (getBaseActivity() != null) {
            Snackbar snackbar = Snackbar
                    .make(getBaseActivity().findViewById(R.id.app_root), message, Snackbar.LENGTH_LONG);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(c);
            TextView tv = (TextView) snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);
            snackbar.show();
        }
    }

    protected void showGenericNotification(int message, int c) {
        if (getBaseActivity() != null) {
            Snackbar snackbar = Snackbar
                    .make(getBaseActivity().findViewById(R.id.app_root), message, Snackbar.LENGTH_LONG);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(c);
            TextView tv = (TextView) snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);
            snackbar.show();
        }
    }

    /**
     * Display an error
     */
    public void showError(String error) {
        showGenericNotification(error, Utils.getColorByAttr(getBaseActivity(), R.attr.bbErrorColor));
    }

    /**
     * Display a success message
     */
    public void showSuccess(String message) {
        showGenericNotification(message, Utils.getColorByAttr(getBaseActivity(), R.attr.bbSuccessColor));
    }

    /**
     * Display a success message
     */
    public void showInfo(String message) {
        showGenericNotification(message, Utils.getColorByAttr(getBaseActivity(), R.attr.bbInfoColor));
    }

    /**
     * Display an error
     */
    public void showError(int error) {
        showGenericNotification(error, Utils.getColorByAttr(getBaseActivity(), R.attr.bbErrorColor));
    }

    /**
     * Display a success message
     */
    public void showSuccess(int message) {
        showGenericNotification(message, Utils.getColorByAttr(getBaseActivity(), R.attr.bbSuccessColor));
    }

    /**
     * Display a success message
     */
    public void showInfo(int message) {
        showGenericNotification(message, Utils.getColorByAttr(getBaseActivity(), R.attr.bbInfoColor));
    }

    /**
     * Shows a "loading" message with a small loading animation
     */
    public void showLoadingAnimation() {
        if (mPullToRefreshLayout != null) {
            mPullToRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mPullToRefreshLayout.setRefreshing(true);
                }
            });
        }
    }

    /**
     * Start the content loader
     */
    public void startLoader(LoaderManager.LoaderCallbacks l) {
        startLoader(l, new Bundle());
    }

    /**
     * Start the content loader providing arguments
     */
    public void startLoader(LoaderManager.LoaderCallbacks l, Bundle args) {
        getLoaderManager().restartLoader(CONTENT_LOADER_ID, args, l);
    }

    /**
     * Start the content loader providing arguments
     */
    public void stopLoader() {
        getLoaderManager().destroyLoader(CONTENT_LOADER_ID);
    }

}
