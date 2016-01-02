package com.mde.potdroid.views;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.R;
import com.mde.potdroid.fragments.TopicFragment;
import com.mde.potdroid.helpers.Utils;

import java.io.*;

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
                        final Uri uri = Uri.parse(getArguments().getString(ARG_IMAGE_URI));
                        switch (i) {
                            case 0:
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(uri, "image/*");
                                startActivity(intent);
                                break;
                            case 1:
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                shareIntent.setType("image/*");
                                startActivity(Intent.createChooser(shareIntent, "Share with"));
                                break;
                            case 2:
                                InputStream inStream = null;
                                try {
                                    inStream = getActivity().getContentResolver().openInputStream(uri);
                                    File sdCard = Environment.getExternalStorageDirectory();
                                    File dir = new File (sdCard.getAbsolutePath() + "/Download");
                                    dir.mkdirs();
                                    File file = new File(dir, uri.getLastPathSegment());

                                    FileOutputStream f = new FileOutputStream(file);
                                    byte[] buffer = new byte[1024];
                                    int len1;
                                    while ((len1 = inStream.read(buffer)) > 0) {
                                        f.write(buffer, 0, len1);
                                    }
                                    f.close();

                                    ((TopicFragment)getTargetFragment()).showSuccess(String.format(
                                            "Gespeichert in /sdcard/Download/%s", uri.getLastPathSegment()
                                    ));

                                } catch (FileNotFoundException e) {
                                    Utils.printException(e);

                                    ((TopicFragment)getTargetFragment()).showError(
                                            "Datei nicht gefunden."
                                    );
                                } catch (IOException e) {
                                    Utils.printException(e);
                                    ((TopicFragment)getTargetFragment()).showError(
                                            "Unbekannter Fehler."
                                    );
                                } finally {
                                    if (inStream != null) {
                                        try {
                                            inStream.close();
                                        } catch (IOException e) {
                                            Utils.printException(e);
                                            ((TopicFragment)getTargetFragment()).showError(
                                                    "Unbekannter Fehler."
                                            );
                                        }
                                    }
                                }
                        }
                    }
                }).build();
    }

}