package com.carnot.libclasses;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Created by javid on 25/4/16.
 */
public class NavigationHelper {

    public static void CallNextScreen(final Context mContext, final Class<?> toClass, boolean withFinish, boolean isShowAnimation) {
        try {
            Intent intent = new Intent();
            intent.setClass(mContext, toClass);
            mContext.startActivity(intent);
            if (withFinish)
                ((Activity) mContext).finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void CallNextScreenWithResultCallBack(final Context mContext, final Class<?> toClass, int responseCode, boolean withFinish, boolean isShowAnimation) {
        try {
            Intent intent = new Intent();
            intent.setClass(mContext, toClass);
            ((Activity) mContext).startActivityForResult(intent, responseCode);
            if (withFinish)
                ((Activity) mContext).finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void CallNextScreen(final Context mContext, Intent intent, boolean withFinish, boolean isShowAnimation) {
        try {
            mContext.startActivity(intent);
            if (withFinish)
                ((Activity) mContext).finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void CallNextScreenWithResultCallBack(final Context mContext, Intent intent, int responseCode, boolean withFinish, boolean isShowAnimation) {
        try {
            if (intent == null)
                return;

            ((Activity) mContext).startActivityForResult(intent, responseCode);
            if (withFinish)
                ((Activity) mContext).finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
