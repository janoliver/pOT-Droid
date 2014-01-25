package com.mde.potdroid;

import android.os.Bundle;
import com.mde.potdroid.fragments.EditorFragment;
import com.mde.potdroid.helpers.Utils;

/**
 * The Forum overview container.
 */
public class EditorActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!Utils.isLoggedIn())
            finish();

        EditorFragment bm = (EditorFragment) getSupportFragmentManager()
                .findFragmentByTag("editor");
        if (bm == null)
            bm = EditorFragment.newInstance(mExtras);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.content, bm, "editor").commit();
        }
    }

}
