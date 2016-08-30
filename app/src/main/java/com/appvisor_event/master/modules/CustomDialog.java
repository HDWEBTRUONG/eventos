package com.appvisor_event.master;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by wangyuanshi on 2016/08/29.
 */
public class CustomDialog implements View.OnClickListener{

    private Dialog mDialog;
    private OnCustomDialogClickListener mOnCustomDialogClickListener;
    public static final int LOAD_URL_VIA_WEBVIEW = 1;
    public static final int LOAD_URL_VIA_BROWSER = 2;

    public CustomDialog(Context context, String title, String content,
                        String buttonContent, final String url, final int type,
                        final OnCustomDialogClickListener onCustomDialogClickListener) {

        this.mOnCustomDialogClickListener = onCustomDialogClickListener;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDialog = new Dialog(context, R.style.CustomDialog);
        mDialog.setContentView(R.layout.dialog_custom);
        mDialog.findViewById(R.id.img_close).setOnClickListener(this);
        mDialog.findViewById(R.id.relative_custom_dialog_root).setOnClickListener(this);
        ((TextView)mDialog.findViewById(R.id.txt_custom_dialog_title)).setText(title);
        ((TextView)mDialog.findViewById(R.id.btn_custom_dialog_content)).setText(content);
        Button button = (Button) mDialog.findViewById(R.id.btn_custom_dialog);

        RelativeLayout relativeContent = (RelativeLayout) mDialog.findViewById(R.id.relative_custom_dialog_content);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) relativeContent.getLayoutParams();
        int marginTopBottomSize = (int) (displayMetrics.heightPixels * 0.15);
        int marginLeftRightSize = (int) (displayMetrics.widthPixels * 0.01);
        params.setMargins(marginLeftRightSize, marginTopBottomSize, marginLeftRightSize, marginTopBottomSize);

        if (buttonContent != null){
            button.setVisibility(View.VISIBLE);
            button.setText(buttonContent);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onCustomDialogClickListener != null){
                        onCustomDialogClickListener.onButtonClick();
                    }
//                    if (type == LOAD_URL_VIA_WEBVIEW){
//                        loadUrlViaWebView(url);
//                    }else if (type == LOAD_URL_VIA_BROWSER){
//                        loadUrlViaBrowser(url);
//                    }
                }
            });
        }else{
            button.setVisibility(View.GONE);
        }

    }

    public void show(){
        mDialog.show();
    }

//    private void loadUrlViaBrowser(String url) {
//        Log.i("test",url);
//    }
//
//
//    private void loadUrlViaWebView(String url) {
//        Log.i("tes1t",url);
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_close:
                mDialog.dismiss();
                if (mOnCustomDialogClickListener != null){
                    mOnCustomDialogClickListener.onCancelClick();
                }
                break;
            case R.id.relative_custom_dialog_root:
                mDialog.dismiss();
                break;
        }
    }


    public interface OnCustomDialogClickListener{
        void onCancelClick();
        void onButtonClick();
    }

    public static class Builder{

        private Context context;
        private String mTitle;
        private String mContent;
        private String mBtnContent;
        private String mBtnUrl;
        private int mLoadUrlType;
        private OnCustomDialogClickListener mOnCustomDialogClickListener;

        public Builder(Context context){
            this.context = context;
        }

        public Builder setTitle(String title){
            this.mTitle = title;
            return this;
        }

        public Builder setContent(String content){
            this.mContent = content;
            return this;
        }

        public Builder setButtonContent(String buttonContent, String url, int type){
            this.mBtnContent = buttonContent;
            this.mBtnUrl = url;
            this.mLoadUrlType = type;
            return this;
        }

        public Builder setOnCustomDialogClickListener(OnCustomDialogClickListener onCustomDialogClickListener){
            this.mOnCustomDialogClickListener = onCustomDialogClickListener;
            return this;
        }

        public CustomDialog build(){
            return new CustomDialog(context, mTitle, mContent,
                    mBtnContent, mBtnUrl, mLoadUrlType, mOnCustomDialogClickListener);
        }

    }
}
