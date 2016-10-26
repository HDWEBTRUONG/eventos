package com.appvisor_event.master;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by BraveSoft on 16/10/21.
 */
public class ShareDialog  {
    private Context mContext;
    private MyDialog dialog;
    private TextView tv_certain;
    private ImageView iv_twitter,iv_facebook,iv_instagram;
    public static final int SHARE_TWITTER=1;
    public static final int SHARE_FACEBOOK=2;
    public static final int SHARE_INSTAGRAM=3;


    public ShareDialog(Context context){
        this.mContext=context;
        dialog=new MyDialog(context, R.style.MyDialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.share_dialog);
        tv_certain= (TextView) dialog.findViewById(R.id.share_certain);
        tv_certain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        iv_twitter= (ImageView) dialog.findViewById(R.id.share_twitter);
        iv_facebook= (ImageView) dialog.findViewById(R.id.share_facebook);
        iv_instagram= (ImageView) dialog.findViewById(R.id.share_instagram);

        iv_twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mshareLisenter.resultMessage(SHARE_TWITTER);
                dialog.dismiss();
            }
        });
        iv_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mshareLisenter.resultMessage(SHARE_FACEBOOK);
                dialog.dismiss();
            }
        });
        iv_instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mshareLisenter.resultMessage(SHARE_INSTAGRAM);
                dialog.dismiss();
            }
        });
    }

    public interface ShareLisenter{
        void resultMessage(int type);
    }
    public ShareLisenter mshareLisenter;
    public void setOnMessageLisenter(ShareLisenter shareLisenter){
        this.mshareLisenter=shareLisenter;
    }
}
