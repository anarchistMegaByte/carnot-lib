package com.carnot.custom_views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by pankaj on 7/14/16.
 */
public class EditableLinearLayout extends LinearLayout {
    public EditableLinearLayout(Context context) {
        super(context);
    }

    public EditableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    boolean isInEditMode = false;

    public void setInEditMode(boolean inEditMode) {
        isInEditMode = inEditMode;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isInEditMode)
            return super.onInterceptTouchEvent(ev);
        else
            return true;
    }
}
