package com.mde.potdroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.mde.potdroid.fragments.TopicFragment;

/**
 * The Activity that contains a TopicFragment. It handles some callbacks of the
 * Formlistener after Post submission.
 */
public class TopicActivity extends BaseActivity {

    private TopicFragment mTopicFragment;
    private Integer mTopicId = 0;
    private Integer mPage = 1;
    private Integer mPostId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setArgs(getIntent());

        // create and add the fragment
        mTopicFragment = (TopicFragment) getSupportFragmentManager().findFragmentByTag("topic");
        if (mTopicFragment == null)
            mTopicFragment = TopicFragment.newInstance(mTopicId, mPage, mPostId);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mTopicFragment, "topic")
                    .commit();
        }
    }

    public void setArgs(Intent i) {
        Bundle b = i.getExtras();

        if (Intent.ACTION_VIEW.equals(i.getAction())) {

            Uri u = i.getData();

            if (u.getQueryParameter("TID") != null)
                mTopicId = Integer.parseInt(u.getQueryParameter("TID"));

            if (u.getQueryParameter("PID") != null)
                mPostId = Integer.parseInt(u.getQueryParameter("PID"));

            if (u.getQueryParameter("page") != null)
                mPage = Integer.parseInt(u.getQueryParameter("page"));

        } else {

            mTopicId = b.getInt("thread_id", 0);
            mPage = b.getInt("page", 1);
            mPostId = b.getInt("post_id", 0);

        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setArgs(intent);

        // close left sidebar
        Bundle args = mTopicFragment.getArguments();
        args.putInt(TopicFragment.ARG_TOPIC_ID, mTopicId);
        args.putInt(TopicFragment.ARG_PAGE, mPage);
        args.putInt(TopicFragment.ARG_POST_ID, mPostId);
        mTopicFragment.registerScroll(mPostId);

        mTopicFragment.refreshPage();

        closeLeftDrawer();
        closeRightDrawer();
    }

}
