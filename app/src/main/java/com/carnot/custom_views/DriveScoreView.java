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
import com.carnot.global.Utility;

/**
 * Created by pankaj on 30/3/16.
 */
public class DriveScoreView extends View {

    Paint paintPrimary, paintSecondry, paintTransparent;
    int score;

    public DriveScoreView(Context context) {
        super(context);
    }

    public DriveScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DriveScoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DriveScoreView, defStyleAttr, 0);

        int primaryColor = a.getColor(R.styleable.DriveScoreView_primary_color, Color.parseColor("#00000000"));
        int secondryColor = a.getColor(R.styleable.DriveScoreView_secondary_color, Color.parseColor("#00000000"));
        enableColorScheam = a.getBoolean(R.styleable.DriveScoreView_enable_color_scheme, true);
        score = a.getInt(R.styleable.DriveScoreView_score, 0);
        score = score * 360 / 100;

        paintPrimary = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPrimary.setStyle(Paint.Style.FILL_AND_STROKE);
        paintPrimary.setColor(primaryColor);

        paintSecondry = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSecondry.setStyle(Paint.Style.FILL_AND_STROKE);
        paintSecondry.setColor(secondryColor);

        paintTransparent = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTransparent.setStyle(Paint.Style.FILL_AND_STROKE);
        paintTransparent.setColor(Color.TRANSPARENT);
    }

    public void setScore(double score) {
        this.score = (int) score * 360 / 100;

        if (enableColorScheam) {
            if (score < 50) {
                paintPrimary.setColor(Utility.getColor(getContext(), R.color.app_red_light));
                paintSecondry.setColor(Utility.getColor(getContext(), R.color.app_red_dark));
            } else if (score >= 50 && score < 75) {
                paintPrimary.setColor(Utility.getColor(getContext(), R.color.app_yello_light));
                paintSecondry.setColor(Utility.getColor(getContext(), R.color.app_yello_dark));
            } else if (score >= 75) {
                paintPrimary.setColor(Utility.getColor(getContext(), R.color.app_green_light));
                paintSecondry.setColor(Utility.getColor(getContext(), R.color.app_green_dark));
            }
        }
        invalidate();
    }

    boolean enableColorScheam = false;

    public void enableColorScheam(boolean isEnable) {
        enableColorScheam = isEnable;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        RectF rectF = new RectF(0, 0, getWidth(), getHeight());
//        canvas.drawRect(rectF, paintTransparent);
        canvas.drawArc(rectF, -90, 360, true, paintPrimary);
        canvas.drawArc(rectF, -90, -(360 - score), true, paintSecondry);
        super.onDraw(canvas);
    }
}
