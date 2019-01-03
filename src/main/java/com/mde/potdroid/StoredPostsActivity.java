package com.mde.potdroid;

import com.mde.potdroid.fragments.StoredPostsFragment;

import android.os.Bundle;

public class StoredPostsActivity extends BaseActivity {
    StoredPostsFragment mStoredPostsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStoredPostsFragment = (StoredPostsFragment) getSupportFragmentManager()
                .findFragmentByTag("storedposts");

        if (mStoredPostsFragment == null)
            mStoredPostsFragment = StoredPostsFragment.newInstance();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mStoredPostsFragment, "storedposts")
                    .commit();
        }
    }
}
