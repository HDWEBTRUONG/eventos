package com.appvisor_event.master.modules.Spiral.ETicket;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by bsfuji on 16/08/02.
 */
public class QRCodeScannerActivity extends Activity implements ZXingScannerView.ResultHandler
{
    private ZXingScannerView mScannerView;
    private ArrayList<BarcodeFormat> formats;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initScannerView();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        mScannerView.stopCamera();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        mScannerView.stopCamera();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void handleResult(Result result)
    {
        Intent intent = new Intent();
        intent.putExtra("qrcode_data",result.getText());
        setResult(RESULT_OK, intent);

        finish();
    }

    private void initScannerView()
    {
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);

        ArrayList<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(formats);

        mScannerView.setResultHandler(this);

        mScannerView.startCamera();
    }
}
