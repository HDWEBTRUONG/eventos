package com.appvisor_event.master.modules.JavascriptHandler;

import android.content.Context;

import com.appvisor_event.master.modules.FavoritSeminar;
import com.appvisor_event.master.modules.JavascriptManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by bsfuji on 15/07/27.
 */
public class FavoritSeminarJavascriptHandler implements JavascriptManager.JavascriptHandlerInterface
{
    private final static String HANDLE_URL = "/api/favorites/send";

    private Context        context        = null;
    private String         alertString    = null;
    private FavoritSeminar favoritSeminar = FavoritSeminar.getInstance();

    public FavoritSeminarJavascriptHandler(Context context)
    {
        this.context = context;
    }

    @Override
    public boolean onJsAlert(String alertString)
    {
        this.alertString = alertString;

        return this.analyze();
    }

    private boolean analyze()
    {
        try {
            if (this.isHandleUrl())
            {
                this.resetFavoritSeminarAlarm();
                return true;
            }
        } catch (JSONException e) {
//            e.printStackTrace();
        }
        return false;
    }

    private boolean isHandleUrl() throws JSONException {
        String url = this.getUrl();
        if (null == url)
        {
            return false;
        }

        return (-1 != url.indexOf(HANDLE_URL));
    }

    private JSONObject jsonAlert() throws JSONException
    {
        return new JSONObject(this.alertString);
    }

    private JSONObject jsonMeta() throws JSONException
    {
        return this.jsonAlert().getJSONObject("meta");
    }

    private String getUrl() throws JSONException
    {
        return this.jsonMeta().getString("url");
    }

    private JSONObject jsonResponse() throws JSONException
    {
        return this.jsonAlert().getJSONObject("response");
    }

    private JSONObject jsonData() throws JSONException
    {
        return this.jsonResponse().getJSONObject("data");
    }

    private JSONArray jsonFavoritSeminars() throws JSONException
    {
        return this.jsonData().getJSONArray("EventSeminars");
    }

    private void resetFavoritSeminarAlarm() throws JSONException
    {
        this.favoritSeminar.resetAlarm(this.context);
        this.favoritSeminar.clear();

        JSONArray jsonFavoritSeminars = this.jsonFavoritSeminars();
        for (int i = 0; i < jsonFavoritSeminars.length(); i++)
        {
            JSONObject jsonFavoritSeminar = jsonFavoritSeminars.getJSONObject(i).getJSONObject("EventSeminar");
            this.registFavoritSeminar(jsonFavoritSeminar);
        }

        this.favoritSeminar.setAlarm(this.context);
    }

    private void registFavoritSeminar(JSONObject jsonFavoritSeminar) throws JSONException
    {
        FavoritSeminar.Bean favoritSeminarBean = new FavoritSeminar.Bean(jsonFavoritSeminar);
        this.favoritSeminar.add(favoritSeminarBean);
    }
}
