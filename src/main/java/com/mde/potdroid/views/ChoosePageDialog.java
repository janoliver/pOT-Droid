package com.mde.potdroid.views;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.R;
import com.mde.potdroid.fragments.TopicFragment;

import java.util.ArrayList;

public class ChoosePageDialog extends DialogFragment {
    public static ChoosePageDialog getInstance(int pages) {
        ChoosePageDialog f = new ChoosePageDialog();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("pages", pages);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence[] items = new CharSequence[getArguments().getInt("pages")];
        for(int i=0; i < items.length; ++i) {
            items[i] = "Seite "+(i+1);
        }

        return new MaterialDialog.Builder(getActivity())
                .title(R.string.action_topage)
                .adapter(new ChoosePageDialog.PageListAdapter(), new GridLayoutManager(getActivity(), 4))
                .build();
    }

    public class PageListAdapter extends RecyclerView.Adapter<ChoosePageDialog.PageListAdapter.ViewHolder> {
        private ArrayList<String> mDataset = new ArrayList<String>();

        @Override
        public ChoosePageDialog.PageListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FrameLayout v = (FrameLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_number, parent, false);

            return new ChoosePageDialog.PageListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ChoosePageDialog.PageListAdapter.ViewHolder holder, final int position) {
            String icon = mDataset.get(position);

            holder.mText.setText(icon);

            holder.mRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((TopicFragment)getTargetFragment()).goToPage(position + 1);
                    getDialog().dismiss();
                }
            });
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

        PageListAdapter() {
            CharSequence[] items = new CharSequence[getArguments().getInt("pages")];
            for(int i=0; i < items.length; ++i) {
                mDataset.add(i, ""+(i+1));
            }
        }

    }
}