package com.carnot.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.carnot.R;

public class AppEditTextLight extends AppCompatEditText {

    static Typeface typeface = null;

    public AppEditTextLight(Context context) {
        super(context);
        init();
    }

    public AppEditTextLight(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.appEditTextStyle);
        init();
    }

    public AppEditTextLight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.appEditTextStyle);
        init();
    }


    private void init() {
        if (typeface == null)
            typeface = Typeface.createFromAsset(getContext().getAssets(), "Grotesk_Light.otf");
        setTypeface(typeface);
    }

}

