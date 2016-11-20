package com.carnot.network;

public abstract class NetworkCallbacks implements NetworkMethods {

    @Override
    public void successWithString(Object values, WebServiceConfig.WebService webService) {

    }

    @Override
    public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {

    }

    @Override
    public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {

    }

    public void loadOffline() {

    }
}
