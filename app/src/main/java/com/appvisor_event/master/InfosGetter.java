package com.appvisor_event.master;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by kawa on 16/07/26.
 */
public class InfosGetter extends Thread {

    public String mResponse;  // 送信結果を受け取る変数
    String mUrl;  // 送信先のURL

    // コンストラクタの設定。
    public InfosGetter(String url ) {
        mUrl = url;
    }

    // 送信処理をrunメソッドに記述する
    @Override
    public void run () {

        // 送信用のデータが格納されている場合
        try {

            URI uri = new URI ( mUrl );

            // postメソッドで送る。
            HttpGet request = new HttpGet( uri );

            DefaultHttpClient client = new DefaultHttpClient ();

            // requestを発行、responseを受け取る。
            HttpResponse response = client.execute ( request );

            // ステイタスコードを受け取る。
            int status = response.getStatusLine () .getStatusCode ();

            //ステータスが判断していないので、懸念点として残ります
            // responseを格納
            mResponse = EntityUtils.toString(response.getEntity(), "UTF-8");

            client.getConnectionManager () .shutdown ();

        } catch ( URISyntaxException e ) {

            e.printStackTrace ();
            Log.i ( "HTTP", "URISyntaxException");

        } catch ( UnsupportedEncodingException e ) {

            e.printStackTrace ();
            Log.i ( "HTTP", "UnsupportedEncodingException");

        } catch ( ClientProtocolException e ) {

            e.printStackTrace ();
            Log.i ( "HTTP", "ClientProtocolException");

        } catch ( IOException e ) {

            e.printStackTrace ();
            Log.i ( "HTTP", "IOException");

        }

        super.run ();
    }
}
