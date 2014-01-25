package com.mde.potdroid.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import com.mde.potdroid.AboutActivity;
import com.mde.potdroid.R;

/**
 * Shows a dialog with update information. The user can decide to be redirected to the About page
 */
public class UpdateInfoDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(Html.fromHtml(getString(R.string.update_info)))
                .setPositiveButton(R.string.update_info_about, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(), AboutActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.update_info_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
