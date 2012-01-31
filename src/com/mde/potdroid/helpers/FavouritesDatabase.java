package com.mde.potdroid.helpers;

import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.mde.potdroid.models.Bookmark;

public class FavouritesDatabase {

    private static final String  DATABASE_NAME    = "potdroid";
    private static final Integer DATABASE_VERSION = 2;
    
    private FavouritesOpenHelper mFavouritesOpenHelper;
    
    public FavouritesDatabase(Context cx) {
        mFavouritesOpenHelper = new FavouritesOpenHelper(cx);
    }
    
    public void close() {
        mFavouritesOpenHelper.close();
    }
    
    public Boolean isFavourite(Bookmark bm) {
        Cursor qry = query("bm_id = ?", new String[] {bm.getId().toString()}, null);
        if(qry != null) {
            qry.close();
            return true;
        }
        return false;
    }
    
    public void deleteFavourite(Bookmark bm) {
        if(isFavourite(bm)) {
            mFavouritesOpenHelper.getWritableDatabase().delete(FavouritesOpenHelper.TABLENAME, 
                    "bm_id = ?", new String[] {bm.getId().toString()});
        }
    } 
    
    public void addFavourite(Bookmark bm) {
        if(!isFavourite(bm)) {
            ContentValues values = new ContentValues();
            values.put("bm_id", bm.getId());

            mFavouritesOpenHelper.getWritableDatabase().insert(FavouritesOpenHelper.TABLENAME, null, values);
        }
    }
    
    public void cleanFavourites(Map<Integer, Bookmark> bookmarks) {
        Cursor qry = query("", new String[] {}, null);
        Integer bmId;
        if (qry != null) {
            while (qry.isAfterLast() == false) {
                bmId = new Integer(qry.getString(0));
                if(!bookmarks.containsKey(bmId)) {
                    deleteFavourite(new Bookmark(bmId));
                }
                qry.moveToNext();
            }
        }
    }
    
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
