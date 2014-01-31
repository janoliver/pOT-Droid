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
import com.mde.potdroid.fragments.EditorFragment;
import com.mde.potdroid.helpers.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The icon selection dialog
 */
public class IconSelectionDialog extends DialogFragment {
    private ArrayList<String> mIcons = new ArrayList<String>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final EditorFragment fragment = ((EditorFragment) getTargetFragment());

        // find all icons
        mIcons.add("Kein icon");
        AssetManager aMan = getActivity().getAssets();
        try {
            mIcons.addAll(Arrays.asList(aMan.list("thread-icons")));
        } catch (IOException e) {
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_icon_selection);
        builder.setAdapter(new IconListAdapter(getActivity()),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Integer icon_id = Integer
                                    .parseInt(mIcons.get(which).substring(4).split("\\.")[0]);
                            fragment.setIconById(icon_id);
                        } catch (NumberFormatException e) {
                            fragment.setIconById(-1);
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
            String icon = mIcons.get(position);

            TextView name = (TextView) row.findViewById(R.id.name);
            name.setText(icon);

            try {
                Drawable dr = Utils.getIcon(getActivity(), icon);
                dr.setBounds(0, 0, (int)name.getTextSize(), (int)name.getTextSize());
                name.setCompoundDrawables(dr, null, null, null);

            } catch (IOException e) {}


            return (row);
        }
    }
}