package com.carnot.network;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public interface RequestLifeCycle {

    public NetworkResponse validateResponse(JSONObject jsonObject);

    public HashMap<String, String> getHeaders(WebServiceModel webService);

    public boolean isInternetConnected();

    public File createNewVideoFile();

    public File createNewImageFile();

    public String getNoInternetMessage();
}