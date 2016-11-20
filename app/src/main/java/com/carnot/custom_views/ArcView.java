package com.carnot.custom_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.carnot.R;

/**
 * Created by root on 9/4/16.
 */
public class ArcView extends View {
    Paint paintPrimary, paintSecondry;
    public int startingAngle, sweepAngle;
    String text;
    private Path mArc;

    public ArcView(Context context) {
        super(context);
    }

    public ArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ArcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {

        mArc = new Path();


        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ArcView, defStyleAttr, 0);

        int primaryColor = a.getColor(R.styleable.ArcView_primaryColor, Color.parseColor("#00000000"));
        int secondryColor = a.getColor(R.styleable.ArcView_secondaryColor, Color.parseColor("#00000000"));
        startingAngle = a.getInt(R.styleable.ArcView_starting_angle, 0);
        sweepAngle = a.getInt(R.styleable.ArcView_sweep_angle, 0);
        text = a.getString(R.styleable.ArcView_text);

        paintPrimary = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPrimary.setStyle(Paint.Style.FILL_AND_STROKE);
        paintPrimary.setColor(primaryColor);

        paintSecondry = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSecondry.setStyle(Paint.Style.FILL_AND_STROKE);
        paintSecondry.setColor(secondryColor);


    }

    public void updateRPM(int rpm) {
        //Sweep angle of 270 => 6000 RPM
        //Sweep angle of 0   =>    0 RPM
        //Linear interpolation
        sweepAngle = (int) (rpm / 6000.0 * 270.0);
        //text = "RPM " + Integer.toString(rpm);
        //invalidate();
    }

    public Animation getAnimation(int latst_rpm){
        final int old_angle = sweepAngle;
        final int rpm_value = latst_rpm;
        final int new_angle = (int) (latst_rpm / 6000.0 * 270.0);
        Animation ani = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                super.applyTransformation(interpolatedTime, t);
                sweepAngle = sweepAngle + (int)((new_angle - sweepAngle) * interpolatedTime);
                text = "RPM " + Integer.toString(rpm_value);
                invalidate();
            }
        };
        return ani;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        RectF oval = new RectF(0, 0, getWidth(), getHeight());
        mArc.addArc(oval, 135, -90);

        Rect textBounds = new Rect();
        Paint mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintText.setColor(Color.WHITE);
        mPaintText.setTextSize(22f);
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.getTextBounds(text, 0, text.length(), textBounds);

        RectF secondaryRect = new RectF(0, 0, getWidth(), getHeight());
        RectF primaryRect = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawArc(secondaryRect, 0, 360, true, paintSecondry);
        canvas.drawArc(primaryRect, startingAngle, sweepAngle, true, paintPrimary);
//        canvas.drawArc(oval, 135, -90, true, mPaintText);
        canvas.drawTextOnPath(text, mArc, 0, -5, mPaintText);
        super.onDraw(canvas);
    }
}
