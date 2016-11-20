package com.carnot.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class AppTextViewLight extends AppCompatTextView {

    static Typeface typeface = null;

    public AppTextViewLight(Context context) {
        super(context);
        init();
    }

    public AppTextViewLight(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AppTextViewLight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        if (typeface == null)
            typeface = Typeface.createFromAsset(getContext().getAssets(), "Grotesk_Light.otf");

        setTypeface(typeface);
    }

}
