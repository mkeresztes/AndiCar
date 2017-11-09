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

import andicar.n.persistence.DBReportAdapter;

public class StatisticsActivity extends AppCompatActivity {

    //statistics type == CommonListActivity.ACTIVITY_TYPE_... (mileage, refuel, etc.)
    public static final String STATISTICS_TYPE_KEY = "StatisticsType";

    public static final String WHERE_CONDITIONS_KEY = "WhereConditions";

    private Bundle mWhereConditions = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int statisticsType;

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

        statisticsType = extras.getInt(STATISTICS_TYPE_KEY, -1);
        if (statisticsType == -1)
            return;

        mWhereConditions = extras.getBundle(WHERE_CONDITIONS_KEY);

        switch (statisticsType) {
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
//        StringBuilder htmContent = new StringBuilder();
        CharSequence spanText;

        TextView tvStatistics = findViewById(R.id.tvStatistics);

        DBReportAdapter reportAdapter = new DBReportAdapter(this, DBReportAdapter.MILEAGE_LIST_STATISTICS_SELECT_BY_TYPES, mWhereConditions);
        Cursor c = reportAdapter.fetchReport(-1);
        if (c == null)
            return;

//        htmContent.append("<b>Trips by types:</b>");
        spanText = apply(new CharSequence[]{"Trips by types:"}, new StyleSpan(Typeface.BOLD));

        while (c.moveToNext()) {
//            htmContent.append("<br>    ").append(c.getString(0)).append(": ").append(c.getString(1)).append("; ").append(c.getString(2));
            spanText = TextUtils.concat(spanText,
                    apply(new CharSequence[]{"\n\t\t" + c.getString(0) + ": " + c.getString(1) + "; " + c.getString(2)}, new StyleSpan(Typeface.ITALIC)));
        }
        c.close();
        reportAdapter.close();

        tvStatistics.setText(spanText);
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
            } else {
                text.removeSpan(tag);
            }
        }
    }
}
