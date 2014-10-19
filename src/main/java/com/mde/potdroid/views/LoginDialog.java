package com.mde.potdroid.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.Network;
import com.mde.potdroid.helpers.SettingsWrapper;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * This class is a DialogPreference for the Login Action. It takes care of showing the login form
 * along with a loading animation (that is shown during the connection) and appropriately handles
 * the cancel button.
 */
public class LoginDialog extends DialogPreference {

    private SettingsWrapper mSettingsWrapper;
    // ui elements
    private Button mPositiveButton;
    private Button mNegativeButton;
    private EditText mUsername;
    private EditText mPassword;
    // true if a server request is made, false otherwise
    private Boolean mLoggingIn;

    public LoginDialog(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSettingsWrapper = new SettingsWrapper(getContext());
        mLoggingIn = false;

        setDialogLayoutResource(R.layout.dialog_login);
    }

    /**
     * Show/hide the loading animation and disable/enable the form elements.
     */
    public void setIsLoading(boolean is_loading) {
        if (is_loading && !mLoggingIn) {
            mLoggingIn = true;
            getDialog().findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
            mPositiveButton.setEnabled(false);
            mUsername.setEnabled(false);
            mPassword.setEnabled(false);
        } else if (mLoggingIn) {
            mLoggingIn = false;
            getDialog().findViewById(R.id.login_progress).setVisibility(View.INVISIBLE);
            mPositiveButton.setEnabled(true);
            mUsername.setEnabled(true);
            mPassword.setEnabled(true);
        }
    }

    @Override
    protected void showDialog(Bundle bundle) {
        super.showDialog(bundle);

        mPositiveButton = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        mNegativeButton = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEGATIVE);
        mUsername = (EditText) getDialog().findViewById(R.id.user_name);
        mPassword = (EditText) getDialog().findViewById(R.id.user_password);

        // when the "login" button is clicked, we create a new LoginTask and execute it with the
        // provided credentials
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String user_name = mUsername.getText().toString().trim();
                final String user_password = mPassword.getText().toString();

                Network n = new Network(getContext());
                n.login(user_name, user_password, new Network.LoginCallback() {
                    @Override
                    public void onSuccess() {
                        Style style = new Style.Builder()
                                .setBackgroundColorValue(Style.holoGreenLight)
                                .setHeightDimensionResId(R.dimen.notification_height_fallback)
                                .build();
                        Crouton.makeText((Activity) getContext(), R.string.msg_login_success, style).show();

                        mSettingsWrapper.setUsername(user_name);
                        getDialog().dismiss();
                    }

                    @Override
                    public void onFailure() {
                        Style style = new Style.Builder()
                                .setBackgroundColorValue(Style.holoRedLight)
                                .setHeightDimensionResId(R.dimen.notification_height_fallback)
                                .build();
                        Crouton.makeText((Activity) getContext(), R.string.msg_login_failure, style).show();
                    }
                });
            }
        });

        // when the cancel button is clicked, there are two options: If the login process is
        // running,
        // the button cancels it and re-enables the input elements and the login button. If the
        // login process is not running, it simply closes the logindialog.
        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }

    @Override
    public View onCreateDialogView() {
        View v = super.onCreateDialogView();

        // check, if we know a username. If so, fill in the username form
        if (mSettingsWrapper.hasUsername())
            ((EditText) v.findViewById(R.id.user_name)).setText(mSettingsWrapper.getUsername());

        return v;
    }
}