package com.carnot.network;

import org.json.JSONException;

public interface NetworkMethods {
    //	public MRequest requestService(ContentValues values, final WebService webService);
    public void successWithString(Object values, WebServiceConfig.WebService webService) throws JSONException;

    public void failedWithMessage(Object values, WebServiceConfig.WebService webService);

    public void failedForNetwork(Object values, WebServiceConfig.WebService webService);
}
