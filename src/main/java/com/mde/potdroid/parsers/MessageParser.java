package com.mde.potdroid.parsers;

import com.mde.potdroid.models.Message;
import com.mde.potdroid.models.User;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML Parser for a PM message.
 */
public class MessageParser
{

    public static final String URL = "pm/?a=2&mid=";
    public static final String SEND_URL = "pm/?a=6";
    private Message mMessage;
    private Pattern mMessagePattern = Pattern.compile("Betreff</td> <td class='hh'><b>([^<]+)" +
            "</td>.*<td class='h'>(Absender|Empf&auml;nger)</td> <td class='hh'>.*<a " +
            "href='http://my.mods.de/([0-9]+)' target='_blank'.*?>([^<]+?)</a>.*Gesendet</td> <td" +
            " class='hh'><b>([0-9:\\. ]+)</td>.*<td colspan='2' class='b'>(.+)</td> </tr>  <tr> " +
            "<td colspan='2' class='h'></td> </tr>.*</table>", Pattern.DOTALL | Pattern.MULTILINE);

    public MessageParser() {
        mMessage = new Message();
    }

    public static String getUrl(Integer message_id) {
        return URL + message_id.toString();
    }

    public Message parse(String html, Integer message_id) throws IOException {

        Matcher m = mMessagePattern.matcher(html);

        if (m.find()) {

            User from = new User(Integer.parseInt(m.group(3)));
            from.setNick(m.group(4));
            mMessage.setFrom(from);

            mMessage.setTitle(m.group(1));
            mMessage.setId(message_id);
            mMessage.setText(m.group(6));
            mMessage.setOutgoing(!m.group(4).equals("Absender"));

            try {
                DateFormat df = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.ENGLISH);
                mMessage.setDate(df.parse(m.group(5)));
            } catch (ParseException e) {
            }

            return mMessage;
        } else {
            throw new IOException("Macthing error.");
        }

    }
}