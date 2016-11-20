package com.carnot.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.custom_views.CounterAppTextViewMedium;
import com.carnot.custom_views.DriveScoreView;
import com.carnot.custom_views.MileageView;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.models.RiderProfile;
import com.carnot.models.Trip;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by javid on 31/3/16.
 */
public class ActivityRiderDashboard extends BaseActivity {


    private RecyclerView recyclerView;
    Button btnViewAllTrips;
    TextView txtViewAllTrips, txtTrips, txtDriveScore, txtIsOnTrip;
    CounterAppTextViewMedium txtTripCompleted, txtNoHardBreak, txtHardAcceleration, txtIdlingTime, txtAverageMileage;
    DriveScoreView driveScore;
    RecyclerView recyclerViewHighlights;

    private String arrayWeeks[] = new String[]{"S", "M", "T", "W", "T", "F", "S"};
    String xVals[] = new String[]{"0", "30", "60", "90", "120", "150", "180"};

    RiderProfile model = null;
    CardView cardView;
    LineChart linechar;
    View shadowView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_dashboard);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle("Amit Gupta");
        try {
            model = (RiderProfile) Utility.parseFromString(getIntent().getStringExtra(ConstantCode.INTENT_DATA), RiderProfile.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((Toolbar) findViewById(R.id.toolbar)).setBackgroundResource(R.color.transparent);

        if (model.isOnTrip) {
            getSupportActionBar().setSubtitle("ON TRIP");
        }
    }

    @Override
    public void initView() {

        cardView = (CardView) links(R.id.card_view);
        shadowView = (View) links(R.id.shadow_view);
//        cardView.setShadowPadding(0, 0, 0, 0);
        cardView.setMaxCardElevation(0);

        txtTripCompleted = (CounterAppTextViewMedium) links(R.id.counter_txt_trip_completed);
        txtNoHardBreak = (CounterAppTextViewMedium) links(R.id.counter_txt_no_hard_breaks);
        txtHardAcceleration = (CounterAppTextViewMedium) links(R.id.counter_txt_hard_accelerations);
        txtIdlingTime = (CounterAppTextViewMedium) links(R.id.counter_txt_idling_time);
        txtAverageMileage = (CounterAppTextViewMedium) links(R.id.counter_txt_average_mileage);

        txtTrips = (TextView) links(R.id.txt_trips);
        txtDriveScore = (TextView) links(R.id.txt_drive_score);
        driveScore = (DriveScoreView) links(R.id.drive_score);
        txtIsOnTrip = (TextView) links(R.id.txt_is_on_trip);

        cardView = (CardView) links(R.id.card_view);

        txtViewAllTrips = (TextView) links(R.id.txt_view_all_trips);
        btnViewAllTrips = (Button) links(R.id.btn_view_all_trips);

        ((TextView) links(R.id.tv_viewall_badges)).setOnClickListener(onClickListener);

        txtViewAllTrips.setOnClickListener(onClickListener);
        btnViewAllTrips.setOnClickListener(onClickListener);

        recyclerViewHighlights = (RecyclerView) links(R.id.recycler_view_highlights);
        linechar = (LineChart) links(R.id.linechart_driverscore);

    }


    @Override
    public void postInitView() {
        if (Utility.isAndroidAPILevelGreaterThenEqual(Build.VERSION_CODES.LOLLIPOP)) {
            shadowView.setVisibility(View.GONE);
        } else {
            shadowView.setVisibility(View.VISIBLE);
        }

        recyclerViewHighlights.setNestedScrollingEnabled(false);
        recyclerViewHighlights.setHasFixedSize(true);
    }

    @Override
    public void addAdapter() {

        ArrayList<Trip> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(new Trip());
        }

        CustomAdapter customAdapter = new CustomAdapter(list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerViewHighlights.setLayoutManager(linearLayoutManager);

//        VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration((int) Utility.convertDpToPixel(getResources().getDimension(R.dimen.scale_1dp), mActivity));
//        recyclerViewHighlights.addItemDecoration(decoration);

        recyclerViewHighlights.setAdapter(customAdapter);

    }

    @Override
    public void loadData() {
        loadPastTrip();
        loadMileageGraph(linechar);

        //Setting values

        txtTrips.setText(model.nTrips + "");
        txtDriveScore.setText(model.driverscore + "");
        driveScore.setScore(model.driverscore);
        if (model.isOnTrip) {
            txtIsOnTrip.setVisibility(View.VISIBLE);
        } else {
            txtIsOnTrip.setVisibility(View.INVISIBLE);
        }

//        txtTripCompleted.setCounter(model.nTrips);
//        txtNoHardBreak.setCounter(model.hardBreak);
//        txtHardAcceleration.setCounter(model.hardAcc);
//        txtIdlingTime.setCounter(model.idlingTime);
//        txtAverageMileage.setCounter(model.mileage);

    }

    private int[] mColors = new int[]{
            ColorTemplate.VORDIPLOM_COLORS[0],
            ColorTemplate.VORDIPLOM_COLORS[1],
            ColorTemplate.VORDIPLOM_COLORS[2]
    };

    private void loadMileageGraph(LineChart lineChart) {
//        lineChart = (LineChart) links(R.id.linechart_mileage);
       /* mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
       */
        lineChart.setDrawGridBackground(false);

        // no description text
        lineChart.setDescription("");
        lineChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        lineChart.setTouchEnabled(false);

        // enable scaling and dragging
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(false);
        lineChart.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.green_secondary_color));


        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(0f);
        llXAxis.disableDashedLine();
        llXAxis.setLabel("");


        /*XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisLineColor(ContextCompat.getColor(mActivity, R.color.transparent));
        xAxis.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
        xAxis.setDrawAxisLine(true);
        xAxis.setGridColor(ContextCompat.getColor(mActivity, R.color.transparent));
        xAxis.setDrawGridLines(true);
        xAxis.setLabelsToSkip(0);
*/
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisLineColor(ContextCompat.getColor(mActivity, R.color.transparent));
        xAxis.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
        xAxis.setDrawAxisLine(true);
        xAxis.setGridColor(ContextCompat.getColor(mActivity, R.color.transparent));
        xAxis.setDrawGridLines(false);
        xAxis.setLabelsToSkip(0);


        //    Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        /*LimitLine ll1 = new LimitLine(50f, "");
        ll1.setLineWidth(2f);
//        ll1.enableDashedLine(20f, 10f, 0f);
        ll1.setLabel("");
        ll1.setLineColor(ContextCompat.getColor(mActivity, android.R.color.white));
        lineChart.getAxisLeft().addLimitLine(ll1);
        */

        lineChart.getAxisLeft().setAxisMaxValue(200f);
        lineChart.getAxisLeft().setAxisMinValue(0f);
        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

        // add data
        setLineData(7, 100, lineChart);

        lineChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
//        mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = lineChart.getLegend();
        lineChart.getLegend().setEnabled(false);

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);

        // // dont forget to refresh the drawing
        // mChart.invalidate();
        if (lineChart.getData() != null) {
            lineChart.getData().setHighlightEnabled(false);


            List<ILineDataSet> sets = lineChart.getData()
                    .getDataSets();

            for (ILineDataSet iSet : sets) {

                LineDataSet set = (LineDataSet) iSet;

                set.setDrawCircles(true);
                set.setDrawValues(true);
                set.setDrawFilled(false);
                set.setDrawCubic(false);
                set.disableDashedLine();
                set.setLineWidth(2f);


            }
        }
        lineChart.setPinchZoom(false);
        lineChart.setAutoScaleMinMaxEnabled(false);
    }

    private void setLineData(int count, float range, LineChart barChartPastTrip) {


        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add(arrayWeeks[i]);
        }

        ArrayList<Entry> yVals = new ArrayList<Entry>();


        for (int i = 0; i < count; i++) {

            float mult = (range + 1);
            float val = (float) (Math.random() * mult) + 3;// + (float)

            yVals.add(new Entry((int) val, i));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "");

       /* yVals.add(new Entry(30f, 0));
        yVals.add(new Entry(50f, 0));
        yVals.add(new Entry(70f, 0));
        yVals.add(new Entry(90f, 0));
        yVals.add(new Entry(20f, 0));
        yVals.add(new Entry(50f, 0));
        yVals.add(new Entry(70f, 0));*/


        // create a dataset and give it a type
//        LineDataSet set1 = new LineDataSet(yVals,"y Values");

        /*set1.disableDashedLine();
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.GREEN);
        set1.setLineWidth(0f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(14f);
        set1.setValueTextColor(ContextCompat.getColor(mActivity, R.color.white));
        set1.setDrawFilled(true);*/

        set1.setColor(ContextCompat.getColor(mActivity, R.color.white));
        set1.setLineWidth(2.5f);
        set1.setCircleColor(ContextCompat.getColor(mActivity, R.color.transparent));
        set1.setCircleRadius(5f);
//        set1.setFillColor(Color.rgb(240, 238, 70));
        set1.setDrawCubic(false);
        set1.setDrawValues(true);
        set1.setValueTextSize(10f);
        set1.setValueTextColor(ContextCompat.getColor(mActivity, R.color.white));

        set1.setAxisDependency(YAxis.AxisDependency.RIGHT);


        /*if (Utils.getSDKInt() >= 18) {
            // fill drawable only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(mActivity, R.drawable.fade_red);
            set1.setFillDrawable(drawable);
        } else {
            set1.setFillColor(Color.BLACK);
        }*/

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        barChartPastTrip.setData(data);
    }

    private float getRandom(float range, float startsfrom) {
        return (float) (Math.random() * range) + startsfrom;
    }

    private final int itemcount = 12;

    private LineData generateLineData() {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < itemcount; index++)
            entries.add(new Entry(getRandom(30, 150), index));

        LineDataSet set = new LineDataSet(entries, "Line DataSet");
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.rgb(240, 238, 70));
        set.setCircleRadius(5f);
        set.setFillColor(Color.rgb(240, 238, 70));
        set.setDrawCubic(true);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.rgb(240, 238, 70));

        set.setAxisDependency(YAxis.AxisDependency.RIGHT);

        d.addDataSet(set);

        return d;
    }

    private void loadPastTrip() {

        //************************************************ Line Chart ****************************************************************
        BarChart mBarChartPastTrip = (BarChart) links(R.id.barchart_past_trip);

        mBarChartPastTrip.setDescription("");
        mBarChartPastTrip.setVisibleYRangeMaximum(200f, YAxis.AxisDependency.LEFT);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mBarChartPastTrip.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mBarChartPastTrip.setPinchZoom(false);

        mBarChartPastTrip.setDrawBarShadow(false);
        mBarChartPastTrip.getAxisLeft().setEnabled(false);
        mBarChartPastTrip.getAxisRight().setEnabled(false);
        mBarChartPastTrip.setDrawGridBackground(false);

        mBarChartPastTrip.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.dark_gray));
        XAxis xAxisBar = mBarChartPastTrip.getXAxis();
        xAxisBar.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisBar.setSpaceBetweenLabels(0);
        xAxisBar.setDrawGridLines(false);
        xAxisBar.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
        xAxisBar.setLabelsToSkip(4);

        mBarChartPastTrip.getAxisLeft().setDrawGridLines(false);

        // setting data
       /* mSeekBarX.setProgress(10);
        mSeekBarY.setProgress(100);*/
        setBarData(10, 100, mBarChartPastTrip);

        // add a nice and smooth animation
        mBarChartPastTrip.animateY(2500);

        mBarChartPastTrip.getLegend().setEnabled(false);
    }

    private void setBarData(int x, int y, BarChart mBarChart) {

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < x + 1; i++) {
            float mult = (y + 1);
            float val1 = (float) (Math.random() * mult);
            yVals1.add(new BarEntry((int) val1, i));
        }

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < x + 1; i++) {
            xVals.add(Utility.months[i % 12]);

        }

        BarDataSet set1 = new BarDataSet(yVals1, "Data Set");
        List<Integer> list = new ArrayList<>();
        list.add(ContextCompat.getColor(mActivity, R.color.light_gray));
        list.add(ContextCompat.getColor(mActivity, R.color.light_gray));
        list.add(ContextCompat.getColor(mActivity, R.color.light_gray));

        list.add(ContextCompat.getColor(mActivity, R.color.light_gray));
        list.add(ContextCompat.getColor(mActivity, R.color.purple));
        set1.setColors(list);

        set1.setDrawValues(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);


        mBarChart.setData(data);
        mBarChart.invalidate();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == txtViewAllTrips.getId() || v.getId() == btnViewAllTrips.getId()) {
                Intent intent = new Intent(mActivity, ActivityAllTrips.class);
                startActivity(intent);
            } else if (v.getId() == R.id.tv_viewall_badges) {
                Intent intent = new Intent(mActivity, ActivityAllBadge.class);
                startActivity(intent);
            }

        }
    };

    public static class CustomAdapter extends RecyclerView.Adapter {

        private final List<Trip> data;
        private Context context;
        private ArrayList<Boolean> expandState;

        public CustomAdapter(final List<Trip> data) {
            this.data = data;
            expandState = new ArrayList<Boolean>();
            for (int i = 0; i < data.size(); i++) {
                expandState.add(false);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            this.context = parent.getContext();
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.inflate_trip_view_car, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final Trip item = data.get(position);

           /* ((ViewHolder) holder).imgPhoto.setImageResource(R.drawable.img_caronmap_small);
            ((ViewHolder) holder).imgPhoto.setBackgroundResource(R.drawable.circle_black);*/
        }


        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            DriveScoreView driveScoreView;
            MileageView mileageView;
            public ImageView imgPhoto;
            TextView txtName, txtMileage, txtDriveScore, txtDateTime, txtAddress;

            public ViewHolder(View v) {
                super(v);

                driveScoreView = (DriveScoreView) v.findViewById(R.id.drive_score_view);
                mileageView = (MileageView) v.findViewById(R.id.mileage_view);
                imgPhoto = (ImageView) v.findViewById(R.id.img_photo);
                txtName = (TextView) v.findViewById(R.id.txt_name);
                txtMileage = (TextView) v.findViewById(R.id.txt_mileage);
                txtDriveScore = (TextView) v.findViewById(R.id.txt_drive_score);
                txtDateTime = (TextView) v.findViewById(R.id.txt_date_time);
                txtAddress = (TextView) v.findViewById(R.id.txt_address);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rider_dashboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
            if (item.getItemId() == R.id.action_privacy_settings){
                intent = new Intent(mActivity, ActivityPrivacySettings.class);
                startActivity(intent);
                return true;}
            if (item.getItemId() == R.id.action_edit_profile){
                intent = new Intent(mActivity, ActivityRiderProfile.class);
                startActivity(intent);}

            return false;
        }
    }
