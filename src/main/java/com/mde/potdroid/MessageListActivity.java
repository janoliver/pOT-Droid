package com.mde.potdroid;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;

import com.mde.potdroid.fragments.MessageListFragment;
import com.mde.potdroid.models.MessageList;

/**
 * The Container Activity for the MessageList, containing a TabBar for the
 * inbox and outbox folders.
 */
public class MessageListActivity extends BaseActivity implements ActionBar.TabListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(
                actionBar.newTab().setText("Posteingang").setTag(MessageList.TAG_INBOX)
                        .setTabListener(this)
        );
        actionBar.addTab(
                actionBar.newTab().setText("Postausgang").setTag(MessageList.TAG_OUTBOX)
                        .setTabListener(this)
        );
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        MessageListFragment fr = (MessageListFragment) getSupportFragmentManager()
                .findFragmentByTag((String) tab.getTag());
        if (fr == null)
            fr = MessageListFragment.newInstance((String) tab.getTag());

        ft.replace(R.id.content, fr, (String) tab.getTag());
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // hide the given tab
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // probably ignore this event
    }
}
