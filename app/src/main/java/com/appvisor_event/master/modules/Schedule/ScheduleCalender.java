package com.appvisor_event.master.modules.Schedule;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;

import java.util.TimeZone;

/**
 * Created by bsfuji on 2017/03/29.
 */

public class ScheduleCalender
{
    private static final String TAG = "ScheduleCalender";

    public static long addEvent(Activity activity, final long calendarId, final String title, final String description, final String colorKey, final long startMillis, final long endMillis)
    {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
        {
            return 0;
        }

        final ContentResolver contentResolver = activity.getContentResolver();
        final ContentValues   contentValues   = new ContentValues();
        contentValues.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        contentValues.put(CalendarContract.Events.TITLE, title);
        contentValues.put(CalendarContract.Events.DESCRIPTION, description);
        contentValues.put(CalendarContract.Events.EVENT_COLOR_KEY, colorKey);
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        contentValues.put(CalendarContract.Events.DTSTART, startMillis);
        contentValues.put(CalendarContract.Events.DTEND, endMillis);

        final Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, contentValues);

        final long eventId = Long.parseLong(uri.getLastPathSegment());

        return eventId;
    }

    public static int deleteEvent(Activity activity, final long eventId)
    {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
        {
            return 0;
        }

        final ContentResolver contentResolver = activity.getContentResolver();

        Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        return contentResolver.delete(eventUri, null, null);
    }
}
