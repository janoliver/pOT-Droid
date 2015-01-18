package com.mde.potdroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.mde.potdroid.fragments.BoardFragment;

/**
 * Activity that wraps the BoardFragment
 */
public class BoardActivity extends BaseActivity {

    private BoardFragment mBoardFragment;
    private Integer mBoardId = 0;
    private Integer mPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setArgs(getIntent());

        // create and add the fragment
        mBoardFragment = (BoardFragment) getSupportFragmentManager().findFragmentByTag("board");
        if (mBoardFragment == null)
            mBoardFragment = BoardFragment.newInstance(mBoardId, mPage);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mBoardFragment, "board")
                    .commit();
        }
    }

    public void setArgs(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {

            Uri u = intent.getData();

            if (u.getQueryParameter("BID") != null)
                mBoardId = Integer.parseInt(u.getQueryParameter("BID"));

            if (u.getQueryParameter("page") != null)
                mPage = Integer.parseInt(u.getQueryParameter("page"));

        } else {

            mBoardId = intent.getExtras().getInt("board_id", 0);
            mPage = intent.getExtras().getInt("page", 1);

        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setArgs(intent);

        // close left sidebar
        Bundle args = mBoardFragment.getArguments();
        args.putInt(BoardFragment.ARG_ID, mBoardId);
        args.putInt(BoardFragment.ARG_PAGE, mPage);

        mBoardFragment.refreshPage();

        closeLeftDrawer();
        closeRightDrawer();
    }
}
