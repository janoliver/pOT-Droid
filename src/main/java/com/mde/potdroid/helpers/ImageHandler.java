package com.mde.potdroid.helpers;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import com.mde.potdroid.helpers.cache.DiskLruCache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.File;
import java.io.IOException;

/**
 * Handles the downloading and caching of images.
 */
public class ImageHandler {

    private final Context mContext;
    private final String mDir;
    private final int mCacheSize;
    private DiskLruCache mDiskCache;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;

    private boolean mHttpDiskCacheStarting = true;
    private final Object mHttpDiskCacheLock = new Object();

    public static final String BENDER_SUBDIR = "benders";
    public static final String PICTURE_SUBDIR = "pictures";

    private static ImageHandler mCachedBenderHandler;
    private static ImageHandler mCachedPictureHandler;

    public static synchronized ImageHandler getPictureHandler(Context context) {
        if (mCachedPictureHandler == null) {
            SettingsWrapper settings = new SettingsWrapper(context);
            mCachedPictureHandler = new ImageHandler(context.getApplicationContext(), PICTURE_SUBDIR, settings.getCacheSize());
        }
        return mCachedPictureHandler;
    }

    public static synchronized ImageHandler getBenderHandler(Context context) {
        if (mCachedBenderHandler == null) {
            SettingsWrapper settings = new SettingsWrapper(context);
            mCachedBenderHandler = new ImageHandler(context.getApplicationContext(), BENDER_SUBDIR, settings.getBenderCacheSize());
        }
        return mCachedBenderHandler;
    }

    private ImageHandler(Context cx, String uniqueName, int diskCacheSize) {
        mContext = cx;
        mDir = uniqueName;
        mCacheSize = diskCacheSize;

        initCache();
    }

    public void initCache() {
        File cacheDir = getCacheDir(mContext, mDir);
        if (!cacheDir.exists())
            cacheDir.mkdirs();

        synchronized (mHttpDiskCacheLock) {
            try {
                mDiskCache = DiskLruCache.open(cacheDir, APP_VERSION, VALUE_COUNT, mCacheSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mHttpDiskCacheStarting = false;
            mHttpDiskCacheLock.notifyAll();
        }
    }

    protected void clearCacheInternal() {
        synchronized (mHttpDiskCacheLock) {
            if (mDiskCache != null && !mDiskCache.isClosed()) {
                try {
                    mDiskCache.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mDiskCache = null;
                mHttpDiskCacheStarting = true;
                initCache();
            }
        }
    }

    protected void flushCacheInternal() {
        synchronized (mHttpDiskCacheLock) {
            if (mDiskCache != null) {
                try {
                    mDiskCache.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void closeCacheInternal() {
        synchronized (mHttpDiskCacheLock) {
            if (mDiskCache != null) {
                try {
                    if (!mDiskCache.isClosed()) {
                        mDiskCache.close();
                        mDiskCache = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    public synchronized ParcelFileDescriptor getEntry(String key) {
        DiskLruCache.Snapshot snapshot;
        synchronized (mHttpDiskCacheLock) {
            // Wait for disk cache to initialize
            while (mHttpDiskCacheStarting) {
                try {
                    mHttpDiskCacheLock.wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            if (mDiskCache != null) {
                try {
                    snapshot = mDiskCache.get(key);
                    if (snapshot != null) {
                        return ParcelFileDescriptor.open(snapshot.getFile(0), ParcelFileDescriptor.MODE_READ_ONLY);
                    } else {
                        return null;
                    }
                } catch (IOException | IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public boolean containsKey(String key) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot;
        try {
            snapshot = mDiskCache.get(key);
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contained;

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
            callback.onSuccess(url, localUri.toString(), true);
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
                callback.onSuccess(url, localUri.toString(), false);
            }
        });

    }

    public String getImagePathIfExists(final String url) {
        final Uri localUri = CacheContentProvider.getContentUriFromUrlOrUri(url, mDir);
        final String cacheKey = Utils.md5(localUri.toString());

        if (containsKey(cacheKey)) {
            return localUri.toString();
        }

        return null;
    }

    public static File getCacheDir(Context cx, String uniqueName) {
        return new File(cx.getExternalFilesDir(null), uniqueName);
    }

    public interface ImageHandlerCallback {
        void onSuccess(final String url, final String path, boolean from_cache);

        void onFailure(final String url);
    }
}
