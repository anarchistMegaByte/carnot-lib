package com.carnot.custom_views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.carnot.R;
import com.carnot.global.Utility;

/**
 * Created by pankaj on 31/3/16.
 */
public class LinePagerIndicator extends LinearLayout {

    private int skip = 0;

    public LinePagerIndicator(Context context) {
        super(context);
        init();
    }

    public LinePagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LinePagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        setOrientation(LinearLayout.HORIZONTAL);
    }

    int width;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        if (viewPager != null && viewPager.getAdapter() != null) {
            int no = viewPager.getAdapter().getCount() - skip;
            removeAllViews();
            width = width / no;
            LayoutParams params = new LayoutParams(width, 20);
            params.setMarginEnd(2);
            params.setMarginStart(2);
            for (int i = 0; i < no; i++) {
                View view = new View(getContext());
                view.setLayoutParams(params);
                if (i == 0 && skip == 0)
                    view.setBackgroundColor(Utility.getColor(getContext(), R.color.colorAccent));
                else
                    view.setBackgroundColor(Utility.getColor(getContext(), R.color.divider_grey_color));
                addView(view);
            }
        }
    }

    ViewPager viewPager;

    public void setUp(ViewPager viewPager, int skip) {
        this.viewPager = viewPager;
        this.skip = skip;
    }

    public void onPageChange(int currentPage) {
        if (getChildCount() > currentPage) {
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (i <= currentPage)
                    view.setBackgroundColor(Utility.getColor(getContext(), R.color.colorAccent));
                else
                    view.setBackgroundColor(Utility.getColor(getContext(), R.color.divider_grey_color));
            }
        }
    }
}
