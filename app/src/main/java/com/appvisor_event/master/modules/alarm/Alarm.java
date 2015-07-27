package com.appvisor_event.master.modules.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by bsfuji on 15/07/26.
 */
public class Alarm
{
    private final static String ACTION_ALARM = Alarm.class.getPackage().getName() + ".alarm";

    private static Alarm sharedInstance = new Alarm();

    private ArrayList<Integer> requestCodes = new ArrayList<Integer>();
    private int                requestCode  = 0;

    public static Alarm getInstance()
    {
        return sharedInstance;
    }

    private Alarm() {}

    public void add(Context context, Bean alarmBean, Date fireDate)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                fireDate.getTime(),
                this.alarmPendingIntent(context, this.requestCode, alarmBean)
        );

        this.requestCodes.add(new Integer(this.requestCode));
        this.requestCode++;
    }

    public void cancelAll(Context context)
    {
        AlarmManager alarmManager = this.alarmManager(context);

        for (Integer requestCode : this.requestCodes)
        {
            alarmManager.cancel(this.alarmPendingIntent(context, requestCode));
        }

        this.requestCodes.clear();
        this.requestCode = 0;
    }

    private AlarmManager alarmManager(Context context)
    {
        return (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
    }

    private PendingIntent alarmPendingIntent(Context context, int requestCode)
    {
        return this.alarmPendingIntent(context, requestCode, null);
    }

    private PendingIntent alarmPendingIntent(Context context, int requestCode, Bean alarmBean)
    {
        Intent intent = new Intent(context.getApplicationContext(), AlermBroadcastReceiver.class);
        intent.setAction(this.ACTION_ALARM);
        intent.putExtra("requestCode", requestCode);
        intent.putExtra("alarmBean", alarmBean);

        return PendingIntent.getBroadcast(
                context.getApplicationContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );
    }

    public static class Bean implements Serializable
    {
        private int    id                  = 0;
        private String intentClassName     = null;
        private String title               = null;
        private String contentText         = null;
        private int    smallIconResourceId = 0;
        private int    largeIconResourceId = 0;

        public int getId()
        {
            return this.id;
        }

        public String getIntentClassName()
        {
            return this.intentClassName;
        }

        public String getTitle()
        {
            return this.title;
        }

        public String getContentText()
        {
            return this.contentText;
        }

        public int getSmallIconResourceId()
        {
            return this.smallIconResourceId;
        }

        public int getLargeIconResourceId()
        {
            return this.largeIconResourceId;
        }

        public Bean setId(int id)
        {
            this.id = id;

            return this;
        }
        
        public Bean setIntentClass(Class intentClass)
        {
            this.intentClassName = intentClass.getName();

            return this;
        }

        public Bean setTitle(String title)
        {
            this.title = title;

            return this;
        }

        public Bean setContentText(String contentText)
        {
            this.contentText = contentText;

            return this;
        }

        public Bean setSmallIconResourceId(int resourceId)
        {
            this.smallIconResourceId = resourceId;

            return this;
        }

        public Bean setLargeIconResourceId(int resourceId)
        {
            this.largeIconResourceId = resourceId;

            return this;
        }
    }
}
