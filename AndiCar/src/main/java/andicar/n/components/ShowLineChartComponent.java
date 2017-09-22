package andicar.n.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import andicar.n.persistence.DBReportAdapter;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 22.09.2017.
 */

@SuppressWarnings("unused")
public class ShowLineChartComponent extends LinearLayout {
    public static final int SHOW_FUEL_EFF = 1;
    public static final int SHOW_FUEL_CONS = 2;
    private static final float LEFT_AXIS_TEXT_SIZE = 10f;
    private static final float VALUE_FONT_SIZE = 12f;

    private Context mCtx;
    private View mChartHeader;
    private TextView mChartTitle;
    private LineChart mChart;
    private PopupMenu mChartFilterMenu;
    private YAxis leftAxis;


    private SharedPreferences mPreferences = AndiCar.getDefaultSharedPreferences();
    private String mChartTitleText;
    private int mChartFilterNoRecords = 5;
    private int mWhatData;
    private long mLastSelectedCarID;

    public ShowLineChartComponent(Context context, int what, @Nullable String title) {
        super(context);
        mCtx = context;
        mWhatData = what;
        mChartTitleText = title;
        init();
    }

    public ShowLineChartComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ShowLineChartComponent, 0, 0);

        try {
            mChartTitleText = a.getString(R.styleable.ShowLineChartComponent_chartTitleText);
        }
        finally {
            a.recycle();
        }
        init();
    }

    public void setChartTitleText(String chartTitleText) {
        if (this.mChartTitle != null) {
            this.mChartTitle.setText(chartTitleText);
        }
    }

    private void setChartsLineHeight(boolean forNoData) {
        Float divider;
        if (forNoData) {
            divider = 10f;
        }
        else {
            divider = 4f;
        }
        mChart.getLayoutParams().height = Math.round(Utils.getScreenWidthInPixel(mCtx) / divider);
        mChart.requestLayout();
    }


    private void init() {
        if (mWhatData == SHOW_FUEL_EFF)
            mChartFilterNoRecords = mPreferences.getInt(mCtx.getString(R.string.line_chart_filter_no_of_records_key_1), 5);
        else
            mChartFilterNoRecords = mPreferences.getInt(mCtx.getString(R.string.line_chart_filter_no_of_records_key_2), 5);

        mLastSelectedCarID = mPreferences.getLong(mCtx.getString(R.string.pref_key_last_selected_car_id), -1);

        View rootView = inflate(mCtx, R.layout.component_line_chart, this);

        mChartHeader = rootView.findViewById(R.id.chartHeader);

        mChartTitle = rootView.findViewById(R.id.chartTitle);
        mChart = rootView.findViewById(R.id.lineChart);
        if (mChartTitle != null) {
            mChartTitle.setText(mChartTitleText);
        }

        mChart.setDrawGridBackground(false);
        // no description text
        mChart.getDescription().setEnabled(false);
        // enable touch gestures
        mChart.setTouchEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        ChartMarkerView mv = new ChartMarkerView(mCtx, R.layout.line_chart_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart

        XAxis xAxis = mChart.getXAxis();
        xAxis.setEnabled(false);

        leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.setTextSize(LEFT_AXIS_TEXT_SIZE);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(true);
        mChart.getAxisRight().setEnabled(false);


        ImageButton menuButton = rootView.findViewById(R.id.btnMenu);
        if (menuButton != null) {
            mChartFilterMenu = new PopupMenu(mCtx, menuButton);
            mChartFilterMenu.getMenuInflater().inflate(R.menu.menu_list_chart_data_selection, mChartFilterMenu.getMenu());

            menuButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mChartFilterMenu.show();
                }
            });

            mChartFilterMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    SharedPreferences.Editor e = mPreferences.edit();
                    if (menuItem.getItemId() == R.id.chart_filter_last_5) {
                        mChartFilterNoRecords = 5;
                        if (mWhatData == SHOW_FUEL_EFF)
                            e.putInt(mCtx.getString(R.string.line_chart_filter_no_of_records_key_1), 5);
                        else
                            e.putInt(mCtx.getString(R.string.line_chart_filter_no_of_records_key_2), 5);
                    }
                    else if (menuItem.getItemId() == R.id.chart_filter_last_10) {
                        mChartFilterNoRecords = 10;
                        if (mWhatData == SHOW_FUEL_EFF)
                            e.putInt(mCtx.getString(R.string.line_chart_filter_no_of_records_key_1), 10);
                        else
                            e.putInt(mCtx.getString(R.string.line_chart_filter_no_of_records_key_2), 10);
                    }
                    else {
                        mChartFilterNoRecords = -1;
                        if (mWhatData == SHOW_FUEL_EFF)
                            e.putInt(mCtx.getString(R.string.line_chart_filter_no_of_records_key_1), -1);
                        else
                            e.putInt(mCtx.getString(R.string.line_chart_filter_no_of_records_key_2), -1);
                    }
                    e.apply();
                    setData(mWhatData);
                    return true;
                }
            });
        }

        setData(mWhatData);
    }

    private void setData(int whatData) {
        float minValue = 1000f;
        float maxValue = 0f;

        DBReportAdapter dbReportAdapter = new DBReportAdapter(mCtx, null, null);
        Bundle sqlWWhereCondition = new Bundle();

        sqlWWhereCondition.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_REFUEL, DBReportAdapter.COL_NAME_REFUEL__CAR_ID) + "=", Long.toString(mLastSelectedCarID));
        sqlWWhereCondition.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_REFUEL, DBReportAdapter.COL_NAME_REFUEL__ISFULLREFUEL) + "=", "Y");

        dbReportAdapter.setReportSql(DBReportAdapter.REFUEL_LIST_SELECT_NAME, sqlWWhereCondition);
        Cursor mCursor = dbReportAdapter.fetchReport(mChartFilterNoRecords);
        if (mCursor == null) {
            try {
                dbReportAdapter.close();
            }
            catch (Exception ignored) {
            }
            return;
        }

        ArrayList<Entry> values = new ArrayList<>();
        int i = 0;
        //add the values from last to first
        BigDecimal previousFullRefuelIndex;
        BigDecimal distance;
        BigDecimal fuelQty;
        BigDecimal fuelCons;
        BigDecimal fuelEff;
        Double tmpFuelQty;


        if (mCursor.moveToLast()) {
            setChartsLineHeight(false);
            previousFullRefuelIndex = new BigDecimal(mCursor.getDouble(13));
            if (previousFullRefuelIndex.compareTo(BigDecimal.ZERO) >= 0) {
                distance = (new BigDecimal(mCursor.getString(11))).subtract(previousFullRefuelIndex);

                tmpFuelQty = dbReportAdapter.getFuelQtyForCons(mCursor.getLong(16), previousFullRefuelIndex, mCursor.getDouble(11));
                fuelQty = new BigDecimal(tmpFuelQty == null ? 0d : tmpFuelQty);
                fuelCons = fuelQty.multiply(new BigDecimal("100")).divide(distance, 10, RoundingMode.HALF_UP);
                fuelEff = distance.divide(fuelQty, 10, RoundingMode.HALF_UP);

                switch (whatData) {
                    case SHOW_FUEL_EFF:
                        values.add(new Entry(i, fuelEff.floatValue()));
                        if (fuelEff.floatValue() < minValue) {
                            minValue = fuelEff.floatValue();
                        }
                        if (fuelEff.floatValue() > maxValue) {
                            maxValue = fuelEff.floatValue();
                        }
                        break;
                    case SHOW_FUEL_CONS:
                        values.add(new Entry(i, fuelCons.floatValue()));
                        if (fuelCons.floatValue() < minValue) {
                            minValue = fuelCons.floatValue();
                        }
                        if (fuelCons.floatValue() > maxValue) {
                            maxValue = fuelCons.floatValue();
                        }
                }
                i++;
            }
        }
        else {
            //no data
            setChartsLineHeight(true);
            mChartHeader.setVisibility(GONE);
            try {
                mCursor.close();
                dbReportAdapter.close();
            } catch (Exception ignored) {
            }
            return;
        }

        while (mCursor.moveToPrevious()) {
            previousFullRefuelIndex = new BigDecimal(mCursor.getDouble(13));
            distance = (new BigDecimal(mCursor.getString(11))).subtract(previousFullRefuelIndex);

            tmpFuelQty = dbReportAdapter.getFuelQtyForCons(mCursor.getLong(16), previousFullRefuelIndex, mCursor.getDouble(11));
            fuelQty = new BigDecimal(tmpFuelQty == null ? 0d : tmpFuelQty);
            fuelCons = fuelQty.multiply(new BigDecimal("100")).divide(distance, 10, RoundingMode.HALF_UP);
            fuelEff = distance.divide(fuelQty, 10, RoundingMode.HALF_UP);
            switch (whatData) {
                case SHOW_FUEL_EFF:
                    values.add(new Entry(i, fuelEff.floatValue()));
                    if (fuelEff.floatValue() < minValue) {
                        minValue = fuelEff.floatValue();
                    }
                    if (fuelEff.floatValue() > maxValue) {
                        maxValue = fuelEff.floatValue();
                    }
                    break;
                case SHOW_FUEL_CONS:
                    values.add(new Entry(i, fuelCons.floatValue()));
                    if (fuelCons.floatValue() < minValue) {
                        minValue = fuelCons.floatValue();
                    }
                    if (fuelCons.floatValue() > maxValue) {
                        maxValue = fuelCons.floatValue();
                    }
            }
            i++;
        }

        try {
            mCursor.close();
            dbReportAdapter.close();
        }
        catch (Exception ignored) {
        }

        leftAxis.setAxisMaximum(Math.round(maxValue) + 1);
        leftAxis.setAxisMinimum(Math.round(minValue) - 1);

        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            mChart.clear();
        }

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
        set1.setValueTextSize(VALUE_FONT_SIZE);
        set1.setDrawFilled(true);
        set1.setFormLineWidth(1f);
        set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        set1.setFormSize(15.f);

        if (com.github.mikephil.charting.utils.Utils.getSDKInt() >= 18) {
            // fill drawable only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(mCtx, R.drawable.line_chart_fade_red);
            set1.setFillDrawable(drawable);
        }
        else {
            set1.setFillColor(Color.RED);
        }

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(dataSets);

        // set data
        mChart.setData(data);
        mChart.animateX(250);
        Legend l = mChart.getLegend();
        //hide the legend
        l.setEnabled(false);
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

                tvContent.setText(com.github.mikephil.charting.utils.Utils.formatNumber(ce.getHigh(), 3, true));
            }
            else {

                tvContent.setText(com.github.mikephil.charting.utils.Utils.formatNumber(e.getY(), 3, true));
            }

            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }

}
