package com.carnot.fragment;

import android.content.Context;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.activity.ActivityAllTrips;
import com.carnot.activity.ActivityDashboard;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseFragment;
import com.carnot.libclasses.LoadMoreRecyclerAdapter;
import com.carnot.libclasses.VerticalSpaceItemDecoration;
import com.carnot.models.RiderProfile;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by pankaj on 31/3/16.
 */
public class FragmentCarnot extends BaseFragment {
    int UP = -1, DOWN = 1;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    CustomAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    SwipeRefreshLayout swipeToRefreshLayout;

    public FragmentCarnot() {
        setContentView(R.layout.fragment_carnot, false, false);
    }

    @Override
    public void initVariable() {

    }

    @Override
    public void initView() {
        progressBar = (ProgressBar) links(R.id.progress_bar);
        linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerView = (RecyclerView) links(R.id.recycler_view);
        swipeToRefreshLayout = (SwipeRefreshLayout) links(R.id.swipe_to_refresh);
        swipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadList();
            }
        });

    }

    @Override
    public void postInitView() {
        super.postInitView();
        VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration((int) Utility.convertDpToPixel(getResources().getDimension(R.dimen.recycler_view_divider_height), mActivity));
        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void addAdapter() {
        adapter = new CustomAdapter(mActivity);
        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Commenting start of activity as per client comment
                /*Intent intent = new Intent(mActivity, ActivityRiderDashboard.class);
                RiderProfile model = adapter.getItem(position);
                intent.putExtra(ConstantCode.INTENT_DATA, Utility.getJsonString(model));
                startActivity(intent);*/
            }
        });
        adapter.setIsLoadMoreEnabled(true, R.layout.load_more_progress, recyclerView, new LoadMoreRecyclerAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Utility.showLog(ActivityAllTrips.class, "LoadMoreFired");
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.onLoadMoreComplete();
                    }
                }, 2000);
            }
            @Override
            public void onLoadMoreWithParameters(int v, int p, int t)
            {

            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void loadData() {

        progressBar.setVisibility(View.VISIBLE);
//        loadList();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadList();
    }

    private void loadList() {

        String id = ConstantCode.DEFAULT_USER_ID;
        if (Utility.getLoggedInUser() != null)
            id = Utility.getLoggedInUser().id + "";

        WebUtils.call(WebServiceConfig.WebService.RIDER_LANDING, new String[]{id}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                progressBar.setVisibility(View.GONE);
                JSONObject json = (JSONObject) values;

                Type collectionTypeDayType = new TypeToken<ArrayList<RiderProfile>>() {
                }.getType();
                ArrayList<RiderProfile> list = Utility.parseArrayFromString(json.optString(ConstantCode.data), RiderProfile[].class);
                adapter.setItem(list);
                swipeToRefreshLayout.setRefreshing(false);
                enableOrDisableScrolling();

                if (Utility.getLoggedInUser() != null) {
                    for (RiderProfile riderProfile : list) {
                        riderProfile.id = Utility.getLoggedInUser().id;
                        riderProfile.save();
                    }
                }
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
                progressBar.setVisibility(View.GONE);
                showToast(values.toString());
                enableOrDisableScrolling();
                swipeToRefreshLayout.setRefreshing(false);
            }

            @Override
            public void loadOffline() {
                super.loadOffline();
                progressBar.setVisibility(View.GONE);
                ArrayList<RiderProfile> list = RiderProfile.readAll();
                adapter.setItem(list);
                swipeToRefreshLayout.setRefreshing(false);
                enableOrDisableScrolling();

                for (RiderProfile riderProfile : list) {
                    riderProfile.id = Utility.getLoggedInUser().id;
                    riderProfile.save();
                }
            }
        });
    }

    public final static class CustomAdapter extends LoadMoreRecyclerAdapter {
        private ArrayList<RiderProfile> riderProfileList;
        private Context context;

        public CustomAdapter(Context context) {
            this.context = context;
            riderProfileList = new ArrayList<RiderProfile>();
        }

        public RiderProfile getItem(int position) {
            if (riderProfileList.size() > position) return riderProfileList.get(position);
            return null;
        }

        public void setItem(ArrayList<RiderProfile> list) {
            riderProfileList = list;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_carnot_view, parent, false);
            CustomViewHolder holder = new CustomViewHolder(view);
            return holder;
        }

        @Override
        public void onActualBindViewHolder(final RecyclerView.ViewHolder holder1, final int position) {
            final CustomViewHolder holder = ((CustomViewHolder) holder1);

            RiderProfile item = riderProfileList.get(position);

            holder.txt.setText(item.name);
            holder.txtGenderAge.setText(("F".equalsIgnoreCase(item.gender) ? "Female" : "Male") + " " + (item.age != null ? item.age : ""));
            holder.txtTrips.setText(item.nTrips + "");
            holder.txtDriveScore.setText(item.driverscore + "");
            Utility.showCircularImageView(context, holder.img, item.photo, R.drawable.ic_profile_circle_black);

            /*Glide
                    .with(context)
                    .load(item.photo)
                    .transform(new CircleTransform(context))
                    .placeholder(R.drawable.ic_profile_circle_black)
//                    .centerCrop()
                    .error(R.drawable.ic_profile_circle_black)
                    .crossFade()
                    .into(holder.img);*/

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(null, holder.itemView, position, 0);
                }
            });
        }

        @Override
        public int onActualItemCount() {
            return riderProfileList.size();
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {
            public TextView txt, txtGenderAge, txtTrips, txtDriveScore;
            public ImageView img;

            public CustomViewHolder(View view) {
                super(view);
                txt = (TextView) view.findViewById(R.id.txt_name);
                txtGenderAge = (TextView) view.findViewById(R.id.txt_gender_age);
                txtTrips = (TextView) view.findViewById(R.id.txt_trips);
                txtDriveScore = (TextView) view.findViewById(R.id.txt_drive_score);
                img = (ImageView) view.findViewById(R.id.img);
            }
        }

        AdapterView.OnItemClickListener clickListener;

        public void setOnItemClickListener(AdapterView.OnItemClickListener clickListener) {
            this.clickListener = clickListener;
        }
    }

    /**
     * This method is to stop the appbaractivity to quick scroll when there is not much amount of data to scroll.
     */
    private void enableOrDisableScrolling() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (recyclerView.canScrollVertically(DOWN) || recyclerView.canScrollVertically(UP)) {
                    ((ActivityDashboard) mActivity).enableScroll();
                } else {
                    ((ActivityDashboard) mActivity).disableScroll();
                }
            }
        }, 100);
    }
}