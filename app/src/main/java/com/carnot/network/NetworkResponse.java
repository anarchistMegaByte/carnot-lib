package com.carnot.network;

/**
 * Created by root on 29/9/15.
 */
public class NetworkResponse {
    public static final int FAILED = 100;
    public static final int FAILED_WITH_MESSAGE = 101;
    public static final int SUCCESS = 102;
    public int code;
    public String response, errorMessage;
}
