package com.mde.potdroid.helpers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import com.mde.potdroid.PotDroidApplication;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

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
    private DiskCache mCache;

    @Override
    public boolean onCreate() {
        PotDroidApplication.initImageLoader(getContext());
        final ImageLoader il = ImageLoader.getInstance();
        mCache = il.getDiskCache();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final File file = getFileForUri(uri);

        final int lastDot = file.getName().lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = file.getName().substring(lastDot + 1);
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }

        return "application/octet-stream";
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        final File f = getFileForUri(getContentUriFromUrlOrUri(uri.toString()));

        if (f.exists()) {
            return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
        }

        throw new FileNotFoundException(uri.getPath());
    }

    private File getFileForUri(Uri uri) {
        return DiskCacheUtils.findInCache(uri.toString(), mCache);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final File file = getFileForUri(uri);

        if (projection == null) {
            projection = COLUMNS;
        }

        String[] cols = new String[projection.length];
        Object[] vals = new Object[projection.length];
        int i = 0;
        for (String col : projection) {
            if (MediaStore.MediaColumns.DATA.equals(col)) {
                cols[i] = MediaStore.MediaColumns.DATA;
                vals[i] = file;
            } else if (MediaStore.MediaColumns.DATE_ADDED.equals(col)) {
                cols[i] = MediaStore.MediaColumns.DATE_ADDED;
                vals[i] = file.lastModified();
            } else if (MediaStore.MediaColumns.DATE_MODIFIED.equals(col)) {
                cols[i] = MediaStore.MediaColumns.DATE_MODIFIED;
                vals[i] = file.lastModified();
            } else if (MediaStore.MediaColumns.DISPLAY_NAME.equals(col)) {
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
                vals[i] = file.length();
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

    public static Uri getContentUriFromUrlOrUri(String rawUrl) {
        if(rawUrl.startsWith(CONTENT_URI.toString()))
            return Uri.parse(rawUrl);
        try {
            URL url = new URL(rawUrl.replace("%20","+"));
            return Uri.parse(CONTENT_URI + url.getHost() + url.getPath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
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

    public static class HashFileNameGenerator extends Md5FileNameGenerator {
        @Override
        public String generate(String url) {
            Uri uri = getContentUriFromUrlOrUri(url);
            if(uri == null)
                return super.generate(url);
            return super.generate(uri.toString());
        }
    }

}