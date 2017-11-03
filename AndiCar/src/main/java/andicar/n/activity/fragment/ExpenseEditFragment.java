package andicar.n.activity.fragment;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.math.BigDecimal;

import andicar.n.persistence.DBAdapter;
import andicar.n.service.JobStarter;
import andicar.n.service.ToDoNotificationJob;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by miki on 13.03.2017.
 */

public class ExpenseEditFragment extends BaseEditFragment {

    private static final int INSERT_MODE__PRICE = 0;
    private static final int INSERT_MODE__AMOUNT = 1;

    private long mCarDefaultCurrencyId;
    private long mUOMId;
    private long mBPartnerId;

    private String mCurrencyCode;
    private String mCarDefaultCurrencyCode;
    private String mIndex;
    private String mDocumentNo;
    private String mBPartnerName;
    private String mAddress;
    private String mTag;

    private ArrayAdapter<String> mAddressAdapter;

    private BigDecimal mConversionRate;
    private BigDecimal mEnteredAmount;
    private BigDecimal mConvertedAmount;
    private BigDecimal mEnteredPrice;
    private BigDecimal mConvertedPrice;
    private BigDecimal mQuantity;

    private Spinner spnUOM;

    private EditText etIndex;
    private EditText etUserInput;
    private EditText etConversionRate;
    private EditText etQuantity;

    private TextView tvConvertedAmountValue;
    private TextView tvConvertedAmountLabel;
    private TextView tvCalculatedTextLabel;
    private TextView tvCalculatedTextContent;

    private AutoCompleteTextView acBPartner;
    private AutoCompleteTextView acAddress;

    private RadioButton rbInsertModePrice;
    private RadioButton rbInsertModeAmount;
    private LinearLayout llConversionRateZone1;
    private LinearLayout llConversionRateZone2;
    private LinearLayout llCalculatedTextZone;

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!viewsLoaded) {
                return;
            }

            try {
                if (rbInsertModePrice.isChecked()) {
                    mEnteredPrice = new BigDecimal(etUserInput.getText().toString());
                    calculateAmount();
                }
                else {
                    mEnteredAmount = new BigDecimal(etUserInput.getText().toString());
                    calculatePrice();
                }

            }
            catch (NumberFormatException ignored) {
            }
            if (mCurrencyId != mCarDefaultCurrencyId) {
                try {
                    if (etConversionRate.getText().toString().length() > 0) {
                        setConversionRate(new BigDecimal(etConversionRate.getText().toString()));
                    }
                    if (mConversionRate != null) {
                        calculateConvertedAmount();
                    }
                }
                catch (NumberFormatException ignored) {
                }
            }
        }
    };

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
                    initDefaultValues();
                    break;
            }
        }
    }

    private void loadDataFromDB() {
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_EXPENSE, DBAdapter.COL_LIST_EXPENSE_TABLE, mRowId);

        assert c != null;

        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);

        setCarId(c.getLong(DBAdapter.COL_POS_EXPENSE__CAR_ID));
        setDriverId(c.getLong(DBAdapter.COL_POS_EXPENSE__DRIVER_ID));
        setExpCategoryId(c.getLong(DBAdapter.COL_POS_EXPENSE__EXPENSECATEGORY));
        setExpTypeId(c.getLong(DBAdapter.COL_POS_EXPENSE__EXPENSETYPE_ID));
        mlDateTimeInMillis = c.getLong(DBAdapter.COL_POS_EXPENSE__DATE) * 1000;
        initDateTimeFields();
        mIndex = c.getString(DBAdapter.COL_POS_EXPENSE__INDEX);

        setCurrencyId(c.getLong(DBAdapter.COL_POS_EXPENSE__CURRENCYENTERED_ID));

        try {
            if (c.getString(DBAdapter.COL_POS_EXPENSE__AMOUNTENTERED) != null) {
                mEnteredAmount = new BigDecimal(c.getString(DBAdapter.COL_POS_EXPENSE__AMOUNTENTERED));
            }
            else {
                mEnteredAmount = null;
            }
        }
        catch (NumberFormatException ignored) {
            mEnteredAmount = null;
        }

        try {
            if (c.getString(DBAdapter.COL_POS_EXPENSE__CURRENCYRATE) != null) {
                setConversionRate(new BigDecimal(c.getString(DBAdapter.COL_POS_EXPENSE__CURRENCYRATE)));
            }
            else {
                setConversionRate(null);
            }
        }
        catch (NumberFormatException ignored) {
            setConversionRate(null);
        }

        try {
            if (c.getString(DBAdapter.COL_POS_EXPENSE__AMOUNT) != null) {
                mConvertedAmount = new BigDecimal(c.getString(DBAdapter.COL_POS_EXPENSE__AMOUNT));
            }
            else {
                mConvertedAmount = null;
            }
        }
        catch (NumberFormatException ignored) {
            mConvertedAmount = null;
        }

        try {
            if (c.getString(DBAdapter.COL_POS_EXPENSE__QUANTITY) != null) {
                mQuantity = new BigDecimal(c.getString(DBAdapter.COL_POS_EXPENSE__QUANTITY));
            }
            else {
                mQuantity = null;
            }
        }
        catch (NumberFormatException ignored) {
            mQuantity = null;
        }

        mDocumentNo = c.getString(DBAdapter.COL_POS_EXPENSE__DOCUMENTNO);
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);

        if (c.getString(DBAdapter.COL_POS_EXPENSE__UOM_ID) != null && c.getString(DBAdapter.COL_POS_EXPENSE__UOM_ID).length() > 0) {
            mUOMId = c.getLong(DBAdapter.COL_POS_EXPENSE__UOM_ID);
        }
        else {
            mUOMId = -1;
        }

        Cursor c2;
        //bpartner
        if (c.getString(DBAdapter.COL_POS_EXPENSE__BPARTNER_ID) != null && c.getString(DBAdapter.COL_POS_EXPENSE__BPARTNER_ID).length() > 0) {
            mBPartnerId = c.getLong(DBAdapter.COL_POS_EXPENSE__BPARTNER_ID);
            String selection = DBAdapter.COL_NAME_GEN_ROWID + "= ? ";
            String[] selectionArgs = {Long.toString(mBPartnerId)};
            c2 = mDbAdapter.query(DBAdapter.TABLE_NAME_BPARTNER, DBAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs, null);
            if (c2.moveToFirst()) {
                mBPartnerName = c2.getString(DBAdapter.COL_POS_GEN_NAME);
            }
            c2.close();

            if (c.getString(DBAdapter.COL_POS_EXPENSE__BPARTNER_LOCATION_ID) != null
                    && c.getString(DBAdapter.COL_POS_EXPENSE__BPARTNER_LOCATION_ID).length() > 0) {
                long mAddressId = c.getLong(DBAdapter.COL_POS_EXPENSE__BPARTNER_LOCATION_ID);
                selectionArgs[0] = Long.toString(mAddressId);
                c2 = mDbAdapter.query(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_LIST_BPARTNERLOCATION_TABLE, selection, selectionArgs,
                        null);
                if (c2.moveToFirst()) {
                    mAddress = c2.getString(DBAdapter.COL_POS_BPARTNERLOCATION__ADDRESS);
                }
                c2.close();
            }
            mAddressAdapter = null;
            if (getActivity() != null) {
                String[] entries = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS, null, mBPartnerId, 0);
                if (entries != null) {
                    mAddressAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, entries);
                }
            }
        }
        else {
            mBPartnerId = -1;
        }

        //fill tag
        if (c.getString(DBAdapter.COL_POS_EXPENSE__TAG_ID) != null && c.getString(DBAdapter.COL_POS_EXPENSE__TAG_ID).length() > 0) {
            mTagId = c.getLong(DBAdapter.COL_POS_EXPENSE__TAG_ID);
            String selection = DBAdapter.COL_NAME_GEN_ROWID + "= ? ";
            String[] selectionArgs = {Long.toString(mTagId)};
            c2 = mDbAdapter.query(DBAdapter.TABLE_NAME_TAG, DBAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs, null);
            if (c2.moveToFirst()) {
                mTag = c2.getString(DBAdapter.COL_POS_GEN_NAME);
            }
            c2.close();
        }
        c.close();
    }

    @Override
    public void initDefaultValues() {
        super.initDefaultValues();
        mCarDefaultCurrencyId = mDbAdapter.getCarCurrencyID(mCarId);
        mCarDefaultCurrencyCode = mDbAdapter.getCurrencyCode(mCarDefaultCurrencyId);

        setCurrencyId(mCarDefaultCurrencyId);

        mUOMId = -1;
        mEnteredPrice = null;
        mConvertedPrice = null;
        mEnteredAmount = null;
        mConvertedAmount = null;
        mQuantity = null;
        setConversionRate(null);

        mBPartnerId = -1;
        mBPartnerName = null;
        mAddress = null;
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        showDateTime();

        llConversionRateZone1 = mRootView.findViewById(R.id.llConversionRateZone1);
        llConversionRateZone2 = mRootView.findViewById(R.id.llConversionRateZone2);
        llCalculatedTextZone = mRootView.findViewById(R.id.llCalculatedTextZone);

        etIndex = mRootView.findViewById(R.id.etIndex);
        etConversionRate = mRootView.findViewById(R.id.etConversionRate);
        etConversionRate.addTextChangedListener(textWatcher);
        etQuantity = mRootView.findViewById(R.id.etQuantity);
        etQuantity.addTextChangedListener(textWatcher);
        etUserInput = mRootView.findViewById(R.id.etUserInput);
        etUserInput.addTextChangedListener(textWatcher);

        acBPartner = mRootView.findViewById(R.id.acBPartner);
        acBPartner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 0) {
                    acAddress.setEnabled(false);
                    acAddress.setText(null);
                    acAddress.setHint(mResource.getString(R.string.gen_bpartner).replace(":", "") + " "
                            + mResource.getString(R.string.gen_required).replace(":", "").toLowerCase());
                }
                else {
                    acAddress.setEnabled(true);
                    acAddress.setHint(null);
                }
            }
        });

        //for init address autocomplete
        acBPartner.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    String selection = "UPPER (" + DBAdapter.COL_NAME_GEN_NAME + ") = ?";
                    String[] selectionArgs = {acBPartner.getText().toString().toUpperCase()};
                    Cursor c = mDbAdapter.query(DBAdapter.TABLE_NAME_BPARTNER, DBAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs,
                            null);
                    String bPartnerIdStr = null;
                    if (c.moveToFirst()) {
                        bPartnerIdStr = c.getString(DBAdapter.COL_POS_GEN_ROWID);
                    }
                    c.close();
                    if (bPartnerIdStr != null && bPartnerIdStr.length() > 0) {
                        mBPartnerId = Long.parseLong(bPartnerIdStr);
                    }
                    else {
                        mBPartnerId = 0;
                    }

                    mAddressAdapter = null;
                    if (ExpenseEditFragment.this.getActivity() != null) {
                        String[] entries = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS, null, mBPartnerId, 0);
                        if (entries != null) {
                            mAddressAdapter = new ArrayAdapter<>(ExpenseEditFragment.this.getActivity(), android.R.layout.simple_list_item_1, entries);
                        }
                        acAddress.setAdapter(mAddressAdapter);
                    }
                }
            }
        });
        acAddress = mRootView.findViewById(R.id.acAddress);
        acAddress.setAdapter(mAddressAdapter);

        tvConvertedAmountValue = mRootView.findViewById(R.id.tvConvertedAmountValue);
        tvConvertedAmountLabel = mRootView.findViewById(R.id.tvConvertedAmountLabel);
        tvCalculatedTextContent = mRootView.findViewById(R.id.tvCalculatedTextContent);
        tvCalculatedTextLabel = mRootView.findViewById(R.id.tvCalculatedTextLabel);

        rbInsertModePrice = mRootView.findViewById(R.id.rbInsertModePrice);
        rbInsertModeAmount = mRootView.findViewById(R.id.rbInsertModeAmount);
        RadioGroup rg = mRootView.findViewById(R.id.rgInsertMode);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (checkedId == rbInsertModePrice.getId()) {
                    ExpenseEditFragment.this.setInsertMode(INSERT_MODE__PRICE);
                }
                else {
                    ExpenseEditFragment.this.setInsertMode(INSERT_MODE__AMOUNT);
                }

            }
        });

        spnUOM = mRootView.findViewById(R.id.spnUOM);
        spnUOM.setTag(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG);
        spnUOM.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                long newId = mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_UOM, adapterView.getAdapter().getItem(i).toString());
                setSpinnerTextToCode(adapterView, newId, view);

                if (adapterView.getTag() != null && adapterView.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                    adapterView.setTag(null);
                    return;
                }
                mUOMId = newId;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        viewsLoaded = true;
        setInsertMode(INSERT_MODE__AMOUNT);
    }

    @Override
    protected void initSpecificControls() {
        ArrayAdapter<String> mBPartnerAdapter;
        Utils.initSpinner(mDbAdapter, spnUOM, DBAdapter.TABLE_NAME_UOM, DBAdapter.WHERE_CONDITION_ISACTIVE, mUOMId, true);
        //setup bpartner adapter
        mBPartnerAdapter = null;
        if (getContext() != null) {
            String[] entries = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNER, null, null, 0, 0);
            if (entries != null) {
                mBPartnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, entries);
            }
            acBPartner.setAdapter(mBPartnerAdapter);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void showValuesInUI() {
        etIndex.setText(mIndex);
        etUserInput.setText(Utils.numberToString(mEnteredAmount, false, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT));

        if (mConversionRate != null) {
            etConversionRate.setText(mConversionRate.toString());
        }
        else {
            etConversionRate.setText("");
        }

        if (mQuantity != null) {
            etQuantity.setText(mQuantity.toString());
        }
        else {
            etQuantity.setText("");
        }

        tvConvertedAmountValue.setText(Utils.numberToString(mConvertedAmount, false, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT));

        etDocumentNo.setText(mDocumentNo);
        acUserComment.setText(mUserComment);

        if (mBPartnerId > -1) {
            acBPartner.setText(mBPartnerName);
            acAddress.setText(mAddress);
        }
        else {
            acBPartner.setText("");
            acAddress.setEnabled(false);
            acAddress.setText(null);
            acAddress.setHint(mResource.getString(R.string.gen_bpartner).replace(":", "") + " " + mResource.getString(R.string.gen_required).replace(":", ""));
        }

        acTag.setText(mTag);
        acUserComment.setText(mUserComment);
        setSpecificLayout();
    }

    @Override
    public void setSpecificLayout() {
        if (!viewsLoaded) {
            return;
        }

        setConversionRateZoneVisible(mCarDefaultCurrencyId != mCurrencyId);
        mCurrencyCode = mDbAdapter.getCurrencyCode(mCurrencyId);
        if (mConversionRate == null && mCurrencyId != mCarDefaultCurrencyId)
            setConversionRate(mDbAdapter.getCurrencyRate(mCurrencyId, mCarDefaultCurrencyId));
        etConversionRate.setText("");
        if (mConversionRate != null) {
            etConversionRate.append(mConversionRate.toString());
        }
    }

    @Override
    protected boolean saveData() {
        ContentValues data = new ContentValues();
        Bundle analyticsParams = new Bundle();

        data.put(DBAdapter.COL_NAME_GEN_NAME, "Expense");
        data.put(DBAdapter.COL_NAME_GEN_ISACTIVE, "Y");
        data.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, acUserComment.getText().toString());
        data.put(DBAdapter.COL_NAME_EXPENSE__CAR_ID, mCarId);
        data.put(DBAdapter.COL_NAME_EXPENSE__DRIVER_ID, mDriverId);
        data.put(DBAdapter.COL_NAME_EXPENSE__EXPENSECATEGORY_ID, mExpCategoryId);
        data.put(DBAdapter.COL_NAME_EXPENSE__EXPENSETYPE_ID, mExpTypeId);
        if (etIndex.getText().toString().length() > 0) {
            data.put(DBAdapter.COL_NAME_EXPENSE__INDEX, etIndex.getText().toString());
        }
        else {
            data.put(DBAdapter.COL_NAME_EXPENSE__INDEX, (String) null);
        }

        if (mEnteredAmount != null) {
            data.put(DBAdapter.COL_NAME_EXPENSE__AMOUNTENTERED, mEnteredAmount.toString());
        }
        else {
            data.put(DBAdapter.COL_NAME_EXPENSE__AMOUNTENTERED, (String) null);
        }

        if (mEnteredPrice != null) {
            data.put(DBAdapter.COL_NAME_EXPENSE__PRICEENTERED, mEnteredPrice.toString());
        }
        else {
            data.put(DBAdapter.COL_NAME_EXPENSE__PRICEENTERED, (String) null);
        }

        if (etQuantity.getText().toString().trim().length() > 0) {
            data.put(DBAdapter.COL_NAME_EXPENSE__QUANTITY, etQuantity.getText().toString());
        }
        else {
            data.put(DBAdapter.COL_NAME_EXPENSE__QUANTITY, (String) null);
        }


        if (mUOMId != -1) {
            data.put(DBAdapter.COL_NAME_EXPENSE__UOM_ID, mUOMId);
        }
        else {
            data.put(DBAdapter.COL_NAME_EXPENSE__UOM_ID, (Long) null);
        }

        data.put(DBAdapter.COL_NAME_EXPENSE__CURRENCYENTERED_ID, mCurrencyId);
        data.put(DBAdapter.COL_NAME_EXPENSE__CURRENCY_ID, mCarDefaultCurrencyId);
        if (mCurrencyId == mCarDefaultCurrencyId) {
            if (mEnteredAmount == null) {
                if (rbInsertModePrice.isChecked()) {
                    calculateAmount();
                    //check again
                    if (mEnteredAmount == null) { //notify the user
                        Toast toast = Toast.makeText(getActivity(), mResource.getString(R.string.gen_amount_label) + " ?", Toast.LENGTH_LONG);
                        toast.show();
                        return false;
                    }
                }
                else {
                    Toast toast = Toast.makeText(getActivity(), mResource.getString(R.string.gen_amount_label) + " ?", Toast.LENGTH_LONG);
                    toast.show();
                    return false;
                }
                analyticsParams.putInt(ConstantValues.ANALYTICS_IS_MULTI_CURRENCY, 0);
            }
            data.put(DBAdapter.COL_NAME_EXPENSE__AMOUNT, mEnteredAmount.toString());
            if (mEnteredPrice != null) {
                data.put(DBAdapter.COL_NAME_EXPENSE__PRICE, mEnteredPrice.toString());
            }
            else {
                data.put(DBAdapter.COL_NAME_EXPENSE__PRICE, (String) null);
            }

            data.put(DBAdapter.COL_NAME_EXPENSE__CURRENCYRATE, "1");
        }
        else {
            data.put(DBAdapter.COL_NAME_EXPENSE__AMOUNT, mConvertedAmount.toString());
            if (mConvertedPrice != null) {
                data.put(DBAdapter.COL_NAME_EXPENSE__PRICE, mConvertedPrice.toString());
            }
            else {
                data.put(DBAdapter.COL_NAME_EXPENSE__PRICE, (String) null);
            }

            data.put(DBAdapter.COL_NAME_EXPENSE__CURRENCYRATE, mConversionRate.toString());
            analyticsParams.putInt(ConstantValues.ANALYTICS_IS_MULTI_CURRENCY, 1);
        }

        data.put(DBAdapter.COL_NAME_EXPENSE__DATE, mlDateTimeInMillis / 1000);
        data.put(DBAdapter.COL_NAME_EXPENSE__DOCUMENTNO, etDocumentNo.getText().toString());

        if (acBPartner.getText().toString().length() > 0) {
            String selection = "UPPER (" + DBAdapter.COL_NAME_GEN_NAME + ") = UPPER( ? )";
            String[] selectionArgs = {acBPartner.getText().toString()};
            Cursor c = mDbAdapter.query(DBAdapter.TABLE_NAME_BPARTNER, DBAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs, null);
            String bPartnerIdStr = null;
            if (c.moveToFirst()) {
                bPartnerIdStr = c.getString(DBAdapter.COL_POS_GEN_ROWID);
            }
            c.close();
            if (bPartnerIdStr != null && bPartnerIdStr.length() > 0) {
                mBPartnerId = Long.parseLong(bPartnerIdStr);
                data.put(DBAdapter.COL_NAME_EXPENSE__BPARTNER_ID, mBPartnerId);
            }
            else {
                ContentValues tmpData = new ContentValues();
                tmpData.put(DBAdapter.COL_NAME_GEN_NAME, acBPartner.getText().toString());
                mBPartnerId = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_BPARTNER, tmpData);
                if (mBPartnerId >= 0) {
                    data.put(DBAdapter.COL_NAME_EXPENSE__BPARTNER_ID, mBPartnerId);
                }
            }

            if (acAddress.getText().toString().length() > 0) {
                String[] selectionArgs2 = {acAddress.getText().toString(), Long.toString(mBPartnerId)};
                selection = "UPPER (" + DBAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS + ") = UPPER( ? ) AND " + DBAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID + " = ?";
                c = mDbAdapter.query(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs2,
                        null);
                String addressIdStr = null;
                if (c.moveToFirst()) {
                    addressIdStr = c.getString(DBAdapter.COL_POS_GEN_ROWID);
                }
                c.close();
                if (addressIdStr != null && addressIdStr.length() > 0) {
                    data.put(DBAdapter.COL_NAME_EXPENSE__BPARTNER_LOCATION_ID, Long.parseLong(addressIdStr));
                }
                else {
                    ContentValues tmpData = new ContentValues();
                    tmpData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID, mBPartnerId);
                    tmpData.put(DBAdapter.COL_NAME_GEN_NAME, acAddress.getText().toString());
                    tmpData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS, acAddress.getText().toString());
                    long newAddressId = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_BPARTNERLOCATION, tmpData);
                    if (newAddressId >= 0) {
                        data.put(DBAdapter.COL_NAME_EXPENSE__BPARTNER_LOCATION_ID, newAddressId);
                    }
                }
            }
            else {
                data.put(DBAdapter.COL_NAME_EXPENSE__BPARTNER_LOCATION_ID, (String) null);
            }
        }
        else {
            data.put(DBAdapter.COL_NAME_EXPENSE__BPARTNER_ID, (String) null);
            data.put(DBAdapter.COL_NAME_EXPENSE__BPARTNER_LOCATION_ID, (String) null);
        }

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
                data.put(DBAdapter.COL_NAME_EXPENSE__TAG_ID, mTagId);
            }
            else {
                ContentValues tmpData = new ContentValues();
                tmpData.put(DBAdapter.COL_NAME_GEN_NAME, acTag.getText().toString());
                mTagId = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_TAG, tmpData);
                if (mTagId >= 0) {
                    data.put(DBAdapter.COL_NAME_EXPENSE__TAG_ID, mTagId);
                }
            }
        }
        else {
            data.put(DBAdapter.COL_NAME_EXPENSE__TAG_ID, (String) null);
        }

        int dbRetVal;
        if (mRowId == -1) {
            dbRetVal = ((Long) mDbAdapter.createRecord(DBAdapter.TABLE_NAME_EXPENSE, data)).intValue();
            if (dbRetVal < 0) {
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
            dbRetVal = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_EXPENSE, mRowId, data);
            if (dbRetVal != -1) {
                String errMsg;
                errMsg = mResource.getString(dbRetVal);
                if (dbRetVal == R.string.error_000) {
                    errMsg = errMsg + "\n" + mDbAdapter.mErrorMessage;
                }
                Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), errMsg, mDbAdapter.mException, false);
                return false;
            }
        }

        SharedPreferences.Editor prefEditor = mPreferences.edit();
        if (mPreferences.getBoolean(AndiCar.getAppResources().getString(R.string.pref_key_gen_remember_last_tag), false) && mTagId > 0) {
            prefEditor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_gen_last_tag_id), mTagId);
        }

        prefEditor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_driver_id), mDriverId);
        prefEditor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_expense_last_selected_expense_type_id), mExpTypeId);
        prefEditor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_expense_last_selected_expense_category_id), mExpCategoryId);
        prefEditor.apply();

        //check if mileage to-do exists
        if (etIndex.getText().toString().length() > 0) {
//            Intent intent = new Intent(getActivity(), ToDoNotificationService.class);
//            intent.putExtra(ToDoNotificationJob.SET_JUST_NEXT_RUN_KEY, false);
//            intent.putExtra(ToDoManagementService.CAR_ID_KEY, mCarId);
//            getActivity().startService(intent);
            Bundle serviceParams = new Bundle();
            serviceParams.putLong(ToDoNotificationJob.CAR_ID_KEY, mCarId);
            JobStarter.startServicesUsingFBJobDispatcher(getActivity(), JobStarter.SERVICE_STARTER_START_TODO_NOTIFICATION_SERVICE, serviceParams);
        }

        analyticsParams.putInt(ConstantValues.ANALYTICS_IS_TEMPLATE_USED, (isTemplateUsed ? 1 : 0));
        Utils.sendAnalyticsEvent(getActivity(), "ExpenseEdit", analyticsParams, false);

        return true;
    }

    @Override
    public void setCurrencyId(long currencyId) {
        super.setCurrencyId(currencyId);
        if (!viewsLoaded) {
            return;
        }
        setSpecificLayout();
        if (rbInsertModeAmount.isChecked()) {
            calculatePrice();
        }
        else {
            calculateAmount();
        }
    }

    @Override
    public void setCarId(long carId) {
        super.setCarId(carId);
        mCarDefaultCurrencyId = mDbAdapter.getCarCurrencyID(carId);
        mCarDefaultCurrencyCode = mDbAdapter.getCurrencyCode(mCarDefaultCurrencyId);
//        setConversionRate(BigDecimal.ONE);
        setSpecificLayout();
    }

    private void setConversionRate(BigDecimal rate) {
        mConversionRate = rate;
    }

    @SuppressLint("SetTextI18n")
    private void setConversionRateZoneVisible(boolean isVisible) {
        if (!viewsLoaded) {
            return;
        }

        if (isVisible) {
            llConversionRateZone1.setVisibility(View.VISIBLE);
            llConversionRateZone2.setVisibility(View.VISIBLE);
            if (mCarDefaultCurrencyCode == null) {
                mCarDefaultCurrencyCode = "";
            }
            tvConvertedAmountLabel.setText(String.format(mResource.getString(R.string.gen_converted_amount_label), mCarDefaultCurrencyCode) + " = ");
//            etConversionRate.setTag(getString(R.string.gen_required));
        }
        else {
//            etConversionRate.setTag(null);
            llConversionRateZone1.setVisibility(View.GONE);
            llConversionRateZone2.setVisibility(View.GONE);
        }
    }

    private void setInsertMode(int insertMode) {
        if (!viewsLoaded) {
            return;
        }

        if (insertMode == INSERT_MODE__PRICE) {
            tvCalculatedTextLabel.setText(mResource.getString(R.string.gen_amount_label));
            etQuantity.setHint(mResource.getString(R.string.gen_required));
            etQuantity.setTag(mResource.getString(R.string.gen_required));
//            etUserInput.setTag(mResource.getString(R.string.gen_required));
            if (etUserInput.getText().length() > 0) {
                mEnteredPrice = new BigDecimal(etUserInput.getText().toString());
            }

            calculateAmount();
        }
        else {
            tvCalculatedTextLabel.setText(mResource.getString(R.string.gen_price_label));
            etQuantity.setHint(null);
            etQuantity.setTag(null);
            //reset to default look (maybe was marked for mandatory)
            etQuantity.setBackground(etIndex.getBackground());
//            etUserInput.setTag(null);
            if (etUserInput.getText().length() > 0) {
                mEnteredAmount = new BigDecimal(etUserInput.getText().toString());
            }

            calculatePrice();
            if (mCurrencyId != mCarDefaultCurrencyId) {
                calculateConvertedAmount();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void calculatePrice() {
        if (!viewsLoaded) {
            return;
        }

        String qtyStr = etQuantity.getText().toString();
        if (qtyStr.length() == 0) {
//            mQuantity = null;
            llCalculatedTextZone.setVisibility(View.GONE);
            return;
        }

        llCalculatedTextZone.setVisibility(View.VISIBLE);
        String amountStr = etUserInput.getText().toString();
        if (amountStr.length() > 0) {
            try {
                mEnteredAmount = new BigDecimal(amountStr);
                mQuantity = new BigDecimal(qtyStr);
                if (mQuantity.signum() != 0) {
                    mEnteredPrice = mEnteredAmount.divide(mQuantity, ConstantValues.DECIMALS_PRICE, ConstantValues.ROUNDING_MODE_PRICE);
                    tvCalculatedTextContent.setText(Utils.numberToString(mEnteredPrice, true, ConstantValues.DECIMALS_PRICE, ConstantValues.ROUNDING_MODE_PRICE) + " "
                            + mCurrencyCode);
                } else {
                    mEnteredPrice = null;
                    tvCalculatedTextContent.setText(null);
                }
            } catch (NumberFormatException ignored) {
            }
        } else {
            mEnteredPrice = null;
            tvCalculatedTextContent.setText(null);
        }
    }

    @SuppressLint("SetTextI18n")
    private void calculateAmount() {
        if (!viewsLoaded) {
            return;
        }

        String qtyStr = etQuantity.getText().toString();
        if (qtyStr.length() == 0) {
//            mQuantity = null;
            llCalculatedTextZone.setVisibility(View.GONE);
            return;
        }

        llCalculatedTextZone.setVisibility(View.VISIBLE);
        String priceStr = etUserInput.getText().toString();
        if (priceStr.length() > 0) {
            try {
                mEnteredPrice = new BigDecimal(priceStr);
                mQuantity = new BigDecimal(qtyStr);
                mEnteredAmount = mEnteredPrice.multiply(mQuantity).setScale(ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT);
                tvCalculatedTextContent.setText(Utils.numberToString(mEnteredAmount, true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " "
                        + mCurrencyCode);
            } catch (NumberFormatException ignored) {
            }
        } else {
            mEnteredAmount = null;
            tvCalculatedTextContent.setText(null);
        }

        if (mCurrencyId != mCarDefaultCurrencyId) {
            calculateConvertedAmount();
        }
    }

    private void calculateConvertedAmount() {
        if (!viewsLoaded) {
            return;
        }

        if (mConversionRate == null) {
            tvConvertedAmountValue.setText("");
            return;
        }
        if (mCarDefaultCurrencyId == mCurrencyId) {
            return;
        }
        String convertedAmountStr;
        try {
            if (mEnteredAmount != null) {
                mConvertedAmount = mEnteredAmount.multiply(mConversionRate).setScale(ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT);
                convertedAmountStr = Utils.numberToString(mConvertedAmount, true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT);
                tvConvertedAmountValue.setText(convertedAmountStr);
            }
            if (mEnteredPrice != null) {
                mConvertedPrice = mEnteredPrice.multiply(mConversionRate).setScale(ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT);
            }
        }
        catch (NumberFormatException ignored) {
        }
    }

    public long getUOMId() {
        return mUOMId;
    }

    public void setUOMId(long UOMId) {
        this.mUOMId = UOMId;
    }
}
