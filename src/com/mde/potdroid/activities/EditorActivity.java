/*
 * Copyright (C) 2012 mods.de community 
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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mde.potdroid.R;
import com.mde.potdroid.activities.TopicActivity.DataHandler;
import com.mde.potdroid.helpers.PotNotification;
import com.mde.potdroid.helpers.PotUtils;
import com.mde.potdroid.helpers.WebsiteInteraction;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;

/**
 * In this activity, the forum and the containing categories are shown.
 */
public class EditorActivity extends BaseActivity {
    private EditText mTitle;
    private EditText mBody;
    private DataHandler mDataHandler;
    
    
    public static final int ACTION_REPLY = 1;
    public static final int ACTION_QUOTE = 2 ;
    public static final int ACTION_EDIT = 3;
    

    /**
     * Starting point of the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // prepare the views
        setContentView(R.layout.activity_editor);
        
        // preset some text
        mTitle = (EditText)findViewById(R.id.editor_title);
        mBody = (EditText)findViewById(R.id.editor_body);

        // initialize the data handler
        mDataHandler = (DataHandler)mFragmentManager.findFragmentByTag("data");
        if (mDataHandler == null) {
            mDataHandler = new DataHandler();
            mFragmentManager.beginTransaction().add(mDataHandler, "data").commit();
            
            mDataHandler.mThread = (Topic)getIntent().getSerializableExtra("thread");
            mDataHandler.mPost = (Post)getIntent().getSerializableExtra("post");
            mDataHandler.mAction = getIntent().getIntExtra("action", ACTION_REPLY);
            
            String text = "";
            if(mDataHandler.mAction == ACTION_QUOTE) {
                text = "[quote=" + mDataHandler.mThread.getId() + "," + mDataHandler.mPost.getId() + ",\""
                        + mDataHandler.mPost.getAuthor().getNick() + "\"][b]\n" + mDataHandler.mPost.getText() + "\n[/b][/quote]";
            } else if(mDataHandler.mAction == ACTION_EDIT) {
                text = mDataHandler.mPost.getText();
            }
            mDataHandler.mBody = text;
            mDataHandler.mTitle = "";
        }
        fillView();
        
    }
    
    @Override
    public void onSaveInstanceState(Bundle state)
    {
        mDataHandler.mBody = mBody.getText().toString();
        mDataHandler.mTitle = mTitle.getText().toString();

        super.onSaveInstanceState(state);
    }

    /**
     * This fragment handles the data 
     */
    public static class DataHandler extends FragmentBase {
        private String    mTitle;
        private String    mBody;
        private Topic     mThread;
        private Post      mPost;
        private Integer   mAction;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
    
    private void fillView() {
        mBody.setText(mDataHandler.mBody);
        mTitle.setText(mDataHandler.mTitle);
        mBody.requestFocus();
        mBody.setSelection(mBody.getText().length());
        
        // set the subtitle
        getSupportActionBar().setSubtitle(mDataHandler.mAction == ACTION_EDIT ? 
                "Post bearbeiten" : "Antwort verfassen");
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.save:
            savePost();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void savePost() {
        new PostEditer().execute(mDataHandler.mThread, mDataHandler.mPost, mTitle.getText().toString(), 
                    mBody.getText().toString(), mDataHandler.mAction);
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.actionmenu_editor, menu);
        return true;
    }
    
    
    @Override
    public void refresh() {};
    
    public class PostEditer extends AsyncTask<Object, Object, Boolean> {

        private ProgressDialog mDialog;
        private WebsiteInteraction mWebsiteInteraction;

        public PostEditer() {
            super();
            mWebsiteInteraction = PotUtils.getWebsiteInteractionInstance(EditorActivity.this);
        }

        @Override
        protected void onPreExecute() {
            mDialog = new PotNotification(EditorActivity.this, this, false);
            mDialog.setMessage("Sende Post...");
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            Topic thread = (Topic) params[0];
            Post post = (Post) params[1];
            String title = (String) params[2];
            String body = (String) params[3];
            Boolean editing = (Integer)params[4] == ACTION_EDIT;
            
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            
            nameValuePairs.add(new BasicNameValuePair("message", body));
            nameValuePairs.add(new BasicNameValuePair("submit", "Eintragen"));
            nameValuePairs.add(new BasicNameValuePair("TID", "" + thread.getId()));
            
            if(editing) {
                nameValuePairs.add(new BasicNameValuePair("PID", "" + post.getId()));
                nameValuePairs.add(new BasicNameValuePair("token", post.getEdittoken()));
                nameValuePairs.add(new BasicNameValuePair("edit_title", title));
                
                return mWebsiteInteraction.sendPost(PotUtils.BOARD_URL_EDITPOST, nameValuePairs);
            } else {
                nameValuePairs.add(new BasicNameValuePair("SID", ""));
                nameValuePairs.add(new BasicNameValuePair("PID", ""));
                nameValuePairs.add(new BasicNameValuePair("token", thread.getNewreplytoken()));
                nameValuePairs.add(new BasicNameValuePair("post_title", title));

                return mWebsiteInteraction.sendPost(PotUtils.BOARD_URL_POST, nameValuePairs);
            }
            
        }

        @Override
        protected void onCancelled() {
            mDialog.dismiss();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(EditorActivity.this, "Fehlgeschlagen.", Toast.LENGTH_SHORT).show();
            } else {
                EditorActivity.this.setResult(RESULT_OK);
                EditorActivity.this.finish();
            }
            mDialog.dismiss();
        }
    }
}