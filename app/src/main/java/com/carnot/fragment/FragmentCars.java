package com.carnot.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.carnot.App;
import com.carnot.R;
import com.carnot.Services.BLEService;
import com.carnot.activity.ActivityAddNewCarSetup;
import com.carnot.activity.ActivityCarDashboard;
import com.carnot.activity.ActivityCarPassport;
import com.carnot.activity.ActivityDashboard;
import com.carnot.activity.ActivityOnTrip;
import com.carnot.custom_views.AppTextViewLight;
import com.carnot.custom_views.AutoResizeTextView;
import com.carnot.global.AlarmService;
import com.carnot.global.ConstantCode;
import com.carnot.global.PrefManager;
import com.carnot.global.Utility;
import com.carnot.global.gcm.GCMMessage;
import com.carnot.global.gcm.GCMUtils;
import com.carnot.libclasses.BaseFragment;
import com.carnot.libclasses.DateHelper;
import com.carnot.libclasses.PermissionHelper;
import com.carnot.libclasses.googlemaps.GoogleMapUtils;
import com.carnot.models.CarDiagnostics;
import com.carnot.models.Cars;
import com.carnot.models.TripDetailMain;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by pankaj on 31/3/16.
 */
public class FragmentCars extends BaseFragment {

    private final static String TAG = "FragmentCars";
    private AlarmManager alarmManager;

    private LinearLayout dotsLayout;

    private ViewPager pager = null;
    private MainPagerAdapter pagerAdapter = null;

    CardView cardView;
    View shadowView;
    LatLng latLng;
    GoogleMap googleMap;
    ProgressBar progressBar;
    AutoResizeTextView txtStatusTitle;
    TextView txtStatusSubTitle;
    GCMUtils gcmUtils;
    ArrayList<GCMMessage> gcmMessagesList;
    ImageView imgCloseCard;
    private String[] gpsErrorMessage;

    private final static int REQUEST_ENABLE_BT = 1;
    //TODO : Remove this
    //SyncController syncController;

    //For my car says card position in the PagerAdapter
    HashMap<Integer, Integer> carPositionHashMap = new HashMap<>();

    HashMap<Integer, ArrayList<GCMMessage>> gcmNotif = new HashMap<>();

    public FragmentCars() {
        setContentView(R.layout.fragment_cars, false, false);
    }

    @Override
    public void initVariable() {
        PermissionHelper.init(mActivity);
        gpsErrorMessage = getResources().getStringArray(R.array.gps_messages);
        gcmMessagesList = new ArrayList<>();

        gcmUtils = new GCMUtils(mActivity);
        gcmUtils.register(notificationReceiver);
        //TODO : remove this
        //syncController = new SyncController(mActivity);

    }

    public void creatingMyCarSaysCard(Cars particularCar)
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        carPositionHashMap.put(particularCar.id, carPositionHashMap.size());
        FrameLayout v0 = (FrameLayout) inflater.inflate (R.layout.layout_for_mycar_says_card, null);

        txtStatusTitle = (AutoResizeTextView)v0.findViewById(R.id.txt_status_title);
        txtStatusTitle.setText("Hey " + particularCar.name + ", this is where I will talk to you !!");
        pagerAdapter.addView (v0, carPositionHashMap.size());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dotsLayout = (LinearLayout) links(R.id.dots);
        int carCount = -1;
        pagerAdapter = new MainPagerAdapter();
        pager = (ViewPager) view.findViewById (R.id.view_pager);
        pager.setAdapter(pagerAdapter);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        ArrayList<Cars> allCarsList = Cars.readAll();
        if(allCarsList != null) {
            if (allCarsList.size() > 0) {
                for (Cars particularCar : allCarsList) {
                    carCount++;
                    //Log.v("---TAG---",particularCar.id + "");
                    carPositionHashMap.put(particularCar.id, carCount);

                    FrameLayout v0 = (FrameLayout) inflater.inflate(R.layout.layout_for_mycar_says_card, null);
                    ImageView iv = (ImageView)v0.findViewById(R.id.img_close_card_internal);
                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int carIDToDeleteFrom = -1;
                            Log.v("Position in  Adapter : ",pager.getCurrentItem()+"");
                            for (Map.Entry<Integer, Integer> e : carPositionHashMap.entrySet()) {
                                int key = e.getKey();
                                int value = e.getValue();
                                if(value == pager.getCurrentItem())
                                {
                                    carIDToDeleteFrom = key;
                                    Log.v("CarID in  Adapter : ",carIDToDeleteFrom+"");
                                    break;
                                }
                            }
                            if(carIDToDeleteFrom != -1)
                            {
                                if(isComesFromNotification)
                                {
                                    getArguments().remove(ConstantCode.INTENT_MESSAGE);
                                    isComesFromNotification = false;
                                    //updateMyCarSaysCard();
                                    //updateMyCarSaysCardNew(carIDToDeleteFrom);
                                }
                                else
                                {
                                    if(gcmNotif.get(carIDToDeleteFrom).size() > 0)
                                    {
                                        Log.v("FragmentCars : ", "GCM Message From TOP Deleted Successfully");
                                        GCMMessage gcmMessage = gcmNotif.get(carIDToDeleteFrom).get(gcmNotif.get(carIDToDeleteFrom).size()-1);
                                        GCMUtils.cancelNotification(mActivity, gcmMessage.notification_id);
                                        gcmNotif.get(carIDToDeleteFrom).remove(gcmNotif.get(carIDToDeleteFrom).size()-1);
                                        gcmMessage.dismissMessage();
                                    }
                                }

                            }
                            updateMyCarSaysCardNew(carIDToDeleteFrom);
                        }
                    });


                    AppTextViewLight carName = (AppTextViewLight) v0.findViewById(R.id.txt_status_car_name);
                    AppTextViewLight at_subtitile = (AppTextViewLight) v0.findViewById(R.id.txt_status_sub_title);
                    carName.setText(particularCar.name + " says:");
                    txtStatusTitle = (AutoResizeTextView) v0.findViewById(R.id.txt_status_title);
                    if (!Utility.isConnectingToInternet(getActivity())) {
                        txtStatusTitle.setText(getResources().getString(R.string.internet_not_present));
                    } else {
                        txtStatusTitle.setText(getResources().getString(R.string.dummey_welcome, Utility.getLoggedInUser().name));
                    }
                    at_subtitile.setText("#MyCarSays");
                    pagerAdapter.addView(v0, carCount);
                    updateMyCarSaysCardNew(particularCar.id);
                }
            }
        }

        pagerAdapter.notifyDataSetChanged();
        if(dotsLayout!=null)
            addDots(allCarsList.size());
    }

    GCMUtils.NotificationReceive notificationReceiver = new GCMUtils.NotificationReceive() {
        @Override
        public void onNotificationReceive(GCMMessage message) {
            if (isAdded()) {
                Log.v("FragmentCars"," message : "+message.message + " ,carid : "+message.carId);
                if (message.showInMyCarSays) {
                    //TODO : NEWLY ADDED
                    makeNotificationPriorityCarWise(message);
                    updateMyCarSaysCardNew(message.carId);
                    //makeNotificationPriorityWise(message);
                    //updateMyCarSaysCard();
                }
                if (message.showInNotificationBell) {
                    ((ActivityDashboard) mActivity).updateNotificationCounter();
                }
            }
        }

        @Override
        public void onNotificationDismiss(GCMMessage message) {
            //dismiss message if there is it exists
            try {
                if (isAdded())
                {
                    int carIDToDeleteFrom = message.carId;
                    ArrayList<GCMMessage> allIn = gcmNotif.get(carIDToDeleteFrom);
                    if(allIn != null)
                    {
                        if(allIn.size() > 0 )
                        {
                            GCMMessage gcmMessage = gcmNotif.get(carIDToDeleteFrom).get(gcmNotif.get(carIDToDeleteFrom).size()-1);
                            GCMUtils.cancelNotification(mActivity, gcmMessage.notification_id);
                            gcmNotif.get(carIDToDeleteFrom).remove(gcmNotif.get(carIDToDeleteFrom).size()-1);
                            gcmMessage.dismissMessage();
                        }
                    }
                    updateMyCarSaysCardNew(carIDToDeleteFrom);


                    /*
                    if (gcmMessagesList != null) {
                        for (int i = gcmMessagesList.size() - 1; i >= 0; i--) {
                            if (gcmMessagesList.get(i).notification_id == message.notification_id) {
                                gcmMessagesList.get(i).dismissMessage();
                                gcmMessagesList.remove(i);
                                i--;
                                //updateMyCarSaysCard();
                                break;
                            }
                        }
                    }
                    */
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Loading all the notification from database.
     */
    private void loadAllNotificationFromDatabase() {
        ArrayList<GCMMessage> list = GCMMessage.readAllNotificationInASCOrder();
        for (GCMMessage message : list) {
            //TODO : NEWLY ADDED
            makeNotificationPriorityCarWise(message);
            //makeNotificationPriorityWise(message);
        }
        Log.v("{Inside }", "(here a m)");
        ArrayList<Cars> all = Cars.readAll();
        for(Cars oneCar : all)
        {
            updateMyCarSaysCardNew(oneCar.id);
        }

        //updateMyCarSaysCard();
    }

    private List<ImageView> dots;

    public void addDots(final int NUM_PAGES) {
        dots = new ArrayList<>();

        for (int i = 0; i < NUM_PAGES; i++) {

            ImageView dot = new ImageView(getContext());
            dot.setImageDrawable(getResources().getDrawable(R.drawable.circle_purple));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    12,
                    12
            );
            params.setMargins(10,0,0,0);
            dot.setLayoutParams(params);
            dotsLayout.addView(dot);
            dotsLayout.invalidate();
            dots.add(dot);

        }
        if(NUM_PAGES >0)
        {
            pager.setCurrentItem(0,true);
            selectDot(0,NUM_PAGES);
        }
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.v("--TAG--",position + "");
                selectDot(position, NUM_PAGES);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public void selectDot(int idx, int NUM_PAGES) {
        Resources res = getResources();
        for(int i = 0; i < NUM_PAGES; i++) {
            int drawableId = (i==idx)?(R.drawable.circle_purple):(R.drawable.circle_white);
            Drawable drawable = res.getDrawable(drawableId);
            dots.get(i).setImageDrawable(drawable);
        }
    }

    //Logic to make all the notification priority wise, and if necessary then removing it.

    private void makeNotificationPriorityWise(GCMMessage message) {
        //Printing the GCM Message List
        /*
        Log.v("Inserting : ", message.carId + " " + message.priority);
        Log.v("--------------","------------------------------------");
        for(int i = gcmMessagesList.size() -1 ; i>=0; i--)
        {
            Log.v("Printing--", gcmMessagesList.get(i).notification_id + "");
            Log.v("Printing--", gcmMessagesList.get(i).message + "");
            Log.v("Printing--", gcmMessagesList.get(i).priority + "");
            Log.v("Printing--", gcmMessagesList.get(i).sub_priority + "");
            Log.v("Printing--", gcmMessagesList.get(i).carId + "");
        }
        Log.v("--------------","------------------------------------");
        */
        if (message.showInMyCarSays) {
            if (txtStatusTitle != null)
            {
                if (gcmMessagesList.size() > 0)
                {
                    for (int i = gcmMessagesList.size() - 1; i >= 0; i--)
                    {

                        if (gcmMessagesList.get(i).notification_id == message.notification_id) {
                            break;
                        }

                        if (message.priority > gcmMessagesList.get(i).priority) {

                            //Checking if this notification says it can dismiss when notification comes
                            if (gcmMessagesList.get(i).dismissCarSays || gcmMessagesList.get(i).dismissCardAppears) {
                                gcmMessagesList.get(i).dismissMessage();
                                gcmMessagesList.remove(i);
                                i--;
                            }

                            if (gcmMessagesList.size() > i + 1) {
                                gcmMessagesList.add(i + 1, message);
                                break;
                            } else {
                                gcmMessagesList.add(message);
                                break;
                            }
                        } else if (message.priority < gcmMessagesList.get(i).priority) {
                            if (i == 0) {
                                gcmMessagesList.add(0, message);
                                break;
                            }
                            continue;
                        } else {
                            //Checking if this notification says it can dismiss when notification comes
                            if (message.sub_priority > gcmMessagesList.get(i).sub_priority) {
                                if (gcmMessagesList.get(i).dismissCarSays || gcmMessagesList.get(i).dismissCardAppears) {
                                    gcmMessagesList.get(i).dismissMessage();
                                    gcmMessagesList.remove(i);
                                    i--;
                                }
                                gcmMessagesList.add(i + 1, message);
                                break;
                            } else if (message.sub_priority < gcmMessagesList.get(i).sub_priority) {
                                if (i == 0) {
                                    gcmMessagesList.add(0, message);
                                    break;
                                }
                                continue;
                            } else {
                                if (gcmMessagesList.get(i).dismissCarSays || gcmMessagesList.get(i).dismissCardAppears) {
                                    gcmMessagesList.get(i).dismissMessage();
                                    gcmMessagesList.remove(i);
                                    i--;
                                }
                                gcmMessagesList.add(i + 1, message);
                                break;
                            }
                        }
                    }
                }
                else {
                    gcmMessagesList.add(message);
                }
            }
        }
        /*
        Log.v("Inserting : ", message.carId + " " + message.priority);
        Log.v("--------------","------------------------------------");
        for(int i = gcmMessagesList.size() -1 ; i>=0; i--)
        {
            Log.v("Printing--", gcmMessagesList.get(i).notification_id + "");
            Log.v("Printing--", gcmMessagesList.get(i).message + "");
            Log.v("Printing--", gcmMessagesList.get(i).priority + "");
            Log.v("Printing--", gcmMessagesList.get(i).sub_priority + "");
            Log.v("Printing--", gcmMessagesList.get(i).carId + "");
        }
        Log.v("--------------","------------------------------------");
        */
    }


    private void makeNotificationPriorityCarWise(GCMMessage message) {
        Log.v("TAGIING : ----",message.carId + " " + message.priority + " " + message.sub_priority + " " + message.message);
        ArrayList<GCMMessage> listOfGCMMessage;
        Log.v("-------------",message.carId + "");
        int carID = message.carId;
        if(gcmNotif.get(carID)!=null)
        {
            listOfGCMMessage = gcmNotif.get(carID);
        }
        else
        {
            gcmNotif.put(carID, new ArrayList<GCMMessage>());
            listOfGCMMessage = gcmNotif.get(carID);
        }
        if(listOfGCMMessage != null)
        {
            if (message.showInMyCarSays) {
                if (listOfGCMMessage.size() > 0)
                {
                    for (int i = listOfGCMMessage.size() - 1; i >= 0; i--)
                    {
                        if (listOfGCMMessage.get(i).notification_id == message.notification_id) {
                            break;
                        }

                        if (message.priority > listOfGCMMessage.get(i).priority) {

                            //Checking if this notification says it can dismiss when notification comes
                            if (listOfGCMMessage.get(i).dismissCarSays || listOfGCMMessage.get(i).dismissCardAppears) {
                                listOfGCMMessage.get(i).dismissMessage();
                                listOfGCMMessage.remove(i);
                                i--;
                            }

                            if (listOfGCMMessage.size() > i + 1) {
                                listOfGCMMessage.add(i + 1, message);
                                break;
                            } else {
                                listOfGCMMessage.add(message);
                                break;
                            }
                        } else if (message.priority < listOfGCMMessage.get(i).priority) {
                            if (i == 0) {
                                listOfGCMMessage.add(0, message);
                                break;
                            }
                            continue;
                        } else {
                            //Checking if this notification says it can dismiss when notification comes
                            if (message.sub_priority > listOfGCMMessage.get(i).sub_priority) {
                                if (listOfGCMMessage.get(i).dismissCarSays || listOfGCMMessage.get(i).dismissCardAppears) {
                                    listOfGCMMessage.get(i).dismissMessage();
                                    listOfGCMMessage.remove(i);
                                    i--;
                                }
                                listOfGCMMessage.add(i + 1, message);
                                break;
                            } else if (message.sub_priority < listOfGCMMessage.get(i).sub_priority) {
                                if (i == 0) {
                                    listOfGCMMessage.add(0, message);
                                    break;
                                }
                                continue;
                            } else {
                                if (listOfGCMMessage.get(i).dismissCarSays || listOfGCMMessage.get(i).dismissCardAppears) {
                                    listOfGCMMessage.get(i).dismissMessage();
                                    listOfGCMMessage.remove(i);
                                    i--;
                                }
                                listOfGCMMessage.add(i + 1, message);
                                break;
                            }
                        }
                    }
                }
                else {
                    listOfGCMMessage.add(message);
                }
            }
        }

        /*
        Log.v("Inserting : ", message.carId + " " + message.priority);
        Log.v("--------------","------------------------------------");
        for(int i = gcmMessagesList.size() -1 ; i>=0; i--)
        {
            Log.v("Printing--", gcmMessagesList.get(i).notification_id + "");
            Log.v("Printing--", gcmMessagesList.get(i).message + "");
            Log.v("Printing--", gcmMessagesList.get(i).priority + "");
            Log.v("Printing--", gcmMessagesList.get(i).sub_priority + "");
            Log.v("Printing--", gcmMessagesList.get(i).carId + "");
        }
        Log.v("--------------","------------------------------------");
        */
    }



    boolean isComesFromNotification;

    private SpannableStringBuilder addClickablePart(String str) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(str);

        int idx1 = str.indexOf("[");
        int idx2 = str.indexOf("]");

        final String clickString1 = str.substring(idx1+1, idx2-1);
        ssb.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(ConstantCode.CONTACT_FOR_HELP));
                startActivity(intent);
            }
        }, idx1, idx2, 0);

        ssb.delete(str.indexOf("["),str.indexOf("[")+1);
        ssb.delete(str.indexOf("]")-1,str.indexOf("]"));

        return ssb;
    }

    private SpannableStringBuilder addClickablePartForOnTrip(String str) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(str);

        int idx1 = str.indexOf("[");
        int idx2 = str.indexOf("]");

        final String clickString1 = str.substring(idx1+1, idx2-1);
        ssb.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(getContext().getApplicationContext(), ActivityOnTrip.class);
                startActivity(intent);
            }
        }, idx1, idx2, 0);

        ssb.delete(str.indexOf("["),str.indexOf("[")+1);
        ssb.delete(str.indexOf("]")-1,str.indexOf("]"));

        return ssb;
    }

    private SpannableStringBuilder addClickablePartForBluetoothOn(String str) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(str);

        int idx1 = str.indexOf("[");
        int idx2 = str.indexOf("]");

        final String clickString1 = str.substring(idx1+1, idx2-1);
        ssb.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }, idx1, idx2, 0);

        ssb.delete(str.indexOf("["),str.indexOf("[")+1);
        ssb.delete(str.indexOf("]")-1,str.indexOf("]"));

        return ssb;
    }

    private SpannableStringBuilder addClickablePartForLocOn(String str) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(str);

        int idx1 = str.indexOf("[");
        int idx2 = str.indexOf("]");

        final String clickString1 = str.substring(idx1+1, idx2-1);
        ssb.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        }, idx1, idx2, 0);

        ssb.delete(str.indexOf("["),str.indexOf("[")+1);
        ssb.delete(str.indexOf("]")-1,str.indexOf("]"));

        return ssb;
    }

    /**
     * Updating mycarsays
     */
    public void updateMyCarSaysCard() {

        //If there is some message in locatl stack then show it from that else show default message ie. welcome <>, its nice...

        if (gcmMessagesList.size() > 0) {
            final GCMMessage gcmMessage = gcmMessagesList.get(gcmMessagesList.size() - 1);
            txtStatusTitle.setText(gcmMessage.message);
            txtStatusTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
            txtStatusTitle.resetTextSize();
            txtStatusSubTitle.setText(gcmMessage.title);
            imgCloseCard.setVisibility(View.VISIBLE);
            ((View) txtStatusTitle.getParent()).setBackgroundResource(Utility.getStatusBackground(gcmMessage.category));
            txtStatusTitle.setTextColor(Utility.getColor(mActivity, Utility.getStatusTextColor(gcmMessage.category)));

            if(gcmMessage.notification_id == ConstantCode.FIFTEEN_MIN_TIMER_COMPLETED_AND_NO_GPS_PRESENT - gcmMessage.carId)
            {
                String myString = String.format(getString(R.string.after_fifteen_minute_timer), Utility.getLoggedInUser().name);
                int i1 = myString.indexOf("[");
                int i2 = myString.indexOf("]");
                txtStatusTitle.setMovementMethod(LinkMovementMethod.getInstance());
                txtStatusTitle.setText(addClickablePart(myString), TextView.BufferType.SPANNABLE);
            }
            if(gcmMessage.notification_id == ConstantCode.UPDATE_CAR_INFO_MY_CAR_SAYS_CARD - gcmMessage.carId)
            {
                txtStatusTitle.setClickable(true);
                txtStatusTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cars car = Cars.readSpecific(String.valueOf(gcmMessage.carId));
                        Log.v("CHECKING CAR ID ",car.id + "");
                        Intent i = new Intent(mActivity, ActivityCarPassport.class);
                        i.putExtra(ConstantCode.INTENT_CAR_ID, String.valueOf(car.id));
                        i.putExtra(ConstantCode.INTENT_CAR_NAME, car.name);
                        i.putExtra(ConstantCode.INTENT_CAR_STATUS, car.isOnTrip);
                        startActivity(i);
                    }
                });
            }
        } else {
            String txt = String.format(getString(R.string.dummey_welcome),Utility.getLoggedInUser().name);
            txtStatusTitle.setText(txt);
            txtStatusTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
            txtStatusTitle.resetTextSize();
            txtStatusSubTitle.setText("");
            imgCloseCard.setVisibility(View.GONE);
            ((View) txtStatusTitle.getParent()).setBackgroundResource(Utility.getStatusBackground("default"));
            txtStatusTitle.setTextColor(Utility.getColor(mActivity, Utility.getStatusTextColor("default")));
        }

        //Used to forcefully show the message when it comes from notification tray
        if (getArguments() != null && getArguments().containsKey(ConstantCode.INTENT_MESSAGE)) {
            txtStatusTitle.setText(getArguments().getString(ConstantCode.INTENT_MESSAGE));
            txtStatusTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
            txtStatusTitle.resetTextSize();
            txtStatusSubTitle.setText(getArguments().getString(ConstantCode.INTENT_TITLE));
            imgCloseCard.setVisibility(View.VISIBLE);
            isComesFromNotification = true;
        }
//        txtStatusTitle.setText("Lorem Ipsum dollar sit amet, Pankaj sharma openxcell developer kalpesh chandani, ronak abcdef anurag");
    }

    private SpannableStringBuilder addClickablePartForLocation(String str) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(str);

        int idx1 = str.indexOf("[");
        int idx2 = str.indexOf("]");
        /*
        int idx1 = str.indexOf("[");
        int idx2 = 0;
        while (idx1 != -1) {
            idx2 = str.indexOf("]", idx1) + 1;
            final String clickString = str.substring(idx1+1, idx2-1);
            ssb.setSpan(new ClickableSpan() {

                @Override
                public void onClick(View widget) {
                    Toast.makeText(widget.getContext(), clickString,
                            Toast.LENGTH_SHORT).show();
                }
            }, idx1, idx2, 0);
            idx1 = str.indexOf("[", idx2);
        }
        */
        final String clickString = str.substring(idx1+1, idx2-1);
        ssb.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        }, idx1, idx2, 0);

        idx1 = str.indexOf("{");
        idx2 = str.indexOf("}");

        final String clickString1 = str.substring(idx1+1, idx2-1);
        ssb.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }, idx1, idx2, 0);

        ssb.delete(str.indexOf("["),str.indexOf("[")+1);
        ssb.delete(str.indexOf("]")-1,str.indexOf("]"));

        ssb.delete(str.indexOf("{"),str.indexOf("{")+1);
        ssb.delete(str.indexOf("}")-1,str.indexOf("}"));

        return ssb;
    }


    public void updateMyCarSaysCardNew(int carID) {
        //If there is some message in locatl stack then show it from that else show default message ie. welcome <>, its nice...
        View v;
        Log.v("CAR ID : ",carID + "");
        if(carID != -1)
        {
            if(pagerAdapter != null)
            {
                if(carPositionHashMap.get(carID) == null)
                {
                    Log.v("Adding entry into", carID + "");
                    Log.v("Creating :", "My car syay card !!");
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    Cars particularCar = Cars.readSpecific(String.valueOf(carID));
                    if(particularCar != null)
                    {
                        carPositionHashMap.put(particularCar.id, carPositionHashMap.size());
                        FrameLayout v0 = (FrameLayout) inflater.inflate (R.layout.layout_for_mycar_says_card, null);

                        ImageView iv = (ImageView)v0.findViewById(R.id.img_close_card_internal);
                        iv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int carIDToDeleteFrom = -1;
                                Log.v("Position in  Adapter : ",pager.getCurrentItem()+"");
                                for (Map.Entry<Integer, Integer> e : carPositionHashMap.entrySet()) {
                                    int key = e.getKey();
                                    int value = e.getValue();
                                    if(value == pager.getCurrentItem())
                                    {
                                        carIDToDeleteFrom = key;
                                        Log.v("CarID in  Adapter : ",carIDToDeleteFrom+"");
                                        break;
                                    }
                                }
                                if(carIDToDeleteFrom != -1)
                                {
                                    if(isComesFromNotification)
                                    {
                                        getArguments().remove(ConstantCode.INTENT_MESSAGE);
                                        isComesFromNotification = false;
                                        //updateMyCarSaysCard();
                                        //updateMyCarSaysCardNew(carIDToDeleteFrom);
                                    }
                                    else
                                    {
                                        if(gcmNotif.get(carIDToDeleteFrom).size() > 0)
                                        {
                                            Log.v("FragmentCars : ", "GCM Message From TOP Deleted Successfully");
                                            GCMMessage gcmMessage = gcmNotif.get(carIDToDeleteFrom).get(gcmNotif.get(carIDToDeleteFrom).size()-1);
                                            GCMUtils.cancelNotification(mActivity, gcmMessage.notification_id);
                                            gcmNotif.get(carIDToDeleteFrom).remove(gcmNotif.get(carIDToDeleteFrom).size()-1);
                                            gcmMessage.dismissMessage();
                                        }
                                    }
                                }
                                updateMyCarSaysCardNew(carIDToDeleteFrom);
                            }
                        });

                        txtStatusTitle = (AutoResizeTextView)v0.findViewById(R.id.txt_status_title);
                        txtStatusTitle.setText("Hey " + particularCar.name + ", this is where I will talk to you !!");
                        pagerAdapter.addView (v0, carPositionHashMap.size()-1);

                        pagerAdapter.notifyDataSetChanged();
                        pager.setCurrentItem(carPositionHashMap.size()-1,true);
                        ImageView dot = new ImageView(getContext());
                        dot.setImageDrawable(getResources().getDrawable(R.drawable.circle_greyish_white));

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                12,
                                12
                        );
                        params.setMargins(10,0,0,0);
                        dot.setLayoutParams(params);
                        dotsLayout.addView(dot);
                        dotsLayout.invalidate();
                        dots.add(dot);
                    }
                }

                v = pagerAdapter.getView(carPositionHashMap.get(carID));
                AutoResizeTextView at = (AutoResizeTextView)v.findViewById(R.id.txt_status_title);
                AppTextViewLight at_subtitile  = (AppTextViewLight) v.findViewById(R.id.txt_status_sub_title);
                AppTextViewLight carName = (AppTextViewLight) v.findViewById(R.id.txt_status_car_name);
                ImageView iv = (ImageView)v.findViewById(R.id.img_close_card_internal);

                if(gcmNotif.get(carID) != null)
                {

                    if (gcmNotif.get(carID).size() > 0) {

                        final GCMMessage gcmMessage = gcmNotif.get(carID).get(gcmNotif.get(carID).size() - 1);
                        Cars forCarName = Cars.readSpecific(String.valueOf(carID));
                        if(forCarName.name != null)
                        {

                            carName.setText(forCarName.name + " says :\n\n");
                            //at.setText(forCarName.name + " says : \n \n" + gcmMessage.message);
                        }
                        else
                        {
                            carName.setText("Your Car Says , \n\n");
                            //at.setText("Your Car Says , \n \n" + gcmMessage.message);
                        }
                        at.setText(gcmMessage.message);
                        at.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
                        at.resetTextSize();
                        at_subtitile.setText("#MyCarSays");
                        if(!gcmMessage.isDismissed)
                        {
                            iv.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            iv.setVisibility(View.GONE);
                        }
                        if(gcmMessage.priority == 1 && (gcmMessage.sub_priority == 0 || gcmMessage.sub_priority == 5
                                || gcmMessage.sub_priority == 7 || gcmMessage.sub_priority == 10
                                || gcmMessage.sub_priority == 6 || gcmMessage.sub_priority == 3 || gcmMessage.sub_priority == 4))
                        {
                            //Log.e("TAg","Enterred Here");
                            iv.setVisibility(View.GONE);
                        }
                        //TODO : Close image View removed currently as there is no my car says which needs to be removed.
                        //For later use uncomment this
                        /*
                        if(gcmMessage.notification_id == ConstantCode.GPS_MESSAGE - gcmMessage.carId
                                || gcmMessage.notification_id == ConstantCode.NO_GI_REGULAR - gcmMessage.carId)
                        {
                            iv.setVisibility(View.GONE);
                        }
                        else
                        {
                            iv.setVisibility(View.VISIBLE);
                        }
                        */
                        ((View) at.getParent()).setBackgroundResource(Utility.getStatusBackground(gcmMessage.category));
                        at.setTextColor(Utility.getColor(mActivity, Utility.getStatusTextColor(gcmMessage.category)));


                        if(gcmMessage.notification_id == ConstantCode.NO_GI_FIRST_TIME - gcmMessage.carId)
                        {
                            String myString = String.format(getString(R.string.after_fifteen_minute_timer), Utility.getLoggedInUser().name);
                            int i1 = myString.indexOf("[");
                            int i2 = myString.indexOf("]");
                            at.setMovementMethod(LinkMovementMethod.getInstance());
                            at.setText(addClickablePart(myString), TextView.BufferType.SPANNABLE);
                        }
                        if(gcmMessage.notification_id == ConstantCode.FIFTEEN_MIN_TIMER_COMPLETED_AND_NO_GPS_PRESENT - gcmMessage.carId)
                        {
                            String myString = String.format(getString(R.string.after_fifteen_minute_timer), Utility.getLoggedInUser().name);
                            int i1 = myString.indexOf("[");
                            int i2 = myString.indexOf("]");
                            at.setMovementMethod(LinkMovementMethod.getInstance());
                            at.setText(addClickablePart(myString), TextView.BufferType.SPANNABLE);
                        }
                        if(gcmMessage.notification_id == ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP_BLE_CONNECTED - gcmMessage.carId)
                        {
                            //need to change message here :
                            String myString = String.format(getString(R.string.my_car_says_card_for_on_trip_status_ble_connectd));
                            int i1 = myString.indexOf("[");
                            int i2 = myString.indexOf("]");
                            at.setMovementMethod(LinkMovementMethod.getInstance());
                            at.setText(addClickablePartForOnTrip(myString), TextView.BufferType.SPANNABLE);
                        }
                        if(gcmMessage.notification_id == ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP - gcmMessage.carId)
                        {
                            LocationManager lm = (LocationManager)getActivity().getApplication().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (mBluetoothAdapter == null) {
                                //Does not support Bluetooth
                            } else {
                                if (!mBluetoothAdapter.isEnabled()) {
                                    if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                                    {
                                        //start gps + bluetooth permission dialogue
                                        String myString = mActivity.getString(R.string.my_car_says_card_for_on_trip_status_with_no_location);
                                        at.setMovementMethod(LinkMovementMethod.getInstance());
                                        at.setText(addClickablePartForLocation(myString), TextView.BufferType.SPANNABLE);
                                    }
                                    else
                                    {
                                        //start only bluetooth
                                        String myString = String.format(getString(R.string.my_car_says_card_for_on_trip_status));
                                        int i1 = myString.indexOf("[");
                                        int i2 = myString.indexOf("]");
                                        at.setMovementMethod(LinkMovementMethod.getInstance());
                                        at.setText(addClickablePartForBluetoothOn(myString), TextView.BufferType.SPANNABLE);
                                    }

                                }
                                else
                                {
                                    if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                                    {
                                        //start gps permission dialogue
                                        String myString = String.format(getString(R.string.my_car_says_card_for_on_trip_status_enable_loc));
                                        int i1 = myString.indexOf("[");
                                        int i2 = myString.indexOf("]");
                                        at.setMovementMethod(LinkMovementMethod.getInstance());
                                        at.setText(addClickablePartForLocOn(myString), TextView.BufferType.SPANNABLE);
                                    }
                                    else
                                    {
                                        //start no one
                                        at.setText(getString(R.string.my_car_says_card_for_no_device_near));
                                    }

                                }
                            }
                            //need to change message here :

                        }
                        if(gcmMessage.notification_id == ConstantCode.UPDATE_CAR_INFO_MY_CAR_SAYS_CARD - gcmMessage.carId)
                        {
                            at.setClickable(true);
                            at.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Cars car = Cars.readSpecific(String.valueOf(gcmMessage.carId));
                                    Intent i = new Intent(mActivity, ActivityCarPassport.class);
                                    i.putExtra(ConstantCode.INTENT_CAR_ID, String.valueOf(car.id));
                                    i.putExtra(ConstantCode.INTENT_CAR_NAME, car.name);
                                    i.putExtra(ConstantCode.INTENT_CAR_STATUS, car.isOnTrip);
                                    startActivity(i);
                                }
                            });
                        }
                        Log.v("INSIDE UPDATE CARD",gcmMessage.message);
                    }
                    else
                    {
                        String txt = String.format(getString(R.string.dummey_welcome),Utility.getLoggedInUser().name);
                        at.setText(txt);
                        at.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
                        at.resetTextSize();
                        at.setVisibility(View.GONE);
                        ((View) at.getParent()).setBackgroundResource(Utility.getStatusBackground("default"));
                        at.setTextColor(Utility.getColor(mActivity, Utility.getStatusTextColor("default")));
                        //Log.e("INSIDE UPDATE CARD : ",carID + txt);
                    }
                }
                //Used to forcefully show the message when it comes from notification tray
                if (getArguments() != null && getArguments().containsKey(ConstantCode.INTENT_MESSAGE)) {
                    Log.v("FragmentCars : ", getArguments().getString(ConstantCode.INTENT_DATA));
                    try{
                        JSONObject job = new JSONObject(getArguments().getString(ConstantCode.INTENT_DATA));
                        Log.v("FragmentCars : ",job.getBoolean("showInMyCarSays") + "");
                        if(job.getBoolean("showInMyCarSays"))
                        {
                            if(pagerAdapter!=null)
                            {
                                if(carPositionHashMap.get(job.getInt("carId")) != null)
                                {
                                    View v1 = pagerAdapter.getView(carPositionHashMap.get(job.getInt("carId")));
                                    if(v1 != null)
                                    {
                                        AutoResizeTextView at_1 = (AutoResizeTextView)v1.findViewById(R.id.txt_status_title);
                                        AppTextViewLight at_subtitile_1  = (AppTextViewLight) v1.findViewById(R.id.txt_status_sub_title);
                                        AppTextViewLight carName_1 = (AppTextViewLight) v1.findViewById(R.id.txt_status_car_name);
                                        ImageView iv_1 = (ImageView)v1.findViewById(R.id.img_close_card_internal);

                                        Cars forCarName = Cars.readSpecific(String.valueOf(job.getInt("carId")));
                                        if(forCarName.name != null)
                                        {

                                            carName.setText(forCarName.name + " says :\n\n");
                                            //at.setText(forCarName.name + " says : \n \n" + gcmMessage.message);
                                        }
                                        else
                                        {
                                            Log.v("TAG 5" , "yo");
                                            carName.setText("Your Car Says , \n\n");
                                            //at.setText("Your Car Says , \n \n" + gcmMessage.message);
                                        }

                                        at.setText(getArguments().getString(ConstantCode.INTENT_MESSAGE));
                                        at.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
                                        at.resetTextSize();
                                        at_subtitile.setText("#MyCarSays");
                                        iv_1.setVisibility(View.VISIBLE);
                                        ((View) at.getParent()).setBackgroundResource(Utility.getStatusBackground(job.getString("category")));
                                        at.setTextColor(Utility.getColor(mActivity, Utility.getStatusTextColor(job.getString("category"))));
                                        isComesFromNotification = true;
                                    }
                                }
                            }
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }


                }
                //pagerAdapter.notifyDataSetChanged();

            }

        }
        //TODO : Used to forcefully show the message when it comes from notification tray
    }


    @Override
    public void initView() {
        initMaps();

        progressBar = (ProgressBar) links(R.id.progress_bar);
        cardView = (CardView) links(R.id.card_view);
        shadowView = (View) links(R.id.shadow_view);
        //txtStatusTitle = (AutoResizeTextView) links(R.id.txt_status_title);
        //txtStatusSubTitle = (TextView) links(R.id.txt_status_sub_title);

        //imgCloseCard = (ImageView) links(R.id.img_close_card);
        cardView.setMaxCardElevation(0);
    }

    @Override
    public void addAdapter() {
        /*
        imgCloseCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Checking this notification comes from notification tray then simply discard it
                if (isComesFromNotification) {
                    getArguments().remove(ConstantCode.INTENT_MESSAGE);
                    isComesFromNotification = false;
                    //updateMyCarSaysCard();
                } else {
                    if(gcmMessagesList.size()-1 >= 0)
                    {
                        //Update the top certifications in database as dismiss = true, and simply removing it from notification
                        GCMMessage gcmMessage = gcmMessagesList.get(gcmMessagesList.size() - 1);
                        GCMUtils.cancelNotification(mActivity, gcmMessage.notification_id);
                        gcmMessagesList.remove(gcmMessagesList.size() - 1);
                        gcmMessage.dismissMessage();
                        //updateMyCarSaysCard();
                    }
                }
            }
        });
        */
    }

    @Override
    public void postInitView() {
        super.postInitView();

        //updateMyCarSaysCard();


        /**
         * Here, we write a condition because for lolipop and above card view automatically show shadow and for pre lolipop devices we show view
         */
        if (Utility.isAndroidAPILevelGreaterThenEqual(Build.VERSION_CODES.LOLLIPOP)) {
            shadowView.setVisibility(View.GONE);
        } else {
            shadowView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void loadData() {
        Bundle bnd = getArguments();
        listCars = Cars.readAll();
        if (!(bnd != null && bnd.containsKey(ConstantCode.INTENT_ACTION) && ConstantCode.ACTION_REGISTER_CAR.equalsIgnoreCase(bnd.getString(ConstantCode.INTENT_ACTION)))) {
            loadMapData();
        }

        if (listCars.size() > 0) {
            //TODO : why this check is required
            if (listCars.get(0).lat != null)
                addMarkers(listCars);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }

        loadAllNotificationFromDatabase();

    }

    boolean goingToAddFirstCar = false;

    /**
     * Read all the local cars with there recent trip id so we can compare that we need to sync or not.
     *
     * @return
     */
    private HashMap<Integer, Integer> loadRecentTripIdFromOurDatabase() {
        return TripDetailMain.readAllWithRecentTrips();
    }

    Intent broadcast;
    boolean isGPSMessageThere = false;
//    int counter;

    /**
     * Used to get the car information from server and populating the google map markers
     */
    private void loadMapData() {

        String id = ConstantCode.DEFAULT_USER_ID;
        if (Utility.getLoggedInUser() != null)
        {
            id = Utility.getLoggedInUser().id + "";

            //TODO : Check alarm
            ArrayList<Cars> listOfCars = Cars.readAll();
            if(listOfCars != null)
            {
                if(listOfCars.size() != 0)
                {
                    /*
                    for(Cars carHere : listOfCars)
                    {
                        if(PrefManager.getInstance().contains(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carHere.id)))
                        {
                            if(PrefManager.getInstance().getBoolean(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carHere.id), false))
                            {
                                PrefManager.getInstance().edit().remove(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carHere.id)).commit();
                                GCMMessage messageGPS = new GCMMessage();
                                messageGPS.notification_id = ConstantCode.FIFTEEN_MIN_TIMER_COMPLETED_AND_NO_GPS_PRESENT - carHere.id;
                                messageGPS.carId = carHere.id;
                                messageGPS.showInMyCarSays = true;
                                messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                                messageGPS.priority = 1;
                                messageGPS.sub_priority = 5;
                                messageGPS.message = getString(R.string.after_fifteen_minute_timer, Utility.getLoggedInUser().name);
                                //TODO: ADDED NEWLY
                                makeNotificationPriorityCarWise(messageGPS);
                                //makeNotificationPriorityWise(messageGPS);
                                updateMyCarSaysCardNew(carHere.id);
                                //updateMyCarSaysCard();
                            }
                            else
                            {
                                PrefManager.getInstance().edit().remove(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carHere.id)).commit();
                            }
                        }
                    }
                    */
                }
            }
        }

        WebUtils.call(WebServiceConfig.WebService.GARAGE, new String[]{id}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                if(progressBar != null)
                    progressBar.setVisibility(View.GONE);
                JSONObject json = (JSONObject) values;

                //Sending broadcast to update car dashboard screen
                if (broadcast == null)
                    broadcast = new Intent(ConstantCode.BROADCAST_TRIP_SYNCED);
                broadcast.putExtra(ConstantCode.INTENT_DATA, values.toString());
                broadcast.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_GARAGE_UPDATED);
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(broadcast);

                //TODO : COMMENT THIS OUT FINALLY
                //Loading latest trip id from database so that we can compare if from while syncing
                //HashMap<Integer, Integer> recentTrips = loadRecentTripIdFromOurDatabase();

                listCars = Utility.parseArrayFromString(json.optString(ConstantCode.data), Cars[].class);

                if (listCars.size() > 0)
                {
                    for(Cars c : listCars) {
                        removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP, c.id);
                        removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP_BLE_CONNECTED, c.id);
                        removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_REGULAR, c.id);
                        removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_FOR_NO_INTERNET, c.id);
                    }
                    addMarkers(listCars);
                    loadDiagnosis(listCars);
                    //Saving data in Database

                    int finalLock = 1;

                    for (Cars listCar : listCars)
                    {

                        //Log.v(TAG, "KEEP YPUR EYES HERE : " + listCar.lock + " " + listCar.id);
                        if (Utility.getLoggedInUser() != null) {
                            //TODO : update latest trip ID into Server trip id column of respective cars
                            //Log.v("TAG","-----Our function called");
                            Cars carCheck = Cars.readSpecific(String.valueOf(listCar.id));
                            /*
                            if(carCheck == null)
                            {
                                isCarsNotEnterBefore = true;
                            }

                            if(isCarsNotEnterBefore)
                            {
                                Cars.updateFlagParameters(listCar.id+ "", true, false);
                            }
                            */
                            listCar.updateSelectedValuesFromGarageApi();
                            //updating fields in local database
                            //TODO : remove this from here and put it under ActivityAllTrips.java file
                            //getting the recent trips from server if any
                            //getRecentTripsDetail(listCar.id, listCar.latest_trip, recentTrips.get(listCar.id) != null ? recentTrips.get(listCar.id) : 0, listCar.flag);

                            //Checking the gps message no use of this
                            //TODO : no use of this :P , be smart dont be like BILL
                            finalLock *= listCar.lock;

                            Intent intentP = new Intent(App.getContext(), AlarmService.class);
                            intentP.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_TIMER_COMPLETED);
                            intentP.putExtra(ConstantCode.INTENT_CAR_ID, listCar.id);
                            PendingIntent toBeCancel = PendingIntent.getBroadcast(App.getContext(), listCar.id, intentP, 0);


                            boolean alarmUp = (PendingIntent.getBroadcast(App.getContext(), listCar.id,
                                    intentP,
                                    PendingIntent.FLAG_NO_CREATE) != null);


                            //TODO : adding my car says card logic for newly added car
                            final Cars carFromLocalDB = Cars.readSpecific(String.valueOf(listCar.id));
                            if(carFromLocalDB == null)
                            {
                                Log.v(TAG, "carFromLocalDb = null");
                            }
                            removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE ,carFromLocalDB.id);
                            //Log.v("##### The Welcome #####",carFromLocalDB.isFirstTimeEntry + "");
                            if(carFromLocalDB.isFirstTimeEntry)
                            {
                                Log.v(TAG, "FIRST TIME_____" );
                                if(!carFromLocalDB.isTimerStarted)
                                {
                                    Log.v(TAG, "Timer started for " + listCar.id);
                                    if (alarmManager == null)
                                        alarmManager = (AlarmManager) App.getContext().getSystemService(Context.ALARM_SERVICE);

                                    Intent intentPendingIntent = new Intent(App.getContext(), AlarmService.class);
                                    intentPendingIntent.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_TIMER_COMPLETED);
                                    intentPendingIntent.putExtra(ConstantCode.INTENT_CAR_ID, carFromLocalDB.id);
                                    PendingIntent pi = PendingIntent.getBroadcast(App.getContext(), carFromLocalDB.id, intentPendingIntent, 0);

                                    Calendar calendarToday = Calendar.getInstance();
                                    calendarToday.add(Calendar.MINUTE, 2);
                                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendarToday.getTimeInMillis(), pi);
                                    carFromLocalDB.isTimerStarted = true;
                                    Cars.updateIsTimerStarted(String.valueOf(carFromLocalDB.id),true);
                                }

                                //isThisPreviousWelcomeMessage == -1
                                //indicates carFromLocalDB.lut == "p'j's Birthday
                                //TODO : change this to something default value in future for this card to appear for the time being this doesnt function.
                                //for the time beinfg let it be but cahnge if condition to carFromLocalDB.lut == "PJ's Birthday"
                                Log.v("TAG!!!!!!!!!!!",carFromLocalDB.lock + "");
                                if(carFromLocalDB.lut.equals("00:00:00"))
                                {
                                    /*
                                    removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT ,carFromLocalDB.id);
                                    removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_FIRST_TIME, carFromLocalDB.id);
                                    */
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT ,carFromLocalDB.id);
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_FIRST_TIME, carFromLocalDB.id);
                                    Log.v("{INSIDE}" , "LUT");
                                    //if(!isCurrentNotificationIdPresent(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, carFromLocalDB.id))
                                    if(!isCurrentNotificationIdPresentNew(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, carFromLocalDB.id))
                                    {
                                        GCMMessage messageGPS = new GCMMessage();
                                        messageGPS.notification_id = ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM - carFromLocalDB.id;
                                        messageGPS.showInMyCarSays = true;
                                        messageGPS.carId = carFromLocalDB.id;
                                        messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                                        messageGPS.priority = 1;
                                        messageGPS.sub_priority = 5;
                                        messageGPS.message = getString(R.string.my_car_says_first_time_LUT,Utility.getLoggedInUser().name);
                                        //TODO: ADDED NEWLY
                                        // makeNotificationPriorityWise(messageGPS);
                                        makeNotificationPriorityCarWise(messageGPS);
                                        updateMyCarSaysCardNew(carFromLocalDB.id);
                                        //updateMyCarSaysCard();
                                    }
                                    if(PrefManager.getInstance().contains(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carFromLocalDB.id)))
                                    {
                                        if(PrefManager.getInstance().getBoolean(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carFromLocalDB.id), false))
                                        {
                                            GCMMessage messageGPS = new GCMMessage();
                                            messageGPS.notification_id = ConstantCode.FIFTEEN_MIN_TIMER_COMPLETED_AND_NO_GPS_PRESENT - carFromLocalDB.id;
                                            messageGPS.carId = carFromLocalDB.id;
                                            messageGPS.showInMyCarSays = true;
                                            messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                                            messageGPS.priority = 1;
                                            messageGPS.sub_priority = 5;
                                            messageGPS.message = getString(R.string.my_car_says_first_time_timer_off, Utility.getLoggedInUser().name);
                                            //TODO: ADDED NEWLY
                                            makeNotificationPriorityCarWise(messageGPS);
                                            //makeNotificationPriorityWise(messageGPS);
                                            updateMyCarSaysCardNew(carFromLocalDB.id);
                                            //updateMyCarSaysCard();
                                            PrefManager.getInstance().edit().remove(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carFromLocalDB.id)).commit();
                                        }
                                        else
                                        {

                                        }
                                    }
                                }
                                else
                                {
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.FIFTEEN_MIN_TIMER_COMPLETED_AND_NO_GPS_PRESENT, carFromLocalDB.id);

                                    if(carFromLocalDB.lock == 0 || carFromLocalDB.lock == 2)
                                    {
                                        Log.e("{INSIDE}" , carFromLocalDB.id + "");
//                                            PrefManager.getInstance().edit().remove(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carFromLocalDB.id)).commit();
//                                            if(carFromLocalDB.lock == 0 || carFromLocalDB.lock == 2)
//                                            {
//                                                Log.v("{INSIDE}" , carFromLocalDB.id + "");
                                                /*
                                                removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT, carFromLocalDB.id);
                                                removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, carFromLocalDB.id);
                                                */
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT, carFromLocalDB.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, carFromLocalDB.id);
                                        //if(!isCurrentNotificationIdPresent(ConstantCode.NO_GI_FIRST_TIME, carFromLocalDB.id))
                                        if(!isCurrentNotificationIdPresentNew(ConstantCode.NO_GI_FIRST_TIME, carFromLocalDB.id))
                                        {
                                            GCMMessage messageGPS = new GCMMessage();
                                            messageGPS.notification_id = ConstantCode.NO_GI_FIRST_TIME - carFromLocalDB.id;
                                            messageGPS.showInMyCarSays = true;
                                            messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                                            messageGPS.priority = 1;
                                            messageGPS.sub_priority = 5;
                                            messageGPS.carId = carFromLocalDB.id;
                                            messageGPS.message = getString(R.string.my_car_says_first_time_lock_two_or_zero, Utility.getLoggedInUser().name);
                                            //TODO : ADDED NEWLY
                                            // makeNotificationPriorityWise(messageGPS);
                                            makeNotificationPriorityCarWise(messageGPS);
                                            updateMyCarSaysCardNew(carFromLocalDB.id);
                                            //updateMyCarSaysCard();
                                        }
                                    }
                                    if(carFromLocalDB.lock == 1)
                                    {
                                        PrefManager.getInstance().edit().remove(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carFromLocalDB.id)).commit();
                                        Log.v("{INSIDE}" , "1");
                                        /*
                                        removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_FIRST_TIME, carFromLocalDB.id);
                                        removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, carFromLocalDB.id);
                                        */
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_FIRST_TIME, carFromLocalDB.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, carFromLocalDB.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.FIFTEEN_MIN_TIMER_COMPLETED_AND_NO_GPS_PRESENT, carFromLocalDB.id);

                                        GCMMessage messageGPS = new GCMMessage();
                                        messageGPS.notification_id = ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT - carFromLocalDB.id;
                                        final int notif = ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT;
                                        final int carID = carFromLocalDB.id;
                                        messageGPS.showInMyCarSays = true;
                                        messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                                        messageGPS.priority = 1;
                                        messageGPS.sub_priority = 5;
                                        messageGPS.carId = carFromLocalDB.id;
                                        messageGPS.message = getString(R.string.my_car_says_first_time_lock, Utility.getLoggedInUser().name);
                                        //TODO : ADDED NEWLY
                                        //makeNotificationPriorityWise(messageGPS);
                                        makeNotificationPriorityCarWise(messageGPS);
                                        updateMyCarSaysCardNew(carFromLocalDB.id);
                                        //updateMyCarSaysCard();

                                        final String carName = carFromLocalDB.name;
                                        /*
                                        //After 30 secs
                                        Handler hd = new Handler();
                                        hd.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                //removeWelcomeMessageFromGCMList(notif,carID);
                                                removeWelcomeMessageFromGCMListNew(notif,carID);
                                                GCMMessage messageGPS = new GCMMessage();
                                                messageGPS.notification_id = ConstantCode.UPDATE_CAR_INFO_MY_CAR_SAYS_CARD - carID;
                                                messageGPS.showInMyCarSays = true;
                                                messageGPS.carId = carID;
                                                messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                                                messageGPS.priority = 1;
                                                messageGPS.sub_priority = 6;
                                                messageGPS.message = getString(R.string.update_your_car_info, carName);
                                                //TODO : ADDED NEWLY
                                                //makeNotificationPriorityWise(messageGPS);
                                                makeNotificationPriorityCarWise(messageGPS);
                                                updateMyCarSaysCardNew(carID);
                                                //updateMyCarSaysCard();
                                            }
                                        }, 30000);
                                        */
                                        if(alarmUp)
                                        {
                                            if (alarmManager == null)
                                                alarmManager = (AlarmManager) App.getContext().getSystemService(Context.ALARM_SERVICE);
                                            alarmManager.cancel(toBeCancel);

                                            if(!PrefManager.getInstance().contains(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carFromLocalDB.id)) || !(PrefManager.getInstance().getBoolean(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carFromLocalDB.id),false)))
                                            {
                                                //Always getting created here only not anywhere else
                                                PrefManager.putBoolean(ConstantCode.ACTION_TIMER_COMPLETED + String.valueOf(carFromLocalDB.id), false);
                                            }
                                            else
                                            {
                                                PrefManager.getInstance().edit().putBoolean(ConstantCode.ACTION_TIMER_COMPLETED,false).commit();
                                            }

                                        }

                                        Cars.updateIsFistTimeEntry(String.valueOf(carFromLocalDB.id),false);

                                    }
                                    /*
                                    if(carFromLocalDB.lock == 2)
                                    {
                                        Log.v("{INSIDE}" , "2");
                                        removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_FIRST_TIME - carFromLocalDB.id);
                                        removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM - carFromLocalDB.id);
                                        if(!isCurrentNotificationIdPresent(ConstantCode.GPS_MESSAGE_WHEN_GSM_PRESENT - carFromLocalDB.id))
                                        {
                                            GCMMessage messageGPS = new GCMMessage();
                                            messageGPS.notification_id = ConstantCode.GPS_MESSAGE_WHEN_GSM_PRESENT - carFromLocalDB.id;
                                            messageGPS.showInMyCarSays = true;
                                            messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                                            messageGPS.priority = 9;
                                            messageGPS.sub_priority = 9;
                                            messageGPS.message = getString(R.string.first_time_welcome_message_for_gsm_present, carFromLocalDB.name);
                                            makeNotificationPriorityWise(messageGPS);
                                            updateMyCarSaysCard();
                                        }
                                    }
                                    */
                                }
                                Log.v("TAG------------", carFromLocalDB.isFirstTimeEntry + " ");
                            }
                            else
                            {

                                //if(!isCurrentNotificationIdPresent(ConstantCode.NO_GI_REGULAR, carFromLocalDB.id))
                                if(!isCurrentNotificationIdPresentNew(ConstantCode.NO_GI_REGULAR, carFromLocalDB.id))
                                {
                                    /*
                                    removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_FIRST_TIME, carFromLocalDB.id);
                                    removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, carFromLocalDB.id);
                                    removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT, carFromLocalDB.id);
                                    removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE, carFromLocalDB.id);
                                    */
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_FIRST_TIME, carFromLocalDB.id);
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, carFromLocalDB.id);
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT, carFromLocalDB.id);
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE, carFromLocalDB.id);
                                    isGPSMessageThere = false;

                                    GCMMessage messageGPS = new GCMMessage();
                                    messageGPS.notification_id = ConstantCode.GPS_MESSAGE - carFromLocalDB.id;
                                    messageGPS.showInMyCarSays = true;
                                    messageGPS.carId = carFromLocalDB.id;
                                    messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                                    messageGPS.priority = 1;
                                    messageGPS.sub_priority= 4;
                                    if (listCar.lock == 0) {
                                        if(isGPSMessageThere == false)
                                        {
                                            if(isAdded())
                                            {
                                                messageGPS.message = getString(R.string.my_car_says_regular_lock_zero);
                                                //TODO : ADDED NEWLY
                                                //makeNotificationPriorityWise(messageGPS);
                                                makeNotificationPriorityCarWise(messageGPS);
                                                updateMyCarSaysCardNew(carFromLocalDB.id);
                                                //updateMyCarSaysCard();
                                                isGPSMessageThere = true;
                                            }

                                        }
                                        isGPSMessageThere = true;

                                    } else if (listCar.lock == 2) {
                                        if(isGPSMessageThere == false)
                                        {
                                            if(isAdded())
                                            {
                                                //Log.v("Inside"," : regular" + carFromLocalDB.id);
                                                messageGPS.message = getString(R.string.my_car_says_regular_lock_two);
                                                //TODO : ADDED NEWLY
                                                //makeNotificationPriorityWise(messageGPS);
                                                makeNotificationPriorityCarWise(messageGPS);
                                                updateMyCarSaysCardNew(carFromLocalDB.id);
                                                //updateMyCarSaysCard();
                                                isGPSMessageThere = true;
                                            }

                                        }
                                        isGPSMessageThere = true;
                                    }
                                    else if(listCar.lock == 1)
                                    {
                                        if(isGPSMessageThere == false)
                                        {
                                            if(isAdded())
                                            {
                                                messageGPS.sub_priority= 0;
                                                //Log.e("Inside"," : regular" + carFromLocalDB.id);
                                                //Log.v("Inside"," : regular" + carFromLocalDB.id);
                                                messageGPS.message = getString(R.string.my_car_says_regular_lock_one, Utility.getLoggedInUser().name);
                                                //TODO : ADDED NEWLY
                                                //makeNotificationPriorityWise(messageGPS);
                                                makeNotificationPriorityCarWise(messageGPS);
                                                updateMyCarSaysCardNew(carFromLocalDB.id);
                                                //updateMyCarSaysCard();
                                                isGPSMessageThere = true;
                                            }

                                        }
                                        isGPSMessageThere = true;
                                    }
                                    else {
                                        notificationReceiver.onNotificationDismiss(messageGPS);
                                        isGPSMessageThere = false;
                                    }
                                }
                                else
                                {
                                    //Log.v("TAG","I m Here");
                                }

                            }
                        }
                    }
                    /*
                    if (Utility.getLoggedInUser() != null) {

                        // [ START - logic to show GPS message ]
                        GCMMessage messageGPS = new GCMMessage();
                        messageGPS.notification_id = ConstantCode.GPS_MESSAGE;
                        messageGPS.showInMyCarSays = true;
                        messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                        messageGPS.priority = 4;
                        if (finalLock == 0) {
                            if (isGPSMessageThere == false) {
                                messageGPS.message = getString(R.string.first_time_login_welcome_message,Utility.getLoggedInUser().name);
                                makeNotificationPriorityWise(messageGPS);
                                updateMyCarSaysCard();
                                isGPSMessageThere = true;
                            }
                        } else if (finalLock == 2) {
                            messageGPS.message = gpsErrorMessage[1];
                            if (isGPSMessageThere == false) {
                                makeNotificationPriorityWise(messageGPS);
                                updateMyCarSaysCard();
                                isGPSMessageThere = true;
                            }
                        } else {
                            notificationReceiver.onNotificationDismiss(messageGPS);
                            isGPSMessageThere = false;
                        }
                        // [ END - logic to show GPS message ]
                    }
                    */
                    if (goingToAddFirstCar) {
                        ((ActivityDashboard) mActivity).getIntroFrameLayout().setVisibility(View.VISIBLE);
                    }
                    goingToAddFirstCar = false;

                    if (Utility.getLoggedInUser() != null) {
                        scheduleNextUpdate();
                    }
                } else {
                    if (ActivityAddNewCarSetup.isActivityStarted == false) {
                        Log.i("START", "AddNewCarActivityStarted");
                        Intent intent = new Intent(mActivity, ActivityAddNewCarSetup.class);
                        intent.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_REGISTER_CAR); //Sending argument because we need to disable back and to show welcome screen.
                        mActivity.startActivityForResult(intent, ConstantCode.ACTION_ACTIVITY_ADD_NEW_CAR);
                        goingToAddFirstCar = true;
                    }
                }
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
                progressBar.setVisibility(View.GONE);
                showToast(values.toString());
                /*Intent intent = new Intent(mActivity, ActivityAddNewCarSetup.class);
                intent.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_REGISTER_CAR); //Sending argument because we need to disable back and to show welcome screen.
                mActivity.startActivity(intent);
                goingToAddFirstCar = true;*/
                /*
                ArrayList<Cars> list = Cars.readAll();
                if(list != null)
                {
                    for(Cars c : list)
                    {
                        GCMMessage messageGPS = new GCMMessage();
                        messageGPS.notification_id = ConstantCode.GPS_MESSAGE - c.id;
                        messageGPS.showInMyCarSays = true;
                        messageGPS.carId = c.id;
                        messageGPS.message = "Please connect to the internet so that I can communicate with you";
                        messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                        messageGPS.priority = 1;
                        messageGPS.sub_priority= 10;
                        makeNotificationPriorityCarWise(messageGPS);
                        updateMyCarSaysCardNew(c.id);
                    }
                }
                */
                scheduleNextUpdate();
            }

            @Override
            public void loadOffline() {
                super.loadOffline();
                /*progressBar.setVisibility(View.GONE);
                listCars = Cars.readAll();
                if (listCars.size() > 0) {
                    addMarkers(listCars);
                }*/

                ArrayList<Cars> list = Cars.readAll();
                if(list != null)
                {
                    if(list.size() > 0)
                    {
                        addMarkers(list);
                    }
                    for(Cars c : list)
                    {
                        removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP, c.id);
                        GCMMessage messageGPS = new GCMMessage();
                        messageGPS.notification_id = ConstantCode.MY_CAR_SAYS_FOR_NO_INTERNET - c.id;
                        messageGPS.showInMyCarSays = true;
                        messageGPS.carId = c.id;
                        if(Utility.getLoggedInUser() != null)
                            messageGPS.message = getString(R.string.my_car_says_internet_not_present,Utility.getLoggedInUser().name);
                        else
                            messageGPS.message = getString(R.string.my_car_says_internet_not_present, "User");
                        messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                        messageGPS.priority = 1;
                        messageGPS.sub_priority= 10;
                        makeNotificationPriorityCarWise(messageGPS);
                        updateMyCarSaysCardNew(c.id);
                    }
                }
                scheduleNextUpdate();
            }
        });
    }

    //structure to save current progress work on syncing so new request will not go unless first one is completed
    static ArrayList<String> goesForOnline;

    /**
     * Used to sync the recent trip and here conditions are checking if flag comes from server is 1 then we need to syn else our local latest trip id is less then server trip id
     *
     * @param id
     * @param serverRecentTrip
     * @param localRecentTrip
     * @param flag
     */
    private void getRecentTripsDetail(int id, int serverRecentTrip, int localRecentTrip, int flag) {
        //Checking if local recent trip is less then server recent trip then fetch from server

        Log.d("TAG", "getRecentTripsDetail() called with: " + "id = [" + id + "], serverRecentTrip = [" + serverRecentTrip + "], localRecentTrip = [" + localRecentTrip + "], flag = [" + flag + "]");
        if (goesForOnline == null) {
            goesForOnline = new ArrayList<String>();
        }
        if (localRecentTrip < serverRecentTrip || flag == 1) {

            //checking if this request is already in progress then remove no need to sync
            if (!goesForOnline.contains(serverRecentTrip + "-" + localRecentTrip))
            {
                //TODO : this function from here itself
                //Syncing the trips
                //syncController.sync(id + "", localRecentTrip + "", serverRecentTrip + "");

                //Calling from intent service, we have commented it because now we created a SyncController that performing this task
                /*Intent intent = new Intent(mActivity, LoadTripDetailService.class);
                intent.putExtra(ConstantCode.INTENT_CAR_ID, id + "");
                intent.putExtra(ConstantCode.INTENT_SERVER_RECENT_TRIP_ID, serverRecentTrip + "");
                intent.putExtra(ConstantCode.INTENT_TRIP_ID, localRecentTrip + "");
                mActivity.startService(intent);*/

                //Saving value in local datastructure because it will not call again if the that trip is is progress
                goesForOnline.add(serverRecentTrip + "-" + localRecentTrip);
            }
        } else {
            //saving the status in preference to make the indiation of "Sync is in progress" on FragmentCarDashboard
            if (TextUtils.isEmpty(PrefManager.getInstance().getString(String.valueOf(id), null)) == false) {
                PrefManager.getInstance().edit().remove(String.valueOf(id)).commit();
                Intent broadcast = new Intent(ConstantCode.BROADCAST_TRIP_SYNCED);
                broadcast.putExtra(ConstantCode.INTENT_CAR_ID, Integer.parseInt(id + ""));
                broadcast.putExtra(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_SYNC_COMPLETE);
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(broadcast);
            }
        }
    }

    //Used to update the flag on local datastructure so that on next calling of garage screen it will sync
    public static void setFlag(String serverRecentTrip, String localRecentTrip) {
        if (goesForOnline.contains(serverRecentTrip + "-" + localRecentTrip)) {
            goesForOnline.remove(serverRecentTrip + "-" + localRecentTrip);
        }
    }

    public static ArrayList<String> getFlags() {
        return goesForOnline;
    }

    /**
     * Loading Diagnosis values from server and save this values in local database
     *
     * @param listCars
     */
    private void loadDiagnosis(ArrayList<Cars> listCars) {
        if (callFromHandler == false) {
            for (Cars listCar : listCars) {
                final int id = listCar.id;
                WebUtils.call(WebServiceConfig.WebService.GET_CAR_DIAGNOSTIC, new String[]{id + ""}, null, new NetworkCallbacks() {
                    @Override
                    public void successWithString(Object values, WebServiceConfig.WebService webService) {
                        super.successWithString(values, webService);
                        JSONObject json = (JSONObject) values;
                        try {
                            CarDiagnostics carDiagnostics = (CarDiagnostics) Utility.parseFromString(json.optString(ConstantCode.data), CarDiagnostics.class);
                            carDiagnostics.id = id;
                            carDiagnostics.save();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Utility.showLogE(FragmentCarDiagnostics.class, e.getMessage());
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeUpdates();
        if (gcmUtils != null)
            gcmUtils.unregister();

        /*
        if (syncController != null) {
            syncController.cancelSync();
        }
        */
    }

    /**
     * Used to intialize the maps and when map is loaded then show the markers
     */
    private void initMaps() {
        links(R.id.map).setVisibility(View.INVISIBLE);
        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
        if (mSupportMapFragment == null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mSupportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, mSupportMapFragment).commit();
        }
        if (mSupportMapFragment != null) {

            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    if (googleMap != null)
                    {
                        FragmentCars.this.googleMap = googleMap;
                        googleMap.getUiSettings().setAllGesturesEnabled(true);

                        links(R.id.map).setVisibility(View.VISIBLE);

                        if (pendingTask == true) {
                            addMarkers(listCars);
                            pendingTask = false;
                        }
                    }
                }
            });
        }
    }

    boolean pendingTask = false;
    ArrayList<Marker> listMarkers;
    ArrayList<Cars> listCars;
    MarkerOptions markerOptions = new MarkerOptions();

    /**
     * Used to add markers on google maps
     *
     * @param list
     */
    public void addMarkers(ArrayList<Cars> list) {
        if (googleMap == null) {
            Log.e("I know you buddy : ","google map is here null");
            pendingTask = true;
            return;
        }

        if (listMarkers == null) {
            listMarkers = new ArrayList<Marker>();
        }

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //On marker click we are opening the CarDashboard screen
                ArrayList<Cars> tempCars = Cars.readAll();
                Cars car = getCarFromList(tempCars, Integer.parseInt(marker.getSnippet()));
                Log.v(TAG, car.name);
                if (!TextUtils.isEmpty(car.name)) {
                    Intent intent = new Intent(mActivity, ActivityCarDashboard.class);
                    intent.putExtra(ConstantCode.INTENT_CAR_ID, String.valueOf(car.id));//sending as string
                    intent.putExtra(ConstantCode.INTENT_CAR_NAME, String.valueOf(car.name));
                    intent.putExtra(ConstantCode.INTENT_CAR_STATUS, car.isOnTrip);//sending as boolean
                    startActivity(intent);
                }


                return true;
            }
        });
        int position = 0;
        for (Cars cars : list) {
            if (cars == null)
                break;

            if (markerOptions == null) {
                markerOptions = new MarkerOptions();
            }

            try {
                latLng = new LatLng(Double.parseDouble(cars.lat), Double.parseDouble(cars.lon));
            } catch (Exception e) {
                latLng = new LatLng(ConstantCode.DEFAULT_LAT, ConstantCode.DEFAULT_LONG);
            }

            markerOptions.position(latLng);
            Marker marker = null;


            //We are checking if already exist then update the marker only
            boolean found = false;
            for (Marker listMarker : listMarkers) {
                if (Integer.parseInt(listMarker.getSnippet()) == cars.id) {
                    listMarker.setPosition(latLng);
                    marker = listMarker;
                    found = true;
                }
            }
            if (found == false) {
                marker = googleMap.addMarker(markerOptions);
                marker.setSnippet(cars.id + "");
                listMarkers.add(marker);
            }

            int isOnTripBubbleShown = 0;
            //Inflating marker from layout
            View viewMarkerView = LayoutInflater.from(mActivity).inflate(R.layout.car_marker, null, false);
            ((TextView) viewMarkerView.findViewById(R.id.txt_marker_name)).setText(cars.name);
            ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setVisibility(View.GONE);
            if (cars.isOnTrip) {
                if(Utility.isConnectingToInternet(mActivity)) {
                    viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.GONE);
                    viewMarkerView.findViewById(R.id.info_window).setVisibility(View.VISIBLE);
                    ((TextView) viewMarkerView.findViewById(R.id.txt_speed)).setText(cars.speed + "");

                    Calendar onTripSyncTime = DateHelper.getCalendarFromServer(cars.lut);
                    if(onTripSyncTime != null) {
                        Calendar todayTime = Calendar.getInstance();
                        //Log.e(todayTime.getTimeInMillis()+" : ",onTripSyncTime.getTimeInMillis()+"");
                        if (todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis() > 0) {
                            long sec = TimeUnit.MILLISECONDS.toSeconds(Math.abs(todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis()));
                            if (sec > 30) {
                                isOnTripBubbleShown = 3;
                                CharSequence relativeTime = DateUtils.getRelativeDateTimeString(mActivity, onTripSyncTime.getTimeInMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.YEAR_IN_MILLIS, 0);
                                if (relativeTime != null) {
                                    ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setText("Last Sync " + relativeTime);
                                    ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setVisibility(View.VISIBLE);

                                    viewMarkerView.findViewById(R.id.info_window).setVisibility(View.GONE);
                                    viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.GONE);
                                    viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.GONE);
                                    viewMarkerView.findViewById(R.id.img_sync_marker).setVisibility(View.VISIBLE);

                                    if(!cars.isFirstTimeEntry)
                                    {
                                        /*
                                    removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_FIRST_TIME ,cars.id);
                                    removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, cars.id);
                                    removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT, cars.id);
                                    removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE, cars.id);
                                    */
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP ,cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP_BLE_CONNECTED, cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_FIRST_TIME ,cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT, cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE, cars.id);

                                        //removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_REGULAR - cars.id);
                                        //if(!isCurrentNotificationIdPresent(ConstantCode.NO_GI_REGULAR, cars.id))
                                        if(!isCurrentNotificationIdPresentNew(ConstantCode.NO_GI_REGULAR, cars.id))
                                        {
                                            GCMMessage messageGPS = new GCMMessage();
                                            messageGPS.notification_id = ConstantCode.NO_GI_REGULAR - cars.id;
                                            messageGPS.carId = cars.id;
                                            messageGPS.showInMyCarSays = true;
                                            messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                                            messageGPS.priority = 1;
                                            messageGPS.sub_priority = 10;
                                            messageGPS.message = getString(R.string.my_car_says_regular_LUT);
                                            //TODO : ADDED NEWLY
                                            makeNotificationPriorityCarWise(messageGPS);
                                            //makeNotificationPriorityWise(messageGPS);
                                        }

                                        updateMyCarSaysCardNew(cars.id);
                                        //updateMyCarSaysCard();

                                    }
                                }
                            } else {

                                if (cars.isOnTrip) {
                                    isOnTripBubbleShown = 1;
                                    viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.VISIBLE);
                                    if(!cars.isFirstTimeEntry)
                                    {
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP ,cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP_BLE_CONNECTED, cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_FIRST_TIME ,cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT, cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE, cars.id);
                                        if(BLEService.passKeyWriteDone)
                                        {
                                            GCMMessage messageGPS = new GCMMessage();
                                            messageGPS.notification_id = ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP_BLE_CONNECTED - cars.id;
                                            messageGPS.carId = cars.id;
                                            messageGPS.showInMyCarSays = true;
                                            messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                                            messageGPS.priority = 1;
                                            messageGPS.sub_priority = 3;
                                            messageGPS.message = getString(R.string.my_car_says_card_for_on_trip_status_ble_connectd);
                                            //TODO : ADDED NEWLY
                                            makeNotificationPriorityCarWise(messageGPS);
                                            updateMyCarSaysCardNew(cars.id);
                                        }
                                        else
                                        {
                                            GCMMessage messageGPS = new GCMMessage();
                                            messageGPS.notification_id = ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP - cars.id;
                                            messageGPS.carId = cars.id;
                                            messageGPS.showInMyCarSays = true;
                                            messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                                            messageGPS.priority = 1;
                                            messageGPS.sub_priority = 3;
                                            messageGPS.message = getString(R.string.my_car_says_card_for_on_trip_status);
                                            //TODO : ADDED NEWLY
                                            makeNotificationPriorityCarWise(messageGPS);
                                            updateMyCarSaysCardNew(cars.id);
                                        }
                                    }

                                } else {
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP_BLE_CONNECTED, cars.id);
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP, cars.id);
                                    viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.GONE);
                                }
                            }
                        }

                    }
                    else
                    {
                        //TODO:handle this else case afterwards
                    }
                }
                else {
                    isOnTripBubbleShown = 2;
                    Calendar onTripSyncTime = DateHelper.getCalendarFromServer(cars.lut);
                    if(onTripSyncTime != null) {
                        Calendar todayTime = Calendar.getInstance();
                        if (todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis() > 0) {
                            long sec = TimeUnit.MILLISECONDS.toSeconds(Math.abs(todayTime.getTimeInMillis() - onTripSyncTime.getTimeInMillis()));
                            if (sec > 30) {
                                CharSequence relativeTime = DateUtils.getRelativeDateTimeString(mActivity, onTripSyncTime.getTimeInMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.YEAR_IN_MILLIS, 0);
                                if (relativeTime != null) {
                                    ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setText("Last Sync " + relativeTime);
                                    ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setVisibility(View.VISIBLE);
                                    viewMarkerView.findViewById(R.id.info_window).setVisibility(View.GONE);
                                    viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.GONE);
                                    viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.GONE);
                                    viewMarkerView.findViewById(R.id.img_sync_marker).setVisibility(View.VISIBLE);


                                    if(!cars.isFirstTimeEntry)
                                    {
                                        /*
                                    removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_FIRST_TIME ,cars.id);
                                    removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, cars.id);
                                    removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT, cars.id);
                                    removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE, cars.id);
                                    */
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP ,cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP_BLE_CONNECTED, cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_FIRST_TIME ,cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT, cars.id);
                                        removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE, cars.id);

                                        //removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_REGULAR - cars.id);
                                        //if(!isCurrentNotificationIdPresent(ConstantCode.NO_GI_REGULAR, cars.id))
                                        if(!isCurrentNotificationIdPresentNew(ConstantCode.NO_GI_REGULAR, cars.id))
                                        {
                                            GCMMessage messageGPS = new GCMMessage();
                                            messageGPS.notification_id = ConstantCode.NO_GI_REGULAR - cars.id;
                                            messageGPS.carId = cars.id;
                                            messageGPS.showInMyCarSays = true;
                                            messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                                            messageGPS.priority = 1;
                                            messageGPS.sub_priority = 10;
                                            messageGPS.message = getString(R.string.my_car_says_regular_LUT);
                                            //TODO : ADDED NEWLY
                                            makeNotificationPriorityCarWise(messageGPS);
                                            //makeNotificationPriorityWise(messageGPS);
                                        }

                                        updateMyCarSaysCardNew(cars.id);
                                        //updateMyCarSaysCard();

                                    }
                                }
                                else
                                {
                                    //remove the my car says card for NO_GI_REGULAR
                                    //removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_REGULAR, cars.id);
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_REGULAR, cars.id);
                                }
                            }
                            else
                            {
                                ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setVisibility(View.GONE);
                                viewMarkerView.findViewById(R.id.info_window).setVisibility(View.GONE);
                                viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.GONE);
                                viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.GONE);
                                viewMarkerView.findViewById(R.id.img_sync_marker).setVisibility(View.VISIBLE);

                                removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_REGULAR, cars.id);
                                //removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_REGULAR, cars.id);
                            }
                        }
                        else
                        {
                            removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_REGULAR, cars.id);
                            //removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_REGULAR, cars.id);
                        }
                    }
                    else
                    {
                        //removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_REGULAR, cars.id);
                        removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_REGULAR, cars.id);
                    }
                }
            } else {
                removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP, cars.id);
                removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP_BLE_CONNECTED, cars.id);
                viewMarkerView.findViewById(R.id.info_window).setVisibility(View.GONE);
                viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.VISIBLE);
            }
            if(!cars.isOnTrip)
            {
                removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP, cars.id);
                removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP_BLE_CONNECTED, cars.id);
            }
            //Checking if sync time is greater then 10 min then we need to last sync status on the marker
            if(isOnTripBubbleShown == 0)
            {
                Calendar syncTime = DateHelper.getCalendarFromServer(cars.lut);
                if (syncTime != null) {
                    Calendar todayTime = Calendar.getInstance();
                    if (todayTime.getTimeInMillis() - syncTime.getTimeInMillis() > 0) {
                        //getting minutes between the last sync time and current time
                        long min = TimeUnit.MILLISECONDS.toMinutes(Math.abs(todayTime.getTimeInMillis() - syncTime.getTimeInMillis()));
                        if (min > 10) {
                            CharSequence relativeTime = DateUtils.getRelativeDateTimeString(mActivity, syncTime.getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.YEAR_IN_MILLIS, 0);
                            if (relativeTime != null) {
                                ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setText("Last Sync " + relativeTime);
                                ((TextView) viewMarkerView.findViewById(R.id.txt_sync_status)).setVisibility(View.VISIBLE);
                                viewMarkerView.findViewById(R.id.info_window).setVisibility(View.GONE);
                                viewMarkerView.findViewById(R.id.txt_status_on_trip).setVisibility(View.GONE);
                                viewMarkerView.findViewById(R.id.img_marker).setVisibility(View.GONE);
                                viewMarkerView.findViewById(R.id.img_sync_marker).setVisibility(View.VISIBLE);
                                Cars carCheck = Cars.readSpecific(String.valueOf(cars.id));
                                if(!carCheck.isFirstTimeEntry)
                                {
                                    /*
                                removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_FIRST_TIME, cars.id);
                                removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, cars.id);
                                removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT, cars.id);
                                removeWelcomeMessageFromGCMList(ConstantCode.GPS_MESSAGE, cars.id);
                                */
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.MY_CAR_SAYS_CARD_FOR_ON_TRIP, cars.id);
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_FIRST_TIME, cars.id);
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_NO_GPS_AND_GSM, cars.id);
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE_WHEN_GPS_PRESENT, cars.id);
                                    removeWelcomeMessageFromGCMListNew(ConstantCode.GPS_MESSAGE, cars.id);
                                    //removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_REGULAR - cars.id);

                                    //Log.e("Inside"," : {regular}" + isCurrentNotificationIdPresent(ConstantCode.NO_GI_REGULAR, cars.id));
                                    //if(!isCurrentNotificationIdPresent(ConstantCode.NO_GI_REGULAR, cars.id))
                                    if(!isCurrentNotificationIdPresentNew(ConstantCode.NO_GI_REGULAR, cars.id))
                                    {
                                        GCMMessage messageGPS = new GCMMessage();
                                        messageGPS.notification_id = ConstantCode.NO_GI_REGULAR - cars.id;
                                        messageGPS.carId = cars.id;
                                        messageGPS.showInMyCarSays = true;
                                        messageGPS.category = ConstantCode.NOTIFICATION_CATEGORY_CUSTOM;
                                        messageGPS.priority = 1;
                                        messageGPS.sub_priority = 10;
                                        messageGPS.message = getString(R.string.my_car_says_regular_LUT);
                                        //TODO : ADDED NEWLY
                                        //makeNotificationPriorityWise(messageGPS);
                                        makeNotificationPriorityCarWise(messageGPS);
                                        updateMyCarSaysCardNew(cars.id);
                                        //updateMyCarSaysCard();
                                    }

                                }

                            }
                            else
                            {
                                //removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_REGULAR, cars.id);
                                removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_REGULAR, cars.id);
                            }
                        }
                        else
                        {
                            removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_REGULAR, cars.id);
                            //removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_REGULAR, cars.id);
                        }
                    }
                    else
                    {
                        removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_REGULAR, cars.id);
                        //removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_REGULAR, cars.id);
                    }
                }
                else
                {
                    removeWelcomeMessageFromGCMListNew(ConstantCode.NO_GI_REGULAR, cars.id);
                    //removeWelcomeMessageFromGCMList(ConstantCode.NO_GI_REGULAR, cars.id);
                }
            }

            if (marker != null) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(Utility.getBitmap(viewMarkerView, mActivity)));
                if (cars.isOnTrip) {
                    marker.setAnchor(0.5f, 0.7f);
                } else {
                    marker.setAnchor(0.5f, 0.4f);
                }
            }

            position++;
        }

        if (callFromHandler == false) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        GoogleMapUtils.zoomToFitAllMarker(mActivity, googleMap, false, listMarkers);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 500);
        }
    }

    private Cars getCarFromList(ArrayList<Cars> list, int carId) {
        for (Cars cars : list) {
            if (cars.id == carId) {
                return cars;
            }
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ConstantCode.ACTION_ACTIVITY_ADD_NEW_CAR) { //code to open activityaddnewcar
                progressBar.setVisibility(View.VISIBLE);
                callFromHandler = false;
                loadMapData();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("TAG", "setMenuVisibility onresume() called with: " + "");
        ArrayList<Cars> list = Cars.readAll();
        if(list != null)
        {
            for(Cars c : list)
            {
                updateMyCarSaysCardNew(c.id);
            }
        }
        scheduleNextUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void removeUpdates() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    /**
     * Used to schedule next update of garage screen after ConstantCode.MAP_REFRESH_TIMEOUT
     */
    public void scheduleNextUpdate() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        if (handler == null)
            handler = new Handler();

        if (mActivity != null)
            handler.postDelayed(runnable, ConstantCode.MAP_REFRESH_TIMEOUT);
    }

    //Update at continues updates
    Handler handler;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (Utility.getLoggedInUser() != null) {
                callFromHandler = true;
                loadMapData();
            }
        }
    };
    boolean callFromHandler = false;

    public void cameraUpdate() {
        if (googleMap != null) {
            Log.e("FragmentCars : Marker ", listMarkers.size() + "");
            if (googleMap != null && listMarkers != null && listMarkers.size() > 0) {
                try {
                    Log.e("FragmentCars : ","Updating homing button!!");
                    GoogleMapUtils.zoomToFitAllMarker(mActivity, googleMap, true, listMarkers);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else
            {
                if(googleMap == null)
                    Log.e("FragmentCars : ", "Google Map == null 0");
                if(listMarkers == null)
                    Log.e("FragmentCars : ", "listMarkers==null 0");
                if(listMarkers.size() == 0)
                    Log.e("FragmentCars : ", "listMarkers.size()==0 0");
            }
        }
        else
        {
            Log.e("FragmentCars : ", "googleMap == null 1");
        }
    }

    /*
    public void removeWelcomeMessageFromGCMList(int notificationID, int carId)
    {
        for(GCMMessage gcm : gcmMessagesListgcmMessagesList)
        {
            if(gcm.notification_id == notificationID - carId)
            {
                gcmMessagesList.remove(gcmMessagesList.indexOf(gcm));
                updateMyCarSaysCardNew(carId);
                //updateMyCarSaysCard();
                break;
    */
                /*
                if(gcmMessagesList.get(gcmMessagesList.size()-1).notification_id != notificationID)
                {
                    gcmMessagesList.remove(gcmMessagesList.indexOf(gcm));
                    updateMyCarSaysCard();
                    break;
                }
                */
    /*
            }
        }
    }
    */

    public void removeWelcomeMessageFromGCMListNew(int notificationID, int carId)
    {
        if(gcmNotif != null)
        {
            if(gcmNotif.get(carId) != null)
            {
                if(gcmNotif.get(carId).size() > 0)
                {
                    for(GCMMessage gcm : gcmNotif.get(carId))
                    {
                        if(gcm.notification_id == notificationID - carId)
                        {
                            gcmNotif.get(carId).remove(gcmNotif.get(carId).indexOf(gcm));
                            //updateMyCarSaysCardNew(carId);
                            //updateMyCarSaysCard();
                            break;
                        }
                    }
                }

            }
        }
    }


    /*
    public boolean isCurrentNotificationIdPresent(int notif_id, int carId)
    {
        boolean status = false;
        if(gcmMessagesList != null)
        {
            if(gcmMessagesList.size() > 0)
            {
                for(GCMMessage gcm : gcmMessagesList)
                {
                    if(gcm.notification_id == notif_id - carId )
                    {
                        status = true;
                        break;
                    }
                }
            }
            else
            {
                return status;
            }
        }

        return status;

    }
    */

    public boolean isCurrentNotificationIdPresentNew(int notif_id, int carId)
    {
        boolean status = false;
        if(gcmNotif != null)
        {
            if(gcmNotif.get(carId) != null)
            {
                if(gcmNotif.get(carId).size() > 0)
                {
                    for(GCMMessage gcm : gcmNotif.get(carId))
                    {
                        if(gcm.notification_id == notif_id - carId )
                        {
                            status = true;
                            break;
                        }
                    }
                }
                else
                {
                    return status;
                }
            }

        }
        return status;

    }

    public class MainPagerAdapter extends PagerAdapter
    {
        // This holds all the currently displayable views, in order from left to right.
        private ArrayList<View> views = new ArrayList<View>();

        //-----------------------------------------------------------------------------
        // Used by ViewPager.  "Object" represents the page; tell the ViewPager where the
        // page should be displayed, from left-to-right.  If the page no longer exists,
        // return POSITION_NONE.
        @Override
        public int getItemPosition (Object object)
        {
            //return POSITION_NONE;

            int index = views.indexOf (object);
            if (index == -1)
                return POSITION_NONE;
            else
                return index;

        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager.  Called when ViewPager needs a page to display; it is our job
        // to add the page to the container, which is normally the ViewPager itself.  Since
        // all our pages are persistent, we simply retrieve it from our "views" ArrayList.
        @Override
        public Object instantiateItem (ViewGroup container, int position)
        {
            View v = views.get (position);
            container.addView (v);
            return v;
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager.  Called when ViewPager no longer needs a page to display; it
        // is our job to remove the page from the container, which is normally the
        // ViewPager itself.  Since all our pages are persistent, we do nothing to the
        // contents of our "views" ArrayList.
        @Override
        public void destroyItem (ViewGroup container, int position, Object object)
        {
            container.removeView (views.get (position));
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager; can be used by app as well.
        // Returns the total number of pages that the ViewPage can display.  This must
        // never be 0.
        @Override
        public int getCount ()
        {
            return views.size();
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager.
        @Override
        public boolean isViewFromObject (View view, Object object)
        {
            return view == object;
        }

        //-----------------------------------------------------------------------------
        // Add "view" to right end of "views".
        // Returns the position of the new view.
        // The app should call this to add pages; not used by ViewPager.
        public int addView (View v)
        {
            return addView (v, views.size());
        }

        //-----------------------------------------------------------------------------
        // Add "view" at "position" to "views".
        // Returns position of new view.
        // The app should call this to add pages; not used by ViewPager.
        public int addView (View v, int position)
        {
            views.add (position, v);
            return position;
        }

        //-----------------------------------------------------------------------------
        // Removes "view" from "views".
        // Retuns position of removed view.
        // The app should call this to remove pages; not used by ViewPager.
        public int removeView (ViewPager pager, View v)
        {
            return removeView (pager, views.indexOf (v));
        }

        //-----------------------------------------------------------------------------
        // Removes the "view" at "position" from "views".
        // Retuns position of removed view.
        // The app should call this to remove pages; not used by ViewPager.
        public int removeView (ViewPager pager, int position)
        {
            // ViewPager doesn't have a delete method; the closest is to set the adapter
            // again.  When doing so, it deletes all its views.  Then we can delete the view
            // from from the adapter and finally set the adapter to the pager again.  Note
            // that we set the adapter to null before removing the view from "views" - that's
            // because while ViewPager deletes all its views, it will call destroyItem which
            // will in turn cause a null pointer ref.
            pager.setAdapter (null);
            views.remove (position);
            pager.setAdapter (this);

            return position;
        }

        //-----------------------------------------------------------------------------
        // Returns the "view" at "position".
        // The app should call this to retrieve a view; not used by ViewPager.
        public View getView (int position)
        {
            return views.get (position);
        }

        // Other relevant methods:

        // finishUpdate - called by the ViewPager - we don't care about what pages the
        // pager is displaying so we don't use this method.
    }

    //-----------------------------------------------------------------------------
    // Here's what the app should do to add a view to the ViewPager.
    public void addView (View newPage)
    {
        int pageIndex = pagerAdapter.addView (newPage);
        // You might want to make "newPage" the currently displayed page:
        pager.setCurrentItem (pageIndex, true);
    }

    //-----------------------------------------------------------------------------
    // Here's what the app should do to remove a view from the ViewPager.
    public void removeView (View defunctPage)
    {
        int pageIndex = pagerAdapter.removeView (pager, defunctPage);
        // You might want to choose what page to display, if the current page was "defunctPage".
        if (pageIndex == pagerAdapter.getCount())
            pageIndex--;
        pager.setCurrentItem (pageIndex);
    }

    //-----------------------------------------------------------------------------
    // Here's what the app should do to get the currently displayed page.
    public View getCurrentPage ()
    {
        return pagerAdapter.getView (pager.getCurrentItem());
    }

    //-----------------------------------------------------------------------------
    // Here's what the app should do to set the currently displayed page.  "pageToShow" must
    // currently be in the adapter, or this will crash.
    public void setCurrentPage (View pageToShow)
    {
        pager.setCurrentItem (pagerAdapter.getItemPosition (pageToShow), true);
    }

}