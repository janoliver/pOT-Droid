package com.mde.potdroid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.*;
import com.mde.potdroid.BaseActivity;
import com.mde.potdroid.ForumActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.SettingsActivity;
import com.mde.potdroid.helpers.Utils;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * The Base Fragment class that all Fragments should inherit. Provides some methods
 * for convenient access of objects and handles loading animations.
 */
public abstract class BaseFragment extends Fragment implements OnRefreshListener
{

    // this is the ID of the content loader
    protected static final int CONTENT_LOADER_ID = 0;

    // we need this to convert dip to px for the icons
    protected float mDensity;

    // the pulltorefresh instance
    protected PullToRefreshLayout mPullToRefreshLayout;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // the fragment instance should persist upon orientation changes.
        setRetainInstance(true);

        // instantiate and calculate the display metrics
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDensity = displayMetrics.density;

        // Now find the PullToRefreshLayout to setup
        mPullToRefreshLayout = (PullToRefreshLayout) getView().findViewById(R.id.ptr_layout);

        if(mPullToRefreshLayout != null) {
            // Now setup the PullToRefreshLayout
            ActionBarPullToRefresh.from(getActivity())
                    .allChildrenArePullable()
                    .listener(this)
                    .setup(mPullToRefreshLayout);

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
        if(mPullToRefreshLayout != null) {
            mPullToRefreshLayout.setRefreshComplete();
        }
        getBaseActivity().setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onRefreshStarted(View view) {

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
     * When the user is not logged in, we add preferences and home to
     * the Actionbar Menu
     * @param menu the menu
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
        getLoaderManager().restartLoader(CONTENT_LOADER_ID, null, l);
    }

    /**
     * Display an error
     */
    public void showError(String error) {
        Utils.toast(getBaseActivity(), error);
    }

    /**
     * Shows a "loading" message with a small loading animation
     */
    public void showLoadingAnimation() {
        if(mPullToRefreshLayout != null) {
            mPullToRefreshLayout.setRefreshing(true);
        }
        getBaseActivity().setProgressBarIndeterminateVisibility(true);
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
