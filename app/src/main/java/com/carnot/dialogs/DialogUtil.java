package com.carnot.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.carnot.R;

/**
 * Created by javid on 7/4/16.
 */
public class DialogUtil {

    public static void ConfirmDialog(Context mContext, String title, String message, String lbl_positive, String lbl_nagative, DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }


        builder.setMessage(message + "");
        if (TextUtils.isEmpty(lbl_positive)) {
            builder.setPositiveButton(mContext.getString(R.string.lbl_ok), okListener == null ? null : okListener);//second parameter used for onclicklistener
        } else {
            builder.setPositiveButton(lbl_positive, null);//second parameter used for onclicklistener
        }

        if (TextUtils.isEmpty(lbl_nagative)) {
            builder.setNegativeButton(mContext.getString(R.string.lbl_cancel), okListener == null ? null : okListener);//second parameter used for onclicklistener
        } else {
            builder.setNegativeButton(lbl_nagative, null);//second parameter used for onclicklistener
        }
        builder.show();

    }

    public static ProgressDialog ProgressDialog(Context mContext, String message) {
        ProgressDialog progressDialog = new ProgressDialog(mContext, R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage(message);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        return progressDialog;
    }
}
