package com.mde.potdroid3;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import com.mde.potdroid3.fragments.MessageListFragment;
import com.mde.potdroid3.models.MessageList;

public class MessageListActivity extends BaseActivity implements ActionBar.TabListener {
    private MessageListFragment mMessageList;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mActionBar.addTab(
                mActionBar.newTab().setText("Posteingang").setTag(MessageList.TAG_INBOX).setTabListener(this)
        );
        mActionBar.addTab(
                mActionBar.newTab().setText("Postausgang").setTag(MessageList.TAG_OUTBOX).setTabListener(this)
        );

        mMessageList = (MessageListFragment)getSupportFragmentManager().findFragmentByTag(MessageList.TAG_INBOX);
        if(mMessageList == null)
            mMessageList = MessageListFragment.newInstance(MessageList.TAG_INBOX);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction().add(R.id.content, mMessageList, MessageList.TAG_INBOX)
                    .commit();
        }
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        MessageListFragment fr = (MessageListFragment)getSupportFragmentManager()
                .findFragmentByTag((String)tab.getTag());
        if(fr == null)
            fr = MessageListFragment.newInstance((String)tab.getTag());

        ft.replace(R.id.content, fr, (String) tab.getTag());
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // hide the given tab
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // probably ignore this event
    }

}
