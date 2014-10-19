package com.mde.potdroid;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import com.mde.potdroid.fragments.MessageListFragment;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.MessageList;

/**
 * The Container Activity for the MessageList, containing a TabBar for the
 * inbox and outbox folders.
 */
public class MessageListActivity extends BaseActivity implements ActionBar.TabListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!Utils.isLoggedIn())
            finish();

    }

    public void setupTabs(ActionBar actionBar) {

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(
                actionBar.newTab().setText(R.string.tab_inbox).setTag(MessageList.TAG_INBOX)
                        .setTabListener(this)
        );
        actionBar.addTab(
                actionBar.newTab().setText(R.string.tab_outbox).setTag(MessageList.TAG_OUTBOX)
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
