package com.activeandroid;

import java.util.ArrayList;

/**
 * Created by pankaj on 6/29/16.
 */
public interface OnReadData<T extends Model> {
    public void onRead(T type, ArrayList<T> list);
}
