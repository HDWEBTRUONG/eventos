package com.appvisor_event.master.modules;

import android.content.Context;

import com.appvisor_event.master.modules.alarm.FavoritSeminarAlarm;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by bsfuji on 15/07/27.
 */
public class FavoritSeminar
{
    private static FavoritSeminar sharedInstance = new FavoritSeminar();

    public static FavoritSeminar getInstance()
    {
        return sharedInstance;
    }

    private ArrayList<Bean> beans = new ArrayList<Bean>();

    private FavoritSeminar() {}

    public void add(Bean bean)
    {
        this.beans.add(bean);
    }

    public void clear()
    {
        this.beans.clear();
    }

    public void setAlarm(Context context)
    {
        for (Bean bean : this.beans)
        {
            FavoritSeminarAlarm.add(context, bean.getId(), bean.getTitle(), bean.getStartDate());
        }
    }

    public void resetAlarm(Context context)
    {
        FavoritSeminarAlarm.cancelAll(context);
    }

    public static class Bean
    {
        private int    id        = 0;
        private String title     = null;
        private String startDate = null;
        private String endDate   = null;

        public Bean(JSONObject jsonFavoritSeminar) throws JSONException
        {
            this.id        = this.parseId(jsonFavoritSeminar);
            this.title     = this.parseTitle(jsonFavoritSeminar);
            this.startDate = this.parseStartDate(jsonFavoritSeminar);
            this.endDate   = this.parseEndDate(jsonFavoritSeminar);
        }

        public int getId()
        {
            return this.id;
        }

        public String getTitle()
        {
            return this.title;
        }

        public String getStartDate()
        {
            return this.startDate;
        }

        public String getEndDate()
        {
            return this.endDate;
        }

        private int parseId(JSONObject jsonFavoritSeminar) throws JSONException
        {
            return jsonFavoritSeminar.getInt("id");
        }

        private String parseTitle(JSONObject jsonFavoritSeminar) throws JSONException
        {
            return jsonFavoritSeminar.getString("title");
        }

        private String parseStartDate(JSONObject jsonFavoritSeminar) throws JSONException
        {
            return jsonFavoritSeminar.getString("start_date");
        }

        private String parseEndDate(JSONObject jsonFavoritSeminar) throws JSONException
        {
            return jsonFavoritSeminar.getString("end_date");
        }
    }
}
