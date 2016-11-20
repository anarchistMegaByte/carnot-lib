package com.carnot.network;


public class WebServiceModel {

    public String url;
    public int method;
    public boolean isAddHeader;

    public WebServiceModel(String url, int method, boolean isAddHeader) {
        // TODO Auto-generated constructor stub
        this.url = url;
        this.method = method;
        this.isAddHeader = isAddHeader;
    }
}
