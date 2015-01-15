package com.mde.potdroid.views;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.SettingsWrapper;

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

        SettingsWrapper s = new SettingsWrapper(getActivity());
        setStyle(DialogFragment.STYLE_NORMAL, s.getTheme());

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
        SettingsWrapper s = new SettingsWrapper(getActivity());
        ContextThemeWrapper context = new ContextThemeWrapper(getActivity(), s.getTheme());

        final LayoutInflater inflater = LayoutInflater.from(context);

        final LinearLayout input_layout = (LinearLayout)inflater.inflate(R.layout.template_holder, null);

        for(int i=0;i < mNumberInputs; ++i) {
            FrameLayout editTextHolder = (FrameLayout)inflater.inflate(R.layout.template_edittext, null);
            EditText editText = (EditText) editTextHolder.getChildAt(0);
            if(mHints.length > i)
                editText.setHint(mHints[i]);
            input_layout.addView(editTextHolder);
        }


        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .title(mTitle)
                .customView(input_layout, false)
                .positiveText("Ok")
                //.negativeText("Abbrechen")
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        ArrayList<String> values = new ArrayList<String>();
                        for (int c = 0; c < mNumberInputs; c++) {
                            EditText e = (EditText) ((FrameLayout) input_layout.getChildAt(c)).getChildAt(0);
                            values.add(e.getText().toString());
                        }
                        mCallback.success(values, mCode);
                        dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dismiss();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        FrameLayout editTextHolder = (FrameLayout) inflater.inflate(R.layout.template_edittext, null);
                        input_layout.addView(editTextHolder,mNumberInputs++);
                    }
                });

        if(mExpandable) {
            builder.neutralText("+ Item");
        }

        return builder.build();
    }

    public interface SuccessCallback {
        public void success(ArrayList<String> input, int code);
    }
}