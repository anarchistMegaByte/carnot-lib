package com.carnot.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.activity.ActivityAudibleErrorCodes;
import com.carnot.adapter.ErrorCodesAdapter;
import com.carnot.custom_views.expandablelayout.ExpandableRelativeLayout;
import com.carnot.global.ConstantCode;
import com.carnot.global.SettingsUtility;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseFragment;
import com.carnot.libclasses.PermissionHelper;
import com.carnot.models.CarDiagnostics;
import com.carnot.models.CarErrors;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by pankaj on 1/4/16.
 */
public class FragmentCarDiagnostics extends BaseFragment implements View.OnClickListener {


    private Button btnScanCarError;
    private ExpandableRelativeLayout expandableAdvanceStatsNoti;
    ExpandableRelativeLayout expandableCarErrorCodes;
    private View viewScanLeftRight, viewScanRightLeft;
    private ExpandableRelativeLayout expandTypePressureDetail;
    private ImageView imgSignalBar, imgBluetooth, imgEngineOil, imgCoolantTemp, imgBattery;
    private RecyclerView recyclerViewErrors;
    private TextView txtConnectedStatus, txtBluetoothStatus;
    //    private String[] arrErrors = new String[]{"P0729", "P0442", "P0729", "P0442", "P0729", "P0442"};
    private ArrayList<CarErrors> arrErrors;
    TextView txtBatteryHealth, txtCabinTemp, txtEngineOil, txtCoolantTemperature;
    TextView txtTyreFrontLeft, txtTyreFrontRight, txtTyreBottomLeft, txtTyreBottomRight, lblErrorCodeTitle;

    public FragmentCarDiagnostics() {
        setContentView(R.layout.fragment_cardiagnostics, true, false);
    }

    @Override
    public void initVariable() {
        arrErrors = new ArrayList<CarErrors>();

    }

    @Override
    public void initView() {

        //Initializing all the views
        imgEngineOil = (ImageView) links(R.id.img_engine_oil);
        imgBattery = (ImageView) links(R.id.img_battery_health);
        imgCoolantTemp = (ImageView) links(R.id.img_coolant_temp);
        txtConnectedStatus = (TextView) links(R.id.txt_connected_status);
        txtBluetoothStatus = (TextView) links(R.id.txt_bluetooth_status);
        txtBatteryHealth = (TextView) links(R.id.txt_battery_health);
        txtCabinTemp = (TextView) links(R.id.txt_cabin_temperature);
        txtEngineOil = (TextView) links(R.id.txt_engine_oil);
        txtCoolantTemperature = (TextView) links(R.id.txt_coolant_temperature);
        lblErrorCodeTitle = (TextView) links(R.id.lbl_error_code_title);
        imgSignalBar = (ImageView) links(R.id.img_signal_bar);
        imgBluetooth = (ImageView) links(R.id.img_bluetooth);

        viewScanLeftRight = links(R.id.view_scan_bar_left_right);
        viewScanRightLeft = links(R.id.view_scan_bar_right_left);
        btnScanCarError = (Button) links(R.id.btn_scan_car_error);
        expandableCarErrorCodes = (ExpandableRelativeLayout) links(R.id.expandable_car_error_codes);
        recyclerViewErrors = (RecyclerView) links(R.id.recycler_view_errors);
//        recyclerViewErrors.setHasFixedSize(false);
        expandableAdvanceStatsNoti = (ExpandableRelativeLayout) links(R.id.expandable_advance_stats_noti);
        expandTypePressureDetail = (ExpandableRelativeLayout) links(R.id.expandableLayout_tyrepressure);


        txtTyreFrontLeft = (TextView) links(R.id.txt_tyre_front_left);
        txtTyreFrontRight = (TextView) links(R.id.txt_tyre_front_right);
        txtTyreBottomLeft = (TextView) links(R.id.txt_tyre_bottom_left);
        txtTyreBottomRight = (TextView) links(R.id.txt_tyre_bottom_right);

//        expandTypePressureDetail.setInterpolator(Utils.createInterpolator(Utils.ACCELERATE_INTERPOLATOR));

    }

    @Override
    public void postInitView() {
        super.postInitView();
        viewScanLeftRight.setVisibility(View.INVISIBLE);
        viewScanRightLeft.setVisibility(View.INVISIBLE);
    }

    @Override
    public void addAdapter() {
        ((LinearLayout) links(R.id.ll_tyre_pressure)).setOnClickListener(this);
        ((LinearLayout) links(R.id.ll_audibleErrors)).setOnClickListener(this);
        ((LinearLayout) links(R.id.ll_read_car_e_manual)).setOnClickListener(this);
        ((LinearLayout) links(R.id.ll_need_help)).setOnClickListener(this);


        btnScanCarError.setOnClickListener(this);
    }

    @Override
    public void loadData() {

        /*if (Utility.isConnectingToInternet(mActivity)) {
            imgSignalBar.setImageResource(R.drawable.ic_signal_green);
        } else {
            imgSignalBar.setImageResource(R.drawable.ic_signal_red);
        }*/
//        loadErrors();
        loadFromDatabase();

    }


    @Override
    public void onResume() {
        super.onResume();
        if (imgSignalBar != null) {


            updateConnection(Utility.isConnectingToInternet(mActivity));
            /*if (Utility.isConnectingToInternet(mActivity)) {
                imgSignalBar.setImageResource(R.drawable.ic_signal_green);
            } else {
                imgSignalBar.setImageResource(R.drawable.ic_signal_red);
            }*/

            if (SettingsUtility.getInstance(mActivity).isBluetoothEnabled()) {
                imgBluetooth.setImageResource(R.drawable.ic_bluetooth_green);
                txtBluetoothStatus.setText(getString(R.string.lbl_connected));
            } else {
                imgBluetooth.setImageResource(R.drawable.ic_bluetooth_red);
                txtBluetoothStatus.setText(getString(R.string.lbl_not_connected));
            }
        }
    }

    /**
     * Updating the connection
     *
     * @param isConnected
     */
    public void updateConnection(boolean isConnected) {
        if (imgSignalBar != null && txtConnectedStatus != null) {
            if (isConnected == true) {
                imgSignalBar.setImageResource(R.drawable.ic_signal_green);
                txtConnectedStatus.setText(getString(R.string.lbl_connected));
            } else {
                imgSignalBar.setImageResource(R.drawable.ic_signal_red);
                txtConnectedStatus.setText(getString(R.string.lbl_not_connected));
            }
        }
    }

    /**
     * Loding the diagnosis values from database and update the views
     */
    private void loadFromDatabase() {
        final String carId = getArguments().getString(ConstantCode.INTENT_CAR_ID) + "";
        CarDiagnostics carDiagnostics = CarDiagnostics.readSpecific(carId);
        fillValuesFromDiagnostics(carDiagnostics);
    }

    /**
     * Populating the views from car diagnosis
     *
     * @param carDiagnostics
     */
    private void fillValuesFromDiagnostics(CarDiagnostics carDiagnostics) {
        if (getActivity() != null && carDiagnostics != null) {
            loadValues(carDiagnostics);
            if (arrErrors.size() > 0) {
                lblErrorCodeTitle.setText(arrErrors.size() + " error codes found");
            } else {
                lblErrorCodeTitle.setText(getString(R.string.lbl_no_errors_found));
            }
        } else {
            lblErrorCodeTitle.setText(getString(R.string.lbl_no_errors_found));
        }
    }

    /**
     * Used to call the diagnostics web service
     */
    private void loadDiagnostic() {
        final String carId = getArguments().getString(ConstantCode.INTENT_CAR_ID) + "";
        WebUtils.call(WebServiceConfig.WebService.GET_CAR_DIAGNOSTIC, new String[]{carId}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                JSONObject json = (JSONObject) values;
                try {
                    CarDiagnostics carDiagnostics = (CarDiagnostics) Utility.parseFromString(json.optString(ConstantCode.data), CarDiagnostics.class);
                    if (getActivity() != null) {
                        loadValues(carDiagnostics);
                    }

                    //saving the cardiagnostics value in database
                    carDiagnostics.save();

                    //Creating dynamic height because already calculated at startup but there may be chance that errorscode inscreases and height get changed
                    int titleHeight = (int) Utility.convertDpToPixel(25, mActivity);
                    int rowHeight = (int) Utility.convertDpToPixel(25 + 10, mActivity);
                    rowHeight = rowHeight * carDiagnostics.car_errors.size();
                    int padding = (int) Utility.convertDpToPixel(10 + 10, mActivity);


                    expandableCarErrorCodes.forceMeasure(titleHeight + rowHeight + padding);
                    expandableCarErrorCodes.collapse();
                    fillValuesFromDiagnostics(carDiagnostics);
                } catch (Exception e) {
                    e.printStackTrace();
                    Utility.showLogE(FragmentCarDiagnostics.class, e.getMessage());
                }
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
                showToast(values.toString());
            }

            @Override
            public void loadOffline() {
                super.loadOffline();

            }
        });
    }


    /**
     * Initializing the views from cardiagnostics
     *
     * @param carDiagnostics
     */
    private void loadValues(CarDiagnostics carDiagnostics) {

//        arrErrors = new String[]{"P0729", "P0442", "P0729"};
        arrErrors = (ArrayList<CarErrors>) carDiagnostics.car_errors;

        //Preloading the errors
        loadErrors();

        txtCabinTemp.setText(carDiagnostics.cabin_temperature + "" + ConstantCode.degree_symbol + "");
        txtEngineOil.setText(carDiagnostics.engine_oil ? "OK" : "Less");
        imgEngineOil.setImageResource(carDiagnostics.engine_oil ? R.drawable.ic_engine_oil_green : R.drawable.ic_engine_oil_red_white);

        if (carDiagnostics.coolant_temperature) {
            txtCoolantTemperature.setText(getString(R.string.lbl_ok));
            imgCoolantTemp.setImageResource(R.drawable.ic_coolant_temperature_green);
        } else {
            txtCoolantTemperature.setText(getString(R.string.lbl_high));
            imgCoolantTemp.setImageResource(R.drawable.ic_coolant_temperature_red);
        }

        //Changing battery health colors based on condition
        txtBatteryHealth.setText(carDiagnostics.battery_health + "%");
        if (carDiagnostics.battery_health >= 75) {
            imgBattery.setImageResource(R.drawable.ic_battery_green);
        } else if (carDiagnostics.battery_health >= 50 && carDiagnostics.battery_health < 75) {
            imgBattery.setImageResource(R.drawable.ic_battery_yellow);
        } else {
            imgBattery.setImageResource(R.drawable.ic_battery_red);
        }
    }

    @Override
    public void onClick(View v) {
            if (v.getId() ==  R.id.ll_tyre_pressure){
                expandTypePressureDetail.toggle();}
            else if (v.getId() ==   R.id.btn_scan_car_error){

                //on scan or dismiss button click we are toggling the errors views
                if (getString(R.string.lbl_dismiss).equalsIgnoreCase(btnScanCarError.getText().toString())) {
                    expandableCarErrorCodes.toggle();
                    btnScanCarError.setText(getString(R.string.lbl_scan_for_errors));
                } else {
                    if (expandableCarErrorCodes.isExpanded()) {
                        expandableCarErrorCodes.toggle();
                    }

                    //starting the animation of car scanning
                    scanCarForError();
                }}
            else if (v.getId() ==   R.id.ll_audibleErrors){
                callAudibleScreen();}
            else if (v.getId() ==  R.id.ll_read_car_e_manual){}

            else if (v.getId() ==   R.id.ll_need_help){
                    Intent intentDialer = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:9805003587"));//9805003587,
                startActivity(intentDialer);}
            else if (v.getId() ==    R.id.ll_batteryHealth){
                showErrorsOverlay();}

            else if (v.getId() == R.id.ll_carTemperature){
                showErrorsOverlay();}

            else if (v.getId() == R.id.ll_engineOil){
                showErrorsOverlay();}

            else if (v.getId() ==  R.id.ll_cabinetTemperature){
                showErrorsOverlay();}
        }

    /**
     * Toggle error code information
     */
    private void showErrorsOverlay() {
        expandableAdvanceStatsNoti.toggle();
    }

    private int animationTimes = 0, timeSet = 1;
    private int animationDuration = 2500;

    /**
     * Set This value to set how many time scan animation occours right now it is 2.
     */
    private final int SCAN_TIMES = 2;

    /**
     * Used to start the car animation and webservice
     */
    private void scanCarForError() {
        loadDiagnostic();
        animationTimes = 0;
        timeSet = 0;
        btnScanCarError.setEnabled(false);

        animate();

    }


    ObjectAnimator animLeftRight, animReverse;


    /**
     * Animating the scan animation over the car.
     */
    private void animate() {
        if (isExpanded) {
            expandableCarErrorCodes.toggle();
        }

        //Left to right Animation
        animLeftRight = ObjectAnimator.ofFloat(viewScanLeftRight, "translationX", -viewScanLeftRight.getWidth(), viewScanLeftRight.getWidth());
        animLeftRight.setDuration(animationDuration);
        animLeftRight.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (viewScanLeftRight != null) {
                    viewScanLeftRight.setBackgroundResource(R.drawable.scan_background_reverse);
                    viewScanLeftRight.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animLeftRight.start();

        //Right to left Animation
        animReverse = ObjectAnimator.ofFloat(viewScanRightLeft, "translationX", viewScanRightLeft.getWidth(), -viewScanRightLeft.getWidth());
        animReverse.setDuration(animationDuration);
        animReverse.setStartDelay(animationDuration - 1000);
        animReverse.start();
        animReverse.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (viewScanRightLeft != null) {
                    viewScanRightLeft.setVisibility(View.VISIBLE);
                    viewScanRightLeft.setBackgroundResource(R.drawable.scan_background);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (btnScanCarError != null) {
                    animationTimes++;
                    if (animationTimes == SCAN_TIMES) {
                        expandableCarErrorCodes.toggle();
                        btnScanCarError.setEnabled(true);
                        if (arrErrors.size() > 0) {
                            btnScanCarError.setText(getString(R.string.lbl_dismiss));
                        }
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        timeSet++;
        if (timeSet < SCAN_TIMES) {

            //Scheduling next left to right animation
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animate();
                }
            }, animationDuration * 2 - 1000 * 2);
        }
    }

    boolean isExpanded = false;

    int height = 0;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (animLeftRight != null) {
            animLeftRight.cancel();
            animReverse.cancel();
        }
    }

    ErrorCodesAdapter adapter;

    private void loadErrors() {
        LinearLayoutManager layoutmanger = new LinearLayoutManager(getActivity());
        recyclerViewErrors.setHasFixedSize(true);
        recyclerViewErrors.setLayoutManager(layoutmanger);

        if (adapter == null) {
            adapter = new ErrorCodesAdapter(mActivity, arrErrors);
//            VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration((int) Utility.convertDpToPixel(getResources().getDimension(R.dimen.scale_0dp), mActivity));
//            recyclerViewErrors.addItemDecoration(decoration);
        } else {
            adapter.setList(arrErrors);
        }
        recyclerViewErrors.setAdapter(adapter);


    }

    private void callAudibleScreen() {
        Intent intent = new Intent(mActivity, ActivityAudibleErrorCodes.class);
        mActivity.startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
