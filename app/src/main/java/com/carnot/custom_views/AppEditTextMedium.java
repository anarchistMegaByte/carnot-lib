package com.carnot.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

/**
 * Created by pankaj on 31/3/16.
 */
public class AppEditTextMedium extends AppCompatEditText {

    static Typeface typeface = null;

    public AppEditTextMedium(Context context) {
        super(context);
        init();
    }

    public AppEditTextMedium(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AppEditTextMedium(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        if (typeface == null)
            typeface = Typeface.createFromAsset(getContext().getAssets(), "Grotesk_Medium.otf");

        setTypeface(typeface);
    }

}
