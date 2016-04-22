package com.appvisor_event.master;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.appvisor_event.master.modules.AppPermission.AppPermission;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class QrCodeActivity extends Activity implements ZXingScannerView.ResultHandler, LocationListener, AppPermission.Interface {

    private ZXingScannerView mScannerView;
    private ArrayList<BarcodeFormat> formats;
    private double latitude;
    private double longitude;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private Location location;
    private Result result;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; //1 meter
    private static final long MIN_TIME_UPDT = 10 * 60;

    private static final String[] needPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    protected LocationManager locationManager;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AppPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppPermission.checkPermission(this, needPermissions))
        {
            startScanner();
        }
        else {
            AppPermission.requestPermissions(this, needPermissions);
        }
    }

    private void startScanner()
    {
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);
        formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(formats);
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();
        location = getLocation();
    }

    private final Runnable delayFunc= new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent();
            intent.putExtra("data",result.getText());
            intent.putExtra("lon",longitude);
            intent.putExtra("lat",latitude);
            setResult(RESULT_OK,intent);
            QrCodeActivity.this.finish();
        }
    };

    @Override
    public void onPause() {
        super.onPause();

        if (null != mScannerView)
        {
            mScannerView.stopCamera();   // Stop camera on pause
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if (null != mScannerView)
        {
            mScannerView.stopCamera();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void handleResult(final Result rawResult){
        Log.e("handler", rawResult.getText()); // Prints scan results
        Log.e("handler", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode)
        result = rawResult;
        new Handler().postDelayed(delayFunc, 2500);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 端末の戻るボタンを押した時にwebviewの戻る履歴があれば1つ前のページに戻る
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public Location getLocation() {

        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("TAG1", "CANT GET THE POSSITION");
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, MIN_TIME_UPDT, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (location != null) {
                        Log.d("TAG1", "NETWORK");
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        Log.d("TAG1", "GPS");
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_UPDT, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public Boolean isRequirePermission(String permission) {
        AppPermission.log(String.format("isRequirePermission: %s", permission));

        Boolean isRequirePermission = false;

        switch (permission)
        {
            case Manifest.permission.CAMERA:
            case Manifest.permission.ACCESS_FINE_LOCATION:
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                isRequirePermission = true;
                break;
        }

        return isRequirePermission;
    }

    @Override
    public void showErrorDialog() {
        AppPermission.log(String.format("showErrorDialog"));

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.permission_dialog_title))
                .setMessage(getString(R.string.permission_dialog_message_camera_and_location))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AppPermission.openSettings(QrCodeActivity.this);
                        QrCodeActivity.this.finish();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void allRequiredPermissions() {
        startScanner();
    }
}
