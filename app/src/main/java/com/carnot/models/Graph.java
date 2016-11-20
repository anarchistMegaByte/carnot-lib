package com.carnot.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.OnReadAsync;
import com.activeandroid.OnReadData;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.carnot.global.Utility;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by javid on 2/5/16.
 */
@Table(name = "Graph")
public class Graph extends Model {

    /*@Column
    public int iFuelPrice;
    @Column
    public int carId;
    @Column
    public boolean status;
    @Column
    public String message;
    @Column
    public double d; //Distance covered in this trip
    @Column(name = "graphId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int id; //The id of the trip for which data is to be seen
    @Column
    public double m; //Average mileage in this trip
    @Column
    public String sts; //Trip start timestamp
    @Column
    public int t; //Trip time duration*/

    public static ArrayList<Graph> readAll(String carId) {
        ArrayList<Graph> list = new Select().from(Graph.class)
                .where("car_id='" + carId + "'")
                .execute();

        return list;
    }

    public static void readAllAsync(String carId, final OnReadData callback) {
        ArrayList<Graph> list = new Select().from(Graph.class)
                .where("car_id='" + carId + "'")
                .execute();

        new Select().from(Graph.class).where("car_id='" + carId + "'")
                .executeAsync(new OnReadAsync() {
                    @Override
                    public <T extends Model> void onRead(ArrayList<T> list) {
                        callback.onRead(null, list);
                    }
                });
    }


    //================================

    @Column
    public double avg_mileage;
    @Column
    public int car_id;
    @Column
    public double avg_cost;
    @Column
    public double avg_distance;
    @Column
    public double avg_duration;
    @Column
    public double avg_drive_score;
    @Column
    public long timestamp;
    @Column
    public int day;
    @Column
    public int month;
    @Column
    public int year;

    public static Graph readSpecific(String carId, String day, String month, String year)
    {
        ArrayList<Graph> list = new Select().from(Graph.class)
                .where("car_id='" + carId + "' AND day='" + day + "' AND month='" + month + "' AND year='" + year +"'")
                .execute();
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public static Graph readLast(String carId) {
        ArrayList<Graph> list = new Select().from(Graph.class)
                .where("car_id='" + carId + "'")
                .orderBy("timestamp DESC")
                .limit(1)
                .execute();

        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public Long save() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);

//        roundValues();

        return super.save();
    }

    public void roundValues() {
//        avg_drive_score = Utility.round(avg_drive_score);
        avg_mileage = Utility.round(avg_mileage, 1);
        avg_cost = Utility.roundToInt(avg_cost);
        avg_distance = Utility.round(avg_distance, 1);
        //avg_duration = Utility.roundToInt(avg_duration / 60 / 60);
        avg_duration = (float) avg_duration / 60 / 60;
        //Log.e("AVG_MILEAGE" , String.valueOf(avg_duration));
    }
}
