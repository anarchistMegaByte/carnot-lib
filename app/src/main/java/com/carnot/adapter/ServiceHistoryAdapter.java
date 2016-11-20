package com.carnot.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.carnot.R;
import com.carnot.custom_views.expandablelayout.ExpandableRelativeLayout;
import com.carnot.models.Document;

import java.util.ArrayList;
import java.util.List;


public class ServiceHistoryAdapter extends RecyclerView.Adapter<ServiceHistoryAdapter.ViewHolder> {

    private List<Document> data;
    private Context context;
    private ArrayList<Boolean> expandState;

    public ServiceHistoryAdapter() {

    }

    public ServiceHistoryAdapter(final List<Document> data) {
        this.data = data;
        expandState = new ArrayList<Boolean>();
        for (int i = 0; i < data.size(); i++) {
            expandState.add(false);
        }
    }

    public void setItems(final List<Document> data) {
        this.data = data;
        expandState = new ArrayList<Boolean>();
        for (int i = 0; i < data.size(); i++) {
            expandState.add(false);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.inflate_servicedetail_view, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Document item = data.get(position);

        holder.tvCenterName.setText(item.info);
        holder.tvDate.setText(item.date);
//        holder.imgDoc.setImageURI(Uri.parse(item.img));

        Glide.with(context)
                .load(item.img)
                .error(R.drawable.img_no_image)
                .crossFade()
                .into(holder.imgDoc);


        holder.expandableLayout.setExpanded(expandState.get(position));


        holder.ivArrow.setSelected(expandState.get(position));

        holder.ivServicePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = true;
                if (holder.expandableLayout.isExpanded()) {
                    state = false;
                }
                expandState.set(position, state);
                holder.ivArrow.setSelected(state);
                holder.expandableLayout.toggle();

                if (expandState.get(position)) {
                    holder.tvCenterName.setVisibility(View.VISIBLE);
                    holder.ivServicePic.setVisibility(View.VISIBLE);
                } else {
                    holder.tvCenterName.setVisibility(View.GONE);
                    holder.ivServicePic.setVisibility(View.GONE);
                }

            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = true;
                if (holder.expandableLayout.isExpanded()) {
                    state = false;
                }
                expandState.set(position, state);
                holder.ivArrow.setSelected(state);
                holder.expandableLayout.toggle();

                if (expandState.get(position)) {
                    holder.tvCenterName.setVisibility(View.VISIBLE);
                    holder.ivServicePic.setVisibility(View.VISIBLE);
                } else {
                    holder.tvCenterName.setVisibility(View.GONE);
                    holder.ivServicePic.setVisibility(View.GONE);
                }

            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public void clear() {
        if (data != null) {
            int last = data.size();
            data.clear();
            notifyItemRangeRemoved(0, last);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvDate, tvCenterName;
        public ImageView ivServicePic, ivArrow;
        public ImageView imgDoc;
        public ExpandableRelativeLayout expandableLayout;

        public ViewHolder(View v) {
            super(v);
            tvDate = (TextView) v.findViewById(R.id.tv_date);
            tvCenterName = (TextView) v.findViewById(R.id.tv_center_name);

            ivServicePic = (ImageView) v.findViewById(R.id.iv_service_picture);
            imgDoc = (ImageView) v.findViewById(R.id.img_doc);
            ivArrow = (ImageView) v.findViewById(R.id.iv_icon_dropdown);
            expandableLayout = (ExpandableRelativeLayout) v.findViewById(R.id.service_inflate_expandableLayout);
            expandableLayout.setExpanded(false);

        }
    }


}