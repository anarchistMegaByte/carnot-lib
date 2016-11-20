package com.carnot.Services;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.carnot.activity.ActivityCarDashboard;
import com.carnot.global.ConstantCode;
import com.carnot.models.Cars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by prathamesh on 18/7/16.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEService extends Service {
    private int CAR_NO = -1;
    private ArrayList<Cars> list;
    public static boolean passKeyWriteDone = false;

    private static final String TAG = "BLEService";
    public static final String BLE_DEVICE_NAME = "Carnot-D";
    private static final int APP_VERSION_BLE_CONN_PROC = 21;
    private static final int MINIMUM_RSSI_VALUE = -90;

    public static final String DEFAULT_STM_UUID = "00000000000000000000000000000000";

    private static String PASS_KEY = "";
    public static final String BLE_DATA_SERVICE_UUID = "29da1420-d622-4243-bd58-6d91bc634aef";
    public static final String BLE_INFO_SERVICE_UUID = "29da1490-d622-4243-bd58-6d91bc634aef";

    /**
     * Characteristic UUIDs
     */
    public static final String BLE_DATA_CHAR_REALTIME_UUID = "29da1421-d622-4243-bd58-6d91bc634aef";
    public static final String BLE_DATA_CHAR_STM_RELAY_UUID = "29da1422-d622-4243-bd58-6d91bc634aef";
    public static final String BLE_DATA_CHAR_LOCATION_UUID = "29da1423-d622-4243-bd58-6d91bc634aef";
    public static final String BLE_DATA_CHAR_ACCELEROMETER_UUID = "29da1424-d622-4243-bd58-6d91bc634aef";
    public static final String BLE_INFO_CHAR_STM_UID_UUID = "29da1496-d622-4243-bd58-6d91bc634aef";
    public static final String BLE_INFO_CHAR_NRF_UID_UUID = "29da1498-d622-4243-bd58-6d91bc634aef";
    public static final String BLE_INFO_CHAR_PASSKEY_UUID = "29da1499-d622-4243-bd58-6d91bc634aef";

    /**
     * Actions
     */
    public static final String UI_UPDATE_ACTION = "com.carnot.uiupdateaction";

    /**
     * Intent Types
     */
    public static final int INTENT_TYPE_LOCATION = 601;
    public static final int INTENT_TYPE_ACCELEROMETER = 602;
    public static final int INTENT_TYPE_REALTIME = 608;
    public static final int INTENT_TYPE_STM_RELAY = 609;

    /**
     * Extras
     */
    //Intent Type Extra
    public static final String EXTRA_INTENT_TYPE = "intentType";

    //Location Intent Extras
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_GPS_SPEED = "gpsSpeed";
    public static final String EXTRA_ORIENTATION = "orientation";
    public static final String EXTRA_HDOP = "hdop";
    public static final String EXTRA_SECOND = "second";
    public static final String EXTRA_MINUTE = "minute";
    public static final String EXTRA_HOUR = "hour";
    public static final String EXTRA_DATE = "date";
    public static final String EXTRA_MONTH = "month";
    public static final String EXTRA_YEAR = "year";

    //Realtime Intent Extras
    public static final String EXTRA_DISTANCE = "distance";
    public static final String EXTRA_RUNTIME = "runtime";
    public static final String EXTRA_RPM = "rpm";
    public static final String EXTRA_SPEED = "speed";
    public static final String EXTRA_THROTTLE = "throttle";
    public static final String EXTRA_LOAD = "load";
    public static final String EXTRA_INSTANT_FUEL_VOL = "instant_fuel_vol";

    //STM Relay Intent Extras
    public static final String EXTRA_DRIVE_SCORE = "drive_score";
    public static final String EXTRA_TOTAL_FUEL = "total_fuel";
    public static final String EXTRA_AVERAGE_SPEED = "average_speed";
    public static final String EXTRA_TOTAL_DISTANCE = "total_distance";
    public static final String EXTRA_TOTAL_TIME = "total_time";

    //Accelerometer Intent Extras
    public static final String EXTRA_ACC_X = "acc_x";
    public static final String EXTRA_ACC_Y = "acc_y";
    public static final String EXTRA_ACC_Z = "acc_z";
    public static final String EXTRA_V_BAT = "v_bat";
    public static final String EXTRA_B_BAT = "b_bat";
    public static final String EXTRA_S_REG = "s_reg";

    private BluetoothAdapter mBlueToothAdap;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters, listDevices;
    private BluetoothGatt mGatt;

    private Context mContext;
    private Handler mHandler;

    private String bleDeviceAddress = null;
    private String bleDeviceName = null;
    private BluetoothGattCharacteristic passkeyUIDCharacteristic = null;

    private Queue<BluetoothGattDescriptor> writeDescriptorQueue = new LinkedList<BluetoothGattDescriptor>();
    private Queue<BluetoothGattCharacteristic> readCharacteristicQueue = new LinkedList<BluetoothGattCharacteristic>();

    private Intent updateIntent;
    private final Handler UIUpdateHandler = new Handler();

    /**
     * Location Characteristic Data Variables
     */
    private int latitude;
    private int longitude;
    private int gpsSpeed;
    private int orientation;
    private int hdop;
    private int second;
    private int minute;
    private int hour;
    private int date;
    private int month;
    private int year;

    /**
     * Trip (STM Relay) Characteristic Data Variables
     */
    private float driveScore;
    private float totalFuel;
    private float averageSpeed;
    private float totalDistance;
    private int totalTime;

    /**
     * Realtime Characteristic Data Variables
     */
    private float instantDistance;
    private float instantFuelVol;
    private int rpm;
    private int speed;
    private int load;
    private int throttle;

    /**
     * Accelerometer Characteristic Data Variables
     */
    private int accX;
    private int accY;
    private int accZ;
    private int vBat;
    private int bBat;
    private int sReg;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    public class LocalBinder extends Binder {
        public BLEService getService() {
            return BLEService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Gathering bonding state of the BLE connection.
     */
    private final BroadcastReceiver mBondReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                switch (state) {
                    case BluetoothDevice.BOND_BONDING:
                        // Bonding...
                        Log.d("BOND", "Bonding...");
                        break;

                    case BluetoothDevice.BOND_BONDED:
                        // Bonded...
                        Log.d("BOND", "Bonded!");
                        break;

                    case BluetoothDevice.BOND_NONE:
                        // Not bonded...
                        Log.d("BOND", "Not bonded :(");
                        break;
                }
            }
        }
    };

    /**
     * Listen's the event when Bluetooth hardware is manually switched ON or OFF by the user.
     * <p>
     * ACTION_STATE_CHANGED Start the BLE Scanning procedure.
     * STATE_OFF and STATE_TURNING_OFF release the bluetooth scanner, adapter and callbacksReceived.
     * </p>
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        Log.i("Bluetooth State", "ON");
                        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                        mBlueToothAdap = bluetoothManager.getAdapter();
                        initiateScan();
                        break;

                    case BluetoothAdapter.STATE_OFF:
                        //Release the scanner and the adapter
                        Log.i("Bluetooth State", "OFF");
                        mLEScanner = null;
                        mBlueToothAdap = null;
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i("Bluetooth State", "Turning On");
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //Release the scanner and the adapter
                        Log.i("Bluetooth State", "Turning Off");
                        mLEScanner = null;
                        mBlueToothAdap = null;
                        break;
                }
            }
        }
    };

    /**
     * Method to start scanning for nearby BLE devices. Pass true to start scanning,
     * pass false to disable scanning. Scanning method depends on the API version
     *
     * @param enable Indicating whether to start/stop scanning for BLE devices.
     */
    private void scanLeDevice(final boolean enable) {
        //TODO: Handle scan failures
        if (enable) {
            Log.d("LESCAN", "Starting Scan");
            if (Build.VERSION.SDK_INT < APP_VERSION_BLE_CONN_PROC) {
                mBlueToothAdap.startLeScan(mLeScanCallback);
            } else {
                setupCallbacks();
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            Log.d("LESCAN", "Stopping Scan");
            if (Build.VERSION.SDK_INT < APP_VERSION_BLE_CONN_PROC) {
                mBlueToothAdap.stopLeScan(mLeScanCallback);
            } else {
                if (mLEScanner != null) {
                    mLEScanner.stopScan(mScanCallback);
                }
            }
        }
    }

    private void initiateScan() {
        if (mBlueToothAdap == null || !mBlueToothAdap.isEnabled()) {
            //If adapter is null or is not enabled
            //Log.e(TAG, "Bluetooth Is OFF. Cannot scan :(");
        } else {
            // [SCAN PROCEDURE]
            if (Build.VERSION.SDK_INT >= APP_VERSION_BLE_CONN_PROC) {
                mLEScanner = mBlueToothAdap.getBluetoothLeScanner();
                if (mLEScanner != null) {
                    ScanSettings.Builder bd = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
                    settings = bd.build();
                    filters = new ArrayList<ScanFilter>();
                } else {
                    //Log.e(TAG, "mLEScanner in timedTask runnable is null");
                }
            }
            scanLeDevice(true);
            // [SCAN PROCEDURE]
        }
    }

    private ScanCallback mScanCallback = null;

    /**
     * Setting up callbacks before starting the scan.
     * Function written separately as app crashes for API below 21 as ScanCallBack function is undefined for them.
     */
    private void setupCallbacks() {

        if (mScanCallback != null) return;

        if (Build.VERSION.SDK_INT >= APP_VERSION_BLE_CONN_PROC) {
            mScanCallback = new ScanCallback() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.d(TAG, "OnScanResult");
                    //Crash when a remote device will null name discovered. Hence the null check here.
                    if (result.getDevice().getName() != null) {
                        if (result.getDevice().getName().equals(BLE_DEVICE_NAME) && result.getRssi() > MINIMUM_RSSI_VALUE) {
                            bleDeviceName = result.getDevice().getName();
                            bleDeviceAddress = result.getDevice().getAddress();
                            //TODO: Store device name and address in SharedPrefs

                            connectToDevice(result.getDevice());
                            Log.d(TAG, "Connect to device");
                        }
                    }
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult sr : results) {
                        Log.i("BatchScanResult", sr.toString());
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Log.e("ScanFailed", "Error Code: " + errorCode);
                }
            };
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            if (device.getName() != null) {
                if (device.getName().equals(BLE_DEVICE_NAME) && rssi > MINIMUM_RSSI_VALUE) {
                    Runnable man = new Runnable() {
                        @Override
                        public void run() {
                            bleDeviceName = device.getName();
                            bleDeviceAddress = device.getAddress();

                            //TODO: Store device name and address in SharedPrefs
                            connectToDevice(device);
                            Log.d(TAG, "Connect to device");
                        }
                    };
                    man.run();
                }
            }
        }
    };

    /**
     * Used to establish connection to bluetooth device.
     * and stop furthur scanning procedure.
     *
     * @param device the device to which we want to connect.
     */
    public void connectToDevice(final BluetoothDevice device) {
        if (mGatt == null) {
            Log.d(TAG, "Attempting to connect");
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);    //Will stop after first device detection
        } else {
            Log.e(TAG, "Bluetooth GATT already connected to someone");
            try {
                mGatt.disconnect();
                mGatt.close();
                mGatt = null;
            } catch (NullPointerException np) {
                np.printStackTrace();
            }
            connectToDevice(device);
        }
    }

    public void enableNotifications(BluetoothGatt gatt, BluetoothGattCharacteristic charc) {
        boolean enabled = true;
        gatt.setCharacteristicNotification(charc, enabled);
        BluetoothGattDescriptor descriptor = charc.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        writeDescriptorQueue.add(descriptor);
    }

    public void readCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic charc) {
        if (gatt != null) {
            if (charc != null) {
                readCharacteristicQueue.add(charc);
            }
        }
    }

    public void processBLEoperations(BluetoothGatt gatt) {
        if (writeDescriptorQueue.size() > 0) {
            gatt.writeDescriptor(writeDescriptorQueue.element());
        } else if (readCharacteristicQueue.size() > 0) {
            gatt.readCharacteristic(readCharacteristicQueue.element());
        } else {

        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e(TAG, String.valueOf(status));
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    CAR_NO = 0;
                    passKeyWriteDone = false;
                    updateIntent.putExtra(ConstantCode.PASS_KEY_STATUS,passKeyWriteDone);
                    sendBroadcast(updateIntent);
                    gatt.discoverServices();
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    if(status == 19)
                    {
                        CAR_NO++;
                    }
                    else
                    {
                        CAR_NO = 0;
                    }
                    if (gatt.equals(mGatt)) {
                        mGatt.close();
                        mGatt = null;
                        Log.e("gattCallback", "STATE_DISCONNECTED and compared");

                        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                        mBlueToothAdap = bluetoothManager.getAdapter();
                        initiateScan();
                    }
                    Log.i("gattCallback", "STATE_DISCONNECTED");
                    passKeyWriteDone = false;
                    updateIntent.putExtra(ConstantCode.PASS_KEY_STATUS,passKeyWriteDone);
                    sendBroadcast(updateIntent);
                    break;

                default:
                    Log.d("gattCallback", "STATE_OTHER");
                    break;
            }
        }

        public byte[] hexStringToByteArray(String s) {
            int len = s.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i+1), 16));
            }
            return data;
        }


        /**
         * Six Bytes long
         * @param nrf
         * @return
         */
        public String calculatePassKey(String nrf)
        {
            if(nrf != null)
            {
                String passKey = null;
                int nrfIdStringLen = nrf.length();
                char[] nrfIdCharArray = nrf.toCharArray();
                Log.e("Tag", nrf);
                Log.e("TAG", String.valueOf(nrfIdCharArray[0]));
                //passKey 1st Byte = 1st Byte of NRF ID
                passKey = String.valueOf(nrfIdCharArray[0]) + String.valueOf(nrfIdCharArray[1]) + "";
                Log.e("TAG", passKey);
                //passkey 2nd Byte = 5th Byte of NRF ID
                passKey = passKey + nrfIdCharArray[8] + nrfIdCharArray[9] + "";

                //passkey 3rd Byte = 2nd Byte of NRF ID
                passKey = passKey + nrfIdCharArray[2] + nrfIdCharArray[3] + "";

                //passkey 4th Byte = 6th Byte of NRF ID
                passKey = passKey + nrfIdCharArray[10] + nrfIdCharArray[11] + "";

                //passkey 5th Byte = 3rd Byte of NRF ID
                passKey = passKey + nrfIdCharArray[4] + nrfIdCharArray[5] + "";

                //passkey 6th Byte = 7th Byte of NRF ID
                passKey = passKey + nrfIdCharArray[12] + nrfIdCharArray[13] + "";

                return passKey;
            }
            return null;
        }

        public String getNRFId()
        {
            if(list != null)
            {
                //TODO : fetch list of nrf id's from Cars table
                if(CAR_NO >= list.size())
                    CAR_NO = 0;
                Cars car= list.get(CAR_NO);
                return car.nrfid;
            }
            return null;

            //return "e8b1a2abb8777a8b";
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            List<BluetoothGattService> services = gatt.getServices();
            Log.e(TAG, "Services discovery complete: " + Integer.toString(status) + ", Services Number: " + Integer.toString(services.size()));
            PASS_KEY = calculatePassKey(getNRFId());
            Log.e("TAG",PASS_KEY);
            Log.e("TAG", String.valueOf(passKeyWriteDone));
            if(PASS_KEY != null)
            {
                if(!passKeyWriteDone)
                {
                    BluetoothGattService gs1 = gatt.getService(UUID.fromString(BLE_INFO_SERVICE_UUID));
                    BluetoothGattCharacteristic ch = gs1.getCharacteristic(UUID.fromString(BLE_INFO_CHAR_PASSKEY_UUID));
                    byte[] b = hexStringToByteArray(PASS_KEY);
                    ch.setValue(b);
                    boolean check = gatt.writeCharacteristic(ch);
                    if(check)
                    {
                        Log.e(TAG, "Write Characteristic for PassKey Verification Initiated !");
                    }
                    else
                    {
                        Log.e(TAG, "Write Characteristic for PassKey Verification Failed !");
                    }
                }
            }
            else
            {
                Log.e(TAG, "PASS_KEY is : " + PASS_KEY);
            }


            if(passKeyWriteDone)
            {
                for (BluetoothGattService gs : services) {
                    Log.i("[SRV]", gs.getUuid().toString());
                    List<BluetoothGattCharacteristic> allCharacteristics = gs.getCharacteristics();
                    for (final BluetoothGattCharacteristic singularChar : allCharacteristics) {
                        Log.i("[CHR]", singularChar.getUuid().toString());

                        switch (singularChar.getUuid().toString()) {
                            case BLE_INFO_CHAR_STM_UID_UUID:
                            case BLE_INFO_CHAR_NRF_UID_UUID:
                                readCharacteristic(gatt, singularChar);
                                break;

                            case BLE_INFO_CHAR_PASSKEY_UUID:
                                passkeyUIDCharacteristic = singularChar;
                                break;

                            case BLE_DATA_CHAR_REALTIME_UUID:
                            case BLE_DATA_CHAR_STM_RELAY_UUID:
                            case BLE_DATA_CHAR_LOCATION_UUID:
                            case BLE_DATA_CHAR_ACCELEROMETER_UUID:
                                enableNotifications(gatt, singularChar);
                                if (writeDescriptorQueue.size() == 1) {
                                    gatt.writeDescriptor(writeDescriptorQueue.element());
                                }
                                break;
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e(TAG, "-----IN READ-----");
            switch (characteristic.getUuid().toString()) {
                case BLE_INFO_CHAR_NRF_UID_UUID:
                    byte[] nrfUID = Arrays.copyOf(characteristic.getValue(), 8);
                    final String nrfUIDString = bytesToHex(nrfUID);
                    Log.d("[NRFUID]", nrfUIDString);
                    //TODO: Compare the NRF UID with that received from the server
                    //If not match, force disconnect & do not reconnect to this address
                    break;

                case BLE_INFO_CHAR_STM_UID_UUID:
                    //Store this as the device UID in Shared Prefs.
                    //Convert the 20 bytes received into a 16 byte array
                    byte[] stmUID = Arrays.copyOf(characteristic.getValue(), 16);
                    final String stmUIDString = bytesToHex(stmUID);
                    Log.d("[STMUID]", stmUIDString);

                    if (!stmUIDString.equals(DEFAULT_STM_UUID)) {
                        //We have received the correct STM UID
                        //TODO: Compare the STM UID with that received from server
                        //If not match, force disconnect & do not reconnect to this address
                    } else {
                        //We have received the default STM UID
                        //TODO: Schedule a STM UID read after 30 seconds
                    }
                    break;
            }

            //pop the item that we just finished reading
            readCharacteristicQueue.remove();

            //process pending BLE read and write operations
            processBLEoperations(gatt);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if(characteristic.getUuid().toString().equals(BLE_INFO_CHAR_PASSKEY_UUID))
            {
                if(BluetoothGatt.GATT_SUCCESS == status)
                {
                    if (!passKeyWriteDone) {
                        mGatt.discoverServices();
                        passKeyWriteDone = true;
                        updateIntent.putExtra(ConstantCode.PASS_KEY_STATUS,passKeyWriteDone);
                        sendBroadcast(updateIntent);
                    }
                }
                else
                {
                    passKeyWriteDone = false;
                    updateIntent.putExtra(ConstantCode.PASS_KEY_STATUS,passKeyWriteDone);
                    sendBroadcast(updateIntent);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            int temp;

            switch (characteristic.getUuid().toString()) {
                case BLE_DATA_CHAR_REALTIME_UUID:
                    Log.d(TAG, "Char Realtime");

                    byte[] realtime = Arrays.copyOf(characteristic.getValue(), 13);
                    temp = (realtime[0] & 0xFF) | ((realtime[1] & 0xFF) << 8) | ((realtime[2] & 0xFF) << 16) | ((realtime[3] & 0xFF) << 24);
                    instantDistance = Float.intBitsToFloat(temp);
                    temp = (realtime[4] & 0xFF) | ((realtime[5] & 0xFF) << 8) | ((realtime[6] & 0xFF) << 16) | ((realtime[7] & 0xFF) << 24);
                    instantFuelVol = Float.intBitsToFloat(temp);
                    rpm = ((realtime[8] & 0xFF) | ((realtime[9] & 0xFF) << 8));
                    speed = (realtime[10] & 0xFF);
                    load = (realtime[11] & 0xFF);
                    throttle = (realtime[12] & 0xFF);

                    UIUpdateHandler.post(new updateUI(INTENT_TYPE_REALTIME, instantDistance));
                    break;

                case BLE_DATA_CHAR_STM_RELAY_UUID:
                    Log.d(TAG, "Char STM Relay");

                    byte[] stmRelay = Arrays.copyOf(characteristic.getValue(), 18);
                    temp = (stmRelay[0] & 0xFF) | ((stmRelay[1] & 0xFF) << 8) | ((stmRelay[2] & 0xFF) << 16) | ((stmRelay[3] & 0xFF) << 24);
                    driveScore = Float.intBitsToFloat(temp);
                    temp = (stmRelay[4] & 0xFF) | ((stmRelay[5] & 0xFF) << 8) | ((stmRelay[6] & 0xFF) << 16) | ((stmRelay[7] & 0xFF) << 24);
                    totalFuel = Float.intBitsToFloat(temp);
                    temp = (stmRelay[8] & 0xFF) | ((stmRelay[9] & 0xFF) << 8) | ((stmRelay[10] & 0xFF) << 16) | ((stmRelay[11] & 0xFF) << 24);
                    averageSpeed = Float.intBitsToFloat(temp);
                    temp = (stmRelay[12] & 0xFF) | ((stmRelay[13] & 0xFF) << 8) | ((stmRelay[14] & 0xFF) << 16) | ((stmRelay[15] & 0xFF) << 24);
                    totalDistance = Float.intBitsToFloat(temp);
                    totalTime = ((stmRelay[16] & 0xFF) | ((stmRelay[17] & 0xFF) << 8));

                    UIUpdateHandler.post(new updateUI(INTENT_TYPE_STM_RELAY, driveScore));
                    break;

                case BLE_DATA_CHAR_LOCATION_UUID:
                    Log.d(TAG, "Char Location");

                    byte[] location = Arrays.copyOf(characteristic.getValue(), 20);
                    latitude = (location[0] & 0xFF) | ((location[1] & 0xFF) << 8) | ((location[2] & 0xFF) << 16) | ((location[3] & 0xFF) << 24);
                    longitude = (location[4] & 0xFF) | ((location[5] & 0xFF) << 8) | ((location[6] & 0xFF) << 16) | ((location[7] & 0xFF) << 24);
                    gpsSpeed = ((location[8] & 0xFF) | ((location[9] & 0xFF) << 8));
                    orientation = ((location[10] & 0xFF) | ((location[11] & 0xFF) << 8));
                    hdop = (location[12] & 0xFF) | ((location[13] & 0xFF) << 8) | ((location[14] & 0xFF) << 16) | ((location[15] & 0xFF) << 24);
                    second = (location[16] & 0xFF);
                    minute = (location[17] & 0xFF);
                    hour = (location[18] & 0xFF);
                    date = (location[19] & 0xFF);
                    //month = (location[1] & 0xFF);
                    //year = (location[16] & 0xFF);

                    UIUpdateHandler.post(new updateUI(INTENT_TYPE_LOCATION, latitude));
                    break;

                case BLE_DATA_CHAR_ACCELEROMETER_UUID:
                    Log.d(TAG, "Char Accelerometer");
                    byte[] accelerometer = Arrays.copyOf(characteristic.getValue(), 12);
                    accX = ((accelerometer[0] & 0xFF) | ((accelerometer[1] & 0xFF) << 8));
                    accY = ((accelerometer[2] & 0xFF) | ((accelerometer[3] & 0xFF) << 8));
                    accZ = ((accelerometer[4] & 0xFF) | ((accelerometer[5] & 0xFF) << 8));
                    vBat = ((accelerometer[6] & 0xFF) | ((accelerometer[7] & 0xFF) << 8));
                    bBat = ((accelerometer[8] & 0xFF) | ((accelerometer[9] & 0xFF) << 8));
                    sReg = ((accelerometer[10] & 0xFF) | ((accelerometer[11] & 0xFF) << 8));

                    UIUpdateHandler.post(new updateUI(INTENT_TYPE_ACCELEROMETER, accX));
                    break;
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.e(TAG, "-----IN DESCRIPTOR WRITE-----");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e("[DESCW]", "Callback: Wrote GATT Descriptor successfully.");
            } else {
                Log.d("[DESCW]", "Callback: Error writing GATT Descriptor: " + status);
            }

            //pop the item that we just finishing writing
            writeDescriptorQueue.remove();

            //process pending BLE read and write operations
            processBLEoperations(gatt);

            /*
            //if there is more to write, do it!
            if(writeDescriptorQueue.size() > 0)
            {
                //Oh yes there is more to write. Writing descriptors...
                gatt.writeDescriptor(writeDescriptorQueue.element());
            }
            else if(readCharacteristicQueue.size() > 0)
            {
                gatt.readCharacteristic(readCharacteristicQueue.element());
            }
            */
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        updateIntent = new Intent(UI_UPDATE_ACTION);

        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_LONG).show();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBlueToothAdap = bluetoothManager.getAdapter();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        IntentFilter ifilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBondReceiver, ifilter);

        list = Cars.readAll();

        initiateScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }

    /**
     * BroadCasting the intent so that UI could be updated across all the screens
     * Intent type are :
     * <ul>
     * <li>INTENT_TYPE_LOCATION</li>
     * <li>INTENT_TYPE_ACCELERATION</li>
     * <li>INTENT_TYPE_REALTIME</li>
     * <li>INTENT_TYPE_STM_RELAY</li>
     * </ul>
     */
    private class updateUI implements Runnable {
        private int intentType = 0;
        private Object obj;

        public updateUI(int type, Object o) {
            intentType = type;
            obj = o;
        }

        @Override
        public void run() {
            updateIntent.putExtra(EXTRA_INTENT_TYPE, intentType);
            switch (intentType) {
                case INTENT_TYPE_LOCATION:
                    updateIntent.putExtra(EXTRA_LATITUDE, latitude);
                    updateIntent.putExtra(EXTRA_LONGITUDE, longitude);
                    updateIntent.putExtra(EXTRA_GPS_SPEED, gpsSpeed);
                    updateIntent.putExtra(EXTRA_ORIENTATION, orientation);
                    updateIntent.putExtra(EXTRA_HDOP, hdop);
                    updateIntent.putExtra(EXTRA_SECOND, second);
                    updateIntent.putExtra(EXTRA_MINUTE, minute);
                    updateIntent.putExtra(EXTRA_HOUR, hour);
                    updateIntent.putExtra(EXTRA_DATE, date);
                    updateIntent.putExtra(EXTRA_MONTH, month);
                    updateIntent.putExtra(EXTRA_YEAR, year);
                    break;

                case INTENT_TYPE_REALTIME:
                    updateIntent.putExtra(EXTRA_DISTANCE, instantDistance);
                    updateIntent.putExtra(EXTRA_RPM, rpm);
                    updateIntent.putExtra(EXTRA_LOAD, load);
                    updateIntent.putExtra(EXTRA_INSTANT_FUEL_VOL, instantFuelVol);
                    updateIntent.putExtra(EXTRA_SPEED, speed);
                    updateIntent.putExtra(EXTRA_THROTTLE, throttle);
                    break;

                case INTENT_TYPE_STM_RELAY:
                    updateIntent.putExtra(EXTRA_DRIVE_SCORE, driveScore);
                    updateIntent.putExtra(EXTRA_TOTAL_FUEL, totalFuel);
                    updateIntent.putExtra(EXTRA_AVERAGE_SPEED, averageSpeed);
                    updateIntent.putExtra(EXTRA_TOTAL_DISTANCE, totalDistance);
                    updateIntent.putExtra(EXTRA_TOTAL_TIME, totalTime);
                    break;

                case INTENT_TYPE_ACCELEROMETER:
                    updateIntent.putExtra(EXTRA_ACC_X, accX);
                    updateIntent.putExtra(EXTRA_ACC_Y, accY);
                    updateIntent.putExtra(EXTRA_ACC_Z, accZ);
                    updateIntent.putExtra(EXTRA_V_BAT, vBat);
                    updateIntent.putExtra(EXTRA_B_BAT, bBat);
                    updateIntent.putExtra(EXTRA_S_REG, sReg);
                    break;

                default:
                    break;
            }

            sendBroadcast(updateIntent);
        }
    }

    /**
     * The Hex array.
     */
    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    /**
     * Bytes to hex string conversion.
     *
     * @param bytes the bytes
     * @return the string
     */
    //Fetched from here: http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    //The best solution there is
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
