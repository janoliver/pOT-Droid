package com.mde.potdroid3;

import android.os.Bundle;
import com.mde.potdroid3.fragments.FormFragment;
import com.mde.potdroid3.fragments.MessageFragment;

public class MessageActivity extends BaseActivity implements FormFragment.FormListener
{
    private MessageFragment mMessageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int mid = mExtras.getInt("message_id", 0);

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
    public void onSuccessReply(int pid) {
        closeRightSidebar();
    }

    @Override
    public void onSuccessEdit() {
        closeRightSidebar();
    }
}
