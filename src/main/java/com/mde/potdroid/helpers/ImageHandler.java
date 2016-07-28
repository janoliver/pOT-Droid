package com.mde.potdroid.helpers;

import android.content.Context;
import android.net.Uri;
import com.jakewharton.disklrucache.DiskLruCache;
import okhttp3.*;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Handles the downloading and caching of images.
 */
public class ImageHandler {

    private final Context mContext;
    private final String mDir;
    private DiskLruCache mDiskCache;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;

    public static final String BENDER_SUBDIR = "benders";
    public static final String PICTURE_SUBDIR = "pictures";

    private static ImageHandler mCachedBenderHandler;
    private static ImageHandler mCachedPictureHandler;

    public static synchronized ImageHandler getPictureHandler(Context context) {
        if (mCachedPictureHandler == null) {
            mCachedPictureHandler = new ImageHandler(context.getApplicationContext(), PICTURE_SUBDIR, 1024 * 1024 * 50);
        }
        return mCachedPictureHandler;
    }

    public static synchronized ImageHandler getBenderHandler(Context context) {
        if (mCachedBenderHandler == null) {
            mCachedBenderHandler = new ImageHandler(context.getApplicationContext(), BENDER_SUBDIR, 1024 * 1024 * 50);
        }
        return mCachedBenderHandler;
    }

    public ImageHandler(Context cx, String uniqueName, int diskCacheSize) {
        mContext = cx;
        mDir = uniqueName;

        getCacheDir(mContext, mDir).mkdirs();

        try {
            final File diskCacheDir = getCacheDir(mContext, mDir);
            mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void put(String key, BufferedSource data) {

        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit(key);
            if (editor == null) {
                return;
            }

            BufferedSink sink = null;
            try {
                sink = Okio.buffer(Okio.sink(editor.newOutputStream(0)));
                sink.writeAll(data);
                sink.close();
            } finally {
                sink.close();
            }

            mDiskCache.flush();
            editor.commit();

        } catch (IOException e) {
            try {
                if (editor != null) {
                    editor.abort();
                }
            } catch (IOException ignored) {
                // ignored
            }
        }

    }

    public synchronized InputStream getEntry(String key) {
        InputStream in = null;
        try {
            DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
            if (snapshot == null) {
                return null;
            }
            in = snapshot.getInputStream(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return in;
    }

    public boolean containsKey(String key) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get(key);
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        return contained;

    }

    public void clearCache() {
        try {
            mDiskCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }

    /**
     * Retrieves an image either from cache or network using facebook's fresco library. Expects
     * a callback class.
     *
     * @param url the url of the image to be retrieved.
     */
    public void retrieveImage(final String url, final ImageHandlerCallback callback) throws IOException {
        final Uri localUri = CacheContentProvider.getContentUriFromUrlOrUri(url, mDir);
        final String cacheKey = Utils.md5(localUri.toString());

        if (containsKey(cacheKey)) {
            callback.onSuccess(url, localUri.toString());
            return;
        }

        Network network = new Network(mContext);
        Request request = new Request.Builder().url(url).build();
        network.getHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(url);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                put(cacheKey, response.body().source());
                response.body().close();
                callback.onSuccess(url, localUri.toString());
            }
        });

    }

    public static File getCacheDir(Context cx, String uniqueName) {
        return new File(cx.getExternalFilesDir(null), uniqueName);
    }

    public interface ImageHandlerCallback {
        void onSuccess(final String url, final String path);

        void onFailure(final String url);
    }
}
