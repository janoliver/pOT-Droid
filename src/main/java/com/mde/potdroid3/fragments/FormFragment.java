package com.mde.potdroid3.fragments;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.mde.potdroid3.R;
import com.mde.potdroid3.helpers.Network;
import com.mde.potdroid3.helpers.Utils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class FormFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Boolean> {

    private TextView mTitle;
    private EditText mEditTitle;
    private EditText mEditText;
    private int mTopicId;
    private int mPostId;
    private String mToken;
    private int mMode;

    public static int MODE_EDIT = 1;
    public static int MODE_REPLY = 2;

    public static FormFragment newInstance() {
        return new FormFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ImageButton home = (ImageButton) getView().findViewById(R.id.button_send);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoader(FormFragment.this);
            }
        });

        ImageButton preferences = (ImageButton) getView().findViewById(R.id.button_cancel);
        preferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cancel loader
            }
        });

        mTitle = (TextView) getView().findViewById(R.id.text_form_title);
        mEditText = (EditText) getView().findViewById(R.id.edit_content);
        mEditTitle = (EditText) getView().findViewById(R.id.edit_title);
    }

    public void setIsNewPost(int topic_id, String token) {
        mTopicId = topic_id;
        mToken = token;
        mMode = MODE_REPLY;
        mTitle.setText("Antwort verfassen");
    }

    public void setIsEditPost(int topic_id, int post_id, String token) {
        mTopicId = topic_id;
        mToken = token;
        mPostId = post_id;
        mMode = MODE_EDIT;
        mTitle.setText("Post bearbeiten");
    }

    public void appendText(String text)
    {
        mEditText.append(text);
    }

    protected int getLayout() {
        return R.layout.layout_sidebar_form;
    }

    @Override
    public Loader<Boolean> onCreateLoader(int i, Bundle bundle) {
        AsyncPostSubmitter l = new AsyncPostSubmitter(getActivity(), mNetwork, mMode, mTopicId,
                mPostId, mToken, mEditTitle.getText().toString(), mEditText.getText().toString());
        showLoadingAnimation();

        return l;
    }

    @Override
    public void onLoadFinished(Loader<Boolean> sender, Boolean success) {
        hideLoadingAnimation();
    }

    @Override
    public void onLoaderReset(Loader<Boolean> sender) {
        hideLoadingAnimation();
    }

    @Override
    public void showLoadingAnimation() {
        getView().findViewById(R.id.send_progress).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingAnimation() {
        getView().findViewById(R.id.send_progress).setVisibility(View.INVISIBLE);
    }

    /**
     * Takes care of loading the topic XML asynchroneously.
     */
    static class AsyncPostSubmitter extends AsyncTaskLoader<Boolean> {
        private Network mNetwork;
        private int mTopicId;
        private int mPostId;
        private String mToken;
        private String mText;
        private String mTitle;
        private int mMode;
        private Context mContext;

        AsyncPostSubmitter(Context cx, Network network, int mode, int thread_id, int post_id,
                           String token, String title, String text) {
            super(cx);
            mContext = cx;
            mNetwork = network;

            mMode = mode;
            mTopicId = thread_id;
            mPostId = post_id;
            mToken = token;
            mText = text;
            mTitle = title;
        }

        @Override
        public Boolean loadInBackground() {
            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                nameValuePairs.add(new BasicNameValuePair("message", mText));
                nameValuePairs.add(new BasicNameValuePair("submit", "Eintragen"));
                nameValuePairs.add(new BasicNameValuePair("TID", "" + mTopicId));
                nameValuePairs.add(new BasicNameValuePair("token", mToken));

                if(mMode == MODE_EDIT) {
                    nameValuePairs.add(new BasicNameValuePair("PID", "" + mPostId));
                    nameValuePairs.add(new BasicNameValuePair("edit_title", mTitle));
                    //nameValuePairs.add(new BasicNameValuePair("edit_icon", mIconId));

                    return mNetwork.sendPost(Utils.BOARD_URL_EDITPOST, nameValuePairs);
                } else if(mMode == MODE_REPLY) {
                    nameValuePairs.add(new BasicNameValuePair("SID", ""));
                    nameValuePairs.add(new BasicNameValuePair("PID", ""));
                    nameValuePairs.add(new BasicNameValuePair("post_title", mTitle));
                    //nameValuePairs.add(new BasicNameValuePair("post_icon", mIconId));

                    return mNetwork.sendPost(Utils.BOARD_URL_POST, nameValuePairs);
                } else {
                    return false;
                }

            } catch (Exception e) {
                return false;
            }
        }

    }

}
