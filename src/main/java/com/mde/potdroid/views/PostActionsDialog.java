package com.mde.potdroid.views;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.afollestad.materialdialogs.MaterialDialog;
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

        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(),
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
                    case 5:
                        return Utils.isLoggedIn();
                }
                return true;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setEnabled(isEnabled(position));
                return view;
            }
        };

        final MaterialDialog dialog = new MaterialDialog.Builder(fragment.getBaseActivity())
                .items(post_menu)
                .title(R.string.post_dialog_title)
                .adapter(adapter, null)
                .build();

        ListView listView = dialog.getListView();
        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dialog.dismiss();
                    switch (position) {
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
                            fragment.clipboardPostUrl(getArguments().getInt(ARG_POST_ID));
                            break;
                        case 5:
                            fragment.pmToAuthor(getArguments().getInt(ARG_POST_ID));
                            break;
                    }
                }
            });
        }

        return dialog;
    }

}