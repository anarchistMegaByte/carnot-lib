package com.carnot.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;


/**
 * Created by javid on 7/4/16.
 */
public class CustomDialog extends Dialog {


    public CustomDialog(Context context, int style) {
        super(context, style);
    }


    public void setContentView(int resID, String diaTitle) {
        super.setContentView(resID);
        this.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void setCancelBtnClick(ImageView imgCancel) {
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}