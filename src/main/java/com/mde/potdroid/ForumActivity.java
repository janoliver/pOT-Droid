package com.mde.potdroid;

import android.content.Intent;
import android.os.Bundle;
import com.mde.potdroid.fragments.ForumFragment;

/**
 * The Forum overview container.
 */
public class ForumActivity extends BaseActivity {
    private ForumFragment mForumFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mForumFragment = (ForumFragment)getSupportFragmentManager().findFragmentByTag("forum");
        if (mForumFragment == null)
            mForumFragment = ForumFragment.newInstance();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mForumFragment, "forum")
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // close left sidebar
        mForumFragment.onRefresh(null);
        closeLeftDrawer();
    }
}
