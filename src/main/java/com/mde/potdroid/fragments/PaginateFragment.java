package com.mde.potdroid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mde.potdroid.R;

/**
 * This Fragment extends BaseFragment and provides some more methods and an interface
 * for those Fragments who have pagination functionality.
 */
abstract public class PaginateFragment extends BaseFragment
{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionbar().setCustomView(R.layout.view_paginate);
        getActionbar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM |
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionmenu_paginate, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // only show the paginate buttons if there are before or after the current
        if (!isLastPage()) {
            menu.findItem(R.id.nav_next).setIcon(R.drawable.dark_navigation_fwd).setEnabled(true);
            menu.findItem(R.id.nav_lastpage).setIcon(R.drawable.dark_navigation_ffwd).setEnabled
                    (true);
        }

        if (!isFirstPage()) {
            menu.findItem(R.id.nav_firstpage).setIcon(R.drawable.dark_navigation_frwd).setEnabled
                    (true);
            menu.findItem(R.id.nav_previous).setIcon(R.drawable.dark_navigation_rwd).setEnabled
                    (true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.nav_next:
                goToNextPage();
                return true;
            case R.id.nav_previous:
                goToPrevPage();
                return true;
            case R.id.nav_firstpage:
                goToFirstPage();
                return true;
            case R.id.nav_lastpage:
                goToLastPage();
                return true;
            case R.id.nav_refresh:
                refreshPage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * All the functions below must be implemented by the child class.
     */

    public abstract void goToNextPage();

    public abstract void goToPrevPage();

    public abstract void goToLastPage();

    public abstract void goToFirstPage();

    public abstract boolean isLastPage();

    public abstract boolean isFirstPage();

    public abstract void refreshPage();
}
