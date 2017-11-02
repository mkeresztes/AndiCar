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
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import andicar.n.persistence.DBAdapter;
import andicar.n.persistence.DBReportAdapter;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 22.09.2017.
 */

@SuppressWarnings("unused")
public class LineChartComponent extends LinearLayout {
    public static final int SHOW_FUEL_EFF = 1;
    public static final int SHOW_FUEL_CONS = 2;
    public static final int SHOW_FUEL_PRICE_EVOLUTION = 3;
    private static final float LEFT_AXIS_TEXT_SIZE = 10f;
    private static final float VALUE_FONT_SIZE = 12f;

    private Context mCtx;
    private View mChartHeader;
    private TextView mChartTitle;
    private TextView mInfo;
    private LineChart mChart;
    private PopupMenu mChartFilterMenu;
    private YAxis leftAxis;
    private XAxis xAxis;

    private SharedPreferences mPreferences = AndiCar.getDefaultSharedPreferences();
    private String mChartTitleText;
    private int mChartFilterNoRecords = 5;
    private int mWhatData;
    private long mLastSelectedCarID;
    private ArrayList<Long> mChartDates = new ArrayList<>();
    private long mSelectedFuelTypeID;
    private String mSelectedFuelTypeName;

    public LineChartComponent(Context context, int what, @Nullable String title) {
        super(context);
        mCtx = context;
        mWhatData = what;
        if (mWhatData == SHOW_FUEL_PRICE_EVOLUTION) {
            mSelectedFuelTypeID = mPreferences.getLong(mCtx.getString(R.string.pref_key_selected_fuel_type_id_for_fuel_prices), -1L);
            mSelectedFuelTypeName = mPreferences.getString(mCtx.getString(R.string.pref_key_selected_fuel_type_name_for_fuel_prices), "");
        }
        mChartTitleText = title;
        init();
    }

    public LineChartComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LineChartComponent, 0, 0);

        try {
            mChartTitleText = a.getString(R.styleable.LineChartComponent_chartTitleText);
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
            if (Utils.getScreenWidthInDP(mCtx) < 900) {
                if (mWhatData == SHOW_FUEL_PRICE_EVOLUTION) {
                    divider = 3f;
                }
                else {
                    divider = 4f;
                }
            }
            else {
                if (mWhatData == SHOW_FUEL_PRICE_EVOLUTION) {
                    divider = 4f;
                }
                else {
                    divider = 6f;
                }
            }
        }

        mChart.getLayoutParams().height = Math.round(Utils.getScreenWidthInPixel(mCtx) / divider);
        mChart.requestLayout();
    }


    private void init() {
        View rootView = inflate(mCtx, R.layout.component_line_chart, this);

        switch (mWhatData) {
            case SHOW_FUEL_EFF:
                mChartFilterNoRecords = mPreferences.getInt(mCtx.getString(R.string.pref_key_no_of_records_for_fuel_eff), 5);
                break;
            case SHOW_FUEL_CONS:
                mChartFilterNoRecords = mPreferences.getInt(mCtx.getString(R.string.pref_key_no_of_records_for_fuel_cons), 5);
                break;
            case SHOW_FUEL_PRICE_EVOLUTION:
                mChartFilterNoRecords = mPreferences.getInt(mCtx.getString(R.string.pref_key_no_of_records_for_fuel_prices), 5);
                break;
            default:
                mChartFilterNoRecords = 5;
        }

        mLastSelectedCarID = mPreferences.getLong(mCtx.getString(R.string.pref_key_last_selected_car_id), -1);

        ImageButton menuButton = rootView.findViewById(R.id.btnMenu);
        if (menuButton != null) {
            mChartFilterMenu = new PopupMenu(mCtx, menuButton);
            mChartFilterMenu.getMenuInflater().inflate(R.menu.menu_list_chart_data_selection, mChartFilterMenu.getMenu());
            if (mWhatData == SHOW_FUEL_PRICE_EVOLUTION) {
                try {
                    DBAdapter dbAdapter = new DBAdapter(mCtx);
                    ArrayList<Pair<Long, String>> fuelTypes = dbAdapter.getFuelTypesForCar(mLastSelectedCarID);
                    dbAdapter.close();
                    if (fuelTypes != null) {
                        SubMenu mnuFuelTypes = mChartFilterMenu.getMenu().addSubMenu(R.string.pref_fuel_type_title);
                        boolean savedFuelTypeExists = false;
                        for (Pair<Long, String> fuelType : fuelTypes) {
                            //noinspection ConstantConditions
                            if (fuelType.first == mSelectedFuelTypeID)
                                savedFuelTypeExists = true;
                            //noinspection ConstantConditions
                            mnuFuelTypes.add(-10, -1 * fuelType.first.intValue(), Menu.NONE, fuelType.second);
                        }
                        if (!savedFuelTypeExists) {
                            //noinspection ConstantConditions
                            mSelectedFuelTypeID = fuelTypes.get(0).first;
                            mSelectedFuelTypeName = fuelTypes.get(0).second;
                        }
                    }
                } catch (Exception ignored) {
                }
            }

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
                    } else if (menuItem.getItemId() == R.id.chart_filter_last_10) {
                        mChartFilterNoRecords = 10;
                    } else if (menuItem.getItemId() == R.id.chart_filter_all) {
                        mChartFilterNoRecords = -1;
                    } else if (menuItem.getItemId() < 0) {
                        mSelectedFuelTypeID = -1 * menuItem.getItemId();
                        mSelectedFuelTypeName = menuItem.getTitle().toString();
                        mChartTitle.setText(String.format(mCtx.getString(R.string.line_chart_fuel_price_title), mSelectedFuelTypeName));
                    } else
                        return false;

                    switch (mWhatData) {
                        case SHOW_FUEL_EFF:
                            e.putInt(mCtx.getString(R.string.pref_key_no_of_records_for_fuel_eff), mChartFilterNoRecords);
                            break;
                        case SHOW_FUEL_CONS:
                            e.putInt(mCtx.getString(R.string.pref_key_no_of_records_for_fuel_cons), mChartFilterNoRecords);
                            break;
                        case SHOW_FUEL_PRICE_EVOLUTION:
                            e.putInt(mCtx.getString(R.string.pref_key_no_of_records_for_fuel_prices), mChartFilterNoRecords);
                            e.putLong(mCtx.getString(R.string.pref_key_selected_fuel_type_id_for_fuel_prices), mSelectedFuelTypeID);
                            e.putString(mCtx.getString(R.string.pref_key_selected_fuel_type_name_for_fuel_prices), mSelectedFuelTypeName);
                    }

                    e.apply();
                    setData(mWhatData);
                    return true;
                }
            });
        }

        mChartHeader = rootView.findViewById(R.id.chartHeader);
        mInfo = rootView.findViewById(R.id.tvInfo);
        mInfo.setVisibility(GONE);

        mChartTitle = rootView.findViewById(R.id.chartTitle);
        mChart = rootView.findViewById(R.id.lineChart);
        if (mChartTitleText != null) {
            mChartTitle.setText(mChartTitleText);
        } else {
            if (mWhatData == SHOW_FUEL_PRICE_EVOLUTION) {
                mChartTitle.setText(String.format(mCtx.getString(R.string.line_chart_fuel_price_title), mSelectedFuelTypeName));
            }
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

        xAxis = mChart.getXAxis();
        if (mWhatData == SHOW_FUEL_CONS || mWhatData == SHOW_FUEL_EFF)
            xAxis.setEnabled(false);
        else {
            xAxis.setEnabled(true);
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(new DateAxisFormatter());
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelRotationAngle(-15);
        }

        leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.setTextSize(LEFT_AXIS_TEXT_SIZE);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(true);
        mChart.getAxisRight().setEnabled(false);

        setData(mWhatData);
    }

    private void setData(int whatData) {
        float minValue = 1000f;
        float maxValue = 0f;

        mChartDates.clear();

        DBReportAdapter dbReportAdapter = new DBReportAdapter(mCtx, null, null);
        Bundle sqlWWhereCondition = new Bundle();

        sqlWWhereCondition.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_REFUEL, DBReportAdapter.COL_NAME_REFUEL__CAR_ID) + "=", Long.toString(mLastSelectedCarID));
        if (whatData != SHOW_FUEL_PRICE_EVOLUTION) {
            sqlWWhereCondition.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_REFUEL, DBReportAdapter.COL_NAME_REFUEL__ISFULLREFUEL) + "=", "Y");
        } else
            sqlWWhereCondition.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_REFUEL, DBReportAdapter.COL_NAME_REFUEL__EXPENSECATEGORY_ID) + "=",
                    Long.toString(mSelectedFuelTypeID));

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
        BigDecimal yValue;
        Long xValue;
        Double tmpFuelQty;

        if (mCursor.moveToLast()) {
            setChartsLineHeight(false);
            if (whatData == SHOW_FUEL_CONS || whatData == SHOW_FUEL_EFF) {
                previousFullRefuelIndex = new BigDecimal(mCursor.getDouble(13));
                if (previousFullRefuelIndex.compareTo(BigDecimal.ZERO) >= 0) {
                    distance = (new BigDecimal(mCursor.getString(11))).subtract(previousFullRefuelIndex);

                    tmpFuelQty = dbReportAdapter.getFuelQtyForCons(mCursor.getLong(16), previousFullRefuelIndex, mCursor.getDouble(11));
                    fuelQty = new BigDecimal(tmpFuelQty == null ? 0d : tmpFuelQty);
                    //fuel cons
                    yValue = fuelQty.multiply(new BigDecimal("100")).divide(distance, 10, RoundingMode.HALF_UP);
                    if (yValue.compareTo(new BigDecimal(0.5)) > 0) { //if < 0.5 => consider invalid fill-up, eliminate from graph
                        switch (whatData) {
                            case SHOW_FUEL_EFF:
                                //fuel eff
                                yValue = distance.divide(fuelQty, 10, RoundingMode.HALF_UP);
                                values.add(new Entry(i, yValue.floatValue()));
                                break;
                            case SHOW_FUEL_CONS:
                                values.add(new Entry(i, yValue.floatValue()));
                        }
                        if (yValue.floatValue() < minValue) {
                            minValue = yValue.floatValue();
                        }
                        if (yValue.floatValue() > maxValue) {
                            maxValue = yValue.floatValue();
                        }
                        i++;
                    }
                }
            } else if (whatData == SHOW_FUEL_PRICE_EVOLUTION) {
                yValue = new BigDecimal(mCursor.getDouble(8)); //price
                mChartDates.add(mCursor.getLong(4)); //date
                values.add(new Entry(i, yValue.floatValue()));

                if (yValue.floatValue() < minValue) {
                    minValue = yValue.floatValue();
                }
                if (yValue.floatValue() > maxValue) {
                    maxValue = yValue.floatValue();
                }
                i++;
            }
        }
        else {
            //no data
            setChartsLineHeight(true);
            if (whatData == SHOW_FUEL_CONS || whatData == SHOW_FUEL_EFF) {
                mInfo.setText(R.string.line_chart_info_1);
            }

            mChartHeader.setVisibility(GONE);
            try {
                mCursor.close();
                dbReportAdapter.close();
            } catch (Exception ignored) {
            }
            return;
        }

        while (mCursor.moveToPrevious()) {
            if (whatData == SHOW_FUEL_CONS || whatData == SHOW_FUEL_EFF) {
                previousFullRefuelIndex = new BigDecimal(mCursor.getDouble(13));
                distance = (new BigDecimal(mCursor.getString(11))).subtract(previousFullRefuelIndex);

                tmpFuelQty = dbReportAdapter.getFuelQtyForCons(mCursor.getLong(16), previousFullRefuelIndex, mCursor.getDouble(11));
                fuelQty = new BigDecimal(tmpFuelQty == null ? 0d : tmpFuelQty);
                //fuel cons
                yValue = fuelQty.multiply(new BigDecimal("100")).divide(distance, 10, RoundingMode.HALF_UP);
                if (yValue.compareTo(new BigDecimal(0.5)) < 0) //consider invalid fill-up. eliminate from graph
                    continue;

                switch (whatData) {
                    case SHOW_FUEL_EFF:
                        yValue = distance.divide(fuelQty, 10, RoundingMode.HALF_UP);
                        values.add(new Entry(i, yValue.floatValue()));
                        if (yValue.floatValue() < minValue) {
                            minValue = yValue.floatValue();
                        }
                        if (yValue.floatValue() > maxValue) {
                            maxValue = yValue.floatValue();
                        }
                        break;
                    case SHOW_FUEL_CONS:
                        values.add(new Entry(i, yValue.floatValue()));
                        if (yValue.floatValue() < minValue) {
                            minValue = yValue.floatValue();
                        }
                        if (yValue.floatValue() > maxValue) {
                            maxValue = yValue.floatValue();
                        }
                }
                i++;
            } else if (whatData == SHOW_FUEL_PRICE_EVOLUTION) {
                yValue = new BigDecimal(mCursor.getDouble(8)); //price
                mChartDates.add(mCursor.getLong(4)); //date
                values.add(new Entry(i, yValue.floatValue()));

                if (yValue.floatValue() < minValue) {
                    minValue = yValue.floatValue();
                }
                if (yValue.floatValue() > maxValue) {
                    maxValue = yValue.floatValue();
                }
                i++;
            }
        }

        try {
            mCursor.close();
            dbReportAdapter.close();
        }
        catch (Exception ignored) {
        }

        if (whatData == SHOW_FUEL_CONS || whatData == SHOW_FUEL_EFF) {
            if (values.size() == 0) {
                mInfo.setVisibility(VISIBLE);
                mInfo.setText(R.string.line_chart_info_2);
                setChartsLineHeight(true);
                return;
            } else if (values.size() == 1) {
                mInfo.setVisibility(VISIBLE);
                mInfo.setText(R.string.line_chart_info_3);
            }
        } else {
            mInfo.setVisibility(GONE);
        }

        maxValue = Math.round(maxValue + 0.5); //round up
        minValue = Math.round(minValue - 0.5); //round down

        leftAxis.setAxisMaximum(maxValue + (maxValue * 0.1f)); //add 10% boundary
        leftAxis.setAxisMinimum(minValue - (minValue * 0.1f)); //add 10% boundary

        if (whatData == SHOW_FUEL_PRICE_EVOLUTION) {
            xAxis.setLabelCount(mChartDates.size(), true);
        }

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

        // callbacks every time the MarkerView is redrawn, can be used to update the
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

    private class DateAxisFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            try {
                return Utils.getFormattedDateTime((mChartDates.get((int) value)) * 1000, true);
            } catch (Exception ignored) {
                return "";
            }

        }
    }
}
