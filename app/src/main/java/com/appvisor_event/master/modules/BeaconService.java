package com.appvisor_event.master.modules;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

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

    public static ArrayList<Activity> activities=new ArrayList<Activity>();

    private ArrayList<Region> listregions=null;
    private Region mapRegion=null;

    private ArrayList<String> beacons_inRegion;

    private HashMap<String,JSONArray> beacons_message;

    private Set<String> beaconSendedset;
    private SimpleDateFormat simpleDateFormat;

    private boolean isJP=true;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //                region = new Region("1", Identifier.parse("99813696-8e94-4c20-acf0-8f874d18f4bd"), null, null);
    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("Test","Service onCreate!!!!");

        listregions = new ArrayList<Region>();
        beacons_inRegion=new ArrayList<String>();
        beacons_message =new HashMap<String,JSONArray>();
        beaconSendedset=getSendedMessageSet();
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
    static public  void addActivity(Activity activity)
    {
        activities.add(activity);
    }

    static public Activity getTopActivity()
    {
        if(activities.size()==0) {
            return  null;
        }
        else
        {
            return  activities.get(activities.size() - 1);
        }
    }

    static  public  void removeTopActivety(Activity activity)
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
                        Log.i(TAG,String.valueOf(beacon.getIdentifier(2))+"::::"+beacon.getDistance());
                        Log.d(TAG, "UUID:" + beacon.getId1() + ", major:" + beacon.getId2() + ", minor:" + beacon.getId3() + ", Distance:" + beacon.getDistance() + ",RSSI" + beacon.getRssi() + ", TxPower" + beacon.getTxPower());
                        String bkey=""+beacon.getId1()+beacon.getId2()+Integer.toHexString(Integer.parseInt(String.valueOf(beacon.getId3())));
                        beacons_inRegion.add(bkey);

                        if(beacons_message.get(bkey)!=null)
                        {
                            Log.i(TAG,bkey);
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
                                    Log.i("Message:::",fromtime+ "VV"+strnowtime.compareTo(fromtime)+"VV"+strnowtime+"VV"+strnowtime.compareTo(endtime)+"VV"+endtime);
                                    if(strnowtime.compareTo(fromtime)>=0&&strnowtime.compareTo(endtime)<=0)
                                    {
//                                        beaconSendedset.add(messageid);
//                                        setSendedMessageSet(beaconSendedset);
                                        //メッセージを表示
                                        JSONObject msgcontent = message.getJSONObject("message");
                                        if(msgcontent!=null)
                                        {
                                            String title="";
                                            String body="";
                                            String link="";
                                            if(isJP)
                                            {
                                                JSONObject jpmessage = msgcontent.getJSONObject("jp");
                                                title=jpmessage.getString("title");
                                                body=jpmessage.getString("body");
                                                link=jpmessage.getString("link");
                                            }
                                            else
                                            {
                                                JSONObject enmessage = msgcontent.getJSONObject("en");
                                            }
//                                            showMessageDailog();
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
                        //TO Send MAP set //TODO ①
                        Log.i(TAG +"NEAR::", String.valueOf(nearestbeacon.getIdentifier(0)) + "  " + String.valueOf(nearestbeacon.getIdentifier(1)) + "  " + Integer.toHexString(Integer.parseInt(String.valueOf(nearestbeacon.getIdentifier(2)))));
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
                    Log.d(TAG,"no beacon data!!!");
                    //TO clear MAP set  //TODO　②
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

    private void showMessageDailog(String title,String message,String link)
    {
        Activity topactivity= getTopActivity();
        if(topactivity!=null)
        {
            //アプリ起動中
            if(isRunningForeground())
            {
                Log.i(TAG+" dailog:",message);
            }
            else
            {
                //push
                Log.i(TAG+" push:",message);
            }
        }
        else
        {
            //push
            Log.i(TAG+" push:",message);
        }
    }

    public boolean isRunningForeground(){
        String packageName=getPackageName(getBaseContext());
        String topActivityClassName=getTopActivityName(getBaseContext());
        System.out.println("packageName="+packageName+",topActivityClassName="+topActivityClassName);
        if (packageName!=null&&topActivityClassName!=null&&topActivityClassName.startsWith(packageName)) {

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

    public String getPackageName(Context context){
        String packageName = context.getPackageName();
        return packageName;
    }


}
