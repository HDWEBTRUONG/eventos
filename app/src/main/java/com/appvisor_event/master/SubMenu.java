package com.appvisor_event.master;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.appvisor_event.master.camerasquare.CameraSquareActivity;
import com.appvisor_event.master.modules.AppPermission.AppPermission;
import com.appvisor_event.master.modules.BeaconService;

import java.util.HashMap;
import java.util.Map;

public class SubMenu extends BaseActivity {

    private WebView myWebView;
    private boolean mIsFailure = false;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String device_id;
    private Map<String, String> extraHeaders;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //UUIDの取得
        device_id  = AppUUID.get(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");
         //メニューリストを表示
         setContentView(R.layout.menu_list);

         extraHeaders = new HashMap<String, String>();
         extraHeaders.put("user-id", device_id);

         //レイアウトで指定したWebViewのIDを指定する。
         myWebView = (WebView)findViewById(R.id.webView1);
         //リンクをタップしたときに標準ブラウザを起動させない
         myWebView.setWebViewClient(new WebViewClient());
         // JS利用を許可する
         myWebView.getSettings().setJavaScriptEnabled(true);
         // ドロワー画面のページを表示する。
         myWebView.loadUrl(Constants.SUB_MENU_URL, extraHeaders);
         //CATHEを使用する
        if(isCachePolicy())
        {
            myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }else {
            //CATHEを使用する
            myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        }

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
                myWebView.loadUrl(Constants.SUB_MENU_URL);
            }
        });
        // SwipeRefreshLayoutの設定
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorScheme(R.color.red, R.color.green, R.color.blue, R.color.yellow);
    }

    private boolean isCachePolicy()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        if(cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected())
        {
            return false;
        }else
        {
            return true;
        }
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

                if (url.equals(Constants.SUB_MENU_URL)) {

                } else if(url.indexOf(Constants.EXHIBITER_DOMAIN) != -1){
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("key.url",url);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.nothing,R.anim.right_out);
                } else if (url.indexOf(Constants.HREF_PHOTO_FRAMES) != -1){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA);
                    }else{
                        Intent intent = new Intent(SubMenu.this, CameraSquareActivity.class);//getApplication()
                        startActivity(intent);
                    }

                } else {

                    if((url.indexOf(Constants.RegARFlag) != -1))
                    {
                        if(!BeaconService.isUnityService) {
                            //テストFOR Unity
                            finish();
                            BeaconService.isUnityService = true;
                            Intent intent = new Intent(SubMenu.this, TgsUnityActivity.class);
                            startActivity(intent);
                        }
                    }
                    else {
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(SubMenu.this, CameraSquareActivity.class);//getApplication()
                startActivity(intent);
            } else {
                // Permission Denied
                Toast.makeText(SubMenu.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
