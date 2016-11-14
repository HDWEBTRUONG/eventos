package com.appvisor_event.master;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.appvisor_event.master.camerasquare.CameraSquareActivity;
import com.appvisor_event.master.model.FrameBean;
import com.appvisor_event.master.model.ItemsBean;
import com.appvisor_event.master.modules.AppLanguage.AppLanguage;
import com.appvisor_event.master.util.SPUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ImageActivity extends Activity implements View.OnClickListener, ShareDialog.ShareLisenter, RecycleAdapter.RecycleImgListener {
    private CustomImageView imageView;
    private ImageView button;
    private Thread thread;
    private ProgressDialog progressDialog;
    private ShareDialog shareDialog;
    private ArrayList<ImageItem> list;
    private ImageButton button_close, button_back;
    private RecyclerView recyclerView;
    private RecycleAdapter adapter;
    private ArrayList<ArrayList<ImageItem>> mList;
    private String frame;
    private LinearLayout layout_recycle;
    private FrameBean frameBean;
    private String img_name="";
    private int share_position;
    private List<ItemsBean> items;

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
        button = (ImageView) findViewById(R.id.button);
        button_close = (ImageButton) findViewById(R.id.button_close);
        button_back = (ImageButton) findViewById(R.id.camera_back);
        layout_recycle = (LinearLayout) findViewById(R.id.layout_recycle);
        button_close.setOnClickListener(this);
        button_back.setOnClickListener(this);

        Intent intent = getIntent();
        String img_url = intent.getExtras().getString("image_url");
        frame = SPUtils.get(getApplicationContext(), "frame", "") + "";
        mList = new ArrayList<>();
         frameBean = new Gson().fromJson(frame, FrameBean.class);
        if (frame != null && !frame.equals("") && frameBean != null) {
            if (AppLanguage.getLanguageWithStringValue(ImageActivity.this).equals("ja")) {
                items=frameBean.getJa();
                for (int i = 0; i < frameBean.getJa().size(); i++) {
                    list = new ArrayList<>();
                    ImageItem item_back = new ImageItem();
                    item_back.setName(img_url);
                    list.add(item_back);
                    for (int j = 0; j < frameBean.getJa().get(i).getItems().size(); j++) {
                        ImageItem item = new ImageItem();
                        item.setName(frameBean.getJa().get(i).getItems().get(j).getName());
                        item.setScale((float) frameBean.getJa().get(i).getItems().get(j).getWidth());
                        item.setWidth_position((float) frameBean.getJa().get(i).getItems().get(j).getX());
                        item.setHeight_position((float) frameBean.getJa().get(i).getItems().get(j).getY());
                        list.add(item);
                    }
                    mList.add(list);
                }
            } else {
                items=frameBean.getEn();
                for (int i = 0; i < frameBean.getEn().size(); i++) {
                    list = new ArrayList<>();
                    ImageItem item_back = new ImageItem();
                    item_back.setName(img_url);
                    list.add(item_back);
                    for (int j = 0; j < frameBean.getEn().get(i).getItems().size(); j++) {
                        ImageItem item = new ImageItem();
                        item.setName(frameBean.getEn().get(i).getItems().get(j).getName());
                        item.setScale((float) frameBean.getEn().get(i).getItems().get(j).getWidth());
                        item.setWidth_position((float) frameBean.getEn().get(i).getItems().get(j).getX());
                        item.setHeight_position((float) frameBean.getEn().get(i).getItems().get(j).getY());
                        list.add(item);
                    }
                    mList.add(list);
                }
            }
            recyclerView = (RecyclerView) findViewById(R.id.image_recycle);
            ((SimpleItemAnimator)recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            LinearLayoutManager mLinearManager = new LinearLayoutManager(this);
            mLinearManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(mLinearManager);
            adapter = new RecycleAdapter(this, mList);
            adapter.setOnMenuListener(this);
            recyclerView.setAdapter(adapter);
            if (mList != null && mList.size() > 0) {
                imageView.addImage(mList.get(0));
            }
        } else {
            layout_recycle.setVisibility(View.GONE);
            imageView.addDefault(img_url);
        }


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
                shareDialog = new ShareDialog(ImageActivity.this);
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
        if (!img_name.equals("")){
            File f = new File(file.getAbsolutePath() + "/", img_name+".png");
            if (f.exists()) {
                f.delete();
            }
        }
        img_name=System.currentTimeMillis()+"";
        File f = new File(file.getAbsolutePath() + "/", img_name+".png");
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
            this.sendBroadcast(intent);
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
        switch (v.getId()) {
            case R.id.button_close:
                Intent intent = new Intent(ImageActivity.this, CameraSquareActivity.class);
                setResult(CameraSquareActivity.RESULT_FINISH, intent);
                finish();
                break;
            case R.id.camera_back:
                showCameradialog();
                break;
        }
    }

    @Override
    public void resultMessage(int type) {
        progressDialog.show();
        switch (type) {
            case ShareDialog.SHARE_TWITTER:
                ShareDetailUtils.shareTwitter(ImageActivity.this,items.get(share_position).getMessage(), "", "", "", Environment.getExternalStorageDirectory() + "/EventImage/" + img_name+".png");
                progressDialog.dismiss();
                break;
            case ShareDialog.SHARE_FACEBOOK:
                ShareDetailUtils.shareFaceBook(ImageActivity.this,items.get(share_position).getMessage(), "", "", "", Environment.getExternalStorageDirectory() + "/EventImage/" +  img_name+".png");
                progressDialog.dismiss();
                break;
            case ShareDialog.SHARE_INSTAGRAM:
                ShareDetailUtils.shareInstgram(ImageActivity.this,items.get(share_position).getMessage(), "", "", "", Environment.getExternalStorageDirectory() + "/EventImage/" + img_name+".png");
                progressDialog.dismiss();
                break;
        }
    }

    @Override
    public void RecycleClick(int position) {
        imageView.addImage(mList.get(position));
        share_position=position;
    }


    private void showCameradialog() {
        String content = "";
        String ok = "";
        String cancel = "";

        if (AppLanguage.getLanguageWithStringValue(this).equals("ja")) {
            content = getResources().getString(R.string.image_camera_dialog_jp);
            ok = getResources().getString(R.string.image_camera_dialog_yes_jp);
            cancel = getResources().getString(R.string.image_camera_dialog_no_jp);

        } else {
            content = getResources().getString(R.string.image_camera_dialog_en);
            ok = getResources().getString(R.string.image_camera_dialog_yes_en);
            cancel = getResources().getString(R.string.image_camera_dialog_no_en);
        }
        new AlertDialog.Builder(this).setMessage(content)
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }

                )
                .setNegativeButton(cancel, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }

                )
                .show();
    }
}
