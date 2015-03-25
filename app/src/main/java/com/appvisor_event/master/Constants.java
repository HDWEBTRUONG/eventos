package com.appvisor_event.master;

public class Constants {
    private static final String TAG = Constants.class.getSimpleName();

    private Constants(){}
    //ホームのURL
    public static final String Event = "edix";
    //ホームのURL
    public static final String HOME_URL = "http://stg-api.appvisor-event.com/" + Event;
    //サブメニューのURL
    public static final String SUB_MENU_URL = "http://stg-api.appvisor-event.com/" + Event + "/menu";
    //ブースのURL
    public static final String BOOTH_URL = "http://stg-api.appvisor-event.com/" + Event + "/areamap";
    //ブースのURL
    public static final String HALL_URL = "http://stg-api.appvisor-event.com/" + Event + "/hall/maps";
    //ERRORのURL
    public static final String ERROR_URL = "data:text/html,chromewebdata";
    //アプリ内に表示させるドメイン
    public static final String APPLI_DOMAIN = "appvisor-event";
    //アプリ内に表示させるドメイン
    public static final String EXHIBITER_DOMAIN = "exhibitor.reedexpo.co.jp";
    // GoogleMapのURL
    public static final String GOOGLEMAP_URL = "www.google.com/maps";
    // GoogleMapのURL
    public static final String GOOGLEMAP_URL2 = "maps.google.com/maps";
    // ユーザー取得のためのAPI_
    public static final String REGISTER_API_URL = "http://stg-api.appvisor-event.com/" + Event + "/api/users/register.json";
    //PUSHの設定値
    public static final String APPID = "qXCjqre1kb";
    public static final String GCM_SENDER_ID = "733514090177";
}
