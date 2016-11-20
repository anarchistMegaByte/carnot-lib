package com.carnot.controller;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.carnot.App;
import com.carnot.R;
import com.carnot.activity.ActivityAllTrips;
import com.carnot.fragment.FragmentCars;
import com.carnot.global.ConstantCode;
import com.carnot.global.PrefManager;
import com.carnot.global.Utility;
import com.carnot.libclasses.DateHelper;
import com.carnot.models.Cars;
import com.carnot.models.Graph;
import com.carnot.models.GraphData;
import com.carnot.models.Routes;
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
 * Created by pankaj on 7/26/16.
 * SyncController is used to perform syncing of trip when there are some trips on server.
 */
public class SyncController {

    private static final String TAG = "SyncController";

    Context context;

    public SyncController(Context context) {
        this.context = context;
    }

    /**
     * Method used to sync trips
     *
     * @param carId
     * @param tripId
     * @param serverRecentTripId
     */
    public void sync(final String carId, final String tripId, final String serverRecentTripId) {

        try{
            //used for onResume
            //Log.e(TAG,"Trip Call Started");
            if(PrefManager.getInstance().contains(ConstantCode.TRIP_SYNC_FLAG))
            {
                //Log.e("TRIP_SYNC_FLAG 1 : ", "MADE TRUE");
                PrefManager.putBoolean(ConstantCode.TRIP_SYNC_FLAG, true);
            }
            else
            {
                //Log.e("TRIP_SYNC_FLAG 2 : ", "MADE TRUE");
                PrefManager.getInstance().edit().putBoolean(ConstantCode.TRIP_SYNC_FLAG, true).commit();
            }
            PrefManager.getInstance().edit().putString(carId, context.getString(R.string.msg_syncing_is_in_progress)).commit();
            //for getting trip sync updates.
            Intent broadcast = new Intent(ConstantCode.BROADCAST_SHOW_TRIP_SYNC_BANNER);
            broadcast.putExtra(ConstantCode.INTENT_CAR_ID, Integer.parseInt(carId + ""));
            broadcast.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_SYNC_STARTED);
            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        final WebServiceModel webServiceModel = WebServiceConfig.getMethods().get(WebServiceConfig.WebService.GET_RECENT_TRIPS);
        final String url = String.format(webServiceModel.url, new String[]{tripId, carId});

        //Cancelling the request queue for the same url it is it in progress previous it is only precaution measure so that duplicate trips not sync
        WebUtils.getRequestQueue().cancelAll(url);
        Log.e("TRIPS in A CALL : ","S " + tripId + " " + Calendar.getInstance().getTimeInMillis());
        WebUtils.call(WebServiceConfig.WebService.GET_RECENT_TRIPS, new String[]{tripId, carId}, null, new NetworkCallbacks() {
                    @Override
                    public void successWithString(Object values, WebServiceConfig.WebService webService) {
                        super.successWithString(values, webService);
                        Log.e("TRIPS in A CALL : ","E "+tripId + " " + Calendar.getInstance().getTimeInMillis());
                        String carsid = carId;
                        Log.d(TAG, "successWithString() called with: " + "values = [" + values + "], webService = [" + webService + "]");
                        if (Utility.getLoggedInUser() == null)
                            return;

                        JSONObject json = (JSONObject) values;
                        //Calling this method to add intermediate values and
                        performCalculation(carId, tripId, serverRecentTripId, json);

                        //PrefManager.getInstance().edit().putBoolean(ConstantCode.TRIP_SYNC_FLAG, false).commit();

                    }


                    @Override
                    public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                        super.failedWithMessage(values, webService);

                        Log.e(TAG, "failedWithMessage() called with: " + "values = [" + values + "], webService = [" + webService + "]"+"[car id]="+carId);
                        if (values != null && "No new trips".equalsIgnoreCase(values.toString())) {
                            try {
                                //to remove banner for onResume
                                if(PrefManager.getInstance().contains(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA))
                                {
                                    PrefManager.getInstance().edit().putLong(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis()).commit();
                                }
                                else
                                {
                                    PrefManager.putLong(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis());
                                }
                                //to get instant updates if trip sync completed.
                                Intent broadcast = new Intent(ConstantCode.BROADCAST_TRIP_SYNCED);
                                broadcast.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_SYNC_COMPLETE);
                                broadcast.putExtra(ConstantCode.INTENT_CAR_ID, Integer.parseInt(carId));
                                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            //to remove banner for onResume
                            if(PrefManager.getInstance().contains(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA))
                            {
                                PrefManager.getInstance().edit().putLong(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis()).commit();
                            }
                            else
                            {
                                PrefManager.putLong(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis());
                            }
                            //to get instant updates if trip sync completed.
                            Log.d(TAG, "failedWithMessage() called with: " + "values = [" + values + "], webService = [" + webService + "]");
                            Intent broadcast = new Intent(ConstantCode.BROADCAST_TRIP_SYNCED);
                            broadcast.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_SYNC_COMPLETE);
                            broadcast.putExtra(ConstantCode.INTENT_CAR_ID, Integer.parseInt(carId));
                            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(PrefManager.getInstance().contains(ConstantCode.TRIP_SYNC_FLAG))
                        {
                            //Log.e("TRIP_SYNC_FLAG 3 : ", "MADE FALSE");
                            PrefManager.putBoolean(ConstantCode.TRIP_SYNC_FLAG, false);
                        }
                        else
                        {
                            //Log.e("TRIP_SYNC_FLAG 4 : ", "MADE FALSE");
                            PrefManager.getInstance().edit().putBoolean(ConstantCode.TRIP_SYNC_FLAG, false).commit();
                        }
                        //TODO : SYNC_LOGIC change
                        //ActivityAllTrips.setFlag(Integer.parseInt(carId),serverRecentTripId, tripId);
                    }

                    @Override
                    public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                        super.failedForNetwork(values, webService);
                        Log.d(TAG, "failedForNetwork() called with: " + "values = [" + values + "], webService = [" + webService + "]");
                        //TODO : SYNC_LOGIC
                        //ActivityAllTrips.setFlag(Integer.parseInt(carId),serverRecentTripId, tripId);
                        try {
                            //to remove banner for onResume
                            if(PrefManager.getInstance().contains(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA))
                            {
                                PrefManager.getInstance().edit().putLong(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis()).commit();
                            }
                            else
                            {
                                PrefManager.putLong(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis());
                            }
                            //to get instant updates if trip sync completed.
                            Intent broadcast = new Intent(ConstantCode.BROADCAST_TRIP_SYNCED);
                            broadcast.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_SYNC_COMPLETE);
                            broadcast.putExtra(ConstantCode.INTENT_CAR_ID, Integer.parseInt(carId));
                            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try{
                            PrefManager.getInstance().edit().remove(String.valueOf(carId)).commit();
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                        if(PrefManager.getInstance().contains(ConstantCode.TRIP_SYNC_FLAG))
                        {
                            //Log.e("TRIP_SYNC_FLAG 5 : ", "MADE FALSE");
                            PrefManager.putBoolean(ConstantCode.TRIP_SYNC_FLAG, false);
                        }
                        else
                        {
                            //Log.e("TRIP_SYNC_FLAG 6 : ", "MADE FALSE");
                            PrefManager.getInstance().edit().putBoolean(ConstantCode.TRIP_SYNC_FLAG, false).commit();
                        }
                    }

                    @Override
                    public void loadOffline() {
                        super.loadOffline();
                        Log.d(TAG, "loadOffline() called with: " + "");
                        try {
                            //to remove banner for onResume
                            if(PrefManager.getInstance().contains(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA))
                            {
                                PrefManager.getInstance().edit().putLong(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis()).commit();
                            }
                            else
                            {
                                PrefManager.putLong(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis());
                            }
                            //to get instant updates if trip sync completed.
                            Intent broadcast = new Intent(ConstantCode.BROADCAST_TRIP_SYNCED);
                            broadcast.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_SYNC_COMPLETE);
                            broadcast.putExtra(ConstantCode.INTENT_CAR_ID, Integer.parseInt(carId));
                            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try{
                            PrefManager.getInstance().edit().remove(String.valueOf(carId)).commit();
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                        //TODO: SYNC_LOGIC change
                        //ActivityAllTrips.setFlag(Integer.parseInt(carId),serverRecentTripId, tripId);
                        if(PrefManager.getInstance().contains(ConstantCode.TRIP_SYNC_FLAG))
                        {
                            //Log.e("TRIP_SYNC_FLAG 7 : ", "MADE FALSE");
                            PrefManager.putBoolean(ConstantCode.TRIP_SYNC_FLAG, false);
                        }
                        else
                        {
                            //Log.e("TRIP_SYNC_FLAG 8 : ", "MADE FALSE");
                            PrefManager.getInstance().edit().putBoolean(ConstantCode.TRIP_SYNC_FLAG, false).commit();
                        }
                    }
                }
        );
    }

    /**
     * Used to saving the trip in database and add graph values in graph table on day bases means more then 1 trip on same date make 1 entry on Graph Table with that date.
     *
     * @param carId
     * @param tripId
     * @param serverRecentTripId
     * @param json
     */
    public void performCalculation(final String carId, final String tripId, final String serverRecentTripId, final JSONObject json) {
        if (cancelSync == true) {
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                ArrayList<TripDetailMain> tripDetailsArrayList = Utility.parseArrayFromString(json.optString(ConstantCode.data), TripDetailMain[].class);
                /**
                 * All the below formula shared no google sheet - https://docs.google.com/spreadsheets/d/1wTVfoEkRa0aKsfJU1A-PtCpLlmaKY68ObpRWGBbu15s/edit#gid=2136442752
                 */

                //For Getting current calculated values from database.
                boolean isSyncFullCompleted = false;
                if (tripDetailsArrayList.size() > 0) {

                    int carId = tripDetailsArrayList.get(0).car_id;
                    Cars car = Cars.readSpecific(carId + "");
                    Calendar tripCalendar = Calendar.getInstance();
                    Calendar calendarToday = Calendar.getInstance();

                    int maxTripId = 0;

                    //Saving all the trips in local database
                    for (TripDetailMain tripDetails : tripDetailsArrayList) {

                        tripCalendar = DateHelper.getCalendarFromServer(tripDetails.start_time);
                        if (!(yearsBetween(calendarToday, tripCalendar) == 0 || yearsBetween(calendarToday, tripCalendar) == 1)) {
                            //Log.e("CHECKING : ", yearsBetween(calendarToday, tripCalendar) +" ");
                            continue;
                        }

                        if (tripDetails.distance <= 0) {
                            continue;
                        }

                        if (tripDetails.trip_id == Integer.parseInt(serverRecentTripId))
                            isSyncFullCompleted = true;

                        maxTripId = Math.max(maxTripId, tripDetails.trip_id);

                        TripDetailMain tdm = TripDetailMain.readSpecific(String.valueOf(tripDetails.trip_id));
                        if(tdm != null)
                        {
                            Routes.removeExistingEntries(tripDetails.trip_id);
                        }
                        //saving trip on TripDetailMain table
                        tripDetails.save();
                    }

                    //Saving max field id in local database for specific car.
                    if (maxTripId > 0)
                    {
                        car.local_latest_trip = maxTripId;
                        if (Utility.getLoggedInUser() != null) {
                            Log.e("TRIPS in A CALL : ","MAX TRIP ID : " + maxTripId);
                            car.updateLocalLatestTripValues(maxTripId);
                        }
                        Cars updatedInfo = Cars.readSpecific(car.id + "");
                        //Make get recent trip call here
                        if(updatedInfo.serverTripID != updatedInfo.local_latest_trip) {
                            Log.e("TRIPS in A CALL : ","SyncController");
                            Log.e("TRIPS in A CALL : ", "server Trip id : "+ car.serverTripID);
                            Log.e("TRIPS in A CALL : ", "local Trip id : "+ car.local_latest_trip);
                            ActivityAllTrips.getRecentTripsDetail(App.getContext(), updatedInfo.id, updatedInfo.serverTripID, updatedInfo.local_latest_trip, updatedInfo.serverFlag);
                        }else{
                            if(PrefManager.getInstance().contains(ConstantCode.TRIP_SYNC_FLAG))
                            {
                                //Log.e("TRIP_SYNC_FLAG 9 : ", "MADE FALSE");
                                PrefManager.putBoolean(ConstantCode.TRIP_SYNC_FLAG, false);
                            }
                            else
                            {
                                //Log.e("TRIP_SYNC_FLAG 10 : ", "MADE FALSE");
                                PrefManager.getInstance().edit().putBoolean(ConstantCode.TRIP_SYNC_FLAG, false).commit();
                            }
                            //Log.e(TAG, "serverTripId == LocalTripID !!!!!! ");
                        }

                        //=========================================
                        //==    ======GRAPH CALCULATION START==========
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

                                //simply skipping the trip with distance less then or equal to zero
                                if (tripDetails.distance <= 0) {
                                    continue;
                                }

                                //discarding any trip whose time is more then one year
                                tripCalendar = DateHelper.getCalendarFromServer(tripDetails.start_time);
                                if (!(yearsBetween(calendarToday, tripCalendar) == 0 || yearsBetween(calendarToday, tripCalendar) == 1)) {
                                    continue;
                                }
                                Log.e("TRIPS in A CALL : ", tripDetails.trip_id + " ");
                                //if graph Last is blank means it is new entry so creating new entry in database.
                                //Code is written assuming tripDetail.distance !=0 , so also tripdDetail.avg_mileage !=0
                                //showSyncLog("CURRENT : " + (graphLast != null ? tripCalendar.get(Calendar.DAY_OF_MONTH) + "/" + tripCalendar.get(Calendar.MONTH) : "") + "================" + "TRIP : " + tripCalendar.get(Calendar.DAY_OF_MONTH) + "/" + tripCalendar.get(Calendar.MONTH));
                                if (graphLast == null) {
                                    calendarLast = DateHelper.getCalendarFromServer(tripDetails.start_time);
                                    graphLast = new Graph();
                                    graphLast.avg_drive_score = Utility.round(((graphLast.avg_drive_score * graphLast.avg_distance) + (tripDetails.drive_score * tripDetails.distance)) / (graphLast.avg_distance + tripDetails.distance));
                                    graphLast.avg_mileage = Utility.round(((graphLast.avg_distance + tripDetails.distance) / (tripDetails.distance / tripDetails.avg_mileage)));
                                    graphLast.avg_cost = Utility.round((tripDetails.distance / tripDetails.avg_mileage) * getPrice(car.fuel));
                                    graphLast.avg_distance = Utility.round(graphLast.avg_distance + tripDetails.distance);
                                    graphLast.avg_duration = Utility.round(graphLast.avg_duration + tripDetails.trip_time);
                                    graphLast.car_id = carId;
                                    graphLast.timestamp = calendarLast.getTimeInMillis();
                                    graphLast.save();
                                    showSyncLog("First : ADD NEW ENTRY " + calendarLast.get(Calendar.DAY_OF_MONTH) + "/" + calendarLast.get(Calendar.MONTH));

                                    //Log.e(TAG, "-----New Entry into GraphData table");
                                    GraphData gdOld = GraphData.readSpecific(String.valueOf(carId), String.valueOf(calendarLast.get(Calendar.DAY_OF_MONTH)), String.valueOf(calendarLast.get(Calendar.MONTH)), String.valueOf(calendarLast.get(Calendar.YEAR)));
                                    if(gdOld != null)
                                    {
                                        gdOld.avg_drive_score = Utility.round(((graphLast.avg_drive_score * graphLast.avg_distance) + (tripDetails.drive_score * tripDetails.distance)) / (graphLast.avg_distance + tripDetails.distance));
                                        gdOld.avg_mileage = Utility.round(((graphLast.avg_distance + tripDetails.distance) / (tripDetails.distance / tripDetails.avg_mileage)));
                                        gdOld.avg_cost = Utility.round((tripDetails.distance / tripDetails.avg_mileage) * getPrice(car.fuel));
                                        gdOld.avg_distance = Utility.round(graphLast.avg_distance + tripDetails.distance);
                                        gdOld.avg_duration = Utility.round(graphLast.avg_duration + tripDetails.trip_time);
                                        gdOld.car_id = carId;
                                        gdOld.timestamp = calendarLast.getTimeInMillis();
                                        gdOld.save();

                                    }

                                } else
                                {
                                    //Checking the days between last graph entry and current entry if it is zero(0) then performing the calculation with previous saved value and new trip, and saving it in database(Merging)

                                    //TODO : Check weather end date is greater that start date.
                                    /**
                                     * calenderLast            ----->     Start Date
                                     * tripDetails.timestamp   ----->     End date
                                     * Present Bug = if end date - start date < 0
                                     * then too we end up pushing values into Graph table by increamenting dates.
                                     * Make changes in daysBetween(startDate,Endate)
                                     */

                                    //long daysBetween = daysBetween(calendarLast, DateHelper.getCalendarFromServer(tripDetails.timestamp));
                                    //Log.e("TAG",String.valueOf(tripDetails.trip_id));
                                    long daysBetween = daysBetweenWithoutAbsoluteDiff(calendarLast, DateHelper.getCalendarFromServer(tripDetails.start_time));
                                    //Log.e("TAG", String.valueOf(daysBetween));
                                    if (daysBetween == 0 || daysBetween < 0)
                                    {
                                        Calendar cal = DateHelper.getCalendarFromServer(tripDetails.start_time);

                                        Graph graphOld = Graph.readSpecific(String.valueOf(carId), String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), String.valueOf(cal.get(Calendar.MONTH)), String.valueOf(cal.get(Calendar.YEAR)));

                                        //Log.e(TAG, String.valueOf(carId));
                                        GraphData gdOld = GraphData.readSpecific(String.valueOf(carId), String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), String.valueOf(cal.get(Calendar.MONTH)), String.valueOf(cal.get(Calendar.YEAR)));
                                        if(gdOld!= null)
                                        {
                                            //Querying the tripDetailsMain Table for all the trips of the current day
                                            ArrayList<TripDetailMain> tripsForTheDay = TripDetailMain.readAllTripsForSpecificDate(String.valueOf(tripDetails.car_id),String.valueOf(cal.get(Calendar.DAY_OF_MONTH)),String.valueOf(cal.get(Calendar.MONTH)),String.valueOf(cal.get(Calendar.YEAR)));
                                            //Log.e(TAG, tripsForTheDay.toString());
                                            //Log.e("Start Date of trip : ",tripDetails.start_time);
                                            //Log.e("Old Values : ",String.valueOf(gdOld.avg_distance));
                                            //Log.e("To Be added : ", String.valueOf(tripDetails.distance));
                                            double totalDistanceToday = 0.0;
                                            double duration = 0.0;
                                            double costToday = 0.0;
                                            double avgMileage = 0.0;
                                            double avgDriveScore = 0.0;

                                            //for avg mileage calculation
                                            double totalFuelTillNow = 0.0;
                                            double totalDriveScoreDotDistance = 0.0;

                                            for(TripDetailMain trip : tripsForTheDay)
                                            {
                                                totalDistanceToday = totalDistanceToday + trip.distance;
                                                double driveScoreDotDistanceForCurrentTrip = trip.distance*trip.drive_score;
                                                totalDriveScoreDotDistance = totalDriveScoreDotDistance + driveScoreDotDistanceForCurrentTrip;
                                                if(totalDistanceToday!=0)
                                                    avgDriveScore = totalDriveScoreDotDistance / totalDistanceToday;


                                                double fuelForCurrentTrip = 0.0;
                                                if(trip.avg_mileage !=0 )
                                                {
                                                    fuelForCurrentTrip = (trip.distance/trip.avg_mileage);
                                                    totalFuelTillNow = totalFuelTillNow + fuelForCurrentTrip;
                                                    avgMileage = (totalDistanceToday) / totalFuelTillNow;

                                                    //avg_cost calculation
                                                    costToday = totalFuelTillNow * getPrice(car.fuel);
                                                }

                                                duration = duration + trip.trip_time;

                                            }
                                            /*
                                            if(graphOld!=null)
                                            {
                                                graphOld.avg_drive_score = Utility.round(avgDriveScore);
                                                graphOld.avg_cost = Utility.round(costToday);
                                                graphOld.avg_mileage = Utility.round(avgMileage);
                                                graphOld.car_id = tripDetails.car_id;
                                                graphOld.avg_distance = Utility.round(totalDistanceToday);
                                                graphOld.timestamp = cal.getTimeInMillis();
                                                graphOld.avg_duration = Utility.round(duration);
                                                graphOld.save();
                                            }
                                            */
                                            gdOld.avg_drive_score = Utility.round(avgDriveScore);
                                            gdOld.avg_cost = Utility.round(costToday);
                                            gdOld.avg_mileage = Utility.round(avgMileage);
                                            gdOld.car_id = tripDetails.car_id;
                                            gdOld.avg_distance = Utility.round(totalDistanceToday);
                                            gdOld.timestamp = cal.getTimeInMillis();
                                            gdOld.avg_duration = Utility.round(duration);
                                            gdOld.save();

                                            updatingCurrentWeeksInfo(String.valueOf(carId), tripDetails.start_time);
                                            updatingCurrentMonthsInfo(String.valueOf(carId), tripDetails.start_time, getPrice(car.fuel));

                                        /*
                                        Calendar cal = DateHelper.getCalendarFromServer(tripDetails.start_time);
                                        Graph graphOld = Graph.readSpecific(String.valueOf(carId), String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), String.valueOf(cal.get(Calendar.MONTH)), String.valueOf(cal.get(Calendar.YEAR)));
                                        graphLast.avg_drive_score = Utility.round(((graphLast.avg_drive_score * graphLast.avg_distance) + (tripDetails.drive_score * tripDetails.distance)) / (graphLast.avg_distance + tripDetails.distance));
                                        graphLast.avg_mileage = Utility.round( (graphLast.avg_distance + tripDetails.distance) / ((graphLast.avg_distance / graphLast.avg_mileage) + (tripDetails.distance / tripDetails.avg_mileage)));
                                        graphLast.avg_cost = Utility.round(((graphLast.avg_distance / graphLast.avg_mileage ) + (tripDetails.distance / tripDetails.avg_mileage)) * getPrice(car.fuel));
                                        graphLast.avg_distance = Utility.round(graphLast.avg_distance + tripDetails.distance);
                                        graphLast.avg_duration = Utility.round(graphLast.avg_duration + tripDetails.trip_time);
                                        graphLast.car_id = carId;
                                        graphLast.timestamp = cal.getTimeInMillis();
                                        graphLast.save();
                                        */
                                            showSyncLog("MERGING " + calendarLast.get(Calendar.DAY_OF_MONTH) + "/" + calendarLast.get(Calendar.MONTH));
                                        }

                                    } else
                                    {
                                        //if days is greater then 0 then adding intermediate days as blank so it fill in the charts in CarDashboard screen
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
                                                    showSyncLog("INSERTING BLANK ENTRIES " + calendarLast.get(Calendar.DAY_OF_MONTH) + "/" + calendarLast.get(Calendar.MONTH));
                                                }
                                            }
                                        }
                                        //At last we are inserting the entry in database
                                        graphLast = new Graph();
                                        calendarLast = DateHelper.getCalendarFromServer(tripDetails.timestamp);
                                        if(tripDetails.distance != 0)
                                            graphLast.avg_drive_score = Utility.round((tripDetails.drive_score * tripDetails.distance) /  tripDetails.distance);
                                        else
                                            graphLast.avg_drive_score = 0.0;

                                        graphLast.avg_mileage = Utility.round(tripDetails.avg_mileage);
                                        graphLast.avg_cost = Utility.round((tripDetails.distance / tripDetails.avg_mileage) * getPrice(car.fuel));
                                        graphLast.avg_distance = Utility.round(tripDetails.distance);
                                        graphLast.avg_duration = Utility.round(tripDetails.trip_time);
                                        graphLast.car_id = carId;
                                        graphLast.timestamp = calendarLast.getTimeInMillis();
                                        graphLast.save();
                                        showSyncLog("ADD NEW ENTRY " + calendarLast.get(Calendar.DAY_OF_MONTH) + "/" + calendarLast.get(Calendar.MONTH));


                                        //Log.e(TAG, "-----New Entry into GraphData table");
                                        GraphData gdOld = GraphData.readSpecific(String.valueOf(carId), String.valueOf(calendarLast.get(Calendar.DAY_OF_MONTH)), String.valueOf(calendarLast.get(Calendar.MONTH)), String.valueOf(calendarLast.get(Calendar.YEAR)));
                                        if(gdOld != null)
                                        {
                                            if(tripDetails.distance != 0)
                                                gdOld.avg_drive_score = Utility.round((tripDetails.drive_score * tripDetails.distance) /  tripDetails.distance);
                                            else
                                                gdOld.avg_drive_score = 0.0;

                                            gdOld.avg_mileage = Utility.round(tripDetails.avg_mileage);
                                            gdOld.avg_cost = Utility.round((tripDetails.distance / tripDetails.avg_mileage) * getPrice(car.fuel));
                                            gdOld.avg_distance = Utility.round(tripDetails.distance);
                                            gdOld.avg_duration = Utility.round(tripDetails.trip_time);
                                            gdOld.car_id = carId;
                                            gdOld.timestamp = calendarLast.getTimeInMillis();
                                            gdOld.save();
                                        }
                                    }
                                }
                                i++;
                            }
                        }
                        //========================================
                        //========GRAPH CALCULATION END===========
                        //========================================
                    }
                    if (cancelSync == false) {
                        try {
                            //Sending broadcast to FragmentCarDiagnostics so view can refresh and load all the details from database
                            //This will only happen for at last only.
                            if (isSyncFullCompleted && car!=null && car.local_latest_trip == car.serverTripID) {

                                PrefManager.putLong(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis());

                                Intent broadcast = new Intent(ConstantCode.BROADCAST_TRIP_SYNCED);
                                broadcast.putExtra(ConstantCode.INTENT_CAR_ID, Integer.parseInt(carId + ""));
                                broadcast.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_SYNC_COMPLETE);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
                                PrefManager.getInstance().edit().remove(String.valueOf(carId)).commit();
                                if(PrefManager.getInstance().contains(ConstantCode.TRIP_SYNC_FLAG))
                                {
                                    //Log.e("TRIP_SYNC_FLAG 11 : ", "MADE FALSE");
                                    PrefManager.putBoolean(ConstantCode.TRIP_SYNC_FLAG, false);
                                }
                                else
                                {
                                    //Log.e("TRIP_SYNC_FLAG 12 : ", "MADE FALSE");
                                    PrefManager.getInstance().edit().putBoolean(ConstantCode.TRIP_SYNC_FLAG, false).commit();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //TODO : SYNC_LOGIC change
                    //ActivityAllTrips.setFlag(carId,serverRecentTripId, tripId);
                    /*
                    try{
                        PrefManager.getInstance().edit().remove(String.valueOf(carId)).commit();

                        PrefManager.putLong(String.valueOf(carId)+ConstantCode.LAST_SYNC_TIME_FOR_CARS_TRIP_DATA,Calendar.getInstance().getTimeInMillis());

                        Intent broadcast = new Intent(ConstantCode.BROADCAST_TRIP_SYNCED);
                        broadcast.putExtra(ConstantCode.INTENT_CAR_ID, Integer.parseInt(carId + ""));
                        broadcast.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_SYNC_COMPLETE);
                        broadcast.putExtra(ConstantCode.INTENT_DATA, Calendar.getInstance().getTimeInMillis());
                        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    */
                }
                //Log.e("TRIPS in A CALL : ", "************");
                return null;
            }
        }.execute();
    }

    private void showSyncLog(String msg) {
        Log.d("SYNC_LOG", "" + msg);
    }

    /**
     * Get price as per car wise.
     *
     * @param fuelType
     * @return
     */
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

    /**
     * Getting the days between start and end dates
     *
     * @param startDate
     * @param endDate
     * @return
     */
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

    /**
     * Getting the days between start and end dates
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public long daysBetweenWithoutAbsoluteDiff(Calendar startDate, Calendar endDate) {
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
        if(end >= start)
        {
            return TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
        }
        else
            return -1;
    }

    /**
     * Getting years between maxDate and minDate
     *
     * @param maxDate
     * @param minDate
     * @return
     */
    public long yearsBetween(Calendar maxDate, Calendar minDate) {
        long max = maxDate.get(Calendar.YEAR);
        long min = minDate.get(Calendar.YEAR);
        return max - min;
    }

    boolean cancelSync = false;

    //Used to cancel the sync from FragmentCars
    public void cancelSync() {
        cancelSync = true;
    }

    public void updatingCurrentWeeksInfo(String carID, String tripTimeStamp)
    {

        //Current week list
        ArrayList<GraphData> daysOfWeek = GraphData.readCurrentWeek(String.valueOf(carID), tripTimeStamp);
        double totalDistanceForTheWeek = 0.0;
        double avgDriveScore = 0.0;
        double totalDriveScoreDotDistance = 0.0;
        for(GraphData weeklyData : daysOfWeek)
        {
            totalDistanceForTheWeek = totalDistanceForTheWeek + weeklyData.avg_distance;
            double driveScoreDotDistanceForCurrentDay = weeklyData.avg_distance*weeklyData.avg_drive_score;
            totalDriveScoreDotDistance = totalDriveScoreDotDistance + driveScoreDotDistanceForCurrentDay;
            if(totalDistanceForTheWeek!=0)
                avgDriveScore = totalDriveScoreDotDistance / totalDistanceForTheWeek;
        }
        GraphData.updateAvgValueOfWeek(tripTimeStamp, String.valueOf(carID),avgDriveScore);

    }

    public void updatingCurrentMonthsInfo(String carID, String tripTimeStamp, double fuelPrice)
    {
        //Current Month List
        ArrayList<GraphData> daysOfMonth = GraphData.readCurrentMonth(String.valueOf(carID), tripTimeStamp);
        //Log.e("TAG",daysOfMonth.size()+"");
        double totalDistanceForTheMonth = 0.0;
        double avgDriveScore = 0.0;
        double totalDriveScoreDotDistance = 0.0;
        double totalFuelTillNow = 0.0;
        double avgMileage = 0.0;
        double costForTheMonth = 0.0;
        double duration =0.0;
        for(GraphData monthlyData : daysOfMonth)
        {
            totalDistanceForTheMonth = totalDistanceForTheMonth + monthlyData.avg_distance;
            double driveScoreDotDistanceForCurrentDay = monthlyData.avg_distance*monthlyData.avg_drive_score;
            totalDriveScoreDotDistance = totalDriveScoreDotDistance + driveScoreDotDistanceForCurrentDay;
            if(totalDistanceForTheMonth!=0)
                avgDriveScore = totalDriveScoreDotDistance / totalDistanceForTheMonth;


            double fuelForCurrentDay = 0.0;
            if(monthlyData.avg_mileage !=0 )
            {
                fuelForCurrentDay = (monthlyData.avg_distance/monthlyData.avg_mileage);
                totalFuelTillNow = totalFuelTillNow + fuelForCurrentDay;
                avgMileage = (totalDistanceForTheMonth) / totalFuelTillNow;

                //avg_cost calculation
                costForTheMonth = totalFuelTillNow * fuelPrice;

                duration = duration + monthlyData.avg_duration;
            }

        }
        GraphData.updateAvgValueOfMonth(tripTimeStamp, String.valueOf(carID), avgDriveScore, totalDistanceForTheMonth, avgMileage, costForTheMonth, duration);

    }
}
