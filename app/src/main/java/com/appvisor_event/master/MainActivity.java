package com.appvisor_event.master;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
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
import android.text.format.Time;

import com.appvisor_event.master.modules.AppLanguage.AppLanguage;
import com.appvisor_event.master.modules.Gcm.GcmClient;
import com.google.android.gcm.GCMRegistrar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//import biz.appvisor.push.android.sdk.AppVisorPush;

public class MainActivity extends Activity {

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
    private static MainActivity sInstance;
    public static int preset_flg = 0;
    private int script_flg = 0;
    private String local;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preset_flg = 1;
        //UUIDの取得
        device_id = AppUUID.get(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");
        //DEVICE_TOKENの取得
        device_token = GCMRegistrar.getRegistrationId(this).replace("-","").replace(" ","").replace(">","").replace("<","");
        Log.d("device_token",device_token);

        extraHeaders = new HashMap<String, String>();
        extraHeaders.put("user-id", device_id);

        //ホーム画面の設定
        setContentView(R.layout.activity_main);

        //レイアウトで指定したWebViewのIDを指定する。
        myWebView = (WebView) findViewById(R.id.webView1);

        // JS利用を許可する
        myWebView.getSettings().setJavaScriptEnabled(true);

        //CATHEを使用する
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        // Android 5.0以降は https のページ内に http のコンテンツがある場合に表示出来ない為設定追加。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            myWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        //端末の言語設定を取得
        local = Resources.getSystem().getConfiguration().locale.getLanguage().toString() ;
        AppLanguage.setLanguageWithStringValue(this.getApplicationContext(), local);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // UUIDが取得できていれば、URLをロードする。
        if(!mIsFailure){
            if (device_id != null){
                //最初にホーム画面のページを表示する。
                myWebView.loadUrl(active_url+"?language="+local,extraHeaders);
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
                myWebView.loadUrl(active_url+"?language="+local,extraHeaders);
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
            myJsonSender.mData = device_id ;
            myJsonSender.mLanguage = local ;
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
        sInstance = this;
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
                if (!myWebView.canGoBackOrForward(2)) {
                    preset_flg = 1;
                }
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
            active_url = url;
            String passcode;
            String checkPasscode = "?passcode=";
            int passcode_index = url.indexOf(checkPasscode);

            if((url.indexOf(Constants.APPLI_DOMAIN) != -1) || (url.indexOf(Constants.GOOGLEMAP_URL) != -1)|| (url.indexOf(Constants.GOOGLEMAP_URL2) != -1) || (url.indexOf(Constants.EXHIBITER_DOMAIN) != -1)) {
                if(passcode_index != -1) {
                    passcode = url.substring(passcode_index+10,url.length());
                    SharedPreferences data = getSharedPreferences("ricoh_passcode", MainActivity.getInstance().getApplicationContext().MODE_PRIVATE);
                    SharedPreferences.Editor editor = data.edit();
                    editor.putString("passcode", passcode);
                    editor.apply();
                }else {
                    MainActivity.this.myWebView.stopLoading();
                }
                return false;
            }else{
                if (url.indexOf("app-api://") != -1){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Passcode Error")
                            .setMessage("Please re-enter the passcode")
                            .setPositiveButton("OK", null)
                            .show();

                    return true;
                }
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
                if (url.indexOf(Constants.HOME_URL) != -1 || url.indexOf("?passcode=") != -1) {
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
            if (preset_flg != 0 || script_flg == 1) {
                SharedPreferences data = getSharedPreferences("ricoh_passcode", MainActivity.getInstance().getApplicationContext().MODE_PRIVATE);
                String passcode = data.getString("passcode","");
                if (!passcode.equals("")){
                    view.loadUrl("javascript:setPasscode('"+passcode+"');");
                    script_flg = 1;
                }
                if (preset_flg == 0 && script_flg == 1){
                    script_flg = 0;
                }
                preset_flg = 0;
                if (view.getUrl() != null) {
                    if (!view.getUrl().equals(url)) {
                        view.loadUrl(url);
                    }
                }
            }
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
        }
    }

    private void actionPushNortification(Bundle extras)
    {
        int notificationId = extras.getInt("id", 0);

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

    public static synchronized MainActivity getInstance() {
        return sInstance;
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences data = getSharedPreferences("ricoh_passcode", MainActivity.getInstance().getApplicationContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        Date date = new Date(System.currentTimeMillis());
        editor.putString("time", String.valueOf(date.getTime()));
        editor.putInt("background_flg", 1);
        editor.apply();
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences data = getSharedPreferences("ricoh_passcode", MainActivity.getInstance().getApplicationContext().MODE_PRIVATE);
        String passcode = data.getString("passcode","");
        Date date = new Date(System.currentTimeMillis());
        String time = data.getString("time","");
        int background_flg = data.getInt("background_flg",0);
        if (!passcode.equals("")) {
            if (background_flg == 1) {
                long old_time = Long.parseLong(time);
                long current_time = date.getTime();
                long deff = (current_time - old_time) / (1000*60*60);
                if (deff > 71) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
//                    MainActivity.this.myWebView.loadUrl(Constants.HOME_URL+"?language="+local);
                    preset_flg = 1;
                }
            }
        }
        SharedPreferences.Editor editor = data.edit();
        editor.putInt("background_flg", 0);
        editor.apply();
    }
}
