package com.mde.potdroid.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.Spanned;
import com.mde.potdroid.AboutActivity;
import com.mde.potdroid.R;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Shows a dialog with update information. The user can decide to be redirected to the About page
 */
public class UpdateInfoDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        GregorianCalendar now = new GregorianCalendar();
        now.setTime(new Date());

        // the month goes from 0 to 11
        GregorianCalendar xmas2014 = new GregorianCalendar(2014, 11, 24, 23, 59);

        Spanned msg;
        if (now.getTimeInMillis() <= xmas2014.getTimeInMillis())
            msg = Html.fromHtml(getString(R.string.update_info_xmas));
        else
            msg = Html.fromHtml(getString(R.string.update_info));


        builder.setMessage(msg)
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
