package com.carnot.fragment;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;

import com.carnot.R;
import com.carnot.activity.ActivityCarDashboard;
import com.carnot.activity.ActivityOnTrip;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseFragment;
import com.carnot.libclasses.PermissionHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by root on 15/4/16.
 */
public class FragmentNotificationTowed extends BaseFragment {

    public FragmentNotificationTowed() {
        setContentView(R.layout.fragment_notification_towed);
    }

    @Override
    public void initVariable() {

    }

    @Override
    public void initView() {

    }

    @Override
    public void addAdapter() {

    }

    @Override
    public void loadData() {
        initMaps();
    }

    private void initMaps() {
        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
        if (mSupportMapFragment == null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
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

                        PermissionHelper.askForPermission(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, new PermissionHelper.PermissionCallback() {
                            @Override
                            public void permissionGranted() {
                                googleMap.setMyLocationEnabled(true);
                            }

                            @Override
                            public void permissionRefused() {

                            }
                        });

                        LatLng latLng = new LatLng(23.034691, 72.508221);
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 14);
                        googleMap.animateCamera(update);

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        Marker marker = googleMap.addMarker(markerOptions);
                        /*googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                            @Override
                            public View getInfoWindow(Marker marker) {
                                View view = LayoutInflater.from(mActivity).inflate(R.layout.car_marker_info_window, null, false);
                                return view;
                            }

                            @Override
                            public View getInfoContents(Marker marker) {
                                return null;
                            }
                        });
                        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {

                                Intent intent = new Intent(mActivity, ActivityOnTrip.class);
                                startActivity(intent);
                            }
                        });*/

                        View view = LayoutInflater.from(mActivity).inflate(R.layout.car_marker_noti, null, false);
                        view.findViewById(R.id.marker_background).setBackgroundResource(R.color.transparent);
                        view.findViewById(R.id.txt_marker_name).setVisibility(View.GONE);
                        view.findViewById(R.id.txt_marker_message).setVisibility(View.GONE);
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(Utility.getBitmap(view, mActivity)));
                        marker.showInfoWindow();

                        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                marker.showInfoWindow();
                                /*Intent intent = new Intent(mActivity, ActivityCarDashboard.class);
                                startActivity(intent);*/
                                return true;
                            }
                        });
                    }
                }
            });
        }
    }
}
