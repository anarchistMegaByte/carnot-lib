package com.carnot.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by root on 6/4/16.
 */
@Table(name = "Trip")
public class Trip extends Model {

    @Column(name = "tripId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int id;

    //It is used only to store the headers like today, yesterday, this week, older
    public String headers;

    public Trip() {
    }

    public Trip(String header) {
        this.headers = header;
    }


    @Column
    public int carId;

    @Column
    private double mileage;

    public double getMileage() {

        //Rounding value to 2 decimal places
        try {
            DecimalFormat fmt = new DecimalFormat("0.00");
            this.mileage = Double.parseDouble(fmt.format(mileage));
        } catch (Exception e) {
            this.mileage = mileage;
        }
        return this.mileage;
    }

    @Column
    public String dateTime;
    @Column
    public String location;
    @Column
    public String status;
    @Column
    public int idling;
    @Column
    public double elat;
    @Column
    public int clutch;
    @Column
    public int hardBreak;
    @Column
    public double slat;
    @Column
    public String date;
    @Column
    public String photo;
    @Column
    public String timestamp;
    @Column
    public double elon;
    @Column
    public int hardAcc;
    @Column
    public double driveScore;
    @Column
    public int speeding;
    @Column
    public String name;
    @Column
    public int maxMileage;
    @Column
    public double slon;

    @Column
    public String start;
    @Column
    public String end;


    public static ArrayList<Trip> readAll(String carId) {
        ArrayList<Trip> list = new Select().from(Trip.class)
                .where("carId='" + carId + "'")
                .execute();
        return list;
    }

    //called for filtering
    public static ArrayList<Trip> readAll(String carId, String date) {
        ArrayList<Trip> list = new Select().from(Trip.class)
                .where("carId='" + carId + "' AND date LIKE '" + date + "%'")
                .execute();
        return list;
    }

    /*public static Trip readSpecific(String tripId) {
        ArrayList<TripDetails> list = new Select().from(TripDetails.class)
                .where("tripId='" + tripId + "'")
                .execute();
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }*/
}
