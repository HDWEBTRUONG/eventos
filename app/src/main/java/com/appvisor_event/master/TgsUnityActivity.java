package com.appvisor_event.master;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.appvisor_event.master.modules.AppLanguage.AppLanguage;
import com.appvisor_event.master.modules.DLAndUnzipService;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TgsUnityActivity extends UnityPlayerActivity {

    private InfosGetter arInfoGetter;
    DownloadAndUnzipBroadcastReceiver dlReceiver;
    IntentFilter intentFilter;
    private  boolean isJa = false;
    private  int curversion=0;
    private String arPathdir = null;
    private  boolean isDataReady=false;

    private  String arzippassword=null;
    private  int downloadversion=0;
    private String sharefilepath=null;
    private SimpleDateFormat simpleDateFormat;
    private BeaconMessageUnityReceiver beaconMessageUnityReceiver;
    private IntentFilter beaconMessageUnityintentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);
        //言語設定　１英語　０日本語
        isJa=isJpLanguage();
        if(isJa) {
            UnityPlayer.UnitySendMessage("InitCamera", "InitLocalLanguage", "0");
        }
        else
        {
            UnityPlayer.UnitySendMessage("InitCamera", "InitLocalLanguage", "1");
        }

        if(isFirstStart()) {
            new com.appvisor_event.master.ARTutorialDialog(this, new com.appvisor_event.master.ARTutorialDialog.OnARTutorialDialogListener() {
                @Override
                public void onCancel() {
                    //save
                    setFirstStart(false);
                }
            }).show();
        }

        arPathdir=getFilesDir().toString()+"/data/";
        try {
            curversion=getARversion();
            arInfoGetter = new InfosGetter(Constants.AR_API+curversion);
            arInfoGetter.start();
            arInfoGetter.join();

            // responseがあればログ出力する。
            if (arInfoGetter.mResponse != null && arInfoGetter.mResponse != "") {
                try {
                    Date nowtime=new Date();
                    String strnowtime = simpleDateFormat.format(nowtime);
                    JSONObject arinfojson = new JSONObject(arInfoGetter.mResponse);
                    if (arinfojson.getInt("status") == 200) {
                        //ARDownloadなど
                        //ダウンロード準備
                        setARInfo(arInfoGetter.mResponse);
                        dlReceiver=new DownloadAndUnzipBroadcastReceiver();
                        intentFilter=new IntentFilter();
                        intentFilter.addAction("DOWNLOAD_ARFILE");
                        registerReceiver(dlReceiver,intentFilter);

                        JSONObject period=arinfojson.getJSONObject("period");
                        //時間を判断
                        String fromtime = period.getString("from");
                        String endtime = period.getString("to");

                        if(strnowtime.compareTo(fromtime)>=0&&strnowtime.compareTo(endtime)<=0) {
                            //ダウンロード開始
                            Intent intent = new Intent(this, DLAndUnzipService.class);
                            String downloadUrl = arinfojson.getString("url");
                            downloadversion = arinfojson.getInt("version");
                            arzippassword = arinfojson.getString("password");
                            intent.putExtra("downloadUrl", downloadUrl);
                            startService(intent);
                        }
                        else
                        {
                            //期間外のため表示できません
                            if(isJa) {

                                showCloseAlter("期間外のため表示できません");
                            }
                            else
                            {
                                showCloseAlter("The expiration date has passed");
                            }
                        }
                    }
                    else if(arinfojson.getInt("status") == 301) {
                        //AR初期
                        if(curversion==0)
                        {
                            if(checkDataFull())
                            {
                                //ARデータ初期
                                UnityPlayer.UnitySendMessage("InitCamera", "ARVuforiaDataPath", getXmlPath(arPathdir));
                                UnityPlayer.UnitySendMessage("InitCamera", "InitARObjectInfo", readCsvFile(arPathdir));
                            }
                            else
                            {
                                AssetManager assetManager = getResources().getAssets();
                                zipFiletoLocalfile(assetManager.open("data.zip"));
                                File arzip=new File(getFilesDir().toString()+"/AR.zip");
                                unZipARFile(arzip,"12345678");
                                //ARデータ初期
                                UnityPlayer.UnitySendMessage("InitCamera", "ARVuforiaDataPath", getXmlPath(arPathdir));
                                UnityPlayer.UnitySendMessage("InitCamera", "InitARObjectInfo", readCsvFile(arPathdir));

                            }
                        }
                        else
                        {
                            JSONObject re_arinfojson = new JSONObject(getARInfo());
                            JSONObject period=re_arinfojson.getJSONObject("period");
                            String fromtime = period.getString("from");
                            String endtime = period.getString("to");
                            if(strnowtime.compareTo(fromtime)>=0&&strnowtime.compareTo(endtime)<=0) {
                                //ARデータ初期
                                UnityPlayer.UnitySendMessage("InitCamera", "ARVuforiaDataPath", getXmlPath(arPathdir));
                                UnityPlayer.UnitySendMessage("InitCamera", "InitARObjectInfo", readCsvFile(arPathdir));
                            }
                            else
                            {
                                //期間外のため表示できません
                                if(isJa) {

                                    showCloseAlter("期間外のため表示できません");
                                }
                                else
                                {
                                    showCloseAlter("The expiration date has passed");
                                }
                            }
                        }
                    }
                    else if(arinfojson.getInt("status") == 300) {
                        //期間外のため表示できません
                        if(isJa) {

                            showCloseAlter("期間外のため表示できません");
                        }
                        else
                        {
                            showCloseAlter("The expiration date has passed");
                        }
                    }
                    else
                    {
                        //エラーが発生しました
                        if(isJa) {
                            showCloseAlter("エラーが発生しました");
                        }
                        else
                        {
                            showCloseAlter("An error occurred.");
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    //エラーが発生しました
                    if(isJa) {
                        showCloseAlter("エラーが発生しました");
                    }
                    else
                    {
                        showCloseAlter("error occurred!");
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            //エラーが発生しました
            if(isJa) {
                showCloseAlter("エラーが発生しました");
            }
            else
            {
                showCloseAlter("error occurred!");
            }
        }

        beaconMessageUnityReceiver=new BeaconMessageUnityReceiver();
        beaconMessageUnityintentFilter=new IntentFilter();
        beaconMessageUnityintentFilter.addAction("Beacon_message_unity");
        registerReceiver(beaconMessageUnityReceiver,beaconMessageUnityintentFilter);
    }

    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    public void SavetoCamera(String filepath)
    {
        sharefilepath="";
        File sharefile = new File(filepath);
        if(sharefile.exists())
        {
            File TGSPhotoPath=new File(Environment.getExternalStorageDirectory()+"/TGS");
            if(!TGSPhotoPath.exists()||!TGSPhotoPath.isDirectory())
            {
                TGSPhotoPath.mkdir();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File expFile = new File(TGSPhotoPath.getPath() + File.separator + "IMG_" + timeStamp + ".png");
            FileChannel inChannel = null;
            FileChannel outChannel = null;

            try {
                inChannel = new FileInputStream(sharefile).getChannel();
                outChannel = new FileOutputStream(expFile).getChannel();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inChannel != null)
                    try {
                        inChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if (outChannel != null)
                    try {
                        outChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }

            //画像を保存する
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(expFile);
            intent.setData(uri);
            this.sendBroadcast(intent);
            sharefilepath=TGSPhotoPath.getPath() + File.separator + "IMG_" + timeStamp + ".png";
        }
    }


    public  void zipFiletoLocalfile(InputStream ins) {
        try {

            File file=new File(getFilesDir().toString()+"/AR.zip");
            if(file.exists())
            {
                file.delete();
            }
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void ARUnityClose()
    {
        //AR画面からホーム画面に戻ります
        UnityPlayer.currentActivity.runOnUiThread(new Runnable(){
            public void run()
            {
                TgsUnityActivity.this.mUnityPlayer.quit();
                UnityPlayer.currentActivity.finish();
            }
        });
        unregisterReceiver(beaconMessageUnityReceiver);
    }

    public void SharewithSNS (String modenames)
    {
        String sharesns[]=modenames.split(",");
        if(sharesns !=null && sharesns.length>0) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
            String snstext="";
            for(int i=0;i<sharesns.length;i++) {
                String[] sharestext = readShareFile(sharesns[i]);
                if(sharestext!=null) {
                    //シェア
                    if (isJa) {
                        snstext += (sharestext[0] + " #" + sharestext[1] + " ");
                    } else {
                        snstext += (sharestext[3] + " #" + sharestext[4] + " ");
                    }
                }
            }
            if(snstext!="") {
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, snstext);
            }
            String imgPath = sharefilepath;
            File file = new File(imgPath);
            shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(file));
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, ""));
        }
    }

    private void showCloseAlter(String message)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ARUnityClose();
            }
        }).create();
        alertDialog.show();
    }


    //システムに言語及び国は日本に設定する場合、日本語と判断する
    private  boolean isJpLanguage() {
        return AppLanguage.isJapanese(this);
    }

    //バジョン取得及びデータ初期()
    private int getARversion()
    {
        SharedPreferences sharedPreferences=getSharedPreferences("ardata",Context.MODE_PRIVATE);
        int version = sharedPreferences.getInt("androidversion",-1 );
        if(version==-1)
        {
            setARversion(0);
            return 0;
        }
        else
        {
            if(checkDataFull())
            {
                return version;
            }
            else
            {
                cleanPath(arPathdir);
                return 0;
            }
        }

    }

    private  boolean checkDataFull()
    {

        boolean reFull = true;
        File filepath = new File(arPathdir);
        if (filepath.exists()) {
            if (getXmlPath(arPathdir).equals("")) {
                reFull = false;
            }
        } else {
            reFull = false;
        }

        return reFull;
    }


    private void cleanPath(String path)
    {
        File filepath=new File(path);
        if(filepath.exists())
        {
            deleteDir(filepath);
        }
    }



    private void deleteDir(File path)
    {
        File[] contents = path.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        path.delete();
    }


    private void setARversion(int androidversion)
    {
        SharedPreferences sharedPreferences=getSharedPreferences("ardata",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("androidversion", androidversion);
        editor.apply();
    }


    private  boolean isFirstStart()
    {
        SharedPreferences sharedPreferences=getSharedPreferences("ardata",Context.MODE_PRIVATE);
        boolean firststart = sharedPreferences.getBoolean("firststart",true );
        if(firststart)
        {
            return firststart;
        }
        else
        {
            return firststart;
        }
    }

    private void setFirstStart(boolean isstart)
    {
        SharedPreferences sharedPreferences=getSharedPreferences("ardata",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("firststart", isstart);
        editor.apply();
    }

    public void unZipARFile(File zipfile,String password) throws ZipException
    {

        try {
            if (zipfile.exists()) {
                //解凍
                ZipFile zipFile = new ZipFile(zipfile);
                if (zipFile.isEncrypted()) {
                    zipFile.setPassword(password);
                }
                zipFile.extractAll(getFilesDir().toString());
            } else {
                throw new ZipException();
            }
        }catch (ZipException e)
        {
            throw new ZipException();
        }
    }


    private String readCsvFile(String filePath)
    {
        String strCsv="";
        File file =new File(filePath+"/ar.csv");
        BufferedReader reader =null;
        try {
            reader =new BufferedReader(new FileReader(file));
            String strline = null;
            while ((strline =reader.readLine())!=null)
            {
                if(strCsv.equals(""))
                {
                    strCsv += strline;
                }
                else {
                    strCsv += ("|"+strline);
                }
            }
            reader.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            strCsv="";
        }
        finally {
            if (reader != null) {
                try{
                    reader.close();;
                } catch (IOException e)
                {
                    strCsv="";
                }
            }
        }
        return strCsv;
    }

    private String[] readShareFile(String filename)
    {

        String[] arrayShare= null;
        File file =new File(arPathdir+filename+".txt");
        if(file.exists()&&file.isFile()) {
            BufferedReader reader = null;
            arrayShare= new String[6];
            try {
                reader = new BufferedReader(new FileReader(file));
                String strline = null;
                int index=0;
                while ((strline = reader.readLine()) != null) {
                    if(index>5)
                    {
                        break;
                    }
                    arrayShare[index]=strline;
                    index++;
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                arrayShare =null;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        arrayShare =null;
                    }
                }
            }
        }
        return arrayShare;

    }


    private String getXmlPath(String filePath)
    {
        File dir =new File(filePath);
        String repath="";
        if(dir.isDirectory()&&dir.exists())
        {
            String [] paths=dir.list();
            if(paths == null)
            {
                Log.d("failed","files is not created!");
            }
            else
            {
                for(int i=0;i<paths.length;i++)
                {
                    if(paths[i].indexOf(".xml")!=-1)
                    {
                        repath = filePath+paths[i];
                        break;
                    }
                }
            }
        }
        return repath;
    }

    class  DownloadAndUnzipBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            int finishPercent=bundle.getInt("finishPercent");
            if(finishPercent>=100) {
                //初期設定
                //TODO
                try {
                    File arzip=new File(getFilesDir().toString()+"/AR.zip");
                    unZipARFile(arzip,arzippassword);

                    if(checkDataFull()) {
                        setARversion(downloadversion);
                        //ARデータ初期
                        UnityPlayer.UnitySendMessage("InitCamera", "ARVuforiaDataPath", getXmlPath(arPathdir));
                        UnityPlayer.UnitySendMessage("InitCamera", "InitARObjectInfo", readCsvFile(arPathdir));
                    }
                } catch (ZipException e) {
                    e.printStackTrace();
                }
            }
            else {
                UnityPlayer.UnitySendMessage("InitCamera", "InitPercent", finishPercent + "%");
            }
        }
    }

    private String getARInfo()
    {
        SharedPreferences sharedPreferences=getSharedPreferences("ardata", Context.MODE_PRIVATE);
        String messages = sharedPreferences.getString("arinfo",null);
        return  messages;
    }

    private void setARInfo(String messages)
    {
        SharedPreferences sharedPreferences=getSharedPreferences("ardata",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("arinfo", messages);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    class  BeaconMessageUnityReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            showBeaconMeaasge(bundle.getString("title"), bundle.getString("body"), bundle.getString("link"), bundle.getInt("isInternal"));
        }
    }

    public void showBeaconMeaasge(String title, String message, final String link, final int isWebview)
    {
        if(link != null) {
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
                                Intent intent = new Intent(TgsUnityActivity.this, Contents.class);
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
}