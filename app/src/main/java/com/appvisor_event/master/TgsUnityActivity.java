package com.appvisor_event.master;

//import android.app.AlertDialog;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.content.res.AssetManager;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.View;
//
//import com.appvisor_event.master.modules.DLAndUnzipService;
//import com.unity3d.player.UnityPlayer;
//import com.unity3d.player.UnityPlayerActivity;
//
//import net.lingala.zip4j.core.ZipFile;
//import net.lingala.zip4j.exception.ZipException;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Locale;
//
//public class TgsUnityActivity extends UnityPlayerActivity {
//
//    private InfosGetter arInfoGetter;
//    DownloadAndUnzipBroadcastReceiver dlReceiver;
//    IntentFilter intentFilter;
//    private  boolean isJa = false;
//    private  int curversion=0;
//    private String arPathdir = null;
//    private  boolean isDataReady=false;
//
//    private  String arzippassword=null;
//    private  int downloadversion=0;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        //言語設定　１英語　０日本語
//        isJa=isJpLanguage();
//        if(isJa) {
//            UnityPlayer.UnitySendMessage("InitCamera", "InitLocalLanguage", "0");
//        }
//        else
//        {
//            UnityPlayer.UnitySendMessage("InitCamera", "InitLocalLanguage", "1");
//        }
//
//        arPathdir=getFilesDir().toString()+"/data/";
//
//        try {
//            curversion=getARversion();
//            Log.i("test url :",Constants.AR_API+curversion);
//            arInfoGetter = new InfosGetter(Constants.AR_API+curversion);
//            arInfoGetter.start();
//            arInfoGetter.join();
//
//            Log.i("test url:","111111111111111111");
//
//            // responseがあればログ出力する。
//            if (arInfoGetter.mResponse != null && arInfoGetter.mResponse != "") {
//                try {
//                    Log.i("test url:","2222222222222");
//                    JSONObject arinfojson = new JSONObject(arInfoGetter.mResponse);
//                    Log.i("test url:","nnnnnnnnnn");
//                    if (arinfojson.getInt("status") == 200) {
//                        Log.i("test url:","4444444444");
//                        Log.i("test url:",arinfojson.toString());
//                        //ARDownloadなど
//                        //ダウンロード準備
//                        dlReceiver=new DownloadAndUnzipBroadcastReceiver();
//                        intentFilter=new IntentFilter();
//                        intentFilter.addAction("DOWNLOAD_ARFILE");
//                        registerReceiver(dlReceiver,intentFilter);
//
//                        //ダウンロード開始
//                        Intent intent =new Intent(this, DLAndUnzipService.class);
//                        String downloadUrl=arinfojson.getString("url");
//                        downloadversion=arinfojson.getInt("version");
//                        arzippassword=arinfojson.getString("password");
//                        intent.putExtra("downloadUrl",downloadUrl);
//                        Log.i("test url:",downloadUrl);
//                        Log.i("test password:",arzippassword);
//                        startService(intent);
//
//                    }
//                    else if(arinfojson.getInt("status") == 301) {
//                        //AR初期
//                        if(curversion==0)
//                        {
//                            Log.i("test url:","2");
//                            if(checkDataFull())
//                            {
//                                //ARデータ初期
//                                UnityPlayer.UnitySendMessage("InitCamera", "ARVuforiaDataPath", getXmlPath(arPathdir));
//                                UnityPlayer.UnitySendMessage("InitCamera", "InitARObjectInfo", readCsvFile(arPathdir));
//                            }
//                            else
//                            {
//                                AssetManager assetManager = getResources().getAssets();
//                                zipFiletoLocalfile(assetManager.open("data.zip"));
//                                File arzip=new File(getFilesDir().toString()+"/AR.zip");
//                                unZipARFile(arzip,"12345678");
//                                //ARデータ初期
//                                UnityPlayer.UnitySendMessage("InitCamera", "ARVuforiaDataPath", getXmlPath(arPathdir));
//                                UnityPlayer.UnitySendMessage("InitCamera", "InitARObjectInfo", readCsvFile(arPathdir));
//
//                            }
//                        }
//                        else
//                        {
//                            Log.i("test url:","4");
//                            //ARデータ初期
//                            UnityPlayer.UnitySendMessage("InitCamera", "ARVuforiaDataPath", getXmlPath(arPathdir));
//                            UnityPlayer.UnitySendMessage("InitCamera", "InitARObjectInfo", readCsvFile(arPathdir));
//                        }
//                    }
//                    else if(arinfojson.getInt("status") == 300) {
//                        //期間外のため表示できません
//                        if(isJa) {
//
//                            showCloseAlter("期間外のため表示できません");
//                        }
//                        else
//                        {
//                            showCloseAlter("can not be displayed because of the outside of the period!");
//                        }
//                    }
//                    else
//                    {
//                        //エラーが発生しました
//                        if(isJa) {
//                            showCloseAlter("エラーが発生しました");
//                        }
//                        else
//                        {
//                            showCloseAlter("error occurred!");
//                        }
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    //エラーが発生しました
//                    if(isJa) {
//                        showCloseAlter("エラーが発生しました");
//                    }
//                    else
//                    {
//                        showCloseAlter("error occurred!");
//                    }
//                }
//            }
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//            //エラーが発生しました
//            if(isJa) {
//                showCloseAlter("エラーが発生しました");
//            }
//            else
//            {
//                showCloseAlter("error occurred!");
//            }
//        }
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(String name, Context context, AttributeSet attrs) {
//        return super.onCreateView(name, context, attrs);
//    }
//
//    public void SavetoCamera()
//    {
//        //画像を保存する
//    }
//
//
//    public  void zipFiletoLocalfile(InputStream ins) {
//        try {
//
//            File file=new File(getFilesDir().toString()+"/AR.zip");
//            if(file.exists())
//            {
//                file.delete();
//            }
//            OutputStream os = new FileOutputStream(file);
//            int bytesRead = 0;
//            byte[] buffer = new byte[8192];
//            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
//                os.write(buffer, 0, bytesRead);
//            }
//            os.close();
//            ins.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public void ARUnityClose()
//    {
//        //AR画面からホーム画面に戻ります
//        UnityPlayer.currentActivity.runOnUiThread(new Runnable(){
//            public void run()
//            {
//                TgsUnityActivity.this.mUnityPlayer.quit();
//                UnityPlayer.currentActivity.finish();
//            }
//        });
//    }
//
//    public void SharewithSNS (String modenames)
//    {
//        //シェア
//    }
//
//    private void showCloseAlter(String message)
//    {
//        AlertDialog alertDialog = new AlertDialog.Builder(this).setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                ARUnityClose();
//            }
//        }).create();
//        alertDialog.show();
//    }
//
//
//    //システムに言語及び国は日本に設定する場合、日本語と判断する
//    private  boolean isJpLanguage() {
//        Locale locale = Locale.getDefault();
//        if(locale.equals(Locale.JAPAN))
//        {
//            return true;
//        }
//        else
//        {
//            return false;
//        }
//
//    }
//
//    //バジョン取得及びデータ初期()
//    private int getARversion()
//    {
//        SharedPreferences sharedPreferences=getSharedPreferences("ardata",Context.MODE_PRIVATE);
//        int version = sharedPreferences.getInt("androidversion",-1 );
//        if(version==-1)
//        {
//            setARversion(0);
//            return 0;
//        }
//        else
//        {
//            if(checkDataFull())
//            {
//                return version;
//            }
//            else
//            {
//                cleanPath(arPathdir);
//                return 0;
//            }
//        }
//
//    }
//
//    private  boolean checkDataFull()
//    {
//
//        boolean reFull = true;
//        File filepath = new File(arPathdir);
//        if (filepath.exists()) {
//            if (getXmlPath(arPathdir).equals("")) {
//                reFull = false;
//            }
//        } else {
//            reFull = false;
//        }
//
//        return reFull;
//    }
//
//
//    private void cleanPath(String path)
//    {
//        File filepath=new File(path);
//        if(filepath.exists())
//        {
//            deleteDir(filepath);
//        }
//    }
//
//
//
//    private void deleteDir(File path)
//    {
//        File[] contents = path.listFiles();
//        if (contents != null) {
//            for (File f : contents) {
//                deleteDir(f);
//            }
//        }
//        path.delete();
//    }
//
//
//    private void setARversion(int androidversion)
//    {
//        SharedPreferences sharedPreferences=getSharedPreferences("ardata",Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt("androidversion", androidversion);
//        editor.apply();
//    }
//
//
//    private  boolean isFirstStart()
//    {
//        SharedPreferences sharedPreferences=getSharedPreferences("ardata",Context.MODE_PRIVATE);
//        boolean firststart = sharedPreferences.getBoolean("firststart",true );
//        if(firststart)
//        {
//            return firststart;
//        }
//        else
//        {
//            return firststart;
//        }
//    }
//
//    private void setFirstStart(boolean isstart)
//    {
//        SharedPreferences sharedPreferences=getSharedPreferences("ardata",Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean("firststart", isstart);
//        editor.apply();
//    }
//
//    public void unZipARFile(File zipfile,String password) throws ZipException
//    {
//
//        try {
//            if (zipfile.exists()) {
//                //解凍
//                ZipFile zipFile = new ZipFile(zipfile);
//                if (zipFile.isEncrypted()) {
//                    zipFile.setPassword(password);
//                }
//                zipFile.extractAll(getFilesDir().toString());
//            } else {
//                throw new ZipException();
//            }
//        }catch (ZipException e)
//        {
//            throw new ZipException();
//        }
//    }
//
//
//    private String readCsvFile(String filePath)
//    {
//        String strCsv="";
//        File file =new File(filePath+"/ar.csv");
//        BufferedReader reader =null;
//        try {
//            reader =new BufferedReader(new FileReader(file));
//            String strline = null;
//            while ((strline =reader.readLine())!=null)
//            {
//                if(strCsv.equals(""))
//                {
//                    strCsv += strline;
//                }
//                else {
//                    strCsv += ("|"+strline);
//                }
//            }
//            reader.close();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            strCsv="";
//        }
//        finally {
//            if (reader != null) {
//                try{
//                    reader.close();;
//                } catch (IOException e)
//                {
//                    strCsv="";
//                }
//            }
//        }
//
//        Log.i("test url:","5:::"+strCsv);
//        return strCsv;
//    }
//
//    private String getXmlPath(String filePath)
//    {
//        File dir =new File(filePath);
//        String repath="";
//        if(dir.isDirectory()&&dir.exists())
//        {
//            String [] paths=dir.list();
//            if(paths == null)
//            {
//                Log.d("failed","files is not created!");
//            }
//            else
//            {
//                for(int i=0;i<paths.length;i++)
//                {
//                    if(paths[i].indexOf(".xml")!=-1)
//                    {
//                        repath = filePath+paths[i];
//                        break;
//                    }
//                }
//            }
//        }
//        return repath;
//    }
//
//    class  DownloadAndUnzipBroadcastReceiver extends BroadcastReceiver
//    {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Bundle bundle = intent.getExtras();
//            int finishPercent=bundle.getInt("finishPercent");
//            if(finishPercent>=100) {
//                //初期設定
//                //TODO
//                try {
//                    File arzip=new File(getFilesDir().toString()+"/AR.zip");
//                    unZipARFile(arzip,arzippassword);
//
//                    if(checkDataFull()) {
//                        setARversion(downloadversion);
//                        //ARデータ初期
//                        UnityPlayer.UnitySendMessage("InitCamera", "ARVuforiaDataPath", getXmlPath(arPathdir));
//                        UnityPlayer.UnitySendMessage("InitCamera", "InitARObjectInfo", readCsvFile(arPathdir));
//                    }
//                } catch (ZipException e) {
//                    e.printStackTrace();
//                }
//            }
//            else {
//                UnityPlayer.UnitySendMessage("InitCamera", "InitPercent", finishPercent + "%");
//            }
//        }
//    }
//}
public class TgsUnityActivity
{}

