package com.mde.potdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.mde.potdroid.fragments.BoardFragment;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;

/**
 * This is a small launcher activity that forwards to the start Activity as set+
 * in the preferences.
 */
public class LauncherActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent;
        SettingsWrapper s = new SettingsWrapper(this);
        Utils.setApplicationContext(getApplicationContext());

        if(s.getStartActivity() == SettingsWrapper.START_FORUM) {
            intent = new Intent(LauncherActivity.this, BoardActivity.class);
            intent.putExtra(BoardFragment.ARG_ID, s.getStartForum());
            intent.putExtra(BoardFragment.ARG_PAGE, 1);
        } else if(s.getStartActivity() == SettingsWrapper.START_BOOKMARKS && Utils.isLoggedIn()) {
            intent = new Intent(LauncherActivity.this, BookmarkActivity.class);
        } else {
            intent = new Intent(LauncherActivity.this, ForumActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
