package com.appvisor_event.master;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.appvisor_event.master.modules.AppLanguage.AppLanguage;
import com.appvisor_event.master.modules.AppPermission.AppPermission;
import com.appvisor_event.master.modules.BeaconService;
import com.appvisor_event.master.modules.Document.Document;
import com.appvisor_event.master.modules.Document.DocumentsActivity;
import com.appvisor_event.master.modules.Gcm.GcmClient;
import com.appvisor_event.master.modules.StartupAd.StartupAd;
import com.google.android.gcm.GCMRegistrar;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

//import biz.appvisor.push.android.sdk.AppVisorPush;

public class MainActivity extends BaseActivity implements AppPermission.Interface {

    private GcmClient gcmClient = null;

    private WebView myWebView;
//    private AppVisorPush appVisorPush;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String active_url = Constants.HOME_URL;
    private String device_id;
    private String device_token;
    private Map<String, String> extraHeaders;
    private MyHttpSender myJsonSender;
    private DeviceTokenSender myJsonDeviceTokenSender;
    //レイアウトで指定したWebViewのIDを指定する。
    private boolean mIsFailure = false;

    private InfosGetter pushGetter;

    //beaconメッセージ関連
    private InfosGetter myJsonbeacon;

    private GPSManager gps;

    private static final String[] beaconDetectionRequiredPermissions = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int beaconDetectionRequiredPermissionsRequestCode = 100;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AppPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public Boolean isRequirePermission(int requestCode, String permission) {
        AppPermission.log(String.format("isRequirePermission: %s", permission));

        Boolean isRequirePermission = false;
        switch (requestCode)
        {
            case beaconDetectionRequiredPermissionsRequestCode:
                switch (permission)
                {
                    case android.Manifest.permission.ACCESS_FINE_LOCATION:
                    case android.Manifest.permission.ACCESS_COARSE_LOCATION:
                        isRequirePermission = true;
                        break;
                }
                break;
        }

        return isRequirePermission;
    }

    @Override
    public void showErrorDialog(int requestCode) {
        AppPermission.log(String.format("showErrorDialog"));
        switch (requestCode)
        {

            case beaconDetectionRequiredPermissionsRequestCode:
                if(AppLanguage.isJapanese(this)) {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_dialog_title))
                            .setMessage(getString(R.string.permission_dialog_message_location))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AppPermission.openSettings(MainActivity.this);
                                }
                            })
                            .create()
                            .show();
                }
                else
                {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_dialog_title_en))
                            .setMessage(getString(R.string.permission_dialog_message_location_en))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AppPermission.openSettings(MainActivity.this);
                                }
                            })
                            .create()
                            .show();
                }
                break;
        }
    }

    @Override
    public void allRequiredPermissions(int requestCode, String[] permissions) {
        switch (requestCode)
        {
            case beaconDetectionRequiredPermissionsRequestCode:
                startService(new Intent(MainActivity.this, BeaconService.class));
                break;
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //UUID, Appid, versionの取得
        device_id  = User.getUUID(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");
        String app_id = User.getAppID(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");
        String version =  User.getAppVersion(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");

        //DEVICE_TOKENの取得
        device_token = GCMRegistrar.getRegistrationId(this).replace("-","").replace(" ","").replace(">","").replace("<","");
        Log.d("device_token",device_token);

        Intent mainintent = getIntent();
        Bundle bundle = mainintent.getExtras();
        if(bundle!=null)
        {
            if(bundle.getString("isbeacon")!=""&&bundle.getString("isbeacon")!=null) {
                showBeaconMeaasge(bundle.getString("title"), bundle.getString("body"), bundle.getString("link"), bundle.getInt("isInternal"));
                sendAPIInfo(device_id, bundle.getString("msgid"), "2");
            }
        }

        extraHeaders = new HashMap<String, String>();
        extraHeaders.put("user-id", device_id);
        extraHeaders.put("app-id", app_id);
        extraHeaders.put("app-version", version);

        //ホーム画面の設定
        setContentView(R.layout.activity_main);

        //レイアウトで指定したWebViewのIDを指定する。
        myWebView = (WebView) findViewById(R.id.webView1);

        // JS利用を許可する
        myWebView.getSettings().setJavaScriptEnabled(true);

        //CATHEを使用する
        if (isCachePolicy())
        {
            myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        else
        {
            myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        }

        // Android 5.0以降は https のページ内に http のコンテンツがある場合に表示出来ない為設定追加。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            myWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        //端末の言語設定を取得
        String local = Resources.getSystem().getConfiguration().locale.getLanguage().toString();
        if(isFirstStart()) {
            //端末の言語設定を取得
            AppLanguage.setLanguageWithStringValue(this.getApplicationContext(), local);
            setIsFirstStarts(false);
        }
        else
        {
            local=AppLanguage.getLanguageWithStringValue(this.getApplicationContext());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // UUIDが取得できていれば、URLをロードする。
        if(!mIsFailure){
            if (device_id != null){
                //最初にホーム画面のページを表示する。
                myWebView.loadUrl(active_url,extraHeaders);
            }
        }

        //ズーム機能を有効にする
        myWebView.setVerticalScrollbarOverlay(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setSupportZoom(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUseWideViewPort(true);

        // WEBクライアントを呼ぶ
        myWebView.setWebViewClient(mWebViewClient);

        myWebView.goBack();

        // 更新ボタンを使用した場合の処理
        Button update_button = (Button)findViewById(R.id.update_button);

        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // エラーをTRUEに戻す
                mIsFailure = false;
                // WEBクライアントを呼ぶ
                myWebView.setWebViewClient(mWebViewClient);
                //URLを表示する
                extraHeaders.put("user-id", device_id);
                myWebView.loadUrl(active_url,extraHeaders);
            }
        });

        // SwipeRefreshLayoutの設定
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.yellow);

        Log.d("device_id",device_id);

        try {

            // 引数にサーバーのURLを入れる。
            myJsonSender = new MyHttpSender ( Constants.REGISTER_API_URL );
            myJsonSender.mLanguage = local;
            myJsonSender.mDeviseID = device_id ;
            myJsonSender.mAppID = app_id ;
            myJsonSender.mVersion = version ;
            myJsonSender.start ();
            myJsonSender.join ();


            // responseがあればログ出力する。
            if ( myJsonSender.mResponse != null ) {
                Log.i ( "message", myJsonSender.mResponse );
            }

        } catch ( InterruptedException e ) {

            e.printStackTrace ();
            Log.i ( "JSON", e.toString () );

        }
        Log.d("device_id",device_id);

        try {

            // 引数にサーバーのURLを入れる。
            myJsonDeviceTokenSender = new DeviceTokenSender ( Constants.DEVICE_TOKEN_API_URL );
            myJsonDeviceTokenSender.device_id = device_id ;
            myJsonDeviceTokenSender.device_token = device_token ;
            myJsonDeviceTokenSender.app_id = app_id ;
            myJsonDeviceTokenSender.version = version ;
            myJsonDeviceTokenSender.start ();
            myJsonDeviceTokenSender.join ();


            // responseがあればログ出力する。
            if ( myJsonDeviceTokenSender.mResponse != null ) {
                Log.i ( "message", myJsonDeviceTokenSender.mResponse );
            }

        } catch ( InterruptedException e ) {

            e.printStackTrace ();
            Log.i ( "JSON", e.toString () );

        }

        this.initGCM();
        this.checkGCMNotification();

        try {
            myJsonbeacon = new InfosGetter(Constants.Beacon_MESSAGE_API+getBeaconVersion());
            myJsonbeacon.start();
            myJsonbeacon.join();
            Log.d("Test josn",Constants.Beacon_MESSAGE_API+getBeaconVersion());
            if (myJsonbeacon.mResponse != null && myJsonbeacon.mResponse != "") {
                JSONObject beaconjson = new JSONObject(myJsonbeacon.mResponse);
                Log.d("Test josn",myJsonbeacon.mResponse);
                if (beaconjson.getInt("status") == 200) {
                    Log.d("Test josn",myJsonbeacon.mResponse);
                    //beaconサービス起動
                    BeaconService.beaconobjs=beaconjson;
                    setBeaconMessages(beaconjson.toString());
                    setBeaconVersion(beaconjson.getString("version"));

                }
                else
                {
                    if(getBeaconMessages()!=null) {
                        beaconjson = new JSONObject(getBeaconMessages());
                        BeaconService.beaconobjs = beaconjson;
                    }
                }

                stopService(new Intent(MainActivity.this, BeaconService.class));
                if(BeaconService.beaconobjs!=null&&BeaconService.beaconobjs.getJSONArray("beacons").length()>0)
                {
                    gps = new GPSManager(this);
                    if(!gps.canGetLocation)
                    {
                        if(AppLanguage.isJapanese(this)) {
                            gps.showSettingsAlert();
                        }
                        else
                        {
                            gps.showSettingsAlertEn();
                        }
                    }
                    if (AppPermission.checkPermission(this, beaconDetectionRequiredPermissions))
                    {
                        startService(new Intent(MainActivity.this, BeaconService.class));
                    }
                    else {
                        AppPermission.requestPermissions(this, beaconDetectionRequiredPermissionsRequestCode, beaconDetectionRequiredPermissions);
                    }
                }
                else
                {
                    stopService(new Intent(MainActivity.this, BeaconService.class));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StartupAd.setShown(false);

        checkVersion();
    }

    @Override
    protected void onResume(){
        super.onResume();

        GcmClient.checkPlayServices(this);
    }

    @Override
    protected void onRestart(){
        Log.d("RESTART","mainActivityに戻った");
        super.onRestart();
//        this.recreate();
        myWebView.reload();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 端末の戻るボタンを押した時にwebviewの戻る履歴があれば1つ前のページに戻る
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (myWebView.canGoBack() == true) {
                myWebView.goBack();
                return true;
            }
            new AlertDialog.Builder(this)
                    .setTitle("アプリケーションの終了")
                    .setMessage("アプリケーションを終了してよろしいですか？")
                    .setPositiveButton("はい", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO 自動生成されたメソッド・スタブ
                            MainActivity.this.finish();
                        }
                    })
                    .setNegativeButton("いいえ", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO 自動生成されたメソッド・スタブ

                        }
                    })
                    .show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            myWebView.reload();

            if (active_url.indexOf(Constants.HOME_URL) != -1)
            {
                checkVersion();
            }
            // 3秒待機
//            new Handler().postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//
//                }
//            }, 10000);
        }
    };

    /** WebViewClientクラス */
    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                final URL urlObject = new URL(url);

                // 「資料」リクエスト
                if (Document.isDocumentUrl(urlObject))
                {
                    showDocumentsActivity();
                    return true;
                }

            } catch (MalformedURLException e) {}

            if (url.indexOf(Constants.HOME_URL) != -1)
            {
                checkVersion();
            }

            active_url = url;
            if((url.indexOf(Constants.APPLI_DOMAIN) != -1)
                    || (url.indexOf(Constants.GOOGLEMAP_URL) != -1)
                    || (url.indexOf(Constants.GOOGLEMAP_URL2) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_1) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_2) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_3) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_4) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_5) != -1)) {
                MainActivity.this.myWebView.stopLoading();
                return false;
            }else{
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }

        }
        /**
         * @see android.webkit.WebViewClient#onPageFinished(android.webkit.WebView, java.lang.String)
         */

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if(url.equals(Constants.ERROR_URL)){
                mIsFailure = true;
            }
            if (mIsFailure) {
                //WEBVIEWを非表示にする。
                findViewById(R.id.webView1).setVisibility(View.GONE);
                //SWIPEを非表示にする。
                findViewById(R.id.swipe_refresh_layout).setVisibility(View.GONE);
                //エラーページを表示する
                findViewById(R.id.error_page).setVisibility(View.VISIBLE);
            }else {
                if (url.equals(Constants.HOME_URL)) {
                    active_url = url;
                    mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
                    mSwipeRefreshLayout.setEnabled(true);
                    //SWIPEを表示にする。
                    findViewById(R.id.swipe_refresh_layout).setVisibility(View.VISIBLE);
                    //WEBVIEWを表示にする
                    findViewById(R.id.webView1).setVisibility(View.VISIBLE);
                    //エラーページを非表示にする
                    findViewById(R.id.error_page).setVisibility(View.INVISIBLE);
                } else {
                    active_url = url;
                    // TODO Auto-generated method stub
                    // インテントのインスタンス生成
                    Intent intent = new Intent(MainActivity.this, Contents.class);
                    // URLを表示
                    intent.putExtra("key.url", active_url);
                    // サブ画面の呼び出し
                    startActivity(intent);
                }
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mSwipeRefreshLayout.setRefreshing(false);

            showStartupAd();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            mIsFailure = true;
            myWebView.loadUrl("");
            //WEBVIEWを非表示にする。
            findViewById(R.id.webView1).setVisibility(View.GONE);
            //SWIPEを非表示にする。
            findViewById(R.id.swipe_refresh_layout).setVisibility(View.GONE);
            //エラーページを表示する
            findViewById(R.id.error_page).setVisibility(View.VISIBLE);
        }
    };

    private void initGCM()
    {
        if (GcmClient.checkPlayServices(this))
        {
            this.gcmClient = new GcmClient(this.getApplicationContext());

            Log.d("MainActivity", "RegistrationID: " + this.gcmClient.getRegistrationId());
        }
    }

    private void checkGCMNotification()
    {
        Bundle extras = this.getIntent().getExtras();
        if (null != extras && extras.getBoolean("GcmNotification", false))
        {
            for (String key : extras.keySet())
            {
                Log.d("MainActivity", "checkGCMNotification: " + key + " = " + extras.get(key));
            }
            this.actionPushNortification(extras);
            this.sendPushNotificationResponse(extras.getString("id", null));
        }
    }

    private void actionPushNortification(Bundle extras)
    {
        int notificationId = extras.getInt("notificationId", 0);

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle("お知らせ")
                .setMessage(extras.getString("content", ""))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        final String link = extras.getString("link", null);

        if (null != link)
        {
            final int linkId = Integer.parseInt(link);

            final String linkOpenType = extras.getString("link_open_type", null);
            if (null != linkOpenType && linkOpenType.equals("internal"))
            {
                dialog.setNegativeButton("開く", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        GcmClient gcmClient = new GcmClient(MainActivity.this.getApplicationContext());

                        Intent intent = new Intent(MainActivity.this, Contents.class);
                        intent.putExtra("key.url", gcmClient.getLink(linkId));
                        startActivity(intent);
                    }
                });
            }
            else
            {
                dialog.setNegativeButton("開く", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GcmClient gcmClient = new GcmClient(MainActivity.this.getApplicationContext());
                        gcmClient.openLink(linkId);
                    }
                });
            }
        }

        dialog.create().show();
    }

    /**
     * 起動時広告を表示する。
     * ただし一度表示したら一度バックグラウンドに遷移しない限り表示しない。
     */
    private void showStartupAd()
    {
        if (null == myWebView)
        {
            return;
        }

        if (StartupAd.isAlreadyShown())
        {
            return;
        }

        myWebView.loadUrl("javascript:showAd();");

        StartupAd.setShown(true);
    }

    private String getBeaconVersion()
    {
        SharedPreferences sharedPreferences=getSharedPreferences("beaconData", Context.MODE_PRIVATE);
        String version = sharedPreferences.getString("beaconVersion","-1" );
        return  version;
    }

    private void sendPushNotificationResponse(String pushId)
    {
        if (null == pushId || !(pushId instanceof String))
        {
            return;
        }

        this.gcmClient.sendResponse(pushId);
    }

    private void setBeaconVersion(String curversion)
    {
        SharedPreferences sharedPreferences=getSharedPreferences("beaconData",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("beaconVersion", curversion);
        editor.apply();
    }

    private String getBeaconMessages()
    {
        SharedPreferences sharedPreferences=getSharedPreferences("beaconData", Context.MODE_PRIVATE);
        String messages = sharedPreferences.getString("beaconmessages","{\"status\":500}");
        return  messages;
    }

    private void setBeaconMessages(String messages)
    {
        SharedPreferences sharedPreferences=getSharedPreferences("beaconData",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("beaconmessages", messages);
        editor.apply();
    }

    private boolean isFirstStart()
    {
        SharedPreferences sharedPreferences=getSharedPreferences("appData", Context.MODE_PRIVATE);
        boolean messages = sharedPreferences.getBoolean("isFirstStart",true);
        return  messages;
    }

    private void setIsFirstStarts(Boolean started)
    {
        SharedPreferences sharedPreferences=getSharedPreferences("appData",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isFirstStart", started);
        editor.apply();
    }

    private void sendAPIInfo(String uuid,String msgid,String msgType)
    {
        try {
            pushGetter = new InfosGetter(Constants.Beacon_AGGREGATE_API+"uuid="+uuid+"&MsgID="+msgid+"&Type="+msgType);
            pushGetter.start();
            pushGetter.join();
//            if (pushGetter.mResponse != null && pushGetter.mResponse != "") {
//                Log.d("pushGetter",pushGetter.mResponse);
//                Log.d("pushGetter",Constants.Beacon_AGGREGATE_API+"uuid="+uuid+"&MsgID="+msgid+"&Type="+msgType);
//            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showDocumentsActivity()
    {
        Intent intent = new Intent(this, DocumentsActivity.class);
        startActivity(intent);
    }
}
