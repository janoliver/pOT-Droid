package com.mde.potdroid.helpers;

import android.content.Context;
import com.mde.potdroid.R;
import com.mde.potdroid.models.Message;
import com.samskivert.mustache.Mustache;

import java.io.*;
import java.text.SimpleDateFormat;

/**
 * This class, given a Message object, turns it into displayable HTML code
 * for the WebView we use.
 */
public class MessageBuilder {

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
        Mustache.compiler().compile(reader).execute(new MessageContext(message, mContext), sw);
        return sw.toString();
    }

    /**
     * A wrapper class for Message objects, needed by JMoustache.
     */
    class MessageContext {

        private Message mMessage;
        private Context mContext;

        public MessageContext(Message m, Context cx) {
            mMessage = m;
            mContext = cx;
        }

        public String getCssFile() {
            return Utils.getStringByAttr(mContext, R.attr.bbTopicCssFile);
        }

        public Integer getId() {
            return mMessage.getId();
        }

        public String getAuthor() {
            if(mMessage.isSystem())
                return "System";
            return mMessage.getFrom().getNick();
        }

        public Integer getAuthorId() {
            if(mMessage.isSystem())
                return 0;
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
            return new SimpleDateFormat(mContext.getString(R.string.default_time_format))
                    .format(mMessage.getDate());
        }

        public String getTitle() {
            return mMessage.getTitle();
        }

        public String getText() {
            return mMessage.getText();
        }
    }

}
