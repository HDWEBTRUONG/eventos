package com.appvisor_event.master.modules;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AssetsManager
{
    private final static String JAVA_EOL = System.getProperty("line.separator");

    private Context context;
    
    public AssetsManager(Context context)
    {
        this.context = context;
    }
    
    public String getStringFromFile(String filename)
    {
        InputStream    inputStream    = null;
        BufferedReader bufferedReader = null;
        
        StringBuilder stringBuilder = new StringBuilder();
        try {
            try {
                inputStream = this.context.getAssets().open(filename);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
       
                String string;
                while((string = bufferedReader.readLine()) != null) {
                    stringBuilder.append(string + "\n");
                }
            }
            finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        }
        catch (IOException e) {
        }
        
        return stringBuilder.toString();
    }

    public String loadStringFromFile(String filename)
    {
        BufferedReader bufferedReader = this.loadBufferedReader(filename);
        if (null == bufferedReader)
        {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        try {
            String string = null;
            while(null != (string = bufferedReader.readLine()))
            {
                stringBuilder.append(string + JAVA_EOL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return stringBuilder.toString();
        }
    }

    private BufferedReader loadBufferedReader(String filename)
    {
        return new BufferedReader(new InputStreamReader(this.loadStreamFromFile(filename)));
    }

    private InputStream loadStreamFromFile(String filename)
    {
        try {
            return this.context.getAssets().open(filename);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
