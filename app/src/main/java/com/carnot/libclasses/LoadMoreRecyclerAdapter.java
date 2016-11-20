package com.carnot.libclasses;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.carnot.R;

/**
 * Created by root on 4/4/16.
 */
public abstract class LoadMoreRecyclerAdapter extends RecyclerView.Adapter {
    public boolean isLoadMoreEnabled = false;
    private int loadMoreRes;
    public int VIEW_PROG = 100;
    private ProgressBar progressBar;

    private int visibleItemCount, pastVisiblesItems, totalItemCount;
    boolean isLoading = false;

    public Object getItem(int position) {
        return null;
    }

    public void setIsLoadMoreEnabled(boolean isLoadMoreEnabled, int loadMoreRes, final RecyclerView recyclerView, final OnLoadMoreListener onLoadMoreListener) {
        this.isLoadMoreEnabled = isLoadMoreEnabled;
        this.loadMoreRes = loadMoreRes;

        if (isLoadMoreEnabled) {

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (dy > 0) {

                        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                            visibleItemCount = linearLayoutManager.getChildCount();
                            totalItemCount = linearLayoutManager.getItemCount();
                            pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                            if (isLoading) return;
                            //Log.e("LOAD MORE : ",visibleItemCount+ " "+ pastVisiblesItems + " "+ totalItemCount);
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                if (onLoadMoreListener != null) {
                                    isLoading = true;
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.VISIBLE);
                                    }
                                    onLoadMoreListener.onLoadMore();
                                    onLoadMoreListener.onLoadMoreWithParameters(visibleItemCount, pastVisiblesItems, totalItemCount);
                                }
                            }
                        }
                    }
                }
            });

            /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                            visibleItemCount = linearLayoutManager.getChildCount();
                            totalItemCount = linearLayoutManager.getItemCount();
                            pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                            if (isLoading) return;
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                if (onLoadMoreListener != null) {
                                    isLoading = true;
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.VISIBLE);
                                    }
                                    onLoadMoreListener.onLoadMore();
                                }
                            }
                        }
                    }
                });

            } else {
                recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                            visibleItemCount = linearLayoutManager.getChildCount();
                            totalItemCount = linearLayoutManager.getItemCount();
                            pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                            if (isLoading) return;
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                if (onLoadMoreListener != null) {
                                    isLoading = true;
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.VISIBLE);
                                    }
                                    onLoadMoreListener.onLoadMore();
                                }
                            }
                        }
                    }
                });
            }*/
        }
    }

    public void onLoadMoreComplete() {

        if (isLoadMoreEnabled) {
            isLoading = false;
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
        void onLoadMoreWithParameters(int v, int p, int t);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_PROG) {
            View v = LayoutInflater.from(parent.getContext()).inflate(loadMoreRes, parent, false);
            return new ProgressViewHolder(v);
        } else {
            return onActualCreateViewHolder(parent, viewType);
        }
    }

    public abstract RecyclerView.ViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == VIEW_PROG) {

        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(null, holder.itemView, position, 0);
                }
            });
            onActualBindViewHolder(holder, position);
        }
    }

    AdapterView.OnItemClickListener clickListener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public abstract void onActualBindViewHolder(RecyclerView.ViewHolder holder, int position);

    @Override
    public int getItemViewType(int position) {

        if (isLoadMoreEnabled) {
            return position == getItemCount() - 1 ? VIEW_PROG : getActualItemViewType(position);
        } else {
            return getActualItemViewType(position);
        }
    }

    public int getActualItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        if (isLoadMoreEnabled)
            return onActualItemCount() + 1;
        return onActualItemCount();
    }

    public abstract int onActualItemCount();

    public class ProgressViewHolder extends RecyclerView.ViewHolder {


        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.load_more_progress);
        }
    }
}
