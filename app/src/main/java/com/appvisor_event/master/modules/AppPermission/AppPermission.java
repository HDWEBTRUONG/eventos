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
    public static final int PERMISSION_REQUEST_CODE = 1;

    public interface Interface {
        Boolean isRequirePermission(String permission);
        void showErrorDialog();
        void allRequiredPermissions();
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

    public static void requestPermissions(Activity activity, String[] permissions)
    {
        log("requestPermissions");
        ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
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
        switch (requestCode)
        {
            case AppPermission.PERMISSION_REQUEST_CODE:
                if (!checkGrantPermissions(appPermissionInterface, permissions, grantResults))
                {
                    appPermissionInterface.showErrorDialog();
                    return;
                }

                appPermissionInterface.allRequiredPermissions();
                break;
        }
    }

    private static Boolean checkGrantPermissions(AppPermission.Interface appPermissionInterface, String[] permissions, int[] grantResults)
    {
        AppPermission.log("confirmGrantPermissions");

        int i = 0;
        for (int grantResult : grantResults)
        {
            if (PackageManager.PERMISSION_GRANTED != grantResult)
            {
                if (appPermissionInterface.isRequirePermission(permissions[i]))
                {
                    return false;
                }
            }

            i++;
        }

        return true;
    }

    private static void logPermissions (String[] permissions)
    {
        for (String permission : permissions)
        {
            log(String.format("permission: %s", permission));
        }
    }

    private static void logGrantResults (int[] grantResults)
    {
        for (int grantResult : grantResults)
        {
            log(String.format("grantResult: %d, %b", grantResult, (PackageManager.PERMISSION_GRANTED == grantResult)));
        }
    }
}
