package com.appvisor_event.master.modules.Gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by bsfuji on 2014/11/17.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("", "onReceive");
        ComponentName component = new ComponentName(context.getPackageName(), com.appvisor_event.master.modules.Gcm.GcmIntentService.class.getName());

        startWakefulService(context, (intent.setComponent(component)));
        setResultCode(Activity.RESULT_OK);
    }
}
