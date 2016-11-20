package com.carnot.models;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chaks on 10/6/16.
 */

@Table(name="GlobalNotifSwitch")
public class GlobalNotifSwitch extends Model{

    @Column
    public Integer geofence;

    @Column
    public Integer accident;

    @Column
    public Integer towing;

    @Column
    public Integer vandalism;

    @Column
    public Integer rashDriving;

    @Column
    public Integer carId;

    @Column
    public String vandalStart;

    @Column
    public String vandalEnd;


    public static ArrayList<GlobalNotifSwitch> readAll(Integer carId) {
        ArrayList<GlobalNotifSwitch> list = new Select().from(GlobalNotifSwitch.class)
                .where("carId='" + carId + "'")
                .execute();
        return list;
    }

    public static Boolean edit_sync(final Context context, final GlobalNotifSwitch gns, HashMap<String, Object> metaList, final Snackbar snackbar){
        Log.e("Edit Sync", "edit_sync: metalist " + metaList );
        final Boolean[] status = {false};
        String carId = String.valueOf(gns.carId);
        WebUtils.call(WebServiceConfig.WebService.SET_GLOBAL_NOTIFICATIONS, new String[]{carId}, metaList, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                JSONObject json = (JSONObject) values;
                Log.e("Edit Sync", "successWithString: " + json);
                update_custom(gns);
                status[0] = true;
                snackbar.show();
            }
            @Override
            public void loadOffline(){
                super.loadOffline();
                Toast toast = Toast.makeText(context, "No Internet Connection. Changes will not be saved", Toast.LENGTH_LONG);
                toast.show();
            }
        });
        return status[0];
    }

    public static void update_custom(GlobalNotifSwitch gns){
        Log.e("Save switch", "update_custom: " + gns.geofence + " " + gns.accident);
        new Update(GlobalNotifSwitch.class).set("geofence=?,vandalism=?,towing=?,accident=?,rashDriving=?,vandalStart=?,vandalEnd=?",
                gns.geofence, gns.vandalism, gns.towing, gns.accident, gns.rashDriving, gns.vandalStart, gns.vandalEnd).
                where("carId=?", gns.carId).execute();
    }

//          CHECK WHY THIS BREAKS
//        public GlobalNotifSwitch GlobalNotifSwitch(Boolean geofence, Boolean accident, Boolean towing, Boolean vandalism, Boolean rashDriving, Integer carId) {
//            this.geofence = geofence;
//            this.accident = accident;
//            this.towing = towing;
//            this.vandalism = vandalism;
//            this.rashDriving = rashDriving;
//            this.carId = carId;
//            this.save();
//            return this;
//        }

}
