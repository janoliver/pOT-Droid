package com.mde.potdroid.views;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.BaseActivity;
import com.mde.potdroid.MediaActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.fragments.MediaFragment;
import com.mde.potdroid.fragments.TopicFragment;
import com.mde.potdroid.helpers.ImageHandler;

import java.io.File;

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
                .title(R.string.image_dialog_title)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        final String url = getArguments().getString(ARG_IMAGE_URI);
                        final Uri uri = Uri.parse(url);
                        switch (i) {
                            case 0:
                                Intent intent = new Intent(getActivity(), MediaActivity.class);
                                if (url.endsWith("gif"))
                                    intent.putExtra(MediaFragment.ARG_TYPE, "gif");
                                else
                                    intent.putExtra(MediaFragment.ARG_TYPE, "image");
                                intent.putExtra(MediaFragment.ARG_URI, url);
                                startActivityForResult(intent, 0);
                                break;
                            case 1:
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                shareIntent.setType("image/*");
                                startActivity(Intent.createChooser(shareIntent, "Share with"));
                                break;
                            case 2:
                                ((TopicFragment) getTargetFragment()).getBaseActivity().verifyStoragePermissions(getActivity(), new BaseActivity.ExternalPermissionCallback() {
                                    @Override
                                    public void granted() {
                                        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                        ImageHandler.downloadImage(getActivity(), dir, uri, new ImageHandler.ImageDownloadCallback() {
                                            @Override
                                            public void onSuccess(Uri uri, File f) {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ((TopicFragment) getTargetFragment()).showSuccess(R.string.msg_img_download_success);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(Uri uri, Exception e) {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        ((TopicFragment) getTargetFragment()).showError(R.string.msg_img_download_error);
                                                    }
                                                });
                                            }
                                        });
                                    }

                                    @Override
                                    public void denied() {
                                        ((TopicFragment) getTargetFragment()).showError(R.string.msg_permission_denied_error);
                                    }
                                });


                        }
                    }
                }).build();
    }

}