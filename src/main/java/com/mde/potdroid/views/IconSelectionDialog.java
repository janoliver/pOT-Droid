package com.mde.potdroid.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_icon_selection);
        builder.setAdapter(new IconListAdapter(getActivity()),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(mCallback != null) {
                            if(mIsSmileys)
                                mCallback.selected(mIcons.get(which), getKeyByValue(TopicBuilder.mSmileys, mIcons.get(which)));
                            else
                                mCallback.selected(mIcons.get(which), null);
                        }

                    }
                });
        return builder.create();
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