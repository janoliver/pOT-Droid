package com.mde.potdroid.helpers;

import android.content.Context;
import android.net.Uri;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.internal.Closeables;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.memory.PooledByteBufferInputStream;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Handles the downloading and caching of images.
 */
public class ImageHandler {

    private final Context mContext;
    private static boolean frescoInitialized = false;

    public ImageHandler(Context cx) {
        mContext = cx;

        getCacheDir(mContext).mkdirs();

        if(!frescoInitialized) {
            Network network = new Network(mContext);
            ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
                    .newBuilder(mContext, network.getHttpClient()).build();

            Fresco.initialize(mContext, config);
            frescoInitialized = true;
        }
    }

    /**
     * Retrieves an image either from cache or network using facebook's fresco library. Expects
     * a callback class.
     * @param url the url of the image to be retrieved.
     */
    public void retrieveImage(final String url, final ImageHandlerCallback callback) {
        final String filename = CacheContentProvider.getFilenameForCache(url);
        final File tempFile = new File(getCacheDir(mContext), filename);
        final Uri localUri = CacheContentProvider.getContentUriFromUrlOrUri(url);

        if(tempFile.exists()) {
            callback.onSuccess(url, localUri.toString());
            return;
        }

        final ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url)).
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
        dataSource.subscribe(dataSubscriber, CallerThreadExecutor.getInstance());
    }

    public static void saveInputStream(final File outFile, final PooledByteBufferInputStream is,
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

    public static File getCacheDir(Context cx) {
        return new File(cx.getExternalFilesDir(null), "fresco_cache/");

    }

    public void clearCache() {
        for (File child : getCacheDir(mContext).listFiles())
            child.delete();
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
