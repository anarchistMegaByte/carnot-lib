package com.carnot.models;

import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.carnot.adapter.GeofencesAdapter;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Created by chaks on 10/4/16.
 */

@Table(name="Geofence")
public class Geofence extends Model{

    @Column
    public String name;

    @Column
    public Integer carId;

    @Column
    public Integer status;

    @Column
    public String radius;

    @Column
    public String lat;

    @Column
    public String lng;

    @Column
    public String geo_id;

    @Column
    public Integer isLive;


    public static ArrayList<Geofence> readAll(Integer carId) {
        ArrayList<Geofence> list = new Select().from(Geofence.class)
                .where("carId='" + carId + "'")
                .execute();
        return list;
    }

    public static void custom_delete(String geo_id){
        new Delete().from(Geofence.class).where("geo_id= ?", geo_id).execute();
    }

    public static void update_all(Geofence geo){
        new Update(Geofence.class).set("name=?,carId=?,status=?,radius=?,lat=?,lng=?,isLive=?",
                geo.name, geo.carId, geo.status, geo.radius, geo.lat, geo.lng, geo.isLive).
                where("geo_id=?", geo.geo_id).execute();
        Log.e("Geofence Models", "update_all: updated");
    }


    public static Integer get_carId(String geo_id){
        ArrayList<Geofence> geo = new Select().from(Geofence.class)
                .where("geo_id=?",  geo_id)
                .execute();
        return  geo.get(0).carId;
    }

    public static Boolean delete_all(){
        new Delete().from(Geofence.class).executeSingle();
        return  true;
    }

    public static Boolean edit_sync(final String geo_id, final HashMap<String, Object> metaList, final String args, final Snackbar snackbar){
            Log.e("Edit Sync", "edit_sync: metalist " + metaList );
            final Boolean[] status = {false};
            final String carId = String.valueOf(get_carId(geo_id));
            WebUtils.call(WebServiceConfig.WebService.EDIT_GEOFENCE, new String[]{carId}, metaList, new NetworkCallbacks() {
                @Override
                public void successWithString(Object values, WebServiceConfig.WebService webService) {
                    super.successWithString(values, webService);
//                todo add to local db on success
                    JSONObject json = (JSONObject) values;
                    Log.e("Edit Sync", "successWithString: " + json);
                    status[0] = true;
                    snackbar.show();
//                    update on success response
                    Geofence geo = new Geofence();
                    geo.carId = Integer.valueOf(carId);
                    geo.geo_id = (String) metaList.get("geo_id");
                    geo.lat = (String) metaList.get("lat");
                    geo.lng = (String) metaList.get("lng");
                    geo.name = (String) metaList.get("name");
                    geo.radius = (String) metaList.get("radius");
                    geo.status = (Integer) metaList.get("status");
                    geo.isLive = (Integer) metaList.get("isLive");
                    Geofence.update_all(geo);

                }
                @Override
                public void loadOffline(){
                    super.loadOffline();
                    Toast toast = Toast.makeText(snackbar.getView().getContext(), "No Internet Connection. Changes will not be saved", Toast.LENGTH_LONG);
                    toast.show();
                }
            });

        return true;

    }

    public static Boolean del_geo(String geo_id){
        final Boolean[] status = {false};
        String carId = String.valueOf(get_carId(geo_id));
        WebUtils.call(WebServiceConfig.WebService.DELETE_GEOFENCE, new String[]{carId}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                JSONObject json = (JSONObject) values;
                Log.e("Delete Sync", "successWithString: " + json);
                status[0] = true;
            }
        });
        return status[0];
    }


    public static Geofence get(String geo_id){
        Log.e("get fucntion", "get: geoid " + geo_id);
        ArrayList<Geofence> all = Geofence.readAll(1);
        Log.e("get fucntion", "get: all " + all);

        ArrayList<Geofence> geo = new Select().from(Geofence.class)
                .where("geo_id=?", geo_id)
                .execute();
        Log.e("get fucntion", "get: " + geo);
        return geo.get(0);

    }

}
