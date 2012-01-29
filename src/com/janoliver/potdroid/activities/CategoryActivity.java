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

package com.janoliver.potdroid.activities;

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
import com.janoliver.potdroid.baseclasses.BaseListActivity;
import com.janoliver.potdroid.helpers.PotNotification;
import com.janoliver.potdroid.models.Board;
import com.janoliver.potdroid.models.Category;

/**
 * The view where one category is shown and all the boards it contains.
 */
public class CategoryActivity extends BaseListActivity {

    private Board[] mBoards;
    private Category mCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create Board object
        Bundle extras = getIntent().getExtras();
        mCategory = mObjectManager.getCategory(extras.getInt("CID"));

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

    private void fillView() {

        CategoryViewAdapter adapter = new CategoryViewAdapter(CategoryActivity.this);
        mListView.addHeaderView(getHeaderView());
        mListView.setAdapter(adapter);
        CategoryActivity.this.setTitle("Kategorie: " + mCategory.getName());
    }

    /**
     * Returns the header view for the list.
     * 
     * @return View header
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
     * @author oli Custom view adapter for the ListView items
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

    @Override
    public void refresh() {
    };

    /**
     * @author oli
     * 
     *         This class starts an async task (opening another system thread)
     *         to preload the view. It shows and handles the progressbar and the
     *         messages to the user. The magic happens in the doInBackground()
     *         method.
     */
    class PrepareAdapter extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new PotNotification(CategoryActivity.this, this, true);
            dialog.setMessage("Lade...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(mCategory.getName() == "")
                mObjectManager.getForum(true);
            mBoards = mCategory.getBoards();
            
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mBoards == null) {
                Toast.makeText(CategoryActivity.this, "Verbindungsfehler!", Toast.LENGTH_LONG)
                        .show();
            } else {
                fillView();
            }
            dialog.dismiss();
        }
    }
}