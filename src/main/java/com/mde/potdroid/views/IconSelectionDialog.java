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
import com.mde.potdroid.helpers.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * The icon selection dialog
 */
public class IconSelectionDialog extends DialogFragment {
    private ArrayList<String> mIcons = new ArrayList<String>();
    private Boolean mIsSmileys;
    private IconSelectedCallback mCallback;

    public static HashMap<String, String> mSmileys;
    static {
        mSmileys = new HashMap<String, String>();

        mSmileys.put("banghead.gif", ":bang:");
        mSmileys.put("biggrin.gif", ":D");
        mSmileys.put("confused.gif", ":confused:");
        mSmileys.put("freaked.gif", ":huch:");
        mSmileys.put("hm.gif", ":hm:");
        mSmileys.put("mata.gif", ":mata:");
        mSmileys.put("sceptic.gif", ":what:");
        mSmileys.put("smiley-pillepalle.gif", ":moo:");
        mSmileys.put("urgs.gif", ":wurgs:");
        mSmileys.put("wink.gif", ";)");
        mSmileys.put("icon1.gif", ":zyklop:");
        mSmileys.put("icon2.gif", ":P");
        mSmileys.put("icon5.gif", "^^");
        mSmileys.put("icon7.gif", ":)");
        mSmileys.put("icon8.gif", ":|");
        mSmileys.put("icon12.gif", ":(");
        mSmileys.put("icon13.gif", ":mad:");
        mSmileys.put("icon15.gif", ":eek:");
        mSmileys.put("icon16.gif", ":o");
        mSmileys.put("icon18.gif", ":roll:");
    }

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
            }

        } catch (IOException e) { }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_icon_selection);
        builder.setAdapter(new IconListAdapter(getActivity()),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(mCallback != null) {
                            if(mIsSmileys)
                                mCallback.selected(mIcons.get(which), mSmileys.get(mIcons.get(which)));
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
                icon = mSmileys.get(mIcons.get(position));
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
}