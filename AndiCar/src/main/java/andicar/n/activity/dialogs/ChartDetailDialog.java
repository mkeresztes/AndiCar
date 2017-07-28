/*
 * AndiCar
 *
 *  Copyright (c) 2017 Miklos Keresztes (miklos.keresztes@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package andicar.n.activity.dialogs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import org.andicar2.activity.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import andicar.n.persistence.DBReportAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 25.05.2017.
 */

public class ChartDetailDialog extends AppCompatActivity {

    public final static String CHART_DATA_EXTRAS_KEY = "ChartData";
    public final static String CHART_TITLE_KEY = "ChartTitle";

    private HorizontalBarChart mChart = null;
    private Legend mChartLegend = null;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            float chartBarHeight = 50 * Utils.getScreenDensity(this);
            float chartLegendHeight = 70 * Utils.getScreenDensity(this);

            super.onCreate(savedInstanceState);

            ArrayList<DBReportAdapter.chartData> cdList = null;
            //prevent keyboard from automatic pop up
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                cdList = (ArrayList<DBReportAdapter.chartData>) extras.getSerializable(CHART_DATA_EXTRAS_KEY);
                setTitle(extras.getString(CHART_TITLE_KEY));
            }


            setFinishOnTouchOutside(true);

            setContentView(R.layout.dialog_chart_details);

            Button btnClose = (Button) findViewById(R.id.btnClose);
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //close the dialog
                    finish();
                }
            });

            mChart = (HorizontalBarChart) findViewById(R.id.horizontalBarChart);
            if (mChart == null) {
                return;
            }

            LinearLayout llChartContainer = (LinearLayout) findViewById(R.id.llChartContainer);

            if (cdList != null) {
                llChartContainer.getLayoutParams().height = Math.round((cdList.size() * chartBarHeight) + chartLegendHeight);
                llChartContainer.requestLayout();
            }

            mChart.setDrawBarShadow(false);
            mChart.setDrawGridBackground(false);
            mChart.setDrawValueAboveBar(true);
            mChart.getDescription().setEnabled(false);
            mChart.setHighlightPerTapEnabled(false);
            // if more than 60 entries are displayed in the chart, no values will be
            // drawn
            mChart.setMaxVisibleValueCount(60);
            // scaling can now only be done on x- and y-axis separately
            mChart.setPinchZoom(false);
            // draw shadows for each bar that show the maximum value
            // mChart.setDrawBarShadow(true);
            mChart.setDrawGridBackground(false);

            XAxis xl = mChart.getXAxis();
            xl.setPosition(XAxis.XAxisPosition.BOTTOM);
            xl.setDrawAxisLine(true);
            xl.setDrawGridLines(false);
            xl.setDrawLabels(false);
            xl.setGranularity(10f);

            YAxis yl = mChart.getAxisLeft();
            yl.setDrawAxisLine(true);
            yl.setDrawGridLines(false);
            yl.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        yl.setInverted(true);

            YAxis yr = mChart.getAxisRight();
            yr.setDrawLabels(false);

            mChartLegend = mChart.getLegend();
            mChartLegend.setWordWrapEnabled(true);
            mChartLegend.setYOffset(15f); //the Y space bellow the legends
            mChartLegend.setTextSize(14f);
//        mChartLegend.setYEntrySpace(10f); //the Y space between the labels
            setData(cdList);

            mChart.setFitBars(true);
//        mChart.animateY(200);
        }
        catch (Exception e) {
            Utils.showReportableErrorDialog(this, null, e.getMessage(), e, false);
        }
    }

    //    private void setData(int count, float range) {
    private void setData(ArrayList<DBReportAdapter.chartData> cdList) throws Exception {
        if (cdList == null || cdList.size() == 0) {
            mChart.clear();
            return;
        }

        float barWidth = 4f;
        float spaceForBar = 5f;
        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        BarDataSet set1;

        List<LegendEntry> legendEntries = new ArrayList<>();
        List<LegendEntry> tmpLegendEntries = new ArrayList<>();

        for (int i = 0; i < cdList.size(); i++) {
            DBReportAdapter.chartData cd = cdList.get(i);
            yVals1.add(new BarEntry(i * spaceForBar, cd.value));

            LegendEntry entry = new LegendEntry();
            entry.formColor = ConstantValues.CHART_COLORS.get(i % ConstantValues.CHART_COLORS.size());
            entry.label = cd.label + " (" + String.format(Locale.getDefault(), "%.2f", cd.value2) + "%)";
            entry.formSize = (mChartLegend.getTextSize() / Utils.getScreenDensity(this)) * 0.9f;
            tmpLegendEntries.add(entry);
        }

        //invert the label order
        for (int i = tmpLegendEntries.size(); i > 0; i--) {
            legendEntries.add(tmpLegendEntries.get(i - 1));
        }
        mChartLegend.setCustom(legendEntries);

        set1 = new BarDataSet(yVals1, "");

        set1.setDrawIcons(false);
        set1.setColors(ConstantValues.CHART_COLORS);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setBarWidth(barWidth);
        data.setValueTextSize(12f);
        mChart.setData(data);
    }
}
