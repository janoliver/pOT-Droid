package com.mde.potdroid3.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.mde.potdroid3.helpers.Network;

/**
 * This class provides some methods to show Notifications on top of the fragment.
 */
public abstract class BaseFragment extends Fragment {

    protected LayoutInflater mInflater;
    protected Network mNetwork = null;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNetwork = new Network(getActivity());
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
        getLoaderManager().restartLoader(0, args, l).forceLoad();
    }

    /**
     * Start the content loader providing arguments
     */
    public void stopLoader() {
        getLoaderManager().destroyLoader(0);
    }

    /**
     * Restart the content loader
     */
    public void restartLoader(LoaderManager.LoaderCallbacks l) {
        getLoaderManager().restartLoader(0, null, l).forceLoad();
    }

    /**
     * Shows a "loading" message with a small loading animation
     */
    public void showLoadingAnimation() {
        getActivity().setProgressBarIndeterminateVisibility(true);
    }

    /**
     * Hides the loading message
     */
    public void hideLoadingAnimation() {
        getActivity().setProgressBarIndeterminateVisibility(false);
    }

    /**
     * Display an error
     */
    public void showError(String error) {
        Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
    }

}
