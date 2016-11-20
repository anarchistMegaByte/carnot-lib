package com.carnot.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.carnot.R;

/**
 * Created by pankaj on 30/3/16.
 */
public class AppButton extends AppCompatButton {
    Typeface typeface;

    public AppButton(Context context) {
        super(context);
        init();
    }

    public AppButton(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.appButtonStyle);
        init();
    }

    public AppButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.appButtonStyle);
        init();
    }

    private void init() {
        if (typeface == null)
            typeface = Typeface.createFromAsset(getContext().getAssets(), "Grotesk_Medium.otf");

        setTypeface(typeface);
    }
}
