package com.carnot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.carnot.App;
import com.carnot.R;
import com.carnot.adapter.AllTripGroupedAdapter;
import com.carnot.controller.SyncController;
import com.carnot.global.ConstantCode;
import com.carnot.global.PrefManager;
import com.carnot.global.UILoadingHelper;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.DateHelper;
import com.carnot.libclasses.LoadMoreRecyclerAdapter;
import com.carnot.libclasses.VerticalSpaceItemDecoration;
import com.carnot.models.Cars;
import com.carnot.models.TripDetailMain;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by root on 8/4/16.
 * Activity to load all the trips
 */
public class ActivityAllTrips extends BaseActivity {

    private static final String TAG = "ActivityAllTrips";

    private final String DATEPICKER_TAG = "datepicker";
    RecyclerView recyclerView;
    AllTripGroupedAdapter adapter;
    SwipeRefreshLayout swipeToRefreshLayout;
    UILoadingHelper uiLoadingHelper;
    DatePickerDialog datePickerDialog;
    TextView txtFilterText;
    TextView txtForSyncInProgress;
    private ArrayList<TripDetailMain> listTripFinal = new ArrayList<TripDetailMain>();
    private CheckBox chkShowShortTrips;


    String carId;
    boolean shouldShowShortTrips = false;
    static SyncController syncController;

    private int numItemsToLoad = 5;
    private int start = numItemsToLoad;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_trips);
    }

    @Override
    public void initVariable() {

        uiLoadingHelper = new UILoadingHelper();
        setToolbar(R.id.toolbar, true);
        setTitle(R.string.lbl_all_trips);

        carId = getIntent().getExtras().getString(ConstantCode.INTENT_CAR_ID);
        shouldShowShortTrips = getIntent().getExtras().getBoolean(ConstantCode.INTENT_CAR_SHOW_SHORT_TRIPS);
        if (getIntent() != null) {
            getSupportActionBar().setTitle(getIntent().getExtras().getString(ConstantCode.INTENT_CAR_NAME));
            Cars currentCar = Cars.readSpecific(carId);
            if(currentCar != null)
            {
                if(getIntent().getExtras().getBoolean(ConstantCode.INTENT_CAR_STATUS))
                {
                    if(Utility.isConnectingToInternet(mActivity))
                    {
                        Calendar onTripSyncTime = DateHelper.getCalendarFromServer(currentCar.lut);
                        if(onTripSyncTime != null){
                            Calendar todayTime = Calendar.getInstance();
                            if (todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis() > 0) {
                                long sec = TimeUnit.MILLISECONDS.toSeconds(Math.abs(todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis()));
                                long min = TimeUnit.MILLISECONDS.toMinutes(Math.abs(todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis()));
                                if (sec > 30 || min > 10) {
                                    getSupportActionBar().setSubtitle("");
                                }
                                else
                                {
                                    if(currentCar.isOnTrip)
                                        getSupportActionBar().setSubtitle("ON TRIP");
                                }
                            }
                        }
                    }
                    else{
                        getSupportActionBar().setSubtitle("");
                    }
                }
            }


        }

        final Calendar calendar = Calendar.getInstance();

        //Date Picker dialog to search list with date
        Calendar now = Calendar.getInstance();
        datePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        monthOfYear = monthOfYear + 1;
                        String searchDateFormat = String.format("%02d-%02d-%02d", year, monthOfYear, dayOfMonth);
                        uiLoadingHelper.startProgress();

                        //Fetching list from database based on date query
                        ArrayList<TripDetailMain> listTrip = TripDetailMain.readAllWithFilter(carId, searchDateFormat);
                        if (listTrip.size() > 0) {
                            adapter.setItem(listTrip);
                            uiLoadingHelper.showContent();
                            txtFilterText.setText(getString(R.string.lbl_search_for_x, searchDateFormat));
                            txtFilterText.setVisibility(View.VISIBLE);
                        } else {
                            txtFilterText.setVisibility(View.GONE);
                            uiLoadingHelper.showError(getString(R.string.msg_no_trips_for_x, searchDateFormat));
                        }
                        swipeToRefreshLayout.setRefreshing(false);
                    }
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.setYearRange(Calendar.getInstance().get(Calendar.YEAR) - 100, Calendar.getInstance().get(Calendar.YEAR));

        txtForSyncInProgress = (TextView)links(R.id.txt_notification_title);
        txtForSyncInProgress.setFocusable(true);
        txtForSyncInProgress.setVisibility(View.GONE);
        syncController = new SyncController(mActivity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterForSyncStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(chkShowShortTrips != null) {
            chkShowShortTrips.setChecked(shouldShowShortTrips);
        }
        if(Utility.isConnectingToInternet(mActivity))
        {
            txtForSyncInProgress.setVisibility(View.GONE);
            checkForGreenBanner();
        }
        else
        {
            txtForSyncInProgress.setVisibility(View.VISIBLE);
            txtForSyncInProgress.setText(getString(R.string.no_internet_connection_found));
        }

    }
    /*
    void doACallToUpdateTrips(Context mActivity)
    {

        if(Utility.isConnectingToInternet(mActivity))
        {
            String msg = PrefManager.getInstance().getString(String.valueOf(carId), null);
            Log.e(TAG, "GREEN BANNER STATUS" + "------"+msg);
            if (msg != null) {
                txtForSyncInProgress.setText(getString(R.string.msg_syncing_is_in_progress));
                txtForSyncInProgress.setVisibility(View.VISIBLE);
            }
            else {
                txtForSyncInProgress.setText(getString(R.string.msg_syncing_is_in_progress));
                txtForSyncInProgress.setVisibility(View.VISIBLE);
                new AsyncTask<Void, Void, Void>(){

                    @Override
                    protected Void doInBackground(Void... params) {
                        callGetRecentTripWebApi(App.getContext());
                        return null;
                    }
                }.execute();

                txtForSyncInProgress.setVisibility(View.GONE);
            }
        }
        else
        {
            txtForSyncInProgress.setVisibility(View.VISIBLE);
            txtForSyncInProgress.setText(getString(R.string.no_internet_connection_found));
        }
    }
    */
    @Override
    public void initView() {
        recyclerView = (RecyclerView) links(R.id.recycler_view);
        txtFilterText = (TextView) links(R.id.txt_filter_text);
        swipeToRefreshLayout = (SwipeRefreshLayout) links(R.id.swipe_to_refresh);
        swipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                txtFilterText.setVisibility(View.GONE);
                latestHeader = "";
                start = numItemsToLoad;
                listTripFinal = new ArrayList<TripDetailMain>();
                loadFromDatabase2();
                //TODO : call a function to update the trips in local DB
                if(!checkForGreenBanner())
                {
                    //Log.e("Checking....1","hie");
                    if(carId != null)
                    {
                        //Log.e("Checking....1","hie");
                        Cars car = Cars.readSpecific(carId);
                        if(car!= null)
                        {
                            //Log.e("Checking....1","hie");
                            Log.e("CALL FROM : ","Pull DOWN");
                            if(!PrefManager.getBoolean(ConstantCode.TRIP_SYNC_FLAG))
                            {
                                Log.e("CALL FROM : ", "refreshView");
                                getRecentTripsDetail(mActivity, car.id, car.serverTripID, car.local_latest_trip, car.serverFlag);
                            }
                            else{
                                Log.e("Busy : ","Trip syncing lpease wait !!");
                            }
                        }
                    }
                }

            }
        });
        uiLoadingHelper.set(links(R.id.txt_empty), links(R.id.progress_bar), recyclerView);
        links(R.id.txt_empty).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtFilterText.setVisibility(View.GONE);
                ArrayList<TripDetailMain> listTrip = TripDetailMain.readSpecificTrips(carId, -1, shouldShowShortTrips);
                if (listTrip.size() > 0) {
                    listTrip = makeHeadersInList(listTrip);
                    adapter.setItem(listTrip);
                    uiLoadingHelper.showContent();
                } else {
                    uiLoadingHelper.showError(getString(R.string.msg_no_trips));
                }
                swipeToRefreshLayout.setRefreshing(false);
            }
        });
        chkShowShortTrips = (CheckBox) links(R.id.chk_show_short_trips);

        chkShowShortTrips.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                txtFilterText.setVisibility(View.GONE);
                Log.e("Chekcing : ", isChecked + "");
                shouldShowShortTrips = isChecked;
                Cars mCar;
                if(carId != null) {
                    mCar = Cars.readSpecific(carId);
                    mCar.updateShowShortTrip(chkShowShortTrips.isChecked());
                }
                latestHeader = "";
                start = numItemsToLoad;
                listTripFinal = new ArrayList<TripDetailMain>();
                loadFromDatabase2();
            }
        });

    }
    /*
    public static void callGetRecentTripWebApi(Context mTemp)
    {
        if(Utility.isConnectingToInternet(mTemp))
        {
            if(!PrefManager.getInstance().contains(ConstantCode.TRIP_SYNC_FLAG))
            {
                makeGetRecentTripCall(mTemp);
            }
            else
            {
                if(!PrefManager.getBoolean(ConstantCode.TRIP_SYNC_FLAG))
                {
                    makeGetRecentTripCall(mTemp);
                }
                else
                {
                    Toast.makeText(mTemp, "Trip Syncing Under Progress",Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Previous request is under process");
                }
            }
            //makeGetRecentTripCall(mTemp);
        }
        else
        {
            //Toast.makeText(mTemp,"Network Connectivity Issue",Toast.LENGTH_SHORT).show();
        }
    }
    */

    public static void makeGetRecentTripCall(Context mTemp){

        Log.e(TAG,"makeGetRecentTripCall()");
        //TODO : fetch local latest trips and server latest trips from cars table of a particular car
        HashMap<Integer, ArrayList<Integer>> localTripsInformation = Cars.readAllWithRecentTripsFromLocalDB();
        /**
         * ArrayList<Integer>
         *     0 -- Local Latest Trip ID
         *     1 -- Server Latest Trip ID
         *     2 -- Flag
        */
        //TODO :call get recent trip detail here.
        Calendar cal = Calendar.getInstance();
        if(Utility.getLoggedInUser() != null)
        {
            if(localTripsInformation != null)
            {
                for(int carID : localTripsInformation.keySet())
                {
                    Log.e(TAG,carID+" "+localTripsInformation.get(carID));
                    //PrefManager.putLong(ConstantCode.LAST_SYNC_TIME_FOR_TRIPS, cal.getTimeInMillis());
                    Log.e("CALL FROM : ", "All Trips Acitivity");
                    getRecentTripsDetail(mTemp,carID, localTripsInformation.get(carID).get(1), localTripsInformation.get(carID).get(0) != null ? localTripsInformation.get(carID).get(0) : 0, localTripsInformation.get(carID).get(2));
                }

            }
            else
            {
                Log.e(TAG, "Make GARAGE API call first");
            }
        }
    }

    //structure to save current progress work on syncing so new request will not go unless first one is completed
    //TODO :
    //static HashMap<Integer,ArrayList<String>> goesForOnline;

    /**
     * Used to sync the recent trip and here conditions are checking if flag comes from server is 1 then we need to syn else our local latest trip id is less then server trip id
     *
     * @param id
     * @param serverRecentTrip
     * @param localRecentTrip
     * @param flag
     */
    public static void getRecentTripsDetail(Context mTemp, int id, int serverRecentTrip, int localRecentTrip, int flag) {
        //Checking if local recent trip is less then server recent trip if true then fetch from server

        Log.e(TAG, "getRecentTripsDetail() called with: " + "id = [" + id + "], serverRecentTrip = [" + serverRecentTrip + "], localRecentTrip = [" + localRecentTrip + "], flag = [" + flag + "]");
        //TODO :
        /*
        if (goesForOnline == null) {
            goesForOnline = new HashMap<Integer,ArrayList<String>>();
        }
        if(goesForOnline.get(id)==null)
        {
            goesForOnline.put(id, new ArrayList<String>());
        }
        */

        syncController = new SyncController(mTemp);
        if (localRecentTrip < serverRecentTrip || flag == 1) {

            //checking if this request is already in progress then remove no need to sync
            //TODO :
            /*
            if ( !goesForOnline.get(id).contains(serverRecentTrip + "-" + localRecentTrip) )
            {
            */
            Log.e(TAG,"*******API CALL MADE*********");
            //Syncing the trips
            syncController.sync(id + "", localRecentTrip + "", serverRecentTrip + "");

            //Saving value in local datastructure because it will not call again if the that trip is is progress
            //TODO :
            /*
            goesForOnline.get(id).add(serverRecentTrip + "-" + localRecentTrip);
            }
            */
        } else {
            //saving the status in preference to make the indiation of "Sync is in progress" on FragmentCarDashboard
            if (TextUtils.isEmpty(PrefManager.getInstance().getString(String.valueOf(id), null)) == false) {
                Calendar cal = Calendar.getInstance();
                PrefManager.getInstance().edit().remove(String.valueOf(id)).commit();
                Intent broadcast = new Intent(ConstantCode.BROADCAST_TRIP_SYNCED);
                broadcast.putExtra(ConstantCode.INTENT_CAR_ID, Integer.parseInt(id + ""));
                broadcast.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_SYNC_COMPLETE);
                broadcast.putExtra(ConstantCode.INTENT_DATA, cal.getTimeInMillis());
                LocalBroadcastManager.getInstance(mTemp).sendBroadcast(broadcast);
            }
        }
    }

    //TODO:
    /*
    //Used to update the flag on local datastructure so that on next calling of garage screen it will sync
    public static void setFlag(int carID,String serverRecentTrip, String localRecentTrip) {
        if (goesForOnline.get(carID).contains(serverRecentTrip + "-" + localRecentTrip)) {
            goesForOnline.get(carID).remove(serverRecentTrip + "-" + localRecentTrip);
        }
    }

    public static HashMap<Integer, ArrayList<String>> getFlags() {
        return goesForOnline;
    }
    */

    @Override
    public void postInitView() {
        txtFilterText.setVisibility(View.GONE);
    }

    @Override
    public void addAdapter() {
        adapter = new AllTripGroupedAdapter(mActivity);


        /*adapter.setIsLoadMoreEnabled(false, R.layout.load_more_progress, recyclerView, new LoadMoreRecyclerAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Utility.showLog(ActivityAllTrips.class, "LoadMoreFired");
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.onLoadMoreComplete();
                    }
                }, 2000);
            }
        });*/

        //setting on click listener so we can open trip detail
        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(mActivity, ActivityTripDetails.class);
                TripDetailMain objectTrip = adapter.getItem(position);
                if (objectTrip != null && objectTrip.id > 0) {
                    //intent.putExtra(ConstantCode.INTENT_DATA, Utility.getJsonString(objectTrip));
                    intent.putExtra(ConstantCode.INTENT_TRIP_ID, String.valueOf(objectTrip.trip_id));
                    intent.putExtras(getIntent().getExtras());
                    startActivity(intent);
                }

            }
        });

        //Click listener to clear the filter and loading original list without search criteria
        txtFilterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtFilterText.setVisibility(View.GONE);
                latestHeader = "";
                start = numItemsToLoad;
                listTripFinal = new ArrayList<TripDetailMain>();
                loadFromDatabase2();
                /*
                ArrayList<TripDetailMain> listTrip = TripDetailMain.readSpecificTrips(carId, -1, shouldShowShortTrips);
                if (listTrip.size() > 0) {
                    listTrip = makeHeadersInList(listTrip);
                    adapter.setItem(listTrip);
                    uiLoadingHelper.showContent();
                } else {
                    uiLoadingHelper.showError(getString(R.string.msg_no_trips));
                }
                swipeToRefreshLayout.setRefreshing(false);
                */
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerView.setLayoutManager(linearLayoutManager);

        VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration((int) Utility.convertDpToPixel(getResources().getDimension(R.dimen.scale_0dp), mActivity));
        recyclerView.addItemDecoration(decoration);

        adapter.setIsLoadMoreEnabled(true, R.layout.load_more_progress, recyclerView, new LoadMoreRecyclerAdapter.OnLoadMoreListener() {
            /*
            public void additems() {
                if (flag_loading) {
                    Log.e(TAG, "EndReached");
                    //mTripListAdapter.swapCursor(TripTable.getTripsAfterScrollReachedBottom(start + 5));
                    start = start + numItemsToLoad;
                    Log.e(TAG,start +"");
                    ArrayList<TripDetailMain> updateList = TripDetailMain.readSpecificTripsForLoadONScroll(carId, start, numItemsToLoad, shouldShowShortTrips);
                    updateList = makeHeadersInList(updateList);
                    listTripFinal.addAll(updateList);
                    int curSize = adapter.getItemCount();
                    adapter.notifyItemRangeInserted(curSize, listTripFinal.size() - 1);

                    //mListView.setSelection(mListView.getCount() - 5);
                    flag_loading = false;
                    adapter.onLoadMoreComplete();
                }
            }
            */
            @Override
            public void onLoadMore() {

            }

            @Override
            public void onLoadMoreWithParameters(int v, int p, int t) {
                Log.e(TAG,"---------------End Reached");
                start = start + numItemsToLoad;
                Log.e("---<<NUMBER>>--",start +"");
                ArrayList<TripDetailMain> updateList = TripDetailMain.readSpecificTripsForLoadONScroll(carId, start, numItemsToLoad, shouldShowShortTrips);
                updateList = makeHeadersInListNew(updateList);
                listTripFinal.addAll(updateList);
                int curSize = adapter.getItemCount();
                adapter.notifyItemRangeInserted(curSize, listTripFinal.size() - 1);
                adapter.onLoadMoreComplete();
                /*
                if (p + v == t && t != 0 && prevTotalItemCount != t) {
                    if (flag_loading == false) {

                        flag_loading = true;
                        additems();
                        prevTotalItemCount = t;
                        //uiLoadingHelper.showContent();
                        adapter.onLoadMoreComplete();
                    }
                }
                */
            }
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void loadData() {
        //loading offline data
        loadFromDatabase2();
        Log.e(TAG,"Broadcaster registered");
        registerForSyncStatus();
        checkForGreenBanner();
    }

    public boolean checkForGreenBanner()
    {
        String msg = PrefManager.getInstance().getString(String.valueOf(carId), null);
        Log.e(TAG, "GREEN BANNER STATUS" + "------"+msg);
        if (msg != null) {
            txtForSyncInProgress.setText(getString(R.string.msg_syncing_is_in_progress));
            txtForSyncInProgress.setVisibility(View.VISIBLE);
            return true;
        } else {
            txtForSyncInProgress.setVisibility(View.GONE);
            return false;
        }
    }

    /**
     * Used to load offline data from database
     */
    private void loadFromDatabase2() {
        listTripFinal = new ArrayList<TripDetailMain>();
        Log.e("Load Database : ", shouldShowShortTrips + "");
        ArrayList<TripDetailMain> listTrip = TripDetailMain.readSpecificTripsForInitScroll(carId, 1, 10, shouldShowShortTrips);
        Log.e("Init Size : ",listTrip.size()+"");
        if (listTrip.size() > 0) {
            //Making the headers
            listTrip = makeHeadersInListNew(listTrip);
            listTripFinal.addAll(listTrip);
            adapter.setItem(listTripFinal);
            swipeToRefreshLayout.setRefreshing(false);
            uiLoadingHelper.showContent();
        } else {

            uiLoadingHelper.startProgress();
        }
    }

    /**
     * Sort the list based on timestamp, Right now it is not request as list comes from database as sorting order
     *
     * @param listGraphs
     * @return
     */
    private ArrayList<TripDetailMain> sortByDate(ArrayList<TripDetailMain> listGraphs) {
        Collections.sort(listGraphs, new Comparator<TripDetailMain>() {
            @Override
            public int compare(TripDetailMain fruit1, TripDetailMain fruit2) {
                Calendar calendar1 = DateHelper.getCalendarFromServer(fruit1.timestamp);
                calendar1.set(Calendar.SECOND, 0);
                calendar1.set(Calendar.MILLISECOND, 0);
                Calendar calendar2 = DateHelper.getCalendarFromServer(fruit2.timestamp);
                calendar2.set(Calendar.SECOND, 0);
                calendar2.set(Calendar.MILLISECOND, 0);

                //DESC ORDER
                if (calendar1.getTimeInMillis() == calendar2.getTimeInMillis())
                    return 0;
                else if (calendar1.getTimeInMillis() > calendar2.getTimeInMillis())
                    return -1;
                else
                    return 1;
            }
        });
        return listGraphs;
    }
    private String latestHeader = "";

    /**
     * Used to make the headers in the list like today, yesterday, this week, older
     *
     * @param list
     * @return
     */
    private ArrayList<TripDetailMain> makeHeadersInListNew(ArrayList<TripDetailMain> list) {
        Log.e("Headers" , "checking " + list.size());
        //we are removing sort by date because now we are getting from local database
//        list = sortByDate(list);
        ArrayList<TripDetailMain> listHeadersTrip = new ArrayList<>();
        int i = 0;
        for (TripDetailMain trip : list) {
            String header = getGroupedDate(DateHelper.getCalendarFromServer(trip.start_time));
            Log.e("HEaDERS : ", header);
            if (i == 0) {
                if(latestHeader != header)
                {
                    latestHeader = header;
                    listHeadersTrip.add(new TripDetailMain(header));
                }

            } else {
                if (!latestHeader.equals(listHeadersTrip.get(listHeadersTrip.size() - 1).headers)) {
                    latestHeader = header;
                    listHeadersTrip.add(new TripDetailMain(header));
                }else if(latestHeader != header){
                    latestHeader = header;
                    listHeadersTrip.add(new TripDetailMain(header));
                }
            }
            trip.headers = header;
            listHeadersTrip.add(trip);
            i++;
        }
        return listHeadersTrip;
    }


    /**
     * Used to make the headers in the list like today, yesterday, this week, older
     *
     * @param list
     * @return
     */
    private ArrayList<TripDetailMain> makeHeadersInList(ArrayList<TripDetailMain> list) {

        //we are removing sort by date because now we are getting from local database
//        list = sortByDate(list);
        ArrayList<TripDetailMain> listHeadersTrip = new ArrayList<>();
        int i = 0;
        for (TripDetailMain trip : list) {
            String header = getGroupedDate(DateHelper.getCalendarFromServer(trip.timestamp));
            if (i == 0) {
                listHeadersTrip.add(new TripDetailMain(header));
            } else {
                if (!header.equals(listHeadersTrip.get(listHeadersTrip.size() - 1).headers)) {
                    listHeadersTrip.add(new TripDetailMain(header));
                }
            }
            trip.headers = header;
            listHeadersTrip.add(trip);
            i++;
        }
        return listHeadersTrip;
    }

    /**
     * Getting the group category title TODAY, YESTERDAY, THIS WEEK, OLDER based on date
     *
     * @param calendar
     * @return
     */
    private String getGroupedDate(Calendar calendar) {

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (daysBetween(today, calendar) == 0) {
            return "TODAY";
        } else if (daysBetween(today, calendar) == 1) {
            return "YESTERDAY";
        } else if (daysBetween(today, calendar) <= 7) {
            return "LAST 7 DAYS";
        } else {
            return "OLDER";
        }
    }

    /**
     * Returns days between two calendar(dates)
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static long daysBetween(Calendar startDate, Calendar endDate) {
        long end = endDate.getTimeInMillis();
        long start = startDate.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
    }

    /**
     * Return true or false whether two date having same week
     *
     * @param startDate
     * @param endDate
     * @return
     */
    private static boolean isSameWeek(Calendar startDate, Calendar endDate) {
        int weekBetween = endDate.get(Calendar.WEEK_OF_YEAR) - startDate.get(Calendar.WEEK_OF_YEAR);
        //Log.e("WEEK TOH KAR LEIN: ", endDate.get(Calendar.WEEK_OF_YEAR) + " " + startDate.get(Calendar.WEEK_OF_YEAR) + " " + weekBetween );
        if (weekBetween == 0 && startDate.get(Calendar.YEAR) == endDate.get(Calendar.YEAR)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_all_trips, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_calendar) {

            datePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);

        }
        return super.onOptionsItemSelected(item);
    }

    boolean isRegistered = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() called with: " + "context = [" + context + "], intent = [" + intent + "]");
            if (intent.hasExtra(ConstantCode.INTENT_ACTION) && ConstantCode.ACTION_SYNC_COMPLETE.equalsIgnoreCase(intent.getStringExtra(ConstantCode.INTENT_ACTION))) {
                if (intent.hasExtra(ConstantCode.INTENT_CAR_ID)) {
                    if (Integer.parseInt(carId) == intent.getIntExtra(ConstantCode.INTENT_CAR_ID, 0)) {
                        checkForGreenBanner();
                    }
                }
            } else if (intent.hasExtra(ConstantCode.INTENT_ACTION) && ConstantCode.ACTION_SYNC_STARTED.equalsIgnoreCase(intent.getStringExtra(ConstantCode.INTENT_ACTION))) {
                if (intent.hasExtra(ConstantCode.INTENT_CAR_ID)) {
                    if (Integer.parseInt(carId) == intent.getIntExtra(ConstantCode.INTENT_CAR_ID, 0)) {
                        Log.e(TAG,"From BroadCast Reciever");
                        checkForGreenBanner();
                    }
                }
            }
        }
    };

    /**
     * Registering to locale broadcast manager to stay updated with garage api calls so we can update the location and sync time
     */
    private void registerForSyncStatus() {
        if (isRegistered == false) {
            LocalBroadcastManager.getInstance(mActivity).registerReceiver(receiver, new IntentFilter(ConstantCode.BROADCAST_TRIP_SYNCED));
            LocalBroadcastManager.getInstance(mActivity).registerReceiver(receiver, new IntentFilter(ConstantCode.BROADCAST_SHOW_TRIP_SYNC_BANNER));
            isRegistered = true;
        }
    }

    /**
     * Unregistering local broadcast
     */
    private void unregisterForSyncStatus() {
        if (isRegistered == true) {
            LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(receiver);
            isRegistered = false;
        }
    }
    //[END- Registering for the updating car garage changes and sync status and values if last sync or not.]
}
