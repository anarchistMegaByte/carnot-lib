package com.carnot.Services;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.toolbox.Volley;
import com.carnot.App;
import com.carnot.R;
import com.carnot.fragment.FragmentCars;
import com.carnot.global.ConstantCode;
import com.carnot.global.PrefManager;
import com.carnot.global.Utility;
import com.carnot.libclasses.DateHelper;
import com.carnot.models.Cars;
import com.carnot.models.Graph;
import com.carnot.models.TripDetailMain;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebServiceModel;
import com.carnot.network.WebUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by pankaj on 6/24/16.
 */
public class LoadTripDetailService extends IntentService {


    private static final String TAG = "LoadTripDetailService";

    public LoadTripDetailService() {
        super("LoadTripDetailService");
    }

    public LoadTripDetailService(String name) {
        super(name);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent() called with: " + "intent = [" + intent + "]");
//        /*
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        super.onStartCommand(intent, flags, startId);*/

        if (intent == null) {
//            return START_NOT_STICKY;
            return;
        }

        final String carId = intent.getStringExtra(ConstantCode.INTENT_CAR_ID);
        final String tripId = intent.getStringExtra(ConstantCode.INTENT_TRIP_ID);
        final String serverRecentTripId = intent.getStringExtra(ConstantCode.INTENT_SERVER_RECENT_TRIP_ID);

        PrefManager.getInstance().edit().putString(carId, getString(R.string.msg_syncing_is_in_progress)).commit();

        Log.d(TAG, "onHandleIntent() called with: " + "intent = [" + intent + "] CAR ID: " + carId + ", TRIP ID: " + tripId + ", RECENT TRIP ID =" + serverRecentTripId);

        final WebServiceModel webServiceModel = WebServiceConfig.getMethods().get(WebServiceConfig.WebService.GET_RECENT_TRIPS);
        final String url = String.format(webServiceModel.url, new String[]{tripId, carId});
        Volley.newRequestQueue(App.getContext()).cancelAll(url);

        WebUtils.call(WebServiceConfig.WebService.GET_RECENT_TRIPS, new String[]{tripId, carId}, null, new NetworkCallbacks() {
                    @Override
                    public void successWithString(Object values, WebServiceConfig.WebService webService) {
                        super.successWithString(values, webService);

                        String carsid = carId;
                        Log.d(TAG, "successWithString() called with: " + "values = [" + values + "], webService = [" + webService + "]");
                        if (Utility.getLoggedInUser() == null)
                            return;

                        JSONObject json = (JSONObject) values;
                        ArrayList<TripDetailMain> tripDetailsArrayList = Utility.parseArrayFromString(json.optString(ConstantCode.data), TripDetailMain[].class);

//                        double db_drive_score = 0;
//                        double db_mileage = 0;
//                        double db_distance = 0;
//                        double db_time = 0;
//                        double db_speed = 0;
//
//                        double db_last_trip_id = 0;
//                        double db_last_drive_score = 0;
//                        double db_last_mileage = 0;
//                        double db_last_distance = 0;
//                        double db_last_time = 0;
//                        double db_last_speed = 0;

                        /**
                         * All the below formula shared no google sheet - https://docs.google.com/spreadsheets/d/1wTVfoEkRa0aKsfJU1A-PtCpLlmaKY68ObpRWGBbu15s/edit#gid=2136442752
                         */

                        //For Getting current calculated values from database.
                        boolean isSyncFullCompleted = false;
                        if (tripDetailsArrayList.size() > 0) {

                            int carId = tripDetailsArrayList.get(0).car_id;
                            Cars car = Cars.readSpecific(carId + "");
//                            //if database have the calculated values
//                            if (car != null && car.id > 0) {
//                                db_drive_score = car.avg_drive_score;
//                                db_mileage = car.avg_mileage;
//                                db_distance = car.avg_distance;
//                                db_time = car.avg_time;
//                                db_speed = car.avg_speed;
//
//                                db_last_trip_id = car.last_trip_id;
//                                db_last_drive_score = car.avg_last_drive_score;
//                                db_last_mileage = car.avg_last_mileage;
//                                db_last_distance = car.avg_last_distance;
//                                db_last_time = car.avg_last_time;
//                                db_last_speed = car.avg_last_speed;
//                            }

                            Calendar tripCalendar = Calendar.getInstance();
                            Calendar calendarToday = Calendar.getInstance();

                            for (TripDetailMain tripDetails : tripDetailsArrayList) {

                                if (tripDetails.trip_id == Integer.parseInt(serverRecentTripId))
                                    isSyncFullCompleted = true;

                                /*//discarding any trip whose time is more then one year
                                tripCalendar = DateHelper.getCalendarFromServer(tripDetails.timestamp);
                                if (!(yearsBetween(calendarToday, tripCalendar) == 0 || yearsBetween(calendarToday, tripCalendar) == 1)) {
                                    continue;
                                }*/

                                //  Formulla to calculate
                                // (NewDriveScore*NewDistance      +       OldDriveScore*OldDistance)/(NewDistance+OldDistance)
                                // (NewDistance      +       OldDistance)/(NewDistance+OldDistance)


                                /*if (carId == 88) {
                                    Log.d(TAG, "SPeed : db_speed + tripDetails.avg_speed = " + db_speed + "+" + tripDetails.avg_speed);
                                }

                                if (tripDetails.trip_id == Integer.parseInt(serverRecentTripId)) {
                                    isSyncFullCompleted = true;
                                    *//*if (db_last_trip_id != tripDetails.trip_id && db_last_distance > 0) {
                                        db_drive_score = ((db_last_drive_score * db_last_distance) + (db_drive_score * db_distance)) / (db_last_distance + db_distance);
                                        db_mileage = ((db_last_mileage * db_last_distance) + (db_mileage * db_distance)) / (db_last_distance + db_distance);
                                        db_distance = db_last_distance + db_distance;
                                        db_time = db_last_time + db_time;
                                        db_speed = db_last_speed + db_speed;
                                    }

                                    db_last_trip_id = tripDetails.trip_id;
                                    db_last_drive_score = tripDetails.drive_score;
                                    db_last_mileage = tripDetails.avg_mileage;
                                    db_last_distance = tripDetails.distance;
                                    db_last_time = tripDetails.trip_time;
                                    db_last_speed = tripDetails.avg_speed;*//*
                                } else {
                                    *//*db_drive_score = ((tripDetails.drive_score * tripDetails.distance) + (db_drive_score * db_distance)) / (tripDetails.distance + db_distance);
                                    db_mileage = ((tripDetails.avg_mileage * tripDetails.distance) + (db_mileage * db_distance)) / (tripDetails.distance + db_distance);
                                    db_distance = tripDetails.distance + db_distance;
                                    db_time = tripDetails.trip_time + db_time;
                                    db_speed = tripDetails.avg_speed + db_speed;

                                    if (db_last_trip_id == tripDetails.trip_id && db_distance > 0) {
                                        db_last_trip_id = 0;
                                        db_last_drive_score = 0;
                                        db_last_mileage = 0;
                                        db_last_distance = 0;
                                        db_last_time = 0;
                                        db_last_speed = 0;
                                    }*//*
                                }
                                db_drive_score = ((tripDetails.drive_score * tripDetails.distance) + (db_drive_score * db_distance)) / (tripDetails.distance + db_distance);
                                db_mileage = ((tripDetails.avg_mileage * tripDetails.distance) + (db_mileage * db_distance)) / (tripDetails.distance + db_distance);
                                db_distance = tripDetails.distance + db_distance;
                                db_time = tripDetails.trip_time + db_time;
                                db_speed = tripDetails.avg_speed + db_speed;*/


                                tripDetails.save();
                            }

                            /*car.drive_score_to_show = Utility.round(((db_drive_score * db_distance) + (db_last_drive_score * db_last_distance)) / (db_distance + db_last_distance));
                            car.mileage_to_show = Utility.round(((db_mileage * db_distance) + (db_last_mileage * db_last_distance)) / (db_distance + db_last_distance));
                            car.distance_to_show = Utility.round(db_distance + db_last_distance);
                            car.time_to_show = Utility.round(db_time + db_last_time);
                            car.speed_to_show = Utility.round(car.distance_to_show / (car.time_to_show / 60 / 60));

                            db_drive_score = Utility.round(db_drive_score);
                            db_mileage = Utility.round(db_mileage);
                            db_distance = Utility.round(db_distance);
                            db_time = Utility.round(db_time);
                            db_speed = Utility.round(db_distance / db_time);

                            db_last_drive_score = Utility.round(db_last_drive_score);
                            db_last_mileage = Utility.round(db_last_mileage);
                            db_last_distance = Utility.round(db_last_distance);
                            db_last_time = Utility.round(db_last_time);
                            db_last_speed = Utility.round(db_last_distance / (db_last_time * 60 * 60));*/


                            //=========================================
                            //========GRAPH CALCULATION START==========
                            //=========================================
                            {
                                //Intialize Chart values for daywise and month wise

                                Calendar calendarLast = null;
                                Graph graphLast = Graph.readLast(carId + "");
                                if (graphLast != null) {
                                    calendarLast = Calendar.getInstance();
                                    calendarLast.setTimeInMillis(graphLast.timestamp);
                                }

                                int i = 0;
                                for (TripDetailMain tripDetails : tripDetailsArrayList) {

                                    //discarding any trip whose time is more then one year
                                    tripCalendar = DateHelper.getCalendarFromServer(tripDetails.timestamp);
                                    if (!(yearsBetween(calendarToday, tripCalendar) == 0 || yearsBetween(calendarToday, tripCalendar) == 1)) {
                                        continue;
                                    }

                                    if (graphLast == null) {
                                        calendarLast = DateHelper.getCalendarFromServer(tripDetails.timestamp);

                                        graphLast = new Graph();
                                        graphLast.avg_drive_score = Utility.round(((graphLast.avg_drive_score * graphLast.avg_distance) + (tripDetails.drive_score * tripDetails.distance)) / (graphLast.avg_distance + tripDetails.distance));
                                        graphLast.avg_mileage = Utility.round(((graphLast.avg_mileage * graphLast.avg_distance) + (tripDetails.avg_mileage * tripDetails.distance)) / (graphLast.avg_distance + tripDetails.distance));
                                        graphLast.avg_cost = Utility.round(graphLast.avg_mileage * ((graphLast.avg_distance + tripDetails.distance) * getPrice(car.fuel)));
                                        graphLast.avg_distance = Utility.round(graphLast.avg_distance + tripDetails.distance);
                                        graphLast.avg_duration = Utility.round(graphLast.avg_duration + tripDetails.trip_time);
                                        graphLast.car_id = carId;
                                        graphLast.timestamp = calendarLast.getTimeInMillis();
                                        graphLast.save();

                                        graphLast.save();

                                    } else {
                                        long daysBetween = daysBetween(calendarLast, DateHelper.getCalendarFromServer(tripDetails.timestamp));
                                        if (daysBetween == 0) {
                                            Calendar cal = DateHelper.getCalendarFromServer(tripDetails.timestamp);
                                            graphLast.avg_drive_score = Utility.round(((graphLast.avg_drive_score * graphLast.avg_distance) + (tripDetails.drive_score * tripDetails.distance)) / (graphLast.avg_distance + tripDetails.distance));
                                            graphLast.avg_mileage = Utility.round(((graphLast.avg_mileage * graphLast.avg_distance) + (tripDetails.avg_mileage * tripDetails.distance)) / (graphLast.avg_distance + tripDetails.distance));
                                            graphLast.avg_cost = Utility.round(graphLast.avg_mileage * ((graphLast.avg_distance + tripDetails.distance) * getPrice(car.fuel)));
                                            graphLast.avg_distance = Utility.round(graphLast.avg_distance + tripDetails.distance);
                                            graphLast.avg_duration = Utility.round(graphLast.avg_duration + tripDetails.trip_time);
                                            graphLast.car_id = carId;
                                            graphLast.timestamp = cal.getTimeInMillis();
                                            graphLast.save();
                                        } else {

                                            if (ConstantCode.ENTER_INTERMEDIATE_DAYS_IN_CHARTS) {
                                                if (daysBetween > 0) {
                                                    for (int day = 0; day < daysBetween - 1; day++) {
                                                        graphLast = new Graph();
                                                        calendarLast.add(Calendar.DATE, 1);
                                                        graphLast.avg_drive_score = 0;
                                                        graphLast.avg_mileage = 0;
                                                        graphLast.avg_cost = 0;
                                                        graphLast.avg_distance = 0;
                                                        graphLast.avg_duration = 0;
                                                        graphLast.car_id = carId;
                                                        graphLast.timestamp = calendarLast.getTimeInMillis();
                                                        graphLast.save();
                                                    }
                                                }
                                            }
                                            graphLast = new Graph();
                                            calendarLast = DateHelper.getCalendarFromServer(tripDetails.timestamp);
                                            graphLast.avg_drive_score = Utility.round(((graphLast.avg_drive_score * graphLast.avg_distance) + (tripDetails.drive_score * tripDetails.distance)) / (graphLast.avg_distance + tripDetails.distance));
                                            graphLast.avg_mileage = Utility.round(((graphLast.avg_mileage * graphLast.avg_distance) + (tripDetails.avg_mileage * tripDetails.distance)) / (graphLast.avg_distance + tripDetails.distance));
                                            graphLast.avg_cost = Utility.round(graphLast.avg_mileage * ((graphLast.avg_distance + tripDetails.distance) * getPrice(car.fuel)));
                                            graphLast.avg_distance = Utility.round(graphLast.avg_distance + tripDetails.distance);
                                            graphLast.avg_duration = Utility.round(graphLast.avg_duration + tripDetails.trip_time);
                                            graphLast.car_id = carId;
                                            graphLast.timestamp = calendarLast.getTimeInMillis();
                                            graphLast.save();

                                        }
                                    }

                                    i++;
                                }
                            }
                            //========================================
                            //========GRAPH CALCULATION END===========
                            //========================================

//                            //Constant values are shared on google formula sheet
//                            car.emissions_mycar_petrol = Utility.round(db_distance / db_mileage * 2.392);
//                            car.emissions_mycar_diesel = Utility.round(db_distance / db_mileage * 2.64);
//                            car.emissions_hybrid_car = Utility.round(70 * db_distance / 1000);
//                            car.emissions_public_transport = Utility.round(45 * db_distance / 1000);
//                            car.nTrees = (int) (car.emissions_mycar_petrol / 21.77);
//
//                            car.carbon_footprint = Utility.round(db_mileage * db_distance * ConstantCode.MULTIPLIER);

                            /*car.avg_drive_score = db_drive_score;
                            car.avg_mileage = db_mileage;
                            car.avg_distance = db_distance;
                            car.avg_time = db_time;
                            car.avg_speed = db_speed;

                            car.avg_last_drive_score = db_last_drive_score;
                            car.avg_last_mileage = db_last_mileage;
                            car.avg_last_distance = db_last_distance;
                            car.avg_last_time = db_last_time;
                            car.avg_last_speed = db_last_speed;*/

//                            car.saveCalculations();
                        }

                        try {
                            if (isSyncFullCompleted) {
                                Intent broadcast = new Intent(ConstantCode.BROADCAST_TRIP_SYNCED);
                                broadcast.putExtra(ConstantCode.INTENT_CAR_ID, Integer.parseInt(carId));
                                broadcast.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_SYNC_COMPLETE);
                                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(broadcast);
                                PrefManager.getInstance().edit().remove(String.valueOf(carId)).commit();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        FragmentCars.setFlag(serverRecentTripId, tripId);

                        stopSelf();
                    }


                    @Override
                    public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                        super.failedWithMessage(values, webService);
                        Log.d(TAG, "failedWithMessage() called with: " + "values = [" + values + "], webService = [" + webService + "]");

                        if (values != null && "No new trips".equalsIgnoreCase(values.toString())) {
                            try {
                                Intent broadcast = new Intent(ConstantCode.BROADCAST_TRIP_SYNCED);
                                broadcast.putExtra(ConstantCode.INTENT_CAR_ID, Integer.parseInt(carId));
                                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(broadcast);
                                PrefManager.getInstance().edit().remove(String.valueOf(carId)).commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        FragmentCars.setFlag(serverRecentTripId, tripId);
                        stopSelf();
                    }

                    @Override
                    public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                        super.failedForNetwork(values, webService);
                        Log.d(TAG, "failedForNetwork() called with: " + "values = [" + values + "], webService = [" + webService + "]");
                        FragmentCars.setFlag(serverRecentTripId, tripId);
                        stopSelf();

                    }

                    @Override
                    public void loadOffline() {
                        super.loadOffline();
                        Log.d(TAG, "loadOffline() called with: " + "");
                        FragmentCars.setFlag(serverRecentTripId, tripId);
                        stopSelf();
                    }
                }

        );

//        return START_NOT_STICKY;
        return;
    }

    private double getPrice(String fuelType) {
        if (ConstantCode.FUEL_TYPE_OTHER.equalsIgnoreCase(fuelType)) {
            return ConstantCode.DEFAULT_PETROL_PRICE;
        } else if (ConstantCode.FUEL_TYPE_DIESEL.equalsIgnoreCase(fuelType)) {
            return ConstantCode.DEFAULT_DIESEL_PRICE;
        } else if (ConstantCode.FUEL_TYPE_PETROL.equalsIgnoreCase(fuelType)) {
            return ConstantCode.DEFAULT_PETROL_PRICE;
        }
        return ConstantCode.DEFAULT_PETROL_PRICE;
    }

    public long daysBetween(Calendar startDate, Calendar endDate) {
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.MILLISECOND, 0);

        long end = endDate.getTimeInMillis();
        long start = startDate.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
    }

    public long yearsBetween(Calendar maxDate, Calendar minDate) {
        long max = maxDate.get(Calendar.YEAR);
        long min = minDate.get(Calendar.YEAR);
        return max - min;
    }

}
