package com.mde.potdroid.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mde.potdroid.BaseActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.helpers.EncodingRequestParams;
import com.mde.potdroid.helpers.Network;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.Message;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;
import com.mde.potdroid.parsers.MessageParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the fragment responsible for the Forms, both the post editing/writing and the
 * PM message form. Activities must implement the FormListener, to be notified on
 * finishing, success and failure of the form.
 */
public class FormFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Bundle>
{

    protected static final String ARG_MODE = "mode";
    protected static final String ARG_TOPIC_ID = "topic_id";
    protected static final String ARG_POST_ID = "post_id";
    protected static final String ARG_TOKEN = "token";
    protected static final String ARG_ICON = "icon";

    // the title view
    protected TextView mTitle;

    // the input title and text views
    protected EditText mEditRcpt;
    protected EditText mEditTitle;
    protected EditText mEditText;
    protected ImageButton mIcon;

    // should be moved to a bundle.
    protected int mTopicId;
    protected int mPostId;
    protected String mToken;
    protected Topic mTopicCache;

    // this holds the kind of form this is. The static fields are defined below.
    public static int MODE_EDIT = 1;
    public static int MODE_REPLY = 2;
    public static int MODE_MESSAGE = 3;

    protected static final String STATUS = "status";
    public static int STATUS_SUCCESS = 0;
    public static int STATUS_FAILED = 1;

    // the form listener
    protected FormListener mCallback;

    // this bundle will be given to the loader
    protected Bundle mFormArguments;
    protected static final String ARG_RCPT = "rcpt";
    protected static final String ARG_TEXT = "text";
    protected static final String ARG_TITLE = "title";

    // the array of icons
    private int mIconId;


    /**
     * * Return new instance of FormFragment. Although this fragment has no parameters,
     * We provide this method for consistency.
     *
     * @return FormFragment
     */
    public static FormFragment newInstance() {
        return new FormFragment();
    }

    /**
     * The containing Activity must implement this interface to be notified about
     * success and failure of the submission
     */
    public interface FormListener
    {

        public void onSuccess(Bundle result);

        public void onFailure(Bundle result);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mFormArguments = getArguments();
    }

    public void setFormListener(FormListener callback) {
        mCallback = callback;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_sidebar_form, container, false);


        // find the send button and add a click listener
        ImageButton send = (ImageButton) v.findViewById(R.id.button_send);
        send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                hideKeyboard();

                Bundle args = new Bundle(mFormArguments);
                args.putString(ARG_RCPT, mEditRcpt.getText().toString());
                args.putString(ARG_TEXT, mEditText.getText().toString());
                args.putString(ARG_TITLE, mEditTitle.getText().toString());
                args.putInt(ARG_ICON, mIconId);

                startLoader(FormFragment.this, args);
            }
        });

        // find the cancel button and add a click listener
        ImageButton cancel = (ImageButton) v.findViewById(R.id.button_cancel);
        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                stopLoader();
                hideKeyboard();
                clearForm();
                if(mTopicCache != null)
                    setIsNewPost(mTopicCache);
                ((BaseActivity) getActivity()).closeRightSidebar();
            }
        });

        ImageButton icon = (ImageButton) v.findViewById(R.id.button_icon);
        icon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                IconSelection id = new IconSelection();
                id.setTargetFragment(FormFragment.this, 0);
                id.show(getBaseActivity().getSupportFragmentManager(), "icondialog");
            }
        });

        mTitle = (TextView) v.findViewById(R.id.text_form_title);
        mEditText = (EditText) v.findViewById(R.id.edit_content);
        mEditTitle = (EditText) v.findViewById(R.id.edit_title);
        mEditRcpt = (EditText) v.findViewById(R.id.edit_rcpt);
        mIcon = (ImageButton) v.findViewById(R.id.button_icon);

        return v;
    }

    public void setIcon(Bitmap bitmap, int icon_id) {
        mIconId = icon_id;
        mIcon.setImageBitmap(bitmap);
    }

    /**
     * Prepare the form as a new post form
     *
     * @param topic The topic Iobject in which to post
     */
    public void setIsNewPost(Topic topic) {
        clearForm();

        Bundle args = new Bundle();
        args.putInt(ARG_MODE, MODE_REPLY);
        args.putInt(ARG_TOPIC_ID, topic.getId());
        args.putString(ARG_TOKEN, topic.getNewreplytoken());
        mTopicCache = topic;
        setFormArguments(args);

        mTitle.setText(getString(R.string.write_answer));
    }

    /**
     * Prepare the form to be an edit post form.
     *
     * @param topic The topic in which to post
     * @param post The post to edit
     */
    public void setIsEditPost(Topic topic, Post post) {
        clearForm();

        Bundle args = new Bundle();
        args.putInt(ARG_MODE, MODE_EDIT);
        args.putInt(ARG_TOPIC_ID, topic.getId());
        args.putInt(ARG_POST_ID, post.getId());
        args.putString(ARG_TOKEN, post.getEdittoken());
        setFormArguments(args);

        mTitle.setText(getString(R.string.edit_post));
    }

    /**
     * Prepare the form as a new PM form.
     *
     * @param message The message object if this is a reply or null if new PM
     */
    public void setIsMessage(Message message) {
        clearForm();

        Bundle args = new Bundle();
        args.putInt(ARG_MODE, MODE_MESSAGE);
        setFormArguments(args);

        // show the recipient field
        mEditRcpt.setVisibility(View.VISIBLE);
        mIcon.setVisibility(View.GONE);

        mTitle.setText(getString(R.string.write_message));

        // when the message object is given, fill in the recipient,
        // subject and message
        if (message != null) {
            // recipient
            mEditRcpt.setText(message.getFrom().getNick());

            // title with or without Re: prefix
            String prefix = "";
            if (!message.getTitle().substring(0, 3).equals("Re:"))
                prefix = "Re: ";
            mEditTitle.setText(prefix + message.getTitle());

            // read in the message string as HTML, so <br> is converted into line
            // breaks and so on.
            BufferedReader bufReader = new BufferedReader(new StringReader(
                    Html.fromHtml(message.getText()).toString()));

            // we need to build the message text using a string builder, so
            // we can prefix each line with a > (for quotes)
            String line;
            StringBuilder content = new StringBuilder();

            String ds = new SimpleDateFormat(getString(R.string.standard_time_format)).format
                    (message.getDate());
            String quote_line = "> %1$s \n";
            content.append(String.format(getString(R.string.message_header),
                    message.getFrom().getNick(), ds));
            try {
                while ((line = bufReader.readLine()) != null)
                    content.append(String.format(quote_line, line));
            } catch (IOException e) {
                // this will never occur.
            }

            // finally set the text and go to the beginning of the edit text
            mEditText.setText(content.toString());
            mEditText.setSelection(0);
        }
    }

    /**
     * This appends a given to the form text field.
     *
     * @param text The text to append
     */
    public void appendText(String text) {
        mEditText.append(text);
    }

    // set the arguments for the FormFragment
    protected void setFormArguments(Bundle args) {
        mFormArguments = args;
    }

    @Override
    public Loader<Bundle> onCreateLoader(int i, Bundle args) {

        showLoadingAnimation();

        if (mFormArguments.getInt(ARG_MODE) == MODE_MESSAGE)
            return new AsyncMessageSubmitter(getBaseActivity(), args);
        else
            return new AsyncPostSubmitter(getBaseActivity(), args);
    }

    @Override
    public void onLoadFinished(Loader<Bundle> sender, Bundle result) {
        hideLoadingAnimation();
        clearForm();

        if (result.getInt(STATUS, STATUS_FAILED) == STATUS_SUCCESS) {
            if(mCallback != null)
                mCallback.onSuccess(result);
        } else {
            if(mCallback != null)
                mCallback.onFailure(result);
        }
    }

    public void clearForm() {
        mEditText.setText("");
        mEditTitle.setText("");
        mTopicId = 0;
        mPostId = 0;
        mIcon.setImageResource(R.drawable.dark_navigation_cancel);
        mToken = "";
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getBaseActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    @Override
    public void onLoaderReset(Loader<Bundle> sender) {
        hideLoadingAnimation();
    }

    /**
     * We have a different loading animation in this fragment.
     */
    @Override
    public void showLoadingAnimation() {
        getView().findViewById(R.id.send_progress).setVisibility(View.VISIBLE);
    }

    /**
     * We have a different loading animation in this fragment.
     */
    @Override
    public void hideLoadingAnimation() {
        try {
            getView().findViewById(R.id.send_progress).setVisibility(View.INVISIBLE);
        } catch (NullPointerException e) {
            // the view was already detached. Never mind...
        }
    }

    /**
     * Set the icon by providing its id
     * @param iconId the icon id
     */
    public void setIconById(Integer iconId) {

        Bitmap icon;
        try {
            icon = Utils.getBitmapIcon(getActivity(), iconId);
            Bitmap bm = Bitmap.createScaledBitmap(icon, 80, 80, true);
            mIcon.setImageBitmap(bm);
            mIconId = iconId;
        } catch (IOException e) {}
    }

    static class AsyncPostSubmitter extends AsyncHttpLoader<Bundle>
    {

        protected int mMode;

        AsyncPostSubmitter(Context cx, Bundle args) {
            super(cx, Network.BOARD_URL_POST, AsyncHttpLoader.POST);

            mMode = args.getInt(ARG_MODE);

            EncodingRequestParams r = new EncodingRequestParams();

            r.setEncoding(Network.ENCODING_ISO);

            // this must resemble the same form on the website
            r.add("message", args.getString(ARG_TEXT));
            r.add("submit", "Eintragen");
            r.add("TID", "" + args.getInt(ARG_TOPIC_ID));
            r.add("token", args.getString(ARG_TOKEN));

            if (mMode == MODE_EDIT) {
                r.add("PID", "" + args.getInt(ARG_POST_ID));
                r.add("edit_title", args.getString(ARG_TITLE));
                r.add("edit_icon", new Integer(args.getInt(ARG_ICON)).toString());
                r.add("edit_converturls", "1");
                setUrl(Network.BOARD_URL_EDITPOST);
            } else if (mMode == MODE_REPLY) {
                r.add("SID", "");
                r.add("PID", "");
                r.add("post_title", args.getString(ARG_TITLE));
                r.add("post_icon", new Integer(args.getInt(ARG_ICON)).toString());
                r.add("post_converturls", "1");
                setUrl(Network.BOARD_URL_POST);
            }

            setParams(r);

        }

        @Override
        public Bundle processNetworkResponse(String response) {
            // check if the correct success website is displayed

            Pattern pattern = Pattern.compile("thread.php\\?TID=([0-9]+)&temp=[0-9]+&PID=([0-9]+)");
            Matcher m = pattern.matcher(response);

            Bundle result = new Bundle();
            result.putInt(ARG_MODE, mMode);

            if (m.find()) {
                result.putInt(STATUS, FormFragment.STATUS_SUCCESS);
                result.putInt(ARG_POST_ID, Integer.parseInt(m.group(2)));
            } else {
                result.putInt(STATUS, FormFragment.STATUS_FAILED);
            }

            return result;
        }
    }

    static class AsyncMessageSubmitter extends AsyncHttpLoader<Bundle>
    {

        protected int mMode;

        AsyncMessageSubmitter(Context cx, Bundle args) {
            super(cx, MessageParser.SEND_URL, AsyncHttpLoader.POST);

            mMode = args.getInt(ARG_MODE);

            EncodingRequestParams r = new EncodingRequestParams();

            r.setEncoding(Network.ENCODING_ISO);

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
            // check if the correct success website is displayed

            Pattern pattern = Pattern.compile("Nachricht wird an");
            Matcher m = pattern.matcher(response);

            Bundle result = new Bundle();
            result.putInt(ARG_MODE, mMode);

            if (m.find()) {
                result.putInt(STATUS, FormFragment.STATUS_SUCCESS);
            } else {
                result.putInt(STATUS, FormFragment.STATUS_FAILED);
            }

            return result;
        }
    }

    /**
     * The icon selection dialog
     */
    public static class IconSelection extends DialogFragment
    {
        private ArrayList<String> mIcons = new ArrayList<String>();

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final FormFragment fragment = ((FormFragment) getTargetFragment());

            // find all icons
            AssetManager aMan = getActivity().getAssets();
            try {
                mIcons.addAll(Arrays.asList(aMan.list("thread-icons")));
            } catch (IOException e) {}

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.icon_selection);
            builder.setAdapter(new IconListAdapter(getActivity()),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Bitmap icon;
                            try {
                                icon = Utils.getBitmapIcon(getActivity(), mIcons.get(which));
                                Bitmap bm = Bitmap.createScaledBitmap(icon, 80, 80, true);
                                Integer icon_id = Integer
                                        .parseInt(mIcons.get(which).substring(4).split("\\.")[0]);
                                fragment.setIcon(bm, icon_id);
                            } catch (IOException e) {}

                        }
                    });
            return builder.create();
        }

        /**
         * Custom view adapter for the ListView items
         */
        public class IconListAdapter extends ArrayAdapter<String>
        {
            Activity context;

            IconListAdapter(Activity context) {
                super(context, R.layout.listitem_icon, R.id.name, mIcons);
                this.context = context;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = context.getLayoutInflater();
                View row = inflater.inflate(R.layout.listitem_icon, null);
                String icon = mIcons.get(position);

                TextView name = (TextView) row.findViewById(R.id.name);
                name.setText(icon);

                try {
                    Drawable dr = Utils.getIcon(getActivity(), icon);
                    dr.setBounds(0,0,20*(int)((FormFragment) getTargetFragment()).mDensity,
                            20*(int)((FormFragment) getTargetFragment()).mDensity);
                    name.setCompoundDrawables(dr, null, null, null);

                } catch (IOException e) { }


                return (row);
            }
        }
    }



}
