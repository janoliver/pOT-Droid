package com.mde.potdroid.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.mde.potdroid.R;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.helpers.FormEncodingBuilder;
import com.mde.potdroid.helpers.Network;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.parsers.MessageParser;
import com.mde.potdroid.views.IconSelectionDialog;
import com.mde.potdroid.views.PromptDialog;

import org.apache.http.Header;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the fragment responsible for the Forms, both the post editing/writing and the
 * PM message form. Activities must implement the FormListener, to be notified on
 * finishing, success and failure of the form.
 */
public class EditorFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Bundle>,
        IconSelectionDialog.IconSelectedCallback {


    public static final String BOARD_URL_POST = "newreply.php";
    public static final String BOARD_URL_THREAD = "newthread.php";
    public static final String BOARD_URL_EDITPOST = "editreply.php";
    protected static final String ARG_CLOSED = "closed";
    protected static final String ARG_MODE = "mode";
    protected static final String ARG_TOPIC_ID = "topic_id";
    protected static final String ARG_POST_ID = "post_id";
    protected static final String ARG_BOARD_ID = "baord_id";
    protected static final String ARG_TOKEN = "token";
    protected static final String ARG_ICON = "icon";
    protected static final String ARG_RCPT = "rcpt";
    protected static final String ARG_SUBTITLE = "subtitle";
    protected static final String ARG_TAGS = "tags";
    protected static final String ARG_TEXT = "text";
    protected static final String ARG_TITLE = "title";
    protected static final String ARG_STATUS = "status";
    // this holds the kind of form this is. The static fields are defined below.
    public static int MODE_EDIT = 1;
    public static int MODE_REPLY = 2;
    public static int MODE_MESSAGE = 3;
    public static int MODE_THREAD = 4;
    protected static SparseArray<String> mSimpleTags;

    static {
        mSimpleTags = new SparseArray<String>();
        mSimpleTags.append(R.id.bold, "b");
        mSimpleTags.append(R.id.underline, "u");
        mSimpleTags.append(R.id.italic, "i");
        mSimpleTags.append(R.id.striked, "s");
        mSimpleTags.append(R.id.code, "code");
        mSimpleTags.append(R.id.quote, "quote");
        mSimpleTags.append(R.id.spoiler, "spoiler");
        mSimpleTags.append(R.id.tex, "tex");
    }

    // the input title and text views
    protected EditText mEditRcpt;
    protected EditText mEditTitle;
    protected EditText mEditSubtitle;
    protected EditText mEditTags;
    protected EditText mEditText;
    protected ImageButton mIconButton;
    protected ImageButton mBBButton;
    // the array of icons
    private int mIconId;

    private ActionMenuView mBBToolbarView;

    /**
     * * Return new instance of FormFragment. Although this fragment has no parameters,
     * We provide this method for consistency.
     *
     * @return FormFragment
     */
    public static EditorFragment newInstance(Bundle args) {
        EditorFragment ef = new EditorFragment();
        ef.setArguments(args);
        return ef;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_editor, container, false);

        // assign some views
        mEditText = (EditText) v.findViewById(R.id.edit_content);
        mEditTitle = (EditText) v.findViewById(R.id.edit_title);
        mEditRcpt = (EditText) v.findViewById(R.id.edit_rcpt);
        mEditSubtitle = (EditText) v.findViewById(R.id.edit_subtitle);
        mEditTags = (EditText) v.findViewById(R.id.edit_tags);
        mIconButton = (ImageButton) v.findViewById(R.id.button_icon);
        mBBButton = (ImageButton) v.findViewById(R.id.button_bb);

        mIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IconSelectionDialog id = IconSelectionDialog.newInstance(false);
                id.setCallback(EditorFragment.this);
                id.show(getBaseActivity().getSupportFragmentManager(), "icondialog");
            }
        });


        // fill the form
        if (getArguments().getString(ARG_TEXT) != null)
            mEditText.setText(Utils.toUnicodeEmojis(getArguments().getString(ARG_TEXT)));

        if (getArguments().getString(ARG_TITLE) != null)
            mEditTitle.setText(getArguments().getString(ARG_TITLE));

        if (getArguments().getInt(ARG_ICON, 0) > 0)
            setIconById(getArguments().getInt(ARG_ICON));

        if (getArguments().getString(ARG_RCPT) != null)
            mEditRcpt.setText(getArguments().getString(ARG_RCPT));

        final HorizontalScrollView bbcodeToolbarHolder = (HorizontalScrollView) v.findViewById(R.id.bbcode_toolbar_holder);
        mBBToolbarView = (ActionMenuView) v.findViewById(R.id.bbcode_toolbar);

        Menu menu = mBBToolbarView.getMenu();
        getActivity().getMenuInflater().inflate(R.menu.bbcode_menu, menu);

        mBBToolbarView.setOnMenuItemClickListener(new BBCodeHandler(getBaseActivity(), mEditText));

        if (!mSettings.isBBCodeEditor()) {
            bbcodeToolbarHolder.setVisibility(View.GONE);
        }

        mBBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bbcodeToolbarHolder.getVisibility() == View.GONE)
                    bbcodeToolbarHolder.setVisibility(View.VISIBLE);
                else
                    bbcodeToolbarHolder.setVisibility(View.GONE);
            }
        });

        // set the title
        if (getArguments().getInt(ARG_MODE, MODE_REPLY) == MODE_REPLY)
            getActionbar().setTitle(R.string.subtitle_form_write_post);
        else if (getArguments().getInt(ARG_MODE, MODE_REPLY) == MODE_EDIT)
            getActionbar().setTitle(R.string.subtitle_form_edit_post);
        else if (getArguments().getInt(ARG_MODE, MODE_REPLY) == MODE_MESSAGE) {
            mIconButton.setVisibility(View.GONE);
            mBBButton.setVisibility(View.GONE);
            mEditRcpt.setVisibility(View.VISIBLE);
            mBBToolbarView.setVisibility(View.GONE);
            getActionbar().setTitle(R.string.subtitle_form_write_pm);
        } else if (getArguments().getInt(ARG_MODE, MODE_REPLY) == MODE_THREAD) {
            mEditSubtitle.setVisibility(View.VISIBLE);
            mEditTags.setVisibility(View.VISIBLE);
            getActionbar().setTitle(R.string.subtitle_form_write_thread);
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getArguments().getBoolean(ARG_CLOSED, false))
            showInfo(R.string.msg_topic_closed);

        mEditText.requestFocus();

        hideLoadingAnimation();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_editor, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.send:
                hideKeyboard();

                Bundle args = new Bundle(getArguments());
                args.putString(ARG_RCPT, mEditRcpt.getText().toString());
                args.putString(ARG_SUBTITLE, mEditSubtitle.getText().toString());
                args.putString(ARG_TAGS, mEditTags.getText().toString());
                args.putString(ARG_TEXT, Utils.fromUnicodeEmojis(mEditText.getText().toString()));
                args.putString(ARG_TITLE, mEditTitle.getText().toString());
                args.putInt(ARG_ICON, mIconId);

                startLoader(EditorFragment.this, args);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Bundle> onCreateLoader(int i, Bundle args) {

        showLoadingAnimation();


        if (getArguments().getInt(ARG_MODE) == MODE_MESSAGE)
            return new AsyncMessageSubmitter(getBaseActivity(), args);
        else if (getArguments().getInt(ARG_MODE) == MODE_REPLY ||
                getArguments().getInt(ARG_MODE) == MODE_EDIT)
            return new AsyncPostSubmitter(getBaseActivity(), args);
        else if (getArguments().getInt(ARG_MODE) == MODE_THREAD)
            return new AsyncThreadSubmitter(getBaseActivity(), args);

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Bundle> sender, Bundle result) {
        hideLoadingAnimation();

        if (result != null) {
            Intent intent = new Intent();
            intent.putExtras(result);
            getActivity().setResult(result.getInt(ARG_STATUS, Activity.RESULT_CANCELED), intent);
            getActivity().finish();
        } else {
            showError(getString(R.string.msg_posting_error));
        }
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
     * Set the icon by providing its id
     *
     * @param iconId the icon id
     */
    public void setIconById(Integer iconId) {
        if (iconId < 0) {
            mIconId = 0;
            mIconButton.setImageResource(R.drawable.ic_smiley);
        } else {
            Bitmap icon;
            try {
                int size = (int) (mEditTitle.getTextSize() * 1.5);
                icon = Utils.getBitmapIcon(getActivity(), iconId);
                Bitmap bm = Bitmap.createScaledBitmap(icon, size, size, true);
                mIconButton.setImageBitmap(bm);
                mIconId = iconId;
            } catch (IOException e) {
                // nothing.
            }
        }
    }

    @Override
    public void selected(String filename, String smiley) {
        try {

            Integer icon_id = Integer
                    .parseInt(filename.substring(4).split("\\.")[0]);
            setIconById(icon_id);
        } catch (NumberFormatException e) {
            setIconById(-1);
        }
    }

    static class AsyncPostSubmitter extends AsyncHttpLoader<Bundle> {

        protected int mMode;

        AsyncPostSubmitter(Context cx, Bundle args) {
            super(cx, BOARD_URL_POST, AsyncHttpLoader.POST);

            mMode = args.getInt(ARG_MODE);

            FormEncodingBuilder r = new FormEncodingBuilder(Network.ENCODING_ISO);

            // this must resemble the same form on the website
            r.add("message", args.getString(ARG_TEXT));
            r.add("submit", "Eintragen");
            r.add("TID", new Integer(args.getInt(ARG_TOPIC_ID)).toString());
            r.add("token", args.getString(ARG_TOKEN));

            if (mMode == MODE_EDIT) {
                r.add("PID", "" + args.getInt(ARG_POST_ID));
                r.add("edit_title", args.getString(ARG_TITLE));
                r.add("edit_icon", new Integer(args.getInt(ARG_ICON)).toString());
                r.add("edit_converturls", "1");
                setUrl(BOARD_URL_EDITPOST);
            } else if (mMode == MODE_REPLY) {
                r.add("SID", "");
                r.add("PID", "");
                r.add("post_title", args.getString(ARG_TITLE));
                r.add("post_icon", new Integer(args.getInt(ARG_ICON)).toString());
                r.add("post_converturls", "1");
                setUrl(BOARD_URL_POST);
            }

            setParams(r.build());

        }

        @Override
        public Bundle processNetworkResponse(String response) {
            // check if the correct success website is displayed

            Pattern pattern = Pattern.compile("thread.php\\?TID=([0-9]+)&temp=[0-9]+&PID=([0-9]+)");
            Matcher m = pattern.matcher(response);

            Bundle result = new Bundle();
            result.putInt(ARG_MODE, mMode);

            if (m.find()) {
                result.putInt(ARG_STATUS, Activity.RESULT_OK);
                result.putInt(ARG_POST_ID, Integer.parseInt(m.group(2)));
            } else {
                result.putInt(ARG_STATUS, Activity.RESULT_CANCELED);
            }

            return result;
        }

        @Override
        protected void onNetworkFailure(int statusCode, Header[] headers,
                                        String responseBody, Throwable error) {
            Utils.printException(error);
            deliverResult(null);
        }
    }

    static class AsyncThreadSubmitter extends AsyncHttpLoader<Bundle> {

        protected int mMode;

        AsyncThreadSubmitter(Context cx, Bundle args) {
            super(cx, BOARD_URL_THREAD, AsyncHttpLoader.POST);

            mMode = args.getInt(ARG_MODE);

            FormEncodingBuilder r = new FormEncodingBuilder(Network.ENCODING_ISO);

            // this must resemble the same form on the website
            r.add("message", args.getString(ARG_TEXT));
            r.add("submit", "Eintragen");
            r.add("BID", new Integer(args.getInt(ARG_BOARD_ID)).toString());
            r.add("thread_subtitle", args.getString(ARG_SUBTITLE));
            r.add("thread_title", args.getString(ARG_TITLE));
            r.add("thread_icon", new Integer(args.getInt(ARG_ICON)).toString());
            r.add("thread_converturls", "1");
            r.add("thread_tags", args.getString(ARG_TAGS));
            r.add("token", args.getString(ARG_TOKEN));

            setParams(r.build());

        }

        @Override
        public Bundle processNetworkResponse(String response) {
            // check if the correct success website is displayed

            Pattern pattern = Pattern.compile("thread.php\\?TID=([0-9]+)");
            Matcher m = pattern.matcher(response);

            Bundle result = new Bundle();
            result.putInt(ARG_MODE, mMode);

            if (m.find()) {
                result.putInt(ARG_STATUS, Activity.RESULT_OK);
                result.putInt(ARG_TOPIC_ID, Integer.parseInt(m.group(1)));
            } else {
                result.putInt(ARG_STATUS, Activity.RESULT_CANCELED);
            }

            return result;
        }

        @Override
        protected void onNetworkFailure(int statusCode, Header[] headers,
                                        String responseBody, Throwable error) {

            Utils.printException(error);
            deliverResult(null);
        }
    }

    static class AsyncMessageSubmitter extends AsyncHttpLoader<Bundle> {

        protected int mMode;

        AsyncMessageSubmitter(Context cx, Bundle args) {
            super(cx, MessageParser.SEND_URL, AsyncHttpLoader.POST);

            mMode = args.getInt(ARG_MODE);

            FormEncodingBuilder r = new FormEncodingBuilder(Network.ENCODING_ISO);

            r.add("rcpts", "0");
            r.add("rcpt", args.getString("rcpt"));
            r.add("subj", args.getString("title"));
            r.add("msg", args.getString("text"));
            r.add("mf_sc", "1");
            r.add("submit", "Senden");

            setParams(r.build());

        }

        @Override
        public Bundle processNetworkResponse(String response) {
            // check if the correct success website is displayed

            Pattern pattern = Pattern.compile("Nachricht wird an");
            Matcher m = pattern.matcher(response);

            Bundle result = new Bundle();
            result.putInt(ARG_MODE, mMode);

            if (m.find()) {
                result.putInt(ARG_STATUS, Activity.RESULT_OK);
            } else {
                result.putInt(ARG_STATUS, Activity.RESULT_CANCELED);
            }

            return result;
        }

        @Override
        protected void onNetworkFailure(int statusCode, Header[] headers,
                                        String responseBody, Throwable error) {

            Utils.printException(error);
            deliverResult(null);
        }


    }

    public static class BBCodeHandler implements ActionMenuView.OnMenuItemClickListener, PromptDialog.SuccessCallback, IconSelectionDialog.IconSelectedCallback {
        private EditText mEditText;
        private AppCompatActivity mActivity;

        public BBCodeHandler(AppCompatActivity activity, EditText edittext) {
            mEditText = edittext;
            mActivity = activity;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int start = mEditText.getSelectionStart();
            int end = mEditText.getSelectionEnd();
            if (mSimpleTags.indexOfKey(item.getItemId()) > -1) {
                String code = mSimpleTags.get(item.getItemId());
                mEditText.getText().insert(start, String.format("[%s]", code));
                mEditText.getText().insert(end + 2 + code.length(), String.format("[/%s]", code));
                mEditText.setSelection(start + 2 + code.length());
                return true;
            } else {
                PromptDialog d;
                switch (item.getItemId()) {
                    case R.id.smiley:
                        IconSelectionDialog id = IconSelectionDialog.newInstance(true);
                        id.setCallback(this);
                        id.show(mActivity.getSupportFragmentManager(), "icondialog");
                        return true;
                    case R.id.image:
                        d = PromptDialog.newInstance("Bild einfügen", "URL...", R.id.image);
                        d.setCallback(this);
                        d.show(mActivity.getSupportFragmentManager(), "imgedialog");
                        return true;
                    case R.id.video:
                        d = PromptDialog.newInstance("Video einfügen", "URL...", R.id.video);
                        d.setCallback(this);
                        d.show(mActivity.getSupportFragmentManager(), "videodialog");
                        return true;
                    case R.id.list:
                        d = PromptDialog.newInstance("Liste einfügen", 3, new String[]{"a, 1, leer", "Item...", "Item..."}, true, R.id.list);
                        d.setCallback(this);
                        d.show(mActivity.getSupportFragmentManager(), "listdialog");
                        return true;
                    case R.id.url:
                        d = PromptDialog.newInstance("Link einfügen", 2, new String[]{"Text", "URL..."}, false, R.id.url);
                        d.setCallback(this);
                        d.show(mActivity.getSupportFragmentManager(), "linkdialog");

                    default:
                        return false;
                }

            }
        }

        @Override
        public void selected(String filename, String smiley) {

            mEditText.getText().insert(mEditText.getSelectionStart(), smiley);
        }

        @Override
        public void success(ArrayList<String> input, int code) {
            String insert;
            switch (code) {
                case R.id.image:
                    insert = String.format("[img]%s[/img]", input.get(0));
                    mEditText.getText().insert(mEditText.getSelectionStart(), insert);
                    return;
                case R.id.video:
                    insert = String.format("[video]%s[/video]", input.get(0));
                    mEditText.getText().insert(mEditText.getSelectionStart(), insert);
                    return;
                case R.id.url:
                    if (input.get(0).equals(""))
                        insert = String.format("[url]%s[/url]", input.get(1));
                    else
                        insert = String.format("[url=%s]%s[/url]", input.get(1), input.get(0));
                    mEditText.getText().insert(mEditText.getSelectionStart(), insert);
                    return;
                case R.id.list:
                    StringBuilder result = new StringBuilder();

                    if (input.get(0).toLowerCase().equals("a") || input.get(0).equals("1"))
                        result.append(String.format("[list=%s]", input.get(0)));
                    else
                        result.append("[list]");
                    for (int i = 1; i < input.size(); ++i) {
                        if (!input.get(i).equals(""))
                            result.append(String.format("[*] %s\n", input.get(i)));
                    }
                    result.append("[/list]");
                    mEditText.getText().insert(mEditText.getSelectionStart(), result);
                    return;
                default:
                    return;
            }
        }
    }

}
