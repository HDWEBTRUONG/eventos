package com.appvisor_event.master.modules.Gcm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.appvisor_event.master.AppUUID;
import com.appvisor_event.master.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by bsfuji on 2014/11/17.
 */
public class GcmClient
{
    public static interface GcmSettingListener
    {
        public void onSuccess();
        public void onFailed();
    }

    private static final String BASE_URL                         = Constants.GCM_BASE_URL;
    private static final String REGISTRATION_URL                 = BASE_URL + "/registration.php";
    private static final String SETTINGS_URL                     = BASE_URL + "/settings.php";
    private static final String REDIRECT_URL                     = BASE_URL + "/redirect.php";
    private static final String EXTRA_MESSAGE                    = "message";
    private static final String PROPERTY_REG_ID                  = "registration_id";
    private static final String PROPERTY_APP_VERSION             = "appVersion";
    private static final String GCM_ENABLE_SETTING               = "gcm_enable_setting";
    private static final int    PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private String               senderId = Constants.GCM_SENDER_ID;
    private String         registrationId = "";
    private GoogleCloudMessaging      gcm = null;
    private Context               context = null;

    public GcmClient(Context context)
    {
        this.context = context;

        this.gcm = GoogleCloudMessaging.getInstance(context);
        this.registrationId = this.getRegistrationId();
        if (this.registrationId.isEmpty())
        {
            this.registerInBackground();
        }
        else
        {
            this.sendRegistrationIdToBackend();
        }
    }

    public static boolean checkPlayServices(Activity activity)
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                activity.finish();
            }

            return false;
        }

        return true;
    }

    public String getLink(int link)
    {
        return REDIRECT_URL + "?link=" + link;
    }

    public void openLink(int link)
    {
        Uri uri = Uri.parse(REDIRECT_URL + "?link=" + link);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        GcmClient.this.context.startActivity(intent);
    }

    public String getRegistrationId()
    {
        final SharedPreferences preferences = this.getGCMPreferences();
        String registrationId = preferences.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty())
        {
            return "";
        }

        int registeredVersion = preferences.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = this.getAppVersion(GcmClient.this.context);
        if (registeredVersion != currentVersion)
        {
            return "";
        }

        return registrationId;
    }

    private SharedPreferences getGCMPreferences()
    {
        return this.context.getSharedPreferences("GCM", this.context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context)
    {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException exception) {
            throw new RuntimeException("Could not get package name: " + exception);
        }
    }

    private void registerInBackground()
    {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try
                {
                    if (GcmClient.this.gcm == null)
                    {
                        GcmClient.this.gcm = GoogleCloudMessaging.getInstance(GcmClient.this.context);
                    }

                    GcmClient.this.registrationId = GcmClient.this.gcm.register(GcmClient.this.senderId);
                    GcmClient.this.sendRegistrationIdToBackend();
                    GcmClient.this.storeRegistrationId(GcmClient.this.registrationId);
                }
                catch (IOException exception)
                {
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                Log.d("", "registrationId = " + GcmClient.this.registrationId);
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(String registrationId)
    {
        final SharedPreferences preferences = this.getGCMPreferences();
        int appVersion = this.getAppVersion(GcmClient.this.context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROPERTY_REG_ID, registrationId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void sendRegistrationIdToBackend()
    {
        HttpConnection httpConnection = new HttpConnection();
        httpConnection.addParam("mobileId", AppUUID.get(this.context.getApplicationContext()).replace("-","").replace(" ","").replace(">","").replace("<",""));
        httpConnection.addParam("platform", "android");
        httpConnection.addParam("token", GcmClient.this.registrationId);
        httpConnection.asyncPost(REGISTRATION_URL);
    }

    public boolean getGcmEnableSetting()
    {
        SharedPreferences preferences = this.getGCMPreferences();
        return preferences.getBoolean(GCM_ENABLE_SETTING, true);
    }

    private void storeGcmEnableSetting(boolean isEnable)
    {
        SharedPreferences preferences = this.getGCMPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(GCM_ENABLE_SETTING, isEnable);
        editor.commit();
    }

    public void sendPushSettingToBackend(Boolean isOn, final GcmSettingListener gcmSettingListener)
    {
        final Handler handler = new Handler();
        HttpConnection httpConnection = new HttpConnection(new com.appvisor_event.master.modules.Gcm.HttpConnection.HttpConnectionListener() {
            @Override
            public void onFinished(final String responseString) {
                handler.post(new Runnable() {
                    @Override
                    public void run () {
                        if ("OK".equals(responseString)) {
                            GcmClient.this.storeGcmEnableSetting(!GcmClient.this.getGcmEnableSetting());
                            gcmSettingListener.onSuccess();
                            return;
                        }
                        gcmSettingListener.onFailed();
                    }
                });
            }

            @Override
            public void onFailed(Exception exception) {
                handler.post(new Runnable() {
                    @Override
                    public void run () {
                        gcmSettingListener.onFailed();
                    }
                });
            }
        });
        httpConnection.addParam("token", GcmClient.this.registrationId);
        httpConnection.addParam("enable", isOn.toString());
        httpConnection.asyncPost(SETTINGS_URL);
    }
}
