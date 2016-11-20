package com.carnot.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.activity.ActivityErrorCodeDetail;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.models.CarErrors;

import java.util.ArrayList;

/**
 * Created by Mayank on 30-12-2015.
 */
public class ErrorCodesAdapter extends RecyclerView.Adapter<ErrorCodesAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<CarErrors> lstErros;
    private int selectedPos = 0;

    public ErrorCodesAdapter(Context context, ArrayList<CarErrors> lstErros) {
        this.mContext = context;
        this.lstErros = lstErros;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.inflate_errorcode_view, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;

    }


    @Override
    public void onBindViewHolder(ErrorCodesAdapter.ViewHolder holder, final int position) {

        holder.llErrorCode.setText(lstErros.get(position).code);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String blog = lstErros.get(position).code;
                CallErrorCodeDetail(lstErros.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return lstErros != null ? lstErros.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView llErrorCode;

        public ViewHolder(View itemView) {
            super(itemView);

            llErrorCode = (TextView) itemView.findViewById(R.id.lbl_error_code);
        }
    }

    public synchronized void setList(ArrayList<CarErrors> arrListing) {
        this.lstErros = arrListing;
        notifyDataSetChanged();

    }

    private void CallErrorCodeDetail(CarErrors errorCode) {
        Intent intent = new Intent(mContext, ActivityErrorCodeDetail.class);
        intent.putExtra(ConstantCode.INTENT_DATA, Utility.getJsonString(errorCode)); //sending car error json object
        mContext.startActivity(intent);

    }
}
