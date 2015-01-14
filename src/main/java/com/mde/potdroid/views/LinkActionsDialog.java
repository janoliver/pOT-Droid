package com.mde.potdroid.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.mde.potdroid.fragments.TopicFragment;

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
        final TopicFragment fragment = (TopicFragment) getTargetFragment();

        // get the menu items
        final CharSequence[] items = {getArguments().getString(ARG_IMAGE_URI)};
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getBaseActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(getArguments().getString(ARG_IMAGE_URI)));
                        startActivity(i);
                        break;
                }
            }
        });
        return builder.create();
    }

}