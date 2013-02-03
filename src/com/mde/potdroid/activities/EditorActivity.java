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
    private Topic mThread = null;
    private Post mPost = null;
    private Integer mAction;
    
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
        
        // topic
        mThread = (Topic)getIntent().getSerializableExtra("thread");
        mPost = (Post)getIntent().getSerializableExtra("post");
        mAction = getIntent().getIntExtra("action", ACTION_REPLY);
        
        // preset some text
        mTitle = (EditText)findViewById(R.id.editor_title);
        mBody = (EditText)findViewById(R.id.editor_body);
        
        
        
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        String text = "";
        if(mAction == ACTION_QUOTE) {
            text = "[quote=" + mThread.getId() + "," + mPost.getId() + ",\""
                    + mPost.getAuthor().getNick() + "\"][b]\n" + mPost.getText() + "\n[/b][/quote]";
        } else if(mAction == ACTION_EDIT) {
            text = mPost.getText();
        }
        mBody.setText(text);
        mBody.requestFocus();
        mBody.setSelection(mBody.getText().length());
        
        refreshLeftMenu();
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
        new PostEditer().execute(mThread, mPost, mTitle.getText().toString(), 
                    mBody.getText().toString(), mAction);
        
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