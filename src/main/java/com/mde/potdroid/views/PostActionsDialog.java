package com.mde.potdroid.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import com.mde.potdroid.R;
import com.mde.potdroid.fragments.TopicFragment;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;

import java.util.HashMap;
import java.util.List;

/**
 * This DialogFragment shows a Menu for a Post with some actions
 */
public class PostActionsDialog extends DialogFragment {

    public static final String ARG_POST_ID = "post_id";
    public static final String TAG = "postmenu";

    public static PostActionsDialog getInstance(int post_id) {
        PostActionsDialog f = new PostActionsDialog();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt(ARG_POST_ID, post_id);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final TopicFragment fragment = (TopicFragment) getTargetFragment();

        // get the menu items
        final String[] post_menu = getResources().getStringArray(R.array.post_dialog);

        LayoutInflater inflater = fragment.getBaseActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getBaseActivity());
        final View dialog_view = inflater.inflate(R.layout.dialog_post_actions, null);
        /*builder.setView(dialog_view)
                .setTitle(R.string.dialog_post_actions)
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });*/
        builder.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, post_menu) {

            @Override
            public boolean isEnabled(int position) {
                switch (position) {
                    case 0:
                        SettingsWrapper settings = new SettingsWrapper(getActivity());
                        return Utils.isLoggedIn() && fragment.getTopic()
                                .getPostById(getArguments().getInt(ARG_POST_ID))
                                .getAuthor()
                                .getId() == settings.getUserId();
                    case 1:
                        return Utils.isLoggedIn();
                    case 2:
                        return Utils.isLoggedIn();
                    case 4:
                        return Utils.isLoggedIn();
                }
                return true;
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        fragment.editPost(getArguments().getInt(ARG_POST_ID));
                        break;
                    case 1:
                        fragment.quotePost(getArguments().getInt(ARG_POST_ID));
                        break;
                    case 2:
                        //fragment.bookmarkPost(getArguments().getInt(ARG_POST_ID), d);
                        break;
                    case 3:
                        fragment.linkPost(getArguments().getInt(ARG_POST_ID));
                        break;
                    case 4:
                        fragment.pmToAuthor(getArguments().getInt(ARG_POST_ID));
                        break;
                }
            }
        });

        // Create the AlertDialog object and return it
        final Dialog d = builder.create();
/*
        IconButton quote_button = (IconButton) dialog_view.findViewById(R.id.button_quote);
        quote_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.quotePost(getArguments().getInt(ARG_POST_ID));
                d.cancel();
            }
        });

        IconButton edit_button = (IconButton) dialog_view.findViewById(R.id.button_edit);
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.editPost(getArguments().getInt(ARG_POST_ID));
                d.cancel();
            }
        });

        IconButton bookmark_button = (IconButton) dialog_view.findViewById(R.id
                .button_bookmark);
        bookmark_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.bookmarkPost(getArguments().getInt(ARG_POST_ID), d);
            }
        });

        IconButton url_button = (IconButton) dialog_view.findViewById(R.id.button_link);
        url_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.linkPost(getArguments().getInt(ARG_POST_ID));
                d.cancel();
            }
        });

        IconButton pm_button = (IconButton) dialog_view.findViewById(R.id.button_pm);
        pm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.pmToAuthor(getArguments().getInt(ARG_POST_ID));
                d.cancel();
            }
        });

        // disable the buttons if the user is not logged in
        if (!Utils.isLoggedIn()) {
            quote_button.disable();
            edit_button.disable();
            bookmark_button.disable();
            pm_button.disable();
        }
*/

        return d;
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}