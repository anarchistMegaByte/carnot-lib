package com.carnot.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.adapter.InviteRiderAdapter;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.VerticalSpaceItemDecoration;
import com.carnot.models.RiderModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by javid on 31/3/16.
 */
public class ActivityInviteRider extends BaseActivity implements View.OnClickListener, TextWatcher {

    private RecyclerView recyclerView;
    private List<RiderModel> listRider;
    private EditText edtAutoRiderSearch;
    private InviteRiderAdapter adapter;
    private TextView txtLabelSearchRider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_rider);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle(getString(R.string.lbl_invite_a_rider));
    }

    @Override
    public void initView() {

        txtLabelSearchRider = (TextView) links(R.id.txt_search_rider);
        recyclerView = (RecyclerView) links(R.id.recycler_view);
        edtAutoRiderSearch = (EditText) links(R.id.edt_search_rider);
        edtAutoRiderSearch.addTextChangedListener(this);


    }


    private void resetAdapter() {
        adapter = new InviteRiderAdapter(listRider);
        recyclerView.setAdapter(adapter);
    }

    private void fillDummyData() {
        listRider = new ArrayList<>();
        RiderModel blog = new RiderModel();
        blog.sRiderName = "Rider1";
        blog.sGender = "Male";
        blog.sAge = "35";
        listRider.add(blog);


        blog = new RiderModel();
        blog.sRiderName = "Zimmy";
        blog.sGender = "Male";
        blog.sAge = "36";
        listRider.add(blog);


        blog = new RiderModel();
        blog.sRiderName = "Rahul";
        blog.sGender = "Male";
        blog.sAge = "38";
        listRider.add(blog);

        blog = new RiderModel();
        blog.sRiderName = "John";
        blog.sGender = "Female";
        blog.sAge = "30";
        blog.isInvited = true;
        listRider.add(blog);
    }


    @Override
    public void postInitView() {
        txtLabelSearchRider.setVisibility(View.INVISIBLE);
    }

    @Override
    public void addAdapter() {

        ((Button) links(R.id.btn_invite)).setOnClickListener(this);
    }

    @Override
    public void loadData() {
        fillDummyData();
        recyclerView.setHasFixedSize(true);
        VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration((int) Utility.convertDpToPixel(getResources().getDimension(R.dimen.scale_1dp), mActivity));
        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        resetAdapter();

    }


    @Override
    public void onClick(View v) {

//
//     DialogUtil.ConfirmDialog(ActivityInviteRider.this, getString(R.string.dialog_title_confirm_changes), getString(R.string.lbl_confirm_dialog_msg), null, null, DialogCallBack);
    }

    DialogInterface.OnClickListener DialogCallBack = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {


    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!TextUtils.isEmpty(s) && s.length() > 0) {
            txtLabelSearchRider.setVisibility(View.VISIBLE);

            final List<RiderModel> filteredModelList = filter(listRider, s.toString());
            adapter.setFilter(filteredModelList);


        } else {
            txtLabelSearchRider.setVisibility(View.INVISIBLE);
            resetAdapter();
        }
    }

    private List<RiderModel> filter(List<RiderModel> models, String query) {
        query = query.toLowerCase();

        final List<RiderModel> filteredModelList = new ArrayList<>();
        for (RiderModel model : models) {
            final String text = model.sRiderName.toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

}
