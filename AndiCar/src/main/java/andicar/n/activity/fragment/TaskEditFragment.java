package andicar.n.activity.fragment;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.andicar2.activity.R;

import java.util.Calendar;

import andicar.n.activity.miscellaneous.TaskCarLinkActivity;
import andicar.n.persistence.DB;
import andicar.n.persistence.DBAdapter;
import andicar.n.persistence.TaskCarLinkDataBinder;
import andicar.n.service.ToDoManagementService;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by miki on 01.03.2017.
 */

public class TaskEditFragment extends BaseEditFragment {

    public static final String TASK_SCHEDULED_FOR_TIME = "T";
    public static final String TASK_SCHEDULED_FOR_MILEAGE = "M";
    public static final String TASK_SCHEDULED_FOR_BOTH = "B";
    private static final int TASK_TIME_FREQUENCY_TYPE_ONETIME = 0;
    public static final int TASK_TIME_FREQUENCY_TYPE_DAILY = 1;
    public static final int TASK_TIME_FREQUENCY_TYPE_WEEKLY = 2;
    public static final int TASK_TIME_FREQUENCY_TYPE_MONTHLY = 3;
    public static final int TASK_TIME_FREQUENCY_TYPE_YEARLY = 4;
    private static final String TASK_CAR_LINK_ID_KEY = "TaskCarLinkId";
    private static final int TASK_CAR_LINK_REQUEST_CODE = 1;
    private EditText etFrequency = null;
    private EditText etTimeReminder = null;
    private EditText etMileage = null;
    private EditText etReminderMileage = null;
    private EditText etNoOfNextToDo = null;
    private CheckBox ckIsDifferentStartingTime = null;
    private CheckBox ckOnLastDay = null;

    private Spinner spnTaskType = null;
    private Spinner spnScheduleFrequency = null;
    private Spinner spnLastDayMonth = null;

    private TextView tvMileageLabelEvery = null;
    private TextView tvMileageLabelOn = null;
    private TextView tvFirstTimeRunExplanation = null;
    private TextView tvFirstMileageRunExplanation = null;
    private TextView tvOr = null;
    private TextView tvOrLastDay = null;
    private TextView tvStartingTimeLbl = null;
    private TextView tvTimeReminderUnitLbl = null;
    private TextView tvLinkedCarsHelp = null;

    private RadioButton rbOneTime = null;
    private RadioButton rbRecurrent = null;
    private RadioButton rbTimeDriven = null;
    private RadioButton rbMileageDriven = null;
    private RadioButton rbTimeAndMileageDriven = null;

    private LinearLayout llStartingTime = null;
    private LinearLayout llRecurrentTimeSettings = null;
    private LinearLayout llMileageZone = null;
    private LinearLayout llLinkedCarsZone = null;
    private LinearLayout llLastMonthDay = null;
    private LinearLayout llTimeReminder = null;
    private LinearLayout llToDoCountZone = null;

    private ListView lvLinkedCarsList = null;

    private long mTimeFrequencyTypeId = -1;
    private long mTaskTypeId;
    private long mLongClickId;

    private boolean isDiffStartingTime = true;
    private boolean isTimingEnabled = true;
    private boolean isMileageEnabled = true;
    private boolean isRecurrent = true;
    private boolean saveSuccess = true;
    private boolean isDeleteLinkedCarsOnSave = false;
    private boolean mOnLastDayOfMonth = false;

    private String mScheduledFor = TASK_SCHEDULED_FOR_BOTH;
    private String mLinkDialogCarSelectCondition = null;
    private String mFrequency = null;
    private String mTimeReminder = null;
    private String mMileage = null;
    private String mReminderMileage = null;
    private String mNoOfNextToDo = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            switch (mOperationType) {
                case BaseEditFragment.DETAIL_OPERATION_EDIT: {
                    loadDataFromDB();
                    break;
                }
                default: //new record
                    initDefaultValues();
                    break;
            }
        }
    }

    @SuppressLint("WrongConstant")
    private void loadDataFromDB() {
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_TASK, DBAdapter.COL_LIST_TASK_TABLE, mRowId);

        assert c != null;
        mName = c.getString(DBAdapter.COL_POS_GEN_NAME);
        mIsActive = c.getString(DBAdapter.COL_POS_GEN_ISACTIVE).equals("Y");
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        mTaskTypeId = c.getLong(DBAdapter.COL_POS_TASK__TASKTYPE_ID);

        isRecurrent = (c.getString(DBAdapter.COL_POS_TASK__ISRECURRENT).equals("Y"));
        mScheduledFor = c.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR);
        switch (mScheduledFor) {
            case TASK_SCHEDULED_FOR_BOTH:
                isMileageEnabled = true;
                isTimingEnabled = true;
                break;
            case TASK_SCHEDULED_FOR_TIME:
                isMileageEnabled = false;
                isTimingEnabled = true;
                break;
            case TASK_SCHEDULED_FOR_MILEAGE:
                isMileageEnabled = true;
                isTimingEnabled = false;
                break;
        }
        if (c.getString(DBAdapter.COL_POS_TASK__ISDIFFERENTSTARTINGTIME) != null) {
            isDiffStartingTime = c.getString(DBAdapter.COL_POS_TASK__ISDIFFERENTSTARTINGTIME).equals("Y");
            mFrequency = c.getString(DBAdapter.COL_POS_TASK__TIMEFREQUENCY);
        }
        mTimeFrequencyTypeId = c.getLong(DBAdapter.COL_POS_TASK__TIMEFREQUENCYTYPE);

        if (c.getString(DBAdapter.COL_POS_TASK__STARTINGTIME) != null) {
            mlDateTimeInMillis = c.getLong(DBAdapter.COL_POS_TASK__STARTINGTIME) * 1000;
        }
        else {
            mlDateTimeInMillis = System.currentTimeMillis();
        }

        if (isTimingEnabled) {
            initDateTimeFields();

            //YEAR == 1970 is a convention for last day of the month
            //noinspection WrongConstant
            if (mDateTimeCalendar.get(Calendar.YEAR) == 1970) {
                mOnLastDayOfMonth = true;
                //we need only the time part because we know the day (last of the month)
                isTimeOnly = true;
            }
            else {
                mOnLastDayOfMonth = false;
                isTimeOnly = false;
            }
            initDateTimeFields();
//            initDateTime(startingTimeInMilliseconds);
        }
        else {
            mlDateTimeInMillis = System.currentTimeMillis() + ConstantValues.ONE_DAY_IN_MILLISECONDS;
            initDateTimeFields();
//            initDateTime(System.currentTimeMillis() + StaticValues.ONE_DAY_IN_MILLISECONDS);
        }

        mTimeReminder = c.getString(DBAdapter.COL_POS_TASK__TIMEREMINDERSTART);

        mMileage = c.getString(DBAdapter.COL_POS_TASK__RUNMILEAGE);

        mReminderMileage = c.getString(DBAdapter.COL_POS_TASK__MILEAGEREMINDERSTART);

        mNoOfNextToDo = c.getString(DBAdapter.COL_POS_TASK__TODOCOUNT);

        c.close();

    }

    @Override
    public void initDefaultValues() {
        super.initDefaultValues();
        mFrequency = "1";
        mTimeReminder = "30";
        mReminderMileage = "300";
        mNoOfNextToDo = "3";
        mTimeFrequencyTypeId = TASK_TIME_FREQUENCY_TYPE_DAILY;
        isDiffStartingTime = true;
        isTimingEnabled = true;
        isMileageEnabled = true;
        isRecurrent = true;
        mOnLastDayOfMonth = false;
        mScheduledFor = TASK_SCHEDULED_FOR_BOTH;
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        spnTaskType = mRootView.findViewById(R.id.spnTaskType);
        spnTaskType.setTag(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG);
        spnTaskType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getTag() != null && adapterView.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                    adapterView.setTag(null);
                    return;
                }
                mTaskTypeId = mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_TASKTYPE, adapterView.getAdapter().getItem(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
//        ImageButton btnNewTaskType = (ImageButton) mRootView.findViewById(R.id.btnNewTaskType);
//        btnNewTaskType.setOnClickListener(onNewTaskTypeClickListener);
        ImageButton btnLinkCar = mRootView.findViewById(R.id.btnLinkCar);
        btnLinkCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor mPrefEditor = mPreferences.edit();
                isFinishAfterSave = false;
                TaskEditFragment.this.saveData();
                isFinishAfterSave = true;
                mPrefEditor.putLong(TASK_CAR_LINK_ID_KEY, -1);
                mPrefEditor.apply();
                if (!saveSuccess) {
                    return;
                }
                mLinkDialogCarSelectCondition = DBAdapter.WHERE_CONDITION_ISACTIVE + " AND " + DBAdapter.COL_NAME_GEN_ROWID + " NOT IN (SELECT "
                        + DBAdapter.COL_NAME_TASK_CAR__CAR_ID + " FROM " + DBAdapter.TABLE_NAME_TASK_CAR + " " + " WHERE "
                        + DBAdapter.COL_NAME_TASK_CAR__TASK_ID + " = " + Long.toString(mRowId) + ")";
                if (!isRecurrent && isMileageEnabled && !isTimingEnabled) { //select only cars with current mileage < due mileage
                    mLinkDialogCarSelectCondition = mLinkDialogCarSelectCondition + " AND " + DBAdapter.COL_NAME_CAR__INDEXCURRENT + " < "
                            + etMileage.getText().toString();
                }

                //check if unlinked cars exists.
                String checkSQL = "SELECT * " + " FROM " + DBAdapter.TABLE_NAME_CAR + " " + " WHERE 1 = 1 " + mLinkDialogCarSelectCondition;
                Cursor c = mDbAdapter.query(checkSQL, null);

                if (!c.moveToNext()) {//no record exist
                    c.close();
                    if (!isRecurrent && isMileageEnabled && !isTimingEnabled) {
                        Utils.showNotReportableErrorDialog(TaskEditFragment.this.getActivity(), TaskEditFragment.this.getString(R.string.gen_warning), TaskEditFragment.this.getString(R.string.task_edit_no_cars_available_message));
                    }
                    else {
                        Utils.showInfoDialog(TaskEditFragment.this.getActivity(), TaskEditFragment.this.getString(R.string.gen_info), TaskEditFragment.this.getString(R.string.task_edit_all_cars_linked_message));
                    }
                }
                else {
                    c.close();
                    isFinishAfterSave = false;
                    TaskEditFragment.this.saveData();
                    isFinishAfterSave = true;
                    showTaskCarEditWindow(-1);
//                    Intent i = new Intent(TaskEditFragment.this.getActivity(), TaskCarLinkActivity.class);
//                    i.putExtra(DBAdapter.COL_NAME_TASK_CAR__TASK_ID, mRowId);
//                    i.putExtra(TaskCarLinkActivity.IS_TIMING_ENABLED, isTimingEnabled);
//                    i.putExtra(TaskCarLinkActivity.IS_MILEAGE_ENABLED, isMileageEnabled);
//                    i.putExtra(TaskCarLinkActivity.IS_RECURRENT, isRecurrent);
//                    i.putExtra(TaskCarLinkActivity.IS_DIFFERENT_STARTING_TIME, isDiffStartingTime);
//                    i.putExtra(TaskCarLinkActivity.STARTING_MILEAGE, etMileage.getText().toString());
//                    i.putExtra(TaskCarLinkActivity.STARTING_TIME_IN_MILLIS, mlDateTimeInMillis);
//                    TaskEditFragment.this.startActivityForResult(i, TASK_CAR_LINK_REQUEST_CODE);
                }
            }
        });

        rbOneTime = mRootView.findViewById(R.id.rbOneTime);
        rbRecurrent = mRootView.findViewById(R.id.rbRecurrent);
        ckIsDifferentStartingTime = mRootView.findViewById(R.id.ckIsDifferentStartingTime);
        ckIsDifferentStartingTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                isDiffStartingTime = isChecked;
                TaskEditFragment.this.setSpecificLayout();
//                fillLinkedCarsData();
            }
        });
        llStartingTime = mRootView.findViewById(R.id.llStartingTime);
        llRecurrentTimeSettings = mRootView.findViewById(R.id.llRecurrentTimeSettings);
        llLastMonthDay = mRootView.findViewById(R.id.llLastMonthDay);
        llMileageZone = mRootView.findViewById(R.id.llMileageZone);
        tvMileageLabelEvery = mRootView.findViewById(R.id.tvMileageLabelEvery);
        tvMileageLabelOn = mRootView.findViewById(R.id.tvMileageLabelOn);
        tvFirstTimeRunExplanation = mRootView.findViewById(R.id.tvFirstTimeRunExplanation);
        tvOrLastDay = mRootView.findViewById(R.id.tvOrLastDay);
        tvStartingTimeLbl = mRootView.findViewById(R.id.tvStartingTimeLbl);
        tvLinkedCarsHelp = mRootView.findViewById(R.id.tvLinkedCarsHelp);

        llLinkedCarsZone = mRootView.findViewById(R.id.llLinkedCarsZone);
        llTimeReminder = mRootView.findViewById(R.id.llTimeReminder);
        llToDoCountZone = mRootView.findViewById(R.id.llToDoCountZone);

        RadioGroup rgRepeating = mRootView.findViewById(R.id.rgRepeating);
        rgRepeating.setTag(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG);
        rgRepeating.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (radioGroup.getTag() != null && radioGroup.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                    radioGroup.setTag(null);
                    return;
                }

                if (checkedId == rbOneTime.getId()) {
                    isRecurrent = false;
                    mTimeFrequencyTypeId = TASK_TIME_FREQUENCY_TYPE_ONETIME;
                }
                else { //
                    isRecurrent = true;
                    mTimeFrequencyTypeId = spnScheduleFrequency.getSelectedItemId() + 1;
                }
                TaskEditFragment.this.setSpecificLayout();
//                fillLinkedCarsData();
            }
        });

        spnScheduleFrequency = mRootView.findViewById(R.id.spnScheduleFrequency);
        spnScheduleFrequency.setTag(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG);
        spnScheduleFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getTag() != null && adapterView.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                    adapterView.setTag(null);
                    return;
                }
                mTimeFrequencyTypeId = l + 1; //0 is one time
                if (mTimeFrequencyTypeId != TASK_TIME_FREQUENCY_TYPE_MONTHLY) {
                    mOnLastDayOfMonth = false;
                }
                initDateTimeFields();
                setSpecificLayout();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spnLastDayMonth = mRootView.findViewById(R.id.spnLastDayMonth);

        ckOnLastDay = mRootView.findViewById(R.id.ckOnLastDay);
        ckOnLastDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (mTimeFrequencyTypeId == TASK_TIME_FREQUENCY_TYPE_MONTHLY) {
                    if (isChecked) {
                        isTimeOnly = true; //just the time because we know the day (last day of month)
                        TaskEditFragment.this.initDateTimeFields();
                    }
                    else {
                        isTimeOnly = false;
                        //noinspection WrongConstant
                        if (mDateTimeCalendar.get(Calendar.YEAR) == 1970) {
                            mlDateTimeInMillis = System.currentTimeMillis() + ConstantValues.ONE_DAY_IN_MILLISECONDS;
                            TaskEditFragment.this.initDateTimeFields();
                        }
                        else {
                            TaskEditFragment.this.initDateTimeFields();
                        }
                    }
                }
                TaskEditFragment.this.setSpecificLayout();
            }
        });

        RadioGroup rgScheduleType = mRootView.findViewById(R.id.rgScheduleType);
        rgScheduleType.setTag(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG);
        rgScheduleType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (radioGroup.getTag() != null && radioGroup.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                    radioGroup.setTag(null);
                    return;
                }

                if (checkedId == R.id.rbTimeDriven) {
                    isTimingEnabled = true;
                    isMileageEnabled = false;
                    mScheduledFor = TASK_SCHEDULED_FOR_TIME;
                }
                else if (checkedId == R.id.rbMileageDriven) {
                    isTimingEnabled = false;
                    isMileageEnabled = true;
                    mScheduledFor = TASK_SCHEDULED_FOR_MILEAGE;
                }
                else if (checkedId == R.id.rbTimeAndMileageDriven) {
                    isTimingEnabled = true;
                    isMileageEnabled = true;
                    mScheduledFor = TASK_SCHEDULED_FOR_BOTH;
                }
                TaskEditFragment.this.setSpecificLayout();
//                fillLinkedCarsData();
            }
        });

        etFrequency = mRootView.findViewById(R.id.etFrequency);
        etTimeReminder = mRootView.findViewById(R.id.etTimeReminder);
        tvTimeReminderUnitLbl = mRootView.findViewById(R.id.tvTimeReminderUnitLbl);
        etMileage = mRootView.findViewById(R.id.etMileage);
        etReminderMileage = mRootView.findViewById(R.id.etReminderMileage);

        rbTimeDriven = mRootView.findViewById(R.id.rbTimeDriven);
        rbMileageDriven = mRootView.findViewById(R.id.rbMileageDriven);
        rbTimeAndMileageDriven = mRootView.findViewById(R.id.rbTimeAndMileageDriven);
        tvOr = mRootView.findViewById(R.id.tvOr);

//        llLinkedCarsList = (LinearLayout) mRootView.findViewById(R.id.llLinkedCarsList);
        lvLinkedCarsList = mRootView.findViewById(R.id.lvLinkedCarsList);
        lvLinkedCarsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showTaskCarEditWindow(id);
            }
        });

        lvLinkedCarsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                mLongClickId = id;
                return false;
            }
        });
        lvLinkedCarsList.setOnCreateContextMenuListener(this);


        tvFirstMileageRunExplanation = mRootView.findViewById(R.id.tvFirstMileageRunExplanation);

        etNoOfNextToDo = mRootView.findViewById(R.id.etNoOfNextToDo);
    }

    private void showTaskCarEditWindow(long id) {
        Intent i = new Intent(TaskEditFragment.this.getActivity(), TaskCarLinkActivity.class);
        i.putExtra(DBAdapter.COL_NAME_TASK_CAR__TASK_ID, mRowId);
        i.putExtra(TaskCarLinkActivity.IS_TIMING_ENABLED, isTimingEnabled);
        i.putExtra(TaskCarLinkActivity.IS_MILEAGE_ENABLED, isMileageEnabled);
        i.putExtra(TaskCarLinkActivity.IS_RECURRENT, isRecurrent);
        i.putExtra(TaskCarLinkActivity.IS_DIFFERENT_STARTING_TIME, isDiffStartingTime);
        i.putExtra(TaskCarLinkActivity.STARTING_MILEAGE, etMileage.getText().toString());
        i.putExtra(TaskCarLinkActivity.STARTING_TIME_IN_MILLIS, mlDateTimeInMillis);
        i.putExtra(DBAdapter.COL_NAME_GEN_ROWID, id);
        TaskEditFragment.this.startActivityForResult(i, TASK_CAR_LINK_REQUEST_CODE);
    }

    @Override
    protected void initSpecificControls() {
        Utils.initSpinner(mDbAdapter, spnTaskType, DBAdapter.TABLE_NAME_TASKTYPE, DBAdapter.WHERE_CONDITION_ISACTIVE, mTaskTypeId, false);
    }

    @Override
    protected void showValuesInUI() {
        etName.setText(mName);
        ckIsActive.setChecked(mIsActive);
        acUserComment.setText(mUserComment);

        if (isRecurrent) {
            rbRecurrent.setChecked(true);
        }
        else {
            rbOneTime.setChecked(true);
        }

        switch (mScheduledFor) {
            case TASK_SCHEDULED_FOR_BOTH:
                rbTimeAndMileageDriven.setChecked(true);
                break;
            case TASK_SCHEDULED_FOR_TIME:
                rbTimeDriven.setChecked(true);
                break;
            case TASK_SCHEDULED_FOR_MILEAGE:
                rbMileageDriven.setChecked(true);
                break;
        }
        ckIsDifferentStartingTime.setChecked(isDiffStartingTime);
        etFrequency.setText(mFrequency);
        spnScheduleFrequency.setSelection(((Long) mTimeFrequencyTypeId).intValue() - 1);
        etTimeReminder.setText(mTimeReminder);
        etMileage.setText(mMileage);
        etReminderMileage.setText(mReminderMileage);
        etNoOfNextToDo.setText(mNoOfNextToDo);

        if (isTimingEnabled) {
            if (mOnLastDayOfMonth) {
                ckOnLastDay.setChecked(true);
                spnLastDayMonth.setSelection(mDateTimeCalendar.get(Calendar.MONTH));
            }
        }
        setSpecificLayout();
        fillLinkedCarsData();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setSpecificLayout() {
        if (isRecurrent) {
            llToDoCountZone.setVisibility(View.VISIBLE);
            if (isTimingEnabled) {
                tvStartingTimeLbl.setText(R.string.task_edit_starting_time_label);
                ckIsDifferentStartingTime.setVisibility(View.VISIBLE);
                llRecurrentTimeSettings.setVisibility(View.VISIBLE);
                llTimeReminder.setVisibility(View.VISIBLE);
                if (mTimeFrequencyTypeId == TASK_TIME_FREQUENCY_TYPE_DAILY) {
                    tvTimeReminderUnitLbl.setText(R.string.gen_minutes);
                    if (mRowId == -1) { //new record
                        etTimeReminder.setText("30");
                    }

                }
                else {
                    tvTimeReminderUnitLbl.setText(R.string.gen_days);
                    if (mRowId == -1) { //new record
                        etTimeReminder.setText("3");
                    }
                }

                if (mTimeFrequencyTypeId == TASK_TIME_FREQUENCY_TYPE_MONTHLY && !isDiffStartingTime) {
                    llLastMonthDay.setVisibility(View.VISIBLE);
                }
                else {
                    llLastMonthDay.setVisibility(View.GONE);
                }
                if (isDiffStartingTime) {
                    tvFirstTimeRunExplanation.setVisibility(View.VISIBLE);
                    llStartingTime.setVisibility(View.GONE);
                    llLinkedCarsZone.setVisibility(View.VISIBLE);
                    tvLinkedCarsHelp.setVisibility(View.VISIBLE);
                    lvLinkedCarsList.setVisibility(View.VISIBLE);
                    isDeleteLinkedCarsOnSave = false;
                }
                else {
                    tvFirstTimeRunExplanation.setVisibility(View.GONE);
                    llStartingTime.setVisibility(View.VISIBLE);
                    if (mTimeFrequencyTypeId == TASK_TIME_FREQUENCY_TYPE_MONTHLY && ckOnLastDay.isChecked()) {
                        spnLastDayMonth.setVisibility(View.VISIBLE);
//                        btnPickDate.setVisibility(View.GONE);
                        tvOrLastDay.setVisibility(View.GONE);
                    }
                    else {
                        spnLastDayMonth.setVisibility(View.GONE);
//                        btnPickDate.setVisibility(View.VISIBLE);
                        tvOrLastDay.setVisibility(View.VISIBLE);
                    }
                    llLinkedCarsZone.setVisibility(View.GONE);
                    tvLinkedCarsHelp.setVisibility(View.GONE);
                    lvLinkedCarsList.setVisibility(View.GONE);
                    isDeleteLinkedCarsOnSave = true;
                }
            }
            else {
                tvFirstTimeRunExplanation.setVisibility(View.GONE);
                ckIsDifferentStartingTime.setVisibility(View.GONE);
                llRecurrentTimeSettings.setVisibility(View.GONE);
                llStartingTime.setVisibility(View.GONE);
                llTimeReminder.setVisibility(View.GONE);
                llLastMonthDay.setVisibility(View.GONE);
                spnScheduleFrequency.setTag(null);
            }

            if (isTimingEnabled && isMileageEnabled) {
                tvOr.setVisibility(View.VISIBLE);
            }
            else {
                tvOr.setVisibility(View.GONE);
            }

            if (isMileageEnabled) {
                llMileageZone.setVisibility(View.VISIBLE);
                llLinkedCarsZone.setVisibility(View.VISIBLE);
                tvLinkedCarsHelp.setVisibility(View.VISIBLE);
                lvLinkedCarsList.setVisibility(View.VISIBLE);
                tvMileageLabelEvery.setVisibility(View.VISIBLE);
                tvMileageLabelOn.setVisibility(View.GONE);
                isDeleteLinkedCarsOnSave = false;
                tvFirstMileageRunExplanation.setVisibility(View.VISIBLE);
            }
            else {
                llMileageZone.setVisibility(View.GONE);
            }
        }
        else { //one time
            llToDoCountZone.setVisibility(View.GONE);
            tvStartingTimeLbl.setText(R.string.gen_on);
            tvFirstTimeRunExplanation.setVisibility(View.GONE);
            tvFirstMileageRunExplanation.setVisibility(View.GONE);
            llRecurrentTimeSettings.setVisibility(View.GONE);
            llLastMonthDay.setVisibility(View.GONE);
            ckIsDifferentStartingTime.setVisibility(View.GONE);
            tvMileageLabelEvery.setVisibility(View.GONE);
            tvMileageLabelOn.setVisibility(View.VISIBLE);
            tvTimeReminderUnitLbl.setText(R.string.gen_days);
            spnLastDayMonth.setVisibility(View.GONE);

            if (isTimingEnabled) {
                llStartingTime.setVisibility(View.VISIBLE);
                llTimeReminder.setVisibility(View.VISIBLE);
            }
            else {
                llStartingTime.setVisibility(View.GONE);
                llTimeReminder.setVisibility(View.GONE);
            }

            if (isTimingEnabled && isMileageEnabled) {
                tvOr.setVisibility(View.VISIBLE);
            }
            else {
                tvOr.setVisibility(View.GONE);
            }

            if (isMileageEnabled) {
                llMileageZone.setVisibility(View.VISIBLE);
                llLinkedCarsZone.setVisibility(View.VISIBLE);
                tvLinkedCarsHelp.setVisibility(View.VISIBLE);
                lvLinkedCarsList.setVisibility(View.VISIBLE);
                isDeleteLinkedCarsOnSave = false;
            }
            else {
                llMileageZone.setVisibility(View.GONE);
                llLinkedCarsZone.setVisibility(View.GONE);
                tvLinkedCarsHelp.setVisibility(View.GONE);
                lvLinkedCarsList.setVisibility(View.GONE);
                isDeleteLinkedCarsOnSave = true;
            }
        }
        showDateTime();
    }

    @Override
    protected boolean saveData() {
        if (isMileageEnabled) {
            try {
                //noinspection ResultOfMethodCallIgnored
                Integer.parseInt(etMileage.getText().toString());
            }
            catch (Exception e) {
                Toast toast = Toast.makeText(getActivity(), mResource.getString(R.string.error_065), Toast.LENGTH_SHORT);
                toast.show();
                etMileage.requestFocus();
                saveSuccess = false;
                return false;
            }

            try {
                //noinspection ResultOfMethodCallIgnored
                Integer.parseInt(etReminderMileage.getText().toString());
            }
            catch (Exception e) {
                Toast toast = Toast.makeText(getActivity(), mResource.getString(R.string.error_065), Toast.LENGTH_SHORT);
                toast.show();
                etReminderMileage.requestFocus();
                saveSuccess = false;
                return false;
            }

            if (etMileage.getText().toString().length() == 0) {
                Toast toast = Toast.makeText(getActivity(), mResource.getString(R.string.task_edit_fill_mileage), Toast.LENGTH_SHORT);
                toast.show();
                etMileage.requestFocus();
                saveSuccess = false;
                return false;
            }

            if (etReminderMileage.getText().toString().length() == 0) {
                etReminderMileage.setText("0");
            }

            if (Integer.parseInt(etMileage.getText().toString()) < Integer.parseInt(etReminderMileage.getText().toString())) {
                Toast toast = Toast.makeText(getActivity(), mResource.getString(R.string.task_edit_mileage_frequency_smaller_than_reminder),
                        Toast.LENGTH_SHORT);
                toast.show();
                etMileage.requestFocus();
                saveSuccess = false;
                return false;
            }
        }

        if (isTimingEnabled && isRecurrent) {
            if (etFrequency.getText().toString().length() == 0 || Integer.parseInt(etFrequency.getText().toString()) == 0) {
                Toast toast = Toast.makeText(getActivity(), mResource.getString(R.string.task_edit_fill_frequency), Toast.LENGTH_SHORT);
                toast.show();
                etFrequency.requestFocus();
                saveSuccess = false;
                return false;
            }
            if (etTimeReminder.getText().toString().length() == 0) {
                etTimeReminder.setText("0");
            }
        }

        //at least one linked car required
        if (isFinishAfterSave && !isDeleteLinkedCarsOnSave) {
            if (lvLinkedCarsList.getCount() == 0) { //no linked car
                Toast toast = Toast.makeText(getActivity(), mResource.getString(R.string.task_edit_no_linked_car_message), Toast.LENGTH_SHORT);
                toast.show();
                return false;
            }
        }
        if (!isRecurrent && isTimingEnabled) {//check if the starting time is in the future
            if (mlDateTimeInMillis < System.currentTimeMillis()) {
                Toast toast = Toast.makeText(getActivity(), mResource.getString(R.string.task_edit_starting_time_in_future_message),
                        Toast.LENGTH_SHORT);
                toast.show();
                saveSuccess = false;
                return false;
            }
        }

        if (isRecurrent) {
            if (etNoOfNextToDo.getText().toString().length() == 0) {
                Toast toast = Toast.makeText(getActivity(),
                        mResource.getString(R.string.gen_fill_mandatory) + ": " + mResource.getString(R.string.task_edit_todo_count), Toast.LENGTH_SHORT);
                toast.show();
                saveSuccess = false;
                etNoOfNextToDo.requestFocus();
                return false;
            } else {
                try {
                    int toDoCount = Integer.parseInt(etNoOfNextToDo.getText().toString());
                    if (toDoCount < 2) {
                        Toast toast = Toast.makeText(getActivity(), mResource.getString(R.string.error_114), Toast.LENGTH_LONG);
                        toast.show();
                        etNoOfNextToDo.requestFocus();
                        saveSuccess = false;
                        return false;
                    }
                } catch (Exception e) {
                    Toast toast = Toast.makeText(getActivity(), mResource.getString(R.string.error_065), Toast.LENGTH_SHORT);
                    toast.show();
                    etNoOfNextToDo.requestFocus();
                    saveSuccess = false;
                    return false;
                }
            }
        }


        ContentValues data = new ContentValues();
        data.put(DBAdapter.COL_NAME_GEN_NAME, etName.getText().toString());
        data.put(DBAdapter.COL_NAME_GEN_ISACTIVE, (ckIsActive.isChecked() ? "Y" : "N"));
        data.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, acUserComment.getText().toString());

        data.put(DBAdapter.COL_NAME_TASK__TASKTYPE_ID, mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_TASKTYPE, spnTaskType.getSelectedItem().toString()));
        data.put(DBAdapter.COL_NAME_TASK__SCHEDULEDFOR, mScheduledFor);
        data.put(DBAdapter.COL_NAME_TASK__ISRECURRENT, (isRecurrent ? "Y" : "N"));
        if (isRecurrent) {
            data.put(DBAdapter.COL_NAME_TASK__TODOCOUNT, etNoOfNextToDo.getText().toString());
        }
        else {
            data.put(DBAdapter.COL_NAME_TASK__TODOCOUNT, 1);
        }
        if (isTimingEnabled) {
            if (isRecurrent) {
                data.put(DBAdapter.COL_NAME_TASK__ISDIFFERENTSTARTINGTIME, (isDiffStartingTime ? "Y" : "N"));
                data.put(DBAdapter.COL_NAME_TASK__TIMEFREQUENCY, etFrequency.getText().toString());
                if (isDiffStartingTime) {
                    data.put(DBAdapter.COL_NAME_TASK__STARTINGTIME, (Long) null);
                }
                else {
                    if (mTimeFrequencyTypeId == TASK_TIME_FREQUENCY_TYPE_MONTHLY && ckOnLastDay.isChecked()) {
                        mDateTimeCalendar.set(Calendar.MONTH, spnLastDayMonth.getSelectedItemPosition());
                        data.put(DBAdapter.COL_NAME_TASK__STARTINGTIME, mDateTimeCalendar.getTimeInMillis() / 1000);
                    }
                    else {
                        data.put(DBAdapter.COL_NAME_TASK__STARTINGTIME, mlDateTimeInMillis / 1000);
                    }
                }
            }
            else {
                data.put(DBAdapter.COL_NAME_TASK__ISDIFFERENTSTARTINGTIME, (String) null);
                data.put(DBAdapter.COL_NAME_TASK__TIMEFREQUENCY, (String) null);
                data.put(DBAdapter.COL_NAME_TASK__STARTINGTIME, mlDateTimeInMillis / 1000);
            }

            data.put(DBAdapter.COL_NAME_TASK__TIMEFREQUENCYTYPE, mTimeFrequencyTypeId);
            data.put(DBAdapter.COL_NAME_TASK__TIMEREMINDERSTART, etTimeReminder.getText().toString());
        }
        else {
            data.put(DBAdapter.COL_NAME_TASK__STARTINGTIME, (Integer) null);
            data.put(DBAdapter.COL_NAME_TASK__TIMEREMINDERSTART, (Integer) null);
            data.put(DBAdapter.COL_NAME_TASK__TIMEREMINDERSTART, (Integer) null);
        }
        if (isMileageEnabled) {
            data.put(DBAdapter.COL_NAME_TASK__RUNMILEAGE, etMileage.getText().toString());
            data.put(DBAdapter.COL_NAME_TASK__MILEAGEREMINDERSTART, etReminderMileage.getText().toString());
        }
        else {
            data.put(DBAdapter.COL_NAME_TASK__RUNMILEAGE, (Integer) null);
            data.put(DBAdapter.COL_NAME_TASK__MILEAGEREMINDERSTART, (Integer) null);
        }

        if (mRowId == -1) {
            mRowId = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_TASK, data);
            saveSuccess = mRowId > 0;
            if (mRowId < 0) {
                if (mRowId == -1) //DB Error
                {
                    Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), mDbAdapter.mErrorMessage, mDbAdapter.mException);
                }
                else { //precondition error
                    Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(-1 * ((Long) mRowId).intValue()));
                }
                return false;
            }
            else {
                if (isFinishAfterSave && getActivity() != null) {
                    //generate the To-Dos
                    Intent intent = new Intent(getActivity(), ToDoManagementService.class);
                    intent.putExtra(ToDoManagementService.TASK_ID_KEY, mRowId);
                    getActivity().startService(intent);
                }
            }
        }
        else {
            int updResult = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_TASK, mRowId, data);
            if (updResult != -1) {
                String errMsg;
                errMsg = mResource.getString(updResult);
                if (updResult == R.string.error_000) {
                    errMsg = errMsg + "\n" + mDbAdapter.mErrorMessage;
                }
                Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), errMsg, mDbAdapter.mException);
                return false;
            }
            else {
                saveSuccess = true;
                if (isFinishAfterSave) {
                    //final save => recreate the todos (delete existing & recreate)
                    String[] deleteArgs = {Long.toString(mRowId)};
                    mDbAdapter.deleteRecords(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__TASK_ID + " = ? AND "
                            + DBAdapter.COL_NAME_TODO__ISDONE + " = 'N'", deleteArgs);

                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), ToDoManagementService.class);
                        intent.putExtra(ToDoManagementService.TASK_ID_KEY, mRowId);
                        getActivity().startService(intent);
                    }
                }
            }
        }
        //delete existent linked cars if the current configuration not support linked cars
        String[] selectionArgs = {Long.toString(mRowId)};
        if (isFinishAfterSave && isDeleteLinkedCarsOnSave) {
            mDbAdapter.deleteRecords(DBAdapter.TABLE_NAME_TASK_CAR, DBAdapter.COL_NAME_TASK_CAR__TASK_ID + "= ?", selectionArgs);
        }

        ContentValues newContent = new ContentValues();
        if (!isTimingEnabled) {
            newContent.put(DBAdapter.COL_NAME_TASK_CAR__FIRSTRUN_DATE, (Long) null);
            mDbAdapter.updateRecords(DBAdapter.TABLE_NAME_TASK_CAR, DBAdapter.COL_NAME_TASK_CAR__TASK_ID + " = ?", selectionArgs, newContent);
        }
        if (!isMileageEnabled) {
            newContent.put(DBAdapter.COL_NAME_TASK_CAR__FIRSTRUN_MILEAGE, (Integer) null);
            mDbAdapter.updateRecords(DBAdapter.TABLE_NAME_TASK_CAR, DBAdapter.COL_NAME_TASK_CAR__TASK_ID + " = ?", selectionArgs, newContent);
        }
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TASK_CAR_LINK_REQUEST_CODE) {
            fillLinkedCarsData();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, ConstantValues.CONTEXT_MENU_EDIT_ID, 0, mResource.getString(R.string.gen_edit));
        menu.add(0, ConstantValues.CONTEXT_MENU_DELETE_ID, 0, mResource.getString(R.string.gen_delete));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == ConstantValues.CONTEXT_MENU_EDIT_ID) {
            showTaskCarEditWindow(mLongClickId);
            return true;
        }
        else if (item.getItemId() == ConstantValues.CONTEXT_MENU_DELETE_ID) {
            if (getActivity() != null) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(R.string.gen_confirm);
                alertDialog.setMessage(R.string.gen_delete_confirmation);
                alertDialog.setCancelable(false);

                alertDialog.setPositiveButton(R.string.gen_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String[] deleteArgs = {Long.toString(mLongClickId)};
                                mDbAdapter.deleteRecords(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__ISDONE + " = 'N' " + " AND "
                                        + DBAdapter.COL_NAME_TODO__CAR_ID + " = " + "(SELECT " + DBAdapter.COL_NAME_TASK_CAR__CAR_ID + " FROM "
                                        + DBAdapter.TABLE_NAME_TASK_CAR + " WHERE " + DBAdapter.COL_NAME_GEN_ROWID + " = ? )", deleteArgs);
                                int deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_TASK_CAR, mLongClickId);
                                if (deleteResult != -1) {
                                    Utils.showNotReportableErrorDialog(TaskEditFragment.this.getActivity(), TaskEditFragment.this.getString(R.string.gen_error),
                                            TaskEditFragment.this.getString(deleteResult));
                                } else {
                                    fillLinkedCarsData();
                                }
                            }
                        });

                alertDialog.setNegativeButton(R.string.gen_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            }
            return true;
        }
        else {
            return super.onContextItemSelected(item);
        }
    }

    private void fillLinkedCarsData() {
        String firstRun = "";
        if (isRecurrent) {
            if (isTimingEnabled && isDiffStartingTime) {
                firstRun = "'" + mResource.getString(R.string.task_car_edit_start_date) + " ' || " + " '[#1]'";
            }
            if (isMileageEnabled) {
                if (firstRun.length() > 0) {
                    firstRun = firstRun + " || '; " + mResource.getString(R.string.task_car_edit_start_mileage) + " ' || "
                            + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TASK_CAR, DBAdapter.COL_NAME_TASK_CAR__FIRSTRUN_MILEAGE) + " || ' ' || "
                            + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_UOM, DBAdapter.COL_NAME_UOM__CODE);
                }
                else {
                    firstRun = "'" + mResource.getString(R.string.task_car_edit_start_mileage) + " ' || "
                            + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TASK_CAR, DBAdapter.COL_NAME_TASK_CAR__FIRSTRUN_MILEAGE) + " || ' ' || "
                            + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_UOM, DBAdapter.COL_NAME_UOM__CODE);
                }
            }
        }
        else {
            firstRun = "''";
        }

        if (firstRun.length() == 0) {
            return;
        }

        firstRun = firstRun + " AS FirstRun, ";

        String[] selectionArgs = {Long.toString(mRowId)};
        //@formatter:off
        String selectSql =
                "SELECT "
                        + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TASK_CAR, DBAdapter.COL_NAME_GEN_ROWID) + ", " //#0
                        + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_CAR, DBAdapter.COL_NAME_GEN_NAME) + ", " //#1
                        + firstRun //#2
                        + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TASK_CAR, DBAdapter.COL_NAME_TASK_CAR__FIRSTRUN_DATE) + " " //#3
                + " FROM " + DBAdapter.TABLE_NAME_TASK_CAR + " "
                    + " JOIN " + DBAdapter.TABLE_NAME_CAR
                        + " ON " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TASK_CAR, DBAdapter.COL_NAME_TASK_CAR__CAR_ID)
                            + " = " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_CAR, DBAdapter.COL_NAME_GEN_ROWID)
                    + " JOIN " + DBAdapter.TABLE_NAME_UOM
                        + " ON " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_CAR, DBAdapter.COL_NAME_CAR__LENGTH_UOM_ID)
                            + "=" + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_UOM, DBAdapter.COL_NAME_GEN_ROWID)
                + " WHERE "
                        + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TASK_CAR, DBAdapter.COL_NAME_TASK_CAR__TASK_ID) + " = ? "
                + " ORDER BY " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_CAR, DBAdapter.COL_NAME_GEN_NAME);
        //@formatter:on

        int listLayout = R.layout.twoline_list2;

        Cursor mLinkedCarsCursor = mDbAdapter.execSelectSql(selectSql, selectionArgs);
        //noinspection deprecation
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(), listLayout, mLinkedCarsCursor, new String[]{DBAdapter.COL_NAME_GEN_NAME,
                "FirstRun"}, new int[]{R.id.tvTwoLineListText1, R.id.tvTwoLineListText2});
        cursorAdapter.setViewBinder(new TaskCarLinkDataBinder());
        lvLinkedCarsList.setAdapter(cursorAdapter);
    }

}
