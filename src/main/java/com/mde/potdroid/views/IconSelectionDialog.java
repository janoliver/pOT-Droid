package com.mde.potdroid.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.mde.potdroid.R;
import com.mde.potdroid.fragments.FormFragment;
import com.mde.potdroid.helpers.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The icon selection dialog
 */
public class IconSelectionDialog extends DialogFragment
{
    private ArrayList<String> mIcons = new ArrayList<String>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FormFragment fragment = ((FormFragment) getTargetFragment());

        // find all icons
        AssetManager aMan = getActivity().getAssets();
        try {
            mIcons.addAll(Arrays.asList(aMan.list("thread-icons")));
        } catch (IOException e) {}

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.icon_selection);
        builder.setAdapter(new IconListAdapter(getActivity()),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Bitmap icon;
                        try {
                            icon = Utils.getBitmapIcon(getActivity(), mIcons.get(which));
                            Bitmap bm = Bitmap.createScaledBitmap(icon, 80, 80, true);
                            Integer icon_id = Integer
                                    .parseInt(mIcons.get(which).substring(4).split("\\.")[0]);
                            fragment.setIcon(bm, icon_id);
                        } catch (IOException e) {}

                    }
                });
        return builder.create();
    }

    /**
     * Custom view adapter for the ListView items
     */
    public class IconListAdapter extends ArrayAdapter<String>
    {
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
                dr.setBounds(0, 0, 20 * ((FormFragment) getTargetFragment()).getDensity(),
                        20 * ((FormFragment) getTargetFragment()).getDensity());
                name.setCompoundDrawables(dr, null, null, null);

            } catch (IOException e) { }


            return (row);
        }
    }
}