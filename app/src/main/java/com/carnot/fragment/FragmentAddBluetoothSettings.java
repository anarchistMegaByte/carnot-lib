package com.carnot.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;

import com.carnot.R;
import com.carnot.global.SettingsUtility;
import com.carnot.libclasses.BaseFragment;

/**
 * Created by pankaj on 1/4/16.
 * Fragment to change bluetooth status at time of adding new car dialog it is open from ActivityAddNewCarSetup
 */
public class FragmentAddBluetoothSettings extends BaseFragment {

    CheckBox chkBluetooth;

    public FragmentAddBluetoothSettings() {
        setContentView(R.layout.fragment_add_new_car_setup_bluetooth);
    }

    @Override
    public void initVariable() {

    }

    @Override
    public void initView() {
        chkBluetooth = (CheckBox) links(R.id.chk_bluetooth);
        chkBluetooth.setChecked(SettingsUtility.getInstance(mActivity).isBluetoothEnabled());

        chkBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Checking is there is bluetooth in device then change the bluetooth status
                if (SettingsUtility.getInstance(mActivity).hasBluetoothInDevice()) {
                    changeBluetoothStatus(((CheckBox) v).isChecked());
                } else {
                    showSnackbar(getString(R.string.msg_device_dont_have_bluetooth));
                    chkBluetooth.setChecked(false);
                }
            }
        });
    }

    /**
     * Changing bluetooth status
     *
     * @param isChecked
     */
    private void changeBluetoothStatus(boolean isChecked) {
        //Checking if System is Marshmallow and we have the permission to write setting then we open the intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(mActivity)) {
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);
                return;
            }
        } else {
            SettingsUtility.getInstance(mActivity).changeBluetoothStatus(isChecked);
            chkBluetooth.setChecked(isChecked);

            //We are delaying to check whether bluetooth is enabled or not by 2 seconds because bluetooth enabling and disabling takes sometime.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (chkBluetooth != null)
                        chkBluetooth.setChecked(SettingsUtility.getInstance(mActivity).isBluetoothEnabled());
                }
            }, 2000);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //updating bluetooth status by checking whether bluetooth is open or not.
        chkBluetooth.setChecked(SettingsUtility.getInstance(mActivity).isBluetoothEnabled());
    }

    @Override
    public void onFragmentResume() {
        super.onFragmentResume();
        //updating bluetooth status by checking whether bluetooth is open or not.
        chkBluetooth.setChecked(SettingsUtility.getInstance(mActivity).isBluetoothEnabled());
    }

    @Override
    public void addAdapter() {

    }

    @Override
    public void loadData() {

    }
}
