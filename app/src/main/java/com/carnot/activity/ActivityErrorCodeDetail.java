package com.carnot.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.models.CarErrors;


/**
 * Created by pankaj on 31/3/16.
 */
public class ActivityErrorCodeDetail extends BaseActivity {


    private final String errorCategories[] = {"LOW", "MEDIUM", "CRITICAL"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_errorcodedetail);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        if (getIntent().hasExtra(ConstantCode.INTENT_DATA)) {
            CarErrors carErrors = (CarErrors) Utility.parseFromString(getIntent().getStringExtra(ConstantCode.INTENT_DATA), CarErrors.class);
            setTitle(getString(R.string.lbl_error_code, carErrors.code.toUpperCase()));
            setTitle(carErrors.title);
            if (carErrors.category < errorCategories.length) {
                ((TextView) links(R.id.lbl_error_category)).setText(errorCategories[carErrors.category]);
                ((TextView) links(R.id.txt_desc)).setText(carErrors.desc);
                if (carErrors.category == 2) {
                    links(R.id.lbl_error_category).setBackgroundColor(Utility.getColor(mActivity, R.color.app_red_dark));
                } else if (carErrors.category == 0) {
                    links(R.id.lbl_error_category).setBackgroundColor(Utility.getColor(mActivity, R.color.app_yello_dark));
                }
            }
        }
    }

    @Override
    public void initView() {

    }


    @Override
    public void postInitView() {

    }

    @Override
    public void addAdapter() {

    }

    @Override
    public void loadData() {

    }


}
