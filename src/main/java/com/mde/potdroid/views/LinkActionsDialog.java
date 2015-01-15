package com.mde.potdroid.views;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;

/**
 * This DialogFragment shows a Menu for a Post with some actions
 */
public class LinkActionsDialog extends DialogFragment {

    public static final String ARG_IMAGE_URI = "link_uri";
    public static final String TAG = "linkmenu";

    public static LinkActionsDialog getInstance(Uri link_uri) {
        LinkActionsDialog f = new LinkActionsDialog();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URI, link_uri.toString());
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final CharSequence[] items = {getArguments().getString(ARG_IMAGE_URI)};

        return new MaterialDialog.Builder(getActivity())
                .items(items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        switch (i) {
                            case 0:
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(getArguments().getString(ARG_IMAGE_URI)));
                                startActivity(intent);
                                break;
                        }
                    }
                }).build();
    }

}