package com.mde.potdroid.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.widget.Toast;

/**
 * The Base Fragment class that all Fragments should inherit. Provides some methods
 * for convenient access of objects and handles loading animations.
 */
public abstract class BaseFragment extends Fragment
{
    // this is the ID of the content loader
    protected static final int CONTENT_LOADER_ID = 0;

    /**
     * When detached, stop all loaders.
     */
    @Override
    public void onDetach() {
        super.onDetach();

        stopLoader();
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
        getLoaderManager().initLoader(CONTENT_LOADER_ID, args, l);
    }

    /**
     * Start the content loader providing arguments
     */
    public void stopLoader() {
        getLoaderManager().destroyLoader(CONTENT_LOADER_ID);
    }

    /**
     * Restart the content loader
     */
    public void restartLoader(LoaderManager.LoaderCallbacks l) {
        getLoaderManager().restartLoader(CONTENT_LOADER_ID, null, l);
    }

    /**
     * Shows a "loading" message with a small loading animation
     */
    public void showLoadingAnimation() {
        getSupportActivity().setProgressBarIndeterminateVisibility(true);
    }

    /**
     * Hides the loading message
     */
    public void hideLoadingAnimation() {
        getSupportActivity().setProgressBarIndeterminateVisibility(false);
    }

    /**
     * Display an error
     */
    public void showError(String error) {
        Toast.makeText(getSupportActivity(), error, Toast.LENGTH_LONG).show();
    }

    /**
     * Get the activity of the current fragment
     * @return SupportActivity
     */
    public ActionBarActivity getSupportActivity() {
        return (ActionBarActivity) super.getActivity();
    }

    /**
     * Get the actionbar reference
     * @return ActionBar
     */
    public ActionBar getActionbar() {
        return getSupportActivity().getSupportActionBar();
    }

    /**
     * Get a layout inflater instance.
     * @return LayoutInflater
     */
    public LayoutInflater getInflater() {
        return getSupportActivity().getLayoutInflater();
    }

}
