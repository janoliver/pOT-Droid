package com.mde.potdroid;

import android.os.Bundle;
import com.mde.potdroid.fragments.EditorFragment;

/**
 * The Forum overview container.
 */
public class EditorActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EditorFragment bm = (EditorFragment) getSupportFragmentManager()
                .findFragmentByTag("editor");
        if (bm == null)
            bm = EditorFragment.newInstance(mExtras);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.content, bm, "editor").commit();
        }
    }

}
