package com.appvisor_event.master;

public class Constants {
    private static final String TAG = Constants.class.getSimpleName();

    private Constants(){}
    //ホームのURL
//    public static final String Event = "ricoh-test";
    public static  String Event = "ricoh_passcode/passcode";
    //ベースURL
    public static  String BASE_URL = "http://stg-api.appvisor-event.com/";
    //ホームのURL
    public static  String HOME_URL = BASE_URL + Event;
    //サブメニューのURL
    public static  String SUB_MENU_URL = BASE_URL + Event + "/menu";
    //ブースのURL
    public static  String BOOTH = "/booths/";
    //会場案内図のURL
    public static  String HALL_URL = BASE_URL + Event + "/hall/maps";
    //お気に入りのURL
    public static  String FAVORITE_URL = BASE_URL + Event + "/favorites";
    //設定画面のURL
    public static  String SETTING_URL = BASE_URL + Event + "/settings";
    //ERRORのURL
    public static final String ERROR_URL = "data:text/html,chromewebdata";
    //アプリ内に表示させるドメイン
    public static final String APPLI_DOMAIN = "appvisor-event";
    //アプリ内に表示させるドメイン
    public static final String EXHIBITER_DOMAIN = "XXXXX";
    // GoogleMapのURL
    public static final String GOOGLEMAP_URL = "www.google.com/maps";
    // GoogleMapのURL
    public static final String GOOGLEMAP_URL2 = "maps.google.com/maps";
    // ユーザー取得のためのAPI
    public static final String REGISTER_API_URL = BASE_URL + Event + "/api/users/register.json";
    // device_tokenのためのAPI
    public static final String DEVICE_TOKEN_API_URL = BASE_URL + Event + "/api/users/update.json";
    //PUSHの設定値
    public static final String GCM_BASE_URL = "";
    public static final String GCM_SENDER_ID = "";
}
