package com.mde.potdroid;

import android.os.Bundle;

import com.mde.potdroid.fragments.BookmarkFragment;
import com.mde.potdroid.fragments.TopicFragment;

/**
 * The Container activity for the bookmark list.
 */
public class BookmarkActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BookmarkFragment bm = (BookmarkFragment) getSupportFragmentManager()
                .findFragmentByTag("bookmarks");
        if (bm == null)
            bm = BookmarkFragment.newInstance();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, bm, "bookmarks")
                    .commit();
        }
    }
}
