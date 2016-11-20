package com.carnot.network;

import com.android.volley.Request;

import java.util.HashMap;

/**
 * Created by root on 22/4/16.
 */
public class WebServiceConfig {

    public enum WebService {
        RIDER_PROFILE,
        LOGIN,
        REGISTER,
        GARAGE, // garage home screen listing
        TRIPS_BY_CAR,
        RIDER_LANDING,
        GET_SPECIFIC_CAR,
        GET_CAR_DIAGNOSTIC,
        GET_CAR_GRAPHS,
        //        GET_TRIP_GRAPH,
        GET_TRIP_DETAILS,
        GET_RECENT_TRIPS,
        UPLOAD_DOCUMENTS,
        SET_SPEED_LIMIT,
        GET_SPEED_LIMIT,
        GET_DOCUMENTS,
        GET_BRANDS,
        GET_MODELS,
        SAVE_CAR_PROFILE,
        GET_CAR_PROFILE,
        SEND_OTP,
        FORGOT_PASSWORD,
        CHANGE_PASSWORD,
        ADD_EMERGENCY_NUMBER,
        GET_EMERGENCY_NUMBER,
        GET_MY_PROFILE,
        SAVE_MY_PROFILE,
        GET_INSTRUCTION_VIDEO_URL,
        REGISTER_FOR_GCM,
        UNREGISTER_FOR_GCM,
        LOGOUT,
        CANCEL_ACCIDENT_NOTIF,

        GET_ALL_GEOFENCE,
        EDIT_GEOFENCE,
        GET_SINGLE_GEOFENCE,
        DELETE_GEOFENCE,

        GET_GLOBAL_NOTIFICATIONS,
        SET_GLOBAL_NOTIFICATIONS,

        ALL_NOTIFICATION,
        TEST
    }

    private static HashMap<WebService, WebServiceModel> list;

    public static HashMap<WebService, WebServiceModel> getMethods() {

        if (list == null) {
            list = new HashMap<WebService, WebServiceModel>();
            list.put(WebService.RIDER_PROFILE, new WebServiceModel(RIDER_PROFILE, Request.Method.GET, true));
            list.put(WebService.LOGIN, new WebServiceModel(LOGIN, Request.Method.POST, true));
            list.put(WebService.REGISTER, new WebServiceModel(REGISTER, Request.Method.POST, true));

//            list.put(WebService.TRIPS, new WebServiceModel(TRIPS, Request.Method.GET, true));
            list.put(WebService.TRIPS_BY_CAR, new WebServiceModel(TRIPS_BY_CAR, Request.Method.GET, true));
            list.put(WebService.GARAGE, new WebServiceModel(GARAGE, Request.Method.GET, true));
            list.put(WebService.RIDER_LANDING, new WebServiceModel(RIDER_LANDING, Request.Method.GET, true));
            list.put(WebService.GET_SPECIFIC_CAR, new WebServiceModel(GET_SPECIFIC_CAR, Request.Method.GET, true));
            list.put(WebService.GET_CAR_DIAGNOSTIC, new WebServiceModel(GET_CAR_DIAGNOSTIC, Request.Method.GET, true));
            list.put(WebService.GET_CAR_GRAPHS, new WebServiceModel(GET_CAR_GRAPHS, Request.Method.GET, true));
//            list.put(WebService.GET_TRIP_GRAPH, new WebServiceModel(GET_TRIP_GRAPH, Request.Method.GET, true));
            list.put(WebService.ALL_NOTIFICATION, new WebServiceModel(ALL_NOTIFICATION, Request.Method.GET, true));
            list.put(WebService.GET_TRIP_DETAILS, new WebServiceModel(GET_TRIP_DETAILS, Request.Method.GET, true));
            list.put(WebService.GET_RECENT_TRIPS, new WebServiceModel(GET_RECENT_TRIPS, Request.Method.GET, true));
            list.put(WebService.UPLOAD_DOCUMENTS, new WebServiceModel(UPLOAD_DOCUMENTS, Request.Method.POST, true));
            list.put(WebService.SET_SPEED_LIMIT, new WebServiceModel(SET_SPEED_LIMIT, Request.Method.POST, true));
            list.put(WebService.GET_SPEED_LIMIT, new WebServiceModel(GET_SPEED_LIMIT, Request.Method.GET, true));
            list.put(WebService.GET_DOCUMENTS, new WebServiceModel(GET_DOCUMENTS, Request.Method.GET, true));
            list.put(WebService.SAVE_CAR_PROFILE, new WebServiceModel(SAVE_CAR_PROFILE, Request.Method.POST, true));
            list.put(WebService.GET_CAR_PROFILE, new WebServiceModel(GET_CAR_PROFILE, Request.Method.GET, true));

            list.put(WebService.ADD_EMERGENCY_NUMBER, new WebServiceModel(ADD_EMERGENCY_NUMBER, Request.Method.POST, true));
            list.put(WebService.GET_EMERGENCY_NUMBER, new WebServiceModel(GET_EMERGENCY_NUMBER, Request.Method.GET, true));
            list.put(WebService.GET_INSTRUCTION_VIDEO_URL, new WebServiceModel(GET_INSTRUCTION_VIDEO_URL, Request.Method.POST, true));

            list.put(WebService.SEND_OTP, new WebServiceModel(SEND_OTP, Request.Method.POST, true));
            list.put(WebService.FORGOT_PASSWORD, new WebServiceModel(FORGOT_PASSWORD, Request.Method.GET, true));
            list.put(WebService.CHANGE_PASSWORD, new WebServiceModel(CHANGE_PASSWORD, Request.Method.POST, true));
            list.put(WebService.GET_BRANDS, new WebServiceModel(GET_BRANDS, Request.Method.GET, true));
            list.put(WebService.GET_MODELS, new WebServiceModel(GET_MODELS, Request.Method.GET, true));
            list.put(WebService.GET_MY_PROFILE, new WebServiceModel(GET_MY_PROFILE, Request.Method.GET, true));
            list.put(WebService.SAVE_MY_PROFILE, new WebServiceModel(SAVE_MY_PROFILE, Request.Method.POST, true));

            list.put(WebService.REGISTER_FOR_GCM, new WebServiceModel(REGISTER_FOR_GCM, Request.Method.POST, true));
            list.put(WebService.UNREGISTER_FOR_GCM, new WebServiceModel(UNREGISTER_FOR_GCM, Request.Method.POST, true));
            list.put(WebService.LOGOUT, new WebServiceModel(LOGOUT, Request.Method.POST, true));
            list.put(WebService.CANCEL_ACCIDENT_NOTIF, new WebServiceModel(CANCEL_ACCIDENT_NOTIF, Request.Method.POST, true));

//            Geofence related
            list.put(WebService.GET_ALL_GEOFENCE, new WebServiceModel(GET_ALL_GEOFENCE, Request.Method.GET, true));
            list.put(WebService.EDIT_GEOFENCE, new WebServiceModel(EDIT_GEOFENCE, Request.Method.POST, true));
            list.put(WebService.GET_SINGLE_GEOFENCE, new WebServiceModel(GET_SINGLE_GEOFENCE, Request.Method.GET, true));
            list.put(WebService.DELETE_GEOFENCE, new WebServiceModel(DELETE_GEOFENCE, Request.Method.POST, true));

//            Notifications - Global
            list.put(WebService.GET_GLOBAL_NOTIFICATIONS, new WebServiceModel(GET_GLOBAL_NOTIFICATIONS, Request.Method.GET, true));
            list.put(WebService.SET_GLOBAL_NOTIFICATIONS, new WebServiceModel(SET_GLOBAL_NOTIFICATIONS, Request.Method.POST, true));


            list.put(WebService.TEST, new WebServiceModel(TEST, Request.Method.GET, false));
        }
        return list;
    }

    //====================== LINKS =========================

    public static String TEST = "https://api.swip.uk.com/main/logon?user=jmm&pass=jmm&devicecli=01614771200&key=develkey123456789";
//    public static String BASE_URL = "https://carnot-api.herokuapp.com/";

    //STAGING SERVER
    public static String BASE_URL = "https://carnot-cloud.herokuapp.com/";

//    LOCAL PRODUCTION SERVER
//    public static String BASE_URL = "http://192.168.1.4:8080/";

    /*
        Activity Name : ActivityALLTrips
        Purpose: This endpoint retrieves the list of trips with info belonging to a particular user.
        Created Date :     25th April,2016
        Modify Date :     25th April,2016
        Developer name : Javid Piprani
     */
//    private static final String TRIPS = BASE_URL + "users/%s/trips/";
    //    private static final String GET_TRIP_GRAPH = BASE_URL + "data/trips/%s/graph/";

    private static final String RIDER_PROFILE = BASE_URL + "users/%s/fullprofile/";
    private static final String LOGIN = BASE_URL + "users/login/";
    private static final String REGISTER = BASE_URL + "users/register/";
    private static final String TRIPS_BY_CAR = BASE_URL + "cars/%s/trips/";
    private static final String GET_TRIP_DETAILS = BASE_URL + "data/trips/%s/";
    private static final String GARAGE = BASE_URL + "users/%s/garage/";
    private static final String RIDER_LANDING = BASE_URL + "users/%s/fullprofile/";
    private static final String GET_SPECIFIC_CAR = BASE_URL + "cars/%s/";
    private static final String GET_CAR_DIAGNOSTIC = BASE_URL + "cars/%s/diagnostics/";
    private static final String GET_CAR_GRAPHS = BASE_URL + "cars/%s/graphs/";
    private static final String ALL_NOTIFICATION = BASE_URL + "users/%s/notifications/";
    private static final String UPLOAD_DOCUMENTS = BASE_URL + "cars/%s/upload/";
    private static final String GET_DOCUMENTS = BASE_URL + "cars/%s/download/%s/";
    private static final String SET_SPEED_LIMIT = BASE_URL + "cars/%s/speedlimit/";
    private static final String GET_SPEED_LIMIT = BASE_URL + "cars/%s/speedlimit/";
    private static final String SAVE_CAR_PROFILE = BASE_URL + "cars/save/";
    private static final String GET_CAR_PROFILE = BASE_URL + "cars/%s/overview/";
    private static final String VERIFY_OTP = BASE_URL + "users/verify/";
    private static final String SEND_OTP = BASE_URL + "users/getotp/";
    private static final String FORGOT_PASSWORD = BASE_URL + "users/%s/fgpwd/";
    private static final String CHANGE_PASSWORD = BASE_URL + "users/%s/chpwd/";
    private static final String GET_BRANDS = BASE_URL + "cars/brands/";
    private static final String GET_MODELS = BASE_URL + "cars/%s/models/";
    private static final String ADD_EMERGENCY_NUMBER = BASE_URL + "users/%s/econtacts/";
    private static final String GET_EMERGENCY_NUMBER = BASE_URL + "users/%s/econtacts/";
    private static final String GET_INSTRUCTION_VIDEO_URL = BASE_URL + "cars/getlinks/";
    private static final String GET_MY_PROFILE = BASE_URL + "users/%s/settings/";
    private static final String SAVE_MY_PROFILE = BASE_URL + "users/%s/settings/";
    private static final String GET_RECENT_TRIPS = BASE_URL + "data/trips/%s/%s/";
    private static final String REGISTER_FOR_GCM = BASE_URL + "gcm/register/";
    private static final String UNREGISTER_FOR_GCM = BASE_URL + "gcm/unregister/";
    private static final String LOGOUT = BASE_URL + "users/logout/";
    private static final String CANCEL_ACCIDENT_NOTIF = BASE_URL + "users/cancel/";
    private static final String GET_ALL_GEOFENCE = BASE_URL + "cars/all_geofence/%s/";
    private static final String EDIT_GEOFENCE = BASE_URL + "cars/edit_geofence/%s/";
    private static final String GET_SINGLE_GEOFENCE = BASE_URL + "cars/get_single_geofence/%s/";
    private static final String DELETE_GEOFENCE = BASE_URL + "cars/del_geofence/%s/";
    private static final String GET_GLOBAL_NOTIFICATIONS = BASE_URL + "cars/get_global_notifications/%s/";
    private static final String SET_GLOBAL_NOTIFICATIONS = BASE_URL + "cars/set_global_notifications/%s/";

}

