package com.appvisor_event.master;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MyHttpSender extends Thread {

    // フィールド変数の設定
    String mLanguage;  // 送信用の文字列をここに格納
    String mDeviseID;  // 送信用device_id
    String mAppID;  // 送信用app_id
    String mVersion;  // 送信用version
    String mResponse;  // 送信結果を受け取る変数
    String mUrl;  // 送信先のURL

    // コンストラクタの設定。
    MyHttpSender ( String url ) {
        mUrl = url;
    }

    // 送信処理をrunメソッドに記述する
    @Override
    public void run () {

        // 送信用のデータが格納されていなければreturn
        if (mDeviseID == null || mAppID == null || mVersion == null || mLanguage == null) {
            return;
        }

        // 送信用のデータが格納されている場合
        try {

            URI uri = new URI ( mUrl );

            // postメソッドで送る。
            HttpPost request = new HttpPost ( uri );

            DefaultHttpClient client = new DefaultHttpClient ();

            // 送るデータをnameとvalueの組にし、ArrayListに格納する。
            ArrayList<NameValuePair> nameValuePairs = new ArrayList < NameValuePair > () ;
            nameValuePairs.add(new BasicNameValuePair("language", mLanguage));
            nameValuePairs.add ( new BasicNameValuePair( "device_id", mDeviseID ) );
            nameValuePairs.add ( new BasicNameValuePair( "app_id", mAppID ) );
            nameValuePairs.add ( new BasicNameValuePair( "version", mVersion ) );

            // ArrayListをセットする。
            request.setEntity ( new UrlEncodedFormEntity( nameValuePairs ) );

            // requestを発行、responseを受け取る。
            HttpResponse response = client.execute ( request );

            // ステイタスコードを受け取る。
            int status = response.getStatusLine () .getStatusCode ();

            Log.i ( "HTTP", "status code = " + status );

            // 正常終了したか判断。
            if ( status == HttpStatus.SC_OK ) {

                // responseを格納
                mResponse = EntityUtils.toString(response.getEntity(), "UTF-8");

            } else {

                Log.i ( "HTTP", "status code = " + status );

            }

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