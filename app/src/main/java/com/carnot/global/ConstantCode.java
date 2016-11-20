package com.carnot.global;

import android.os.Environment;

import java.io.File;

public class ConstantCode {

    public static final boolean ENTER_INTERMEDIATE_DAYS_IN_CHARTS = true;
    public static final int MAP_REFRESH_TIMEOUT = 5000;
    public static final String DATE_FORMAT_DD_MM_YY = "dd-MM-yyyy";

    public static final String BROADCAST_TRIP_SYNCED = "com.carnot.broadcast_trip_synced";
    public static final String BROADCAST_MESSAGE_RECEIVE = "com.carnot.broadcast_message_receive";
    public static final String BROADCAST_NOTIFICATION_RECEIVE = "com.carnot.broadcast_notification_receive";

    public static final String IMAGE_FOLDER = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "Carnot"
            + File.separator + "images";
    public static final String VIDEO_FOLDER = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "Carnot"
            + File.separator + "video";


    public static final char degree_symbol = 0x00B0;

    public static final String sCar_E_manualUrl = "http://openxcell.workhive.co/projects/filedownload/MjA0MzI=/MC4yMTk0MjU3NDc0MDM4NzU=";

    public static final String NOTIFICATION_TYPE_TOW_ALERT = "Tow Alert";
    public static final String NOTIFICATION_TYPE_DIAGNOSTICS = "Diagnostics";
    public static final String NOTIFICATION_TYPE_PASSPORT = "Passport";
    public static final String NOTIFICATION_TYPE_SECURITY_LOCATION = "Security & Location";
    public static final String NOTIFICATION_TYPE_SOS = "SOS Crash";
    public static final String NOTIFICATION_TYPE_ACHIEVEMENT = "Achievement Unlocked";
    public static final String INTENT_NOTIFICATION_TYPE = "intent_notification_type";
    public static final String INTENT_MESSAGE = "intent_message";
    public static final String INTENT_DATA = "intent_data";
    public static final String INTENT_DISMISS = "intent_dismiss";
    public static final String INTENT_CAR_ID = "intent_car_id";
    public static final String INTENT_CAR_SHOW_SHORT_TRIPS = "intent_car_show_short_trips";
    public static final String INTENT_SERVER_RECENT_TRIP_ID = "intent_server_recent_trip_id";
    public static final String INTENT_TRIP_ID = "intent_trip_id";

    public static final String INTENT_NAME = "intent_name";
    public static final String INTENT_LICENSE_NO = "intent_license_no";
    public static final String INTENT_FUEL_TYPE = "intent_fuel_type";
    public static final String INTENT_BRAND = "intent_brand";
    public static final String INTENT_MODEL = "intent_model";

    public static final String INTENT_IMAGE_URL = "intent_image_url";
    public static final String INTENT_VIDEO_URL = "intent_video_url";
    public static final String INTENT_CAR_NAME = "intent_car_name";
    public static final String INTENT_CAR_STATUS = "intent_car_status";
    public static final String PREF_INTRO_DONE = "pref_intro_done";
    public static final String INTENT_DOCUMENT_TYPE = "pref_document_type";
    public static final String INTENT_ACTION = "intent_action";
    public static final String ACTION_SYNC_COMPLETE = "action_sync_complete";
    public static final String ACTION_GARAGE_UPDATED = "action_garage_updated";

    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREV = "action_prev";
    public static final String ACTION_UPDATE = "action_update";
    public static final String ACTION_REGISTER_CAR = "action_register_car";

    public static final double DEFAULT_LAT = 23.0225;
    public static final double DEFAULT_LONG = 72.5714;

    public static final String OTP = "otp";
    //WEB PARAMS
    public static final String IN_STATUS = "status";
    public static final String IN_MESSAGE = "message";
    public static final String email = "email";
    public static final String password = "password";
    public static final String name = "name";
    public static final String contacts = "contacts";

    public static final String em = "em";
    public static final String ph = "ph";
    public static final String nm = "nm";
    public static final String gd = "gd";
    public static final String ag = "ag";
    public static final String photo = "photo";


    public static final String ln = "ln";
    public static final String profiles = "profiles";
    public static final String cars = "cars";
    public static final String data = "data";
    public static final String related_trips = "related_trips";
    public static final String price = "price";
    public static final String route = "route";
    public static final String phone = "phone";
    public static final String otp = "otp";
    public static final String brands = "brands";
    public static final String models = "models";



    public static final String PREF_SHOW_SHORT_TRIPS = "pref_show_short_trips";
    public static final String PREF_LOGGED_IN_USER_DETAILS = "pref_logged_in_user_details";
    public static final String PREF_LOGGED_IN_USER_ID = "pref_logged_in_user_id";
    public static final String PREF_LOGGED_IN_USER_NAME = "pref_logged_in_user_name";

    public static final String DEFAULT_USER_JSON = "{\"message\":\"Success\",\"status\":200,\"email\":\"pankaj@openxcell.com\",\"id\":41,\"token\":\"19d441e9b688ed718935f25491ba3518b0ef1f51\"}";
    public static final String DEFAULT_USER_ID = "32";
    public static final String DEFAULT_USER_TOKEN = "04af7328b2346602ad7d581ff738eb25a5482cbf";

    public static final String DEFAULT_API_KEY = "JXn8e6C29jhZ065i8wyQktY33YD3s9zy";

    public static final String dtype = "dtype";
    public static final String dinfo = "dinfo";
    public static final String content_type = "content_type";
    public static final String file = "file";
    public static final String meta = "meta";

    public static final String speed_limit = "speed_limit";
    public static final String sl = "sl";
    public static final int DOCUMENT_TYPE_REGISTRATION = 1;
    public static final int DOCUMENT_TYPE_PUC = 2;
    public static final int DOCUMENT_TYPE_INSURANCE = 3;
    public static final int DOCUMENT_TYPE_DL = 4;

    public static final String model = "model";
    public static final String brand = "brand";
    public static final String printedPasscode = "printedPasscode";
    public static final String userid = "userid";
    public static final String carid = "carid";
    public static final String imageUrl = "imageUrl";
    public static final String videoUrl = "videoUrl";

    public static final String gcmCarID = "car_id";

    public static final String male = "m";
    public static final String female = "f";
    public static final int ACTION_ACTIVITY_ADD_NEW_CAR = 101;
    public static final int MAX_MILEAGE = 20;

    //pankaj@openxcelltechnolabs.com
//    public static final String PLACES_API_KEY = "AIzaSyAl161TCUyKC-q2PkVCWGiuufypAXf2zwU";

    /*Account created via carnot2016@gmail.com*/
    public static final String PLACES_API_KEY = "AIzaSyDmlsuoXwLH7adiFg78dkzrjdP3ZRxOHm8";
    public static final double MULTIPLIER = 2.6;
    public static final double DEFAULT_PETROL_PRICE = 70;
    public static final double DEFAULT_DIESEL_PRICE = 60;
    public static final boolean DEBUG = false;
    public static final String SENT_TOKEN_TO_SERVER = "sent_token_to_server";
    public static final String REGISTRATION_COMPLETE = "registration_complete";
    public static final String reg_id = "reg_id";
    public static final String user_id = "user_id";
    public static final String dev_id = "dev_id";

    public static final String id = "id";
    public static final String trigger = "trigger";
    public static final String category = "category";
    public static final String collapse_key = "collapse_key";
    public static final String dismiss = "dismiss";
    public static final String metadata = "metadata";
    public static final String message = "message";
    public static final String title = "title";
    public static final String priority = "priority";
    public static final String sub_priority = "sub-priority";
    public static final String timestamp = "timestamp";
    public static final long FULL_SCREEN_NOTIFICATION_DELAY_SEC = 1000 * 30;
    public static final String lat = "lat";
    public static final String lon = "lon";
    public static final String code = "code";
    public static final String INTENT_NOTIFICATION_ID = "intent_notification_id";
    public static final String ACTION_DISMISS = "action_dismiss";
    public static final String ACTION_REMIND = "action_remind";
    public static final String time = "time";
    public static final String NOTIFICATION_CATEGORY_SECURITY = "Security";//Red
    public static final String NOTIFICATION_CATEGORY_PASSPORT = "Passport";//Green
    public static final String NOTIFICATION_CATEGORY_DIAGNOSTICS = "Diagnostic";//Amber
    public static final String NOTIFICATION_CATEGORY_ONBOARDING = "Onboarding";//Green
    public static final String NOTIFICATION_CATEGORY_INSIGHT = "Insight";//Green
    public static final String NOTIFICATION_CATEGORY_CUSTOM = "Custom";//Yellow
    public static final String notification_id = "notification_id";
    public static final String INTENT_TITLE = "intent_title";
    public static final String imgUrl = "imgUrl";
    public static final String ft = "ft";
    public static final String fuel = "fuel";
    public static final String FUEL_TYPE_PETROL = "Petrol";
    public static final String FUEL_TYPE_DIESEL = "Diesel";
    public static final String FUEL_TYPE_OTHER = "Other";
    public static final String pwd = "pwd";
    public static final String platform = "platform";

    public static boolean SHOW_SHORT_TRIPS_DEFAULT = true;


    //By MANAV
    public static final String ACTION_TRIP_SYNC_ALRAM = "action_trip_sync";
    public static final String INTENT_TRIP_SYNC = "trip_sync_alram_settings";
    public static final String INTENT_APPLICATION_CONTEXT = "applicationContext";
    public static final String LAST_SYNC_TIME_FOR_TRIPS = "tripsLastSyncTimeForNotAllowingRepeatedWebApiCalls";
    public static final int THRESHOLD_FOR_LAST_SYNC_WEB_API_CALL = 30000;
    public static final int THRESHOLD_FOR_LAST_SYNC_RECENT_TRIPS_TEXT = 5 * 60* 1000;
    //trip syncing in progress label
    public static final String BROADCAST_SHOW_TRIP_SYNC_BANNER = "com.carnot.broadcast_show.trip_sync_banner";
    public static final String ACTION_SYNC_STARTED = "com.carnot.sync_started";
    //Alaram Timing for calling trip Sync WEB API.
    public static final int ALARM_TIME_FOR_GET_RECENT_TRIP_API_CALL = 1000 * 60 * 60 * 24;
    public static final String LAST_SYNC_TIME_FOR_CARS_TRIP_DATA = "lastSyncTimeForCarsTripData";
    public static final String ACTION_TIMER_COMPLETED = "action_timer_completed";
    public static final int TIMER_COMPLETED_ID = 901;
    public static final String ACTION_AUTO_DISMISS_WELCOME_MESSAGE = "action_auto_dismiss_welcome_message";
    public static int GPS_MESSAGE_WHEN_NO_GPS_AND_GSM = -80000;
    public static int GPS_MESSAGE_WHEN_GPS_PRESENT = -70000;
    public static int GPS_MESSAGE_WHEN_GSM_PRESENT = -60000;
    public static int UPDATE_CAR_INFO_MY_CAR_SAYS_CARD = -50000;
    public static int FIFTEEN_MIN_TIMER_COMPLETED_AND_NO_GPS_PRESENT = -40000;
    public static int MY_CAR_SAYS_CARD_FOR_ON_TRIP = -90000;
    public static int MY_CAR_SAYS_CARD_FOR_ON_TRIP_BLE_CONNECTED = -200000;
    public static int MY_CAR_SAYS_FOR_NO_INTERNET = -100000;
    public static final String RECENTLY_ADDED_CAR = "recently_added_car";
    public static int NO_GI_FIRST_TIME = -30000;
    public static int NO_GI_REGULAR = -20000;
    public static int GPS_MESSAGE = -10000;

    public static String TRIP_SYNC_FLAG = "TripSyncFlag";


    public static final String CONTACT_FOR_HELP = "tel:9805003587";

    public static final String PASS_KEY_STATUS = "passKeyStatus";


}