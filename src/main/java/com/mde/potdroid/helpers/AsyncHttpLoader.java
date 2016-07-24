package com.mde.potdroid.helpers;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.Loader;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.Header;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;

/**
 * This class provides a Loader to asynchroneously load POST or GET requests from the web.
 * One should extend this class to provide the abstract method "parseContent", in which some
 * postprocessing can be done. This, as well as the loading, runs asynchroneously in a separate
 * thread.
 * The HTTP Client from the Network class is used, so that headers and cookies are in place.
 */
public abstract class AsyncHttpLoader<E> extends Loader<E> {

    // the calling activity
    private Context mActivity;
    final Handler mUiThreadHandler = new Handler();

    // request type codes
    public static final Integer GET = 0;
    public static final Integer POST = 1;
    public static final String DEFAULT_ENCODING = Network.ENCODING_UTF8;

    // the instance of the Network class
    protected Network mNetwork;

    // request URL (without (!) the Network.BASE_URL part)
    protected String mRequestUrl;

    // request mode, GET or POST (see above)
    protected Integer mMode;

    // the parameters for the request, mostly required for POST requests
    protected RequestBody mParams;

    // the encoding to use when decoding the response
    protected String mEncoding;

    // the data cache
    protected E mData;

    private Callback mHandler = new Callback() {
        @Override
        public void onFailure(Call call, final IOException e) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    AsyncHttpLoader.this.onNetworkFailure(0, null, "", e);
                }
            });
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);

            String stringResult;
            try {
                stringResult = new String(response.body().bytes(), mEncoding);
            } catch (UnsupportedEncodingException e) {
                stringResult = response.body().string();
            }

            AsyncHttpLoader.this.processResponse(stringResult);
        }
    };

    /**
     * Constructor
     *
     * @param context The application context to use
     * @param url     The request URL WITHOUT the base (forum.mods.de/bb)
     */
    public AsyncHttpLoader(Context context, String url) {
        this(context, url, GET, null);
    }

    /**
     * Constructor
     *
     * @param context The application context to use
     * @param url     The request URL WITHOUT the base (forum.mods.de/bb)
     * @param mode    The request mode, GET or POST
     */
    public AsyncHttpLoader(Context context, String url, Integer mode) {
        this(context, url, mode, null);
    }

    /**
     * Constructor
     *
     * @param context The application context to use
     * @param url     The request URL WITHOUT the base (forum.mods.de/bb)
     * @param mode    The request mode, GET or POST
     * @param params  Request params, mostly for POST
     */
    public AsyncHttpLoader(Context context, String url, Integer mode, RequestBody params) {
        this(context, url, mode, params, DEFAULT_ENCODING);
    }

    /**
     * Constructor
     *
     * @param context  The application context to use
     * @param url      The request URL WITHOUT the base (forum.mods.de/bb)
     * @param mode     The request mode, GET or POST
     * @param params   Request params, mostly for POST
     * @param encoding Which encoding to use to decode the response
     */
    public AsyncHttpLoader(Context context, String url, Integer mode, RequestBody params,
                           String encoding) {
        super(context);

        mRequestUrl = url;
        mNetwork = new Network(getContext());
        mMode = mode;
        mParams = params;
        mEncoding = encoding;
        mActivity = context;
    }

    private Activity getActivity() {
        return (Activity)mActivity;
    }

    /**
     * Update the request URL.
     *
     * @param url the new requestb url
     */
    public void setUrl(String url) {
        mRequestUrl = url;
    }

    /**
     * Change the timout
     *
     * @param seconds the timeout
     */
    public void setTimeout(Integer seconds) {
        mNetwork.setTimeout(seconds);
    }

    /**
     * Update the request parameters
     *
     * @param p The new request parameters.
     */
    public void setParams(RequestBody p) {
        mParams = p;
    }

    /**
     * Update the encoding
     *
     * @param encoding The new encoding.
     */
    public void setEncoding(String encoding) {
        mEncoding = encoding;
    }

    /**
     * Start the AsyncTask to process the response.
     *
     * @param response The response string
     */
    protected void processResponse(final String response) {
        final Runnable mUpdateResults = new Runnable() {
            public void run() {
                new ResponseTask().execute(response);
            }
        };
        mUiThreadHandler.post(mUpdateResults);
    }

    /**
     * Overridden method from the Loader class. Take care of initializing
     * everything and, if no data is present, start loading with forceLoad().
     */
    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if (mData != null)
            deliverResult(mData);

        if (mNetwork == null)
            mNetwork = new Network(getContext());

        if (mData == null) {

            if(Utils.getConnectionType(mActivity) == Utils.NETWORK_NONE) {
                onNetworkFailure(0, null, "", new ConnectException());
            } else {
                forceLoad();
            }

        }
    }

    /**
     * Actually submit the network request.
     */
    @Override
    protected void onForceLoad() {
        super.onForceLoad();

        if (mMode.equals(GET)) {
            mNetwork.get(mRequestUrl, mHandler);
        } else if (mMode.equals(POST)) {
            mNetwork.post(mRequestUrl, mParams, mHandler);
        }
    }

    /**
     * Stop loading by trying to cancel both the Network request and the AsyncTask of parsing
     */
    @Override
    protected void onStopLoading() {
        super.onStopLoading();

        mNetwork.cancelLoad();
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
     * This method must be implemented by the user subclassing the Loader. It takes teh
     * network response String and must return an Object E.
     *
     * @param response The response HTML/XML/whatever.
     * @return The generated Object E
     */
    protected abstract E processNetworkResponse(String response);

    /**
     * Called upon failure of the network Request.
     *
     * @param statusCode   the HTTP status Code
     * @param headers      The header list
     * @param responseBody The response Body, already decoded
     * @param error        A throwable with the error
     */
    protected void onNetworkFailure(int statusCode, Header[] headers,
                                    String responseBody, Throwable error) {
    }

    /**
     * Called, when the postprocessing is finished.
     */
    protected void onProcessingFinished() {
    }

    /**
     * Called, when the postprocessing is started.
     */
    protected void onProcessingStarted() {
    }

    /**
     * Called, when the postprocessing is cancelled.
     */
    protected void onProcessingCancelled() {
    }

    /**
     * This AsyncTask calls the processNetworkResponse in a separate thread.
     */
    protected class ResponseTask extends AsyncTask<String, Void, E> {

        protected E doInBackground(String... response) {
            return processNetworkResponse(response[0]);
        }

        @Override
        protected void onPreExecute() {
            onProcessingStarted();
        }

        protected void onPostExecute(E result) {
            onProcessingFinished();
            mData = result;
            deliverResult(mData);
        }

        @Override
        protected void onCancelled() {
            onProcessingCancelled();
        }
    }

}
