package com.carnot.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.dialogs.DialogUtil;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.EasyPermissions;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by pankaj on 31/3/16.
 * Activity used to show forgot password options and to change it
 */
public class ActivityForgotPassword extends BaseActivity {

    EditText edtMobile;
    Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle(R.string.lbl_forgot_password);

        Log.d("SMS", "Registering receiver");
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(receiver, new IntentFilter(ConstantCode.BROADCAST_MESSAGE_RECEIVE));
        isRegistered = true;
    }

    @Override
    public void initView() {
        edtMobile = (EditText) links(R.id.edt_mobile);
        btnSend = (Button) links(R.id.btn_send);
    }

    @Override
    public void postInitView() {

    }

    @Override
    public void addAdapter() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    forgotPassword();
                }
            }
        });
    }

    private boolean validate() {
        if (Utility.isEmpty(edtMobile)) {
            showSnackbar(getString(R.string.msg_mendatory_mobile), MESSAGE_TYPE_ERROR);
            return false;
        } else if (edtMobile.getText().toString().trim().length() != 10) {
            showSnackbar(getString(R.string.msg_mobile_length), MESSAGE_TYPE_ERROR);
            return false;
        }
        return true;
    }


    @Override
    public void loadData() {

    }

    private void forgotPassword() {

        final ProgressDialog progressDialog = DialogUtil.ProgressDialog(mActivity, "Please wait");

        WebUtils.call(WebServiceConfig.WebService.FORGOT_PASSWORD, new String[]{edtMobile.getText().toString().trim()}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                progressDialog.dismiss();

                JSONObject jsonObject = (JSONObject) values;
                JSONObject jsonData = jsonObject.optJSONObject(ConstantCode.data);
                String otp = jsonData.optString(ConstantCode.otp);
                showOTPVerifyDialog(otp);
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);

                progressDialog.dismiss();
//                showSnackbar("" +
                if (values.toString().contains("Code already sent to user")) {
                    showOTPVerifyDialog("123456");
                } else {
                    showToast("" + values.toString());
                }
            }

            @Override
            public void loadOffline() {
                super.loadOffline();
                showSnackbar(getString(R.string.msg_internet_alert), MESSAGE_TYPE_ERROR);
            }
        });
    }

    private void changePassword(String newPassword) {
        final ProgressDialog progressDialog = DialogUtil.ProgressDialog(mActivity, "Please wait");
        HashMap<String, Object> params = new HashMap<>();
        params.put(ConstantCode.pwd, newPassword);

        WebUtils.call(WebServiceConfig.WebService.CHANGE_PASSWORD, new String[]{edtMobile.getText().toString().trim()}, params, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                progressDialog.dismiss();
                showSnackbar(getString(R.string.msg_password_changed_successfully), MESSAGE_TYPE_SUCCESS);
                if (dialogVerify != null && dialogVerify.isShowing()) {
                    dialogVerify.dismiss();
                }
                if (dialogChangePassword != null && dialogChangePassword.isShowing())
                    dialogChangePassword.dismiss();
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
                progressDialog.dismiss();
                showToast("" + values.toString());
            }

            @Override
            public void loadOffline() {
                super.loadOffline();
                showSnackbar(getString(R.string.msg_internet_alert), MESSAGE_TYPE_ERROR);
            }
        });
    }

    boolean isRegistered = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRegistered) {
            Log.d("SMS", "UnRegistering receiver");
            LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(receiver);
            isRegistered = false;
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SMS", "onReceive ActivitySignup() called with: " + "context = [" + context + "], intent = [" + intent + "]");
            if (intent.hasExtra(ConstantCode.OTP)) {
                if (edtOtp != null)
                    edtOtp.setText(intent.getStringExtra(ConstantCode.OTP));
            }
        }
    };

    AlertDialog dialogVerify;
    TextView txtErroMsg;
    EditText edtOtp;

    public void showOTPVerifyDialog(final String otp) {

        requestForSMSReadPermission();

        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);

        View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_verify_otp, null);
        edtOtp = (EditText) view.findViewById(R.id.edt_otp);
        txtErroMsg = (TextView) view.findViewById(R.id.txt_error_msg);
        final Button btnVerify = (Button) view.findViewById(R.id.btn_verify);
        final ImageView imgBack = (ImageView) view.findViewById(R.id.img_back);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogVerify.dismiss();
            }
        });
        edtOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                txtErroMsg.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utility.isEmpty(edtOtp)) {

                    //Here we are not going to server to check the OTP because otp comes from send
//                    verifyOtp(edtOtp.getText().toString().trim());

                    if (edtOtp.getText().toString().trim().equalsIgnoreCase(otp)) {
                        showChangePasswordDialog(edtOtp.getText().toString().trim());
                    } else {
                        txtErroMsg.setText(getString(R.string.lbl_incorrect_code_try_again));
                        txtErroMsg.setVisibility(View.VISIBLE);
                    }
                } else {
                    edtOtp.requestFocus();
                    txtErroMsg.setText(getString(R.string.msg_otp_code_blank));
                    txtErroMsg.setVisibility(View.VISIBLE);
                }
            }
        });
        builder.setView(view);
        dialogVerify = builder.create();
        dialogVerify.setCanceledOnTouchOutside(false);
        dialogVerify.setCancelable(false);
        dialogVerify.show();
    }

    private AlertDialog dialogChangePassword;
    private EditText edtPassword;
    private TextView txtErroMsgChangePassword;

    public void showChangePasswordDialog(final String otp) {

//        requestForSMSReadPermission();

        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);

        View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_change_password, null);
        edtPassword = (EditText) view.findViewById(R.id.edt_password);
        txtErroMsgChangePassword = (TextView) view.findViewById(R.id.txt_error_msg_change_password);
        final Button btnChangePassword = (Button) view.findViewById(R.id.btn_change_password);
        final ImageView imgBack = (ImageView) view.findViewById(R.id.img_back);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogChangePassword.dismiss();
            }
        });
        edtOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                txtErroMsgChangePassword.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utility.isEmpty(edtPassword)) {
                    txtErroMsg.setText(getString(R.string.msg_mendatory_password));
                    txtErroMsg.setVisibility(View.VISIBLE);
                } else if (edtPassword.getText().toString().trim().length() < 8 || edtPassword.getText().toString().trim().length() > 20) {
                    txtErroMsg.setText(getString(R.string.msg_password_length));
                    txtErroMsg.setVisibility(View.VISIBLE);
                } else {
                    changePassword(edtPassword.getText().toString().trim());
                    txtErroMsg.setVisibility(View.GONE);
                }
            }
        });
        builder.setView(view);
        dialogChangePassword = builder.create();
        dialogChangePassword.setCanceledOnTouchOutside(false);
        dialogChangePassword.setCancelable(false);
        dialogChangePassword.show();
    }


    private void requestForSMSReadPermission() {
        EasyPermissions.requestPermissions(ActivityForgotPassword.this, new EasyPermissions.PermissionCallbacks() {
            @Override
            public void onPermissionsGranted(int requestCode, List<String> perms) {

            }

            @Override
            public void onPermissionsDenied(int requestCode, List<String> perms) {

            }

            @Override
            public void onPermissionsPermanentlyDeclined(int requestCode, List<String> perms) {

            }
        }, "", 106, Manifest.permission.RECEIVE_SMS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
