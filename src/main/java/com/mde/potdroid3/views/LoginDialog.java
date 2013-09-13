package com.mde.potdroid3.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.mde.potdroid3.R;
import com.mde.potdroid3.helpers.Network;
import com.mde.potdroid3.helpers.SettingsWrapper;

/**
 * This class is a DialogPreference for the Login Action. It takes care of showing the login form
 * along with a loading animation (that is shown during the connection) and appropriately handles
 * the cancel button.
 */
public class LoginDialog extends DialogPreference {

    private Context mContext;
    private SettingsWrapper mSettingsWrapper;

    // ui elements
    private Button mPositiveButton;
    private Button mNegativeButton;
    private EditText mUsername;
    private EditText mPassword;

    // true if a server request is made, false otherwise
    private Boolean mLoggingIn;

    // container to hold the asynctask
    private LoginTask mLoginTask;

    public LoginDialog(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mSettingsWrapper = new SettingsWrapper(mContext);
        mLoggingIn = false;

        setDialogLayoutResource(R.layout.dialog_login);
    }

    @Override
    public View onCreateDialogView() {
        View v = super.onCreateDialogView();

        // check, if we know a username. If so, fill in the username form
        if(mSettingsWrapper.hasUsername())
            ((EditText)v.findViewById(R.id.user_name)).setText(mSettingsWrapper.getUsername());

        return v;
    }

    @Override
    protected void showDialog(Bundle bundle) {
        super.showDialog(bundle);

        mPositiveButton = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        mNegativeButton = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEGATIVE);
        mUsername = (EditText)getDialog().findViewById(R.id.user_name);
        mPassword = (EditText)getDialog().findViewById(R.id.user_password);

        // when the "login" button is clicked, we create a new LoginTask and execute it with the
        // provided credentials
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user_name = mUsername.getText().toString().trim();
                String user_password = mPassword.getText().toString();
                mLoginTask = new LoginTask(user_name);
                mLoginTask.execute(user_password);
            }
        });

        // when the cancel button is clicked, there are two options: If the login process is running,
        // the button cancels it and re-enables the input elements and the login button. If the
        // login process is not running, it simply closes the logindialog.
        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLoggingIn) {
                    mLoginTask.cancel(true);
                } else {
                    getDialog().dismiss();
                }
            }
        });
    }

    /**
     * Show/hide the loading animation and disable/enable the form elements.
     */
    public void setIsLoading(boolean is_loading) {
        if(is_loading && !mLoggingIn) {
            mLoggingIn = true;
            getDialog().findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
            mPositiveButton.setEnabled(false);
            mUsername.setEnabled(false);
            mPassword.setEnabled(false);
        } else if(mLoggingIn) {
            mLoggingIn = false;
            getDialog().findViewById(R.id.login_progress).setVisibility(View.INVISIBLE);
            mPositiveButton.setEnabled(true);
            mUsername.setEnabled(true);
            mPassword.setEnabled(true);
        }
    }

    /**
     * The AsyncTask we use for the login attempt. We want to run network stuff in the background,
     * so we don't lock our UI thread.
     */
    public class LoginTask extends AsyncTask<String, Void, Boolean> {

        // simply to set the username in the app settings upon success.
        private String mUsername;

        public LoginTask(String username) {
            super();

            mUsername = username;
        }

        protected void onPreExecute() {
            LoginDialog.this.setIsLoading(true);
        }

        protected void onCancelled() {
            LoginDialog.this.setIsLoading(false);
        }

        protected Boolean doInBackground(String... params) {
            String password = params[0];

            try {
                Network n = new Network(mContext);
                if (n.login(mUsername, password)) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        protected void onPostExecute(Boolean success) {
            if(success) {
                Toast.makeText(mContext, mContext.getString(R.string.login_success), Toast.LENGTH_LONG).show();
                mSettingsWrapper.setUsername(mUsername);
                LoginDialog.this.getDialog().dismiss();
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.login_failure), Toast.LENGTH_LONG).show();
                LoginDialog.this.setIsLoading(false);
            }
        }
    }
}