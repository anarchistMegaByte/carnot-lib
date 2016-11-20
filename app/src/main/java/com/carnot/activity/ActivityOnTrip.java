package com.carnot.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import android.view.animation.Transformation;
import android.widget.ImageView;

import com.carnot.R;
import com.carnot.Services.BLEService;
import com.carnot.custom_views.AppTextViewLight;
import com.carnot.custom_views.ArcView;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.RippleBackground;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Receive data from BLE Service into this Activity and update the UI
 * Inputs required:
 * - Speed, RPM, Load for Odometer
 * - Instantaneous mileage (dist / fuel) for Graph
 * - Average mileage (dist / fuel) for Graph
 * - Trip distance, Trip Average Speed, Trip mileage (same as Average mileage)
 */

/**
 * Created by root on 8/4/16.
 */
public class ActivityOnTrip extends BaseActivity {

    public static final boolean BLE_ENABLED = true;
    ViewPager viewPager;
    View shadowView;
    CardView cardView;
    ImageView imgMoveLeft, imgMoveRight;
    AppTextViewLight tripDistanceView, tripSpeedView, tripMileageView;

    private boolean isTagged = false;

    //Display variables.
    //Set their values with data received from BLE and refresh their UIs to see them displayed on the screen
    private int displayRPM = 0;
    private int displaySpeed = 0;
    private int displayLoad = 0;
    private float displayTripMileage = 0;
    private float displayTripDistance = 0;
    private float displayTripSpeed = 0;

    private double displayLatitude = 0;
    private double displayLongitude = 0;

    private BroadcastReceiver updateBR;

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 101;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 102;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 103;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 104;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_trip);

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

        if (BLE_ENABLED) {
            //TODO: Long running service. Move this to App Launch
            //TODO: Start this service only for Android with API Level >= 18
            startService(new Intent(this, BLEService.class));
        }

        updateBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUIfromIntent(intent);
            }
        };
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (BLE_ENABLED) {
            unregisterReceiver(updateBR);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BLE_ENABLED) {
            IntentFilter i = new IntentFilter(BLEService.UI_UPDATE_ACTION);
            registerReceiver(updateBR, i);
        }
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    //TODO: if not require else remove it.
    //Implemened if any new functionality is to be implemented.
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
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //Release the scanner and the adapter
                        Log.e("Bluetooth State", "Turning Off");
                        break;
                }
            }
        }
    };


    @Override
    public void initView() {
        initMaps();
        imgMoveLeft = (ImageView) links(R.id.img_move_left);
        imgMoveRight = (ImageView) links(R.id.img_move_right);
        cardView = (CardView) links(R.id.card_view);
//        cardView.setShadowPadding(0, 0, 0, 0);
        cardView.setMaxCardElevation(0);
        viewPager = (ViewPager) links(R.id.view_pager);
        shadowView = (View) links(R.id.shadow_view);
        tripDistanceView = (AppTextViewLight) links(R.id.trip_distance_view);
        tripSpeedView = (AppTextViewLight) links(R.id.trip_speed_view);
        tripMileageView = (AppTextViewLight) links(R.id.trip_mileage_view);
    }

    @Override
    public void postInitView() {
/**
 * Here, we write a condition because for lolipop and above card view automatically show shadow and for pre lolipop devices we show view
 */
        if (isAndroidAPILevelGreaterThenEqual(Build.VERSION_CODES.LOLLIPOP)) {
            shadowView.setVisibility(View.GONE);
        } else {
            shadowView.setVisibility(View.VISIBLE);
        }
    }

    public static boolean isAndroidAPILevelGreaterThenEqual(int apiLevel) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= apiLevel) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void addAdapter() {
        CustomPagerAdapter adapter = new CustomPagerAdapter();
        viewPager.setAdapter(adapter);
        imgMoveLeft.setOnClickListener(onClickListener);
        imgMoveRight.setOnClickListener(onClickListener);
        if (BLE_ENABLED) {
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (position == 0 && positionOffset == 0 && positionOffsetPixels == 0) {
                        updateUI();
                        updateTripStats();
                    }
                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == imgMoveLeft.getId()) {
                if (viewPager.getCurrentItem() == 1) {
                    viewPager.setCurrentItem(0, true);
//                    updateUI();
//                    updateTripStats();
                }
            } else if (v.getId() == imgMoveRight.getId()) {
                if (viewPager.getCurrentItem() == 0) {
                    viewPager.setCurrentItem(1, true);
//                    updateUI();
//                    updateTripStats();
                }
            }
        }
    };

    @Override
    public void loadData() {

    }

    private void updateUI() {
        switch (viewPager.getCurrentItem()) {
            case 0:
                if (isTagged) {
                    Animation ani = ((ArcView) (viewPager.findViewWithTag("TAG_ODOMETER_VIEW").findViewById(R.id.arc_view))).getAnimation(displayRPM);
                    ani.setDuration(500);
                    ((ArcView) (viewPager.findViewWithTag("TAG_ODOMETER_VIEW").findViewById(R.id.arc_view))).startAnimation(ani);
                    //((ArcView) (viewPager.findViewWithTag("TAG_ODOMETER_VIEW").findViewById(R.id.arc_view))).updateRPM(displayRPM);
                    ((AppTextViewLight) (viewPager.findViewWithTag("TAG_ODOMETER_VIEW").findViewById(R.id.speed_indicator)))
                            .setText(String.format(Locale.getDefault(), "%d", displaySpeed));
                    //TODO: Display Load values
                    ((RippleBackground) (viewPager.findViewWithTag("TAG_ODOMETER_VIEW").findViewById(R.id.riple_background))).updatePulse(displayLoad);
                }
                break;

            case 1:
                if (isTagged) {
                    ((AppTextViewLight) (viewPager.findViewWithTag("TAG_GRAPH_VIEW").findViewById(R.id.avg_mileage_indicator)))
                            .setText(String.format(Locale.getDefault(), "%.1f", displayTripMileage));
                }
                break;
        }
    }

    private void updateChart(float instMil) {
        Log.e("BLE mileag ", instMil + "");
        if (isTagged) {
            updateMileageGraph(
                    ((LineChart) (viewPager.findViewWithTag("TAG_GRAPH_VIEW").findViewById(R.id.linechart_mileage))),
                    getCurrentDateTimeYear(),
                    instMil);
        }
    }

    private void updateUIfromIntent(Intent intent) {
        int type = intent.getIntExtra(BLEService.EXTRA_INTENT_TYPE, 0);
        switch (type) {
            case BLEService.INTENT_TYPE_LOCATION:
                displayLatitude = convertGeoCoordinates(intent.getIntExtra(BLEService.EXTRA_LATITUDE, 0));
                displayLongitude = convertGeoCoordinates(intent.getIntExtra(BLEService.EXTRA_LONGITUDE, 0));
                break;

            case BLEService.INTENT_TYPE_REALTIME:
                displayRPM = (intent.getIntExtra(BLEService.EXTRA_RPM, 0)) / 4;
                displaySpeed = intent.getIntExtra(BLEService.EXTRA_SPEED, 0);
                displayLoad = ((int) (((intent.getIntExtra(BLEService.EXTRA_LOAD, 0)) * 100.0) / 255.0));

                float instMileage;
                if (intent.getFloatExtra(BLEService.EXTRA_INSTANT_FUEL_VOL, 0) == 0.0f)
                    instMileage = 0.0f;
                else
                    instMileage = intent.getFloatExtra(BLEService.EXTRA_DISTANCE, 0) / intent.getFloatExtra(BLEService.EXTRA_INSTANT_FUEL_VOL, 0);

                updateUI();
                updateChart(instMileage);
                break;

            case BLEService.INTENT_TYPE_STM_RELAY:
                displayTripDistance = intent.getFloatExtra(BLEService.EXTRA_TOTAL_DISTANCE, 0.0f);
                displayTripSpeed = intent.getFloatExtra(BLEService.EXTRA_AVERAGE_SPEED, 0.0f);
                displayTripMileage = intent.getFloatExtra(BLEService.EXTRA_TOTAL_DISTANCE, 0) / intent.getFloatExtra(BLEService.EXTRA_TOTAL_FUEL, 0);
                updateUI();
                updateTripStats();
                break;

            case BLEService.INTENT_TYPE_ACCELEROMETER:
            default:
                break;
        }
    }

    private double convertGeoCoordinates(int latitude) {
        Double lat = latitude / 10000.0;
        Double lat_by_100;
        int lat_100;

        lat_by_100 = lat / 100.0;
        lat_100 = lat_by_100.intValue();
        lat = lat_100 + (((lat / 100.0) - lat_100) / 60.0 * 100.0);
        return lat;
    }

    private void updateTripStats(float tDistance, float tSpeed, float tMileage) {
        tripDistanceView.setText(String.format(Locale.getDefault(), "%.2f", tDistance));
        tripSpeedView.setText(String.format(Locale.getDefault(), "%.2f", tSpeed));
        tripMileageView.setText(String.format(Locale.getDefault(), "%.2f", tMileage));
    }

    private void updateTripStats() {
        tripDistanceView.setText(String.format(Locale.getDefault(), "%.2f", displayTripDistance));
        tripSpeedView.setText(String.format(Locale.getDefault(), "%.2f", displayTripSpeed));
        tripMileageView.setText(String.format(Locale.getDefault(), "%.2f", displayTripMileage));
    }

    private void initMaps() {
        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mSupportMapFragment == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mSupportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, mSupportMapFragment).commit();

        }
        if (mSupportMapFragment != null) {

            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    if (googleMap != null) {
                        googleMap.getUiSettings().setAllGesturesEnabled(true);
//                        googleMap.getUiSettings().setZoomControlsEnabled(true);

                        LatLng latLng = new LatLng(23.0273, 72.5074);
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 14);
                        googleMap.animateCamera(update);

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        final Marker marker = googleMap.addMarker(markerOptions);
                        /*googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                            @Override
                            public View getInfoWindow(Marker marker) {
                                View view = LayoutInflater.from(mActivity).inflate(R.layout.car_marker, null, false);
                                return view;
                            }

                            @Override
                            public View getInfoContents(Marker marker) {
                                return null;
                            }
                        });*/
                        /*googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                showToast("Info window clicked");
                            }
                        });*/

                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.img_car_on_map_small_marker)));
//                        marker.showInfoWindow();

                        /*googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                showToast("Marker clicked");
                                Intent intent = new Intent(mActivity, ActivityCarDashboard.class);
                                startActivity(intent);
                                return true;
                            }
                        });*/

                        final Handler h = new Handler();
                        final int delay = 5 * 1000;

                        h.postDelayed(new Runnable() {
                            public void run() {

                                // This portion of code runs after each delay.
                                LatLng carLocation = new LatLng(displayLatitude, displayLongitude);
                                marker.setPosition(carLocation);
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(carLocation));
                                h.postDelayed(this, delay);
                            }
                        }, delay);
                    }
                }
            });
        }
    }

    public class CustomPagerAdapter extends PagerAdapter {


        public CustomPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view;
            if (position == 0) {
                view = LayoutInflater.from(mActivity).inflate(R.layout.inflate_on_trip_speed, container, false);
                view.setTag("TAG_ODOMETER_VIEW");
                RippleBackground rippleBackground = (RippleBackground) view.findViewById(R.id.riple_background);
                rippleBackground.startRippleAnimation();
            } else {
                view = LayoutInflater.from(mActivity).inflate(R.layout.inflate_on_trip_mileage, container, false);
                view.setTag("TAG_GRAPH_VIEW");
                loadMileageGraph((LineChart) view.findViewById(R.id.linechart_mileage));
            }
            isTagged = true;
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

    private void loadMileageGraph(LineChart lineChart) {
       /* mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
       */
        lineChart.setDrawGridBackground(false);

        // no description text
        lineChart.setDescription("");
        lineChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        lineChart.setTouchEnabled(true);

        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(true);
//        lineChart.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.dark_gray));


        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(0f);
        llXAxis.disableDashedLine();
        llXAxis.setLabel("");

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisLineColor(ContextCompat.getColor(mActivity, R.color.grid_line));
        xAxis.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
        xAxis.setDrawAxisLine(true);
        xAxis.setGridColor(ContextCompat.getColor(mActivity, R.color.grid_line));
        xAxis.setDrawGridLines(true);
        xAxis.setLabelsToSkip(4);
        xAxis.setDrawLabels(false);


        //    Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        /*LimitLine ll1 = new LimitLine(50f, "");
        ll1.setLineWidth(2f);
//        ll1.enableDashedLine(20f, 10f, 0f);
        ll1.setLabel("");
        ll1.setLineColor(ContextCompat.getColor(mActivity, android.R.color.white));
        lineChart.getAxisLeft().addLimitLine(ll1);*/

        //TODO: Max value for mileage set to 35 and not 100
        lineChart.getAxisLeft().setAxisMaxValue(35f);
        //lineChart.getAxisLeft().setAxisMaxValue(100f);
        lineChart.getAxisLeft().setAxisMinValue(0f);
        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

        // add data
        setLineData(0, 0, lineChart);

        lineChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
//        mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = lineChart.getLegend();
        lineChart.getLegend().setEnabled(false);

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);

        // // dont forget to refresh the drawing
        // mChart.invalidate();
        if (lineChart.getData() != null) {
            lineChart.getData().setHighlightEnabled(false);


            List<ILineDataSet> sets = lineChart.getData()
                    .getDataSets();

            for (ILineDataSet iSet : sets) {

                LineDataSet set = (LineDataSet) iSet;
                //set.setDrawCircles(true);
                //set.setDrawValues(true);
                set.setDrawCircles(false);
                set.setDrawValues(false);
                set.setDrawFilled(true);
                set.setDrawCubic(true);
                set.disableDashedLine();
                set.setLineWidth(0f);
            }
        }
        lineChart.setPinchZoom(false);
        lineChart.setAutoScaleMinMaxEnabled(false);
    }

    private ArrayList<String> xVals;
    private ArrayList<Entry> yVals;

    private void updateMileageGraph(LineChart lineChart, String time, float mil) {
        xVals.add(time);
        yVals.add(new Entry(mil, yVals.size()));

        //TODO: Keep latest 300 values in the graph, discard the rest. The following is a temporary fix
        if (xVals.size() > 300) {
            setLineData(0, 0, lineChart);
        }

        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private void setLineData(int count, float range, LineChart barChartPastTrip) {

        xVals = new ArrayList<String>();
        yVals = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            xVals.add("");
        }

        for (int i = 0; i < count; i++) {

            float mult = (range + 1);
            float val = (float) (Math.random() * mult) + 3;// + (float)
            yVals.add(new Entry(val, i));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "");

        set1.disableDashedLine();
        set1.setColor(Color.BLACK);
        //TODO: un coment for green dot at x axis
        //set1.setCircleColor(Color.GREEN);

        set1.setDrawCircles(false);
        set1.setDrawValues(false);
        set1.setLineWidth(0f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(14f);
        set1.setValueTextColor(ContextCompat.getColor(mActivity, R.color.white));
        set1.setDrawFilled(true);

        if (Utils.getSDKInt() >= 18) {
            // fill drawable only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(mActivity, R.drawable.fade_red);
            set1.setFillDrawable(drawable);
        } else {
            set1.setFillColor(Color.BLACK);
        }

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        barChartPastTrip.setData(data);
    }

    /**
     * Gets current date time year.
     *
     * @return Return the current datetime in the format "yy-MM-dd HH:mm:ss"
     */
    public static String getCurrentDateTimeYear() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = new Date();
        return dateFormat.format(date);
    }

}
