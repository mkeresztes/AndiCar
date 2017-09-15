package andicar.n.components;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.andicar2.activity.R;

import java.util.ArrayList;
import java.util.Locale;

import andicar.n.activity.dialogs.ChartDetailDialog;
import andicar.n.persistence.DBReportAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;
import andicar.n.view.AndiCarPieChart;

/**
 * Created by Miklos Keresztes on 15.09.2017.
 */

public class ShowChartsComponent extends LinearLayout {
    private static final int CLICK_ACTION_THRESHOLD = 200;
    Context mCtx;
    TextView mChart1Title;
    TextView mChart2Title;
    TextView mChart3Title;
    TextView mChartFooter;
    LinearLayout mChartsLine;
    AndiCarPieChart mChart1;
    AndiCarPieChart mChart2;
    AndiCarPieChart mChart3;
    String mChart1TitleText;
    String mChart2TitleText;
    String mChart3TitleText;
    String mChartFooterText;
    private long mLastTouchDown;

    public ShowChartsComponent(Context context) {
        super(context);
        mCtx = context;
    }

    public ShowChartsComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ShowRecordComponent, 0, 0);

        try {
            mChart1TitleText = a.getString(R.styleable.ShowChartsComponent_chart1TitleText);
            mChart2TitleText = a.getString(R.styleable.ShowChartsComponent_chart2TitleText);
            mChart3TitleText = a.getString(R.styleable.ShowChartsComponent_chart3TitleText);
            mChartFooterText = a.getString(R.styleable.ShowChartsComponent_chartFooterText);
        }
        finally {
            a.recycle();
        }
        init();
    }

    private void init() {
        View rootView = inflate(mCtx, R.layout.component_show_charts, this);

        mChartsLine = rootView.findViewById(R.id.chartsLine);
        mChart1Title = rootView.findViewById(R.id.chart1Title);
        mChart2Title = rootView.findViewById(R.id.chart2Title);
        mChart3Title = rootView.findViewById(R.id.chart3Title);
        mChartFooter = rootView.findViewById(R.id.chartFooter);
        mChart1 = rootView.findViewById(R.id.chart1);
        mChart2 = rootView.findViewById(R.id.chart2);
        mChart3 = rootView.findViewById(R.id.chart3);

        if (mChart1Title != null) {
            mChart1Title.setText(mChart1TitleText);
        }
        if (mChart2Title != null) {
            mChart2Title.setText(mChart2TitleText);
        }
        if (mChart3Title != null) {
            mChart3Title.setText(mChart3TitleText);
        }
        if (mChartFooter != null) {
            mChartFooter.setText(mChartFooterText);
        }
    }

    public void setChart1TitleText(String chart1TitleText) {
        if (this.mChart1Title != null) {
            this.mChart1Title.setText(chart1TitleText);
        }
    }

    public void setChart2TitleText(String chart2TitleText) {
        if (this.mChart2Title != null) {
            this.mChart2Title.setText(chart2TitleText);
        }
    }

    public void setChart3TitleText(String chart3TitleText) {
        if (this.mChart3Title != null) {
            this.mChart3Title.setText(chart3TitleText);
        }
    }

    //    public void setChart1TitleText(int resId) {
//        this.mChart1Title.setText(mCtx.getString(resId));
//    }
//
//    public void setChart2TitleText(int resId) {
//        this.mChart2Title.setText(mCtx.getString(resId));
//    }
//
//    public void setChart3TitleText(int resId) {
//        this.mChart3Title.setText(mCtx.getString(resId));
//    }
//
    public void setChartFooterText(String chartFooterText) {
        if (chartFooterText == null || chartFooterText.trim().length() == 0) {
            this.mChartFooter.setVisibility(GONE);
        }
        else {
            this.mChartFooter.setVisibility(VISIBLE);
            this.mChartFooter.setText(chartFooterText);
        }
    }

    public void setChartsLineHeight(boolean forNoData) {
        Float multiplicand;
        if (forNoData) {
            multiplicand = 0.1f;
        }
        else {
            if (mChartsLine.getTag() != null && mChartsLine.getTag().equals(getResources().getString(R.string.chart_key_legendAtRight))) {
                multiplicand = 0.6f; //legend at right of the chart
            }
            else {
                multiplicand = 1.3f;
            }
        }
        mChartsLine.getLayoutParams().height = Math.round(Utils.getScreenWidthInPixel(mCtx) / mChartsLine.getChildCount() * multiplicand);
        mChartsLine.requestLayout();
    }

    public void drawChart(int chartIndex, ArrayList<DBReportAdapter.chartData> chartData, String title) throws Exception {
        AndiCarPieChart chart;

        switch (chartIndex) {
            case 1:
                chart = mChart1;
                break;
            case 2:
                chart = mChart2;
                break;
            case 3:
                chart = mChart3;
                break;
            default:
                chart = null;
        }

        if (chart == null) {
            return;
        }

        chart.clear();
        if (chartData.size() == 0) {
            return;
        }

        setDefaultPieChartProperties(chart);
        setChartData(chart, title, chartData);
        chart.getDescription().setEnabled(false);

        //simulate the onClick action (becouse PieChart does not receive this action).
        //the implemntation based on https://stackoverflow.com/questions/17831395/how-can-i-detect-a-click-in-an-ontouch-listener
        chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mLastTouchDown = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - mLastTouchDown < CLICK_ACTION_THRESHOLD) {
                            AndiCarPieChart apc = (AndiCarPieChart) view;
                            if (apc.getChartDataEntries() == null || apc.getChartDataEntries().size() == 0) {
                                Toast.makeText(mCtx, mCtx.getString(R.string.chart_no_data), Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            else {
                                Intent i = new Intent(mCtx, ChartDetailDialog.class);
                                i.putExtra(ChartDetailDialog.CHART_DATA_EXTRAS_KEY, apc.getChartDataEntries());
                                i.putExtra(ChartDetailDialog.CHART_TITLE_KEY, apc.getTitle());
                                mCtx.startActivity(i);
                                return true;
                            }
                        }
                        break;
                }
                return true;

            }
        });
    }

    private void setDefaultPieChartProperties(AndiCarPieChart pieChart) {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setRotationAngle(0);
        //disable rotation of the chart by touch
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);

        pieChart.animateY(700, Easing.EasingOption.EaseInOutQuad);
        // entry label styling
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setTouchEnabled(true);

        Legend l = pieChart.getLegend();
        if (mChartsLine.getTag() != null && mChartsLine.getTag().equals(getResources().getString(R.string.chart_key_legendAtRight))) {
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        }
        else {
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        }
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(10f);
    }

    private void setChartData(AndiCarPieChart pieChart, String title, ArrayList<DBReportAdapter.chartData> chartDataEntries) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        int i;
        int mChartTop = 3;

        pieChart.setChartData(chartDataEntries);
        pieChart.setTitle(title);

        for (i = 0; i < (chartDataEntries.size() <= mChartTop ? chartDataEntries.size() : mChartTop); i++) {
            entries.add(new PieEntry(chartDataEntries.get(i).value, chartDataEntries.get(i).label + " (" +
                    String.format(Locale.getDefault(), "%.2f", chartDataEntries.get(i).value2) + "%)"));
        }

        float otherValues = 0;
        float otherValues2 = 0;
        int j;
        for (j = i; j < chartDataEntries.size(); j++) {
            otherValues = otherValues + chartDataEntries.get(j).value;
            otherValues2 = otherValues2 + chartDataEntries.get(j).value2;
        }
        if (j > i) {
            entries.add(new PieEntry(otherValues, String.format(mCtx.getString(R.string.chart_label_others), otherValues2, "%")));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
//        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add the colors
        dataSet.setColors(ConstantValues.CHART_COLORS);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
//        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);

        data.setValueTextColor(Color.WHITE);
//        data.setValueTypeface(mTfLight);
        pieChart.setData(data);

        // undo all highlights
//        pieChart.highlightValues(null);

//        pieChart.setBackgroundColor(ConstantValues.CHART_COLORS.get(ConstantValues.CHART_COLORS.size()-1));

        pieChart.notifyDataSetChanged(); // let the chart know it's data changed
        pieChart.invalidate();
    }
}
