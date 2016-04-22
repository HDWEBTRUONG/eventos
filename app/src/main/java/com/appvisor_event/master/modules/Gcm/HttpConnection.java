package com.appvisor_event.master.modules.Gcm;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bsfuji on 2014/11/17.
 */
public class HttpConnection extends AsyncTask<String, String, String>
{
    public static interface HttpConnectionListener
    {
        public void onFinished(String responseString);
        public void onFailed(Exception exception);
    }

    private DefaultHttpClient      httpClient             = null;
    private List<NameValuePair>    params                 = null;
    private HttpConnectionListener httpConnectionListener = null;

    public HttpConnection()
    {
        this.httpClient = new DefaultHttpClient();
        this.params     = new ArrayList<NameValuePair>();
    }

    public HttpConnection(HttpConnectionListener httpConnectionListener) {
        this();
        this.setHttpConnectionListener(httpConnectionListener);
    }

    public void setHttpConnectionListener(HttpConnectionListener httpConnectionListener) {
        this.httpConnectionListener = httpConnectionListener;
    }

    public void addParam(String key, String value)
    {
        this.params.add(new BasicNameValuePair(key, value));
    }

    public void asyncPost(String url)
    {
        this.execute(url);
    }

    @Override
    protected String doInBackground(String... url)
    {
        HttpPost request = new HttpPost(url[0]);
        try {
            request.setEntity(new UrlEncodedFormEntity(this.params));

            String result = this.httpClient.execute(request, new ResponseHandler<String>()
            {
                @Override
                public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException
                {
                    String responseString = "NG";
                    switch (response.getStatusLine().getStatusCode())
                    {
                        case HttpStatus.SC_OK:
                            responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
                            break;
                    }
                    return responseString;
                }
            });

            HttpConnection.this.callbackFinished(result);

            Log.d("HTTPConnection", "result = " + result);

            return result;
        }
        catch (ClientProtocolException exception)
        {
            HttpConnection.this.callbackFailed(exception);

            Log.d("HTTPConnection", "HTTPConnection = " + exception);
        }
        catch (IOException exception)
        {

            HttpConnection.this.callbackFailed(exception);

            Log.d("HTTPConnection", "IOException = " + exception);
        }

        return "NG";
    }

    private void callbackFinished(String responseString)
    {
        if (null != this.httpConnectionListener)
        {
            this.httpConnectionListener.onFinished(responseString);
        }
    }

    private void callbackFailed(Exception exception)
    {
        if (null == this.httpConnectionListener)
        {
            return;
        }

        this.httpConnectionListener.onFailed(exception);
    }
}
