package com.appvisor_event.master;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

import static java.sql.Types.NULL;


/**
 * Created by ookuma on 2017/04/06.
 */

public class ImageViewerActivity extends Activity implements View.OnClickListener {
    private LinearLayout mainLayout;
    private RelativeLayout mContext;
    private String mPlan;
    ImageViewTouch mImage;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
//        ImageView menuView = (ImageView) this.findViewById(R.id.menu_buttom);
//        menuView.setVisibility(View.GONE);
//        TextView textView = (TextView) findViewById(R.id.content_text);
//        textView.setText("FacebookPhoto");
        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mainLayout.setOnClickListener(this);

        Intent i = getIntent();
        String urlStr = i.getStringExtra("image_url");
        mPlan = i.getStringExtra("plan");
        if (mPlan != null){
            if (mPlan.equals("free")){
                Button saveButton = (Button) findViewById(R.id.button);
                saveButton.setVisibility(View.GONE);
            }
        }

        mImage = (ImageViewTouch) findViewById(R.id.image);
        Glide.with(this.getApplicationContext())
                .load(urlStr)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mImage.setOnClickListener(ImageViewerActivity.this);
                        mImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
                        return false;
                    }
                })
                .into(mImage);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void onClickButtonBack(View view) {
        ImageViewerActivity.this.finish();
    }

    public void onClick(View v) {
        if (v.getId() != R.id.image){
            ImageViewerActivity.this.finish();
        }
    }

    public void savePhoto(View v) {
        ImageView image = (ImageView)findViewById(R.id.image);
        Bitmap sampleImage = ((GlideBitmapDrawable)image.getDrawable().getCurrent()).getBitmap();
        saveBitmap(sampleImage);
    }

    public void closePhoto(View v) {
        ImageViewerActivity.this.finish();
    }

    public void saveBitmap(Bitmap saveImage) {

        final String SAVE_DIR = "/MyPhoto/";
        File file = new File(Environment.getExternalStorageDirectory().getPath() + SAVE_DIR);
        try{
            if(!file.exists()){
                file.mkdir();
            }
        }catch(SecurityException e){
            e.printStackTrace();
            throw e;
        }

        Date mDate = new Date();
        SimpleDateFormat fileNameDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String fileName = fileNameDate.format(mDate) + ".jpg";
        String AttachName = file.getAbsolutePath() + "/" + fileName;

        try {
            FileOutputStream out = new FileOutputStream(AttachName);
            saveImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

        // save index
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = getContentResolver();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put("_data", AttachName);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Toast.makeText(this,"保存されました",Toast.LENGTH_SHORT).show();
    }

    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}
