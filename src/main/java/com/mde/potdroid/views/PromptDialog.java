package com.mde.potdroid.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * The icon selection dialog
 */
public class PromptDialog extends DialogFragment {
    protected SuccessCallback mCallback;
    protected Integer mNumberInputs;
    protected Integer mCode;
    protected String[] mHints;
    protected Boolean mExpandable;
    protected String mTitle;

    public PromptDialog(String title, String hint, int code) {

        mNumberInputs = 1;
        mHints = new String[] {hint};
        mExpandable = false;
        mCode = code;
        mTitle = title;
    }

    public PromptDialog(String title, int number_inputs, String[] hints, boolean expandable, int code) {
        mNumberInputs = number_inputs;
        mHints = hints;
        mExpandable = expandable;
        mCode = code;
        mTitle = title;
    }

    public void setCallback(SuccessCallback callback) {
        mCallback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LinearLayout input_layout = new LinearLayout(getActivity());
        input_layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layout_params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        input_layout.setLayoutParams(layout_params);

        for(int i=0;i < mNumberInputs; ++i) {
            EditText editText = new EditText(getActivity());
            if(mHints.length > i)
                editText.setHint(mHints[i]);
            input_layout.addView(editText);
        }

        if(mExpandable) {
            Button expand_button = new Button(getActivity());
            expand_button.setText("Weiteres Item");
            input_layout.addView(expand_button);

            expand_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText editText = new EditText(getActivity());
                    input_layout.addView(editText, mNumberInputs++);
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setView(input_layout);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ArrayList<String> values = new ArrayList<String>();
                for(int c = 0; c <  mNumberInputs; c++) {
                    EditText e = (EditText) input_layout.getChildAt(c);
                    values.add(e.getText().toString());
                }
                mCallback.success(values, mCode);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        return builder.create();
    }

    public interface SuccessCallback {
        public void success(ArrayList<String> input, int code);
    }
}