package com.carnot.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;

import com.carnot.R;


/**
 * Created by pankaj on 30/3/16.
 */
public class AppRadioButtom extends AppCompatRadioButton {

    private Typeface typeface;
    public AppRadioButtom(Context context) {
        super(context);
        init();
    }

    public AppRadioButtom(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.appRadioButtonStyle);
        init();
    }

    public AppRadioButtom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.appRadioButtonStyle);
        init();
    }

    private void init() {
        if (typeface == null)
            typeface = Typeface.createFromAsset(getContext().getAssets(), "Grotesk_Light.otf");
        setTypeface(typeface);
    }
}
