package com.appvisor_event.master.modules;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.appvisor_event.master.Contents;
import com.appvisor_event.master.R;

/**
 * Created by gomez on 2016/04/17.
 */
public class WebAppInterface {
    Context context;
    private ViewGroup linearLayout;
    private View imageView;

    public WebAppInterface(Context context){
        this.context = context;
    }

    @JavascriptInterface
    public void beacons(String data){
        ((Contents)context).startBeacon();
    }

    @JavascriptInterface
    public void openQRCodeScanner(){
        ((Contents)context).startQR();
    }

    @JavascriptInterface
    public void addNavigationBarButton(String fileName, String url){
        linearLayout = (ViewGroup) linearLayout.findViewById(R.id.title_bar);
        View view = (View)linearLayout.findViewById(R.id.menu_buttom);
        view.setVisibility(View.GONE);
        View searchLayout = LayoutInflater.from((Contents)context).inflate(R.layout.search_layout,null);
        linearLayout.addView(searchLayout);
    }

    @JavascriptInterface
    public void clipBoard(String txt){
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("number",txt);
        clipboard.setPrimaryClip(clip);
    }
}
