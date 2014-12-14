package com.mde.potdroid.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import com.mde.potdroid.R;
import com.mde.potdroid.fragments.TopicFragment;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;

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
                        fragment.bookmarkPost(getArguments().getInt(ARG_POST_ID), null);
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


        return d;
    }

}