package com.carnot.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.appsee.Appsee;
import com.carnot.App;
import com.carnot.R;
import com.carnot.Services.BLEService;
import com.carnot.global.AlarmService;
import com.carnot.global.ConstantCode;
import com.carnot.global.PrefManager;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.models.Cars;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Activity that comes at startup and after SPLASH_TIMEOUT it will
 * redirect user to dashboard screen or signup activity depending on whether user is logged in app or not
 */

/**
 * Created by pankaj on 31/3/16.
 */
public class ActivitySplash extends BaseActivity {

    private static Context mTemp;
    private static final String TAG = "ActivitySplash";

    //Splash timeout in milliseconds
    private final int SPLASH_TIMEOUT = 1000 * 2;

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 101;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 102;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 103;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 104;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        Appsee.start("4ad4c8bdac9748a4b30095d77171b63f");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED)
        {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MY_PERMISSIONS_REQUEST_BLUETOOTH);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED)
        {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN);
        }

    }


    @Override
    public void initVariable() {
        /*
        if (ActivityOnTrip.BLE_ENABLED) {
            //Long running service. Move this to App Launch
            //Start this service only for Android with API Level >= 18
            startService(new Intent(this, BLEService.class));
        }
        */
    }

    @Override
    public void initView() {

    }

    @Override
    public void postInitView() {

    }

    @Override
    public void addAdapter() {

    }

    Handler handler;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Intent intent;

            //Checking if there is loggedin user then opening Dashboard screen else signup screen
            if (Utility.getLoggedInUser() != null) {
                intent = new Intent(mActivity, ActivityDashboard.class);
            } else {
                intent = new Intent(mActivity, ActivitySignup.class);
            }

            startActivity(intent);
            finish();
        }
    };

    @Override
    public void loadData() {
        handler = new Handler();
        handler.postDelayed(runnable, SPLASH_TIMEOUT);

        //Checking if user is already logged in then calling the car garage webservice to load cars previous to main page
        if (Utility.getLoggedInUser() != null) {
            loadCarsData();
        }
    }


    //Loading garage data inititally on splash screen so that On FragmentCars it is not takes time to load.
    private void loadCarsData() {

        String id = ConstantCode.DEFAULT_USER_ID;
        if (Utility.getLoggedInUser() != null)
            id = Utility.getLoggedInUser().id + "";

        WebUtils.call(WebServiceConfig.WebService.GARAGE, new String[]{id}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                JSONObject json = (JSONObject) values;
                ArrayList<Cars> listCars = Utility.parseArrayFromString(json.optString(ConstantCode.data), Cars[].class);
                if (listCars.size() > 0) {
                    //Saving data in Database
                    for (Cars listCar : listCars) {
                        //Saving only selected values isOnTrip=?, lat=?, lon=?, speed=?, lut=?, name=?
                        listCar.updateSelectedValuesFromGarageApi();
                    }
                }
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
            }

            @Override
            public void loadOffline() {
                super.loadOffline();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    public static void setAlarmForTripSync()
    {
        //if "AlarmForTripSyncSet" is not present in the shared pref itself
        if(!PrefManager.getInstance().contains(App.getContext().getString(R.string.set_alarm_for_get_recent_trip_api)) || !(PrefManager.getInstance().getBoolean(App.getContext().getString(R.string.set_alarm_for_get_recent_trip_api),false)))
        {
            Intent alarmIntent = new Intent(App.getContext(), AlarmService.class);
            alarmIntent.putExtra(ConstantCode.INTENT_TRIP_SYNC, ConstantCode.ACTION_TRIP_SYNC_ALRAM);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getContext(), 0, alarmIntent, 0);

            AlarmManager manager = (AlarmManager) App.getContext().getSystemService(Context.ALARM_SERVICE);
            int interval = 1000 * 60 * 60 * 24;

            /* Set the alarm to start at 2:30 PM */
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 14);
            calendar.set(Calendar.MINUTE, 30);
            Log.e("Alaram set now","wwait man ");
            /* Repeating on every Once a day interval */
            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    ConstantCode.ALARM_TIME_FOR_GET_RECENT_TRIP_API_CALL, pendingIntent);
            PrefManager.putBoolean(App.getContext().getString(R.string.set_alarm_for_get_recent_trip_api),true);
            Log.e(TAG, "-----Alaram set !-----");
        }
        else
        {
            Log.e(TAG, "-----Alaram Already set !-----");
        }
    }
}
