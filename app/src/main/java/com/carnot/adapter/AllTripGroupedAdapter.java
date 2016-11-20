package com.carnot.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.custom_views.DriveScoreView;
import com.carnot.custom_views.MileageView;
import com.carnot.global.Utility;
import com.carnot.libclasses.DateHelper;
import com.carnot.libclasses.LoadMoreRecyclerAdapter;
import com.carnot.models.Cars;
import com.carnot.models.TripDetailMain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AllTripGroupedAdapter extends LoadMoreRecyclerAdapter {

    private final int ITEM_HEADER = 1;
    private final int ITEM_DATA = 2;

    private List<TripDetailMain> data;
    private Context context;
    private ArrayList<Boolean> expandState;

    public AllTripGroupedAdapter(Context context) {
        this.context = context;
        expandState = new ArrayList<Boolean>();

    }

    public void setItem(final List<TripDetailMain> data) {
        this.data = data;
        expandState.clear();
        for (int i = 0; i < data.size(); i++) {
            expandState.add(false);
        }
        notifyDataSetChanged();
    }

    public void setItem(final List<TripDetailMain> data, int sizeToUse) {
        if (data != null && data.size() > 0) {
            this.data = data.subList(0, sizeToUse);
            for (int i = 0; i < Math.min(data.size(), sizeToUse); i++) {
                expandState.add(false);
            }
            notifyDataSetChanged();
        }
    }

    @Override

    public int getActualItemViewType(int position) {
//        super.getActualItemViewType(position);
        if (getItem(position).trip_id == 0 && getItem(position).timestamp == null) {
            return ITEM_HEADER;
        } else {
            return ITEM_DATA;
        }
    }

    @Override
    public RecyclerView.ViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        if (viewType == ITEM_DATA) {
            return new ViewHolderData(LayoutInflater.from(context).inflate(R.layout.inflate_trip_view, parent, false));
        } else {
            return new ViewHolderHeader(LayoutInflater.from(context).inflate(R.layout.inflate_text_header, parent, false));
        }
    }


    @Override
    public void onActualBindViewHolder(final RecyclerView.ViewHolder holder1, final int position) {
        final TripDetailMain item = data.get(position);

        if (holder1 instanceof ViewHolderData) {

            ViewHolderData holder = (ViewHolderData) holder1;

        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onItemClick(null, holder1.itemView, position, 0);
            }
        });*/

            holder.driveScoreView.setScore((int) item.drive_score);
            holder.txtDriveScore.setText(String.valueOf(item.drive_score));
            holder.txtMileage.setText(String.valueOf(item.avg_mileage));
            holder.mileageView.setScore((int) item.avg_mileage);
            //TODO: Remove
            //Utility.showCircularImageView(context, holder.imgPhoto, (String) item.photo, R.drawable.ic_profile_circle_black);
            String date = DateHelper.getFormatedDate(item.start_time, new String[]{DateHelper.DATE_FORMAT_TRIP_SERVER1, DateHelper.DATE_FORMAT_TRIP_SERVER2}, DateHelper.DATE_FORMAT_TRIP_LOCAL, true, true);
            holder.txtDateTime.setText(date);
            //TODO:
            //holder.txtName.setText(TextUtils.isEmpty(item.name) ? "" : item.name);
            holder.txtName.setText("TRIP DISTANCE");
            holder.tripDist.setText(item.distance + "");
//            holder.txtName.setText(item.id + "-" + item.distance);
            Cars car = Cars.readSpecific(String.valueOf(item.car_id));
            if(car.isOnTrip && car.serverTripID == item.trip_id)
            {
                if(Utility.isConnectingToInternet(context))
                {
                    holder.txtAddress.setText(" | " +"OnTrip");
                }
                Calendar syncTime = DateHelper.getCalendarFromServer(car.lut);
                if (syncTime != null) {
                    Calendar todayTime = Calendar.getInstance();
                    if (todayTime.getTimeInMillis() - syncTime.getTimeInMillis() > 0) {
                        //getting minutes between the last sync time and current time
                        long min = TimeUnit.MILLISECONDS.toMinutes(Math.abs(todayTime.getTimeInMillis() - syncTime.getTimeInMillis()));
                        if (min > 10) {
                            holder.txtAddress.setText(" | " + "Trip Sync Interrupted");
                            holder.txtAddress.setTextColor(Color.YELLOW);
                        }
                    }
                }
            }
            else
            {
                if (!TextUtils.isEmpty(item.start) && !TextUtils.isEmpty(item.end)) {
                    holder.txtAddress.setText(" | " + item.start + " - " + item.end);
                } else if (!TextUtils.isEmpty(item.start)) {
                    holder.txtAddress.setText(" | " + item.start);
                } else if (!TextUtils.isEmpty(item.end)) {
                    holder.txtAddress.setText(" | " + item.end);
                } else {
                    holder.txtAddress.setText("");
                }
                holder.txtAddress.setTextColor(Color.WHITE);
            }

        } else if (holder1 instanceof ViewHolderHeader) {
            ViewHolderHeader holder = (ViewHolderHeader) holder1;
            holder.txtHeaders.setText(item.headers);
        }

    }

    public TripDetailMain getItem(int position) {
        TripDetailMain blog = null;

        if (data != null)
            blog = data.get(position);
        return blog;
    }

    @Override
    public int onActualItemCount() {

        if (data != null)
            return data.size();
        return 0;
    }

    public static class ViewHolderData extends RecyclerView.ViewHolder {
        DriveScoreView driveScoreView;
        MileageView mileageView;
        ImageView imgPhoto;
        TextView txtName, txtMileage, txtDriveScore, txtDateTime, txtAddress, tripDist;

        public ViewHolderData(View v) {
            super(v);
            tripDist = (TextView)v.findViewById(R.id.tripDistance);
            driveScoreView = (DriveScoreView) v.findViewById(R.id.drive_score_view);
            mileageView = (MileageView) v.findViewById(R.id.mileage_view);
            //TODO:
            //imgPhoto = (ImageView) v.findViewById(R.id.img_photo);
            txtName = (TextView) v.findViewById(R.id.txt_name);
            txtMileage = (TextView) v.findViewById(R.id.txt_mileage);
            txtDriveScore = (TextView) v.findViewById(R.id.txt_drive_score);
            txtDateTime = (TextView) v.findViewById(R.id.txt_date_time);
            txtAddress = (TextView) v.findViewById(R.id.txt_address);

        }
    }

    public static class ViewHolderHeader extends RecyclerView.ViewHolder {

        TextView txtHeaders;

        public ViewHolderHeader(View v) {
            super(v);

            txtHeaders = (TextView) v.findViewById(R.id.txt_header);

        }
    }


    /*AdapterView.OnItemClickListener clickListener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }*/
}