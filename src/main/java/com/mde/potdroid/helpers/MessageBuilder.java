package com.mde.potdroid.helpers;

import android.content.Context;

import com.mde.potdroid.models.Message;
import com.samskivert.mustache.Mustache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

/**
 * This class, given a Message object, turns it into displayable HTML code
 * for the WebView we use.
 */
public class MessageBuilder
{

    // a reference to the context
    private Context mContext;

    public MessageBuilder(Context cx) {
        mContext = cx;
    }

    /**
     * Parse a message object to HTML using JMoustache template engine.
     *
     * @param message the Message object
     * @return HTML code
     * @throws IOException
     */
    public String parse(Message message) throws IOException {
        InputStream is = mContext.getResources().getAssets().open("message.html");
        Reader reader = new InputStreamReader(is);
        StringWriter sw = new StringWriter();
        Mustache.compiler().compile(reader).execute(new MessageContext(message), sw);
        return sw.toString();
    }

    /**
     * A wrapper class for Message objects, needed by JMoustache.
     */
    class MessageContext
    {

        private Message mMessage;

        public MessageContext(Message m) {
            mMessage = m;
        }

        public Integer getId() {
            return mMessage.getId();
        }

        public String getAuthor() {
            return mMessage.getFrom().getNick();
        }

        public Integer getAuthorId() {
            return mMessage.getFrom().getId();
        }

        public String getAvatar() {
            return "";
        }

        public Boolean getOutgoing() {
            return mMessage.isOutgoing();
        }

        public Integer getAvatarId() {
            return 0;
        }

        public String getAvatarPath() {
            return "";
        }

        public String getDate() {
            return mMessage.getDate().toString();
        }

        public String getTitle() {
            return mMessage.getTitle();
        }

        public String getText() {
            return mMessage.getText();
        }
    }

}
