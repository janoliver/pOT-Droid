package com.mde.potdroid.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import com.mde.potdroid.helpers.cache.DiskLruCache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.*;
import java.net.MalformedURLException;

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

    public void clearCache() {
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
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
                    if (snapshot == null) {
                        DiskLruCache.Editor editor = mDiskCache.edit(key);
                        if (editor == null)
                            return;

                        BufferedSink sink = null;
                        try {
                            out = editor.newOutputStream(0);
                            sink = Okio.buffer(Okio.sink(out));
                            sink.writeAll(data);
                            sink.close();
                        } catch(Exception e) {
                            editor.abort();
                        } finally {
                            if (sink != null)
                                sink.close();
                            editor.commit();
                        }
                    }
                } catch (IOException | IllegalStateException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null)
                            out.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }

    }

    public ParcelFileDescriptor getEntry(String key) {
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
                    DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
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
                    DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
                    return (snapshot != null);
                } catch (IOException | IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;

    }

    /**
     * Retrieves an image either from cache or network using facebook's fresco library. Expects
     * a callback class.
     *
     * @param url the url of the image to be retrieved.
     */
    public void retrieveImage(final String url, final ImageHandlerCallback callback) {
        retrieveImage(url, null, callback);
    }

    public void retrieveImage(final String url, final String referer, final ImageHandlerCallback callback) {
        final Uri localUri;
        try {
            localUri = CacheContentProvider.getContentUriFromUrlOrUri(url, mDir);
        } catch (MalformedURLException e) {
            callback.onFailure(url);
            return;
        }
        final String cacheKey = Utils.md5(localUri.toString());

        if (containsKey(cacheKey)) {
            callback.onSuccess(url, localUri.toString(), true);
            return;
        }

        Network network = new Network(mContext);
        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (referer != null && referer.trim().length() > 0) {
            requestBuilder.header("Referer", referer);
        }
        Request request = requestBuilder.build();
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

    public static void downloadImage(final Context cx, File dir, final Uri uri, final ImageDownloadCallback callback) {
        ImageHandler ih = ImageHandler.getPictureHandler(cx.getApplicationContext());

        try {
            ih.retrieveImage(uri.toString(), new ImageHandler.ImageHandlerCallback() {
                @Override
                public void onSuccess(String url, final String path, boolean from_cache) {
                    InputStream is = null;
                    OutputStream os = null;
                    try {
                        is = cx.getContentResolver().openInputStream(Uri.parse(path));

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            // Modern way: MediaStore (Android 10+)
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.MediaColumns.DISPLAY_NAME, uri.getLastPathSegment());
                            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/*");
                            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/pOT-Droid");

                            Uri imageUri = cx.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                            if (imageUri != null) {
                                os = cx.getContentResolver().openOutputStream(imageUri);
                            }
                        } else {
                            // Legacy way: File API (Pre-Android 10)
                            if (!dir.exists()) dir.mkdirs();
                            File file = new File(dir, uri.getLastPathSegment());
                            os = new FileOutputStream(file);
                        }

                        if (os != null && is != null) {
                            byte[] buffer = new byte[1024];
                            int len1;
                            while ((len1 = is.read(buffer)) > 0) {
                                os.write(buffer, 0, len1);
                            }
                            os.close();
                            callback.onSuccess(uri, null);
                        } else {
                            callback.onFailure(uri, new IOException("Failed to save image"));
                        }
                    } catch (IOException e) {
                        callback.onFailure(uri, e);
                    } finally {
                        try { if (is != null) is.close(); } catch (IOException ignored) {}
                        try { if (os != null) os.close(); } catch (IOException ignored) {}
                    }
                }

                @Override
                public void onFailure(String url) {
                    callback.onFailure(uri, null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getImagePathIfExists(final String url) throws MalformedURLException {
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

    public interface ImageDownloadCallback {
        void onSuccess(final Uri uri, final File f);

        void onFailure(final Uri uri, Exception e);
    }
}
