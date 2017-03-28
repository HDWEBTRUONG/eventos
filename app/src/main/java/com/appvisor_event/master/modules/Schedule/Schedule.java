package com.appvisor_event.master.modules.Schedule;

import java.net.URL;

/**
 * Created by bsfuji on 2017/03/27.
 */

public class Schedule
{
    private static Schedule instance = null;

    private Schedule() {}

    public static Schedule sharedInstance()
    {
        if (null == instance)
        {
            instance = new Schedule();
        }

        return instance;
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

    public static String scheduleGetJavascriptWithUrl(URL url)
    {
        if (isRegistCalenderUrlForDetail(url))
        {
            return "Detail.schedule();";
        }

        if (isDeleteCalenderUrlForDetail(url))
        {
            return "Detail.schedule();";
        }

        if (isRegistCalenderUrlForMass(url))
        {
            return "MassRegistration.schedules();";
        }

        if (isDeleteCalenderUrlForMass(url))
        {
            return "MassRegistration.schedules();";
        }

        return null;
    }

    public static String scheduleRegistSuccessJavascriptWithUrl(URL url)
    {
        if (isRegistCalenderUrlForDetail(url))
        {
            return "Detail.onRegistSuccess();";
        }

        if (isRegistCalenderUrlForMass(url))
        {
            return "MassRegistration.onRegistSuccess();";
        }

        return null;
    }

    public static String scheduleDeleteSuccessJavascriptWithUrl(URL url)
    {
        if (isRegistCalenderUrlForDetail(url))
        {
            return "Detail.onDeleteSuccess();";
        }

        if (isRegistCalenderUrlForMass(url))
        {
            return "MassRegistration.onDeleteSuccess();";
        }

        return null;
    }

    public static String scheduleRegistFailedJavascriptWithUrl(URL url)
    {
        if (isRegistCalenderUrlForDetail(url))
        {
            return "Detail.onRegistFailed();";
        }

        if (isRegistCalenderUrlForMass(url))
        {
            return "MassRegistration.onRegistFailed();";
        }

        return null;
    }

    public static String scheduleDeleteFailedJavascriptWithUrl(URL url)
    {
        if (isRegistCalenderUrlForDetail(url))
        {
            return "Detail.onDeleteFailed();";
        }

        if (isRegistCalenderUrlForMass(url))
        {
            return "MassRegistration.onDeleteFailed();";
        }

        return null;
    }

    public void registSchedulesWithJsonString(String schedulesJsonString)
    {

    }

    public void deleteSchedulesWithJsonString(String schedulesJsonString)
    {

    }
}
