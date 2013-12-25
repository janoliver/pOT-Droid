package com.mde.potdroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.mde.potdroid.fragments.FormFragment;
import com.mde.potdroid.fragments.MessageFragment;
import com.mde.potdroid.helpers.Utils;

/**
 * Activity that displays as MessageFragment showing a PM message
 */
public class MessageActivity extends BaseActivity implements FormFragment.FormListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Integer mid = 0;

        // check, if the activity was opened from externally
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {

            Uri u = intent.getData();
            if (u.getQueryParameter("mid") != null)
                mid = Integer.parseInt(u.getQueryParameter("mid"));

        } else {

            mid = mExtras.getInt("message_id", 0);

        }

        // create and add the fragment
        MessageFragment messageFragment = (MessageFragment) getSupportFragmentManager()
                .findFragmentByTag("message");
        if (messageFragment == null)
            messageFragment = MessageFragment.newInstance(mid);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, messageFragment, "message")
                    .commit();
        }

    }

    @Override
    protected int getLayout() {
        return R.layout.layout_sidebar_rl;
    }

    /**
     * Called upon Form success.
     * @param result a bundle containing information about the result
     */
    @Override
    public void onSuccess(Bundle result) {
        closeRightSidebar();
        Utils.toast(this, getString(R.string.send_successful));
    }

    /**
     * Called upon Form failure.
     * @param result a bundle containing information about the result
     */
    @Override
    public void onFailure(Bundle result) {
        Utils.toast(this, getString(R.string.send_failure));
    }


}
