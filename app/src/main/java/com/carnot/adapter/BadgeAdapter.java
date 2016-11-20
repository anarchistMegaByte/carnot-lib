package com.carnot.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.custom_views.DriveScoreView;
import com.carnot.dialogs.BadgeRewardDialog;
import com.carnot.models.BadgeModel;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter {

    private final List<BadgeModel> data;
    private Context mContext;


    public BadgeAdapter(Context context, final List<BadgeModel> data) {
        this.data = data;

    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        this.mContext = parent.getContext();
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.inflate_badge_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final BadgeModel item = data.get(position);

        ((ViewHolder) holder).ivbadge.setImageResource(item.res_id_badge);
        ((ViewHolder) holder).txtName.setText(item.sBadgeName);
        if (TextUtils.isEmpty(item.sBadgeName)) {
            ((ViewHolder) holder).ivTransparent.setVisibility(View.GONE);
        } else {
            ((ViewHolder) holder).ivTransparent.setVisibility(View.VISIBLE);

        }
        ((ViewHolder) holder).ivTransparent.setScore(item.percentage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BadgeRewardDialog dialog = new BadgeRewardDialog(mContext, null);
                dialog.show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivbadge;

        DriveScoreView ivTransparent;
        TextView txtName;

        public ViewHolder(View v) {
            super(v);


            ivbadge = (ImageView) v.findViewById(R.id.iv_badge);
            ivTransparent = (DriveScoreView) v.findViewById(R.id.drive_score_view);
            txtName = (TextView) v.findViewById(R.id.txt_name);

        }
    }
}