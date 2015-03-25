package com.appvisor_event.master;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import biz.appvisor.push.android.sdk.AppVisorPush;

public class Contents extends Activity{

    private WebView myWebView;
    private AppVisorPush appVisorPush;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String active_url = Constants.HOME_URL;
    private String device_token;
    private Map<String, String> extraHeaders;
    private MyHttpSender myJsonSender;
    //レイアウトで指定したWebViewのIDを指定する。
    private boolean mIsFailure = false;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //UUIDの取得
        device_token = UUID.randomUUID().toString().replace("-","").replace(" ","");

        extraHeaders = new HashMap<String, String>();
        extraHeaders.put("user_id", device_token);

        //ホーム画面の設定
        setContentView(R.layout.activity_main_contents);

        //タイトルバーを非表示
        findViewById(R.id.title_bar).setVisibility(View.GONE);

        //レイアウトで指定したWebViewのIDを指定する。
        myWebView = (WebView) findViewById(R.id.webView1);

        // JS利用を許可する
        myWebView.getSettings().setJavaScriptEnabled(true);

        //CATHEを使用しない
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        // インテントを取得
        Intent intent = getIntent();
        // インテントに保存されたデータを取得
        active_url = intent.getStringExtra("key.url");
        Log.d("active_url_contents",active_url);

        if(!mIsFailure){
            if (device_token != null){
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

        myWebView.setWebViewClient(mWebViewClient);

        myWebView.goBack();

        ImageButton btn = (ImageButton) findViewById(R.id.menu_buttom);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // インテントのインスタンス生成
                Intent intent = new Intent(Contents.this, SubMenu.class);
                int requestCode = 1;
                startActivityForResult(intent, requestCode);

            }
        });

        Button update_button = (Button)findViewById(R.id.update_button);

        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // エラーをTRUEに戻す
                mIsFailure = false;
                //URLを表示する
                myWebView.loadUrl(active_url,extraHeaders);
            }
        });


        //sdk初期化(必須)
        this.appVisorPush = AppVisorPush.sharedInstance();
        //AppVisorPush用のAPPIDを設定します。
        String appID = Constants.APPID;
        this.appVisorPush.setAppInfor(getApplicationContext(), appID);
        //通知関連の内容を設定します。(GCM_SENDER_ID,通知アイコン,ステータスバーアイコン,通知で起動するClass名、デフォルトの通知タイトル)
        this.appVisorPush.startPush(Constants.GCM_SENDER_ID, 0, R.drawable.ic_launcher, MainActivity.class, getString(R.string.app_name));
        //Push反応率チェック(必須)
        this.appVisorPush.trackPushWithActivity(this);

        // SwipeRefreshLayoutの設定
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorScheme(R.color.red, R.color.green, R.color.blue, R.color.yellow);

        Log.d("device_token", device_token);

        try {

            // 引数にサーバーのURLを入れる。
            myJsonSender = new MyHttpSender ( Constants.REGISTER_API_URL );
            myJsonSender.mData = device_token ;
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

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 端末の戻るボタンを押した時にwebviewの戻る履歴があれば1つ前のページに戻る
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (myWebView.canGoBack() == true) {
                myWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    active_url = bundle.getString("key.url", "");
                    if(active_url.equals(Constants.HOME_URL)){
                        finish();
                    }

                    if (null != myWebView) {
                        myWebView.loadUrl(active_url,extraHeaders);
                    }
                }
                break;
            default:
                break;
        }
    }

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            // 3秒待機
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                    myWebView.reload();
                }
            }, 3000);
        }
    };

    /** WebViewClientクラス */
    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            active_url = url;
            if((url.indexOf(Constants.APPLI_DOMAIN) != -1) || (url.indexOf(Constants.GOOGLEMAP_URL) != -1)|| (url.indexOf(Constants.GOOGLEMAP_URL2) != -1) || (url.indexOf(Constants.EXHIBITER_DOMAIN) != -1)) {
                Contents.this.myWebView.loadUrl(url, Contents.this.extraHeaders);
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
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(url.equals(Constants.ERROR_URL)){
                mIsFailure = true;
            }
            if (mIsFailure) {
                //ホーム以外の場合はタイトルバーを表示する
                findViewById(R.id.title_bar).setVisibility(View.GONE);
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
                    //ホームの場合はタイトルバーを非表示にする
                    findViewById(R.id.title_bar).setVisibility(View.GONE);
                    //SWIPEを表示にする。
                    findViewById(R.id.swipe_refresh_layout).setVisibility(View.VISIBLE);
                    //WEBVIEWを表示にする
                    findViewById(R.id.webView1).setVisibility(View.VISIBLE);
                    //エラーページを非表示にする
                    findViewById(R.id.error_page).setVisibility(View.INVISIBLE);

                } else if ((url.indexOf(Constants.GOOGLEMAP_URL) != -1) || (url.indexOf(Constants.GOOGLEMAP_URL2) != -1)) {
                    active_url = url;
                    // SwipeRefreshLayoutの設定
                    mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
                    //SWIPEを表示にする。
                    findViewById(R.id.swipe_refresh_layout).setVisibility(View.VISIBLE);
                    //ホーム以外の場合はタイトルバーを表示する
                    findViewById(R.id.title_bar).setVisibility(View.VISIBLE);
                    //WEBVIEWを表示にする
                    findViewById(R.id.webView1).setVisibility(View.VISIBLE);
                    //エラーページを非表示にする
                    findViewById(R.id.error_page).setVisibility(View.INVISIBLE);
                    //更新処理はできなくする
                    mSwipeRefreshLayout.setEnabled(false);
                    // IDからTextViewインスタンスを取得
                    TextView textView = (TextView) findViewById(R.id.content_text);
                    // 表示するテキストの設定
                    textView.setText(Constants.GOOGLEMAP_TITLE);
                } else if ((url.indexOf(Constants.BOOTH_URL) != -1) || (url.indexOf(Constants.HALL_URL) != -1)) {
                    active_url = url;
                    // SwipeRefreshLayoutの設定
                    mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
                    //SWIPEを表示にする。
                    findViewById(R.id.swipe_refresh_layout).setVisibility(View.VISIBLE);
                    //ホーム以外の場合はタイトルバーを表示する
                    findViewById(R.id.title_bar).setVisibility(View.VISIBLE);
                    //WEBVIEWを表示にする
                    findViewById(R.id.webView1).setVisibility(View.VISIBLE);
                    //エラーページを非表示にする
                    findViewById(R.id.error_page).setVisibility(View.INVISIBLE);
                    //更新処理はできなくする
                    mSwipeRefreshLayout.setEnabled(false);
                    // IDからTextViewインスタンスを取得
                    TextView textView = (TextView) findViewById(R.id.content_text);
                    // 表示するテキストの設定
                    textView.setText(myWebView.getTitle());
                } else {
                    active_url = url;
                    mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
                    mSwipeRefreshLayout.setEnabled(true);
                    //ホーム以外の場合はタイトルバーを表示する
                    findViewById(R.id.title_bar).setVisibility(View.VISIBLE);
                    //SWIPEを表示にする。
                    findViewById(R.id.swipe_refresh_layout).setVisibility(View.VISIBLE);
                    //WEBVIEWを表示にする
                    findViewById(R.id.webView1).setVisibility(View.VISIBLE);
                    //エラーページを非表示にする
                    findViewById(R.id.error_page).setVisibility(View.INVISIBLE);
                    // IDからTextViewインスタンスを取得
                    TextView textView = (TextView) findViewById(R.id.content_text);
                    // 表示するテキストの設定
                    textView.setText(myWebView.getTitle());
                }
            }
        }



        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            mIsFailure = true;
            myWebView.loadUrl("");
        }
    };
}
