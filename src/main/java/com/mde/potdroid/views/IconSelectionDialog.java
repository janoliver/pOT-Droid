package com.mde.potdroid.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.TopicBuilder;
import com.mde.potdroid.helpers.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
                mIcons.addAll(Arrays.asList(aMan.list("smileys")));
                mIcons.remove("tourette.gif");
            }

        } catch (IOException e) { }

        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_icon_selection)
                .adapter(new IconListAdapter(getActivity()), null)
                .build();

        ListView listView = dialog.getListView();
        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                    dialog.dismiss();
                    if(mCallback != null) {
                        if(mIsSmileys)
                            mCallback.selected(mIcons.get(which), getKeyByValue(TopicBuilder.mSmileys, mIcons.get(which)));
                        else
                            mCallback.selected(mIcons.get(which), null);
                    }

                }
            });
        }

        return dialog;
    }

    /**
     * Custom view adapter for the ListView items
     */
    public class IconListAdapter extends ArrayAdapter<String> {
        Activity context;

        IconListAdapter(Activity context) {
            super(context, R.layout.listitem_icon, R.id.name, mIcons);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View row = inflater.inflate(R.layout.listitem_icon, null);
            String icon;

            if(mIsSmileys)
                icon = getKeyByValue(TopicBuilder.mSmileys, mIcons.get(position));
            else
                icon = mIcons.get(position);

            TextView name = (TextView) row.findViewById(R.id.name);
            name.setText(icon);

            try {
                Drawable dr;
                if(!mIsSmileys)
                    dr = Utils.getIcon(getActivity(), mIcons.get(position));
                else
                    dr = Utils.getSmiley(getActivity(), mIcons.get(position));
                dr.setBounds(0, 0, (int)name.getTextSize(), (int)name.getTextSize());
                name.setCompoundDrawables(dr, null, null, null);

            } catch (IOException e) {}


            return (row);
        }
    }

    public interface IconSelectedCallback {
        public void selected(String filename, String smiley);
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