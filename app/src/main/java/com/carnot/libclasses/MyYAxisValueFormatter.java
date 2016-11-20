package com.carnot.libclasses;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.Utils;

import java.text.DecimalFormat;

public class MyYAxisValueFormatter implements YAxisValueFormatter {



    @Override
    public String getFormattedValue(float value, YAxis yAxis) {

        return Utils.formatNumber(value, 0, true);

    }
}