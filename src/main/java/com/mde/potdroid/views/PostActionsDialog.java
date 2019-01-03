package com.mde.potdroid.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.R;
import com.mde.potdroid.fragments.TopicFragment;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.Utils;

import java.util.ArrayList;
import java.util.Arrays;

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

        final MaterialDialog dialog = new MaterialDialog.Builder(fragment.getBaseActivity())
                .items(post_menu)
                .title(R.string.post_dialog_title)
                .adapter(new PostActionAdapter(post_menu, getActivity(), fragment), null)
                .build();

        return dialog;
    }


    /**
     * Custom view adapter for the ListView items
     */
    public class PostActionAdapter extends RecyclerView.Adapter<PostActionsDialog.PostActionAdapter.ViewHolder> {
        private ArrayList<String> mDataset;
        TopicFragment mTarget;
        SettingsWrapper mSettings;

        @Override
        public PostActionsDialog.PostActionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FrameLayout v = (FrameLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_icon, parent, false);

            return new PostActionsDialog.PostActionAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final PostActionsDialog.PostActionAdapter.ViewHolder holder, int position) {
            final String action = mDataset.get(position);

            holder.mText.setText(action);

            boolean isEnabled = true;
            switch (position) {
                case 0:
                    isEnabled = Utils.isLoggedIn() && mTarget.getTopic()
                            .getPostById(getArguments().getInt(ARG_POST_ID))
                            .getAuthor()
                            .getId() == mSettings.getUserId();
                    break;
                case 1:
                    isEnabled = Utils.isLoggedIn();
                    break;
                case 2:
                    isEnabled = Utils.isLoggedIn();
                    break;
                case 5:
                    isEnabled = Utils.isLoggedIn();
                    break;
            }

            holder.mText.setEnabled(isEnabled);


            if(isEnabled) {
                holder.mText.setTextColor(Utils.getColorByAttr(getContext(), R.attr.md_content_color));
                holder.mRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (holder.getAdapterPosition()) {
                            case 0:
                                mTarget.editPost(getArguments().getInt(ARG_POST_ID));
                                break;
                            case 1:
                                mTarget.quotePost(getArguments().getInt(ARG_POST_ID));
                                break;
                            case 2:
                                mTarget.bookmarkPost(getArguments().getInt(ARG_POST_ID), null);
                                break;
                            case 3:
                                mTarget.savePost(getArguments().getInt(ARG_POST_ID), null);
                                break;
                            case 4:
                                mTarget.linkPost(getArguments().getInt(ARG_POST_ID));
                                break;
                            case 5:
                                mTarget.clipboardPostUrl(getArguments().getInt(ARG_POST_ID));
                                break;
                            case 6:
                                mTarget.pmToAuthor(getArguments().getInt(ARG_POST_ID));
                                break;
                        }
                        getDialog().dismiss();
                    }
                });
            } else
                holder.mText.setTextColor(Utils.getColorByAttr(getContext(), R.attr.md_content_color_disabled));
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            FrameLayout mRoot;
            TextView mText;

            ViewHolder(FrameLayout container) {
                super(container);
                mRoot = container;
                mText = (TextView) mRoot.findViewById(R.id.name);
            }
        }

        PostActionAdapter(String[] items, Context ctx, TopicFragment target) {
            mDataset = new ArrayList<>(Arrays.asList(items));
            mSettings = new SettingsWrapper(ctx);
            mTarget = target;
        }

    }

}