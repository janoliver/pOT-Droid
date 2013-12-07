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
import com.mde.potdroid3.models.Message;
import com.mde.potdroid3.models.Post;
import com.mde.potdroid3.models.Topic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the fragment responsible for the Forms, both the post editing/writing and the 
 * PM message form. Activities must implement the FormListener, to be notified on 
 * finishing, success and failure of the form. 
 */
public class FormFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Bundle>
{

    // the title view
    protected TextView mTitle;
    
    // the input title and text views
    protected EditText mEditRcpt;
    protected EditText mEditTitle;
    protected EditText mEditText;
    
    // should be moved to a bundle.
    protected int mTopicId;
    protected int mPostId;
    protected String mToken;
    
    // this holds the kind of form this is. The static fields are defined below.
    public static int MODE_EDIT = 1;
    public static int MODE_REPLY = 2;
    public static int MODE_MESSAGE = 3;

    public static int STATUS_SUCCESS = 0;
    public static int STATUS_FAILED = 1;

    // the form listener
    protected FormListener mCallback;

    protected Bundle mFormArguments;

    public static FormFragment newInstance() {
        return new FormFragment();
    }

    // Container Activity must implement this interface
    public interface FormListener {
        public void onSuccess(Bundle result);
        public void onFailure(Bundle result);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (FormListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement FormListener");
        }

        mFormArguments = getArguments();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = super.onCreateView(inflater, container, saved);

        // find the send button and add a click listener
        ImageButton home = (ImageButton) v.findViewById(R.id.button_send);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();

                Bundle args = new Bundle(mFormArguments);
                args.putString("rcpt", mEditRcpt.getText().toString());
                args.putString("text", mEditText.getText().toString());
                args.putString("title", mEditTitle.getText().toString());

                startLoader(FormFragment.this, args);
            }
        });

        // find the cancel button and add a click listener
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
        mEditRcpt = (EditText) v.findViewById(R.id.edit_rcpt);
        mEditRcpt.setVisibility(View.GONE);

        return v;
    }

    /**
     * Prepare the form as a new post form
     * @param topic The topic Iobject in which to post
     */
    public void setIsNewPost(Topic topic) {
        clearForm();

        Bundle args = new Bundle();
        args.putInt("mode", MODE_REPLY);
        args.putInt("topic_id", topic.getId());
        args.putString("token", topic.getNewreplytoken());
        setFormArguments(args);

        mEditRcpt.setVisibility(View.GONE);
        mTitle.setText("Antwort verfassen");
    }

    /**
     * Prepare the form to be an edit post form.
     * @param topic The topic in which to post
     * @param post The post to edit
     */
    public void setIsEditPost(Topic topic, Post post) {
        clearForm();

        Bundle args = new Bundle();
        args.putInt("mode", MODE_EDIT);
        args.putInt("topic_id", topic.getId());
        args.putInt("post_id", post.getId());
        args.putString("token", post.getEdittoken());
        setFormArguments(args);

        mEditRcpt.setVisibility(View.GONE);
        mTitle.setText("Post bearbeiten");
    }

    /**
     * Prepare the form as a new PM form.
     * @param message The message object if this is a reply or null if new PM
     */
    public void setIsMessage(Message message) {
        clearForm();

        Bundle args = new Bundle();
        args.putInt("mode", MODE_MESSAGE);
        setFormArguments(args);

        mEditRcpt.setVisibility(View.VISIBLE);
        mTitle.setText("PM verfassen");

        // when the message object is given, fill in the recipient,
        // subject and message
        if(message != null) {
            // recipient
            mEditRcpt.setText(message.getFrom().getNick());

            // title with or without Re: prefix
            String prefix = "";
            if(!message.getTitle().substring(0,3).equals("Re:"))
                prefix = "Re: ";
            mEditTitle.setText(prefix + message.getTitle());

            BufferedReader bufReader = new BufferedReader(new StringReader(
                    android.text.Html.fromHtml(message.getText()).toString()));
            String line;
            StringBuilder content = new StringBuilder();

            //@TODO Date formatting
            content.append("\n\n------------------------\n");
            content.append(message.getFrom().getNick() +
                    " schrieb am " + message.getDate().toString() + ":\n");
            try {
                while((line = bufReader.readLine()) != null ) {
                    content.append("> " + line + "\n");
                }
            } catch (IOException e) {}

            mEditText.setText(content.toString());
            mEditText.setSelection(0);
        }
    }

    /**
     * This appends a given to the form text field.
     * @param text The text to append
     */
    public void appendText(String text) {
        mEditText.append(text);
    }

    protected int getLayout() {
        return R.layout.layout_sidebar_form;
    }

    protected void setFormArguments(Bundle args) {
        mFormArguments = args;
    }

    @Override
    public Loader<Bundle> onCreateLoader(int i, Bundle args) {

        showLoadingAnimation();

        if(mFormArguments.getInt("mode") == MODE_MESSAGE)
            return new AsyncMessageSubmitter(getSupportActivity(), args);
        else
            return new AsyncPostSubmitter(getSupportActivity(), args);
    }

    @Override
    public void onLoadFinished(Loader<Bundle> sender, Bundle result) {
        hideLoadingAnimation();
        clearForm();

        if(result.getInt("status", STATUS_FAILED) == STATUS_SUCCESS)
            mCallback.onSuccess(result);
        else
            mCallback.onFailure(result);
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
    public void onLoaderReset(Loader<Bundle> sender) {
        hideLoadingAnimation();
    }

    @Override
    public void showLoadingAnimation() {
        getView().findViewById(R.id.send_progress).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingAnimation() {
        // @TODO Crash, when the fragment is already detached.
        getView().findViewById(R.id.send_progress).setVisibility(View.INVISIBLE);
    }

    static class AsyncPostSubmitter extends AsyncHttpLoader<Bundle> {
        protected int mMode;

        AsyncPostSubmitter(Context cx, Bundle args) {
            super(cx, Network.BOARD_URL_POST, AsyncHttpLoader.POST, null, "ISO-8859-15");

            mMode = args.getInt("mode");

            RequestParams r = new RequestParams();

            r.add("message", args.getString("text"));
            r.add("submit", "Eintragen");
            r.add("TID", "" + args.getInt("topic_id"));
            r.add("token", args.getString("token"));

            if(mMode == MODE_EDIT) {
                r.add("PID", "" + args.getInt("post_id"));
                r.add("edit_title", args.getString("title"));
                setUrl(Network.BOARD_URL_EDITPOST);
            } else if(mMode == MODE_REPLY) {
                r.add("SID", "");
                r.add("PID", "");
                r.add("post_title", args.getString("title"));
                setUrl(Network.BOARD_URL_POST);
            }

            setParams(r);

        }

        @Override
        public Bundle processNetworkResponse(String response) {
            Pattern pattern = Pattern.compile("thread.php\\?TID=([0-9]+)&temp=[0-9]+&PID=([0-9]+)");
            Matcher m = pattern.matcher(response);

            Bundle result = new Bundle();
            result.putInt("mode", mMode);

            if (m.find()) {
                result.putInt("status", FormFragment.STATUS_SUCCESS);
                result.putInt("post_id", Integer.parseInt(m.group(2)));
            } else {
                result.putInt("status", FormFragment.STATUS_FAILED);
            }

            return result;
        }
    }

    static class AsyncMessageSubmitter extends AsyncHttpLoader<Bundle> {
        protected int mMode;

        AsyncMessageSubmitter(Context cx, Bundle args) {
            super(cx, Message.Html.SEND_URL, AsyncHttpLoader.POST, null, "ISO-8859-15");

            mMode = args.getInt("mode");

            RequestParams r = new RequestParams();

            r.add("rcpts", "0");
            r.add("rcpt", args.getString("rcpt"));
            r.add("subj", args.getString("title"));
            r.add("msg", args.getString("text"));
            r.add("mf_sc", "1");
            r.add("submit", "Senden");

            setParams(r);

        }

        @Override
        public Bundle processNetworkResponse(String response) {
            Pattern pattern = Pattern.compile("Nachricht wird an");
            Matcher m = pattern.matcher(response);

            Bundle result = new Bundle();
            result.putInt("mode", mMode);

            if (m.find()) {
                result.putInt("status", FormFragment.STATUS_SUCCESS);
            } else {
                result.putInt("status", FormFragment.STATUS_FAILED);
            }

            return result;
        }
    }
}
