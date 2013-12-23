package com.mde.potdroid;

import android.os.Bundle;

import com.mde.potdroid.fragments.BookmarkFragment;

public class BookmarkActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction().add(R.id.content, new BookmarkFragment())
                    .commit();
        }
    }
}
