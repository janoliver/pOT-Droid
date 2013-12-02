package com.mde.potdroid3.helpers;

import android.content.Context;
import com.mde.potdroid3.models.Message;
import com.samskivert.mustache.Mustache;

import java.io.*;

/**
 * Created by oli on 8/10/13.
 */
public class MessageBuilder {

    private Context mContext;

    public MessageBuilder(Context cx) {
        mContext = cx;
    }

    public String parse(Message m) throws IOException {
        InputStream is = mContext.getResources().getAssets().open("message.html");
        Reader reader = new InputStreamReader(is);
        StringWriter sw = new StringWriter();
        Mustache.compiler().compile(reader).execute(new MessageContext(m), sw);
        return sw.toString();
    }

    class MessageContext {
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
