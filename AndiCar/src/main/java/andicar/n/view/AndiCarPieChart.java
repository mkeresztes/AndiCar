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

package andicar.n.view;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.PieChart;

import java.util.ArrayList;

import andicar.n.persistence.DBReportAdapter;

/**
 * Created by Miklos Keresztes on 25.05.2017.
 */

public class AndiCarPieChart extends PieChart {

    private ArrayList<DBReportAdapter.chartData> mChartDataList = null;
    private String mTitle = null;

    public AndiCarPieChart(Context context) {
        super(context);
    }

    public AndiCarPieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AndiCarPieChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void clear() {
        super.clear();
        mChartDataList = null;
        mTitle = null;
    }

    /**
     * Keeps the whole data entries for showing it on the chart dialog showed when a chart is touched
     *
     * @param chartDataEntries the whole data entries
     */
    public void setChartData(ArrayList<DBReportAdapter.chartData> chartDataEntries) {
        mChartDataList = chartDataEntries;
    }

    public ArrayList<DBReportAdapter.chartData> getChartDataEntries() {
        return mChartDataList;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }
}
