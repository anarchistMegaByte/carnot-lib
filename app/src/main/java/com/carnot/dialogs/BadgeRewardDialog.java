package com.carnot.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.carnot.R;


/**
 * Created by javid on 7/4/16.
 */
public class BadgeRewardDialog extends CustomDialog {

    public BadgeRewardDialog(Context context) {
        super(context, R.style.AppCompatAlertDialogStyle);

//        new BadgeRewardDialog(context, null);
    }

    public BadgeRewardDialog(Context context, View.OnClickListener listener) {
        super(context, R.style.AppCompatAlertDialogStyle);
        setContentView(R.layout.dialog_badge_reward, null);
        FrameLayout fm = (FrameLayout) findViewById(R.id.test);
        View view = LayoutInflater.from(context).inflate(R.layout.test_view, null, false);
        fm.addView(view);

    }


}
