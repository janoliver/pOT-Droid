package com.mde.potdroid.services;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mde.potdroid.MessageActivity;
import com.mde.potdroid.MessageListActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.Network;
import com.mde.potdroid.helpers.SettingsWrapper;
import com.mde.potdroid.models.Message;
import com.mde.potdroid.models.MessageList;
import com.mde.potdroid.parsers.MessageListParser;
import org.apache.http.Header;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by oli on 12/5/13.
 */
public class MessagePollingAlarm extends BroadcastReceiver
{
    public static final int NOTIFICATION_ID = 1337;

    @Override
    public void onReceive(final Context context, Intent intent) {

        Network network = new Network(context);
        network.get(MessageListParser.INBOX_URL, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String stringResult;

                try {
                    stringResult = new String(responseBody, "ISO-8859-15");
                } catch (UnsupportedEncodingException e) {
                    stringResult = new String(responseBody);
                }


                try {
                    MessageListParser p = new MessageListParser();
                    MessageList list = p.parse(stringResult);
                    handleNotification(list, context);
                } catch (IOException e) {}

            }
        });

    }

    public void handleNotification(MessageList list, Context context) {
        ArrayList<Message> unreadMessages = list.getUnreadMessages();

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(unreadMessages.size() == 0) {
            manager.cancel(NOTIFICATION_ID);
            return;
        }

        // build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.statusbaricon);
        builder.setLargeIcon(
                ((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_launcher)).getBitmap()
        );
        builder.setContentTitle(list.getNumberOfUnreadMessages() + " neue PMs!");
        builder.setAutoCancel(true);
        builder.setOnlyAlertOnce(true);

        // sound and vibrate
        SettingsWrapper settings = new SettingsWrapper(context);
        if(settings.isNotificationVibrate())
            builder.setVibrate(new long[] {0, 500});

        builder.setSound(Uri.parse(settings.getNotificationSoundURI()));

        // prepare the intent
        Intent messageIntent;

        if(unreadMessages.size() > 1) {

            // the inbox style
            // we want to display the senders and titles of the unread messages, but at most 3.
            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

            int counter = 0;
            for(Message m : unreadMessages) {
                style.addLine(Html.fromHtml("<b>" + m.getFrom().getNick() + "</b>: " + m.getTitle()));
                if(++counter >= 3)
                    break;
            }

            // if there are more, then a summary with the number is displayed.
            if(counter < unreadMessages.size())
                style.setSummaryText("+ " + (unreadMessages.size() - counter) + " weitere.");

            builder.setStyle(style);

            messageIntent = new Intent(context, MessageListActivity.class);

        } else {

            // only a single message, so display it the normal way.
            Message m = unreadMessages.get(0);
            builder.setContentText(Html.fromHtml("<b>" + m.getFrom().getNick() + "</b>: " + m.getTitle()));

            messageIntent = new Intent(context, MessageActivity.class);
            messageIntent.putExtra("message_id", unreadMessages.get(0).getId());
        }

        // this is only for the back button behaviour
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MessageListActivity.class);
        stackBuilder.addNextIntent(messageIntent);

        PendingIntent pi = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    public void setAlarm(Context context) {
        cancelAlarm(context);

        SettingsWrapper settings = new SettingsWrapper(context);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, MessagePollingAlarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000 * settings.pollMessagesInterval(), pi);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, MessagePollingAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}