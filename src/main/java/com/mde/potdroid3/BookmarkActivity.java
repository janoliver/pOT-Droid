package com.mde.potdroid3;

import android.os.Bundle;
import com.mde.potdroid3.fragments.BookmarkFragment;

public class BookmarkActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(R.id.content, new BookmarkFragment())
                .commit();
    }
}
