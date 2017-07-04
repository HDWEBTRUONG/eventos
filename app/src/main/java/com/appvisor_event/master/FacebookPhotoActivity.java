package com.appvisor_event.master;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import static com.appvisor_event.master.Constants.BASE_URL;
import static com.appvisor_event.master.Constants.HOME_URL;
import static com.appvisor_event.master.R.id.imageView;

/**
 * Created by ookuma on 2017/04/05.
 */

public class FacebookPhotoActivity extends Activity implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener {
    private GridView mGridview;
    private List<String> imgList = new ArrayList<String>();
    private MyHttpSender myJsonSender;
    private FacebookAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Boolean isReload;
    private Boolean isLoading;
    private int page;
    private int currentFirstVisibleItem;
    private int currentVisibleItemCount;
    private int currentScrollState;
    private ProgressDialog mProgressDialog;
    private Boolean isReadAll;
    private String mPlan;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_photo);

        isReload = false;
        isLoading = false;
        isReadAll = false;
        page = 1;

        mPlan = getIntent().getStringExtra("plan");

        ImageView menuView = (ImageView)this.findViewById(R.id.menu_buttom);
        menuView.setVisibility(View.GONE);
        TextView textView = (TextView) findViewById(R.id.content_text);
        textView.setText("FacebookPhoto");

        mGridview = (GridView) findViewById(R.id.gridView);
        mGridview.setOnScrollListener(this);
        mGridview.setOnItemClickListener(this);
        adapter = new FacebookAdapter(this.getApplicationContext(), R.layout.facebook_photo_item, imgList);
        mGridview.setAdapter(adapter);
        getImagePath();

        // SwipeRefreshLayoutの設定
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.yellow);
    }

    private void getImagePath() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    String url = HOME_URL+"/api/facebook_photo/get?page="+page;
                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    String str = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                    JSONObject json = new JSONObject(str);
                    JSONArray sources = new JSONArray(json.getString("photo_sources"));
                    if (isReload){
                        imgList.clear();
                        isReload = false;
                    }
                    if (sources.length() < 25){
                        isReadAll = true;
                    }
                    for (int i=0; i < sources.length();i++){
                        imgList.add((String) sources.get(i));
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            adapter.notifyDataSetChanged();
                            mSwipeRefreshLayout.setRefreshing(false);
                            isLoading = false;
                            if (mProgressDialog != null) {
                                mProgressDialog.hide();
                            }
                        }
                    });

                } catch(Exception ex) {
                    System.out.println(ex);
                }
            }
        }).start();
    }

    public void onClickButtonBack(View view) {
            FacebookPhotoActivity.this.finish();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

        this.currentFirstVisibleItem = firstVisibleItem;
        this.currentVisibleItemCount = visibleItemCount;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        this.currentScrollState = scrollState;
        this.isScrollCompleted();


    }

    private void isScrollCompleted() {
        final int lastItem = currentFirstVisibleItem + currentVisibleItemCount;

        if (this.currentScrollState == SCROLL_STATE_IDLE && lastItem == imgList.size()) {
            if (!isLoading && !isReadAll) {
                if (mProgressDialog == null){
                    mProgressDialog = new ProgressDialog(this);
                }
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage("読み込み中");
                mProgressDialog.setCancelable(true);
                mProgressDialog.show();

                isLoading = true;
                page++;
                getImagePath();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String url = imgList.get(position);
        // インテントのインスタンス生成
        Intent intent = new Intent(FacebookPhotoActivity.this, ImageViewerActivity.class);
        intent.putExtra("image_url", url);
        intent.putExtra("plan", mPlan);
        // サブ画面の呼び出し
        startActivity(intent);
    }

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            isReload = true;
            isReadAll = false;
            page = 1;
            getImagePath();
        }
    };

    class ViewHolder {
        ImageView imageView;
    }

    // BaseAdapter を継承した FacebookAdapter クラスのインスタンス生成
    class FacebookAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private int layoutId;
        private Context adapterContent;
        public FacebookAdapter(Context context, int layoutId, List<String> imgList) {
            super();
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.layoutId = layoutId;
            adapterContent = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null)
            {
                // main.xml の <GridView .../> に grid_items.xml を inflate して convertView とする
                convertView = inflater.inflate(layoutId, parent, false);
                // ViewHolder を生成
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(imageView);
                WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
                Display disp = wm.getDefaultDisplay();
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int)(Double.valueOf(disp.getWidth()) / 2.0), (int)(Double.valueOf(disp.getWidth()) / 2.0));
                holder.imageView.setLayoutParams(layoutParams);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            Glide.with(adapterContent).load(imgList.get(position)).into(holder.imageView);
            return convertView;
        }

        @Override
        public int getCount() {
            // List<String> imgList の全要素数を返す
            return imgList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }

}


