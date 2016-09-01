package com.appvisor_event.master;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

/**
 * Created by wangyuanshi on 2016/08/30.
 */
public class ARTutorialDialog extends Dialog {

    public ARTutorialDialog(Context context, final OnARTutorialDialogListener onARTutorialDialogListener) {
        super(context, R.style.CustomDialog);
        setContentView(R.layout.dialog_ar_tutorial);
//        findViewById(R.id.img_vr_tutorial).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(onARTutorialDialogListener != null){
//                    onARTutorialDialogListener.onCancel();
//                }
//                dismiss();
//            }
//        });
//        findViewById(R.id.relative_ar_tutorial_root_view).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(onARTutorialDialogListener != null){
//                    onARTutorialDialogListener.onCancel();
//                }
//                dismiss();
//            }
//        });
        findViewById(R.id.ar_guide_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onARTutorialDialogListener != null){
                    onARTutorialDialogListener.onCancel();
                }
                dismiss();
            }
        });
    }

    public interface OnARTutorialDialogListener{
        void onCancel();
    }

}
