package com.mde.potdroid3;

import android.os.Bundle;
import com.mde.potdroid3.fragments.BoardFragment;

public class BoardActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {

            int bid = mExtras.getInt("board_id", 0);
            int page = mExtras.getInt("page", 1);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, BoardFragment.newInstance(bid, page))
                    .commit();
        }
    }
}
