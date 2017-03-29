package com.appvisor_event.master.modules.Schedule;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.net.URL;

/**
 * Created by bsfuji on 2017/03/27.
 */

public class Schedule
{
    private WebView   webView = null;
    private Activity activity = null;
    private URL           url = null;

    public Schedule(WebView webView, Activity activity)
    {
        webView.addJavascriptInterface(this, "ScheduleJavascriptInterface");

        this.webView  = webView;
        this.activity = activity;
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
        Log.d("tto", "onReceiveSchedulesJsonString: " + schedulesJsonString);

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
