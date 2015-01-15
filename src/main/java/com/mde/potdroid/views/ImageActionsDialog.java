package com.mde.potdroid.views;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.R;

/**
 * This DialogFragment shows a Menu for a Post with some actions
 */
public class ImageActionsDialog extends DialogFragment {

    public static final String ARG_IMAGE_URI = "image_uri";
    public static final String TAG = "imagemenu";

    public static ImageActionsDialog getInstance(Uri image_uri) {
        ImageActionsDialog f = new ImageActionsDialog();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URI, image_uri.toString());
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new MaterialDialog.Builder(getActivity())
                .items(R.array.image_dialog)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        switch (i) {
                            case 0:
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse(getArguments().getString(ARG_IMAGE_URI)), "image/*");
                                startActivity(intent);
                                break;
                            case 1:

                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(getArguments().getString(ARG_IMAGE_URI)));
                                shareIntent.setType("image/*");
                                startActivity(Intent.createChooser(shareIntent, "Share"));
                                break;
                        }
                    }
                }).build();
    }

}