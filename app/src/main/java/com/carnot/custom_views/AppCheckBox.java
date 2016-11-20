package com.carnot.custom_views;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import com.carnot.R;


/**
 * Created by pankaj on 30/3/16.
 */
public class AppCheckBox extends AppCompatCheckBox {
    public AppCheckBox(Context context) {
        super(context);
        init();
    }

    public AppCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.appCheckBoxStyle);
        init();
    }

    public AppCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.appCheckBoxStyle);
        init();
    }

    private void init() {

    }
}
