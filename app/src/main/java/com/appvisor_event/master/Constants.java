package com.appvisor_event.master;

public class Constants {
    private static final String TAG = Constants.class.getSimpleName();

    private Constants(){}
    //ホームのURL
    public static final String HOME_URL = "http://stg-api.appvisor-event.com/edix";
    //サブメニューのURL
    public static final String SUB_MENU_URL = "http://stg-api.appvisor-event.com/edix/menu";
    //サブメニューのURL
    public static final String BOOTH_URL = "http://stg-api.appvisor-event.com/edix/areamap/";
    //サブメニューのURL
    public static final String ERROR_URL = "data:text/html,chromewebdata";
    //アプリ内に表示させるドメイン
    public static final String APPLI_DOMAIN = "appvisor-event";
    // GoogleMapのURL
    public static final String GOOGLEMAP_URL = "www.google.com/maps";
    // GoogleMapのURL
    public static final String GOOGLEMAP_URL2 = "maps.google.com/maps";
    // GoogleMapのタイトル
    public static final String GOOGLEMAP_TITLE = "アクセス";
    // ユーザー取得のためのAPI_
    public static final String REGISTER_API_URL = "http://stg-api.appvisor-event.com/edix/api/users/register.json";
    // ユーザー取得のためのAPI_PATH
    public static final String REGISTER_API_PATH = "/api/users/register.json";
    //PUSHの設定値
    public static final String APPID = "qXCjqre1kb";
    public static final String GCM_SENDER_ID = "733514090177";
    //レスポンス
    public static final String ERROR_RES = "error";
    //エラーコード
    public static final String NO_ERROR = "ERROR00";
}
