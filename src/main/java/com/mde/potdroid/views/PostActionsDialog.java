package com.mde.potdroid.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import com.mde.potdroid.R;
import com.mde.potdroid.fragments.TopicFragment;
import com.mde.potdroid.helpers.Utils;

/**
 * This DialogFragment shows a Menu for a Post with some actions
 */
public class PostActionsDialog extends DialogFragment
{

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

        LayoutInflater inflater = fragment.getBaseActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getBaseActivity());
        View dialog_view = inflater.inflate(R.layout.dialog_post_actions, null);
        builder.setView(dialog_view)
                .setTitle(R.string.post_actions)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        // Create the AlertDialog object and return it
        final Dialog d = builder.create();

        IconButton quote_button = (IconButton) dialog_view.findViewById(R.id.button_quote);
        quote_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                fragment.quotePost(getArguments().getInt(ARG_POST_ID));
                d.cancel();
            }
        });

        IconButton edit_button = (IconButton) dialog_view.findViewById(R.id.button_edit);
        edit_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                fragment.editPost(getArguments().getInt(ARG_POST_ID));
                d.cancel();
            }
        });

        IconButton bookmark_button = (IconButton) dialog_view.findViewById(R.id
                .button_bookmark);
        bookmark_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                fragment.bookmarkPost(getArguments().getInt(ARG_POST_ID), d);
            }
        });

        IconButton url_button = (IconButton) dialog_view.findViewById(R.id.button_link);
        url_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                fragment.linkPost(getArguments().getInt(ARG_POST_ID));
                d.cancel();
            }
        });

        // disable the buttons if the user is not logged in
        if(!Utils.isLoggedIn()) {
            quote_button.disable();
            edit_button.disable();
            bookmark_button.disable();
        }


        return d;
    }
}