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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.math.BigDecimal;

import andicar.n.activity.CommonDetailActivity;
import andicar.n.activity.CommonListActivity;
import andicar.n.persistence.DBAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 10.01.2017.
 */

public class CarEditFragment extends BaseEditFragment {
    private long mUOMLengthId;
    private long mUOMVolumeId;
    private String mCarModel;
    private String mCarRegNo;
    private BigDecimal mInitialIndex;

    private Spinner spnUomLength = null;
    private Spinner spnUomVolume = null;
    private EditText etName = null;
    private EditText etCarModel = null;
    private EditText etCarRegNo = null;
    private EditText etIndexStart = null;

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
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_CAR, DBAdapter.COL_LIST_CAR_TABLE, mRowId);

        assert c != null;

        mName = c.getString(DBAdapter.COL_POS_GEN_NAME);
        mIsActive = c.getString(DBAdapter.COL_POS_GEN_ISACTIVE).equals("Y");
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        mCarModel = c.getString(DBAdapter.COL_POS_CAR__MODEL);
        mCarRegNo = c.getString(DBAdapter.COL_POS_CAR__REGISTRATIONNO);
        try {
            mInitialIndex = new BigDecimal(c.getString(DBAdapter.COL_POS_CAR__INDEXSTART));
        }
        catch (NumberFormatException ignored) {
        }

        mUOMLengthId = c.getLong(DBAdapter.COL_POS_CAR__UOMLENGTH_ID);
        mUOMVolumeId = c.getLong(DBAdapter.COL_POS_CAR__UOMVOLUME_ID);
        setCurrencyId(c.getLong(DBAdapter.COL_POS_CAR__CURRENCY_ID));
        c.close();
    }

    @Override
    public void initDefaultValues() {
        super.initDefaultValues();

        String locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = getContext().getResources().getConfiguration().getLocales().get(0).getCountry();
        }
        else {
            //noinspection deprecation
            locale = getContext().getResources().getConfiguration().locale.getCountry();
        }
        switch (locale) {
            case "US":
                setCurrencyId(mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_CURRENCY, "USD"));
                mUOMLengthId = mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_UOM, "mi");
                mUOMVolumeId = mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_UOM, "gal US");

                break;
            case "CA":
                setCurrencyId(mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_CURRENCY, "CAD"));
                mUOMLengthId = mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_UOM, "mi");
                mUOMVolumeId = mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_UOM, "gal US");

                break;
            case "HU":
                setCurrencyId(mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_CURRENCY, "HUF"));
                mUOMLengthId = mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_UOM, "km");
                mUOMVolumeId = mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_UOM, "l");

                break;
            case "RO":
                setCurrencyId(mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_CURRENCY, "RON"));
                mUOMLengthId = mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_UOM, "km");
                mUOMVolumeId = mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_UOM, "l");

                break;
            case "GB":
                setCurrencyId(mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_CURRENCY, "GBP"));
                mUOMLengthId = mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_UOM, "mi");
                mUOMVolumeId = mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_UOM, "gal GB");

                break;
            default:
                setCurrencyId(mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_CURRENCY, "EUR"));
                mUOMLengthId = mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_UOM, "km");
                mUOMVolumeId = mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_UOM, "l");
                break;
        }

        mCarModel = "";
        mCarRegNo = "";
        mInitialIndex = null;
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        spnUomLength = mRootView.findViewById(R.id.spnUomLength);
        spnUomLength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                long newId = mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_UOM, adapterView.getAdapter().getItem(i).toString());
                setSpinnerTextToCode(adapterView, newId, view);

                if (adapterView.getTag() != null && adapterView.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                    adapterView.setTag(null);
                    return;
                }

                mUOMLengthId = newId;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spnUomVolume = mRootView.findViewById(R.id.spnUomVolume);
        spnUomVolume.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                long newId = mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_UOM, adapterView.getAdapter().getItem(i).toString());
                setSpinnerTextToCode(adapterView, newId, view);

                if (adapterView.getTag() != null && adapterView.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                    adapterView.setTag(null);
                    return;
                }

                mUOMVolumeId = newId;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        etName = mRootView.findViewById(R.id.etName);
        etCarModel = mRootView.findViewById(R.id.etCarModel);
        etCarRegNo = mRootView.findViewById(R.id.etCarRegNo);
        etIndexStart = mRootView.findViewById(R.id.etIndexStart);
        ckIsActive = mRootView.findViewById(R.id.ckIsActive);

        ImageButton btnNewCurrency = mRootView.findViewById(R.id.btnNewCurrency);
        btnNewCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CommonDetailActivity.class);
                intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_CURRENCY);
                intent.putExtra(BaseEditFragment.RECORD_ID_KEY, -1L);
                intent.putExtra(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_NEW);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        long newId = 0;
        if (data != null) {
            newId = data.getIntExtra("mRowId", 0);
        }

        if (newId > 0) {
            setCurrencyId(newId);
            Utils.initSpinner(mDbAdapter, spnCurrency, DBAdapter.TABLE_NAME_CURRENCY, DBAdapter.WHERE_CONDITION_ISACTIVE, mCurrencyId, false);
        }
    }

    @Override
    protected void initSpecificControls() {
        Utils.initSpinner(mDbAdapter, spnUomLength, DBAdapter.TABLE_NAME_UOM,
                DBAdapter.WHERE_CONDITION_ISACTIVE + " AND " + DBAdapter.COL_NAME_UOM__UOMTYPE + " = '" + ConstantValues.UOM_LENGTH_TYPE_CODE + "'", mUOMLengthId, false);

        Utils.initSpinner(mDbAdapter, spnUomVolume, DBAdapter.TABLE_NAME_UOM,
                DBAdapter.WHERE_CONDITION_ISACTIVE + " AND " + DBAdapter.COL_NAME_UOM__UOMTYPE + " = '" + ConstantValues.UOM_VOLUME_TYPE_CODE + "'", mUOMVolumeId, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void showValuesInUI() {
        etName.setText(mName);
        ckIsActive.setChecked(mIsActive);
        acUserComment.setText(mUserComment);
        etCarModel.setText(mCarModel);
        etCarRegNo.setText(mCarRegNo);
        if (mInitialIndex != null) {
            etIndexStart.setText(mInitialIndex.toString());
        }
        else {
            etIndexStart.setText("");
        }
    }

    @Override
    public void setSpecificLayout() {
    }

    @Override
    protected boolean saveData() {
        BigDecimal bdStartIndex = null;
        String strIndexStart = etIndexStart.getText().toString();
        if (strIndexStart.length() > 0) {
            try {
                bdStartIndex = new BigDecimal(strIndexStart);
            }
            catch (NumberFormatException e) {
                Toast toast = Toast.makeText(getActivity(),
                        mResource.getString(R.string.gen_invalid_number) + ": " + mResource.getString(R.string.car_edit_index_start_label),
                        Toast.LENGTH_SHORT);
                toast.show();
                return false;
            }
        }
        ContentValues cvData = new ContentValues();
        cvData.put(DBAdapter.COL_NAME_GEN_NAME, etName.getText().toString());
        cvData.put(DBAdapter.COL_NAME_GEN_ISACTIVE, (ckIsActive.isChecked() ? "Y" : "N"));
        cvData.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, acUserComment.getText().toString());
        cvData.put(DBAdapter.COL_NAME_CAR__MODEL, etCarModel.getText().toString());
        cvData.put(DBAdapter.COL_NAME_CAR__REGISTRATIONNO, etCarRegNo.getText().toString());
        cvData.put(DBAdapter.COL_NAME_CAR__INDEXSTART, bdStartIndex != null ? bdStartIndex.toString() : BigDecimal.ZERO.toString());
        cvData.put(DBAdapter.COL_NAME_CAR__UOMLENGTH_ID, mUOMLengthId);
        cvData.put(DBAdapter.COL_NAME_CAR__UOMVOLUME_ID, mUOMVolumeId);
        cvData.put(DBAdapter.COL_NAME_CAR__CURRENCY_ID, mCurrencyId);

        int dbRetVal;
        String strErrMsg;
        if (mRowId == -1) {
            //when a new car defined the current index is same with the start index
            cvData.put(DBAdapter.COL_NAME_CAR__INDEXCURRENT, bdStartIndex != null ? bdStartIndex.toString() : BigDecimal.ZERO.toString());
            dbRetVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_CAR, cvData)).intValue();
            if (dbRetVal > 0) {
                //used when new car definition is launched from the main navigation
                Intent resultIntent = new Intent();
                resultIntent.putExtra(DBAdapter.COL_NAME_GEN_ROWID, dbRetVal);
                getActivity().setResult(Activity.RESULT_OK, resultIntent);

                return true;
            }
            else {
                if (dbRetVal == -1) //DB Error
                {
                    Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), mDbAdapter.mErrorMessage, mDbAdapter.mException, false);
                }
                else//precondition error
                {
                    Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(-1 * dbRetVal), false);
                }
                return false;
            }
        }
        else {
            dbRetVal = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_CAR, mRowId, cvData);
            if (dbRetVal != -1) {
                strErrMsg = mResource.getString(dbRetVal);
                if (dbRetVal == R.string.error_000) {
                    strErrMsg = strErrMsg + "\n" + mDbAdapter.mErrorMessage;
                }
                Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), strErrMsg, mDbAdapter.mException, false);
                return false;
            }
            else {
                //if the selected car in the main activity is inactivated, invalidate it
                if (mRowId == mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_car_id), -1) && !ckIsActive.isChecked()) {
                    mPreferences.edit().putLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_car_id), -1).apply();
                }
                return true;
            }
        }
    }
}
