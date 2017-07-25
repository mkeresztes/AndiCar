package andicar.n.activity.fragment;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.math.BigDecimal;
import java.util.Calendar;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by miki on 02.02.2017.
 */

public class ReimbursementRateEditFragment extends BaseEditFragment {

    private final Calendar calValidFrom = Calendar.getInstance();
    private final Calendar calValidTo = Calendar.getInstance();
    private TextView tvValidFromValue;
    private TextView tvValidToValue;
    private EditText etRate;
    private TextView tvRateUOM;
    private long mValidFromDate;
    private long mValidToDate;
    private final DatePickerDialog.OnDateSetListener dateChanged = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            if (datePicker.getTag().equals(mResource.getString(R.string.gen_valid_from_label))) {
                calValidFrom.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
                mValidFromDate = calValidFrom.getTimeInMillis();
                tvValidFromValue.setText(DateFormat.getDateFormat(getContext()).format(calValidFrom.getTime()));
            }
            else {
                calValidTo.set(year, monthOfYear, dayOfMonth, 23, 59, 59);
                mValidToDate = calValidTo.getTimeInMillis();
                tvValidToValue.setText(DateFormat.getDateFormat(getContext()).format(calValidTo.getTime()));
            }
        }
    };
    private String mRate;
    private String mRateUOM;
    private String mCarCurrencyCode;

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
        else {
            mValidFromDate = savedInstanceState.getLong("mValidFromDate");
            calValidFrom.setTimeInMillis(mValidFromDate);
            mValidToDate = savedInstanceState.getLong("mValidToDate");
            calValidTo.setTimeInMillis(mValidToDate);
            mRate = savedInstanceState.getString("mRate");
            mCarCurrencyCode = savedInstanceState.getString("mCarCurrencyCode");
            mRateUOM = savedInstanceState.getString("mRateUOM");
        }
    }

    private void loadDataFromDB() {
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_REIMBURSEMENT_CAR_RATES, DBAdapter.COL_LIST_REIMBURSEMENT_CAR_RATES_TABLE, mRowId);

        assert c != null;
        setExpTypeId(c.getLong(DBAdapter.COL_POS_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID));
        setCarId(c.getLong(DBAdapter.COL_POS_REIMBURSEMENT_CAR_RATES__CAR_ID));
        mRate = Utils.numberToString((new BigDecimal(c.getDouble(DBAdapter.COL_POS_REIMBURSEMENT_CAR_RATES__RATE)).stripTrailingZeros()), false,
                ConstantValues.DECIMALS_RATES, ConstantValues.ROUNDING_MODE_RATES);
        mValidFromDate = c.getLong(DBAdapter.COL_POS_REIMBURSEMENT_CAR_RATES__VALIDFROM) * 1000;
        calValidFrom.setTimeInMillis(mValidFromDate);
        mValidToDate = c.getLong(DBAdapter.COL_POS_REIMBURSEMENT_CAR_RATES__VALIDTO) * 1000;
        calValidTo.setTimeInMillis(mValidToDate);

        c.close();

        mCarCurrencyCode = mDbAdapter.getCurrencyCode(mDbAdapter.getCarCurrencyID(mCarId));
        mRateUOM = mDbAdapter.getUOMCode(mDbAdapter.getCarUOMLengthID(mCarId));
    }

    @Override
    public void initDefaultValues() {
        super.initDefaultValues();

        calValidFrom.set(calValidFrom.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
        mValidFromDate = calValidFrom.getTimeInMillis();
        calValidTo.set(calValidFrom.get(Calendar.YEAR), Calendar.DECEMBER, 31, 23, 59, 59);
        mValidToDate = calValidTo.getTimeInMillis();
        mRate = "";
        setCarId(mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_car_id), -1));
        mCarCurrencyCode = mDbAdapter.getCurrencyCode(mDbAdapter.getCarCurrencyID(mCarId));
        setExpTypeId(mDbAdapter.getFirstActiveID(DBAdapter.TABLE_NAME_EXPENSETYPE, null, DBAdapter.COL_NAME_GEN_NAME));
        mRateUOM = mDbAdapter.getUOMCode(mDbAdapter.getCarUOMLengthID(mCarId));
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        tvValidFromValue = mRootView.findViewById(R.id.tvValidFromValue);
        tvValidFromValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dp = new DatePickerDialog(ReimbursementRateEditFragment.this.getActivity(), dateChanged, calValidFrom.get(Calendar.YEAR), calValidFrom.get(Calendar.MONTH), calValidFrom.get(Calendar.DAY_OF_MONTH));
                dp.getDatePicker().setTag(mResource.getString(R.string.gen_valid_from_label));
//                dp.setTitle(mResource.getString(R.string.gen_valid_from_label));
                dp.show();
            }
        });
        tvValidToValue = mRootView.findViewById(R.id.tvValidToValue);
        tvValidToValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dp = new DatePickerDialog(ReimbursementRateEditFragment.this.getActivity(), dateChanged, calValidTo.get(Calendar.YEAR), calValidTo.get(Calendar.MONTH), calValidTo.get(Calendar.DAY_OF_MONTH));
                dp.getDatePicker().setTag(mResource.getString(R.string.gen_valid_to_label));
//                dp.setTitle(mResource.getString(R.string.gen_valid_to_label));
                dp.show();
            }
        });
        etRate = mRootView.findViewById(R.id.etRate);
        tvRateUOM = mRootView.findViewById(R.id.tvRateUOM);

        viewsLoaded = true;
    }

    @Override
    protected void initSpecificControls() {

    }

    @Override
    protected void showValuesInUI() {
        tvValidFromValue.setText(DateFormat.getDateFormat(getContext()).format(calValidFrom.getTime()));
        tvValidToValue.setText(DateFormat.getDateFormat(getContext()).format(calValidTo.getTime()));
        etRate.setText(mRate);
        tvRateUOM.setText(mCarCurrencyCode + " / " + mRateUOM);
    }

    @Override
    public void setSpecificLayout() {

    }

    @Override
    protected boolean saveData() {
        ContentValues data = new ContentValues();
        data.put(DBAdapter.COL_NAME_GEN_NAME, "");
        data.put(DBAdapter.COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID, mExpTypeId);
        data.put(DBAdapter.COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID, mCarId);
        calValidFrom.set(Calendar.HOUR_OF_DAY, 0);
        calValidFrom.set(Calendar.MINUTE, 0);
        calValidFrom.set(Calendar.SECOND, 0);
        calValidFrom.set(Calendar.MILLISECOND, 0);
        calValidTo.set(Calendar.HOUR_OF_DAY, 23);
        calValidTo.set(Calendar.MINUTE, 59);
        calValidTo.set(Calendar.SECOND, 59);
        calValidTo.set(Calendar.MILLISECOND, 999);

        data.put(DBAdapter.COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM, calValidFrom.getTimeInMillis() / 1000);
        data.put(DBAdapter.COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO, calValidTo.getTimeInMillis() / 1000);
        data.put(DBAdapter.COL_NAME_REIMBURSEMENT_CAR_RATES__RATE, etRate.getText().toString());

        int dbRetVal;
        if (mRowId == -1) {
            dbRetVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_REIMBURSEMENT_CAR_RATES, data)).intValue();
            if (dbRetVal > 0) {
                return true;
            }
            else {
                if (dbRetVal == -1) //DB Error
                {
                    Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), mDbAdapter.mErrorMessage, mDbAdapter.mException, false);
                }
                else //precondition error
                {
                    Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(-1 * dbRetVal), false);
                }

                return false;
            }
        }
        else {
            int updResult = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_REIMBURSEMENT_CAR_RATES, mRowId, data);
            if (updResult != -1) {
                String errMsg;
                errMsg = mResource.getString(updResult);
                if (updResult == R.string.error_000) {
                    errMsg = errMsg + "\n" + mDbAdapter.mErrorMessage;
                }
                Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), errMsg, mDbAdapter.mException, false);
                return false;
            }
            else {
                return true;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("mValidFromDate", mValidFromDate);
        outState.putLong("mValidToDate", mValidToDate);
        outState.putString("mRate", mRate);
        outState.putString("mCarCurrencyCode", mCarCurrencyCode);
        outState.putString("mRateUOM", mRateUOM);
    }

    @Override
    public void setCarId(long carId) {
        super.setCarId(carId);

        if (!viewsLoaded) {
            return;
        }

        mCarCurrencyCode = mDbAdapter.getCurrencyCode(mDbAdapter.getCarCurrencyID(mCarId));
        mRateUOM = mDbAdapter.getUOMCode(mDbAdapter.getCarUOMLengthID(mCarId));
        tvRateUOM.setText(mCarCurrencyCode + " / " + mRateUOM);
    }
}
