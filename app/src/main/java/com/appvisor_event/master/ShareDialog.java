package com.appvisor_event.master;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appvisor_event.master.modules.AppLanguage.AppLanguage;

/**
 * Created by BraveSoft on 16/10/21.
 */
public class ShareDialog  {
    private Context mContext;
    private MyDialog dialog;
    private TextView tv1,tv2,tv3,tv4,tv_certain;
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
        tv1= (TextView) dialog.findViewById(R.id.tv_01);
        tv2= (TextView) dialog.findViewById(R.id.tv_02);
        tv3= (TextView) dialog.findViewById(R.id.tv_03);
        tv4= (TextView) dialog.findViewById(R.id.tv_04);
        tv_certain= (TextView) dialog.findViewById(R.id.share_certain);
        if (AppLanguage.getLanguageWithStringValue(context).equals("ja")) {
            tv1.setText(context.getResources().getText(R.string.share_dialog_text_01_jp));
            tv2.setText(context.getResources().getText(R.string.share_dialog_text_02_jp));
            tv3.setText(context.getResources().getText(R.string.share_dialog_text_03_jp));
            tv4.setText(context.getResources().getText(R.string.share_dialog_text_04_jp));
            tv_certain.setText(context.getResources().getText(R.string.share_dialog_close_jp));
        } else {
            tv1.setText(context.getResources().getText(R.string.share_dialog_text_01_en));
            tv2.setText(context.getResources().getText(R.string.share_dialog_text_02_en));
            tv3.setText(context.getResources().getText(R.string.share_dialog_text_03_en));
            tv4.setText(context.getResources().getText(R.string.share_dialog_text_04_en));
            tv_certain.setText(context.getResources().getText(R.string.share_dialog_close_en));
        }
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
