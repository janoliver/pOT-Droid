package com.mde.potdroid.activities;

import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mde.potdroid.R;
import com.mde.potdroid.helpers.ObjectManager;
import com.mde.potdroid.helpers.PotNotification;
import com.mde.potdroid.helpers.PotUtils;
import com.mde.potdroid.helpers.WebsiteInteraction;
import com.mde.potdroid.models.Bookmark;

public class NavigationFragment extends Fragment {
    protected WebsiteInteraction mWebsiteInteraction;
    protected ObjectManager      mObjectManager;
    protected SharedPreferences  mSettings;
    private Activity             mActivity;
    private ListView             mBookmarksView;
    private LayoutInflater       mInflater;
    
    /**
     * mBookmarks is a Map with all the bookmarks stored as 
     * <Id, BookmarkObject> values.
     */
    private Map<Integer, Bookmark> mBookmarks;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mActivity = getActivity();
        
        mWebsiteInteraction = PotUtils.getWebsiteInteractionInstance(mActivity);
        mObjectManager      = PotUtils.getObjectManagerInstance(mActivity);
        mSettings           = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        
        mInflater = inflater;
        
        View fragmentView = inflater.inflate(R.layout.fragment_navigation, container, false);
        
        // logged in and stuff
        TextView loggedIn = (TextView) fragmentView.findViewById(R.id.hello);
        loggedIn.setText(mObjectManager.isLoggedIn() ? 
                "Hallo " + mObjectManager.getCurrentUser().getNick() + "!" : 
                    "Hallo Unbekannter!");
        
        // populate the bookmarks listview
        mBookmarksView = (ListView) fragmentView.findViewById( R.id.bookmark_list);
        mBookmarksView.setAdapter( null );
        new PrepareAdapter().execute((Void[]) null);
        
        // Inflate the layout for this fragment
        return fragmentView;
    }
    
    /**
     * Custom view adapter for the ListView items
     */
    class BookmarkViewAdapter extends ArrayAdapter<Bookmark> {
        Activity context;

        BookmarkViewAdapter(Activity context) {
            super(context, R.layout.listitem_fragment_bookmark, R.id.name, 
                    mBookmarks.values().toArray(new Bookmark[0]));
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            
            // dp to px
            
            View row = mInflater.inflate(R.layout.listitem_fragment_bookmark, null);
            Bookmark bm = (Bookmark)mBookmarks.values().toArray()[position];
            
            TextView name = (TextView) row.findViewById(R.id.name);
            TextView descr = (TextView) row.findViewById(R.id.newposts);
            
            name.setText(bm.getThread().getTitle());
            
            if(bm.getNumberOfNewPosts() > 0) {
                descr.setText("" + bm.getNumberOfNewPosts());
            } else {
                name.setTextColor(0xFFaaaaaa);
                name.setTextSize(11);
                name.setPadding(0, 0, (int)(20 * getResources().getDisplayMetrics().density + 0.5f), 0);
                descr.setVisibility(View.GONE);
            }
            
            
            return row;
        }
        
    }
    
    
    
    /**
     * This async task shows a loader and updates the bookmark object.
     * When it is finished, the loader is hidden.
     */
    class PrepareAdapter extends AsyncTask<Void, Void, Exception> {
        ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = new PotNotification(mActivity, this, true);
            mDialog.setMessage("Lade...");
            mDialog.show();
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                mBookmarks = mObjectManager.getBookmarks();
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception e) {
            if(e == null) {
                
                BookmarkViewAdapter adapter = new BookmarkViewAdapter(mActivity);
                mBookmarksView.setAdapter(adapter);

                mDialog.dismiss();
            } else {
                Toast.makeText(mActivity, "Verbindungsfehler!", Toast.LENGTH_LONG).show();
                mDialog.dismiss();
                e.printStackTrace();
            }
        }
    }
}