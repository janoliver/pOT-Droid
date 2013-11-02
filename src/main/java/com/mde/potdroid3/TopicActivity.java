package com.mde.potdroid3;

import android.os.Bundle;
import com.mde.potdroid3.fragments.FormFragment;
import com.mde.potdroid3.fragments.TopicFragment;

public class TopicActivity extends BaseActivity implements FormFragment.FormListener
{
    private TopicFragment mTopicFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int bid = mExtras.getInt("thread_id", 0);
        int page = mExtras.getInt("page", 1);
        int pid = mExtras.getInt("post_id", 0);

        mTopicFragment = (TopicFragment)getFragmentManager().findFragmentByTag("topic");
        if(mTopicFragment == null)
            mTopicFragment = TopicFragment.newInstance(bid, page, pid);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.content, mTopicFragment, "topic")
                    .commit();
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.layout_activity_single_fragment_rl;
    }

    @Override
    public void onSuccessReply(int pid) {
        closeRightSidebar();
        mTopicFragment.goToLastPost(pid);
    }

    @Override
    public void onSuccessEdit() {
        closeRightSidebar();
        mTopicFragment.refreshPage();
    }
}
