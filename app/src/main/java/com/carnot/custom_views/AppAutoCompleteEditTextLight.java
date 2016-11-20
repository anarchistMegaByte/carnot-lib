package com.carnot.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;

import com.carnot.R;

public class AppAutoCompleteEditTextLight extends AppCompatAutoCompleteTextView {

    static Typeface typeface = null;

    public AppAutoCompleteEditTextLight(Context context) {
        super(context);
        init();
    }

    public AppAutoCompleteEditTextLight(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.appAutoEditTextStyle);
        init();
    }

    public AppAutoCompleteEditTextLight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.appAutoEditTextStyle);
        init();
    }


    private void init() {
        if (typeface == null)
            typeface = Typeface.createFromAsset(getContext().getAssets(), "Grotesk_Light.otf");
        setTypeface(typeface);
    }

}