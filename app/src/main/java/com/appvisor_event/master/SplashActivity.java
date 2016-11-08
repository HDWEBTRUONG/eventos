package com.appvisor_event.master;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

import com.appvisor_event.master.model.PhotoResponse;
import com.appvisor_event.master.modules.BeaconService;
import com.appvisor_event.master.util.DataCleanManager;
import com.appvisor_event.master.util.SPUtils;
import com.google.gson.Gson;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.http.HttpException;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

public class SplashActivity extends Activity {
    private final static String TAG = SplashActivity.class.getSimpleName();
    private InfosGetter infosGetter;
    private Handler hdl = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // タイトルを非表示にします。
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // splash.xmlをViewに指定します。
        setContentView(R.layout.splash);

        BeaconService.activities.clear();
        getInfoByApi();

    }

    class splashHandler implements Runnable {
        public void run() {
            // スプラッシュ完了後に実行するActivityを指定します。
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
            // SplashActivityを終了させます。
            SplashActivity.this.finish();
        }
    }

    public void getInfoByApi() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    int oldVersion = (int) SPUtils.get(getApplicationContext(), "version", 0);
                    infosGetter = new InfosGetter(String.format(Locale.JAPANESE, Constants.PHOTO_FRAME, oldVersion));
                    infosGetter.start();
                    infosGetter.join();
                    if (infosGetter.mResponse != null && infosGetter.mResponse != "") {
                        Log.d("Response", infosGetter.mResponse + "");
                        Gson gson = new Gson();
                        PhotoResponse photoResponse = gson.fromJson(infosGetter.mResponse, PhotoResponse.class);
                        if (photoResponse.getStatus() == 200) {
                            if (oldVersion != photoResponse.getVersion()) {
                                SPUtils.put(getApplicationContext(), "version", photoResponse.getVersion());
                                SPUtils.put(getApplicationContext(), "url", photoResponse.getUrl());
                                SPUtils.put(getApplicationContext(), "frame", gson.toJson(photoResponse.getFrame()));

                                dlAndUpZip();
                            }
                        }
                    }

                    Log.d("version", SPUtils.get(getApplicationContext(), "version", 0) + "");
                    Log.d("url", SPUtils.get(getApplicationContext(), "url", "") + "");
                    Log.d("frame", SPUtils.get(getApplicationContext(), "frame", "") + "");
                } catch (Exception e) {
                    Log.e(TAG, "get photo api error", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // 500ms遅延させてsplashHandlerを実行します。
                hdl.postDelayed(new splashHandler(), 1000);
            }
        }.execute();
    }

    public void dlAndUpZip() {
        try {
            //ダウンロードするURLを取得する

            //ダウンロードURLを初期化
            String str_downloadUrl = (String) SPUtils.get(getApplicationContext(), "url", "");
            if (str_downloadUrl == null || str_downloadUrl.length() == 0) {
                return;
            }
            URL downloadUrl = new URL(str_downloadUrl);

            URLConnection connection = downloadUrl.openConnection();
            HttpURLConnection conn = (HttpURLConnection) connection;
            conn.setAllowUserInteraction(false);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new HttpException();
            }
            int contentLength = conn.getContentLength();

            InputStream inputStream = conn.getInputStream();
            FileOutputStream outputStream = openFileOutput("images.zip", MODE_PRIVATE);
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(outputStream));

            //データを読み込みする
            byte[] databytes = new byte[4096];
            int readByte = 0, totalByte = 0;
            while (-1 != (readByte = dataInputStream.read(databytes))) {
                dataOutputStream.write(databytes, 0, readByte);
                totalByte += readByte;
            }
            dataInputStream.close();
            dataOutputStream.close();
            try {
                DataCleanManager.cleanCustomCache(getFilesDir().toString() + "/images/");
                File zipfile = new File(getFilesDir().toString() + "/images.zip");
                if (zipfile.exists()) {
                    //解凍
                    ZipFile zipFile = new ZipFile(zipfile);
                    zipFile.extractAll(getFilesDir().toString()+"/images");
                }
            } catch (ZipException e) {
                Log.d(TAG, "Unpack the zip error", e);
            }
        } catch (IOException e) {
            Log.i(TAG, "IOException", e);
        } catch (HttpException e) {
            Log.i(TAG, "HttpException", e);
        }
    }

}