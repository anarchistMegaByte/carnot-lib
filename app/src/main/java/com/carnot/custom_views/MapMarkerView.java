
package com.carnot.custom_views;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.global.Utility;
import com.carnot.models.GraphType;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.Utils;

import java.text.DecimalFormat;


/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
public class MapMarkerView extends MarkerView {

    private TextView tvContent, tvLabel;

    public MapMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        tvContent = (TextView) findViewById(R.id.tvContent);
        tvLabel = (TextView) findViewById(R.id.tvLabel);

    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        if (e instanceof CandleEntry) {
            CandleEntry ce = (CandleEntry) e;
            tvContent.setText("" + Utils.formatNumber(ce.getHigh(), 0, true));
        } else {

//            tvContent.setText("" + Utils.formatNumber(e.getVal(), 0, true));
            try {
                tvContent.setText("" + Utility.round(e.getVal(), 1));
            } catch (Exception exce) {
                tvContent.setText("" + Utils.formatNumber(e.getVal(), 0, true));
            }

            if (e.getData() != null && e.getData() instanceof GraphType) {
                GraphType graphType = (GraphType) e.getData();
                if (graphType.type == GraphType.MILEAGE) {
                    tvContent.setText("" + Utility.round(e.getVal(), 1));
                } else if (graphType.type == GraphType.COST) {
                    tvContent.setText("" + new DecimalFormat("##,##,##0").format(Utility.roundToInt(e.getVal())));
                    tvContent.setTextSize(15.0f);
                } else if (graphType.type == GraphType.DISTANCE) {
                    tvContent.setText("" + Utility.round(e.getVal(), 1));
                } else if (graphType.type == GraphType.DURATION) {
                    tvContent.setText("" + String.valueOf(e.getVal()).replace(".",":"));
                    tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
                    //tvLabel.setText("Hrs");
                }
                tvLabel.setText(((GraphType) e.getData()).message);
            } else {
                tvLabel.setText((String) e.getData());
            }

        }
     }

    public String formattingForDurationBarChart(double val)
    {
        int hrs = (int) val;
        int min = (int) ((val - (int)val)*100);
        return String.valueOf(hrs) + ":" + String.valueOf(min) ;
    }


    @Override
    public int getXOffset(float xpos) {
        // this will center the marker-view horizontally
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset(float ypos) {
        // this will cause the marker-view to be above the selected value
        return -getHeight();
    }
}
