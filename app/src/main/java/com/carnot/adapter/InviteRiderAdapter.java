package com.carnot.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.custom_views.expandablelayout.ExpandableRelativeLayout;
import com.carnot.models.AudibleErrors;
import com.carnot.models.RiderModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class InviteRiderAdapter extends RecyclerView.Adapter<InviteRiderAdapter.ViewHolder> {

    private List<RiderModel> data;
    private Context context;

    public InviteRiderAdapter(final List<RiderModel> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.inflate_invite_rider_view, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final RiderModel item = data.get(position);

        holder.tvGenderNAge.setText(item.sGender + " , " + item.sAge);
        holder.tvName.setText(item.sRiderName);

        if (item.isInvited) {
            holder.ivStatus.setImageResource(R.drawable.ic_selected_black_small);
        } else {
            holder.ivStatus.setImageResource(R.drawable.ic_add_profile);
        }
        holder.ivStatus.setOnClickListener(new View.OnClickListener() {
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



    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvGenderNAge, tvName, tvTime;
        public ImageView ivRiderpic, ivStatus;


        public ViewHolder(View v) {
            super(v);

            ivRiderpic = (ImageView) v.findViewById(R.id.iv_riderpic);
            tvName = (TextView) v.findViewById(R.id.tv_ridername);
            tvGenderNAge = (TextView) v.findViewById(R.id.tv_gender_age);
            ivStatus = (ImageView) v.findViewById(R.id.iv_status);

        }
    }

    public void setFilter(List<RiderModel> countryModels) {
        data = new ArrayList<>();
        data.addAll(countryModels);
        notifyDataSetChanged();
    }

    /*private static class RiderFilter extends Filter {

        private final InviteRiderAdapter adapter;

        private final List<RiderModel> originalList;

        private final List<RiderModel> filteredList;

        private RiderFilter(InviteRiderAdapter adapter, List<RiderModel> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = new LinkedList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();

                for (final RiderModel rider : originalList) {
                    if (rider.sRiderName.contains(filterPattern)) {
                        filteredList.add(rider);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.data.clear();
            adapter.data.addAll((ArrayList<RiderModel>) results.values);
            adapter.notifyDataSetChanged();
        }
    }
*/
}