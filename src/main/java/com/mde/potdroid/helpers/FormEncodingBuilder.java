package com.mde.potdroid.helpers;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * This is a clone of the okhttp encoding class that allows for different encodings than UTF-8
 */
public final class FormEncodingBuilder {

    private String mEncoding = Network.ENCODING_UTF8;
    private final StringBuilder content = new StringBuilder();

    public FormEncodingBuilder() {}

    public FormEncodingBuilder(String encoding) {
        mEncoding = encoding;
    }

    public void setEncoding(String encoding) {
        mEncoding = encoding;
    }

    public String getEncoding() {
        return mEncoding;
    }

    protected MediaType getMediaType() {
        return MediaType.parse("application/x-www-form-urlencoded;charset=" + mEncoding);
    }

    /** Add new key-value pair. */
    public FormEncodingBuilder add(String name, String value) {
        if (content.length() > 0) {
            content.append('&');
        }
        try {
            content.append(URLEncoder.encode(name, mEncoding))
                    .append('=')
                    .append(URLEncoder.encode(value, mEncoding));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        return this;
    }



    public RequestBody build() {
        if (content.length() == 0) {
            throw new IllegalStateException("Form encoded body must have at least one part.");
        }

        // Convert to bytes so RequestBody.create() doesn't add a charset to the content-type.
        byte[] contentBytes = content.toString().getBytes(Charset.forName(mEncoding));
        return RequestBody.create(getMediaType(), contentBytes);
    }
}