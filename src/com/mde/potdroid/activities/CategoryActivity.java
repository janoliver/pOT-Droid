/*
 * Copyright (C) 2011 Jan Oliver Oelerich <janoliver@oelerich.org>
 *
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this software, and changing it is allowed as long as the 
 * name is changed.
 *
 *           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *  0. You just DO WHAT THE FUCK YOU WANT TO. 
 */

package com.mde.potdroid.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.janoliver.potdroid.R;
import com.mde.potdroid.baseclasses.BaseListActivity;
import com.mde.potdroid.helpers.ObjectManager.ParseErrorException;
import com.mde.potdroid.helpers.PotNotification;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Category;

/**
 * The view where one category is shown and all the boards it contains.
 */
public class CategoryActivity extends BaseListActivity {

    /**
     * mBoards is an array of all the boards within the category mCategory.
     */
    private Board[]  mBoards;
    private Category mCategory;

    /**
     * Starting point of the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create Board object
        mCategory = mObjectManager.getCategory(mExtras.getInt("CID"));

        // the view
        setListAdapter(null);
        new PrepareAdapter().execute((Void[]) null);

        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Intent intent = new Intent(CategoryActivity.this, BoardActivity.class);
                    intent.putExtra("BID", mBoards[position - 1].getId());
                    intent.putExtra("CID", mCategory.getId());
                    intent.putExtra("page", 1);
                    startActivity(intent);
                }
            }
        });

    }
    
    /**
     * After having downloaded the data, fill the view
     */
    private void fillView() {
        CategoryViewAdapter adapter = new CategoryViewAdapter(CategoryActivity.this);
        mListView.addHeaderView(getHeaderView());
        mListView.setAdapter(adapter);
        CategoryActivity.this.setTitle("Kategorie: " + mCategory.getName());
    }

    /**
     * Returns the header view for the list.
     */
    public View getHeaderView() {
        LayoutInflater inflater = this.getLayoutInflater();
        View row = inflater.inflate(R.layout.header_general, null);

        TextView loggedin = (TextView) row.findViewById(R.id.loggedin);
        loggedin.setText(mObjectManager.isLoggedIn() ? "Hallo "
                + mObjectManager.getCurrentUser().getNick() : "nicht eingeloggt");

        return (row);
    }

    /**
     * Custom view adapter for the ListView items
     */
    class CategoryViewAdapter extends ArrayAdapter<Board> {
        Activity context;

        CategoryViewAdapter(Activity context) {
            super(context, R.layout.listitem_category, R.id.name, mBoards);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View row = inflater.inflate(R.layout.listitem_category, null);

            TextView name = (TextView) row.findViewById(R.id.name);
            name.setText(mBoards[position].getName());
            TextView descr = (TextView) row.findViewById(R.id.description);
            descr.setText(mBoards[position].getDescription());

            return (row);
        }
    }

    /**
     * Shopuld be implemented someday.
     */
    @Override
    public void refresh() {};

    /**
     * This async task shows a loader and updates the forum object.
     * When it is finished, the loader is hidden.
     */
    class PrepareAdapter extends AsyncTask<Void, Void, Exception> {
        ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = new PotNotification(CategoryActivity.this, this, true);
            mDialog.setMessage("Lade...");
            mDialog.show();
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                mObjectManager.getForum(false);
                mCategory = mObjectManager.getCategory(mCategory.getId());
                mBoards = mCategory.getBoards();
                return null;
            } catch (ParseErrorException e) {
                this.cancel(true);
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception e) {
            if(e == null) {
                fillView();
                mDialog.dismiss();
            } else {
                Toast.makeText(CategoryActivity.this, "Verbindungsfehler!", Toast.LENGTH_LONG).show();
                mDialog.dismiss();
                e.printStackTrace();
            }
        }
    }
}