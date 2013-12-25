package com.mde.potdroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.mde.potdroid.fragments.BoardFragment;

/**
 * Activity that wraps the BoardFragment
 */
public class BoardActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Integer bid = 0;
        Integer page = 0;

        // check, if the activity was opened from externally
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {

            Uri u = intent.getData();

            if (u.getQueryParameter("BID") != null)
                bid = Integer.parseInt(u.getQueryParameter("BID"));

            if (u.getQueryParameter("page") != null)
                page = Integer.parseInt(u.getQueryParameter("page"));

        } else {

            bid = mExtras.getInt("board_id", 0);
            page = mExtras.getInt("page", 1);

        }

        // create and add the fragment
        BoardFragment bm = (BoardFragment) getSupportFragmentManager()
                .findFragmentByTag("board");
        if (bm == null)
            bm = BoardFragment.newInstance(bid, page);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, bm, "board")
                    .commit();
        }
    }
}
