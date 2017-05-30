package com.appvisor_event.master;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import static com.appvisor_event.master.Constants.LOGGED_IN_STATUS_KEY;
import static com.appvisor_event.master.Constants.USED_LOGIN;

public class SplashActivity extends AppActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // タイトルを非表示にします。
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // splash.xmlをViewに指定します。
        setContentView(R.layout.splash);
        Handler hdl = new Handler();
        // 500ms遅延させてsplashHandlerを実行します。
        hdl.postDelayed(new splashHandler(), 1000);
    }
    class splashHandler implements Runnable {
        public void run() {
            if (Constants.USED_LOGIN){
                if (isLoggedInStatus()){
                    moveToMainActivity();
                }else {
                    // ログイン機能を使用していて、かつログイン状態でない場合のみログイン画面を表示する
                    moveToLoginActivity();
                }
            }else{
                moveToMainActivity();
            }

        }

        private void moveToMainActivity(){
            // スプラッシュ完了後に実行するActivityを指定します。
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
            // SplashActivityを終了させます。
            SplashActivity.this.finish();
        }

        private void moveToLoginActivity(){
            // スプラッシュ完了後に実行するActivityを指定します。
            Intent intent = new Intent(getApplication(), LoginActivity.class);
            startActivity(intent);
            // SplashActivityを終了させます。
            SplashActivity.this.finish();
        }

        private boolean isLoggedInStatus(){
            SharedPreferences prefs = getSharedPreferences(Constants.LOGGED_IN_STATUS_SP_KEY, Context.MODE_PRIVATE);
            String status = prefs.getString(Constants.LOGGED_IN_STATUS_KEY, "");
            if (status.equals(Constants.LOGGED_IN_YES)){
                return true;
            }else{
                return false;
            }
        }
    }
}