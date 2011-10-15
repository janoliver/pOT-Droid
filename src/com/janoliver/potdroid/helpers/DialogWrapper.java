/*
 * Copyright (C) 2011 Jan Oliver Oelerich <janoliver@oelerich.org>
 *
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this software, and changing it is allowed as long as the 
 * name is changed.
 *
 *           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *  0. You just DO WHAT THE FUCK YOU WANT TO. 
 */

package com.janoliver.potdroid.helpers;

import android.view.View;
import android.widget.EditText;

import com.janoliver.potdroid.R;

/**
 * This class is needed for the reply and edit dialogs. It allows us to change
 * the values of the fields even though we use the alert dialog builder.
 */
public class DialogWrapper {
    protected EditText mTextbox = null;
    protected EditText mTexttitle = null;
    protected View mBase = null;

    public DialogWrapper(View base) {
        mBase = base;
    }

    public String getText() {
        return (getValueField().getText().toString());
    }

    public String getTitle() {
        return (getTitleField().getText().toString());
    }

    private EditText getValueField() {
        if (mTextbox == null) {
            mTextbox = (EditText) mBase.findViewById(R.id.replybox);
        }

        return (mTextbox);
    }

    private EditText getTitleField() {
        if (mTexttitle == null) {
            mTexttitle = (EditText) mBase.findViewById(R.id.replytitle);
        }

        return (mTexttitle);
    }
}