package com.carnot.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.fragment.FragmentNotificationDiagnosis;
import com.carnot.fragment.FragmentNotificationSOS;
import com.carnot.fragment.FragmentNotificationTowed;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;

/**
 * Created by root on 13/4/16.
 * Activity to show notification details
 */
public class ActivityNotificationDetail extends BaseActivity {

    TextView txtMessage;
    CardView cardView;
    View shadowView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);

        setTitle("");
        ((Toolbar) links(R.id.toolbar)).setBackgroundResource(R.color.transparent);
    }

    @Override
    public void initView() {
        cardView = (CardView) links(R.id.card_view);
        txtMessage = (TextView) links(R.id.txt_message);
        shadowView = (View) links(R.id.shadow_view);
    }

    @Override
    public void postInitView() {
//        cardView.setShadowPadding(0, 0, 0, 0);
        cardView.setMaxCardElevation(0);
        /**
         * Here, we write a condition because for lolipop and above card view automatically show shadow and for pre lolipop devices we show view
         */
        if (Utility.isAndroidAPILevelGreaterThenEqual(Build.VERSION_CODES.LOLLIPOP)) {
            shadowView.setVisibility(View.GONE);
        } else {
            shadowView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void addAdapter() {

    }

    @Override
    public void loadData() {
        txtMessage.setText(getIntent().getStringExtra(ConstantCode.INTENT_MESSAGE));

        if (ConstantCode.NOTIFICATION_TYPE_TOW_ALERT.equalsIgnoreCase(getIntent().getStringExtra(ConstantCode.INTENT_NOTIFICATION_TYPE))) {
            FragmentNotificationTowed fragmentNotificationTowed = new FragmentNotificationTowed();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentNotificationTowed).commit();
        } else if (ConstantCode.NOTIFICATION_TYPE_SOS.equalsIgnoreCase(getIntent().getStringExtra(ConstantCode.INTENT_NOTIFICATION_TYPE))) {
            FragmentNotificationSOS fragmentNotificationSOS = new FragmentNotificationSOS();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentNotificationSOS).commit();
        } else if (ConstantCode.NOTIFICATION_TYPE_DIAGNOSTICS.equalsIgnoreCase(getIntent().getStringExtra(ConstantCode.INTENT_NOTIFICATION_TYPE))) {
            FragmentNotificationDiagnosis fragmentNotificationDiagnosis = new FragmentNotificationDiagnosis();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentNotificationDiagnosis).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notification_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_close) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
