package andicar.n.persistence.viewadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.util.Calendar;

import andicar.n.activity.CommonListActivity;
import andicar.n.persistence.DBAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by miki on 09.03.2017.
 */

public class ToDoViewAdapter extends BaseViewAdapter {
    private final Resources mRes;
    private final Context mCtx;
    private DBAdapter mDBAdapter;

    public ToDoViewAdapter(Context ctx, Cursor cursor, CommonListActivity parentActivity, boolean isTwoPane, int scrollToPosition, long lastSelectedItemId) {
        super(cursor, parentActivity, isTwoPane, scrollToPosition, lastSelectedItemId);
        mViewAdapterType = VIEW_ADAPTER_TYPE_TODO;
        mDBAdapter = new DBAdapter(ctx);
        mRes = AndiCar.getAppResources();
        mCtx = ctx;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (mDBAdapter != null) {
            mDBAdapter.close();
            mDBAdapter = null;
        }
    }

    @SuppressLint("WrongConstant")
    @SuppressWarnings("deprecation")
    @Override
    protected void cursorViewBinder(DefaultViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String line1Content;
        String line2Content;

        mCursor.moveToPosition(position);

        holder.mRecordID = mCursor.getLong(0);

        String dataString = mCursor.getString(1);
        if (dataString.contains("[#5]")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.mFirstLine.setTextColor(mRes.getColor(R.color.todo_overdue_text_color, mCtx.getTheme()));
            }
            else {
                holder.mFirstLine.setTextColor(mRes.getColor(R.color.todo_overdue_text_color));
            }
        }
        else if (dataString.contains("[#15]")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.mFirstLine.setTextColor(mRes.getColor(R.color.todo_done_text_color, mCtx.getTheme()));
            }
            else {
                holder.mFirstLine.setTextColor(mRes.getColor(R.color.todo_done_text_color));
            }
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.mFirstLine.setTextColor(mRes.getColor(android.R.color.primary_text_light, mCtx.getTheme()));
            }
            else {
                holder.mFirstLine.setTextColor(Color.BLACK);
            }
        }
//        line1Content = dataString.replace("[#1]", mRes.getString(R.string.gen_type_label)).replace("[#2]", mRes.getString(R.string.gen_todo))
        line1Content = dataString.replace("[#1]", "").replace("[#2]", ": ")
                .replace("[#3]", mRes.getString(R.string.gen_car_label)).replace("[#4]", mRes.getString(R.string.todo_status_label))
                .replace("[#5]", mRes.getString(R.string.todo_overdue_label)).replace("[#6]", mRes.getString(R.string.todo_scheduled_label))
                .replace("[#15]", mRes.getString(R.string.todo_done_label));
        holder.mFirstLine.setText(line1Content);

        if (mCursor.getString(2) != null) {
            long time = System.currentTimeMillis();
            Calendar now = Calendar.getInstance();
            Calendar cal = Calendar.getInstance();

            long estMileageDueDays = mCursor.getLong(7);
            String timeStr = "";
            if (estMileageDueDays >= 0) {
                if (estMileageDueDays == 99999999999L) {
                    timeStr = mRes.getString(R.string.todo_estimated_mileage_date_no_data);
                }
                else {
                    if (mCursor.getString(1).contains("[#5]")) {
                        timeStr = mRes.getString(R.string.todo_overdue_label);
                    }
                    else {
                        cal.setTimeInMillis(time + (estMileageDueDays * ConstantValues.ONE_DAY_IN_MILLISECONDS));
                        if (cal.get(Calendar.YEAR) - now.get(Calendar.YEAR) > 5) {
                            timeStr = mRes.getString(R.string.todo_estimated_mileage_date_too_far);
                        }
                        else {
                            if (cal.getTimeInMillis() - now.getTimeInMillis() < 365 * ConstantValues.ONE_DAY_IN_MILLISECONDS) // 1 year
                            {
                                timeStr = Utils.getFormattedDateTime(time + (estMileageDueDays * ConstantValues.ONE_DAY_IN_MILLISECONDS), true);
                            }
                            else {
                                timeStr = DateFormat.format("MMM, yyyy", cal).toString();
                            }

                        }
                    }
                }
            }

            line2Content = mCursor.getString(2)
                    .replace("[#7]", mRes.getString(R.string.todo_scheduled_date_label))
                    .replace(
                            "[#8]", Utils.getFormattedDateTime(mCursor.getLong(4) * 1000, false))
                    .replace("[#9]", mRes.getString(R.string.gen_or)).replace("[#10]", mRes.getString(R.string.todo_scheduled_mileage_label))
                    .replace("[#11]", Utils.numberToString(mCursor.getDouble(5), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH))
                    .replace("[#12]", mRes.getString(R.string.todo_mileage)).replace("[#13]", mRes.getString(R.string.todo_estimated_mileage_date))
                    .replace("[#14]", timeStr);
        }
        else {
            line2Content = null;
        }

        if (line2Content != null) {
            if (holder.mSecondLine != null) { //three line lists
                holder.mSecondLine.setText(line2Content);
            }
            else {
                holder.mThirdLine.setText(line2Content);
            }
        }
    }
}
