package com.mde.potdroid.helpers;

import android.content.Context;
import android.content.res.Configuration;
import com.mde.potdroid.R;
import com.mde.potdroid.models.Message;
import com.samskivert.mustache.Mustache;

import java.io.*;

/**
 * This class, given a Message object, turns it into displayable HTML code
 * for the WebView we use.
 */
public class MessageBuilder {

    // a reference to the context
    private Context mContext;
    private SettingsWrapper mSettings;
    private BenderHandler mBenderHandler;

    public MessageBuilder(Context cx) {
        mContext = cx;
        mSettings = new SettingsWrapper(cx);

        mBenderHandler = new BenderHandler(cx);
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
        private CssStyleWrapper mStyle;

        public MessageContext(Message m, Context cx) {
            mMessage = m;
            mContext = cx;
            mStyle = new CssStyleWrapper(cx);
        }

        public CssStyleWrapper getStyle() {
            return mStyle;
        }

        public String getThemeVariant() {
            return mSettings.getThemeVariant();
        }


        public boolean getBenderHead() {
            return mSettings.benderPosition() == 1 ||
                    (mSettings.benderPosition() == 3 &&
                            mContext.getResources().getConfiguration().orientation
                                    == Configuration.ORIENTATION_PORTRAIT);
        }

        public boolean getBenderBody() {
            return mSettings.benderPosition() == 2 ||
                    (mSettings.benderPosition() == 3 &&
                            mContext.getResources().getConfiguration().orientation
                                    == Configuration.ORIENTATION_LANDSCAPE);
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

        public String getAvatarBackground() {
            if(mMessage.isSystem())
                return "";
            String path = mBenderHandler.getAvatarFilePathIfExists(mMessage.getFrom());
            if (path == null)
                return "";
            else
                return String.format("style=\"background-image:url(%s)\"", path);
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
            return Utils.getFormattedTime(mContext.getString(R.string.default_time_format), mMessage.getDate());
        }

        public String getTitle() {
            return mMessage.getTitle();
        }

        public String getText() {
            return mMessage.getText();
        }
    }

}
