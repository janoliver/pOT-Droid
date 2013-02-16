package com.mde.potdroid.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mde.potdroid.R;
import com.mde.potdroid.helpers.ObjectManager;
import com.mde.potdroid.helpers.ObjectManager.NotLoggedInException;
import com.mde.potdroid.helpers.PotUtils;
import com.mde.potdroid.helpers.WebsiteInteraction;
import com.mde.potdroid.models.Bookmark;

public class LeftMenu extends Fragment {
    protected WebsiteInteraction mWebsiteInteraction;
    protected ObjectManager      mObjectManager;
    protected SharedPreferences  mSettings;
    private Activity             mActivity;
    private ListView             mBookmarksView;
    private LayoutInflater       mInflater;
    private View                 mFragmentView;
    private RelativeLayout       mLoader;
    private Boolean              mRefreshing = false;
    
    /**
     * mBookmarks is a Map with all the bookmarks stored as 
     * <Id, BookmarkObject> values.
     */
    private Bookmark[] mBookmarks;
    
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
        
        mFragmentView = mInflater.inflate(R.layout.sidebar_content, container, false);
        
        mLoader = (RelativeLayout) mFragmentView.findViewById(R.id.loadingPanel);
        mLoader.setVisibility(View.GONE);
        
        // logged in and stuff
        TextView loggedIn = (TextView) mFragmentView.findViewById(R.id.hello);
        loggedIn.setText(mObjectManager.isLoggedIn() ? 
                "Hallo " + mObjectManager.getCurrentUser().getNick() + "!" : 
                    "Hallo Unbekannter!");
        
        // populate the bookmarks listview
        mBookmarksView = (ListView) mFragmentView.findViewById( R.id.bookmark_list);
        mBookmarksView.setAdapter( null );
        mBookmarksView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openThread(mBookmarks[position]);
            }
        });
        
        View refreshView = (RelativeLayout) mFragmentView.findViewById( R.id.refresh_view);
        refreshView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                refresh();
            }
        });
        
        // Inflate the layout for this fragment
        return mFragmentView;
    }
    
    public void refresh() {
        if(!mRefreshing)
            new PrepareAdapter().execute((Void[]) null);
    }
    
    public void openThread(Bookmark bm) {
        Intent intent = new Intent(mActivity, TopicActivity.class);
        intent.putExtra("TID", bm.getThread().getId());
        intent.putExtra("PID", bm.getLastPost().getId());
        startActivity(intent);
    }
    
    public void setListError(String error) {
        TextView err = (TextView)mFragmentView.findViewById(R.id.error_message);
        if(error.length() == 0) {
            err.setVisibility(View.GONE);
        } else {
            err.setText(error);
            err.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Custom view adapter for the ListView items
     */
    class BookmarkViewAdapter extends ArrayAdapter<Bookmark> {
        Activity context;

        BookmarkViewAdapter(Activity context) {
            super(context, R.layout.sidebar_bookmark_listitem, R.id.name, mBookmarks);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            
            View row = mInflater.inflate(R.layout.sidebar_bookmark_listitem, null);
            Bookmark bm = mBookmarks[position];
            
            TextView name = (TextView) row.findViewById(R.id.name);
            name.setText(bm.getThread().getTitle());
            TextView descr = (TextView) row.findViewById(R.id.newposts);
            descr.setText("" + bm.getNumberOfNewPosts());
            
            return row;
        }
        
    }
    
    
    
    /**
     * This async task shows a loader and updates the bookmark object.
     * When it is finished, the loader is hidden.
     */
    class PrepareAdapter extends AsyncTask<Void, Void, Exception> {
        
        @Override
        protected void onPreExecute() {
            setListError("");
            mRefreshing = true;
            mLoader.setVisibility(View.VISIBLE);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Bookmark[] tmp = mObjectManager.getBookmarks();
                ArrayList<Bookmark> tmp2 = new ArrayList<Bookmark>();
                for (Bookmark bm: tmp)
                    if(bm.getNumberOfNewPosts() > 0)
                        tmp2.add(bm);
                mBookmarks = tmp2.toArray(new Bookmark[0]);
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception e) {
            mLoader.setVisibility(View.GONE);
            mRefreshing = false;
            if(e == null && mBookmarks.length > 0) {
                BookmarkViewAdapter adapter = new BookmarkViewAdapter(mActivity);
                mBookmarksView.setAdapter(adapter);
            } else if(e == null && mBookmarks.length == 0) {
                setListError("Keine ungelesenen Bookmarks.");
            } else if(e instanceof NotLoggedInException) {
                setListError("Nicht eingeloggt.");
            } else {
                setListError("Verbindungsfehler.");
            }
        }
    }
}