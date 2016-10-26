package com.appvisor_event.master;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class ImageActivity extends Activity implements View.OnClickListener,ShareDialog.ShareLisenter{
    private CustomImageView imageView, imageView1, imageView2, imageView3, imageView4,imageView_default;
    private ImageView button;
    private Thread thread;
    private ProgressDialog progressDialog;
    private LayerDrawable layerDrawable1, layerDrawable2, layerDrawable3, layerDrawable4;
    private int width,heigth;
    private ShareDialog shareDialog;
    private ArrayList<ImageItem> list,list1,list2,list3;
    private ImageButton button_close,button_back;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_image);
        imageView = (CustomImageView) findViewById(R.id.img);
        imageView1 = (CustomImageView) findViewById(R.id.image1);
        imageView2 = (CustomImageView) findViewById(R.id.image2);
        imageView3 = (CustomImageView) findViewById(R.id.image3);
        imageView4 = (CustomImageView) findViewById(R.id.image4);
        imageView_default = (CustomImageView) findViewById(R.id.img_default);
        imageView1.setOnClickListener(this);
        imageView2.setOnClickListener(this);
        imageView3.setOnClickListener(this);
        imageView4.setOnClickListener(this);
        button = (ImageView) findViewById(R.id.button);
//        Drawable[] layers1 = new Drawable[2];
//        layers1[0] = ContextCompat.getDrawable(this, R.drawable.screen);
//        layers1[1] = new PaintDrawable(ContextCompat.getColor(this, R.color.black10));
//        layerDrawable1 = new LayerDrawable(layers1);
            button_close= (ImageButton) findViewById(R.id.button_close);
        button_back= (ImageButton) findViewById(R.id.camera_back);
        button_close.setOnClickListener(this);
        button_back.setOnClickListener(this);

            WindowManager wm = (WindowManager)this.getSystemService(Context.WINDOW_SERVICE);

            width = wm.getDefaultDisplay().getWidth();
            heigth = wm.getDefaultDisplay().getHeight();

        list=new ArrayList<>();
        list1=new ArrayList<>();
        list2=new ArrayList<>();
        list3=new ArrayList<>();

        Intent intent=getIntent();
        String img_url=intent.getExtras().getString("image_url");

        for (int i = 0; i <3; i++) {
            ImageItem item=new ImageItem();

            if (i>0){
                item.setWidth_position(i*0.1f);
                item.setHeight_position(i*0.2f);
                item.setScale(0.1f);
            }
            if (i==0){
                item.setName(img_url);
            }else if (i==1){
                item.setId(R.drawable.bulb_on_64);
            }else if (i==2){
                item.setId(R.drawable.calendar_64);
            }
            list.add(item);
        }

        for (int i = 0; i <3; i++) {
            ImageItem item=new ImageItem();
            item.setName("event"+i);
            if (i>0){
                item.setWidth_position(i*0.13f);
                item.setHeight_position(i*0.1f);
                item.setScale(0.15f);
            }
            if (i==0){
                item.setName(img_url);
            }else if (i==1){
                item.setId(R.drawable.case_64);
            }else if (i==2){
                item.setId(R.drawable.bulb_on_64);
            }
            list1.add(item);
        }

        for (int i = 0; i <3; i++) {
            ImageItem item=new ImageItem();
            item.setName("event"+i);
            if (i>0){
                item.setWidth_position(i*0.2f);
                item.setHeight_position(i*0.3f);
                item.setScale(0.11f);
            }
            if (i==0){
                item.setName(img_url);
            }else if (i==1){
                item.setId(R.drawable.case_64);
            }else if (i==2){
                item.setId(R.drawable.chart_area_64);
            }
            list2.add(item);
        }

        for (int i = 0; i <3; i++) {
            ImageItem item=new ImageItem();
            item.setName("event"+i);
            if (i>0){
                item.setWidth_position(i*0.11f);
                item.setHeight_position(i*0.15f);
                item.setScale(0.12f);
            }
            if (i==0){
                item.setName(img_url);
            }else if (i==1){
                item.setId(R.drawable.cart_64);
            }else if (i==2){
                item.setId(R.drawable.chart_bar_64);
            }
            list3.add(item);
        }

        imageView.addImage(1,list);
        imageView1.addImage(2,list);
        imageView2.addImage(2,list1);
        imageView3.addImage(2,list2);
        imageView4.addImage(2,list3);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(ImageActivity.this);
                progressDialog.show();
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setDrawingCacheEnabled(true);
                        saveBitmap(imageView.getDrawingCache());
                        imageView.setDrawingCacheEnabled(false);
                        thread.interrupt();

                    }
                });
                thread.start();

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Log.e("test", "分享图片");
                progressDialog.dismiss();
                progressDialog.cancel();
                shareDialog= new ShareDialog(ImageActivity.this);
                shareDialog.setOnMessageLisenter(ImageActivity.this);
            }
        }
    };

    /**
     * 保存方法
     */
    public void saveBitmap(Bitmap bm) {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            Log.e("test", "路径错误");
            return;
        }

        Log.e("test", "保存图片");
        File file = new File(Environment.getExternalStorageDirectory() + "/EventImage/");
        if (!file.exists()) {
            file.mkdirs();
        }
        File f = new File(file.getAbsolutePath() + "/", "TEST.png");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 0, out);
            out.flush();
            out.close();
            Log.i("test", "已经保存");
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(f);
            intent.setData(uri);
            this.sendBroadcast(intent);//这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
            Message msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        imageView_default.setVisibility(View.GONE);
        switch (v.getId()) {
            case R.id.image1:
                imageView.addImage(1,list);
                break;
            case R.id.image2:
                imageView.addImage(1,list1);
                break;
            case R.id.image3:
                imageView.addImage(1,list2);
                break;
            case R.id.image4:
                imageView.addImage(1,list3);
                break;
            case R.id.button_close:
                finish();
                break;
            case R.id.camera_back:
                finish();
                break;
        }
    }

    @Override
    public void resultMessage(int type) {
        progressDialog.show();
        switch (type){
            case ShareDialog.SHARE_TWITTER:
                ShareDetailUtils.shareTwitter(ImageActivity.this, "title", "subtitle", "media", "herf", Environment.getExternalStorageDirectory() + "/EventImage/" + "TEST.png");
                progressDialog.dismiss();
                break;
            case ShareDialog.SHARE_FACEBOOK:
                ShareDetailUtils.shareFaceBook(ImageActivity.this, "title", "subtitle", "media", "herf", Environment.getExternalStorageDirectory() + "/EventImage/" + "TEST.png");
                progressDialog.dismiss();
                break;
            case ShareDialog.SHARE_INSTAGRAM:
                ShareDetailUtils.shareInstgram(ImageActivity.this, "title", "subtitle", "media", "herf", Environment.getExternalStorageDirectory() + "/EventImage/" + "TEST.png");
                progressDialog.dismiss();
                break;
        }
    }
}
