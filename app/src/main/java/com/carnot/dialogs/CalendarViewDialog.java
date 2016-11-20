package com.carnot.dialogs;

import android.content.Context;
import android.view.View;

import com.carnot.R;

/**
 * Created by root on 15/4/16.
 */
public class CalendarViewDialog extends CustomDialog{
    public CalendarViewDialog(Context context) {
        super(context, R.style.AppCompatAlertDialogStyle);

//        new BadgeRewardDialog(context, null);
    }

    public CalendarViewDialog(Context context, View.OnClickListener listener) {
        super(context, R.style.AppCompatAlertDialogStyle);
        setContentView(R.layout.dialog_calendar_view, null);
    }
}
