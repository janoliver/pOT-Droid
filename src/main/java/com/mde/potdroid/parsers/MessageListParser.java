package com.mde.potdroid.parsers;

import com.mde.potdroid.models.Message;
import com.mde.potdroid.models.MessageList;
import com.mde.potdroid.models.User;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML Parser for the PM message list.
 */
public class MessageListParser {

    public static final String INBOX_URL = "pm/?a=0&cid=1";
    public static final String OUTBOX_URL = "pm/?a=0&cid=2";
    private MessageList mMessageList;
    private Pattern mMessagePaggern = Pattern.compile("<a href='\\?a=2&mid=([0-9]+)'>([^<]+)" +
            "</a></td> "
            + "<td (class=\"bold\" |)style='width: 40%'>(<a href='http://my.mods.de/([0-9]+)' "
            + "target='_blank'>([^<]+)</a>|System)</td> <td (class=\"bold\" |)style='width: 15%'>" +
            "([.: 0-9]+)</td>");

    public MessageListParser() {
        mMessageList = new MessageList();
    }

    public static String getUrl(String mode) {
        if (mode.equals(MessageList.TAG_INBOX))
            return INBOX_URL;
        else
            return OUTBOX_URL;
    }

    public MessageList parse(String html) throws IOException {
        Matcher m = mMessagePaggern.matcher(html);

        int n_unread = 0;

        while (m.find()) {

            Message message = new Message();
            message.setId(Integer.parseInt(m.group(1)));
            message.setTitle(m.group(2));
            message.setUnread(m.group(3).compareTo("") != 0);

            if (m.group(4).equals("System")) {
                message.setSystem(true);
            } else {
                message.setSystem(false);
                User author = new User(Integer.parseInt(m.group(5)));
                author.setNick(m.group(6));
                message.setFrom(author);
            }

            try {
                DateFormat df = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.ENGLISH);
                message.setDate(df.parse(m.group(8)));
            } catch (ParseException e) {
            }

            mMessageList.addMessage(message);

            if (message.isUnread())
                n_unread++;
        }

        mMessageList.setNumberOfUnreadMessages(n_unread);

        return mMessageList;
    }
}
