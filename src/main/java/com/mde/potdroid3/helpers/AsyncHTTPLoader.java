package com.mde.potdroid3.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.Loader;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.apache.http.Header;

import java.io.UnsupportedEncodingException;

/**
 * Created by oli on 11/27/13.
 */
public abstract class AsyncHTTPLoader<E> extends Loader<E> {
    public static final Integer GET = 0;
    public static final Integer POST = 1;

    private Network mNetwork;
    private String mRequestUrl;
    private Integer mMode;
    private RequestParams mParams;
    private String mEncoding;

    private AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String stringResult = "";
            try {
                stringResult = new String(responseBody, mEncoding);
            } catch (UnsupportedEncodingException e) {
                stringResult = new String(responseBody);
            }
            AsyncHTTPLoader.this.processResponse(stringResult);
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
        {
            error.printStackTrace();
        }

        @Override
        public void onRetry() {
        }

        @Override
        public void onProgress(int bytesWritten, int totalSize) {
        }

        @Override
        public void onFinish() {
        }
    };

    private AsyncTask<String, Void, E> mProcessResponseTask = new AsyncTask<String, Void, E>() {
        protected E doInBackground(String ... response) {
            return parseResponse(response[0]);
        }

        protected void onPostExecute(E result) {
            deliverResult(result);
        }
    };

    public AsyncHTTPLoader(Context context, String url) {
        this(context, url, GET, null);
    }

    public AsyncHTTPLoader(Context context, String url, Integer mode) {
        this(context, url, mode, null);
    }

    public AsyncHTTPLoader(Context context, String url, Integer mode, RequestParams params) {
        this(context, url, mode, params, "UTF-8");
    }

    public AsyncHTTPLoader(Context context, String url, Integer mode, RequestParams params,
                           String encoding) {
        super(context);

        mRequestUrl = url;
        mNetwork = new Network(getContext());
        mMode = mode;
        mParams = params;
        mEncoding = encoding;
    }

    public void setUrl(String url) {
        mRequestUrl = url;
    }

    public void setParams(RequestParams p) {
        mParams = p;
    }

    public void setEncoding(String encoding) {
        mEncoding = encoding;
    }

    protected void processResponse(final String response) {
        mProcessResponseTask.execute(response);
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();

        if(mMode == GET)
            mNetwork.get(mRequestUrl, mParams, mHandler);
        else if(mMode == POST)
            mNetwork.post(mRequestUrl, mParams, mHandler);
    }

    public abstract E parseResponse(String response);


}
