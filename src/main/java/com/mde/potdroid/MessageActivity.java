package com.mde.potdroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.mde.potdroid.fragments.FormFragment;
import com.mde.potdroid.fragments.MessageFragment;
import com.mde.potdroid.helpers.Utils;

public class MessageActivity extends BaseActivity implements FormFragment.FormListener
{

    private MessageFragment mMessageFragment;

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

        mMessageFragment = (MessageFragment) getSupportFragmentManager().findFragmentByTag
                ("message");
        if (mMessageFragment == null)
            mMessageFragment = MessageFragment.newInstance(mid);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mMessageFragment, "message")
                    .commit();
        }

    }

    @Override
    protected int getLayout() {
        return R.layout.layout_sidebar_rl;
    }

    @Override
    public void onSuccess(Bundle result) {
        closeRightSidebar();
        Utils.toast(this, getString(R.string.send_successful));
    }

    @Override
    public void onFailure(Bundle result) {
        Utils.toast(this, getString(R.string.send_failure));
    }


}
