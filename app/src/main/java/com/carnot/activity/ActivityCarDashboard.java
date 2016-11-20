package com.carnot.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.carnot.R;
import com.carnot.Services.BLEService;
import com.carnot.fragment.FragmentCarDashboard;
import com.carnot.fragment.FragmentCarDiagnostics;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.DateHelper;
import com.carnot.libclasses.EasyPermissions;
import com.carnot.models.Cars;
import com.google.android.gms.maps.GoogleMap;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by javid on 31/3/16.
 * Activity Car Dashboard that contains CarDashbaord and CarDiagnosis Fragment
 */
public class ActivityCarDashboard extends BaseActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    CustomPagerAdapter pagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_dashboard);
        if (ActivityOnTrip.BLE_ENABLED) {
            //Long running service. Move this to App Launch
            //Start this service only for Android with API Level >= 18
            startService(new Intent(this, BLEService.class));
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        IntentFilter fl = new IntentFilter(BLEService.UI_UPDATE_ACTION);
        registerReceiver(mPassKeyUpdate, fl);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mPassKeyUpdate);
    }

    //Real device testing needed.
    private final BroadcastReceiver mPassKeyUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(intent.hasExtra(ConstantCode.PASS_KEY_STATUS))
            {
                Log.i("TAG","TAG");
                if(intent.getBooleanExtra(ConstantCode.PASS_KEY_STATUS,false))
                {
                    if(onTripMenuItem != null)
                    {
                        onTripMenuItem.setVisible(true);
                    }
                }
                else
                {
                    if(onTripMenuItem != null)
                    {
                        onTripMenuItem.setVisible(false);
                    }
                }
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        Log.i("Bluetooth State", "ON");
                        break;

                    case BluetoothAdapter.STATE_OFF:
                        //Release the scanner and the adapter
                        Log.e("Bluetooth State", "OFF");
                        if(onTripMenuItem != null)
                        {
                            Log.e("Tag", "---OFF");
                            onTripMenuItem.setVisible(false);
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //Release the scanner and the adapter
                        Log.e("Bluetooth State", "Turning Off");
                        if(onTripMenuItem != null)
                            onTripMenuItem.setVisible(false);
                        break;
                }
            }
        }
    };

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        //   setTitle(R.string.lbl_cars);
        getSupportActionBar().setTitle(getIntent().getExtras().getString(ConstantCode.INTENT_CAR_NAME));
        Cars carsLUT = Cars.readSpecific(getIntent().getExtras().getString(ConstantCode.INTENT_CAR_ID));
        if (getIntent().getExtras().getBoolean(ConstantCode.INTENT_CAR_STATUS))
        {
            if(Utility.isConnectingToInternet(mActivity))
            {
                Calendar onTripSyncTime = DateHelper.getCalendarFromServer(carsLUT.lut);
                if(onTripSyncTime != null)
                {
                    Calendar todayTime = Calendar.getInstance();
                    if (todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis() > 0) {
                        long sec = TimeUnit.MILLISECONDS.toSeconds(Math.abs(todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis()));
                        long min = TimeUnit.MILLISECONDS.toMinutes(Math.abs(todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis()));
                        if (sec > 30 || min > 10) {
                            getSupportActionBar().setSubtitle("");
                        }
                        else
                        {
                            if(carsLUT.isOnTrip)
                                getSupportActionBar().setSubtitle("ON TRIP");
                        }
                    }
                }
                else
                {
                    //TODO : handle this case after wards
                    getSupportActionBar().setSubtitle("");
                }

            }
            else
            {
                getSupportActionBar().setSubtitle("");
            }
        }
    }

    @Override
    public void initView() {
        tabLayout = (TabLayout) links(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.lbl_tab_dashboard));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.lbl_tab_diagnostics));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        pagerAdapter = new CustomPagerAdapter(getSupportFragmentManager(), 2);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void postInitView() {

    }

    @Override
    public void addAdapter() {

    }

    @Override
    public void loadData() {

    }

    public void updateConnection(boolean isConnected) {
        ((FragmentCarDiagnostics) pagerAdapter.getItem(1)).updateConnection(isConnected);
    }


    public class CustomPagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;
        FragmentCarDashboard fragmentCarDashboard;
        FragmentCarDiagnostics fragmentCarDiagnostics;

        public CustomPagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    if (fragmentCarDashboard == null) {
                        fragmentCarDashboard = new FragmentCarDashboard();
                        fragmentCarDashboard.setArguments(getIntent().getExtras());
                    }
                    return fragmentCarDashboard;
                case 1:
                    if (fragmentCarDiagnostics == null) {
                        fragmentCarDiagnostics = new FragmentCarDiagnostics();
                        fragmentCarDiagnostics.setArguments(getIntent().getExtras());
                    }
                    return fragmentCarDiagnostics;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }


    MenuItem onTripMenuItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_car_diagnostics, menu);
        onTripMenuItem = menu.findItem(R.id.action_on_trip);
        if (onTripMenuItem != null) {
            if (pendingMenuVisibility == true) {
                onTripMenuItem.setVisible(isShow);
            } else
                onTripMenuItem.setVisible(false);
        }
        return true;
    }

    private boolean pendingMenuVisibility = true;
    private boolean isShow = false;

    public void updateMenuIcon(boolean show) {
        if (onTripMenuItem != null) {
            onTripMenuItem.setVisible(show);
//            invalidateOptionsMenu();
        } else {
            isShow = show;
            pendingMenuVisibility = true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
            if( item.getItemId() == R.id.action_on_trip){
                if(BLEService.passKeyWriteDone)
                {
                    intent = new Intent(mActivity, ActivityOnTrip.class);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(mActivity, "Your Phone is not connected to device. Wait for a moment and try again !",Toast.LENGTH_SHORT ).show();
                    int started = 0;
                    ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
                    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                        if (BLEService.class.getName().equals(service.service.getClassName())) {
                            //your service is running
                            started = 1;
                        }
                    }
                    if(started == 0)
                        startService(new Intent(mActivity, BLEService.class));
                }
            }

                //intent = new Intent(mActivity, ActivityOnTrip.class);
                //startActivity(intent);
            else if(item.getItemId() == R.id.action_share)
                takeScreenshot();

            else if(item.getItemId() == R.id.action_car_passport){
                intent = new Intent(ActivityCarDashboard.this, ActivityCarPassport.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                return true;}
            else if(item.getItemId() ==  R.id.action_car_profile){
                intent = new Intent(ActivityCarDashboard.this, ActivityCarProfile.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                return true;}
            else if(item.getItemId() == R.id.action_device_info){
                intent = new Intent(ActivityCarDashboard.this, ActivityDeviceInfo.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                return true;}
            else if(item.getItemId() == R.id.action_rash_driving_noti){
                intent = new Intent(ActivityCarDashboard.this, ActivityNotificationSettings.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                return true;}
        return false;
    }

    public float getActionBar_Toolbar_Bottom_Height() {

        return ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0).getHeight() - (getSupportActionBar().getHeight() + tabLayout.getHeight());
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        IntentFilter fl = new IntentFilter(BLEService.UI_UPDATE_ACTION);
        registerReceiver(mPassKeyUpdate, fl);

    }

    public void takeScreenshot() {

        EasyPermissions.requestPermissions(ActivityCarDashboard.this, new EasyPermissions.PermissionCallbacks() {
            @Override
            public void onPermissionsGranted(int requestCode, List<String> perms) {


                //Taking first page fragment from adapter.
                final Fragment fragment = pagerAdapter.getItem(0);
                if (fragment instanceof FragmentCarDashboard) {
                    GoogleMap googleMap = ((FragmentCarDashboard) fragment).getMap();
                    googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                        @Override
                        public void onSnapshotReady(Bitmap bitmap) {

//                            View v1 = getWindow().getDecorView().getRootView();
                            View v1 = ((FragmentCarDashboard) fragment).frameLayoutMaps;
                            Bitmap wholeBitmap = Utility.screenShot(v1);
                            Bitmap bmOverlay = Bitmap.createBitmap(wholeBitmap.getWidth(), wholeBitmap.getHeight(), wholeBitmap.getConfig());
                            Canvas canvas = new Canvas(bmOverlay);
                            canvas.drawBitmap(wholeBitmap, new Matrix(), null);
                            canvas.drawBitmap(bitmap, 0, 0, null);
                            File file = Utility.saveBitmap(mActivity, bmOverlay);
                            Utility.shareImage(mActivity, file);
                        }
                    });
                } else if (fragment instanceof FragmentCarDiagnostics) {
                    File file = Utility.takeScreenshot(mActivity);
                    Utility.shareImage(mActivity, file);
                }
            }

            @Override
            public void onPermissionsDenied(int requestCode, List<String> perms) {

            }

            @Override
            public void onPermissionsPermanentlyDeclined(int requestCode, List<String> perms) {
                /*Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                intent.setData(uri);
                mActivity.startActivity(intent);*/
            }
        }, null, 101, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
