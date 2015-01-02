package com.mde.potdroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.widget.FrameLayout;
import com.mde.potdroid.fragments.MessageListFragment;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.MessageList;

/**
 * The Container Activity for the MessageList, containing a TabBar for the
 * inbox and outbox folders.
 */
public class MessageListActivity extends BaseActivity {
    FragmentPagerAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!Utils.isLoggedIn())
            finish();

        FrameLayout content = (FrameLayout) findViewById(R.id.content);
        getLayoutInflater().inflate(R.layout.layout_messages_container, content, true);

        ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
        adapterViewPager = new MessageListPagerAdapter(getSupportFragmentManager(), this);
        vpPager.setAdapter(adapterViewPager);


        PagerTabStrip strip = (PagerTabStrip) findViewById(R.id.pager_header);
        strip.setDrawFullUnderline(false);

    }

    public static class MessageListPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;
        private MessageListActivity mActivity;

        public MessageListPagerAdapter(FragmentManager fragmentManager, MessageListActivity activity) {
            super(fragmentManager);
            mActivity = activity;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return mActivity.getFragmentByTag(MessageList.TAG_INBOX);
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return mActivity.getFragmentByTag(MessageList.TAG_OUTBOX);
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return mActivity.getResources().getString(R.string.tab_inbox);
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return mActivity.getResources().getString(R.string.tab_outbox);
                default:
                    return null;
            }
        }

    }

    public Fragment getFragmentByTag(String tag) {
        MessageListFragment fr = (MessageListFragment) getSupportFragmentManager()
                .findFragmentByTag(tag);
        if (fr == null)
            fr = MessageListFragment.newInstance(tag);
        return fr;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        closeLeftDrawer();
    }

}
