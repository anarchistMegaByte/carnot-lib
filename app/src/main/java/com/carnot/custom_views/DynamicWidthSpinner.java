package com.carnot.custom_views;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

/**
 * Created by pankaj on 5/3/16.
 */
public class DynamicWidthSpinner extends AppCompatSpinner {

    public DynamicWidthSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicWidthSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DynamicWidthSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    //
    public void measureContentWidth(int position) {
        if (getAdapter() == null) {
            return;
        }
        View view = getAdapter().getView(position, null, this);
        if (view.getLayoutParams() == null) {
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        view.measure(MeasureSpec.makeMeasureSpec(0,
                MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0,
                MeasureSpec.UNSPECIFIED));
        int width = view.getMeasuredWidth();
        width += 45;
        getLayoutParams().width = width;
    }

    @Override
    public void setOnItemSelectedListener(final OnItemSelectedListener listener) {
        super.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                listener.onItemSelected(parent, view, position, id);
//                measureContentWidth(position);
                /*new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        measureContentWidth(position);
                    }
                }, 500);*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                listener.onNothingSelected(parent);
            }
        });
    }


}
