package com.appvisor_event.master;

import android.app.Activity;

import com.appvisor_event.master.modules.PermissionRequestManager.PermissionRequestManager;

/**
 * Created by bsfuji on 2017/03/28.
 */

public class AppActivity extends Activity
{
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionRequestManager.getInstance().handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
