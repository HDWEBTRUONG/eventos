package com.appvisor_event.master;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import com.appvisor_event.master.modules.AppLanguage.AppLanguage;
import com.appvisor_event.master.modules.BeaconService;

public class BaseActivity extends AppActivity {
    public int activityIndex=0;
    BeaconMessageReceiver beaconMessageReceiver;
    IntentFilter beaconMessageintentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(BeaconService.getTopActivity()==null)
        {
            activityIndex=0;
        }
        else
        {
            activityIndex=BeaconService.getTopActivity().activityIndex+1;
        }
        beaconMessageReceiver=new BeaconMessageReceiver();
        beaconMessageintentFilter=new IntentFilter();
        beaconMessageintentFilter.addAction("Beacon_message");
        registerReceiver(beaconMessageReceiver,beaconMessageintentFilter);
        BeaconService.addActivity(this);
    }

    public void showBeaconMeaasge(String title, String message, final String link, final int isWebview)
    {

        if(link != null&&!("".equals(link))&&link!=""&&link.length()!=0) {
            if(isWebview==1) {
                new com.appvisor_event.master.CustomDialog.Builder(this)
                        .setTitle(title)
                        .setContent(message)
                        .setButtonContent(AppLanguage.isJapanese(this)?"リンク先へ移動する":"Move to the link", link, com.appvisor_event.master.CustomDialog.LOAD_URL_VIA_BROWSER)
                        .setOnCustomDialogClickListener(new com.appvisor_event.master.CustomDialog.OnCustomDialogClickListener() {
                            @Override
                            public void onCancelClick() {

                            }

                            @Override
                            public void onButtonClick() {
                                    Intent intent = new Intent(BaseActivity.this, Contents.class);
                                    // URLを表示
                                    intent.putExtra("key.url", link);
                                    intent.putExtra("isMessagefrom", true);
                                    // サブ画面の呼び出し
                                    startActivity(intent);
                            }
                        })
                        .build().show();
            }
            else
            {
                new com.appvisor_event.master.CustomDialog.Builder(this)
                        .setTitle(title)
                        .setContent(message)
                        .setButtonContent(AppLanguage.isJapanese(this)?"リンク先へ移動する":"MOVE TO LINK", link, com.appvisor_event.master.CustomDialog.LOAD_URL_VIA_WEBVIEW)
                        .setOnCustomDialogClickListener(new com.appvisor_event.master.CustomDialog.OnCustomDialogClickListener() {
                            @Override
                            public void onCancelClick() {

                            }

                            @Override
                            public void onButtonClick() {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                            }
                        })
                        .build().show();
            }
        }
        else
        {
            new com.appvisor_event.master.CustomDialog.Builder(this)
                    .setTitle(title)
                    .setContent(message)
                    .setOnCustomDialogClickListener(new com.appvisor_event.master.CustomDialog.OnCustomDialogClickListener() {
                        @Override
                        public void onCancelClick() {

                        }

                        @Override
                        public void onButtonClick() {

                        }
                    })
                    .build().show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BeaconService.removeTopActivety(this);
        unregisterReceiver(beaconMessageReceiver);
    }

    class  BeaconMessageReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(BeaconService.getTopActivity().activityIndex==activityIndex) {
                showBeaconMeaasge(bundle.getString("title"), bundle.getString("body"), bundle.getString("link"), bundle.getInt("isInternal"));
            }
        }
    }
}
