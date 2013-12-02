package com.mde.potdroid3.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * This class provides some methods to show Notifications on top of the fragment.
 */
public abstract class BaseFragment extends Fragment {

    protected LayoutInflater mInflater;
    protected static final int CONTENT_LOADER_ID = 0;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        return inflater.inflate(getLayout(), container, false);
    }

    abstract protected int getLayout();

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

    public ActionBarActivity getSupportActivity() {
        return (ActionBarActivity)super.getActivity();
    }

    public ActionBar getActionbar() {
        return getSupportActivity().getSupportActionBar();
    }

}
