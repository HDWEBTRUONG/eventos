package com.appvisor_event.master;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.HashMap;
import java.util.Map;

public class SubMenu extends Activity {

    private WebView myWebView;
    private boolean mIsFailure = false;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String uuid;
    private Map<String, String> extraHeaders;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //UUIDの取得
         uuid  = AppUUID.get(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");
         //メニューリストを表示
         setContentView(R.layout.menu_list);

         extraHeaders = new HashMap<String, String>();
         extraHeaders.put("UUID", uuid);

         //レイアウトで指定したWebViewのIDを指定する。
         myWebView = (WebView)findViewById(R.id.webView1);
         //リンクをタップしたときに標準ブラウザを起動させない
         myWebView.setWebViewClient(new WebViewClient());
         // JS利用を許可する
         myWebView.getSettings().setJavaScriptEnabled(true);
         //最初にホーム画面のページを表示する。
         myWebView.loadUrl(Constants.SUB_MENU_URL,extraHeaders);
         //CATHEを使用しない
         myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

         overridePendingTransition(R.anim.right_in, R.anim.nothing);

         myWebView.setWebViewClient(mWebViewClient);

         ImageButton menu_buttom = (ImageButton)findViewById(R.id.menu_buttom_return);
         menu_buttom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 次画面のアクティビティ終了
                finish();

                overridePendingTransition(R.anim.nothing,R.anim.right_out);

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
//                myWebView.setWebViewClient(mWebViewClient_err);

                myWebView.loadUrl(Constants.SUB_MENU_URL);
            }
        });
        // SwipeRefreshLayoutの設定
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorScheme(R.color.red, R.color.green, R.color.blue, R.color.yellow);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (mIsFailure) {
                //ホーム以外の場合はタイトルバーを表示する
                findViewById(R.id.title_bar).setVisibility(View.GONE);
                //SWIPEを非表示にする。
                findViewById(R.id.swipe_refresh_layout).setVisibility(View.GONE);
                //WEBVIEWを非表示にする。
                findViewById(R.id.webView1).setVisibility(View.GONE);
                //エラーページを表示する
                findViewById(R.id.error_page).setVisibility(View.VISIBLE);
            }else{
                //ホーム以外の場合はタイトルバーを表示する
                findViewById(R.id.title_bar).setVisibility(View.VISIBLE);
                //SWIPEを非表示にする。
                findViewById(R.id.swipe_refresh_layout).setVisibility(View.VISIBLE);
                //WEBVIEWを非表示にする。
                findViewById(R.id.webView1).setVisibility(View.VISIBLE);
                //エラーページを表示する
                findViewById(R.id.error_page).setVisibility(View.GONE);

                if (url.equals(Constants.SUB_MENU_URL)) {
                } else {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("key.url",url);

                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);

                    finish();
                    overridePendingTransition(R.anim.nothing,R.anim.right_out);
                }
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            mIsFailure = true;
            myWebView.loadUrl("");
        }
    };
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
}
