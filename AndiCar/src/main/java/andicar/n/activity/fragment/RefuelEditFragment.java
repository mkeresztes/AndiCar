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

package andicar.n.activity.fragment;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import java.math.RoundingMode;

import andicar.n.persistence.DB;
import andicar.n.persistence.DBAdapter;
import andicar.n.service.JobStarter;
import andicar.n.service.ToDoNotificationJob;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 13.12.2016.
 * Fragment for editing refuels
 */

@SuppressWarnings("FieldCanBeLocal")
public class RefuelEditFragment extends BaseEditFragment {

    private static final int INSERT_MODE__PRICE = 0;
    private static final int INSERT_MODE__AMOUNT = 1;
    private int mInsertMode = 0; //0 = price; 1 = amount
    private EditText etIndex;
    private EditText etQuantity;
    private EditText etUserInput;
    private EditText etConversionRate;

    private AutoCompleteTextView acBPartner;
    private AutoCompleteTextView acAddress;

    private TextView tvCalculatedTextContent;
    private TextView tvCalculatedTextLabel;
    private TextView tvBaseUOMQtyValue;
    private TextView tvConversionRateLabel;
    private TextView tvConvertedAmountLabel;

    private Spinner spnUomFuel;

    private CheckBox ckIsFullRefuel;
    private CheckBox ckIsAlternativeFuel;

    private RadioButton rbInsertModePrice;

    private LinearLayout llBaseUOMQtyZone;
    private LinearLayout llConversionRateZone;
    private LinearLayout llConvertedAmountZone;

    private long mCarDefaultCurrencyId = -1;
    private long mDefaultUOMVolumeId = -1;
    private long mUOMFuelId = -1;
    private long mBPartnerId = -1;
    private long mBPartnerLocationId = -1;

    private String mCarDefaultCurrencyCode = "";
    private String mCurrencyCode = "";
    private String mDefaultUOMVolumeCode = "";
    private String mDocumentNo = "";

    private BigDecimal mCurrencyConversionRate = null;
    private BigDecimal mPriceEntered = null;
    private BigDecimal mPriceConverted = null;
    private BigDecimal mAmountEntered = null;
    private BigDecimal mAmountConverted = null;
    private BigDecimal mUOMVolumeConversionRate = null;
    private BigDecimal mBaseUOMQty = null;
    private final TextWatcher textWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable e) {
            if (etConversionRate.getText().toString().length() > 0) {
                try {
                    setCurrencyConversionRate(new BigDecimal(etConversionRate.getText().toString()));
                }
                catch (NumberFormatException ex) {
                    setCurrencyConversionRate(null);
                }
            }
            else {
                setCurrencyConversionRate(null);
            }

            if (mUOMFuelId != mDefaultUOMVolumeId && etQuantity.toString().length() > 0) {
                calculateBaseUOMQty();
            }
            calculatePriceAmount();
        }
    };
    private BigDecimal mIndex = null;
    private BigDecimal mQuantityEntered = null;
    private BigDecimal mQuantityBaseUOM = null;
    private boolean mIsFullRefuel = false;
    private boolean mIsAlternativeFuel = false;
    private ArrayAdapter<String> mAddressAdapter;
    private ArrayAdapter<String> mBPartnerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //this fragment can uses templates for filling data
        isUseTemplate = true;
        if (savedInstanceState != null) {
            setUOMFuelId(savedInstanceState.getLong("mUOMFuelId"));
            mCarDefaultCurrencyId = savedInstanceState.getLong("mCarDefaultCurrencyId");
            mCarDefaultCurrencyCode = savedInstanceState.getString("mCarDefaultCurrencyCode");
            mDefaultUOMVolumeId = savedInstanceState.getLong("mDefaultUOMVolumeId");
            mDefaultUOMVolumeCode = savedInstanceState.getString("mDefaultUOMVolumeCode");

            setSpecificLayout();
        }
        else {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        showDateTime();

        return mRootView;
    }

    @SuppressLint("SetTextI18n")
    private void loadDataFromDB() {
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_REFUEL, DBAdapter.COL_LIST_REFUEL_TABLE, mRowId);

        assert c != null;
        super.setCarId(c.getLong(DBAdapter.COL_POS_REFUEL__CAR_ID));

        mCarDefaultCurrencyId = mDbAdapter.getCarCurrencyID(mCarId);
        mCarDefaultCurrencyCode = mDbAdapter.getCurrencyCode(mCarDefaultCurrencyId);
        mDefaultUOMVolumeId = mDbAdapter.getCarUOMFuelID(mCarId, true);
        mDefaultUOMVolumeCode = mDbAdapter.getUOMCode(mDefaultUOMVolumeId);

        setDriverId(c.getLong(DBAdapter.COL_POS_REFUEL__DRIVER_ID));
        setExpCatOrFuelTypeId(c.getLong(DBAdapter.COL_POS_REFUEL__EXPENSECATEGORY_ID));
        setExpTypeId(c.getLong(DBAdapter.COL_POS_REFUEL__EXPENSETYPE_ID));
        setUOMFuelId(c.getLong(DBAdapter.COL_POS_REFUEL__UOMVOLUMEENTERED_ID));
        setCurrencyId(c.getLong(DBAdapter.COL_POS_REFUEL__CURRENCYENTERED_ID));
        if (c.getString(DBAdapter.COL_POS_REFUEL__BPARTNER_ID) != null && c.getString(DBAdapter.COL_POS_REFUEL__BPARTNER_ID).length() > 0) {
            mBPartnerId = c.getLong(DBAdapter.COL_POS_REFUEL__BPARTNER_ID);
            if (c.getString(DBAdapter.COL_POS_REFUEL__BPARTNER_LOCATION_ID) != null
                    && c.getString(DBAdapter.COL_POS_REFUEL__BPARTNER_LOCATION_ID).length() > 0) {
                mBPartnerLocationId = c.getLong(DBAdapter.COL_POS_REFUEL__BPARTNER_LOCATION_ID);
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
        if (c.getString(DBAdapter.COL_POS_REFUEL__TAG_ID) != null && c.getString(DBAdapter.COL_POS_REFUEL__TAG_ID).length() > 0) {
            mTagId = c.getLong(DBAdapter.COL_POS_REFUEL__TAG_ID);
            mTagStr = mDbAdapter.getNameById(DBAdapter.TABLE_NAME_TAG, mTagId);
        }

        try {
            setCurrencyConversionRate(new BigDecimal(c.getString(DBAdapter.COL_POS_REFUEL__CURRENCYRATE)));
            mUOMVolumeConversionRate = new BigDecimal(c.getString(DBAdapter.COL_POS_REFUEL__UOMVOLCONVERSIONRATE));
        }
        catch (NumberFormatException ignored) {
        }
        mlDateTimeInMillis = c.getLong(DBAdapter.COL_POS_REFUEL__DATE) * 1000;
        initDateTimeFields();
        mIndex = new BigDecimal(c.getString(DBAdapter.COL_POS_REFUEL__INDEX));
        mQuantityEntered = new BigDecimal(c.getString(DBAdapter.COL_POS_REFUEL__QUANTITYENTERED));
        mQuantityBaseUOM = new BigDecimal(c.getString(DBAdapter.COL_POS_REFUEL__QUANTITY));
        mPriceEntered = new BigDecimal(c.getString(DBAdapter.COL_POS_REFUEL__PRICEENTERED));
        mDocumentNo = c.getString(DBAdapter.COL_POS_REFUEL__DOCUMENTNO);
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        mIsFullRefuel = c.getString(DBAdapter.COL_POS_REFUEL__ISFULLREFUEL).equals("Y");
        mIsAlternativeFuel = c.getString(DBAdapter.COL_POS_REFUEL__ISALTERNATIVEFUEL).equals("Y");

        if (mCurrencyId != mCarDefaultCurrencyId) {
            mCurrencyCode = mDbAdapter.getCurrencyCode(mCurrencyId);
        }

        c.close();
    }

    @Override
    public void initDefaultValues() {
        super.initDefaultValues();

        mCarDefaultCurrencyId = mDbAdapter.getCarCurrencyID(mCarId);
        setCurrencyId(mCarDefaultCurrencyId);

        mCarDefaultCurrencyCode = mDbAdapter.getCurrencyCode(mCarDefaultCurrencyId);
        mCurrencyCode = mCarDefaultCurrencyCode;

        setInsertMode(mPreferences.getInt(AndiCar.getAppResources().getString(R.string.pref_key_refuel_insert_mode), INSERT_MODE__PRICE));

        if (rbInsertModePrice != null) {
            if (mInsertMode == INSERT_MODE__PRICE) {
                rbInsertModePrice.setChecked(true);
            }
            else {
                rbInsertModePrice.setChecked(false);
            }
        }

        setUOMFuelId(mDbAdapter.getCarUOMFuelID(mCarId,
                mDbAdapter.getFuelUOMType(mExpCatOrFuelTypeId).equals(mDbAdapter.getCarFuelUOMType(mCarId, true))));
        mDefaultUOMVolumeId = mUOMFuelId;
        mDefaultUOMVolumeCode = mDbAdapter.getUOMCode(mDefaultUOMVolumeId);

        if (mPreferences.getBoolean(AndiCar.getAppResources().getString(R.string.pref_key_gen_remember_last_tag), false)
                && mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_gen_last_tag_id), 0) > 0) {
            mTagId = mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_gen_last_tag_id), 0);
            String selection = DBAdapter.COL_NAME_GEN_ROWID + "= ? ";
            String[] selectionArgs = {Long.toString(mTagId)};
            Cursor c = mDbAdapter.query(DBAdapter.TABLE_NAME_TAG, DBAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs, null);
            if (c.moveToFirst()) {
                mTagStr = c.getString(DBAdapter.COL_POS_GEN_NAME);
            }
            c.close();
        }
        else {
            mTagStr = null;
        }

        mlDateTimeInMillis = System.currentTimeMillis();
        initDateTimeFields();

        mBPartnerId = -1;
        mBPartnerLocationId = -1;
        setCurrencyConversionRate(null);
        mQuantityBaseUOM = null;
        mQuantityEntered = null;
        mIsAlternativeFuel = false;
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {

        if (mRootView == null) {
            return;
        }

        spnUomFuel = mRootView.findViewById(R.id.spnUomFuel);
        spnUomFuel.setTag(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG);
        spnUomFuel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                long newId = mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_UOM, adapterView.getAdapter().getItem(i).toString());
                setSpinnerTextToCode(adapterView, newId, view);

                if (adapterView.getTag() != null && adapterView.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                    adapterView.setTag(null);
                    return;
                }

                setUOMFuelId(newId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        etIndex = mRootView.findViewById(R.id.etIndex);
        etQuantity = mRootView.findViewById(R.id.etQuantity);
        etQuantity.addTextChangedListener(textWatcher);
        etUserInput = mRootView.findViewById(R.id.etUserInput);
        etUserInput.addTextChangedListener(textWatcher);
        etConversionRate = mRootView.findViewById(R.id.etConversionRate);
        etConversionRate.addTextChangedListener(textWatcher);

        acBPartner = mRootView.findViewById(R.id.acBPartner);
        acBPartner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() == 0) {
                    acAddress.setEnabled(false);
                    acAddress.setText(null);
                    acAddress.setHint(mResource.getString(R.string.fill_up_edit_gas_station).replace(":", "") + " "
                            + mResource.getString(R.string.gen_required).toLowerCase());
                }
                else {
                    acAddress.setEnabled(true);
                    acAddress.setHint(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
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
                    if (getActivity() != null) {
                        String[] entries = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS, null, mBPartnerId, 0);
                        if (entries != null) {
                            mAddressAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, entries);
                        }
                        acAddress.setAdapter(mAddressAdapter);
                    }
                }
            }
        });

        acAddress = mRootView.findViewById(R.id.acAddress);
        acAddress.setAdapter(mAddressAdapter);

        tvCalculatedTextContent = mRootView.findViewById(R.id.tvCalculatedTextContent);
        tvCalculatedTextLabel = mRootView.findViewById(R.id.tvCalculatedTextLabel);
        tvBaseUOMQtyValue = mRootView.findViewById(R.id.tvBaseUOMQtyValue);
        tvConversionRateLabel = mRootView.findViewById(R.id.tvConversionRateLabel);
        tvConvertedAmountLabel = mRootView.findViewById(R.id.tvConvertedAmountLabel);

        ckIsFullRefuel = mRootView.findViewById(R.id.ckIsFullRefuel);
        ckIsAlternativeFuel = mRootView.findViewById(R.id.ckIsAlternativeFuel);
        ckIsAlternativeFuel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (viewsLoaded)
                    mIsAlternativeFuel = isChecked;
            }
        });

        rbInsertModePrice = mRootView.findViewById(R.id.rbInsertModePrice);
        RadioGroup rg = mRootView.findViewById(R.id.rgExpenseInsertMode);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (checkedId == rbInsertModePrice.getId()) {
                    RefuelEditFragment.this.setInsertMode(INSERT_MODE__PRICE);
                }
                else {
                    RefuelEditFragment.this.setInsertMode(INSERT_MODE__AMOUNT);
                }
            }
        });

        llBaseUOMQtyZone = mRootView.findViewById(R.id.llBaseUOMQtyZone);
        llConversionRateZone = mRootView.findViewById(R.id.llConversionRateZone);
        llConvertedAmountZone = mRootView.findViewById(R.id.llConvertedAmountZone);

        viewsLoaded = true;
        setSpecificLayout();
    }

    protected void initSpecificControls() {
        initSpnUomFuel();

        //setup bpartner adapter
        mBPartnerAdapter = null;
        if (getActivity() != null) {
            String[] entries = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_BPARTNER, null,
                    "WHERE " + DB.COL_NAME_GEN_ISACTIVE + " = 'Y' " +
                            " AND LENGTH(TRIM(" + DB.COL_NAME_GEN_NAME + ")) > 0 " +
                            " AND " + DB.COL_NAME_BPARTNER__ISGASSTATION + " = 'Y'", 0, 0);
            if (entries != null) {
                mBPartnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, entries);
            }
            acBPartner.setAdapter(mBPartnerAdapter);
        }
    }

    private void initSpnUomFuel() {
        mUOMFuelId = Utils.initSpinner(mDbAdapter, spnUomFuel, DBAdapter.TABLE_NAME_UOM,
                DBAdapter.WHERE_CONDITION_ISACTIVE + " AND " +
                        DBAdapter.COL_NAME_UOM__UOMTYPE + " = '" + mDbAdapter.getFuelUOMType(mExpCatOrFuelTypeId) + "'", mUOMFuelId, mRowId > 0, false);
//                        DBAdapter.COL_NAME_UOM__UOMTYPE + " = '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "'", mUOMFuelId, false);
        if (viewsLoaded) {
            long selectedUOMId = mDbAdapter.getIdByCode(DB.TABLE_NAME_UOM, spnUomFuel.getSelectedItem().toString());
            if (selectedUOMId != mUOMFuelId && selectedUOMId > 0)
                mUOMFuelId = selectedUOMId;
        }
    }

    @SuppressLint("SetTextI18n")
    protected void showValuesInUI() {
        etIndex.setText(Utils.numberToString(mIndex, false, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH));
        etQuantity.setText(Utils.numberToString(mQuantityEntered, false, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME));
        etUserInput.setText(Utils.numberToString(mPriceEntered, false, ConstantValues.DECIMALS_PRICE, ConstantValues.ROUNDING_MODE_PRICE));
        etConversionRate.setText(Utils.numberToString(mCurrencyConversionRate, false, ConstantValues.DECIMALS_PRICE, ConstantValues.ROUNDING_MODE_PRICE));
        if (mBPartnerId >= 0) {
            acBPartner.setText(mDbAdapter.getNameById(DBAdapter.TABLE_NAME_BPARTNER, mBPartnerId));
        }
        else {
            acBPartner.setText("");
        }
        if (mBPartnerLocationId >= 0) {
            acAddress.setText(mDbAdapter.getNameById(DBAdapter.TABLE_NAME_BPARTNERLOCATION, mBPartnerLocationId));
        }
        else {
            acAddress.setText("");
        }

        if (mDefaultUOMVolumeId != mUOMFuelId) {
            tvBaseUOMQtyValue.setText(Utils.numberToString(mQuantityBaseUOM, true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME) + " " + mDefaultUOMVolumeCode);
            setBaseUOMQtyZoneVisibility(true);
        }

        ckIsFullRefuel.setChecked(mIsFullRefuel);
        rbInsertModePrice.setChecked(true);
        etDocumentNo.setText(mDocumentNo);
        acUserComment.setText(mUserComment);
        acTag.setText(mTagStr);

        calculatePriceAmount();
        calculateBaseUOMQty();
    }

    @Override
    public void setSpecificLayout() {
        if (mRootView == null) {
            return;
        }

        if (mDbAdapter.isAFVCar(mCarId) &&
                mDbAdapter.getCarFuelUOMType(mCarId, true).equals(mDbAdapter.getCarFuelUOMType(mCarId, false))) {
            ckIsAlternativeFuel.setVisibility(View.VISIBLE);
            ckIsAlternativeFuel.setChecked(mIsAlternativeFuel);
        }
        else {
            ckIsAlternativeFuel.setVisibility(View.GONE);
        }

        if (mUOMFuelId != mDefaultUOMVolumeId) {
            setBaseUOMQtyZoneVisibility(true);
        }
        else {
            setBaseUOMQtyZoneVisibility(false);
        }

        mUOMVolumeConversionRate = mDbAdapter.getUOMConversionRate(mUOMFuelId, mDefaultUOMVolumeId);
        calculateBaseUOMQty();

        mCurrencyCode = mDbAdapter.getCurrencyCode(mCurrencyId);
        if (mCurrencyId != mCarDefaultCurrencyId) {
            setConversionRateVisibility(true);
            etConversionRate.setText("");
            if (mCurrencyConversionRate == null) {
                setCurrencyConversionRate(mDbAdapter.getCurrencyRate(mCurrencyId, mCarDefaultCurrencyId));
            }

            if (etConversionRate != null) {
                if (mCurrencyConversionRate != null) {
                    etConversionRate.append(mCurrencyConversionRate.toString());
                }
            }
        }
        else {
            setConversionRateVisibility(false);
        }

        calculatePriceAmount();
    }

    @Override
    protected boolean saveData() {
        Bundle analyticsParams = new Bundle();

        calculatePriceAmount();
        calculateBaseUOMQty();

        ContentValues data = new ContentValues();
        data.put(DBAdapter.COL_NAME_GEN_NAME, ConstantValues.EXPENSES_COL_FROM_REFUEL_TABLE_NAME);
        data.put(DBAdapter.COL_NAME_GEN_ISACTIVE, "Y");
        data.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, acUserComment.getText().toString());
        data.put(DBAdapter.COL_NAME_REFUEL__CAR_ID, mCarId);
        data.put(DBAdapter.COL_NAME_REFUEL__DRIVER_ID, mDriverId);
        data.put(DBAdapter.COL_NAME_REFUEL__EXPENSECATEGORY_ID, mExpCatOrFuelTypeId);
        data.put(DBAdapter.COL_NAME_REFUEL__EXPENSETYPE_ID, mExpTypeId);
        data.put(DBAdapter.COL_NAME_REFUEL__INDEX, etIndex.getText().toString());
        data.put(DBAdapter.COL_NAME_REFUEL__QUANTITYENTERED, etQuantity.getText().toString());
        data.put(DBAdapter.COL_NAME_REFUEL__UOMVOLUMEENTERED_ID, mUOMFuelId);
        calculatePriceAmount();
        if (mPriceEntered == null || mAmountEntered == null) {
            Toast toast = Toast.makeText(getContext(),
                    mResource.getString(R.string.gen_price_label) + ": " + mResource.getString(R.string.gen_required), Toast.LENGTH_SHORT);
            toast.show();
            etUserInput.setText("");
            return false;
        }

        data.put(DBAdapter.COL_NAME_REFUEL__PRICEENTERED, mPriceEntered.toString());
        data.put(DBAdapter.COL_NAME_REFUEL__AMOUNTENTERED, mAmountEntered.toString());
        data.put(DBAdapter.COL_NAME_REFUEL__CURRENCYENTERED_ID, mCurrencyId);
        data.put(DBAdapter.COL_NAME_REFUEL__DATE, mlDateTimeInMillis / 1000);
        data.put(DBAdapter.COL_NAME_REFUEL__DOCUMENTNO, etDocumentNo.getText().toString());
        data.put(DBAdapter.COL_NAME_REFUEL__ISFULLREFUEL, (ckIsFullRefuel.isChecked() ? "Y" : "N"));
        data.put(DBAdapter.COL_NAME_REFUEL__ISALTERNATIVEFUEL, (mIsAlternativeFuel ? "Y" : "N"));

        if (mUOMFuelId == mDefaultUOMVolumeId) {
            data.put(DBAdapter.COL_NAME_REFUEL__QUANTITY, etQuantity.getText().toString());
            data.put(DBAdapter.COL_NAME_REFUEL__UOMVOLUME_ID, mUOMFuelId);
            data.put(DBAdapter.COL_NAME_REFUEL__UOMVOLCONVERSIONRATE, "1");
            analyticsParams.putInt(ConstantValues.ANALYTICS_IS_MULTI_UOM, 0);
        }
        else {
            data.put(DBAdapter.COL_NAME_REFUEL__QUANTITY, mBaseUOMQty.toString());
            data.put(DBAdapter.COL_NAME_REFUEL__UOMVOLUME_ID, mDefaultUOMVolumeId);
            data.put(DBAdapter.COL_NAME_REFUEL__UOMVOLCONVERSIONRATE, mUOMVolumeConversionRate.toString());
            analyticsParams.putInt(ConstantValues.ANALYTICS_IS_MULTI_UOM, 1);
        }

        if (mCurrencyId == mCarDefaultCurrencyId) {
            data.put(DBAdapter.COL_NAME_REFUEL__PRICE, mPriceEntered.toString());
            data.put(DBAdapter.COL_NAME_REFUEL__AMOUNT, mAmountEntered.toString());
            data.put(DBAdapter.COL_NAME_REFUEL__CURRENCY_ID, mCarDefaultCurrencyId);
            data.put(DBAdapter.COL_NAME_REFUEL__CURRENCYRATE, "1");
            analyticsParams.putInt(ConstantValues.ANALYTICS_IS_MULTI_CURRENCY, 0);
        }
        else {
            data.put(DBAdapter.COL_NAME_REFUEL__PRICE, mPriceConverted.toString());
            data.put(DBAdapter.COL_NAME_REFUEL__AMOUNT, mAmountConverted.toString());
            data.put(DBAdapter.COL_NAME_REFUEL__CURRENCY_ID, mCarDefaultCurrencyId);
            data.put(DBAdapter.COL_NAME_REFUEL__CURRENCYRATE, mCurrencyConversionRate.toString());
            analyticsParams.putInt(ConstantValues.ANALYTICS_IS_MULTI_CURRENCY, 1);
        }

        if (acBPartner.getText().toString().length() > 0) {
            String selection = "UPPER (" + DBAdapter.COL_NAME_GEN_NAME + ") = UPPER( ? ) ";
            String[] selectionArgs = {acBPartner.getText().toString()};
            Cursor c = mDbAdapter.query(DBAdapter.TABLE_NAME_BPARTNER, DBAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs, null);
            String bPartnerIdStr = null;
            if (c.moveToFirst()) {
                bPartnerIdStr = c.getString(DBAdapter.COL_POS_GEN_ROWID);
            }
            c.close();
            if (bPartnerIdStr != null && bPartnerIdStr.length() > 0) {
                mBPartnerId = Long.parseLong(bPartnerIdStr);
                data.put(DBAdapter.COL_NAME_REFUEL__BPARTNER_ID, mBPartnerId);
            }
            else {
                ContentValues tmpData = new ContentValues();
                tmpData.put(DBAdapter.COL_NAME_GEN_NAME, acBPartner.getText().toString());
                tmpData.put(DBAdapter.COL_NAME_BPARTNER__ISGASSTATION, "Y");
                mBPartnerId = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_BPARTNER, tmpData);
                if (mBPartnerId >= 0) {
                    data.put(DBAdapter.COL_NAME_REFUEL__BPARTNER_ID, mBPartnerId);
                }
            }

            if (acAddress.getText().toString().length() > 0) {
                selection = "UPPER (" + DBAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS + ") = UPPER( ? ) AND " + DBAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID
                        + " = ?";
                String[] selectionArgs2 = {acAddress.getText().toString(), Long.toString(mBPartnerId)};
                c = mDbAdapter.query(DBAdapter.TABLE_NAME_BPARTNERLOCATION, DBAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs2,
                        null);
                String addressIdStr = null;
                if (c.moveToFirst()) {
                    addressIdStr = c.getString(DBAdapter.COL_POS_GEN_ROWID);
                }
                c.close();
                if (addressIdStr != null && addressIdStr.length() > 0) {
                    data.put(DBAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID, Long.parseLong(addressIdStr));
                }
                else {
                    ContentValues tmpData = new ContentValues();
                    tmpData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID, mBPartnerId);
                    tmpData.put(DBAdapter.COL_NAME_GEN_NAME, acAddress.getText().toString());
                    tmpData.put(DBAdapter.COL_NAME_BPARTNERLOCATION__ADDRESS, acAddress.getText().toString());
                    long newAddressId = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_BPARTNERLOCATION, tmpData);
                    if (newAddressId >= 0) {
                        data.put(DBAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID, newAddressId);
                    }
                }
            }
            else {
                data.put(DBAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID, (String) null);
            }
        }
        else {
            data.put(DBAdapter.COL_NAME_REFUEL__BPARTNER_ID, (String) null);
            data.put(DBAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID, (String) null);
        }

        if (acTag.getText().toString().length() > 0) {
            String selection = "UPPER (" + DBAdapter.COL_NAME_GEN_NAME + ") = UPPER( ? )";
            String[] selectionArgs = {acTag.getText().toString()};
            Cursor c = mDbAdapter.query(DBAdapter.TABLE_NAME_TAG, DBAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs, null);
            String tagIdStr = null;
            if (c.moveToFirst()) {
                tagIdStr = c.getString(DBAdapter.COL_POS_GEN_ROWID);
            }
            c.close();
            if (tagIdStr != null && tagIdStr.length() > 0) {
                mTagId = Long.parseLong(tagIdStr);
                data.put(DBAdapter.COL_NAME_REFUEL__TAG_ID, mTagId);
            }
            else {
                ContentValues tmpData = new ContentValues();
                tmpData.put(DBAdapter.COL_NAME_GEN_NAME, acTag.getText().toString());
                mTagId = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_TAG, tmpData);
                if (mTagId >= 0) {
                    data.put(DBAdapter.COL_NAME_REFUEL__TAG_ID, mTagId);
                }
            }
        }
        else {
            data.put(DBAdapter.COL_NAME_REFUEL__TAG_ID, (String) null);
        }

        if (mOperationType.equals(BaseEditFragment.DETAIL_OPERATION_NEW)) {
            Long createResult = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_REFUEL, data);
            if (createResult.intValue() < 0) {
                if (createResult.intValue() == -1) //DB Error
                {
                    Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), mDbAdapter.mErrorMessage, mDbAdapter.mException);
                }
                else //precondition error
                {
                    Utils.showNotReportableErrorDialog(getActivity(), getString(R.string.gen_error), getString(-1 * createResult.intValue()));
                }

                return false;
            }
        }
        else {
            int updResult = mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_REFUEL, mRowId, data);
            if (updResult != -1) {
                String errMsg;
                errMsg = mResource.getString(updResult);
                if (updResult == R.string.error_000) {
                    errMsg = errMsg + "\n" + mDbAdapter.mErrorMessage;
                }
                Utils.showReportableErrorDialog(getActivity(), getString(R.string.error_sorry), errMsg, mDbAdapter.mException);

                return false;
            }
        }

        SharedPreferences.Editor prefEditor = mPreferences.edit();
        if (mPreferences.getBoolean(AndiCar.getAppResources().getString(R.string.pref_key_gen_remember_last_tag), false) && mTagId > 0) {
            prefEditor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_gen_last_tag_id), mTagId);
        }

        prefEditor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_driver_id), mDriverId);
        prefEditor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_refuel_last_selected_expense_type_id), mExpTypeId);
        prefEditor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_refuel_last_selected_expense_category_id) + "_" + mCarId, mExpCatOrFuelTypeId);
        prefEditor.putInt(AndiCar.getAppResources().getString(R.string.pref_key_refuel_insert_mode), mInsertMode);
        prefEditor.apply();

//        Intent intent = new Intent(getContext(), ToDoNotificationService.class);
//        intent.putExtra(ToDoNotificationJob.SET_JUST_NEXT_RUN_KEY, false);
//        intent.putExtra(ToDoManagementService.CAR_ID_KEY, mCarId);
//        getActivity().startService(intent);
        Bundle serviceParams = new Bundle();
        serviceParams.putLong(ToDoNotificationJob.CAR_ID_KEY, mCarId);
        JobStarter.startServicesUsingFBJobDispatcher(getActivity(), JobStarter.SERVICE_STARTER_START_TODO_NOTIFICATION_SERVICE, serviceParams);

        analyticsParams.putInt(ConstantValues.ANALYTICS_IS_TEMPLATE_USED, isTemplateUsed ? 1 : 0);
        Utils.sendAnalyticsEvent(getActivity(), "RefuelEdit", analyticsParams, false);
        analyticsParams.clear();
        analyticsParams.putInt(ConstantValues.ANALYTICS_IS_AFV, mDbAdapter.isAFVCar(mCarId) ? 1 : 0);
        Utils.sendAnalyticsEvent(getActivity(), "Common", analyticsParams, false);

        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("mUOMFuelId", mUOMFuelId);
        outState.putLong("mCarDefaultCurrencyId", mCarDefaultCurrencyId);
        outState.putString("mCarDefaultCurrencyCode", mCarDefaultCurrencyCode);
        outState.putLong("mDefaultUOMVolumeId", mDefaultUOMVolumeId);
        outState.putString("mDefaultUOMVolumeCode", mDefaultUOMVolumeCode);
    }

    @Override
    public void setCarId(long carId) {
        super.setCarId(carId);

        if (!viewsLoaded) {
            return;
        }

        mUOMFuelId = mDbAdapter.getCarUOMFuelID(mCarId, true);

        initSpnExpCatOrFuelType();
        setExpCatOrFuelTypeId(mDbAdapter.getIdByName(DB.TABLE_NAME_EXPENSECATEGORY, spnExpCatOrFuelType.getSelectedItem().toString()));

        long newCarCurrencyId = mDbAdapter.getCarCurrencyID(mCarId);
        if (newCarCurrencyId != mCurrencyId) {
            setCurrencyId(newCarCurrencyId);
            mCarDefaultCurrencyId = mCurrencyId;
            mCarDefaultCurrencyCode = mDbAdapter.getCurrencyCode(mCarDefaultCurrencyId);
            setCurrencyConversionRate(null);
            mCurrencyId = Utils.initSpinner(mDbAdapter, spnCurrency, DBAdapter.TABLE_NAME_CURRENCY, DBAdapter.WHERE_CONDITION_ISACTIVE, mCurrencyId, mRowId > 0, false);

            setConversionRateVisibility(false);
            calculatePriceAmount();
        }

        setDefaultUOMId();
    }

    @Override
    public void setExpCatOrFuelTypeId(long expCatOrFuelTypeId) {
        long oldFuelId = mExpCatOrFuelTypeId;
        super.setExpCatOrFuelTypeId(expCatOrFuelTypeId);

        if (!viewsLoaded) {
            return;
        }

        if (!mDbAdapter.getFuelUOMType(oldFuelId).equals(mDbAdapter.getFuelUOMType(mExpCatOrFuelTypeId))) {
            setDefaultUOMId();
            initSpnUomFuel();
        }
    }

    private void setDefaultUOMId() {
        long newCarUOMVolumeId;

        if (mDbAdapter.isAFVCar(mCarId)) {
            if (mDbAdapter.getFuelUOMType(mExpCatOrFuelTypeId).equals(mDbAdapter.getCarFuelUOMType(mCarId, true))) {
                //primary fuel
                newCarUOMVolumeId = mDbAdapter.getCarUOMFuelID(mCarId, true);
                mIsAlternativeFuel = false;
            }
            else {
                //alternative fuel
                newCarUOMVolumeId = mDbAdapter.getCarUOMFuelID(mCarId, false);
                mIsAlternativeFuel = true;
            }
        }
        else {
            newCarUOMVolumeId = mDbAdapter.getCarUOMFuelID(mCarId, true);
            mIsAlternativeFuel = false;
        }

        setUOMFuelId(newCarUOMVolumeId);
        mDefaultUOMVolumeId = mUOMFuelId;
        mDefaultUOMVolumeCode = mDbAdapter.getUOMCode(mDefaultUOMVolumeId);

        setBaseUOMQtyZoneVisibility(false);
    }

    @Override
    public void setCurrencyId(long currencyId) {
        super.setCurrencyId(currencyId);
        setSpecificLayout();
    }

    public void setUOMFuelId(long id) {
        mUOMFuelId = id;
        setSpecificLayout();
    }

    private void setCurrencyConversionRate(BigDecimal rate) {
        mCurrencyConversionRate = rate;
    }

    private void setInsertMode(int insertMode) {
        mInsertMode = insertMode;
        if (tvCalculatedTextLabel == null) {
            return;
        }

        if (mInsertMode == INSERT_MODE__PRICE) {
            tvCalculatedTextLabel.setText(mResource.getString(R.string.gen_amount_label));
//            etUserInput.setTag(mResource.getString(R.string.gen_price_label));
        }
        else {
            tvCalculatedTextLabel.setText(mResource.getString(R.string.gen_price_label));
//            etUserInput.setTag(mResource.getString(R.string.gen_amount_label));
        }
        calculatePriceAmount();
    }

    private void setBaseUOMQtyZoneVisibility(boolean visible) {
        if (llBaseUOMQtyZone == null) {
            return;
        }

        if (visible) {
            llBaseUOMQtyZone.setVisibility(View.VISIBLE);
        }
        else {
            llBaseUOMQtyZone.setVisibility(View.GONE);
        }
    }

    private void setConversionRateVisibility(boolean visible) {
        if (llConvertedAmountZone == null) {
            return;
        }

        if (visible) {
            llConvertedAmountZone.setVisibility(View.VISIBLE);
            if (llConversionRateZone != null) {
                llConversionRateZone.setVisibility(View.VISIBLE);
            }
            etConversionRate.setVisibility(View.VISIBLE);
//            etConversionRate.setTag(mResource.getString(R.string.gen_rate_label));
            tvConversionRateLabel.setVisibility(View.VISIBLE);

        }
        else {
            llConvertedAmountZone.setVisibility(View.GONE);
            if (llConversionRateZone != null) {
                llConversionRateZone.setVisibility(View.GONE);
            }
//            etConversionRate.setTag(null);
            etConversionRate.setVisibility(View.INVISIBLE);
            tvConversionRateLabel.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void calculatePriceAmount() {
        if (etQuantity == null) {
            return;
        }

        String qtyStr = etQuantity.getText().toString();
        String userInputStr = etUserInput.getText().toString();

        mAmountConverted = null;
        if (qtyStr.length() > 0 && userInputStr.length() > 0) {
            try {
                BigDecimal qtyBd = new BigDecimal(qtyStr);

                if (qtyBd.signum() == 0 && mInsertMode == INSERT_MODE__AMOUNT) {
                    rbInsertModePrice.setSelected(true);
                    mInsertMode = INSERT_MODE__PRICE;
                }

                if (mInsertMode == INSERT_MODE__PRICE) { //calculate amount
                    mPriceEntered = new BigDecimal(userInputStr);
                    mAmountEntered = qtyBd.multiply(mPriceEntered);
                    tvCalculatedTextContent.setText(Utils.numberToString(mAmountEntered, true,
                            ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " + mCurrencyCode);
                } else { //INSERT_MODE__AMOUNT - calculate price
                    mAmountEntered = new BigDecimal(userInputStr);
                    mPriceEntered = mAmountEntered.divide(qtyBd, 10, RoundingMode.HALF_UP);
                    tvCalculatedTextContent.setText(Utils.numberToString(mPriceEntered, true,
                            ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " " + mCurrencyCode);
                }
                if (mCarDefaultCurrencyId != mCurrencyId && mCurrencyConversionRate != null) {
                    mAmountConverted = mAmountEntered.multiply(mCurrencyConversionRate);
                    mPriceConverted = mPriceEntered.multiply(mCurrencyConversionRate);

                    if (mCarDefaultCurrencyCode == null) {
                        mCarDefaultCurrencyCode = "";
                    }

                    tvConvertedAmountLabel.setText(String.format(mResource.getString(R.string.gen_converted_price_label), mCarDefaultCurrencyCode) + " = "
                            + Utils.numberToString(mPriceConverted, true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + "; "
                            + String.format(mResource.getString(R.string.gen_converted_amount_label), mCarDefaultCurrencyCode) + " = "
                            + Utils.numberToString(mAmountConverted, true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT));
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void calculateBaseUOMQty() {
        if (mUOMFuelId == mDefaultUOMVolumeId || tvBaseUOMQtyValue == null) {
            return;
        }

        if (mUOMVolumeConversionRate == null) {
            tvBaseUOMQtyValue.setText(mResource.getString(R.string.fill_up_edit_no_conversion_rate_message));
            return;
        }
        tvBaseUOMQtyValue.setText("");
        String qtyStr = etQuantity.getText().toString();
        if (qtyStr.length() > 0) {
            try {
                mBaseUOMQty = (new BigDecimal(qtyStr)).multiply(mUOMVolumeConversionRate);

                tvBaseUOMQtyValue.setText(Utils.numberToString(mBaseUOMQty, true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME) + " " + mDefaultUOMVolumeCode);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public long getUOMId() {
        return mUOMFuelId;
    }
}
