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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.andicar2.activity.R;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 30.01.2017.
 */

public class UOMEditFragment extends BaseEditFragment {

    private String mCode;
    private String mUOMType;

    private EditText etCode;
    private Spinner spnUomType;

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
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_UOM, DBAdapter.COL_LIST_UOM_TABLE, mRowId);

        assert c != null;

        mName = c.getString(DBAdapter.COL_POS_GEN_NAME);
        mIsActive = c.getString(DBAdapter.COL_POS_GEN_ISACTIVE).equals("Y");
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        mCode = c.getString(DBAdapter.COL_POS_UOM__CODE);
        mUOMType = c.getString(DBAdapter.COL_POS_UOM__UOMTYPE);
        c.close();
    }

    @Override
    public void initDefaultValues() {
        super.initDefaultValues();

        mUOMType = ConstantValues.UOM_OTHER_TYPE_CODE;
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        etCode = mRootView.findViewById(R.id.etCode);
        ckIsActive = mRootView.findViewById(R.id.ckIsActive);
        spnUomType = mRootView.findViewById(R.id.spnUomType);
    }

    @Override
    protected void initSpecificControls() {
        if (getActivity() != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.uom_type_entries, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnUomType.setAdapter(adapter);
        }
    }

    @Override
    protected void showValuesInUI() {
        etName.setText(mName);
        ckIsActive.setChecked(mIsActive);
        acUserComment.setText(mUserComment);
        etCode.setText(mCode);
        if (mUOMType != null) {
            switch (mUOMType) {
                case ConstantValues.UOM_LENGTH_TYPE_CODE:
                    spnUomType.setSelection(0);
                    break;
                case ConstantValues.UOM_VOLUME_TYPE_CODE:
                    spnUomType.setSelection(1);
                    break;
                case ConstantValues.UOM_OTHER_TYPE_CODE:
                    spnUomType.setSelection(2);
                    break;
            }
        }
    }

    @Override
    public void setSpecificLayout() {

    }

    @Override
    protected boolean saveData() {
        ContentValues data = new ContentValues();
        data.put(DBAdapter.COL_NAME_GEN_NAME, etName.getText().toString());
        data.put(DBAdapter.COL_NAME_GEN_ISACTIVE, (ckIsActive.isChecked() ? "Y" : "N"));
        data.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, acUserComment.getText().toString());
        data.put(DBAdapter.COL_NAME_UOM__CODE, etCode.getText().toString());
        if (spnUomType.getSelectedItemPosition() == 0) {
            data.put(DBAdapter.COL_NAME_UOM__UOMTYPE, ConstantValues.UOM_LENGTH_TYPE_CODE);
        }
        else if (spnUomType.getSelectedItemPosition() == 1) {
            data.put(DBAdapter.COL_NAME_UOM__UOMTYPE, ConstantValues.UOM_VOLUME_TYPE_CODE);
        }
        else {
            data.put(DBAdapter.COL_NAME_UOM__UOMTYPE, ConstantValues.UOM_OTHER_TYPE_CODE);
        }

        int dbRetVal;
        if (mRowId == -1) {
            dbRetVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_UOM, data)).intValue();
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
            dbRetVal = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_UOM, mRowId, data);
            if (dbRetVal != -1) {
//                String errMsg;
//                errMsg = mResource.getString(dbRetVal);
//                if (dbRetVal == R.string.error_000)
//                    errMsg = errMsg + "\n" + mDbAdapter.mErrorMessage;
//                Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), errMsg, mDbAdapter.mException);
                Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(dbRetVal));
                return false;
            }
            else {
                return true;
            }
        }
    }
}
