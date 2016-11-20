package com.carnot.global.exception_handler;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.carnot.App;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A StackInfoSender that performs an individual http POST to a URL for each
 * stack info provided. The http requests will be performed inside of a single
 * <code>AsyncTask</code>, so submitStackInfos must be called from the main thread.
 * <p/>
 * The data sent is identical to the data sent in
 * the original android-remote-stacktrace:
 * <ul>
 * <li>package_name
 * <li>package_version
 * <li>phone_model
 * <li>android_version
 * <li>stacktrace
 * </ul>
 *
 * @author pretz
 */
public class HttpPostStackInfoSender implements StackInfoSender {

    private static final String TAG = "HttpPostStackInfoSender";

    private final String mPostUrl;

    /**
     * Construct a new HttpPostStackInfoSender that will submit
     * stack traces by POSTing them to postUrl.
     */
    public HttpPostStackInfoSender(String postUrl) {
        mPostUrl = postUrl;
    }

    public void submitStackInfos(Collection<StackInfo> stackInfos, final String packageName) {

        Log.d(TAG, "submitStackInfos() called with: " + "stackInfos = [" + stackInfos + "], packageName = [" + packageName + "]");
        for (final StackInfo info : stackInfos.toArray(new StackInfo[0])) {


            StringRequest request = new StringRequest(Request.Method.POST, mPostUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Log.d(TAG, "getParams() called with: " + "");
                    HashMap<String, String> nvps = new HashMap<String, String>();
                    nvps.put("package_name", packageName);
                    nvps.put("package_version", info.getPackageVersion());
                    nvps.put("phone_model", info.getPhoneModel());
                    nvps.put("android_version", info.getAndroidVersion());
                    nvps.put("stacktrace", TextUtils.join("\n", info.getStacktrace()));

                    return nvps;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(App.getContext());
            queue.add(request);
        }

       /* new AsyncTask<StackInfo, Void, Void>() {

            @Override
            protected Void doInBackground(StackInfo... infos) {
                final DefaultHttpClient httpClient = new DefaultHttpClient();
                for (final StackInfo info : infos) {
                    HttpPost httpPost = new HttpPost(mPostUrl);
                    final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                    nvps.add(new BasicNameValuePair("package_name", packageName));
                    nvps.add(new BasicNameValuePair("package_version", info.getPackageVersion()));
                    nvps.add(new BasicNameValuePair("phone_model", info.getPhoneModel()));
                    nvps.add(new BasicNameValuePair("android_version", info.getAndroidVersion()));
                    nvps.add(new BasicNameValuePair("stacktrace", TextUtils.join("\n", info.getStacktrace())));
                    try {
                        httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
                        // We don't care about the response, so we just hope it went well and on with it
                        httpClient.execute(httpPost);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Error sending stack traces", e);
                    } catch (ClientProtocolException e) {
                        Log.e(TAG, "Error sending stack traces", e);
                    } catch (IOException e) {
                        Log.e(TAG, "Error sending stack traces", e);
                    }
                }
                return null;
            }
        }.execute(stackInfos.toArray(new StackInfo[0]));*/
    }

}
