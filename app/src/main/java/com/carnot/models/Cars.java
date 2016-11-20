package com.carnot.models;

import android.database.Cursor;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.carnot.global.Utility;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

@Table(name = "Cars")
public class Cars extends Model {

    @Column(name = "carId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int id;
    @Column
    public String lat;
    public int latest_trip;
    @Column
    public int local_latest_trip;
    @Column
    public String lon;
    @Column
    public String lut; //last updated time
    @Column
    public String fuel;
    public int flag;
    @Column
    public String speed;
    @Column
    public int lock;// whether device is locked or not
    @Column
    public double avgSpeed;

    public double getSpeed() {

        //Rounding value to 2 decimal places
        try {
            DecimalFormat fmt = new DecimalFormat("0.00");
            this.avgSpeed = Double.parseDouble(fmt.format(avgSpeed));
        } catch (Exception e) {

        }
        return this.avgSpeed;
    }

    @Column
    public boolean isOnTrip;
    @Column
    public String name;

    @Column
    public boolean showShortTrips;

    @Column
    public int distance;
    @Column
    public int time;
    @Column
    public int nTrips;
    @Column
    public double mileage;

    //not necessary fields =============================
    @Column
    public int carfp;
    @Column
    public int cyclefp;
    @Column
    public int driveScore;
    @Column
    public int carbonfp;

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


    /*@Column
    public List<Trip> trips = new ArrayList<Trip>();*/

    @Column
    public int userId;

    //Car Bluetooth related fields
    @Column
    public String stmid;
    @Column
    public String key;
    @Column
    public String device_pk;
    @Column
    public String nrfid;


    @Column
    public int hardBreak;
    @Column
    public int hardAcc;
    @Column
    public int idling;
    @Column
    public int speeding;
    @Column
    public int clutch;
    @Column
    public String ln;
    @Column
    public String brand;
    @Column
    public String model;

    //TODO : add extra field for server trip id
    @Column
    public int serverTripID;
    @Column
    public int serverFlag;

    //TODO : fields added for fxing my car says card to be displayed for the first time when ever new car is added.
    @Column
    public boolean isFirstTimeEntry;
    @Column
    public boolean isTimerStarted;

    @Override
    public Long save() {
        userId = Utility.getLoggedInUser().id;
        showShortTrips = true;
        long id = super.save();
        Log.d("DB", "save() called with: " + "" + id);
        return id;
    }

    public static ArrayList<Cars> readAll() {
        if(Utility.getLoggedInUser() != null)
        {
            ArrayList<Cars> list = new Select().from(Cars.class)
                    .where("userId='" + Utility.getLoggedInUser().id + "'")
                    .execute();
            Log.d("DB", "readAll() called with: " + list.size());
            return list;
        }
        return null;
    }

    public static Cars readSpecific(String carId) {
        if(Utility.getLoggedInUser() != null)
        {
            ArrayList<Cars> list = new Select().from(Cars.class)
                    .where("carId='" + carId + "' AND userId='" + Utility.getLoggedInUser().id + "'")
                    .execute();
            if (list != null && list.size() > 0) {
                return list.get(0);
            }
            return null;
        }
        return null;
    }


    public static void updateFlagParameters(String carId, boolean isFirstTrip, boolean isTimerSet)
    {

        ArrayList<Cars> list = new Select().from(Cars.class)
                .where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + carId + "'")
                .execute();

        if(list.size() > 0)
        {
            Log.e("i m here--------","hey theer you come");
            //Log.e("DB", "only updating information");
            Log.e("uPDATINGfLAGpARAMETERS", isFirstTrip + "");
            new Update(Cars.class).set("isFirstTimeEntry=?, isTimerStarted=?", new String[]{isFirstTrip ? "1" : "0", isTimerSet ? "1" : "0"}).where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + carId + "'").execute();
        }
        else
        {

        }
    }

    /**
     * Updating isOnTrip, lat, lon, speed, lut, name fields
     */
    public void updateSelectedValues() {
        ArrayList<Cars> list = new Select().from(Cars.class)
                .where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + id + "'")
                .execute();
        if (list.size() > 0) {
            //Log.d("DB", "only updating information");
            new Update(Cars.class).set("isOnTrip=?, lat=?, lon=?, speed=?, lut=?, name=?", new String[]{isOnTrip ? "1" : "0", lat, lon, speed, lut, name}).where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + id + "'").execute();
        } else {
            Log.d("DB", "saving new entry");
            userId = Utility.getLoggedInUser().id;
            long id = super.save();
            super.save();
        }
    }

    //TODO : function that updates server trip id into Cars table from garage api only
    /**
     * Updating isOnTrip, lat, lon, speed, lut, name , serverTripID fields
     */
    public void updateSelectedValuesFromGarageApi() {
        //Log.e("FromGarageApi",id+" ");
        ArrayList<Cars> list = new Select().from(Cars.class)
                .where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + id + "'")
                .execute();
        if (list.size() > 0) {
            //Log.e("DB", "only updating information");
            new Update(Cars.class).set("lock=?,isOnTrip=?, lat=?, lon=?, speed=?, lut=?, serverTripID=?, serverFlag=?", new String[]{lock+"",isOnTrip ? "1" : "0", lat, lon, speed, lut, latest_trip+"", flag+""}).where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + id + "'").execute();
        } else {
            Log.e("DB", "saving new entry");
            userId = Utility.getLoggedInUser().id;
            showShortTrips = true;
            long id1 = super.save();
            long id2 = super.save();
            Log.e("FromGarageApi", id + " " + id1 + " " + id2);
            Utility.insertIntoGraphDataTable(id);
        }
    }


    /**
     * Updating BLE Related fields and fuel that is called at time of login response
     */
    public void updateDeviceKeysOnly() {
        ArrayList<Cars> list = new Select().from(Cars.class)
                .where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + id + "'")
                .execute();
        if (list.size() > 0) {
            //Log.d("DB", "only updating information");
            new Update(Cars.class).set("name=?, fuel=?, stmid=?, key=?, device_pk=?, nrfid=?", new String[]{name,fuel, stmid, key, device_pk, nrfid}).where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + id + "'").execute();
        } else
        {
            Log.e("DB", "---saving new entry---");
            Log.e("FromSecurityCode", " : AddASecurityCode");
            userId = Utility.getLoggedInUser().id;
            showShortTrips = true;
            isFirstTimeEntry = true;
            isTimerStarted = false;

            long id1 = super.save();
            long id2 = super.save();
            Log.e("FromSecurityCode", id + " " + id1 + " " + id2);
            Utility.insertIntoGraphDataTable(id);
        }
    }

    public void updateDeviceKeysOnlyAtSignIn() {
        ArrayList<Cars> list = new Select().from(Cars.class)
                .where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + id + "'")
                .execute();
        if (list.size() > 0) {
            //Log.d("DB", "only updating information");
            new Update(Cars.class).set("name=?,fuel=?, stmid=?, key=?, device_pk=?, nrfid=?", new String[]{name, fuel, stmid, key, device_pk, nrfid}).where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + id + "'").execute();
        } else {
            //Log.e("DB", "---saving new entry---");
            Log.e("FromSignIN","SIGN IN");
            userId = Utility.getLoggedInUser().id;
            showShortTrips = true;
            isFirstTimeEntry = false;
            isTimerStarted = true;
            Utility.insertIntoGraphDataTable(id);
            long id = super.save();
            super.save();
        }
    }

    /**
     * Updating local latest trip id which is used for Syncing and used to check whether local latest trip is less then Server Latest Trip
     *
     * @param local_latest_trip
     */
    public void updateLocalLatestTripValues(int local_latest_trip) {
        ArrayList<Cars> list = new Select().from(Cars.class)
                .where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + id + "'")
                .execute();
        if (list.size() > 0) {
            //Log.d("DB", "only updating information");
            new Update(Cars.class).set("local_latest_trip=?", new String[]{local_latest_trip + ""}).where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + id + "'").execute();
        } else {
            Log.d("DB", "saving new entry");
            Log.e("FromLocalLatestTrips","LOCALLATEST TRIPS");
            showShortTrips = true;
            userId = Utility.getLoggedInUser().id;
            Utility.insertIntoGraphDataTable(id);
            long id = super.save();
            super.save();
        }
    }

    /**
     * Saving profile only from ActivityCarProfile
     */
    public void saveProfileOnly() {
        new Update(Cars.class).set("name=?, ln=?, brand=?, model=?, fuel=?", new String[]{name + "", ln + "", brand + "", model + "", fuel + ""}).where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + id + "'").execute();
    }

    /**
     * Updating whether to show short trips or not called from ActivityCarProfile
     *
     * @param showShortTrips
     */
    public void updateShowShortTrip(boolean showShortTrips) {
        this.showShortTrips = showShortTrips;
        new Update(Cars.class).set("showShortTrips=?", new String[]{showShortTrips ? "1" : "0"}).where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + id + "'").execute();
    }

    //TODO : function to get HashMap of carID and its corresponding serverTripID

    //TODO : function to get HashMap of carID and its corresponding localLatestTripID
    public static HashMap<Integer, ArrayList<Integer>> readAllWithRecentTripsFromLocalDB() {

        HashMap<Integer, ArrayList<Integer>> hashMap = new HashMap<>();
        ArrayList<Integer> values;
//        Cursor cursor = ActiveAndroid.getDatabase().rawQuery("Select MAX(tripId) as tripId, car_id from TripDetailMain group by car_id order by tripId DESC", null);
        Cursor cursor = ActiveAndroid.getDatabase().rawQuery("Select carId, serverTripID, serverFlag, local_latest_trip from Cars", null);
        if (cursor.moveToFirst()) {
            do {
                values = new ArrayList<>();
                //hashMap.put(cursor.getInt(cursor.getColumnIndex("carId")), cursor.getInt(cursor.getColumnIndex("local_latest_trip")));
                values.add(cursor.getInt(cursor.getColumnIndex("local_latest_trip")));
                values.add(cursor.getInt(cursor.getColumnIndex("serverTripID")));
                values.add(cursor.getInt(cursor.getColumnIndex("serverFlag")));
                hashMap.put(cursor.getInt(cursor.getColumnIndex("carId")), values);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return hashMap;
    }

    public static void updateIsTimerStarted(String carId, boolean isTimerSet)
    {

        ArrayList<Cars> list = new Select().from(Cars.class)
                .where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + carId + "'")
                .execute();

        if(list.size() > 0)
        {
            Log.e("i m here--------","hey theer you come");
            //Log.e("DB", "only updating information");
            Log.e("uPDATINGfLAGpARAMETERS", isTimerSet + "");
            new Update(Cars.class).set("isTimerStarted=?", new String[]{isTimerSet ? "1" : "0"}).where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + carId + "'").execute();
        }
        else
        {

        }
    }
    public static void updateIsFistTimeEntry(String carId, boolean isFirstTimeEntry)
    {

        ArrayList<Cars> list = new Select().from(Cars.class)
                .where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + carId + "'")
                .execute();

        if(list.size() > 0)
        {
            Log.e("i m here--------","hey theer you come");
            //Log.e("DB", "only updating information");
            Log.e("uPDATINGfLAGpARAMETERS", isFirstTimeEntry + "");
            new Update(Cars.class).set("isFirstTimeEntry=?", new String[]{isFirstTimeEntry ? "1" : "0"}).where("userId='" + Utility.getLoggedInUser().id + "' AND carId='" + carId + "'").execute();
        }
        else
        {

        }
    }

    public static LatLng current_location(String carId){
        ArrayList<Cars> list = new Select().from(Cars.class).where("carId='" + carId + "'").execute();
        if(list.size() == 1){
            LatLng car_location = new LatLng(Double.parseDouble(list.get(0).lat), Double.parseDouble(list.get(0).lon));
            return car_location;

        }
        else{
            return new LatLng(0,0);
        }

    }
}