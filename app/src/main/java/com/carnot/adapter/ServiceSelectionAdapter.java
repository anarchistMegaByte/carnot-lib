package com.carnot.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.custom_views.expandablelayout.ExpandableRelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ServiceSelectionAdapter extends RecyclerView.Adapter<ServiceSelectionAdapter.ViewHolder> {

    private final List<String> data;
    private Context context;
    private ArrayList<Boolean> expandState;
    private String[] arrServices = new String[]{"Engine Oil(Check/Change)", "Filter(Change)", "Air Filter(Check/Change)", "Coolant(Check/Change)", "Break Filter(Check/Change)", "Gear Filter(Check/Change)", "Steering Filter(Check/Change)", "Battery Water(Check/Change)"};
    private ArrayList<ServiceNameAdapter> adapters;

    public ServiceSelectionAdapter(final List<String> data) {
        this.data = data;
        expandState = new ArrayList<Boolean>();
        adapters = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            expandState.add(false);
            adapters.add(null);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.inflate_service_selection_view, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final String item = data.get(position);


        holder.tvName.setText(item);

        if (adapters.get(position) == null) {
            adapters.set(position, new ServiceNameAdapter(context, Arrays.asList(arrServices)));
        }

        LinearLayoutManager layoutmanger = new LinearLayoutManager(context);
        holder.recyclerServices.setLayoutManager(layoutmanger);
        holder.recyclerServices.setAdapter(adapters.get(position));

//        fillsErros(adapters.get(position), );
        holder.expandableLayout.setExpanded(expandState.get(position));
        if (position == 4) {
            int i;
        }

        holder.ivCheked.setSelected(expandState.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = true;
                if (holder.expandableLayout.isExpanded()) {
                    state = false;
                }
                expandState.set(position, state);
                holder.ivCheked.setSelected(state);
                holder.expandableLayout.toggle();
            }
        });
        holder.ivCheked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = true;
                if (holder.expandableLayout.isExpanded()) {
                    state = false;
                }
                expandState.set(position, state);
                holder.ivCheked.setSelected(state);
                holder.expandableLayout.toggle();
            }
        });
    }

    public JSONObject fillValues(JSONObject meta) {

        int position = 0;
        for (String arrService : arrServices) {
            if (position == 0) { //TODO sending only first
                try {
                    meta.put(arrService, adapters.get(position).getSelectedJSONArray());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            position++;
        }
        return meta;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public ImageView ivCheked;
        public ExpandableRelativeLayout expandableLayout;
        public RecyclerView recyclerServices;

        public ViewHolder(View v) {
            super(v);

            tvName = (TextView) v.findViewById(R.id.tv_service_name);
            ivCheked = (ImageView) v.findViewById(R.id.iv_icon_dropdown);
            expandableLayout = (ExpandableRelativeLayout) v.findViewById(R.id.service_selection_inflate_expandableLayout);
            expandableLayout.setExpanded(false);
            recyclerServices = (RecyclerView) v.findViewById(R.id.recycler_services);
        }
    }


}