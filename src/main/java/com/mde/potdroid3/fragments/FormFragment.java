package com.mde.potdroid3.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

public class FormFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Integer> {

    private TextView mTitle;
    private EditText mEditTitle;
    private EditText mEditText;
    private int mTopicId;
    private int mPostId;
    private String mToken;
    private int mMode;

    FormListener mCallback;

    public static int MODE_EDIT = 1;
    public static int MODE_REPLY = 2;

    public static FormFragment newInstance() {
        return new FormFragment();
    }

    // Container Activity must implement this interface
    public interface FormListener {
        public void onSuccessReply(int pid);
        public void onSuccessEdit();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (FormListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPostedListener");
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = super.onCreateView(inflater, container, saved);

        ImageButton home = (ImageButton) v.findViewById(R.id.button_send);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();

                Bundle args = new Bundle();
                args.putInt("mode", mMode);
                args.putInt("pid", mPostId);
                args.putInt("tid", mTopicId);
                args.putString("token", mToken);
                args.putString("text", mEditText.getText().toString());
                args.putString("title", mEditTitle.getText().toString());

                startLoader(FormFragment.this, args);
            }
        });

        ImageButton preferences = (ImageButton) v.findViewById(R.id.button_cancel);
        preferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLoader();
            }
        });

        mTitle = (TextView) v.findViewById(R.id.text_form_title);
        mEditText = (EditText) v.findViewById(R.id.edit_content);
        mEditTitle = (EditText) v.findViewById(R.id.edit_title);

        return v;
    }

    public void setIsNewPost(int topic_id, String token) {
        clearForm();
        mTopicId = topic_id;
        mToken = token;
        mMode = MODE_REPLY;
        mTitle.setText("Antwort verfassen");
    }

    public void setIsEditPost(int topic_id, int post_id, String token) {
        clearForm();
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
    public Loader<Integer> onCreateLoader(int i, Bundle args) {
        AsyncPostSubmitter l = new AsyncPostSubmitter(getSupportActivity(), mNetwork, args);
        showLoadingAnimation();

        return l;
    }

    @Override
    public void onLoadFinished(Loader<Integer> sender, Integer pid) {
        hideLoadingAnimation();
        if(mMode == MODE_REPLY && pid > 0) {
            clearForm();
            mCallback.onSuccessReply(pid);
        } else if(mMode == MODE_EDIT) {
            clearForm();
            mCallback.onSuccessEdit();
        }
    }

    public void clearForm() {
        mEditText.setText("");
        mEditTitle.setText("");
        mTopicId = 0;
        mPostId = 0;
        mToken = "";
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSupportActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(),
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    public void onLoaderReset(Loader<Integer> sender) {
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
    static class AsyncPostSubmitter extends AsyncTaskLoader<Integer> {
        private Network mNetwork;
        private int mTopicId;
        private int mPostId;
        private String mToken;
        private String mText;
        private String mTitle;
        private int mMode;
        private Context mContext;

        AsyncPostSubmitter(Context cx, Network network, Bundle args) {
            super(cx);
            mContext = cx;
            mNetwork = network;

            mMode = args.getInt("mode");
            mTopicId = args.getInt("tid");
            mPostId = args.getInt("pid");
            mToken = args.getString("token");
            mText = args.getString("text");
            mTitle = args.getString("title");
        }

        @Override
        public Integer loadInBackground() {
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
                    return 0;
                }

            } catch (Exception e) {
                return 0;
            }
        }

    }

}
