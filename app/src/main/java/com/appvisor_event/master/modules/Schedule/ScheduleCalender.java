package com.appvisor_event.master.modules.Schedule;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;

import java.util.TimeZone;

/**
 * Created by bsfuji on 2017/03/29.
 */

public class ScheduleCalender
{
    private static final String TAG = "ScheduleCalender";

    // プロジェクション配列
    private static final String[] CALENDAR_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR_KEY
    };

    // プロジェクション配列のインデックス
    private static final int CALENDAR_PROJECTION_IDX_ID           = 0;
    private static final int CALENDAR_PROJECTION_IDX_ACCOUNT_NAME = 1;

    private static class CalenderAccount
    {
        private long   id          = 0;
        private String accountName = null;

        public CalenderAccount(long id, String accountName)
        {
            this.id          = id;
            this.accountName = accountName;
        }

        public long getId()
        {
            return this.id;
        }

        public String getAccountName()
        {
            return this.accountName;
        }
    }

    public static CalenderAccount getFirstCalenderAccount(Activity activity, final String accountType)
    {
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT && ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
        {
            return null;
        }

        // クエリ条件を設定する
        final Uri uri = CalendarContract.Calendars.CONTENT_URI;
        final String[] projection = CALENDAR_PROJECTION;
        final String selection = "((" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND (" + CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " = ?))";
        final String[] selectionArgs = new String[] {
                accountType,
                "700"
        };
        final String sortOrder = null;

        // クエリを発行してカーソルを取得する
        final ContentResolver contentResolver = activity.getContentResolver();
        final Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);

        while (cursor.moveToNext())
        {
            final long   id          = cursor.getLong(CALENDAR_PROJECTION_IDX_ID);
            final String accountName = cursor.getString(CALENDAR_PROJECTION_IDX_ACCOUNT_NAME);

            return new CalenderAccount(id, accountName);
        }

        return null;
    }

    public static long addEvent(Activity activity, final String title, final String description, final long startMillis, final long endMillis)
    {
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT && ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
        {
            return 0;
        }

        CalenderAccount calenderAccount = getFirstCalenderAccount(activity, "com.google");
        if (null == calenderAccount)
        {
            return 0;
        }

        final ContentResolver contentResolver = activity.getContentResolver();
        final ContentValues   contentValues   = new ContentValues();
        contentValues.put(CalendarContract.Events.CALENDAR_ID, calenderAccount.getId());
        contentValues.put(CalendarContract.Events.TITLE, title);
        contentValues.put(CalendarContract.Events.DESCRIPTION, description);
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        contentValues.put(CalendarContract.Events.DTSTART, startMillis);
        contentValues.put(CalendarContract.Events.DTEND, endMillis);

        final Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, contentValues);

        final long eventId = Long.parseLong(uri.getLastPathSegment());

        return eventId;
    }

    public static int deleteEvent(Activity activity, final long eventId)
    {
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT && ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
        {
            return 0;
        }

        final ContentResolver contentResolver = activity.getContentResolver();

        Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        return contentResolver.delete(eventUri, null, null);
    }
}
