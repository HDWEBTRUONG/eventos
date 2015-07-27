package com.appvisor_event.master.modules.alarm;

import android.content.Context;

import com.appvisor_event.master.MainActivity;
import com.appvisor_event.master.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by bsfuji on 15/07/26.
 */
public class FavoritSeminarAlarm
{
    private static int ALARM_TIMING_IN_MUNITES = -10;

    private static Alarm alarm = Alarm.getInstance();

    public static void add(Context context, int id, String title, String startDateString)
    {
        if (FavoritSeminarAlarm.isPastDate(FavoritSeminarAlarm.startDate(startDateString)))
        {
            return;
        }

        Alarm.Bean alarmBean = new Alarm.Bean()
                .setId(id)
                .setIntentClass(MainActivity.class)
                .setTitle(context.getString(R.string.app_name))
                .setContentText(FavoritSeminarAlarm.text(title, startDateString))
                .setSmallIconResourceId(R.drawable.ic_launcher);

        FavoritSeminarAlarm.alarm.add(context, alarmBean, FavoritSeminarAlarm.fireDate(startDateString));
    }

    public static void cancelAll(Context context)
    {
        FavoritSeminarAlarm.alarm.cancelAll(context);
    }

    private static String text(String title, String startDateString)
    {
        return String.format("「%s」が%sから開始します。", title, FavoritSeminarAlarm.startTime(startDateString));
    }

    private static Date fireDate(String startDateString)
    {
        Calendar calender = Calendar.getInstance();
        calender.setTime(FavoritSeminarAlarm.startDate(startDateString));
        calender.add(Calendar.MINUTE, FavoritSeminarAlarm.ALARM_TIMING_IN_MUNITES);

        return calender.getTime();
    }

    private static Date startDate(String startDateString)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return simpleDateFormat.parse(startDateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String startTime(String startDateString)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

        Date startDate = FavoritSeminarAlarm.startDate(startDateString);
        return simpleDateFormat.format(startDate);
    }

    private static boolean isPastDate(Date startDate)
    {
        return 0 < new Date().compareTo(startDate);
    }
}
