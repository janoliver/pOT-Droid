package com.mde.potdroid.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ViewGroup root = (ViewGroup) mPaginateLayout.getParent();
            ViewGroup contentView = (ViewGroup) root.findViewById(R.id.content);
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) contentView.getLayoutParams();
            p.addRule(RelativeLayout.ABOVE, R.id.paginate_view);
            contentView.setLayoutParams(p);
        }
    }

    public void refreshPaginateLayout() {
        mPaginateLayout = getBaseActivity().refreshAndGetPaginateLayout();

        //IconButton refreshButton = (IconButton) paginateWidget.findViewById(R.id.button_refresh);
        IconButton fwdButton = (IconButton) mPaginateLayout.findViewById(R.id.button_fwd);
        IconButton ffwdButton = (IconButton) mPaginateLayout.findViewById(R.id.button_ffwd);
        IconButton rwdButton = (IconButton) mPaginateLayout.findViewById(R.id.button_rwd);
        IconButton frwdButton = (IconButton) mPaginateLayout.findViewById(R.id.button_frwd);

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
        int index = root.indexOfChild(root.findViewById(R.id.paginate_view));
        root.removeViewAt(index);
        View newPaginateView = inflater.inflate(R.layout.view_paginate, root, false);
        root.addView(newPaginateView, index);

        // Apply the new layout ordering to the content view
        ViewGroup contentView = (ViewGroup)root.findViewById(R.id.content);
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)contentView.getLayoutParams();
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            p.addRule(RelativeLayout.ABOVE, R.id.paginate_view);
        } else {
            p.addRule(RelativeLayout.ABOVE, 0);
        }
        contentView.setLayoutParams(p);

        refreshPaginateLayout();
    }
}
