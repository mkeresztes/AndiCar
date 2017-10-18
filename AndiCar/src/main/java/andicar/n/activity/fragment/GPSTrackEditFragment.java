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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import andicar.n.activity.CommonDetailActivity;
import andicar.n.activity.CommonListActivity;
import andicar.n.activity.miscellaneous.GPSTrackMap;
import andicar.n.persistence.DBAdapter;
import andicar.n.persistence.DBReportAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 15.03.2017.
 */

public class GPSTrackEditFragment extends BaseEditFragment {
    private String mStatistics;
    private long mMileageId = -1;

    private TextView tvTrackStats;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //this fragment can uses templates for filling data
        isUseTemplate = true;

        if (savedInstanceState == null) {
            switch (mOperationType) {
                case BaseEditFragment.DETAIL_OPERATION_EDIT: {
                    loadDataFromDB();
                    break;
                }
                default: //new record
                    Utils.showReportableErrorDialog(this.getActivity(), "Unexpected error", "Invalid operation type: " + mOperationType,
                            new Exception("Error"), false);
            }
        }
        else {
            mMileageId = savedInstanceState.getLong("mMileageId", -1);
            mStatistics = savedInstanceState.getString("mStatistics");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("mMileageId", mMileageId);
        outState.putString("mStatistics", mStatistics);
    }

    private void loadDataFromDB() {

        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_LIST_GPSTRACK_TABLE, mRowId);

        assert c != null;
        setCarId(c.getLong(DBAdapter.COL_POS_GPSTRACK__CAR_ID));
        setDriverId(c.getLong(DBAdapter.COL_POS_GPSTRACK__DRIVER_ID));
        setExpTypeId(c.getLong(DBAdapter.COL_POS_GPSTRACK__EXPENSETYPE_ID));
        if (c.getString(DBAdapter.COL_POS_GPSTRACK__MILEAGE_ID) != null) {
            mMileageId = c.getLong(DBAdapter.COL_POS_GPSTRACK__MILEAGE_ID);
        }

        mlDateTimeInMillis = c.getLong(DBAdapter.COL_POS_GPSTRACK__DATE) * 1000;
        initDateTimeFields();

        mName = c.getString(DBAdapter.COL_POS_GEN_NAME);
        mTagId = c.getLong(DBAdapter.COL_POS_GPSTRACK__TAG_ID);
        mTagStr = mDbAdapter.getNameById(DBAdapter.TABLE_NAME_TAG, mTagId);
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);

        c.close();

        Bundle whereConditions = new Bundle();
        whereConditions.clear();
        whereConditions.putString(DBReportAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_NAME_GEN_ROWID) + "=",
                String.valueOf(mRowId));
        DBReportAdapter reportDb = new DBReportAdapter(getActivity(), DBReportAdapter.GPS_TRACK_LIST_SELECT_NAME, whereConditions);
        c = reportDb.fetchReport(1);
        if (c != null && c.moveToFirst()) {
            mStatistics = String.format(c.getString(2),
                    getString(R.string.gps_track_detail_var_1),
                    "\n" + getString(R.string.gps_track_detail_var_2),
                    "\n" + getString(R.string.gps_track_detail_var_3),
                    "\n" + getString(R.string.gps_track_detail_var_4),
                    "\n" + getString(R.string.gps_track_detail_var_5) + " " + Utils.getTimeString(c.getLong(4)),
                    "\n" + getString(R.string.gps_track_detail_var_6) + " " + Utils.getTimeString(c.getLong(5)),
                    "\n" + getString(R.string.gps_track_detail_var_7),
                    "\n" + getString(R.string.gps_track_detail_var_8),
                    "\n" + getString(R.string.gps_track_detail_var_9),
                    "\n" + getString(R.string.gps_track_detail_var_10),
                    "\n" + getString(R.string.gps_track_detail_var_11),
                    "\n" + getString(R.string.gps_track_detail_var_12) + " " + Utils.getTimeString(c.getLong(8)),
                    "\n" + getString(R.string.gps_track_detail_var_13) + " " + Utils.getTimeString(c.getLong(4) - c.getLong(8) - c.getLong(5)));

        }
        if (c != null) {
            c.close();
        }
        reportDb.close();


    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        tvTrackStats = mRootView.findViewById(R.id.tvTrackStats);
        ListView lvTrackFileList = mRootView.findViewById(R.id.lvTrackFileList);

        //statistics
        String selection = DBAdapter.COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID + "=?";
        String[] selectionArgs = {Long.toString(mRowId)};
        int layout = R.layout.oneline_list_layout;

        //noinspection deprecation
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(), layout, mDbAdapter.query(DBAdapter.TABLE_NAME_GPSTRACKDETAIL,
                DBAdapter.COL_LIST_GPSTRACKDETAIL_TABLE, selection, selectionArgs, DBAdapter.COL_NAME_GPSTRACKDETAIL__FILE),
                new String[]{DBAdapter.COL_NAME_GPSTRACKDETAIL__FILE}, new int[]{R.id.tvOneLineListTextSmall});

        lvTrackFileList.setAdapter(cursorAdapter);
    }

    @Override
    protected void initSpecificControls() {

    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void showValuesInUI() {
        etName.setText(mName);
        acTag.setText(mTagStr);
        acUserComment.setText(mUserComment);
//        tvCarLabel.setText(mResource.getString(R.string.gen_car_label) + " " + mDbAdapter.getCarName(mCarId));
        spnCar.setEnabled(false);
        showDateTime();
        tvDateTimeValue.setEnabled(false);
        tvTrackStats.setText(mStatistics);
    }

    @Override
    public void setSpecificLayout() {

    }

    @Override
    protected boolean saveData() {
        ContentValues data = new ContentValues();
        data.put(DBAdapter.COL_NAME_GEN_NAME, etName.getText().toString());
        data.put(DBAdapter.COL_NAME_GEN_ISACTIVE, "Y");
        data.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, acUserComment.getText().toString());
        data.put(DBAdapter.COL_NAME_GPSTRACK__DRIVER_ID, mDriverId);
        data.put(DBAdapter.COL_NAME_GPSTRACK__EXPENSETYPE_ID, mExpTypeId);
        if (acTag.getText().toString().length() > 0) {
            String selection = "UPPER (" + DBAdapter.COL_NAME_GEN_NAME + ") = UPPER( ? ) ";
            String[] selectionArgs = {acTag.getText().toString()};
            Cursor c = mDbAdapter.query(DBAdapter.TABLE_NAME_TAG, DBAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs, null);
            String tagIdStr = null;
            if (c.moveToFirst()) {
                tagIdStr = c.getString(DBAdapter.COL_POS_GEN_ROWID);
            }
            c.close();
            if (tagIdStr != null && tagIdStr.length() > 0) {
                mTagId = Long.parseLong(tagIdStr);
                data.put(DBAdapter.COL_NAME_GPSTRACK__TAG_ID, mTagId);
            }
            else {
                ContentValues tmpData = new ContentValues();
                tmpData.put(DBAdapter.COL_NAME_GEN_NAME, acTag.getText().toString());
                mTagId = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_TAG, tmpData);
                if (mTagId >= 0) {
                    data.put(DBAdapter.COL_NAME_GPSTRACK__TAG_ID, mTagId);
                }
            }
        }
        else {
            data.put(DBAdapter.COL_NAME_GPSTRACK__TAG_ID, (String) null);
        }

        int updResult = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_GPSTRACK, mRowId, data);
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
            SharedPreferences.Editor prefEditor = mPreferences.edit();
            if (mPreferences.getBoolean(AndiCar.getAppResources().getString(R.string.pref_key_gen_remember_last_tag), false) && mTagId > 0) {
                prefEditor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_gen_last_tag_id), mTagId);
            }
            prefEditor.apply();
            return true;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (isVisible()) {
            inflater.inflate(R.menu.menu_gpstrack_detail_additional, menu);
            if (mMileageId < 0) {
                menu.findItem(R.id.action_show_mileag_edit).setVisible(false);
            }
            else {
                menu.findItem(R.id.action_show_mileag_edit).setVisible(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_show_gpstrack_on_map) {
            Intent gpstrackShowMapIntent = new Intent(getActivity(), GPSTrackMap.class);
            gpstrackShowMapIntent.putExtra(GPSTrackMap.GPS_TRACK_ID, mRowId);
            startActivity(gpstrackShowMapIntent);
            return true;
        }
        else if (id == R.id.action_send) {
//            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            if (!FileUtils.isFileSystemAccessGranted(getActivity())) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ConstantValues.REQUEST_ACCESS_EXTERNAL_STORAGE);
            }
            else {
                Utils u = new Utils();
                u.shareGPSTrack(getActivity(), mResource, mRowId);
            }
            return true;
        }
        else if (id == R.id.action_show_mileag_edit && mMileageId > 0) {
            Intent gpstrackEditIntent = new Intent(getActivity(), CommonDetailActivity.class);
            gpstrackEditIntent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_MILEAGE);
            gpstrackEditIntent.putExtra(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_EDIT);
            gpstrackEditIntent.putExtra(BaseEditFragment.RECORD_ID_KEY, mMileageId);
            startActivity(gpstrackEditIntent);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

}
