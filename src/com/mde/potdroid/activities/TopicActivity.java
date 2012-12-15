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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mde.potdroid.R;
import com.mde.potdroid.helpers.DialogWrapper;
import com.mde.potdroid.helpers.PostDialogs;
import com.mde.potdroid.helpers.PotNotification;
import com.mde.potdroid.helpers.PotUtils;
import com.mde.potdroid.helpers.TopicHtmlGenerator;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;

/**
 * Shows a thread. By far the most involving activity. And, of course, the most
 * important one. Also, Ponies suck.
 */
public class TopicActivity extends BaseActivity {

    private WebView   mWebView;
    private ViewGroup mLinearLayout;
    private Topic     mThread;
    private Integer   mPage            = 1;
    private Integer   mPid             = 0;
    private String    mHtmlCode        = "";

    /**
     * Starting point of the activity.
     */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set view
        setContentView(R.layout.activity_topic);
        mLinearLayout = (ViewGroup) findViewById(R.id.linlayout);
        mWebView = (WebView) findViewById(R.id.webview);
        
        // take care of the javascript and html. JSInterface is defined below.
        mWebView.getSettings().setJavaScriptEnabled(true);
        JSInterface myJsInterface = new JSInterface(mWebView);
        mWebView.addJavascriptInterface(myJsInterface, "JSI");
        mWebView.loadData("<html><body></body></html>", "text/html", "utf-8");

        // was only the orientation changed?
        final Orientation threadSaved = (Orientation) getLastNonConfigurationInstance();
        if (threadSaved == null) {

            // the URL handler
            if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                Uri uri = Uri.parse(getIntent().getDataString());
                Integer topic_id = Integer.valueOf(uri.getQueryParameter("TID"));
                mPage = 1;
                mPid = 0;
                mThread = mObjectManager.getTopic(topic_id);
            } else {
                mThread = mObjectManager.getTopic(mExtras.getInt("TID"));
                mPid    = mExtras.getInt("PID",0);
                mPage   = mExtras.getInt("page",1);
            }
            
            new ThreadLoader().execute(mPid, mPage);
        } else {
            Orientation o = threadSaved;
            mThread = o.mTopic;
            mPage   = o.mPage;
            mHtmlCode = o.mHtmlCode;
            mPid    = o.mPid;
            
            fillView();
        }
        
    }
    
    /**
     * This is a small class to help storing objects in case of orientation change
     */
    public class Orientation {
        public Topic mTopic;
        public Integer mPage;
        public String mHtmlCode;
        public Integer mPid;
        public float mScroll;
        public Orientation(Topic t, Integer p, String s, Integer pid) {
            mTopic = t;
            mPage = p;
            mHtmlCode = s;
            mPid = pid;
        }
    }

    /**
     * Needed for orientation change
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        final Orientation o = new Orientation(mThread, mPage, mHtmlCode, mPid);
        return o;
    }

    /**
     * After downloading, shows the thread in the current activity.
     */
    private void fillView() {
        
        mWebView.loadDataWithBaseURL("file:///android_asset/", mHtmlCode, "text/html", "UTF-8", null);
        setTitle(mThread.getTitle());
        mLinearLayout.addView(getHeaderView(), 0);

        try {
            Thread.sleep(200);
            mWebView.loadUrl("javascript:scrollToElement('" + mThread.getPid() + "')");
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
        MenuInflater inflater = getMenuInflater();
        if (mObjectManager.isLoggedIn()) {
            inflater.inflate(R.menu.iconmenu_thread, menu);
            return true;
        } else {
            inflater.inflate(R.menu.iconmenu_paginate, menu);
            return true;
        }
    }

    /**
     * options menu selection handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case android.R.id.home:
            goToPresetActivity();
            return true;
        case R.id.reply:
            if (mThread.isClosed()) {
                Toast.makeText(TopicActivity.this, "Thread geschlossen.",
                        Toast.LENGTH_SHORT).show();
            } else {
                replyDialog("");
            }
            return true;
        case R.id.preferences:
            Intent intent = new Intent(this, PreferenceActivityPot.class);
            startActivity(intent);
            return true;
        case R.id.forumact:
            goToForumActivity();
            return true;
        case R.id.refresh:
            refresh();
            return true;
        case R.id.bookmarks:
            goToBookmarkActivity();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Opens dialog to reply to thread. String text is a preset text for the
     * reply textfield.
     */
    public void replyDialog(String text) {
        int orientation = getResources().getConfiguration().orientation;
        setRequestedOrientation(orientation);

        // get input box and put it in wrapper class
        LayoutInflater inflater = this.getLayoutInflater();
        View replyBox = inflater.inflate(R.layout.dialog_write, null);
        
        EditText textField = (EditText) replyBox.findViewById(R.id.replybox);
        textField.setText(text);
        textField.requestFocus();
        textField.setSelection(text.length());
        
        final DialogWrapper wrapper = new DialogWrapper(replyBox);

        // build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Antwort verfassen").setCancelable(false)
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TopicActivity.this
                                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        dialog.cancel();
                    }
                }).setPositiveButton("Senden", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).setView(replyBox);
        AlertDialog dialog = builder.create();
        dialog.show();

        // we need to overwrite the button again to keep the dialog open
        // on "cancel" click
        Button theButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new PostDialogs.PostWriter(TopicActivity.this).execute(mThread, wrapper);
            }
        });
    }

    /**
     * Nearly the same as replyDialog, but to edit the text. Expects the post to
     * edit as parameter.
     */
    public void editDialog(Post post) {
        int orientation = getResources().getConfiguration().orientation;
        setRequestedOrientation(orientation);

        // get input box and put it in wrapper class
        LayoutInflater inflater = this.getLayoutInflater();
        View replyBox = inflater.inflate(R.layout.dialog_write, null);
        
        EditText textField = (EditText) replyBox.findViewById(R.id.replybox);
        textField.setText(post.getText());
        textField.requestFocus();
        textField.setSelection(post.getText().length());
        
        EditText titleField = (EditText) replyBox.findViewById(R.id.replytitle);
        titleField.setText(post.getTitle());
        
        final DialogWrapper wrapper = new DialogWrapper(replyBox);

        // build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Post toEdit = post;
        builder.setMessage("Beitrag editieren").setCancelable(false)
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TopicActivity.this
                                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        dialog.cancel();
                    }
                }).setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).setView(replyBox);
        AlertDialog dialog = builder.create();
        dialog.show();

        // we need to overwrite the button again to keep the dialog open
        // on "cancel" click
        Button theButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new PostDialogs.PostEditer(TopicActivity.this).execute(mThread, wrapper, toEdit);
            }
        });
    }

    /**
     * returns the view that is set as header for the list
     */
    public View getHeaderView() {
        LayoutInflater inflater = this.getLayoutInflater();
        View row = inflater.inflate(R.layout.header_thread, null);

        // choose page number dialog
        Button descr = (Button) row.findViewById(R.id.buttonList);
        descr.setText(mPage + "/" + mThread.getLastPage());
        descr.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final CharSequence[] pages = new String[mThread.getLastPage()];
                // list pages
                for (int i = 0; i < pages.length; i++) {
                    pages[i] = "Seite " + (i + 1);
                    if (mPage == (i + 1)) {
                        pages[i] = pages[i] + " (aktuell)";
                    }
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(TopicActivity.this);
                builder.setTitle("Seite wählen");
                builder.setItems(pages, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        toPage(item + 1);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCancelable(true);
                dialog.show();
            }
        });

        // prev page
        Button prev = (Button) row.findViewById(R.id.buttonPrev);
        if (mPage == 1) {
            prev.setEnabled(false);
        } else {
            prev.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    showPreviousPage();
                }
            });
        }

        // next page
        Button next = (Button) row.findViewById(R.id.buttonNext);
        if (mPage == mThread.getLastPage()) {
            next.setEnabled(false);
        } else {
            next.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    showNextPage();
                }
            });
        }

        return (row);
    }

    /**
     * This method calls this same activity refreshing the view (or switching
     * the site, when thread.setPage() has been set.
     */
    public void toPage(int page) {
        Intent intent = new Intent(TopicActivity.this, TopicActivity.class);
        intent.putExtra("TID", mThread.getId());
        intent.putExtra("page", page);
        intent.putExtra("lastPage", false);
        intent.putExtra("unreadCount", 0);
        startActivity(intent);
    }

    /**
     * refresh the current view
     */
    public void refresh() {
        Intent intent = new Intent(TopicActivity.this, TopicActivity.class);
        intent.putExtra("TID", mThread.getId());
        intent.putExtra("page", mPage);
        intent.putExtra("lastPage", false);
        intent.putExtra("unreadCount", 0);
        startActivity(intent);
        finish();
    }

    /**
     * show the next page if there is one. (cycle otherwise)
     */
    public void showNextPage() {
        if (mPage < mThread.getLastPage()) {
            mPage++;
            refresh();
        } else if (mPage > 1) {
            mPage = 1;
            refresh();
        } else {
            Toast.makeText(this, "Keine weiteren Seiten vorhanden", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * show the previous page if there is one. (cycle otherwise)
     */
    public void showPreviousPage() {
        if (mPage > 1) {
            mPage--;
            refresh();
        } else if (mThread.getLastPage() > 1) {
            mPage = mThread.getLastPage();
            refresh();
        } else {
            Toast.makeText(this, "Keine weiteren Seiten vorhanden", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * go to the forum activity
     */
    protected void goToForumActivity() {
        Intent intent = new Intent(this, ForumActivity.class);
        intent.putExtra("noredirect", true);
        startActivity(intent);
    }
    
    /**
     * Go to the activity defined in the settings for the actionbar.
     */
    protected void goToPresetActivity() {
        int loc = Integer.valueOf(mSettings.getString("mataloc", "0"));
        switch (loc) {
        case 0:
            goToBookmarkActivity();
            break;
        case 2:
            Intent intent = new Intent(this, BoardActivity.class);
            intent.putExtra("BID", 14);
            intent.putExtra("page", 1);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            break;
        case 1:
        default:
            goToForumActivity();
            break;
        }
    }

    /**
     * go to the bookmark activity
     */
    protected void goToBookmarkActivity() {
        Intent intent = new Intent(this, BookmarkActivity.class);
        startActivity(intent);
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
                    mThread = mObjectManager.getTopicByPid(mThread.getId(), pid);
                    mPage   = mThread.getLastFetchedPage();
                } else {
                    mThread = mObjectManager.getTopicByPage(mThread.getId(), page, true);
                }
            } catch (Exception e) {
                this.cancel(true);
                return e;
            }
            
            // generate html code. This can take a while depending on the amount of bbcode involved...
            publishProgress("Parse Thread...");
            try {
                TopicHtmlGenerator gen = new TopicHtmlGenerator(mThread, mPage, TopicActivity.this);
                mHtmlCode = gen.buildTopic();
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
                Post post = mThread.getPosts().get(mPage)[post_id];

                switch (which) {
                
                case 0:
                    // edit post dialog (check if user is allowed first)
                    if (post.getAuthor().getId() != mObjectManager.getCurrentUser().getId()) {
                        Toast.makeText(TopicActivity.this, "Du darfst diesen Post nicht editieren.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        editDialog(post);
                    }
                    break;
                    
                case 1:
                    // reply dialog with quote
                    String text = "[quote=" + mThread.getId() + "," + post.getId() + ",\""
                            + post.getAuthor().getNick() + "\"][b]\n" + post.getText() + "\n[/b][/quote]";
                    replyDialog(text);
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

} 
