package andicar.n.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import org.andicar2.activity.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import andicar.n.persistence.DB;
import andicar.n.persistence.DBReportAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

public class StatisticsActivity extends AppCompatActivity {

    //statistics type == CommonListActivity.ACTIVITY_TYPE_... (mileage, refuel, etc.)
    public static final String STATISTICS_TYPE_KEY = "StatisticsType";
    public static final String WHERE_CONDITIONS_KEY = "WhereConditions";
    private int mStatisticsType;
    private CharSequence mSpanText_Values;
    private StringBuilder mEmailText_Filters = new StringBuilder("");
    private StringBuilder mEmailText_Values = new StringBuilder("");

    private Bundle mWhereConditions = null;
    private DBReportAdapter reportAdapter;

    private TextView tvStatisticsValues;

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
        if (extras == null) {
            return;
        }

        mStatisticsType = extras.getInt(STATISTICS_TYPE_KEY, -1);
        if (mStatisticsType == -1) {
            return;
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null) {
            menu.clear();
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_share, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            share();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void share() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SENDTO);
            shareIntent.setData(Uri.parse("mailto:"));
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, " AndiCar - " + getTitle());
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    mEmailText_Filters.toString().replace("\t", "    ") + "\n\n" +
                            "Data:\n" +
                            mEmailText_Values.toString().replace("\t", "    ") + "\n\nSent by AndiCar (http://www.andicar.org)");
            startActivity(shareIntent);
        }
        catch (ActivityNotFoundException e) {
            Utils.showNotReportableErrorDialog(this, e.getMessage(), null);
        }
    }

    private void fillStatistics() {
        Set<String> whereColumns = null;
        CharSequence spanText_Filters;
        CharSequence[] cs;
        HashMap<Integer, String> filterList = new HashMap<Integer, String>();

        reportAdapter = new DBReportAdapter(this, null, null);

        tvStatisticsValues = findViewById(R.id.tvStatisticsValues);
        TextView tvStatisticsFilters = findViewById(R.id.tvStatisticsFilters);

        if (mWhereConditions != null) {
            whereColumns = mWhereConditions.keySet();
        }

        boolean isNotFiltered = true;
        cs = new CharSequence[]{"Filters: "};
        spanText_Filters = apply(cs, new StyleSpan(Typeface.BOLD));
        filterList.put(0, cs[0].toString());

        if (whereColumns != null && whereColumns.size() > 0) {
            for (String whereColumn : whereColumns) {
                if (whereColumn.toUpperCase().contains("DEF_CAR_ID")) {
                    cs = new CharSequence[]{"\n\t\tCar: " + reportAdapter.getNameById(DB.TABLE_NAME_CAR,
                            Long.decode(mWhereConditions.getString(whereColumn)))};
                    spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.ITALIC)));
                    filterList.put(1, cs[0].toString());
                    isNotFiltered = false;
                }
                else if (whereColumn.toUpperCase().contains("DEF_DRIVER_ID")) {
                    cs = new CharSequence[]{"\n\t\tDriver: " + reportAdapter.getNameById(DB.TABLE_NAME_DRIVER,
                            Long.decode(mWhereConditions.getString(whereColumn)))};
                    spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.ITALIC)));
                    filterList.put(2, cs[0].toString());
                    isNotFiltered = false;
                }
                else if (whereColumn.toUpperCase().contains("DEF_EXPENSETYPE_ID")) {
                    if (mStatisticsType == CommonListActivity.ACTIVITY_TYPE_MILEAGE) {
                        cs = new CharSequence[]{"\n\t\tTrip type: " + reportAdapter.getNameById(DB.TABLE_NAME_EXPENSETYPE,
                                Long.decode(mWhereConditions.getString(whereColumn)))};
                        spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.ITALIC)));
                        filterList.put(3, cs[0].toString());
                    }
                    else if (mStatisticsType == CommonListActivity.ACTIVITY_TYPE_REFUEL) {
                        cs = new CharSequence[]{"\n\t\tFill-up type: " + reportAdapter.getNameById(DB.TABLE_NAME_EXPENSETYPE,
                                Long.decode(mWhereConditions.getString(whereColumn)))};
                        spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.ITALIC)));
                        filterList.put(3, cs[0].toString());
                    }
                    else if (mStatisticsType == CommonListActivity.ACTIVITY_TYPE_EXPENSE) {
                        cs = new CharSequence[]{"\n\t\tExpense type: " + reportAdapter.getNameById(DB.TABLE_NAME_EXPENSETYPE,
                                Long.decode(mWhereConditions.getString(whereColumn)))};
                        spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.ITALIC)));
                        filterList.put(3, cs[0].toString());
                    }
                    isNotFiltered = false;
                }
                else if (whereColumn.toUpperCase().contains("DEF_EXPENSECATEGORY_ID")) {
                    if (mStatisticsType == CommonListActivity.ACTIVITY_TYPE_REFUEL) {
                        cs = new CharSequence[]{"\n\t\tFuel type: " + reportAdapter.getNameById(DB.TABLE_NAME_EXPENSECATEGORY,
                                Long.decode(mWhereConditions.getString(whereColumn)))};
                        spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.ITALIC)));
                        filterList.put(4, cs[0].toString());
                    }
                    else if (mStatisticsType == CommonListActivity.ACTIVITY_TYPE_EXPENSE) {
                        cs = new CharSequence[]{"\n\t\tExpense category: " + reportAdapter.getNameById(DB.TABLE_NAME_EXPENSECATEGORY,
                                Long.decode(mWhereConditions.getString(whereColumn)))};
                        spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.ITALIC)));
                        filterList.put(4, cs[0].toString());
                    }
                    isNotFiltered = false;
                }
                else if (whereColumn.toUpperCase().contains("DATE >=")) {
                    cs = new CharSequence[]{"\n\t\tDate from: " +
                            Utils.getFormattedDateTime(Long.decode(mWhereConditions.getString(whereColumn)) * 1000, true)};
                    spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.ITALIC)));
                    filterList.put(5, cs[0].toString());
                    isNotFiltered = false;
                }
                else if (whereColumn.toUpperCase().contains("DATE <=")) {
                    cs = new CharSequence[]{"\n\t\tDate to: " +
                            Utils.getFormattedDateTime(Long.decode(mWhereConditions.getString(whereColumn)) * 1000, true)};
                    spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.ITALIC)));
                    filterList.put(6, cs[0].toString());
                    isNotFiltered = false;
                }
                else if (whereColumn.toUpperCase().contains("USERCOMMENT") && mWhereConditions.getString(whereColumn) != null &&
                        !mWhereConditions.getString(whereColumn).equals("%")) {
                    //noinspection ConstantConditions
                    cs = new CharSequence[]{"\n\t\tComment: " +
                            (mWhereConditions.getString(whereColumn).equals("") ? "without comment" : mWhereConditions.getString(whereColumn))};
                    spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.ITALIC)));
                    filterList.put(7, cs[0].toString());
                    isNotFiltered = false;
                }
                else if (whereColumn.toUpperCase().contains("DEF_TAG.NAME") && mWhereConditions.getString(whereColumn) != null &&
                        !mWhereConditions.getString(whereColumn).equals("%")) {
                    //noinspection ConstantConditions
                    cs = new CharSequence[]{"\n\t\tTag: " +
                            (mWhereConditions.getString(whereColumn).equals("") ? "without tag" : mWhereConditions.getString(whereColumn))};
                    spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.ITALIC)));
                    filterList.put(7, cs[0].toString());
                    isNotFiltered = false;
                }
                else if (whereColumn.toUpperCase().contains("DEF_TAG_ID") && mWhereConditions.getString(whereColumn) != null) {
                    //noinspection ConstantConditions
                    cs = new CharSequence[]{"\n\t\tTag: " +
                            (mWhereConditions.getString(whereColumn).equals("NULL") ? "without tag" :
                                    reportAdapter.getNameById(DB.TABLE_NAME_TAG,
                                            Long.decode(mWhereConditions.getString(whereColumn))))};
                    spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.ITALIC)));
                    filterList.put(7, cs[0].toString());
                    isNotFiltered = false;
                }
            }
        }
        else {
            cs = new CharSequence[]{"no filters"};
            spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.BOLD)));
            filterList.put(7, cs[0].toString());
        }

        if (isNotFiltered) {
            cs = new CharSequence[]{"no filters"};
            spanText_Filters = TextUtils.concat(spanText_Filters, apply(cs, new StyleSpan(Typeface.BOLD)));
            filterList.put(7, cs[0].toString());
        }

        tvStatisticsFilters.setText(spanText_Filters);
        Map<Integer, String> orderedFilterList = new TreeMap<>(filterList);
        Set set2 = orderedFilterList.entrySet();
        for (Object aSet2 : set2) {
            Map.Entry me2 = (Map.Entry) aSet2;
            mEmailText_Filters.append(me2.getValue());
        }

        if (mStatisticsType == CommonListActivity.ACTIVITY_TYPE_MILEAGE) {
            fillMileageStatistics();
        }
        else if (mStatisticsType == CommonListActivity.ACTIVITY_TYPE_REFUEL) {
            fillRefuelStatistics();
        }
        else if (mStatisticsType == CommonListActivity.ACTIVITY_TYPE_EXPENSE) {
            fillExpenseStatistics();
        }

        try {
            reportAdapter.close();
        }
        catch (Exception ignored) {
        }
    }

    private void fillExpenseStatistics() {
        Cursor c;
        CharSequence[] cs;
        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_EXPENSE_TOTAL, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c == null) {
            try {
                reportAdapter.close();
            }
            catch (Exception ignored) {
            }
            return;
        }

        if (c.moveToNext()) {
            cs = new CharSequence[]{"Total expenses: " +
                    Utils.numberToString(c.getString(0), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                    c.getString(1)};
            mSpanText_Values = apply(cs, new StyleSpan(Typeface.BOLD));
            mEmailText_Values.append(cs[0].toString());
        }
        else {
            try {
                c.close();
                reportAdapter.close();
            }
            catch (Exception ignored) {
            }
            return;
        }

        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_EXPENSE_BY_TYPES, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c != null) {
            cs = new CharSequence[]{"\n\nExpenses by types:"};
            mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.BOLD)));
            mEmailText_Values.append(cs[0].toString());

            while (c.moveToNext()) {
                cs = new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                        Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                        c.getString(2)};
                mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.ITALIC)));
                mEmailText_Values.append(cs[0].toString());
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_EXPENSE_BY_CATEGORIES, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c != null) {
            cs = new CharSequence[]{"\n\nExpenses by categories:"};
            mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.BOLD)));
            mEmailText_Values.append(cs[0].toString());

            while (c.moveToNext()) {
                cs = new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                        Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                        c.getString(2)};
                mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.ITALIC)));
                mEmailText_Values.append(cs[0].toString());
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_EXPENSE_BY_TAGS, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c != null) {
            cs = new CharSequence[]{"\n\nExpenses by tags:"};
            mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.BOLD)));
            mEmailText_Values.append(cs[0].toString());

            while (c.moveToNext()) {
                cs = new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                        Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                        c.getString(2)};
                mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.ITALIC)));
                mEmailText_Values.append(cs[0].toString());
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_EXPENSE_BY_DRIVERS, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c != null) {
            cs = new CharSequence[]{"\n\nExpenses by drivers:"};
            mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.BOLD)));
            mEmailText_Values.append(cs[0].toString());

            while (c.moveToNext()) {
                cs = new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                        Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                        c.getString(2)};
                mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.ITALIC)));
                mEmailText_Values.append(cs[0].toString());
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        tvStatisticsValues.setText(mSpanText_Values);
    }

    private void fillRefuelStatistics() {
        Cursor c;
        CharSequence[] cs;

        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_REFUEL_TOTAL, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c == null) {
            try {
                reportAdapter.close();
            }
            catch (Exception ignored) {
            }
            return;
        }

        if (c.moveToNext()) {
            cs = new CharSequence[]{"Total fill-ups: " +
                    Utils.numberToString(c.getString(0), true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME) + " " +
                    c.getString(2) + "; " +
                    Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                    c.getString(3)};
            mSpanText_Values = apply(cs, new StyleSpan(Typeface.BOLD));
            mEmailText_Values.append(cs[0].toString());
        }
        else {
            try {
                c.close();
                reportAdapter.close();
            }
            catch (Exception ignored) {
            }
            return;
        }

        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_REFUEL_BY_TYPES, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c != null) {
            cs = new CharSequence[]{"\n\nFill-ups by types:"};
            mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.BOLD)));
            mEmailText_Values.append(cs[0].toString());

            while (c.moveToNext()) {
                cs = new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                        Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME) + " " +
                        c.getString(2) + "; " +
                        Utils.numberToString(c.getString(3), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                        c.getString(4)};
                mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.ITALIC)));
                mEmailText_Values.append(cs[0].toString());
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_REFUEL_BY_FUELTYPES, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c != null) {
            cs = new CharSequence[]{"\n\nFill-ups by fuel types:"};
            mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.BOLD)));
            mEmailText_Values.append(cs[0].toString());

            while (c.moveToNext()) {
                cs = new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                        Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME) + " " +
                        c.getString(2) + "; " +
                        Utils.numberToString(c.getString(3), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                        c.getString(4)};
                mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.ITALIC)));
                mEmailText_Values.append(cs[0].toString());
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_REFUEL_BY_TAGS, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c != null) {
            cs = new CharSequence[]{"\n\nFill-ups by tags:"};
            mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.BOLD)));
            mEmailText_Values.append(cs[0].toString());

            while (c.moveToNext()) {
                cs = new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                        Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME) + " " +
                        c.getString(2) + "; " +
                        Utils.numberToString(c.getString(3), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                        c.getString(4)};
                mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.ITALIC)));
                mEmailText_Values.append(cs[0].toString());
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_REFUEL_BY_DRIVERS, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c != null) {
            cs = new CharSequence[]{"\n\nFill-ups by drivers:"};
            mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.BOLD)));
            mEmailText_Values.append(cs[0].toString());

            while (c.moveToNext()) {
                cs = new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                        Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME) + " " +
                        c.getString(2) + "; " +
                        Utils.numberToString(c.getString(3), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                        c.getString(4)};
                mSpanText_Values = TextUtils.concat(mSpanText_Values,
                        apply(cs, new StyleSpan(Typeface.ITALIC)));
                mEmailText_Values.append(cs[0].toString());
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        tvStatisticsValues.setText(mSpanText_Values);
    }

    private void fillMileageStatistics() {
        Cursor c;
        CharSequence[] cs;
        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_MILEAGE_TOTAL, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c == null) {
            try {
                reportAdapter.close();
            }
            catch (Exception ignored) {
            }
            return;
        }

        if (c.moveToNext()) {
            cs = new CharSequence[]{"Total trips: " +
                    Utils.numberToString(c.getString(0), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) +
                    " " + c.getString(1)};
            mSpanText_Values = apply(cs, new StyleSpan(Typeface.BOLD));
            mEmailText_Values.append(cs[0].toString());
        }
        else {
            try {
                c.close();
                reportAdapter.close();
            }
            catch (Exception ignored) {
            }
            return;
        }

        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_MILEAGE_BY_TYPES, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c != null) {
            cs = new CharSequence[]{"\n\nTrips by types:"};
            mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.BOLD)));
            mEmailText_Values.append(cs[0].toString());

            while (c.moveToNext()) {
                cs = new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                        Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) +
                        " " + c.getString(2)};
                mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.ITALIC)));
                mEmailText_Values.append(cs[0].toString());
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_MILEAGE_BY_TAGS, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c != null) {
            cs = new CharSequence[]{"\n\nTrips by tags:"};
            mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.BOLD)));
            mEmailText_Values.append(cs[0].toString());

            while (c.moveToNext()) {
                cs = new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                        Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) +
                        " " + c.getString(2)};
                mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.ITALIC)));
                mEmailText_Values.append(cs[0].toString());
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        reportAdapter.setReportSql(DBReportAdapter.LIST_STATISTICS_MILEAGE_BY_DRIVERS, mWhereConditions);
        c = reportAdapter.fetchReport(-1);
        if (c != null) {
            cs = new CharSequence[]{"\n\nTrips by drivers:"};
            mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.BOLD)));
            mEmailText_Values.append(cs[0].toString());

            while (c.moveToNext()) {
                cs = new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                        Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) +
                        " " + c.getString(2)};
                mSpanText_Values = TextUtils.concat(mSpanText_Values, apply(cs, new StyleSpan(Typeface.ITALIC)));
                mEmailText_Values.append(cs[0].toString());
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        tvStatisticsValues.setText(mSpanText_Values);
    }

    /**
     * Returns a CharSequence that concatenates the specified array of CharSequence
     * objects and then applies a list of zero or more tags to the entire range.
     *
     * @param content an array of character sequences to apply a style to
     * @param tags    the styled span objects to apply to the content
     *                such as android.text.style.StyleSpan
     */
    private static CharSequence apply(CharSequence[] content, Object... tags) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        openTags(text, tags);
        for (CharSequence item : content) {
            text.append(item);
        }
        closeTags(text, tags);
        return text;
    }

    /**
     * Iterates over an array of tags and applies them to the beginning of the specified
     * Spannable object so that future text appended to the text will have the styling
     * applied to it. Do not call this method directly.
     */
    private static void openTags(Spannable text, Object[] tags) {
        for (Object tag : tags) {
            text.setSpan(tag, 0, 0, Spannable.SPAN_MARK_MARK);
        }
    }

    /**
     * "Closes" the specified tags on a Spannable by updating the spans to be
     * endpoint-exclusive so that future text appended to the end will not take
     * on the same styling. Do not call this method directly.
     */
    private static void closeTags(Spannable text, Object[] tags) {
        int len = text.length();
        for (Object tag : tags) {
            if (len > 0) {
                text.setSpan(tag, 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else {
                text.removeSpan(tag);
            }
        }
    }
}
