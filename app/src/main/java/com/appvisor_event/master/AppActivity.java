package com.appvisor_event.master;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.appvisor_event.master.modules.ForceUpdate.ForceUpdate;
import com.appvisor_event.master.modules.ForceUpdate.ForceUpdateApiClient;
import com.appvisor_event.master.modules.PermissionRequestManager.PermissionRequestManager;

public class AppActivity extends Activity
{
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionRequestManager.getInstance().handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();

        checkVersion();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    protected boolean isCachePolicy()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        if(cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected())
        {
            return false;
        }else
        {
            return true;
        }
    }

    protected void checkVersion()
    {
        ForceUpdate.checkVersion(getApplicationContext(), new ForceUpdate.CheckVersionListener() {
            @Override
            public void OnSuccess(ForceUpdateApiClient.Response response) {
                if (ForceUpdate.isNotSatisfiedVersion(response))
                {
                    ForceUpdate.showAlertViewWithData(getFragmentManager(), response.getData());
                }
                else
                {
                    ForceUpdate.dismissAlertView();
                }
            }

            @Override
            public void OnError(Exception exception) {

            }
        });
    }
}
