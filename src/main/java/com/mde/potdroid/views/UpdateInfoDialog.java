package com.mde.potdroid.views;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.text.Spanned;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.AboutActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.Utils;

/**
 * Shows a dialog with update information. The user can decide to be redirected to the About page
 */
public class UpdateInfoDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());

        Spanned msg = Utils.fromHtml(getString(R.string.update_info));
        String version;
        try {
            version = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "";
        }

        builder.content(msg)
                .title("pOT-Droid " + version)
                .positiveText(R.string.update_info_about)
                .negativeText(R.string.update_info_cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent = new Intent(getActivity(), AboutActivity.class);
                        startActivity(intent);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.build();
    }

}
