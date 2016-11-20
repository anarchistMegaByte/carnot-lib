package com.carnot.libclasses;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import com.carnot.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fyu on 11/3/14.
 */

public class RippleBackground extends RelativeLayout {

    private static final int DEFAULT_RIPPLE_COUNT = 6;
    private static final int DEFAULT_DURATION_TIME = 3000;
    private static final float DEFAULT_SCALE = 6.0f;
    private static final int DEFAULT_FILL_TYPE = 0;

    private static final String TAG = "[RIPPLE]";

    private int rippleColor;
    private float rippleStrokeWidth;
    private float rippleRadius;
    private int rippleDurationTime;
    private int rippleAmount;
    private int rippleDelay;
    private float rippleScale;
    private int rippleType;
    private Paint paint;
    private boolean animationRunning = false;
    private AnimatorSet animatorSet;
    private ArrayList<ObjectAnimator> animatorList;
    //private LayoutParams rippleParams;
    private ArrayList<RippleView> rippleViewList = new ArrayList<RippleView>();

    public RippleBackground(Context context) {
        super(context);
    }

    public RippleBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RippleBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        if (isInEditMode())
            return;

        if (null == attrs) {
            throw new IllegalArgumentException("Attributes should be provided to this view,");
        }

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleBackground);
        rippleColor = typedArray.getColor(R.styleable.RippleBackground_rb_color, getResources().getColor(R.color.rippelColor));
        rippleStrokeWidth = typedArray.getDimension(R.styleable.RippleBackground_rb_strokeWidth, getResources().getDimension(R.dimen.rippleStrokeWidth));
        rippleRadius = typedArray.getDimension(R.styleable.RippleBackground_rb_radius, getResources().getDimension(R.dimen.rippleRadius));
        rippleDurationTime = typedArray.getInt(R.styleable.RippleBackground_rb_duration, DEFAULT_DURATION_TIME);
        rippleAmount = typedArray.getInt(R.styleable.RippleBackground_rb_rippleAmount, DEFAULT_RIPPLE_COUNT);
        rippleScale = typedArray.getFloat(R.styleable.RippleBackground_rb_scale, DEFAULT_SCALE);
        rippleType = typedArray.getInt(R.styleable.RippleBackground_rb_type, DEFAULT_FILL_TYPE);
        typedArray.recycle();

        //rippleRadius = 60;
        rippleAmount = 6;
        rippleColor = getResources().getColor(R.color.colorAccent);

        rippleDelay = rippleDurationTime / rippleAmount;

        paint = new Paint();
        paint.setAntiAlias(true);
        if (rippleType == DEFAULT_FILL_TYPE) {
            rippleStrokeWidth = 0;
            paint.setStyle(Paint.Style.FILL);
            Log.d("[RIPPLE]", "FILL");
        } else {
            paint.setStyle(Paint.Style.STROKE);
            Log.d("[RIPPLE]", "STROKE");
        }

        paint.setColor(rippleColor);

        //rippleParams = new LayoutParams((int) (2 * (rippleRadius + rippleStrokeWidth) * rippleScale), (int) (2 * (rippleRadius + rippleStrokeWidth) * rippleScale));
        //rippleParams.addRule(CENTER_IN_PARENT, TRUE);

        animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorList = new ArrayList<ObjectAnimator>();

        /**
         * Create rippleAmount number of RippleViews
         * Each rippleView should have a set of rippleParams (h*w) such that:
         * rippleAmount is equally split between:
         * A: 2 * (rippleRadius + rippleStrokeWidth)
         * B: 2 * (rippleRadius + rippleStrokeWidth) * rippleScale
         *
         * Create alphaAnimators for each view to animate transparency of each ring
         */

        /**
         * Max allowed size of any circle view: 220 dp (largest circle)
         * Print values of w and check if any of them exceed 220
         * Ripple radius is 60, Ripple Scale is 2
         * Radius values should range from 60 to 120 (dias thus from 120 to 240)
         */

        for (int i = 0; i < rippleAmount; i++) {
            RippleView rippleView = new RippleView(getContext());

            //int w = (int)(2 * (rippleRadius + rippleStrokeWidth) + 2 * (rippleRadius + rippleStrokeWidth) * rippleScale * (i + 1) / rippleAmount);
            int w = (int)(2 * rippleRadius + 2 * rippleRadius * rippleScale * (i + 1) / rippleAmount);
            //int w = (int)(2 * (rippleRadius + (rippleRadius * (rippleScale - 1)) / (rippleAmount - 1) * i));
            Log.d(TAG, "W " + w + " RR " + rippleRadius);
            LayoutParams rParams = new LayoutParams(w, w);
            rParams.addRule(CENTER_IN_PARENT, TRUE);
            addView(rippleView, rParams);
            rippleViewList.add(rippleView);

//            final ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleX", 1.0f, rippleScale);
//            scaleXAnimator.setRepeatCount(ObjectAnimator.INFINITE);
//            scaleXAnimator.setRepeatMode(ObjectAnimator.RESTART);
//            scaleXAnimator.setStartDelay(i * rippleDelay);
//            scaleXAnimator.setDuration(rippleDurationTime);
//            animatorList.add(scaleXAnimator);
//            final ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleY", 1.0f, rippleScale);
//            scaleYAnimator.setRepeatCount(ObjectAnimator.INFINITE);
//            scaleYAnimator.setRepeatMode(ObjectAnimator.RESTART);
//            scaleYAnimator.setStartDelay(i * rippleDelay);
//            scaleYAnimator.setDuration(rippleDurationTime);
//            animatorList.add(scaleYAnimator);

            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(rippleView, "Alpha", 1.0f, 0.0f);
            alphaAnimator.setDuration(750);
//            alphaAnimator.setRepeatCount(ObjectAnimator.INFINITE);
//            alphaAnimator.setRepeatMode(ObjectAnimator.RESTART);
//            alphaAnimator.setStartDelay(i * rippleDelay);
//            alphaAnimator.setDuration(rippleDurationTime);
            animatorList.add(alphaAnimator);
        }

//        animatorSet.playTogether(animatorList);
    }

    private class RippleView extends View {

        public RippleView(Context context) {
            super(context);
            this.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int radius = (Math.min(getWidth(), getHeight())) / 2;
            canvas.drawCircle(radius, radius, radius - rippleStrokeWidth, paint);
        }
    }

    public void startRippleAnimation() {
        if (!isRippleAnimationRunning()) {
            for (RippleView rippleView : rippleViewList) {
                rippleView.setVisibility(VISIBLE);
            }
            //animatorSet.start();
            animationRunning = true;
        }
    }

    public void stopRippleAnimation() {
        if (isRippleAnimationRunning()) {
            //animatorSet.end();
            animationRunning = false;
        }
    }

    public boolean isRippleAnimationRunning() {
        return animationRunning;
    }

    public static int MAX_LOAD = 100;

    public void updatePulse(int load) {
        //int nRipples = (int)Math.ceil(load * 1.0 / MAX_LOAD * rippleAmount);
        //Alternate ceiling function
        int nRipples = (load * rippleAmount - 1) / MAX_LOAD + 1;
        Log.d(TAG, "Load and ripples " + Integer.toString(load) + " " + Integer.toString(nRipples));
        for (int i = 0; i < rippleAmount; i++) {
            float alphaVal = (i <= nRipples) ? (1.0f * (1.0f - i * 1.0f / (float) nRipples)) : 0.0f;
            Log.d(TAG, "Alpha " + Float.toString(alphaVal));
            animatorList.get(i).setFloatValues(alphaVal);
            animatorList.get(i).start();
        }
    }

    private static Map<Float, Float> pxCache = new HashMap();

    /**
     * Convert dp to pixel float.
     *
     * @param dp      the dp
     * @param context the context
     * @return the float
     */
    public float convertDpToPixel(float dp, final Context context) {

        Float f = pxCache.get(dp);
        if (f == null) {
            synchronized (pxCache) {
                f = calculateDpToPixel(dp, context);
                pxCache.put(dp, f);
            }
        }
        return f;
    }

    /**
     * Calculate dp to pixel float.
     *
     * @param dp      the dp
     * @param context the context
     * @return the float
     */
    public float calculateDpToPixel(float dp, Context context) {

        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;

    }
}
