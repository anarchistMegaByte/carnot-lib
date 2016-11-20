package com.carnot.global.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.carnot.R;
import com.carnot.global.ConstantCode;
import com.carnot.global.PrefManager;
import com.carnot.global.Utility;
import com.carnot.models.User;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.util.HashMap;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token);


        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            PrefManager.getInstance().edit().putBoolean(ConstantCode.SENT_TOKEN_TO_SERVER, false).commit();

        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(ConstantCode.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(final String token) {
        // Add custom implementation, as needed.

        final User user = Utility.getLoggedInUser();
        if (user != null) {

            //saving the registration id in loggedin user
            user.registration_gcm = token;
            user.save();
            Utility.setLoggedInUser(user);

            HashMap<String, Object> params = new HashMap<>();
            params.put(ConstantCode.user_id, user.id);
            params.put(ConstantCode.reg_id, token);
            params.put(ConstantCode.platform, "Android");

            Log.e("RegisterationService : ", token + "");

            WebUtils.call(WebServiceConfig.WebService.REGISTER_FOR_GCM, null, params, new NetworkCallbacks() {
                @Override
                public void successWithString(Object values, WebServiceConfig.WebService webService) {
                    super.successWithString(values, webService);

                    PrefManager.getInstance().edit().putBoolean(ConstantCode.SENT_TOKEN_TO_SERVER, true).commit();

                }

                @Override
                public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                    super.failedWithMessage(values, webService);

                }

                @Override
                public void loadOffline() {
                    super.loadOffline();

                }
            });
        }
    }
}
