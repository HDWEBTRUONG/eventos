package com.appvisor_event.master.modules.Advertisement;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.appvisor_event.master.Constants;
import com.appvisor_event.master.InfosGetter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by bsfuji on 2016/11/29.
 */

public class Advertisement
{
    private static String TAG = Advertisement.class.getSimpleName();

    public static boolean   isLoaded     = false;
    public static JSONArray list         = null;
    public static int       interval     = -1;
    public static int       currentIndex = 0;
    public static float     ratio        = 0.0f;

    private static int numberOfLoadedImage = 0;

    public static void load(Context context)
    {
        Log.d(TAG, "load");

        try {
            DisplayImageOptions ad_defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true).build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).defaultDisplayImageOptions(ad_defaultOptions).build();
            ImageLoader.getInstance().init(config);

            // 引数にサーバーのURLを入れる。
            InfosGetter getter = new InfosGetter(Constants.AdvertisementUrl());
            getter.start();
            getter.join();

            // responseがあればログ出力する。
            if (getter.mResponse != null && getter.mResponse != "") {
                try {
                    JSONObject adsjson = new JSONObject(getter.mResponse);
                    if (adsjson.getInt("changetime") > 0) {
                        ImageLoader imageLoader = ImageLoader.getInstance();
                        list = adsjson.getJSONArray("ads");
                        if (list != null && list.length() > 0) {
                            interval = adsjson.getInt("changetime");
                            if (interval <= 0) {
                                interval = 5;
                            }
                            for (int i = 0; i < list.length(); i++) {
                                JSONObject adJson = list.getJSONObject(i);
                                String ad_image = adJson.getString("imageurl");
                                imageLoader.loadImage(ad_image, new SimpleImageLoadingListener() {
                                    @Override
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                        int ad_height = loadedImage.getHeight();
                                        int ad_width = loadedImage.getWidth();
                                        float compare_ratio = (float) ad_height / (float) ad_width;
                                        if (ratio < compare_ratio) {
                                            ratio = compare_ratio;
                                        }
                                        numberOfLoadedImage++;
                                        if (numberOfLoadedImage == list.length()) {
                                            isLoaded = true;
                                        }
                                    }
                                });
                            }
                        } else {
                            isLoaded = true;
                            interval = -1;
                            list = null;
                        }
                    } else {
                        isLoaded = true;
                        list = null;
                        interval = -1;
                    }

                } catch (JSONException e) {
                    isLoaded = true;
                    list = null;
                    interval = -1;
                    e.printStackTrace();
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();

        }
    }

    public static void clear()
    {
        Log.d(TAG, "clear");

        isLoaded            = false;
        list                = null;
        interval            = -1;
        currentIndex        = 0;
        ratio               = 0.0f;
        numberOfLoadedImage = 0;
    }
}
