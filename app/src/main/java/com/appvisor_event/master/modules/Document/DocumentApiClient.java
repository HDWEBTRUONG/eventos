package com.appvisor_event.master.modules.Document;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by bsfuji on 2017/02/02.
 */

public class DocumentApiClient extends Thread
{
    public class Response
    {
        public String           code  = null;
        public List<JSONObject> items = new ArrayList<>();

        public Response(String data)
        {
            parse(data);
        }

        public List<JSONObject> getItems()
        {
            return items;
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
                        JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++)
                        {
                            JSONObject item = data.getJSONObject(i);
                            Iterator<String> keys = item.keys();
                            while (keys.hasNext())
                            {
                                String key = keys.next();
                                JSONArray childItems = item.getJSONArray(key);
                                for (int j = 0; j < childItems.length(); j++)
                                {
                                    JSONObject childItem = childItems.getJSONObject(j);
                                    JSONObject document  = childItem.getJSONObject("EventDocument");
                                    items.add(document);
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception exception) {}
        }
    }

    private String url      = null;
    private String language = null;

    private Response response = null;

    public DocumentApiClient(String url, String language)
    {
        this.url      = url;
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

            ArrayList<NameValuePair> parameters = new ArrayList <NameValuePair> () ;
            parameters.add(new BasicNameValuePair("language", language));

            HttpGet request = new HttpGet(uri + "?" + URLEncodedUtils.format(parameters, "UTF-8"));

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
