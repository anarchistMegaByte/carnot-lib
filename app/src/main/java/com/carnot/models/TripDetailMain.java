package com.carnot.models;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.OnReadData;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.carnot.global.Utility;
import com.carnot.libclasses.DateHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by pankaj on 6/24/16.
 */
@Table(name = "TripDetailMain")
public class TripDetailMain extends Model {
    @Column(name = "tripId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int trip_id;

    @Column(name = "device_gen_id")
    public int id;
    @Column
    public double distance;


    public String headers;
    @Column
    public int free_space;

    @Column
    public int message_id;
    @Column
    public double avg_mileage;
    @Column
    public int speeding_count;
    @Column
    public ArrayList<Routes> route = new ArrayList<Routes>();
    @Column
    public String jsonRoutes;
    @Column
    public String start_time;
    @Column
    public double avg_speed;
    @Column
    public double start_lat;

    @Column
    public double start_lon;


    //=============

    /*@Column
    public int hard_brake_count;
    @Column
    public int hardAcc;
    @Column
    public int hard_acc_count;
    @Column
    public int hardBrake;
    *//*@Column
    public int idling;*//*
    @Column
    public int cluth;
    //=============*/

    @Column
    public int total_idling_time;

    //==========DRIVE SCORE RELATED CHANGED NEW 8-7=========

    @Column
    public int drive_score;

    @Column
    public double os_sc;
    @Column
    public double os_m;
    @Column
    public String os_p;

    @Column
    public double hab_sc;
    @Column
    public double hab_m;
    @Column
    public String hab_p;

    @Column
    public double ed_sc;
    @Column
    public double ed_m;
    @Column
    public String ed_p;

    @Column
    public double i_sc;
    @Column
    public double i_m;
    @Column
    public String i_p;

    @Column
    public double gc_sc;
    @Column
    public double gc_m;
    @Column
    public String gc_p;

    //==============================================


    @Column
    public String name;
    @Column
    public String photo;
    @Column
    public String end;
    @Column
    public String start;
    @Column
    public double end_lon;
    @Column
    public String timestamp;
    @Column
    public double end_lat;
    @Column
    public int total_running_time;
    @Column
    public double total_fuel;
    @Column
    public int trip_time;
    @Column
    public String end_time;
    @Column
    public int car_id;

    @Column
    public int day;
    @Column
    public int month;
    @Column
    public int year;
    @Column
    public long tripStartTimeInMilis;

    public TripDetailMain() {

    }

    public TripDetailMain(String header) {
        this.headers = header;
    }

    @Override
    public Long save() {

        Calendar cal = DateHelper.getCalendarFromServer(start_time);
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(timestamp);
        tripStartTimeInMilis = cal.getTimeInMillis();
        if (cal != null) {
            day = cal.get(Calendar.DAY_OF_MONTH);
            month = cal.get(Calendar.MONTH);
            year = cal.get(Calendar.YEAR);
        }
        long id = super.save();
        if (route != null && route.size() > 0) {
            //Log.e("Routes Size : ", route.size() + " For trip : " + this.trip_id);
            for (Routes rt : route) {
                rt.tripId = this.trip_id;
                rt.save();
            }
        }
        return id;
    }


    /*public static ArrayList<TripDetails> readAll() {
        ArrayList<TripDetails> list = new Select().from(TripDetails.class).execute();
        if (list != null && list.size() > 0) {
            for (TripDetails model : list) {
                model.route = new Select().from(Routes.class).where("tripId=?", new String[]{model.trip_id + ""}).execute();
            }
        }
        return list;
    }*/


    //called for filtering
    public static ArrayList<TripDetailMain> readAllWithFilter(String carId, String date) {
        ArrayList<TripDetailMain> list = new Select().from(TripDetailMain.class)
                .where("car_id='" + carId + "' AND start_time LIKE '" + date + "%'")
                .execute();

        return list;
    }

    public static void readValuesForChartsAsync(String carId, final OnReadData callback) {

        final String query = "SELECT " +
                "year," +
                "month," +
                "day," +
                "SUM(distance) as distance, " +
                "SUM(trip_time) as time, " +
                "SUM(distance)/SUM(trip_time)*60*60 as speed, " +
                "SUM(avg_mileage*distance)/SUM(distance) as mileage," +
                "SUM(drive_score*distance)/SUM(distance) as drive_score" +
                "FROM TripDetailMain " +
                "WHERE car_id='" + carId + "'" +
                "GROUP BY year, month, day" +
                "ORDER BY year ASC, month ASC, day ASC";

        new AsyncTask<Void, Void, ArrayList<Graph>>() {

            @Override
            protected ArrayList<Graph> doInBackground(Void... params) {
                Cursor cursor = ActiveAndroid.getDatabase().rawQuery(query, null);
                ArrayList<Graph> graphList = new ArrayList<>();
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        Graph graph = new Graph();
                        graph.year = cursor.getInt(cursor.getColumnIndex("year"));
                        graph.month = cursor.getInt(cursor.getColumnIndex("month"));
                        graph.day = cursor.getInt(cursor.getColumnIndex("day"));

                        graph.avg_mileage = cursor.getDouble(cursor.getColumnIndex("mileage"));
                        graph.avg_cost = cursor.getDouble(cursor.getColumnIndex("mileage")) * cursor.getDouble(cursor.getColumnIndex("distance")); //need to add cost as per fuel type
                        graph.avg_distance = cursor.getDouble(cursor.getColumnIndex("distance"));
                        graph.avg_duration = cursor.getDouble(cursor.getColumnIndex("time"));
                        graph.avg_drive_score = cursor.getDouble(cursor.getColumnIndex("drive_score"));
                        graphList.add(graph);
                    } while (cursor.moveToNext());
                }
                cursor.close();
                return graphList;
            }

            @Override
            protected void onPostExecute(ArrayList<Graph> list) {
                super.onPostExecute(list);
                callback.onRead(null, list);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static ArrayList<TripDetailMain> readSpecificTrips(String carId, int limit, boolean showShortTrips) {

        //Reading specific trips of car with condition we
        From obj = new Select().from(TripDetailMain.class)
                .where("car_id='" + carId + "'")
                .and(showShortTrips ? "distance >= 0" : "distance >= 1")
                .orderBy("device_gen_id DESC");

        if (limit > 0) {
            obj.limit(limit);
        }

        ArrayList<TripDetailMain> list = obj.execute();

        /*ArrayList<TripDetailMain> list = new Select().from(TripDetailMain.class)
                .where("car_id='" + carId + "'")
                .orderBy("tripId DESC")

                .execute();*/

        for (TripDetailMain tripDetailMain : list) {
            tripDetailMain.route = new Select().from(Routes.class).where("tripId=?", new String[]{tripDetailMain.trip_id + ""}).orderBy("routeId ASC").execute();
        }
        return list;
    }

    public static ArrayList<TripDetailMain> readSpecificTripsForInitScroll(String carId, int start, int limit, boolean showShortTrips) {
        /*
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        today.add(Calendar.DAY_OF_YEAR, -7);
        Log.e("Main Nai Khelunga ;",today.getTimeInMillis()+"");
        */

        //Reading specific trips of car with condition we
        From obj = new Select().from(TripDetailMain.class)
                //.where("car_id='" + carId + "' AND tripStartTimeInMilis >= " + String.valueOf(today.getTimeInMillis()) + "")
                .where("car_id='" + carId + "'")
                .and(showShortTrips ? "distance >= 0" : "distance >= 1")
                .orderBy("device_gen_id DESC");

        if (limit > 0) {
            obj.limit(limit);
        }

        ArrayList<TripDetailMain> list = obj.execute();
        Log.e("Fetch Count :",list.size() + "");
        /*ArrayList<TripDetailMain> list = new Select().from(TripDetailMain.class)
                .where("car_id='" + carId + "'")
                .orderBy("tripId DESC")

                .execute();*/

        for (TripDetailMain tripDetailMain : list) {
            tripDetailMain.route = new Select().from(Routes.class).where("tripId=?", new String[]{tripDetailMain.trip_id + ""}).orderBy("routeId ASC").execute();
        }
        return list;
    }

    public static ArrayList<TripDetailMain> readSpecificTripsForLoadONScroll(String carId, int start, int limit, boolean showShortTrips) {

        //Reading specific trips of car with condition we
        From obj = new Select().from(TripDetailMain.class)
                    .where("car_id='" + carId + "'")
                .and(showShortTrips ? "distance >= 0" : "distance >= 1")
                .orderBy("device_gen_id DESC")
                .offset(start);

        if (limit > 0) {
            obj.limit(limit);
        }

        ArrayList<TripDetailMain> list = obj.execute();

        /*ArrayList<TripDetailMain> list = new Select().from(TripDetailMain.class)
                .where("car_id='" + carId + "'")
                .orderBy("tripId DESC")

                .execute();*/

        for (TripDetailMain tripDetailMain : list) {
            Log.e("SEARCHING...",tripDetailMain.trip_id+"");
            tripDetailMain.route = new Select().from(Routes.class).where("tripId=?", new String[]{tripDetailMain.trip_id + ""}).orderBy("routeId ASC").execute();
        }
        return list;
    }


    //TODO : remove from here and put it in Cars.java file as this nly queries the cars table
    public static HashMap<Integer, Integer> readAllWithRecentTrips() {

        HashMap<Integer, Integer> hashMap = new HashMap<>();
//        Cursor cursor = ActiveAndroid.getDatabase().rawQuery("Select MAX(tripId) as tripId, car_id from TripDetailMain group by car_id order by tripId DESC", null);
        Cursor cursor = ActiveAndroid.getDatabase().rawQuery("Select carId, local_latest_trip from Cars", null);
        if (cursor.moveToFirst()) {
            do {
                hashMap.put(cursor.getInt(cursor.getColumnIndex("carId")), cursor.getInt(cursor.getColumnIndex("local_latest_trip")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return hashMap;
    }

    public static TripDetailMain readSpecific(String tripId) {
        ArrayList<TripDetailMain> list = new Select().from(TripDetailMain.class)
                .where("tripId='" + tripId + "'")
                .execute();
        if (list != null && list.size() > 0) {

            list.get(0).route = new Select().from(Routes.class).where("tripId=?", new String[]{list.get(0).trip_id + ""}).orderBy("routeId ASC").execute();

            return list.get(0);
        }
        return null;
    }

    public static ArrayList<TripDetailMain> readAllTripsForSpecificDate(String carId, String day, String month, String year)
    {
        ArrayList<TripDetailMain> list = new Select().from(TripDetailMain.class).where("car_id='" + carId + "' AND day='" + day + "' AND month='" + month + "' AND year='" + year +"'")
                .execute();
        if (list != null && list.size() > 0) {
            return list;
        }
        return null;
    }

    public static void replaceJsonPoints(int tripId, String jsonr){
        ArrayList<Cars> list = new Select().from(TripDetailMain.class)
                .where("tripId='" + tripId  + "'")
                .execute();
        if (list.size() > 0) {
            //Log.d("DB-----Now", "only updating information");
            new Update(TripDetailMain.class).set("jsonRoutes=?", new String[]{jsonr + ""}).where("tripId='" + tripId  + "'").execute();
        }
    }
}