package com.appvisor_event.master;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

public class SubMenu extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //メニューリストを表示
         setContentView(R.layout.menu_list);
         //レイアウトで指定したWebViewのIDを指定する。
         final WebView myWebView = (WebView)findViewById(R.id.webView1);
         //リンクをタップしたときに標準ブラウザを起動させない
         myWebView.setWebViewClient(new WebViewClient());
         // JS利用を許可する
         myWebView.getSettings().setJavaScriptEnabled(true);
         //最初にホーム画面のページを表示する。
         myWebView.loadUrl(Constants.SUB_MENU_URL);

         overridePendingTransition(R.anim.right_in, R.anim.nothing);

         myWebView.setWebViewClient(new WebViewClient() {

             @Override
             public void onPageStarted(WebView View, String url, Bitmap favicon) {
                 super.onPageStarted(View, url, favicon);
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
         });

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
         }
}
