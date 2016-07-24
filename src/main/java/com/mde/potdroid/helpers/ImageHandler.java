package com.mde.potdroid.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import com.jakewharton.disklrucache.DiskLruCache;
import com.mde.potdroid.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Handles the downloading and caching of images.
 */
public class ImageHandler {

    private final Context mContext;
    private DiskLruCache mDiskCache;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;
    public static final int IO_BUFFER_SIZE = 8 * 1024;

    public ImageHandler(Context cx, String uniqueName, int diskCacheSize) {
        mContext = cx;

        getCacheDir(mContext, uniqueName).mkdirs();

        try {
            final File diskCacheDir = getCacheDir(mContext, uniqueName);
            mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void put(String key, BufferedSource data) {

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
            if (BuildConfig.DEBUG) {
                Log.d("cache_test_DISK_", "image put on disk cache " + key);
            }

        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.d("cache_test_DISK_", "ERROR on: image put on disk cache " + key);
            }
            try {
                if (editor != null) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }

    }

    public Bitmap getBitmap(String key) {

        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        try {

            snapshot = mDiskCache.get(key);
            if (snapshot == null) {
                return null;
            }
            final InputStream in = snapshot.getInputStream(0);
            if (in != null) {
                final BufferedInputStream buffIn =
                        new BufferedInputStream(in, IO_BUFFER_SIZE);
                bitmap = BitmapFactory.decodeStream(buffIn);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d("cache_test_DISK_", bitmap == null ? "" : "image read from disk " + key);
        }

        return bitmap;

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
        if (BuildConfig.DEBUG) {
            Log.d("cache_test_DISK_", "disk cache CLEARED");
        }
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
    public void retrieveImage(final String url, final ImageHandlerCallback callback, String uniqueName) throws IOException {
        final String filename = CacheContentProvider.getFilenameForCache(url);
        final File tempFile = new File(getCacheDir(mContext, uniqueName), filename);
        final Uri localUri = CacheContentProvider.getContentUriFromUrlOrUri(url);

        if (tempFile.exists()) {
            callback.onSuccess(url, localUri.toString());
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        put(filename, response.body().source());
        response.body().close();

        callback.onSuccess(url, localUri.toString());

        /*final ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url)).
                setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH).build();
        DataSource<CloseableReference<PooledByteBuffer>> dataSource =
                Fresco.getImagePipeline().fetchEncodedImage(request, mContext);

        DataSubscriber<CloseableReference<PooledByteBuffer>> dataSubscriber =
                new BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {
                    @Override
                    protected void onNewResultImpl(
                            DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                        if (!dataSource.isFinished()) {
                            return;
                        }

                        CloseableReference<PooledByteBuffer> buffRef = dataSource.getResult();
                        if (buffRef != null) {
                            PooledByteBufferInputStream is = new PooledByteBufferInputStream(buffRef.get());
                            try {
                                saveInputStream(tempFile, is, new FileSaverCallback() {
                                    @Override
                                    public void onSuccess() {
                                        callback.onSuccess(url, localUri.toString());
                                    }

                                    @Override
                                    public void onFailure() {
                                        callback.onFailure(url);
                                    }
                                });
                            } finally {
                                Closeables.closeQuietly(is);
                                CloseableReference.closeSafely(buffRef);
                            }
                        }
                    }

                    @Override
                    protected void onFailureImpl(
                            DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                        callback.onFailure(url);
                    }
                };
        dataSource.subscribe(dataSubscriber, CallerThreadExecutor.getInstance());*/
    }

    /*public static void saveInputStream(final File outFile, final PooledByteBufferInputStream is,
                                       FileSaverCallback callback) {
        try {
            OutputStream output = new FileOutputStream(outFile);

            try {
                byte[] buffer = new byte[4 * 1024];
                int read;

                while ((read = is.read(buffer)) != -1)
                    output.write(buffer, 0, read);

                output.flush();
            } finally {
                output.close();

                callback.onSuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure();
        }
    }
*/
    public static File getCacheDir(Context cx, String uniqueName) {
        return new File(cx.getExternalFilesDir(null), uniqueName);
    }

    public interface ImageHandlerCallback {
        void onSuccess(final String url, final String path);

        void onFailure(final String url);
    }

    private interface FileSaverCallback {
        void onSuccess();

        void onFailure();
    }
}
