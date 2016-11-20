package com.carnot.fragment;

import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.OnReadData;
import com.carnot.App;
import com.carnot.R;
import com.carnot.Services.BLEService;
import com.carnot.activity.ActivityAllTrips;
import com.carnot.activity.ActivityCarDashboard;
import com.carnot.activity.ActivityNotification;
import com.carnot.activity.ActivityTripDetails;
import com.carnot.adapter.AllTripAdapter;
import com.carnot.custom_views.CarbonFootprintView;
import com.carnot.custom_views.DriveScoreView;
import com.carnot.custom_views.MapMarkerView;
import com.carnot.custom_views.MileageView;
import com.carnot.custom_views.TouchableSupportMapFragment;
import com.carnot.global.ConstantCode;
import com.carnot.global.PrefManager;
import com.carnot.global.Utility;
import com.carnot.global.gcm.GCMMessage;
import com.carnot.libclasses.BaseFragment;
import com.carnot.libclasses.DateHelper;
import com.carnot.libclasses.MyValueFormatter;
import com.carnot.libclasses.MyYAxisValueFormatter;
import com.carnot.libclasses.SwipeDismissTouchListener;
import com.carnot.libclasses.VerticalSpaceItemDecoration;
import com.carnot.models.Cars;
import com.carnot.models.Graph;
import com.carnot.models.GraphData;
import com.carnot.models.GraphType;
import com.carnot.models.Trip;
import com.carnot.models.TripDetailMain;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by pankaj on 1/4/16.
 */
public class FragmentCarDashboard extends BaseFragment implements OnChartValueSelectedListener {

    private static final String TAG = "FragmentCarDashboard";
    private final int SKIP_X_VALUES = 1;
    //weekly, monthly, yearly
    private int[] limitsDuration = {7, 31, 12, 10, 10};
    int entriesInSinglePage = 7;
    int driveScoreDurationCurrentPosition = 0;
    int durationCurrentPosition = 0;

    NestedScrollView scrollView;
    RecyclerView recyclerViewHighlights, recyclerViewTrees;
    CarbonFootprintView carbonFootprintMyCar, carbonFootprintHybridCar, carbonFootprintPublicTransportAvg;
    LinearLayout ll_mileage, ll_carbon_footprint, ll_distance, ll_trip_hr;
    Button btnViewAllTrips;
    TextView txtViewAllTrips;

    LineChart linechartDriverscore;
    GoogleMap googleMap;
    LatLng latLng = new LatLng(23.0273, 72.5074);
    TextView txt_notification_title, txtDriveScoreGraph, txtDriveScoreThisWeek;
    double thisWeekDriveScore;
    double thisMonthDriveScore;
    double thisYearDriveScore;

    AllTripAdapter customAdapter;
    Cars car;

    FrameLayout frmChildGraphs, frmChildPastTripHighLights, frmChildBarchart, frmChildProgressbar;
    LinearLayout linearFootprint;
    FrameLayout linearGraph;
    TextView txtMileage, txtAvgSpeed, txtDistance, txtTripTime, txtNoOfTrees, txtDriveScore;
    DriveScoreView driveScoreView;
    //    ImageView icArrowExpand;
    MileageView mileageView;
    private Spinner spinnerGraphOptions;

    private TextView txtErrorNoTrip;
    private Spinner spinnerGraphDuration;
    private Spinner spinnerDriveScoreGraphDuration;
    private int iSeletedGraph = 0;

    ArrayList<GraphData> listGraphsNew;


    ArrayList<Graph> listGraphs;
    ArrayList<String> xVals = new ArrayList<String>();
    ArrayList<BarEntry> yVals;

    ArrayList<String> xValsDriveScore = new ArrayList<String>();
    ArrayList<Entry> yValsDriveScore;

    ImageView refreshView;

    private int iFuelPrice;
    BarChart mBarChart;

    //BY MANAV
    TextView txtForLastSyncTime;

    public FragmentCarDashboard() {
        setContentView(R.layout.fragment_car_dashboard);
    }

    @Override
    public void initVariable() {

    }


    @Override
    public void initView() {
        initMaps();

//        frm_expand = (FrameLayout) links(R.id.frm_expand) ;
//        expandableDetails = (ExpandableRelativeLayout) links(R.id.expandable_details);
        refreshView = (ImageView) links(R.id.refreshImage);
        txtErrorNoTrip = (TextView) links(R.id.txt_lbl_recent_trips);
        txtForLastSyncTime = (TextView) links(R.id.txt_lbl_last_sync_time);
        txtForLastSyncTime.setVisibility(View.GONE);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)txtForLastSyncTime.getLayoutParams();
        params.setMargins(10, 0, 0, 0); //substitute parameters for left, top, right, bottom
        txtForLastSyncTime.setLayoutParams(params);
//        counterTripCompleted = (CounterAppTextViewMedium) links(R.id.counter_txt_trip_completed);
//        counterNoHardBreaks = (CounterAppTextViewMedium) links(R.id.counter_txt_no_hard_breaks);
//        counterHardAcceleration = (CounterAppTextViewMedium) links(R.id.counter_txt_hard_accelerations);
//        counterIdlingTime = (CounterAppTextViewMedium) links(R.id.counter_txt_idling_time);
//        counterAvgMileage = (CounterAppTextViewMedium) links(R.id.counter_txt_average_mileage);

        linechartDriverscore = (LineChart) links(R.id.linechart_driverscore);
//        driverScoreViewProgressBar = (DriverScoreViewProgressBar) links(R.id.driver_score_view_progressBar);
//        driverScoreViewProgressBar.setTitleVisibility(View.VISIBLE);
        txtMileage = (TextView) links(R.id.txt_mileage);
        txtDriveScore = (TextView) links(R.id.txt_drive_score);
        txtDriveScoreGraph = (TextView) links(R.id.txt_drive_score_graph);
        txtDriveScoreThisWeek = (TextView) links(R.id.txt_drive_score_this_week);
        driveScoreView = (DriveScoreView) links(R.id.drive_score_view);
        txtAvgSpeed = (TextView) links(R.id.txt_avg_speed);
        txtDistance = (TextView) links(R.id.txt_distance);
        txtTripTime = (TextView) links(R.id.txt_trip_time);
//        txtIsOnTrip = (TextView) links(R.id.txt_is_on_trip);
        mileageView = (MileageView) links(R.id.mileage_view);
        txtNoOfTrees = (TextView) links(R.id.txt_no_of_trees);
//        icArrowExpand = (ImageView) links(R.id.ic_arrow_expand);

        txt_notification_title = (TextView) links(R.id.txt_notification_title);
        txt_notification_title.setFocusable(true);
        txt_notification_title.setVisibility(View.GONE);
        txtViewAllTrips = (TextView) links(R.id.txt_view_all_trips);
        btnViewAllTrips = (Button) links(R.id.btn_view_all_trips);

        scrollView = (NestedScrollView) links(R.id.scroll_view);
        ll_mileage = (LinearLayout) links(R.id.ll_mileage);
//        ll_trip = (LinearLayout) links(R.id.ll_trip);
        ll_carbon_footprint = (LinearLayout) links(R.id.ll_carbon_footprint);
        ll_distance = (LinearLayout) links(R.id.ll_distance);
        ll_trip_hr = (LinearLayout) links(R.id.ll_trip_hr);

        frmChildBarchart = (FrameLayout) links(R.id.frm_chart);
        frmChildPastTripHighLights = (FrameLayout) links(R.id.frm_child_past_trip_highlights);
        frmChildProgressbar = (FrameLayout) links(R.id.frm_child_progress);


        linearFootprint = (LinearLayout) links(R.id.linearChildFootprint);
        linearGraph = (FrameLayout) links(R.id.linear_graph);

        spinnerGraphOptions = (Spinner) links(R.id.spiner_child_chartoptions);
        spinnerGraphDuration = (Spinner) links(R.id.spiner_duration_selection);
        spinnerDriveScoreGraphDuration = (Spinner) links(R.id.spiner_drive_score_duration_selection);

//      frmChildTripHr = (FrameLayout) links(R.id.frm_child_trip_hr);


        recyclerViewHighlights = (RecyclerView) links(R.id.recycler_view_highlights);
        recyclerViewTrees = (RecyclerView) links(R.id.recycler_view_trees);
        carbonFootprintMyCar = (CarbonFootprintView) links(R.id.carbon_footprint_mycar);
        carbonFootprintHybridCar = (CarbonFootprintView) links(R.id.carbon_footprint_hybrid_car);
        carbonFootprintPublicTransportAvg = (CarbonFootprintView) links(R.id.carbon_footprint_public_transport_avg);
        txt_notification_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, ActivityNotification.class);
                startActivity(intent);
            }
        });
        txt_notification_title.setOnTouchListener(new SwipeDismissTouchListener(txt_notification_title, "", new SwipeDismissTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(Object token) {
                return true;
            }

            @Override
            public void onDismiss(View view, Object token) {
                txt_notification_title.setVisibility(View.GONE);
            }
        }));

        txt_notification_title.setVisibility(View.GONE);


        frmChildProgressbar.setVisibility(View.VISIBLE);

        spinnerGraphOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
//                ((TextView) adapter.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorPrimary));

                if (iSeletedGraph != position) {

                    iSeletedGraph = position;
                    resetGraphs();
//                    TextView tv = (TextView) adapter.getChildAt(0);
//                    tv.setEllipsize(TextUtils.TruncateAt.END);
//                    int n = 2; // the exact number of lines you want to display
//                    tv.setLines(n);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerDriveScoreGraphDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                /*
                if (driveScoreDurationCurrentPosition != position) {

                }
                */
                driveScoreDurationCurrentPosition = position;
                entriesInSinglePage = limitsDuration[position];

                //TODO : changes made
                if (listGraphsNew == null) {
                    Log.e(TAG , limitsDuration[position] + " "+ " in drive score");
                    return;
                }

                //getting all the X-Axis values and Y-Axis values from mapsGraphXVals and mapsGraphDriveEntry and loading the values in graph with loadMileageGraphNew();
                if (driveScoreDurationCurrentPosition == 0) {
                    yValsDriveScore = mapsGraphDriveEntry.get(DRIVE_SCORE_W);
                    xValsDriveScore = mapsGraphXVals.get(WEEKLY);
                    //Log.e("FragmentCars : ",xValsDriveScore.toString()+"");
                    txtDriveScoreGraph.setText(((int) thisWeekDriveScore) + "");
                    txtDriveScoreThisWeek.setText("This Week");
                } else if (driveScoreDurationCurrentPosition == 1) {
                    yValsDriveScore = mapsGraphDriveEntry.get(DRIVE_SCORE_M);
                    xValsDriveScore = mapsGraphXVals.get(MONTHLY);
                    txtDriveScoreGraph.setText(((int) thisMonthDriveScore) + "");
                    txtDriveScoreThisWeek.setText("This Month");
                } else if (driveScoreDurationCurrentPosition == 2) {
                    yValsDriveScore = mapsGraphDriveEntry.get(DRIVE_SCORE_Y);
                    xValsDriveScore = mapsGraphXVals.get(YEARLY);
                    txtDriveScoreGraph.setText(((int) thisYearDriveScore) + "");
                    txtDriveScoreThisWeek.setText("This Year");
                }
                if(xValsDriveScore != null)
                    loadMileageGraphNew();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerGraphDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /*
                if (durationCurrentPosition != position) {

                }
                */
                durationCurrentPosition = position;
                //setting the max limit according to position selected from Weekly, Monthly, Yearly
                entriesInSinglePage = limitsDuration[position];
                Log.e(TAG , limitsDuration[position] + " "+ " in graph");
                //Resetting the graphs
                resetGraphs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        refreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PrefManager.getInstance().contains(ConstantCode.TRIP_SYNC_FLAG))
                {
                    if(!PrefManager.getBoolean(ConstantCode.TRIP_SYNC_FLAG))
                    {
                        Log.e("CALL FROM : ", "refreshView");
                        ActivityAllTrips.getRecentTripsDetail(App.getContext(), car.id, car.serverTripID, car.local_latest_trip, car.serverFlag);
                    }
                    else {
                        Log.e("FronRefreshView : ", "Already syncing wait for it to complete");
                        if(car.local_latest_trip==car.serverTripID && car.serverFlag==0)
                        {
                            if(PrefManager.getInstance().contains(String.valueOf(car.id)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA))
                            {
                                PrefManager.getInstance().edit().putLong(String.valueOf(car.id)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis()).commit();
                            }
                            else
                            {
                                PrefManager.putLong(String.valueOf(car.id)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis());
                            }
                        }
                        if(car != null)
                        {
                            CharSequence relativeTime=null;
                            long time = PrefManager.getInstance().getLong(String.valueOf(car.id)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,-1);
                            Calendar todayTime = Calendar.getInstance();
                            if(time != -1)
                            {
                                if (todayTime.getTimeInMillis() - time > 0) {
                                    //getting minutes between the last sync time and current time
                                    long min = TimeUnit.MILLISECONDS.toMinutes(Math.abs(todayTime.getTimeInMillis() - time));

                                    relativeTime = DateUtils.getRelativeTimeSpanString(time, todayTime.getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, 0);
                                    if (relativeTime != null) {
                                        txtForLastSyncTime.setText("Last Sync At : "+relativeTime);
                                        txtForLastSyncTime.setVisibility(View.VISIBLE);
                                    }

                                }
                            }
                        }
                        else
                        {
                            txtForLastSyncTime.setVisibility(View.GONE);
                        }
                    }
                }
                else
                {
                    if(car.local_latest_trip==car.serverTripID && car.serverFlag==0)
                    {
                        if(PrefManager.getInstance().contains(String.valueOf(car.id)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA))
                        {
                            PrefManager.getInstance().edit().putLong(String.valueOf(car.id)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis()).commit();
                        }
                        else
                        {
                            PrefManager.putLong(String.valueOf(car.id)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis());
                        }
                    }
                    if(car != null)
                    {
                        CharSequence relativeTime=null;
                        long time = PrefManager.getInstance().getLong(String.valueOf(car.id)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,-1);
                        Calendar todayTime = Calendar.getInstance();
                        if(time != -1)
                        {
                            if (todayTime.getTimeInMillis() - time > 0) {
                                //getting minutes between the last sync time and current time
                                long min = TimeUnit.MILLISECONDS.toMinutes(Math.abs(todayTime.getTimeInMillis() - time));

                                relativeTime = DateUtils.getRelativeTimeSpanString(time, todayTime.getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, 0);
                                if (relativeTime != null) {
                                    txtForLastSyncTime.setText("Last Sync At : "+relativeTime);
                                    txtForLastSyncTime.setVisibility(View.VISIBLE);
                                }

                            }
                        }
                    }
                    else
                    {
                        txtForLastSyncTime.setVisibility(View.GONE);
                    }
                    //ActivityAllTrips.getRecentTripsDetail(App.getContext(), car.id, car.serverTripID, car.local_latest_trip, car.serverFlag);
                }
            }
        });
        //************************************************ Line Chart ****************************************************************
        mBarChart = (BarChart) links(R.id.barchart);
        linearFootprint.setVisibility(View.GONE);
//        linearGraph.setVerticalGravity(View.GONE);
        frmChildBarchart.setVisibility(View.GONE);
    }

    private void resetGraphs() {
        frmChildBarchart.setVisibility(View.VISIBLE);
        linearFootprint.setVisibility(View.GONE);
        linearGraph.setVisibility(View.GONE);
        links(R.id.txt_left_legend_name).setVisibility(View.GONE);
        switch (iSeletedGraph) {
            case 0:
                // load mileage graph
                linearGraph.setVisibility(View.VISIBLE);
                //TODO : Changes made
                if (listGraphsNew == null) {
                    break;
                }
                if (durationCurrentPosition == 0) {
                    yVals = mapsGraph.get(MILEAGE_W);
                    xVals = mapsGraphXVals.get(WEEKLY);
                } else if (durationCurrentPosition == 1) {
                    yVals = mapsGraph.get(MILEAGE_M);
                    xVals = mapsGraphXVals.get(MONTHLY);
                } else if (durationCurrentPosition == 2) {
                    yVals = mapsGraph.get(MILEAGE_Y);
                    xVals = mapsGraphXVals.get(YEARLY);
                }

                ((TextView) links(R.id.txt_left_legend_name)).setText("KMPL");
                links(R.id.txt_left_legend_name).setVisibility(View.VISIBLE);
//                yVals = fillmileageValues();
                if(xVals != null && yVals != null)
                {
                    if(xVals.size() >= yVals.size())
                        resetBarChart();
                }


                break;
            case 1:
                // load cost graph
                linearGraph.setVisibility(View.VISIBLE);
                if (listGraphsNew == null) {
                    break;
                }

                if (durationCurrentPosition == 0) {
                    yVals = mapsGraph.get(COST_W);
                    xVals = mapsGraphXVals.get(WEEKLY);
                } else if (durationCurrentPosition == 1) {
                    yVals = mapsGraph.get(COST_M);
                    xVals = mapsGraphXVals.get(MONTHLY);
                } else if (durationCurrentPosition == 2) {
                    yVals = mapsGraph.get(COST_Y);
                    xVals = mapsGraphXVals.get(YEARLY);
                }

                ((TextView) links(R.id.txt_left_legend_name)).setText("INR");
                links(R.id.txt_left_legend_name).setVisibility(View.VISIBLE);
//                yVals = fillCostValues();
                resetBarChart();

                break;
            case 2:
                // load distance graph
                linearGraph.setVisibility(View.VISIBLE);
                if (listGraphsNew == null) {
                    break;
                }

                if (durationCurrentPosition == 0) {
                    yVals = mapsGraph.get(DISTANCE_W);
                    xVals = mapsGraphXVals.get(WEEKLY);
                } else if (durationCurrentPosition == 1) {
                    yVals = mapsGraph.get(DISTANCE_M);
                    xVals = mapsGraphXVals.get(MONTHLY);
                } else if (durationCurrentPosition == 2) {
                    yVals = mapsGraph.get(DISTANCE_Y);
                    xVals = mapsGraphXVals.get(YEARLY);
                }

                ((TextView) links(R.id.txt_left_legend_name)).setText("KMS");
                links(R.id.txt_left_legend_name).setVisibility(View.VISIBLE);

//                yVals = fillDistanceValues();
                resetBarChart();
                break;
            case 3:
                // load duration graph
                linearGraph.setVisibility(View.VISIBLE);
                if (listGraphsNew == null) {
                    break;
                }

                if (durationCurrentPosition == 0) {
                    yVals = mapsGraph.get(DURATION_W);
                    xVals = mapsGraphXVals.get(WEEKLY);
                } else if (durationCurrentPosition == 1) {
                    yVals = mapsGraph.get(DURATION_M);
                    xVals = mapsGraphXVals.get(MONTHLY);
                } else if (durationCurrentPosition == 2) {
                    yVals = mapsGraph.get(DURATION_Y);
                    xVals = mapsGraphXVals.get(YEARLY);
                }

                ((TextView) links(R.id.txt_left_legend_name)).setText("HRS");
                links(R.id.txt_left_legend_name).setVisibility(View.VISIBLE);

//                yVals = fillDurationValues();
                resetBarChart();

                break;
            case 4:
                // load carbon footprint graph
                linearFootprint.setVisibility(View.VISIBLE);
                linearFootprint.requestLayout();


                break;
        }
    }


    @Override
    public void postInitView() {
        super.postInitView();

        recyclerViewTrees.setNestedScrollingEnabled(false);
        recyclerViewTrees.setHasFixedSize(true);

        recyclerViewHighlights.setNestedScrollingEnabled(false);
        recyclerViewHighlights.setHasFixedSize(true);

//        driverScoreViewProgressBar.setVisibility(View.VISIBLE);
        linechartDriverscore.setVisibility(View.GONE);


        //changing spinner dropdown color

        changeDropDownColorAccent(spinnerGraphOptions);
        changeDropDownColorAccent(spinnerGraphDuration);
        changeDropDownColorAccent(spinnerDriveScoreGraphDuration);

//        driverScoreViewProgressBar.setVisibility(View.GONE);
        ((View) links(R.id.txt_drive_score_graph).getParent()).setVisibility(View.VISIBLE);
        linechartDriverscore.setVisibility(View.VISIBLE);
    }

    private void changeDropDownColorAccent(Spinner spinner) {
        Drawable spinnerDrawable = spinner.getBackground().getConstantState().newDrawable();
        spinnerDrawable.setColorFilter(Utility.getColor(mActivity, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            spinner.setBackground(spinnerDrawable);
        } else {
            spinner.setBackgroundDrawable(spinnerDrawable);
        }
    }

    @Override
    public void addAdapter() {


        txtViewAllTrips.setOnClickListener(onClickListener);
        btnViewAllTrips.setOnClickListener(onClickListener);
        ll_mileage.setOnClickListener(onClickListener);
//        ll_trip.setOnClickListener(onClickListener);

        ll_carbon_footprint.setOnClickListener(onClickListener);
        ll_distance.setOnClickListener(onClickListener);
        ll_trip_hr.setOnClickListener(onClickListener);

        ArrayList<Trip> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(new Trip());
        }
        customAdapter = new AllTripAdapter(mActivity);
        customAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(mActivity, ActivityTripDetails.class);
                TripDetailMain objectTrip = customAdapter.getItem(position);
                if (objectTrip != null) {
                    Log.e("LOKKING", getArguments() + "");
                    intent.putExtra(ConstantCode.INTENT_TRIP_ID, String.valueOf(objectTrip.trip_id));
                    intent.putExtras(getArguments());
                }
                startActivity(intent);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerViewHighlights.setLayoutManager(linearLayoutManager);

        VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration((int) Utility.convertDpToPixel(mActivity.getResources().getDimension(R.dimen.scale_4dp), mActivity));
        recyclerViewHighlights.addItemDecoration(decoration);
        recyclerViewHighlights.setAdapter(customAdapter);

        VerticalSpaceItemDecoration decorationGridView = new VerticalSpaceItemDecoration((int) Utility.convertDpToPixel(mActivity.getResources().getDimension(R.dimen.scale_4dp), mActivity));
        recyclerViewTrees.addItemDecoration(decorationGridView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 12);
        recyclerViewTrees.setLayoutManager(gridLayoutManager);
        /*TreesAdapter treesAdapter = new TreesAdapter(84);
        recyclerViewTrees.setAdapter(treesAdapter);*/
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v.getId() == ll_mileage.getId()) {
//                smoothScrollTo(frmChildPastTripHighLights);
            } else if (v.getId() == ll_trip_hr.getId()) {
//                smoothScrollTo(frmChildTripHr);
            } else if (v.getId() == txtViewAllTrips.getId() || v.getId() == btnViewAllTrips.getId()) {
                openAllTripActivity();
            }

        }
    };

    private void openAllTripActivity() {
        Intent intent = new Intent(mActivity, ActivityAllTrips.class);
        intent.putExtras(getArguments());
        intent.putExtra(ConstantCode.INTENT_CAR_SHOW_SHORT_TRIPS, car.showShortTrips);
        startActivity(intent);
    }


    public LinearLayout frameLayoutMaps;

    private void initMaps() {


        frameLayoutMaps = (LinearLayout) links(R.id.ll_main_view);
        frameLayoutMaps.setVisibility(View.INVISIBLE);
        frameLayoutMaps.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) frameLayoutMaps.getLayoutParams();

                lp.height = (int) (((ActivityCarDashboard) mActivity).getActionBar_Toolbar_Bottom_Height());

                frameLayoutMaps.setLayoutParams(lp);
                frameLayoutMaps.setVisibility(View.VISIBLE);
            }
        });


        TouchableSupportMapFragment mSupportMapFragment = (TouchableSupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mSupportMapFragment != null) {

            if (mSupportMapFragment instanceof TouchableSupportMapFragment) {
                ((TouchableSupportMapFragment) mSupportMapFragment).setListener(new TouchableSupportMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                    }
                });
            }
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    if (googleMap != null) {
                        FragmentCarDashboard.this.googleMap = googleMap;
                        googleMap.getUiSettings().setAllGesturesEnabled(true);

                        if (pendingTask == true) {
                            addTemporaryMarker(car, false);
                            pendingTask = false;
                        }
                    }
                }
            });
        }
    }

    Marker marker;

    /**
     * Adding the marker on google map when garage api calls and when this screen is first loaded
     *
     * @param car
     * @param animate
     */
    public void addTemporaryMarker(final Cars car, boolean animate) {

        if(BLEService.passKeyWriteDone)
        {

            ((ActivityCarDashboard) mActivity).updateMenuIcon(true);
        }
        else
        {
            //((ActivityCarDashboard) mActivity).updateMenuIcon(false);
            int started = 0;
            ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (BLEService.class.getName().equals(service.service.getClassName())) {
                    //your service is running
                    started = 1;
                }
            }
            if(started == 0)
                getActivity().startService(new Intent(mActivity, BLEService.class));
        }

        if (googleMap == null) {
            pendingTask = true;
            return;
        }

        latLng = new LatLng(Double.parseDouble(car.lat), Double.parseDouble(car.lon));

        if (marker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            marker = googleMap.addMarker(markerOptions);
        } else {
            marker.setPosition(latLng);
        }

        //Inflating marker from xml layout
        View viewMarkerView = LayoutInflater.from(mActivity).inflate(R.layout.car_marker, null, false);
        ((TextView) viewMarkerView.findViewById(R.id.txt_marker_name)).setText(car.name);
        ((TextView) viewMarkerView.findViewById(R.id.txt_marker_name)).setVisibility(View.GONE);
        ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setVisibility(View.GONE);
        viewMarkerView.findViewById(R.id.img_sync_marker).setVisibility(View.GONE);

        int isOnTripBubbleShown = 0;

        if (car.isOnTrip)
        {
            if(Utility.isConnectingToInternet(mActivity))
            {
                Calendar onTripSyncTime = DateHelper.getCalendarFromServer(car.lut);
                if(onTripSyncTime != null)
                {
                    Calendar todayTime = Calendar.getInstance();
                    if (todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis() > 0) {
                        long sec = TimeUnit.MILLISECONDS.toSeconds(Math.abs(todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis()));
                        if (sec > 30) {
                            isOnTripBubbleShown = 2;
                            CharSequence relativeTime = DateUtils.getRelativeDateTimeString(mActivity, onTripSyncTime.getTimeInMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.YEAR_IN_MILLIS, 0);
                            if (relativeTime != null) {
                                ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setText("Last Sync " + relativeTime);
                                ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setVisibility(View.VISIBLE);
                                viewMarkerView.findViewById(R.id.info_window).setVisibility(View.GONE);
                                viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.GONE);
                                viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.GONE);
                                viewMarkerView.findViewById(R.id.img_sync_marker).setVisibility(View.VISIBLE);
                            }
                            ((ActivityCarDashboard) mActivity).updateConnection(false);
                        } else {
                            ((ActivityCarDashboard) mActivity).updateConnection(true);
                            viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.GONE);
                            viewMarkerView.findViewById(R.id.info_window).setVisibility(View.VISIBLE);
                            ((TextView) viewMarkerView.findViewById(R.id.txt_speed)).setText(car.speed + "");
                            if (car.isOnTrip) {
                                isOnTripBubbleShown = 1;
                                viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.VISIBLE);
                                if(BLEService.passKeyWriteDone)
                                    ((ActivityCarDashboard) mActivity).updateMenuIcon(true);
                            } else {
                                viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.GONE);
                            }
                        }

                    }
                    else
                    {
                        ((ActivityCarDashboard) mActivity).updateConnection(false);
                        viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.GONE);
                        viewMarkerView.findViewById(R.id.info_window).setVisibility(View.VISIBLE);
                        ((TextView) viewMarkerView.findViewById(R.id.txt_speed)).setText(car.speed + "");

                        if (car.isOnTrip) {
                            isOnTripBubbleShown = 1;
                            if(BLEService.passKeyWriteDone)
                                ((ActivityCarDashboard) mActivity).updateMenuIcon(true);
                            viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.VISIBLE);
                        } else {
                            viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.GONE);
                        }
                    }
                }
                else
                {
                    ((ActivityCarDashboard) mActivity).updateConnection(false);
                    viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.GONE);
                    viewMarkerView.findViewById(R.id.info_window).setVisibility(View.VISIBLE);
                    ((TextView) viewMarkerView.findViewById(R.id.txt_speed)).setText(car.speed + "");

                    if (car.isOnTrip) {
                        isOnTripBubbleShown = 1;
                        if(BLEService.passKeyWriteDone)
                            ((ActivityCarDashboard) mActivity).updateMenuIcon(true);
                        viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.VISIBLE);
                    } else {
                        viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.GONE);
                    }
                }
            }
            else
            {
                ((ActivityCarDashboard) mActivity).updateConnection(false);
                Calendar onTripSyncTime = DateHelper.getCalendarFromServer(car.lut);
                if(onTripSyncTime != null)
                {
                    Calendar todayTime = Calendar.getInstance();
                    if (todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis() > 0) {
                        long sec = TimeUnit.MILLISECONDS.toSeconds(Math.abs(todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis()));
                        if (sec > 30) {
                            isOnTripBubbleShown = 2;
                            CharSequence relativeTime = DateUtils.getRelativeDateTimeString(mActivity, onTripSyncTime.getTimeInMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.YEAR_IN_MILLIS, 0);
                            if (relativeTime != null) {
                                ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setText("Last Sync " + relativeTime);
                                ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setVisibility(View.VISIBLE);
                                viewMarkerView.findViewById(R.id.info_window).setVisibility(View.GONE);
                                viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.GONE);
                                viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.GONE);
                                viewMarkerView.findViewById(R.id.img_sync_marker).setVisibility(View.VISIBLE);
                            }
                        }else {
                            viewMarkerView.findViewById(R.id.info_window).setVisibility(View.GONE);
                            viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.GONE);
                            viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.GONE);
                            viewMarkerView.findViewById(R.id.img_sync_marker).setVisibility(View.VISIBLE);
                        }
                    }
                } else {

                }
            }
        } else {
            viewMarkerView.findViewById(R.id.info_window).setVisibility(View.GONE);
            viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.VISIBLE);
        }


        if(isOnTripBubbleShown == 0)
        {
            //Checking if sync time is greater then 10 min then we need to last sync status on the marker
            Calendar syncTime = DateHelper.getCalendarFromServer(car.lut);
            if (syncTime != null) {
                Calendar todayTime = Calendar.getInstance();
                //getting minutes between the last sync time and current time
                long min = TimeUnit.MILLISECONDS.toMinutes(Math.abs(todayTime.getTimeInMillis() - syncTime.getTimeInMillis()));
                if (min > 10) {
                    CharSequence relativeTime = DateUtils.getRelativeDateTimeString(mActivity, syncTime.getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.YEAR_IN_MILLIS, 0);
                    if (relativeTime != null) {
                        ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setText("Last Sync " + relativeTime);
                        ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setVisibility(View.VISIBLE);
                        viewMarkerView.findViewById(R.id.info_window).setVisibility(View.GONE);
                        viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.GONE);
                        viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.GONE);
                        viewMarkerView.findViewById(R.id.img_sync_marker).setVisibility(View.VISIBLE);
                    }
                    ((ActivityCarDashboard) mActivity).updateConnection(false);
                } else {
                    ((ActivityCarDashboard) mActivity).updateConnection(true);
                }
            } else {
                ((ActivityCarDashboard) mActivity).updateConnection(false);
            }
        }


        //Getting the bitmap from view and setting to marker
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(Utility.getBitmap(viewMarkerView, mActivity)));

        if (car.isOnTrip) {
            marker.setAnchor(0.5f, 0.7f);
        } else {
            marker.setAnchor(0.5f, 0.4f);
        }

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        if (animate) {
            googleMap.animateCamera(update);
        } else {
            googleMap.moveCamera(update);
        }
    }

    private void smoothScrollTo(View view) {
        ObjectAnimator.ofInt(scrollView, "scrollY", (int) view.getY()).setDuration(mActivity.getResources().getInteger(R.integer.expand_animation_duration)).start();
    }

    @Override
    public void loadData() {
        final String carId = getArguments().getString(ConstantCode.INTENT_CAR_ID) + "";
        registerForSyncStatus();
        fillValuesForCar(carId);

    }

    private void fillValuesForCar(String carId) {
        loadFromDatabase();
        loadRoutesFromDatabase(Integer.parseInt(carId));
        //loadAllGraphs();
        loadAllGraphsFromGraphData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterForSyncStatus();
    }

    ArrayList<String> datesList;
    HashMap<String, ArrayList<BarEntry>> charts;


    private void loadAllGraphsFromGraphData()
    {
        //Log.e(TAG, "Function is called here now");
        final String carId = getArguments().getString(ConstantCode.INTENT_CAR_ID) + "";
        listGraphsNew = GraphData.readAll(carId);
        //Log.e("TAG","------I M "+listGraphsNew.size() + "");
        GraphData.readAllAsync(carId, new OnReadData() {
            @Override
            public void onRead(Model type, ArrayList list) {
                mapsGraphXVals = new HashMap<>();
                mapsGraphXVals.put(WEEKLY, new ArrayList<String>());
                mapsGraphXVals.put(MONTHLY, new ArrayList<String>());
                mapsGraphXVals.put(YEARLY, new ArrayList<String>());

                mapsGraph = new HashMap<>();
                mapsGraphDriveEntry = new HashMap<>();

                mapsGraphDriveEntry.put(DRIVE_SCORE_W, new ArrayList<Entry>());
                mapsGraphDriveEntry.put(DRIVE_SCORE_M, new ArrayList<Entry>());
                mapsGraphDriveEntry.put(DRIVE_SCORE_Y, new ArrayList<Entry>());


                mapsGraph.put(MILEAGE_W, new ArrayList<BarEntry>());
                mapsGraph.put(MILEAGE_M, new ArrayList<BarEntry>());
                mapsGraph.put(MILEAGE_Y, new ArrayList<BarEntry>());

                mapsGraph.put(COST_W, new ArrayList<BarEntry>());
                mapsGraph.put(COST_M, new ArrayList<BarEntry>());
                mapsGraph.put(COST_Y, new ArrayList<BarEntry>());

                mapsGraph.put(DISTANCE_W, new ArrayList<BarEntry>());
                mapsGraph.put(DISTANCE_M, new ArrayList<BarEntry>());
                mapsGraph.put(DISTANCE_Y, new ArrayList<BarEntry>());

                mapsGraph.put(DURATION_W, new ArrayList<BarEntry>());
                mapsGraph.put(DURATION_M, new ArrayList<BarEntry>());
                mapsGraph.put(DURATION_Y, new ArrayList<BarEntry>());

                Calendar calendar = Calendar.getInstance();
                String dateDay;
                String dateWeek;
                String dateYear;
                //int count =-1;
                if(listGraphsNew != null && listGraphsNew.size() > 0)
                {
                    ArrayList<GraphData> weekly = GraphData.readAllByWeekRule(carId);
                    Calendar weeklyCalendar = Calendar.getInstance();
                    for(GraphData gd : weekly) {
                        weeklyCalendar.set(Calendar.DAY_OF_MONTH, gd.day);
                        weeklyCalendar.set(Calendar.MONTH, gd.month);
                        weeklyCalendar.set(Calendar.YEAR, gd.year);
                        //count ++;
                        //Log.e("TAG", "--- " + gd.day + "----");
                        gd.roundValues();

                        dateWeek = DateHelper.getFormatedDate(weeklyCalendar, DateHelper.WEEK_SUN);

                        mapsGraphDriveEntry.get(DRIVE_SCORE_W).add(new BarEntry((float) gd.avg_drive_score, mapsGraphDriveEntry.get(DRIVE_SCORE_W).size()));
                        mapsGraph.get(MILEAGE_W).add(new BarEntry((float) gd.avg_mileage, mapsGraph.get(MILEAGE_W).size()));
                        mapsGraph.get(COST_W).add(new BarEntry((float) gd.avg_cost, mapsGraph.get(COST_W).size()));
                        mapsGraph.get(DISTANCE_W).add(new BarEntry((float) gd.avg_distance, mapsGraph.get(DISTANCE_W).size()));
                        mapsGraph.get(DURATION_W).add(new BarEntry((float) gd.avg_duration, mapsGraph.get(DURATION_W).size()));

                        mapsGraphDriveEntry.get(DRIVE_SCORE_W).get(mapsGraphDriveEntry.get(DRIVE_SCORE_W).size() - 1).setData(DateHelper.getFormatedDate(weeklyCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN));
                        mapsGraphDriveEntry.get(DRIVE_SCORE_W).get(mapsGraphDriveEntry.get(DRIVE_SCORE_W).size() - 1).setData(null);
                        mapsGraph.get(MILEAGE_W).get(mapsGraph.get(MILEAGE_W).size() - 1).setData(new GraphType(GraphType.MILEAGE, DateHelper.getFormatedDate(weeklyCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(COST_W).get(mapsGraph.get(COST_W).size() - 1).setData(new GraphType(GraphType.COST, DateHelper.getFormatedDate(weeklyCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DURATION_W).get(mapsGraph.get(DURATION_W).size() - 1).setData(new GraphType(GraphType.DURATION, DateHelper.getFormatedDate(weeklyCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DISTANCE_W).get(mapsGraph.get(DISTANCE_W).size() - 1).setData(new GraphType(GraphType.DISTANCE, DateHelper.getFormatedDate(weeklyCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));

                        //inserting week name
                        mapsGraphXVals.get(WEEKLY).add(dateWeek);
                    }

                    ArrayList<GraphData> monthly = GraphData.readAllByMonthlyRule(carId);
                    Calendar monthlyCalendar = Calendar.getInstance();

                    for(GraphData gd : monthly) {

                        monthlyCalendar.set(Calendar.DAY_OF_MONTH, gd.day);
                        monthlyCalendar.set(Calendar.MONTH, gd.month);
                        monthlyCalendar.set(Calendar.YEAR, gd.year);
                        //count ++;
                        //Log.e("TAG", "--- " + monthlyCalendar.get(Calendar.DAY_OF_MONTH) + "----");
                        gd.roundValues();


                        dateDay = DateHelper.getFormatedDate(monthlyCalendar, DateHelper.DATE);
                        //Log.e("TAG", dateDay);
                        mapsGraphDriveEntry.get(DRIVE_SCORE_M).add(new BarEntry((float)gd.avg_drive_score, mapsGraphDriveEntry.get(DRIVE_SCORE_M).size()));
                        mapsGraph.get(MILEAGE_M).add(new BarEntry((float) gd.avg_mileage, mapsGraph.get(MILEAGE_M).size()));
                        mapsGraph.get(COST_M).add(new BarEntry((float) gd.avg_cost, mapsGraph.get(COST_M).size()));
                        mapsGraph.get(DISTANCE_M).add(new BarEntry((float) gd.avg_distance, mapsGraph.get(DISTANCE_M).size()));
                        mapsGraph.get(DURATION_M).add(new BarEntry((float) gd.avg_duration, mapsGraph.get(DURATION_M).size()));

                        mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(mapsGraphDriveEntry.get(DRIVE_SCORE_M).size() - 1).setData(DateHelper.getFormatedDate(monthlyCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN));
                        mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(mapsGraphDriveEntry.get(DRIVE_SCORE_M).size() - 1).setData(null);
                        mapsGraph.get(MILEAGE_M).get(mapsGraph.get(MILEAGE_M).size() - 1).setData(new GraphType(GraphType.MILEAGE, DateHelper.getFormatedDate(monthlyCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(COST_M).get(mapsGraph.get(COST_M).size() - 1).setData(new GraphType(GraphType.COST, DateHelper.getFormatedDate(monthlyCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DURATION_M).get(mapsGraph.get(DURATION_M).size() - 1).setData(new GraphType(GraphType.DURATION, DateHelper.getFormatedDate(monthlyCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DISTANCE_M).get(mapsGraph.get(DISTANCE_M).size() - 1).setData(new GraphType(GraphType.DISTANCE, DateHelper.getFormatedDate(monthlyCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));

                        mapsGraphXVals.get(MONTHLY).add(dateDay);

                    }

                    //TODO : make it compatible for year 2017 data
                    Calendar monthYearly = Calendar.getInstance();

                    ArrayList<Integer> dayOfTheYearForMonths = new ArrayList<Integer>();
                    dayOfTheYearForMonths.add(1);
                    dayOfTheYearForMonths.add(32);
                    dayOfTheYearForMonths.add(61);
                    dayOfTheYearForMonths.add(92);
                    dayOfTheYearForMonths.add(122);
                    dayOfTheYearForMonths.add(153);
                    dayOfTheYearForMonths.add(183);
                    dayOfTheYearForMonths.add(214);
                    dayOfTheYearForMonths.add(245);
                    dayOfTheYearForMonths.add(275);
                    dayOfTheYearForMonths.add(306);
                    dayOfTheYearForMonths.add(336);


                    double totalDistanceForTheYear = 0.0;
                    double avgDriveScore_y = 0.0;
                    double totalDriveScoreDotDistance = 0.0;

                    for(int i : dayOfTheYearForMonths)
                    {

                        ArrayList<GraphData> oneMonth = GraphData.readSpecificMonth(String.valueOf(carId),i);

                        monthYearly.set(Calendar.DAY_OF_YEAR,i);
                        dateYear = DateHelper.getFormatedDate(monthYearly, DateHelper.MONTH_JAN);
                        //Log.e("TAG",dateYear);
                        mapsGraphDriveEntry.get(DRIVE_SCORE_Y).add(new BarEntry((float)oneMonth.get(0).monthwiseDriveScoreValue, mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size()));
                        mapsGraph.get(MILEAGE_Y).add(new BarEntry((float) oneMonth.get(0).monthwiseMileageValue, mapsGraph.get(MILEAGE_Y).size()));
                        mapsGraph.get(COST_Y).add(new BarEntry((float) oneMonth.get(0).monthwiseCostValue, mapsGraph.get(COST_Y).size()));
                        mapsGraph.get(DISTANCE_Y).add(new BarEntry((float) oneMonth.get(0).monthwiseDistanceValue, mapsGraph.get(DISTANCE_Y).size()));
                        String val = getDurationString((int)oneMonth.get(0).monthwiseDurationValue);
                        mapsGraph.get(DURATION_Y).add(new BarEntry((float) Float.parseFloat(val), mapsGraph.get(DURATION_Y).size()));

                        mapsGraphDriveEntry.get(DRIVE_SCORE_Y).get(mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size() - 1).setData(DateHelper.getFormatedDate(monthYearly, DateHelper.DATE + "-" + DateHelper.MONTH_JAN));
                        mapsGraphDriveEntry.get(DRIVE_SCORE_Y).get(mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size() - 1).setData(null);
                        mapsGraph.get(MILEAGE_Y).get(mapsGraph.get(MILEAGE_Y).size() - 1).setData(new GraphType(GraphType.MILEAGE, DateHelper.getFormatedDate(monthYearly, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(COST_Y).get(mapsGraph.get(COST_Y).size() - 1).setData(new GraphType(GraphType.COST, DateHelper.getFormatedDate(monthYearly, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DURATION_Y).get(mapsGraph.get(DURATION_Y).size() - 1).setData(new GraphType(GraphType.DURATION, DateHelper.getFormatedDate(monthYearly, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DISTANCE_Y).get(mapsGraph.get(DISTANCE_Y).size() - 1).setData(new GraphType(GraphType.DISTANCE, DateHelper.getFormatedDate(monthYearly, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));

                        mapsGraphXVals.get(YEARLY).add(dateYear);

                        totalDistanceForTheYear = totalDistanceForTheYear + oneMonth.get(0).monthwiseDistanceValue;
                        //Log.e("Monthly values - ",totalDistanceForTheYear + "");
                        double driveScoreDotDistanceForCurrentDay = oneMonth.get(0).monthwiseDistanceValue*oneMonth.get(0).monthwiseDriveScoreValue;
                        totalDriveScoreDotDistance = totalDriveScoreDotDistance + driveScoreDotDistanceForCurrentDay;
                        if(totalDistanceForTheYear!=0)
                        {
                            avgDriveScore_y = totalDriveScoreDotDistance / totalDistanceForTheYear;
                            thisYearDriveScore = avgDriveScore_y;
                        }

                    }
                    //Log.e("YEARLY VALUE",thisYearDriveScore+"");

                    thisWeekDriveScore = GraphData.fetchAvgValueForCurrentWeek(String.valueOf(carId)).get(0).weekLabel;
                    thisMonthDriveScore = GraphData.fetchAvgValueForCurrentMonth(String.valueOf(carId)).get(0).monthwiseDriveScoreValue;

                    /*
                    int counter = 0;
                    double lastDistance = 0;
                    //Calculating AVG Drive score that is to be shown above drive score line charts for MONTHLY VIEW
                    for (int i1 = mapsGraphDriveEntry.get(DRIVE_SCORE_M).size() - 1; i1 >= 0; i1--) {

                        Object data = mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(i1).getData();
                        if (data != null && counter <= 30) {
                            GraphData graph = (GraphData) data;
                            if (graph.avg_distance * lastDistance == 0) {
                                thisMonthDriveScore = graph.avg_drive_score;
                                lastDistance = graph.avg_distance;
                            } else if (counter <= 30) {
//                            thisMonthDriveScore += mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(i1).getVal();
                                if ((graph.avg_distance * lastDistance) > 0) {
                                    thisMonthDriveScore = (((graph.avg_drive_score * graph.avg_distance) + (thisMonthDriveScore * lastDistance)) / (graph.avg_distance + lastDistance));//Used the formula to calculate the avg
                                    lastDistance = graph.avg_distance + lastDistance;
                                }
                            }
                        }
                        counter++;
                    }

                    lastDistance = 0;
                    counter = 0;
                    //Calculating AVG Drive score that is to be shown above drive score line charts for WEEKLY VIEW
                    for (int i1 = mapsGraphDriveEntry.get(DRIVE_SCORE_W).size() - 1; i1 >= 0; i1--) {

                        Object data = mapsGraphDriveEntry.get(DRIVE_SCORE_W).get(i1).getData();
                        if (data != null && counter <= 7) {

                            GraphData graph = (GraphData) data;
                            if (graph.avg_distance * lastDistance == 0) {
                                thisWeekDriveScore = graph.avg_drive_score;
                                lastDistance = graph.avg_distance;
                            } else if (counter <= 7) {
//                            thisMonthDriveScore += mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(i1).getVal();
                                if ((graph.avg_distance * lastDistance) > 0) {
                                    thisWeekDriveScore = (((graph.avg_drive_score * graph.avg_distance) + (thisWeekDriveScore * lastDistance)) / (graph.avg_distance + lastDistance));//Used the formula to calculate the avg
                                    lastDistance = graph.avg_distance + lastDistance;
                                }
                            }
                        }
                        counter++;
                    }

                    lastDistance = 0;
                    counter = 0;
                    //Calculating AVG Drive score that is to be shown above drive score line charts for YEARLY VIEW
                    for (int i1 = mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size() - 1; i1 >= 0; i1--) {

                        Object data = mapsGraphDriveEntry.get(DRIVE_SCORE_Y).get(i1).getData();
                        if (data != null && counter <= 12) {
                            GraphData graph = (GraphData) data;
                            if (graph.avg_distance * lastDistance == 0) {
                                thisYearDriveScore = graph.avg_drive_score;
                                lastDistance = graph.avg_distance;
                            } else if (counter <= 12) {
//                            thisYearDriveScore += mapsGraphDriveEntry.get(DRIVE_SCORE_Y).get(i1).getVal();
                                if ((graph.avg_distance * lastDistance) > 0) {
                                    thisYearDriveScore = (((graph.avg_drive_score * graph.avg_distance) + (thisYearDriveScore * lastDistance)) / (graph.avg_distance + lastDistance));//Used the formula to calculate the avg
                                    lastDistance = graph.avg_distance + lastDistance;
                                }
                            }
                            lastDistance = graph.avg_distance + lastDistance;
                        }
                        counter++;
                    }
                    */
                    thisWeekDriveScore = Utility.roundToInt(thisWeekDriveScore);
                    thisMonthDriveScore = Utility.roundToInt(thisMonthDriveScore);
                    thisYearDriveScore = Utility.roundToInt(thisYearDriveScore);

                    yValsDriveScore = mapsGraphDriveEntry.get(DRIVE_SCORE_W);
                    xValsDriveScore = mapsGraphXVals.get(WEEKLY);
                    txtDriveScoreGraph.setText(((int) thisWeekDriveScore) + "");
                    txtDriveScoreThisWeek.setText("This Week");

                    int i =0;
                    if (mapsGraph.get(MILEAGE_Y).size() >= 12 && mapsGraphXVals.get(YEARLY).size() >= 12) {

                        Collections.rotate(mapsGraphDriveEntry.get(DRIVE_SCORE_Y).subList(0, 12), -3);
                        Collections.rotate(mapsGraph.get(MILEAGE_Y).subList(0, 12), -3);
                        Collections.rotate(mapsGraph.get(COST_Y).subList(0, 12), -3);
                        Collections.rotate(mapsGraph.get(DURATION_Y).subList(0, 12), -3);
                        Collections.rotate(mapsGraph.get(DISTANCE_Y).subList(0, 12), -3);

                        Collections.rotate(mapsGraphXVals.get(YEARLY).subList(0, 12), -3);

                        //Reseting the x values in sequence to 0,1,2,3..
                        i = 0;
                        for (BarEntry barEntry : mapsGraph.get(MILEAGE_Y)) {
                            barEntry.setXIndex(i++);
                        }
                        i = 0;
                        for (BarEntry barEntry : mapsGraph.get(COST_Y)) {
                            barEntry.setXIndex(i++);
                        }
                        i = 0;
                        for (BarEntry barEntry : mapsGraph.get(DURATION_Y)) {
                            barEntry.setXIndex(i++);
                        }
                        i = 0;
                        for (BarEntry barEntry : mapsGraph.get(DISTANCE_Y)) {
                            barEntry.setXIndex(i++);
                        }

                        i = 0;
                        for (Entry entry : mapsGraphDriveEntry.get(DRIVE_SCORE_Y)) {
                            entry.setXIndex(i++);
                        }
                    }

                    //Resting the graphs
                    if(spinnerGraphDuration != null)
                    {
                        spinnerGraphDuration.setSelection(0);
                        /*
                        View item_view = (View)spinnerGraphDuration.getChildAt(item_postion);
                        long item_id = spinnerGraphDuration.getAdapter().getItemId(item_postion);
                        boolean gad = spinnerGraphDuration.performItemClick(item_view, 0, item_id);
                        Log.e("-----TAG-------", gad + " ");
                        */
                    }
                    entriesInSinglePage = limitsDuration[spinnerGraphDuration.getSelectedItemPosition()];
                    resetGraphs();

                    //Loading drive score chart
                    if(spinnerDriveScoreGraphDuration != null)
                    {

                        spinnerDriveScoreGraphDuration.setSelection(0);
                        /*
                        View item_view = (View)spinnerDriveScoreGraphDuration.getChildAt(item_postion);
                        long item_id = spinnerDriveScoreGraphDuration.getAdapter().getItemId(item_postion);
                        boolean gad = spinnerDriveScoreGraphDuration.performItemClick(item_view, 0, item_id);
                        Log.e("-----TAG-------", gad + " ");
                        */
                    }
                    entriesInSinglePage = limitsDuration[spinnerDriveScoreGraphDuration.getSelectedItemPosition()];
                    loadMileageGraphNew();
                }
            }
        });
    }


    private String getDurationString(int seconds) {

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return hours + "." +minutes;
    }

    private String twoDigitString(int number) {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }

    private double getPrice(String fuelType) {
        if (ConstantCode.FUEL_TYPE_OTHER.equalsIgnoreCase(fuelType)) {
            return ConstantCode.DEFAULT_PETROL_PRICE;
        } else if (ConstantCode.FUEL_TYPE_DIESEL.equalsIgnoreCase(fuelType)) {
            return ConstantCode.DEFAULT_DIESEL_PRICE;
        } else if (ConstantCode.FUEL_TYPE_PETROL.equalsIgnoreCase(fuelType)) {
            return ConstantCode.DEFAULT_PETROL_PRICE;
        }
        return ConstantCode.DEFAULT_PETROL_PRICE;
    }

    /**
     * ALL THE LOGIC TO LOAD THE CHARTS AND CATEGORIZING THE VALUES BASED ON DURATION WEEKLY, MONTHLY, YEARLY
     */
    private void loadAllGraphs() {
        final String carId = getArguments().getString(ConstantCode.INTENT_CAR_ID) + "";
        listGraphs = Graph.readAll(carId);

        Graph.readAllAsync(carId, new OnReadData<Graph>() {
            @Override
            public void onRead(Graph type, ArrayList<Graph> list) {

                mapsGraphXVals = new HashMap<>();
                mapsGraphXVals.put(WEEKLY, new ArrayList<String>());
                mapsGraphXVals.put(MONTHLY, new ArrayList<String>());
                mapsGraphXVals.put(YEARLY, new ArrayList<String>());

                mapsGraph = new HashMap<>();
                mapsGraphDriveEntry = new HashMap<>();

                mapsGraphDriveEntry.put(DRIVE_SCORE_W, new ArrayList<Entry>());
                mapsGraphDriveEntry.put(DRIVE_SCORE_M, new ArrayList<Entry>());
                mapsGraphDriveEntry.put(DRIVE_SCORE_Y, new ArrayList<Entry>());

                mapsGraph.put(MILEAGE_W, new ArrayList<BarEntry>());
                mapsGraph.put(MILEAGE_M, new ArrayList<BarEntry>());
                mapsGraph.put(MILEAGE_Y, new ArrayList<BarEntry>());

                mapsGraph.put(COST_W, new ArrayList<BarEntry>());
                mapsGraph.put(COST_M, new ArrayList<BarEntry>());
                mapsGraph.put(COST_Y, new ArrayList<BarEntry>());

                mapsGraph.put(DISTANCE_W, new ArrayList<BarEntry>());
                mapsGraph.put(DISTANCE_M, new ArrayList<BarEntry>());
                mapsGraph.put(DISTANCE_Y, new ArrayList<BarEntry>());

                mapsGraph.put(DURATION_W, new ArrayList<BarEntry>());
                mapsGraph.put(DURATION_M, new ArrayList<BarEntry>());
                mapsGraph.put(DURATION_Y, new ArrayList<BarEntry>());

                //-----------------
                Calendar calendar = Calendar.getInstance();
                String dateYear;
                String dateDay;
                String dateWeek;

                ArrayList<Graph> listYear = new ArrayList<>();

                String weekStartdate = "";
                //-----------------------

                //Inserting starting blank values from starting of month till the first entry in Graph Table
                if (listGraphs != null && listGraphs.size() > 0) {
                    Calendar calendarToday = Calendar.getInstance();
                    calendarToday.setTimeInMillis(listGraphs.get(0).timestamp);
                    //DAY
                    Calendar lastDayCalendar = (Calendar) calendarToday.clone();
                    lastDayCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    for (int day = 1; day < calendarToday.get(Calendar.DAY_OF_MONTH); day++) {
                        lastDayCalendar.set(Calendar.DAY_OF_MONTH, day);
                        dateDay = DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE);

                        mapsGraphDriveEntry.get(DRIVE_SCORE_M).add(new BarEntry(0.0f, mapsGraphDriveEntry.get(DRIVE_SCORE_M).size()));
                        mapsGraph.get(MILEAGE_M).add(new BarEntry(0.0f, mapsGraph.get(MILEAGE_M).size()));
                        mapsGraph.get(COST_M).add(new BarEntry(0.0f, mapsGraph.get(COST_M).size()));
                        mapsGraph.get(DURATION_M).add(new BarEntry(0.0f, mapsGraph.get(DURATION_M).size()));
                        mapsGraph.get(DISTANCE_M).add(new BarEntry(0.0f, mapsGraph.get(DISTANCE_M).size()));

                        //Performing calculation
                        mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(mapsGraphDriveEntry.get(DRIVE_SCORE_M).size() - 1).setData(DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN));
                        mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(mapsGraphDriveEntry.get(DRIVE_SCORE_M).size() - 1).setData(null);
                        mapsGraph.get(MILEAGE_M).get(mapsGraph.get(MILEAGE_M).size() - 1).setData(new GraphType(GraphType.MILEAGE, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(COST_M).get(mapsGraph.get(COST_M).size() - 1).setData(new GraphType(GraphType.COST, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DURATION_M).get(mapsGraph.get(DURATION_M).size() - 1).setData(new GraphType(GraphType.DURATION, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DISTANCE_M).get(mapsGraph.get(DISTANCE_M).size() - 1).setData(new GraphType(GraphType.DISTANCE, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));

//                        if (mapsGraph.get(MILEAGE_M).size() % SKIP_X_VALUES == 0)
                        mapsGraphXVals.get(MONTHLY).add(dateDay);
                    }


                    //WEEK
                    lastDayCalendar = (Calendar) calendarToday.clone();
                    lastDayCalendar.set(Calendar.DAY_OF_WEEK, 1);
                    weekStartdate = lastDayCalendar.get(Calendar.DAY_OF_MONTH) + "-" + lastDayCalendar.get(Calendar.MONTH) + "-" + lastDayCalendar.get(Calendar.YEAR);

                    //Inserting starting blank values for week from starting of day of week till the entry in Graph Table
                    for (int day = 1; day < calendarToday.get(Calendar.DAY_OF_WEEK); day++) {
                        lastDayCalendar.set(Calendar.DAY_OF_WEEK, day);
                        dateWeek = DateHelper.getFormatedDate(lastDayCalendar, DateHelper.WEEK_SUN);
                        mapsGraphXVals.get(WEEKLY).add(dateWeek);

                        mapsGraphDriveEntry.get(DRIVE_SCORE_W).add(new Entry(0.0f, mapsGraphDriveEntry.get(DRIVE_SCORE_W).size()));

                        mapsGraph.get(MILEAGE_W).add(new BarEntry(0.0f, mapsGraph.get(MILEAGE_W).size()));
                        mapsGraph.get(COST_W).add(new BarEntry(0.0f, mapsGraph.get(COST_W).size()));
                        mapsGraph.get(DURATION_W).add(new BarEntry(0.0f, mapsGraph.get(DURATION_W).size()));
                        mapsGraph.get(DISTANCE_W).add(new BarEntry(0.0f, mapsGraph.get(DISTANCE_W).size()));

                        //Performing calculation
                        mapsGraphDriveEntry.get(DRIVE_SCORE_W).get(mapsGraphDriveEntry.get(DRIVE_SCORE_W).size() - 1).setData(DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN));
                        mapsGraphDriveEntry.get(DRIVE_SCORE_W).get(mapsGraphDriveEntry.get(DRIVE_SCORE_W).size() - 1).setData(null);
                        mapsGraph.get(MILEAGE_W).get(mapsGraph.get(MILEAGE_W).size() - 1).setData(new GraphType(GraphType.MILEAGE, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(COST_W).get(mapsGraph.get(COST_W).size() - 1).setData(new GraphType(GraphType.COST, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DURATION_W).get(mapsGraph.get(DURATION_W).size() - 1).setData(new GraphType(GraphType.DURATION, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DISTANCE_W).get(mapsGraph.get(DISTANCE_W).size() - 1).setData(new GraphType(GraphType.DISTANCE, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                    }

                    //YEAR
                    lastDayCalendar = (Calendar) calendarToday.clone();

                    //Inserting starting blank values from starting of month of current year till the starting month of first trip
                    for (int month = 0; month < calendarToday.get(Calendar.MONTH); month++) {
                        lastDayCalendar.set(Calendar.MONTH, month);
                        dateYear = DateHelper.getFormatedDate(lastDayCalendar, DateHelper.MONTH_JAN);
                        mapsGraphXVals.get(YEARLY).add(dateYear);

                        mapsGraphDriveEntry.get(DRIVE_SCORE_Y).add(new BarEntry(0.0f, mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size()));
                        mapsGraph.get(MILEAGE_Y).add(new BarEntry(0.0f, mapsGraph.get(MILEAGE_Y).size()));
                        mapsGraph.get(COST_Y).add(new BarEntry(0.0f, mapsGraph.get(COST_Y).size()));
                        mapsGraph.get(DURATION_Y).add(new BarEntry(0.0f, mapsGraph.get(DURATION_Y).size()));
                        mapsGraph.get(DISTANCE_Y).add(new BarEntry(0.0f, mapsGraph.get(DISTANCE_Y).size()));


                        //Performing calculation
                        mapsGraphDriveEntry.get(DRIVE_SCORE_Y).get(mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size() - 1).setData(DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN));
                        mapsGraphDriveEntry.get(DRIVE_SCORE_Y).get(mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size() - 1).setData(null);
                        mapsGraph.get(MILEAGE_Y).get(mapsGraph.get(MILEAGE_Y).size() - 1).setData(new GraphType(GraphType.MILEAGE, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(COST_Y).get(mapsGraph.get(COST_Y).size() - 1).setData(new GraphType(GraphType.COST, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DURATION_Y).get(mapsGraph.get(DURATION_Y).size() - 1).setData(new GraphType(GraphType.DURATION, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DISTANCE_Y).get(mapsGraph.get(DISTANCE_Y).size() - 1).setData(new GraphType(GraphType.DISTANCE, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                    }

                    int i = 0;
                    boolean startForWeekAdding = false;

                    //Iterating over graph values that are day wise basis and inserting it in list
                    for (Graph graph : listGraphs) {

                        graph.roundValues();

                        calendar.setTimeInMillis(graph.timestamp);

                        dateDay = DateHelper.getFormatedDate(calendar, DateHelper.DATE);

                        mapsGraphDriveEntry.get(DRIVE_SCORE_M).add(new BarEntry((float) graph.avg_drive_score, mapsGraphDriveEntry.get(DRIVE_SCORE_M).size()));
                        mapsGraph.get(MILEAGE_M).add(new BarEntry((float) graph.avg_mileage, mapsGraph.get(MILEAGE_M).size()));
                        mapsGraph.get(COST_M).add(new BarEntry((float) graph.avg_cost, mapsGraph.get(COST_M).size()));
                        mapsGraph.get(DISTANCE_M).add(new BarEntry((float) graph.avg_distance, mapsGraph.get(DISTANCE_M).size()));
                        mapsGraph.get(DURATION_M).add(new BarEntry((float) graph.avg_duration, mapsGraph.get(DURATION_M).size()));

                        mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(mapsGraphDriveEntry.get(DRIVE_SCORE_M).size() - 1).setData(DateHelper.getFormatedDate(calendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN));
                        mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(mapsGraphDriveEntry.get(DRIVE_SCORE_M).size() - 1).setData(graph);
                        mapsGraph.get(MILEAGE_M).get(mapsGraph.get(MILEAGE_M).size() - 1).setData(new GraphType(GraphType.MILEAGE, DateHelper.getFormatedDate(calendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(COST_M).get(mapsGraph.get(COST_M).size() - 1).setData(new GraphType(GraphType.COST, DateHelper.getFormatedDate(calendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DURATION_M).get(mapsGraph.get(DURATION_M).size() - 1).setData(new GraphType(GraphType.DURATION, DateHelper.getFormatedDate(calendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DISTANCE_M).get(mapsGraph.get(DISTANCE_M).size() - 1).setData(new GraphType(GraphType.DISTANCE, DateHelper.getFormatedDate(calendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));

                        mapsGraphXVals.get(MONTHLY).add(dateDay);

                        dateWeek = DateHelper.getFormatedDate(calendar, DateHelper.WEEK_SUN);

                        mapsGraphDriveEntry.get(DRIVE_SCORE_W).add(new Entry((float) graph.avg_drive_score, mapsGraphDriveEntry.get(DRIVE_SCORE_W).size()));
                        mapsGraph.get(MILEAGE_W).add(new BarEntry((float) graph.avg_mileage, mapsGraph.get(MILEAGE_W).size()));
                        mapsGraph.get(COST_W).add(new BarEntry((float) graph.avg_cost, mapsGraph.get(COST_W).size()));
                        mapsGraph.get(DISTANCE_W).add(new BarEntry((float) graph.avg_distance, mapsGraph.get(DISTANCE_W).size()));
                        mapsGraph.get(DURATION_W).add(new BarEntry((float) graph.avg_duration, mapsGraph.get(DURATION_W).size()));

                        mapsGraphDriveEntry.get(DRIVE_SCORE_W).get(mapsGraphDriveEntry.get(DRIVE_SCORE_W).size() - 1).setData(DateHelper.getFormatedDate(calendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN));
                        mapsGraphDriveEntry.get(DRIVE_SCORE_W).get(mapsGraphDriveEntry.get(DRIVE_SCORE_W).size() - 1).setData(graph);
                        mapsGraph.get(MILEAGE_W).get(mapsGraph.get(MILEAGE_W).size() - 1).setData(new GraphType(GraphType.MILEAGE, DateHelper.getFormatedDate(calendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(COST_W).get(mapsGraph.get(COST_W).size() - 1).setData(new GraphType(GraphType.COST, DateHelper.getFormatedDate(calendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DURATION_W).get(mapsGraph.get(DURATION_W).size() - 1).setData(new GraphType(GraphType.DURATION, DateHelper.getFormatedDate(calendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DISTANCE_W).get(mapsGraph.get(DISTANCE_W).size() - 1).setData(new GraphType(GraphType.DISTANCE, DateHelper.getFormatedDate(calendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));

                        //inserting week name
                        mapsGraphXVals.get(WEEKLY).add(dateWeek);

                        if (i == 0) {
                            listYear.add(graph);
                        } else {
                            Calendar calendarLast, calendarCurrent;
                            calendarLast = Calendar.getInstance();
                            calendarCurrent = Calendar.getInstance();
                            calendarLast.setTimeInMillis(listYear.get(listYear.size() - 1).timestamp);
                            calendarCurrent.setTimeInMillis(graph.timestamp);
                            long monthsBetween = monthsBetween(calendarCurrent, calendarLast);

                            //populating the yearly graph
                            if (monthsBetween == 0) {
                                listYear.get(listYear.size() - 1).avg_drive_score = ((listYear.get(listYear.size() - 1).avg_drive_score * listYear.get(listYear.size() - 1).avg_distance) + (graph.avg_drive_score * graph.avg_distance)) / (listYear.get(listYear.size() - 1).avg_distance + graph.avg_distance);
                                listYear.get(listYear.size() - 1).avg_mileage = ((listYear.get(listYear.size() - 1).avg_mileage * listYear.get(listYear.size() - 1).avg_distance) + (graph.avg_mileage * graph.avg_distance)) / (listYear.get(listYear.size() - 1).avg_distance + graph.avg_distance);
                                listYear.get(listYear.size() - 1).avg_cost = (listYear.get(listYear.size() - 1).avg_mileage / (listYear.get(listYear.size() - 1).avg_distance + graph.avg_distance)) * ConstantCode.DEFAULT_PETROL_PRICE;
                                listYear.get(listYear.size() - 1).avg_distance = listYear.get(listYear.size() - 1).avg_distance + graph.avg_distance;
                                listYear.get(listYear.size() - 1).avg_duration = listYear.get(listYear.size() - 1).avg_duration + graph.avg_duration;
                            } else {

                                //inserting intermediate days if ther is gap in MONTHS
                                if (ConstantCode.ENTER_INTERMEDIATE_DAYS_IN_CHARTS) {
                                    if (monthsBetween > 0) {
                                        for (int month = 0; month < monthsBetween - 1; month++) {
                                            Graph blankGraph = new Graph();
                                            calendarLast.add(Calendar.MONTH, 1);
                                            blankGraph.timestamp = calendar.getTimeInMillis();
                                            listYear.add(blankGraph);
                                        }
                                    }
                                }

                                Graph blankGraph = new Graph();
                                blankGraph.avg_drive_score = graph.avg_drive_score;
                                blankGraph.avg_mileage = graph.avg_mileage;
                                blankGraph.avg_cost = graph.avg_cost;
                                blankGraph.avg_distance = graph.avg_distance;
                                blankGraph.avg_duration = graph.avg_duration;
                                blankGraph.timestamp = graph.timestamp;
                                calendarLast.setTimeInMillis(graph.timestamp);
                                listYear.add(blankGraph);
                            }
                        }
                        i++;
                    }

                    //Filling Future zero entries depending on weekly, monthly, yearly
                    //DAY
                    lastDayCalendar = (Calendar) calendar.clone();
                    Calendar futureDayCalendar = Calendar.getInstance();
                    futureDayCalendar.set(Calendar.DAY_OF_MONTH, futureDayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    long daysBetween = daysBetween(lastDayCalendar, futureDayCalendar);
                    for (int day = 0; day < daysBetween; day++) {

                        lastDayCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        dateDay = DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE);
                        mapsGraphDriveEntry.get(DRIVE_SCORE_M).add(new BarEntry(0.0f, mapsGraphDriveEntry.get(DRIVE_SCORE_M).size()));
                        mapsGraph.get(MILEAGE_M).add(new BarEntry(0.0f, mapsGraph.get(MILEAGE_M).size()));
                        mapsGraph.get(COST_M).add(new BarEntry(0.0f, mapsGraph.get(COST_M).size()));
                        mapsGraph.get(DURATION_M).add(new BarEntry(0.0f, mapsGraph.get(DURATION_M).size()));
                        mapsGraph.get(DISTANCE_M).add(new BarEntry(0.0f, mapsGraph.get(DISTANCE_M).size()));

                        //Performing the calculation
                        mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(mapsGraphDriveEntry.get(DRIVE_SCORE_M).size() - 1).setData(DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN));
                        mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(mapsGraphDriveEntry.get(DRIVE_SCORE_M).size() - 1).setData(null);
                        mapsGraph.get(MILEAGE_M).get(mapsGraph.get(MILEAGE_M).size() - 1).setData(new GraphType(GraphType.MILEAGE, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(COST_M).get(mapsGraph.get(COST_M).size() - 1).setData(new GraphType(GraphType.COST, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DURATION_M).get(mapsGraph.get(DURATION_M).size() - 1).setData(new GraphType(GraphType.DURATION, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DISTANCE_M).get(mapsGraph.get(DISTANCE_M).size() - 1).setData(new GraphType(GraphType.DISTANCE, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));

//                    if (mapsGraph.get(MILEAGE_M).size() % SKIP_X_VALUES == 0)
                        mapsGraphXVals.get(MONTHLY).add(dateDay);
                    }

                    //WEEK
                    lastDayCalendar = (Calendar) calendar.clone();
                    futureDayCalendar = Calendar.getInstance();
                    futureDayCalendar.add(Calendar.DAY_OF_MONTH, 7 - futureDayCalendar.get(Calendar.DAY_OF_WEEK));
                    daysBetween = daysBetween(lastDayCalendar, futureDayCalendar);
                    for (int day = 0; day < daysBetween; day++) {

                        lastDayCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        dateWeek = DateHelper.getFormatedDate(lastDayCalendar, DateHelper.WEEK_SUN);

                        mapsGraphDriveEntry.get(DRIVE_SCORE_W).add(new Entry(0.0f, mapsGraphDriveEntry.get(DRIVE_SCORE_W).size()));

                        mapsGraph.get(MILEAGE_W).add(new BarEntry(0.0f, mapsGraph.get(MILEAGE_W).size()));
                        mapsGraph.get(COST_W).add(new BarEntry(0.0f, mapsGraph.get(COST_W).size()));
                        mapsGraph.get(DURATION_W).add(new BarEntry(0.0f, mapsGraph.get(DURATION_W).size()));
                        mapsGraph.get(DISTANCE_W).add(new BarEntry(0.0f, mapsGraph.get(DISTANCE_W).size()));

                        mapsGraphDriveEntry.get(DRIVE_SCORE_W).get(mapsGraphDriveEntry.get(DRIVE_SCORE_W).size() - 1).setData(DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN));
                        mapsGraphDriveEntry.get(DRIVE_SCORE_W).get(mapsGraphDriveEntry.get(DRIVE_SCORE_W).size() - 1).setData(null);
                        mapsGraph.get(MILEAGE_W).get(mapsGraph.get(MILEAGE_W).size() - 1).setData(new GraphType(GraphType.MILEAGE, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(COST_W).get(mapsGraph.get(COST_W).size() - 1).setData(new GraphType(GraphType.COST, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DURATION_W).get(mapsGraph.get(DURATION_W).size() - 1).setData(new GraphType(GraphType.DURATION, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DISTANCE_W).get(mapsGraph.get(DISTANCE_W).size() - 1).setData(new GraphType(GraphType.DISTANCE, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));


                        mapsGraphXVals.get(WEEKLY).add(dateWeek);
                        i++;
                    }

                    Calendar calendarYear = Calendar.getInstance();
                    i = 0;
                    for (Graph listGraph : listYear) {
                        calendarYear.setTimeInMillis(listGraph.timestamp);
                        dateYear = DateHelper.getFormatedDate(calendarYear, DateHelper.MONTH_JAN);

                        mapsGraphDriveEntry.get(DRIVE_SCORE_Y).add(new BarEntry((float) listGraph.avg_drive_score, mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size()));
                        mapsGraph.get(MILEAGE_Y).add(new BarEntry((float) listGraph.avg_mileage, mapsGraph.get(MILEAGE_Y).size()));
                        mapsGraph.get(COST_Y).add(new BarEntry((float) listGraph.avg_cost, mapsGraph.get(COST_Y).size()));
                        mapsGraph.get(DURATION_Y).add(new BarEntry((float) listGraph.avg_duration, mapsGraph.get(DURATION_Y).size()));
                        mapsGraph.get(DISTANCE_Y).add(new BarEntry((float) listGraph.avg_distance, mapsGraph.get(DISTANCE_Y).size()));

                        mapsGraphDriveEntry.get(DRIVE_SCORE_Y).get(mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size() - 1).setData(DateHelper.getFormatedDate(calendarYear, DateHelper.DATE + "-" + DateHelper.MONTH_JAN));
                        mapsGraphDriveEntry.get(DRIVE_SCORE_Y).get(mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size() - 1).setData(listGraph);
                        mapsGraph.get(MILEAGE_Y).get(mapsGraph.get(MILEAGE_Y).size() - 1).setData(new GraphType(GraphType.MILEAGE, DateHelper.getFormatedDate(calendarYear, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(COST_Y).get(mapsGraph.get(COST_Y).size() - 1).setData(new GraphType(GraphType.COST, DateHelper.getFormatedDate(calendarYear, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DURATION_Y).get(mapsGraph.get(DURATION_Y).size() - 1).setData(new GraphType(GraphType.DURATION, DateHelper.getFormatedDate(calendarYear, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DISTANCE_Y).get(mapsGraph.get(DISTANCE_Y).size() - 1).setData(new GraphType(GraphType.DISTANCE, DateHelper.getFormatedDate(calendarYear, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));

                        mapsGraphXVals.get(YEARLY).add(dateYear);
                        i++;
                    }

                    lastDayCalendar = (Calendar) calendar.clone();
                    futureDayCalendar = Calendar.getInstance();

                    futureDayCalendar.set(Calendar.MONTH, 11);
                    for (int month = lastDayCalendar.get(Calendar.MONTH) + 1; month <= 11; month++) {
                        lastDayCalendar.set(Calendar.MONTH, month);
                        dateYear = DateHelper.getFormatedDate(lastDayCalendar, DateHelper.MONTH_JAN);

                        mapsGraphDriveEntry.get(DRIVE_SCORE_Y).add(new BarEntry(0.0f, mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size()));
                        mapsGraph.get(MILEAGE_Y).add(new BarEntry(0.0f, mapsGraph.get(MILEAGE_Y).size()));
                        mapsGraph.get(COST_Y).add(new BarEntry(0.0f, mapsGraph.get(COST_Y).size()));
                        mapsGraph.get(DURATION_Y).add(new BarEntry(0.0f, mapsGraph.get(DURATION_Y).size()));
                        mapsGraph.get(DISTANCE_Y).add(new BarEntry(0.0f, mapsGraph.get(DISTANCE_Y).size()));

                        mapsGraphDriveEntry.get(DRIVE_SCORE_Y).get(mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size() - 1).setData(DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN));
                        mapsGraphDriveEntry.get(DRIVE_SCORE_Y).get(mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size() - 1).setData(null);
                        mapsGraph.get(MILEAGE_Y).get(mapsGraph.get(MILEAGE_Y).size() - 1).setData(new GraphType(GraphType.MILEAGE, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(COST_Y).get(mapsGraph.get(COST_Y).size() - 1).setData(new GraphType(GraphType.COST, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DURATION_Y).get(mapsGraph.get(DURATION_Y).size() - 1).setData(new GraphType(GraphType.DURATION, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));
                        mapsGraph.get(DISTANCE_Y).get(mapsGraph.get(DISTANCE_Y).size() - 1).setData(new GraphType(GraphType.DISTANCE, DateHelper.getFormatedDate(lastDayCalendar, DateHelper.DATE + "-" + DateHelper.MONTH_JAN)));

                        mapsGraphXVals.get(YEARLY).add(dateYear);
                        i++;
                    }

                    //Making Yearly graph to financial year means starting from April so we are rotating the list reverse to 3 positions
                    if (mapsGraph.get(MILEAGE_Y).size() >= 12 && mapsGraphXVals.get(YEARLY).size() >= 12) {

                        Collections.rotate(mapsGraphDriveEntry.get(DRIVE_SCORE_Y).subList(0, 12), -3);
                        Collections.rotate(mapsGraph.get(MILEAGE_Y).subList(0, 12), -3);
                        Collections.rotate(mapsGraph.get(COST_Y).subList(0, 12), -3);
                        Collections.rotate(mapsGraph.get(DURATION_Y).subList(0, 12), -3);
                        Collections.rotate(mapsGraph.get(DISTANCE_Y).subList(0, 12), -3);

                        Collections.rotate(mapsGraphXVals.get(YEARLY).subList(0, 12), -3);

                        //Reseting the x values in sequence to 0,1,2,3..
                        i = 0;
                        for (BarEntry barEntry : mapsGraph.get(MILEAGE_Y)) {
                            barEntry.setXIndex(i++);
                        }
                        i = 0;
                        for (BarEntry barEntry : mapsGraph.get(COST_Y)) {
                            barEntry.setXIndex(i++);
                        }
                        i = 0;
                        for (BarEntry barEntry : mapsGraph.get(DURATION_Y)) {
                            barEntry.setXIndex(i++);
                        }
                        i = 0;
                        for (BarEntry barEntry : mapsGraph.get(DISTANCE_Y)) {
                            barEntry.setXIndex(i++);
                        }

                        i = 0;
                        for (Entry entry : mapsGraphDriveEntry.get(DRIVE_SCORE_Y)) {
                            entry.setXIndex(i++);
                        }
                    }

                    thisWeekDriveScore = 0;
                    thisMonthDriveScore = 0;
                    thisYearDriveScore = 0;
                    int counter = 0;
                    double lastDistance = 0;

                    //Calculating AVG Drive score that is to be shown above drive score line charts for MONTHLY VIEW
                    for (int i1 = mapsGraphDriveEntry.get(DRIVE_SCORE_M).size() - 1; i1 >= 0; i1--) {

                        Object data = mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(i1).getData();
                        if (data != null && counter <= 30) {
                            Graph graph = (Graph) data;
                            if (graph.avg_distance * lastDistance == 0) {
                                thisMonthDriveScore = graph.avg_drive_score;
                                lastDistance = graph.avg_distance;
                            } else if (counter <= 30) {
//                            thisMonthDriveScore += mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(i1).getVal();
                                if ((graph.avg_distance * lastDistance) > 0) {
                                    thisMonthDriveScore = (((graph.avg_drive_score * graph.avg_distance) + (thisMonthDriveScore * lastDistance)) / (graph.avg_distance + lastDistance));//Used the formula to calculate the avg
                                    lastDistance = graph.avg_distance + lastDistance;
                                }
                            }
                        }
                        counter++;
                    }

                    lastDistance = 0;
                    counter = 0;
                    //Calculating AVG Drive score that is to be shown above drive score line charts for WEEKLY VIEW
                    for (int i1 = mapsGraphDriveEntry.get(DRIVE_SCORE_W).size() - 1; i1 >= 0; i1--) {

                        Object data = mapsGraphDriveEntry.get(DRIVE_SCORE_W).get(i1).getData();
                        if (data != null && counter <= 7) {

                            Graph graph = (Graph) data;
                            if (graph.avg_distance * lastDistance == 0) {
                                thisWeekDriveScore = graph.avg_drive_score;
                                lastDistance = graph.avg_distance;
                            } else if (counter <= 7) {
//                            thisMonthDriveScore += mapsGraphDriveEntry.get(DRIVE_SCORE_M).get(i1).getVal();
                                if ((graph.avg_distance * lastDistance) > 0) {
                                    thisWeekDriveScore = (((graph.avg_drive_score * graph.avg_distance) + (thisWeekDriveScore * lastDistance)) / (graph.avg_distance + lastDistance));//Used the formula to calculate the avg
                                    lastDistance = graph.avg_distance + lastDistance;
                                }
                            }
                        }
                        counter++;
                    }

                    lastDistance = 0;
                    counter = 0;
                    //Calculating AVG Drive score that is to be shown above drive score line charts for YEARLY VIEW
                    for (int i1 = mapsGraphDriveEntry.get(DRIVE_SCORE_Y).size() - 1; i1 >= 0; i1--) {

                        Object data = mapsGraphDriveEntry.get(DRIVE_SCORE_Y).get(i1).getData();
                        if (data != null && counter <= 12) {
                            Graph graph = (Graph) data;
                            if (graph.avg_distance * lastDistance == 0) {
                                thisYearDriveScore = graph.avg_drive_score;
                                lastDistance = graph.avg_distance;
                            } else if (counter <= 12) {
//                            thisYearDriveScore += mapsGraphDriveEntry.get(DRIVE_SCORE_Y).get(i1).getVal();
                                if ((graph.avg_distance * lastDistance) > 0) {
                                    thisYearDriveScore = (((graph.avg_drive_score * graph.avg_distance) + (thisYearDriveScore * lastDistance)) / (graph.avg_distance + lastDistance));//Used the formula to calculate the avg
                                    lastDistance = graph.avg_distance + lastDistance;
                                }
                            }
                            lastDistance = graph.avg_distance + lastDistance;
                        }
                        counter++;
                    }

                    thisWeekDriveScore = Utility.roundToInt(thisWeekDriveScore);
                    thisMonthDriveScore = Utility.roundToInt(thisMonthDriveScore);
                    thisYearDriveScore = Utility.roundToInt(thisYearDriveScore);

                    yValsDriveScore = mapsGraphDriveEntry.get(DRIVE_SCORE_W);
                    xValsDriveScore = mapsGraphXVals.get(WEEKLY);
                    txtDriveScoreGraph.setText(((int) thisWeekDriveScore) + "");
                    txtDriveScoreThisWeek.setText("This Week");
                }
                //Resting the graphs
                resetGraphs();

                //Loading drive score chart
                loadMileageGraphNew();
            }
        });
    }

    private final String[] financialYearName = {"APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC", "JAN", "FEB", "MAR"};

    private String financialYearMonthName(Calendar calendar) {
        return financialYearName[calendar.get(Calendar.MONTH)];
    }

    /**
     * Loading the values from database.
     */
    private void loadFromDatabase() {
        final String carId = getArguments().getString(ConstantCode.INTENT_CAR_ID) + "";

        car = Cars.readSpecific(carId);

        //Checking the status of syncing if it is there then we need to show it in green banner at top else simply hide it
        /*
        String msg = PrefManager.getInstance().getString(String.valueOf(car.id), null);
        Log.e("TAG", "GREEN BANNER STATUS" + "------"+msg);
        if (msg != null) {
            txt_notification_title.setText(getString(R.string.msg_syncing_is_in_progress));
            txt_notification_title.setVisibility(View.VISIBLE);
        } else {
            txt_notification_title.setVisibility(View.GONE);
        }
        */
        if (getActivity() != null) {
            loadValues(car);

            //adding the marker on google map
            addTemporaryMarker(car, false);

            //loading graph values
            linearGraph.setVisibility(View.VISIBLE);
            linearFootprint.setVisibility(View.GONE);
            frmChildProgressbar.setVisibility(View.GONE);


        }
    }


    /**
     * Loading latest 3 trips from database and display at the end of screen
     *
     * @param id
     */
    private void loadRoutesFromDatabase(int id) {
        car = Cars.readSpecific(id + "");
        //loading max 3 trips from database with the flag whether we need to show shorts trip or not identified by car.showShortTrips
        ArrayList<TripDetailMain> list = TripDetailMain.readSpecificTrips(id + "", 3, car.showShortTrips);
        if (list == null) {
            btnViewAllTrips.setVisibility(View.GONE);
            return;
        } else if (list.size() > 0) {
            if (list.size() < 2) {
                btnViewAllTrips.setVisibility(View.GONE);
            } else {
                btnViewAllTrips.setVisibility(View.VISIBLE);
            }
            ((View) recyclerViewHighlights.getParent()).setVisibility(View.VISIBLE);
//            txtErrorNoTrip.setVisibility(View.GONE);
            txtErrorNoTrip.setText(getString(R.string.lbl_highlights));

        } else {
//            txtErrorNoTrip.setVisibility(View.VISIBLE);
            ((View) recyclerViewHighlights.getParent()).setVisibility(View.GONE);
            txtErrorNoTrip.setText(getString(R.string.lbl_no_trips));
            btnViewAllTrips.setVisibility(View.GONE);
        }
        customAdapter.setItem(list);
    }

    /**
     * Loading the drive score chart values
     */
    private void loadMileageGraphNew() {

        linechartDriverscore.setDrawGridBackground(false);

        // no description text
        linechartDriverscore.setDescription("");
        linechartDriverscore.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        linechartDriverscore.setTouchEnabled(true);

        // enable scaling and dragging
        linechartDriverscore.setDragEnabled(true);
        linechartDriverscore.setScaleEnabled(false);

        linechartDriverscore.setPinchZoom(false);

        XAxis xAxis = linechartDriverscore.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisLineColor(ContextCompat.getColor(mActivity, R.color.transparent));
        xAxis.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
        xAxis.setDrawAxisLine(true);
        xAxis.setGridColor(ContextCompat.getColor(mActivity, R.color.transparent));
        xAxis.setDrawGridLines(false);
        xAxis.setLabelsToSkip(0);

        if (driveScoreDurationCurrentPosition == 1)
            xAxis.setLabelsToSkip(SKIP_X_VALUES);
        else
            xAxis.setLabelsToSkip(0);


        linechartDriverscore.getAxisLeft().setEnabled(false);
        linechartDriverscore.getAxisRight().setEnabled(false);


        // add data
        setLineData();


        Legend l = linechartDriverscore.getLegend();
        linechartDriverscore.getLegend().setEnabled(false);


        l.setForm(Legend.LegendForm.LINE);


        if (linechartDriverscore.getData() != null) {
            linechartDriverscore.getData().setHighlightEnabled(false);


            List<ILineDataSet> sets = linechartDriverscore.getData()
                    .getDataSets();

            for (ILineDataSet iSet : sets) {

                LineDataSet set = (LineDataSet) iSet;

                set.setDrawCircles(true);
                set.setDrawValues(true);
                set.setDrawFilled(false);
                set.setDrawCubic(false);
                set.disableDashedLine();
                set.setLineWidth(2f);
            }
        }
        linechartDriverscore.setPinchZoom(false);


        if (yValsDriveScore != null && yValsDriveScore.size() > 0) {

            linechartDriverscore.setVisibleXRangeMinimum(entriesInSinglePage - 1);
            linechartDriverscore.setVisibleXRangeMaximum(entriesInSinglePage - 1);
            linechartDriverscore.moveViewToX(yValsDriveScore.size() - entriesInSinglePage + 1);


        }
    }


    boolean pendingTask = false;


    /**
     * Showing aggregated values like distance, trip_time, speed, mileage, drive_score
     *
     * @param car
     */
    private void loadValues(Cars car) {

        Calendar calendar = DateHelper.getCalendarFromServer(car.lut);

        Cursor cursor = ActiveAndroid.getDatabase().rawQuery("SELECT " +
                "SUM(distance) as distance, " +
                "SUM(trip_time) as time, " +
                "SUM(distance)/SUM(trip_time)*60*60 as speed, " +
                "SUM(avg_mileage*distance)/SUM(distance) as mileage, " +
                "SUM(drive_score*distance)/SUM(distance) as drive_score " +
                "FROM TripDetailMain where car_id=? GROUP BY car_id;", new String[]{car.id + ""});

        if (cursor != null && cursor.moveToFirst()) {
            String s_distance = cursor.getString(cursor.getColumnIndex("distance"));
            String s_time = cursor.getString(cursor.getColumnIndex("time"));
            String s_speed = cursor.getString(cursor.getColumnIndex("speed"));
            String s_mileage = cursor.getString(cursor.getColumnIndex("mileage"));
            String s_drive_score = cursor.getString(cursor.getColumnIndex("drive_score"));

            double distance = Utility.round(Double.parseDouble(s_distance), 2);
            double time = Utility.round(Double.parseDouble(s_time), 2);
            double speed = Utility.round(Double.parseDouble(s_speed), 2);
            double mileage = Utility.round(Double.parseDouble(s_mileage), 2);
            double drive_score = Utility.round(Double.parseDouble(s_drive_score), 2);

            double emissions_mycar_petrol = Utility.round((distance / mileage) * 2.392);
            double emissions_mycar_diesel = Utility.round((distance / mileage) * 2.64);
            double emissions_hybrid_car = Utility.round((70 * distance) / 1000);
            double emissions_public_transport = Utility.round((45 * distance) / 1000);

            Log.d("EMISSION", "emissions_mycar_petrol [" + emissions_mycar_petrol + "]");
            Log.d("EMISSION", "emissions_mycar_diesel [" + emissions_mycar_diesel + "]");
            Log.d("EMISSION", "emissions_hybrid_car [" + emissions_hybrid_car + "]");
            Log.d("EMISSION", "emissions_public_transport [" + emissions_public_transport + "]");

            int trees = 0;

            txtDistance.setText(distance + "");
            txtAvgSpeed.setText(speed + "");
            txtMileage.setText(mileage + "");
            txtDriveScore.setText(drive_score + "");

            txtTripTime.setText(Utility.getHrsMinsSec(time, "hh:mm"));
            driveScoreView.setScore(drive_score);
            txtDriveScoreGraph.setText(((int) drive_score) + "");
            mileageView.setScore(mileage);

            //Changing the background of drivescore chart as per the following condition
            if (drive_score > 0 && drive_score < 50) {
                ((View) linechartDriverscore.getParent()).setBackgroundResource(R.drawable.status_red_gradient);
            } else if (drive_score >= 50 && drive_score < 75) {
                ((View) linechartDriverscore.getParent()).setBackgroundResource(R.drawable.status_yellow_gradient);
            } else if (drive_score >= 75) {
                ((View) linechartDriverscore.getParent()).setBackgroundResource(R.drawable.status_green_gradient);
            }


            carbonFootprintHybridCar.setProgressText(emissions_hybrid_car + " KG CO2");//"2756KG"
            carbonFootprintHybridCar.setProgress(emissions_hybrid_car, 1000);

            carbonFootprintPublicTransportAvg.setProgressText(emissions_public_transport + " KG CO2");//"30KG"
            carbonFootprintPublicTransportAvg.setProgress(emissions_public_transport, 1000);//car.distance

            //Depending on the fuel type we are calculating the carbon footprint of car
            if (ConstantCode.FUEL_TYPE_OTHER.equalsIgnoreCase(car.fuel)) {
                trees = (int) (emissions_mycar_petrol / 21.77);
                carbonFootprintMyCar.setProgressText(emissions_mycar_petrol + " KG CO2");//"3000KG"
                carbonFootprintMyCar.setProgress(emissions_mycar_petrol, 1000);//car.emissions_mycar_petrol
                if (emissions_mycar_petrol * 100 / 1000 < 30)
                    carbonFootprintMyCar.isCar(true);
            } else if (ConstantCode.FUEL_TYPE_DIESEL.equalsIgnoreCase(car.fuel)) {
                trees = (int) (emissions_mycar_petrol / 21.77);
                carbonFootprintMyCar.setProgressText(emissions_mycar_diesel + " KG CO2");//"3000KG"
                carbonFootprintMyCar.setProgress(emissions_mycar_diesel, 1000);//car.emissions_mycar_petrol
                if (emissions_mycar_petrol * 100 / 1000 < 30)
                    carbonFootprintMyCar.isCar(true);
            } else if (ConstantCode.FUEL_TYPE_PETROL.equalsIgnoreCase(car.fuel)) {
                trees = (int) (emissions_mycar_petrol / 21.77);
                carbonFootprintMyCar.setProgressText(emissions_mycar_petrol + "KG CO2");//"3000KG"
                carbonFootprintMyCar.setProgress(emissions_mycar_petrol, 1000);//car.emissions_mycar_petrol
                if (emissions_mycar_petrol * 100 / 1000 < 30)
                    carbonFootprintMyCar.isCar(true);
            } else {
                trees = (int) (emissions_mycar_petrol / 21.77);
                carbonFootprintMyCar.setProgressText(emissions_mycar_petrol + "KG CO2");//"3000KG"
                carbonFootprintMyCar.setProgress(emissions_mycar_petrol, 1000);//car.emissions_mycar_petrol
                if (emissions_mycar_petrol * 100 / 1000 < 30)
                    carbonFootprintMyCar.isCar(true);
            }

            txtNoOfTrees.setText(getString(R.string.lbl_X_Trees, trees + ""));
            TreesAdapter treesAdapter = new TreesAdapter(trees);
            recyclerViewTrees.setAdapter(treesAdapter);

        } else {
            txtNoOfTrees.setText(getString(R.string.lbl_X_Trees, "0"));
            carbonFootprintHybridCar.setProgressText(0 + " KG CO2");//"2756KG"
            carbonFootprintHybridCar.setProgress(0, 1000);

            carbonFootprintMyCar.setProgressText(0 + " KG CO2");//"3000KG"
            carbonFootprintMyCar.setProgress(0, 1000);//car.emissions_mycar_petrol

            carbonFootprintPublicTransportAvg.setProgressText(0 + " KG CO2");//"30KG"
            carbonFootprintPublicTransportAvg.setProgress(0, 1000);//car.distance
        }
        cursor.close();

    }


    private final String WEEKLY = "weekly";
    private final String MONTHLY = "monthly";
    private final String YEARLY = "yearly";

    private final String DRIVE_SCORE_W = "drive_score_W";
    private final String DRIVE_SCORE_M = "drive_score_M";
    private final String DRIVE_SCORE_Y = "drive_score_Y";

    private final String MILEAGE_W = "mileage_W";
    private final String MILEAGE_M = "mileage_m";
    private final String MILEAGE_Y = "mileage_y";

    private final String COST_W = "cost_w";
    private final String COST_M = "cost_m";
    private final String COST_Y = "cost_y";

    private final String DISTANCE_W = "distance_w";
    private final String DISTANCE_M = "distance_m";
    private final String DISTANCE_Y = "distance_y";

    private final String DURATION_W = "duration_w";
    private final String DURATION_M = "duration_m";
    private final String DURATION_Y = "duration_y";

    //Data structures to hold the list for various charts of mileage, cost, duration, distance
    HashMap<String, ArrayList<Entry>> mapsGraphDriveEntry = new HashMap<>();
    HashMap<String, ArrayList<BarEntry>> mapsGraph = new HashMap<>();
    HashMap<String, ArrayList<String>> mapsGraphXVals = new HashMap<>();

    private String getYearlyName(Calendar calendar) {
        return Utility.getMonth(calendar.get(Calendar.MONTH)) + "\n" + calendar.get(Calendar.YEAR);
    }

    private String getMonthlyName(Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_MONTH) + "\n" + Utility.getMonth(calendar.get(Calendar.MONTH));
    }

    private String getWeeklyName(Calendar calendar) {
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US) + "\n" + calendar.get(Calendar.DAY_OF_MONTH) + " " + Utility.getMonth(calendar.get(Calendar.MONTH));
    }

    /**
     * Getting the month between two calendar objects(2 dates)
     *
     * @param maxDate
     * @param minDate
     * @return
     */
    public long monthsBetween(Calendar maxDate, Calendar minDate) {

        int diffYear = maxDate.get(Calendar.YEAR) - minDate.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + maxDate.get(Calendar.MONTH) - minDate.get(Calendar.MONTH);

        return diffMonth;
/*        long max = maxDate.get(Calendar.MONTH);
        long min = minDate.get(Calendar.MONTH);
        return max - min;*/
    }

    /**
     * Used to get the daysbetween to calendar objects(2 dates)
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public long daysBetween(Calendar startDate, Calendar endDate) {
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.MILLISECOND, 0);

        long end = endDate.getTimeInMillis();
        long start = startDate.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
    }

    private boolean yearlyComparison(Calendar calendarLast, Calendar calendarCurrent) {
        return calendarLast.get(Calendar.YEAR) == calendarCurrent.get(Calendar.YEAR) && calendarLast.get(Calendar.MONTH) == calendarCurrent.get(Calendar.MONTH);
    }

    private boolean montlyComparison(Calendar calendarLast, Calendar calendarCurrent) {
        return calendarLast.get(Calendar.YEAR) == calendarCurrent.get(Calendar.YEAR) && calendarLast.get(Calendar.MONTH) == calendarCurrent.get(Calendar.MONTH) && calendarLast.get(Calendar.DAY_OF_MONTH) == calendarCurrent.get(Calendar.DAY_OF_MONTH);
    }

    private boolean weeklyComparison(Calendar calendarLast, Calendar calendarCurrent) {
        return calendarLast.get(Calendar.YEAR) == calendarCurrent.get(Calendar.YEAR) && calendarLast.get(Calendar.MONTH) == calendarCurrent.get(Calendar.MONTH) && calendarLast.get(Calendar.DAY_OF_MONTH) == calendarCurrent.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Reseting the bar charts when user clicks on type of charts views ie. Mileage, cost, duration, distance OR time selection drop down ie. weekly, monthly, yearly
     */
    private void resetBarChart() {

        mBarChart.setDescription("");

        mBarChart.setPinchZoom(false);
        mBarChart.setDoubleTapToZoomEnabled(false);

        mBarChart.setDrawBarShadow(false);
        mBarChart.getAxisLeft().setEnabled(false);
        mBarChart.getAxisRight().setEnabled(false);
        mBarChart.setDrawGridBackground(false);

        mBarChart.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.dark_gray));
        XAxis xAxisBar = mBarChart.getXAxis();
        xAxisBar.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisBar.setSpaceBetweenLabels(0);
        xAxisBar.setDrawGridLines(false);

        if (durationCurrentPosition == 1)
            xAxisBar.setLabelsToSkip(SKIP_X_VALUES);
        else
            xAxisBar.setLabelsToSkip(0);

        xAxisBar.setTextColor(ContextCompat.getColor(mActivity, R.color.white));

        mBarChart.getAxisLeft().setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        mBarChart.getAxisLeft().setDrawGridLines(false);//TODO changed to  true->false
        mBarChart.getAxisLeft().setEnabled(true);


        setBarChartData();

        mBarChart.getLegend().setEnabled(false);

        mBarChart.setDrawValueAboveBar(false);


        MapMarkerView mv = new MapMarkerView(mActivity, R.layout.custom_marker_view);
        mBarChart.setMarkerView(mv);


        YAxisValueFormatter custom = new MyYAxisValueFormatter();

        YAxis leftAxis = mBarChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(0f);
        leftAxis.setGranularity(0.0f);
        leftAxis.setTextColor(mActivity.getResources().getColor(R.color.white));

        //Skipping to show last entries
        if (yVals != null && yVals.size() > 0) {
            mBarChart.setVisibleXRangeMinimum(entriesInSinglePage);
            mBarChart.setVisibleXRangeMaximum(entriesInSinglePage);
            mBarChart.moveViewToX(yVals.size() - entriesInSinglePage);
        }
        mBarChart.invalidate();
    }

    private void setBarChartData() {
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        if (yVals == null) {
            yVals = new ArrayList<>();
        }
        if (xVals == null) {
            xVals = new ArrayList<>();
        }
        Log.e("x vas: ",xVals + "");
        BarDataSet set1 = new BarDataSet(yVals, "Data Set");
        List<Integer> list = new ArrayList<>();
        list.add(ContextCompat.getColor(mActivity, R.color.light_gray));
        /*list.add(ContextCompat.getColor(mActivity, R.color.light_gray));
        list.add(ContextCompat.getColor(mActivity, R.color.light_gray));

        list.add(ContextCompat.getColor(mActivity, R.color.light_gray));
        list.add(ContextCompat.getColor(mActivity, R.color.purple));*/
        set1.setColors(list);

        set1.setDrawValues(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(xVals, dataSets);

        mBarChart.setData(data);
        mBarChart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if (e == null)
            return;

        RectF bounds = mBarChart.getBarBounds((BarEntry) e);
        PointF position = mBarChart.getPosition(e, YAxis.AxisDependency.LEFT);


        Log.i("bounds", bounds.toString());
        Log.i("position", position.toString());

        Log.i("x-index",
                "low: " + mBarChart.getLowestVisibleXIndex() + ", high: "
                        + mBarChart.getHighestVisibleXIndex());
    }

    @Override
    public void onNothingSelected() {

    }

    public GoogleMap getMap() {
        return googleMap;
    }


    /**
     * Trees Adapter used to show the number of tress on emission chart
     */
    public static class TreesAdapter extends RecyclerView.Adapter {

        int size;
        private Context context;

        public TreesAdapter(int size) {
            this.size = size;
        }

        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            this.context = parent.getContext();
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.inflate_trees, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return size;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {


            public ViewHolder(View v) {
                super(v);

            }
        }

    }

    //filling data for linechart i.e Mileage Graph on top
    private void setLineData() {


        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yValsDriveScore, "");

        set1.disableDashedLine();
        set1.setColor(Color.WHITE);
        set1.setCircleColor(Color.GREEN);
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
        LineData data = new LineData(xValsDriveScore, dataSets);

        // set data
        linechartDriverscore.setData(data);
        linechartDriverscore.setAutoScaleMinMaxEnabled(false);
        linechartDriverscore.setDoubleTapToZoomEnabled(true);
        linechartDriverscore.setPinchZoom(true);
        linechartDriverscore.setDragEnabled(true);
    }


    @Override
    public void onResume() {
        super.onResume();

//        scheduleNextUpdate();
        try {
            if (getArguments() != null && getArguments().containsKey(ConstantCode.INTENT_CAR_ID)) {

                loadRoutesFromDatabase(Integer.parseInt(getArguments().getString(ConstantCode.INTENT_CAR_ID)));
                checkForGreenBanner(Integer.parseInt(getArguments().getString(ConstantCode.INTENT_CAR_ID)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(car != null)
        {
            CharSequence relativeTime=null;
            long time = PrefManager.getInstance().getLong(String.valueOf(car.id)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,-1);
            Calendar todayTime = Calendar.getInstance();
            if(time != -1)
            {
                if (todayTime.getTimeInMillis() - time > 0) {
                    //getting minutes between the last sync time and current time
                    long min = TimeUnit.MILLISECONDS.toMinutes(Math.abs(todayTime.getTimeInMillis() - time));

                    relativeTime = DateUtils.getRelativeTimeSpanString(time, todayTime.getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, 0);
                    if (relativeTime != null) {
                        txtForLastSyncTime.setText("Last Sync At : "+relativeTime);
                        txtForLastSyncTime.setVisibility(View.VISIBLE);
                    }

                }
            }
        }
        else
        {
            txtForLastSyncTime.setVisibility(View.GONE);
        }



        //making car specific getRecentTripCalls
        if(car!=null)
        {

            if(PrefManager.getInstance().contains(ConstantCode.TRIP_SYNC_FLAG))
            {

                if(!PrefManager.getBoolean(ConstantCode.TRIP_SYNC_FLAG))
                {
                    Log.e("CALL FROM : ", "onResume");
                    ActivityAllTrips.getRecentTripsDetail(App.getContext(), car.id, car.serverTripID, car.local_latest_trip, car.serverFlag);
                }
                else
                {
                    Log.e("CALL FROM : ", "Already syncing wait for it to complete");
                }
            }
            else
            {
                Log.e("CALL FROM : ", "onResume");
                ActivityAllTrips.getRecentTripsDetail(App.getContext(), car.id, car.serverTripID, car.local_latest_trip, car.serverFlag);
            }
        }
    }

    public void checkForGreenBanner(int carID)
    {
        String msg = PrefManager.getInstance().getString(String.valueOf(carID), null);
        Log.e(TAG, "GREEN BANNER STATUS" + "------"+msg);
        if (msg != null) {
            txt_notification_title.setText(getString(R.string.msg_syncing_is_in_progress));
            txt_notification_title.setVisibility(View.VISIBLE);
        } else {
            txt_notification_title.setVisibility(View.GONE);
        }
    }


    /**
     * [START - Registering for the updating car garage changes and sync status and values if last sync or not.]
     */
    boolean isRegistered = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Garage Reciver", "onReceive() called with: " + "context = [" + context + "], intent = [" + intent + "]");
            if (intent.hasExtra(ConstantCode.INTENT_ACTION) && ConstantCode.ACTION_SYNC_COMPLETE.equalsIgnoreCase(intent.getStringExtra(ConstantCode.INTENT_ACTION))) {
                if (intent.hasExtra(ConstantCode.INTENT_CAR_ID)) {
                    if (car != null && car.id == intent.getIntExtra(ConstantCode.INTENT_CAR_ID, 0))
                    {
                        fillValuesForCar(car.id + "");
                        if(intent.hasExtra(ConstantCode.INTENT_DATA))
                        {
                            CharSequence relativeTime=null;
                            long time = intent.getLongExtra(ConstantCode.INTENT_DATA,0);
                            Calendar todayTime = Calendar.getInstance();
                            if (todayTime.getTimeInMillis() - time > 0) {
                                //getting minutes between the last sync time and current time
                                long min = TimeUnit.MILLISECONDS.toMinutes(Math.abs(todayTime.getTimeInMillis() - time));

                                relativeTime = DateUtils.getRelativeTimeSpanString(time, todayTime.getTimeInMillis(),DateUtils.MINUTE_IN_MILLIS, 0);
                                if (relativeTime != null) {
                                    txtForLastSyncTime.setText("Last Sync At : "+relativeTime);
                                    txtForLastSyncTime.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                        else
                        {
                            CharSequence relativeTime=null;
                            if(PrefManager.getInstance().contains(String.valueOf(car.id)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA))
                            {
                                long time = PrefManager.getLong(String.valueOf(car.id)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA);

                                Calendar todayTime = Calendar.getInstance();
                                if (todayTime.getTimeInMillis() - time > 0) {

                                    //getting minutes between the last sync time and current time
                                    long min = TimeUnit.MILLISECONDS.toMinutes(Math.abs(todayTime.getTimeInMillis() - time));
                                    Log.e("ATG","I m Here" + min);
                                    relativeTime = DateUtils.getRelativeTimeSpanString(time, todayTime.getTimeInMillis(),DateUtils.MINUTE_IN_MILLIS, 0);
                                    if (relativeTime != null) {
                                        txtForLastSyncTime.setText("Last Sync At : "+relativeTime);
                                        txtForLastSyncTime.setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                        }
                        //txt_notification_title.setVisibility(View.GONE);
                    }
                    txt_notification_title.setVisibility(View.GONE);
                }
                //TODO :
                /*
                if(PrefManager.getInstance().contains(ConstantCode.LAST_SYNC_TIME_FOR_TRIPS))
                {
                    Calendar cal = Calendar.getInstance();
                    long time = intent.getLongExtra(ConstantCode.INTENT_DATA,0);
                    if(cal.getTimeInMillis()-time > ConstantCode.THRESHOLD_FOR_LAST_SYNC_RECENT_TRIPS_TEXT)
                    {
                        if(time != 0)
                        {
                            Date date = new Date(time);
                            DateFormat formatter = new SimpleDateFormat("h:mm a,d MMM yyyy");
                            String dateFormatted = formatter.format(date);
                            String text = "Last Sync at : " + dateFormatted;
                            txtForLastSyncTime.setText(text);
                            txtForLastSyncTime.setVisibility(View.VISIBLE);
                        }
                    }

                }
                */
            } else if (intent.hasExtra(ConstantCode.INTENT_ACTION) && ConstantCode.ACTION_GARAGE_UPDATED.equalsIgnoreCase(intent.getStringExtra(ConstantCode.INTENT_ACTION))) {
                if (intent.hasExtra(ConstantCode.INTENT_DATA)) {
                    try {
                        JSONObject json = new JSONObject(intent.getStringExtra(ConstantCode.INTENT_DATA));

                        //when garage screen is updated then we are checking if the current car is there then we are updating the marker.
                        ArrayList<Cars> listCars = Utility.parseArrayFromString(json.optString(ConstantCode.data), Cars[].class);
                        int carId = Integer.parseInt(getArguments().getString(ConstantCode.INTENT_CAR_ID) + "");
                        if (listCars.size() > 0) {
                            for (Cars listCar : listCars) {
                                if (listCar.id == carId) {
                                    addTemporaryMarker(listCar, true);
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else if(intent.hasExtra(ConstantCode.INTENT_ACTION) && ConstantCode.ACTION_SYNC_STARTED.equalsIgnoreCase(intent.getStringExtra(ConstantCode.INTENT_ACTION)))
            {
                int carNumber = intent.getIntExtra(ConstantCode.INTENT_CAR_ID,-1);
                if(carNumber != -1)
                {
                    if(carNumber == Integer.parseInt(getArguments().getString(ConstantCode.INTENT_CAR_ID)))
                    {
                        if(PrefManager.getInstance().getString(String.valueOf(carNumber),null) != null)
                        {
                            txt_notification_title.setText(PrefManager.getInstance().getString(String.valueOf(carNumber),null));
                            txt_notification_title.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            Log.e(TAG,String.valueOf(carNumber)+" is not syncing");
                        }
                    }
                    else
                    {
                        Log.e(TAG, "Not syncing for this car "+String.valueOf(carNumber));
                    }
                }
                else
                {
                    Log.e(TAG, "Car Number in BroadcastRecieve "+String.valueOf(carNumber));
                }
            }
        }
    };

    /**
     * Registering to locale broadcast manager to stay updated with garage api calls so we can update the location and sync time
     */
    private void registerForSyncStatus() {
        if (isRegistered == false) {
            LocalBroadcastManager.getInstance(mActivity).registerReceiver(receiver, new IntentFilter(ConstantCode.BROADCAST_TRIP_SYNCED));
            LocalBroadcastManager.getInstance(mActivity).registerReceiver(receiver, new IntentFilter(ConstantCode.BROADCAST_SHOW_TRIP_SYNC_BANNER));
            isRegistered = true;
        }
    }

    /**
     * Unregistering local broadcast
     */
    private void unregisterForSyncStatus() {
        if (isRegistered == true) {
            LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(receiver);
            isRegistered = false;
        }
    }
    //[END- Registering for the updating car garage changes and sync status and values if last sync or not.]
}
