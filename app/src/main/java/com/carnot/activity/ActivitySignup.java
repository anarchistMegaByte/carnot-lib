package com.carnot.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.carnot.R;
import com.carnot.dialogs.DialogUtil;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.EasyPermissions;
import com.carnot.models.User;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Activity to add new Registration
 */

/**
 * Created by pankaj on 31/3/16.
 */
public class ActivitySignup extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String privacyLink = "http://www.carnot.co.in/legal/";

    ProgressBar progressBar;
    public static final String TAG = "TAG";
    public static final int RC_SIGN_IN = 100;
    TextView txtSignIn;
    EditText edtEmail, edtMobile, edtPassword, edtName;
    Button btnSignUp;
    FrameLayout frmGoogleLogin, frmFacebookLogin;
    CheckBox cb_box;
    com.carnot.custom_views.AppTextViewMedium TC;
    //    LoginButton loginButton;
//    CallbackManager callbackManager;
//    SignInButton signInButton;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, false);
        setTitle(R.string.lbl_signup);

        //Registering MESSAGE RECEVIER BROADCAST so we can Auto-Read OTP
        Log.d("SMS", "Registering receiver");
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(receiver, new IntentFilter(ConstantCode.BROADCAST_MESSAGE_RECEIVE));
        isRegistered = true;

//        initFacebookLogin();
//        initGooglePlusLogin();

        initGCM();
    }

    BroadcastReceiver mRegistrationBroadcastReceiver;

    private void initGCM() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(ConstantCode.SENT_TOKEN_TO_SERVER, false);
            }
        };

    }

    @Override
    public void initView() {
        edtEmail = (EditText) links(R.id.edt_email);
        edtMobile = (EditText) links(R.id.edt_mobile);
        edtPassword = (EditText) links(R.id.edt_password);
        edtName = (EditText) links(R.id.edt_name);
        progressBar = (ProgressBar) links(R.id.progress_bar);
        txtSignIn = (TextView) links(R.id.txt_sign_in);
        btnSignUp = (Button) links(R.id.btn_sign_up);

        frmFacebookLogin = (FrameLayout) links(R.id.frm_facebook_login);
        frmGoogleLogin = (FrameLayout) links(R.id.frm_google_login);

        progressBar.setVisibility(View.GONE);

        cb_box = (CheckBox) links(R.id.cb_box);
        TC = (com.carnot.custom_views.AppTextViewMedium) links(R.id.TAndC);

    }

    /*private void initFacebookLogin() {
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) links(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                getProfileData();
            }

            @Override
            public void onCancel() {
                // App code
                showToast("Facebook login cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                showToast("Facebook login onerror");
            }
        });
    }


    private void initGooglePlusLogin() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
//                .enableAutoManage(this *//* FragmentActivity *//*, this *//* OnConnectionFailedListener *//*)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


//        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
//        signInButton.setSize(SignInButton.SIZE_STANDARD);
//        signInButton.setScopes(gso.getScopeArray());
//        signInButton.setOnClickListener(clickListener);

    }

    private void getProfileData() {
        progressBar.setVisibility(View.VISIBLE);
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                progressBar.setVisibility(View.GONE);
                Utility.showLog(ActivitySignup.class, object.toString());
//                showToast("Thanks for login with facebook " + object.optString("name"));
                User user = (User) Utility.parseFromString(ConstantCode.DEFAULT_USER_JSON, User.class);
                Utility.setLoggedInUser(user);
                startDashboardActivity();
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }*/

    @Override
    public void postInitView() {

    }


    private SpannableStringBuilder addClickablePart(String str) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(str);

        int idx1 = str.indexOf("[");
        int idx2 = str.indexOf("]");
        /*
        int idx1 = str.indexOf("[");
        int idx2 = 0;
        while (idx1 != -1) {
            idx2 = str.indexOf("]", idx1) + 1;
            final String clickString = str.substring(idx1+1, idx2-1);
            ssb.setSpan(new ClickableSpan() {

                @Override
                public void onClick(View widget) {
                    Toast.makeText(widget.getContext(), clickString,
                            Toast.LENGTH_SHORT).show();
                }
            }, idx1, idx2, 0);
            idx1 = str.indexOf("[", idx2);
        }
        */
        final String clickString = str.substring(idx1+1, idx2-1);
        ssb.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyLink));
                startActivity(browserIntent);
            }
        }, idx1, idx2, 0);

        idx1 = str.indexOf("{");
        idx2 = str.indexOf("}");

        final String clickString1 = str.substring(idx1+1, idx2-1);
        ssb.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyLink));
                startActivity(browserIntent);
            }
        }, idx1, idx2, 0);

        ssb.delete(str.indexOf("["),str.indexOf("[")+1);
        ssb.delete(str.indexOf("]")-1,str.indexOf("]"));

        ssb.delete(str.indexOf("{"),str.indexOf("{")+1);
        ssb.delete(str.indexOf("}")-1,str.indexOf("}"));

        return ssb;
    }


    @Override
    public void addAdapter() {
        txtSignIn.setOnClickListener(clickListener);
        btnSignUp.setOnClickListener(clickListener);
        frmFacebookLogin.setOnClickListener(clickListener);
        frmGoogleLogin.setOnClickListener(clickListener);

        String myString = mActivity.getString(R.string.terms_and_condition);
        TC.setMovementMethod(LinkMovementMethod.getInstance());
        TC.setText(addClickablePart(myString), TextView.BufferType.SPANNABLE);
        /*
        int i1 = myString.indexOf("[");
        int i2 = myString.indexOf("e\"");

        int i3 = myString.indexOf("\"P");
        int i4 = myString.indexOf("y\"");
        //cb_box.setText("I have read and agree to abide by the \"Terms and Conditions\" of the product");

        TC.setText(myString, TextView.BufferType.SPANNABLE);
        Spannable mySpannable = (Spannable)TC.getText();
        ClickableSpan myClickableSpan = new ClickableSpan()
        {
            @Override
            public void onClick(View widget) {
                widget.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.carnot.co.in"));
                        startActivity(browserIntent);
                    }
                });
            }
        };
        mySpannable.setSpan(myClickableSpan, i1+1, i2 + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mySpannable.setSpan(myClickableSpan, i1+1, i4+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TC.setMovementMethod(LinkMovementMethod.getInstance());
        */
    }

    @Override
    public void loadData() {

    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
                if (v.getId() ==  R.id.txt_sign_in){
                    Utility.hideKeyboard(mActivity);
                    intent = new Intent(mActivity, ActivitySignin.class);
                    startActivity(intent);}
                else if (v.getId() ==  R.id.btn_sign_up){
                    Utility.hideKeyboard(mActivity);
                    if (validate()) {
                        showOTPConfirmDialog();
//                        showOTPVerifyDialog();
                    }}

                    /*DateHelper dateHelper = new DateHelper();

                    String fromDate = "17/4/2016 01:01:00";
                    String fromFormat = DateHelper.DATE + "/" + DateHelper.MONTH_NUMBER + "/" + DateHelper.YEAR_4_DIGIT + " " + DateHelper.HOUR_12_HOUR_2_DIGIT + ":" + DateHelper.MINUTE + ":" + DateHelper.SECONDS;
                    String toFormat = fromFormat;
                    String fromTimeZone = "Asia/Kolkata";
                    String toTimeZone = "Atlantic/Bermuda";
//                    String time=dateHelper.getFormatedDate(fromDate, fromFormat, toFormat, fromTimeZone, toTimeZone);
                    String time = dateHelper.getFormatedDate(fromDate, fromFormat, toFormat, true, true);
                    String a = "";*/
                /*case R.id.frm_facebook_login:
                    AccessToken accessToken = AccessToken.getCurrentAccessToken();
                    if (accessToken == null)
                        loginButton.performClick();
                    else {
                        startDashboardActivity();
                    }
                    break;*/
//                case R.id.frm_google_login:
                    /*LoginManager.getInstance().logOut();
                    showToast("Signout from Facebook");*/
//                    googlePlusSignIn();
//                    break;

//                case R.id.sign_in_button:
//                    break;
        }
    };

    User user;

    /**
     * Creating new signup request.
     */
    private void signUp() {
        final ProgressDialog progressDialog = DialogUtil.ProgressDialog(mActivity, "Please wait");
        HashMap<String, Object> params = new HashMap<>();
        params.put(ConstantCode.phone, edtMobile.getText().toString().trim());
        if (!Utility.isEmpty(edtEmail))
            params.put(ConstantCode.email, edtEmail.getText().toString().trim());
        params.put(ConstantCode.password, edtPassword.getText().toString().trim());
        params.put(ConstantCode.name, edtName.getText().toString().trim());
        WebUtils.call(WebServiceConfig.WebService.REGISTER, null, params, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                progressDialog.dismiss();
                if (dialogVerify.isShowing())
                    dialogVerify.dismiss();
                JSONObject json = (JSONObject) values;

                user = (User) Utility.parseFromString(json.optString(ConstantCode.data), User.class);

                //saving it in pref
                Utility.setLoggedInUser(user);
                user.save();
                startDashboardActivity();
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
                progressDialog.dismiss();
                showSnackbar("" + values.toString(), MESSAGE_TYPE_ERROR);
            }

            @Override
            public void loadOffline() {
                super.loadOffline();
                progressDialog.dismiss();
                showSnackbar(getString(R.string.msg_internet_alert), MESSAGE_TYPE_ERROR);
            }
        });
    }

    /**
     * Starting dashboard activity with INTENT_ACTION as ACTION_REGISTER_CAR so that we can open add new car dialog
     */
    private void startDashboardActivity() {
        Intent intent = new Intent(mActivity, ActivityDashboard.class);
        intent.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_REGISTER_CAR);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /*private void googlePlusSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }*/

    /**
     * Method to validate all the fields if some thing is wrong then it return false else true.
     *
     * @return
     */
    private boolean validate() {
        if (Utility.isEmpty(edtName)) {
            showSnackbar(getString(R.string.msg_mendatory_name), MESSAGE_TYPE_ERROR);
//            edtName.setError("It is mendatory");
            return false;
        } else if (Utility.isEmpty(edtMobile)) {
            showSnackbar(getString(R.string.msg_mendatory_mobile), MESSAGE_TYPE_ERROR);
            return false;
        } else if (!Utility.isEmpty(edtEmail) && !Utility.isValidEmail(edtEmail.getText().toString().trim())) {
            showSnackbar(getString(R.string.msg_mendatory_email), MESSAGE_TYPE_ERROR);
            return false;
        } else if (Utility.isEmpty(edtPassword)) {
            showSnackbar(getString(R.string.msg_mendatory_password), MESSAGE_TYPE_ERROR);
            return false;
        }
        //Checking name must contains 3 characters
        else if (!edtName.getText().toString().trim().matches("(.*[a-z]){3}")) {
            showSnackbar(getString(R.string.msg_name_atleast_3_character), MESSAGE_TYPE_ERROR);
            return false;
        } else if (edtMobile.getText().toString().trim().length() != 10) {
            showSnackbar(getString(R.string.msg_mobile_length), MESSAGE_TYPE_ERROR);
            return false;
        }
        //Checking password length
        else if (edtPassword.getText().toString().trim().length() < 8 || edtPassword.getText().toString().trim().length() > 20) {
            showSnackbar(getString(R.string.msg_password_length), MESSAGE_TYPE_ERROR);
            return false;
        }
        //Checking terms and conditions applied.
        else if(!cb_box.isChecked())
        {
            showSnackbar("Please Accept the terms and conditions...", MESSAGE_TYPE_ERROR);
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);

        /*if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }*/
    }

    /*private void handleSignInResult(GoogleSignInResult result) {
        progressBar.setVisibility(View.GONE);
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
//            showToast("Thanks for login with Google Plus [" + acct.getDisplayName() + "," + acct.getEmail() + "]");
            User user = (User) Utility.parseFromString(ConstantCode.DEFAULT_USER_JSON, User.class);
            Utility.setLoggedInUser(user);
            startDashboardActivity();
        } else {
//             Signed out, show unauthenticated UI.
            showToast("Can't Login with your account, May be you are not using Google+");
        }
    }*/

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * It will show the OTP Confirmation Dialog
     */
    public void showOTPConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);

        builder.setTitle(getString(R.string.lbl_confirmation));

        builder.setMessage(getString(R.string.msg_confirm_mobile_no, edtMobile.getText().toString().trim()));

        builder.setPositiveButton(getString(R.string.lbl_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendOtp();
            }
        });

        builder.setNegativeButton(getString(R.string.lbl_edit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                edtMobile.requestFocus();
            }
        });//second parameter used for onclicklistener

        builder.show();

    }

    /**
     * This will call to send the OTP to user mobile number
     */
    private void sendOtp() {
        final ProgressDialog progressDialog = DialogUtil.ProgressDialog(mActivity, "Please wait");
        HashMap<String, Object> params = new HashMap<>();
        params.put(ConstantCode.phone, edtMobile.getText().toString().trim());

        WebUtils.call(WebServiceConfig.WebService.SEND_OTP, null, params, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                progressDialog.dismiss();
//                                showToast("Login success " + values.toString());
                //saving it in pref
//                Utility.setLoggedInUser(user);
//                startDashboardActivity();
//                showOTPConfirmDialog();

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
//        signUp();
    }

    AlertDialog dialogVerify;
    TextView txtErroMsg;
    EditText edtOtp;

    /**
     * This will display the OTP Verification Dialog
     *
     * @param otp
     */
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
                        signUp();
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
        dialogVerify.show();

        /*btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });*/
    }

    boolean isRegistered = false;


    @Override
    protected void onDestroy() {
        //Unregistering message broadcast receiver because this activity is going to destroy
        if (isRegistered) {
            Log.d("SMS", "UnRegistering receiver");
            LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(receiver);
            isRegistered = false;
        }
        super.onDestroy();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SMS", "onReceive ActivitySignup() called with: " + "context = [" + context + "], intent = [" + intent + "]");

            //Checking if it has message contains OTP field and otp Edittext is not null then we will put OTP on EditText
            if (intent.hasExtra(ConstantCode.OTP)) {
                if (edtOtp != null)
                    edtOtp.setText(intent.getStringExtra(ConstantCode.OTP));
            }
        }
    };

    /**
     * Requesting user for RECEIVE_SMS for Auto-Read OTP
     */
    private void requestForSMSReadPermission() {
        EasyPermissions.requestPermissions(ActivitySignup.this, new EasyPermissions.PermissionCallbacks() {
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


    //======================

}
