package org.andicar2.activity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import andicar.n.persistence.DBReportAdapter;

public class TestActivity extends AppCompatActivity {
    private LineChart mChart;
    private static final int SET_FUEL_EFF_DATA = 1;
    private static final int SET_FUEL_CONS_DATA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        mChart = (LineChart) findViewById(R.id.lineChart);
        mChart.setDrawGridBackground(false);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
//        mChart.setDragEnabled(true);
//        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
//         mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        // mChart.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        ChartMarkerView mv = new ChartMarkerView(this, R.layout.line_chart_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart

        // x-axis limit line
//        LimitLine llXAxis = new LimitLine(10f, "Index 10");
//        llXAxis.setLineWidth(4f);
//        llXAxis.enableDashedLine(10f, 10f, 0f);
//        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setEnabled(false);
//        xAxis.enableGridDashedLine(10f, 10f, 0f);

        //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        //xAxis.addLimitLine(llXAxis); // add x-axis limit line


        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
//        leftAxis.setAxisMaximum(12f);
//        leftAxis.setAxisMinimum(5f);
        leftAxis.setTextSize(15f);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);

        setData(SET_FUEL_CONS_DATA, leftAxis);

//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);

        mChart.animateX(250);
        //mChart.invalidate();

//        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        //hide the legend
        l.setEnabled(false);
//
//        // modify the legend ...
//        l.setForm(Legend.LegendForm.LINE);
    }

    private void setData(int whatData, YAxis leftAxis) {

        float minValue = 1000f;
        float maxValue = 0f;

        DBReportAdapter dbReportAdapter = new DBReportAdapter(getApplicationContext(), null, null);
        Bundle sqlWWhereCondition = new Bundle();

        sqlWWhereCondition.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_REFUEL, DBReportAdapter.COL_NAME_REFUEL__CAR_ID) + "=", Long.toString(3));
        sqlWWhereCondition.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_REFUEL, DBReportAdapter.COL_NAME_REFUEL__ISFULLREFUEL) + "=", "Y");

        dbReportAdapter.setReportSql(DBReportAdapter.REFUEL_LIST_SELECT_NAME, sqlWWhereCondition);
        Cursor mCursor = dbReportAdapter.fetchReport(5);
        if (mCursor == null) {
            try {
                dbReportAdapter.close();
            } catch (Exception ignored) {
            }
            return;
        }

        ArrayList<Entry> values = new ArrayList<>();
        int i = 0;
        //add the values from last to first
        mCursor.moveToLast();
        BigDecimal previousFullRefuelIndex = new BigDecimal(mCursor.getDouble(13));
        BigDecimal distance = (new BigDecimal(mCursor.getString(11))).subtract(previousFullRefuelIndex);
        BigDecimal fuelQty;

        Double t = dbReportAdapter.getFuelQtyForCons(mCursor.getLong(16), previousFullRefuelIndex, mCursor.getDouble(11));
        fuelQty = new BigDecimal(t == null ? 0d : t);
        BigDecimal fuelCons = fuelQty.multiply(new BigDecimal("100")).divide(distance, 10, RoundingMode.HALF_UP);
        BigDecimal fuelEff = distance.divide(fuelQty, 10, RoundingMode.HALF_UP);
        switch (whatData) {
            case SET_FUEL_EFF_DATA:
                values.add(new Entry(i, fuelEff.floatValue()));
                if (fuelEff.floatValue() < minValue)
                    minValue = fuelEff.floatValue();
                if (fuelEff.floatValue() > maxValue)
                    maxValue = fuelEff.floatValue();
                break;
            case SET_FUEL_CONS_DATA:
                values.add(new Entry(i, fuelCons.floatValue()));
                if (fuelCons.floatValue() < minValue)
                    minValue = fuelCons.floatValue();
                if (fuelCons.floatValue() > maxValue)
                    maxValue = fuelCons.floatValue();
        }
        i++;
        while (mCursor.moveToPrevious()) {
            previousFullRefuelIndex = new BigDecimal(mCursor.getDouble(13));
            distance = (new BigDecimal(mCursor.getString(11))).subtract(previousFullRefuelIndex);

            t = dbReportAdapter.getFuelQtyForCons(mCursor.getLong(16), previousFullRefuelIndex, mCursor.getDouble(11));
            fuelQty = new BigDecimal(t == null ? 0d : t);
            fuelCons = fuelQty.multiply(new BigDecimal("100")).divide(distance, 10, RoundingMode.HALF_UP);
            fuelEff = distance.divide(fuelQty, 10, RoundingMode.HALF_UP);
            switch (whatData) {
                case SET_FUEL_EFF_DATA:
                    values.add(new Entry(i, fuelEff.floatValue()));
                    if (fuelEff.floatValue() < minValue)
                        minValue = fuelEff.floatValue();
                    if (fuelEff.floatValue() > maxValue)
                        maxValue = fuelEff.floatValue();
                    break;
                case SET_FUEL_CONS_DATA:
                    values.add(new Entry(i, fuelCons.floatValue()));
                    if (fuelCons.floatValue() < minValue)
                        minValue = fuelCons.floatValue();
                    if (fuelCons.floatValue() > maxValue)
                        maxValue = fuelCons.floatValue();
            }
            i++;
        }

        try {
            mCursor.close();
            dbReportAdapter.close();
        } catch (Exception ignored) {
        }

        leftAxis.setAxisMaximum(Math.round(maxValue) + 1);
        leftAxis.setAxisMinimum(Math.round(minValue) - 1);

        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet 1");

            set1.setDrawIcons(false);

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(15f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.line_chart_fade_red);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart.setData(data);
        }
    }

    class ChartMarkerView extends MarkerView {

        private TextView tvContent;

        public ChartMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);

            tvContent = findViewById(R.id.tvContent);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {

            if (e instanceof CandleEntry) {

                CandleEntry ce = (CandleEntry) e;

                tvContent.setText(Utils.formatNumber(ce.getHigh(), 3, true));
            } else {

                tvContent.setText(Utils.formatNumber(e.getY(), 3, true));
            }

            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }
}
