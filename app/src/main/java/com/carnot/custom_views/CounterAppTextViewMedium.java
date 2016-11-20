package com.carnot.custom_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.global.Utility;

/**
 * Created by pankaj on 31/3/16.
 */
public class CounterAppTextViewMedium extends LinearLayout {

    private TextView txtCounter, txtName;

    public CounterAppTextViewMedium(Context context) {
        super(context);
    }

    public CounterAppTextViewMedium(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CounterAppTextViewMedium(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }


    private void init(AttributeSet attrs, int defStyleAttr) {

        inflate(getContext(), R.layout.include_view_counter_text_view, this);
        txtCounter = (TextView) findViewById(R.id.txt_counter);
        txtName = (TextView) findViewById(R.id.txt_title);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ArcView, defStyleAttr, 0);
        String text = a.getString(R.styleable.ArcView_text);
        txtName.setText(text);
    }

    public void setCounter(double value1, double value2) {
        try {
            txtCounter.setText(Utility.roundToInt(value1) + "/" + Utility.roundToInt(value2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCounter(int no) {

        if (no < 0) {
            txtCounter.setText(no + "");
            txtCounter.setTextColor(Utility.getColor(getContext(), R.color.red));
        } else {
            txtCounter.setText("+" + no + "");
            txtCounter.setTextColor(Utility.getColor(getContext(), R.color.green_text_color));
        }
    }

    public void setText(String text) {
        txtName.setText(text);
    }
}
