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

package com.mde.potdroid.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.mde.potdroid.models.Bookmark;

/**
 * This class is a helper for the sqlite database to store the favourited
 * bookmarks. So far, this is not a very good implementation. Also, the Bookmark
 * model should be involved more... 
 */
public class FavouritesDatabase {

    private static final String  DATABASE_NAME    = "potdroid";
    private static final Integer DATABASE_VERSION = 2;
    
    // this is the SqliteOpenHelper class we define below
    private FavouritesOpenHelper mFavouritesOpenHelper;
    
    /**
     * Constructor. Instantiate the sqliteOpenHelper.
     */
    public FavouritesDatabase(Context cx) {
        mFavouritesOpenHelper = new FavouritesOpenHelper(cx);
    }
    
    /**
     * close the db.
     */
    public void close() {
        mFavouritesOpenHelper.close();
    }
    
    /**
     * Check if a Bookmark is marked as favourite or not.
     */
    public Boolean isFavourite(Bookmark bm) {
        Cursor qry = query("bm_id = ?", new String[] {bm.getId().toString()}, null);
        if(qry != null) {
            qry.close();
            return true;
        }
        return false;
    }
    
    /**
     * Delete a bookmark from the favourites database.
     */
    public void deleteFavourite(Bookmark bm) {
        if(isFavourite(bm)) {
            mFavouritesOpenHelper.getWritableDatabase().delete(FavouritesOpenHelper.TABLENAME, 
                    "bm_id = ?", new String[] {bm.getId().toString()});
        }
    } 
    
    /**
     * Add a bookmark to the favourites database.
     */
    public void addFavourite(Bookmark bm) {
        if(!isFavourite(bm)) {
            ContentValues values = new ContentValues();
            values.put("bm_id", bm.getId());

            mFavouritesOpenHelper.getWritableDatabase().insert(FavouritesOpenHelper.TABLENAME, null, values);
        }
    }
    
    /**
     * Clean all the entries from the favourites database, that are not bookmarks
     * anymore so we don't end up with a messy database.
     */
    public void cleanFavourites(Bookmark[] bookmarks) {
        Cursor qry = query("", new String[] {}, null);
        Integer bmId;
        Boolean found;
        if (qry != null) {
            while (qry.isAfterLast() == false) {
                bmId = Integer.valueOf(qry.getString(0));
                found = false;
                for(Bookmark bm: bookmarks)
                    if(bm.getId() == bmId)
                        found = true;
                
                if(!found)
                    deleteFavourite(new Bookmark(bmId));
                
                qry.moveToNext();
            }
        }
    }
    
    /**
     * A query helper. selection is the condition, selectionArgs will replace
     * ? that appear in the condition and columns are the columns to be fetched.
     */
    private Cursor query(String selection, 
            String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FavouritesOpenHelper.TABLENAME);
        
        Cursor cursor = builder.query(mFavouritesOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }
    
    /**
     * The favourites open helper. This class creates the database if
     * it doesn't exist.
     */
    public class FavouritesOpenHelper extends SQLiteOpenHelper {
        public static final String TABLENAME = "favourites";
        
        FavouritesOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLENAME + " (bm_id INT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {}
    }
}
