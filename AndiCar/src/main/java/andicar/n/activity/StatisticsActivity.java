package andicar.n.activity;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.WindowManager;
import android.widget.TextView;

import org.andicar2.activity.R;

import java.util.Set;

import andicar.n.persistence.DB;
import andicar.n.persistence.DBReportAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

public class StatisticsActivity extends AppCompatActivity {

    //statistics type == CommonListActivity.ACTIVITY_TYPE_... (mileage, refuel, etc.)
    public static final String STATISTICS_TYPE_KEY = "StatisticsType";
    public static final String WHERE_CONDITIONS_KEY = "WhereConditions";
    private int mStatisticsType;

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

    private void fillStatistics() {
        CharSequence spanTextFilters;
        Set<String> whereColumns = null;

        reportAdapter = new DBReportAdapter(this, null, null);

        tvStatisticsValues = findViewById(R.id.tvStatisticsValues);
        TextView tvStatisticsFilters = findViewById(R.id.tvStatisticsFilters);

        if (mWhereConditions != null) {
            whereColumns = mWhereConditions.keySet();
        }

        boolean isNotFiltered = true;
        spanTextFilters = apply(new CharSequence[]{"Filters: "}, new StyleSpan(Typeface.BOLD));
        if (whereColumns != null && whereColumns.size() > 0) {
            for (String whereColumn : whereColumns) {
                if (whereColumn.toUpperCase().contains("DEF_DRIVER_ID")) {
                    spanTextFilters = TextUtils.concat(spanTextFilters,
                            apply(new CharSequence[]{"\n\t\tDriver: " + reportAdapter.getNameById(DB.TABLE_NAME_DRIVER,
                                    Long.decode(mWhereConditions.getString(whereColumn)))}, new StyleSpan(Typeface.ITALIC)));
                    isNotFiltered = false;
                }
                if (whereColumn.toUpperCase().contains("DEF_CAR_ID")) {
                    spanTextFilters = TextUtils.concat(spanTextFilters,
                            apply(new CharSequence[]{"\n\t\tCar: " + reportAdapter.getNameById(DB.TABLE_NAME_CAR,
                                    Long.decode(mWhereConditions.getString(whereColumn)))}, new StyleSpan(Typeface.ITALIC)));
                    isNotFiltered = false;
                }
                if (whereColumn.toUpperCase().contains("DATE >=")) {
                    spanTextFilters = TextUtils.concat(spanTextFilters,
                            apply(new CharSequence[]{"\n\t\tDate from: " +
                                    Utils.getFormattedDateTime(Long.decode(mWhereConditions.getString(whereColumn)) * 1000, true)}, new StyleSpan(Typeface.ITALIC)));
                    isNotFiltered = false;
                }
                if (whereColumn.toUpperCase().contains("DATE <=")) {
                    spanTextFilters = TextUtils.concat(spanTextFilters,
                            apply(new CharSequence[]{"\n\t\tDate to: " +
                                    Utils.getFormattedDateTime(Long.decode(mWhereConditions.getString(whereColumn)) * 1000, true)}, new StyleSpan(Typeface.ITALIC)));
                    isNotFiltered = false;
                }
                else if (whereColumn.toUpperCase().contains("DEF_EXPENSETYPE_ID")) {
                    if (mStatisticsType == CommonListActivity.ACTIVITY_TYPE_MILEAGE) {
                        spanTextFilters = TextUtils.concat(spanTextFilters,
                                apply(new CharSequence[]{"\n\t\tTrip type: " + reportAdapter.getNameById(DB.TABLE_NAME_EXPENSETYPE,
                                        Long.decode(mWhereConditions.getString(whereColumn)))}, new StyleSpan(Typeface.ITALIC)));
                    }
                    else if (mStatisticsType == CommonListActivity.ACTIVITY_TYPE_REFUEL) {
                        spanTextFilters = TextUtils.concat(spanTextFilters,
                                apply(new CharSequence[]{"\n\t\tFill-up type: " + reportAdapter.getNameById(DB.TABLE_NAME_EXPENSETYPE,
                                        Long.decode(mWhereConditions.getString(whereColumn)))}, new StyleSpan(Typeface.ITALIC)));
                    }
                    else if (mStatisticsType == CommonListActivity.ACTIVITY_TYPE_EXPENSE) {
                        spanTextFilters = TextUtils.concat(spanTextFilters,
                                apply(new CharSequence[]{"\n\t\tExpense type: " + reportAdapter.getNameById(DB.TABLE_NAME_EXPENSETYPE,
                                        Long.decode(mWhereConditions.getString(whereColumn)))}, new StyleSpan(Typeface.ITALIC)));
                    }
                    isNotFiltered = false;
                }
                else if (whereColumn.toUpperCase().contains("DEF_EXPENSECATEGORY_ID")) {
                    if (mStatisticsType == CommonListActivity.ACTIVITY_TYPE_REFUEL) {
                        spanTextFilters = TextUtils.concat(spanTextFilters,
                                apply(new CharSequence[]{"\n\t\tFuel type: " + reportAdapter.getNameById(DB.TABLE_NAME_EXPENSECATEGORY,
                                        Long.decode(mWhereConditions.getString(whereColumn)))}, new StyleSpan(Typeface.ITALIC)));
                    }
                    else if (mStatisticsType == CommonListActivity.ACTIVITY_TYPE_EXPENSE) {
                        spanTextFilters = TextUtils.concat(spanTextFilters,
                                apply(new CharSequence[]{"\n\t\tExpense category: " + reportAdapter.getNameById(DB.TABLE_NAME_EXPENSECATEGORY,
                                        Long.decode(mWhereConditions.getString(whereColumn)))}, new StyleSpan(Typeface.ITALIC)));
                    }
                    isNotFiltered = false;
                }
                else //noinspection ConstantConditions
                    if (whereColumn.toUpperCase().contains("USERCOMMENT") && mWhereConditions.getString(whereColumn) != null && !mWhereConditions.getString(whereColumn).equals("%")) {
                        //noinspection ConstantConditions
                        spanTextFilters = TextUtils.concat(spanTextFilters,
                                apply(new CharSequence[]{"\n\t\tComment: " +
                                        (mWhereConditions.getString(whereColumn).equals("") ? "without comment" : mWhereConditions.getString(whereColumn))}, new StyleSpan(Typeface.ITALIC)));
                        isNotFiltered = false;
                    }
                    else //noinspection ConstantConditions
                        if (whereColumn.toUpperCase().contains("DEF_TAG.NAME") && mWhereConditions.getString(whereColumn) != null && !mWhereConditions.getString(whereColumn).equals("%")) {
                            //noinspection ConstantConditions
                            spanTextFilters = TextUtils.concat(spanTextFilters,
                                    apply(new CharSequence[]{"\n\t\tTag: " +
                                            (mWhereConditions.getString(whereColumn).equals("") ? "without tag" : mWhereConditions.getString(whereColumn))}, new StyleSpan(Typeface.ITALIC)));
                            isNotFiltered = false;
                        }
                        else if (whereColumn.toUpperCase().contains("DEF_TAG_ID") && mWhereConditions.getString(whereColumn) != null) {
                            //noinspection ConstantConditions
                            spanTextFilters = TextUtils.concat(spanTextFilters,
                                    apply(new CharSequence[]{"\n\t\tTag: " +
                                            (mWhereConditions.getString(whereColumn).equals("NULL") ? "without tag" :
                                                    reportAdapter.getNameById(DB.TABLE_NAME_TAG,
                                                            Long.decode(mWhereConditions.getString(whereColumn))))}, new StyleSpan(Typeface.ITALIC)));
                            isNotFiltered = false;
                        }
            }
        }
        else {
            spanTextFilters = TextUtils.concat(spanTextFilters, apply(new CharSequence[]{"no filters"}, new StyleSpan(Typeface.BOLD)));
        }

        if (isNotFiltered) {
            spanTextFilters = TextUtils.concat(spanTextFilters, apply(new CharSequence[]{"no filters"}, new StyleSpan(Typeface.BOLD)));
        }

        tvStatisticsFilters.setText(spanTextFilters);

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
        CharSequence spanTextValues;
        Cursor c;
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
            spanTextValues = apply(new CharSequence[]{"Total expenses: " +
                            Utils.numberToString(c.getString(0), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                            c.getString(1)},
                    new StyleSpan(Typeface.BOLD));
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
            spanTextValues = TextUtils.concat(spanTextValues, apply(new CharSequence[]{"\n\nExpenses by types:"}, new StyleSpan(Typeface.BOLD)));

            while (c.moveToNext()) {
                spanTextValues = TextUtils.concat(spanTextValues,
                        apply(new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                                Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                                c.getString(2)}, new StyleSpan(Typeface.ITALIC)));
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
            spanTextValues = TextUtils.concat(spanTextValues, apply(new CharSequence[]{"\n\nExpenses by categories:"}, new StyleSpan(Typeface.BOLD)));

            while (c.moveToNext()) {
                spanTextValues = TextUtils.concat(spanTextValues,
                        apply(new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                                Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                                c.getString(2)}, new StyleSpan(Typeface.ITALIC)));
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
            spanTextValues = TextUtils.concat(spanTextValues, apply(new CharSequence[]{"\n\nExpenses by tags:"}, new StyleSpan(Typeface.BOLD)));

            while (c.moveToNext()) {
                spanTextValues = TextUtils.concat(spanTextValues,
                        apply(new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                                Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                                c.getString(2)}, new StyleSpan(Typeface.ITALIC)));
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
            spanTextValues = TextUtils.concat(spanTextValues, apply(new CharSequence[]{"\n\nExpenses by drivers:"}, new StyleSpan(Typeface.BOLD)));

            while (c.moveToNext()) {
                spanTextValues = TextUtils.concat(spanTextValues,
                        apply(new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                                Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                                c.getString(2)}, new StyleSpan(Typeface.ITALIC)));
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        tvStatisticsValues.setText(spanTextValues);
    }

    private void fillRefuelStatistics() {
        CharSequence spanTextValues;
        Cursor c;
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
            spanTextValues = apply(new CharSequence[]{"Total fill-ups: " +
                            Utils.numberToString(c.getString(0), true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME) + " " +
                            c.getString(2) + "; " +
                            Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                            c.getString(3)},
                    new StyleSpan(Typeface.BOLD));
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
            spanTextValues = TextUtils.concat(spanTextValues, apply(new CharSequence[]{"\n\nFill-ups by types:"}, new StyleSpan(Typeface.BOLD)));

            while (c.moveToNext()) {
                spanTextValues = TextUtils.concat(spanTextValues,
                        apply(new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                                Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME) + " " +
                                c.getString(2) + "; " +
                                Utils.numberToString(c.getString(3), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                                c.getString(4)}, new StyleSpan(Typeface.ITALIC)));
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
            spanTextValues = TextUtils.concat(spanTextValues, apply(new CharSequence[]{"\n\nFill-ups by fuel types:"}, new StyleSpan(Typeface.BOLD)));

            while (c.moveToNext()) {
                spanTextValues = TextUtils.concat(spanTextValues,
                        apply(new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                                Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME) + " " +
                                c.getString(2) + "; " +
                                Utils.numberToString(c.getString(3), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                                c.getString(4)}, new StyleSpan(Typeface.ITALIC)));
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
            spanTextValues = TextUtils.concat(spanTextValues, apply(new CharSequence[]{"\n\nFill-ups by tags:"}, new StyleSpan(Typeface.BOLD)));

            while (c.moveToNext()) {
                spanTextValues = TextUtils.concat(spanTextValues,
                        apply(new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                                Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME) + " " +
                                c.getString(2) + "; " +
                                Utils.numberToString(c.getString(3), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                                c.getString(4)}, new StyleSpan(Typeface.ITALIC)));
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
            spanTextValues = TextUtils.concat(spanTextValues, apply(new CharSequence[]{"\n\nFill-ups by drivers:"}, new StyleSpan(Typeface.BOLD)));

            while (c.moveToNext()) {
                spanTextValues = TextUtils.concat(spanTextValues,
                        apply(new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                                Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME) + " " +
                                c.getString(2) + "; " +
                                Utils.numberToString(c.getString(3), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " +
                                c.getString(4)}, new StyleSpan(Typeface.ITALIC)));
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        tvStatisticsValues.setText(spanTextValues);
    }

    private void fillMileageStatistics() {
        CharSequence spanTextValues;
        Cursor c;
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
            spanTextValues = apply(new CharSequence[]{"Total trips: " +
                    Utils.numberToString(c.getString(0), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) +
                    " " + c.getString(1)}, new StyleSpan(Typeface.BOLD));
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
            spanTextValues = TextUtils.concat(spanTextValues, apply(new CharSequence[]{"\n\nTrips by types:"}, new StyleSpan(Typeface.BOLD)));

            while (c.moveToNext()) {
                spanTextValues = TextUtils.concat(spanTextValues,
                        apply(new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                                Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) +
                                " " + c.getString(2)}, new StyleSpan(Typeface.ITALIC)));
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
            spanTextValues = TextUtils.concat(spanTextValues, apply(new CharSequence[]{"\n\nTrips by tags:"}, new StyleSpan(Typeface.BOLD)));

            while (c.moveToNext()) {
                spanTextValues = TextUtils.concat(spanTextValues,
                        apply(new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                                Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) +
                                " " + c.getString(2)}, new StyleSpan(Typeface.ITALIC)));
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
            spanTextValues = TextUtils.concat(spanTextValues, apply(new CharSequence[]{"\n\nTrips by drivers:"}, new StyleSpan(Typeface.BOLD)));

            while (c.moveToNext()) {
                spanTextValues = TextUtils.concat(spanTextValues,
                        apply(new CharSequence[]{"\n\t\t" + c.getString(0) + ": " +
                                Utils.numberToString(c.getString(1), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) +
                                " " + c.getString(2)}, new StyleSpan(Typeface.ITALIC)));
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        tvStatisticsValues.setText(spanTextValues);
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
