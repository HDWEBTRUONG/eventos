package com.appvisor_event.master.modules.Photoframe;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.appvisor_event.master.Constants;
import com.appvisor_event.master.InfosGetter;
import com.appvisor_event.master.model.PhotoResponse;
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

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bsfuji on 2016/11/29.
 *
 * フォトフレームモジュール
 *
 * 現在のバージョン情報を取得し、保持しているバージョンよりも新しければデータを取得する。
 * 最後に取得した時にサーバーから返却されたデータのバージョンを保持する。
 * パスコード利用時はスラッグ切り替え時に保持しているデータを破棄する。
 */
public class Photoframe
{
    private static String TAG = Photoframe.class.getSimpleName();

    public interface OnCheckListener
    {
        void onFinishCheck(int version, String url, String frame);
    }

    public interface OnDownloadListener
    {
        void onFinishDownload();
    }

    public static boolean isNewVersion(Context context, int version)
    {
        int cachedVersion = (int) SPUtils.get(context, "version", 0);
        return (Constants.isPasscodeEnable() || cachedVersion != version);
    }

    public static void clearCache(Context context)
    {
        DataCleanManager.cleanCustomCache(context.getFilesDir().toString() + "/images/");

        Log.d(TAG, "clearCache");
    }

    public static void updateVersion(Context context, int version, String url, String frame)
    {
        if (Constants.isPasscodeEnable())
        {
            version = 0;
        }

        SPUtils.put(context, "version", version);
        SPUtils.put(context, "url",     url);
        SPUtils.put(context, "frame",   frame);

        Log.d(TAG, "updateCache");
    }

    public static void check(final Context context, final OnCheckListener listener)
    {
        Log.d(TAG, "check");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    int oldVersion = (int) SPUtils.get(context, "version", 0);
                    InfosGetter infosGetter = new InfosGetter(String.format(Locale.JAPANESE, Photoframe.CheckURL(), oldVersion));
                    infosGetter.start();
                    infosGetter.join();
                    if (infosGetter.mResponse != null && infosGetter.mResponse != "") {
                        Log.d("Response", infosGetter.mResponse + "");
                        Gson gson = new Gson();
                        PhotoResponse photoResponse = gson.fromJson(infosGetter.mResponse, PhotoResponse.class);
                        if (photoResponse.getStatus() == 200) {
                            if (listener instanceof OnCheckListener)
                            {
                                listener.onFinishCheck(
                                        photoResponse.getVersion(),
                                        photoResponse.getUrl(),
                                        gson.toJson(photoResponse.getFrame())
                                );
                            }
                        }
                    }

                    Log.d("version", SPUtils.get(context, "version", 0) + "");
                    Log.d("url", SPUtils.get(context, "url", "") + "");
                    Log.d("frame", SPUtils.get(context, "frame", "") + "");
                } catch (Exception e) {
                    Log.e(TAG, "get photo api error", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {}
        }.execute();
    }

    public static void donwloadData(String url, Context context, OnDownloadListener listener)
    {
        Log.d(TAG, "donwloadData");

        try {
            if (url == null || url.length() == 0) {
                return;
            }
            URL downloadUrl = new URL(url);

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
            FileOutputStream outputStream = context.openFileOutput("images.zip", MODE_PRIVATE);
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
                File zipfile = new File(context.getFilesDir().toString() + "/images.zip");
                if (zipfile.exists()) {
                    Photoframe.clearCache(context);

                    //解凍
                    ZipFile zipFile = new ZipFile(zipfile);
                    zipFile.extractAll(context.getFilesDir().toString()+"/images/");

                    if (listener instanceof OnDownloadListener)
                    {
                        listener.onFinishDownload();
                    }
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

    private static String CheckURL()
    {
        return Constants.HomeUrl() + "/api/photoframes/check?os=android&version=%d";
    }
}
