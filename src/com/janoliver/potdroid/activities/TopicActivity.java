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

import java.io.IOException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import android.widget.TextView;
import android.widget.Toast;

import com.janoliver.potdroid.R;
import com.janoliver.potdroid.baseclasses.BaseActivity;
import com.janoliver.potdroid.helpers.DialogWrapper;
import com.janoliver.potdroid.helpers.PotNotification;
import com.janoliver.potdroid.helpers.PotUtils;
import com.janoliver.potdroid.helpers.TopicHtmlGenerator;
import com.janoliver.potdroid.models.Post;
import com.janoliver.potdroid.models.Topic;

/**
 * Shows a thread. By far the most involving activity. And, of course, the most
 * important one.
 */
public class TopicActivity extends BaseActivity {

    private WebView   mWebView;
    private ViewGroup mLinearLayout;
    private Topic     mThread;
    private Integer   mContextMenuInfo;
    private Integer   mPage            = 1;
    private Integer   mPid             = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWebsiteInteraction = PotUtils.getWebsiteInteractionInstance(this);

        // set view
        setContentView(R.layout.topic);
        mLinearLayout = (ViewGroup) findViewById(R.id.linlayout);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        JSInterface myJsInterface = new JSInterface(mWebView);
        mWebView.addJavascriptInterface(myJsInterface, "JSInterface");
        mWebView.loadData("<html><body></body></html>", "text/html", "utf-8");

        // was only the orientation changed?
        final Topic threadSaved = (Topic) getLastNonConfigurationInstance();
        if (threadSaved == null) {
            // scroll to post
            Bundle extras = getIntent().getExtras();

            mThread = mObjectManager.getTopic(extras.getInt("TID"));
            mPid    = extras.getInt("PID",0);
            mPage   = extras.getInt("page",1);
            
            new ThreadLoader().execute(mPid, mPage);
        } else {
            mThread = threadSaved;
            showThread();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        final Topic threadSaved = mThread;
        return threadSaved;
    }

    private void showThread() {
        TopicHtmlGenerator gen      = new TopicHtmlGenerator(mThread, mPage, TopicActivity.this);
        String             htmlCode = "";
        
        try {
            htmlCode = gen.buildTopic();
        } catch (IOException e1) {
            Toast.makeText(TopicActivity.this, "HTML Generierung schiefgelaufen.",
                    Toast.LENGTH_SHORT).show();
        }

        mWebView.loadDataWithBaseURL("file:///android_asset/", htmlCode, "text/html", "UTF-8", null);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.reply:
            if (mThread.getNewreplytoken() == null) {
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_post, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Post post = mThread.getPosts().get(mPage)[mContextMenuInfo];

        switch (item.getItemId()) {
        case R.id.edit:
            // edit post dialog (check if user is allowed first)
            if (post.getAuthor().getId() != mObjectManager.getCurrentUser().getId()) {
                Toast.makeText(TopicActivity.this, "Du darfst diesen Post nicht editieren.",
                        Toast.LENGTH_SHORT).show();
            } else {
                editDialog(post);
                return true;
            }
            return true;
        case R.id.cite:
            // reply dialog with quote
            String text = "[quote=" + mThread.getId() + "," + post.getId() + ",\""
                    + post.getAuthor().getNick() + "\"][b]\n" + post.getText() + "\n[/b][/quote]";
            replyDialog(text);
            return true;
        case R.id.bookmark:
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

            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    /**
     * Opens dialog to reply to thread. String text is a preset text for the
     * reply textfield.
     * 
     * @param text
     */
    public void replyDialog(String text) {
        int orientation = getResources().getConfiguration().orientation;
        setRequestedOrientation(orientation);

        // get input box and put it in wrapper class
        LayoutInflater inflater = this.getLayoutInflater();
        View replyBox = inflater.inflate(R.layout.dialog_write, null);
        TextView textField = (TextView) replyBox.findViewById(R.id.replybox);
        textField.setText(text);
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
                mWebsiteInteraction.new PostWriter(TopicActivity.this).execute(mThread, wrapper);
            }
        });
    }

    /**
     * Nearly the same as replyDialog, but to edit the text. Expects the post to
     * edit as parameter.
     * 
     * @param post
     */

    public void editDialog(Post post) {
        int orientation = getResources().getConfiguration().orientation;
        setRequestedOrientation(orientation);

        // get input box and put it in wrapper class
        LayoutInflater inflater = this.getLayoutInflater();
        View replyBox = inflater.inflate(R.layout.dialog_write, null);
        TextView textField = (TextView) replyBox.findViewById(R.id.replybox);
        textField.setText(post.getText());
        TextView titleField = (TextView) replyBox.findViewById(R.id.replytitle);
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
                mWebsiteInteraction.new PostEditer(TopicActivity.this).execute(mThread, wrapper,
                        toEdit);
            }
        });

    }

    /**
     * returns the view that is set as header for the list
     * 
     * @return View header
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

    public void refresh() {
        Intent intent = new Intent(TopicActivity.this, TopicActivity.class);
        intent.putExtra("TID", mThread.getId());
        intent.putExtra("page", mPage);
        intent.putExtra("lastPage", false);
        intent.putExtra("unreadCount", 0);
        startActivity(intent);
        finish();
    }

    public void showNextPage() {
        if ((mPage * mThread.getPostsPerPage()) < mThread.getNumberOfPosts()) {
            mPage++;
            refresh();
        } else if (mPage > 1) {
            mPage = 1;
            refresh();
        } else {
            Toast.makeText(this, "Keine weiteren Seiten vorhanden", Toast.LENGTH_SHORT).show();
        }
    }

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

    protected void goToForumActivity() {
        Intent intent = new Intent(this, ForumActivity.class);
        intent.putExtra("noredirect", true);
        startActivity(intent);
    }

    protected void goToBookmarkActivity() {
        Intent intent = new Intent(this, BookmarkActivity.class);
        startActivity(intent);
    }

    class ThreadLoader extends AsyncTask<Integer, Object, Void> {

        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = new PotNotification(TopicActivity.this, this, true);
            mDialog.setMessage("Lade Thread...");
            mDialog.show();
        }

        @Override
        protected Void doInBackground(Integer ... args) {
            // first argument is 0 or pid, second argument is 0 or page
            Integer page = args[1];
            Integer pid  = args[0];
            
            if(pid > 0) {
                mThread = mObjectManager.getTopicByPid(mThread.getId(), pid);
                mPage   = mThread.getLastPage();
            } else {
                mThread = mObjectManager.getTopicByPage(mThread.getId(), page, true);
            }
            if (mThread == null) {
                Toast.makeText(TopicActivity.this, "Verbindungsfehler!", Toast.LENGTH_LONG).show();
                this.cancel(true);
                mDialog.dismiss();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            showThread();
            mDialog.dismiss();
        }
    }

    public class JSInterface {

        // private WebView mAppView;

        public JSInterface(WebView appView) {
            // this.mAppView = appView;
        }

        public void showPostContextMenu(String id) {
            id = id.substring(4);
            mContextMenuInfo = new Integer(id).intValue();
            registerForContextMenu(mWebView);
            openContextMenu(mWebView);
            unregisterForContextMenu(mWebView);
        }

        public void showToast(String msg) {
            Toast.makeText(TopicActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
        
        public boolean gravityOn() {
            return PreferenceManager.
                    getDefaultSharedPreferences(TopicActivity.this).
                    getBoolean("gravityInThreads", false);
        }

    }

}
