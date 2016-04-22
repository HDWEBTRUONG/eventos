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

        intent = new Intent(getApplication(), MainActivity.class);
        this.startActivity(intent);
    }

}