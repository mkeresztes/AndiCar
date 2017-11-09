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
package andicar.n.activity.miscellaneous;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import org.andicar2.activity.R;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import andicar.n.persistence.DBAdapter;
import andicar.n.service.ToDoManagementService;
import andicar.n.utils.Utils;

/**
 * @author miki
 */
@SuppressWarnings("FieldCanBeLocal")
public class TaskCarLinkActivity extends AppCompatActivity {
    public static final String IS_TIMING_ENABLED = "isTimingEnabled";
    public static final String IS_MILEAGE_ENABLED = "isMileageEnabled";
    public static final String IS_RECURRENT = "isRecurrent";
    public static final String IS_DIFFERENT_STARTING_TIME = "isDifferentStartingTime";
    public static final String STARTING_MILEAGE = "startingMileage";
    public static final String STARTING_TIME_IN_MILLIS = "startingTime";

    private final Calendar mDateTimeCalendar = Calendar.getInstance();
    private long mTaskID;
    private long mCarID;
    private long mRowId;
    private long mlFirstRunDateInMillis;
    private String mStartingMileageText;
    private boolean isTimingEnabled;
    private boolean isMileageEnabled;
    private boolean isRecurrent;
    private boolean isDifferentStartingTime;
    private DBAdapter mDbAdapter;
    private TextView tvDateTimeValue = null;
    private Spinner spnCar;

    private EditText etIndexStart = null;

    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);

        mDbAdapter = new DBAdapter(getApplicationContext());

        if (getIntent().getExtras() == null) {
            Utils.showReportableErrorDialog(this, getString(R.string.error_sorry), "Task-car link no extras", null);
            return;
        }
        mTaskID = getIntent().getExtras().getLong(DBAdapter.COL_NAME_TASK_CAR__TASK_ID, -1);
        mRowId = getIntent().getExtras().getLong(DBAdapter.COL_NAME_GEN_ROWID, -1);

        isTimingEnabled = getIntent().getExtras().getBoolean(IS_TIMING_ENABLED);
        isMileageEnabled = getIntent().getExtras().getBoolean(IS_MILEAGE_ENABLED);
        isRecurrent = getIntent().getExtras().getBoolean(IS_RECURRENT);
        mStartingMileageText = getIntent().getExtras().getString(STARTING_MILEAGE);
        mlFirstRunDateInMillis = getIntent().getExtras().getLong(STARTING_TIME_IN_MILLIS);
        isDifferentStartingTime = getIntent().getExtras().getBoolean(IS_DIFFERENT_STARTING_TIME);

        setFinishOnTouchOutside(false);

        setContentView(R.layout.activity_task_car_link);

        tvDateTimeValue = findViewById(R.id.tvDateTimeValue);
        tvDateTimeValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection WrongConstant
                SwitchDateTimeDialogFragment dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                        getString(R.string.gen_set_date),
                        getString(R.string.gen_ok),
                        getString(R.string.gen_cancel)
                );

                // Assign values
                dateTimeDialogFragment.startAtCalendarView();
                // Assign each element, default element is the current moment
                dateTimeDialogFragment.setDefaultHourOfDay(mDateTimeCalendar.get(Calendar.HOUR_OF_DAY));
                dateTimeDialogFragment.setDefaultMinute(mDateTimeCalendar.get(Calendar.MINUTE));
                dateTimeDialogFragment.setDefaultDay(mDateTimeCalendar.get(Calendar.DAY_OF_MONTH));
                dateTimeDialogFragment.setDefaultMonth(mDateTimeCalendar.get(Calendar.MONTH));
                dateTimeDialogFragment.setDefaultYear(mDateTimeCalendar.get(Calendar.YEAR));

                // Define new day and month format
                try {
                    dateTimeDialogFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("dd MMMM", Locale.getDefault()));
                }
                catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
                    Log.e("SwitchDateTimeDialog", e.getMessage());
                }

                // Set listener
                dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Date date) {
                        // Date is get on positive button click
                        // Do something
                        mlFirstRunDateInMillis = date.getTime();
                        mDateTimeCalendar.setTimeInMillis(mlFirstRunDateInMillis);
                        showDateTime();
                    }

                    @Override
                    public void onNegativeButtonClick(Date date) {
                        // Date is get on negative button click
                    }
                });

                // Show
                dateTimeDialogFragment.show(TaskCarLinkActivity.this.getSupportFragmentManager(), "dialog_time");

            }
        });
        etIndexStart = findViewById(R.id.etIndexStart);

        LinearLayout llStartingDateZone = findViewById(R.id.llStartingDateZone);
        LinearLayout llStartingMileageZone = findViewById(R.id.llStartingMileageZone);

        if (!isTimingEnabled || !isRecurrent || !isDifferentStartingTime) {
            llStartingDateZone.setVisibility(View.GONE);
        }

        if (!isMileageEnabled || !isRecurrent) {
            llStartingMileageZone.setVisibility(View.GONE);
        }

        if (mRowId == -1) { //new record
//            mlFirstRunDateInMillis = System.currentTimeMillis();
            mDateTimeCalendar.setTimeInMillis(mlFirstRunDateInMillis);
            etIndexStart.setText(mStartingMileageText);
            etIndexStart.setSelection(etIndexStart.getText().length()); //place the cursor at end of the text
            mCarID = -1;
        }
        else {
            Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_TASK_CAR, DBAdapter.COL_LIST_TASK_CAR_TABLE, mRowId);
            if (c != null) {
                if (c.moveToFirst()) {
                    mCarID = c.getLong(DBAdapter.COL_POS_TASK_CAR__CAR_ID);
                    if (c.getString(DBAdapter.COL_POS_TASK_CAR__FIRSTRUN_DATE) != null) {
                        mlFirstRunDateInMillis = c.getLong(DBAdapter.COL_POS_TASK_CAR__FIRSTRUN_DATE) * 1000;
                    }
                    else {
                        mlFirstRunDateInMillis = System.currentTimeMillis();
                    }
                    mDateTimeCalendar.setTimeInMillis(mlFirstRunDateInMillis);

                    if (c.getString(DBAdapter.COL_POS_TASK_CAR__FIRSTRUN_MILEAGE) != null) {
                        etIndexStart.setText(c.getString(DBAdapter.COL_POS_TASK_CAR__FIRSTRUN_MILEAGE));
                    }
                    else {
                        etIndexStart.setText("0");
                    }
                    etIndexStart.setSelection(etIndexStart.getText().length()); //place the cursor at end of the text
                }
                c.close();
            }
        }
        showDateTime();

        spnCar = findViewById(R.id.spnCar);
        String mLinkDialogCarSelectCondition =
                DBAdapter.WHERE_CONDITION_ISACTIVE +
                        " AND " + DBAdapter.COL_NAME_GEN_ROWID +
                        " NOT IN (SELECT " + DBAdapter.COL_NAME_TASK_CAR__CAR_ID +
                        " FROM " + DBAdapter.TABLE_NAME_TASK_CAR + " " +
                        " WHERE " + DBAdapter.COL_NAME_TASK_CAR__TASK_ID + " = " + Long.toString(mTaskID);
        if (mCarID > -1) //include the current car (edit an existing record)
        {
            mLinkDialogCarSelectCondition = mLinkDialogCarSelectCondition +
                    " AND " + DBAdapter.COL_NAME_TASK_CAR__CAR_ID + " <> " + mCarID;
        }
        mLinkDialogCarSelectCondition = mLinkDialogCarSelectCondition + ")";

        if (!isRecurrent && isMileageEnabled && !isTimingEnabled) { //select only cars with current mileage < due mileage
            mLinkDialogCarSelectCondition = mLinkDialogCarSelectCondition +
                    " AND " + DBAdapter.COL_NAME_CAR__INDEXCURRENT + " < " + mStartingMileageText;
        }

        Utils.initSpinner(mDbAdapter, spnCar, DBAdapter.TABLE_NAME_CAR, mLinkDialogCarSelectCondition, mCarID, false);
        if (mRowId == -1) {
            spnCar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    mCarID = mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_CAR, adapterView.getAdapter().getItem(position).toString());
                    try {
                        BigDecimal currentIndex = mDbAdapter.getCarCurrentIndex(mCarID);
                        if (currentIndex == null) {
                            currentIndex = BigDecimal.ZERO;
                        }
                        BigDecimal mileage = new BigDecimal(mStartingMileageText);
                        int i = currentIndex.intValue();
                        int m = mileage.intValue();
                        etIndexStart.setText(Integer.toString(((i / m) + 1) * m));
                    }
                    catch (Exception e) {
                        etIndexStart.setText("");
                    }
                    etIndexStart.setSelection(etIndexStart.getText().length()); //place the cursor at end of the text
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
        else {
            spnCar.setEnabled(false);
        }


        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TaskCarLinkActivity.this.finish();
            }
        });

        Button btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int notFilledViewID = Utils.checkMandatoryFields(TaskCarLinkActivity.this.findViewById(R.id.vgRoot));
                if (notFilledViewID > -1) {
                    TaskCarLinkActivity.this.findViewById(notFilledViewID).setBackgroundResource(R.drawable.ui_mandatory_border_edittext);
                    Toast.makeText(TaskCarLinkActivity.this.getApplicationContext(), TaskCarLinkActivity.this.getString(R.string.gen_fill_mandatory), Toast.LENGTH_LONG).show();
                    return;
                }
                if (TaskCarLinkActivity.this.saveData()) {
                    TaskCarLinkActivity.this.finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbAdapter.close();
    }

    @SuppressLint("SetTextI18n")
    private void showDateTime() {
        tvDateTimeValue.setText(DateFormat.getDateFormat(getApplicationContext()).format(mDateTimeCalendar.getTime()) + " "
                + DateFormat.getTimeFormat(getApplicationContext()).format(mDateTimeCalendar.getTime()));
    }

    private boolean saveData() {
        ContentValues data = new ContentValues();
        data.put(DBAdapter.COL_NAME_GEN_NAME, mDbAdapter.getNameById(DBAdapter.TABLE_NAME_TASK, mTaskID) + " <-> " + mDbAdapter.getCarName(mCarID));
        data.put(DBAdapter.COL_NAME_GEN_ISACTIVE, "Y");
        data.put(DBAdapter.COL_NAME_TASK_CAR__TASK_ID, mTaskID);
        data.put(DBAdapter.COL_NAME_TASK_CAR__CAR_ID, mCarID);
        if (isTimingEnabled) {
            data.put(DBAdapter.COL_NAME_TASK_CAR__FIRSTRUN_DATE, mlFirstRunDateInMillis / 1000);
        }
        else {
            data.put(DBAdapter.COL_NAME_TASK_CAR__FIRSTRUN_DATE, (Long) null);
        }

        if (isMileageEnabled && isRecurrent) {
            data.put(DBAdapter.COL_NAME_TASK_CAR__FIRSTRUN_MILEAGE, etIndexStart.getText().toString());
        }
        else {
            data.put(DBAdapter.COL_NAME_TASK_CAR__FIRSTRUN_MILEAGE, (Long) null);
        }

        if (mRowId == -1) { //new link
            int retVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_TASK_CAR, data)).intValue();
            if (retVal < 0) {
                if (retVal == -1) {
                    Utils.showReportableErrorDialog(getApplicationContext(), getString(R.string.error_sorry), mDbAdapter.mErrorMessage, mDbAdapter.mException);
                }
                else {
                    Utils.showNotReportableErrorDialog(getApplicationContext(), getString(R.string.gen_error), getString(-1 * retVal));
                }
                return false;
            }
            else {
                Intent intent = new Intent(getApplicationContext(), ToDoManagementService.class);
                intent.putExtra(ToDoManagementService.TASK_ID_KEY, mTaskID);
                getApplicationContext().startService(intent);
                return true;
            }
        }
        else {
            long retVal = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_TASK_CAR, mRowId, data);
            if (retVal > -1) {
                String errorMessage;
                int errCode = -1 * ((Long) retVal).intValue();
                errorMessage = getString(errCode);
                Utils.showReportableErrorDialog(getApplicationContext(), getString(R.string.error_sorry), errorMessage, mDbAdapter.mException);
                return false;
            }
            else {
                Intent intent = new Intent(getApplicationContext(), ToDoManagementService.class);
                intent.putExtra(ToDoManagementService.TASK_ID_KEY, mTaskID);
                getApplicationContext().startService(intent);
                return true;
            }
        }
    }
}
