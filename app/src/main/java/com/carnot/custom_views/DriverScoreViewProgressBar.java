package com.carnot.custom_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.global.Utility;

/**
 * Created by root on 11/4/16.
 */
public class DriverScoreViewProgressBar extends LinearLayout {
    public static final int STYLE_GREEN = 1;
    public static final int STYLE_BLACK = 2;
    public static final int STYLE_YELLOW = 3;
    public static final int STYLE_RED = 4;
    int style;
    View parent;
    TextView txtProgress;
    int current, total;

    int whiteColor;
    int blackTranslucent;
    int greenColor;
    int greenSecondaryColor;
    int greycolor;

    public DriverScoreViewProgressBar(Context context) {
        super(context);
    }

    public DriverScoreViewProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DriverScoreViewProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }


    MileageView mileageView;
    LinearLayout linearProgressBar;
    TextView txt_drive_score_this_week;

    boolean enableColorScheam = true;

    private void init(AttributeSet attrs, int defStyleAttr) {
        inflate(getContext(), R.layout.drive_score_progress_view, this);

        whiteColor = Utility.getColor(getContext(), R.color.white);
        blackTranslucent = Utility.getColor(getContext(), R.color.black_translucent);
        greenColor = Utility.getColor(getContext(), R.color.green_text_color);
        greenSecondaryColor = Utility.getColor(getContext(), R.color.green_secondary_color);
        greycolor = Utility.getColor(getContext(), R.color.black_translucent);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DriveScoreView, defStyleAttr, 0);
        enableColorScheam = a.getBoolean(R.styleable.DriveScoreView_enable_color_scheme, true);

        mileageView = (MileageView) findViewById(R.id.mileage_view);
        mileageView.enableColorScheam(enableColorScheam);
        linearProgressBar = (LinearLayout) findViewById(R.id.linear_progress_bar);
        txtProgress = (TextView) findViewById(R.id.txt_progress);
        parent = (LinearLayout) findViewById(R.id.parent);
        txt_drive_score_this_week = (TextView) findViewById(R.id.txt_drive_score_this_week);
        txt_drive_score_this_week.setVisibility(View.INVISIBLE);
//        setStyle(STYLE_BLACK);
    }

    public void setTitleVisibility(int visibleMode) {
        txt_drive_score_this_week.setVisibility(visibleMode);
    }

    public void setStyle(int style) {
        this.style = style;

        if (style == STYLE_GREEN) {
            parent.setBackgroundResource(R.drawable.mileage_progress_green_gradient);
            txtProgress.setTextColor(whiteColor);
            mileageView.setPrimaryColor(whiteColor);
            mileageView.setSecondaryColor(blackTranslucent);
            for (int i = 0; i < linearProgressBar.getChildCount(); i++) {
                LinearLayout ll = (LinearLayout) linearProgressBar.getChildAt(i);
                ((TextView) ll.getChildAt(0)).setTextColor(whiteColor);
                ll.getChildAt(1).setBackgroundColor(greycolor);
                ((TextView) ll.getChildAt(2)).setTextColor(whiteColor);
            }
        } else if (style == STYLE_BLACK) {
            parent.setBackgroundResource(R.color.service_history_row_background);
            txtProgress.setTextColor(greenColor);
            mileageView.setPrimaryColor(greenColor);
            mileageView.setSecondaryColor(greenSecondaryColor);
            for (int i = 0; i < linearProgressBar.getChildCount(); i++) {
                LinearLayout ll = (LinearLayout) linearProgressBar.getChildAt(i);
                ((TextView) ll.getChildAt(0)).setTextColor(greenColor);
                ll.getChildAt(1).setBackgroundColor(whiteColor);
                ((TextView) ll.getChildAt(2)).setTextColor(greenColor);
            }
        }
    }

    int width;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
//        refresh();
    }

    public void setProgressText(int progress) {
        txtProgress.setText(progress + "");
    }

    public void setProgress(int current, int total) {
        this.current = current;
        this.total = total;
        if (current <= total) {
            float per = ((float) current / total) * 100;


            if (per > 0 && per < 50) {
//                parent.setBackgroundResource(R.drawable.status_red_gradient);
                style = STYLE_RED;
            } else if (per >= 50 && per < 75) {
//                parent.setBackgroundResource(R.drawable.status_yellow_gradient);
                style = STYLE_YELLOW;
            } else if (per >= 75) {
//                parent.setBackgroundResource(R.drawable.status_green_gradient);
                style = STYLE_GREEN;
            } else {
                style = STYLE_BLACK;
            }

            if (enableColorScheam) {
                mileageView.enableColorScheam(enableColorScheam);
            }
            mileageView.setMaxScore(total);
            mileageView.setScore((int) Math.round(per / 10) * 10);

            if (enableColorScheam) {
                mileageView.enableColorScheam(enableColorScheam);
                int per1 = (int) Math.round((double) per / 10);
                if (linearProgressBar.getChildCount() >= per1) {
                    if (style == STYLE_GREEN) {
                        parent.setBackgroundResource(R.drawable.status_green_gradient);
                        for (int i = 0; i < linearProgressBar.getChildCount(); i++) {
                            LinearLayout ll = (LinearLayout) linearProgressBar.getChildAt(i);
                            ((TextView) ll.getChildAt(0)).setTextColor(whiteColor);
                            ll.getChildAt(1).setBackgroundColor(greycolor);
                            ((TextView) ll.getChildAt(2)).setTextColor(whiteColor);

                            if (i == per1) {
                                ((TextView) ll.getChildAt(0)).setTextColor(whiteColor);
                                ll.getChildAt(1).setBackgroundColor(whiteColor);
                                ((TextView) ll.getChildAt(2)).setTextColor(whiteColor);
                                ((TextView) ll.getChildAt(0)).setVisibility(View.VISIBLE);
                                ((TextView) ll.getChildAt(2)).setVisibility(View.INVISIBLE);//HIDEING THIS AS PER ROHAN 26-6
                                ((TextView) ll.getChildAt(2)).setText(current + "");
                            } else {
                                ((TextView) ll.getChildAt(0)).setVisibility(View.INVISIBLE);
                                ((TextView) ll.getChildAt(2)).setVisibility(View.INVISIBLE);
                            }
                        }
                    } else if (style == STYLE_BLACK) {
                        parent.setBackgroundResource(R.color.service_history_row_background);
                        for (int i = 0; i < linearProgressBar.getChildCount(); i++) {
                            LinearLayout ll = (LinearLayout) linearProgressBar.getChildAt(i);
                            ((TextView) ll.getChildAt(0)).setTextColor(greenColor);
                            ll.getChildAt(1).setBackgroundColor(whiteColor);
                            ((TextView) ll.getChildAt(2)).setTextColor(greenColor);

                            if (i == per1) {
                                ((TextView) ll.getChildAt(0)).setTextColor(greenColor);
                                ll.getChildAt(1).setBackgroundColor(greenColor);
                                ((TextView) ll.getChildAt(2)).setTextColor(greenColor);
                                ((TextView) ll.getChildAt(0)).setVisibility(View.VISIBLE);
                                ((TextView) ll.getChildAt(2)).setVisibility(View.INVISIBLE);//HIDEING THIS AS PER ROHAN 26-6
                                ((TextView) ll.getChildAt(2)).setText(current + "");
                            } else {
                                ((TextView) ll.getChildAt(0)).setVisibility(View.INVISIBLE);
                                ((TextView) ll.getChildAt(2)).setVisibility(View.INVISIBLE);
                            }
                        }
                    } else if (style == STYLE_YELLOW) {
                        parent.setBackgroundResource(R.drawable.status_yellow_gradient);
                        for (int i = 0; i < linearProgressBar.getChildCount(); i++) {
                            LinearLayout ll = (LinearLayout) linearProgressBar.getChildAt(i);
                            ((TextView) ll.getChildAt(0)).setTextColor(whiteColor);
                            ll.getChildAt(1).setBackgroundColor(greycolor);
                            ((TextView) ll.getChildAt(2)).setTextColor(whiteColor);

                            if (i == per1) {
                                ((TextView) ll.getChildAt(0)).setTextColor(whiteColor);
                                ll.getChildAt(1).setBackgroundColor(whiteColor);
                                ((TextView) ll.getChildAt(2)).setTextColor(whiteColor);
                                ((TextView) ll.getChildAt(0)).setVisibility(View.VISIBLE);
                                ((TextView) ll.getChildAt(2)).setVisibility(View.INVISIBLE);//HIDEING THIS AS PER ROHAN 26-6
                                ((TextView) ll.getChildAt(2)).setText(current + "");
                            } else {
                                ((TextView) ll.getChildAt(0)).setVisibility(View.INVISIBLE);
                                ((TextView) ll.getChildAt(2)).setVisibility(View.INVISIBLE);
                            }
                        }
                    } else if (style == STYLE_RED) {
                        parent.setBackgroundResource(R.drawable.status_red_gradient);
                        for (int i = 0; i < linearProgressBar.getChildCount(); i++) {
                            LinearLayout ll = (LinearLayout) linearProgressBar.getChildAt(i);
                            ((TextView) ll.getChildAt(0)).setTextColor(whiteColor);
                            ll.getChildAt(1).setBackgroundColor(greycolor);
                            ((TextView) ll.getChildAt(2)).setTextColor(whiteColor);

                            if (i == per1) {
                                ((TextView) ll.getChildAt(0)).setTextColor(whiteColor);
                                ll.getChildAt(1).setBackgroundColor(whiteColor);
                                ((TextView) ll.getChildAt(2)).setTextColor(whiteColor);
                                ((TextView) ll.getChildAt(0)).setVisibility(View.VISIBLE);
                                ((TextView) ll.getChildAt(2)).setVisibility(View.INVISIBLE);//HIDEING THIS AS PER ROHAN 26-6
                                ((TextView) ll.getChildAt(2)).setText(current + "");
                            } else {
                                ((TextView) ll.getChildAt(0)).setVisibility(View.INVISIBLE);
                                ((TextView) ll.getChildAt(2)).setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
            } else {
                int per1 = (int) Math.round((double) per / 10);
                parent.setBackgroundResource(R.color.service_history_row_background);
                for (int i = 0; i < linearProgressBar.getChildCount(); i++) {
                    LinearLayout ll = (LinearLayout) linearProgressBar.getChildAt(i);
                    ((TextView) ll.getChildAt(0)).setTextColor(greenColor);
                    ll.getChildAt(1).setBackgroundColor(whiteColor);
                    ((TextView) ll.getChildAt(2)).setTextColor(greenColor);

                    if (i == per1) {
                        ((TextView) ll.getChildAt(0)).setTextColor(greenColor);
                        ll.getChildAt(1).setBackgroundColor(greenColor);
                        ((TextView) ll.getChildAt(2)).setTextColor(greenColor);
                        ((TextView) ll.getChildAt(0)).setVisibility(View.VISIBLE);
                        ((TextView) ll.getChildAt(2)).setVisibility(View.INVISIBLE);//HIDEING THIS AS PER ROHAN 26-6
                        ((TextView) ll.getChildAt(2)).setText(current + "");
                    } else {
                        ((TextView) ll.getChildAt(0)).setVisibility(View.INVISIBLE);
                        ((TextView) ll.getChildAt(2)).setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
        txtProgress.setText(current + "");
    }

    private void setColorForProgressMarks() {

    }
}
