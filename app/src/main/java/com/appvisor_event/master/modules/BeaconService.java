package com.appvisor_event.master.modules;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.appvisor_event.master.AppUUID;
import com.appvisor_event.master.BaseActivity;
import com.appvisor_event.master.Constants;
import com.appvisor_event.master.InfosGetter;
import com.appvisor_event.master.MainActivity;
import com.appvisor_event.master.R;
import com.appvisor_event.master.modules.AppLanguage.AppLanguage;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by kawa on 16/08/28.
 */
public class BeaconService extends Service implements BeaconConsumer {

    public static final String TAG = "BeaconService";
    public static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    private BeaconManager beaconManager;

    public static JSONObject beaconobjs=null;
    public static String beaconmap=null;
    public static boolean isUnityService=false;

    public static ArrayList<BaseActivity> activities=new ArrayList<BaseActivity>();

    private ArrayList<Region> listregions=null;
    private Region mapRegion=null;

    private ArrayList<String> beacons_inRegion;

    private HashMap<String,JSONArray> beacons_message;

    private Set<String> beaconSendedset;
    private SimpleDateFormat simpleDateFormat;

    public static boolean isJP=true;

    public InfosGetter pushGetter;
    private  String user_uuid;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        listregions = new ArrayList<Region>();
        beacons_inRegion=new ArrayList<String>();
        beacons_message =new HashMap<String,JSONArray>();
        beaconSendedset=getSendedMessageSet();
        isJP= AppLanguage.isJapanese(this);
        user_uuid= AppUUID.get(this);
        simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);
        SharedPreferences sharedPreferences=getSharedPreferences("beaconData", Context.MODE_PRIVATE);
        String messages = sharedPreferences.getString("beaconmessages",null);
        if(messages !=null) {
            try {
                beaconobjs = new JSONObject(messages);
            } catch (JSONException e) {
                beaconobjs =null;
            }
        }
        try {
            beaconManager = BeaconManager.getInstanceForApplication(this);
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));
            beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(5));
            beaconManager.setBackgroundBetweenScanPeriod(TimeUnit.SECONDS.toMillis(5));

            if(beaconobjs!=null)
            {
                JSONArray listbeacons = beaconobjs.getJSONArray("beacons");
                if(listbeacons !=null&&listbeacons.length()>0) {
                    JSONObject beaconMessageobj = listbeacons.getJSONObject(0);
                    JSONObject beaconidobj = beaconMessageobj.getJSONObject("id");
                    String uuid = beaconidobj.getString("uuid");
                    Region reg = new Region("beacon", Identifier.parse(uuid), null, null);
                    listregions.add(reg);

                    for (int i = 0; i < listbeacons.length(); i++) {
                        JSONObject beaconMessage = listbeacons.getJSONObject(i);

                        JSONObject beaconid = beaconMessage.getJSONObject("id");
                        String beaconKey = beaconid.getString("uuid") + beaconid.getString("major") + beaconid.getString("minor");
                        beacons_message.put(beaconKey, beaconMessage.getJSONArray("data"));
                    }
                }
            }
            else if(beaconmap!=null)
            {
                Region reg = new
                        Region("beacon", Identifier.parse(beaconmap),null,null);
                listregions.add(reg);
            }
            beaconManager.bind(this);
            beaconManager.setBackgroundMode(true);
        }
         catch (JSONException e) {
            e.printStackTrace();
        }


    }

    //現在Active管理
    static public  void addActivity(BaseActivity activity)
    {
        activities.add(activity);
    }

    static public BaseActivity getTopActivity()
    {
        if(activities.size()==0) {
            return  null;
        }
        else
        {
            return  activities.get(activities.size() - 1);
        }
    }

    static  public  void removeTopActivety(BaseActivity activity)
    {
        activities.remove(activity);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (beaconManager.isBound(this)) beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Object[] beaconlist=beacons.toArray();
                beacons_inRegion.clear();
                if (beaconlist.length > 0) {
                    Date nowtime=new Date();
                    String strnowtime = simpleDateFormat.format(nowtime);
                    Beacon nearestbeacon=(Beacon)beaconlist[0];
                    for(int i = 0 ;i<beaconlist.length;i++) {

                        Beacon beacon =(Beacon)beaconlist[i];
                        if(beaconmap!=null) {
                            if (nearestbeacon.getDistance() > beacon.getDistance()) {
                                nearestbeacon = beacon;
                            }
                        }
                        String bkey=""+beacon.getId1()+Integer.toHexString(Integer.parseInt(String.valueOf(beacon.getId2())))+Integer.toHexString(Integer.parseInt(String.valueOf(beacon.getId3())));

                        beacons_inRegion.add(bkey);

                        if(beacons_message.get(bkey)!=null)
                        {
                            try {
                            JSONArray beaconmessages=beacons_message.get(bkey);
                            for (int k =0;k<beaconmessages.length();k++)
                            {
                                JSONObject message = beaconmessages.getJSONObject(k);
                                String messageid=message.getString("id");
                                if(beaconSendedset==null||(!beaconSendedset.contains(messageid)))
                                {
                                    JSONObject period=message.getJSONObject("period");
                                    //時間を判断
                                    String fromtime = period.getString("from");
                                    String endtime = period.getString("to");
                                    if(strnowtime.compareTo(fromtime)>=0&&strnowtime.compareTo(endtime)<=0)
                                    {
                                        beaconSendedset.add(messageid);
                                        setSendedMessageSet(beaconSendedset);
                                        //メッセージを表示
                                        JSONObject msgcontent = message.getJSONObject("message");
                                        if(msgcontent!=null)
                                        {
                                            String title="";
                                            String body="";
                                            String link="";
                                            int isInternal=0;
                                            if(isJP)
                                            {
                                                JSONObject jpmessage = msgcontent.getJSONObject("ja");
                                                title=jpmessage.getString("title");
                                                body=jpmessage.getString("body");
                                                link=jpmessage.getString("link");
                                                isInternal=jpmessage.getInt("isInternal");
                                            }
                                            else
                                            {
                                                JSONObject enmessage = msgcontent.getJSONObject("en");
                                                title=enmessage.getString("title");
                                                body=enmessage.getString("body");
                                                link=enmessage.getString("link");
                                                isInternal=enmessage.getInt("isInternal");

                                            }
                                            showMessageDailog(messageid,title,body,link,isInternal);
                                        }

                                    }

                                }
                            }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                    if(beaconmap!=null) {
                        //TO Send MAP set
                        Intent broadcastIntent =new Intent();
                        broadcastIntent.putExtra("beaconON",true);
                        broadcastIntent.putExtra("uuid",String.valueOf(nearestbeacon.getIdentifier(0)));
                        broadcastIntent.putExtra("major",String.valueOf(nearestbeacon.getIdentifier(1)));
                        broadcastIntent.putExtra("minor",Integer.toHexString(Integer.parseInt(String.valueOf(nearestbeacon.getIdentifier(2)))));
                        broadcastIntent.setAction("Beacon_Nearest");
                        getBaseContext().sendBroadcast(broadcastIntent);
                    }
                }
                else
                {
                    //TO clear MAP set
                    Intent broadcastIntent =new Intent();
                    broadcastIntent.putExtra("beaconON",false);
                    broadcastIntent.setAction("Beacon_Nearest");
                    getBaseContext().sendBroadcast(broadcastIntent);
                }
            }
        });
        try {
            for(Region region : listregions) {
                beaconManager.startRangingBeaconsInRegion(region);
                beaconManager.startMonitoringBeaconsInRegion(region);
            }
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
    }

    private Set<String> getSendedMessageSet()
    {
        SharedPreferences sharedPreferences=getSharedPreferences("beaconData", Context.MODE_PRIVATE);
        Set<String> messageSet =  sharedPreferences.getStringSet("sendedmessage",null);
        if(messageSet==null) {
            messageSet= new HashSet<String>();
        }
        return messageSet;
    }

    private void setSendedMessageSet(Set<String> messageSet)
    {
        SharedPreferences sharedPreferences=getSharedPreferences("beaconData",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("sendedmessage", messageSet);
        editor.apply();
    }

    private void showMessageDailog(String msgid,String title,String message,String link,int isWebview)
    {
        BaseActivity topactivity = (BaseActivity) getTopActivity();
        if(topactivity!=null)
        {
            //アプリ起動中
            if(isRunningForeground())
            {
                if(isUnityService)
                {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction("Beacon_message_unity");
                    broadcastIntent.putExtra("msgid", msgid);
                    broadcastIntent.putExtra("title", title);
                    broadcastIntent.putExtra("body", message);
                    broadcastIntent.putExtra("link", link);
                    broadcastIntent.putExtra("isInternal", isWebview);
                    broadcastIntent.putExtra("isNotification", false);
                    getBaseContext().sendBroadcast(broadcastIntent);
                    sendAPIInfo(user_uuid, msgid, "2");
                }
                else {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction("Beacon_message");
                    broadcastIntent.putExtra("msgid", msgid);
                    broadcastIntent.putExtra("title", title);
                    broadcastIntent.putExtra("body", message);
                    broadcastIntent.putExtra("link", link);
                    broadcastIntent.putExtra("isInternal", isWebview);
                    broadcastIntent.putExtra("isNotification", false);
                    getBaseContext().sendBroadcast(broadcastIntent);
                    sendAPIInfo(user_uuid, msgid, "2");
                }
            }
            else
            {
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder mNotifyBuilder =  new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_status).setContentTitle(title).setContentText(message).setAutoCancel(true);
                Intent resultIntent = new Intent(this, MainActivity.class);
                resultIntent.putExtra("msgid",msgid);
                resultIntent.putExtra("title",title);
                resultIntent.putExtra("body",message);
                resultIntent.putExtra("link",link);
                resultIntent.putExtra("isInternal",isWebview);
                resultIntent.putExtra("isNotification",true);
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mNotifyBuilder.setContentIntent(pi);
                mNotificationManager.notify(Integer.parseInt(msgid), mNotifyBuilder.build());
                sendAPIInfo(user_uuid,msgid,"0");
            }
        }
        else
        {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mNotifyBuilder =  new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_status).setContentTitle(title).setContentText(message).setAutoCancel(true);
            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.putExtra("msgid",msgid);
            resultIntent.putExtra("title",title);
            resultIntent.putExtra("body",message);
            resultIntent.putExtra("link",link);
            resultIntent.putExtra("isInternal",isWebview);
            resultIntent.putExtra("isNotification",true);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotifyBuilder.setContentIntent(pi);

            mNotificationManager.notify(Integer.parseInt(msgid), mNotifyBuilder.build());
            sendAPIInfo(user_uuid,msgid,"0");
        }
    }

    private void sendAPIInfo(String uuid,String msgid,String msgType)
    {
        try {
            pushGetter = new InfosGetter(Constants.Beacon_AGGREGATE_API+"uuid="+uuid+"&MsgID="+msgid+"&Type="+msgType); //new InfosGetter(Constants.Beacon_AGGREGATE_API);
            pushGetter.start();
            pushGetter.join();
            if (pushGetter.mResponse != null && pushGetter.mResponse != "") {
                Log.i("pushGetter",pushGetter.mResponse);
                Log.i("pushGetter",Constants.Beacon_AGGREGATE_API+"uuid="+uuid+"&MsgID="+msgid+"&Type="+msgType);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public boolean isRunningForeground(){
        String topActivityClassName=getTopActivityName(this);
        if (topActivityClassName!=null&&topActivityClassName.startsWith("com.appvisor_event.master")) {

            return true;
        } else {

            return false;
        }
    }

    public  String getTopActivityName(Context context){
        String topActivityClassName=null;
        ActivityManager activityManager =
                (ActivityManager)(context.getSystemService(android.content.Context.ACTIVITY_SERVICE )) ;
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1) ;
        if(runningTaskInfos != null){
            ComponentName f=runningTaskInfos.get(0).topActivity;
            topActivityClassName=f.getClassName();
        }
        return topActivityClassName;
    }

}
