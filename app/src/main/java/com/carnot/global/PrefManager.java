package com.carnot.global;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by root on 18/4/16.
 */
public class PrefManager {
    static SharedPreferences preferences;

    public static void init(Context context) {
        SharedPreferences generalPref = getGeneralPref(context);
        if (generalPref.contains(ConstantCode.PREF_LOGGED_IN_USER_ID))
            preferences = context.getSharedPreferences("SharedPref_" + generalPref.getString(ConstantCode.PREF_LOGGED_IN_USER_ID, ""), Context.MODE_PRIVATE);
        else {
            preferences = context.getSharedPreferences("SharedPref", Context.MODE_PRIVATE);
        }
    }

    public static SharedPreferences getGeneralPref(Context context) {
        return context.getSharedPreferences("GeneralPref", Context.MODE_PRIVATE);
    }

    public static SharedPreferences getInstance() {
        if (preferences != null) {
            return preferences;
        }
        Log.e("PrefManager", "Forgot to call PrefManager.init(context) ??");
        return null;
    }

    //String
    public static void putString(String key, String value) {
        if (getInstance() != null) {
            getInstance().edit().putString(key, value).commit();
        }
    }

    public static String getString(String key) {
        if (getInstance() != null) {
            return getInstance().getString(key, "");
        }
        return "";
    }

    //Integer
    public static void putInt(String key, int value) {
        if (getInstance() != null) {
            getInstance().edit().putInt(key, value).commit();
        }
    }

    public static int getInt(String key) {
        if (getInstance() != null) {
            return getInstance().getInt(key, 0);
        }
        return 0;
    }

    //boolean
    public static void putBoolean(String key, boolean value) {
        if (getInstance() != null) {
            getInstance().edit().putBoolean(key, value).commit();
        }
    }

    public static boolean getBoolean(String key) {
        if (getInstance() != null) {
            return getInstance().getBoolean(key, false);
        }
        return false;
    }

    //boolean
    public static void putLong(String key, long value) {
        if (getInstance() != null) {
            getInstance().edit().putLong(key, value).commit();
        }
    }

    public static long getLong(String key) {
        if (getInstance() != null) {
            return getInstance().getLong(key, 0);
        }
        return 0;
    }
}
