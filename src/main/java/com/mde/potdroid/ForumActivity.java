package com.mde.potdroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.mde.potdroid.fragments.BoardFragment;
import com.mde.potdroid.fragments.BookmarkFragment;
import com.mde.potdroid.fragments.ForumFragment;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;

/**
 * The Forum overview container.
 */
public class ForumActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment bm;
        String tag;

        SettingsWrapper s = new SettingsWrapper(this);
        if(!getIntent().hasExtra("overview") && s.getStartActivity() == SettingsWrapper.START_FORUM) {
            tag = "board";
            bm = getSupportFragmentManager().findFragmentByTag(tag);
            if (bm == null)
                bm = BoardFragment.newInstance(s.getStartForum(), 1);
        } else if(!getIntent().hasExtra("overview") &&
                s.getStartActivity() == SettingsWrapper.START_BOOKMARKS && Utils.isLoggedIn()) {
            tag = "bookmarks";
            bm = getSupportFragmentManager().findFragmentByTag(tag);
            if (bm == null)
                bm = BookmarkFragment.newInstance();
        } else {
            tag = "forum";
            bm = getSupportFragmentManager().findFragmentByTag(tag);
            if (bm == null)
                bm = ForumFragment.newInstance();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, bm, tag)
                    .commit();
        }
    }
}
