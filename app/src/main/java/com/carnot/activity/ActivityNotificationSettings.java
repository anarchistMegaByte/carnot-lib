package com.carnot.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.carnot.R;
import com.carnot.adapter.GeofencesAdapter;
import com.carnot.custom_views.customseekbar.DiscreteSeekBar;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.models.Geofence;
import com.carnot.models.GlobalNotifSwitch;
import com.carnot.models.User;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;
import com.tripadvisor.seekbar.ClockView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Created by chaks on 5/10/16.
 * Activity to show setting to show speed threshold
 */
public class ActivityNotificationSettings extends BaseActivity implements ClockView.ClockTimeUpdateListener {

    private static final String TAG = "Notification Alerts";
    RelativeLayout geofence_rl, vandal_rl, tow_rl, rash_rl, accident_rl;
    ListView geofence_lv;
    public static ArrayList<String> geo_names = new ArrayList<>();
    public static ArrayList<Integer> geo_ids = new ArrayList<>();
    public static ArrayList<Boolean> geo_isLive = new ArrayList<>();
    public static String carId;
    public Snackbar snackbar, snackbar_notif;
    private CoordinatorLayout coordinatorLayout;


    //    chkShowShortTrips;
    private DiscreteSeekBar discreteSeekbar;
    private int iSpeedThreshold = 30;
    private ProgressBar progressBar;
    private TextView lblSpeedThreshold;
    private ArrayList<Geofence> geo_list= new ArrayList<>();
    ClockView cv_from;
    ClockView cv_to;
    Boolean isDBEmpty = false;


    private RelativeLayout expandableThreshold;
    LinearLayout parentLayout;

    private CheckBox chk_geofence;
    private CheckBox chk_Accident;
    private CheckBox chk_Towing;
    private CheckBox chk_Vandalism;
    private CheckBox chk_speedthreshold;
    private ImageButton geofence_settings, vandal_settings, tow_settings, rash_settings, accident_settings;
    private GlobalNotifSwitch gns = new GlobalNotifSwitch();
    public DateTime vandal_alert_from = new DateTime(new Date()), vandal_alert_to = new DateTime(new Date());
    public Snackbar nodbentry, noInternet;
    private DateTime minTime, maxTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);
        parentLayout = (LinearLayout) findViewById(R.id.parentLayout);
        snackbar = Snackbar.make(parentLayout, "Geofence Saved", Snackbar.LENGTH_LONG);
        snackbar_notif = Snackbar.make(parentLayout, "Notification Settings Saved", Snackbar.LENGTH_LONG);

        noInternet = Snackbar.make(parentLayout, "No Internet Connection. Changes will not be saved", Snackbar.LENGTH_LONG);
        View sbView = noInternet.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        nodbentry = Snackbar
                .make(parentLayout, "Please go to the previous screen and connect to internet. Local database has no data.", Snackbar.LENGTH_INDEFINITE)
                .setAction("BACK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
        View nodbentryview = nodbentry.getView();
        TextView nodbentrytv = (TextView) nodbentryview.findViewById(android.support.design.R.id.snackbar_text);
        nodbentrytv.setMaxLines(3);
        nodbentry.setActionTextColor(Color.YELLOW);
        Boolean isconnected = Utility.isInternetConnected(this);
        Boolean isconnecting = Utility.isConnectingToInternet(this);



        if (!isconnected || !isconnecting) {
            noInternet.show();
        }


    }

    @Override
    public void onPause(){
        super.onPause();
        datasync();
    }

    @Override
    public void onResume(){
        super.onResume();
//        ArrayList<Geofence> db_geofences = Geofence.readAll(Integer.valueOf(carId));
//        for (int i = 0; i < db_geofences.size(); i++){
//            geo_names.set(i, db_geofences.get(i).name);
//            geo_isLive.set(i, db_geofences.get(i).isLive);
//        }
//        datasync();
//        getGlobalNotifications();
    }


    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle(getString(R.string.lbl_notification_settings));
    }


    @Override
    public void initView() {

//        Vandal time alerts config
        cv_from = (ClockView) findViewById(R.id.min_depart_time_clock_view);
        cv_to = (ClockView) findViewById(R.id.max_depart_time_clock_view);

        DateTime dt = new DateTime();
        Integer month_today = dt.getMonthOfYear();
        Integer day_today = dt.getDayOfMonth();
        Integer year_today = dt.getYear();
        DateTime tom =  dt.plusHours(24);
        Integer month_tom = tom.getMonthOfYear();
        Integer year_tom = tom.getYear();
        Integer day_tom = tom.getDayOfMonth();
        minTime = new DateTime(year_today, month_today, day_today, 20, 0);
        maxTime = new DateTime(year_tom, month_tom, day_tom, 8, 0);
        final DateTime midTime = new DateTime(year_tom, month_tom, day_tom, 0, 0);



        cv_from.setBounds(minTime, midTime, false);
        cv_to.setBounds(midTime, maxTime, false);
        cv_from.setClockTimeUpdateListener(this);
        cv_to.setClockTimeUpdateListener(this);
        chk_speedthreshold = (CheckBox) links(R.id.chk_speedthreshold);
//        chkShowShortTrips = (CheckBox) links(R.id.chk_show_short_trips);

        lblSpeedThreshold = (TextView) links(R.id.lbl_speed_threshold);
        progressBar = (ProgressBar) links(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        expandableThreshold = (RelativeLayout) links(R.id.expandable_threshold);

//        Geofence related
        chk_geofence = (CheckBox) links(R.id.chk_geofence);
        geofence_lv = (ListView) findViewById(R.id.geofences_list);
        geofence_rl=(RelativeLayout) findViewById(R.id.geo_hint);
        geofence_settings = (ImageButton) links(R.id.geofence_settings);

        geofence_rl.setVisibility(View.GONE);
        geofence_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (geofence_rl.getVisibility() == View.VISIBLE){
                    geofence_rl.setVisibility(View.GONE);
                }
                else{
                    geofence_rl.setVisibility(View.VISIBLE);}
            }
        });

        tow_rl=(RelativeLayout) findViewById(R.id.towing_hint);
        tow_settings = (ImageButton) links(R.id.towing_settings);

        tow_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tow_rl.getVisibility() == View.VISIBLE){
                    tow_rl.setVisibility(View.GONE);
                }
                else{
                    tow_rl.setVisibility(View.VISIBLE);}
            }
        });


        accident_rl=(RelativeLayout) findViewById(R.id.accident_hint);
        accident_settings = (ImageButton) links(R.id.accident_settings);

        accident_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accident_rl.getVisibility() == View.VISIBLE){
                    accident_rl.setVisibility(View.GONE);
                }
                else{
                    accident_rl.setVisibility(View.VISIBLE);}
            }
        });

        vandal_rl=(RelativeLayout) findViewById(R.id.vandal_hint);
        vandal_settings = (ImageButton) links(R.id.vandal_settings);

        vandal_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (vandal_rl.getVisibility() == View.VISIBLE){
                    vandal_rl.setVisibility(View.GONE);
                }
                else{
                    vandal_rl.setVisibility(View.VISIBLE);}
            }
        });

        rash_rl=(RelativeLayout) findViewById(R.id.expandable_threshold);
        rash_settings = (ImageButton) links(R.id.rash_settings);

        rash_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rash_rl.getVisibility() == View.VISIBLE){
                    rash_rl.setVisibility(View.GONE);
                }
                else{
                    rash_rl.setVisibility(View.VISIBLE);}
            }
        });





        chk_Accident = (CheckBox) links(R.id.chk_Accident);
        chk_Towing = (CheckBox) links(R.id.chk_Towing);
        chk_Vandalism = (CheckBox) links(R.id.chk_Vandalism);

        chk_Accident.setOnCheckedChangeListener(onClickListener);
        chk_geofence.setOnCheckedChangeListener(onClickListener);
        chk_speedthreshold.setOnCheckedChangeListener(onClickListener);
        chk_Vandalism.setOnCheckedChangeListener(onClickListener);
        chk_Towing.setOnCheckedChangeListener(onClickListener);

    }

    private CompoundButton.OnCheckedChangeListener onClickListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.getId() == R.id.chk_Accident){
                    if (chk_Accident.isChecked()){
                    gns.accident = 1;
                    }
                    else{
                        gns.accident = 0;
                    }}
                else if (compoundButton.getId() == R.id.chk_geofence){
                    if (chk_geofence.isChecked()){
                        gns.geofence = 1;
                    }
                    else{
                        gns.geofence = 0;
                    }}
                else if (compoundButton.getId() == R.id.chk_Towing){
                    if (chk_Towing.isChecked()){
                        gns.towing = 1;
                    }
                    else{
                        gns.towing = 0;
                    }}
                else if (compoundButton.getId() == R.id.chk_Vandalism){
                    if (chk_Vandalism.isChecked()){
                        gns.vandalism = 1;
                    }
                    else{
                        gns.vandalism = 0;
                    }}
                else if (compoundButton.getId() == R.id.chk_speedthreshold){
                    if (chk_speedthreshold.isChecked()){
                        gns.rashDriving = 1;
                    }
                    else{
                        gns.rashDriving = 0;
                    }}

            gns.carId = Integer.valueOf(carId);
            GlobalNotifSwitch.update_custom(gns);
//            datasync();
    }};
    @Override
    public void postInitView() {
//        chkShowShortTrips.setChecked(PrefManager.getInstance().getBoolean(ConstantCode.PREF_SHOW_SHORT_TRIPS, ConstantCode.SHOW_SHORT_TRIPS_DEFAULT));
    }

    @Override
    public void addAdapter() {

//        chkShowShortTrips.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                PrefManager.getInstance().edit().putBoolean(ConstantCode.PREF_SHOW_SHORT_TRIPS, isChecked).commit();
//            }
//        });

        chk_speedthreshold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    if (!expandableThreshold.isExpanded())
//                        expandableThreshold.toggle();
//                } else {
//                    if (expandableThreshold.isExpanded())
//                        expandableThreshold.toggle();
//                }

//                if (isChecked) {
//                    expandableThreshold.setVisibility(View.VISIBLE);
//                } else {
//                    expandableThreshold.setVisibility(View.GONE);
//                }
            }
        });

        /*chk_speedthreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((CheckBox) v).isChecked()) {
                    if (!expandableThreshold.isExpanded()) {
                        expandableThreshold.setExpanded(true);
                    }
                } else {
                    if (expandableThreshold.isExpanded()) {
                        expandableThreshold.setExpanded(false);
                    }
                }
            }
        });*/

        discreteSeekbar = (DiscreteSeekBar) findViewById(R.id.discreteSeekbar);
        updateResultSpeedThreshold(discreteSeekbar.getProgress());
        discreteSeekbar.setOnProgressChangeListener(onProgressChangeListener);

    }

    @Override
    public void loadData() {

//        expandableThreshold.post(new Runnable() {
//            @Override
//            public void run() {
        User user = Utility.getLoggedInUser();
        iSpeedThreshold = user.iSpeedThreshold;
        discreteSeekbar.setProgress(iSpeedThreshold);

        //Checking if speedthreshold is 0 then it means user disabled notification
//        if (iSpeedThreshold > 0) {
//            chk_speedthreshold.setChecked(true);
//            expandableThreshold.setVisibility(View.VISIBLE);
//        } else {
//            chk_speedthreshold.setChecked(false);
//            expandableThreshold.setVisibility(View.GONE);
//        }


//            }
//        });

        lblSpeedThreshold.setText(getString(R.string.lbl_Speed_Threshold, iSpeedThreshold + ""));
        getSpeedLimit();
        getAllGeofence();
        getGlobalNotifications();


    }

    private void updateResultSpeedThreshold(int i) {
        // TODO Auto-generated method stub
        lblSpeedThreshold.setText(getString(R.string.lbl_Speed_Threshold, i + ""));
        iSpeedThreshold = i;
        Log.i("Values for", i + "");
    }

    DiscreteSeekBar.OnProgressChangeListener onProgressChangeListener = new DiscreteSeekBar.OnProgressChangeListener() {

        @Override
        public void onProgressChanged(DiscreteSeekBar seekBar, int value,
                                      boolean fromUser) {
            // TODO Auto-generated method stub
            updateResultSpeedThreshold(value);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        setSpeedLimit(iSpeedThreshold);
    }

    private void getSpeedLimit() {
//        progressBar.setVisibility(View.VISIBLE);
        carId = getIntent().getStringExtra(ConstantCode.INTENT_CAR_ID);
        Log.e(TAG, "getSpeedLimit: here");
        WebUtils.call(WebServiceConfig.WebService.GET_SPEED_LIMIT, new String[]{carId}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
//                                showToast("Login success " + values.toString());

                JSONObject json = (JSONObject) values;
                JSONObject jsonData = json.optJSONObject("data");
                iSpeedThreshold = jsonData.optInt(ConstantCode.speed_limit);
                lblSpeedThreshold.setText(getString(R.string.lbl_Speed_Threshold, iSpeedThreshold + ""));
                discreteSeekbar.setProgress(iSpeedThreshold);

                //Checking if speedthreshold is 0 then it means user disabled notification

                if (iSpeedThreshold > 0 && chk_speedthreshold.isChecked() == false) {
                    chk_speedthreshold.setChecked(true);
                } else if (iSpeedThreshold <= 0 && chk_speedthreshold.isChecked() == true) {
                    chk_speedthreshold.setChecked(false);
                }

                /*if (iSpeedThreshold > 0) {
                    chk_speedthreshold.setChecked(true);
                } else {
                    chk_speedthreshold.setChecked(false);
                }*/
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
                noInternet.show();
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);

            }
        });
    }

    private void setSpeedLimit(int value) {
/*

        LayerDrawable layerDrawable = (LayerDrawable) getResources().getDrawable(R.drawable.button_background);
        Drawable item = layerDrawable.findDrawableByLayerId(R.id.itemPressedFalse);
        item.setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.MULTIPLY);
        Drawable item1 = layerDrawable.findDrawableByLayerId(R.id.itemDefault);
        item1.setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.MULTIPLY);

        GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.gradientDrawble);
        gradientDrawable.setCornerRadius(50);
*/

        //saving in users for offline
        User user = Utility.getLoggedInUser();
        user.iSpeedThreshold = iSpeedThreshold;
        Utility.setLoggedInUser(user);
        user.save();

        String carId = getIntent().getStringExtra(ConstantCode.INTENT_CAR_ID);
        HashMap<String, Object> params = new HashMap<>();
        params.put(ConstantCode.sl, (chk_speedthreshold.isChecked() ? iSpeedThreshold : 0) + ""); //if disabled then sending 0 else some value
        WebUtils.call(WebServiceConfig.WebService.SET_SPEED_LIMIT, new String[]{carId}, params, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
                showToast("failed login " + values.toString());
            }
        });
    }

    private void getAllGeofence(){

        carId = getIntent().getStringExtra(ConstantCode.INTENT_CAR_ID);

        Boolean isconnected = Utility.isInternetConnected(this);
        Boolean isconnecting = Utility.isConnectingToInternet(this);

        if (isconnected && isconnecting) {
            geo_names.clear();
            geo_ids.clear();
            geo_isLive.clear();
            geo_list.clear();

            WebUtils.call(WebServiceConfig.WebService.GET_ALL_GEOFENCE, new String[]{carId}, null, new NetworkCallbacks() {
                @Override
                public void successWithString(Object values, WebServiceConfig.WebService webService) {
                    super.successWithString(values, webService);
                    progressBar.setVisibility(View.GONE);
//                                showToast("Login success " + values.toString());

                    JSONObject json = (JSONObject) values;
                    Log.e(TAG, "successWithString: " + json);
                    JSONObject jsonData = json.optJSONObject("data");
                    Iterator<String> keys = jsonData.keys();

                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        try {
                            if (jsonData.get(key) instanceof JSONObject) {
                                JSONObject geo_data = (JSONObject) jsonData.get(key);
                                Log.e(TAG, "looop iterations: id:"  + key);
                                Geofence geo = new Geofence();
                                geo.carId = geo_data.getInt("car");
                                geo.geo_id = key;
                                geo.lat = geo_data.getString("lat");
                                geo.lng = geo_data.getString("lng");
                                geo.name = geo_data.getString("name");
                                geo.radius = geo_data.getString("radius");
                                geo.status = Integer.parseInt(geo_data.getString("status"));
                                geo.isLive = booleanToInt(geo_data.getBoolean("isLive")) ;

                                geo_names.add(geo.name);
                                geo_ids.add(Integer.valueOf(geo.geo_id));
                                geo_isLive.add(geo_data.getBoolean("isLive"));
                                Integer flag = 0;

                                ArrayList<Geofence> old_data = Geofence.readAll(Integer.valueOf(carId));
                                for (Geofence old_single_geo : old_data) {
                                    if (old_single_geo.geo_id.matches(key)) {
                                        Log.e(TAG, "looop iterations: Old found");
                                        Geofence g = new Select().from(Geofence.class).where("geo_id = ?", key).executeSingle();
                                        Geofence.update_all(geo);
                                        geo_list.add(g);
                                        flag = 1;
                                    }
                                }
                                Log.e(TAG, "looop iterations: flag " + flag);

                                if (flag == 0) {
                                    Log.e(TAG, "successWithString: new found ... saving");
                                    geo.save();
                                    geo_list.add(geo);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("chaks", "successWithString: " + geo_names);
                    geofence_lv.setAdapter(new GeofencesAdapter(mActivity, geo_names, geo_ids, geo_isLive, geo_list, snackbar));
                }

                @Override
                public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                    super.failedWithMessage(values, webService);
                    progressBar.setVisibility(View.GONE);
                    noInternet.show();
                }

                @Override
                public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                    super.failedForNetwork(values, webService);
                    progressBar.setVisibility(View.GONE);
                    noInternet.show();
                }
            });
        }
        else{
            geo_names.clear();
            geo_ids.clear();
            geo_isLive.clear();
            geo_list.clear();
            Log.e(TAG, "getAllGeofence: "  + "Not connected to internet");
            noInternet.show();
            ArrayList<Geofence> old_data = Geofence.readAll(Integer.valueOf(carId));
            if (old_data.size() > 0) {
                for (Geofence old_single_geo : old_data) {
                    Log.e(TAG, "getAllGeofence: geo ids saved in local DB: " + old_single_geo.geo_id);
                    geo_names.add(old_single_geo.name);
                    geo_ids.add(Integer.valueOf(old_single_geo.geo_id));
                    if (old_single_geo.isLive != 0) {
                        geo_isLive.add(true);
                    } else{
                        geo_isLive.add(false);
                    }
                    geo_list.add(old_single_geo);
                }
                geofence_lv.setAdapter(new GeofencesAdapter(mActivity, geo_names, geo_ids, geo_isLive, geo_list, snackbar));}
            else{
                nodbentry.show();
            }

        }
    }
    public static int booleanToInt(boolean value) {
        // Convert true to 1 and false to 0.
        return value ? 1 : 0;
    }

    public void getGlobalNotifications(){

        Boolean isconnected = Utility.isInternetConnected(this);
        Boolean isconnecting = Utility.isConnectingToInternet(this);

        if (isconnected && isconnecting) {
            Log.e(TAG, "getGlobalNotifications: Connected to Internet ");
            final String carId = getIntent().getStringExtra(ConstantCode.INTENT_CAR_ID);
            WebUtils.call(WebServiceConfig.WebService.GET_GLOBAL_NOTIFICATIONS, new String[]{carId}, null, new NetworkCallbacks() {
                @Override
                public void successWithString(Object values, WebServiceConfig.WebService webService) {
                    super.successWithString(values, webService);
                    progressBar.setVisibility(View.GONE);
                    JSONObject json = (JSONObject) values;
                    Log.e(TAG, "successWithString: gns" + values);
                    JSONObject jsonData = json.optJSONObject("data");
                    try {
                        ArrayList<GlobalNotifSwitch> all_gns = GlobalNotifSwitch.readAll(Integer.valueOf(carId));
                        Log.e(TAG, "successWithString: reached here 0 " + all_gns.size());
                        if (all_gns.size() > 0) {
                            gns.geofence = booleanToInt(jsonData.getBoolean("geofence"));
                            gns.vandalism = booleanToInt(jsonData.getBoolean("vandalism"));
                            gns.towing = booleanToInt(jsonData.getBoolean("towing"));
                            gns.accident = booleanToInt(jsonData.getBoolean("accident"));
                            gns.rashDriving = booleanToInt(jsonData.getBoolean("rashDriving"));
                            gns.carId = Integer.valueOf(carId);
                            gns.vandalStart = jsonData.getString("vandalStart");
                            gns.vandalEnd = jsonData.getString("vandalEnd");
                            Log.e(TAG, "successWithString: Saving the update reached here 0");
                            GlobalNotifSwitch.update_custom(gns);
                            update_UI(gns);
                        } else {
                            Log.e(TAG, "successWithString: new gns to be saved");
                            gns = new GlobalNotifSwitch();
                            gns.geofence = booleanToInt(jsonData.getBoolean("geofence"));
                            gns.vandalism = booleanToInt(jsonData.getBoolean("vandalism"));
                            gns.towing = booleanToInt(jsonData.getBoolean("towing"));
                            gns.accident = booleanToInt(jsonData.getBoolean("accident"));
                            gns.rashDriving = booleanToInt(jsonData.getBoolean("rashDriving"));
                            gns.carId = Integer.valueOf(carId);
                            gns.vandalStart = jsonData.getString("vandalStart");
                            gns.vandalEnd = jsonData.getString("vandalEnd");
                            gns.save();
                            Log.e(TAG, "successWithString: new old" + gns);
                            update_UI(gns);
                        }
                    } catch (Exception e1) {
                        Log.e(TAG, "successWithString: Exception" + e1);
                    }
                }

                @Override
                public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                    super.failedWithMessage(values, webService);
                    progressBar.setVisibility(View.GONE);

                    ArrayList<GlobalNotifSwitch> all_gns = GlobalNotifSwitch.readAll(Integer.valueOf(carId));
                    nodbentry.show();
                    Log.e(TAG, "successWithString: failedWithMessage " + all_gns.size());
                    if (all_gns.size() > 0) {
                        gns = all_gns.get(0);
                        Log.e(TAG, "getGlobalNotifications: " + gns.geofence + " " + gns.accident);
                        update_UI(gns);
                        noInternet.show();
                    }
                    else{
                        nodbentry.show();
                        isDBEmpty = true;
                    }
                }

                @Override
                public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                    super.failedForNetwork(values, webService);
                    progressBar.setVisibility(View.GONE);

                    ArrayList<GlobalNotifSwitch> all_gns = GlobalNotifSwitch.readAll(Integer.valueOf(carId));
                    Log.e(TAG, "successWithString: FailedForNetwork " + all_gns.size());
                    if (all_gns.size() > 0) {
                        gns = all_gns.get(0);
                        Log.e(TAG, "getGlobalNotifications: " + gns.geofence + " " + gns.accident);
                        update_UI(gns);
                        noInternet.show();
                    }
                    else{
                        nodbentry.show();
                        isDBEmpty = true;

                    }
                }

                @Override
                public void loadOffline() {
                    super.loadOffline();

                    ArrayList<GlobalNotifSwitch> all_gns = GlobalNotifSwitch.readAll(Integer.valueOf(carId));
                    Log.e(TAG, "successWithString: reached here 0 " + all_gns.size());
                    if (all_gns.size() > 0) {
                        gns = all_gns.get(0);
                        Log.e(TAG, "getGlobalNotifications: " + gns.geofence + " " + gns.accident);
                        update_UI(gns);
                        noInternet.show();
                    }
                    else{
                        nodbentry.show();
                        isDBEmpty = true;

                    }
                }
            });
        }
        else{
            Log.e(TAG, "getGlobalNotifications: Not connected to internet");
            ArrayList<GlobalNotifSwitch> all_gns = GlobalNotifSwitch.readAll(Integer.valueOf(carId));
            Log.e(TAG, "successWithString: reached here 0 " + all_gns.size());
            if (all_gns.size() > 0) {
                gns = all_gns.get(0);
                Log.e(TAG, "getGlobalNotifications: " + gns.geofence + " " + gns.accident);
                update_UI(gns);
                noInternet.show();

            }
            else{
                nodbentry.show();
                isDBEmpty = true;

            }
        }
    }

    public ArrayList<Boolean> get_current_state(){
        ArrayList<Boolean> data = new ArrayList<Boolean>(Arrays.asList(new Boolean[5]));
        Collections.fill(data, Boolean.TRUE);
//        geo -- vand -- rash -- towing -- accident
        data.set(0, chk_geofence.isChecked());
        data.set(1, chk_Vandalism.isChecked());
        data.set(2, chk_speedthreshold.isChecked());
        data.set(3, chk_Towing.isChecked());
        data.set(4, chk_Accident.isChecked());
        return data;

    }

    public void update_UI(GlobalNotifSwitch gns) {
        Log.e(TAG, "update_UI: UI update" + gns.geofence + gns.accident);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd:MM:yyyy HH:mm:ss");
        try{
            Log.e(TAG, "update_UI: Old time" + gns.vandalStart + " " + gns.vandalEnd);
            vandal_alert_from = formatter.parseDateTime(gns.vandalStart);
            vandal_alert_to = formatter.parseDateTime(gns.vandalEnd);
            Log.e(TAG, "update_UI: After parsing " + vandal_alert_from + " " + vandal_alert_to );
            cv_from.setNewCurrentTime(vandal_alert_from);
            cv_to.setNewCurrentTime(vandal_alert_to);
            Log.e(TAG, "UpdateUI: Old time found from DB");

        }catch (Exception e){
            vandal_alert_from = new DateTime(2016, 4, 26, 20, 0);
            vandal_alert_to = new DateTime(2016, 4, 27, 8, 0);
            cv_from.setNewCurrentTime(vandal_alert_from);
            cv_to.setNewCurrentTime(vandal_alert_to);
            Log.e(TAG, "UpdateUI: New time saved as a placebo");
        }




//       Switching checkboxes. DB --> UI connection
//        TODO refactor this to use Arraylist
        if (gns.geofence != 0) {
            chk_geofence.setChecked(true);
        } else {
            chk_geofence.setChecked(false);
        }

        if (gns.vandalism!= 0) {
            chk_Vandalism.setChecked(true);
        } else {
            chk_Vandalism.setChecked(false);
        }

        if (gns.rashDriving!= 0) {
            chk_speedthreshold.setChecked(true);
        } else {
            chk_speedthreshold.setChecked(false);
        }

        if (gns.towing!= 0) {
            chk_Towing.setChecked(true);
        } else {
            chk_Towing.setChecked(false);
        }

        if (gns.accident!= 0) {
            chk_Accident.setChecked(true);
        } else {
            chk_Accident.setChecked(false);
        }


//    Get final state and then edit_sync


    }

    public void datasync(){
        ArrayList<Boolean> config_data = get_current_state();
        Log.e(TAG, "update_UI: config data" +  config_data);
        String format_string = "dd:MM:yyyy HH:mm:ss";
        DateTimeFormatter fmt = DateTimeFormat.forPattern(format_string);
        Log.e(TAG, "datasync: vandal_start " + vandal_alert_from + " vandal_end " + vandal_alert_to);


        HashMap<String, Object> metaList = new HashMap<String, Object>();
        try {
            metaList.put("geofence", config_data.get(0));
            metaList.put("vandalism", config_data.get(1));
            metaList.put("rashDriving", config_data.get(2));
            metaList.put("towing", config_data.get(3));
            metaList.put("accident", config_data.get(4));
            metaList.put("vandalStart", fmt.print(vandal_alert_from));
            metaList.put("vandalEnd", fmt.print(vandal_alert_to));

        } catch (Exception e) {
            e.printStackTrace();
        }

        gns.geofence = booleanToInt(config_data.get(0));
        gns.vandalism = booleanToInt(config_data.get(1));
        gns.rashDriving = booleanToInt(config_data.get(2));
        gns.towing = booleanToInt(config_data.get(3));
        gns.accident = booleanToInt(config_data.get(4));
        gns.carId = Integer.valueOf(carId);
        gns.vandalStart = fmt.print(vandal_alert_from);
        gns.vandalEnd = fmt.print(vandal_alert_to);

        GlobalNotifSwitch.edit_sync(this, gns, metaList, snackbar_notif);
    }


    @Override
    public void onClockTimeUpdate(ClockView clockView, DateTime currentTime) {
        ClockView cv = clockView;

        if(cv.getId() == R.id.min_depart_time_clock_view)
        {
            vandal_alert_from = currentTime;
            vandal_alert_from.plusHours(12);
            Log.e(TAG, "onClockTimeUpdate First: " + vandal_alert_from );
            datasync();
        }
        else if(cv.getId() == R.id.max_depart_time_clock_view)
        {
            vandal_alert_to = currentTime;
            Log.e(TAG, "onClockTimeUpdate Second: " + vandal_alert_from );
            datasync();

        }
    }
}
