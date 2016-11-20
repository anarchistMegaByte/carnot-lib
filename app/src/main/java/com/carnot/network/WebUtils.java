package com.carnot.network;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.carnot.App;
import com.carnot.R;
import com.carnot.activity.ActivitySignup;
import com.carnot.global.Utility;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by root on 22/4/16.
 */
public class WebUtils {

    private static final boolean useOK_HTTP = false;

    public static void call(final WebServiceConfig.WebService webService, String urlParams[], HashMap<String, Object> params, NetworkCallbacks response) {

        final WebServiceModel webServiceModel = WebServiceConfig.getMethods().get(webService);

        if (CustomRequestLifeCycle.getInstance(App.getContext()).isInternetConnected()) {
            if (webServiceModel.method == Request.Method.GET) {
                get(webService, urlParams, params, response);
            } else {
                post(webService, urlParams, params, response);
            }
        } else {
            response.loadOffline();
        }
    }

    public static void get(final WebServiceConfig.WebService webService, String urlParams[], final HashMap<String, Object> params, final NetworkCallbacks networkCallbacks) {

        showLog("===================================");
//        HashMap<String, String> params = null;
//        JSONObject param = new JSONObject(params);
        final CustomRequestLifeCycle lifeCycle = CustomRequestLifeCycle.getInstance(App.getContext());
        final WebServiceModel webServiceModel = WebServiceConfig.getMethods().get(webService);
        final String url = String.format(webServiceModel.url, urlParams);
        showLog("URL [GET]: " + url);
        showLogOfParam(params);

        if (useOK_HTTP) {
            new AsyncTask<Void, Void, Object>() {

                @Override
                protected Object doInBackground(Void... pp) {
                    try {
                        OkHttpClient client = new OkHttpClient();
                        MediaType mediaType = MediaType.parse("application/octet-stream");
                        RequestBody body = RequestBody.create(mediaType, params != null ? new JSONObject(params).toString() : "");
                        okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
                                .url(url)
                                .get();

                        HashMap<String, String> headers = lifeCycle.getHeaders(webServiceModel);
                        Iterator<Map.Entry<String, String>> it = headers.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            builder.addHeader(pair.getKey() + "", pair.getValue() + "");
                        }

                        okhttp3.Response response = client.newCall(builder.build()).execute();

                        okhttp3.Response responseOkHttp = (okhttp3.Response) response;
                        ResponseBody responseBody = responseOkHttp.body();
                        return new JSONObject(responseBody.string());

                    } catch (Exception e) {
                        return e;
                    }
                }

                @Override
                protected void onPostExecute(Object response) {
                    super.onPostExecute(response);

                    try {
                        showLog("**********");
                        showLog("URL [GET]: " + url);
                        showLogOfParam(params);
                        showLog("RESPONSE : " + response.toString());
                        showLog("**********");
                        NetworkResponse networkResponse = lifeCycle.validateResponse((JSONObject) response);
                        if (networkResponse.code == NetworkResponse.SUCCESS) {
                            networkCallbacks.successWithString(response, webService);
                        } else if (networkResponse.code == NetworkResponse.FAILED_WITH_MESSAGE) {
                            networkCallbacks.failedWithMessage(networkResponse.errorMessage, webService);
                        } else if (networkResponse.code == NetworkResponse.FAILED) {
                            //Use in case some token expires to logout or not
                            networkCallbacks.failedWithMessage(networkResponse.errorMessage, webService);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        networkCallbacks.failedWithMessage(e.getMessage(), webService);
                    }
                }
            }.execute();
        } else {

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(webServiceModel.method, url, params != null ? new JSONObject(params) : null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    showLog("**********");
                    showLog("URL [GET]: " + url);
                    showLogOfParam(params);
                    showLog("RESPONSE : " + response.toString());
                    showLog("**********");
                    NetworkResponse networkResponse = lifeCycle.validateResponse(response);
                    if (networkResponse.code == NetworkResponse.SUCCESS) {
                        networkCallbacks.successWithString(response, webService);
                    } else if (networkResponse.code == NetworkResponse.FAILED_WITH_MESSAGE) {
                        networkCallbacks.failedWithMessage(networkResponse.errorMessage, webService);
                    } /*else if (networkResponse.code == NetworkResponse.FAILED) {
                        //Use in case some token expires to logout or not
                        networkCallbacks.failedWithMessage(networkResponse.errorMessage, webService);
                    } */ else if (networkResponse.code == NetworkResponse.FAILED) {
                        //Use in case some token expires to logout or not
                        Utility.logoutExistingUser();
                        Intent intent = new Intent(App.getContext(), ActivitySignup.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        App.getContext().startActivity(intent);
//                        networkCallbacks.failedWithMessage(networkResponse.errorMessage, webService);
                    }

                    //THIS IS TEMPORARY until client change service response to proper {status, message, data} format
                /*if (response != null && response.toString().trim().length() > 2) {
                    networkCallbacks.successWithString(response, webService);
                } else {
                    networkCallbacks.failedWithMessage(response, webService);
                }*/
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    showLog("**********");
                    showLog("URL [GET]: " + url);
                    showLogOfParam(params);
                    showLogE("RESPONSE : " + (error.networkResponse == null ? (error != null ? error.getLocalizedMessage() : "NullVolleyError") : new String(error.networkResponse.data)));
                    showLog("**********");
//                    networkCallbacks.failedWithMessage((error.networkResponse == null ? "" : new String(error.networkResponse.data)), webService);
                    networkCallbacks.failedWithMessage((error.networkResponse == null ? "" : App.getContext().getString(R.string.server_error)), webService);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    //sending JSONObject that's why no need to send it.
                /*if (params != null) {
                    return params;
                }*/
                    return super.getParams();
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = lifeCycle.getHeaders(webServiceModel);
                    headers.putAll(super.getHeaders());
//                    headers.put("WWW-Authenticate", "xBasic realm=\"fake\"");
                    showLogOfHeaders(headers);
                    return headers;
                /*Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token 04af7328b2346602ad7d581ff738eb25a5482cbf");
                params.put("Apikey", "JXn8e6C29jhZ065i8wyQktY33YD3s9zy");
                return params;*/
                }

                @Override
                public byte[] getBody() {
                    return super.getBody();
                }
            };

            jsonObjReq.setTag(url);

            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(5 * DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 0));
//            RequestQueue queue = Volley.newRequestQueue(App.getContext());
            getRequestQueue().add(jsonObjReq);
        }
        showLog("===================================");
    }

    private static RequestQueue requestQueue;

    public static RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(App.getContext());
        }
        return requestQueue;
    }


    public static void post(final WebServiceConfig.WebService webService, String urlParams[], final HashMap<String, Object> params, final NetworkCallbacks networkCallbacks) {

//        HashMap<String, String> params = null;
//        JSONObject param = new JSONObject(params);
        /*try {
            param.put("email", "1@openxcell.com");
            param.put("password", "123456");
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        final CustomRequestLifeCycle lifeCycle = CustomRequestLifeCycle.getInstance(App.getContext());
        final WebServiceModel webServiceModel = WebServiceConfig.getMethods().get(webService);
        final String url = String.format(webServiceModel.url, urlParams);
        showLog("URL [POST]: " + url);
        showLogOfParam(params);

        if (useOK_HTTP) {
            new AsyncTask<Void, Void, Object>() {

                @Override
                protected Object doInBackground(Void... pp) {
                    try {
                        OkHttpClient client = new OkHttpClient();
                        MediaType mediaType = MediaType.parse("application/octet-stream");
                        RequestBody body = RequestBody.create(mediaType, params != null ? new JSONObject(params).toString() : "");
                        okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
                                .url(url)
                                .post(body);

                        HashMap<String, String> headers = lifeCycle.getHeaders(webServiceModel);
                        Iterator<Map.Entry<String, String>> it = headers.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            builder.addHeader(pair.getKey() + "", pair.getValue() + "");
                        }

                        okhttp3.Response response = client.newCall(builder.build()).execute();

                        okhttp3.Response responseOkHttp = (okhttp3.Response) response;
                        ResponseBody responseBody = responseOkHttp.body();
                        return new JSONObject(responseBody.string());

                    } catch (Exception e) {
                        return e;
                    }
                }

                @Override
                protected void onPostExecute(Object response) {
                    super.onPostExecute(response);
                    try {
                        showLog("**********");
                        showLog("URL [POST]: " + url);
                        showLogOfParam(params);
                        if (response != null)
                            showLog("RESPONSE : " + response.toString());
                        showLog("**********");
                        NetworkResponse networkResponse = lifeCycle.validateResponse((JSONObject) response);
                        if (networkResponse.code == NetworkResponse.SUCCESS) {
                            networkCallbacks.successWithString(response, webService);
                        } else if (networkResponse.code == NetworkResponse.FAILED_WITH_MESSAGE) {
                            networkCallbacks.failedWithMessage(networkResponse.errorMessage, webService);
                        } else if (networkResponse.code == NetworkResponse.FAILED) {
                            //Use in case some token expires to logout or not
                            networkCallbacks.failedWithMessage(networkResponse.errorMessage, webService);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        networkCallbacks.failedWithMessage(e.getMessage(), webService);
                    }
                }
            }.execute();
        } else {

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(webServiceModel.method, url, params != null ? new JSONObject(params) : null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    showLog("**********");
                    showLog("URL [POST]: " + url);
                    showLogOfParam(params);
                    showLog("RESPONSE : " + response.toString());
                    showLog("**********");
                    NetworkResponse networkResponse = lifeCycle.validateResponse(response);
                    if (networkResponse.code == NetworkResponse.SUCCESS) {
                        networkCallbacks.successWithString(response, webService);
                    } else if (networkResponse.code == NetworkResponse.FAILED_WITH_MESSAGE) {
                        networkCallbacks.failedWithMessage(networkResponse.errorMessage, webService);
                    } else if (networkResponse.code == NetworkResponse.FAILED) {
                        //Use in case some token expires to logout or not
                        Utility.logoutExistingUser();
                        Intent intent = new Intent(App.getContext(), ActivitySignup.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        App.getContext().startActivity(intent);
//                        networkCallbacks.failedWithMessage(networkResponse.errorMessage, webService);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    showLog("**********");
                    showLog("URL [POST]: " + url);
                    showLogOfParam(params);
                    showLogE("RESPONSE : " + (error.networkResponse == null ? (error != null ? error.getLocalizedMessage() : "NullVolleyError") : new String(error.networkResponse.data)));
                    showLog("**********");

                    networkCallbacks.failedWithMessage((error.networkResponse == null ? "" : App.getContext().getString(R.string.server_error)), webService);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    //sending JSONObject that's why no need to send it.
                /*if (params != null)
                    return params;*/
                    return super.getParams();
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = lifeCycle.getHeaders(webServiceModel);
                    headers.putAll(super.getHeaders());
//                    headers.put("WWW-Authenticate", "xBasic realm=\"fake\"");
                    showLogOfHeaders(headers);
                    return headers;
                /*Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token 04af7328b2346602ad7d581ff738eb25a5482cbf");
                params.put("Apikey", "JXn8e6C29jhZ065i8wyQktY33YD3s9zy");
                return params;*/
                }
            };
            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(5 * DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 0));
//            RequestQueue queue = Volley.newRequestQueue(App.getContext());
            getRequestQueue().add(jsonObjReq);
        }
        showLog("===================================");
    }

    private static void multipart() {
        new PhotoMultipartRequest();
    }

    private static void showLogOfParam(HashMap<String, Object> params) {
        if (params != null) {
            showLog("PARAMS");
            Set<String> keys = params.keySet();
            for (String key : keys) {
                String value = params.get(key) + "";
                showLog(key + " : " + value);
            }
        } else {
            showLog("PARAMS : No Params");
        }
    }

    private static void showLogOfHeaders(HashMap<String, String> params) {
        if (params != null) {
            showLog("HEADERS");
            Set<String> keys = params.keySet();
            for (String key : keys) {
                String value = params.get(key);
                showLog(key + " : " + value);
            }
        } else {
            showLog("HEADERS : No Params");
        }
    }

    private static void showLog(String message) {
        Log.i("ws", message + "");
    }

    private static void showLogE(String message) {
        Log.e("ws", message + "");
    }

    private static ImageLoader imageLoader;

    public static <T extends NetworkImageView> void loadImage(String url, T view, int defaultRes, int errorRes) {
        imageLoader = CustomVolleyRequest.getInstance(App.getContext()).getImageLoader();
        imageLoader.get(url, ImageLoader.getImageListener(view, defaultRes, errorRes));
        view.setImageUrl(url, imageLoader);
    }

    public static void cancelAll() {
        getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }
}
