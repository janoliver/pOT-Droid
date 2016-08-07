package com.mde.potdroid;

import android.os.Bundle;
import com.mde.potdroid.fragments.MediaFragment;

public class MediaActivity extends BaseActivity {
    private static final String FRAGMENT_TAG = "media";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create and add the fragment
        MediaFragment fragment = (MediaFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null)
            fragment = MediaFragment.newInstance(mExtras);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, fragment, FRAGMENT_TAG)
                    .commit();
        }
    }

}
