package com.mde.potdroid3;

import android.os.Bundle;
import com.mde.potdroid3.fragments.BoardFragment;

public class BoardActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int bid = mExtras.getInt("board_id", 0);
        int page = mExtras.getInt("page", 1);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, BoardFragment.newInstance(bid, page))
                .commit();
    }
}
