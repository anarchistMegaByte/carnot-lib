package com.carnot.global.gcm;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.DateHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by pankaj on 7/6/16.
 */
@Table(name = "GCMMessage")
public class GCMMessage extends Model {
    @Column
    public int userId;
    @Column
    public int notification_id;
    @Column
    public String category;
    @Column
    public String collapse_key;

    //data
    @Column
    public String title;
    @Column
    public String message;
    @Column
    public int sub_priority;
    @Column
    public int priority;
    @Column
    public double lat;
    @Column
    public double lon;
    @Column
    public String code;

    //trigger
    @Column
    public boolean showInTray;
    @Column
    public boolean showInMyCarSays;
    @Column
    public boolean showInNotificationBell;
    @Column
    public boolean showInRingtone;
    @Column
    public boolean showInFullScreen;
    @Column
    public boolean showInCarIcon;

    //dismiss
    @Column
    public boolean dismissTray;
    @Column
    public boolean dismissCarSays;
    @Column
    public boolean dismissRemindTomorrow;
    @Column
    public boolean dismissAtTime;
    @Column
    public boolean dismissCardAppears;

    //meta data
    @Column
    public String coolant_temp;
    @Column
    public String ref_link;
    @Column
    public long dismissTime;
    @Column
    public boolean isRead;
    @Column
    public boolean isDismissed;

    @Column
    public int carId;

    @Column
    public long timestamp;

    public GCMMessage() {

    }

    public GCMMessage(Bundle data) {
        try {
            carId = Integer.parseInt(data.getString(ConstantCode.gcmCarID));
            userId = data.getInt(ConstantCode.userid);

            notification_id = Integer.parseInt(data.getString("id"));
            category = data.getString(ConstantCode.category);
            collapse_key = data.getString(ConstantCode.collapse_key);
            JSONArray jsonTrigger = new JSONArray(data.getString(ConstantCode.trigger));
            for (int i = 0; i < jsonTrigger.length(); i++) {
                showInRingtone = jsonTrigger.getInt(i) == GCMUtils.NOTIFICATION_TRIGGER_RING_VIBRATE ? true : showInRingtone;
                showInTray = jsonTrigger.getInt(i) == GCMUtils.NOTIFICATION_TRIGGER_SYSTEM_TRAY ? true : showInTray;
                showInMyCarSays = jsonTrigger.getInt(i) == GCMUtils.NOTIFICATION_TRIGGER_MY_CAR_SAYS ? true : showInMyCarSays;
                showInFullScreen = jsonTrigger.getInt(i) == GCMUtils.NOTIFICATION_TRIGGER_FULL_SCREEN ? true : showInFullScreen;
                showInNotificationBell = jsonTrigger.getInt(i) == GCMUtils.NOTIFICATION_TRIGGER_BELL_SCREEN ? true : showInNotificationBell;
                showInCarIcon = jsonTrigger.getInt(i) == GCMUtils.NOTIFICATION_TRIGGER_ICON_ON_CAR ? true : showInCarIcon;
            }
            JSONObject jsonData = new JSONObject(data.getString(ConstantCode.data));
            if (jsonData != null) {
                title = jsonData.optString(ConstantCode.title);
                message = jsonData.optString(ConstantCode.message);
                priority = jsonData.optInt(ConstantCode.priority);
                sub_priority = jsonData.optInt(ConstantCode.sub_priority);
                if (jsonData.has(ConstantCode.lat))
                    lat = jsonData.optDouble(ConstantCode.lat);
                if (jsonData.has(ConstantCode.lon))
                    lon = jsonData.optDouble(ConstantCode.lon);
                code = jsonData.optString(ConstantCode.code);
                timestamp = System.currentTimeMillis();
                if (jsonData.has(ConstantCode.timestamp)) {
                    Calendar calendar = DateHelper.getCalendarFromServer(jsonData.getString(ConstantCode.timestamp));
                    if (calendar != null) {
                        timestamp = calendar.getTimeInMillis();
                    }
                }
            }
            JSONArray jsonDismiss = new JSONArray(data.getString(ConstantCode.dismiss));
            for (int i = 0; i < jsonDismiss.length(); i++) {

                dismissTray = jsonDismiss.getInt(i) == GCMUtils.NOTIFICATION_DISMISS_SYSTEM_TRAY ? true : dismissTray;
                dismissCarSays = jsonDismiss.getInt(i) == GCMUtils.NOTIFICATION_DISMISS_MY_CAR_SAYS ? true : dismissCarSays;
                dismissRemindTomorrow = jsonDismiss.getInt(i) == GCMUtils.NOTIFICATION_DISMISS_REMIND_TOMORROW ? true : dismissRemindTomorrow;
                dismissAtTime = jsonDismiss.getInt(i) == GCMUtils.NOTIFICATION_DISMISS_AUTO_DISMISS_TIME ? true : dismissAtTime;
                dismissCardAppears = jsonDismiss.getInt(i) == GCMUtils.NOTIFICATION_DISMISS_CARD_APPEARS ? true : dismissCardAppears;

            }
            JSONObject jsonMetaData = new JSONObject(data.getString(ConstantCode.metadata));
            if (jsonMetaData.has(ConstantCode.time)) {
                String time = jsonMetaData.optString(ConstantCode.time);
                if (time != null && time.contains(":")) {
                    String hrmin[] = time.split(":");
                    if (hrmin.length == 2) {
                        int hrs = Integer.parseInt(hrmin[0]);
                        int mins = Integer.parseInt(hrmin[1]);
                        Calendar calendarToday = Calendar.getInstance();
                        calendarToday.setTimeInMillis(timestamp);
                        calendarToday.set(Calendar.HOUR_OF_DAY, hrs);
                        calendarToday.set(Calendar.MINUTE, mins);
                        calendarToday.set(Calendar.SECOND, 0);
                        dismissTime = calendarToday.getTimeInMillis();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static GCMMessage getHighPriorityLastMessage() {
        ArrayList<GCMMessage> list = new Select().from(GCMMessage.class)
                .where("userId='" + Utility.getLoggedInUser().id + "'")
                .and("isDismissed='0'")
                .and("showInFullScreen='1'")
                .orderBy("priority DESC,sub_priority DESC")
                .execute();

        Log.d("DB", "readAll() called with: " + list.size());
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


    @Override
    public Long save() {
        userId = Utility.getLoggedInUser().id;
        long id = super.save();
        Log.d("DB", "save() called with: " + "" + id);
        return id;
    }

    /**
     * Getting all notification to be shown in notification screen. and updateing the read status to maintain the counter
     *
     * @return
     */
    public static ArrayList<GCMMessage> readAll() {
        ArrayList<GCMMessage> list = new Select().from(GCMMessage.class)
                .where("userId='" + Utility.getLoggedInUser().id + "'")
                .and("showInNotificationBell='1'")
                .orderBy("id DESC")
                .execute();
        Log.d("DB", "readAll() called with: " + list.size());
        //updating unread to read
        updateReadStatus();
        return list;
    }

    /**
     * Getting all notification which are not dismissed with id in ascending order
     *
     * @return
     */
    public static ArrayList<GCMMessage> readAllNotificationInASCOrder() {
        ArrayList<GCMMessage> list = new Select().from(GCMMessage.class)
                .where("userId='" + Utility.getLoggedInUser().id + "'")
                .and("isDismissed='0'")
                .orderBy("id ASC")
                .execute();
        Log.d("DB", "readAll() called with: " + list.size());
        return list;
    }

    /**
     * Getting unread Counter to show on bell icon
     *
     * @return
     */
    public static int getUnreadCount() {
        Cursor cursor = ActiveAndroid.getDatabase().rawQuery("SELECT COUNT(1) COUNT FROM GCMMessage Where isRead='0' AND showInNotificationBell='1' AND userId='" + Utility.getLoggedInUser().id + "'", null);
        int unreadCounter = 0;
        if (cursor != null && cursor.moveToFirst()) {
            unreadCounter = Integer.parseInt(cursor.getString(0));
        }
        cursor.close();
        return unreadCounter;
    }

    /**
     * Updating the read status of all the notification when user goes to notification screen.
     */
    public static void updateReadStatus() {
        new Update(GCMMessage.class).set("isRead=?", new String[]{"1"}).where("userId='" + Utility.getLoggedInUser().id + "'").execute();
    }

    /**
     * Changing the dismiss status of notification so that it will not come again.
     */
    public void dismissMessage() {
        new Update(GCMMessage.class).set("isDismissed=?", new String[]{"1"}).where("notification_id='" + notification_id + "'").execute();
    }
}
