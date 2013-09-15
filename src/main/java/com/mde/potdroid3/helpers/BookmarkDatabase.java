package com.mde.potdroid3.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.mde.potdroid3.models.Bookmark;
import com.mde.potdroid3.models.BookmarkList;
import com.mde.potdroid3.models.Topic;

/**
 * Created by oli on 9/15/13.
 */
public class BookmarkDatabase {
    public static final String DATABASE_NAME = "potdroid";
    public static final String BOOKMARKS_TABLE_NAME = "bookmarks";
    private SQLiteDatabase mDatabase;
    private Context mContext;

    public BookmarkDatabase(Context cx) {
        mContext = cx;
        BookmarkDatabaseOpenHelper helper = new BookmarkDatabaseOpenHelper(mContext);
        mDatabase = helper.getWritableDatabase();
    }

    public void refresh(BookmarkList bl) {
        // clear db
        mDatabase.delete(BOOKMARKS_TABLE_NAME, null, null);

        // refresh database
        ContentValues values = new ContentValues();
        for(Bookmark bm : bl.getBookmarks()) {
            values.clear();
            values.put("id", bm.getId());
            values.put("thread_id", bm.getThread().getId());
            values.put("thread_title", bm.getThread().getTitle());
            values.put("thread_closed", bm.getThread().isClosed());
            values.put("thread_pages", bm.getThread().getNumberOfPages());
            values.put("board_id", bm.getThread().getBoard().getId());
            values.put("board_name", bm.getThread().getBoard().getName());
            values.put("post_id", bm.getLastPost().getId());

            mDatabase.insert(BOOKMARKS_TABLE_NAME, null, values);
        }
    }

    public boolean isBookmark(Topic t) {
        Cursor result = mDatabase.query( BOOKMARKS_TABLE_NAME, new String[]{"id"},
                "thread_id=?", new String[]{t.getId().toString()}, null, null, null);
        return result.getCount() > 0;
    }

    public static class BookmarkDatabaseOpenHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 2;

        private static final String BOOKMARKS_TABLE_CREATE =
                "CREATE TABLE " + BookmarkDatabase.BOOKMARKS_TABLE_NAME + " (" +
                        "id INTEGER  PRIMARY KEY, " +
                        "thread_id INTEGER, " +
                        "thread_title TEXT, " +
                        "thread_closed INTEGER, " +
                        "thread_pages INTEGER, " +
                        "board_id INTEGER, " +
                        "board_name TEXT, " +
                        "post_id INTEGER);";

        BookmarkDatabaseOpenHelper(Context context) {
            super(context, BookmarkDatabase.DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(BOOKMARKS_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            return;
        }
    }
}
