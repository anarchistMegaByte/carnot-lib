package com.carnot.libclasses;

public interface Act_ImpMethods {

    public static final int MESSAGE_TYPE_SUCCESS = 1;
    public static final int MESSAGE_TYPE_WARNING = 2;
    public static final int MESSAGE_TYPE_ERROR = 3;

    public void initVariable();

    public void initView();

    public void postInitView();

    public void addAdapter();

    public void loadData();
}
