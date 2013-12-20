package com.mde.potdroid.models;

import java.util.ArrayList;

/**
 * Created by oli on 11/9/13.
 */
public class MessageList {
    private static final long serialVersionUID = 10L;

    private Integer mNumberOfUnreadMessages;
    private ArrayList<Message> mMessages = new ArrayList<Message>();

    public static final String TAG_INBOX = "inbox";
    public static final String TAG_OUTBOX = "outbox";

    public Integer getNumberOfMessages() {
        return mMessages.size();
    }

    public ArrayList<Message> getMessages() {
        return mMessages;
    }

    public void addMessage(Message message) {
        this.mMessages.add(message);
    }

    public Integer getNumberOfUnreadMessages() {
        return mNumberOfUnreadMessages;
    }

    public void setNumberOfUnreadMessages(Integer numberOfUnreadMessages) {
        mNumberOfUnreadMessages = numberOfUnreadMessages;
    }

    public ArrayList<Message> getUnreadMessages() {
        ArrayList<Message> ret = new ArrayList<Message>();
        for(Message m: getMessages())
            if(m.isUnread())
                ret.add(m);
        return ret;
    }

    public static class Html {
        public static final String INBOX_URL = "pm/?a=0&cid=1";
        public static final String OUTBOX_URL = "pm/?a=0&cid=2";

        public static String getUrl(String mode) {
            if(mode.equals(MessageList.TAG_INBOX))
                return INBOX_URL;
            else
                return OUTBOX_URL;
        }
    }
}
