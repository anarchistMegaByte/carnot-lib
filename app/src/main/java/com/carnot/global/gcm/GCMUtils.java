package com.carnot.global.gcm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.carnot.R;
import com.carnot.activity.ActivityDashboard;
import com.carnot.global.AlarmService;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;

import java.util.Calendar;

/**
 * Created by pankaj on 7/6/16.
 */
public class GCMUtils {

    Context context;
    NotificationReceive callback;
    boolean isRegistered = false;

    public GCMUtils(Context context) {
        this.context = context;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("GCMUtils", "onReceive() called with: " + "context = [" + context + "], intent = [" + intent + "]");
            if (callback != null) {
                GCMMessage gcmMessage = (GCMMessage) Utility.parseFromString(intent.getStringExtra(ConstantCode.INTENT_DATA), GCMMessage.class);
                Log.e("GCMUtils : ",intent.getIntExtra("car_id",-1)+"");
                if (intent.getBooleanExtra(ConstantCode.INTENT_DISMISS, false)) {
                    callback.onNotificationDismiss(gcmMessage);
                } else {

                    callback.onNotificationReceive(gcmMessage);
                }
            }
        }
    };

    public void unregister() {
        if (isRegistered == true) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
            isRegistered = false;
        }
    }

    public void register(NotificationReceive callback) {
        this.callback = callback;
        if (isRegistered == false) {
            LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter(ConstantCode.BROADCAST_NOTIFICATION_RECEIVE));
            isRegistered = true;
        }
    }

    public interface NotificationReceive {
        public void onNotificationReceive(GCMMessage message);

        public void onNotificationDismiss(GCMMessage message);
    }

    //====================STATIC METHODS==================

    public static void sendBroadCast(Context context, GCMMessage gcmMessage) {
        Intent broadcast = new Intent(ConstantCode.BROADCAST_NOTIFICATION_RECEIVE);
        broadcast.putExtra(ConstantCode.INTENT_DATA, Utility.getJsonString(gcmMessage));
        broadcast.putExtra(ConstantCode.INTENT_DISMISS, false);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
    }

    public static void sendDismissBroadCast(Context context, GCMMessage gcmMessage) {
        Intent broadcast = new Intent(ConstantCode.BROADCAST_NOTIFICATION_RECEIVE);
        broadcast.putExtra(ConstantCode.INTENT_DATA, Utility.getJsonString(gcmMessage));
        broadcast.putExtra(ConstantCode.INTENT_DISMISS, true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
    }

    private static NotificationManager notificationManager;

    public static NotificationManager initNotificationManager(Context context) {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    private static AlarmManager alarmManager;

    public static NotificationManager initAlarmManager(Context context) {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        return notificationManager;
    }

    public static void cancelNotification(Context context, int notification_id) {
        initNotificationManager(context);
        notificationManager.cancel(notification_id);
    }

    public static void sendSimpleNotification(Context context, GCMMessage gcmMessage) {
        initNotificationManager(context);
        initAlarmManager(context);

        if (gcmMessage.showInMyCarSays || gcmMessage.showInFullScreen || gcmMessage.showInNotificationBell || gcmMessage.showInCarIcon) {
            GCMUtils.sendBroadCast(context, gcmMessage);
        }

        Intent intent = new Intent(context, ActivityDashboard.class);
        intent.putExtra(ConstantCode.INTENT_MESSAGE, gcmMessage.message);
        intent.putExtra(ConstantCode.INTENT_TITLE, gcmMessage.title);
        intent.putExtra(ConstantCode.INTENT_DATA, Utility.getJsonString(gcmMessage));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, gcmMessage.notification_id /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(gcmMessage.title)
                .setContentText(gcmMessage.message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (gcmMessage.showInRingtone) {
            notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);

            Uri soundUri = Uri.parse("android.resource://com.carnot/" + R.raw.alarm_carnot);
            notificationBuilder.setSound(soundUri);

        }

        if (gcmMessage.showInTray) {
            notificationManager.notify(gcmMessage.notification_id /* ID of notification */, notificationBuilder.build());
        }
    }

    public static void remindForNotification(Context context, GCMMessage gcmMessage) {
        //Reminding Options logic here
        if (gcmMessage.dismissRemindTomorrow) {
            Log.d("ALARM__", "SETTING ALARM 1");
            //REMIND TOMORROW
            Intent intentPendingIntent = new Intent(context, AlarmService.class);
            intentPendingIntent.putExtra(ConstantCode.INTENT_NOTIFICATION_ID, gcmMessage.notification_id);
            intentPendingIntent.putExtra(ConstantCode.INTENT_DATA, Utility.getJsonString(gcmMessage));
            intentPendingIntent.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_REMIND);
            PendingIntent pi = PendingIntent.getBroadcast(context, gcmMessage.notification_id * Utility.getRandomeNumber(8, 1), intentPendingIntent, 0);

            Calendar calendarToday = Calendar.getInstance();
            calendarToday.setTimeInMillis(gcmMessage.timestamp);
            calendarToday.add(Calendar.DAY_OF_MONTH, 1);//adding 1 so it became tomorrow same time
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendarToday.getTimeInMillis(), pi);
        }
        if (gcmMessage.dismissAtTime) {
            Log.d("ALARM__", "SETTING ALARM 2");
            //DISMISS AT SPECIFIC TIME
            Intent intentPendingIntent = new Intent(context, AlarmService.class);
            intentPendingIntent.putExtra(ConstantCode.INTENT_NOTIFICATION_ID, gcmMessage.notification_id);
            intentPendingIntent.putExtra(ConstantCode.INTENT_DATA, Utility.getJsonString(gcmMessage));
            intentPendingIntent.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_DISMISS);
            PendingIntent pi = PendingIntent.getBroadcast(context, gcmMessage.notification_id * Utility.getRandomeNumber(8, 1), intentPendingIntent, 0);

            Calendar calendarToday = Calendar.getInstance();
            calendarToday.setTimeInMillis(gcmMessage.dismissTime);
            calendarToday.set(Calendar.SECOND, 0);
//            Log.d("ALARM__", "SETTING ALARM 2" + calendarToday.getTimeInMillis());
//            calendarToday.add(Calendar.SECOND, 15);
//            Log.d("ALARM__", "SETTING ALARM 2" + calendarToday.getTimeInMillis());
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendarToday.getTimeInMillis(), pi);
        }

    }

    public static int NOTIFICATION_TRIGGER_RING_VIBRATE = 0;
    public static int NOTIFICATION_TRIGGER_SYSTEM_TRAY = 1;
    public static int NOTIFICATION_TRIGGER_MY_CAR_SAYS = 2;//notifi broad
    public static int NOTIFICATION_TRIGGER_FULL_SCREEN = 3;//notifi broad
    public static int NOTIFICATION_TRIGGER_BELL_SCREEN = 4;//notifi broad
    public static int NOTIFICATION_TRIGGER_ICON_ON_CAR = 5;//notifi broad

    public static int NOTIFICATION_DISMISS_SYSTEM_TRAY = 0;//notifi broad
    public static int NOTIFICATION_DISMISS_MY_CAR_SAYS = 1;//notifi broad
    public static int NOTIFICATION_DISMISS_REMIND_TOMORROW = 2;//notifi broad
    public static int NOTIFICATION_DISMISS_AUTO_DISMISS_TIME = 3;//notifi broad
    public static int NOTIFICATION_DISMISS_CARD_APPEARS = 4;//notifi broad

    public static final String Tow_Alert = "M1001";
    public static final String Device_Pullout_Alert = "M1004";
    public static final String Crash_Alert = "M1005";
    public static final String Vandal_Alert = "M1006";
    public static final String Parking_Alert = "M1007";
    public static final String Battery_Critical_Alert = "N1002";
    public static final String Coolant_Error = "T1002";
    public static final String Cabin_Temperature_Error = "T1005";

}
