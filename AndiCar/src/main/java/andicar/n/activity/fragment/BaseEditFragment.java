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
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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
import andicar.n.activity.CreateMileageActivity;
import andicar.n.persistence.DBAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.DataEntryTemplate;
import andicar.n.utils.Utils;

/**
 * Base class for all edit activities. Implement common functionality:
 * -initialise global resources: Bundle mArgumentsBundle, Resources mResource, SharedPreferences mPreferences
 * <p>
 * -implement common initialisations routines for:
 * -spinners: see initSpinner
 */

//source EditActivityBase
@SuppressWarnings("unused")
public abstract class BaseEditFragment extends Fragment {
//        implements OnKeyListener {

    public static final String DETAIL_OPERATION_KEY = "Operation";
    public static final String DETAIL_OPERATION_NEW = "N";
    public static final String DETAIL_OPERATION_EDIT = "E";
    public static final String DETAIL_OPERATION_VIEW = "V";
    public static final String DETAIL_OPERATION_TRACK_TO_MILEAGE = "TTM";
    public static final String DETAIL_PANEL_HIDE_FILL_VIEWS_KEY = "HideFillViews";
    public static final String RECORD_ID_KEY = "RecordID";
    /**
     * used in ExpenseCategory fragment to differentiate fuel categories from other expense categories
     */
    public static final String IS_FUEL_KEY = "IsFuel";
    public static final String BPARTNER_ID_KEY = "mBPartnerId";
    public final View.OnTouchListener spinnerOnTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent me) {
            return false;
        }
    };

    //common ui elements. some fragments can have additional elements
    public View mRootView;
    //for analytics
    public boolean isTemplateUsed = false;
    //common resources used by all edit fragments
    protected Resources mResource = null;
    protected SharedPreferences mPreferences;
    protected Bundle mArgumentsBundle = null;
    protected DBAdapter mDbAdapter;
    //common fields storing the current record _id
    protected long mRowId = -1;
    protected long mlDateTimeInMillis;
    protected long mCarId = -1;
    protected long mDriverId = -1;
    protected long mExpTypeId = -1;
    protected long mExpCatOrFuelTypeId = -1;
    protected long mCurrencyId = -1;
    protected long mTagId = 0;
    protected int mYear;
    protected int mMonth;
    protected int mDay;
    protected int mHour;
    protected int mMinute;
    protected String mOperationType = null;
    protected String mUserComment = null;
    protected String mTagStr = null;
    protected String mName;
    protected ArrayAdapter<String> mUserCommentAdapter;
    protected ArrayAdapter<String> mTagAdapter;
    //the detail fragment is read only in twoPanel (view) mode.
    protected boolean mIsEditable = true;
    //	private boolean isBackgroundSettingsActive = false;
    protected boolean isTimeOnly = false;
    protected boolean isFinishAfterSave = true;
    protected boolean isUseTemplate = false;
    protected boolean mIsActive;
    protected Calendar mDateTimeCalendar = Calendar.getInstance();
    protected ViewGroup vgRoot;
    protected LinearLayout lCarZone;
    protected LinearLayout lDriverZone;
    protected LinearLayout lExpTypeZone;
    protected LinearLayout lExpCatFuelTypeZone;
    protected LinearLayout whenInDialogButtons;
    protected Spinner spnCar;
    protected Spinner spnDriver;
    protected Spinner spnExpType;
    protected Spinner spnExpCatOrFuelType;
    protected Spinner spnCurrency;
    protected TextView tvDateTimeValue;
    protected TextView tvDebugInfo;
    protected EditText etName = null;
    protected EditText etDocumentNo = null;
    protected AutoCompleteTextView acUserComment = null;
    protected AutoCompleteTextView acTag = null;
    protected CheckBox ckIsActive;
    protected boolean viewsLoaded = false;
    //used when only the hour:minute are set (Tasks)
    protected TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @SuppressLint("WrongConstant")
        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
            mDateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mDateTimeCalendar.set(Calendar.MINUTE, minute);
            mlDateTimeInMillis = mDateTimeCalendar.getTimeInMillis();
            initDateTimeFields();
            showDateTime();
        }
    };
    private EditText mEmptyMandatoryField = null;
    private Drawable mStandardFieldBackground = null;
    private DataEntryTemplate mDET = null;

    abstract protected boolean saveData();

    /**
     * Loads the fragment specific UI elements from the layout file
     */
    abstract protected void loadSpecificViewsFromLayoutXML();

    /**
     * Data initialisation for specific UI elements
     */
    abstract protected void initSpecificControls();

    /**
     * Show the values of the fields variables)
     */
    abstract protected void showValuesInUI();

    abstract public void setSpecificLayout();

    /**
     * Called when the activity is first created.
     * In this method only the fields (global variables) are loaded and initialized.
     * The UI elements are loaded and initialized in the onCreateView method.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResource = getResources();
        mArgumentsBundle = getArguments();
        mDbAdapter = new DBAdapter(getContext());
        mPreferences = AndiCar.getDefaultSharedPreferences();

        setHasOptionsMenu(true);

//		boolean isSendCrashReport = mPreferences.getBoolean("SendCrashReport", true);
        mRowId = mArgumentsBundle.getLong(RECORD_ID_KEY, -1L);
        mOperationType = mArgumentsBundle.getString(DETAIL_OPERATION_KEY);
        if (mOperationType != null && mOperationType.equals(DETAIL_OPERATION_VIEW)) {
            mIsEditable = false; //make the ui read only
            mOperationType = DETAIL_OPERATION_EDIT; //to initialise the data from an existing record
        }
        else {
            mIsEditable = true;
        }

        if (savedInstanceState != null) {
            mRowId = savedInstanceState.getLong("mRowId");
            setCarId(savedInstanceState.getLong("mCarId"));
            setDriverId(savedInstanceState.getLong("mDriverId"));
            setExpTypeId(savedInstanceState.getLong("mExpTypeId"));
            setExpCatOrFuelTypeId(savedInstanceState.getLong("mExpCatOrFuelTypeId"));
            setCurrencyId(savedInstanceState.getLong("mCurrencyId"));

            mlDateTimeInMillis = savedInstanceState.getLong("mlDateTimeInMillis");
            mDateTimeCalendar.setTimeInMillis(mlDateTimeInMillis);
            mYear = savedInstanceState.getInt("mYear");
            mMonth = savedInstanceState.getInt("mMonth");
            mDay = savedInstanceState.getInt("mDay");
            mHour = savedInstanceState.getInt("mHour");
            mMinute = savedInstanceState.getInt("mMinute");
            mUserComment = savedInstanceState.getString("mUserComment", null);
            mTagStr = savedInstanceState.getString("mTagStr", null);
            mName = savedInstanceState.getString("mName", null);
            mIsActive = savedInstanceState.getBoolean("mIsActive", true);
            mOperationType = savedInstanceState.getString("mOperationType", "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // inflate the layout based on the fragment type
        if (this instanceof MileageEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_mileage_edit, container, false);
        }
        else if (this instanceof RefuelEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_refuel_edit, container, false);
        }
        else if (this instanceof ExpenseEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_expense_edit, container, false);
        }
        else if (this instanceof GPSTrackEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_gpstrack_edit, container, false);
        }
        else if (this instanceof ToDoViewFragment) {
            mRootView = inflater.inflate(R.layout.fragment_todo_edit, container, false);
        }
        else if (this instanceof GPSTrackControllerFragment) {
            mRootView = inflater.inflate(R.layout.dialog_gpstrack_controller, container, false);
        }
        else if (this instanceof CarEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_car_edit, container, false);
        }
        else if (this instanceof DriverEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_driver_edit, container, false);
        }
        else if (this instanceof UOMEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_uom_edit, container, false);
        }
        else if (this instanceof UOMConversionEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_uom_conversion_edit, container, false);
        }
        else if (this instanceof ExpenseFuelCategoryEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_expense_fuel_category_edit, container, false);
        }
        else if (this instanceof ExpenseTypeEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_expensetype_edit, container, false);
        }
        else if (this instanceof ReimbursementRateEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_reimbursementrate_edit, container, false);
        }
        else if (this instanceof CurrencyEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_currency_edit, container, false);
        }
        else if (this instanceof CurrencyRateEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_currencyrate_edit, container, false);
        }
        else if (this instanceof BPartnerEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_bpartner_edit, container, false);
        }
        else if (this instanceof BPartnerLocationEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_bpartner_location_edit, container, false);
        }
        else if (this instanceof TaskTypeEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_tasktype_edit, container, false);
        }
        else if (this instanceof TaskEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_task_edit, container, false);
        }
        else if (this instanceof BTCarLinkFragment) {
            mRootView = inflater.inflate(R.layout.fragment_bt_car_link, container, false);
        }
        else if (this instanceof TagEditFragment) {
            mRootView = inflater.inflate(R.layout.fragment_tag_edit, container, false);
        }

        View v;
        v = mRootView.findViewById(R.id.fakeFocus);
        if (v != null) {
            v.requestFocus();
        }

        // hide the left and right empty views on small screens
        if (mArgumentsBundle.getBoolean(DETAIL_PANEL_HIDE_FILL_VIEWS_KEY, false)) {
            v = mRootView.findViewById(R.id.leftFillView);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
            v = mRootView.findViewById(R.id.rightFillView);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        }

        // load common ui elements from the layout;
        vgRoot = mRootView.findViewById(R.id.vgRoot);
        lCarZone = mRootView.findViewById(R.id.lCarZone);
        lDriverZone = mRootView.findViewById(R.id.lDriverZone);
        lExpTypeZone = mRootView.findViewById(R.id.lExpTypeZone);
        lExpCatFuelTypeZone = mRootView.findViewById(R.id.lExpCatZone);

        spnCar = mRootView.findViewById(R.id.spnCar);
        if (spnCar != null) {
            spnCar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (adapterView.getTag() != null && adapterView.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                        adapterView.setTag(null);
                        return;
                    }
                    setCarId(mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_CAR, adapterView.getAdapter().getItem(i).toString()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            spnCar.setOnTouchListener(spinnerOnTouchListener);
        }

        spnDriver = mRootView.findViewById(R.id.spnDriver);
        if (spnDriver != null) {
            spnDriver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (adapterView.getTag() != null && adapterView.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                        adapterView.setTag(null);
                        return;
                    }
                    setDriverId(mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_DRIVER, adapterView.getAdapter().getItem(i).toString()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            spnDriver.setOnTouchListener(spinnerOnTouchListener);
        }

        spnExpType = mRootView.findViewById(R.id.spnExpType);
        if (spnExpType != null) {
            spnExpType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (adapterView.getTag() != null && adapterView.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                        adapterView.setTag(null);
                        return;
                    }
                    setExpTypeId(mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_EXPENSETYPE, adapterView.getAdapter().getItem(i).toString()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            spnExpType.setOnTouchListener(spinnerOnTouchListener);
        }

        spnExpCatOrFuelType = mRootView.findViewById(R.id.spnExpCatOrFuelType);
        if (spnExpCatOrFuelType != null) {
            spnExpCatOrFuelType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (adapterView.getTag() != null && adapterView.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                        adapterView.setTag(null);
                        return;
                    }
                    setExpCatOrFuelTypeId(mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_EXPENSECATEGORY, adapterView.getAdapter().getItem(i).toString()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
        spnCurrency = mRootView.findViewById(R.id.spnCurrency);
        if (spnCurrency != null) {
            spnCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    long newId = mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_CURRENCY, adapterView.getAdapter().getItem(i).toString());
                    setSpinnerTextToCode(adapterView, newId, view);

                    if (adapterView.getTag() != null && adapterView.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                        adapterView.setTag(null);
                        return;
                    }
                    setCurrencyId(mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_CURRENCY, adapterView.getAdapter().getItem(i).toString()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        acUserComment = mRootView.findViewById(R.id.acUserComment);
        acTag = mRootView.findViewById(R.id.acTag);

        etName = mRootView.findViewById(R.id.etName);
        etDocumentNo = mRootView.findViewById(R.id.etDocumentNo);

        tvDateTimeValue = mRootView.findViewById(R.id.tvDateTimeValue);

        ckIsActive = mRootView.findViewById(R.id.ckIsActive);

//        whenInDialogButtons = mRootView.findViewById(R.id.whenInDialogButtons);
//        if (whenInDialogButtons != null) {
//            if (getActivity() instanceof CommonDetailActivity) {
//                whenInDialogButtons.setVisibility(View.GONE);
//            }
//            else {
//                Button btnCancel = mRootView.findViewById(R.id.btnCancel);
//                if (btnCancel != null) {
//                    btnCancel.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            getActivity().finish();
//Y                            ;
//                        }
//                    });
//                }
//
//                Button btnSave = mRootView.findViewById(R.id.btnSave);
//                if (btnSave != null) {
//                    btnSave.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            actionDone();
//                        }
//                    });
//                }
//            }
//        }
//
        // load the specific UI elements for each type of fragment
        loadSpecificViewsFromLayoutXML();

        setViewsEditable(vgRoot, mIsEditable);

        initCommonControls();
        initSpecificControls();

        showValuesInUI();

        return mRootView;
    }

    /**
     * Data initialisation for common UI elements
     */
    @SuppressWarnings("UnusedAssignment")
    @SuppressLint("SetTextI18n")
    protected void initCommonControls() {
        long checkID;
        if (spnCar != null) {
            mCarId = Utils.initSpinner(mDbAdapter, spnCar, DBAdapter.TABLE_NAME_CAR, DBAdapter.WHERE_CONDITION_ISACTIVE, mCarId, mRowId > 0, false);
            if (spnCar.getAdapter().getCount() == 1 && lCarZone != null)
                lCarZone.setVisibility(View.GONE);
            else if (lCarZone != null)
                lCarZone.setVisibility(View.VISIBLE);
        }

        if (spnDriver != null) {
            mDriverId = Utils.initSpinner(mDbAdapter, spnDriver, DBAdapter.TABLE_NAME_DRIVER, DBAdapter.WHERE_CONDITION_ISACTIVE, mDriverId, mRowId > 0, false);
            if (spnDriver.getAdapter().getCount() == 1 && lDriverZone != null)
                lDriverZone.setVisibility(View.GONE);
            else if (lDriverZone != null)
                lDriverZone.setVisibility(View.VISIBLE);
        }

        if (spnExpType != null) {
            mExpTypeId = Utils.initSpinner(mDbAdapter, spnExpType, DBAdapter.TABLE_NAME_EXPENSETYPE, DBAdapter.WHERE_CONDITION_ISACTIVE, mExpTypeId, mRowId > 0, false);
            if (spnExpType.getAdapter().getCount() == 1 && lExpTypeZone != null)
                lExpTypeZone.setVisibility(View.GONE);
            else if (lExpTypeZone != null)
                lExpTypeZone.setVisibility(View.VISIBLE);
        }

        initSpnExpCatOrFuelType();

        if (spnCurrency != null) {
            mCurrencyId = Utils.initSpinner(mDbAdapter, spnCurrency, DBAdapter.TABLE_NAME_CURRENCY,
                    DBAdapter.WHERE_CONDITION_ISACTIVE, mCurrencyId, mRowId > 0, false);
        }

        if (acTag != null) {
            setTagAdapter();
        }
        if (acUserComment != null) {
            setUserCommentAdapter();
        }

        if (tvDateTimeValue != null) {
            tvDateTimeValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //noinspection WrongConstant
//                    if(mDateTimeCalendar.get(Calendar.YEAR) > 1970) {
                    if (isTimeOnly) {
                        @SuppressLint("WrongConstant") TimePickerDialog tp = new TimePickerDialog(BaseEditFragment.this.getActivity(), mTimeSetListener, mDateTimeCalendar.get(Calendar.HOUR_OF_DAY), mDateTimeCalendar.get(Calendar.MINUTE), true);
                        tp.show();
                    }
                    else {
//                        new SlideDateTimePicker.Builder(BaseEditFragment.this.getActivity().getSupportFragmentManager())
//                                .setListener(mDateTimeChangeListener)
//                                .setInitialDate(new Date(mlDateTimeInMillis))
//                                .setIndicatorColor(ContextCompat.getColor(BaseEditFragment.this.getContext(), R.color.primary_accent))
//                                .build()
//                                .show();

                        SwitchDateTimeDialogFragment dateTimeDialogFragment;
                        if (BaseEditFragment.this instanceof MileageEditFragment) {
                            dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                                    getString(R.string.gen_start_label),
                                    getString(R.string.gen_ok),
                                    getString(R.string.gen_cancel)
                            );
                        }
                        else {
                            dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                                    getString(R.string.gen_set_date),
                                    getString(R.string.gen_ok),
                                    getString(R.string.gen_cancel)
                            );
                        }
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
                                mlDateTimeInMillis = date.getTime();
                                initDateTimeFields();
                                showDateTime();
                                if (BaseEditFragment.this instanceof MileageEditFragment) {
                                    ((MileageEditFragment) BaseEditFragment.this).calculateTripDuration();
                                    ((MileageEditFragment) BaseEditFragment.this).showTripDuration();
                                }
                            }

                            @Override
                            public void onNegativeButtonClick(Date date) {
                                // Date is get on negative button click
                            }
                        });

                        // Show
                        if (BaseEditFragment.this.getActivity() != null)
                            dateTimeDialogFragment.show(BaseEditFragment.this.getActivity().getSupportFragmentManager(), "dialog_time");
                    }
                }
            });
        }

        //for debug
        tvDebugInfo = mRootView.findViewById(R.id.tvDebugInfo);
        if (tvDebugInfo != null) {
            if (Utils.isDebugVersion() && ConstantValues.DEBUG_IS_SHOW_INFO_IN_FRAGMENTS && getActivity() != null) {
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                float density = getResources().getDisplayMetrics().density;
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                tvDebugInfo.setText(tvDebugInfo.getText() + "; Size in pixel: " + width + " x " + height + "; Size in dp: " + (width / density) + " x " + (height / density) + "; Density: " + density);
            }
            else {
                tvDebugInfo.setVisibility(View.GONE);
            }
        }

        if (isUseTemplate) {
            mDET = new DataEntryTemplate(this, mDbAdapter);
        }
    }

    protected void initSpnExpCatOrFuelType() {
        if (spnExpCatOrFuelType != null) {
            if (this instanceof RefuelEditFragment) {
                mExpCatOrFuelTypeId = Utils.initSpinner(mDbAdapter, spnExpCatOrFuelType, DBAdapter.TABLE_NAME_EXPENSECATEGORY,
                        DBAdapter.WHERE_CONDITION_ISACTIVE + " AND " + DBAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'Y'", mExpCatOrFuelTypeId, mRowId > 0, false);
            }
            else {
                mExpCatOrFuelTypeId = Utils.initSpinner(mDbAdapter, spnExpCatOrFuelType, DBAdapter.TABLE_NAME_EXPENSECATEGORY,
                        DBAdapter.WHERE_CONDITION_ISACTIVE + " AND " + DBAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'N'", mExpCatOrFuelTypeId, mRowId > 0, false);
            }
            if (spnExpCatOrFuelType.getAdapter().getCount() == 1 && lExpCatFuelTypeZone != null)
                lExpCatFuelTypeZone.setVisibility(View.GONE);
            else if (lExpCatFuelTypeZone != null)
                lExpCatFuelTypeZone.setVisibility(View.VISIBLE);

        }
    }

    public void initDefaultValues() {
        mName = null;
        mUserComment = null;
        mIsActive = true;

        setCarId(mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_car_id), -1));
        if (mCarId == -1) {
            setCarId(mDbAdapter.getFirstActiveID(DBAdapter.TABLE_NAME_CAR, null, DBAdapter.COL_NAME_GEN_NAME));
        }

        setDriverId(mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_driver_id), -1));
        if (mDriverId == -1 ||
                !mDbAdapter.isIDActive(DBAdapter.TABLE_NAME_DRIVER, mDriverId)) {
            setDriverId(mDbAdapter.getFirstActiveID(DBAdapter.TABLE_NAME_DRIVER, null, DBAdapter.COL_NAME_GEN_NAME));
        }

        if (this instanceof RefuelEditFragment) {
            setExpTypeId(mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_refuel_last_selected_expense_type_id), -1));
            setExpCatOrFuelTypeId(mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_refuel_last_selected_expense_category_id) + "_" + mCarId, -1));
        }
        else if (this instanceof ExpenseEditFragment) {
            setExpTypeId(mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_expense_last_selected_expense_type_id), -1));
            setExpCatOrFuelTypeId(mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_expense_last_selected_expense_category_id), -1));
        }
        else if (this instanceof MileageEditFragment) {
            setExpTypeId(mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_mileage_last_selected_expense_type_id), -1));
            setExpCatOrFuelTypeId(-1);
        }
        else if (this instanceof GPSTrackControllerFragment) {
            setExpTypeId(mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_gps_track_last_selected_expense_type_id), -1));
            setExpCatOrFuelTypeId(-1);
        }
        else {
            setExpTypeId(-1);
            setExpCatOrFuelTypeId(-1);
        }

        if (mExpTypeId == -1 ||
                !mDbAdapter.isIDActive(DBAdapter.TABLE_NAME_EXPENSETYPE, mExpTypeId)) {
            setExpTypeId(mDbAdapter.getFirstActiveID(DBAdapter.TABLE_NAME_EXPENSETYPE, null, DBAdapter.COL_NAME_GEN_NAME));
        }
        if (mExpCatOrFuelTypeId == -1 ||
                !mDbAdapter.isIDActive(DBAdapter.TABLE_NAME_EXPENSECATEGORY, mExpCatOrFuelTypeId)) {
            if (this instanceof RefuelEditFragment) {
                String selection = DBAdapter.WHERE_CONDITION_ISACTIVE + " AND " + DBAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'Y' ";
                if (mDbAdapter.isAFVCar(mCarId)) {
                    selection = selection +
                            " AND " + DBAdapter.COL_NAME_EXPENSECATEGORY__UOMTYPE + " IN( '" + mDbAdapter.getCarFuelUOMType(mCarId, true) + "', '" +
                            mDbAdapter.getCarFuelUOMType(mCarId, false) + "')";
                } else {
                    selection = selection +
                            " AND " + DBAdapter.COL_NAME_EXPENSECATEGORY__UOMTYPE + " = '" + mDbAdapter.getCarFuelUOMType(mCarId, true) + "'";
                }
                setExpCatOrFuelTypeId(mDbAdapter.getFirstActiveID(DBAdapter.TABLE_NAME_EXPENSECATEGORY,
                        selection, DBAdapter.COL_NAME_GEN_NAME));
            }
            else {
                setExpCatOrFuelTypeId(mDbAdapter.getFirstActiveID(DBAdapter.TABLE_NAME_EXPENSECATEGORY, " AND " + DBAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'N'", DBAdapter.COL_NAME_GEN_NAME));
            }
        }

        mlDateTimeInMillis = System.currentTimeMillis();

        if (mDET != null) {
            mDET.clearSelected();
        }

        initDateTimeFields();
    }

    @SuppressLint("WrongConstant")
    void initDateTimeFields() {
        mDateTimeCalendar.setTimeInMillis(mlDateTimeInMillis);
        if (isTimeOnly) {
            mDateTimeCalendar.set(1970, Calendar.JANUARY, 1);
        }
        mYear = mDateTimeCalendar.get(Calendar.YEAR);
        mMonth = mDateTimeCalendar.get(Calendar.MONTH);
        mDay = mDateTimeCalendar.get(Calendar.DAY_OF_MONTH);
        mHour = mDateTimeCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mDateTimeCalendar.get(Calendar.MINUTE);
        mDateTimeCalendar.set(mYear, mMonth, mDay, mHour, mMinute, 0); //reset seconds to 0
        mlDateTimeInMillis = mDateTimeCalendar.getTimeInMillis();
    }

    //	void updateDateTimeFields() {
//		mDateTimeCalendar.set(mYear, mMonth, mDay, mHour, mMinute, 0);
//		mlDateTimeInMillis = mDateTimeCalendar.getTimeInMillis() / 1000;
//	void updateDateTime2Fields() {
//		mDateTimeCalendar2.set(mYear2, mMonth2, mDay2, mHour2, mMinute2, 0);
//		mlDateTime2InSeconds = mDateTimeCalendar2.getTimeInMillis() / 1000;
    @SuppressLint("SetTextI18n")
    @SuppressWarnings("WrongConstant")
    void showDateTime() {
//        if(mDateTimeCalendar.get(Calendar.YEAR) == 1970) {
        if (isTimeOnly) {
            tvDateTimeValue.setText(DateFormat.getTimeFormat(getContext()).format(mDateTimeCalendar.getTime()));
        }
        else {
            tvDateTimeValue.setText(DateFormat.getDateFormat(getContext()).format(mDateTimeCalendar.getTime()) + " "
                    + DateFormat.getTimeFormat(getContext()).format(mDateTimeCalendar.getTime()));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("mRowId", mRowId);
        outState.putLong("mCarId", mCarId);
        outState.putLong("mDriverId", mDriverId);
        outState.putLong("mExpTypeId", mExpTypeId);
        outState.putLong("mExpCatOrFuelTypeId", mExpCatOrFuelTypeId);
        outState.putLong("mCurrencyId", mCurrencyId);
        outState.putLong("mlDateTimeInMillis", mlDateTimeInMillis);
        outState.putInt("mYear", mYear);
        outState.putInt("mMonth", mMonth);
        outState.putInt("mDay", mDay);
        outState.putInt("mHour", mHour);
        outState.putInt("mMinute", mMinute);
        outState.putString("mOperationType", mOperationType);
        outState.putString("mUserComment", mUserComment);
        outState.putString("mTagStr", mTagStr);
        outState.putString("mName", mName);
        outState.putBoolean("mIsActive", mIsActive);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDbAdapter != null) {
            mDbAdapter.close();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (mIsEditable) {
            if (mOperationType.equals(DETAIL_OPERATION_EDIT) && !(this instanceof ToDoViewFragment)) {
                inflater.inflate(R.menu.menu_delete_done, menu);
            }
            else {
                inflater.inflate(R.menu.menu_done, menu);
                if ((this instanceof MileageEditFragment && !mOperationType.equals(BaseEditFragment.DETAIL_OPERATION_TRACK_TO_MILEAGE))
                        || this instanceof RefuelEditFragment || this instanceof ExpenseEditFragment) {
                    menu.findItem(R.id.action_done_and_new).setVisible(true);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            actionDone(true);
            return true;
        }
        else if (id == R.id.action_done_and_new) {
            actionDone(false);
            initDefaultValues();
            if (spnCurrency != null) {
                mCurrencyId = Utils.initSpinner(mDbAdapter, spnCurrency, DBAdapter.TABLE_NAME_CURRENCY,
                        DBAdapter.WHERE_CONDITION_ISACTIVE, mCurrencyId, false, false);
            }
            showDateTime();
            showValuesInUI();
            return true;
        } else if (id == R.id.action_delete && getActivity() != null) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle(R.string.gen_confirm);
            alertDialog.setMessage(R.string.gen_delete_confirmation);
            alertDialog.setCancelable(false);

            alertDialog.setPositiveButton(R.string.gen_yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int deleteResult = -1;
                            if (BaseEditFragment.this instanceof MileageEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_MILEAGE, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof RefuelEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_REFUEL, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof GPSTrackEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_GPSTRACK, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof ExpenseEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_EXPENSE, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof CarEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_CAR, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof DriverEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_DRIVER, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof UOMEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_UOM, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof UOMConversionEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_UOMCONVERSION, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof ExpenseFuelCategoryEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_EXPENSECATEGORY, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof ExpenseTypeEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_EXPENSETYPE, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof ReimbursementRateEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_REIMBURSEMENT_CAR_RATES, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof CurrencyEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_CURRENCY, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof CurrencyRateEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_CURRENCYRATE, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof BPartnerEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_BPARTNER, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof BPartnerLocationEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_BPARTNERLOCATION, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof TaskTypeEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_TASKTYPE, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof TaskEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_TASK, mRowId);
                            }
                            else if (BaseEditFragment.this instanceof BTCarLinkFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_BTDEVICE_CAR, mRowId);
                            } else if (BaseEditFragment.this instanceof TagEditFragment) {
                                deleteResult = mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_TAG, mRowId);
                            }

                            if (deleteResult == -1) {
                                if (BaseEditFragment.this.getActivity() instanceof CommonDetailActivity //single panel
                                        || BaseEditFragment.this.getActivity() instanceof CreateMileageActivity)
                                {
                                    BaseEditFragment.this.getActivity().finish();
                                }
                            }
                            else {
                                Utils.showNotReportableErrorDialog(BaseEditFragment.this.getActivity(), BaseEditFragment.this.getString(R.string.gen_error), BaseEditFragment.this.getString(deleteResult));
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
        return super.onOptionsItemSelected(item);
    }

    private void actionDone(boolean finishAfterSave) {
        if (!beforeSave()) {
            return;
        }
        if (saveData() && finishAfterSave && getActivity() != null) {
            getActivity().finish();
        }
    }

//	}

//	}

    protected void setViewsEditable(ViewGroup vg, boolean editable) {
        View vwChild;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vwChild = vg.getChildAt(i);
            if (vwChild instanceof ViewGroup) {
                setViewsEditable((ViewGroup) vwChild, editable);
            }

            if (vwChild.getTag(R.string.pref_key_view_is_always_enabled) == null) {
                vwChild.setEnabled(editable);
            }
            else {
                if (!((Boolean) vwChild.getTag(R.string.pref_key_view_is_always_enabled))) {
                    vwChild.setEnabled(editable);
                }
            }
//			if (vwChild.getId() != R.id.btnGPSTrackShowOnMap
//					&& vwChild.getId() != R.id.btnGPSTrackSendAsEmail
//					&& vwChild.getId() != R.id.btnGPSTrackPauseResume
//					&& vwChild.getId() != R.id.btnGPSTrackStartStop) {
//				vwChild.setEnabled(editable);
//			}
            if (vwChild instanceof TextView && !editable) {
                ((TextView) vwChild).setHint("");
            }
        }
    }

    /**
     * called before saving data
     */
    private boolean beforeSave() {

        //check mandatory fields
        int notFilledViewID = Utils.checkMandatoryFields(vgRoot);
        if (notFilledViewID > -1) {
            mEmptyMandatoryField = mRootView.findViewById(notFilledViewID);
            if (mStandardFieldBackground == null) {
                mStandardFieldBackground = mEmptyMandatoryField.getBackground();
            }

            mEmptyMandatoryField.setBackgroundResource(R.drawable.ui_mandatory_border_edittext);
            mEmptyMandatoryField.requestFocus();

            Toast.makeText(getActivity(), getString(R.string.gen_fill_mandatory), Toast.LENGTH_LONG).show();

            //clear the mandatory border when a text is entered
            mEmptyMandatoryField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    mEmptyMandatoryField.setBackground(mStandardFieldBackground);
                }
            });

            return false;
        }

        String strRetVal = checkNumeric(vgRoot, false);
        if (strRetVal != null) {
            Toast toast = Toast.makeText(getContext(), mResource.getString(R.string.gen_invalid_number) + ": " + strRetVal,
                    Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }

        return true;
    }

    //imported from BaseActivity
    protected String checkNumeric(ViewGroup wg, boolean integerOnly) {
        View vwChild;
        EditText etChild;
        String strRetVal;
        if (wg == null) {
            return null;
        }

        for (int i = 0; i < wg.getChildCount(); i++) {
            vwChild = wg.getChildAt(i);
            if (vwChild instanceof ViewGroup) {
                strRetVal = checkNumeric((ViewGroup) vwChild, integerOnly);
                if (strRetVal != null) {
                    return strRetVal;
                }
            }
            else if (vwChild instanceof EditText) {
                etChild = (EditText) vwChild;
                String sValue = etChild.getText().toString();
                if (etChild.getOnFocusChangeListener() == null
                        && (etChild.getInputType() == InputType.TYPE_CLASS_PHONE || etChild.getInputType() == InputType.TYPE_CLASS_NUMBER)) {
                    if (sValue.length() > 0) {
                        try {
                            //check if valid number
                            if (integerOnly)
                            {
                                //noinspection ResultOfMethodCallIgnored
                                Integer.parseInt(sValue);
                            }
                            else {
                                new BigDecimal(sValue);
                            }
                        }
                        catch (NumberFormatException e) {
                            if (etChild.getTag() != null && etChild.getTag().toString() != null) {
                                return etChild.getTag().toString().replace(":", "");
                            }
                            else {
                                return "";
                            }

                        }
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    public void setSpinnerSelectedID(Spinner sp, String text) {
        ArrayAdapter sca = (ArrayAdapter) sp.getAdapter();
        if (sca == null) {
            return;
        }

        for (int i = 0; i < sca.getCount(); i++) {
            if (sca.getItem(i) != null && sca.getItem(i).toString().equals(text)) {
                sp.setSelection(i);
                return;
            }
        }
    }

    public long getCarId() {
        return mCarId;
    }

    public void setCarId(long carId) {
        mCarId = carId;
    }

    public long getDriverId() {
        return mDriverId;
    }

    public void setDriverId(long driverId) {
        mDriverId = driverId;
    }

    public long getExpTypeId() {
        return mExpTypeId;
    }

    public void setExpTypeId(long expTypeId) {
        mExpTypeId = expTypeId;
    }

    public String getOperationType() {
        return mOperationType;
    }

    protected void setUserCommentAdapter() {
        mUserCommentAdapter = null;
        String[] comments = null;
        if (this instanceof MileageEditFragment) {
            comments = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_MILEAGE, null, null, mCarId, 60);
        }
        else if (this instanceof RefuelEditFragment) {
            comments = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_REFUEL, null, null, mCarId, 60);
        }
        else if (this instanceof ExpenseEditFragment) {
            comments = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_EXPENSE, null, null, mCarId, 60);
        }
        else if (this instanceof GPSTrackEditFragment) {
            comments = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_GPSTRACK, null, null, mCarId, 60);
        }
        else if (this instanceof GPSTrackControllerFragment) {
            comments = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_GPSTRACK, null, null, mCarId, 60);
        }

        if (comments != null && getContext() != null) {
            mUserCommentAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, comments);
        }

        acUserComment.setAdapter(mUserCommentAdapter);
    }

    protected void setTagAdapter() {
        mTagAdapter = null;
        String[] tags;
//		if(this instanceof MileageEditFragment)
        tags = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_TAG, null, null, 0, 0);

        if (tags != null && getContext() != null) {
            mTagAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, tags);
        }

        acTag.setAdapter(mTagAdapter);
    }

    void setSpinnerTextToCode(AdapterView<?> arg0, long arg3, View arg1) {
        if (arg1 == null) {
            return;
        }
        String code;
        Spinner tmpSpinner = (Spinner) arg0;
        //set the spinner text to the selected item code
        if ((tmpSpinner.equals(mRootView.findViewById(R.id.spnUomFrom)) || tmpSpinner.equals(mRootView.findViewById(R.id.spnUomTo))
                || tmpSpinner.equals(mRootView.findViewById(R.id.spnUomLength))
                || tmpSpinner.equals(mRootView.findViewById(R.id.spnUomFuel))
                || tmpSpinner.equals(mRootView.findViewById(R.id.spnUOMAltFuel))
                || tmpSpinner.equals(mRootView.findViewById(R.id.spnExpCatOrFuelType))
                || tmpSpinner.equals(mRootView.findViewById(R.id.spnUOM)))
                && arg3 > 0) {
            code = mDbAdapter.getUOMCode(arg3);
            if (code != null) {
                ((TextView) arg1).setText(code);
            }
        }
        else if ((tmpSpinner.equals(mRootView.findViewById(R.id.spnCurrency))
                || tmpSpinner.equals(mRootView.findViewById(R.id.spnCurrencyTo))) && arg3 > 0) {
            code = mDbAdapter.getCurrencyCode(arg3);
            if (code != null) {
                ((TextView) arg1).setText(code);
            }
        }
    }

    public long getExpCategoryId() {
        return mExpCatOrFuelTypeId;
    }

    public void setExpCatOrFuelTypeId(long expCatOrFuelTypeId) {
        mExpCatOrFuelTypeId = expCatOrFuelTypeId;
    }

    public long getCurrencyId() {
        return mCurrencyId;
    }

    public void setCurrencyId(long currencyId) {
        mCurrencyId = currencyId;
    }

}
