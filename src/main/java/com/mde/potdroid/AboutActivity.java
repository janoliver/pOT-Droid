package com.mde.potdroid;

import android.os.Bundle;
import com.mde.potdroid.fragments.AboutFragment;

/**
 * The Activity that contains a AboutFragment. It handles some callbacks of the
 * Formlistener after Post submission.
 */
public class AboutActivity extends BaseActivity {

    private static final String FRAGMENT_TAG = "about";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create and add the fragment
        AboutFragment fragment = (AboutFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null)
            fragment = AboutFragment.newInstance();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, fragment, FRAGMENT_TAG)
                    .commit();
        }
    }

}
