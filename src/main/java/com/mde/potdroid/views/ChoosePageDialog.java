package com.mde.potdroid.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        CharSequence[] items = new CharSequence[getArguments().getInt("pages")];
        for(int i=0; i < items.length; ++i) {
            items[i] = "Seite "+(i+1);
        }

        builder.setTitle(R.string.action_topage)
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((TopicFragment)getTargetFragment()).goToPage(which + 1);
                    }
                });
        return builder.create();
    }
}