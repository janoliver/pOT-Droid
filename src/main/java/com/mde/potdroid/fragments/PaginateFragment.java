package com.mde.potdroid.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import com.mde.potdroid.R;
import com.mde.potdroid.views.IconButton;

/**
 * This Fragment extends BaseFragment and provides some more methods and an interface
 * for those Fragments who have pagination functionality.
 */
abstract public class PaginateFragment extends BaseFragment {

    private LinearLayout mPaginateLayout;

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
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        refreshPaginateLayout();
    }

    public void refreshPaginateLayout() {
        mPaginateLayout = getBaseActivity().getPaginateLayout();

        //IconButton refreshButton = (IconButton) paginateWidget.findViewById(R.id.button_refresh);
        IconButton fwdButton = (IconButton) mPaginateLayout.findViewById(R.id.button_fwd);
        IconButton ffwdButton = (IconButton) mPaginateLayout.findViewById(R.id.button_ffwd);
        IconButton rwdButton = (IconButton) mPaginateLayout.findViewById(R.id.button_rwd);
        IconButton frwdButton = (IconButton) mPaginateLayout.findViewById(R.id.button_frwd);

        /*refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPage();
            }
        });*/

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

        if(anyVisible)
            mPaginateLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        ViewGroup root = (ViewGroup) mPaginateLayout.getParent();
        View child = root.findViewById(R.id.paginate_view);
        int index = root.indexOfChild(child);
        root.removeView(child);
        View newPaginateView = inflater.inflate(R.layout.view_paginate, root, false);
        root.addView(newPaginateView, index);
        refreshPaginateLayout();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //inflater.inflate(R.menu.actionmenu_paginate, menu);
    }

    public abstract void refreshPage();
}
