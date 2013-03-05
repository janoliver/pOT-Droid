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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.DialogFragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.ContextMenu;
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
    private TextView mIcon;
    private DataHandler mDataHandler;
    private ArrayList<String> mIcons = new ArrayList<String>();
    private String   mIconId = "0";
    
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
        mIcon = (TextView)findViewById(R.id.icon);
        
        registerForContextMenu(mIcon);

        // initialize the data handler
        mDataHandler = (DataHandler)mFragmentManager.findFragmentByTag("data");
        if (mDataHandler == null || !mDataHandler.hasData()) {
            mDataHandler = new DataHandler();
            mFragmentManager.beginTransaction().add(mDataHandler, "data").commit();
            
            mDataHandler.mThread = (Topic)getIntent().getSerializableExtra("thread");
            mDataHandler.mPost = (Post)getIntent().getSerializableExtra("post");
            mDataHandler.mAction = getIntent().getIntExtra("action", ACTION_REPLY);
            
            String text = "";
            if(mDataHandler.mAction == ACTION_QUOTE) {
                text = "[quote=" + mDataHandler.mThread.getId() + "," + mDataHandler.mPost.getId() + ",\""
                        + mDataHandler.mPost.getAuthor().getNick() + "\"][b]\n" + mDataHandler.mPost.getText() + "\n[/b][/quote]";
                mDataHandler.mPost.setIcon(0);
            } else if(mDataHandler.mAction == ACTION_EDIT) {
                text = mDataHandler.mPost.getText();
            }
            mDataHandler.mBody = text;
        }
        fillView();
        

        // the icon list
        AssetManager aMan = getAssets();
        try {
            mIcons.addAll(Arrays.asList(aMan.list("icons"))); 
        } catch (IOException e) {}
        
        mIcon.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            public void onClick(View v) {
                IconSelection id = new IconSelection();
                id.show(getSupportFragmentManager(), "icondialog");
            }
        });
        
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
    }
    
    @Override
    public void onSaveInstanceState(Bundle state)
    {
        mDataHandler.mBody = mBody.getText().toString();

        super.onSaveInstanceState(state);
    }

    /**
     * This fragment handles the data 
     */
    public static class DataHandler extends FragmentBase {
        private String    mBody;
        private Topic     mThread;
        private Post      mPost;
        private Integer   mAction;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
        
        /**
         * @return true if all fields != null
         */
        public boolean hasData() {
            return mBody != null && mThread != null && mPost != null && mAction != null;
        }
    }
    
    private void fillView() {
        mBody.setText(mDataHandler.mBody);
        if(mDataHandler.mPost != null)
            mTitle.setText(mDataHandler.mPost.getTitle());
        mBody.requestFocus();
        mBody.setSelection(mBody.getText().length());
        
        if(mDataHandler.mPost != null)
        {
            Bitmap bitmap;
            try {
                bitmap = getIconBitmap("icon"+mDataHandler.mPost.getIcon()+".gif");
                Drawable dr = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 40, 40, true));
                mIcon.setCompoundDrawablesWithIntrinsicBounds(null,null,dr,null);
            } catch (IOException e) {}
            
            mIconId = mDataHandler.mPost.getIcon()+"";
        }
        
        
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
                nameValuePairs.add(new BasicNameValuePair("edit_icon", mIconId));
                
                return mWebsiteInteraction.sendPost(PotUtils.BOARD_URL_EDITPOST, nameValuePairs);
            } else {
                nameValuePairs.add(new BasicNameValuePair("SID", ""));
                nameValuePairs.add(new BasicNameValuePair("PID", ""));
                nameValuePairs.add(new BasicNameValuePair("token", thread.getNewreplytoken()));
                nameValuePairs.add(new BasicNameValuePair("post_title", title));
                nameValuePairs.add(new BasicNameValuePair("post_icon", mIconId));

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
    
    public class IconSelection extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            
            builder.setTitle(R.string.whichicon);
            builder.setAdapter(new IconListAdapter(EditorActivity.this), 
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        
                            Bitmap bitmap;
                            try {
                                bitmap = getIconBitmap(mIcons.get(which));
                                Drawable dr = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 40, 40, true));
                                mIcon.setCompoundDrawablesWithIntrinsicBounds(null,null,dr,null);
                                PotUtils.log(which+" "+mIcons.get(which));
                                mIconId = mIcons.get(which).substring(4).split("\\.")[0];
                            } catch (IOException e) {}
                            
                    }
            });
            return builder.create();
        }
    }
    

    /**
     * Custom view adapter for the ListView items
     */
    class IconListAdapter extends ArrayAdapter<String> {
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
                Bitmap bitmap = getIconBitmap(icon);
                Drawable dr = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 40, 40, true));
                name.setCompoundDrawablesWithIntrinsicBounds(dr,null,null,null);
                
            } catch (IOException e) { }

            
            return (row);
        }
    }
    
    private Bitmap getIconBitmap(String strName) throws IOException
    {
        AssetManager assetManager = getAssets();

        InputStream istr = assetManager.open("icons/" + strName);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        istr.close();

        return bitmap;
    }
}