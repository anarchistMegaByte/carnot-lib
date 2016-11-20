package com.carnot.libclasses;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

/**
 * Created by anarchistmegabyte on 22/8/16.
 */
public class MyValueFormatter implements ValueFormatter {

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        int m1 = (int) Math.floor(value);
        int m2 = (int) Math.floor((value-m1)*100);
        Log.e("TAG", String.valueOf(m1)+ ":"+String.valueOf(m2));
        return m1 + ":" + m2 + "Hrs";
    }

}
