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

package andicar.n.activity.fragment;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import andicar.n.activity.CommonDetailActivity;
import andicar.n.activity.CommonListActivity;
import andicar.n.activity.miscellaneous.GPSTrackMap;
import andicar.n.persistence.DBAdapter;
import andicar.n.service.ToDoManagementService;
import andicar.n.service.ToDoNotificationService;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * @author miki
 */

public class MileageEditFragment extends BaseEditFragment {

    //mileage insert mode: 0 = new index; 1 = mileage
    public static final int INSERT_MODE_INDEX = 0;
    public static final int INSERT_MODE_MILEAGE = 1;
    private final Calendar mDateTimeToCalendar = Calendar.getInstance();
    //mileage insert mode: 0 = new index; 1 = mileage
    private int mInsertMode = 0;
    private long mGpsTrackId = -1;
    private long mlTripDurationInSeconds;
    private long mlDateTimeToInMillis = 0L;
    private long mUOMLengthId = -1;
    private int mYearTo = 0;
    private int mMonthTo = 0;
    private int mDayTo = 0;
    private int mHourTo = 0;
    private int mMinuteTo = 0;
    private boolean mDraftEdit = false;
    private BigDecimal mNewIndex = null; //new BigDecimal("0");
    private BigDecimal mStartIndex = new BigDecimal("0");
    private BigDecimal mStopIndex = null;
    private BigDecimal mUserInputValue = BigDecimal.valueOf(0);

    private boolean mReimbursementCanCalculated = true;

    private String mCarCurrencyCode = "";

    private RadioButton rbInsertModeIndex;
    private RadioButton rbInsertModeMileage;
    private TextView tvCalculatedTextLabel;
    private TextView tvCalculatedTextContent;
    private TextView tvMileageRecInProgress;
    private TextView tvReimbursementValue;
    private TextView tvTripDurationContent;
    private TextView tvDateTimeToValue = null;
    private EditText etStartIndex;
    private EditText etUserInput;

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //this fragment can uses templates for filling data
        isUseTemplate = true;

        if (savedInstanceState != null) {
            mUOMLengthId = savedInstanceState.getLong("mUOMLengthId");
            mGpsTrackId = savedInstanceState.getLong("mGpsTrackId");
            mlDateTimeToInMillis = savedInstanceState.getLong("mlDateTimeToInMillis");
            mYearTo = savedInstanceState.getInt("mYearTo");
            mMonthTo = savedInstanceState.getInt("mMonthTo");
            mDayTo = savedInstanceState.getInt("mDayTo");
            mHourTo = savedInstanceState.getInt("mHourTo");
            mMinuteTo = savedInstanceState.getInt("mMinuteTo");
            mInsertMode = savedInstanceState.getInt("mInsertMode");
            mCarCurrencyCode = savedInstanceState.getString("mCarCurrencyCode", "");

            if (savedInstanceState.containsKey("mNewIndex")) {
                mNewIndex = new BigDecimal(savedInstanceState.getString("mNewIndex", "0"));
            }
            if (savedInstanceState.containsKey("mStartIndex")) {
                mStartIndex = new BigDecimal(savedInstanceState.getString("mStartIndex", "0"));
            }
            if (savedInstanceState.containsKey("mStopIndex")) {
                mStopIndex = new BigDecimal(savedInstanceState.getString("mStopIndex", "0"));
            }
            if (savedInstanceState.containsKey("mUserInputValue")) {
                mUserInputValue = new BigDecimal(savedInstanceState.getString("mUserInputValue", "0"));
            }

        }
        else {
            if (mOperationType.equals(BaseEditFragment.DETAIL_OPERATION_NEW)) {
                //check if drafted mileage exists
                mRowId = getDraftIDIfExists(AndiCar.getDefaultSharedPreferences().getLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_car_id), -1));
                if (mRowId > -1) { //draft exists => switch to edit mode
                    mOperationType = BaseEditFragment.DETAIL_OPERATION_EDIT;
                    mDraftEdit = true;
                }
            }

            switch (mOperationType) {
                case BaseEditFragment.DETAIL_OPERATION_EDIT: {
                    loadDataFromDB();
                    break;
                }
                case BaseEditFragment.DETAIL_OPERATION_TRACK_TO_MILEAGE: {
                    mGpsTrackId = mArgumentsBundle.getLong(GPSTrackControllerFragment.GPS_TRACK_ID_FOR_MILEAGE);
                    Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_LIST_GPSTRACK_TABLE, mGpsTrackId);
                    assert c != null;
                    mInsertMode = INSERT_MODE_INDEX;
                    setCarId(c.getLong(DBAdapter.COL_POS_GPSTRACK__CAR_ID));
                    setDriverId(c.getLong(DBAdapter.COL_POS_GPSTRACK__DRIVER_ID));
                    setExpTypeId(c.getLong(DBAdapter.COL_POS_GPSTRACK__EXPENSETYPE_ID));
                    mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
                    mTagId = c.getLong(DBAdapter.COL_POS_GPSTRACK__TAG_ID);
                    mTagStr = mDbAdapter.getNameById(DBAdapter.TABLE_NAME_TAG, mTagId);

                    String gpsTrackStartIndexStr = mArgumentsBundle.getString(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_INDEX_FOR_MILEAGE, "0");
                    if (gpsTrackStartIndexStr == null || gpsTrackStartIndexStr.length() == 0) {
                        gpsTrackStartIndexStr = "0";
                    }

                    BigDecimal gpsTrackStartIndex = new BigDecimal(gpsTrackStartIndexStr);
                    getStartIndex();
                    if (gpsTrackStartIndex.compareTo(mStartIndex) > 0) {
                        mStartIndex = gpsTrackStartIndex;
                    }

                    try {
                        mStopIndex = mStartIndex.add(new BigDecimal(c.getString(DBAdapter.COL_POS_GPSTRACK__DISTANCE))).setScale(0,
                                BigDecimal.ROUND_HALF_DOWN);
                    }
                    catch (NumberFormatException ignored) {
                    }
                    mlDateTimeInMillis = mArgumentsBundle.getLong(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_START_TIME_FOR_MILEAGE);
                    mlDateTimeToInMillis = System.currentTimeMillis();
                    calculateTripDuration();
                    c.close();
                    break;
                }
                default: //new record
                    initDefaultValues();
                    break;
            }
            Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_CAR, DBAdapter.COL_LIST_CAR_TABLE, mCarId);
            if (c != null) {
                mUOMLengthId = c.getLong(DBAdapter.COL_POS_CAR__UOMLENGTH_ID);
                c.close();
            }
            mCarCurrencyCode = mDbAdapter.getCurrencyCode(mDbAdapter.getCarCurrencyID(mCarId));
            initDateTimeFields();
            initDateTimeToFields();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        showDateTime();

        showDateTimeTo();
        showReimbursementValue();

        return mRootView;
    }

    /**
     * load data from an existing record
     */
    private void loadDataFromDB() {
//		if(!mDraftEdit)
//            mRowId = mArgumentsBundle.getLong(ConstantValues.RECORD_ID_KEY);

        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_MILEAGE, DBAdapter.COL_LIST_MILEAGE_TABLE, mRowId);
        setCarId(c != null ? c.getLong(DBAdapter.COL_POS_MILEAGE__CAR_ID) : -1);
        setDriverId(c != null ? c.getLong(DBAdapter.COL_POS_MILEAGE__DRIVER_ID) : -1);
        try {
            mStartIndex = (new BigDecimal(c != null ? c.getDouble(DBAdapter.COL_POS_MILEAGE__INDEXSTART) : 0).setScale(ConstantValues.DECIMALS_LENGTH,
                    ConstantValues.ROUNDING_MODE_LENGTH));
            if (mDraftEdit) {
                mStopIndex = null;
            }
            else {
                //check if this is a draft or not
                if ((c != null ? c.getString(DBAdapter.COL_POS_MILEAGE__INDEXSTOP) : null) == null) { //draft
                    mStopIndex = null;
                    mDraftEdit = true;
                }
                else {
                    mStopIndex = (new BigDecimal(c.getDouble(DBAdapter.COL_POS_MILEAGE__INDEXSTOP)).setScale(ConstantValues.DECIMALS_LENGTH,
                            ConstantValues.ROUNDING_MODE_LENGTH));
                    mDraftEdit = false;
                }
            }
        }
        catch (NumberFormatException ignored) {
        }
        mInsertMode = INSERT_MODE_INDEX;
        mUserComment = c != null ? c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT) : "";
        setExpTypeId(c != null ? c.getLong(DBAdapter.COL_POS_MILEAGE__EXPENSETYPE_ID) : -1);

        //fill tag
        if ((c != null ? c.getString(DBAdapter.COL_POS_MILEAGE__TAG_ID) : null) != null && c.getString(DBAdapter.COL_POS_MILEAGE__TAG_ID).length() > 0) {
            mTagId = c.getLong(DBAdapter.COL_POS_MILEAGE__TAG_ID);
            mTagStr = mDbAdapter.getNameById(DBAdapter.TABLE_NAME_TAG, mTagId);
        }

        mlDateTimeInMillis = c != null ? c.getLong(DBAdapter.COL_POS_MILEAGE__DATE) * 1000 : 0;
        if (mDraftEdit) //update datetimeTo to current time
        {
            mlDateTimeToInMillis = System.currentTimeMillis();
        }
        else {
            mlDateTimeToInMillis = c != null ? c.getLong(DBAdapter.COL_POS_MILEAGE__DATE_TO) * 1000 : 0;
        }

        calculateTripDuration();
        if (c != null) {
            c.close();
        }

        //get the gps track id (if exists)
        String selection = DBAdapter.COL_NAME_GPSTRACK__MILEAGE_ID + " = ? ";
        String[] selectionArgs = {Long.toString(mRowId)};
        c = mDbAdapter.query(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_LIST_GEN_ROWID, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            mGpsTrackId = c.getLong(0);
        }
        c.close();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.andicar.activity.BaseEditFragment#initDefaultValues()
     */
    @Override
    public void initDefaultValues() {
        super.initDefaultValues();

        if (mStartIndex.equals(new BigDecimal("0"))) {
            mStartIndex = mDbAdapter.getCarLastMileageIndex(mCarId);
        }

        mInsertMode = mPreferences.getInt(AndiCar.getAppResources().getString(R.string.pref_key_mileage_insert_mode), 0);
        //loadSpecificViewsFromLayoutXML tag
        if (mPreferences.getBoolean(AndiCar.getAppResources().getString(R.string.pref_key_gen_remember_last_tag), false)
                && mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_gen_last_tag_id), 0) > 0) {
            mTagId = mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_gen_last_tag_id), 0);
            String selection = DBAdapter.COL_NAME_GEN_ROWID + "= ? ";
            String[] selectionArgs = {Long.toString(mTagId)};
            Cursor c = mDbAdapter.query(DBAdapter.TABLE_NAME_TAG, DBAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs, null);
            if (c.moveToFirst()) {
                mTagStr = c.getString(DBAdapter.COL_POS_GEN_NAME);
            }
            c.close();
        }
        else {
            mTagStr = "";
        }

        mlDateTimeInMillis = System.currentTimeMillis();
        initDateTimeFields();
        mlDateTimeToInMillis = mlDateTimeInMillis;
        initDateTimeToFields();
        calculateTripDuration();
    }

    @SuppressLint("SetTextI18n")
    protected void loadSpecificViewsFromLayoutXML() {
        if (mRootView == null) {
            return;
        }

        tvCalculatedTextContent = mRootView.findViewById(R.id.tvCalculatedTextContent);
        tvReimbursementValue = mRootView.findViewById(R.id.tvReimbursementValue);
        tvTripDurationContent = mRootView.findViewById(R.id.tvTripDurationContent);

        etStartIndex = mRootView.findViewById(R.id.etIndexStart);
        etStartIndex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().length() > 0) {
                    try {
                        mStartIndex = new BigDecimal(editable.toString().trim());
                    } catch (Exception ignored) {
                        mStartIndex = new BigDecimal("0");
                    }
                }
                else {
                    mStartIndex = new BigDecimal("0");
                }

                calculateMileageOrNewIndex();
            }
        });

        etUserInput = mRootView.findViewById(R.id.etUserInput);
        etUserInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().length() > 0) {
                    try {
                        mUserInputValue = new BigDecimal(editable.toString().trim());
                    } catch (Exception ignored) {
                        mUserInputValue = new BigDecimal("0");
                    }
                }
                else {
                    mUserInputValue = new BigDecimal("0");
                }

                calculateMileageOrNewIndex();
            }
        });

        rbInsertModeIndex = mRootView.findViewById(R.id.rbInsertModeIndex);
        rbInsertModeMileage = mRootView.findViewById(R.id.rbInsertModeMileage);
        RadioGroup rg = mRootView.findViewById(R.id.rgMileageInsertMode);
//        rg.setOnCheckedChangeListener(rgOnCheckedChangeListener);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (i == rbInsertModeIndex.getId()) {
                    MileageEditFragment.this.setInsertMode(INSERT_MODE_INDEX);//new index
                }
                else {
                    MileageEditFragment.this.setInsertMode(INSERT_MODE_MILEAGE);
                }
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putInt(AndiCar.getAppResources().getString(R.string.pref_key_mileage_insert_mode), mInsertMode);
                editor.apply();
                MileageEditFragment.this.calculateMileageOrNewIndex();
            }
        });

        tvCalculatedTextLabel = mRootView.findViewById(R.id.tvCalculatedTextLabel);

        tvMileageRecInProgress = mRootView.findViewById(R.id.tvMileageRecInProgress);
        tvMileageRecInProgress.setTextColor(Color.RED);

        tvDateTimeToValue = mRootView.findViewById(R.id.tvDateTimeToValue);
        if (tvDateTimeToValue != null) {
            tvDateTimeToValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Initialize
                    SwitchDateTimeDialogFragment dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                            getString(R.string.gen_stop_label),
                            getString(R.string.gen_ok),
                            getString(R.string.gen_cancel)
                    );

                    // Assign values
                    dateTimeDialogFragment.startAtCalendarView();
                    // Assign each element, default element is the current moment
                    dateTimeDialogFragment.setDefaultHourOfDay(mDateTimeToCalendar.get(Calendar.HOUR_OF_DAY));
                    dateTimeDialogFragment.setDefaultMinute(mDateTimeToCalendar.get(Calendar.MINUTE));
                    dateTimeDialogFragment.setDefaultDay(mDateTimeToCalendar.get(Calendar.DAY_OF_MONTH));
                    dateTimeDialogFragment.setDefaultMonth(mDateTimeToCalendar.get(Calendar.MONTH));
                    dateTimeDialogFragment.setDefaultYear(mDateTimeToCalendar.get(Calendar.YEAR));

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
                            mlDateTimeToInMillis = date.getTime();
                            initDateTimeToFields();
                            showDateTimeTo();
                            calculateTripDuration();
                            showTripDuration();
                        }

                        @Override
                        public void onNegativeButtonClick(Date date) {
                            // Date is get on negative button click
                        }
                    });

                    // Show
                    dateTimeDialogFragment.show(MileageEditFragment.this.getActivity().getSupportFragmentManager(), "dialog_time");
                }
            });
        }

        viewsLoaded = true;
    }

    @SuppressLint("SetTextI18n")
    protected void initSpecificControls() {

        if (mInsertMode == INSERT_MODE_INDEX) {
            rbInsertModeIndex.setChecked(true);
            tvCalculatedTextLabel.setText(mResource.getString(R.string.mileage_edit_option_mileage_label) + ": ");
//            etUserInput.setTag(mResource.getString(R.string.gen_stop_label));
        }
        else {
            rbInsertModeMileage.setChecked(true);
            tvCalculatedTextLabel.setText(mResource.getString(R.string.gen_index_label2) + ": ");
//            etUserInput.setTag(mResource.getString(R.string.mileage_edit_option_mileage_label));
        }

        showTripDuration();
        showReimbursementValue();
    }

    protected void showValuesInUI() {
        acUserComment.setText(mUserComment);
        acTag.setText(mTagStr);
        etStartIndex.setText(Utils.numberToString(mStartIndex, false, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH));
        if (mStopIndex != null) {
            etUserInput.setText(Utils.numberToString(mStopIndex, false, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH));
        }

        switch (mOperationType) {
            case BaseEditFragment.DETAIL_OPERATION_EDIT: {
                if (mDraftEdit) {
                    tvMileageRecInProgress.setVisibility(View.VISIBLE);
                }
                else {
                    tvMileageRecInProgress.setVisibility(View.GONE);
                }

                spnCar.setEnabled(false);
                rbInsertModeIndex.setChecked(true);
                break;
            }
            case BaseEditFragment.DETAIL_OPERATION_TRACK_TO_MILEAGE: {
                tvMileageRecInProgress.setVisibility(View.GONE);
                break;
            }
            default:
                tvMileageRecInProgress.setVisibility(View.GONE);
                initDefaultValues();
                break;
        }
    }

    public void setSpecificLayout() {
    }

    @Override
    protected boolean saveData() {
        //check mandatory fields & index preconditions
        calculateMileageOrNewIndex();

        int checkResult;
        ContentValues data = new ContentValues();
        data.put(DBAdapter.COL_NAME_GEN_NAME, "");
        data.put(DBAdapter.COL_NAME_GEN_ISACTIVE, "Y");
        data.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, acUserComment.getText().toString());
        data.put(DBAdapter.COL_NAME_MILEAGE__DATE, mlDateTimeInMillis / 1000);
        data.put(DBAdapter.COL_NAME_MILEAGE__CAR_ID, mCarId);
        data.put(DBAdapter.COL_NAME_MILEAGE__DRIVER_ID, mDriverId);
        data.put(DBAdapter.COL_NAME_MILEAGE__INDEXSTART, mStartIndex.setScale(ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH).toString());
        if (etUserInput.getText().toString().trim().length() > 0) {
            data.put(DBAdapter.COL_NAME_MILEAGE__INDEXSTOP, mNewIndex.setScale(ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH).toString());
        }
        else {
            mNewIndex = null;
        }

        data.put(DBAdapter.COL_NAME_MILEAGE__UOMLENGTH_ID, mUOMLengthId);
        data.put(DBAdapter.COL_NAME_MILEAGE__EXPENSETYPE_ID, mExpTypeId);
        data.put(DBAdapter.COL_NAME_MILEAGE__GPSTRACKLOG, "");
        data.put(DBAdapter.COL_NAME_MILEAGE__DATE_TO, mlDateTimeToInMillis / 1000);

        if (acTag.getText().toString().length() > 0) {
            String selection = "UPPER (" + DBAdapter.COL_NAME_GEN_NAME + ") = ?";
            String[] selectionArgs = {acTag.getText().toString().toUpperCase()};
            Cursor c = mDbAdapter.query(DBAdapter.TABLE_NAME_TAG, DBAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs, null);
            String tagIdStr = null;
            if (c.moveToFirst()) {
                tagIdStr = c.getString(DBAdapter.COL_POS_GEN_ROWID);
            }
            c.close();
            if (tagIdStr != null && tagIdStr.length() > 0) {
                mTagId = Long.parseLong(tagIdStr);
                data.put(DBAdapter.COL_NAME_MILEAGE__TAG_ID, mTagId);
            }
            else {
                ContentValues tmpData = new ContentValues();
                tmpData.put(DBAdapter.COL_NAME_GEN_NAME, acTag.getText().toString());
                mTagId = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_TAG, tmpData);
                if (mTagId >= 0) {
                    data.put(DBAdapter.COL_NAME_MILEAGE__TAG_ID, mTagId);
                }
            }
        }
        else {
            data.put(DBAdapter.COL_NAME_MILEAGE__TAG_ID, (String) null);
        }

        if (mOperationType.equals(BaseEditFragment.DETAIL_OPERATION_NEW) || mOperationType.equals(BaseEditFragment.DETAIL_OPERATION_TRACK_TO_MILEAGE)) {
            checkResult = mDbAdapter.checkIndex(-1, mCarId, mStartIndex, mNewIndex);
            if (checkResult == -1) {
                Long result = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_MILEAGE, data);
                if (result.intValue() < 0) {
                    if (result.intValue() == -1) //DB Error
                    {
                        Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), mDbAdapter.mErrorMessage, mDbAdapter.mException, false);
                    }
                    else //precondition error
                    {
                        Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(-1 * result.intValue()), false);
                    }
                    return false;
                }
                //set the mileage id on the gps track
                ContentValues cv = new ContentValues();
                cv.put(DBAdapter.COL_NAME_GPSTRACK__MILEAGE_ID, result);
                mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_GPSTRACK, mGpsTrackId, cv);
            }
        }
        else {
            checkResult = mDbAdapter.checkIndex(mRowId, mCarId, mStartIndex, mNewIndex);
            if (checkResult == -1) {
                int updResult = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_MILEAGE, mRowId, data);
                if (updResult != -1) {
                    Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(updResult), false);
                    return false;
                }
            }
        }
        if (checkResult != -1) //error
        {
            Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(checkResult), false);
            return false;
        }
        else {
            Toast toast = Toast.makeText(getContext(),
                    (mOperationType.equals(BaseEditFragment.DETAIL_OPERATION_NEW) || mOperationType.equals(BaseEditFragment.DETAIL_OPERATION_TRACK_TO_MILEAGE)
                            ? mResource.getString(R.string.mileage_edit_insert_ok_message)
                            : mResource.getString(R.string.mileage_edit_update_ok_message)), Toast.LENGTH_SHORT);
            toast.show();
            setUserCommentAdapter();
            setTagAdapter();
        }

        SharedPreferences.Editor prefEditor = mPreferences.edit();
        if (mPreferences.getBoolean(AndiCar.getAppResources().getString(R.string.pref_key_gen_remember_last_tag), false) && mTagId > 0) {
            prefEditor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_gen_last_tag_id), mTagId);
        }

        prefEditor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_driver_id), mDriverId);
        prefEditor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_mileage_last_selected_exptype_id), mExpTypeId);
        prefEditor.apply();

        //check if mileage to-do exists
        Intent intent = new Intent(getActivity(), ToDoNotificationService.class);
        intent.putExtra(ToDoManagementService.SET_JUST_NEXT_RUN_KEY, false);
        intent.putExtra(ToDoManagementService.CAR_ID_KEY, mCarId);
        getActivity().startService(intent);

        Bundle analyticsParams = new Bundle();
        analyticsParams.putInt(ConstantValues.ANALYTICS_IS_TEMPLATE_USED, isTemplateUsed ? 1 : 0);
        Utils.sendAnalyticsEvent(getActivity(), "MileageEdit", analyticsParams, false);

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("mUOMLengthId", mUOMLengthId);
        outState.putLong("mGpsTrackId", mGpsTrackId);
        outState.putLong("mlDateTimeToInMillis", mlDateTimeToInMillis);
        outState.putInt("mYearTo", mYearTo);
        outState.putInt("mMonthTo", mMonthTo);
        outState.putInt("mDayTo", mDayTo);
        outState.putInt("mHourTo", mHourTo);
        outState.putInt("mMinuteTo", mMinuteTo);
        outState.putInt("mInsertMode", mInsertMode);
        outState.putString("mCarCurrencyCode", mCarCurrencyCode);

        if (mNewIndex != null) {
            outState.putString("mNewIndex", mNewIndex.toString());
        }
        if (mStartIndex != null) {
            outState.putString("mStartIndex", mStartIndex.toString());
        }
        if (mStopIndex != null) {
            outState.putString("mStopIndex", mStopIndex.toString());
        }
        if (mUserInputValue != null) {
            outState.putString("mUserInputValue", mUserInputValue.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//		etStartIndex.setText(Utils.numberToString(mStartIndex, false, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH));
        calculateMileageOrNewIndex();
        calculateTripDuration();
        showTripDuration();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (isVisible()) {
            if (mGpsTrackId > -1 && !mOperationType.equals(BaseEditFragment.DETAIL_OPERATION_TRACK_TO_MILEAGE)) {
                inflater.inflate(R.menu.menu_mileage_detail_additional, menu);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_show_gpstrack_on_map) {
            Intent gpstrackShowMapIntent = new Intent(getActivity(), GPSTrackMap.class);
            gpstrackShowMapIntent.putExtra(GPSTrackMap.GPS_TRACK_ID, mGpsTrackId);
            startActivity(gpstrackShowMapIntent);
            return true;
        }
        if (id == R.id.action_show_gpstrack_edit) {
            Intent gpstrackEditIntent = new Intent(getActivity(), CommonDetailActivity.class);
            gpstrackEditIntent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_GPS_TRACK);
            gpstrackEditIntent.putExtra(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_EDIT);
            gpstrackEditIntent.putExtra(BaseEditFragment.RECORD_ID_KEY, mGpsTrackId);
            startActivity(gpstrackEditIntent);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    void calculateTripDuration() {
        mlTripDurationInSeconds = (mlDateTimeToInMillis - mlDateTimeInMillis) / 1000;
    }

    void showTripDuration() {
        if (mlTripDurationInSeconds > 0) {
            tvTripDurationContent.setVisibility(View.VISIBLE);
            tvTripDurationContent.setText("; " + mResource.getString(R.string.gen_duration) + " " + Utils.getDaysHoursMinutesFromSec(mlTripDurationInSeconds));
        }
        else {
            tvTripDurationContent.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    public void calculateMileageOrNewIndex() throws NumberFormatException {
        try {
            if (mInsertMode == INSERT_MODE_INDEX) { //new index
                mNewIndex = mUserInputValue;
                if (mNewIndex.compareTo(mStartIndex) < 0) {
                    tvCalculatedTextContent.setText("N/A");
                }
                else {
                    BigDecimal mileage = mNewIndex.subtract(mStartIndex);
                    tvCalculatedTextContent.setText(Utils.numberToString(mileage, true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) + " "
                            + mDbAdapter.getUOMCode(mUOMLengthId));
                }
            }
            else { //mileage
                mNewIndex = mStartIndex.add(mUserInputValue);
                tvCalculatedTextContent.setText(Utils.numberToString(mNewIndex, true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) + " "
                        + mDbAdapter.getUOMCode(mUOMLengthId));
            }
            showReimbursementValue();
        }
        catch (NumberFormatException e) {
            Toast.makeText(getContext(), mResource.getString(R.string.gen_invalid_number), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void showReimbursementValue() {
        if (!mReimbursementCanCalculated || mNewIndex == null) {
            return;
        }
        try {
            BigDecimal mReimbursementRate = mDbAdapter.getReimbursementRate(mCarId, mExpTypeId, mlDateTimeInMillis / 1000);
            if (mReimbursementRate.compareTo(BigDecimal.ZERO) != 0 && mNewIndex.compareTo(mStartIndex) > 0) {
                tvReimbursementValue.setVisibility(View.VISIBLE);
                tvReimbursementValue.setText(mResource.getString(R.string.gen_reimbursement)
                        + " "
                        + Utils.numberToString((mNewIndex.subtract(mStartIndex)).multiply(mReimbursementRate), true, ConstantValues.DECIMALS_RATES,
                        ConstantValues.ROUNDING_MODE_RATES) + " " + mCarCurrencyCode);
            }
            else {
                tvReimbursementValue.setVisibility(View.GONE);
            }
        }
        catch (Exception e) {
            mReimbursementCanCalculated = false; //avoid subsequent exceptions
            tvReimbursementValue.setVisibility(View.GONE);
            Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), e.getMessage(), e, false);
        }
    }

    @Override
    void showDateTime() {
        super.showDateTime();
        showReimbursementValue();
        if (mlDateTimeInMillis > mlDateTimeToInMillis) {
            mlDateTimeToInMillis = mlDateTimeInMillis;
            mYearTo = mYear;
            mMonthTo = mMonth;
            mDayTo = mDay;
            mHourTo = mHour;
            mMinuteTo = mMinute;
            showDateTimeTo();
        }
    }

    @Override
    public void setCarId(long carId) {
        super.setCarId(carId);

        if (!viewsLoaded) {
            return;
        }

        if (mOperationType.equals(BaseEditFragment.DETAIL_OPERATION_EDIT) || mOperationType.equals(BaseEditFragment.DETAIL_OPERATION_VIEW)) {
            return;
        }

        mUOMLengthId = mDbAdapter.getCarUOMLengthID(carId);
        mCarCurrencyCode = mDbAdapter.getCurrencyCode(mDbAdapter.getCarCurrencyID(carId));

        //check if draft mileage exists for the selected car
        mRowId = getDraftIDIfExists(carId);
        if (mRowId > -1) { //draft exists => switch to edit mode
            mOperationType = BaseEditFragment.DETAIL_OPERATION_EDIT;
            mDraftEdit = true;
            loadDataFromDB();
            showValuesInUI();
            initDateTimeFields();
            initDateTimeToFields();
            showDateTime();
            showDateTimeTo();
            showTripDuration();
        }
        else {
            mRowId = -1;
//			mOperationType = BaseEditFragment.DETAIL_OPERATION_NEW;
            mDraftEdit = false;
            mStartIndex = BigDecimal.ZERO;
            getStartIndex();
            etStartIndex.setText(Utils.numberToString(mStartIndex, false, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH));
            calculateMileageOrNewIndex();
            showReimbursementValue();
        }
    }

    @Override
    public void setExpTypeId(long expTypeId) {
        super.setExpTypeId(expTypeId);
        showReimbursementValue();
    }

    private void showDateTimeTo() {
        if (tvDateTimeToValue == null) {
            return;
        }

        tvDateTimeToValue.setText(DateFormat.getDateFormat(getContext()).format(mDateTimeToCalendar.getTime()) + " "
                + DateFormat.getTimeFormat(getContext()).format(mDateTimeToCalendar.getTime()));
    }

    private long getDraftIDIfExists(long carID) {
        long retVal = -1;

        String selectionCondition = DBAdapter.COL_NAME_MILEAGE__INDEXSTOP + " IS NULL AND " + DBAdapter.COL_NAME_MILEAGE__CAR_ID + " = ?";
        String[] selectionArgs = {Long.toString(carID)};
        Cursor c = mDbAdapter.query(DBAdapter.TABLE_NAME_MILEAGE, DBAdapter.COL_LIST_MILEAGE_TABLE, selectionCondition, selectionArgs, DBAdapter.COL_NAME_GEN_ROWID);
        if (c.moveToFirst()) { //draft exists => switch to edit mode
            retVal = c.getLong(DBAdapter.COL_POS_GEN_ROWID);
        }
        c.close();
        return retVal;
    }

    private void getStartIndex() throws SQLException {
        try {
            if (mStartIndex.equals(new BigDecimal("0"))) {
                mStartIndex = mDbAdapter.getCarLastMileageIndex(mCarId);
            }
        }
        catch (NumberFormatException ignored) {
        }
    }

    @SuppressLint("WrongConstant")
    private void initDateTimeToFields() {
        mDateTimeToCalendar.setTimeInMillis(mlDateTimeToInMillis);
        mYearTo = mDateTimeToCalendar.get(Calendar.YEAR);
        mMonthTo = mDateTimeToCalendar.get(Calendar.MONTH);
        mDayTo = mDateTimeToCalendar.get(Calendar.DAY_OF_MONTH);
        mHourTo = mDateTimeToCalendar.get(Calendar.HOUR_OF_DAY);
        mMinuteTo = mDateTimeToCalendar.get(Calendar.MINUTE);
        mDateTimeToCalendar.set(mYearTo, mMonthTo, mDayTo, mHourTo, mMinuteTo, 0); //reset seconds to 0
        mlDateTimeToInMillis = mDateTimeToCalendar.getTimeInMillis();
    }

    @SuppressLint("SetTextI18n")
    public void setInsertMode(int insertMode) {
        mInsertMode = insertMode;
        if (mInsertMode == INSERT_MODE_INDEX) {
            tvCalculatedTextLabel.setText(mResource.getString(R.string.mileage_edit_option_mileage_label) + ": ");
//            etUserInput.setTag(mResource.getString(R.string.gen_stop_label));
        }
        else {
            tvCalculatedTextLabel.setText(mResource.getString(R.string.gen_index_label2) + ": ");
//            etUserInput.setTag(mResource.getString(R.string.mileage_edit_option_mileage_label));
        }
    }

}
