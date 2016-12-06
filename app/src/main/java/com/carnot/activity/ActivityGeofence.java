package com.carnot.activity;

/**
 * Created by chaks on 10/4/16.
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.carnot.R;
import com.carnot.Services.FetchAddressIntentService;
import com.carnot.custom_views.customseekbar.DiscreteSeekBar;
import com.carnot.global.LocationUtils;
import com.carnot.libclasses.BaseActivity;
import com.carnot.models.Cars;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashMap;

public class ActivityGeofence extends BaseActivity implements PlaceSelectionListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static String TAG = "MAP LOCATION";
    Context mContext;
    TextView mLocationMarkerText;
    private LatLng mCenterLatLong;
    private LatLng searchLatLng;
    private CircleOptions mapCircleOptions;
    private Circle mapCircle;
    private static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(new LatLng(23.63936, 68.14712), new LatLng(28.20453, 97.34466));
    private static final int REQUEST_SELECT_PLACE = 1000;
    private EditText locationTextView;
    private AddressResultReceiver mResultReceiver;
    protected String mAddressOutput;
    protected String mAreaOutput;
    protected String mCityOutput;
    protected String mStateOutput;
    EditText mLocationAddress;
    TextView mLocationText;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    Toolbar mToolbar;
    private Integer carId;
    private String geo_id;
    private LatLng carLatLng;
    private Boolean geo_isLive;
    private Double geo_radius = 0.5;
    private com.carnot.models.Geofence _geofence;
    private TextView geo_radius_tv;
    private Button discard_button;
    public com.carnot.models.Geofence ini_config;
    private CoordinatorLayout coordinatorLayout;
    private DiscreteSeekBar geo_seekbar;
    public Snackbar snackbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence_map);
        mContext = this;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

//        mLocationMarkerText = (TextView) findViewById(R.id.locationMarkertext);
        mLocationAddress = (EditText) findViewById(R.id.txt_location_123);
        discard_button = (Button) links(R.id.geo_discard_button);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        Intent intent = getIntent();
        geo_id = intent.getStringExtra("geo_id");
        geo_isLive = intent.getBooleanExtra("geo_isLive", false);
        _geofence = com.carnot.models.Geofence.get(geo_id);
        mLocationAddress.setText(_geofence.name);
        ini_config = _geofence;
        geo_radius = Double.valueOf(_geofence.radius);
        Log.e(TAG, "onCreate: geo_id " + geo_id );
        carId = com.carnot.models.Geofence.get_carId(geo_id);
        Log.e(TAG, "onCreate: car_id " + carId );
        carLatLng = Cars.current_location(carId.toString());
        discard_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                geo_radius = Double.valueOf(_geofence.radius);
                Log.e(TAG, "onCreate: geo_id " + geo_id );
                carId = com.carnot.models.Geofence.get_carId(geo_id);
                Log.e(TAG, "onCreate: car_id " + carId );
                carLatLng = Cars.current_location(carId.toString());

                mCenterLatLong = new LatLng(Double.parseDouble(_geofence.lat), Double.parseDouble(_geofence.lng));
                changeMap(mCenterLatLong);
                change_radius((int) (geo_radius*1000));
                geo_seekbar.setProgress((int) (geo_radius*1000));
            }
        });


//        mLocationText = (TextView) findViewById(R.id.Locality);


        getSupportActionBar().setDisplayShowHomeEnabled(true);
        locationTextView = (EditText) findViewById(R.id.txt_location_123);
//        attributionsTextView = (TextView) findViewById(R.id.txt_attributions);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
        getFragmentManager().findFragmentById(R.id.place_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(this);
        autocompleteFragment.setHint("Search");
        autocompleteFragment.setBoundsBias(BOUNDS_INDIA);
        geo_radius_tv = (TextView) links(R.id.geo_radius_tv);
        geo_radius_tv.setText("Radius [" + Double.toString(geo_radius) + "Km]");


        DiscreteSeekBar.OnProgressChangeListener onProgressChangeListener = new DiscreteSeekBar.OnProgressChangeListener() {

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                change_radius(value);
            }
        };

        geo_seekbar = (DiscreteSeekBar) findViewById(R.id.geo_discreteSeekbar);
        geo_seekbar.setProgress((int) (geo_radius*1000));
        geo_seekbar.setOnProgressChangeListener(onProgressChangeListener);

        snackbar = Snackbar.make(coordinatorLayout, "Geofence Saved", Snackbar.LENGTH_LONG);


//        mLocationText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                openAutocompleteActivity();
//
//            }
//
//
//        });
        mapFragment.getMapAsync(this);
        mResultReceiver = new AddressResultReceiver(new Handler());

//        if (checkPlayServices()) {
//            // If this check succeeds, proceed with normal processing.
//            // Otherwise, prompt user to get valid Play Services APK.
//            if (!LocationUtils.isLocationEnabled(mContext)) {
//                // notify user
//                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
//                dialog.setMessage("Location is not enabled!");
//                dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        startActivity(myIntent);
//                    }
//                });
//                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                        // TODO Auto-generated method stub
//
//                    }
//                });
//                dialog.show();
//            }
//            buildGoogleApiClient();
//        }
//        else {
//            Toast.makeText(mContext, "Location not supported in this device", Toast.LENGTH_SHORT).show();
//        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "OnMapReady");
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(_geofence.lat), Double.parseDouble(_geofence.lng)), 15));

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                CameraPosition cameraPosition = mMap.getCameraPosition();
                Log.d("Camera postion change" + "", cameraPosition + "");
                mCenterLatLong = cameraPosition.target;

                mMap.clear();
                try {

                    Location mLocation = new Location("");
                    mLocation.setLatitude(Double.parseDouble(_geofence.lat));
                    mLocation.setLongitude(Double.parseDouble(_geofence.lng));

                    startIntentService(mLocation);
                    mapCircleOptions = new CircleOptions().center(mCenterLatLong).radius(geo_radius*1000).fillColor(0x300200af).strokeColor(0x300200af).strokeWidth(0);
                    mapCircle = mMap.addCircle(mapCircleOptions);
//                    mLocationMarkerText.setText("Lat : " + mCenterLatLong.latitude + "," + "Long : " + mCenterLatLong.longitude);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

//            @Override
//            public void onCameraChange(CameraPosition cameraPosition) {
//                Log.d("Camera postion change" + "", cameraPosition + "");
//                mCenterLatLong = cameraPosition.target;
//
//                mMap.clear();
//                try {
//
//                    Location mLocation = new Location("");
//                    mLocation.setLatitude(mCenterLatLong.latitude);
//                    mLocation.setLongitude(mCenterLatLong.longitude);
//
//                    startIntentService(mLocation);
//                    mapCircleOptions = new CircleOptions().center(mCenterLatLong).radius(geo_radius*1000).fillColor(0x70ff0000).strokeColor(0x70ff0000).strokeWidth(0);
//                    mapCircle = mMap.addCircle(mapCircleOptions);
////                    mLocationMarkerText.setText("Lat : " + mCenterLatLong.latitude + "," + "Long : " + mCenterLatLong.longitude);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//        mMap.setMyLocationEnabled(true);
//        mMap.getUiSettings().setMyLocationButtonEnabled(true);
//
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LatLng mLastLocation = new LatLng(Double.parseDouble(_geofence.lat), Double.parseDouble(_geofence.lng));
//        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
//                mGoogleApiClient);
        if (mLastLocation != null) {
            changeMap(mLastLocation);
            Log.d(TAG, "ON connected");}

//        } else
//            try {
//                LocationServices.FusedLocationApi.removeLocationUpdates(
//                        mGoogleApiClient, this);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        try {
//            LocationRequest mLocationRequest = new LocationRequest();
//            mLocationRequest.setInterval(10000);
//            mLocationRequest.setFastestInterval(5000);
//            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//            LocationServices.FusedLocationApi.requestLocationUpdates(
//                    mGoogleApiClient, mLocationRequest, this);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            if (location != null)
                changeMap(location);
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mGoogleApiClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //    finish();

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //finish();
            }
            return false;
        }
        return true;
    }

    private void changeMap(Location location) {

        Log.d(TAG, "Reaching map" + mMap);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // check if map is created successfully or not
        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(false);
            LatLng latLong;


            latLong = new LatLng(location.getLatitude(), location.getLongitude());

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLong).zoom(16f).tilt(0).build();
            Log.e(TAG, "changeMap: zooom changed");

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
            mCenterLatLong = carLatLng;

            mapCircleOptions = new CircleOptions().center(mCenterLatLong).radius(geo_radius*1000).fillColor(0x300200af).strokeColor(0x300200af).strokeWidth(0);
            Log.e(TAG, "changeMap: mapcircleoptions" + mapCircleOptions.getCenter() );
            change_circle(mapCircleOptions);



//            mLocationMarkerText.setText("Lat : " + location.getLatitude() + "," + "Long : " + location.getLongitude());
//            startIntentService(location);


        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    private void changeMap(LatLng searchLatLng) {

        Log.d(TAG, "Reaching map" + mMap);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // check if map is created successfully or not
        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(false);
            LatLng latLong = searchLatLng;
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLong).zoom(15f).tilt(0).build();

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
            mapCircleOptions = new CircleOptions().center(mCenterLatLong).radius(geo_radius*1000).fillColor(0x300200af).strokeColor(0x300200af).strokeWidth(0);
            change_circle(mapCircleOptions);



//            mLocationMarkerText.setText("Lat : " + latLong.latitude + "," + "Long : " + latLong.longitude);
//            startIntentService(location);


        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    @Override
    public void onPlaceSelected(Place place) {
        Log.i(TAG, "Place Selected: " + place.getName());
        locationTextView.setText(getString(R.string.formatted_place_data, place
                .getName()));

        searchLatLng = place.getLatLng();
        changeMap(searchLatLng);
//        if (!TextUtils.isEmpty(place.getAttributions())){
//            attributionsTextView.setText(Html.fromHtml(place.getAttributions().toString()));
//        }

    }

    @Override
    public void onError(Status status) {
        Log.e(TAG, "onError: Status = " + status.toString());
        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void initVariable() {


    }

    @Override
    public void initView() {
        setToolbar(R.id.toolbar, true);
        setTitle("Set Geofence");


        Button save_button = (Button) links(R.id.geo_save_button);
        Button discard_button = (Button) links(R.id.geo_discard_button);

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String, Object> metaList = new HashMap<String, Object>();
                try {
                    metaList.put("name", locationTextView.getText().toString());
                    metaList.put("radius", geo_radius.toString());
                    metaList.put("lat", String.valueOf(mCenterLatLong.latitude));
                    metaList.put("lng", String.valueOf(mCenterLatLong.longitude));
                    metaList.put("status", _geofence.status);
                    metaList.put("isLive", geo_isLive ? 1:0);
                    metaList.put("geo_id", geo_id);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                final Boolean status =  com.carnot.models.Geofence.edit_sync(geo_id, metaList, this.getClass().getSimpleName(), snackbar);
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


    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(LocationUtils.LocationConstants.RESULT_DATA_KEY);

            mAreaOutput = resultData.getString(LocationUtils.LocationConstants.LOCATION_DATA_AREA);

            mCityOutput = resultData.getString(LocationUtils.LocationConstants.LOCATION_DATA_CITY);
            mStateOutput = resultData.getString(LocationUtils.LocationConstants.LOCATION_DATA_STREET);

            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == LocationUtils.LocationConstants.SUCCESS_RESULT) {
                //  showToast(getString(R.string.address_found));


            }


        }

    }

    /**
     * Updates the address in the UI.
     */
    protected void displayAddressOutput() {
        //  mLocationAddressTextView.setText(mAddressOutput);
        try {
            if (mAreaOutput != null)
                Log.e(TAG, "displayAddressOutput: " + mAddressOutput );
//            TODO: mAreaOutput
//            locationTextView.setText(mAreaOutput);

                // mLocationText.setText(mAreaOutput+ "");

//                mLocationAddress.setText(mAddressOutput);
            //mLocationText.setText(mAreaOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService(Location mLocation) {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(LocationUtils.LocationConstants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(LocationUtils.LocationConstants.LOCATION_DATA_EXTRA, mLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
        Log.e(TAG, "startIntentService: Sent signal");
    }


    private void openAutocompleteActivity() {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called after the autocomplete activity has finished to return its result.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(mContext, data);

                // TODO call location based filter


                LatLng latLong;


                latLong = place.getLatLng();

                //mLocationText.setText(place.getName() + "");

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLong).zoom(16).tilt(0).build();

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));


            }


        } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
            Status status = PlaceAutocomplete.getStatus(mContext, data);
        } else if (resultCode == RESULT_CANCELED) {
            // Indicates that the activity closed before a selection was made. For example if
            // the user pressed the back button.
        }
    }

    public void change_radius(int new_radius){
        Log.e(TAG, "change_radius: radius got" + new_radius);
        geo_radius_tv.setText("Radius [" + Double.toString(geo_radius) + "Km]");
        geo_radius = (double) new_radius/1000;
        Log.e(TAG, "change_radius: New Radius " + geo_radius + "Km");
        CircleOptions changeCircleOptions = new CircleOptions().center(mCenterLatLong).radius(geo_radius*1000).fillColor(0x300200af).strokeColor(0x300200af).strokeWidth(0);
        change_circle(changeCircleOptions);
    }

    public void change_circle(CircleOptions circle){
        if(mapCircle!=null){
            mapCircle.remove();
            Log.e(TAG, "change_circle: Removed");
        }

        mapCircle = mMap.addCircle(circle);
        Log.e(TAG, "change_circle: Circle Addex");
    }




}

