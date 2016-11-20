package com.carnot.custom_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.carnot.R;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;

/**
 * Created by pankaj on 30/3/16.
 */
public class MileageView extends View {

    Paint paintPrimary, paintSecondry;
    double score;

    public MileageView(Context context) {
        super(context);
    }

    public MileageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MileageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DriveScoreView, defStyleAttr, 0);

        int primaryColor = a.getColor(R.styleable.DriveScoreView_primary_color, Color.parseColor("#00000000"));
        int secondryColor = a.getColor(R.styleable.DriveScoreView_secondary_color, Color.parseColor("#00000000"));
        enableColorScheam = a.getBoolean(R.styleable.DriveScoreView_enable_color_scheme, true);
        score = a.getInt(R.styleable.DriveScoreView_score, 0);

        paintPrimary = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPrimary.setStyle(Paint.Style.FILL_AND_STROKE);
        paintPrimary.setColor(primaryColor);

        paintSecondry = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSecondry.setStyle(Paint.Style.FILL_AND_STROKE);
        paintSecondry.setColor(secondryColor);
    }

    public void setPrimaryColor(int primaryColor) {
        paintPrimary.setColor(primaryColor);
    }

    public void setSecondaryColor(int secondaryColor) {
        paintSecondry.setColor(secondaryColor);
    }


    int width;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = MeasureSpec.getSize(w);
        double perOfTotalMileage = (score * 100) / maxScore; // here 24 is max mileage by car so we take percentage from 24 instead of 100;
        if (enableColorScheam) {
            if (perOfTotalMileage > 0 && perOfTotalMileage < 50) {
                paintPrimary.setColor(Utility.getColor(getContext(), R.color.app_red_light));
                paintSecondry.setColor(Utility.getColor(getContext(), R.color.app_red_dark));
            } else if (perOfTotalMileage >= 50 && perOfTotalMileage < 75) {
                paintPrimary.setColor(Utility.getColor(getContext(), R.color.app_yello_light));
                paintSecondry.setColor(Utility.getColor(getContext(), R.color.app_yello_dark));
            } else if (perOfTotalMileage >= 75) {
                paintPrimary.setColor(Utility.getColor(getContext(), R.color.app_green_light));
                paintSecondry.setColor(Utility.getColor(getContext(), R.color.app_green_dark));
            }

        }
        score = perOfTotalMileage * width / 100;
    }

    public void setScoreInPx(int width) {
        score = width;
        invalidate();
    }


    //used for mileage or driverscore
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    private int maxScore = ConstantCode.MAX_MILEAGE;

    public void setScore(double score) {
        double perOfTotalMileage = (score * 100) / maxScore;
        if (enableColorScheam) {
            // here 24 is max mileage by car so we take percentage from 24 instead of 100;
            if (perOfTotalMileage > 0 && perOfTotalMileage < 50) {
                paintPrimary.setColor(Utility.getColor(getContext(), R.color.app_red_light));
                paintSecondry.setColor(Utility.getColor(getContext(), R.color.app_red_dark));
            } else if (perOfTotalMileage >= 50 && perOfTotalMileage < 65) {
                paintPrimary.setColor(Utility.getColor(getContext(), R.color.app_yello_light));
                paintSecondry.setColor(Utility.getColor(getContext(), R.color.app_yello_dark));
            } else if (perOfTotalMileage >= 65) {
                paintPrimary.setColor(Utility.getColor(getContext(), R.color.app_green_light));
                paintSecondry.setColor(Utility.getColor(getContext(), R.color.app_green_dark));
            }


        }
        if (width > 0) {
            score = perOfTotalMileage * width / 100;
            this.score = score;
            invalidate();
        } else {
            this.score = score;
        }
    }

    boolean enableColorScheam = false;

    public void enableColorScheam(boolean isEnable) {
        enableColorScheam = isEnable;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        RectF secondaryRect = new RectF(0, 0, getWidth(), getHeight());
//        canvas.drawArc(rectF, -90, 360, true, paintPrimary);
//        canvas.drawArc(rectF, -90, -(360 - score), true, paintSecondry);

        RectF primaryRect = new RectF(0, 0, (int) score, getHeight());
        canvas.drawRect(secondaryRect, paintSecondry);
        canvas.drawRect(primaryRect, paintPrimary);
        super.onDraw(canvas);
    }
}
