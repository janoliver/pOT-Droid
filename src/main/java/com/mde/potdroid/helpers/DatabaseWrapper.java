package com.mde.potdroid.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.mde.potdroid.models.*;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by oli on 9/15/13.
 */
public class DatabaseWrapper {
    public static final String DATABASE_NAME = "potdroid";
    public static final String BOOKMARKS_TABLE_NAME = "bookmarks";
    public static final String BENDER_TABLE_NAME = "benders";
    private SQLiteDatabase mDatabase;
    private Context mContext;

    public DatabaseWrapper(Context cx) {
        mContext = cx;
        BookmarkDatabaseOpenHelper helper = BookmarkDatabaseOpenHelper.getInstance(mContext);
        mDatabase = helper.getWritableDatabase();
    }

    public void refreshBookmarks(ArrayList<Bookmark> list) {
        // clear db
        try {
            mDatabase.beginTransaction();
            mDatabase.delete(BOOKMARKS_TABLE_NAME, null, null);

            // refresh database
            ContentValues values = new ContentValues();
            for(Bookmark bm : list) {
                values.clear();
                values.put("id", bm.getId());
                values.put("remove_token", bm.getRemovetoken());
                values.put("number_new_posts", bm.getNumberOfNewPosts());
                values.put("thread_id", bm.getThread().getId());
                values.put("thread_title", bm.getThread().getTitle());
                values.put("thread_closed", bm.getThread().isClosed());
                values.put("thread_pages", bm.getThread().getNumberOfPages());
                values.put("board_id", bm.getThread().getBoard().getId());
                values.put("board_name", bm.getThread().getBoard().getName());
                values.put("post_id", bm.getLastPost().getId());

                mDatabase.insert(BOOKMARKS_TABLE_NAME, null, values);
            }

            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    public ArrayList<Bookmark> getBookmarks() {

        ArrayList<Bookmark> ret = new ArrayList<Bookmark>();

        Cursor c = mDatabase.query(BOOKMARKS_TABLE_NAME, null, null, null, null, null,
                "board_id, thread_title");

        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {

                Bookmark b = new Bookmark(c.getInt(c.getColumnIndex("id")));
                b.setRemovetoken(c.getString(c.getColumnIndex("remove_token")));
                b.setNumberOfNewPosts(c.getInt(c.getColumnIndex("number_new_posts")));

                Board board = new Board(c.getInt(c.getColumnIndex("board_id")));
                board.setName(c.getString(c.getColumnIndex("board_name")));

                Topic thread = new Topic(c.getInt(c.getColumnIndex("thread_id")));
                thread.setTitle(c.getString(c.getColumnIndex("thread_title")));
                thread.setIsClosed(c.getInt(c.getColumnIndex("thread_closed")) == 1);
                thread.setNumberOfPages(c.getInt(c.getColumnIndex("thread_pages")));

                Post post = new Post(c.getInt(c.getColumnIndex("post_id")));

                post.setTopic(thread);
                thread.setBoard(board);
                b.setThread(thread);
                b.setLastPost(post);

                ret.add(b);
                c.moveToNext();
            }
        } finally {
            c.close();
        }

        return ret;
    }

    public void updateBender(int id, int user_id, String filename, Date last_seen) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("user_id", user_id);
        values.put("bender_filename", filename);
        values.put("last_seen", last_seen.getTime() * 1000.);
        mDatabase.replace(BENDER_TABLE_NAME, null, values);
    }

    public boolean isBookmark(Topic t) {
        Cursor result = mDatabase.query( BOOKMARKS_TABLE_NAME, new String[]{"id"},
                "thread_id=?", new String[]{t.getId().toString()}, null, null, null);

        Boolean is_bookmark = false;
        try {
            is_bookmark = result.getCount() > 0;
        } finally {
            result.close();
        }

        return is_bookmark;
    }

    public Boolean setCurrentBenderInformation(User u) {
        Cursor c = mDatabase.query(BENDER_TABLE_NAME, new String[] {"id", "bender_filename"},
                "user_id = ?", new String[] {u.getId().toString()}, null, null, "last_seen desc");

        Boolean success = false;
        try {
            if(c.getCount() > 0) {
                c.moveToFirst();
                u.setAvatarId(c.getInt(c.getColumnIndex("id")));
                u.setAvatarFile(c.getString(c.getColumnIndex("bender_filename")));
                success = true;
            }
        } finally {
            c.close();
        }

        return success;
    }

    public static class BookmarkDatabaseOpenHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 4;
        private static BookmarkDatabaseOpenHelper mInstance = null;

        private static final String BOOKMARKS_TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + DatabaseWrapper.BOOKMARKS_TABLE_NAME + " (" +
                        "id INTEGER  PRIMARY KEY, " +
                        "number_new_posts INTEGER, " +
                        "thread_id INTEGER, " +
                        "thread_title TEXT, " +
                        "thread_closed INTEGER, " +
                        "thread_pages INTEGER, " +
                        "board_id INTEGER, " +
                        "board_name TEXT, " +
                        "remove_token TEXT, " +
                        "post_id INTEGER);";

        private static final String BENDER_TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + DatabaseWrapper.BENDER_TABLE_NAME + " (" +
                        "id INTEGER PRIMARY KEY, " +
                        "user_id INTEGER, " +
                        "bender_filename TEXT, " +
                        "last_seen INTEGER);";

        private BookmarkDatabaseOpenHelper(Context context) {
            super(context, DatabaseWrapper.DATABASE_NAME, null, DATABASE_VERSION);
        }

        public static BookmarkDatabaseOpenHelper getInstance(Context ctx) {

            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            // See this article for more information: http://bit.ly/6LRzfx
            if (mInstance == null) {
                mInstance = new BookmarkDatabaseOpenHelper(ctx.getApplicationContext());
            }
            return mInstance;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(BOOKMARKS_TABLE_CREATE);
            db.execSQL(BENDER_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(BOOKMARKS_TABLE_CREATE);
            db.execSQL(BENDER_TABLE_CREATE);
        }
    }
}
