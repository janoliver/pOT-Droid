package com.mde.potdroid.fragments;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.mde.potdroid.BaseActivity;
import com.mde.potdroid.ForumActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.SettingsActivity;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.views.IconDrawable;
import com.mde.potdroid.views.SwipeRefreshLayout;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * The Base Fragment class that all Fragments should inherit. Provides some methods
 * for convenient access of objects and handles loading animations.
 */
public abstract class BaseFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    // this is the ID of the content loader
    protected static final int CONTENT_LOADER_ID = 0;

    // the pulltorefresh instance
    protected SwipeRefreshLayout mPullToRefreshLayout;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // the fragment instance should persist upon orientation changes.
        setRetainInstance(true);

        // Now find the PullToRefreshLayout to setup
        mPullToRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.ptr_layout);

        if(mPullToRefreshLayout == null)
            mPullToRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.ptr_holder);

        if (mPullToRefreshLayout != null) {
            mPullToRefreshLayout.setOnRefreshListener(this);
            mPullToRefreshLayout.setColorSchemeResources(
                    R.color.white,
                    R.color.bbstyle_lighterblue,
                    R.color.white_semi,
                    R.color.bbstyle_darkblue);
        }
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
            mPullToRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {

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
        getBaseActivity().getTheme().resolveAttribute(R.attr.actionBarSize, typedValue, true);
        int[] textSizeAttr = new int[] { R.attr.actionBarSize };
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

        MenuItem settings = menu.findItem(R.id.preferences);
        settings.setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_cogs));

        MenuItem home = menu.findItem(R.id.forumact);
        home.setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_home));

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
        getLoaderManager().restartLoader(CONTENT_LOADER_ID, null, l);
    }

    protected void showGenericNotification(String message, int s) {
        Style style = new Style.Builder()
                .setBackgroundColorValue(s)
                .setHeight(getActionbarHeight())
                .build();
        Crouton.makeText(getBaseActivity(), message, style, getNotificationParent()).show();
    }

    protected void showGenericNotification(int message, int s) {
        Style style = new Style.Builder()
                .setBackgroundColorValue(s)
                .setHeight(getActionbarHeight())
                .build();
        Crouton.makeText(getBaseActivity(), message,  style, getNotificationParent()).show();
    }

    /**
     * Display an error
     */
    public void showError(String error) {
        showGenericNotification(error, Style.holoRedLight);
    }

    /**
     * Display a success message
     */
    public void showSuccess(String message) {
        showGenericNotification(message, Style.holoGreenLight);
    }

    /**
     * Display a success message
     */
    public void showInfo(String message) {
        showGenericNotification(message, Style.holoBlueLight);
    }

    /**
     * Display an error
     */
    public void showError(int error) {
        showGenericNotification(error, Style.holoRedLight);
    }

    /**
     * Display a success message
     */
    public void showSuccess(int message) {
        showGenericNotification(message, Style.holoGreenLight);
    }

    /**
     * Display a success message
     */
    public void showInfo(int message) {
        showGenericNotification(message, Style.holoBlueLight);
    }

    /**
     * Shows a "loading" message with a small loading animation
     */
    public void showLoadingAnimation() {
        if (mPullToRefreshLayout != null) {
            mPullToRefreshLayout.setRefreshing(true);
        }
    }

    /**
     * Start the content loader
     */
    public void startLoader(LoaderManager.LoaderCallbacks l) {
        startLoader(l, null);
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
