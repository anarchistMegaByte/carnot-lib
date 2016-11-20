package com.carnot.global.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.carnot.R;
import com.carnot.activity.ActivityDashboard;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "data: " + data.toString());
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        //Checking if user is not logged in then we have not to do anything simply return
        if (Utility.getLoggedInUser() == null)
            return;


        /**
         * Bundle[{message={     "trigger": [         0,         1,         2     ],     "metadata": {},     "id": 1,     "data": {         "sub-priority": 0,         "priority": 4,         "message": "",         "title": ""     },     "category": "CUSTOM",     "dismiss": [         1     ] }, collapse_key=do_not_collapse}]
         */
        try {

            //below try catch is to only send notication from http://apns-gcm.bryantan.info/
            /**
             * {
             "id": 642293048,
             "trigger": [
             0,
             1,
             2,
             3,
             4
             ],
             "category": "CUSTOM",
             "data": {
             "sub-priority": 0,
             "title": "Your car petrol is out",
             "message": "Hello test priority 2",
             "priority": 4
             },
             "dismiss": [
             1
             ],
             "metadata": {}
             }
             */
            try {
                Log.e("In MyGCMListener abey :",data.containsKey("message")+"");
                if (data.containsKey("message"))
                {
                    if (message == null) {
                        throw new Exception("Message field is null when it is comming from apns-gcm site");
                    }

                    JSONObject jsonObject = new JSONObject(message);
                    data = new Bundle();
                    data.putString(ConstantCode.id, jsonObject.getString("id"));
                    data.putString(ConstantCode.trigger, jsonObject.getString("trigger"));
                    data.putString(ConstantCode.metadata, jsonObject.getString("metadata"));
                    data.putString(ConstantCode.data, jsonObject.getString("data"));
                    data.putString(ConstantCode.category, jsonObject.getString("category"));
                    data.putString(ConstantCode.dismiss, jsonObject.getString("dismiss"));
                    //Put car ID

                    data.putInt(ConstantCode.carid, jsonObject.getInt("car_id"));
                    data.putInt(ConstantCode.userid, jsonObject.getInt("user_id"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            GCMMessage gcmMessage = new GCMMessage(data);
            gcmMessage.isRead = false;
            gcmMessage.save();

            GCMUtils.sendSimpleNotification(getBaseContext(), gcmMessage);
            GCMUtils.remindForNotification(getBaseContext(), gcmMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, ActivityDashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


}