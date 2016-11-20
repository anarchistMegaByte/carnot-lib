package com.carnot.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.activeandroid.ActiveAndroid;
import com.carnot.App;
import com.carnot.R;
import com.carnot.dialogs.DialogUtil;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.CompoundDrawableClickListener;
import com.carnot.models.Cars;
import com.carnot.models.User;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pankaj on 31/3/16.
 * Activity to perform Sign In
 */
public class ActivitySignin extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "TAG";
    public static final int RC_SIGN_IN = 100;
    ProgressBar progressBar;
    EditText edtEmail, edtPassword;
    Button btnSignIn;

    ProgressDialog progressDialog;

    FrameLayout frmGoogleLogin, frmFacebookLogin;
    //    LoginButton loginButton;
//    CallbackManager callbackManager;
//    SignInButton signInButton;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        int i = 5 / 0;
        setContentView(R.layout.activity_signin);
//        initFacebookLogin();
//        initGooglePlusLogin();
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle(R.string.lbl_signin);
    }

    /**
     * Start Dashboard activity with clearing all the task
     */
    private void startDashboardActivity() {
        Intent intent = new Intent(mActivity, ActivityDashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void initView() {
        edtEmail = (EditText) links(R.id.edt_email);
        progressBar = (ProgressBar) links(R.id.progress_bar);
        edtPassword = (EditText) links(R.id.edt_password);
        btnSignIn = (Button) links(R.id.btn_sign_in);
        frmFacebookLogin = (FrameLayout) links(R.id.frm_facebook_login);
        frmGoogleLogin = (FrameLayout) links(R.id.frm_google_login);

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void postInitView() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void addAdapter() {
        edtPassword.setOnTouchListener(new CompoundDrawableClickListener(edtPassword) {
            @Override
            public boolean onRightDrawableClickListener() {
                Intent intent = new Intent(mActivity, ActivityForgotPassword.class);
                startActivity(intent);
                return true;
            }
        });
        btnSignIn.setOnClickListener(clickListener);
        frmFacebookLogin.setOnClickListener(clickListener);
        frmGoogleLogin.setOnClickListener(clickListener);

        if (ConstantCode.DEBUG) {
            edtEmail.setText("prasanna.rawke@carnot.co.in");
            edtPassword.setText("welcome1");
        }
    }

    @Override
    public void loadData() {

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
                showToast("Thanks for login with facebook " + object.optString("name"));
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

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
                if (v.getId()== R.id.btn_sign_in){
                    Utility.hideKeyboard(mActivity, edtPassword);
                    Utility.hideKeyboard(mActivity, edtEmail);
                    if (validate()) {

                        //Requesting webservice to validate email/mobile and password
                        progressDialog = DialogUtil.ProgressDialog(ActivitySignin.this, "Refuelling your car");
                        HashMap<String, Object> params = new HashMap<>();
                        params.put(ConstantCode.email, edtEmail.getText().toString().trim());
                        params.put(ConstantCode.password, edtPassword.getText().toString().trim());
                        WebUtils.call(WebServiceConfig.WebService.LOGIN, null, params, new NetworkCallbacks() {
                            @Override
                            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                                super.successWithString(values, webService);

                                final JSONObject json = (JSONObject) values;

                                User user = (User) Utility.parseFromString(json.optString(ConstantCode.data), User.class);
                                //Saving information in preference and in database
                                Utility.setLoggedInUser(user);
                                user.save();
                                new AsyncTask<Void,Void,Void>(){
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        JSONObject jsonData = json.optJSONObject(ConstantCode.data);
                                        ArrayList<Cars> listCars = Utility.parseArrayFromString(jsonData.optString(ConstantCode.cars), Cars[].class);
                                        if (listCars != null && listCars.size() > 0) {
                                            for (Cars listCar : listCars) {
                                                listCar.userId = Utility.getLoggedInUser().id;
                                                Log.e(TAG, listCar.name + "------------");
                                                listCar.updateDeviceKeysOnlyAtSignIn();
                                            }
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        super.onPostExecute(aVoid);
                                        progressDialog.dismiss();
                                        startDashboardActivity();
                                    }
                                }.execute();
                                //[START- Saving car information specially for fuel type for syncing process]


                                //[END - Saving car information specially for fuel type for syncing process]

                                //starting dashboard Activity

                            }

                            @Override
                            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                                super.failedWithMessage(values, webService);
                                progressDialog.dismiss();
//                                showToast("failed login " + values.toString());
                                showSnackbar("" + values.toString(), MESSAGE_TYPE_ERROR);
                            }

                            @Override
                            public void loadOffline() {
                                super.loadOffline();
                                progressDialog.dismiss();
                                showSnackbar(getString(R.string.msg_internet_alert), MESSAGE_TYPE_ERROR);
                            }
                        });
                    }}

               /* case R.id.frm_facebook_login:
                    AccessToken accessToken = AccessToken.getCurrentAccessToken();
                    if (accessToken == null) {
                        loginButton.performClick();
                    } else {
                        startDashboardActivity();
                    }

                    break;
                case R.id.frm_google_login:
                    *//*LoginManager.getInstance().logOut();
                    showToast("Signout from Facebook");*//*
                    googlePlusSignIn();
                    break;*/
        }
    };

    /**
     * Validating all the fields
     *
     * @return
     */
    private boolean validate() {
        if (Utility.isEmpty(edtEmail)) {
            showSnackbar(getString(R.string.msg_mendatory_email), MESSAGE_TYPE_ERROR);
            return false;
        } else if (Utility.isEmpty(edtPassword)) {
            showSnackbar(getString(R.string.msg_mendatory_password), MESSAGE_TYPE_ERROR);
            return false;
        } else if (!Utility.isValidMobile(edtEmail.getText().toString().trim()) && !Utility.isValidEmail(edtEmail.getText().toString().trim())) {
            showSnackbar(getString(R.string.msg_invalid_username_password), MESSAGE_TYPE_ERROR);
            return false;
        }
        //Checking the length condition on password
        else if (edtPassword.getText().toString().trim().length() < 8 || edtPassword.getText().toString().trim().length() > 20) {
            showSnackbar(getString(R.string.msg_invalid_username_password), MESSAGE_TYPE_ERROR);
            return false;
        }
        return true;
    }

    /*private void googlePlusSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }*/


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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progressDialog != null)
            progressDialog.dismiss();
    }
}
