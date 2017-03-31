package com.appvisor_event.master.modules.Schedule;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by bsfuji on 2017/03/27.
 */

public class Schedule
{
    private WebView   webView = null;
    private Activity activity = null;
    private URL           url = null;

    SharedPreferences sharedPreferences = null;

    public Schedule(WebView webView, Activity activity)
    {
        webView.addJavascriptInterface(this, "ScheduleJavascriptInterface");

        this.webView           = webView;
        this.activity          = activity;
        this.sharedPreferences = activity.getSharedPreferences("Schedule", Context.MODE_PRIVATE);
    }

    public void setUrl(URL url)
    {
        this.url = url;
    }

    public static Boolean isRegistCalenderUrl(URL url)
    {
        return url.getPath().startsWith("/regist-calender/");
    }

    public static Boolean isDeleteCalenderUrl(URL url)
    {
        return url.getPath().startsWith("/delete-calender/");
    }

    public static Boolean isRegistCalenderUrlForDetail(URL url)
    {
        return url.getPath().startsWith("/regist-calender/detail");
    }

    public static Boolean isDeleteCalenderUrlForDetail(URL url)
    {
        return url.getPath().startsWith("/delete-calender/");
    }

    public static Boolean isRegistCalenderUrlForMass(URL url)
    {
        return url.getPath().startsWith("/regist-calender/mass");
    }

    public static Boolean isDeleteCalenderUrlForMass(URL url)
    {
        return url.getPath().startsWith("/delete-calender/mass");
    }

    public String scheduleGetJavascript()
    {
        if (isRegistCalenderUrlForDetail(url))
        {
            return "javascript:ScheduleJavascriptInterface.onReceiveSchedulesJsonString(Detail.schedule());";
        }

        if (isDeleteCalenderUrlForDetail(url))
        {
            return "javascript:ScheduleJavascriptInterface.onReceiveSchedulesJsonString(Detail.schedule());";
        }

        if (isRegistCalenderUrlForMass(url))
        {
            return "javascript:ScheduleJavascriptInterface.onReceiveSchedulesJsonString(MassRegistration.schedules());";
        }

        if (isDeleteCalenderUrlForMass(url))
        {
            return "javascript:ScheduleJavascriptInterface.onReceiveSchedulesJsonString(MassRegistration.schedules());";
        }

        return null;
    }

    public String scheduleRegistSuccessJavascript()
    {
        if (isRegistCalenderUrlForDetail(url))
        {
            return "javascript:Detail.onRegistSuccess();";
        }

        if (isRegistCalenderUrlForMass(url))
        {
            return "javascript:MassRegistration.onRegistSuccess();";
        }

        return null;
    }

    public String scheduleDeleteSuccessJavascript()
    {
        if (isDeleteCalenderUrlForDetail(url))
        {
            return "javascript:Detail.onDeleteSuccess();";
        }

        if (isDeleteCalenderUrlForMass(url))
        {
            return "javascript:MassRegistration.onDeleteSuccess();";
        }

        return null;
    }

    public String scheduleRegistFailedJavascript()
    {
        if (isRegistCalenderUrlForDetail(url))
        {
            return "javascript:Detail.onRegistFailed();";
        }

        if (isRegistCalenderUrlForMass(url))
        {
            return "javascript:MassRegistration.onRegistFailed();";
        }

        return null;
    }

    public String scheduleDeleteFailedJavascript()
    {
        if (isRegistCalenderUrlForDetail(url))
        {
            return "javascript:Detail.onDeleteFailed();";
        }

        if (isRegistCalenderUrlForMass(url))
        {
            return "javascript:MassRegistration.onDeleteFailed();";
        }

        return null;
    }

    @JavascriptInterface
    public void onReceiveSchedulesJsonString(String schedulesJsonString)
    {
        Log.d("Schedule", "onReceiveSchedulesJsonString: " + schedulesJsonString);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));

        try {
            JSONArray schedules = new JSONArray(schedulesJsonString);
            for (int i = 0; i < schedules.length(); i++)
            {
                JSONObject schedule = schedules.getJSONObject(i);

                String scheduleId    = schedule.getString("id");
                String scheduleTitle = schedule.getString("title");
                String scheduleStart = schedule.getString("start");
                String scheduleEnd   = schedule.getString("end");

                long scheduleEventId = sharedPreferences.getLong(scheduleId, 0);

                if (isRegistCalenderUrl(url))
                {
                    if (0 != scheduleEventId)
                    {
                        Log.d("Schedule", "Is saved schedule. " + schedule.toString(2));
                        continue;
                    }

                    scheduleEventId = ScheduleCalender.addEvent(
                            activity,
                            scheduleTitle,
                            null,
                            simpleDateFormat.parse(scheduleStart).getTime(),
                            simpleDateFormat.parse(scheduleEnd).getTime()
                    );

                    if (0 != scheduleEventId)
                    {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong(scheduleId, scheduleEventId);
                        editor.commit();
                    }
                }

                if (isDeleteCalenderUrl(url))
                {
                    if (0 == scheduleEventId)
                    {
                        Log.d("Schedule", "is not saved schedule. " + schedule.toString(2));
                        continue;
                    }

                    int numberOfDeleteSchedules = ScheduleCalender.deleteEvent(activity, scheduleEventId);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(scheduleId);
                    editor.commit();
                }
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isRegistCalenderUrl(url))
                    {
                        webView.loadUrl(scheduleRegistSuccessJavascript());
                        return;
                    }

                    if (isDeleteCalenderUrl(url))
                    {
                        webView.loadUrl(scheduleDeleteSuccessJavascript());
                        return;
                    }
                }
            });
        } catch (Exception e) {
            Log.e("tto", e.getMessage());
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isRegistCalenderUrl(url))
                    {
                        webView.loadUrl(scheduleRegistFailedJavascript());
                        return;
                    }

                    if (isDeleteCalenderUrl(url))
                    {
                        webView.loadUrl(scheduleDeleteFailedJavascript());
                        return;
                    }
                }
            });
        }
    }

    public void registSchedules()
    {
        webView.loadUrl(scheduleGetJavascript());
    }

    public void deleteSchedules()
    {
        webView.loadUrl(scheduleGetJavascript());
    }

    public void cancel()
    {
        if (isRegistCalenderUrl(url))
        {
            webView.loadUrl(scheduleRegistFailedJavascript());
            return;
        }

        if (isDeleteCalenderUrl(url))
        {
            webView.loadUrl(scheduleDeleteFailedJavascript());
            return;
        }
    }
}
