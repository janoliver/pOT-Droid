package com.mde.potdroid3;

import android.os.Bundle;
import android.widget.Toast;
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

        mTopicFragment = (TopicFragment)getSupportFragmentManager().findFragmentByTag("topic");
        if(mTopicFragment == null)
            mTopicFragment = TopicFragment.newInstance(bid, page, pid);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mTopicFragment, "topic")
                    .commit();
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.layout_activity_single_fragment_rl;
    }

    @Override
    public void onSuccess(Bundle result) {
        closeRightSidebar();

        if(result.getInt("mode") == FormFragment.MODE_EDIT)
            mTopicFragment.refreshPage();
        else
            mTopicFragment.goToLastPost(result.getInt("post_id"));
    }

    @Override
    public void onFailure(Bundle result) {
        Toast.makeText(this, "Fehlgeschlagen", Toast.LENGTH_LONG).show();
    }

}
