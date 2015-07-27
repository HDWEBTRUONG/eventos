package com.appvisor_event.master.modules.alarm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

/**
 * Created by bsfuji on 15/07/26.
 */
public class AlarmNotification
{
    private Context    context   = null;
    private Alarm.Bean alarmBean = null;

    public AlarmNotification(Context context)
    {
        this.context = context;
    }

    public AlarmNotification setAlarmBean(Alarm.Bean alarmBean)
    {
        this.alarmBean = alarmBean;

        return this;
    }

    public void fire(int requestCode)
    {
        this.notificationManager().notify(this.notificationId(), this.notification(requestCode));
    }

    private NotificationManager notificationManager()
    {
        return (NotificationManager)this.context.getSystemService(Activity.NOTIFICATION_SERVICE);
    }

    private int notificationId()
    {
        return this.alarmBean.getId();
    }

    private Notification notification(int requestCode)
    {
        Notification notification = this.bigTextStyleNotification(requestCode).build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        return notification;
    }

    private NotificationCompat.BigTextStyle bigTextStyleNotification(int requestCode)
    {
        NotificationCompat.BigTextStyle notification = new NotificationCompat.BigTextStyle(this.notificationBuilder(requestCode));
        notification.bigText(this.alarmBean.getContentText());

        return notification;
    }

    private NotificationCompat.Builder notificationBuilder(int requestCode)
    {
        return new NotificationCompat.Builder(this.context)
                .setContentIntent(this.pendingIntent(requestCode))
                .setContentTitle(this.alarmBean.getTitle())
                .setContentText(this.alarmBean.getContentText())
                .setSmallIcon(this.alarmBean.getSmallIconResourceId())
                .setLargeIcon(this.largeIcon())
                .setAutoCancel(true);
    }

    private PendingIntent pendingIntent(int requestCode)
    {
        Intent intent = new Intent(this.context, this.intentClass());
        return PendingIntent.getActivity(this.context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Class intentClass()
    {
        try {
            return Class.forName(this.alarmBean.getIntentClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap largeIcon()
    {
        if (0 == this.alarmBean.getLargeIconResourceId())
        {
            return null;
        }

        return BitmapFactory.decodeResource(this.context.getResources(), this.alarmBean.getLargeIconResourceId());
    }
}
