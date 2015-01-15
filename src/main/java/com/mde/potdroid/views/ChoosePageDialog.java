package com.mde.potdroid.views;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.R;
import com.mde.potdroid.fragments.TopicFragment;

public class ChoosePageDialog extends DialogFragment {
    public static ChoosePageDialog getInstance(int pages) {
        ChoosePageDialog f = new ChoosePageDialog();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("pages", pages);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence[] items = new CharSequence[getArguments().getInt("pages")];
        for(int i=0; i < items.length; ++i) {
            items[i] = "Seite "+(i+1);
        }

        return new MaterialDialog.Builder(getActivity())
                .title(R.string.action_topage)
                .items(items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        ((TopicFragment)getTargetFragment()).goToPage(i + 1);
                    }
                }).build();
    }
}