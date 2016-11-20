package com.carnot.activity;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.carnot.R;
import com.carnot.adapter.BadgeAdapter;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.ItemDecorationAlbumColumns;
import com.carnot.models.BadgeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by javid on 31/3/16.
 */
public class ActivityAllBadge extends BaseActivity {


    RecyclerView recyclerViewBadgeEarned, recyclerViewBadgeToBeEarned;
    private List<BadgeModel> listBadgeEarned, listBadgeToBeEarned;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_badge);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle(getString(R.string.lbl_all_badges));
        loadDummyData();
        ((Toolbar)links(R.id.toolbar)).setBackgroundResource(R.color.transparent);
    }

    @Override
    public void initView() {


        recyclerViewBadgeEarned = (RecyclerView) links(R.id.recycler_view_earnbadge);

        recyclerViewBadgeToBeEarned = (RecyclerView) links(R.id.recycler_view_to_be_earnbadge);



    }

    private void loadDummyData() {
        // dummy records for Badges Earned
        BadgeModel blog = new BadgeModel();
        listBadgeEarned = new ArrayList<BadgeModel>();

        blog.res_id_badge = R.drawable.ic_badge_kms;
        blog.percentage = 0;
        blog.sBadgeName = getString(R.string.badge_1000_KMS);

        listBadgeEarned.add(blog);


        blog = new BadgeModel();
        blog.res_id_badge = R.drawable.ic_badge_hrs;
        blog.percentage = 0;
        blog.sBadgeName = getString(R.string.badge_100_HRS);


        listBadgeEarned.add(blog);

        blog = new BadgeModel();
        blog.res_id_badge = R.drawable.ic_badge_warrion;
        blog.percentage = 0;
        blog.sBadgeName = getString(R.string.badge_ECO_WARRIOR);


        listBadgeEarned.add(blog);

        // end here for Badges Earned


        // dummy records for Badges  to be earned

        blog = new BadgeModel();
        listBadgeToBeEarned = new ArrayList<BadgeModel>();

        blog.res_id_badge = R.drawable.ic_badge_kms;
        blog.percentage = 25;
        blog.sBadgeName = getString(R.string.badge_5000_KMS);

        listBadgeToBeEarned.add(blog);


        blog = new BadgeModel();
        blog.res_id_badge = R.drawable.ic_badge_hrs;
        blog.percentage = 50;
        blog.sBadgeName = getString(R.string.badge_10000_KMS);


        listBadgeToBeEarned.add(blog);


        blog = new BadgeModel();
        blog.res_id_badge = R.drawable.ic_badge_warrion;
        blog.percentage = 75;
        blog.sBadgeName = getString(R.string.badge_50000_KM);


        listBadgeToBeEarned.add(blog);


        blog = new BadgeModel();
        blog.res_id_badge = R.drawable.ic_badge_kms;
        blog.percentage = 25;
        blog.sBadgeName = getString(R.string.badge_200_HRS);

        listBadgeToBeEarned.add(blog);


        blog = new BadgeModel();
        blog.res_id_badge = R.drawable.ic_badge_hrs;
        blog.percentage = 50;
        blog.sBadgeName = getString(R.string.badge_400_HRS);


        listBadgeToBeEarned.add(blog);

        blog = new BadgeModel();
        blog.res_id_badge = R.drawable.ic_badge_warrion;
        blog.percentage = 75;
        blog.sBadgeName = getString(R.string.badge_800_HRS);


        listBadgeToBeEarned.add(blog);
    }

    @Override
    public void postInitView() {

    }

    @Override
    public void addAdapter() {


        BadgeAdapter customAdapter = new BadgeAdapter(ActivityAllBadge.this, listBadgeEarned);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 3);
        recyclerViewBadgeEarned.setLayoutManager(gridLayoutManager);
        recyclerViewBadgeEarned.setAdapter(customAdapter);


        customAdapter = new BadgeAdapter(ActivityAllBadge.this, listBadgeToBeEarned);
        gridLayoutManager = new GridLayoutManager(mActivity, 3);
        recyclerViewBadgeToBeEarned.setLayoutManager(gridLayoutManager);

        recyclerViewBadgeToBeEarned.addItemDecoration(new ItemDecorationAlbumColumns(getResources().getDimensionPixelSize(R.dimen.scale_5dp), 3));
        recyclerViewBadgeToBeEarned.setAdapter(customAdapter);


    }

    @Override
    public void loadData() {


    }


}
