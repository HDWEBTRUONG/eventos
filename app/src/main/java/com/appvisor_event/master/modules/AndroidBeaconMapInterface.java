package com.appvisor_event.master.modules;

import android.content.Context;
import android.os.Build;
import android.webkit.JavascriptInterface;

import com.appvisor_event.master.Contents;

/**
 * Created by kawa on 16/08/27.
 */
public class AndroidBeaconMapInterface {
    Context context;

    public AndroidBeaconMapInterface(Context context){
        this.context = context;
    }

    @JavascriptInterface
    public void beacons(String beacons){
        if (Build.VERSION.SDK_INT > 17) {
            ((Contents) context).startBeacon(beacons);
        }
    }

}
