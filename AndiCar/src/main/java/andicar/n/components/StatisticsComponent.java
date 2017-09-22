package andicar.n.components;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.andicar2.activity.R;

/**
 * Created by miki on 22.09.2017.
 */

@SuppressWarnings("unused")
public class StatisticsComponent extends LinearLayout {
    private Context mCtx;

    private TextView mHeader;
    private TextView mLastKnownOdometer;
    private TextView mAvgFuelEff;
    private TextView mLastFuelEff;
    private TextView mTotalExpenses;
    private TextView mMileageExpense;

    public StatisticsComponent(Context context) {
        super(context);
        mCtx = context;
        init();
    }

    public StatisticsComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;
        init();
    }

    private void init() {
        View rootView = inflate(mCtx, R.layout.component_statistics, this);

        mHeader = rootView.findViewById(R.id.tvHeader);
        mLastKnownOdometer = rootView.findViewById(R.id.tvLastKnownOdometer);
        mAvgFuelEff = rootView.findViewById(R.id.tvAvgFuelEff);
        mLastFuelEff = rootView.findViewById(R.id.tvLastFuelEff);
        mTotalExpenses = rootView.findViewById(R.id.tvTotalExpenses);
        mMileageExpense = rootView.findViewById(R.id.tvMileageExpense);

        setAvgFuelEffText(null);
        setLastFuelEffText(null);
        setLastKnownOdometerText(null);
        setTotalExpensesText(null);
        setMileageExpenseText(null);
    }

    public void setHeaderText(String headerText) {
        mHeader.setText(headerText);
    }

    public void setLastKnownOdometerText(String lastKnownOdometerText) {
        if (lastKnownOdometerText == null)
            mLastKnownOdometer.setVisibility(GONE);
        else {
            mLastKnownOdometer.setVisibility(VISIBLE);
            mLastKnownOdometer.setText(lastKnownOdometerText);
        }
    }

    public void setAvgFuelEffText(String avgFuelEffText) {
        if (avgFuelEffText == null)
            mAvgFuelEff.setVisibility(GONE);
        else {
            mAvgFuelEff.setVisibility(VISIBLE);
            mAvgFuelEff.setText(avgFuelEffText);
        }
    }

    public void setLastFuelEffText(String lastFuelEffText) {
        if (lastFuelEffText == null)
            mLastFuelEff.setVisibility(GONE);
        else {
            mLastFuelEff.setVisibility(VISIBLE);
            mLastFuelEff.setText(lastFuelEffText);
        }
    }

    public void setTotalExpensesText(String totalExpensesText) {
        if (totalExpensesText == null)
            mTotalExpenses.setVisibility(GONE);
        else {
            mTotalExpenses.setVisibility(VISIBLE);
            mTotalExpenses.setText(totalExpensesText);
        }
    }

    public void setMileageExpenseText(String mileageExpenseText) {
        if (mileageExpenseText == null)
            mMileageExpense.setVisibility(GONE);
        else {
            mMileageExpense.setVisibility(VISIBLE);
            mMileageExpense.setText(mileageExpenseText);
        }
    }

}
