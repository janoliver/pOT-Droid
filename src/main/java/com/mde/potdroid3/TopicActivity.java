package com.mde.potdroid3;

import android.os.Bundle;
import com.mde.potdroid3.fragments.FormFragment;
import com.mde.potdroid3.fragments.TopicFragment;

public class TopicActivity extends BaseActivity implements TopicFragment.OnContentLoadedListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int bid = mExtras.getInt("thread_id", 0);
        int page = mExtras.getInt("page", 1);
        int pid = mExtras.getInt("post_id", 0);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.sidebar_container_right, FormFragment.newInstance())
                    .commit();

            getFragmentManager().beginTransaction()
                    .add(R.id.content, TopicFragment.newInstance(bid, page, pid))
                    .commit();
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.layout_activity_single_fragment_rl;
    }

    @Override
    public void onContentLoaded() {
        // notify the sidebar fragment about the loading state
        mSidebar.refreshBookmarks();
    }
}
