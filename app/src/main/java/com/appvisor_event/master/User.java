package com.appvisor_event.master;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.UUID;

/**
 * Created by Ookuma on 2016/12/13.
 */
public class User {
    private static String uuid = null;
    private static String version = null;
    private static final String UUID_KEY = "UUID_KEY";

    public static String getUUID(Context context) {
        if (uuid != null) {// 既にapp内からinvokeされている場合
            return uuid;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(UUID_KEY, Context.MODE_PRIVATE);
        uuid = sharedPreferences.getString(UUID_KEY, null);
        if (uuid == null) {// 何も設定されていない場合
            uuid = UUID.randomUUID().toString();// randomな文字列を生成
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(UUID_KEY, uuid);
            editor.commit();// 保存
        }
        return uuid;
    }

    public static String getAppID(Context context) {
        return context.getPackageName();
    }

    public static String getAppVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        String pn = context.getPackageName();
        try{
            PackageInfo pi = pm.getPackageInfo(pn, 0);
            version = pi.versionName;
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return version;
    }

}
