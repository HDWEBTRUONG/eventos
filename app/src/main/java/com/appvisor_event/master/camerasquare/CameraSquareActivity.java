package com.appvisor_event.master.camerasquare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.appvisor_event.master.R;


public class CameraSquareActivity extends AppCompatActivity {

    public static final String TAG = CameraSquareActivity.class.getSimpleName();
    public static final int RESULT_FINISH = 1;
    private CameraFragment mfragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.squarecamera_CameraFullScreenTheme);
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_squarecamera_camera);

        if (savedInstanceState == null) {
            mfragment = (CameraFragment) CameraFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, mfragment, CameraFragment.TAG)
                    .commit();
        }
    }

    public void returnPhotoUri(Uri uri) {
        Intent data = new Intent();
        data.setData(uri);

        if (getParent() == null) {
            setResult(RESULT_OK, data);
        } else {
            getParent().setResult(RESULT_OK, data);
        }

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_FINISH) {
            finish();
        } else {
            mfragment.restartCamera2();
        }
    }

    public void onCancel(View view) {
        getSupportFragmentManager().popBackStack();
    }
}
