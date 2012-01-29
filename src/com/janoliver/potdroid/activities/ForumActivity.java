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
import android.preference.PreferenceManager;
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
import com.janoliver.potdroid.models.Category;
import com.janoliver.potdroid.models.Forum;

/**
 * In this activity, the forum and the containing categories are shown.
 */
public class ForumActivity extends BaseListActivity {
    private Category[] mCats;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // redirect to bookmarks?
        Bundle extras      = getIntent().getExtras();
        Boolean noredirect = false;
        if ((extras != null) && extras.containsKey("noredirect") && extras.getBoolean("noredirect")) {
            noredirect = true;
        }
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bookmarkStart", false)
                && !noredirect) {
            finish();
            Intent intent = new Intent(ForumActivity.this, BookmarkActivity.class);
            startActivityForResult(intent, 1);
            return;
        }

        // the view
        setListAdapter(null);

        // cache.
        new PrepareAdapter().execute((Void[]) null);

        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // cats[position].getId()

                if (position > 0) {
                    Intent intent = new Intent(ForumActivity.this, CategoryActivity.class);
                    intent.putExtra("CID", mCats[position - 1].getId());
                    startActivity(intent);
                }

            }
        });

    }

    private void fillView() {
        ForumViewAdapter adapter = new ForumViewAdapter(ForumActivity.this);
        mListView.addHeaderView(getHeaderView());
        mListView.setAdapter(adapter);
    }

    @Override
    public void refresh() {
    };

    /**
     * Returns the header view for the list.
     * 
     * @return View header
     */
    public View getHeaderView() {
        LayoutInflater inflater = this.getLayoutInflater();
        View row = inflater.inflate(R.layout.header_general, null);

        TextView descr = (TextView) row.findViewById(R.id.pagetext);
        descr.setText("Übersicht");

        TextView loggedin = (TextView) row.findViewById(R.id.loggedin);
        loggedin.setText(mObjectManager.isLoggedIn() ? "Hallo "
                + mObjectManager.getCurrentUser().getNick() : "nicht eingeloggt");

        return (row);
    }

    /**
     * @author oli Custom view adapter for the ListView items
     */
    class ForumViewAdapter extends ArrayAdapter<Category> {
        Activity context;

        ForumViewAdapter(Activity context) {
            super(context, R.layout.listitem_category, R.id.name, mCats);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View row = inflater.inflate(R.layout.listitem_category, null);

            TextView name = (TextView) row.findViewById(R.id.name);
            name.setText(mCats[position].getName());
            TextView descr = (TextView) row.findViewById(R.id.description);
            descr.setText(mCats[position].getDescription());

            return (row);
        }
    }

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
            dialog = new PotNotification(ForumActivity.this, this, true);
            dialog.setMessage("Lade...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Forum forum = mObjectManager.getForum(false);
            mCats       = forum.getCategories();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (mCats == null) {
                Toast.makeText(ForumActivity.this, "Verbindungsfehler!", Toast.LENGTH_LONG).show();
            } else {
                fillView();
            }
            dialog.dismiss();
        }
    }

}