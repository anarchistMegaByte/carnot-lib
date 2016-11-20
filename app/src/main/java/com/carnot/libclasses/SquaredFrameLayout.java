package com.carnot.libclasses;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by root on 7/4/16.
 */
public class SquaredFrameLayout extends FrameLayout {

    public SquaredFrameLayout(Context context) {
        super(context);
    }

    public SquaredFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquaredFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
