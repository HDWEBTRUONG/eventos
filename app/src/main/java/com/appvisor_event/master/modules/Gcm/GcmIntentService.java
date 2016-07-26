package com.appvisor_event.master.modules.Gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.appvisor_event.master.MainActivity;
import com.appvisor_event.master.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Random;

/**
 * Created by bsfuji on 2014/11/17.
 */
public class GcmIntentService extends IntentService
{
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        String cmd = extras.getString("CMD", "");
        if (cmd.equals("RST_FULL"))
        {
            return;
        }

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        for (String key : extras.keySet())
        {
            Log.d("GcmIntentService", "onHandleIntent: " + key + " = " + extras.get(key) + ".");
        }

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
            {
                sendNotification(extras);
            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
            {
                sendNotification(extras);
            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                sendNotification(extras);
            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(Bundle extras)
    {
        String content = extras.getString("content", "");
        if (content.equals(""))
        {
            return;
        }

        int notificationId = new Random().nextInt();
        extras.putInt("id", notificationId);
        extras.putBoolean("GcmNotification", true);

        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtras(extras);

        PendingIntent contentIntent = PendingIntent.getActivity(this, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(extras.getString("title", getString(R.string.app_name)))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content)).setContentText(content);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder.setSmallIcon(R.drawable.ic_status);
            builder.setColor(ContextCompat.getColor(this, R.color.ic_status_color));
        }

        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;

        notificationManager.notify(notificationId, notification);
    }
}
