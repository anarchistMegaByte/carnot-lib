package com.carnot.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.custom_views.expandablelayout.ExpandableRelativeLayout;
import com.carnot.global.Utility;
import com.carnot.models.AudibleErrors;

import java.util.ArrayList;
import java.util.List;


public class AudibleErrorsAdapter extends RecyclerView.Adapter<AudibleErrorsAdapter.ViewHolder> {

    private final List<AudibleErrors> data;
    private Context context;
    private ArrayList<Boolean> expandState;
    private MediaPlayer mediaPlayer;

    public AudibleErrorsAdapter(final List<AudibleErrors> data) {
        this.data = data;
        expandState = new ArrayList<Boolean>();
        for (int i = 0; i < data.size(); i++) {
            expandState.add(false);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.inflate_audibleerror_view, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final AudibleErrors item = data.get(position);

        holder.tvDesc.setText(item.sFileDesc);
        holder.tvName.setText(item.sFilename);
        //holder.tvTime.setText(Utility.getHrsMinsSec(item.milliseconds / 1000, "mm:ss"));
        holder.tvTime.setText("");
        holder.tvTime.setVisibility(View.VISIBLE);
        fillsErros(holder.recyclerViewErrors, item);
        holder.expandableLayout.setExpanded(expandState.get(position));
        if (position == 4) {
            int i;
        }

        holder.ivCheked.setSelected(expandState.get(position));
        holder.imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.imgPlay.isSelected()) {
                    pause(lastPlayIcon);
                } else {
                    play(holder.imgPlay, item.res);
                }
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onItemClick(null, holder.itemView, position, 0);
                }
            }
        });
    }


    private ImageView lastPlayIcon;

    private void play(ImageView img, int res) {

        pause(lastPlayIcon);

        //creating new player and starting
        mediaPlayer = MediaPlayer.create(context, res);
        mediaPlayer.setOnCompletionListener(mediaCompletionListener);
        mediaPlayer.start();
        lastPlayIcon = img;
        img.setSelected(true);
    }

    private void pause(ImageView img) {
        //Change last status of player
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            lastPlayIcon = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (lastPlayIcon != null) {
            lastPlayIcon.setSelected(false);
        }

        if (img != null)
            img.setSelected(false);
    }

    MediaPlayer.OnCompletionListener mediaCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            pause(lastPlayIcon);
        }
    };

    private void fillsErros(RecyclerView recyclerViewErrors, AudibleErrors item) {

        //start car scan animation here till get response from server and
        LinearLayoutManager layoutmanger = new LinearLayoutManager(recyclerViewErrors.getContext());
        recyclerViewErrors.setLayoutManager(layoutmanger);
        ErrorCodesAdapter adapter = new ErrorCodesAdapter(recyclerViewErrors.getContext(), item.arrErrors);
        recyclerViewErrors.setAdapter(adapter);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvDesc, tvName, tvTime;
        public ImageView ivCheked, imgPlay;
        public ExpandableRelativeLayout expandableLayout;
        public RecyclerView recyclerViewErrors;

        public ViewHolder(View v) {
            super(v);
            tvDesc = (TextView) v.findViewById(R.id.audible_inflate_error_desc);
            tvName = (TextView) v.findViewById(R.id.audible_inflate_tv_filename);
            tvTime = (TextView) v.findViewById(R.id.audible_inflate_tv_filetime);
            imgPlay = (ImageView) v.findViewById(R.id.img_play);

            ivCheked = (ImageView) v.findViewById(R.id.audible_inflate_iv_checkebox);
            expandableLayout = (ExpandableRelativeLayout) v.findViewById(R.id.audible_inflate_expandableLayout);
            recyclerViewErrors = (RecyclerView) v.findViewById(R.id.recycler_view_errors);
        }
    }

    AdapterView.OnItemClickListener clickListener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

}