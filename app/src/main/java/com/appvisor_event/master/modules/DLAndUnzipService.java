package com.appvisor_event.master.modules;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpException;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


/**
 * Created by kawa on 16/08/26.
 */
public class DLAndUnzipService extends IntentService {

    static final String servicename= "DLAndUnzipService";

   public DLAndUnzipService() {
       super(servicename);
   }


    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            //ダウンロードするURLを取得する
            Bundle bundle =intent.getExtras();
            if(bundle == null)
            {
                return;
            }

            //ダウンロードURLを初期化
            String str_downloadUrl=bundle.getString("downloadUrl");
            String password=bundle.getString("password");
            URL downloadUrl=new URL(str_downloadUrl);

            URLConnection connection=downloadUrl.openConnection();
            HttpURLConnection conn=(HttpURLConnection)connection;
            conn.setAllowUserInteraction(false);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode=conn.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                throw new HttpException();
            }
            int contentLength = conn.getContentLength();

            InputStream inputStream=conn.getInputStream();
            FileOutputStream outputStream=openFileOutput("AR.zip",MODE_PRIVATE);
            DataInputStream dataInputStream=new DataInputStream(inputStream);
            DataOutputStream dataOutputStream=new DataOutputStream(new BufferedOutputStream(outputStream));

            //データを読み込みする
            byte[] databytes=new byte[4096];
            int readByte =0,totalByte=0;
            while (-1 != (readByte = dataInputStream.read(databytes)))
            {
                dataOutputStream.write(databytes,0,readByte);
                totalByte +=readByte;
                sendFileDownload(totalByte,contentLength);
            }
            dataInputStream.close();
            dataOutputStream.close();
            sendFileDownload(1,1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.i(servicename, "IOException");
        }
        catch (HttpException e)
        {
            e.printStackTrace();
            Log.i(servicename, "HttpException");
        }
    }


    protected void sendFileDownload(int totallength,int contentlength)
    {
        Intent broadcastIntent =new Intent();
        int finishPercent = totallength<0?0:(int) (((float)totallength)/((float) contentlength)*100);
        broadcastIntent.putExtra("finishPercent",finishPercent);
        broadcastIntent.setAction("DOWNLOAD_ARFILE");
        getBaseContext().sendBroadcast(broadcastIntent);
    }

}
