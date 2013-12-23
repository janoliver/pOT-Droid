package com.mde.potdroid.fragments;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.mde.potdroid.R;

/**
 * This Fragment extends BaseFragment and provides some more methods and an interface
 * for those Fragments who have pagination functionality.
 */
abstract public class PaginateFragment extends BaseFragment
{

    public abstract void goToFirstPage();

    public abstract void goToLastPage();

    /**
     * All the functions below must be implemented by the child class.
     */

    public abstract void goToNextPage();

    public abstract void goToPrevPage();

    public abstract boolean isFirstPage();

    public abstract boolean isLastPage();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem paginate_layout = menu.findItem(R.id.layout_item);
        LinearLayout paginateWidget = (LinearLayout) MenuItemCompat.getActionView(paginate_layout);

        ImageButton refreshButton = (ImageButton) paginateWidget.findViewById(R.id.button_refresh);
        ImageButton fwdButton = (ImageButton) paginateWidget.findViewById(R.id.button_fwd);
        ImageButton ffwdButton = (ImageButton) paginateWidget.findViewById(R.id.button_ffwd);
        ImageButton rwdButton = (ImageButton) paginateWidget.findViewById(R.id.button_rwd);
        ImageButton frwdButton = (ImageButton) paginateWidget.findViewById(R.id.button_frwd);

        refreshButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                refreshPage();
            }
        });

        fwdButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                goToNextPage();
            }
        });

        ffwdButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                goToLastPage();
            }
        });

        rwdButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                goToPrevPage();
            }
        });

        frwdButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                goToFirstPage();
            }
        });

        // only show the paginate buttons if there are before or after the current
        if (isLastPage()) {
            fwdButton.setVisibility(View.INVISIBLE);
            ffwdButton.setVisibility(View.INVISIBLE);
        }

        if (isFirstPage()) {
            rwdButton.setVisibility(View.INVISIBLE);
            frwdButton.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionmenu_paginate, menu);
    }

    public abstract void refreshPage();
}
