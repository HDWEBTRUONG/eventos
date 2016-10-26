package com.appvisor_event.master;
/*
 * dialog
 */

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

public class MyDialog extends Dialog {
    private Context context;
    private int type = 0;

    public MyDialog(Context context) {
        super(context);
        this.context = context;
    }

    public MyDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public MyDialog(Context context, int theme, int type) {
        super(context, theme);
        this.context = context;
        this.type = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (type != 0) {
            setCanceledOnTouchOutside(true);
            setCancelable(true);
        } else {
            setCanceledOnTouchOutside(false);
            setCancelable(false);
        }
    }
}
