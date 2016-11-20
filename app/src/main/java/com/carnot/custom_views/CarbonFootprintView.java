package com.carnot.custom_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.carnot.R;

/**
 * Created by root on 6/4/16.
 */
public class CarbonFootprintView extends LinearLayout {
    FrameLayout frameLayout;
    int width;
    double current, total;
    TextView txtProgress, txtName;
    View viewGrey;
    ImageView imgCar;
    boolean hideCar = false;

    public CarbonFootprintView(Context context) {
        super(context);
    }

    public CarbonFootprintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CarbonFootprintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }


    private void init(AttributeSet attrs, int defStyleAttr) {
        inflate(getContext(), R.layout.include_view_carbon_footprint, this);
        frameLayout = (FrameLayout) findViewById(R.id.frm_progress);
        txtName = (TextView) findViewById(R.id.txt_name);
        txtProgress = (TextView) findViewById(R.id.txt_progress);
        viewGrey = (View) findViewById(R.id.view_grey);
        imgCar = (ImageView) findViewById(R.id.img_car);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CarbonFootprintView, defStyleAttr, 0);

        String name = a.getString(R.styleable.CarbonFootprintView_name);
        String progress = a.getString(R.styleable.CarbonFootprintView_progress);
        hideCar = a.getBoolean(R.styleable.CarbonFootprintView_hideCar, false);

        if (hideCar) {
            imgCar.setVisibility(View.INVISIBLE);
            viewGrey.setVisibility(View.GONE);
            ((LayoutParams) ((View) viewGrey.getParent()).getLayoutParams()).width = LayoutParams.WRAP_CONTENT;
            ((LayoutParams) ((View) viewGrey.getParent()).getLayoutParams()).weight = 0;
        } else {
            imgCar.setVisibility(View.VISIBLE);
            viewGrey.setVisibility(View.VISIBLE);
        }
        txtName.setText(name);
        txtProgress.setText(progress);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w - 120;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        }, 1);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        refresh();
    }

    public void setProgressText(String progress) {
        txtProgress.setText(progress);
    }

    public void setProgress(double current, double total) {
        this.current = current;
        this.total = total;
        refresh();
    }

    public void isCar(boolean status) {
        frameLayout.setPadding(0, 10, 0, 0);
    }

    private void refresh() {
        if (current <= total && total > 0 && width > 0) {
            double per = (current / total) * 100;
            double requiredWidth = (per / 100) * width;
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
            lp.width = (int) requiredWidth;
            frameLayout.setLayoutParams(lp);

        }
    }
}
