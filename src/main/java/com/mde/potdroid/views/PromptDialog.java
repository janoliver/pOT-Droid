package com.mde.potdroid.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.mde.potdroid.R;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNumberInputs = getArguments().getInt("num_items",1);
        mHints = getArguments().getStringArray("hints");
        mExpandable = getArguments().getBoolean("expandable", false);
        mCode = getArguments().getInt("code", 0);
        mTitle = getArguments().getString("title");
    }

    public static PromptDialog newInstance(String title, int number_inputs, String[] hints,
                                                  boolean expandable, int code) {
        PromptDialog f = new PromptDialog();

        Bundle args = new Bundle();
        args.putBoolean("expandable", expandable);
        args.putInt("num_items", number_inputs);
        args.putString("title", title);
        args.putInt("code", code);
        args.putStringArray("hints", hints);
        f.setArguments(args);

        return f;
    }

    public static PromptDialog newInstance(String title, String hint, int code) {
        return newInstance(title, 1, new String[] {hint}, false, code);
    }


    public void setCallback(SuccessCallback callback) {
        mCallback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        final LinearLayout input_layout = new LinearLayout(getActivity());
        input_layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layout_params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        input_layout.setLayoutParams(layout_params);

        for(int i=0;i < mNumberInputs; ++i) {
            FrameLayout editTextHolder = (FrameLayout)inflater.inflate(R.layout.template_edittext, null);
            EditText editText = (EditText) editTextHolder.getChildAt(0);
            if(mHints.length > i)
                editText.setHint(mHints[i]);
            input_layout.addView(editTextHolder);
        }

        if(mExpandable) {
            FrameLayout buttonHolder = (FrameLayout)inflater.inflate(R.layout.template_button, null);
            Button expand_button = (Button) buttonHolder.getChildAt(0);
            expand_button.setText("Weiteres Item");
            input_layout.addView(buttonHolder);

            expand_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FrameLayout editTextHolder = (FrameLayout) inflater.inflate(R.layout.template_edittext, null);
                    input_layout.addView(editTextHolder,mNumberInputs++);
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setView(input_layout);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ArrayList<String> values = new ArrayList<String>();
                for(int c = 0; c <  mNumberInputs; c++) {
                    EditText e = (EditText) ((FrameLayout)input_layout.getChildAt(c)).getChildAt(0);
                    values.add(e.getText().toString());
                }
                mCallback.success(values, mCode);
                dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }

    public interface SuccessCallback {
        public void success(ArrayList<String> input, int code);
    }
}