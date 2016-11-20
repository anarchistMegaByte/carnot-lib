package com.carnot.activity;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.custom_views.CounterAppTextViewMedium;
import com.carnot.custom_views.DriveScoreView;
import com.carnot.custom_views.DriverScoreViewProgressBar;
import com.carnot.custom_views.MileageView;
import com.carnot.custom_views.TouchableSupportMapFragment;
import com.carnot.custom_views.expandablelayout.ExpandableRelativeLayout;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.DateHelper;
import com.carnot.libclasses.EasyPermissions;
import com.carnot.libclasses.googlemaps.GoogleMapUtils;
import com.carnot.models.Graph;
import com.carnot.models.Routes;
import com.carnot.models.TripDetailMain;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity that shows trip detail with routes on google map.
 */

/**
 * Created by root on 11/4/16.
 */
public class ActivityTripDetails extends BaseActivity {

    private static final int MAP_LINE = 10;
//    GoogleMap googleMap;

    //    DriverScoreViewProgressBar driverScoreViewProgressBarClutchUsage;
    DriverScoreViewProgressBar driverScoreViewProgressBar;
//    ExpandableRelativeLayout expandableClutchUsage, expandableSpeeding, expandableIdlingTime;

    ExpandableRelativeLayout expandableOverspeeding, expandableHardAccBrake, expandableEcoDriving, expandableIdling, expandableGoodGearChange;
    CounterAppTextViewMedium counterOverspeeding, counterHardAccBrake, counterEcoDriving, counterIdling, counterGoodGearChange;

    NestedScrollView scrollView;
    GoogleMap mapMain;
    GoogleMap mapOverspeeding, mapHardAccBrake, mapEcoDriving, mapIdling, mapGoodGearChange;
    ProgressBar progressBarMap;
    ArrayList<Graph> listGraphs;
    private TripDetailMain tripDetail;

    ArrayList<BarEntry> yVals;
    DriveScoreView driveScoreView;
    MileageView mileageView;
    ImageView imgPhoto;
    BarChart mBarChart;
    ProgressBar graphProgressBar;
    ArrayList<String> xVals = new ArrayList<String>();
    TextView txtName, txtMileage, txtDriveScore, txtDateTime, txtAddress, txtDistance, txtTripTime, txtAvgSpeed;
    private ArrayList<LatLng> list;
    private boolean isServerRequest = true;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        SharedElementTransitionHelper.enableTransition(mActivity);

        setContentView(R.layout.activity_trip_detail);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle("Manufacturer XXXX");
        ((Toolbar) links(R.id.toolbar)).setBackgroundResource(R.color.black_translucent);

        if (getIntent().hasExtra(ConstantCode.INTENT_TRIP_ID)) {
            getSupportActionBar().setTitle(getIntent().getStringExtra(ConstantCode.INTENT_CAR_NAME));
        }
        if (getIntent() != null && getIntent().hasExtra(ConstantCode.INTENT_TRIP_ID)) {

            tripDetail = TripDetailMain.readSpecific(getIntent().getStringExtra(ConstantCode.INTENT_TRIP_ID));
            //tripDetail = (TripDetailMain) Utility.parseFromString(getIntent().getStringExtra(ConstantCode.INTENT_DATA), TripDetailMain.class);

            //tripDetail = (Trip) getIntent().getSerializableExtra(ConstantCode.INTENT_DATA);
        }
    }

    Toolbar toolbar;

    @Override
    public void initView() {

        //Initializing only main map, we will initialize other maps when path is drown successfully on main Map
        initMaps(R.id.map);


        graphProgressBar = (ProgressBar) links(R.id.graph_progress_bar);
        mBarChart = (BarChart) links(R.id.barchart);

        toolbar = (Toolbar) links(R.id.toolbar);

        graphProgressBar.setVisibility(View.GONE);
        mBarChart.setVisibility(View.GONE);

        scrollView = (NestedScrollView) links(R.id.scroll_view);

        counterOverspeeding = (CounterAppTextViewMedium) links(R.id.counter_txt_overspeeding);
        counterHardAccBrake = (CounterAppTextViewMedium) links(R.id.counter_txt_hard_acc_brake);
        counterEcoDriving = (CounterAppTextViewMedium) links(R.id.counter_txt_eco_driving);
        counterIdling = (CounterAppTextViewMedium) links(R.id.counter_txt_idling);
        counterGoodGearChange = (CounterAppTextViewMedium) links(R.id.counter_txt_good_gear_change);

        expandableOverspeeding = (ExpandableRelativeLayout) links(R.id.expandable_overspeeding);
        expandableHardAccBrake = (ExpandableRelativeLayout) links(R.id.expandable_hard_acc_brake);
        expandableEcoDriving = (ExpandableRelativeLayout) links(R.id.expandable_eco_driving);
        expandableIdling = (ExpandableRelativeLayout) links(R.id.expandable_idling);
        expandableGoodGearChange = (ExpandableRelativeLayout) links(R.id.expandable_good_gear_change);

        expandableOverspeeding.setExpanded(false);
        expandableHardAccBrake.setExpanded(false);
        expandableEcoDriving.setExpanded(false);
        expandableIdling.setExpanded(false);
        expandableGoodGearChange.setExpanded(false);

        progressBarMap = (ProgressBar) links(R.id.progress_bar_map);


        driverScoreViewProgressBar = (DriverScoreViewProgressBar) links(R.id.driver_score_view_progressBar);


        driveScoreView = (DriveScoreView) findViewById(R.id.drive_score_view);
        mileageView = (MileageView) findViewById(R.id.mileage_view);
        imgPhoto = (ImageView) findViewById(R.id.img_photo);

        txtMileage = (TextView) findViewById(R.id.txt_mileage);
        txtDriveScore = (TextView) findViewById(R.id.txt_drive_score);
        txtDistance = (TextView) findViewById(R.id.txt_distance);
        txtTripTime = (TextView) findViewById(R.id.txt_trip_time);
        txtAvgSpeed = (TextView) findViewById(R.id.txt_avg_speed);

        txtDateTime = (TextView) findViewById(R.id.txt_date_time);
        txtAddress = (TextView) findViewById(R.id.txt_address);

        progressBarMap.setVisibility(View.VISIBLE);

    }

    @Override
    public void postInitView() {

        if (tripDetail != null) {

            driveScoreView.setScore((int) tripDetail.drive_score);
            txtDriveScore.setText(String.valueOf(tripDetail.drive_score));
            txtMileage.setText(String.valueOf(tripDetail.avg_mileage));
            mileageView.setScore((int) tripDetail.avg_mileage);
            Utility.showCircularImageView(ActivityTripDetails.this, imgPhoto, (String) tripDetail.photo, R.drawable.ic_profile_circle_black);

            driverScoreViewProgressBar.setProgress((int) tripDetail.drive_score, 100);

            counterOverspeeding.setCounter(tripDetail.os_sc, tripDetail.os_m);
            counterHardAccBrake.setCounter(tripDetail.hab_sc, tripDetail.hab_m);
            counterEcoDriving.setCounter(tripDetail.ed_sc, tripDetail.ed_m);
            counterIdling.setCounter(tripDetail.i_sc, tripDetail.i_m);
            counterGoodGearChange.setCounter(tripDetail.gc_sc, tripDetail.gc_m);
            String date = DateHelper.getFormatedDate(tripDetail.start_time, new String[]{DateHelper.DATE_FORMAT_TRIP_SERVER1, DateHelper.DATE_FORMAT_TRIP_SERVER2}, DateHelper.DATE_FORMAT_TRIP_LOCAL, true, true);
            txtDateTime.setText(date);
            if (!TextUtils.isEmpty(tripDetail.start) && !TextUtils.isEmpty(tripDetail.end)) {
                txtAddress.setText(" | " + tripDetail.start + " - " + tripDetail.end);
            } else if (!TextUtils.isEmpty(tripDetail.start)) {
                txtAddress.setText(" | " + tripDetail.start);
            } else if (!TextUtils.isEmpty(tripDetail.end)) {
                txtAddress.setText(" | " + tripDetail.end);
            } else {
                txtAddress.setText("");
            }
        }
    }

    public float getActionBar_Toolbar_Bottom_Height() {
        return ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0).getHeight() - getSupportActionBar().getHeight();
//        return Utility.getDeviceWidthHeight(mActivity)[1] - getSupportActionBar().getHeight();
    }

    RelativeLayout frameLayoutMaps;

    private void initMaps(final int res) {

        frameLayoutMaps = (RelativeLayout) links(R.id.rel_main_view);
        frameLayoutMaps.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) frameLayoutMaps.getLayoutParams();

                lp.height = (int) getActionBar_Toolbar_Bottom_Height();

                frameLayoutMaps.setLayoutParams(lp);
            }
        });

        TouchableSupportMapFragment mSupportMapFragment = (TouchableSupportMapFragment) getSupportFragmentManager().findFragmentById(res);

        if (mSupportMapFragment != null) {

            if (mSupportMapFragment instanceof TouchableSupportMapFragment) {
                ((TouchableSupportMapFragment) mSupportMapFragment).setListener(new TouchableSupportMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                    }
                });
            }
            Log.d("PATHONMAP", "start loading map");
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    if (googleMap != null) {
                        if (res == R.id.map_overspeeding) {
                            ActivityTripDetails.this.mapOverspeeding = googleMap;
                            expandableOverspeeding.setExpanded(false);

                        } else if (res == R.id.map_hard_acc_brake) {
                            ActivityTripDetails.this.mapHardAccBrake = googleMap;
                            expandableHardAccBrake.setExpanded(false);

                        } else if (res == R.id.map_eco_driving) {
                            ActivityTripDetails.this.mapEcoDriving = googleMap;
                            expandableEcoDriving.setExpanded(false);

                        } else if (res == R.id.map_idle) {
                            ActivityTripDetails.this.mapIdling = googleMap;
                            expandableIdling.setExpanded(false);

                        } else if (res == R.id.map_good_gear_change) {
                            ActivityTripDetails.this.mapGoodGearChange = googleMap;
                            expandableGoodGearChange.setExpanded(false);

                        } else {
                            ActivityTripDetails.this.mapMain = googleMap;

                            mapMain.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                                @Override
                                public void onMapLoaded() {

                                    Log.d("PATHONMAP", "get map on loaded");
                                    if (list != null) {
                                        showPathOnMap(mapMain, list, isServerRequest, R.id.map);
                                        mapMainDrawnSuccess = true;
                                    }
                                    isMapMainLoaded = true;
//                                    populateValues(tripDetail);
                                }
                            });
                            Log.d("PATHONMAP", "get map on async");


                        }

                        googleMap.getUiSettings().setAllGesturesEnabled(true);
                    }
                }
            });
        }
    }

    /**
     * Reading the points from "points"string and making the list of LatLng so that we can plot the marker(red dot) on map
     *
     * @param map
     * @param points
     */
    private void addIntermediatePoints(final GoogleMap map, final String points) {
        Log.d("EVENTPOINT", "addIntermediatePoints() called with: " + "map = [" + map + "], points = [" + points + "]");
        if(points != null)
        {
            //Spliting all the lat long that are seperated by ,(comma)
            String[] allPoints = points.split(",");
            MarkerOptions markerOptions = new MarkerOptions();
            if (allPoints.length > 0) {
                Log.d("EVENTPOINT", "allpoints length: " + allPoints.length);
                LatLng latLngPoint = null;
                for (String allPoint : allPoints) {
                    String[] ltlng = allPoint.split("_");
                    if (ltlng.length == 2) {
                        try {
                            latLngPoint = new LatLng(Double.parseDouble(ltlng[0]), Double.parseDouble(ltlng[1]));
                            markerOptions.position(latLngPoint);
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_errorcode_red));
                            markerOptions.anchor(0.5f, 0.5f);
                            map.addMarker(markerOptions);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("EVENTPOINT", e.getMessage());
                        }
                    } else {
                        Log.d("EVENTPOINT", "ltlng : " + ltlng);
                    }
                }
            }
        }

    }


    @Override
    public void addAdapter() {

        //Adding click listener so we can plot marker on map
        counterOverspeeding.setOnClickListener(clickListener);
        counterHardAccBrake.setOnClickListener(clickListener);
        counterEcoDriving.setOnClickListener(clickListener);
        counterIdling.setOnClickListener(clickListener);
        counterGoodGearChange.setOnClickListener(clickListener);


    }

    @Override
    public void loadData() {

        if (tripDetail.jsonRoutes == null) {
            Log.d("PATHONMAP", "start loading from db");
            TripDetailMain trip = TripDetailMain.readSpecific(tripDetail.trip_id + "");
            Log.d("PATHONMAP", "get from db");
            if (trip != null) {
                tripDetail = trip;
            }
        }

        //set values
        driveScoreView.setScore(Double.parseDouble(tripDetail.drive_score + ""));
        txtDriveScore.setText(String.valueOf(tripDetail.drive_score));
        txtMileage.setText(String.valueOf(tripDetail.avg_mileage));
        mileageView.setScore(tripDetail.avg_mileage);
        driverScoreViewProgressBar.setProgress((int) tripDetail.drive_score, 100);

        txtDistance.setText(tripDetail.distance + "");
        txtTripTime.setText(Utility.getHrsMinsSec(tripDetail.trip_time, "hh:mm") + "");
        txtAvgSpeed.setText(tripDetail.avg_speed + "");

        progressBarMap.setVisibility(View.VISIBLE);

        //We are making the list of latlong from database in background thread So it will not hang the UI
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                listRoutes = tripDetail.route;
                if (listRoutes.size() > 0) {
                    list = new ArrayList<LatLng>();
                    if (tripDetail.jsonRoutes != null && tripDetail.jsonRoutes.length() > 0) {

                        list = new ArrayList<LatLng>(Arrays.asList((LatLng[]) new Gson().fromJson(tripDetail.jsonRoutes, LatLng[].class)));

                        if (list != null && list.size() > 0) {
                            isServerRequest = false;
                        }
                    } else {
                        for (Routes listRoute : listRoutes) {
                            list.add(new LatLng(listRoute.latitude, listRoute.longitude));
                        }
                        isServerRequest = true;
                    }
                } else {
                    list = new ArrayList<LatLng>();
                    list.add(new LatLng(tripDetail.start_lat, tripDetail.start_lon));
                    isServerRequest = false;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mapMain != null && isMapMainLoaded && mapMainDrawnSuccess == false) {
                    showPathOnMap(mapMain, list, isServerRequest, R.id.map);
                }
            }
        }.execute();
    }

    boolean isMapMainLoaded = false;


    ArrayList<Routes> listRoutes;


    boolean mapMainDrawnSuccess = false;

    /**
     * Draw Starting and Ending Marker and Path between them on map
     *
     * @param map
     * @param list
     * @param isServerRequest
     * @param res
     */
    private void showPathOnMap(final GoogleMap map, final ArrayList<LatLng> list, boolean isServerRequest, final int res) {
        Log.e("Checking trip : ","Yes inm here");
        if (list == null)
            return;
        final Marker sMarker = GoogleMapUtils.setMarker(map, list.get(0).latitude, list.get(0).longitude, null, null, false, R.drawable.ic_location_purple_big);
        if (sMarker != null)
            sMarker.setSnippet("start");

        if (list.size() > 1) {
            final Marker eMarker = GoogleMapUtils.setMarker(map, list.get(list.size() - 1).latitude, list.get(list.size() - 1).longitude, null, null, false, R.drawable.ic_location_black_big);
            if (eMarker != null)
                eMarker.setSnippet("end");
        }
        Log.e("ACTIVITY TRIP DETAILS: ", list.size() +"");
        //TODO : mapp plotting changed.
        ActivityTripDetails.this.list = list;
        if(list.size() > 0)
        {

            ActivityTripDetails.this.isServerRequest = false;
            Gson gson = new Gson();
            tripDetail.jsonRoutes = gson.toJson(list);
            //tripDetail.save();
            TripDetailMain.replaceJsonPoints(tripDetail.trip_id, tripDetail.jsonRoutes);
            Log.e("Checkpoitn 2 : ","2");
            progressBarMap.setVisibility(View.GONE);

            //Initializing other maps once main map path is drown successfully so that it will not hang the UI
            if (res == R.id.map) {
                initRemainingMaps();
            }

            PolylineOptions lineOptions = new PolylineOptions();
            //listLatLngs = listPaths;
            lineOptions.addAll(list);
            lineOptions.width(MAP_LINE);
            lineOptions.color(Color.RED);
            if (lineOptions != null && map != null && lineOptions.getPoints().size() > 0) {
                lineOptions.width(MAP_LINE);
                lineOptions.color(Utility.getColor(ActivityTripDetails.this, R.color.colorAccent));
                map.addPolyline(lineOptions);
            }
        }


        /*
        //Show Path connecting to points in list
        GoogleMapUtils.showDirectionPathOnMapWithWayPoint(mActivity, map, list, isServerRequest, new GoogleMapUtils.DirectionCallback() {
            @Override
            public void processStarts() {
//                progressBarMap.setVisibility(View.VISIBLE);
            }

            @Override
            public void processFinish(ArrayList<LatLng> list) {
                ActivityTripDetails.this.list = list;
                ActivityTripDetails.this.isServerRequest = false;
                Gson gson = new Gson();
                tripDetail.jsonRoutes = gson.toJson(list);
                tripDetail.save();
                progressBarMap.setVisibility(View.GONE);

                //Initializing other maps once main map path is drown successfully so that it will not hang the UI
                if (res == R.id.map) {
                    initRemainingMaps();
                }
            }

            @Override
            public void processFailedWithNoRoute() {
                progressBarMap.setVisibility(View.GONE);
                //Log.e("MAIN YAHAN ---", "Tu kahan1  " + list.size());
                //Initializing other maps once main map loaded successfully so that it will not hang the UI
                if (res == R.id.map) {
                    initRemainingMaps();
                }
            }
        });
        */
        try {
            GoogleMapUtils.zoomToFitAllMarkersWithLat(mActivity, map, false, list.get(0), list.get(list.size() - 1), list.get(list.size() / 2));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Intialize bottom side maps
     */
    private void initRemainingMaps() {
        initMaps(R.id.map_overspeeding);
        initMaps(R.id.map_hard_acc_brake);
        initMaps(R.id.map_eco_driving);
        initMaps(R.id.map_idle);
        initMaps(R.id.map_good_gear_change);
    }

    /**
     * Managing click listener on bottom item, when items are clicked then we are showing path on map and adding the intermediate markers(red dot)
     */
    boolean overspeedingMapDone = false, hardAccBrakeMapDone, ecoDrivingMapDone, idlingMapDone, goodGearChangeMapDone;
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == counterOverspeeding.getId()) {
                if (overspeedingMapDone == false && list != null && mapOverspeeding != null) {

                    showPathOnMap(mapOverspeeding, list, isServerRequest, R.id.map_overspeeding);
                    addIntermediatePoints(mapOverspeeding, tripDetail.os_p);

                    mapOverspeeding.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            GoogleMapUtils.zoomToFitAllMarkersWithLat(mActivity, mapOverspeeding, false, list.get(0), list.get(list.size() - 1), list.get(list.size() / 2));
                        }
                    });


                }
                expandableOverspeeding.toggle();
            } else if (v.getId() == counterHardAccBrake.getId()) {

                if (hardAccBrakeMapDone == false && list != null && mapHardAccBrake != null) {
                    showPathOnMap(mapHardAccBrake, list, isServerRequest, R.id.map_hard_acc_brake);
                    addIntermediatePoints(mapHardAccBrake, tripDetail.hab_p);

                    mapHardAccBrake.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            GoogleMapUtils.zoomToFitAllMarkersWithLat(mActivity, mapHardAccBrake, false, list.get(0), list.get(list.size() - 1), list.get(list.size() / 2));
                        }
                    });
                }

                expandableHardAccBrake.toggle();
            } else if (v.getId() == counterEcoDriving.getId()) {

                if (ecoDrivingMapDone == false && list != null && mapEcoDriving != null) {
                    showPathOnMap(mapEcoDriving, list, isServerRequest, R.id.map_eco_driving);
                    addIntermediatePoints(mapEcoDriving, tripDetail.ed_p);

                    mapEcoDriving.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            GoogleMapUtils.zoomToFitAllMarkersWithLat(mActivity, mapEcoDriving, false, list.get(0), list.get(list.size() - 1), list.get(list.size() / 2));
                        }
                    });
                }

                expandableEcoDriving.toggle();
            } else if (v.getId() == counterIdling.getId()) {

                if (idlingMapDone == false && list != null && mapIdling != null) {
                    showPathOnMap(mapIdling, list, isServerRequest, R.id.map_idle);
                    addIntermediatePoints(mapIdling, tripDetail.i_p);

                    mapIdling.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            GoogleMapUtils.zoomToFitAllMarkersWithLat(mActivity, mapIdling, false, list.get(0), list.get(list.size() - 1), list.get(list.size() / 2));
                        }
                    });
                }

                expandableIdling.toggle();
            } else if (v.getId() == counterGoodGearChange.getId()) {

                if (goodGearChangeMapDone == false && list != null && mapGoodGearChange != null) {
                    showPathOnMap(mapGoodGearChange, list, isServerRequest, R.id.map_good_gear_change);
                    addIntermediatePoints(mapGoodGearChange, tripDetail.gc_p);

                    mapGoodGearChange.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            GoogleMapUtils.zoomToFitAllMarkersWithLat(mActivity, mapGoodGearChange, false, list.get(0), list.get(list.size() - 1), list.get(list.size() / 2));
                        }
                    });
                }

                expandableGoodGearChange.toggle();
            }
        }
    };


    /**
     * Used to take screenshot of top part(Google map and bottom two bars)
     */
    public void takeScreenshot() {
        mapMain.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(final Bitmap bitmap) {

                EasyPermissions.requestPermissions(ActivityTripDetails.this, new EasyPermissions.PermissionCallbacks() {
                    @Override
                    public void onPermissionsGranted(int requestCode, List<String> perms) {

                        Paint transparentPaint = new Paint();
                        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
                        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

                        View v1 = getWindow().getDecorView().getRootView();
                        Bitmap wholeBitmap = Utility.screenShot(frameLayoutMaps);
                        Bitmap bmOverlay = Bitmap.createBitmap(wholeBitmap.getWidth(), wholeBitmap.getHeight(), wholeBitmap.getConfig());

                        Bitmap transparentWhole = Bitmap.createBitmap(wholeBitmap.getWidth(), wholeBitmap.getHeight(), wholeBitmap.getConfig());
                        Canvas transparentWholeCanvas = new Canvas(transparentWhole);
                        transparentWholeCanvas.drawBitmap(wholeBitmap, new Matrix(), null);
                        transparentWholeCanvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), transparentPaint);

                        Canvas canvas = new Canvas(bmOverlay);
                        canvas.drawBitmap(bitmap, 0, 0, null);
                        canvas.drawBitmap(wholeBitmap, new Matrix(), null);
//                        canvas.drawBitmap(Utility.screenShot(frameLayoutMaps), new Matrix(), null);

                        File file = Utility.saveBitmap(mActivity, bmOverlay);
                        Utility.shareImage(mActivity, file);
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
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trip_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.action_share) {
            //Taking screenshot when share button is clicked
            takeScreenshot();
        }
        return true;
    }


}
