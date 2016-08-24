package com.mde.potdroid.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.Network;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;

/**
 * This class is a DialogPreference for the Login Action. It takes care of showing the login form
 * along with a loading animation (that is shown during the connection) and appropriately handles
 * the cancel button.
 */
public class LoginDialog extends PreferenceDialogFragmentCompat {

    private SettingsWrapper mSettingsWrapper;

    private EditText mUsername;
    private EditText mPassword;
    private ProgressBar mLoader;

    // true if a server request is made, false otherwise
    private Boolean mLoggingIn;
    private Activity mContext;

    public static LoginDialog newInstance(String key) {
        final LoginDialog fragment = new LoginDialog();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }


    @Override
    public void onStart() {
        super.onStart();

        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setIsLoading(true);

                    final String user_name = mUsername.getText().toString().trim();
                    final String user_password = mPassword.getText().toString();

                    Network n = new Network(mContext);
                    n.login(user_name, user_password, new Network.LoginCallback() {
                        @Override
                        public void onSuccess() {
                            mContext.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Snackbar snackbar = Snackbar
                                            .make(mContext.findViewById(android.R.id.content), R.string.msg_login_success, Snackbar.LENGTH_LONG)
                                            .setAction("Neu starten", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent i = mContext.getPackageManager()
                                                            .getLaunchIntentForPackage(mContext.getPackageName());
                                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    mContext.startActivity(i);
                                                }
                                            });
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(Utils.getColorByAttr(mContext, R.attr.bbSuccessColor));
                                    TextView tv = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                                    tv.setTextColor(Color.WHITE);
                                    snackbar.show();
                                    setIsLoading(false);
                                }
                            });

                            mSettingsWrapper.setUsername(user_name);
                            d.dismiss();
                        }

                        @Override
                        public void onFailure() {
                            mContext.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Snackbar snackbar = Snackbar
                                            .make(mContext.findViewById(android.R.id.content), R.string.msg_login_failure, Snackbar.LENGTH_LONG);
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(Utils.getColorByAttr(mContext, R.attr.bbErrorColor));
                                    TextView tv = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                                    tv.setTextColor(Color.WHITE);
                                    snackbar.show();
                                    setIsLoading(false);
                                }
                            });
                            d.dismiss();
                        }
                    });
                }
            });
        }
    }


    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        if (mSettingsWrapper.hasUsername())
            mUsername.setText(mSettingsWrapper.getUsername());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mSettingsWrapper = new SettingsWrapper(getContext());
        mLoggingIn = false;
    }

    @Override
    protected View onCreateDialogView(Context context) {
        View v = super.onCreateDialogView(context);

        mUsername = (EditText) v.findViewById(R.id.user_name);
        mPassword = (EditText) v.findViewById(R.id.user_password);
        mLoader = (ProgressBar) v.findViewById(R.id.login_progress);

        return v;
    }

    /**
     * Show/hide the loading animation and disable/enable the form elements.
     */
    public void setIsLoading(boolean is_loading) {
        if (is_loading && !mLoggingIn) {
            mLoggingIn = true;
            mLoader.setVisibility(View.VISIBLE);
            mUsername.setEnabled(false);
            mPassword.setEnabled(false);
        } else if (mLoggingIn) {
            mLoggingIn = false;
            mLoader.setVisibility(View.INVISIBLE);
            mUsername.setEnabled(true);
            mPassword.setEnabled(true);
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // do nothing.
    }

}