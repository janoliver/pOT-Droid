package com.mde.potdroid.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import com.mde.potdroid.R;
import com.mde.potdroid.fragments.TopicFragment;

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
        final TopicFragment fragment = (TopicFragment) getTargetFragment();

        // get the menu items
        final String[] image_menu = getResources().getStringArray(R.array.image_dialog);

        LayoutInflater inflater = fragment.getBaseActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getBaseActivity());
        builder.setItems(R.array.image_dialog, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
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
        });
        return builder.create();
    }

}