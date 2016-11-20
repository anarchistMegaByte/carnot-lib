package com.carnot.libclasses;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Created by pankaj on 31/3/16.
 */
public class CompoundDrawableClickListener implements View.OnTouchListener {

    Drawable drawableLeft, drawableTop, drawableRight, drawableBottom;

    int actionX, actionY;

    public CompoundDrawableClickListener(EditText editText) {
        Drawable[] list = editText.getCompoundDrawables();
        drawableLeft = list[0];
        drawableTop = list[1];
        drawableRight = list[2];
        drawableBottom = list[3];
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Rect bounds;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            actionX = (int) event.getX();
            actionY = (int) event.getY();
            if (drawableBottom != null && drawableBottom.getBounds().contains(actionX, actionY)) {
                return onBottomDrawableClickListener();
            }

            if (drawableTop != null && drawableTop.getBounds().contains(actionX, actionY)) {
                return onTopDrawableClickListener();
            }

            // this works for left since container shares 0,0 origin with bounds
            if (drawableLeft != null) {
                bounds = null;
                bounds = drawableLeft.getBounds();

                int x, y;
                int extraTapArea = (int) (13 * v.getContext().getResources().getDisplayMetrics().density + 0.5);

                x = actionX;
                y = actionY;

                if (!bounds.contains(actionX, actionY)) {
                    /** Gives the +20 area for tapping. */
                    x = (int) (actionX - extraTapArea);
                    y = (int) (actionY - extraTapArea);

                    if (x <= 0)
                        x = actionX;
                    if (y <= 0)
                        y = actionY;

                    /** Creates square from the smallest value */
                    if (x < y) {
                        y = x;
                    }
                }

                if (bounds.contains(x, y)) {
                    if (onLeftDrawableClickListener()) {
                        event.setAction(MotionEvent.ACTION_CANCEL);
                    }
                    return false;
                }
            }

            if (drawableRight != null) {

                bounds = null;
                bounds = drawableRight.getBounds();

                int x, y;
                int extraTapArea = 13;

                /**
                 * IF USER CLICKS JUST OUT SIDE THE RECTANGLE OF THE DRAWABLE
                 * THAN ADD X AND SUBTRACT THE Y WITH SOME VALUE SO THAT AFTER
                 * CALCULATING X AND Y CO-ORDINATE LIES INTO THE DRAWBABLE
                 * BOUND. - this process help to increase the tappable area of
                 * the rectangle.
                 */
                x = (int) (actionX + extraTapArea);
                y = (int) (actionY - extraTapArea);

                /**Since this is right drawable subtract the value of x from the width
                 * of view. so that width - tappedarea will result in x co-ordinate in drawable bound.
                 */
                x = v.getWidth() - x;

                 /*x can be negative if user taps at x co-ordinate just near the width.
                 * e.g views width = 300 and user taps 290. Then as per previous calculation
                 * 290 + 13 = 303. So subtract X from getWidth() will result in negative value.
                 * So to avoid this add the value previous added when x goes negative.
                 */

                if (x <= 0) {
                    x += extraTapArea;
                }

                 /* If result after calculating for extra tappable area is negative.
                 * assign the original value so that after subtracting
                 * extratapping area value doesn't go into negative value.
                 */

                if (y <= 0)
                    y = actionY;

                /**If drawble bounds contains the x and y points then move ahead.*/
                if (bounds.contains(x, y)) {
                    if (onRightDrawableClickListener()) {
                        event.setAction(MotionEvent.ACTION_CANCEL);
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }

    public boolean onLeftDrawableClickListener() {
        return false;
    }

    public boolean onRightDrawableClickListener() {
        return false;
    }

    public boolean onTopDrawableClickListener() {
        return false;
    }

    public boolean onBottomDrawableClickListener() {
        return false;
    }

}
