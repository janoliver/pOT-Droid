package com.mde.potdroid.views;

import android.app.Dialog;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.helpers.TopicBuilder;
import com.mde.potdroid.helpers.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

/**
 * The icon selection dialog
 */
public class IconSelectionDialog extends DialogFragment {
    private ArrayList<String> mIcons = new ArrayList<String>();
    private Boolean mIsSmileys;
    private IconSelectedCallback mCallback;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsSmileys = getArguments().getBoolean("smileys",false);
    }

    public static IconSelectionDialog newInstance(boolean smileys) {
        IconSelectionDialog f = new IconSelectionDialog();

        Bundle args = new Bundle();
        args.putBoolean("smileys", smileys);
        f.setArguments(args);

        return f;
    }


    public void setCallback(IconSelectedCallback callback) {
        mCallback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // find all icons
        AssetManager aMan = getActivity().getAssets();
        try {
            if(!mIsSmileys) {
                mIcons.add("Kein icon");
                mIcons.addAll(Arrays.asList(aMan.list("thread-icons")));
            } else {
                String list = "smileys";

                if (Calendar.getInstance().get(Calendar.MONTH) == Calendar.DECEMBER) {
                    SettingsWrapper s = new SettingsWrapper(getContext());
                    if (s.isUseXmasSmileys()) {
                        list = "smileys_xmas";
                    }
                }

                mIcons.addAll(Arrays.asList(aMan.list(list)));
                mIcons.remove("tourette.gif");
            }

        } catch (IOException e) { }

        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_icon_selection)
                .adapter(new IconListAdapter(), null)
                .build();

        return dialog;
    }

    /**
     * Custom view adapter for the ListView items
     */
    public class IconListAdapter extends RecyclerView.Adapter<IconSelectionDialog.IconListAdapter.ViewHolder> {
        private ArrayList<String> mDataset;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FrameLayout v = (FrameLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_icon, parent, false);

            return new IconSelectionDialog.IconListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            String icon;

            if(mIsSmileys)
                icon = getKeyByValue(TopicBuilder.mSmileys, mIcons.get(position));
            else
                icon = mIcons.get(position);

            holder.mText.setText(icon);

            try {
                Drawable dr;
                if(!mIsSmileys)
                    dr = Utils.getIcon(getActivity(), mIcons.get(position));
                else
                    dr = Utils.getSmiley(getActivity(), mIcons.get(position));
                dr.setBounds(0, 0, (int)holder.mText.getTextSize(), (int)holder.mText.getTextSize());
                holder.mText.setCompoundDrawables(dr, null, null, null);

            } catch (IOException e) {}

            holder.mRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mCallback != null) {
                        if(mIsSmileys)
                            mCallback.selected(mIcons.get(position), getKeyByValue(TopicBuilder.mSmileys, mIcons.get(position)));
                        else
                            mCallback.selected(mIcons.get(position), null);
                    }
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

        IconListAdapter() {
            mDataset = mIcons;
        }

    }

    public interface IconSelectedCallback {
        void selected(String filename, String smiley);
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}