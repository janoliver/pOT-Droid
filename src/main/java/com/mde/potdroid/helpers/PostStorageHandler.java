package com.mde.potdroid.helpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

import android.util.Log;
import android.util.JsonWriter;
import android.util.JsonReader;
import android.content.Context;

import java.io.OutputStreamWriter;
import java.io.InputStreamReader;

public class PostStorageHandler {
    // Public stuff starts here.

    public PostStorageHandler(Context ctx) {
        mContext = ctx;
        readStorage();
    }

    public boolean storePost(StoredPostInfo pi) {
        if (mPosts.contains(pi)) {
            return true;
        }

        mPosts.add(pi);
        return writeStorage();
    }

    public boolean storePost(String topic, String poster, String url, int postid, int topicid, String quote) {
        StoredPostInfo pi = new StoredPostInfo(topic, poster, url, postid, topicid, quote);
        return storePost(pi);
    }

    public List<StoredPostInfo> getPosts() {
        return mPosts;
    }

    public boolean clearStorage() {
        return mContext.deleteFile(FILENAME);
    }

    public boolean deletePost(int postid, int topicid) {
        StoredPostInfo pi = new StoredPostInfo("", "", "", postid, topicid, "");
        return deletePost(pi);
    }

    public boolean deletePost(StoredPostInfo pi) {
        for (Iterator<StoredPostInfo> it = mPosts.iterator(); it.hasNext(); ) {
            StoredPostInfo cur = it.next();
            if (cur.equals(pi)) {
                it.remove();
                break;
            }
        }

        return writeStorage();
    }

    public boolean export(String directory) {
        // TODO
        return false;
    }

    public class StoredPostInfo {
        public String topic;
        public String poster;
        public String url;
        public String fullQuote;
        public int id_topic;
        public int id_post;

        public StoredPostInfo() {
            topic = "";
            poster = "";
            url = "";
            fullQuote = "";
            id_post = 0;
            id_topic = 0;
        }

        public StoredPostInfo(String t, String p, String u, int postid, int topicid, String quote) {
            topic = t;
            poster = p;
            url = u;
            fullQuote = quote;
            id_post = postid;
            id_topic = topicid;
        }

        public boolean equals(StoredPostInfo other) {
            return (this.id_topic == other.id_topic &&
                    this.id_post == other.id_post);
        }
    }

    // Private stuff starts here.

    private final String TAG = "PostStorageHandler";
    private final String FILENAME = "stored-posts.json";

    private Context mContext = null;
    private List<StoredPostInfo> mPosts = new Vector<>();

    private boolean readStorage() {
        if (mContext == null) {
            return false;
        }

        try {
            JsonReader reader = new JsonReader(new InputStreamReader(mContext.openFileInput(FILENAME)));

            mPosts.clear();

            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                StoredPostInfo pi = new StoredPostInfo();

                while (reader.hasNext()) {
                    try {
                        String name = reader.nextName();
                        if (name.equals("url")) {
                            pi.url = reader.nextString();
                        } else if (name.equals("topic")) {
                            pi.topic = reader.nextString();
                        } else if (name.equals("poster")) {
                            pi.poster = reader.nextString();
                        } else if (name.equals("id_post")) {
                            pi.id_post = reader.nextInt();
                        } else if (name.equals("id_topic")) {
                            pi.id_topic = reader.nextInt();
                        } else if (name.equals("fullQuote")) {
                            pi.fullQuote = reader.nextString();
                        }
                    } catch (IllegalStateException e) {
                        Log.e("Exception", "Illegal state: " + e.toString());
                        return false;
                    }
                }

                mPosts.add(pi);
                reader.endObject();
            }
            reader.endArray();
            reader.close();
        } catch (FileNotFoundException e) {
            Log.i("Exception", "No stored posts available yet.");
            mPosts.clear();
            return true;
        } catch (IOException e) {
            Log.e("Exception", "File read failed: " + e.toString());
            return false;
        }
        return true;
    }

    private boolean writeStorage() {
        if (mContext == null) {
            Log.e(TAG, "Context is null.");
            return false;
        }

        try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(mContext.openFileOutput(FILENAME, Context.MODE_PRIVATE)));

            writer.setIndent("  ");
            writer.beginArray();

            for (StoredPostInfo pi : mPosts) {
                writer.beginObject();
                writer.name("topic").value(pi.topic);
                writer.name("poster").value(pi.poster);
                writer.name("url").value(pi.url);
                writer.name("id_topic").value(pi.id_topic);
                writer.name("id_post").value(pi.id_post);
                writer.name("fullQuote").value(pi.fullQuote);
                writer.endObject();
            }

            writer.endArray();
            writer.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            return false;
        }
        return true;
    }
}
