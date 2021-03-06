package com.appvisor_event.master;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appvisor_event.master.modules.Advertisement.Advertisement;
import com.appvisor_event.master.modules.AndroidBeaconMapInterface;
import com.appvisor_event.master.modules.AndroidSpiralETicketInterface;
import com.appvisor_event.master.modules.AppLanguage.AppLanguage;
import com.appvisor_event.master.modules.AppPermission.AppPermission;
import com.appvisor_event.master.modules.AssetsManager;
import com.appvisor_event.master.modules.BeaconService;
import com.appvisor_event.master.modules.JavascriptHandler.FavoritSeminarJavascriptHandler;
import com.appvisor_event.master.modules.JavascriptManager;
import com.appvisor_event.master.modules.PermissionRequestManager.PermissionRequestManager;
import com.appvisor_event.master.modules.Schedule.Schedule;
import com.appvisor_event.master.modules.Spiral.ETicket.QRCodeScannerActivity;
import com.appvisor_event.master.modules.WebAppInterface;
import com.appvisor_event.master.util.SPUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Contents extends BaseActivity implements  AppPermission.Interface {

    private WebView myWebView;
    private static final String TAG = "TAG";
    private BeaconManager beaconManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String active_url = Constants.HOME_URL;
    private String device_id;
    private Map<String, String> extraHeaders;
    private MyHttpSender myJsonSender;
    private BluetoothAdapter bluetoothAdapter;
    //レイアウトで指定したWebViewのIDを指定する。
    private boolean mIsFailure = false;
    private String backurl = "#####";
    private double longitude;
    private double latitude;
    private GPSManager gps;
    private ArrayList<Region> regionB;

    private String imageDownloadUrl;

    private Uri m_uri;

    static boolean isMultAdShow = false;

    private static final String[] beaconDetectionRequiredPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int beaconDetectionRequiredPermissionsRequestCode = 100;

    private static final String[] imageUploadRequiredPermissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private static final int imageUploadRequiredPermissionsRequestCode = 101;

    private static final String[] qrcodeScannerRequiredPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final String[] readingQRcodeRequiredPermissions = {
            Manifest.permission.CAMERA
    };
    private static final int qrcodeScannerRequiredPermissionsRequestCode = 102;

    private static final String[] spiralETicketQRCodeScannerRequiredPermissions = {
            Manifest.permission.CAMERA
    };
    private static final int spiralETicketQRCodeScannerRequiredPermissionsRequestCode = 103;

    private static final String[] imageDownloadRequiredPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int imageDownloadRequiredPermissionsRequestCode = 104;

    private String beaconData;

    private String inputId = null;
    private int width  = 0;
    private int height = 0;
    public String encodedString = "";

    private static final int ShowGalleryChooserRequestCode = 200;

    //広告表示
    private Handler ad_handler = new Handler();
    private String ad_image = null;
    private String ad_link = null;

    private Runnable adRunnable;

    private boolean isFromMessage = false;
    private boolean isInternalOpen = false;

    BeaconContentsReceiver beaconContentsReceiver;
    IntentFilter intentFilter;

    //検知一番近いbeacon
    class  BeaconContentsReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle.getBoolean("beaconON")) {
                String uuid = bundle.getString("uuid");
                String major = bundle.getString("major");
                String minor = bundle.getString("minor");
                sendBeacon(uuid, major, minor);
            }else
            {
                clearBeacon();
            }

        }
    }

    private Schedule schedule = null;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AppPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //UUID, Appid, versionの取得
        device_id  = User.getUUID(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");
        String app_id = User.getAppID(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");
        String version =  User.getAppVersion(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");

//      device_id  = AppUUID.get(this.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<","");
        extraHeaders = new HashMap<String, String>();
        extraHeaders.put("user-id", device_id);
        extraHeaders.put("app-id", app_id);
        extraHeaders.put("app-version", version);
        Log.d("device_token",device_id);

        //ホーム画面の設定
        setContentView(R.layout.activity_main_contents);
        //タイトルバーを非表示
        findViewById(R.id.title_bar).setVisibility(View.GONE);
        //レイアウトで指定したWebViewのIDを指定する。
        myWebView = (WebView) findViewById(R.id.webView1);

        myWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView webView = (WebView) v;
                WebView.HitTestResult hr = webView.getHitTestResult();
                switch (hr.getType())
                {
                    case WebView.HitTestResult.IMAGE_TYPE:
                        String url = hr.getExtra();
                        confirmDownloadImageDialog(url);
                        break;
                }
                return false;
            }
        });

        myWebView.addJavascriptInterface(new WebAppInterface(this),"Android");
        myWebView.addJavascriptInterface(new AndroidSpiralETicketInterface(this),"AndroidSpiralETicketInterface");
        myWebView.addJavascriptInterface(new AndroidBeaconMapInterface(this),"AndroidBeaconMapInterface");
        myWebView.addJavascriptInterface(new WebAppInterface(this),"Android");

        schedule = new Schedule(myWebView, this);
        myWebView.addJavascriptInterface(schedule, "ScheduleJavascriptInterface");
        // JS利用を許可する
        myWebView.getSettings().setJavaScriptEnabled(true);
        // ファイルアクセスを許可する
        myWebView.getSettings().setAllowFileAccess(true);

        if(isCachePolicy())
        {
            myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }else {
            myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        }

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

        // インテントを取得
        Intent intent = getIntent();
        // インテントに保存されたデータを取得
        active_url = intent.getStringExtra("key.url");
        isInternalOpen = intent.getBooleanExtra("internal", false);
        isFromMessage = intent.getBooleanExtra("isMessagefrom",false);
//        Log.d("active_url_contents",active_url);

        if(!mIsFailure){
            if (device_id != null){
                //デバイストークンが取れていれば、URLをロードする。
                extraHeaders.put("user-id", device_id);
                myWebView.loadUrl(active_url,extraHeaders);
            }
        }
        //ズーム機能を有効にする
        myWebView.setVerticalScrollbarOverlay(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setSupportZoom(true);
        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            /* Do whatever you need here */
                return super.onJsAlert(view, url, message, result);
            }
        });

        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUseWideViewPort(true);

        myWebView.setWebViewClient(mWebViewClient);

        myWebView.goBack();

        Button update_button = (Button)findViewById(R.id.update_button);

        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // エラーをTRUEに戻す
                mIsFailure = false;
                // 更新を行う
                extraHeaders.put("user-id", device_id);
                myWebView.loadUrl(active_url,extraHeaders);
            }
        });

        // SwipeRefreshLayoutの設定
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.yellow);

        Log.d("device_token", device_id);

        try {

            // 引数にサーバーのURLを入れる。
            myJsonSender = new MyHttpSender ( Constants.REGISTER_API_URL );
            myJsonSender.mDeviseID = device_id ;
            myJsonSender.mAppID = app_id ;
            myJsonSender.mVersion = version ;
            myJsonSender.start ();
            myJsonSender.join ();


            // responseがあればログ出力する。
            if ( myJsonSender.mResponse != null ) {
//                Log.i ( "message", myJsonSender.mResponse );
            }

        } catch ( InterruptedException e ) {

            e.printStackTrace ();

        }

        // お気に入りに登録しているセミナーの開始時間10分前にローカル通知を発行する準備
        this.setupFavoritSeminarAlarm();

        //広告表示と非表示などコントロール
        if(Advertisement.isLoaded && Advertisement.interval > 0)
        {

            findViewById(R.id.adview).setVisibility(View.VISIBLE);
            findViewById(R.id.error_page).setVisibility(View.GONE);
            int sc_width = getResources().getDisplayMetrics().widthPixels;
            int sc_height = getResources().getDisplayMetrics().heightPixels;
            float sc_density = getResources().getDisplayMetrics().density;
            int ad_width = sc_width;
            int ad_height = (int)(ad_width * Advertisement.ratio);
            LinearLayout.LayoutParams layoutParams_adview = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ad_height);
            findViewById(R.id.adview).setLayoutParams(layoutParams_adview);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (sc_height - 44 * sc_density - ad_height-MainActivity.status_bar_height));
            findViewById(R.id.swipe_refresh_layout).setLayoutParams(layoutParams);

            final ImageLoader imageLoader = ImageLoader.getInstance();
            if(Advertisement.list.length()>1)
            {
                isMultAdShow = true;
                if(adRunnable == null) {
                    adRunnable = new Runnable() {
                        @Override
                        public void run() {

                            try {
                                if (!isMultAdShow) {
                                    return;
                                }
                                JSONObject adJson = Advertisement.list.getJSONObject(Advertisement.currentIndex);
                                ad_image = adJson.getString("imageurl");
                                ad_link = adJson.getString("url");
                                imageLoader.loadImage(ad_image, new SimpleImageLoadingListener() {
                                    @Override
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                                        int ad_width = loadedImage.getWidth();
                                        int ad_height = loadedImage.getHeight();
                                        int sc_width = getResources().getDisplayMetrics().widthPixels;
                                        ad_height = (int) ((float) sc_width / (float) ad_width * ad_height);
                                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ad_height);
                                        ImageView adimageview = ((ImageView) findViewById(R.id.ad));
                                        adimageview.setLayoutParams(layoutParams);

                                        adimageview.setImageBitmap(loadedImage);
                                        adimageview.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ad_link)));
                                            }
                                        });
                                    }
                                });

                                ad_handler.postDelayed(this, Advertisement.interval * 1000);
                                Advertisement.currentIndex++;
                                if (Advertisement.currentIndex >= Advertisement.list.length()) {
                                    Advertisement.currentIndex = 0;
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    ad_handler.post(adRunnable);
                }
            }
            else {

                try
                {
                    JSONObject adJson = Advertisement.list.getJSONObject(Advertisement.currentIndex);
                    ad_image = adJson.getString("imageurl");
                    ad_link = adJson.getString("url");

                    imageLoader.loadImage(ad_image, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            int ad_width = loadedImage.getWidth();
                            int ad_height = loadedImage.getHeight();
                            int sc_width = getResources().getDisplayMetrics().widthPixels;
                            ad_height = (int) ((float) sc_width / (float) ad_width * ad_height);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ad_height);
                            ImageView adimageview = ((ImageView) findViewById(R.id.ad));
                            adimageview.setLayoutParams(layoutParams);

                            adimageview.setImageBitmap(loadedImage);
                            adimageview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ad_link)));
                                }
                            });
                        }
                    });
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            findViewById(R.id.adview).setVisibility(View.GONE);
        }

        beaconContentsReceiver=new BeaconContentsReceiver();
        intentFilter=new IntentFilter();
        intentFilter.addAction("Beacon_Nearest");
        registerReceiver(beaconContentsReceiver,intentFilter);

    }

    private void showGallery()
    {
        if (AppPermission.checkPermission(this, imageUploadRequiredPermissions))
        {
            openGallery();
        }
        else
        {
            AppPermission.requestPermissions(this, imageUploadRequiredPermissionsRequestCode, imageUploadRequiredPermissions);
        }
    }

    private void openGallery()
    {
        //カメラの起動Intentの用意
        String photoName = System.currentTimeMillis() + ".jpg";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, photoName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        m_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, m_uri);

        // ギャラリー用のIntent作成
        Intent intentGallery;
        if (Build.VERSION.SDK_INT < 19) {
            intentGallery = new Intent(Intent.ACTION_GET_CONTENT);
            intentGallery.setType("image/*");
        } else {
            intentGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intentGallery.addCategory(Intent.CATEGORY_OPENABLE);
            intentGallery.setType("image/*");
        }
        Intent intent = Intent.createChooser(intentCamera, "画像の選択");
        intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {intentGallery});
        startActivityForResult(intent, ShowGalleryChooserRequestCode);
    }

    public void onClickSearch(View view){
        if (null == view)
        {
            return;
        }

        view.setBackgroundColor(getResources().getColor(R.color.selected_color));

        String url = (String)view.getTag(R.string.search_button_tag_key);

        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.d("onClickSearch", url);

        myWebView.loadUrl(String.format("javascript:window.location.href = '%s'", url));
    }
    public void onClickButtonBack(View view) {
        // 端末の戻るボタンを押した時にwebviewの戻る履歴があれば1つ前のページに戻る
        if (myWebView.canGoBack() == true) {
            Log.d("戻る前URL",(myWebView.copyBackForwardList().getItemAtIndex(myWebView.copyBackForwardList().getCurrentIndex() -1).getUrl()));
            view .setBackgroundColor(getResources().getColor(R.color.selected_color));
            myWebView.goBack();

        }else{
            view .setBackgroundColor(getResources().getColor(R.color.selected_color));
            // 端末のホーム画面に戻る
            Contents.this.finish();
        }
    }

    public void buttonBar(final String fileName, final String url){
        Log.d("buttonBar", "fileName: " + fileName + " url: " + url);
        runOnUiThread(new Runnable() {
            public void run() {
                int resId = getResources().getIdentifier(fileName, "drawable", getPackageName());
                if (0 != resId)
                {
                    ImageView searchImageButton = (ImageView)findViewById(R.id.bar_search_button);
                    searchImageButton.setImageResource(resId);
                    searchImageButton.setBackgroundColor(0);
                    searchImageButton.setTag(R.string.search_button_tag_key, url);
                    searchImageButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void onClickMenu(View view) {
        // インテントのインスタンス生成
        view.setBackgroundColor(getResources().getColor(R.color.selected_color));
        Intent intent = new Intent(Contents.this, SubMenu.class);
        int requestCode = 1;
        startActivityForResult(intent, requestCode);

    }

    public void startQR(){
        gps = new GPSManager(this);
        if (!gps.canGetLocation)
        {
            if(AppLanguage.isJapanese(this)) {
                gps.showSettingsAlert();
            }
            else
            {
                gps.showSettingsAlertEn();
            }
            return;
        }

        if (AppPermission.checkPermission(this, qrcodeScannerRequiredPermissions))
        {
            startQRCodeScanner();
        }
        else {
            AppPermission.requestPermissions(this, qrcodeScannerRequiredPermissionsRequestCode, qrcodeScannerRequiredPermissions);
        }
    }

    public void openSpiralETicketQRCodeScanner()
    {
        if (AppPermission.checkPermission(this, spiralETicketQRCodeScannerRequiredPermissions))
        {
            startSpiralETicketQRCodeScanner();
        }
        else {
            AppPermission.requestPermissions(this, spiralETicketQRCodeScannerRequiredPermissionsRequestCode, spiralETicketQRCodeScannerRequiredPermissions);
        }
    }

    public void startReadingQRcode(){

        if (AppPermission.checkPermission(this, readingQRcodeRequiredPermissions))
        {
            startQRCodeScanner();
        }
        else {
            AppPermission.requestPermissions(this, qrcodeScannerRequiredPermissionsRequestCode, readingQRcodeRequiredPermissions);
        }
    }

    public void showGalleryChooser(String inputId, int width, int height)
    {
        this.inputId = inputId;
        this.width   = width;
        this.height  = height;

        showGallery();
    }

    private void startQRCodeScanner()
    {
        Intent intent = new Intent(this, QrCodeActivity.class);
        startActivityForResult(intent, 3);
    }

    private void startSpiralETicketQRCodeScanner()
    {
        Intent intent = new Intent(this, QRCodeScannerActivity.class);
        startActivityForResult(intent, 4);
    }

    public void startBeacon(String data)
    {

        beaconData = data;

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
            startBeaconDetection(data);
        }
        else {
            AppPermission.requestPermissions(this, beaconDetectionRequiredPermissionsRequestCode, beaconDetectionRequiredPermissions);
        }
    }

    private void startBeaconDetection(String data)
    {
        if (null == data)
        {
            return;
        }
        BeaconService.beaconmap=data;
        bluetoothAdapter = bluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 2);
        } else {
           if(isBeaconServiceRunning(BeaconService.class))
           {
               stopService(new Intent(Contents.this, BeaconService.class));
           }
            startService(new Intent(Contents.this, BeaconService.class));
        }
    }

    private boolean isBeaconServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 許可しないにすると無限ループに陥るので辞める。
//        if (null != beaconData)
//        {
//            startBeacon(beaconData);
//        }
        BeaconService.isUnityService=false;
    }

    @Override
    public void onRestart(){
        super.onRestart();
        final ImageView btn_back_button = (ImageView)findViewById(R.id.btn_back_button);
        final ImageView menu_buttom = (ImageView)findViewById(R.id.menu_buttom);
        menu_buttom .setBackgroundColor(Color.TRANSPARENT);
        btn_back_button .setBackgroundColor(Color.TRANSPARENT);
//        if(beaconManager!=null) {
//            if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
//        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 端末の戻るボタンを押した時にwebviewの戻る履歴があれば1つ前のページに戻る
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (myWebView.canGoBack() == true) {
//                if(myWebView.copyBackForwardList().getItemAtIndex(-1).getUrl().indexOf(Constants.FAVORITE_URL) != -1){
//                    extraHeaders.put("user-id", device_id);
                myWebView.goBack();
                Log.d("getUrl", myWebView.getUrl());
//                    myWebView.loadUrl(myWebView.copyBackForwardList().getItemAtIndex(myWebView.copyBackForwardList().getCurrentIndex() -1).getUrl(), extraHeaders);
//                }
//                myWebView.goBack();
                return true;
            }else{
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        isMultAdShow = false;
        if(ad_handler != null&&adRunnable != null) {

            ad_handler.removeCallbacks(adRunnable);
        }
        BeaconService.beaconmap=null;
        unregisterReceiver(beaconContentsReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    active_url = bundle.getString("key.url", "");
                    if(active_url.equals(Constants.HomeUrl())){
                        finish();
                    }

                    if (null != myWebView) {
                        Log.d("device_token",device_id);
                        extraHeaders.put("user-id", device_id);
                        myWebView.loadUrl(active_url,extraHeaders);
                    }
                }
                break;
            case 2:
                if(resultCode==RESULT_OK) {
                    this.recreate();
                }
                break;
            case 3:
                if(resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String code = bundle.getString("data");
                    latitude = bundle.getDouble("lat");
                    longitude = bundle.getDouble("lon");
                    String url = myWebView.getOriginalUrl();
                    String reading_qrcode_url = "/reading_qrcode/";

                    if (url.indexOf(reading_qrcode_url) != -1) {
                        myWebView.loadUrl("javascript:Reading.scanQRCode('"+ code + "')");
                    }else {
                        latitude = bundle.getDouble("lat");
                        longitude = bundle.getDouble("lon");
                        myWebView.loadUrl("javascript:CheckIn.scanQRCode('" + device_id + "','" + code + "','" + latitude + "','" + longitude + "')");
                        Log.d("TAG", "javascript:CheckIn.scanQRCode('" + device_id + "','" + code + "','" + latitude + "','" + longitude + "')");
                    }
                }
                break;
            case 4:
                if (RESULT_OK == resultCode)
                {
                    Bundle bundle = data.getExtras();
                    String qrcode_data = bundle.getString("qrcode_data");
                    String javaScript = String.format("javascript:SpiralETicket.qrcodeCheckIn('%s')", qrcode_data);
                    myWebView.loadUrl(javaScript);
                }
                break;
            case ShowGalleryChooserRequestCode:
                if (resultCode == RESULT_OK)
                {
                    Uri uri;

                    if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT)
                    {
                        if (null == data)
                        {
                            uri = m_uri;
                        }
                        else
                        {
                            String dataString = data.getDataString();
                            uri = (dataString != null) ? Uri.parse(dataString) : m_uri;
                        }
                    }
                    else
                    {
                        uri = (data != null) ? data.getData() : m_uri;
                    }

                    if (null != uri)
                    {
                        encodeImagetoString(uri);
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            // 更新処理
//            if(beaconManager!=null) {
//                if (beaconManager.isBound(Contents.this)) beaconManager.unbind(Contents.this);
//            }
            extraHeaders.put("user-id", device_id);
            myWebView.loadUrl(active_url,extraHeaders);
        }
    };

    /** WebViewClientクラス */
    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            beaconData = null;

            // webviewではpdfファイルが開けないので専用のアプリで開く
            if (url.endsWith(".pdf"))
            {
                Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                pdfIntent.setDataAndType(Uri.parse(url), "application/pdf");
                try {
                    startActivity(pdfIntent);
                }
                catch (ActivityNotFoundException exception)
                {
                    try {
                        pdfIntent.setData(Uri.parse(url));
                        startActivity(pdfIntent);
                    }catch (ActivityNotFoundException e) {
                        showOpenMarketForPDFViewerDialog();
                    }
                }

                return true;
            }

            try {
                final URL urlObject = new URL(url);

                // 「スケジュール登録」リクエスト
                if (Schedule.isRegistCalenderUrl(urlObject))
                {
                    registSchedules(urlObject);
                    return true;
                }

                // 「スケジュール削除」リクエスト
                if (Schedule.isDeleteCalenderUrl(urlObject))
                {
                    deleteSchedules(urlObject);
                    return true;
                }

            } catch (MalformedURLException e) {}

            active_url = url;

            // 写真機能の場合は Fragmentを表示する
            if (url.indexOf(Constants.FACEBOOK_PHOTO_URL) != -1){
                String keyword = "&plan=";
                int index = url.indexOf(keyword);
                String plan = url.substring(index + keyword.length(), url.length());
                // インテントのインスタンス生成
                Intent intent = new Intent(Contents.this, FacebookPhotoActivity.class);
                intent.putExtra("plan", plan);
                // サブ画面の呼び出し
                startActivity(intent);
                return true;
            }

            // 速報機能の場合はブラウザで開くようにする。
            if (url.indexOf("/preview/") != -1) {
                String keyword = "&prompt_report_title=";
                int index = url.indexOf(keyword);
                String title = url.substring(index + keyword.length(), url.length());
                final String myUrlStr = url.substring(0, index);

                URL myUrl;
                Uri uri;
                try {
                    myUrl = new URL(myUrlStr);
                    uri = Uri.parse(myUrl.toURI().toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    view.getContext().startActivity(intent);
                    if (url.indexOf("&direct_to_url") != -1){
                        finish();
                    }
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }

            if((url.indexOf(Constants.APPLI_DOMAIN) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_1) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_2) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_3) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_4) != -1)
                    || (url.indexOf(Constants.EXHIBITER_DOMAIN_5) != -1)
                    || isFromMessage
                    || isInternalOpen) {
               if(isFromMessage) {
                   Contents.this.myWebView.loadUrl(url);
                   return false;
               }
                else
               {
                   extraHeaders.put("user-id", device_id);
                   Contents.this.myWebView.loadUrl(url, Contents.this.extraHeaders);
                   return false;
               }
            }else{
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            String nonQueryUrl = (-1 == url.indexOf("?")) ? url : url.substring(0, url.indexOf("?"));
            if (nonQueryUrl.equals(Constants.HOME_URL) || nonQueryUrl.equals(Constants.HomeUrl())) {
                finish();
            }
        }

        /**
         * @see android.webkit.WebViewClient#onPageFinished(android.webkit.WebView, java.lang.String)
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            System.out.println("onPageFinished");
            final ImageView btn_back_button = (ImageView)findViewById(R.id.btn_back_button);
            btn_back_button.setBackgroundColor(Color.TRANSPARENT);

            ImageView searchImageButton = (ImageView)findViewById(R.id.bar_search_button);
            searchImageButton.setVisibility(View.GONE);
            myWebView.loadUrl("javascript:NavigationSearchButton.run()");

            // ajax通信をキャッチしてレスポンスを受け取れるように準備する
            Contents.this.setupJavascriptHandler();

            super.onPageFinished(view, url);

            // ReadingQRcodeの場合はweb側にvalueをセットするリクエストを送る
            if ((url.indexOf(Constants.READING_QRCODE) != -1)) {
                myWebView.loadUrl("javascript:Reading.setCanOpenQRcodeCameraValue()");
            }
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
                String nonQueryUrl = (-1 == url.indexOf("?")) ? url : url.substring(0, url.indexOf("?"));
                if (nonQueryUrl.equals(Constants.HOME_URL) || nonQueryUrl.equals(Constants.HomeUrl())) {
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
                } else if ((url.indexOf(Constants.BOOTH) != -1) || (url.indexOf(Constants.HALL_URL) != -1)) {
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
                    if(myWebView.getTitle() != null) {
                        if (myWebView.getTitle().length() >= 16) {
                            textView.setText(myWebView.getTitle().substring(0, 15) + "...");
                        } else {
                            textView.setText(myWebView.getTitle());
                        }
                    }
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
                    if(myWebView.getTitle() != null) {
                        if (myWebView.getTitle().length() >= 16) {
                            textView.setText(myWebView.getTitle().substring(0, 15) + "...");
                        } else {
                            textView.setText(myWebView.getTitle());
                        }

                        if (url.indexOf("facebook.com") != -1)
                        {
                            textView.setText("Facebook");
                        }
                        else if (url.indexOf("twitter.com") != -1)
                        {
                            textView.setText("Twitter");
                        }
                    }
                }
                // 0.2秒待機
                if(backurl.equals(myWebView.getUrl())){
                    extraHeaders.put("user-id", device_id);
                    myWebView.loadUrl(myWebView.getUrl(),extraHeaders);
                    backurl = "####";

                }
            }
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


    private void setupFavoritSeminarAlarm()
    {
        this.setupWebChromeClient();
        this.setupJavascriptManager();
    }

    private void setupWebChromeClient()
    {
        this.myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                if (0 != message.indexOf("ajax-handler:"))
                {
                    return super.onJsAlert(view, url, message, result);
                }

                message = message.replace("ajax-handler:", "");
                JavascriptManager.getInstance().onJsAlert(message);

                if (0 == url.indexOf(Constants.SettingUrl()))
                {
                    if (0 == message.indexOf("language:"))
                    {
                        String language = message.replace("language:", "");
                        language = language.equals("ja") ? "en" : "ja";
                        AppLanguage.setLanguageWithStringValue(Contents.this.getApplicationContext(), language);

                        BeaconService.isJP=AppLanguage.isJapanese(getApplicationContext());

                        SPUtils.put(getApplicationContext(), "language", "1");
                        myWebView.reload();
                    }
                    else
                    {
                        myWebView.loadUrl("javascript:alert('ajax-handler:language:' + $('#language').children('option[selected]')[0].value)");
                    }
                }

                result.cancel();
                return true;
            }
        });
    }

    private void setupJavascriptManager()
    {
        JavascriptManager.getInstance().addHandler(new FavoritSeminarJavascriptHandler(this.getApplicationContext()));
    }

    private void setupJavascriptHandler()
    {
        this.myWebView.loadUrl(String.format("javascript: %s;", this.ajaxHandlerJavascript()));
    }

    private String ajaxHandlerJavascript()
    {
        return new AssetsManager(this).loadStringFromFile("ajax_handler.js");
    }

    public void sendBeacon(final String ui,final String ma,final String min){
        myWebView.post(new Runnable() {
            @Override
            public void run() {
                myWebView.loadUrl("javascript:BeaconMap.detectBeacon('"+ui+"','"+ma+"','"+min+"')");
            }
        });

    }
    public void clearBeacon(){
        myWebView.post(new Runnable() {
            @Override
            public void run() {
                myWebView.loadUrl("javascript:BeaconMap.clearAllBeacon()");
            }
        });

    }

    @Override
    public Boolean isRequirePermission(int requestCode, String permission) {
        AppPermission.log(String.format("isRequirePermission: %s", permission));

        Boolean isRequirePermission = false;

        switch (requestCode)
        {
            case imageUploadRequiredPermissionsRequestCode:
                switch (permission)
                {
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                    case Manifest.permission.CAMERA:
                        isRequirePermission = true;
                        break;
                }
                break;

            case qrcodeScannerRequiredPermissionsRequestCode:
                switch (permission)
                {
                    case Manifest.permission.CAMERA:
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                    case Manifest.permission.ACCESS_COARSE_LOCATION:
                        isRequirePermission = true;
                        break;
                }
                break;

            case beaconDetectionRequiredPermissionsRequestCode:
                switch (permission)
                {
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                    case Manifest.permission.ACCESS_COARSE_LOCATION:
                        isRequirePermission = true;
                        break;
                }
                break;

            case imageDownloadRequiredPermissionsRequestCode:
                switch (permission)
                {
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
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

        if(AppLanguage.isJapanese(this)) {
            switch (requestCode) {
                case imageUploadRequiredPermissionsRequestCode:
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_dialog_title))
                            .setMessage(getString(R.string.permission_dialog_message_camera_and_storage))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AppPermission.openSettings(Contents.this);
                                }
                            })
                            .create()
                            .show();
                    break;

                case qrcodeScannerRequiredPermissionsRequestCode:
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_dialog_title))
                            .setMessage(getString(R.string.permission_dialog_message_camera_and_location))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AppPermission.openSettings(Contents.this);
                                }
                            })
                            .create()
                            .show();
                    break;

                case beaconDetectionRequiredPermissionsRequestCode:
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_dialog_title))
                            .setMessage(getString(R.string.permission_dialog_message_location))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AppPermission.openSettings(Contents.this);
                                }
                            })
                            .create()
                            .show();
                    break;
            }
        }
        else
        {
            switch (requestCode) {
                case imageUploadRequiredPermissionsRequestCode:
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_dialog_title_en))
                            .setMessage(getString(R.string.permission_dialog_message_camera_and_storage_en))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AppPermission.openSettings(Contents.this);
                                }
                            })
                            .create()
                            .show();
                    break;

                case qrcodeScannerRequiredPermissionsRequestCode:
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_dialog_title_en))
                            .setMessage(getString(R.string.permission_dialog_message_camera_and_location_en))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AppPermission.openSettings(Contents.this);
                                }
                            })
                            .create()
                            .show();
                    break;

                case beaconDetectionRequiredPermissionsRequestCode:
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_dialog_title_en))
                            .setMessage(getString(R.string.permission_dialog_message_location_en))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AppPermission.openSettings(Contents.this);
                                }
                            })
                            .create()
                            .show();
                    break;
            }
        }
    }

    @Override
    public void allRequiredPermissions(int requestCode, String[] permissions) {
        switch (requestCode)
        {
            case imageUploadRequiredPermissionsRequestCode:
                openGallery();
                break;

            case qrcodeScannerRequiredPermissionsRequestCode:
                startQRCodeScanner();
                break;

            case beaconDetectionRequiredPermissionsRequestCode:
                startBeaconDetection(beaconData);
                break;

            case imageDownloadRequiredPermissionsRequestCode:
                downloadImage(imageDownloadUrl);
                break;
        }
    }

    private void encodeImagetoString(final Uri uri)
    {
        new AsyncTask<Void, Void , String>() {
            @Override
            protected String doInBackground(Void... params){
                String encodedString = "";

                try
                {
                    Bitmap bitmap = getBitmapFromUri(uri);
                    if (null != bitmap)
                    {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                        byte[] byte_arr = stream.toByteArray();
                        encodedString = "data:image/png;base64," + Base64.encodeToString(byte_arr, Base64.NO_WRAP);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                return encodedString;
            }

            @Override
            protected void onPostExecute(String imageDataURL)
            {
                myWebView.loadUrl(String.format("javascript:ImageDataReceiver.init('%s');", inputId));

                int splitLength = 200;
                while (true)
                {
                    int length = splitLength < imageDataURL.length() ? splitLength : imageDataURL.length();
                    myWebView.loadUrl(String.format("javascript:ImageDataReceiver.appendData('%s');", imageDataURL.substring(0, length)));

                    if (length == imageDataURL.length())
                    {
                        break;
                    }

                    imageDataURL = imageDataURL.substring(length);
                }

                myWebView.loadUrl("javascript:ImageDataReceiver.complete();");
            }
        }.execute(null, null, null);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        Matrix matrix = new Matrix();
        matrix = setMatrixRotation(matrix, uri);

        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        options.inSampleSize = calculateInSampleSize(options);
        options.inJustDecodeBounds = false;
        Bitmap tmpImage = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        parcelFileDescriptor.close();

        Bitmap image = Bitmap.createBitmap(tmpImage, 0, 0, tmpImage.getWidth(), tmpImage.getHeight(), matrix, true);

        return image;
    }

    private Matrix setMatrixRotation(Matrix matrix, Uri uri) throws IOException {
        String filePath = getPathFromUri(uri);
        if (null == filePath)
        {
            return matrix;
        }

        ExifInterface exifInterface = new ExifInterface(filePath);

        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        switch (orientation) {
            case ExifInterface.ORIENTATION_UNDEFINED:
                break;
            case ExifInterface.ORIENTATION_NORMAL:
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(-90f);
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90f);
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(-90f);
                break;
        }

        return matrix;
    }

    private String getPathFromUri(Uri uri)
    {
        if (null == uri)
        {
            return null;
        }

        if (uri.getScheme().equals("file"))
        {
            return uri.getPath();
        }

        ContentResolver contentResolver = this.getContentResolver();
        Cursor cursor = contentResolver.query(uri, new String[] {MediaStore.Images.Media.DATA}, null, null, null);
        cursor.moveToFirst();
        String path = cursor.getString(0);
        cursor.close();

        return path;
    }

    private int calculateInSampleSize(BitmapFactory.Options options)
    {
        int rawHeight = options.outHeight;
        int rawWidth  = options.outWidth;

        int inSampleSize = 1;
        if (rawHeight > height || rawWidth > width)
        {
            if (rawWidth > rawHeight)
            {
                inSampleSize = Math.round((float)rawHeight / (float)height);
            }
            else {
                inSampleSize = Math.round((float)rawHeight / (float)width);
            }
        }

        return inSampleSize;
    }

    private void showOpenMarketForPDFViewerDialog()
    {
        String dialogTitle     = getResources().getString(R.string.open_market_for_pdf_viewer_dialog_title);
        String dialogMessage   = getResources().getString(R.string.open_market_for_pdf_viewer_dialog_message);
        String dialogButtonYes = getResources().getString(R.string.open_market_for_pdf_viewer_dialog_button_yes);
        String dialogButtonNo  = getResources().getString(R.string.open_market_for_pdf_viewer_dialog_button_no);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(dialogTitle);
        builder.setMessage(dialogMessage);
        builder.setPositiveButton(dialogButtonYes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String dialogMarketId  = getResources().getString(R.string.open_market_for_pdf_viewer_id);

                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse(dialogMarketId));
                        startActivity(marketIntent);
                    }
                });
        builder.setNegativeButton(dialogButtonNo, null);
        builder.create().show();
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

    private void registSchedules(URL url)
    {
        schedule.setUrl(url);
        PermissionRequestManager.getInstance().requestPermissions(Contents.this, new String[]{Manifest.permission.WRITE_CALENDAR}, new PermissionRequestManager.OnRequestPermissionsResultListener() {
            @Override
            public void onRequestPermissionsResult(PermissionRequestManager.PermissionsResult result) {
                if (result.isGrantedAll())
                {
                    schedule.registSchedules();
                }
                else
                {
                    schedule.cancel();
                }
            }
        });
    }

    private void deleteSchedules(URL url)
    {
        schedule.setUrl(url);
        PermissionRequestManager.getInstance().requestPermissions(Contents.this, new String[]{Manifest.permission.WRITE_CALENDAR}, new PermissionRequestManager.OnRequestPermissionsResultListener() {
            @Override
            public void onRequestPermissionsResult(PermissionRequestManager.PermissionsResult result) {
                if (result.isGrantedAll())
                {
                    schedule.deleteSchedules();
                }
                else
                {
                    schedule.cancel();
                }
            }
        });
    }

    private void confirmDownloadImageDialog(final String url)
    {
        new AlertDialog.Builder(this)
                .setMessage("画像を保存しますか？")
                .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkImageDownloadPermissions(url);
                    }
                })
                .setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .show();
    }

    private void checkImageDownloadPermissions(String url)
    {
        if (AppPermission.checkPermission(this, imageDownloadRequiredPermissions))
        {
            downloadImage(url);
        }
        else {
            imageDownloadUrl = url;
            AppPermission.requestPermissions(this, imageDownloadRequiredPermissionsRequestCode, imageDownloadRequiredPermissions);
        }
    }

    private void downloadImage(final String url)
    {
        File image = new File(url);

        DownloadManager mdDownloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, image.getName());
        mdDownloadManager.enqueue(request);
    }
}
