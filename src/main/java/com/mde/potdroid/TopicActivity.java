package com.mde.potdroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import com.mde.potdroid.fragments.FormFragment;
import com.mde.potdroid.fragments.TopicFragment;

public class TopicActivity extends BaseActivity implements FormFragment.FormListener
{
    private TopicFragment mTopicFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Integer tid = 0;
        Integer page = 1;
        Integer pid = 0;

        // check, if the activity was opened from externally
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {

            Uri u = intent.getData();

            if(u.getQueryParameter("TID") != null)
                tid = Integer.parseInt(u.getQueryParameter("TID"));

            if(u.getQueryParameter("PID") != null)
                pid = Integer.parseInt(u.getQueryParameter("PID"));

            if(u.getQueryParameter("page") != null)
                page = Integer.parseInt(u.getQueryParameter("page"));

        } else {

            tid = mExtras.getInt("thread_id", 0);
            page = mExtras.getInt("page", 1);
            pid = mExtras.getInt("post_id", 0);

        }

        mTopicFragment = (TopicFragment)getSupportFragmentManager().findFragmentByTag("topic");
        if(mTopicFragment == null)
            mTopicFragment = TopicFragment.newInstance(tid, page, pid);

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
