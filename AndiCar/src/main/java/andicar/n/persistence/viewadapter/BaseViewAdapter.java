/*
 * AndiCar
 *
 *  Copyright (c) 2016 Miklos Keresztes (miklos.keresztes@gmail.com)
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

package andicar.n.persistence.viewadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import andicar.n.activity.CommonDetailActivity;
import andicar.n.activity.CommonListActivity;
import andicar.n.activity.dialogs.GPSTrackControllerDialogActivity;
import andicar.n.activity.dialogs.ToDoNotificationDialogActivity;
import andicar.n.activity.fragment.BPartnerEditFragment;
import andicar.n.activity.fragment.BTCarLinkFragment;
import andicar.n.activity.fragment.BaseEditFragment;
import andicar.n.activity.fragment.CarEditFragment;
import andicar.n.activity.fragment.CurrencyEditFragment;
import andicar.n.activity.fragment.CurrencyRateEditFragment;
import andicar.n.activity.fragment.DriverEditFragment;
import andicar.n.activity.fragment.ExpenseEditFragment;
import andicar.n.activity.fragment.ExpenseFuelCategoryEditFragment;
import andicar.n.activity.fragment.ExpenseTypeEditFragment;
import andicar.n.activity.fragment.GPSTrackControllerFragment;
import andicar.n.activity.fragment.GPSTrackEditFragment;
import andicar.n.activity.fragment.MileageEditFragment;
import andicar.n.activity.fragment.RefuelEditFragment;
import andicar.n.activity.fragment.ReimbursementRateEditFragment;
import andicar.n.activity.fragment.TagEditFragment;
import andicar.n.activity.fragment.TaskEditFragment;
import andicar.n.activity.fragment.TaskTypeEditFragment;
import andicar.n.activity.fragment.ToDoViewFragment;
import andicar.n.activity.fragment.UOMConversionEditFragment;
import andicar.n.activity.fragment.UOMEditFragment;
import andicar.n.persistence.DB;
import andicar.n.persistence.DBAdapter;
import andicar.n.service.ToDoNotificationJob;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 9/23/16.
 * <p>
 * Default RecyclerViewAdapter
 */

@SuppressWarnings("WeakerAccess")
public abstract class BaseViewAdapter
        extends RecyclerView.Adapter<BaseViewAdapter.DefaultViewHolder> {

    public static final int VIEW_ADAPTER_TYPE_MILEAGE = 11;
    public static final int VIEW_ADAPTER_TYPE_REFUEL = 12;
    public static final int VIEW_ADAPTER_TYPE_EXPENSE = 13;
    public static final int VIEW_ADAPTER_TYPE_GPS_TRACK = 14;
    public static final int VIEW_ADAPTER_TYPE_TODO = 15;
    public static final int VIEW_ADAPTER_TYPE_CAR = 21;
    public static final int VIEW_ADAPTER_TYPE_DRIVER = 22;
    public static final int VIEW_ADAPTER_TYPE_UOM = 23;
    public static final int VIEW_ADAPTER_TYPE_UOM_CONVERSION = 24;
    public static final int VIEW_ADAPTER_TYPE_EXPENSE_CATEGORY = 25;
    public static final int VIEW_ADAPTER_TYPE_FUEL_TYPE = 26;
    public static final int VIEW_ADAPTER_TYPE_EXPENSE_TYPE = 27;
    public static final int VIEW_ADAPTER_TYPE_REIMBURSEMENT_RATE = 28;
    public static final int VIEW_ADAPTER_TYPE_CURRENCY = 29;
    public static final int VIEW_ADAPTER_TYPE_CURRENCY_RATE = 30;
    public static final int VIEW_ADAPTER_TYPE_BPARTNER = 31;
    public static final int VIEW_ADAPTER_TYPE_TASK_TYPE = 32;
    public static final int VIEW_ADAPTER_TYPE_TASK = 33;
    public static final int VIEW_ADAPTER_TYPE_BT_CAR_LINK = 34;
    public static final int VIEW_ADAPTER_TYPE_TAG = 35;
    final AppCompatActivity mParentActivity;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    final boolean isTwoPane;
    final Cursor mCursor;
    //show the detail panel for the first item when the activity is created and is two pane mode
    protected int mSelectedPosition = -1;
    protected long mLastSelectedItemId;
    /**
     * used to differentiate the different types of view adapters
     */
    protected int mViewAdapterType;
    private View lastSelectedView = null;

    BaseViewAdapter(Cursor cursor, AppCompatActivity parentActivity, boolean isTwoPane, int scrollToPosition, long lastSelectedItemId) {
        mCursor = cursor;
        mParentActivity = parentActivity;
        this.isTwoPane = isTwoPane;

        mSelectedPosition = scrollToPosition;
        mLastSelectedItemId = lastSelectedItemId;
        setHasStableIds(true);
    }

    /**
     * used to differentiate the different types of view adapters
     *
     * @param adapterType: see VIEW_ADAPTER_TYPE_XXX constants in this class
     */
    public void setViewAdapterType(int adapterType) {
        this.mViewAdapterType = adapterType;
    }

    @Override
    public DefaultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DefaultViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.common_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final DefaultViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        //bind the cursor data to the ui views
        //TODO see why this can be happened https://github.com/mkeresztes/AndiCar/issues/3
        if (mCursor == null || mCursor.isClosed()) {
            return;
        }

        cursorViewBinder(holder, position);

        holder.mView.setSelected(false);

        if (mLastSelectedItemId > 0) {
            if (mLastSelectedItemId == holder.mRecordID) {
                holder.mView.setSelected(true);
                lastSelectedView = holder.mView;
                if (isTwoPane) {
                    showDetailView(holder.mView.getContext(), false, false);
                }
            }
        }
        else {
            holder.mView.setSelected(true);
            lastSelectedView = holder.mView;
            mLastSelectedItemId = holder.mRecordID;
            if (isTwoPane) {
                showDetailView(holder.mView.getContext(), false, false);
            }
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (lastSelectedView != null) {
                    lastSelectedView.setSelected(false);
                }
                mLastSelectedItemId = holder.mRecordID;
                lastSelectedView = v;
                v.setSelected(true);
                mSelectedPosition = position;
                if (mParentActivity != null && mParentActivity instanceof AdapterItemSelectedListener) {
                    ((AdapterItemSelectedListener) mParentActivity).onAdapterItemSelectedListener(mSelectedPosition, mLastSelectedItemId);
                }

                BaseViewAdapter.this.showDetailView(v.getContext(), false, false);
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mViewAdapterType != VIEW_ADAPTER_TYPE_TODO) {
                    return false;
                }

                //@formatter:off
                String sql =
                        " SELECT * " +
                        " FROM " + DBAdapter.TABLE_NAME_TASK +
                        " WHERE " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TASK, DBAdapter.COL_NAME_GEN_ROWID) +
                                " IN ( " +
                                    "SELECT " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__TASK_ID) +
                                    " FROM " + DBAdapter.TABLE_NAME_TODO +
                                    " WHERE " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_GEN_ROWID) + " = ?)";
                //@formatter:on
                String argValues[] = {Long.toString(holder.mRecordID)};
                DBAdapter dbAdapter = new DBAdapter(v.getContext());
                Cursor taskCursor = dbAdapter.query(sql, argValues);
                int notificationTrigger = -1;
                String minutesOrDays;

                if (!taskCursor.moveToFirst()) {
                    Toast.makeText(v.getContext(), "Unknown error", Toast.LENGTH_LONG).show();
                    try {
                        taskCursor.close();
                        dbAdapter.close();
                    }
                    catch (Exception ignored) {
                    }
                    return true;
                }

                if (taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_TIME)
                        || taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_BOTH)) {
                    notificationTrigger = ToDoNotificationJob.TRIGGERED_BY_TIME;
                }
                if (taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE)
                        || taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_BOTH)) {
                    notificationTrigger = ToDoNotificationJob.TRIGGERED_BY_MILEAGE;
                }

                if (taskCursor.getInt(DBAdapter.COL_POS_TASK__TIMEFREQUENCYTYPE) == TaskEditFragment.TASK_TIMEFREQUENCYTYPE_DAILY) {
                    minutesOrDays = AndiCar.getAppResources().getString(R.string.gen_minutes);
                }
                else {
                    minutesOrDays = AndiCar.getAppResources().getString(R.string.gen_days);
                }

                long carId = -1;
                Cursor c = dbAdapter.execSelectSql(
                        "SELECT " + DBAdapter.COL_NAME_TODO__CAR_ID +
                                " FROM " + DBAdapter.TABLE_NAME_TODO +
                                " WHERE " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_GEN_ROWID) + " = ?", new String[]{Long.toString(holder.mRecordID)});
                if (c.moveToFirst()) {
                    carId = c.getLong(0);
                }
                try {
                    c.close();
                }
                catch (Exception ignored) {
                }

                Intent i = new Intent(v.getContext(), ToDoNotificationDialogActivity.class);
                i.putExtra(ToDoNotificationJob.TODO_ID_KEY, holder.mRecordID);
                i.putExtra(ToDoNotificationDialogActivity.TRIGGERED_BY_KEY, notificationTrigger);
                i.putExtra(ToDoNotificationDialogActivity.CAR_UOM_CODE_KEY, dbAdapter.getUOMCode(dbAdapter.getCarUOMLengthID(carId)));
                i.putExtra(ToDoNotificationDialogActivity.MINUTES_OR_DAYS_KEY, minutesOrDays);
                i.putExtra(ToDoNotificationDialogActivity.STARTED_FROM_NOTIFICATION_KEY, false);

                v.getContext().startActivity(i);

                try {
                    taskCursor.close();
                    dbAdapter.close();
                }
                catch (Exception ignored) {
                }
                return true;
            }
        });
    }

    protected abstract void cursorViewBinder(DefaultViewHolder holder, @SuppressLint("RecyclerView") int position);

    @SuppressWarnings("ConstantConditions")
    public void showDetailView(Context ctx, boolean forceActivity, boolean newRecord) {
        if (newRecord) {
            mLastSelectedItemId = 0;
            mSelectedPosition = 0;
        }

        if (isTwoPane && !forceActivity) {
            Fragment fragment = null;

            Bundle arguments = new Bundle();
            if (newRecord) {
                arguments.putString(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_NEW);
                arguments.putLong(BaseEditFragment.RECORD_ID_KEY, -1L);
            }
            else {
                arguments.putString(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_VIEW);
                arguments.putLong(BaseEditFragment.RECORD_ID_KEY, mLastSelectedItemId);
            }
            arguments.putBoolean(BaseEditFragment.DETAIL_PANEL_HIDE_FILL_VIEWS_KEY, true);

            if (this instanceof MileageViewAdapter) {
                fragment = new MileageEditFragment();
            }
            else if (this instanceof RefuelViewAdapter) {
                fragment = new RefuelEditFragment();
            }
            else if (this instanceof ExpenseViewAdapter) {
                fragment = new ExpenseEditFragment();
            }
            else if (this instanceof GPSTrackViewAdapter) {
                if (newRecord) {
                    fragment = new GPSTrackControllerFragment();
                }
                else {
                    fragment = new GPSTrackEditFragment();
                }
            }
            else if (this instanceof ToDoViewAdapter) {
                fragment = new ToDoViewFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_CAR) {
                fragment = new CarEditFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_DRIVER) {
                fragment = new DriverEditFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_UOM) {
                fragment = new UOMEditFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_UOM_CONVERSION) {
                fragment = new UOMConversionEditFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_EXPENSE_CATEGORY) {
                arguments.putBoolean(BaseEditFragment.IS_FUEL_KEY, false);
                fragment = new ExpenseFuelCategoryEditFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_FUEL_TYPE) {
                arguments.putBoolean(BaseEditFragment.IS_FUEL_KEY, true);
                fragment = new ExpenseFuelCategoryEditFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_EXPENSE_TYPE) {
                fragment = new ExpenseTypeEditFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_REIMBURSEMENT_RATE) {
                fragment = new ReimbursementRateEditFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_CURRENCY) {
                fragment = new CurrencyEditFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_CURRENCY_RATE) {
                fragment = new CurrencyRateEditFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_BPARTNER) {
                fragment = new BPartnerEditFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_TASK_TYPE) {
                fragment = new TaskTypeEditFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_TASK) {
                fragment = new TaskEditFragment();
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_BT_CAR_LINK) {
                fragment = new BTCarLinkFragment();
            } else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_TAG) {
                fragment = new TagEditFragment();
            }
            else {
                Utils.showReportableErrorDialog(ctx, ctx.getString(R.string.gen_error),
                        String.format(ctx.getString(R.string.error_113), this.getClass().toString()), null, false);
            }

            if (fragment != null) {
                fragment.setArguments(arguments);
                mParentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit();
            }
        }
        else {
            Intent intent = new Intent(ctx, CommonDetailActivity.class);
            if (this instanceof MileageViewAdapter) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_MILEAGE);
            }
            else if (this instanceof RefuelViewAdapter) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_REFUEL);
            }
            else if (this instanceof ExpenseViewAdapter) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_EXPENSE);
            }
            else if (this instanceof GPSTrackViewAdapter) {
                if (newRecord) {
                    intent = new Intent(ctx, GPSTrackControllerDialogActivity.class);
                }
                else {
                    intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_GPS_TRACK);
                }
            }
            else if (this instanceof ToDoViewAdapter) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_TODO);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_CAR) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_CAR);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_DRIVER) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_DRIVER);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_UOM) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_UOM);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_UOM_CONVERSION) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_UOM_CONVERSION);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_EXPENSE_CATEGORY) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_EXPENSE_CATEGORY);
                intent.putExtra(BaseEditFragment.IS_FUEL_KEY, false);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_FUEL_TYPE) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_FUEL_TYPE);
                intent.putExtra(BaseEditFragment.IS_FUEL_KEY, true);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_EXPENSE_TYPE) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_EXPENSE_TYPE);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_REIMBURSEMENT_RATE) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_REIMBURSEMENT_RATE);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_CURRENCY) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_CURRENCY);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_CURRENCY_RATE) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_CURRENCY_RATE);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_BPARTNER) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_BPARTNER);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_TASK_TYPE) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_TASK_TYPE);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_TASK) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_TASK);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_BT_CAR_LINK) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_BT_CAR_LINK);
            }
            else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_TAG) {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_TAG);
            }
            else {
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, 0);
            }

            if (newRecord) {
                intent.putExtra(BaseEditFragment.RECORD_ID_KEY, -1L);
            }
            else {
                    intent.putExtra(BaseEditFragment.RECORD_ID_KEY, mLastSelectedItemId);
            }

            ctx.startActivity(intent);
        }
    }

    @Override
    public long getItemId(int position) {
        long retVal = -1;
        if (mCursor.isClosed()) {
            return retVal;
        }

        int savedPosition = mCursor.getPosition();
        if (mCursor.moveToPosition(position)) {
            retVal = mCursor.getLong(0);
        }
        else if (mCursor.moveToLast()) {
            retVal = mCursor.getLong(0);
        }

        mCursor.moveToPosition(savedPosition);
        return retVal;
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        //-3 for position the last clicked item below to the top of the view
        recyclerView.scrollToPosition(mSelectedPosition >= 3 ? mSelectedPosition - 3 : 0);
    }

    public int getPosition() {
        return mSelectedPosition;
    }

    /**
     * deletes the current selected item
     *
     * @return -1 on success, an error code on error
     */
    public int deleteItem() {
        String tableName = "";
        if (this instanceof MileageViewAdapter) {
            tableName = DBAdapter.TABLE_NAME_MILEAGE;
        }
        else if (this instanceof RefuelViewAdapter) {
            tableName = DBAdapter.TABLE_NAME_REFUEL;
        }
        else if (this instanceof ExpenseViewAdapter) {
            tableName = DBAdapter.TABLE_NAME_EXPENSE;
        }
        else if (this instanceof GPSTrackViewAdapter) {
            tableName = DBAdapter.TABLE_NAME_GPSTRACK;
        }
        else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_CAR) {
            tableName = DBAdapter.TABLE_NAME_CAR;
        }
        else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_DRIVER) {
            tableName = DBAdapter.TABLE_NAME_DRIVER;
        }
        else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_UOM) {
            tableName = DBAdapter.TABLE_NAME_UOM;
        }
        else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_UOM_CONVERSION) {
            tableName = DBAdapter.TABLE_NAME_UOMCONVERSION;
        }
        else if (this instanceof SettingsDefaultViewAdapter &&
                (mViewAdapterType == VIEW_ADAPTER_TYPE_EXPENSE_CATEGORY || mViewAdapterType == VIEW_ADAPTER_TYPE_FUEL_TYPE)) {
            tableName = DBAdapter.TABLE_NAME_EXPENSECATEGORY;
        }
        else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_EXPENSE_TYPE) {
            tableName = DBAdapter.TABLE_NAME_EXPENSETYPE;
        }
        else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_REIMBURSEMENT_RATE) {
            tableName = DBAdapter.TABLE_NAME_REIMBURSEMENT_CAR_RATES;
        }
        else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_CURRENCY) {
            tableName = DBAdapter.TABLE_NAME_CURRENCY;
        }
        else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_CURRENCY_RATE) {
            tableName = DBAdapter.TABLE_NAME_CURRENCYRATE;
        }
        else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_BPARTNER) {
            tableName = DBAdapter.TABLE_NAME_BPARTNER;
        }
        else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_TASK_TYPE) {
            tableName = DBAdapter.TABLE_NAME_TASKTYPE;
        }
        else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_BT_CAR_LINK) {
            tableName = DBAdapter.TABLE_NAME_BTDEVICE_CAR;
        }
        else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_TASK) {
            tableName = DBAdapter.TABLE_NAME_TASK;
        }
        else if (this instanceof SettingsDefaultViewAdapter && mViewAdapterType == VIEW_ADAPTER_TYPE_TAG) {
            tableName = DBAdapter.TABLE_NAME_TAG;
        }

        if (tableName.trim().length() > 0) {
            DBAdapter db = new DBAdapter(mParentActivity);
            return db.deleteRecord(tableName, mLastSelectedItemId);
        }
        else {
            return -1;
        }
    }


    public interface AdapterItemSelectedListener {
        void onAdapterItemSelectedListener(int selectedPosition, long selectedItemId);
    }

    class DefaultViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mFirstLine;
        final TextView mSecondLine;
        final TextView mThirdLine;
        long mRecordID;

        DefaultViewHolder(View view) {
            super(view);
            mView = view;
            mFirstLine = view.findViewById(R.id.firstLine);
            mSecondLine = view.findViewById(R.id.secondLine);
            mThirdLine = view.findViewById(R.id.thirdLine);
            mRecordID = -1;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mSecondLine.getText() + " '" + mThirdLine.getText() + "'";
        }
    }
}
