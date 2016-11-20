package com.carnot.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.carnot.R;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mayank on 30-12-2015.
 */
public class ServiceNameAdapter extends RecyclerView.Adapter<ServiceNameAdapter.ViewHolder> {

    private Context mContext;
    private List<String> lstServices;
    private ArrayList<Boolean> selectedStatus;

    public ServiceNameAdapter(Context context, List<String> lstErros) {
        this.mContext = context;
        this.lstServices = lstErros;
        selectedStatus = new ArrayList<>();
        for (String lstService : this.lstServices) {
            selectedStatus.add(false);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.inflate_service_with_checkbox, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;

    }


    @Override
    public void onBindViewHolder(final ServiceNameAdapter.ViewHolder holder, final int position) {

        holder.tvServiceName.setText(lstServices.get(position));
        holder.chk.setChecked(selectedStatus.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean status = !selectedStatus.get(position);
                holder.chk.setChecked(status);
                selectedStatus.set(position, status);
            }
        });
        holder.chk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean status = !selectedStatus.get(position);
                holder.chk.setChecked(status);
                selectedStatus.set(position, status);
            }
        });
    }

    public JSONArray getSelectedJSONArray() {
        JSONArray jsonArray = new JSONArray();
        int i = 0;
        for (Boolean selectedStatu : selectedStatus) {
            if (selectedStatu) {
                jsonArray.put(lstServices.get(i));
            }
            i++;
        }
        return jsonArray;
    }

    @Override
    public int getItemCount() {
        return lstServices != null ? lstServices.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvServiceName;
        CheckBox chk;

        public ViewHolder(View itemView) {
            super(itemView);

            tvServiceName = (TextView) itemView.findViewById(R.id.tv_service_name);
            chk = (CheckBox) itemView.findViewById(R.id.chk);
        }
    }

    public synchronized void setList(List<String> arrListing) {
        this.lstServices = arrListing;
        notifyDataSetChanged();

    }

}
