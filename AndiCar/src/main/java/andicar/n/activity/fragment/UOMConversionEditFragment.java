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
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import org.andicar2.activity.R;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 30.01.2017.
 */

public class UOMConversionEditFragment extends BaseEditFragment {
    private String mUOMFromType = "";
    private long mUOMFromId;
    private long mUOMToId;
    private String mConversionFactor;


    private Spinner spnUomFrom;
    private Spinner spnUomTo;
    private EditText etConversionFactor;

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
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_UOMCONVERSION, DBAdapter.COL_LIST_UOMCONVERSION_TABLE, mRowId);

        assert c != null;
        mName = c.getString(DBAdapter.COL_POS_GEN_NAME);
        mIsActive = c.getString(DBAdapter.COL_POS_GEN_ISACTIVE).equals("Y");
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        mConversionFactor = c.getString(DBAdapter.COL_POS_UOMCONVERSION__RATE);
        mUOMFromId = c.getLong(DBAdapter.COL_POS_UOMCONVERSION__UOMFROM_ID);
        mUOMToId = c.getLong(DBAdapter.COL_POS_UOMCONVERSION__UOMTO_ID);
        c.close();
        c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_UOM, DBAdapter.COL_LIST_UOM_TABLE, mUOMFromId);
        if (c != null) {
            mUOMFromType = c.getString(DBAdapter.COL_POS_UOM__UOMTYPE);
            c.close();
        }
    }

    @Override
    public void initDefaultValues() {
        super.initDefaultValues();

        mUOMFromType = "";
        mUOMFromId = -1;
        mUOMToId = -1;
        mConversionFactor = "";
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        spnUomFrom = mRootView.findViewById(R.id.spnUomFrom);
        spnUomTo = mRootView.findViewById(R.id.spnUomTo);
        etConversionFactor = mRootView.findViewById(R.id.etConversionFactor);
        ckIsActive = mRootView.findViewById(R.id.ckIsActive);
    }

    @Override
    protected void initSpecificControls() {

        spnUomTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mUOMToId = mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_UOM, adapterView.getAdapter().getItem(i).toString());
                setSpinnerTextToCode(adapterView, mUOMToId, view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mUOMFromId = Utils.initSpinner(mDbAdapter, spnUomFrom, DBAdapter.TABLE_NAME_UOM,
                DBAdapter.WHERE_CONDITION_ISACTIVE, mUOMFromId, false);
        spnUomFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mUOMFromId = mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_UOM, adapterView.getAdapter().getItem(i).toString());
                mUOMToId = -1;
                setSpinnerTextToCode(adapterView, mUOMFromId, view);

                Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_UOM, DBAdapter.COL_LIST_UOM_TABLE, mUOMFromId);
                if (c != null) {
                    mUOMFromType = c.getString(DBAdapter.COL_POS_UOM__UOMTYPE);
                    c.close();
                }

                mUOMToId = Utils.initSpinner(mDbAdapter, spnUomTo, DBAdapter.TABLE_NAME_UOM,
                        DBAdapter.WHERE_CONDITION_ISACTIVE + " AND " + DBAdapter.COL_NAME_UOM__UOMTYPE + "='" + mUOMFromType + "' " + " AND " + DBAdapter.COL_NAME_GEN_ROWID + " <> " + mUOMFromId,
                        mUOMToId, false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    protected void showValuesInUI() {
        etName.setText(mName);
        etConversionFactor.setText(mConversionFactor);
        ckIsActive.setChecked(mIsActive);
        acUserComment.setText(mUserComment);
    }

    @Override
    public void setSpecificLayout() {

    }

    @Override
    protected boolean saveData() {

        int checkResult = mDbAdapter.canInsertUpdateUOMConversion(mRowId, mUOMFromId, mUOMToId);
        if (checkResult != -1) {
            Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(checkResult));
            return false;
        }

        ContentValues data = new ContentValues();
        data.put(DBAdapter.COL_NAME_GEN_NAME, etName.getText().toString());
        data.put(DBAdapter.COL_NAME_GEN_ISACTIVE, (ckIsActive.isChecked() ? "Y" : "N"));
        data.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, acUserComment.getText().toString());
        data.put(DBAdapter.COL_NAME_UOMCONVERSION__UOMFROM_ID, mUOMFromId);
        data.put(DBAdapter.COL_NAME_UOMCONVERSION__UOMTO_ID, mUOMToId);
        data.put(DBAdapter.COL_NAME_UOMCONVERSION__RATE, etConversionFactor.getText().toString());

        int dbRetVal;
        if (mRowId == -1) {
            dbRetVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_UOMCONVERSION, data)).intValue();
            if (dbRetVal > 0) {
                return true;
            }
            else {
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
        }
        else {
            dbRetVal = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_UOMCONVERSION, mRowId, data);
            if (dbRetVal != -1) {
                String errMsg;
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
}
