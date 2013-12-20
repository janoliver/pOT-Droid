package com.mde.potdroid3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import com.mde.potdroid3.fragments.FormFragment;
import com.mde.potdroid3.fragments.MessageFragment;

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
            if(u.getQueryParameter("mid") != null)
                mid = Integer.parseInt(u.getQueryParameter("mid"));

        } else {

            mid = mExtras.getInt("message_id", 0);

        }

        mMessageFragment = (MessageFragment)getSupportFragmentManager().findFragmentByTag("message");
        if(mMessageFragment == null)
            mMessageFragment = MessageFragment.newInstance(mid);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mMessageFragment, "message")
                    .commit();
        }

    }

    @Override
    protected int getLayout() {
        return R.layout.layout_activity_single_fragment_rl;
    }

    @Override
    public void onSuccess(Bundle result) {
        closeRightSidebar();
        Toast.makeText(this, "Erfolgreich gesendet", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFailure(Bundle result) {
        Toast.makeText(this, "Fehlgeschlagen", Toast.LENGTH_LONG).show();
    }


}
