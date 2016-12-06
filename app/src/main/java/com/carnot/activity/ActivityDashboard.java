package com.carnot.activity;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.carnot.App;
import com.carnot.R;
import com.carnot.custom_views.AutoResizeTextView;
import com.carnot.dialogs.DialogUtil;
import com.carnot.fragment.FragmentCarnot;
import com.carnot.fragment.FragmentCars;
import com.carnot.global.AlarmService;
import com.carnot.global.ConstantCode;
import com.carnot.global.PrefManager;
import com.carnot.global.Utility;
import com.carnot.global.gcm.GCMMessage;
import com.carnot.global.gcm.GCMUtils;
import com.carnot.global.gcm.RegistrationIntentService;
import com.carnot.libclasses.BaseActivity;
import com.carnot.models.MyProfile;
import com.carnot.models.User;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by pankaj on 31/3/16.
 * ActivityDashboard that opens at starting for logged in user contains FragmnetCars, FragmentCarnot Fragments
 */
public class ActivityDashboard extends BaseActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    CustomPagerAdapter pagerAdapter;
    FloatingActionButton fab;
    AppBarLayout appbar;

    FrameLayout introFrameLayout;

    int introScreen = 1;
    ImageView img;
    Button btnNext;
    Toolbar mToolbar;
    GCMUtils gcmUtils;

    View viewFullScreen;
    AutoResizeTextView fullScreenTitle;
    ImageView imgFullScreenClose;
    GCMMessage lastFullScreenMessage;
    GoogleMap fullScreenGoogleMap;
    Marker fullScreenMarker;
    Button fullScreenBtnDismiss;

    private static final String TAG = "ActivityDashboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, false);
        setTitle(R.string.lbl_cars);
        mToolbar = (Toolbar) links(R.id.toolbar);

        //Calling to get gcm device token and sending to server
        initGCM();
        initFullScreenView();

        if (fullScreenHandler == null)
            fullScreenHandler = new Handler();

        //Checking if there is some data in intent then opening full screen notification that comes while clicking on notification.
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();

            //If there is data then show full screen message elese simply schedule the full screen notification after FULL_SCREEN_NOTIFICATION_DELAY_SEC
            if (bundle != null && bundle.containsKey(ConstantCode.INTENT_DATA)) {
                lastFullScreenMessage = (GCMMessage) Utility.parseFromString(bundle.getString(ConstantCode.INTENT_DATA), GCMMessage.class);
                if (lastFullScreenMessage.showInFullScreen) {
                    setValuesInFullScreen(lastFullScreenMessage);
                }
            } else {
                fullScreenHandler.postDelayed(fullScreenRunnable, ConstantCode.FULL_SCREEN_NOTIFICATION_DELAY_SEC);
            }
        } else {
            fullScreenHandler.postDelayed(fullScreenRunnable, ConstantCode.FULL_SCREEN_NOTIFICATION_DELAY_SEC);
        }
    }

    /**
     * Initializing full screen Views
     */
    public void initFullScreenView() {
        initMap();
        gcmUtils = new GCMUtils(mActivity);
        gcmUtils.register(new GCMUtils.NotificationReceive() {
            @Override
            public void onNotificationReceive(GCMMessage message) {
                boolean shouldShowThisMessage = true;

                //Checking if there is some last full screen message then checking the priority if it matches then check for subpriority, and depending on this message checking if whether to show message else no
                if (lastFullScreenMessage != null) {
                    if (lastFullScreenMessage.priority < message.priority && lastFullScreenMessage.dismissCardAppears) {
                        lastFullScreenMessage.dismissMessage();
                        shouldShowThisMessage = true;
                    } else if (lastFullScreenMessage.priority > message.priority) {
                        shouldShowThisMessage = false;
                    } else {
                        if (lastFullScreenMessage.sub_priority < message.sub_priority && lastFullScreenMessage.dismissCardAppears) {
                            lastFullScreenMessage.dismissMessage();
                            shouldShowThisMessage = true;
                        } else if (lastFullScreenMessage.sub_priority > message.sub_priority) {
                            shouldShowThisMessage = false;
                        } else {
                            shouldShowThisMessage = false;
                        }
                    }
                }

                //Checking if we need to show the message and it is of full screen message then we have to show the full screen message
                if (shouldShowThisMessage && message.showInFullScreen) {
                    showFullScreenNotificationWithHighPriority();
                }

                //Updating the notification counter if has the showInNotificationBell flag as true
                if (message.showInNotificationBell) {
                    ((ActivityDashboard) mActivity).updateNotificationCounter();
                }
            }

            @Override
            public void onNotificationDismiss(GCMMessage message) {

                //checking if there is current notification(Which is visible) and the notifcation which comes for dismiss is same then we dismiss the notification and schedule.
                if (lastFullScreenMessage != null && lastFullScreenMessage.notification_id == message.notification_id) {
                    lastFullScreenMessage.dismissMessage();
                    viewFullScreen.setVisibility(View.GONE);
                    lastFullScreenMessage = null;
                    fullScreenHandler.postDelayed(fullScreenRunnable, ConstantCode.FULL_SCREEN_NOTIFICATION_DELAY_SEC);
                }
            }
        });

        fullScreenHandler = new Handler();
        viewFullScreen = links(R.id.view_full_screen);
        fullScreenTitle = (AutoResizeTextView) links(R.id.txt_full_screen_status_title);
        imgFullScreenClose = (ImageView) links(R.id.img_full_screen_close_card);
        fullScreenBtnDismiss = (Button) links(R.id.btnDismiss);
        imgFullScreenClose.setOnClickListener(dismissClickListener);
        fullScreenBtnDismiss.setOnClickListener(dismissClickListener);
        ((CardView) links(R.id.card_view_full_screen_alert)).setMaxCardElevation(0);
    }

    /**
     * call the webservice to dismiss accidental notification
     */
    View.OnClickListener dismissClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (fullScreenBtnDismiss.getVisibility() == View.VISIBLE) {
                final ProgressDialog progressDialog = DialogUtil.ProgressDialog(mActivity, "Please wait");
                HashMap<String, Object> params = new HashMap<>();
                params.put(ConstantCode.id, lastFullScreenMessage.notification_id + "");
                WebUtils.call(WebServiceConfig.WebService.CANCEL_ACCIDENT_NOTIF, null, params, new NetworkCallbacks() {
                    @Override
                    public void successWithString(Object values, WebServiceConfig.WebService webService) {
                        super.successWithString(values, webService);
                        progressDialog.dismiss();
                        if (timer != null)
                            timer.cancel();
                        lastFullScreenMessage.dismissMessage();
                        viewFullScreen.setVisibility(View.GONE);
                        lastFullScreenMessage = null;
                        fullScreenHandler.postDelayed(fullScreenRunnable, ConstantCode.FULL_SCREEN_NOTIFICATION_DELAY_SEC);
                    }

                    @Override
                    public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                        super.failedWithMessage(values, webService);
                        progressDialog.dismiss();
                        if (timer != null)
                            timer.cancel();
                        lastFullScreenMessage.dismissMessage();
                        viewFullScreen.setVisibility(View.GONE);
                        lastFullScreenMessage = null;
                        fullScreenHandler.postDelayed(fullScreenRunnable, ConstantCode.FULL_SCREEN_NOTIFICATION_DELAY_SEC);
                    }

                    @Override
                    public void loadOffline() {
                        super.loadOffline();
                        showSnackbar(getString(R.string.msg_internet_alert), MESSAGE_TYPE_ERROR);
                    }
                });
            } else {
                if (timer != null)
                    timer.cancel();
                if (lastFullScreenMessage != null)
                    lastFullScreenMessage.dismissMessage();
                viewFullScreen.setVisibility(View.GONE);
                lastFullScreenMessage = null;
                fullScreenHandler.postDelayed(fullScreenRunnable, ConstantCode.FULL_SCREEN_NOTIFICATION_DELAY_SEC);
            }

        }
    };

    Handler fullScreenHandler;
    Runnable fullScreenRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewFullScreen != null && mActivity != null)
                showFullScreenNotificationWithHighPriority();
        }
    };

    private void initMap() {
        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.full_screen_google_map);
        if (mSupportMapFragment == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mSupportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.full_screen_google_map, mSupportMapFragment).commit();
        }
        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    if (googleMap != null) {
                        ActivityDashboard.this.fullScreenGoogleMap = googleMap;
                    }
                }
            });
        }
    }

    private void initMap(final double lat, final double lon) {
        if (fullScreenGoogleMap != null) {
            LatLng latLng = new LatLng(lat, lon);
            setMarker(latLng);
            return;
        }

        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.full_screen_google_map);
        if (mSupportMapFragment == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mSupportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.full_screen_google_map, mSupportMapFragment).commit();
        }
        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    if (googleMap != null) {
                        ActivityDashboard.this.fullScreenGoogleMap = googleMap;
                        googleMap.getUiSettings().setAllGesturesEnabled(true);
                        LatLng latLng = new LatLng(lat, lon);
                        setMarker(latLng);
                    }
                }
            });
        }
    }

    private void setMarker(LatLng latLng) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        fullScreenGoogleMap.animateCamera(update);

        if (fullScreenMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            fullScreenMarker = fullScreenGoogleMap.addMarker(markerOptions);
        } else {
            fullScreenMarker.setPosition(latLng);
        }

        View view = LayoutInflater.from(mActivity).inflate(R.layout.car_marker_noti, null, false);

        if (GCMUtils.Crash_Alert.equalsIgnoreCase(lastFullScreenMessage.code)) {
//            view.findViewById(R.id.marker_background).setBackgroundResource(R.color.transparent);
//            view.findViewById(R.id.txt_marker_name).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.marker_background).setBackgroundResource(R.color.transparent);
            view.findViewById(R.id.txt_marker_name).setVisibility(View.GONE);
        }

        view.findViewById(R.id.txt_marker_message).setVisibility(View.GONE);

        fullScreenMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Utility.getBitmap(view, mActivity)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fullScreenHandler != null)
            fullScreenHandler.removeCallbacks(fullScreenRunnable);

        if (gcmUtils != null)
            gcmUtils.unregister();
        if(progressDialog != null)
            progressDialog.dismiss();
    }

    private void showFullScreenNotificationWithHighPriority() {
        if (Utility.getLoggedInUser() != null) {
            lastFullScreenMessage = GCMMessage.getHighPriorityLastMessage();
            setValuesInFullScreen(lastFullScreenMessage);
        }
    }

    /**
     * Setting the values in full screen notification and showing the location.
     *
     * @param lastFullScreenMessage
     */
    private void setValuesInFullScreen(GCMMessage lastFullScreenMessage) {
        if (timer != null)
            timer.cancel();

        if (lastFullScreenMessage != null) {
            viewFullScreen.setVisibility(View.VISIBLE);
            if (fullScreenTitle != null) {
                fullScreenTitle.setText(lastFullScreenMessage.message);
                fullScreenTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
                fullScreenTitle.resetTextSize();
                ((View) fullScreenTitle.getParent()).setBackgroundResource(Utility.getStatusBackground(lastFullScreenMessage.category));
                fullScreenTitle.setTextColor(Utility.getColor(mActivity, Utility.getStatusTextColor(lastFullScreenMessage.category)));
                initMap(lastFullScreenMessage.lat, lastFullScreenMessage.lon);
            }
            //Checking if it is accidental alert then showing dismiss button and start the countdown for COUNTDOWN_TIME_FOR_ACCEIDENT
            if (GCMUtils.Crash_Alert.equalsIgnoreCase(lastFullScreenMessage.code)) {
                fullScreenBtnDismiss.setVisibility(View.VISIBLE);
                timer = new CountDownTimer(Utility.COUNTDOWN_TIME_FOR_ACCEIDENT, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        fullScreenBtnDismiss.setText(getString(R.string.lbl_dismiss_x, Utility.getMinsSeconds(millisUntilFinished / 1000)));
                    }

                    @Override
                    public void onFinish() {
                        fullScreenBtnDismiss.setText(getString(R.string.lbl_dismiss));
                    }
                };
                timer.start();
            } else {
                fullScreenBtnDismiss.setVisibility(View.GONE);
            }
        }
    }

    CountDownTimer timer;

    @Override
    public void initView() {
        Log.d("START", "initView() called with: " + "");
        img = (ImageView) links(R.id.img);
        btnNext = (Button) links(R.id.btn_next);
        appbar = (AppBarLayout) links(R.id.appbar);
        introFrameLayout = (FrameLayout) links(R.id.intro_frame);
        tabLayout = (TabLayout) links(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_car_white));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_rider_small_white_small_inactive));
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        pagerAdapter = new CustomPagerAdapter(getSupportFragmentManager(), 2);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
                    setTitle(R.string.lbl_cars);
//                    fab.setImageResource(R.drawable.ic_forgot_password_white_small);
                    tabLayout.getTabAt(0).setIcon(R.drawable.ic_car_white);
                    tabLayout.getTabAt(1).setIcon(R.drawable.ic_rider_small_white_small_inactive);
                    fab.show();
                } else {
                    setTitle(R.string.lbl_carnot);
//                    fab.setImageResource(R.drawable.ic_nav_plus_white);
                    tabLayout.getTabAt(0).setIcon(R.drawable.ic_car_white_inactive);
                    tabLayout.getTabAt(1).setIcon(R.drawable.ic_rider_small_white_small);
                    fab.hide();
                }
                appbar.setExpanded(true, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_gps_icon);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewPager.getCurrentItem() == 0) {

                    Fragment fragment = pagerAdapter.getItem(viewPager.getCurrentItem());
                    if (fragment instanceof FragmentCars) {
                        Log.e("TAG", "Homing button.........");
                        ((FragmentCars) fragment).cameraUpdate();
                    }
                } else {
                    Intent intent = new Intent(mActivity, ActivityInviteRider.class);
                    startActivity(intent);
                }

            }
        });

        //Checking if it is comming from signup page then open add car dialog
        if (getIntent() != null && getIntent().hasExtra(ConstantCode.INTENT_ACTION) && ConstantCode.ACTION_REGISTER_CAR.equalsIgnoreCase(getIntent().getStringExtra(ConstantCode.INTENT_ACTION))) {
            Log.i("START", "AddNewCarActivityStarted1");
            Intent intent = new Intent(mActivity, ActivityAddNewCarSetup.class);
            intent.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_REGISTER_CAR);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ConstantCode.ACTION_ACTIVITY_ADD_NEW_CAR) {

                if (getIntent() != null && getIntent().hasExtra(ConstantCode.INTENT_ACTION) && ConstantCode.ACTION_REGISTER_CAR.equalsIgnoreCase(getIntent().getStringExtra(ConstantCode.INTENT_ACTION))) {

                }
                Fragment fragment = pagerAdapter.getItem(viewPager.getCurrentItem());
                if (fragment instanceof FragmentCars) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    @Override
    public void postInitView() {

    }

    @Override
    public void addAdapter() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                introScreen++;
                switch (introScreen) {
                    case 1:
                        img.setImageResource(R.drawable.intro_one);
                        break;
                    case 2:
                        img.setImageResource(R.drawable.intro_two);
                        break;
                    case 3:
                        img.setImageResource(R.drawable.intro_three);
                        break;
                    case 4:
                        img.setImageResource(R.drawable.intro_four);
                        break;
                    default:
//                        ((FragmentCars) pagerAdapter.getItem(0)).addTemporaryMarker();
                        PrefManager.putBoolean(ConstantCode.PREF_INTRO_DONE, true);
                        introFrameLayout.setVisibility(View.GONE);
                        break;
                }
            }
        });

    }


    @Override
    public void loadData() {
        loadProfileFromServer();
        ActivitySplash.setAlarmForTripSync();
        //ActivityAllTrips.callGetRecentTripWebApi(mActivity);
    }

    /**
     * Loading my profile from server only at first time when app is open
     */
    private void loadProfileFromServer() {
        String id = ConstantCode.DEFAULT_USER_ID;
        if (Utility.getLoggedInUser() != null)
            id = Utility.getLoggedInUser().id + "";

        WebUtils.call(WebServiceConfig.WebService.GET_MY_PROFILE, new String[]{id}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);

                if (Utility.getLoggedInUser() != null) {
                    JSONObject json = (JSONObject) values;
                    final MyProfile user = (MyProfile) Utility.parseFromString(json.optString(ConstantCode.data), MyProfile.class);
                    User loggedInUser = Utility.getLoggedInUser();
                    loggedInUser.name = user.nm;
                    loggedInUser.ph = user.ph;
                    loggedInUser.gender = user.gd;
                    loggedInUser.age = user.ag;
                    Utility.setLoggedInUser(loggedInUser);
                    loggedInUser.save();
                }
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
//                progressDialog.dismiss();
//                showToast("" + values.toString());
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
//                progressDialog.dismiss();
            }
        });

        //Loading all emergency numbers and saving it to database and preferences
        WebUtils.call(WebServiceConfig.WebService.GET_EMERGENCY_NUMBER, new String[]{id}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);

                JSONObject json = (JSONObject) values;
                JSONObject jsonData = json.optJSONObject(ConstantCode.data);
                JSONArray jsonContacts = jsonData.optJSONArray(ConstantCode.contacts);
                User user = Utility.getLoggedInUser();
                if (user != null) {
                    user.emergencyContacts = jsonContacts.toString();
                    Utility.setLoggedInUser(user);
                    //saving it to database
                    user.save();
                }
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);

            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);

            }
        });
    }

    public FrameLayout getIntroFrameLayout() {
        return introFrameLayout;
    }


    public class CustomPagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;
        FragmentCars carsFragment;
        FragmentCarnot carnotFragment;

        public CustomPagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    if (carsFragment == null) {
                        carsFragment = new FragmentCars();
                        carsFragment.setArguments(getIntent().getExtras());
                    }
                    return carsFragment;
                case 1:
                    if (carnotFragment == null) {
                        carnotFragment = new FragmentCarnot();
                        carnotFragment.setArguments(getIntent().getExtras());
                    }
                    return carnotFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }

    TextView txtNotifCount;
    public int notificationCounter = 0;

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        View count = menu.findItem(R.id.action_notification).getActionView();
        txtNotifCount = (TextView) count.findViewById(R.id.notif_count);
        updateNotificationCounter();
        count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menu.findItem(R.id.action_notification));
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Updating the counter on bell icon
     */
    public void updateNotificationCounter() {
        if (Utility.getLoggedInUser() != null) {
            this.notificationCounter = GCMMessage.getUnreadCount();
            if (txtNotifCount != null) {
                if (notificationCounter > 0) {
                    txtNotifCount.setText(String.valueOf(notificationCounter));
                    txtNotifCount.setVisibility(View.VISIBLE);
                } else {
                    txtNotifCount.setText(String.valueOf(notificationCounter));
                    txtNotifCount.setVisibility(View.GONE);
                }
            }
        }
    }


    boolean isLogoutDone = false;
    boolean isRemovedFromServer = false;
    boolean isGcmUnregisterComplete = false;
    ProgressDialog progressDialog;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
            if (item.getItemId() ==  R.id.action_notification){
                intent = new Intent(mActivity, ActivityNotification.class);
                startActivity(intent);}

            else if (item.getItemId() == R.id.action_help){
                if (viewPager != null && viewPager.getCurrentItem() == 0) {
                    introScreen = 1;
                    img.setImageResource(R.drawable.intro_one);
                    introFrameLayout.setVisibility(View.VISIBLE);
                }}

            else if (item.getItemId() ==  R.id.action_add_car){
                if (Utility.isConnectingToInternet(mActivity)) {
                    intent = new Intent(mActivity, ActivityAddNewCarSetup.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, ConstantCode.ACTION_ACTIVITY_ADD_NEW_CAR);//Sending argument because we need to disable back and to show welcome screen.
                } else {
                    showSnackbar(mActivity.getString(R.string.msg_internet_alert));
                }}
            else if (item.getItemId() ==  R.id.action_my_profile){
                intent = new Intent(mActivity, ActivityRiderProfile.class);
                startActivity(intent);}
            else if (item.getItemId() ==  R.id.action_logout){

                Log.e("yo i a called: ", "yoy");
                if(Utility.isConnectingToInternet(mActivity))
                {
                    Log.e(TAG,"Connectd to internet");
                    progressDialog = DialogUtil.ProgressDialog(mActivity, "Please wait");

                    //Unregistering device from server
                    HashMap<String, Object> params = new HashMap<>();
                    params.put(ConstantCode.user_id, Utility.getLoggedInUser().id);
                    params.put(ConstantCode.reg_id, Utility.getLoggedInUser().registration_gcm);
                    params.put(ConstantCode.email, Utility.getLoggedInUser().email);
                    WebUtils.call(WebServiceConfig.WebService.UNREGISTER_FOR_GCM, null, params, new NetworkCallbacks() {
                        @Override
                        public void successWithString(Object values, WebServiceConfig.WebService webService) {
                            super.successWithString(values, webService);
                            progressDialog.dismiss();
                            isGcmUnregisterComplete = true;
                            doLogout();
                        }

                        @Override
                        public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                            super.failedWithMessage(values, webService);
                            progressDialog.dismiss();
                            isGcmUnregisterComplete = false;
                        }

                        @Override
                        public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                            super.failedForNetwork(values, webService);
                            progressDialog.dismiss();
                            isGcmUnregisterComplete = false;
                        }

                        @Override
                        public void loadOffline() {
                            progressDialog.dismiss();
                            isGcmUnregisterComplete = false;
                        }
                    });

                    //logout from server.
                    WebUtils.call(WebServiceConfig.WebService.LOGOUT, null, params, new NetworkCallbacks() {
                        @Override
                        public void successWithString(Object values, WebServiceConfig.WebService webService) {
                            super.successWithString(values, webService);
                            progressDialog.dismiss();
                            isRemovedFromServer = true;
                            isLogoutDone = true;
                            doLogout();
                        }

                        @Override
                        public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                            super.failedWithMessage(values, webService);
                            isRemovedFromServer = false;
                            progressDialog.dismiss();
                        }

                        @Override
                        public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                            super.failedForNetwork(values, webService);
                            isRemovedFromServer = false;
                            progressDialog.dismiss();
                        }

                        @Override
                        public void loadOffline() {
                            super.loadOffline();
                            isRemovedFromServer = false;
                            progressDialog.dismiss();
                        }
                    });
                }
                else
                {
                    Toast.makeText(mActivity,"Internet Not Found!",Toast.LENGTH_LONG).show();
                }}

        return false;
    }

    private void doLogout() {
        Log.e(TAG, "doLogout: " + isRemovedFromServer + " " + isGcmUnregisterComplete + " "+ isLogoutDone );

        if(isRemovedFromServer && isGcmUnregisterComplete)
        {
            Log.e("CHECKKN :","sdsdf " + isLogoutDone);
            if (isLogoutDone) {
                NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nMgr.cancelAll();
                WebUtils.cancelAll();
                ((FragmentCars) pagerAdapter.getItem(0)).removeUpdates();
                progressDialog.dismiss();
                ActiveAndroid.getDatabase().close();
                mActivity.deleteDatabase("carnot.db");
                PrefManager.getInstance().edit().clear().apply();
                Utility.logoutExistingUser();
                Intent intent = new Intent(mActivity, ActivitySignup.class);
                startActivity(intent);
                App.getContext().deleteDatabase("carnot.db");
                try {
                    Log.e(TAG, "doLogout: "+"rm -rf " + Environment.getExternalStorageDirectory() + "/data/data/com.carnot/databases/" );
                    Runtime.getRuntime().exec("rm -rf " + "" + "/data/data/com.carnot/databases/");
                } catch (IOException e) {
                    Log.e(TAG, "doLogout: Error in deleting DB" );
                    e.printStackTrace();
                }
                finish();
            }
        }
    }

    boolean isBackPressed = false;

    @Override
    public void onBackPressed() {

        if (introFrameLayout.getVisibility() == View.VISIBLE) {
            introFrameLayout.setVisibility(View.GONE);
            return;
        }

        if (viewFullScreen != null && viewFullScreen.getVisibility() == View.VISIBLE)
            return;

        if (isBackPressed) {
            super.onBackPressed();
        } else {
            isBackPressed = true;
            showToast("Click back button once more to exit");
        }
    }

    public void enableScroll() {
        final AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        mToolbar.setLayoutParams(params);
    }

    public void disableScroll() {
        final AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(0);
        mToolbar.setLayoutParams(params);
    }

    //====================

    private void initGCM() {

        boolean sentToken = PrefManager.getInstance().getBoolean(ConstantCode.SENT_TOKEN_TO_SERVER, false);
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //updating the counter on bell icon from database
        updateNotificationCounter();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
