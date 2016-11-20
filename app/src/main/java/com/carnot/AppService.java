package com.carnot;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class AppService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        final ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                do {
                    String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
                    Log.e("OPENED_APP", packageName);
                } while (true);
            }
        }.execute();

        return START_NOT_STICKY;
    }
}
