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
package andicar.n.utils;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.andicar2.activity.R;

import java.util.Set;

import andicar.n.activity.fragment.BaseEditFragment;
import andicar.n.activity.fragment.ExpenseEditFragment;
import andicar.n.activity.fragment.GPSTrackControllerFragment;
import andicar.n.activity.fragment.MileageEditFragment;
import andicar.n.activity.fragment.RefuelEditFragment;
import andicar.n.persistence.DBAdapter;

/**
 * @author miki
 */
public class DataEntryTemplate {

    private DBAdapter mDbAdapter = null;
    private BaseEditFragment mEditFragment = null;
    private View mRootView = null;
    private Spinner spnTemplate = null;
    private PopupMenu pmOperations = null;

    private long mTemplateID = -1;

    private Resources mResource = null;

    /**
     * the edit activity class:<br>
     * <li><b>EEA:<b> ExpenseEditActivity <li><b>MEA:<b> MileageEditActivity <li>
     * <b>REA:<b> RefuelEditActivity <li><b>GTC:<b> GPSTrackController
     */
    private String mActivityClass = null;

    public DataEntryTemplate(BaseEditFragment ea, final DBAdapter db) {
        mRootView = ea.mRootView;
        mResource = mRootView.getResources();
        mEditFragment = ea;

//		boolean isSendCrashReport = mPreferences.getBoolean("SendCrashReport", true);

        mDbAdapter = db;
        if (mEditFragment instanceof MileageEditFragment) {
            mActivityClass = "MEA";
        }
        else if (mEditFragment instanceof ExpenseEditFragment) {
            mActivityClass = "EEA";
        }
        else if (mEditFragment instanceof RefuelEditFragment) {
            mActivityClass = "REA";
        }
        else if (mEditFragment instanceof GPSTrackControllerFragment) {
            mActivityClass = "GTC";
        }

        ImageButton btnPopupMenu = mRootView.findViewById(R.id.btnPopupMenu);
        if (btnPopupMenu != null) {
            pmOperations = new PopupMenu(mEditFragment.getActivity(), btnPopupMenu);
            pmOperations.getMenuInflater().inflate(R.menu.menu_data_entry_popup, pmOperations.getMenu());

            btnPopupMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pmOperations.show();
                }
            });

            pmOperations.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.de_update) {
                        DataEntryTemplate.this.saveTemplate(mTemplateID, null);
                        Toast.makeText(mEditFragment.getActivity(), R.string.data_template_updated_msg, Toast.LENGTH_LONG).show();
                    }
                    else if (menuItem.getItemId() == R.id.de_new) {
                        if (mEditFragment.getActivity() != null) {
                            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mEditFragment.getActivity());
                            alertDialog.setTitle(R.string.data_template_new);
                            alertDialog.setMessage(R.string.data_template_new_msg);
                            alertDialog.setCancelable(false);

                            final EditText input = new EditText(mEditFragment.getActivity());
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT);
                            input.setLayoutParams(lp);
                            input.setHint(R.string.gen_required);
                            input.setPadding(50, input.getPaddingTop(), 50, 50);

                            alertDialog.setView(input);

                            alertDialog.setPositiveButton(R.string.gen_save,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String name = input.getText().toString();
                                            if (name.trim().length() == 0) {
                                                Toast.makeText(mEditFragment.getActivity(), R.string.gen_fill_mandatory, Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                            ContentValues cv = new ContentValues();
                                            cv.put("Name", name);
                                            cv.put("Comment", name);
                                            int dbRetVal = ((Long) DataEntryTemplate.this.saveTemplate(-1, cv)).intValue();
                                            String strErrMsg;
                                            if (dbRetVal < 0) {
                                                if (dbRetVal == -1) // DB Error
                                                {
                                                    strErrMsg = mDbAdapter.mErrorMessage;
                                                } else
                                                // precondition error
                                                {
                                                    strErrMsg = mResource.getString(-1 * dbRetVal);
                                                }
                                                Toast.makeText(mEditFragment.getActivity(), strErrMsg, Toast.LENGTH_LONG).show();
                                            } else {
                                                DataEntryTemplate.this.updateTemplateList(dbRetVal);
                                                mTemplateID = dbRetVal;
                                                pmOperations.getMenu().getItem(1).setEnabled(true);
                                                pmOperations.getMenu().getItem(2).setEnabled(true);
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
                    }
                    else if (menuItem.getItemId() == R.id.de_delete) {
                        mDbAdapter.deleteRecord(DBAdapter.TABLE_NAME_DATA_TEMPLATE, mTemplateID);
                        DataEntryTemplate.this.updateTemplateList(-1);
                        mTemplateID = -1;
                        pmOperations.getMenu().getItem(1).setEnabled(false);
                        pmOperations.getMenu().getItem(2).setEnabled(false);
                    }

                    return true;
                }
            });
        }

        spnTemplate = mRootView.findViewById(R.id.spnTemplate);
        if (spnTemplate != null) {
            Utils.initSpinner(mDbAdapter, spnTemplate, DBAdapter.TABLE_NAME_DATA_TEMPLATE,
                    DBAdapter.WHERE_CONDITION_ISACTIVE + " AND "
                            + DBAdapter.COL_NAME_DATATEMPLATE__CLASS + " = '" + mActivityClass + "'", -1,
                    true);
            spnTemplate.setOnTouchListener(mEditFragment.spinnerOnTouchListener);

            spnTemplate.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    if (adapterView.getTag() != null && adapterView.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                        adapterView.setTag(null);
                        return;
                    }
                    mTemplateID = mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_DATA_TEMPLATE, adapterView.getAdapter().getItem(position).toString());
                    if (mTemplateID == -1) {
                        mEditFragment.initDefaultValues();
                        pmOperations.getMenu().getItem(1).setEnabled(false);
                        pmOperations.getMenu().getItem(2).setEnabled(false);
                    }
                    else {
                        pmOperations.getMenu().getItem(1).setEnabled(true);
                        pmOperations.getMenu().getItem(2).setEnabled(true);
                        fillFromTemplate(mTemplateID);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }

        LinearLayout llTemplateZone = mRootView.findViewById(R.id.llTemplateZone);
        if (llTemplateZone != null) {
            if (mEditFragment.getOperationType().equals(BaseEditFragment.DETAIL_OPERATION_EDIT) || mEditFragment.getOperationType().equals(BaseEditFragment.DETAIL_OPERATION_VIEW)) {
                llTemplateZone.setVisibility(View.GONE);
            }
            else {
                llTemplateZone.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * save a new or update an existing template
     *
     * @param templateID       if > 0 update the existing template. If == 0 create a new one
     * @param templateMetaData the name of the new template
     * @return -1 * error code on failure or the ID of the template
     */

    private long saveTemplate(long templateID, @Nullable ContentValues templateMetaData) {
        long retVal;
        long dbRetVal;
        ContentValues cv = new ContentValues();
        String name = null;

        //fill the master values
        if (templateMetaData != null && templateMetaData.containsKey("Name")) {
            name = templateMetaData.getAsString("Name");
        }

        if (templateID < 0 && (name == null || name.length() == 0)) {
            return -1;
        }

        cv.put(DBAdapter.COL_NAME_GEN_NAME, name);

        if (templateMetaData != null && templateMetaData.containsKey("Comment")) {
            cv.put(DBAdapter.COL_NAME_GEN_NAME, templateMetaData.getAsString("Comment"));
        }
        else {
            cv.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, "");
        }

        cv.put(DBAdapter.COL_NAME_GEN_ISACTIVE, "Y");
        cv.put(DBAdapter.COL_NAME_DATATEMPLATE__CLASS, mActivityClass);
        if (templateID < 0) {
            retVal = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_DATA_TEMPLATE, cv);
            if (retVal < 0) {
                return retVal;
            }
        }
        else {
            retVal = templateID;
            mDbAdapter.deleteRecords(DBAdapter.TABLE_NAME_DATA_TEMPLATE_VALUES, DBAdapter.COL_NAME_DATATEMPLATEVALUES__TEMPLATE_ID
                    + " = " + templateID, null);
        }

        Bundle values = new Bundle();
        fillValues(values);
        Set<String> keys = values.keySet();
        for (String value : keys) {
            cv.clear();
            cv.put(DBAdapter.COL_NAME_GEN_NAME, value);
            cv.put(DBAdapter.COL_NAME_GEN_ISACTIVE, "Y");
            cv.put(DBAdapter.COL_NAME_DATATEMPLATEVALUES__TEMPLATE_ID, retVal);
            cv.put(DBAdapter.COL_NAME_DATATEMPLATEVALUES__VALUE, values.getString(value));
            dbRetVal = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_DATA_TEMPLATE_VALUES, cv);
            if (dbRetVal < 0) {
                return dbRetVal;
            }
        }

        if (templateID < 0) { //new template
            Toast toast = Toast.makeText(mEditFragment.getActivity(), mResource.getString(R.string.data_template_created_msg), Toast.LENGTH_LONG);
            toast.show();
        }
        else {
            Toast toast = Toast.makeText(mEditFragment.getActivity(), mResource.getString(R.string.data_template_updated_msg), Toast.LENGTH_SHORT);
            toast.show();
        }

        return retVal;
    }

    private void updateTemplateList(long newID) {

        Utils.initSpinner(mDbAdapter, spnTemplate, DBAdapter.TABLE_NAME_DATA_TEMPLATE,
                DBAdapter.WHERE_CONDITION_ISACTIVE + " AND " + DBAdapter.COL_NAME_DATATEMPLATE__CLASS + " = '" + mActivityClass + "'",
                newID, true);
    }

    private void fillFromTemplate(long templateID) {

        long tmpID;
        String selArgs[] = {Long.toString(templateID)};
        Cursor c = mDbAdapter.query(DBAdapter.TABLE_NAME_DATA_TEMPLATE_VALUES, DBAdapter.COL_LIST_DATATEMPLATEVALUES_TABLE,
                DBAdapter.COL_NAME_DATATEMPLATEVALUES__TEMPLATE_ID + " = ?", selArgs, null);
        if (c == null) {
            return;
        }

        mEditFragment.isTemplateUsed = true;

        if (mEditFragment instanceof MileageEditFragment) {
            MileageEditFragment tmpActivity = (MileageEditFragment) mEditFragment;
            String etUserInputStr = "";
            boolean isMileageInsertMode = false;

            while (c.moveToNext()) {
                if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnCar")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setCarId(tmpID);
                    if (mRootView.findViewById(R.id.lCarZone) != null && mRootView.findViewById(R.id.lCarZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnCar), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_CAR, tmpID));
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnDriver")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setDriverId(tmpID);
                    if (mRootView.findViewById(R.id.lDriverZone) != null && mRootView.findViewById(R.id.lDriverZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnDriver), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_DRIVER, tmpID));
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnExpType")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setExpTypeId(tmpID);
                    if (mRootView.findViewById(R.id.lExpTypeZone) != null && mRootView.findViewById(R.id.lExpTypeZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnExpType), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_EXPENSETYPE, tmpID));
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("rbInsertModeIndex")) {
                    ((RadioButton) mRootView.findViewById(R.id.rbInsertModeIndex)).setChecked(c.getString(
                            DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE).equals("Y"));
                    if (c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE).equals("Y")) {
                        tmpActivity.setInsertMode(MileageEditFragment.INSERT_MODE_INDEX);
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("rbInsertModeMileage")) {
                    isMileageInsertMode = c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE).equals("Y");
                    ((RadioButton) mRootView.findViewById(R.id.rbInsertModeMileage)).setChecked(isMileageInsertMode);
                    if (isMileageInsertMode) {
                        tmpActivity.setInsertMode(MileageEditFragment.INSERT_MODE_MILEAGE);
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("etUserInput")) {
                    etUserInputStr = c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("acTag")) {
                    ((AutoCompleteTextView) mRootView.findViewById(R.id.acTag))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("acUserComment")) {
                    ((AutoCompleteTextView) mRootView.findViewById(R.id.acUserComment))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
            }
            if (isMileageInsertMode && etUserInputStr != null) {
                ((EditText) mRootView.findViewById(R.id.etUserInput)).setText(etUserInputStr);
            }
            else {
                ((EditText) mRootView.findViewById(R.id.etUserInput)).setText(null);
            }

            tmpActivity.calculateMileageOrNewIndex();

        }
        else if (mEditFragment instanceof RefuelEditFragment) {
            RefuelEditFragment tmpActivity = (RefuelEditFragment) mEditFragment;
            while (c.moveToNext()) {
                if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnCar")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setCarId(tmpID);
                    if (mRootView.findViewById(R.id.lCarZone) != null && mRootView.findViewById(R.id.lCarZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnCar), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_CAR, tmpID));
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnDriver")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setDriverId(tmpID);
                    if (mRootView.findViewById(R.id.lDriverZone) != null && mRootView.findViewById(R.id.lDriverZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnDriver), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_DRIVER, tmpID));
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnExpType")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setExpTypeId(tmpID);
                    if (mRootView.findViewById(R.id.lExpTypeZone) != null && mRootView.findViewById(R.id.lExpTypeZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnExpType), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_EXPENSETYPE, tmpID));
                    }
                } else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnExpCatOrFuelType")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setExpCatOrFuelTypeId(tmpID);
                    if (mRootView.findViewById(R.id.lExpCatZone) != null && mRootView.findViewById(R.id.lExpCatZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnExpCatOrFuelType), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_EXPENSECATEGORY, tmpID));
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnCurrency")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setCurrencyId(tmpID);
                    tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnCurrency), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_CURRENCY, tmpID));
                } else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnUomFuel")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setUOMFuelId(tmpID);
                    tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnUomFuel), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_UOM, tmpID));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("etUserInput")) {
                    ((EditText) mRootView.findViewById(R.id.etUserInput))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("acBPartner")) {
                    ((AutoCompleteTextView) mRootView.findViewById(R.id.acBPartner))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("acAddress")) {
                    ((AutoCompleteTextView) mRootView.findViewById(R.id.acAddress))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("acTag")) {
                    ((AutoCompleteTextView) mRootView.findViewById(R.id.acTag))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("acUserComment")) {
                    ((AutoCompleteTextView) mRootView.findViewById(R.id.acUserComment))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("rbInsertModeAmount")) {
                    ((RadioButton) mRootView.findViewById(R.id.rbInsertModeAmount))
                            .setChecked(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE).equals("Y"));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("rbInsertModePrice")) {
                    ((RadioButton) mRootView.findViewById(R.id.rbInsertModePrice))
                            .setChecked(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE).equals("Y"));
                } else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("ckIsAlternativeFuel")) {
                    ((CheckBox) mRootView.findViewById(R.id.ckIsAlternativeFuel))
                            .setChecked(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE).equals("Y"));
                }
            }
        }
        else if (mEditFragment instanceof ExpenseEditFragment) {
            ExpenseEditFragment tmpActivity = (ExpenseEditFragment) mEditFragment;
            while (c.moveToNext()) {
                if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnCar")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setCarId(tmpID);
                    if (mRootView.findViewById(R.id.lCarZone) != null && mRootView.findViewById(R.id.lCarZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnCar), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_CAR, tmpID));
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnDriver")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setDriverId(tmpID);
                    if (mRootView.findViewById(R.id.lDriverZone) != null && mRootView.findViewById(R.id.lDriverZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnDriver), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_DRIVER, tmpID));
                    }
                } else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnExpCatOrFuelType")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setExpCatOrFuelTypeId(tmpID);
                    if (mRootView.findViewById(R.id.lExpCatZone) != null && mRootView.findViewById(R.id.lExpCatZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnExpCatOrFuelType), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_EXPENSECATEGORY, tmpID));
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnExpType")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setExpTypeId(tmpID);
                    if (mRootView.findViewById(R.id.lExpTypeZone) != null && mRootView.findViewById(R.id.lExpTypeZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnExpType), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_EXPENSETYPE, tmpID));
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnCurrency")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setCurrencyId(tmpID);
                    tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnCurrency), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_CURRENCY, tmpID));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnUOM")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setUOMId(tmpID);
                    tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnUOM), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_UOM, tmpID));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("etQuantity")) {
                    ((EditText) mRootView.findViewById(R.id.etQuantity))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("etUserInput")) {
                    ((EditText) mRootView.findViewById(R.id.etUserInput))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("acBPartner")) {
                    ((AutoCompleteTextView) mRootView.findViewById(R.id.acBPartner))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("acAddress")) {
                    ((AutoCompleteTextView) mRootView.findViewById(R.id.acAddress))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("acTag")) {
                    ((AutoCompleteTextView) mRootView.findViewById(R.id.acTag))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("acUserComment")) {
                    ((AutoCompleteTextView) mRootView.findViewById(R.id.acUserComment))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("rbInsertModeAmount")) {
                    ((RadioButton) mRootView.findViewById(R.id.rbInsertModeAmount))
                            .setChecked(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE).equals("Y"));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("rbInsertModePrice")) {
                    ((RadioButton) mRootView.findViewById(R.id.rbInsertModePrice))
                            .setChecked(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE).equals("Y"));
                }
            }
        }
        else if (mEditFragment instanceof GPSTrackControllerFragment) {
            GPSTrackControllerFragment tmpActivity = (GPSTrackControllerFragment) mEditFragment;
            while (c.moveToNext()) {
                if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnCar")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setCarId(tmpID);
                    if (mRootView.findViewById(R.id.lCarZone) != null && mRootView.findViewById(R.id.lCarZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnCar), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_CAR, tmpID));
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnDriver")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setDriverId(tmpID);
                    if (mRootView.findViewById(R.id.lDriverZone) != null && mRootView.findViewById(R.id.lDriverZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnDriver), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_DRIVER, tmpID));
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("spnExpType")) {
                    tmpID = c.getLong(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE);
                    tmpActivity.setExpTypeId(tmpID);
                    if (mRootView.findViewById(R.id.lExpTypeZone) != null && mRootView.findViewById(R.id.lExpTypeZone).getVisibility() == View.VISIBLE) {
                        tmpActivity.setSpinnerSelectedID(mRootView.findViewById(R.id.spnExpType), mDbAdapter.getNameById(DBAdapter.TABLE_NAME_EXPENSETYPE, tmpID));
                    }
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("etName")) {
                    ((EditText) mRootView.findViewById(R.id.etName))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("acTag")) {
                    ((AutoCompleteTextView) mRootView.findViewById(R.id.acTag))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("acUserComment")) {
                    ((AutoCompleteTextView) mRootView.findViewById(R.id.acUserComment))
                            .setText(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE));
                }
                else if (c.getString(DBAdapter.COL_POS_GEN_NAME).equals("ckIsCreateMileage")) {
                    ((CheckBox) mRootView.findViewById(R.id.ckIsCreateMileage))
                            .setChecked(c.getString(DBAdapter.COL_POS_DATATEMPLATEVALUES__VALUE).equals("Y"));
                }
            }
        }
        c.close();

        mEditFragment.setSpecificLayout();
    }

    private void fillValues(Bundle cv) {
        if (mEditFragment instanceof MileageEditFragment) {
            cv.putString("spnCar", Long.toString(mEditFragment.getCarId()));
            cv.putString("spnDriver", Long.toString(mEditFragment.getDriverId()));
            cv.putString("spnExpType", Long.toString(mEditFragment.getExpTypeId()));
            cv.putString("rbInsertModeIndex", ((RadioButton) mRootView.findViewById(R.id.rbInsertModeIndex)).isChecked() ? "Y" : "N");
            cv.putString("rbInsertModeMileage", ((RadioButton) mRootView.findViewById(R.id.rbInsertModeMileage)).isChecked() ? "Y" : "N");
            cv.putString("etUserInput", ((EditText) mRootView.findViewById(R.id.etUserInput)).getText().toString());
            cv.putString("acTag", ((AutoCompleteTextView) mRootView.findViewById(R.id.acTag)).getText().toString());
            cv.putString("acUserComment", ((AutoCompleteTextView) mRootView.findViewById(R.id.acUserComment)).getText().toString());
        }
        else if (mEditFragment instanceof ExpenseEditFragment) {
            cv.putString("spnCar", Long.toString(mEditFragment.getCarId()));
            cv.putString("spnDriver", Long.toString(mEditFragment.getDriverId()));
            cv.putString("spnExpCatOrFuelType", Long.toString(mEditFragment.getExpCategoryId()));
            cv.putString("spnExpType", Long.toString(mEditFragment.getExpTypeId()));
            cv.putString("spnCurrency", Long.toString(mEditFragment.getCurrencyId()));
            cv.putString("spnUOM", Long.toString(((ExpenseEditFragment) mEditFragment).getUOMId()));
            cv.putString("rbInsertModeAmount", ((RadioButton) mRootView.findViewById(R.id.rbInsertModeAmount)).isChecked() ? "Y" : "N");
            cv.putString("rbInsertModePrice", ((RadioButton) mRootView.findViewById(R.id.rbInsertModePrice)).isChecked() ? "Y" : "N");
            cv.putString("etQuantity", ((EditText) mRootView.findViewById(R.id.etQuantity)).getText().toString());
            cv.putString("acBPartner", ((AutoCompleteTextView) mRootView.findViewById(R.id.acBPartner)).getText().toString());
            cv.putString("acAddress", ((AutoCompleteTextView) mRootView.findViewById(R.id.acAddress)).getText().toString());
            cv.putString("acTag", ((AutoCompleteTextView) mRootView.findViewById(R.id.acTag)).getText().toString());
            cv.putString("acUserComment", ((AutoCompleteTextView) mRootView.findViewById(R.id.acUserComment)).getText().toString());
            cv.putString("etUserInput", ((EditText) mRootView.findViewById(R.id.etUserInput)).getText().toString());
        }
        else if (mEditFragment instanceof RefuelEditFragment) {
            cv.putString("spnCar", Long.toString(mEditFragment.getCarId()));
            cv.putString("spnDriver", Long.toString(mEditFragment.getDriverId()));
            cv.putString("spnExpCatOrFuelType", Long.toString(mEditFragment.getExpCategoryId()));
            cv.putString("spnExpType", Long.toString(mEditFragment.getExpTypeId()));
            cv.putString("spnCurrency", Long.toString(mEditFragment.getCurrencyId()));
            cv.putString("spnUomFuel", Long.toString(((RefuelEditFragment) mEditFragment).getUOMId()));
            cv.putString("rbInsertModeAmount", ((RadioButton) mRootView.findViewById(R.id.rbInsertModeAmount)).isChecked() ? "Y" : "N");
            cv.putString("rbInsertModePrice", ((RadioButton) mRootView.findViewById(R.id.rbInsertModePrice)).isChecked() ? "Y" : "N");
            cv.putString("etUserInput", ((EditText) mRootView.findViewById(R.id.etUserInput)).getText().toString());
            cv.putString("acBPartner", ((AutoCompleteTextView) mRootView.findViewById(R.id.acBPartner)).getText().toString());
            cv.putString("acAddress", ((AutoCompleteTextView) mRootView.findViewById(R.id.acAddress)).getText().toString());
            cv.putString("acTag", ((AutoCompleteTextView) mRootView.findViewById(R.id.acTag)).getText().toString());
            cv.putString("acUserComment", ((AutoCompleteTextView) mRootView.findViewById(R.id.acUserComment)).getText().toString());
            cv.putString("ckIsAlternativeFuel", ((CheckBox) mRootView.findViewById(R.id.ckIsAlternativeFuel)).isChecked() ? "Y" : "N");
        }
        else if (mEditFragment instanceof GPSTrackControllerFragment) {
            cv.putString("spnCar", Long.toString(mEditFragment.getCarId()));
            cv.putString("spnDriver", Long.toString(mEditFragment.getDriverId()));
            cv.putString("spnExpType", Long.toString(mEditFragment.getExpTypeId()));
            cv.putString("etName", ((EditText) mRootView.findViewById(R.id.etName)).getText().toString());
            cv.putString("ckIsCreateMileage", ((CheckBox) mRootView.findViewById(R.id.ckIsCreateMileage)).isChecked() ? "Y" : "N");
            cv.putString("acTag", ((AutoCompleteTextView) mRootView.findViewById(R.id.acTag)).getText().toString());
            cv.putString("acUserComment", ((AutoCompleteTextView) mRootView.findViewById(R.id.acUserComment)).getText().toString());
        }
    }

    public void clearSelected() {
        spnTemplate.setSelection(0);
    }
}
