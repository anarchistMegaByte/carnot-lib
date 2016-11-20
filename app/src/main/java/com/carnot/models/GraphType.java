package com.carnot.models;

/**
 * Created by pankaj on 7/23/16.
 */
public class GraphType {
    public static final int MILEAGE = 1;
    public static final int COST = 2;
    public static final int DISTANCE = 3;
    public static final int DURATION = 4;
    public int type;
    public String message;

    public GraphType(int type, String msg) {
        this.type = type;
        this.message = msg;
    }
}
