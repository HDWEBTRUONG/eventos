package com.appvisor_event.master;

public class Constants {
    private static final String TAG = Constants.class.getSimpleName();

    private Constants(){}
    //ホームのURL
    public static final String Event = "sap-forum2017";
    //ベースURL
    public static final String BASE_URL = "https://stg-api.appvisor-event.com/";
    // 機能使用の有無
    // ①ログイン機能を使用する場合はtrue、それ以外はfalseを入れる
    public static final Boolean USED_LOGIN = true;

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
    //ログインのURL
    public static final String LOGIN_URL = BASE_URL + Event + "/general_login";
    //設定画面のURL
    public static final String SETTING_URL = BASE_URL + Event + "/settings";
    //ERRORのURL
    public static final String ERROR_URL = "data:text/html,chromewebdata";
    //アプリ内に表示させるドメイン
    public static final String APPLI_DOMAIN = "appvisor-event";
    //アプリ内に表示させるドメイン
    public static final String EXHIBITER_DOMAIN_1 = "sapevent.jp";
    public static final String EXHIBITER_DOMAIN_2 = "XXXXXXXX";
    public static final String EXHIBITER_DOMAIN_3 = "XXXXXXXX";
    public static final String EXHIBITER_DOMAIN_4 = "XXXXXXXX";
    public static final String EXHIBITER_DOMAIN_5 = "XXXXXXXX";
    // GoogleMapのURL
    public static final String GOOGLEMAP_URL = "www.google.com/maps";
    // GoogleMapのURL
    public static final String GOOGLEMAP_URL2 = "maps.google.com/maps";
    // ユーザー取得のためのAPI
    public static final String REGISTER_API_URL = BASE_URL + Event + "/api/users/register.json";
    // device_tokenのためのAPI
    public static final String DEVICE_TOKEN_API_URL = BASE_URL + Event + "/api/users/update.json";
    // バージョンチェックの為のAPI
    public static final String CHECK_VERSION_API_URL = BASE_URL + Event + "/api/users/check_version.json";
    // 資料のためのAPI
    public static final String DOCUMENTS_API_URL = BASE_URL + Event + "/api/documents/get.json";
    //PUSHの設定値
    public static final String GCM_BASE_URL = "https://stg-push.appvisor-event.com/";
    public static final String GCM_SENDER_ID = "485246024931";

    // SharedPreferences KEY
    public static final String LOGGED_IN_STATUS_SP_KEY = "SP_LOGGED_IN";
    public static final String LOGGED_IN_STATUS_KEY = "LOGGED_IN_STATUS";
    public static final String LOGGED_IN_YES = "YES";
    public static final String LOGGED_IN_NO  = "NO";
}
