package com.carnot.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.custom_views.expandablelayout.ExpandableLayoutListenerAdapter;
import com.carnot.custom_views.expandablelayout.ExpandableRelativeLayout;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.MyRadioGroup;
import com.carnot.libclasses.VerticalSpaceItemDecoration;
import com.carnot.models.RiderModel;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by pankaj on 31/3/16.
 */
public class ActivityPrivacySettings extends BaseActivity {


    private MyRadioGroup radioGroupSettings;
    private RadioButton radioWithFamily, radioCustom;
    private ExpandableRelativeLayout mExpandLayoutWithCustom;//, mExpandLayoutWithFamily;
    private RecyclerView recyclerView;
    private List<RiderModel> listRider;
    private CustomContactsAdapter adapter;

    private boolean isWithFamilyChecked = true;
    private SparseBooleanArray expandState = new SparseBooleanArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacysettings);

    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle(R.string.lbl_privacy_settings);
        isWithFamilyChecked = false;
    }

    @Override
    public void initView() {

        radioWithFamily = (RadioButton) links(R.id.radio_withfamily);
        radioCustom = (RadioButton) links(R.id.radio_custom);
        recyclerView = (RecyclerView) links(R.id.recycler_view);

        radioGroupSettings = new MyRadioGroup();
        radioGroupSettings.addView(radioWithFamily);
        radioGroupSettings.addView(radioCustom);


//        mExpandLayoutWithFamily = (ExpandableRelativeLayout) findViewById(R.id.expandableLayout_family);
        mExpandLayoutWithCustom = (ExpandableRelativeLayout) findViewById(R.id.expandableLayout_custom11);
        radioWithFamily.setChecked(true);

        mExpandLayoutWithCustom.setListener(new ExpandableLayoutListenerAdapter() {
            @Override
            public void onPreOpen() {

                radioCustom.setChecked(true);

            }

            @Override
            public void onPreClose() {
//                createRotateAnimator(holder.buttonLayout, 180f, 0f).start();
                radioCustom.setChecked(false);
            }
        });

        radioGroupSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == radioWithFamily.getId()) {

                    mExpandLayoutWithCustom.collapse();


                } else if (v.getId() == radioCustom.getId()) {

                    mExpandLayoutWithCustom.toggle();

                }
//                manageExpanableView();
            }
        });

//        manageExpanableView();


    }


    @Override
    public void postInitView() {

    }

    @Override
    public void addAdapter() {


    }

    @Override
    public void loadData() {
        fillDummyData();
        recyclerView.setHasFixedSize(true);
        VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration((int) Utility.convertDpToPixel(getResources().getDimension(R.dimen.scale_1dp), mActivity));
        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        adapter = new CustomContactsAdapter(listRider);
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

        blog = new RiderModel();
        blog.sRiderName = "John";
        blog.sGender = "Female";
        blog.sAge = "30";
        blog.isInvited = true;
        listRider.add(blog);

        blog = new RiderModel();
        blog.sRiderName = "John";
        blog.sGender = "Female";
        blog.sAge = "30";
        blog.isInvited = true;
        listRider.add(blog);
    }

    public class CustomContactsAdapter extends RecyclerView.Adapter {

        private List<RiderModel> data;
        private Context context;

        public CustomContactsAdapter(final List<RiderModel> data) {
            this.data = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            this.context = parent.getContext();
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.inflate_privacy_settings_contacts, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder1, final int position) {
            final RiderModel item = data.get(position);
            ViewHolder holder = (ViewHolder) holder1;
            holder.tvGenderNAge.setText(item.sGender + " , " + item.sAge);
            holder.tvName.setText(item.sRiderName);

            if (item.isInvited) {
                holder.chk.setChecked(true);
            } else {
                holder.chk.setChecked(false);
            }
            holder.chk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    RiderModel blog = data.get(position);
                    blog.isInvited = !blog.isInvited;

                    data.set(position, blog);
                    notifyItemChanged(position);


                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tvGenderNAge, tvName, tvTime;
            public ImageView ivRiderpic;
            public CheckBox chk;


            public ViewHolder(View v) {
                super(v);

                ivRiderpic = (ImageView) v.findViewById(R.id.iv_riderpic);
                tvName = (TextView) v.findViewById(R.id.tv_ridername);
                tvGenderNAge = (TextView) v.findViewById(R.id.tv_gender_age);
                chk = (CheckBox) v.findViewById(R.id.chk);

            }
        }

        public void setFilter(List<RiderModel> countryModels) {
            data = new ArrayList<>();
            data.addAll(countryModels);
            notifyDataSetChanged();
        }


    }


}
