package com.activeandroid;

import java.util.ArrayList;

/**
 * Created by pankaj on 6/29/16.
 */
public interface OnReadAsync {
    public <T extends Model> void onRead(ArrayList<T> list);
}
