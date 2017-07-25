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
import android.widget.EditText;

import org.andicar2.activity.R;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 17.01.2017.
 */

public class DriverEditFragment extends BaseEditFragment {

    private String mLicenseNo;

    private EditText etName = null;
    private EditText etLicenseNo = null;

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
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_DRIVER, DBAdapter.COL_LIST_DRIVER_TABLE, mRowId);

        assert c != null;

        mName = c.getString(DBAdapter.COL_POS_GEN_NAME);
        mIsActive = c.getString(DBAdapter.COL_POS_GEN_ISACTIVE).equals("Y");
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        mLicenseNo = c.getString(DBAdapter.COL_POS_DRIVER__LICENSE_NO);
        c.close();
    }

    @Override
    public void initDefaultValues() {
        super.initDefaultValues();

        mLicenseNo = null;
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        etName = mRootView.findViewById(R.id.etName);
        etLicenseNo = mRootView.findViewById(R.id.etLicenseNo);
        ckIsActive = mRootView.findViewById(R.id.ckIsActive);
    }

    @Override
    protected void initSpecificControls() {

    }

    @Override
    protected void showValuesInUI() {
        etName.setText(mName);
        ckIsActive.setChecked(mIsActive);
        acUserComment.setText(mUserComment);
        etLicenseNo.setText(mLicenseNo);
    }

    @Override
    public void setSpecificLayout() {

    }

    @Override
    protected boolean saveData() {
        ContentValues cvData = new ContentValues();
        cvData.put(DBAdapter.COL_NAME_GEN_NAME, etName.getText().toString());
        cvData.put(DBAdapter.COL_NAME_GEN_ISACTIVE, (ckIsActive.isChecked() ? "Y" : "N"));
        cvData.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, acUserComment.getText().toString());
        cvData.put(DBAdapter.COL_NAME_DRIVER__LICENSE_NO, etLicenseNo.getText().toString());

        int dbRetVal;
        String strErrMsg;
        if (mRowId == -1) {
            dbRetVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_DRIVER, cvData)).intValue();
            if (dbRetVal > 0) {
                return true;
            }
            else {
                if (dbRetVal == -1) //DB Error
                {
                    Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), mDbAdapter.mErrorMessage, mDbAdapter.mException, false);
                }
                else
                //precondition error
                {
                    Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(-1 * dbRetVal), false);
                }

                return false;
            }
        }
        else {
            dbRetVal = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_DRIVER, mRowId, cvData);
            if (dbRetVal != -1) {
                strErrMsg = mResource.getString(dbRetVal);
                if (dbRetVal == R.string.error_000) {
                    strErrMsg = strErrMsg + "\n" + mDbAdapter.mErrorMessage;
                }
                Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), strErrMsg, mDbAdapter.mException, false);
                return false;
            }
            else {
                return true;
            }
        }
    }
}
