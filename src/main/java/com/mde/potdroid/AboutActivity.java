package com.mde.potdroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.mde.potdroid.fragments.AboutFragment;

/**
 * The Activity that contains a AboutFragment. It handles some callbacks of the
 * Formlistener after Post submission.
 */
public class AboutActivity extends BaseActivity
{

    private AboutFragment mAboutFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create and add the fragment
        mAboutFragment = (AboutFragment) getSupportFragmentManager().findFragmentByTag("about");
        if (mAboutFragment == null)
            mAboutFragment = AboutFragment.newInstance();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mAboutFragment, "about")
                    .commit();
        }
    }

}
