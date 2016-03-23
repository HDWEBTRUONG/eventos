package com.appvisor_event.master;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

public class SplashActivity extends Activity {
    private VideoView video;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        video = (VideoView)findViewById(R.id.videoView);
        // 動画の設定&再生
        String path = "android.resource://" + getPackageName() + "/" + R.raw.splash;
        video.setVideoPath(path);
        // 再生完了通知のリスナーの設定
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp) {
            // TODO Auto-generated method stub
                startActivity(intent);
                SplashActivity.this.finish();
            }
        });
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                intent = new Intent(getApplication(), MainActivity.class);
                video.start();
                new Handler().postDelayed(delayFunc, 500);
            }
        });
    }

    private final Runnable delayFunc= new Runnable() {
        @Override
        public void run() {
            video.setBackgroundColor(00 * 00000000);
        }
    };
}