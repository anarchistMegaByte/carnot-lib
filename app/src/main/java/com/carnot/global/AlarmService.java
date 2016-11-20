package com.carnot.global;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.carnot.App;
import com.carnot.R;
import com.carnot.activity.ActivityAllTrips;
import com.carnot.fragment.FragmentCars;
import com.carnot.global.ConstantCode;
import com.carnot.global.PrefManager;
import com.carnot.global.Utility;
import com.carnot.global.gcm.GCMMessage;
import com.carnot.global.gcm.GCMUtils;

import java.util.Calendar;

/**
 * Created by pankaj on 7/7/16.
 */
public class AlarmService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("ALARM__", "ALARM SERVICE FIRED onReceive() called with: " + "context = [" + context + "], intent = [" + intent + "]");
        if (ConstantCode.ACTION_REMIND.equalsIgnoreCase(intent.getStringExtra(ConstantCode.INTENT_ACTION))) {

            GCMMessage gcmMessage = (GCMMessage) Utility.parseFromString(intent.getStringExtra(ConstantCode.INTENT_DATA), GCMMessage.class);
            gcmMessage.isDismissed = false;
            gcmMessage.save();

            GCMUtils.sendSimpleNotification(context, gcmMessage);

        } else if (ConstantCode.ACTION_DISMISS.equalsIgnoreCase(intent.getStringExtra(ConstantCode.INTENT_ACTION))) {
            Log.d("ALARM__", "DISMISSING ALARM");
            GCMMessage gcmMessage = (GCMMessage) Utility.parseFromString(intent.getStringExtra(ConstantCode.INTENT_DATA), GCMMessage.class);
            gcmMessage.isDismissed = true;
            gcmMessage.save();

            Log.d("ALARM__", "SENDING DISMISS BROADCAST");
            GCMUtils.sendDismissBroadCast(context, gcmMessage);

        }
        else if(ConstantCode.ACTION_TRIP_SYNC_ALRAM.equalsIgnoreCase(intent.getStringExtra(ConstantCode.INTENT_TRIP_SYNC)))
        {
            Calendar cal = Calendar.getInstance();
            if(!PrefManager.getInstance().contains(ConstantCode.TRIP_SYNC_FLAG))
            {
                Log.e("Inside Alaram : ","Alaram");
                ActivityAllTrips.makeGetRecentTripCall(context);
            }
            else
            {
                if(!PrefManager.getBoolean(ConstantCode.TRIP_SYNC_FLAG))
                {
                    ActivityAllTrips.makeGetRecentTripCall(context);
                }
                else
                {
                    //Toast.makeText(mActivity, "Trip Syncing Under Progress",Toast.LENGTH_LONG).show();
                    Log.e("FromAlaram", "Previous request is under process");
                }
            }
        }
        else if(ConstantCode.ACTION_TIMER_COMPLETED.equalsIgnoreCase(intent.getStringExtra(ConstantCode.INTENT_ACTION)))
        {
            int carID = intent.getIntExtra(ConstantCode.INTENT_CAR_ID,-1);
            if(carID != -1)
            {
                if(!PrefManager.getInstance().contains(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carID)) )
                {
                    //Always getting created here only not anywhere else
                    PrefManager.putBoolean(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carID), true);
                }
                else
                {
                    PrefManager.getInstance().edit().putBoolean(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carID),false).commit();
                }
            }
        }
    }
}
