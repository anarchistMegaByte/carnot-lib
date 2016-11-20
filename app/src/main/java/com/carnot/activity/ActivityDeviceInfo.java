package com.carnot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.view.Menu;
import android.view.MenuItem;

import com.carnot.R;
import com.carnot.custom_views.AppTextViewLight;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.models.Cars;

/**
 * Created by javid on 31/3/16.
 */
public class ActivityDeviceInfo extends BaseActivity {

    AppTextViewLight carUniqueID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deviceinfo);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle("");
    }

    @Override
    public void initView() {


        final AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);

        carUniqueID = (AppTextViewLight) findViewById(R.id.uniqueCarID);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == 0) {
                    setTitle("");
                } else {
                    setTitle("");
                }

            }
        });
    }

    @Override
    public void postInitView() {
        String carID = getIntent().getExtras().getString(ConstantCode.INTENT_CAR_ID);
        Cars car = Cars.readSpecific(carID);
        if(car!=null)
        {
            if(!Utility.isEmpty(car.stmid))
            {
                carUniqueID.setText(car.stmid);
            }
            else
            {
                carUniqueID.setText("-");
            }
        }
        else
        {
            carUniqueID.setText("-");
        }

    }

    @Override
    public void addAdapter() {

    }

    @Override
    public void loadData() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_info, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

            if (item.getItemId()==  R.id.action_reset_device){
                Intent intent = new Intent(ActivityDeviceInfo.this, ActivityResetDevice.class);
                startActivity(intent);
                return true;}

        return false;
    }


}
