package com.carnot;

import android.app.Application;
import android.content.Context;

import com.activeandroid.ActiveAndroid;
import com.android.volley.VolleyLog;
import com.carnot.activity.ActivityCarProfile;
import com.carnot.global.PrefManager;


import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import net.hockeyapp.android.CrashManager;

/**
 * Created by root on 18/4/16.
 */
public class App extends Application {


    public static Context getContext() {
        return context;
    }
    /** Instance of the current application. */
    private static App context;

    /**
     * Constructor.
     */
    public App() {
        context = this;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        context = this;
        ActivityCarProfile.IS_FIRST_TIME = true;

        //Initializing active android
        ActiveAndroid.initialize(this);
        PrefManager.init(this);

        VolleyLog.setTag("VolleyLogs");

        crash();
    }

    //CRASH ANALYTICS
    private void crash() {
        CrashManager.register(this);
    }
}
