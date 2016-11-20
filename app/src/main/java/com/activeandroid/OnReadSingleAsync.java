package com.activeandroid;

/**
 * Created by pankaj on 6/29/16.
 */
public interface OnReadSingleAsync {
    public <T extends Model> void onRead(T model);
}
