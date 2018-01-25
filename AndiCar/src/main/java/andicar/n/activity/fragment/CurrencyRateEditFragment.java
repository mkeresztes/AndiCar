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

package andicar.n.activity.fragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.andicar2.activity.R;

import java.math.BigDecimal;
import java.math.RoundingMode;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 21.02.2017.
 */

public class CurrencyRateEditFragment extends BaseEditFragment {

    private long mCurrencyToId;
    private BigDecimal mCurrencyRate;

    private Spinner spnCurrencyTo;
    private EditText etCurrencyRate;
    private TextView tvCurrencyRateLabel;
    private TextView tvCurrencyRateToLabel;
    private TextView tvInverseRateLabel;

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

    private void loadDataFromDB() {
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_CURRENCYRATE, DBAdapter.COL_LIST_CURRENCYRATE_TABLE, mRowId);

        assert c != null;

        setCurrencyId(c.getLong(DBAdapter.COL_POS_CURRENCYRATE__FROMCURRENCY_ID));
        mCurrencyToId = c.getLong(DBAdapter.COL_POS_CURRENCYRATE__TOCURRENCY_ID);
        mCurrencyRate = new BigDecimal(c.getString(DBAdapter.COL_POS_CURRENCYRATE__RATE));
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        mIsActive = c.getString(DBAdapter.COL_POS_GEN_ISACTIVE).equals("Y");
        c.close();
    }

    @Override
    public void initDefaultValues() {
        super.initDefaultValues();

        setCurrencyId(mDbAdapter.getFirstActiveID(DBAdapter.TABLE_NAME_CURRENCY, null, DBAdapter.COL_NAME_GEN_NAME));
        mCurrencyToId = -1;
        mCurrencyRate = null;
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        spnCurrencyTo = mRootView.findViewById(R.id.spnCurrencyTo);
        spnCurrencyTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                long newId = mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_CURRENCY, adapterView.getAdapter().getItem(i).toString());
                setSpinnerTextToCode(adapterView, newId, view);

                mCurrencyToId = newId;
                tvCurrencyRateToLabel.setText(mDbAdapter.getCurrencyCode(mCurrencyToId));
                calculateShowInverseRate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        etCurrencyRate = mRootView.findViewById(R.id.etCurrencyRate);
        etCurrencyRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    mCurrencyRate = new BigDecimal(editable.toString());
                }
                catch (NumberFormatException e) {
                    mCurrencyRate = null;
                }
                calculateShowInverseRate();
            }
        });
        tvCurrencyRateLabel = mRootView.findViewById(R.id.tvCurrencyRateLabel);
        tvCurrencyRateToLabel = mRootView.findViewById(R.id.tvCurrencyRateToLabel);
        tvInverseRateLabel = mRootView.findViewById(R.id.tvInverseRateLabel);
        ckIsActive = mRootView.findViewById(R.id.ckIsActive);

        viewsLoaded = true;
    }

    @Override
    protected void initSpecificControls() {
        mCurrencyToId = Utils.initSpinner(mDbAdapter, spnCurrencyTo, DBAdapter.TABLE_NAME_CURRENCY,
                DBAdapter.WHERE_CONDITION_ISACTIVE + " AND " + DBAdapter.COL_NAME_GEN_ROWID + " <> " + mCurrencyId,
                mCurrencyToId != mCurrencyId ? mCurrencyToId : -1, mRowId > 0, false);
    }

    @Override
    protected void showValuesInUI() {
        etCurrencyRate.setText(mCurrencyRate != null ? mCurrencyRate.toString() : "");
        tvCurrencyRateLabel.setText(String.format(getString(R.string.currency_rate_edit_rate_label), mDbAdapter.getCurrencyCode(mCurrencyId)));
        tvCurrencyRateToLabel.setText(mDbAdapter.getCurrencyCode(mCurrencyToId));
        calculateShowInverseRate();
        ckIsActive.setChecked(mIsActive);
        setSpecificLayout();
    }

    @Override
    public void setSpecificLayout() {
        if (mOperationType.equals(BaseEditFragment.DETAIL_OPERATION_EDIT)) {
            spnCurrency.setEnabled(false);
            spnCurrencyTo.setEnabled(false);
        }
    }

    @Override
    protected boolean saveData() {
        ContentValues cvData = new ContentValues();

        cvData.put(DBAdapter.COL_NAME_GEN_NAME, mDbAdapter.getCurrencyCode(mCurrencyId) + " <-> " + mDbAdapter.getCurrencyCode(mCurrencyToId));
        cvData.put(DBAdapter.COL_NAME_GEN_ISACTIVE, (ckIsActive.isChecked() ? "Y" : "N"));
        cvData.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, acUserComment.getText().toString());
        cvData.put(DBAdapter.COL_NAME_CURRENCYRATE__FROMCURRENCY_ID, mCurrencyId);
        cvData.put(DBAdapter.COL_NAME_CURRENCYRATE__TOCURRENCY_ID, mCurrencyToId);
        cvData.put(DBAdapter.COL_NAME_CURRENCYRATE__RATE, mCurrencyRate.toString());
        cvData.put(DBAdapter.COL_NAME_CURRENCYRATE__INVERSERATE,
                BigDecimal.ONE.divide(mCurrencyRate, 10, RoundingMode.HALF_UP).setScale(ConstantValues.DECIMALS_RATES, ConstantValues.ROUNDING_MODE_RATES).toString());

        int dbRetVal;
        String errMsg;
        if (mRowId == -1) {
            dbRetVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_CURRENCYRATE, cvData)).intValue();
            if (dbRetVal < 0) {
                if (dbRetVal == -1) //DB Error
                {
                    Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), mDbAdapter.mErrorMessage, mDbAdapter.mException);
                }
                else //precondition error
                {
                    Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(-1 * dbRetVal));
                }
                return false;
            }
            else {
                return true;
            }
        }
        else {
            dbRetVal = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_CURRENCYRATE, mRowId, cvData);
            if (dbRetVal != -1) {
                errMsg = mResource.getString(dbRetVal);
                if (dbRetVal == R.string.error_000) {
                    errMsg = errMsg + "\n" + mDbAdapter.mErrorMessage;
                }
                Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), errMsg, mDbAdapter.mException);
                return false;
            }
            else {
                return true;
            }
        }
    }

    @Override
    public void setCurrencyId(long currencyId) {
        super.setCurrencyId(currencyId);

        if (!viewsLoaded) {
            return;
        }

        tvCurrencyRateLabel.setText(String.format(getString(R.string.currency_rate_edit_rate_label), mDbAdapter.getCurrencyCode(mCurrencyId)));
        mCurrencyToId = Utils.initSpinner(mDbAdapter, spnCurrencyTo, DBAdapter.TABLE_NAME_CURRENCY,
                DBAdapter.WHERE_CONDITION_ISACTIVE + " AND " + DBAdapter.COL_NAME_GEN_ROWID + " <> " + mCurrencyId,
                mCurrencyToId != mCurrencyId ? mCurrencyToId : -1, mRowId > 0, false);
        calculateShowInverseRate();
    }

    private void calculateShowInverseRate() {
        if (mCurrencyRate != null && mCurrencyRate.compareTo(BigDecimal.ZERO) != 0) {
            tvInverseRateLabel.setText(String.format(getString(R.string.currency_rate_edit_inverse_rate_label), mDbAdapter.getCurrencyCode(mCurrencyToId),
                    BigDecimal.ONE.divide(mCurrencyRate, 10, RoundingMode.HALF_UP).setScale(ConstantValues.DECIMALS_RATES, ConstantValues.ROUNDING_MODE_RATES).toString(),
                    mDbAdapter.getCurrencyCode(mCurrencyId)));
        }
        else {
            tvInverseRateLabel.setText("N/A");
        }
    }
}
