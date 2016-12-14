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

public class DeviceTokenSender extends Thread {
    // フィールド変数の設定
    String device_id;  // 送信用のdevice_idをここに格納
    String device_token;  // 送信用のdevice_tokenをここに格納
    String app_id;  // 送信用のapp_idをここに格納
    String version;  // 送信用のversionをここに格納
    String mResponse;  // 送信結果を受け取る変数
    String mUrl;  // 送信先のURL

    // コンストラクタの設定。
    DeviceTokenSender ( String url ) {
        mUrl = url;
    }

    // 送信処理をrunメソッドに記述する
    @Override
    public void run () {

        // 送信用のデータが格納されていなければreturn
        if ( device_id == null & device_token == null || app_id == null || version == null) {
            return;
        }

        // 送信用のデータが格納されている場合
        try {

            URI uri = new URI ( mUrl );

            // postメソッドで送る。
            HttpPost request = new HttpPost ( uri );

            DefaultHttpClient client = new DefaultHttpClient ();

            // 送るデータをnameとvalueの組にし、ArrayListに格納する。
            ArrayList<NameValuePair> DeviceValuePairs = new ArrayList < NameValuePair > () ;
            DeviceValuePairs.add ( new BasicNameValuePair( "device_id", device_id ) );
            DeviceValuePairs.add ( new BasicNameValuePair( "device_token", device_token ) );
            DeviceValuePairs.add ( new BasicNameValuePair( "app_id", app_id ) );
            DeviceValuePairs.add ( new BasicNameValuePair( "version", version ) );

            // ArrayListをセットする。
            request.setEntity ( new UrlEncodedFormEntity( DeviceValuePairs ) );

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
