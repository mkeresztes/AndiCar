package andicar.n.activity;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.andicar2.activity.R;

import andicar.n.persistence.DBReportAdapter;

public class StatisticsActivity extends AppCompatActivity {

    //statistics type == CommonListActivity.ACTIVITY_TYPE_... (mileage, refuel, etc.)
    public static final String STATISTICS_TYPE_KEY = "StatisticsType";

    public static final String WHERE_CONDITIONS_KEY = "WhereConditions";

    private Bundle mWhereConditions = null;

    private int mStatisticsType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //prevent keyboard from automatic pop up
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_statistics);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Show the Up button in the action bar.
        //The solution is to change dynamically the parent activity for up navigation
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        if (extras == null)
            return;

        mStatisticsType = extras.getInt(STATISTICS_TYPE_KEY, -1);
        if (mStatisticsType == -1)
            return;

        mWhereConditions = extras.getBundle(WHERE_CONDITIONS_KEY);

        switch (mStatisticsType) {
            case CommonListActivity.ACTIVITY_TYPE_MILEAGE:
                setTitle(String.format(getString(R.string.title_statistics_activity), getString(R.string.gen_trips)));
                break;
            case CommonListActivity.ACTIVITY_TYPE_REFUEL:
                setTitle(String.format(getString(R.string.title_statistics_activity), getString(R.string.gen_fill_ups)));
                break;
            case CommonListActivity.ACTIVITY_TYPE_EXPENSE:
                setTitle(String.format(getString(R.string.title_statistics_activity), getString(R.string.gen_expenses)));
                break;
            case CommonListActivity.ACTIVITY_TYPE_GPS_TRACK:
                setTitle(String.format(getString(R.string.title_statistics_activity), getString(R.string.gen_gps_tracks)));
                break;
        }

        fillStatistics();

    }

    private void fillStatistics() {
        LinearLayout zoneContainer = findViewById(R.id.statisticsContainer);
        if (zoneContainer == null) {
            return;
        }
        zoneContainer.removeAllViews();

        DBReportAdapter reportAdapter = new DBReportAdapter(this, DBReportAdapter.MILEAGE_LIST_STATISTICS_SELECT_BY_TYPES, mWhereConditions);
        Cursor c = reportAdapter.fetchReport(-1);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(5, 15, 0, 0);
        params.gravity = Gravity.START;
        while (c.moveToNext()) {
            TextView t = new TextView(this);
            t.setTextColor(Color.BLACK);
            t.setBackgroundColor(Color.BLUE);
            t.setText(c.getString(0) + ": " + c.getString(1) + "; " + c.getString(2));
            t.setGravity(Gravity.CENTER);
            zoneContainer.addView(t, params);
        }
        zoneContainer.invalidate();
        c.close();
        reportAdapter.close();

    }
}
