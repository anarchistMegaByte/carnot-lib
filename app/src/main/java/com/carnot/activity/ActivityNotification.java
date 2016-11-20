package com.carnot.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import com.carnot.R;
import com.carnot.adapter.NotificationsAdapter;
import com.carnot.custom_views.AutoResizeTextView;
import com.carnot.global.UILoadingHelper;
import com.carnot.global.Utility;
import com.carnot.global.gcm.GCMMessage;
import com.carnot.global.gcm.GCMUtils;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.VerticalSpaceItemDecoration;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by root on 13/4/16.
 * Activity to show All the Notification from Database
 */
public class ActivityNotification extends BaseActivity {

    RecyclerView recyclerView;
    NotificationsAdapter adapter;
    //    ExpandableRelativeLayout expandableRelativeLayout;
    SwipeRefreshLayout swipeToRefreshLayout;

    UILoadingHelper uiLoadingHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle(R.string.lbl_notifications);
        swipeToRefreshLayout = (SwipeRefreshLayout) links(R.id.swipe_to_refresh);
        swipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNotificationFromDatabase();
            }
        });
        uiLoadingHelper = new UILoadingHelper();
    }

    @Override
    public void initView() {
        recyclerView = (RecyclerView) links(R.id.recycler_view);


        uiLoadingHelper.set(links(R.id.txt_empty), links(R.id.progress_bar), swipeToRefreshLayout);

        initFullScreenView();
    }


    //================FULL SCREEN========
    View viewFullScreen;
    AutoResizeTextView fullScreenTitle;
    ImageView imgFullScreenClose;
    GCMMessage lastFullScreenMessage;
    GoogleMap fullScreenGoogleMap;
    Marker fullScreenMarker;
    Button fullScreenBtnDismiss;

    private void initFullScreenView() {
        initMap();

        viewFullScreen = links(R.id.view_full_screen);
        fullScreenTitle = (AutoResizeTextView) links(R.id.txt_full_screen_status_title);
        imgFullScreenClose = (ImageView) links(R.id.img_full_screen_close_card);
        fullScreenBtnDismiss = (Button) links(R.id.btnDismiss);
        imgFullScreenClose.setOnClickListener(dismissClickListener);
        fullScreenBtnDismiss.setOnClickListener(dismissClickListener);
        ((CardView) links(R.id.card_view_full_screen_alert)).setMaxCardElevation(0);
    }

    @Override
    public void onBackPressed() {

        if (viewFullScreen != null && viewFullScreen.getVisibility() == View.VISIBLE)
            return;

        super.onBackPressed();
    }

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
                        ActivityNotification.this.fullScreenGoogleMap = googleMap;
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
                        ActivityNotification.this.fullScreenGoogleMap = googleMap;
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

//        view.findViewById(R.id.marker_background).setBackgroundResource(R.color.transparent);
//        view.findViewById(R.id.txt_marker_name).setVisibility(View.GONE);
        view.findViewById(R.id.txt_marker_message).setVisibility(View.GONE);
        fullScreenMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Utility.getBitmap(view, mActivity)));
    }


    View.OnClickListener dismissClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == fullScreenBtnDismiss.getId()) {
//                if (timer != null)
//                    timer.cancel();
                //TODO call webservice here
            }

            lastFullScreenMessage.dismissMessage();
            viewFullScreen.setVisibility(View.GONE);
            lastFullScreenMessage = null;
        }
    };

//    CountDownTimer timer;

    private void setValuesInFullScreen(GCMMessage lastFullScreenMessage) {
//        if (timer != null)
//            timer.cancel();

        if (lastFullScreenMessage != null) {
            viewFullScreen.setVisibility(View.VISIBLE);
            if (fullScreenTitle != null) {
                fullScreenTitle.setText(lastFullScreenMessage.message);
                fullScreenTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
                fullScreenTitle.resetTextSize();
                initMap(lastFullScreenMessage.lat, lastFullScreenMessage.lon);
                ((View) fullScreenTitle.getParent()).setBackgroundResource(Utility.getStatusBackground(lastFullScreenMessage.category));
                fullScreenTitle.setTextColor(Utility.getColor(mActivity, Utility.getStatusTextColor(lastFullScreenMessage.category)));
            }

            fullScreenBtnDismiss.setVisibility(View.GONE);

            /*if (GCMUtils.Crash_Alert.equalsIgnoreCase(lastFullScreenMessage.code)) {
                fullScreenBtnDismiss.setVisibility(View.VISIBLE);
                timer = new CountDownTimer(Utility.COUNTDOWN_TIME_FOR_ACCEIDENT, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        fullScreenBtnDismiss.setText(getString(R.string.lbl_dismiss_x, Utility.getMinsSeconds(millisUntilFinished / 1000)));
                    }

                    @Override
                    public void onFinish() {

                    }
                };
                timer.start();
            } else {
                fullScreenBtnDismiss.setVisibility(View.GONE);
            }*/
        }
    }


    @Override
    public void postInitView() {

    }

    @Override
    public void addAdapter() {

        /*ArrayList<Notifications> list = new ArrayList<>();
        list.add(new Notifications(ConstantCode.NOTIFICATION_TYPE_DIAGNOSTICS, "My Manufacturer xxxx battery is drained out, Check now"));
        list.add(new Notifications(ConstantCode.NOTIFICATION_TYPE_TOW_ALERT, "Manufacturer xxxx is being towed. Click here to track the location"));
        list.add(new Notifications(ConstantCode.NOTIFICATION_TYPE_SOS, "SOS crash detected for Manufacturer xxxx"));
        list.add(new Notifications(ConstantCode.NOTIFICATION_TYPE_SECURITY_LOCATION, "Manufacturer xxxx was just driven out of the Geo-Fence."));
        list.add(new Notifications(ConstantCode.NOTIFICATION_TYPE_ACHIEVEMENT, "Congratulations you just completed 50000 km in manufacturer xxxx"));
        list.add(new Notifications(ConstantCode.NOTIFICATION_TYPE_DIAGNOSTICS, "Time to change engine oil"));
        list.add(new Notifications(ConstantCode.NOTIFICATION_TYPE_PASSPORT, "Its time to renew PUC for Manufacturer xxxx in 14 days"));
        list.add(new Notifications(ConstantCode.NOTIFICATION_TYPE_SECURITY_LOCATION, "Manufacturer xxxx was just driven out of the Geo-Fence."));*/

        adapter = new NotificationsAdapter(mActivity);
       /* adapter.setIsLoadMoreEnabled(true, R.layout.load_more_progress, recyclerView, new LoadMoreRecyclerAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Utility.showLog(ActivityAllTrips.class, "LoadMoreFired");
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.onLoadMoreComplete();
                    }
                }, 2000);
            }
        });*/

        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastFullScreenMessage = adapter.getItem(position);
                setValuesInFullScreen(lastFullScreenMessage);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerView.setLayoutManager(linearLayoutManager);

        VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration((int) Utility.convertDpToPixel(getResources().getDimension(R.dimen.scale_0dp), mActivity));
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void loadData() {
        uiLoadingHelper.startProgress();
//        loadNotificationFromServer();
        loadNotificationFromDatabase();
    }

    private void loadNotificationFromDatabase() {

        ArrayList<GCMMessage> list = GCMMessage.readAll();
        if (list != null && list.size() > 0) {
            adapter.setItems(list);
            uiLoadingHelper.showContent();
        } else {
            uiLoadingHelper.showError(getString(R.string.msg_no_notification));
        }

    }

    /*private void loadNotificationFromServer() {
        String id = "11";
        if (Utility.getLoggedInUser() != null)
            id = Utility.getLoggedInUser().id + "";

        WebUtils.call(WebServiceConfig.WebService.ALL_NOTIFICATION, new String[]{id}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                JSONObject json = (JSONObject) values;
                Type collectionTypeDayType = new TypeToken<ArrayList<Notifications>>() {
                }.getType();
                ArrayList<Notifications> list = Utility.parseArrayFromString(json.optString(ConstantCode.data), Notifications[].class);
                adapter.setItems(list);
                *//*CarDiagnostics carDiagnostics = (CarDiagnostics) Utility.parseFromString(json.optString(ConstantCode.data), CarDiagnostics.class);
                loadValues(carDiagnostics);*//*
                swipeToRefreshLayout.setRefreshing(false);
                if (list.size() > 0) {
                    uiLoadingHelper.showContent();
                } else {
                    uiLoadingHelper.showError("No Notification");
                }
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
//                showToast(values.toString());
                uiLoadingHelper.showError(values.toString());
                swipeToRefreshLayout.setRefreshing(false);
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
                showToast(values.toString());
                uiLoadingHelper.showError(values.toString());
                swipeToRefreshLayout.setRefreshing(false);
            }
        });
    }*/
}
