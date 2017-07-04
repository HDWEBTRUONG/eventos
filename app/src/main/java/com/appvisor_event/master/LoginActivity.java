package com.appvisor_event.master;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.appvisor_event.master.modules.LoginInterface;
import com.google.android.gcm.GCMRegistrar;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ookuma on 2017/05/29.
 */

public class LoginActivity extends Activity {

    private WebView myWebView;
    //レイアウトで指定したWebViewのIDを指定する。
    private boolean mIsFailure = false;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String device_id;
    private String device_token;
    private Map<String, String> extraHeaders;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UUIDの取得
        device_id = AppUUID.get(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");
        //DEVICE_TOKENの取得
        device_token = GCMRegistrar.getRegistrationId(this).replace("-","").replace(" ","").replace(">","").replace("<","");
        Log.d("device_token",device_token);

        extraHeaders = new HashMap<String, String>();
        extraHeaders.put("user-id", device_id);
        //ホーム画面の設定
        setContentView(R.layout.activity_login);

        //レイアウトで指定したWebViewのIDを指定する。
        myWebView = (WebView) findViewById(R.id.webView);

//        // Android 5.0以降は https のページ内に http のコンテンツがある場合に表示出来ない為設定追加。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            myWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // JS利用を許可する
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.addJavascriptInterface(new LoginInterface(this), "LoginInterface");


        String url = Constants.LOGIN_URL+"";
        myWebView.loadUrl(url,extraHeaders);
        myWebView.setWebChromeClient(new WebChromeClient());
        // WEBクライアントを呼ぶ
        myWebView.setWebViewClient(mWebViewClient);

        // SwipeRefreshLayoutの設定
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout1);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.yellow);
    }

    /** WebViewClientクラス */
    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mSwipeRefreshLayout.setRefreshing(false);

        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            mIsFailure = true;
            myWebView.loadUrl("");
            //WEBVIEWを非表示にする。
            findViewById(R.id.webView).setVisibility(View.GONE);
            //SWIPEを非表示にする。
            findViewById(R.id.swipe_refresh_layout1).setVisibility(View.GONE);
            //エラーページを表示する
            findViewById(R.id.error_page).setVisibility(View.VISIBLE);
        }
    };

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            myWebView.reload();
        }
    };

    public void loggedIn(Context context) {
        SharedPreferences data = context.getSharedPreferences(Constants.LOGGED_IN_STATUS_SP_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        editor.putString(Constants.LOGGED_IN_STATUS_KEY, Constants.LOGGED_IN_YES);
        editor.apply();

        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        finish();
    }
}
