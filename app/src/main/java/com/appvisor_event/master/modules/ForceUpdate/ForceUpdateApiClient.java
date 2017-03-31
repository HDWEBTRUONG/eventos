package com.appvisor_event.master.modules.ForceUpdate;

import android.os.Bundle;
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
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by bsfuji on 2017/02/02.
 */

public class ForceUpdateApiClient extends Thread
{
    public class Response
    {
        public String code     = null;
        public String title    = null;
        public String header   = null;
        public String image    = null;
        public String footer   = null;
        public String storeURL = null;

        public Response(String data)
        {
            parse(data);
        }

        public Bundle getData()
        {
            Bundle data = new Bundle();

            data.putString("code",     this.code);
            data.putString("title",    this.title);
            data.putString("header",   this.header);
            data.putString("image",    this.image);
            data.putString("footer",   this.footer);
            data.putString("storeURL", this.storeURL);

            return data;
        }

        private void parse(String dataString)
        {
            try {
                JSONObject jsonObject = new JSONObject(dataString);

                if (jsonObject.has("response"))
                {
                    JSONObject response = jsonObject.getJSONObject("response");

                    if (response.has("code"))
                    {
                        this.code = response.getString("code");
                    }

                    if (response.has("data"))
                    {
                        JSONObject data = response.getJSONObject("data");

                        if (data.has("title"))
                        {
                            this.title = data.getString("title");
                        }

                        if (data.has("header"))
                        {
                            this.header = data.getString("header");
                        }

                        if (data.has("image"))
                        {
                            this.image = data.getString("image");
                        }

                        if (data.has("footer"))
                        {
                            this.footer = data.getString("footer");
                        }

                        if (data.has("storeURL"))
                        {
                            this.storeURL = data.getString("storeURL");
                        }
                    }
                }
            }
            catch (Exception exception) {}
        }
    }

    private String url      = null;
    private String version  = null;
    private String language = null;

    private Response response = null;

    public ForceUpdateApiClient(String url, String version, String language)
    {
        this.url      = url;
        this.version  = version;
        this.language = language;
    }

    public boolean hasResponse()
    {
        return (null != this.response);
    }

    public Response getResponse()
    {
        return this.response;
    }

    @Override
    public void run ()
    {
        try {
            URI uri = new URI (url);

            DefaultHttpClient httpClient = new DefaultHttpClient ();

            ArrayList<NameValuePair> paramaters = new ArrayList <NameValuePair> () ;
            paramaters.add(new BasicNameValuePair("version", version));
            paramaters.add(new BasicNameValuePair("language", language));

            HttpPost request = new HttpPost(uri);
            request.setEntity(new UrlEncodedFormEntity(paramaters));

            HttpResponse response = httpClient.execute(request);

            int status = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == status)
            {
                String data = EntityUtils.toString(response.getEntity(), "UTF-8");
                this.response = new Response(data);
            }
            else
            {
                Log.i ( "HTTP", "status code = " + status );
            }

            httpClient.getConnectionManager().shutdown();

        } catch (URISyntaxException exception) {

        } catch (UnsupportedEncodingException exception) {

        } catch (ClientProtocolException exception) {

        } catch (IOException exception) {

        }

        super.run();
    }
}
