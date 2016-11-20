package com.carnot.network;

import android.content.Context;

import com.carnot.R;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

/**
 * Created by pankaj on 7/8/15.
 */
public class CustomRequestLifeCycle implements RequestLifeCycle {

    private Context context;
    private static CustomRequestLifeCycle request;

    private CustomRequestLifeCycle(Context context) {
        this.context = context;
    }

    public static CustomRequestLifeCycle getInstance(Context context) {
        if (request == null) {
            request = new CustomRequestLifeCycle(context);
        }
        return request;
    }

    //=========================================================================
    //=================== U S E R   S P E C I F I C   C O D E S ===============
    //=========================================================================


    @Override
    public NetworkResponse validateResponse(JSONObject json) {
        NetworkResponse response = new NetworkResponse();
        if (json != null) {
            response.response = json.toString();
            if (json.has(ConstantCode.IN_STATUS)) {
                if ("true".equalsIgnoreCase(json.optString(ConstantCode.IN_STATUS)) || "200".equalsIgnoreCase(json.optString(ConstantCode.IN_STATUS))) {
                    response.code = NetworkResponse.SUCCESS;
                    return response;
                } else {
//                    failMessage.append(json.has(ConstantCode.IN_MESSAGE) ? json.optString(ConstantCode.IN_MESSAGE) : "No Message for failed");
                    response.code = NetworkResponse.FAILED_WITH_MESSAGE;
                    response.errorMessage = json.has(ConstantCode.IN_MESSAGE) ? json.optString(ConstantCode.IN_MESSAGE) : "No Message for failed";
                    if (json.has(ConstantCode.IN_MESSAGE)) {
                        if (json.optString(ConstantCode.IN_MESSAGE).contains("Invalid Token")) {
                            response.code = NetworkResponse.FAILED;
                        }
                    }
//                    if (response.errorMessage.contains("Invalid Token")) {
//                        response.code = NetworkResponse.FAILED;
//                    }
                    return response;
                }
            }
        }
        response.code = NetworkResponse.FAILED_WITH_MESSAGE;
        response.errorMessage = context.getString(R.string.server_error);
//        failMessage.append(context.getString(R.string.server_error));
        return response;
    }

    @Override
    public HashMap<String, String> getHeaders(WebServiceModel webService) {
        HashMap<String, String> headers = new HashMap<String, String>();

        //Sending token for the currently logged in user
        if (Utility.getLoggedInUser() != null) {
            headers.put("Authorization", "Token " + Utility.getLoggedInUser().token);
        }
//        headers.put("Authorization", "Token " + ConstantCode.DEFAULT_USER_TOKEN);
        headers.put("Apikey", ConstantCode.DEFAULT_API_KEY);
        return headers;
    }

    public String getNoInternetMessage() {
        return context.getString(R.string.msg_internet_alert);
    }

    @Override
    public boolean isInternetConnected() {
        return Utility.isInternetConnected(context);
    }

    @Override
    public File createNewVideoFile() {
        return Utility.createNewVideoFile();
    }

    @Override
    public File createNewImageFile() {
        return null;
    }
}
