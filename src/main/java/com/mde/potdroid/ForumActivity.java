package com.mde.potdroid;

import android.os.Bundle;
import com.mde.potdroid.fragments.ForumFragment;

/**
 * The Forum overview container.
 */
public class ForumActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ForumFragment bm = (ForumFragment) getSupportFragmentManager()
                .findFragmentByTag("forum");
        if (bm == null)
            bm = ForumFragment.newInstance();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, bm, "forum")
                    .commit();
        }
    }
}
