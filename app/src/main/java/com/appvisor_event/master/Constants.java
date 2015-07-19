package com.appvisor_event.master;

public class Constants {
    private static final String TAG = Constants.class.getSimpleName();

    private Constants(){}
    //ホームのURL
    public static final String Event = "hcr";
    //ベースURL
    public static final String BASE_URL = "http://stg-api.appvisor-event.com/";
    //ホームのURL
    public static final String HOME_URL = BASE_URL + Event;
    //サブメニューのURL
    public static final String SUB_MENU_URL = BASE_URL + Event + "/menu";
    //ブースのURL
    public static final String BOOTH = "/booths/";
    //会場案内図のURL
    public static final String HALL_URL = BASE_URL + Event + "/hall/maps";
    //お気に入りのURL
    public static final String FAVORITE_URL = BASE_URL + Event + "/favorites";
    //ERRORのURL
    public static final String ERROR_URL = "data:text/html,chromewebdata";
    //アプリ内に表示させるドメイン
    public static final String APPLI_DOMAIN = "appvisor-event";
    //アプリ内に表示させるドメイン
    public static final String EXHIBITER_DOMAIN = "www.r-expo.jp";
    // GoogleMapのURL
    public static final String GOOGLEMAP_URL = "www.google.com/maps";
    // GoogleMapのURL
    public static final String GOOGLEMAP_URL2 = "maps.google.com/maps";
    // ユーザー取得のためのAPI
    public static final String REGISTER_API_URL = BASE_URL + Event + "/api/users/register.json";
    // device_tokenのためのAPI
    public static final String DEVICE_TOKEN_API_URL = BASE_URL + Event + "/api/users/update.json";
    //PUSHの設定値
    public static final String APPID = "ukaQM8qMpc";
    public static final String GCM_SENDER_ID = "358563145173";
}
