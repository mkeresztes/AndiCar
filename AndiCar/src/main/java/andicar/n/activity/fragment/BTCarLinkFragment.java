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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.andicar2.activity.R;

import java.util.ArrayList;
import java.util.Set;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 21.03.2017.
 */

public class BTCarLinkFragment extends BaseEditFragment {

    private final ArrayList<String> mPairedDevicesMAC = new ArrayList<>();
    private final ArrayList<String> mPairedDevicesName = new ArrayList<>();
    private Spinner spnBTPairedDevices = null;
    private String mMAC = null;

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
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_BTDEVICE_CAR, DBAdapter.COL_LIST_BTDEVICECAR_TABLE, mRowId);
        assert c != null;
        mIsActive = c.getString(DBAdapter.COL_POS_GEN_ISACTIVE).equals("Y");
        mMAC = c.getString(DBAdapter.COL_POS_BTDEVICECAR__MACADDR);
        setCarId(c.getLong(DBAdapter.COL_POS_BTDEVICECAR__CAR_ID));
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        c.close();
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        spnBTPairedDevices = mRootView.findViewById(R.id.spnBTPairedDevices);
    }

    @Override
    protected void initSpecificControls() {
        //@formatter:off
        String extraWhere =
                " AND " + DBAdapter.COL_NAME_GEN_ROWID + " " +
                        " NOT IN (SELECT " + DBAdapter.COL_NAME_BTDEVICECAR__CAR_ID +
                        " FROM " + DBAdapter.TABLE_NAME_BTDEVICE_CAR +
                        " WHERE 1 = 1 " + DBAdapter.WHERE_CONDITION_ISACTIVE +
                        " AND " + DBAdapter.COL_NAME_BTDEVICECAR__CAR_ID + " != " + mCarId + " )";
        //@formatter:on

        Utils.initSpinner(mDbAdapter, spnCar, DBAdapter.TABLE_NAME_CAR, DBAdapter.WHERE_CONDITION_ISACTIVE + extraWhere, mCarId, false);

        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mRowId < 0) {
            if (getActivity() != null) {
                if (mBtAdapter == null) {
                    Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(R.string.error_064), false);
                    getActivity().finish();
                    return;
                } else if (!mBtAdapter.isEnabled()) {
                    Utils.showInfoDialog(getActivity(), getString(R.string.bt_link_link_to_car_bt_disabled_title), getString(R.string.bt_link_link_to_car_bt_disabled_message));
                    getActivity().finish();
                    return;
                }
            }
        }
        // Get a set of currently paired devices
        if (mBtAdapter != null) {
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
            int i = 0;
            int editId = 0;
            if (pairedDevices.size() > 0) {
                String[] args = new String[1];
                Cursor c;
                String tmpStr;
                for (BluetoothDevice device : pairedDevices) {
                    tmpStr = device.getAddress();
                    if (mRowId > 0) {
                        if (mMAC != null && mMAC.equals(tmpStr)) {
                            editId = i;
                        }
                    }

                    //eliminate already linked devices excepting the currently edited device
                    args[0] = tmpStr;
                    //@formatter:off
                    c = mDbAdapter.query(
                            "SELECT * " +
                            " FROM " + DBAdapter.TABLE_NAME_BTDEVICE_CAR +
                            " WHERE 1 = 1 " + DBAdapter.WHERE_CONDITION_ISACTIVE +
                                    " AND " + DBAdapter.COL_NAME_GEN_ROWID + " != " + mRowId +
                                    " AND " + DBAdapter.COL_NAME_BTDEVICECAR__MACADDR + " = ? ", args);
                    //@formatter:on
                    if (c.moveToNext()) {
                        //already linked
                        c.close();
                        continue;
                    }
                    c.close();
                    mPairedDevicesMAC.add(tmpStr);
                    mPairedDevicesName.add(device.getName());
                    i++;
                }
            }
            String[] mPairedDevices = new String[mPairedDevicesName.size()];
            mPairedDevicesName.toArray(mPairedDevices);
            ArrayAdapter<String> mPairedDevicesArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.ui_element_spinner_item, mPairedDevices);
            spnBTPairedDevices.setAdapter(mPairedDevicesArrayAdapter);
            if (editId > 0) {
                spnBTPairedDevices.setSelection(editId);
            }
        }
    }

    @Override
    protected void showValuesInUI() {
        ckIsActive.setChecked(mIsActive);
        acUserComment.setText(mUserComment);
    }

    @Override
    public void setSpecificLayout() {

    }

    @Override
    protected boolean saveData() {
        if (spnBTPairedDevices.getCount() <= 0 || spnCar.getCount() <= 0) {
            return false;
        }
        ContentValues cvData = new ContentValues();
        cvData.put(DBAdapter.COL_NAME_GEN_ISACTIVE, ckIsActive.isChecked() ? "Y" : "N");
        cvData.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, acUserComment.getText().toString());
        cvData.put(DBAdapter.COL_NAME_BTDEVICECAR__CAR_ID, mCarId);
        cvData.put(DBAdapter.COL_NAME_BTDEVICECAR__MACADDR, mPairedDevicesMAC.get(spnBTPairedDevices.getSelectedItemPosition()));
        cvData.put(DBAdapter.COL_NAME_GEN_NAME, mPairedDevicesName.get(spnBTPairedDevices.getSelectedItemPosition()));

        int dbRetVal;
        String strErrMsg;
        if (mRowId == -1) {
            dbRetVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_BTDEVICE_CAR, cvData)).intValue();
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
            dbRetVal = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_BTDEVICE_CAR, mRowId, cvData);
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
