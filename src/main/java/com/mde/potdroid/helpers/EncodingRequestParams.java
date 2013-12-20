package com.mde.potdroid.helpers;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * This is just a hack for the android-async-http library that allows custom encodings
 * for request params. It will probably be added as a feature soon, then this will become
 * obsolete.
 */
public class EncodingRequestParams extends RequestParams {
    protected String mEncoding = HTTP.UTF_8;
    protected boolean useJsonStreamer;

    public void setEncoding(String encoding) {
        mEncoding = encoding;
    }

    public HttpEntity getEntity(ResponseHandlerInterface progressHandler) throws IOException {
        if (useJsonStreamer) {
            return super.getEntity(progressHandler);
        } else if (streamParams.isEmpty() && fileParams.isEmpty()) {
            return createFormEntity();
        } else {
            return super.getEntity(progressHandler);
        }
    }

    private HttpEntity createFormEntity() {
        try {
            return new UrlEncodedFormEntity(getParamsList(), mEncoding);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

}
