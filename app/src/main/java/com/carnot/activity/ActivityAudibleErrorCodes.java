package com.carnot.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ScrollView;

import com.carnot.R;
import com.carnot.adapter.AudibleErrorsAdapter;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.VerticalSpaceItemDecoration;
import com.carnot.models.AudibleErrors;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by pankaj on 31/3/16.
 * Activity to load all audio Error codes
 */
public class ActivityAudibleErrorCodes extends BaseActivity {


    private RecyclerView recyclerView;
    //    private List<AudibleErrors> listAudibleErrors;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audibleerrorcodes);

    }

    @Override
    public void initVariable() {

        setToolbar(R.id.toolbar, true);
        setTitle(getString(R.string.lbl_audible_error_codes));
    }

    @Override
    public void initView() {

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration((int) Utility.convertDpToPixel(getResources().getDimension(R.dimen.scale_1dp), mActivity));
        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AudibleErrorsAdapter adapter = new AudibleErrorsAdapter(getListAudibleErrors());
        recyclerView.setAdapter(adapter);
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

    //Creating the list for adapter
    public List<AudibleErrors> getListAudibleErrors() {
        ArrayList<AudibleErrors> listAudibleErrors = new ArrayList<>();
        listAudibleErrors.add(new AudibleErrors(mActivity, "Battery - bad battery", R.raw.battery_bad_battery, "Looks like your battery has gone bad.It's possible that you have one of these errors"));
        listAudibleErrors.add(new AudibleErrors(mActivity, "Engine - bad or insufficient oil", R.raw.engine_bad_or_insufficient_oil, "Looks like engine is bad or oil is insufficient"));
        listAudibleErrors.add(new AudibleErrors(mActivity, "Engine - Cylinder not working properly", R.raw.engine_cylinder_not_working_properly, "Looks like cylinder not working properly"));
        listAudibleErrors.add(new AudibleErrors(mActivity, "Engine - main belt worn out", R.raw.engine_main_belt_worn_out, "Looks like engine main belt worn out"));
        listAudibleErrors.add(new AudibleErrors(mActivity, "Engine - Tensioner not working properly", R.raw.engine_tensioner_not_working_properly, "Looks like engine tensioner not working properly"));
        listAudibleErrors.add(new AudibleErrors(mActivity, "Transmission - Clutch not proper", R.raw.transmission_clutch_not_proper, "Looks like transmission clutch is not proper"));
        return listAudibleErrors;
    }
}