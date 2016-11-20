package com.carnot.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.global.gcm.GCMMessage;
import com.carnot.libclasses.DateHelper;
import com.carnot.libclasses.LoadMoreRecyclerAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NotificationsAdapter extends LoadMoreRecyclerAdapter {

    private List<GCMMessage> data;
    private Context context;
    private ArrayList<Boolean> expandState;

    public NotificationsAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    /*public NotificationsAdapter(final List<GCMMessage> data) {
        this.data = data;
        expandState = new ArrayList<Boolean>();
        for (int i = 0; i < data.size(); i++) {
            expandState.add(false);
        }
    }*/

    public void setItems(ArrayList<GCMMessage> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    public GCMMessage getItem(int position) {
        return data.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.inflate_notifications, parent, false));
    }

    @Override
    public void onActualBindViewHolder(final RecyclerView.ViewHolder holder1, final int position) {
        final GCMMessage item = data.get(position);
        ViewHolder holder = (ViewHolder) holder1;
        holder.txtNotificationTitle.setText(item.title);
        holder.txtMessage.setText(item.message);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(item.timestamp);
        holder.txtDate.setText(DateHelper.getFormatedDate(calendar, DateHelper.DATE_FORMAT_NOTIFICATION_LIST));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onItemClick(null, holder1.itemView, position, 0);
                }
            }
        });
    }

    @Override
    public int onActualItemCount() {
        return data.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNotificationTitle, txtMessage, txtDate;
        ImageView img;

        public ViewHolder(View v) {
            super(v);
            img = (ImageView) v.findViewById(R.id.img);
            txtDate = (TextView) v.findViewById(R.id.txt_date);
            txtMessage = (TextView) v.findViewById(R.id.txt_message);
            txtNotificationTitle = (TextView) v.findViewById(R.id.txt_notification_title);
        }
    }

    AdapterView.OnItemClickListener clickListener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }
}