package com.carnot.custom_views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.carnot.R;

/**
 * Created by root on 21/4/16.
 */
public class NotificationBar extends FrameLayout {

    ViewDragHelper mDragHelper;

    public NotificationBar(Context context) {
        super(context);
    }

    public NotificationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotificationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDragHelper = ViewDragHelper.create(this, 1.0f, new OurViewDragHelperCallbacks());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        boolean shouldInterceptTouchEvent = mDragHelper.shouldInterceptTouchEvent(ev);
//        return shouldInterceptTouchEvent;
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }
    private class OurViewDragHelperCallbacks extends ViewDragHelper.Callback {

        View view;

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            Log.i("TAG", "view : " + child.toString());
            view = child;
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }


        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);

        }

        ObjectAnimator animLeftRight;

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);

            //when state is stopped
            if (state == ViewDragHelper.STATE_IDLE) {
                Log.i("TAG", "released at " + state + "-" + view.getX() + "/" + view.getWidth());
                //Covered half distance
                if (Math.abs(view.getX()) >= view.getWidth() / 2) {
                    //Code to dismiss to right
                    if (view.getX() > 0) {
                        Log.i("TAG", "released at " + view.getX() + "  " + (view.getWidth() - Math.abs(view.getX())));
                        animLeftRight = ObjectAnimator.ofFloat(view, "X", view.getX(), view.getWidth());

                    }
                    //Code to dismiss to left
                    else {
                        Log.i("TAG", "released at " + view.getX() + "  " + (-(view.getX() > 0 ? -view.getX() : view.getX())));
                        animLeftRight = ObjectAnimator.ofFloat(view, "X", view.getX(), -view.getWidth());

                    }
                }
                //Revert back to original position
                else {
                    Log.i("TAG", "released at " + view.getX() + "  " + (view.getX() > 0 ? -view.getX() : Math.abs(view.getX())));
                    animLeftRight = ObjectAnimator.ofFloat(view, "X", view.getX(), 0);
                }
                animLeftRight.setDuration(getResources().getInteger(R.integer.expand_animation_duration));
                animLeftRight.start();
                view.clearAnimation();
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
}
