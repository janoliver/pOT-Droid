package com.mde.potdroid3.fragments;

import com.mde.potdroid3.R;

public class FormFragment extends BaseFragment {

    public static FormFragment newInstance() {
        return new FormFragment();
    }

    protected int getLayout() {
        return R.layout.layout_sidebar_form;
    }

}
