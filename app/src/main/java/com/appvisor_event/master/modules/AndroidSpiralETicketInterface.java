package com.appvisor_event.master.modules;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.appvisor_event.master.Contents;

/**
 * Created by bsfuji on 2016/08/02.
 */
public class AndroidSpiralETicketInterface {
    Context context;

    public AndroidSpiralETicketInterface(Context context){
        this.context = context;
    }

    @JavascriptInterface
    public void openQRCodeScanner(){
        ((Contents)context).openSpiralETicketQRCodeScanner();
    }
}
