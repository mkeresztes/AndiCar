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
package andicar.n.activity.dialogs;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.andicar2.activity.R;

import java.math.BigDecimal;
import java.util.Calendar;

import andicar.n.persistence.DBAdapter;
import andicar.n.persistence.DBReportAdapter;
import andicar.n.service.JobStarter;
import andicar.n.service.ToDoManagementService;
import andicar.n.service.ToDoNotificationJob;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * @author Miklos Keresztes
 */
public class ToDoNotificationDialogActivity extends AppCompatActivity {
    public static final String TRIGGERED_BY_KEY = "TriggeredBy";
    public static final String CAR_UOM_CODE_KEY = "CarUOMCode";
    public static final String MINUTES_OR_DAYS_KEY = "MinutesOrDays";
    public static final String STARTED_FROM_NOTIFICATION_KEY = "StartedFromNotification";

    private boolean isOKPressed = false;
    private boolean isTodoOK = true;
    private long mToDoID;
    private long mTaskID;
    private int triggeredBy = -1;
    private long carCurrentOdometer;
    private String minutesOrDays;

    @SuppressWarnings("deprecation")
    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);

        //prevent keyboard from automatic pop up
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setFinishOnTouchOutside(false);

        setContentView(R.layout.dialog_todo_notification);

        setTitle("AndiCar " + getString(R.string.todo_alert_title));
        TextView tvText1 = findViewById(R.id.tvText1);
        TextView tvText2 = findViewById(R.id.tvText2);
        TextView tvText3 = findViewById(R.id.tvText3);
        TextView tvText4 = findViewById(R.id.tvText4);
        EditText etPostpone = findViewById(R.id.etPostpone);
        TextView tvPostponeUOM = findViewById(R.id.tvPostponeUOM);
//		findViewById(R.id.fakeFocus).requestFocus();
        LinearLayout llActionZone = findViewById(R.id.llActionZone);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        Bundle mBundleExtras = getIntent().getExtras();
        if (mBundleExtras == null) {
            Utils.showReportableErrorDialog(this, getString(R.string.error_sorry), "ToDo notification dialog - no extras", null);
            return;
        }
        if (mBundleExtras.getBoolean(STARTED_FROM_NOTIFICATION_KEY, true)) {
            btnCancel.setVisibility(View.GONE);
        }
        else {
            View llPostponeZone = findViewById(R.id.llPostponeZone);
            llPostponeZone.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ToDoNotificationDialogActivity.this.finish();
                }
            });
        }

        mToDoID = mBundleExtras.getLong(ToDoNotificationJob.TODO_ID_KEY);
        triggeredBy = mBundleExtras.getInt(TRIGGERED_BY_KEY);
        String carUOMCode = mBundleExtras.getString(CAR_UOM_CODE_KEY);
        minutesOrDays = mBundleExtras.getString(MINUTES_OR_DAYS_KEY);

        Bundle whereConditions = new Bundle();
        whereConditions.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_GEN_ROWID) + "= ",
                Long.toString(mToDoID));

        DBReportAdapter reportDb = new DBReportAdapter(this, DBReportAdapter.TODO_LIST_SELECT_NAME, whereConditions);
        Cursor todoReportCursor = reportDb.fetchReport(1);

        if (todoReportCursor != null && todoReportCursor.moveToFirst()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tvText2.setTextColor(getResources().getColor(R.color.todo_overdue_text_color, getApplicationContext().getTheme()));
            }
            else {
                tvText2.setTextColor(getResources().getColor(R.color.todo_overdue_text_color));
            }
            long todoDueMileage = todoReportCursor.getLong(5);
            carCurrentOdometer = todoReportCursor.getLong(12);
            long todoDueDateSec = todoReportCursor.getLong(4);

            if (triggeredBy == ToDoNotificationJob.TRIGGERED_BY_MILEAGE) {
                tvPostponeUOM.setText(carUOMCode);
                etPostpone.setText("100");
                if (todoDueMileage - carCurrentOdometer > 0) {
                    tvText2.setText(getString(R.string.todo_mileage_left)
                            + Utils.numberToString(todoDueMileage - carCurrentOdometer, true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH)
                            + " " + carUOMCode);
                }
                else {
                    tvText2.setText(getString(R.string.gen_current_index)
                            + Utils.numberToString(carCurrentOdometer, true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) + " "
                            + carUOMCode);
                }
            }
            else {
                tvPostponeUOM.setText(minutesOrDays);
                long currentSecs = System.currentTimeMillis() / 1000;
                if (minutesOrDays.equals(getString(R.string.gen_minutes))) {
                    etPostpone.setText("30");
                    tvText2.setText(getString(R.string.todo_minutes_left)
                            + Utils.numberToString(((todoDueDateSec - currentSecs) / 60) + 1, true, ConstantValues.DECIMALS_LENGTH,
                            ConstantValues.ROUNDING_MODE_LENGTH));
                }
                else {
                    etPostpone.setText("1");
                    tvText2.setText(getString(R.string.todo_days_left)
                            + Utils.numberToString(((todoDueDateSec - currentSecs) / 3600 / 24) + 1, true, ConstantValues.DECIMALS_LENGTH,
                            ConstantValues.ROUNDING_MODE_LENGTH));
                }
                if (todoDueDateSec - currentSecs < 0) {
                    tvText2.setText("");
                }
            }
            if (tvText2.getText().length() == 0) {
                tvText2.setVisibility(View.GONE);
            }

            String dataString = todoReportCursor.getString(1);
            mTaskID = todoReportCursor.getLong(11);
            if (dataString.contains("[#5]")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvText1.setTextColor(getResources().getColor(R.color.todo_overdue_text_color, getApplicationContext().getTheme()));
                }
                else {
                    tvText1.setTextColor(getResources().getColor(R.color.todo_overdue_text_color));
                }
            }
            else if (dataString.contains("[#15]")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvText1.setTextColor(getResources().getColor(R.color.todo_done_text_color, getApplicationContext().getTheme()));
                }
                else {
                    tvText1.setTextColor(getResources().getColor(R.color.todo_done_text_color));
                }
                llActionZone.setVisibility(View.GONE);
                btnSave.setText(R.string.gen_ok);
            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvText1.setTextColor(getResources().getColor(R.color.todo_blue_text_color, getApplicationContext().getTheme()));
                }
                else {
                    tvText1.setTextColor(getResources().getColor(R.color.todo_blue_text_color));
                }
            }

            String text = dataString
                    .replace("[#1]", getString(R.string.gen_type_label) + " ")
                    .replace("[#2]", "; " + getString(R.string.gen_todo) + ": ")
                    .replace("[#3]", "\n" + getString(R.string.gen_car_label))
                    .replace("[#4]", getString(R.string.todo_status_label))
                    .replace("[#5]", getString(R.string.todo_overdue_label))
                    .replace("[#6]", getString(R.string.todo_scheduled_label))
                    .replace("[#15]", getString(R.string.todo_done_label));

            tvText1.setText(text);

            text = todoReportCursor.getString(2);
            text = text
                    .replace("[#7]", getString(R.string.todo_scheduled_date_label))
                    .replace(
                            "[#8]",
                            DateFormat.getDateFormat(this).format(todoReportCursor.getLong(4) * 1000) + " "
                                    + DateFormat.getTimeFormat(this).format(todoReportCursor.getLong(4) * 1000))
                    .replace("[#9]", getString(R.string.gen_or))
                    .replace("[#10]", getString(R.string.todo_scheduled_mileage_label))
                    .replace("[#11]",
                            Utils.numberToString(todoReportCursor.getDouble(5), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH))
                    .replace("[#12]", getString(R.string.todo_mileage)).replace("([#13] [#14])", "");

            tvText3.setText(text);
            text = todoReportCursor.getString(todoReportCursor.getColumnIndex(DBReportAdapter.THIRD_LINE_LIST_NAME));
            if (text != null && text.trim().length() > 0) {
                tvText4.setText(text);
            }
            else {
                tvText4.setVisibility(View.GONE);
            }

            isTodoOK = true;
        }
        else {
            llActionZone.setVisibility(View.GONE);
            btnSave.setText(R.string.gen_ok);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tvText1.setTextColor(getResources().getColor(R.color.todo_overdue_text_color, getApplicationContext().getTheme()));
            }
            else {
                tvText1.setTextColor(getResources().getColor(R.color.todo_overdue_text_color));
            }
            tvText1.setText(R.string.error_061);
            isTodoOK = false;
        }
        if (todoReportCursor != null) {
            todoReportCursor.close();
        }
        reportDb.close();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ToDoNotificationDialogActivity.this.saveData()) {
                    ToDoNotificationDialogActivity.this.finish();
                }
            }
        });
    }

    @SuppressLint("WrongConstant")
    private boolean saveData() {
        isOKPressed = true;
        if (!isTodoOK) {
            finish();
            return false;
        }
        CheckBox ckIsDone = findViewById(R.id.ckIsDone);
        EditText etPostpone = findViewById(R.id.etPostpone);
        BigDecimal postPoneFor;

        DBAdapter mDbAdapter = new DBAdapter(getApplication());

        ContentValues cvData = new ContentValues();
        if (ckIsDone.isChecked()) {
            boolean isRecurrentTask = false;
            if (mTaskID > 0) {
                Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_TASK, DBAdapter.COL_LIST_TASK_TABLE, mTaskID);
                isRecurrentTask = c != null && c.getString(DBAdapter.COL_POS_TASK__ISRECURRENT).equals("Y");
                try {
                    if (c != null) {
                        c.close();
                    }
                }
                catch (Exception ignored) {
                }
            }
            if (!isRecurrentTask) { //if not recurrent => delete the to-do & task
                mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_TODO, mToDoID);
                mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_TASK, mTaskID);
                return true;
            }

            cvData.put(DBAdapter.COL_NAME_TODO__ISDONE, "Y");
        }
        else {
            String strPostPoneFor = etPostpone.getText().toString();
            if (strPostPoneFor.length() > 0) {
                try {
                    postPoneFor = new BigDecimal(strPostPoneFor);
                }
                catch (NumberFormatException e) {
                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.gen_invalid_number), Toast.LENGTH_SHORT);
                    toast.show();
                    etPostpone.requestFocus();
                    return false;
                }
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.gen_fill_mandatory), Toast.LENGTH_SHORT);
                toast.show();
                etPostpone.requestFocus();
                return false;
            }
            if (triggeredBy == ToDoNotificationJob.TRIGGERED_BY_MILEAGE) {
                cvData.put(DBAdapter.COL_NAME_TODO__NOTIFICATIONMILEAGE, carCurrentOdometer + postPoneFor.longValue());
            }
            else {
                Calendar cal = Calendar.getInstance();
                if (minutesOrDays.equals(getString(R.string.gen_minutes))) {
                    cal.add(Calendar.MINUTE, postPoneFor.intValue());
                }
                else {
                    cal.add(Calendar.DAY_OF_YEAR, postPoneFor.intValue());
                }
                cvData.put(DBAdapter.COL_NAME_TODO__NOTIFICATIONDATE, cal.getTimeInMillis() / 1000);
            }
        }
        mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_TODO, mToDoID, cvData);
        if (ckIsDone.isChecked()) {
            //if this to-do is done generate the next entry
            Intent intent = new Intent(this, ToDoManagementService.class);
            intent.putExtra(ToDoManagementService.TASK_ID_KEY, mTaskID);
            this.startService(intent);
        }
        else {
            Utils.setToDoNextRun(this);
        }

        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if back key used show again the notification
        if (!isOKPressed) {
//            Intent i = new Intent(this, ToDoNotificationService.class);
//            i.putExtra(ToDoManagementService.SET_JUST_NEXT_RUN_KEY, false);
//            i.putExtra(ToDoNotificationJob.TODO_ID_KEY, mToDoID);
//            this.startService(i);
            Bundle serviceParams = new Bundle();
            serviceParams.putLong(ToDoNotificationJob.TODO_ID_KEY, mToDoID);
            JobStarter.startServicesUsingFBJobDispatcher(this, JobStarter.SERVICE_STARTER_START_TODO_NOTIFICATION_SERVICE, serviceParams);
        }
    }
}
