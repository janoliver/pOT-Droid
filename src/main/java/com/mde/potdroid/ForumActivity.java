package com.mde.potdroid;

import android.os.Bundle;
import com.mde.potdroid.fragments.ForumFragment;

public class ForumActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, new ForumFragment())
                    .commit();
        }

    }
}
