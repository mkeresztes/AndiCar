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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;

import org.andicar2.activity.R;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 27.02.2017.
 */

public class BPartnerLocationEditFragment extends BaseEditFragment {
    private String mAddress;
    private String mPhone;
    private String mPhone2;
    private String mPostal;
    private String mFax;
    private String mCity;
    private String mRegion;
    private String mCountry;
    private String mContact;
    private String mEmail;
    private long mBPartnerId;

    private EditText etAddress;
    private AutoCompleteTextView acPhone1;
    private AutoCompleteTextView acPhone2;
    private AutoCompleteTextView acPostal;
    private AutoCompleteTextView acFax;
    private AutoCompleteTextView acCity;
    private AutoCompleteTextView acRegion;
    private AutoCompleteTextView acCountry;
    private AutoCompleteTextView acContact;
    private AutoCompleteTextView acEmail;

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
            mBPartnerId = savedInstanceState.getLong(BaseEditFragment.BPARTNER_ID_KEY);
        }
    }

    private void loadDataFromDB() {
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_LIST_BPARTNERLOCATION_TABLE, mRowId);

        assert c != null;
        mName = c.getString(DBAdapter.COL_POS_GEN_NAME);
        mIsActive = c.getString(DBAdapter.COL_POS_GEN_ISACTIVE).equals("Y");
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        mAddress = c.getString(DBAdapter.COL_POS_BPARTNERLOCATION__ADDRESS);
        mPhone = c.getString(DBAdapter.COL_POS_BPARTNERLOCATION__PHONE);
        mPhone2 = c.getString(DBAdapter.COL_POS_BPARTNERLOCATION__PHONE2);
        mPostal = c.getString(DBAdapter.COL_POS_BPARTNERLOCATION__POSTAL);
        mFax = c.getString(DBAdapter.COL_POS_BPARTNERLOCATION__FAX);
        mCity = c.getString(DBAdapter.COL_POS_BPARTNERLOCATION__CITY);
        mRegion = c.getString(DBAdapter.COL_POS_BPARTNERLOCATION__REGION);
        mCountry = c.getString(DBAdapter.COL_POS_BPARTNERLOCATION__COUNTRY);
        mContact = c.getString(DBAdapter.COL_POS_BPARTNERLOCATION__CONTACTPERSON);
        mEmail = c.getString(DBAdapter.COL_POS_BPARTNERLOCATION__EMAIL);
        mBPartnerId = c.getLong(DBAdapter.COL_POS_BPARTNERLOCATION__BPARTNER_ID);
    }

    @Override
    public void initDefaultValues() {
        super.initDefaultValues();
        mAddress = null;
        mPhone = null;
        mPhone2 = null;
        mPostal = null;
        mFax = null;
        mCity = null;
        mRegion = null;
        mCountry = null;
        mContact = null;

        if (mArgumentsBundle.containsKey(BaseEditFragment.BPARTNER_ID_KEY)) {
            mBPartnerId = mArgumentsBundle.getLong(BaseEditFragment.BPARTNER_ID_KEY);
        }
        else {
            mBPartnerId = -1;
        }
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        ckIsActive = mRootView.findViewById(R.id.ckIsActive);
        etAddress = mRootView.findViewById(R.id.etAddress);
        acPhone1 = mRootView.findViewById(R.id.acPhone1);
        acPhone2 = mRootView.findViewById(R.id.acPhone2);
        acPostal = mRootView.findViewById(R.id.acPostal);
        acFax = mRootView.findViewById(R.id.acFax);
        acCity = mRootView.findViewById(R.id.acCity);
        acRegion = mRootView.findViewById(R.id.acRegion);
        acCountry = mRootView.findViewById(R.id.acCountry);
        acContact = mRootView.findViewById(R.id.acContact);
        acEmail = mRootView.findViewById(R.id.acEmail);
        ImageButton btnEmail = mRootView.findViewById(R.id.btnEmail);
        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent actionIntent;
                if (acEmail.getText().toString().length() > 0) {
                    actionIntent = new Intent(Intent.ACTION_SENDTO);
                    actionIntent.setData(Uri.parse("mailto:"));
                    String to[] = {acEmail.getText().toString()};
                    actionIntent.putExtra(Intent.EXTRA_EMAIL, to);
                    actionIntent.putExtra(Intent.EXTRA_TEXT, "\n\n\n\n--\nSent from AndiCar (http://www.andicar.org)\n"
                            + "Manage your cars with the power of open source.");
                    BPartnerLocationEditFragment.this.startActivity(Intent.createChooser(actionIntent, "Send mail..."));
                }
            }
        });
    }

    @Override
    protected void initSpecificControls() {
        String t[] = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_BPARTNERLOCATION__CITY, -1, 0);
        if (t != null) {
            ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, t);
//            acCity.setOnKeyListener(this);
            acCity.setAdapter(cityAdapter);
        }

        t = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_BPARTNERLOCATION__REGION, -1, 0);
        if (t != null) {
            ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, t);
            acRegion.setAdapter(regionAdapter);
        }

        t = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_BPARTNERLOCATION__COUNTRY, -1, 0);
        if (t != null) {
            ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, t);
            acCountry.setAdapter(countryAdapter);
        }

        t = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_BPARTNERLOCATION__CONTACTPERSON, -1, 0);
        if (t != null) {
            ArrayAdapter<String> contactAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, t);
            acContact.setAdapter(contactAdapter);
        }

        t = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_BPARTNERLOCATION__EMAIL, -1, 0);
        if (t != null) {
            ArrayAdapter<String> emailAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, t);
            acEmail.setAdapter(emailAdapter);
        }

        t = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_BPARTNERLOCATION__PHONE, mBPartnerId, 0);
        if (t != null) {
            ArrayAdapter<String> phoneAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, t);
            acPhone1.setAdapter(phoneAdapter);
        }

        t = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_BPARTNERLOCATION__PHONE2, mBPartnerId, 0);
        if (t != null) {
            ArrayAdapter<String> phone2Adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, t);
            acPhone2.setAdapter(phone2Adapter);
        }

        t = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_BPARTNERLOCATION__POSTAL, -1, 0);
        if (t != null) {
            ArrayAdapter<String> postalAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, t);
            acPostal.setAdapter(postalAdapter);
        }

        t = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_BPARTNERLOCATION__FAX, mBPartnerId, 0);
        if (t != null) {
            ArrayAdapter<String> faxAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, t);
            acFax.setAdapter(faxAdapter);
        }

        t = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_GEN_USER_COMMENT, -1, 0);
        if (t != null) {
            ArrayAdapter<String> faxAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, t);
            acUserComment.setAdapter(faxAdapter);
        }
    }

    @Override
    protected void showValuesInUI() {
        etName.setText(mName);
        acUserComment.setText(mUserComment);
        etAddress.setText(mAddress);
        acPhone1.setText(mPhone);
        acPhone2.setText(mPhone2);
        acPostal.setText(mPostal);
        acFax.setText(mFax);
        acCity.setText(mCity);
        acRegion.setText(mRegion);
        acCountry.setText(mCountry);
        acContact.setText(mContact);
        acEmail.setText(mEmail);
        ckIsActive.setChecked(mIsActive);

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
        cvData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID, mBPartnerId);
        cvData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS, etAddress.getText().toString());
        cvData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__PHONE, acPhone1.getText().toString());
        cvData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__PHONE2, acPhone2.getText().toString());
        cvData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__POSTAL, acPostal.getText().toString());
        cvData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__FAX, acFax.getText().toString());
        cvData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__CITY, acCity.getText().toString());
        cvData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__REGION, acRegion.getText().toString());
        cvData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__COUNTRY, acCountry.getText().toString());
        cvData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__CONTACTPERSON, acContact.getText().toString());
        cvData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__EMAIL, acEmail.getText().toString());

        int dbRetVal;
        String errMsg;
        if (mRowId == -1) {
            dbRetVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_BPARTNERLOCATION, cvData)).intValue();
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
            dbRetVal = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_BPARTNERLOCATION, mRowId, cvData);
            if (dbRetVal != -1) {
                errMsg = mResource.getString(dbRetVal);
                if (dbRetVal == R.string.error_000) {
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
        outState.putLong(BaseEditFragment.BPARTNER_ID_KEY, mBPartnerId);
    }
}
