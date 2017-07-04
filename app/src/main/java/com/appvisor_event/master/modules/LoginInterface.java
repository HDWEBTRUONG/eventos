package com.appvisor_event.master.modules;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.appvisor_event.master.LoginActivity;

/**
 * Created by ookuma on 2017/05/29.
 */

public class LoginInterface {

    Context context;

    public LoginInterface(Context context){
        this.context = context;
    }

    @JavascriptInterface
    public void loggedIn(){
        LoginActivity loginA = new LoginActivity();
        loginA.loggedIn(context);
    }
}
