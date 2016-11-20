package com.carnot.models;

import android.text.TextUtils;

/**
 * Created by pankaj on 6/9/16.
 */
public class Contacts {
    public String name;
    public String number;
    public String email;

    @Override
    public String toString() {
        return name + (TextUtils.isEmpty(number) ? "" : " [" + number + "]");
    }
}
