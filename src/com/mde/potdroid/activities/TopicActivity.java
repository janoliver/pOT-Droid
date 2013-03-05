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

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.PotNotification;
import com.mde.potdroid.helpers.PotUtils;
import com.mde.potdroid.helpers.TopicHtmlGenerator;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;

/**
 * Shows a thread. By far the most involving activity. And, of course, the most
 * important one. Also, Ponies suck.
 */
@SuppressLint("SetJavaScriptEnabled")
public class TopicActivity extends BaseActivity {

    private WebView   mWebView;
    private DataHandler mDataHandler;
    private String[]  mTitleNavItems   = {
            "","Aktualisieren", "Letzte Seite", "Erste Seite", "Seite..."
            };
    private static int EDITER_ACTIVITY = 1;
    private Boolean   mEnableNavigation = false;
    private OnNavigationListener mOnNavigationListener;

    /**
     * Starting point of the activity.
     */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set view
        setContentView(R.layout.activity_topic);
        
        // take care of the javascript and html. JSInterface is defined below.
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        JSInterface myJsInterface = new JSInterface(mWebView);
        mWebView.addJavascriptInterface(myJsInterface, "JSI");
        mWebView.loadData("<html><body></body></html>", "text/html", "utf-8");

        // initialize the data handler
        mDataHandler = (DataHandler)mFragmentManager.findFragmentByTag("data");
        if (mDataHandler == null || !mDataHandler.hasData()) {
            mDataHandler = new DataHandler();
            mFragmentManager.beginTransaction().add(mDataHandler, "data").commit();
            
            // initialize the data
            mDataHandler.mThread = mObjectManager.getTopic(mExtras.getInt("TID"));
            mDataHandler.mPid    = mExtras.getInt("PID",0);
            mDataHandler.mPage   = mExtras.getInt("page",1);
            mDataHandler.mHtmlCode = "";
            
            refresh();
        } else {
            fillView();
        }
        
        ActionBar actionBar = getSupportActionBar();
        mOnNavigationListener = new OnNavigationListener() {
            @SuppressWarnings("deprecation")
            public boolean onNavigationItemSelected(int position, long itemId) {
                if(mEnableNavigation) {
                    PotUtils.log(""+position);
                    switch(position) {
                    case 0: // do nothing
                        break;
                    case 1: //refresh
                        refresh();
                        break;
                    case 2: // last page
                        showLastPage();
                        break;
                    case 3: // first page
                        showFirstPage();
                        break;
                    case 4:
                    default:
                        ChoosePageDialog d = new ChoosePageDialog();
                        d.show(getSupportFragmentManager(), "pagedialog");
                        break;
                    }
                } else {
                    mEnableNavigation = true;
                }
                return true;
            } 
        };
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        SpinnerAdapter mSpinnerAdapter = new TitleSpinnerAdapter(actionBar.getThemedContext());
        actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
    }
	
	/**
	 * This fragment handles the data 
	 */
	public static class DataHandler extends FragmentBase {
	    private Topic     mThread;
	    private Integer   mPage;
	    private Integer   mPid;
	    private String    mHtmlCode;
	    
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setRetainInstance(true);
	    }
	    
	    /**
         * @return true if all fields != null
         */
        public boolean hasData() {
            return mThread != null && mPage != null && mPid != null && mHtmlCode != null;
        }
	}
	
    class TitleSpinnerAdapter extends ArrayAdapter<String> {
        private Topic mThread;
        
        TitleSpinnerAdapter(Context context) {
            super(context, android.R.layout.simple_spinner_dropdown_item, mTitleNavItems);
            mThread = TopicActivity.this.mDataHandler.mThread;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                
            LayoutInflater inflater = TopicActivity.this.getLayoutInflater();
            
            View row = inflater.inflate(R.layout.actionbar_title_thread, null);

            TextView title = (TextView) row.findViewById(R.id.title);
            TextView subtitle = (TextView) row.findViewById(R.id.subtitle);
            
            title.setText(mThread.getTitle());
            Spanned subtitleText = Html.fromHtml("Seite <b>" + TopicActivity.this.mDataHandler.mPage
                    + "</b> von <b>" + mThread.getLastPage() + "</b>");
            subtitle.setText(subtitleText);
            
            return row;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (position == 0)
                return new ViewStub(TopicActivity.this);            
            return super.getView(position, null, parent);
        }
    }

    /**
     * After downloading, shows the thread in the current activity.
     */
    private void fillView() {
        
        mWebView.loadDataWithBaseURL("file:///android_asset/", mDataHandler.mHtmlCode, "text/html", "UTF-8", null);
        setTitle(mDataHandler.mThread.getTitle());

        SpinnerAdapter spinnerAdapter = new TitleSpinnerAdapter(this);
        getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, mOnNavigationListener);
        
        try {
            Thread.sleep(200);
            mWebView.loadUrl("javascript:scrollToElement('" + mDataHandler.mThread.getPid() + "')");
        } catch (InterruptedException e) {
            Toast.makeText(TopicActivity.this, "Zum richtigen Post scrollen schiefgelaufen.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Override Volume Buttons
    // Use dispatchKeyEvent Method to catch all actions.
    // return false if override is not wished to send the call back to the
    // superclass.
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("volumeControl", false)) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {
                    showNextPage();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_UP) {
                    showPreviousPage();
                }
                return true;
            }
        }
        // tell the superclass we didnt do anything and it should process the
        // event itself
        return super.dispatchKeyEvent(event);
    }
    
    /**
     * Override the search button
     */
    @Override
    public boolean onSearchRequested() {
        goToPresetActivity();
        return false;
    }

    /**
     * options menu creator
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.actionmenu_topic, menu);
        if (mObjectManager.isLoggedIn()) {
            menu.setGroupVisible(R.id.loggedin, true);
        } else {
            menu.setGroupVisible(R.id.loggedin, false);
        }
        return true;
    }

    /**
     * options menu selection handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        PotUtils.log(item.getItemId()+"loool");
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.reply:
            if (mDataHandler.mThread.isClosed()) {
                Toast.makeText(TopicActivity.this, "Thread geschlossen.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(TopicActivity.this, EditorActivity.class);
                intent.putExtra("thread", mDataHandler.mThread);
                intent.putExtra("action", EditorActivity.ACTION_REPLY);
                startActivityForResult(intent, EDITER_ACTIVITY);
            }
            return true;
        case R.id.next:
            showNextPage();
            return true;
        case R.id.previous:
            showPreviousPage();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * refresh the current view
     */
    public void refresh() {
        new ThreadLoader().execute(mDataHandler.mPid, mDataHandler.mPage);
    }

    /**
     * show the next page if there is one. (cycle otherwise)
     */
    public void showNextPage() {
        if (mDataHandler.mPage < mDataHandler.mThread.getLastPage()) {
            mDataHandler.mPage++;
            mDataHandler.mPid = 0;
            refresh();
        } else if (mDataHandler.mPage > 1) {
            mDataHandler.mPage = 1;
            mDataHandler.mPid = 0;
            refresh();
        } else {
            Toast.makeText(this, "Keine weiteren Seiten vorhanden", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * show the previous page if there is one. (cycle otherwise)
     */
    public void showPreviousPage() {
        if (mDataHandler.mPage > 1) {
            mDataHandler.mPage--;
            mDataHandler.mPid = 0;
            refresh();
        } else if (mDataHandler.mThread.getLastPage() > 1) {
            mDataHandler.mPage = mDataHandler.mThread.getLastPage();
            refresh();
        } else {
            Toast.makeText(this, "Keine weiteren Seiten vorhanden", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * show the öast page
     */
    public void showLastPage() {
        if (mDataHandler.mPage != mDataHandler.mThread.getLastPage()) {
            mDataHandler.mPage = mDataHandler.mThread.getLastPage();
            mDataHandler.mPid = 0;
            refresh();
        } 
    }
    
    /**
     * show the first page
     */
    public void showFirstPage() {
        if (mDataHandler.mPage != 1) {
            mDataHandler.mPage = 1;
            mDataHandler.mPid = 0;
            refresh();
        } 
    }
    
    /**
     * show the first page
     */
    public void showPage(int p) {
        if (mDataHandler.mPage != p) {
            mDataHandler.mPage = p;
            mDataHandler.mPid = 0;
            refresh();
        } 
    }

    /**
     * This async task shows a loader and updates the topic object.
     * When it is finished, the loader is hidden.
     */
    class ThreadLoader extends AsyncTask<Integer, String, Exception> {
        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = new PotNotification(TopicActivity.this, this, true);
            mDialog.setMessage("...");
            mDialog.show();
        }

        @Override
        protected Exception doInBackground(Integer ... args) {
            // first argument is 0 or pid, second argument is 0 or page
            Integer page = args[1];
            Integer pid  = args[0];
            
            // decide whether to use the page or pid for fetching of the xml
            publishProgress("Lade Thread...");
            try {
                if(pid > 0) {
                    mDataHandler.mThread = mObjectManager.getTopicByPid(mDataHandler.mThread.getId(), pid);
                    mDataHandler.mPage   = mDataHandler.mThread.getLastFetchedPage();
                } else {
                    mDataHandler.mThread = mObjectManager.getTopicByPage(mDataHandler.mThread.getId(), page, true);
                }
            } catch (Exception e) {
                this.cancel(true);
                return e;
            }
            
            // generate html code. This can take a while depending on the amount of bbcode involved...
            publishProgress("Parse Thread...");
            try {
                TopicHtmlGenerator gen = new TopicHtmlGenerator(mDataHandler.mThread, mDataHandler.mPage, TopicActivity.this);
                mDataHandler.mHtmlCode = gen.buildTopic();
            } catch (Exception e) {
                return e;
            }
            
            // alles ok.
            return null;
        }
        
        @Override
        protected void onProgressUpdate(String ... args) {
            mDialog.setMessage(args[0]);
        }

        @Override
        protected void onPostExecute(Exception e) {
            if(e == null) {
                fillView();
                ((LeftMenu)getSupportFragmentManager().findFragmentById(R.id.leftmenu)).refresh();
                mDialog.dismiss();
            } else {
                Toast.makeText(TopicActivity.this, "Verbindungsfehler!", Toast.LENGTH_LONG).show();
                mDialog.dismiss();
                e.printStackTrace();
            }
        }
    }
    
    /**
     * This is a dirty workaround to some bug with jelly bean of not handling 
     * the context menu correctly. Instead, we create our own menu with the 
     * alert dialog. 
     */
    public AlertDialog getPostContextMenu(int post_id_p) {
        
        final int post_id = post_id_p;
        
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(TopicActivity.this);
        
        alertDialog.setItems(R.array.post_context_items, new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                Post post = mDataHandler.mThread.getPosts().get(mDataHandler.mPage)[post_id];
                Intent intent = new Intent(TopicActivity.this, EditorActivity.class);
                intent.putExtra("thread", mDataHandler.mThread);
                intent.putExtra("post", post);
                
                switch (which) {
                
                case 0:
                    // edit post dialog (check if user is allowed first)
                    if (post.getAuthor().getId() != mObjectManager.getCurrentUser().getId()) {
                        Toast.makeText(TopicActivity.this, "Du darfst diesen Post nicht editieren.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        intent.putExtra("action", EditorActivity.ACTION_EDIT);
                        startActivityForResult(intent, EDITER_ACTIVITY);
                    }
                    break;
                    
                case 1:
                    intent.putExtra("action", EditorActivity.ACTION_QUOTE);
                    startActivityForResult(intent, EDITER_ACTIVITY);
                    break;
                    
                case 2:
                    // bookmark
                    final String url = PotUtils.ASYNC_URL + "set-bookmark.php?PID=" + post.getId()
                            + "&token=" + post.getBookmarktoken();
                    new Thread(new Runnable() {
                        public void run() {
                            mWebsiteInteraction.callPage(url);

                            TopicActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(TopicActivity.this, "Bookmark hinzugefügt.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                    break;
                    
                default:
                    //nothing
                }
            }
        });
        
        return alertDialog.create();
    }
    
    // refresh this if somethind was posted or edited.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == EDITER_ACTIVITY && resultCode == RESULT_OK) 
            refresh();
    }
    
    /**
     * This class resembles the interface to the javascript in the 
     * WebView where the topic is displayed in.
     */
    public class JSInterface {

        public JSInterface(WebView appView) {}
        
        /**
         * Shows a context menu after long-touch on a post.
         */
        public void showPostContextMenu(String id) {
            final Integer fid = Integer.valueOf(id.substring(4)).intValue();
            
            runOnUiThread(new Runnable() {
				public void run() {
                    AlertDialog d = getPostContextMenu(fid);
                    d.show();
                }
            });
        }
        
        /**
         * Shows a toast issued from within the webviews javascript.
         */
        public void showToast(String msg) {
            Toast.makeText(TopicActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
        
        /**
         * Returns true or false if or not if the gravity feature is switched on.
         */
        public boolean gravityOn() {
            return PreferenceManager.
                    getDefaultSharedPreferences(TopicActivity.this).
                    getBoolean("gravityInThreads", false);
        }
        
        /**
         * Returns true or false if or not if the gravity feature is switched on.
         */
        public boolean resizeImages() {
            return PreferenceManager.
                    getDefaultSharedPreferences(TopicActivity.this).
                    getBoolean("resizeImages", true);
        }
        
        /**
         * Get the setting for the duration of touch to edit/cite
         */
        public int getTouchDuration() {
            return Integer.valueOf(PreferenceManager.
                    getDefaultSharedPreferences(TopicActivity.this).
                    getString("topicTouchDuration", "500")).intValue();
        }

    }
    
    public class ChoosePageDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            CharSequence[] items = new CharSequence[mDataHandler.mThread.getLastPage()];
            for(int i=0; i < items.length; ++i) {
                items[i] = "Seite "+(i+1);
            }
            
            builder.setTitle(R.string.pick_page)
                   .setItems(items, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int which) {
                           showPage(which+1);
                       }
            });
            return builder.create();
        }
    }

} 
