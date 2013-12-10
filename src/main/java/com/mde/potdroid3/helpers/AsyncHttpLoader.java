package com.mde.potdroid3.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.Loader;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.apache.http.Header;

import java.io.UnsupportedEncodingException;

/**
 * This class provides a Loader to asynchroneously load POST or GET requests from the web.
 * One should extend this class to provide the abstract method "parseContent", in which some
 * postprocessing can be done. This, as well as the loading, runs asynchroneously in a separate
 * thread.
 *
 * The HTTP Client from the Network class is used, so that headers and cookies are in place.
 */
public abstract class AsyncHttpLoader<E> extends Loader<E> {

    // request type codes
    public static final Integer GET = 0;
    public static final Integer POST = 1;
    public static final String DEFAULT_ENCODING = "UTF-8";

    // the instance of the Network class
    protected Network mNetwork;

    // request URL (without (!) the Network.BASE_URL part)
    protected String mRequestUrl;

    // request mode, GET or POST (see above)
    protected Integer mMode;

    // the parameters for the request, mostly required for POST requests
    protected RequestParams mParams;

    // the encoding to use when decoding the response
    protected String mEncoding;

    // the data cache
    protected E mData;

    // the process response task container
    protected ResponseTask mCurrentTask;

    /**
     * This is the respoonse handler of the asynchroneous network call, from the
     * android-async-http library.
     */
    private AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {

        /**
         * Try to decode the responseBody and trigger the post processing with
         * processResponse
         */
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String stringResult;

            try {
                stringResult = new String(responseBody, mEncoding);
            } catch (UnsupportedEncodingException e) {
                stringResult = new String(responseBody);
            }

            AsyncHttpLoader.this.processResponse(stringResult);
        }

        /**
         * the following few methods simply forward their calls to the API of
         * the AsyncHttpLoader class.
         */
        @Override
        public void onStart() {
            AsyncHttpLoader.this.onNetworkStarted();
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                              Throwable error)
        {
            String stringResult;

            // try to decode the response again
            try {
                stringResult = new String(responseBody, mEncoding);
            } catch (UnsupportedEncodingException e) {
                stringResult = new String(responseBody);
            } catch (NullPointerException e) {
                stringResult = "";
            }

            AsyncHttpLoader.this.onNetworkFailure(statusCode, headers, stringResult, error);
        }

        @Override
        public void onRetry() {
            AsyncHttpLoader.this.onNetworkRetry();
        }

        @Override
        public void onProgress(int bytesWritten, int totalSize) {
            AsyncHttpLoader.this.onNetworkProgress(bytesWritten, totalSize);
        }

        @Override
        public void onFinish() {
            AsyncHttpLoader.this.onNetworkFinished();
        }
    };

    /**
     * This AsyncTask calls the processNetworkResponse in a separate thread.
     */
    protected class ResponseTask extends AsyncTask<String, Void, E> {

        @Override
        protected void onPreExecute() {
            onProcessingStarted();
        }

        @Override
        protected void onCancelled() {
            onProcessingCancelled();
        }

        protected E doInBackground(String ... response) {
            return processNetworkResponse(response[0]);
        }

        protected void onPostExecute(E result) {
            onProcessingFinished();
            mData = result;
            deliverResult(mData);
        }
    };

    /**
     * Constructor
     * @param context The application context to use
     * @param url The request URL WITHOUT the base (forum.mods.de/bb)
     */
    public AsyncHttpLoader(Context context, String url) {
        this(context, url, GET, null);
    }

    /**
     * Constructor
     * @param context The application context to use
     * @param url The request URL WITHOUT the base (forum.mods.de/bb)
     * @param mode The request mode, GET or POST
     */
    public AsyncHttpLoader(Context context, String url, Integer mode) {
        this(context, url, mode, null);
    }

    /**
     * Constructor
     * @param context The application context to use
     * @param url The request URL WITHOUT the base (forum.mods.de/bb)
     * @param mode The request mode, GET or POST
     * @param params Request params, mostly for POST
     */
    public AsyncHttpLoader(Context context, String url, Integer mode, RequestParams params) {
        this(context, url, mode, params, DEFAULT_ENCODING);
    }

    /**
     * Constructor
     * @param context The application context to use
     * @param url The request URL WITHOUT the base (forum.mods.de/bb)
     * @param mode The request mode, GET or POST
     * @param params Request params, mostly for POST
     * @param encoding Which encoding to use to decode the response
     */
    public AsyncHttpLoader(Context context, String url, Integer mode, RequestParams params,
                           String encoding) {
        super(context);

        mRequestUrl = url;
        mNetwork = new Network(getContext());
        mMode = mode;
        mParams = params;
        mEncoding = encoding;
    }

    /**
     * Update the request URL.
     * @param url the new requestb url
     */
    public void setUrl(String url) {
        mRequestUrl = url;
    }

    /**
     * Update the request parameters
     * @param p The new request parameters.
     */
    public void setParams(RequestParams p) {
        mParams = p;
    }

    /**
     * Update the encoding
     * @param encoding The new encoding.
     */
    public void setEncoding(String encoding) {
        mEncoding = encoding;
    }

    /**
     * Start the AsyncTask to process the response.
     * @param response The response string
     */
    protected void processResponse(String response) {
        new ResponseTask().execute(response);
    }

    /**
     * Overridden method from the Loader class. Take care of initializing
     * everything and, if no data is present, start loading with forceLoad().
     */
    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if(mData != null)
            deliverResult(mData);

        if(mNetwork == null)
            mNetwork = new Network(getContext());

        if(mData == null)
            forceLoad();
    }

    /**
     * Stop loading by trying to cancel both the Network request and the AsyncTask of parsing
     */
    @Override
    protected void onStopLoading() {
        super.onStopLoading();

        mNetwork.cancelLoad();
        //mProcessResponseTask.cancel(true);
    }

    /**
     * Reset the loader, delete all the variable stuff, that is not set by the constructor.
     */
    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        mData = null;
        mNetwork = null;
    }

    /**
     * Actually submit the network request.
     */
    @Override
    protected void onForceLoad() {
        super.onForceLoad();

        if(mMode.equals(GET))
            mNetwork.get(mRequestUrl, mParams, mHandler);
        else if(mMode.equals(POST))
            mNetwork.post(mRequestUrl, mParams, mHandler);
    }

    /**
     * This method must be implemented by the user subclassing the Loader. It takes teh
     * network response String and must return an Object E.
     * @param response The response HTML/XML/whatever.
     * @return The generated Object E
     */
    protected abstract E processNetworkResponse(String response);

    /**
     * Called upon failure of the network Request.
     * @param statusCode the HTTP status Code
     * @param headers The header list
     * @param responseBody The response Body, already decoded
     * @param error A throwable with the error
     */
    protected void onNetworkFailure(int statusCode, Header[] headers,
                                    String responseBody, Throwable error) {}

    /**
     * Called, when the network request is started.
     */
    protected void onNetworkStarted() {}

    /**
     * Called, when the network request is retried
     */
    protected void onNetworkRetry() {}

    /**
     * Called upon progress update of the network request.
     * @param bytesWritten The number of bytes written
     * @param bytesTotal The number of total bytes
     */
    protected void onNetworkProgress(int bytesWritten, int bytesTotal) {}

    /**
     * Called, when the network request is finished.
     */
    protected void onNetworkFinished() {}

    /**
     * Called, when the postprocessing is finished.
     */
    protected void onProcessingFinished() {}

    /**
     * Called, when the postprocessing is started.
     */
    protected void onProcessingStarted() {}

    /**
     * Called, when the postprocessing is cancelled.
     */
    protected void onProcessingCancelled() {}

}
