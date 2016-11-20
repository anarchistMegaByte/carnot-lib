package com.carnot.global;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SettingsUtility {
    private Context context;
    private static SettingsUtility instance;

    private SettingsUtility(Context context) {
        this.context = context;
    }

    public static SettingsUtility getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsUtility(context);
        }
        return instance;
    }

    /**
     * Change screen brigtness it must be withing 1-255
     *
     * @param brigtness
     *//*
    public void setScreenBrigtness(int brigtness) {
        if (brigtness > 255) {
            return;
        }
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brigtness);
    }

    *//**
     * Change screenoff timeout if should be in seconds
     *
     * @param seconds
     *//*
    public void setScreenOffTimeOut(int seconds) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, seconds * 1000);
    }

    *//**
     * Change wifi status
     *
     * @param enableWifi
     *//*

    public void chagneWifiStatus(boolean enableWifi) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (enableWifi != wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(enableWifi);

    }*/

    /**
     * Change bluetooth status
     *
     * @param enableBluetooth
     */
    public void changeBluetoothStatus(boolean enableBluetooth) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            boolean isEnabled = bluetoothAdapter.isEnabled();
            if (enableBluetooth) {
                bluetoothAdapter.enable();
            } else {
                bluetoothAdapter.disable();
            }
        }
    }

    public boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            return false;
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean hasBluetoothInDevice() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            return true;
        }
    }

    public void setMobileDataState(boolean mobileDataEnabled) {
        ConnectivityManager dataManager;
        dataManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Method dataMtd = null;
        try {
            dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        dataMtd.setAccessible(true);
        try {
            dataMtd.invoke(dataManager, mobileDataEnabled);
        } catch (IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public boolean getMobileDataState() {
        try {
            TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");

            if (null != getMobileDataEnabledMethod) {
                boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);

                return mobileDataEnabled;
            }
        } catch (Exception ex) {
            Log.e("UTILITY", "Error getting mobile data state", ex);
        }

        return false;
    }

//    private Toast toast;

    /*public void showCustomToast(String message) {
        View view = LayoutInflater.from(context).inflate(R.layout.toast_layout, null, false);
        TextView txtMessage = (TextView) view.findViewById(R.id.txt_message);
        txtMessage.setText(message);
//        if (toast != null) {
//            toast.cancel();
//        }
        if (toast == null)
            toast = new Toast(context);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }*/
}
