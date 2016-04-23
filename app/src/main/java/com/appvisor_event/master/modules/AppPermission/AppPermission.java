package com.appvisor_event.master.modules.AppPermission;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

/**
 * Created by bsfuji on 2016/04/21.
 */
public class AppPermission
{
    public interface Interface {
        Boolean isRequirePermission(int requestCode, String permission);
        void showErrorDialog(int requestCode);
        void allRequiredPermissions(int requestCode, String[] permissions);
    }

    public static Boolean checkPermission(Activity activity, String[] permissions)
    {
        if (isLessThanAndroidM())
        {
            log("isLessThanAndroidM");
            return true;
        }

        if (hasPermissons(activity, permissions))
        {
            log("hasPermisson");
            return true;
        }

        return false;
    }

    public static void requestPermissions(Activity activity, int requestCode, String[] permissions)
    {
        log("requestPermissions");
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public static void openSettings(Activity activity)
    {
        log("openSettings");

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    private static Boolean isLessThanAndroidM()
    {
        return (Build.VERSION_CODES.M > Build.VERSION.SDK_INT);
    }

    private static Boolean hasPermissons(Activity activity, String[] permissions)
    {
        for (String permission : permissions)
        {
            if (PackageManager.PERMISSION_GRANTED != PermissionChecker.checkSelfPermission(activity, permission))
            {
                return false;
            }
        }

        return true;
    }

    public static void log(String message)
    {
        Log.d(AppPermission.class.getSimpleName(), message);
    }

    public static void onRequestPermissionsResult(AppPermission.Interface appPermissionInterface, int requestCode, String[] permissions, int[] grantResults)
    {
        if (!checkGrantPermissions(appPermissionInterface, requestCode, permissions, grantResults))
        {
            appPermissionInterface.showErrorDialog(requestCode);
            return;
        }

        appPermissionInterface.allRequiredPermissions(requestCode, permissions);
    }

    private static Boolean checkGrantPermissions(AppPermission.Interface appPermissionInterface, int requestCode, String[] permissions, int[] grantResults)
    {
        AppPermission.log("confirmGrantPermissions");

        int i = 0;
        for (int grantResult : grantResults)
        {
            if (PackageManager.PERMISSION_GRANTED != grantResult)
            {
                if (appPermissionInterface.isRequirePermission(requestCode, permissions[i]))
                {
                    return false;
                }
            }

            i++;
        }

        return true;
    }
}
