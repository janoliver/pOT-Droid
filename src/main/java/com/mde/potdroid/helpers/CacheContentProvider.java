package com.mde.potdroid.helpers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        final int lastDot = uri.toString().lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = uri.toString().substring(lastDot + 1).toLowerCase();
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

            if (mime != null) {
                Utils.log(mime);
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
        String filename = getFilenameForCache(uri.toString());
        return new File(ImageHandler.getCacheDir(getContext(), "topic_images"), filename);
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

    public static String getFilenameForCache(String url) {
        HashFileNameGenerator generator = new HashFileNameGenerator();
        return generator.generate(url);
    }

    public static class HashFileNameGenerator {

        private static final String HASH_ALGORITHM = "MD5";
        private static final int RADIX = 10 + 26; // 10 digits + 26 letters

        public String generate(String imageUri) {
            Uri uri = getContentUriFromUrlOrUri(imageUri);
            if(uri != null)
                imageUri = uri.toString();

            byte[] md5 = getMD5(imageUri.getBytes());
            BigInteger bi = new BigInteger(md5).abs();
            return bi.toString(RADIX);
        }

        private byte[] getMD5(byte[] data) {
            byte[] hash = null;
            try {
                MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
                digest.update(data);
                hash = digest.digest();
            } catch (NoSuchAlgorithmException e) {
                // nothing
            }
            return hash;
        }
    }

}