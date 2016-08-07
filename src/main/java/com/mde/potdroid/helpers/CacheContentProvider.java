package com.mde.potdroid.helpers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class CacheContentProvider extends ContentProvider {
    private static final String[] COLUMNS= {
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.TITLE };
    public static final Uri CONTENT_URI = Uri.parse("content://com.mde.potdroid.files/");

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return(URLConnection.guessContentTypeFromName(uri.toString()));
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {

        ImageHandler h;

        // only two possibilities.
        if(uri.toString().startsWith(CONTENT_URI + ImageHandler.BENDER_SUBDIR))
            h = ImageHandler.getBenderHandler(getContext().getApplicationContext());
        else
            h = ImageHandler.getPictureHandler(getContext().getApplicationContext());

        return h.getEntry(Utils.md5(uri.toString()));
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (projection == null) {
            projection = COLUMNS;
        }

        String[] cols = new String[projection.length];
        Object[] vals = new Object[projection.length];
        int i = 0;
        for (String col : projection) {
            if (MediaStore.MediaColumns.DISPLAY_NAME.equals(col)) {
                cols[i] = MediaStore.MediaColumns.DISPLAY_NAME;
                vals[i] = uri.getLastPathSegment();
            } else if (MediaStore.MediaColumns.MIME_TYPE.equals(col)) {
                cols[i] = MediaStore.MediaColumns.MIME_TYPE;
                vals[i] = getType(uri);
            } else if (MediaStore.MediaColumns.TITLE.equals(col)) {
                cols[i] = MediaStore.MediaColumns.TITLE;
                vals[i] = uri.getLastPathSegment();
            } else if (MediaStore.MediaColumns.SIZE.equals(col)) {
                cols[i] = MediaStore.MediaColumns.SIZE;
                vals[i] = AssetFileDescriptor.UNKNOWN_LENGTH;
            }
            i++;
        }
        cols = copyOf(cols, i);
        vals = copyOf(vals, i);
        final MatrixCursor cursor = new MatrixCursor(cols, 1);
        cursor.addRow(vals);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        throw new RuntimeException("Operation not supported");
    }

    public static Uri getContentUriFromUrlOrUri(String rawUrl, String sub_directory) {
        if(rawUrl.startsWith(CONTENT_URI.toString()))
            return Uri.parse(rawUrl);
        try {
            URL url = new URL(rawUrl.replace("%20","+"));
            if(!sub_directory.endsWith("/"))
                sub_directory += "/";
            return Uri.parse(CONTENT_URI + sub_directory + url.getHost() + url.getPath());
        } catch (MalformedURLException e) {
            Utils.printException(e);
            return null;
        }
    }

    private static String[] copyOf(String[] original, int newLength) {
        final String[] result = new String[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    private static Object[] copyOf(Object[] original, int newLength) {
        final Object[] result = new Object[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }


}