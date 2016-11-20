package com.carnot.libclasses.googlemaps;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.carnot.R;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GoogleMapUtils {


    private static final int MAP_LINE = 10;

    public static final String LOG_TAG = "PlacesAutoCompleteAdapter";
    public static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    public static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    public static final String TYPE_DETAIL = "/details";
    public static final String OUT_JSON = "/json";
//    public static final String API_KEY = "AIzaSyBaDspfQCrTQfeZInliTkWkvnRSfU4CjGo";

    /*Account created via carnot2016@gmail.com*/
//    public static final String API_KEY = "AIzaSyDmlsuoXwLH7adiFg78dkzrjdP3ZRxOHm8";

//    public static final String API_KEY = BuildConfig.PLACES_API_KEY;

    public static final float MAX_ZOOM = 14;

    public static AsyncTask<String, Void, String[]> getLatLongForPlace(final String placeid, final String referenceId, final OnGetLatLongCallback callback) {

        String url = PLACES_API_BASE + TYPE_DETAIL + OUT_JSON + "?";
        url += "key=" + ConstantCode.PLACES_API_KEY;
        if (placeid != null)
            url += "&placeid=" + placeid;
        else if (referenceId != null)
            url += "&reference=" + referenceId;

        AsyncTask<String, Void, String[]> task = new AsyncTask<String, Void, String[]>() {
            @Override
            protected String[] doInBackground(String... params) {
                // TODO Auto-generated method stub
                HttpURLConnection conn = null;
                try {
                    StringBuffer jsonResults = new StringBuffer();

                    URL url = new URL(params[0]);
                    conn = (HttpURLConnection) url.openConnection();
                    InputStreamReader in = new InputStreamReader(conn.getInputStream());

                    // Load the results into a StringBuilder
                    int read;
                    char[] buff = new char[1024];
                    while ((read = in.read(buff)) != -1) {
                        jsonResults.append(buff, 0, read);
                    }

                    // Create a JSON object hierarchy from the results
                    JSONObject jsonObj = new JSONObject(jsonResults.toString());
                    JSONObject resultsjsonObj = jsonObj.optJSONObject("result");
                    JSONObject geometryJsonObject = resultsjsonObj.optJSONObject("geometry");
                    JSONObject locationJsonObject = geometryJsonObject.optJSONObject("location");
                    String lat = locationJsonObject.optString("lat");
                    String lng = locationJsonObject.optString("lng");
                    return new String[]{lat, lng};
                } catch (MalformedURLException e) {
                    Log.e("GoogleMapUtils", "Error processing Places API URL", e);
                    return null;
                } catch (IOException e) {
                    Log.e("GoogleMapUtils", "Error connecting to Places API", e);
                    return null;
                } catch (Exception e) {
                    Log.e("GoogleMapUtils", "Error connecting to Places API", e);
                    return null;
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }

            protected void onPostExecute(String[] result) {
                if (result != null) {
                    callback.onSuccess(result[0], result[1], null);
                } else {
                    callback.onSuccess(null, null, new Exception("Cant get location"));
                }
            }

            ;
        };
        task.execute(url);
        return task;
    }

    public static AsyncTask<String, Void, GooglePlace> getAddressFromLatLng(final Context context, final String lat, final String lng, final OnGetAddressCallback callback) {
        return getAddressFromLatLng(context, Double.parseDouble(lat), Double.parseDouble(lng), callback);
    }

    public static AsyncTask<String, Void, GooglePlace> getAddressFromLatLng(final Context context, final double lat, final double lng, final OnGetAddressCallback callback) {

        AsyncTask<String, Void, GooglePlace> task = new AsyncTask<String, Void, GooglePlace>() {
            @Override
            protected GooglePlace doInBackground(String... params) {
                // TODO Auto-generated method stub

                try {
                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(context, Locale.getDefault());

                    addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    GooglePlace place = new GooglePlace();
                    if (addresses.size() > 0) {
                        String address = "";

                        for (int i = 0; i <= addresses.get(0).getMaxAddressLineIndex(); i++) {
                            if (!TextUtils.isEmpty(addresses.get(0).getAddressLine(i)))
                                address += addresses.get(0).getAddressLine(i) + ", ";
                        }
                        address = address.trim();
                        address = address.replaceAll(",$", "");
//                        address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String countryCode = addresses.get(0).getCountryCode();

                        String postalCode = addresses.get(0).getPostalCode();
                        String knownName = addresses.get(0).getFeatureName();

                        place.address = addresses.get(0);
                        place.description = address;
                        place.countryCode = countryCode;
                        place.country = country;
                        place.lat = String.valueOf(lat);
                        place.lng = String.valueOf(lng);
                        return place;
                    }
                } catch (MalformedURLException e) {
                    Log.e("GoogleMapUtils", "Error processing Places API URL", e);
                    return null;
                } catch (IOException e) {
                    Log.e("GoogleMapUtils", "Error connecting to Places API", e);
                    return null;
                } catch (Exception e) {
                    Log.e("GoogleMapUtils", "Error connecting to Places API", e);
                    return null;
                } finally {

                }
                return null;
            }

            protected void onPostExecute(GooglePlace result) {
                if (result != null) {
                    callback.onSuccess(result, null);
                } else {
                    callback.onSuccess(null, new Exception("Cant get address"));
                }
            }

            ;
        };
        task.execute();
        return task;
    }


    public interface OnGetLatLongCallback {
        public void onSuccess(String lat, String lng, Exception exception);
    }

    public interface OnGetAddressCallback {
        public void onSuccess(GooglePlace place, Exception exception);
    }

//    public static void getCurrentLocationBitmap(Activity activity) {
//
////		com.google.android.gms.maps.MapFragment supportMapFragment=new com.google.android.gms.maps.MapFragment();
//        GoogleMap mMap = MapFragment.newInstance().getMap();
//        if (mMap != null) {
//            Toast.makeText(activity, "map is loaded", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(activity, "map is null", Toast.LENGTH_SHORT).show();
//        }
//    }


    public static Marker setMarker(GoogleMap googleMap, double lat, double lng, String desc, Marker prevMarker, boolean updateCameraLocation, int res, float zoom_level) {
        if (googleMap != null) {
            LatLng latlng = new LatLng(lat, lng);
            if (prevMarker != null) {
                prevMarker.setPosition(latlng);
                prevMarker.setTitle(desc);
                prevMarker.isVisible();
            } else {

                MarkerOptions selectedMarkerOptions = new MarkerOptions();
                selectedMarkerOptions.position(latlng);
                if (res != 0)
                    selectedMarkerOptions.icon(BitmapDescriptorFactory.fromResource(res));

                prevMarker = googleMap.addMarker(selectedMarkerOptions);

                prevMarker.setTitle(desc);
            }
            if (updateCameraLocation) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, zoom_level);
                googleMap.animateCamera(cameraUpdate);
            }
            /*else {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, zoom_level);
                googleMap.moveCamera(cameraUpdate);
            }*/
            return prevMarker;
        }
        return null;
    }

    public static void updateCameraPoition(GoogleMap googleMap, double lat, double lng, boolean animate) {

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), googleMap.getCameraPosition().zoom);
        if (animate) {
            googleMap.animateCamera(cameraUpdate);
        } else {
            googleMap.moveCamera(cameraUpdate);
        }

    }

    public static void updateCameraPoition(GoogleMap googleMap, double lat, double lng, boolean animate, int zoom_level) {

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom_level);
        if (animate) {
            googleMap.animateCamera(cameraUpdate);
        } else {
            googleMap.moveCamera(cameraUpdate);
        }

    }

    public static void updateCameraPoition(GoogleMap googleMap, String lat, String lng, boolean animate, int zoom_level) {
        try {
            updateCameraPoition(googleMap, Double.parseDouble(lat), Double.parseDouble(lng), animate, zoom_level);
        } catch (Exception e) {

        }
    }

    public static Marker setMarker(GoogleMap googleMap, String lat, String lng, String desc, Marker prevMarker, boolean updateCameraLocation, int res, float zoom_level) {
        return setMarker(googleMap, Double.parseDouble(lat), Double.parseDouble(lng), desc, prevMarker, updateCameraLocation, res, zoom_level);
    }

    public static Marker setMarker(GoogleMap googleMap, double lat, double lng, String desc, Marker prevMarker, boolean updateCameraLocation, int res) {
        return setMarker(googleMap, lat, lng, desc, prevMarker, updateCameraLocation, res, GoogleMapUtils.MAX_ZOOM);
    }

    public static Marker setMarker(GoogleMap googleMap, String lat, String lng, String desc, Marker prevMarker, boolean updateCameraLocation, int res) {
        return setMarker(googleMap, Double.parseDouble(lat), Double.parseDouble(lng), desc, prevMarker, updateCameraLocation, res, GoogleMapUtils.MAX_ZOOM);
    }

    public static Marker setMarker(GoogleMap googleMap, double lat, double lng, String desc, Marker prevMarker, boolean updateCameraLocation) {
        return setMarker(googleMap, lat, lng, desc, prevMarker, updateCameraLocation, 0, GoogleMapUtils.MAX_ZOOM);
    }

    public static Marker setMarker(GoogleMap googleMap, String lat, String lng, String desc, Marker prevMarker, boolean updateCameraLocation) {
        return setMarker(googleMap, Double.parseDouble(lat), Double.parseDouble(lng), desc, prevMarker, updateCameraLocation, 0, GoogleMapUtils.MAX_ZOOM);
    }

    public static Marker setMarker(GoogleMap googleMap, String lat, String lng, String desc, Marker prevMarker, boolean updateCameraLocation, float zoom_level) {
        return setMarker(googleMap, Double.parseDouble(lat), Double.parseDouble(lng), desc, prevMarker, updateCameraLocation, 0, zoom_level);
    }

    public static void zoomToFitAllMarker(Context context, GoogleMap googleMap, boolean animateCamera, Marker... markers) {
        if (googleMap != null && markers != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            int padding = (int) Utility.convertDpToPixel(60, context); // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            if (animateCamera)
                googleMap.animateCamera(cu);
            else
                googleMap.moveCamera(cu);
        }
    }

    public static void zoomToFitAllMarkersWithLat(Context context, GoogleMap googleMap, boolean animateCamera, LatLng... latLng) {
        if (googleMap != null && latLng != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng ltln : latLng) {
                builder.include(ltln);
            }
            LatLngBounds bounds = builder.build();
            float[] results = new float[1];
            Location.distanceBetween(bounds.northeast.latitude, bounds.northeast.longitude, bounds.southwest.latitude, bounds.southwest.longitude, results);

            CameraUpdate cu;
            if (results[0] < 1000) {
                cu = CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 16);
            } else {
                int padding = (int) Utility.convertDpToPixel(60, context); // offset from edges of the map in pixels
                cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            }

//            int padding = (int) Utility.convertDpToPixel(60, context); // offset from edges of the map in pixels
//            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            try {
                if (animateCamera)
                    googleMap.animateCamera(cu);
                else
                    googleMap.moveCamera(cu);
            } catch (Exception e) {
                e.printStackTrace();
                if (e.toString().contains("View size is too small")) {
                    cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
                    if (animateCamera)
                        googleMap.animateCamera(cu);
                    else
                        googleMap.moveCamera(cu);
                }
            }
        }
    }

    public static void zoomToFitAllMarker(Context context, GoogleMap googleMap, boolean animateCamera, ArrayList<Marker> markers) {
        if(markers == null ||markers.size() == 0)
        {
            Log.e("GoogleUtils : ","markers is zero " + markers.size());
        }
        Log.e("GoogleUtils : ","markers size is " + markers.size());
        if (googleMap != null && markers != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                Log.e("GoogleUtils : ","markers location is " + marker.getPosition());
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            float[] results = new float[1];
            Location.distanceBetween(bounds.northeast.latitude, bounds.northeast.longitude, bounds.southwest.latitude, bounds.southwest.longitude, results);

            CameraUpdate cu;
            if (results[0] < 1000) {
                Log.e("GoogleMapUtils : ", "if results < 1000");
                cu = CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 16);
            } else {
                Log.e("GoogleMapUtils : ", "if results > 1000");
                int padding = (int) Utility.convertDpToPixel(60, context); // offset from edges of the map in pixels
                cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            }

//            int padding = (int) Utility.convertDpToPixel(60, context); // offset from edges of the map in pixels
//            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            if (animateCamera)
                googleMap.animateCamera(cu);
            else
                googleMap.moveCamera(cu);
        }
        else{
            Log.e("GoogleUtils : ","Failing loops");
        }
    }

    public static void showPathsOnMap(final Context context, final GoogleMap googleMap, final ArrayList<LatLng> latLng) {
        /*ArrayList<LatLng> points = new ArrayList<LatLng>();
        for (LatLng lng : latLng) {
            points.add(lng);
        }*/
        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.addAll(latLng);
        /*lineOptions.width(2);
        lineOptions.color(Color.RED);*/

        lineOptions.width(5);
        lineOptions.color(Utility.getColor(context, R.color.colorAccent));
        googleMap.addPolyline(lineOptions);
    }

    public static void showDirectionPathOnMap(final Context context, final GoogleMap googleMap, final double sLat, final double sLong, final double eLat, final double eLong, final DirectionCallback callback) {
        String url = getDirectionsUrl(new LatLng(sLat, sLong), new LatLng(eLat, eLong));

        new AsyncTask<String, Void, PolylineOptions>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                callback.processStarts();
            }

            // Downloading data in non-ui thread
            @Override
            protected PolylineOptions doInBackground(String... url) {

                // For storing data from web service
                String data = "";

                try {
                    // Fetching the data from web service
                    data = downloadUrl(url[0]);
                    //=======================
                    JSONObject jObject;
                    List<List<HashMap<String, String>>> routes = null;

                    try {
                        jObject = new JSONObject(data);
                        DirectionsJSONParser parser = new DirectionsJSONParser();

                        // Starts parsing data
                        routes = parser.parse(jObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    //=======================
                    ArrayList<LatLng> points = null;
                    PolylineOptions lineOptions = null;
                    MarkerOptions markerOptions = new MarkerOptions();

                    // Traversing through all the routes
                    for (int i = 0; i < routes.size(); i++) {
                        points = new ArrayList<LatLng>();
                        lineOptions = new PolylineOptions();

                        // Fetching i-th route
                        List<HashMap<String, String>> path = routes.get(i);

                        // Fetching all the points in i-th route
                        for (int j = 0; j < path.size(); j++) {
                            HashMap<String, String> point = path.get(j);

                            double lat = Double.parseDouble(point.get("lat"));
                            double lng = Double.parseDouble(point.get("lng"));
                            LatLng position = new LatLng(lat, lng);

                            points.add(position);
                        }

                        // Adding all the points in the route to LineOptions
                        lineOptions.addAll(points);
                        lineOptions.width(2);
                        lineOptions.color(Color.RED);
                    }
                    return lineOptions;
                } catch (Exception e) {
                    Log.d("Background Task", e.toString());
                }

                return null;
            }

            // Executes in UI thread, after the execution of
            // doInBackground()
            @Override
            protected void onPostExecute(PolylineOptions result) {
                super.onPostExecute(result);
                if (result != null && googleMap != null) {
                    result.width(5);
                    result.color(Utility.getColor(context, R.color.colorAccent));
                    googleMap.addPolyline(result);
//                    callback.processFinish(result.getPoints());
                }
            }
        }.execute(url);
    }


    public static void showDirectionPathOnMapWithWayPoint(final Context context, final GoogleMap googleMap, final ArrayList<LatLng> listPaths, final boolean isGoToServer, final DirectionCallback callback) {

        String url = "";
        if (isGoToServer == true) {
            if (listPaths.size() > 0) {
                url = getRoadAPIUrl(listPaths);
            } else {
                if (callback != null) {
                    callback.processFailedWithNoRoute();
                }
                return;
            }
        }

        new AsyncTask<String, Void, PolylineOptions>() {

            ArrayList<LatLng> listLatLngs;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                callback.processStarts();
            }

            // Downloading data in non-ui thread
            @Override
            protected PolylineOptions doInBackground(String... url) {

                if (isGoToServer) {
                    Log.d("GOOGLE_MAP", "Getting directions from google api");
                    // For storing data from web service
                    String data = "";
                    try {
                        // Fetching the data from web service
                        data = downloadUrl(url[0]);
                        Log.d("GOOGLE_MAP", "url " + url[0]);
                        Log.d("GOOGLE_MAP", "response " + data);
                        //=======================
                        JSONObject jObject;
                        PolylineOptions lineOptions = new PolylineOptions();
                        try {
                            jObject = new JSONObject(data);
                            DirectionsJSONParser parser = new DirectionsJSONParser();
                            // Starts parsing data
                            listLatLngs = parser.parseRoadApi(jObject);
                            lineOptions.addAll(listLatLngs);
                            lineOptions.width(MAP_LINE);
                            lineOptions.color(Color.RED);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        /*//=======================
                        ArrayList<LatLng> points = null;

                        MarkerOptions markerOptions = new MarkerOptions();

                        // Traversing through all the routes
                        for (int i = 0; i < routes.size(); i++) {
                            points = new ArrayList<LatLng>();
                            lineOptions = new PolylineOptions();

                            // Fetching i-th route
                            List<HashMap<String, String>> path = routes.get(i);

                            // Fetching all the points in i-th route
                            for (int j = 0; j < path.size(); j++) {
                                HashMap<String, String> point = path.get(j);

                                double lat = Double.parseDouble(point.get("lat"));
                                double lng = Double.parseDouble(point.get("lng"));
                                LatLng position = new LatLng(lat, lng);

                                points.add(position);
                            }

                            // Adding all the points in the route to LineOptions
                            lineOptions.addAll(points);
                            lineOptions.width(2);
                            lineOptions.color(Color.RED);
                        }*/
                        return lineOptions;

                    } catch (Exception e) {
                        Log.d("GOOGLE_MAP", e.toString());
                    }
                } else {
                    Log.d("GOOGLE_MAP", "Direct plotting over map");
                    PolylineOptions lineOptions = new PolylineOptions();
                    listLatLngs = listPaths;
                    lineOptions.addAll(listPaths);
                    lineOptions.width(MAP_LINE);
                    lineOptions.color(Color.RED);
                    return lineOptions;
                }
                return null;
            }

            // Executes in UI thread, after the execution of
            // doInBackground()
            @Override
            protected void onPostExecute(PolylineOptions result) {
                super.onPostExecute(result);
                Log.d("GOOGLE_MAP", "onPostExecute() called with: " + "result = [" + result + "]");
                if (result != null && googleMap != null && result.getPoints().size() > 0) {
                    result.width(MAP_LINE);
                    result.color(Utility.getColor(context, R.color.colorAccent));
                    googleMap.addPolyline(result);
                    if (callback != null)
                        callback.processFinish(listLatLngs);
                } else {
                    if (callback != null)
                        callback.processFailedWithNoRoute();
                }

            }
        }.execute(url);
    }

    /*private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
        }
    }*/

    private static String getDirectionsUrl(LatLng origin, LatLng dest) {

        String key = "key=" + ConstantCode.PLACES_API_KEY;

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = key + "&" + str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private static String getRoadAPIUrl(List<LatLng> waypoints) {
        String key = "&key=" + ConstantCode.PLACES_API_KEY;
        // Origin of route

        String str_points = "&path=";
        if (waypoints != null) {
            for (LatLng waypoint : waypoints) {
                str_points += waypoint.latitude + "," + waypoint.longitude + "|";
            }
            str_points = str_points.substring(0, str_points.length() - 1);
        }
        String interpolate = "interpolate=true";

        String url = "https://roads.googleapis.com/v1/snapToRoads?" + interpolate + key + str_points;

        return url;
    }

    private static String getDirectionsUrl(LatLng origin, LatLng dest, List<LatLng> waypoints) {

        String key = "key=" + ConstantCode.PLACES_API_KEY;
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String parameters = key + "&" + str_origin + "&" + str_dest;

        String str_waypoint = "waypoints=";
        if (waypoints != null) {
            for (LatLng waypoint : waypoints) {
                str_waypoint += waypoint.latitude + "," + waypoint.longitude + "|";
            }
            str_waypoint = str_waypoint.substring(0, str_waypoint.length() - 1);
            parameters += "&" + str_waypoint;
        }

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        parameters += "&" + sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private static String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public interface DirectionCallback {
        public void processStarts();

        public void processFinish(ArrayList<LatLng> list);

        public void processFailedWithNoRoute();
    }

}
