package com.carnot.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.util.Log;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.carnot.R;
import com.carnot.custom_views.expandablelayout.ExpandableRelativeLayout;
import com.carnot.global.Utility;
import com.carnot.models.Document;

import java.util.ArrayList;
import java.util.List;


public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.ViewHolder> {

    private final List<Document> data;
    private Context context;

    public DocumentsAdapter() {
        data = new ArrayList<Document>();
    }

    /*public DocumentsAdapter(final List<Document> data) {
        this.data = data;
    }*/
    public void setItems(final List<Document> data) {
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    public void addItem(Document document) {
        this.data.add(document);
        notifyItemInserted(this.data.size() - 1);
    }

    public void clear() {
        if (data != null) {
            int last = data.size();
            data.clear();
            notifyItemRangeRemoved(0, last);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.inflate_document_view, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Document item = data.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onItemClick(null, holder.itemView, position, 0);
            }
        });

        holder.tvDocumentName.setText(item.info);
        holder.tvTitle.setText(!TextUtils.isEmpty(item.no) ? item.no : item.date);
//        item.img = "http://www.popsci.com/sites/popsci.com/files/styles/large_1x_/public/new-google-logo.jpg?itok=ZdIobGek&";

        Log.d("IMAGE_FRESCO", "URL :" + item.img);

        holder.progressBar.setVisibility(View.VISIBLE);
        Glide.with(context)
                .load(item.img)
                .error(R.drawable.img_no_image)
                .crossFade()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.ivDocumentPicture);

//        Log.i("TAG", item.img.replace("\\", ""));
        if (item.isAlert) {
            holder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_alert_red_small, 0);
            holder.tvTitle.setCompoundDrawablePadding((int) Utility.convertDpToPixel(context.getResources().getDimension(R.dimen.scale_10dp), context));
        } else {
            holder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    public Document getItem(int position) {
        return data.get(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvDocumentName, tvTitle;
        public ImageView ivDocumentPicture;
        public ExpandableRelativeLayout expandableLayout;
        public RecyclerView recyclerViewErrors;
        public ProgressBar progressBar;

        public ViewHolder(View v) {
            super(v);
            tvDocumentName = (TextView) v.findViewById(R.id.tv_document_name);
            tvTitle = (TextView) v.findViewById(R.id.tv_document_title);
            ivDocumentPicture = (ImageView) v.findViewById(R.id.iv_document_pic);
            progressBar = (ProgressBar) v.findViewById(R.id.progress_image);
        }
    }

    AdapterView.OnItemClickListener clickListener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

}