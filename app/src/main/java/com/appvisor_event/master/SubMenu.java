package com.appvisor_event.master;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.Intent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SubMenu extends Activity {

    private WebView myWebView;
    private boolean mIsFailure = false;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String device_id;
    private Map<String, String> extraHeaders;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //UUIDの取得
        device_id  = AppUUID.get(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");
         //メニューリストを表示
         setContentView(R.layout.menu_list);

        //どの管理画面を見ているか判定する。
        // インテントを取得
        Intent intent = getIntent();
        String active_url = intent.getStringExtra("key.url");
        active_url = active_url.replaceAll(Constants.BASE_URL, "");
        int index = active_url.indexOf("/");
        Constants.Event = active_url.substring(0,index);

        Log.d("メニューのURL", active_url.substring(0,index));

         extraHeaders = new HashMap<String, String>();
         extraHeaders.put("user-id", device_id);

         //レイアウトで指定したWebViewのIDを指定する。
         myWebView = (WebView)findViewById(R.id.webView1);
         //リンクをタップしたときに標準ブラウザを起動させない
         myWebView.setWebViewClient(new WebViewClient());
         // JS利用を許可する
         myWebView.getSettings().setJavaScriptEnabled(true);
         // ドロワー画面のページを表示する。
         myWebView.loadUrl(Constants.BASE_URL + Constants.Event + "/menu", extraHeaders);
         //CATHEを使用する
         myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

         overridePendingTransition(R.anim.right_in, R.anim.nothing);

         myWebView.setWebViewClient(mWebViewClient);

         final ImageView menu_buttom = (ImageView)findViewById(R.id.menu_buttom_return);
         menu_buttom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 次画面のアクティビティ終了
                menu_buttom .setBackgroundColor(getResources().getColor(R.color.selected_color));
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
                extraHeaders.put("user-id", device_id);
                myWebView.loadUrl(Constants.BASE_URL + Constants.Event + "/menu");
            }
        });
        // SwipeRefreshLayoutの設定
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorScheme(R.color.red, R.color.green, R.color.blue, R.color.yellow);
    }

    @Override
    public void onRestart(){
        final ImageView menu_buttom = (ImageView)findViewById(R.id.menu_buttom_return);
        menu_buttom .setBackgroundColor(Color.TRANSPARENT);

        super.onRestart();
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if((url.indexOf(Constants.APPLI_DOMAIN) != -1)  || (url.indexOf(Constants.EXHIBITER_DOMAIN) != -1)) {
                extraHeaders.put("user-id", device_id);
                SubMenu.this.myWebView.loadUrl(url, SubMenu.this.extraHeaders);
                return false;
            }else{
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
        }
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
            }else {
                //ホーム以外の場合はタイトルバーを表示する
                findViewById(R.id.title_bar).setVisibility(View.VISIBLE);
                //SWIPEを非表示にする。
                findViewById(R.id.swipe_refresh_layout).setVisibility(View.VISIBLE);
                //WEBVIEWを非表示にする。
                findViewById(R.id.webView1).setVisibility(View.VISIBLE);
                //エラーページを表示する
                findViewById(R.id.error_page).setVisibility(View.GONE);

                if (url.equals(Constants.BASE_URL + Constants.Event + "/menu")) {

                } else if(url.indexOf(Constants.EXHIBITER_DOMAIN) != -1){
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("key.url",url);

                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);

                    finish();
                    overridePendingTransition(R.anim.nothing,R.anim.right_out);
                }else {
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
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mSwipeRefreshLayout.setRefreshing(false);
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
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            myWebView.reload();
        }
    };

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
                if (deff > 72) {
                    finish();
                }
            }
        }
    }
}
