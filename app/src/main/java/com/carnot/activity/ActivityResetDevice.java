package com.carnot.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.view.View;
import android.widget.Button;

import com.carnot.R;
import com.carnot.dialogs.DialogUtil;
import com.carnot.libclasses.BaseActivity;

/**
 * Created by javid on 31/3/16.
 */
public class ActivityResetDevice extends BaseActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_device);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle(getString(R.string.lbl_reset_device));
    }

    @Override
    public void initView() {


    }

    @Override
    public void postInitView() {

    }

    @Override
    public void addAdapter() {

        ((Button) links(R.id.btn_reset_device)).setOnClickListener(this);
    }

    @Override
    public void loadData() {


    }


    @Override
    public void onClick(View v) {

        DialogUtil.ConfirmDialog(ActivityResetDevice.this, getString(R.string.dialog_title_confirm_changes), getString(R.string.lbl_confirm_dialog_msg), null, null, DialogCallBack);
    }

    DialogInterface.OnClickListener DialogCallBack = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

}
