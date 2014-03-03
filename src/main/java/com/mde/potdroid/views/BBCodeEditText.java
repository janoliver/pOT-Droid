package com.mde.potdroid.views;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.SettingsWrapper;

import java.util.ArrayList;

/**
 * Created by oli on 2/24/14.
 */
public class BBCodeEditText extends EditText
{

    protected ActionMode mActionMode;
    protected ActionBarActivity mActivity;

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
    }

    public BBCodeEditText(Context context) {
        super(context);
        init();
    }

    public BBCodeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BBCodeEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void disable() {
        setOnClickListener(null);
        setOnFocusChangeListener(null);
    }

    public void init() {
        mActivity = ((ActionBarActivity)getContext());
        SettingsWrapper s = new SettingsWrapper(getContext());

        if(!s.isBBCodeEditor())
            return;

        setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (mActionMode == null)
                        mActionMode = mActivity.startSupportActionMode(new StyleCallback());

                } else {
                    if (mActionMode != null)
                        mActionMode.finish();
                }
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActionMode == null)
                    mActionMode = mActivity.startSupportActionMode(new StyleCallback());
            }
        });
    }

    class StyleCallback implements ActionMode.Callback, IconSelectionDialog.IconSelectedCallback,
        PromptDialog.SuccessCallback{

        protected BBCodeEditText mEditText;

        public StyleCallback() {
            mEditText = BBCodeEditText.this;
        }


        @Override
        public void selected(String filename, String smiley) {
            mEditText.getText().insert(mEditText.getSelectionStart(), smiley);
        }
          
        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.bbcode_menu, menu);

            menu.findItem(R.id.bold).setIcon(IconDrawable.getIconDrawable(getContext(), R.string.icon_bold));
            menu.findItem(R.id.italic).setIcon(IconDrawable.getIconDrawable(getContext(), R.string.icon_italic));
            menu.findItem(R.id.striked).setIcon(IconDrawable.getIconDrawable(getContext(), R.string.icon_strikethrough));
            menu.findItem(R.id.underline).setIcon(IconDrawable.getIconDrawable(getContext(), R.string.icon_underline));
            menu.findItem(R.id.quote).setIcon(IconDrawable.getIconDrawable(getContext(), R.string.icon_quote_left));
            menu.findItem(R.id.code).setIcon(IconDrawable.getIconDrawable(getContext(), R.string.icon_code));
            menu.findItem(R.id.spoiler).setIcon(IconDrawable.getIconDrawable(getContext(), R.string.icon_eye_close));
            menu.findItem(R.id.image).setIcon(IconDrawable.getIconDrawable(getContext(), R.string.icon_picture));
            menu.findItem(R.id.url).setIcon(IconDrawable.getIconDrawable(getContext(), R.string.icon_link));
            menu.findItem(R.id.list).setIcon(IconDrawable.getIconDrawable(getContext(), R.string.icon_list_ol));
            menu.findItem(R.id.smiley).setIcon(IconDrawable.getIconDrawable(getContext(), R.string.icon_smile));

            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int start = mEditText.getSelectionStart();
            int end = mEditText.getSelectionEnd();
            if(mSimpleTags.indexOfKey(item.getItemId()) > -1) {
                String code = mSimpleTags.get(item.getItemId());
                mEditText.getText().insert(start, String.format("[%s]", code));
                mEditText.getText().insert(end + 2 + code.length(), String.format("[/%s]", code));
                mEditText.setSelection(start + 2 + code.length());
                return true;
            } else {
                PromptDialog d;
                switch (item.getItemId()) {
                    case R.id.smiley:
                        IconSelectionDialog id = new IconSelectionDialog(true);
                        id.setCallback(this);
                        id.show(mActivity.getSupportFragmentManager(), "icondialog");
                        return true;
                    case R.id.image:
                        d = new PromptDialog("Bild einfügen", "URL...", R.id.image);
                        d.setCallback(this);
                        d.show(mActivity.getSupportFragmentManager(), "imgedialog");
                        return true;
                    case R.id.list:
                        d = new PromptDialog("Liste einfügen", 5, new String[] {"a, 1, leer", "Item...", "Item..."}, true, R.id.list);
                        d.setCallback(this);
                        d.show(mActivity.getSupportFragmentManager(), "listdialog");
                        return true;
                    case R.id.url:
                        d = new PromptDialog("Link einfügen", 2, new String[] {"Text", "URL..."}, false, R.id.url);
                        d.setCallback(this);
                        d.show(mActivity.getSupportFragmentManager(), "linkdialog");

                    default:
                        return false;
                }

            }


        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mEditText.requestFocus();
            mActionMode = null;
        }

        @Override
        public void success(ArrayList<String> input, int code) {
            String insert;
            switch (code) {
                case R.id.image:
                    insert = String.format("[img]%s[/img]", input.get(0));
                    mEditText.getText().insert(mEditText.getSelectionStart(), insert);
                    return;
                case R.id.url:
                    if(input.get(0).equals(""))
                        insert = String.format("[url]%s[/url]", input.get(1));
                    else
                        insert = String.format("[url=%s]%s[/url]", input.get(1), input.get(0));
                    mEditText.getText().insert(mEditText.getSelectionStart(), insert);
                    return;
                case R.id.list:
                    StringBuilder result = new StringBuilder();

                    if(input.get(0).toLowerCase().equals("a") || input.get(0).equals("1"))
                        result.append(String.format("[list=%s]", input.get(0)));
                    else
                        result.append("[list]");
                    for(String s : input) {
                        if(!s.equals(""))
                            result.append(String.format("[*] %s\n", s));
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