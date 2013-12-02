package com.mde.potdroid3.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.loopj.android.http.RequestParams;
import com.mde.potdroid3.R;
import com.mde.potdroid3.helpers.AsyncHttpLoader;
import com.mde.potdroid3.helpers.Network;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Integer> {

    private TextView mTitle;
    private EditText mEditTitle;
    private EditText mEditText;
    private int mTopicId;
    private int mPostId;
    private String mToken;
    private int mMode;
    private Network mNetwork;

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

        mNetwork = new Network(getActivity());

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
        AsyncPostSubmitter l = new AsyncPostSubmitter(getSupportActivity(), args);
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

    static class AsyncPostSubmitter extends AsyncHttpLoader<Integer> {
        AsyncPostSubmitter(Context cx, Bundle args) {
            super(cx, Network.BOARD_URL_POST, AsyncHttpLoader.POST);

            Integer mode = args.getInt("mode");

            RequestParams r = new RequestParams();

            r.add("message", args.getString("text"));
            r.add("submit", "Eintragen");
            r.add("TID", "" + args.getInt("tid"));
            r.add("token", args.getString("token"));

            if(mode == MODE_EDIT) {
                r.add("PID", "" + args.getInt("pid"));
                r.add("edit_title", args.getString("title"));
                setUrl(Network.BOARD_URL_EDITPOST);
            } else if(mode == MODE_REPLY) {
                r.add("SID", "");
                r.add("PID", "");
                r.add("post_title", args.getString("title"));
                setUrl(Network.BOARD_URL_POST);
            }

            setParams(r);

        }

        @Override
        public Integer processNetworkResponse(String response) {
            Pattern pattern = Pattern.compile("thread.php\\?TID=([0-9]+)&temp=[0-9]+&PID=([0-9]+)");
            Matcher m = pattern.matcher(response);

            if (m.find()) {
                return Integer.parseInt(m.group(2));
            } else {
                return 0;
            }
        }
    }


}
