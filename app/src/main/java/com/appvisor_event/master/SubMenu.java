package com.appvisor_event.master;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.Toast;

import com.appvisor_event.master.camerasquare.CameraSquareActivity;
import com.appvisor_event.master.modules.AppLanguage.AppLanguage;
import com.appvisor_event.master.modules.BeaconService;
import java.util.Date;

import com.appvisor_event.master.modules.Document.Document;
import com.appvisor_event.master.modules.Document.DocumentsActivity;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SubMenu extends BaseActivity {

    private WebView myWebView;
    private boolean mIsFailure = false;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String device_id;
    private Map<String, String> extraHeaders;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //UUID, Appid, versionの取得
         device_id  = User.getUUID(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");
         String app_id = User.getAppID(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");
         String version =  User.getAppVersion(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");

//        device_id  = AppUUID.get(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");
         //メニューリストを表示
         setContentView(R.layout.menu_list);

        //どの管理画面を見ているか判定する。
        // インテントを取得
        Intent intent = getIntent();

         extraHeaders = new HashMap<String, String>();
         extraHeaders.put("user-id", device_id);
         extraHeaders.put("app-id", app_id);
         extraHeaders.put("app-version", version);

         //レイアウトで指定したWebViewのIDを指定する。
         myWebView = (WebView)findViewById(R.id.webView1);
         //リンクをタップしたときに標準ブラウザを起動させない
         myWebView.setWebViewClient(new WebViewClient());
         // JS利用を許可する
         myWebView.getSettings().setJavaScriptEnabled(true);
         // ドロワー画面のページを表示する。
         myWebView.loadUrl(Constants.SubMenuUrl(), extraHeaders);
         //CATHEを使用する
        if (isCachePolicy())
        {
            myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        else
        {
            myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        }

        overridePendingTransition(R.anim.right_in, R.anim.nothing);

        myWebView.setWebViewClient(mWebViewClient);

        final ImageView menu_buttom = (ImageView) findViewById(R.id.menu_buttom_return);
        menu_buttom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 次画面のアクティビティ終了
                menu_buttom.setBackgroundColor(getResources().getColor(R.color.selected_color));
                finish();

                overridePendingTransition(R.anim.nothing, R.anim.right_out);

            }
        });

        Button update_button = (Button) findViewById(R.id.update_button);

        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // エラーをTRUEに戻す
                mIsFailure = false;
                //URLを表示する
                extraHeaders.put("user-id", device_id);
                myWebView.loadUrl(Constants.SubMenuUrl());
            }
        });
        // SwipeRefreshLayoutの設定
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorScheme(R.color.red, R.color.green, R.color.blue, R.color.yellow);
    }

    @Override
    public void onRestart() {
        final ImageView menu_buttom = (ImageView) findViewById(R.id.menu_buttom_return);
        menu_buttom.setBackgroundColor(Color.TRANSPARENT);

        super.onRestart();
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            try {
                final URL urlObject = new URL(url);

                // 「資料」リクエスト
                if (Document.isDocumentUrl(urlObject))
                {
                    showDocumentsActivity();
                    finish();
                    return true;
                }

            } catch (MalformedURLException e) {}

            if((url.indexOf(Constants.APPLI_DOMAIN) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_1) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_2) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_3) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_4) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_5) != -1)) {
                extraHeaders.put("user-id", device_id);
                SubMenu.this.myWebView.loadUrl(url, SubMenu.this.extraHeaders);
                return false;
            }else if (url.contains(Constants.SUB_MENU_URL)){
                return true;
            } else{
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
            } else {
                //ホーム以外の場合はタイトルバーを表示する
                findViewById(R.id.title_bar).setVisibility(View.VISIBLE);
                //SWIPEを非表示にする。
                findViewById(R.id.swipe_refresh_layout).setVisibility(View.VISIBLE);
                //WEBVIEWを非表示にする。
                findViewById(R.id.webView1).setVisibility(View.VISIBLE);
                //エラーページを表示する
                findViewById(R.id.error_page).setVisibility(View.GONE);

                if (url.equals(Constants.SubMenuUrl())) {

                } else if(url.indexOf(Constants.EXHIBITER_DOMAIN_1) != -1
                        || url.indexOf(Constants.EXHIBITER_DOMAIN_2) != -1
                        || url.indexOf(Constants.EXHIBITER_DOMAIN_3) != -1
                        || url.indexOf(Constants.EXHIBITER_DOMAIN_4) != -1
                        || url.indexOf(Constants.EXHIBITER_DOMAIN_5) != -1){
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("key.url", url);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.nothing, R.anim.right_out);
                } else if (url.indexOf(Constants.HREF_PHOTO_FRAMES) != -1) {
                    String p = SubMenu.this.getFilesDir().toString() + "/images";
                    File file=new File(p);
                    if (!file.exists()){
                        myWebView.loadUrl(Constants.SUB_MENU_URL,extraHeaders);
                        showCameradialog();
                    }else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            int checkCallPhonePermission = ContextCompat.checkSelfPermission(SubMenu.this, android.Manifest.permission.CAMERA);
                            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA);
                                return;
                            } else {
                                finish();
                                Intent intent = new Intent(SubMenu.this, CameraSquareActivity.class);//getApplication()
                                startActivity(intent);
//                            myWebView.clearHistory();
//                            myWebView.loadUrl(Constants.SUB_MENU_URL,extraHeaders);
                            }

                        } else {
                            finish();
                            Intent intent = new Intent(SubMenu.this, CameraSquareActivity.class);//getApplication()
                            startActivity(intent);
//                        myWebView.clearHistory();
//                        myWebView.loadUrl(Constants.SUB_MENU_URL,extraHeaders);
                        }
                    }

                } else {

                    if ((url.indexOf(Constants.RegARFlag) != -1)) {
                        if (!BeaconService.isUnityService) {
                            //テストFOR Unity
                            finish();
                            BeaconService.isUnityService = true;
                            Intent intent = new Intent(SubMenu.this, TgsUnityActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("key.url", url);

                        intent.putExtras(bundle);
                        setResult(RESULT_OK, intent);

                        finish();
                        overridePendingTransition(R.anim.nothing, R.anim.right_out);
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

    private void showCameradialog() {
        String content = "";
        String ok = "";

        if (AppLanguage.getLanguageWithStringValue(this).equals("ja")) {
            content = getResources().getString(R.string.camera_no_image_jp);
            ok = getResources().getString(R.string.camera_no_certain_jp);

        } else {
            content = getResources().getString(R.string.camera_no_image_en);
            ok = getResources().getString(R.string.camera_no_certain_en);
        }
        new AlertDialog.Builder(this).setMessage(content)
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }

                ).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("testpermisson", requestCode + "");
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                finish();
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
                }else{
                    SharedPreferences.Editor editor = data.edit();
                    editor.putString("time", String.valueOf(current_time));
                    editor.apply();
                }
            }
        }
    }

    private void showDocumentsActivity()
    {
        Intent intent = new Intent(this, DocumentsActivity.class);
        startActivity(intent);
    }
}
