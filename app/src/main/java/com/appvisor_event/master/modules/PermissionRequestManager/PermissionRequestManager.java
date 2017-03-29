package com.appvisor_event.master.modules.PermissionRequestManager;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bsfuji on 2017/03/28.
 */

public class PermissionRequestManager
{
    private static PermissionRequestManager instance = new PermissionRequestManager();

    private PermissionRequestManager() {}

    public static PermissionRequestManager getInstance()
    {
        return instance;
    }

    public void handleRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode < listeners.size())
        {
            OnRequestPermissionsResultListener listener = listeners.get(requestCode);
            listener.onRequestPermissionsResult(new PermissionsResult(permissions, grantResults));
            listeners.remove(requestCode);
        }
    }

    public class PermissionsResult
    {
        private Map<String, Integer> results = null;

        public PermissionsResult(@NonNull String[] permissions, @NonNull int[] grantResults)
        {
            if (permissions.length != grantResults.length)
            {
                throw new RuntimeException();
            }

            results = new HashMap<>();
            for (int i = 0; i < permissions.length; i++)
            {
                results.put(permissions[i], grantResults[i]);
            }
        }

        public boolean isGrantedAll()
        {
            for (String permission : results.keySet())
            {
                if (!isGrantedPermission(permission))
                {
                    return false;
                }
            }

            return true;
        }

        public boolean isGrantedPermission(String permission)
        {
            if (!results.containsKey(permission))
            {
                return false;
            }

            return (PackageManager.PERMISSION_GRANTED == results.get(permission));
        }

        public boolean isGrantedPermissions(String[] permissions)
        {
            for (String permission : permissions)
            {
                if (!isGrantedPermission(permission))
                {
                    return false;
                }
            }

            return true;
        }
    }

    public interface OnRequestPermissionsResultListener
    {
        void onRequestPermissionsResult(PermissionsResult result);
    }

    private ArrayList<OnRequestPermissionsResultListener> listeners = new ArrayList<OnRequestPermissionsResultListener>();

    public void requestPermissions(Activity activity, String[]permissions, PermissionRequestManager.OnRequestPermissionsResultListener listener)
    {
        int requestCode = listeners.size();

        listeners.add(listener);

        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }
}
