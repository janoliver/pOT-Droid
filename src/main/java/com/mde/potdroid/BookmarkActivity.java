package com.mde.potdroid;

import android.content.Intent;
import android.os.Bundle;
import com.mde.potdroid.fragments.BookmarkFragment;
import com.mde.potdroid.helpers.Utils;

/**
 * The Container activity for the bookmark list.
 */
public class BookmarkActivity extends BaseActivity {
    BookmarkFragment mBookmarkFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!Utils.isLoggedIn())
            finish();

        mBookmarkFragment = (BookmarkFragment) getSupportFragmentManager()
                .findFragmentByTag("bookmarks");
        if (mBookmarkFragment == null)
            mBookmarkFragment = BookmarkFragment.newInstance();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mBookmarkFragment, "bookmarks")
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // close left sidebar
        mBookmarkFragment.onRefresh();
        closeLeftDrawer();
        closeRightDrawer();
    }
}
