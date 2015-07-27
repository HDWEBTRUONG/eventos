package com.appvisor_event.master.modules.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by bsfuji on 15/07/26.
 */
public class AlermBroadcastReceiver extends BroadcastReceiver
{
    private Context    context     = null;
    private int        requestCode = 0;
    private Alarm.Bean alarmBean   = null;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        this.context     = context;
        this.requestCode = intent.getIntExtra("requestCode", 0);
        this.alarmBean   = (Alarm.Bean)intent.getSerializableExtra("alarmBean");

        fire();
    }

    public void fire() {
        this.alarmNotification().fire(this.requestCode);
    }

    private AlarmNotification alarmNotification()
    {
        AlarmNotification notification = new AlarmNotification(this.context);
        notification.setAlarmBean(this.alarmBean);

        return notification;
    }
}
