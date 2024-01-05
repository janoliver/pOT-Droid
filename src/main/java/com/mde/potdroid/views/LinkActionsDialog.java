package com.mde.potdroid.views;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.R;

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

        return new MaterialDialog.Builder(getActivity())
                .title(R.string.link_dialog_title)
                .content(getArguments().getString(ARG_IMAGE_URI))
                .positiveText(R.string.link_dialog_goto)
                .negativeText(R.string.link_dialog_close)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(getArguments().getString(ARG_IMAGE_URI)));
                        startActivity(intent);
                    }
                })
                .build();
    }

}