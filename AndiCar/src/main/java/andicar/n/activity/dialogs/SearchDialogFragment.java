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

package andicar.n.activity.dialogs;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.andicar2.activity.R;

import java.util.Calendar;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 02.12.2016.
 */

public class SearchDialogFragment extends DialogFragment {
    public static final String SEARCH_TYPE_KEY = "searchType";
    public static final String SEARCH_ARGS_KEY = "searchArgs";
    public static final int SEARCH_TYPE_MILEAGE = 1;
    public static final int SEARCH_TYPE_REFUEL = 2;
    public static final int SEARCH_TYPE_EXPENSE = 3;
    public static final int SEARCH_TYPE_TODO = 4;
    public static final int SEARCH_TYPE_GPS_TRACK = 5;

    public static final String CAR_ID_KEY = "CAR_ID";
    public static final String DRIVER_ID_KEY = "DRIVER_ID";
    public static final String TYPE_ID_KEY = "TYPE_ID";
    public static final String CATEGORY_ID_KEY = "CATEGORY_ID";
    public static final String DATE_FROM_IN_MILLIS_KEY = "DATE_FROM";
    public static final String DATE_TO_IN_MILLIS_KEY = "DATE_TO";
    public static final String COMMENT_KEY = "COMMENT";
    public static final String TAG_KEY = "TAG";
    public static final String STATUS_KEY = "STATUS";
    private static Calendar mDateFromCalendar = Calendar.getInstance();
    private static Calendar mDateToCalendar = Calendar.getInstance();
    private int mSearchType;
    private long mCarId;
    private DBAdapter mDbAdapter;
    private Bundle mSearchArgs;
    private TextView tvDateFromSearch;
    private TextView tvDateToSearch;
    private AutoCompleteTextView acUserComment;
    private AutoCompleteTextView acTag;
    private Spinner spnDriver;
    private Spinner spnCar;
    private Spinner spnExpType;
    private Spinner spnExpCategory;
    private Spinner spnStatus;
    private LinearLayout lExpCatZone;

    private final DatePickerDialog.OnDateSetListener onDateChanged = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
            if (datePicker.getTag() != null && datePicker.getTag().equals("dateFromDialog")) {
                mDateFromCalendar.set(year, month, dayOfMonth, 0, 0, 0);
                tvDateFromSearch.setText(Utils.getDateString(getContext(), mDateFromCalendar));
            }
            else if (datePicker.getTag() != null && datePicker.getTag().equals("dateToDialog")) {
                mDateToCalendar.set(year, month, dayOfMonth, 23, 59, 59);
                tvDateToSearch.setText(Utils.getDateString(getContext(), mDateToCalendar));
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchType = getArguments().getInt(SEARCH_TYPE_KEY);
        mSearchArgs = getArguments().getBundle(SEARCH_ARGS_KEY);

        mDbAdapter = new DBAdapter(getContext());

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tvDateToSearch.getText().length() > 0) {
            outState.putLong(DATE_TO_IN_MILLIS_KEY, mDateToCalendar.getTimeInMillis());
        }
        if (tvDateFromSearch.getText().length() > 0) {
            outState.putLong(DATE_FROM_IN_MILLIS_KEY, mDateFromCalendar.getTimeInMillis());
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_search, container);

        getDialog().setTitle(R.string.search_dialog_title);

        setCancelable(false);

        View fakeFocus = view.findViewById(R.id.fakeFocus);
        if (fakeFocus != null) {
            fakeFocus.requestFocus();
        }

        TextView tvExpTypeLabel = view.findViewById(R.id.tvExpTypeLabel);
        tvDateFromSearch = view.findViewById(R.id.tvDateFromSearch);
        tvDateToSearch = view.findViewById(R.id.tvDateToSearch);
        TextView tvStatusLabel = view.findViewById(R.id.tvStatusLabel);
        TextView tvExpCategoryLabel = view.findViewById(R.id.tvExpCategoryLabel);

        ImageButton btnClearDateFrom = view.findViewById(R.id.btnClearDateFrom);
        ImageButton btnClearDateTo = view.findViewById(R.id.btnClearDateTo);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnDone = view.findViewById(R.id.btnDone);

        LinearLayout lDriverZone = view.findViewById(R.id.lDriverZone);
        LinearLayout lStatusZone = view.findViewById(R.id.lStatusZone);
        LinearLayout lTagZone = view.findViewById(R.id.lTagZone);
        lExpCatZone = view.findViewById(R.id.lExpCatZone);

        spnCar = view.findViewById(R.id.spnCar);
        spnDriver = view.findViewById(R.id.spnDriver);
        spnExpType = view.findViewById(R.id.spnExpType);
        spnExpCategory = view.findViewById(R.id.spnExpCategory);
        spnStatus = view.findViewById(R.id.spnStatus);

        acUserComment = view.findViewById(R.id.acUserComment);
        acTag = view.findViewById(R.id.acTag);

        mCarId = mSearchArgs.getLong(CAR_ID_KEY, -1);

        if (mSearchType == SEARCH_TYPE_TODO) {
            Utils.initSpinner(mDbAdapter, spnCar, DBAdapter.TABLE_NAME_CAR, DBAdapter.WHERE_CONDITION_ISACTIVE +
                    " OR (" + DBAdapter.COL_NAME_GEN_ROWID + " IN (SELECT " + DBAdapter.COL_NAME_TODO__CAR_ID +
                    " FROM " + DBAdapter.TABLE_NAME_TODO +
                    " WHERE " + DBAdapter.COL_NAME_TODO__ISDONE + " = 'N'))", mCarId, true);

            Utils.initSpinner(mDbAdapter, spnExpType, DBAdapter.TABLE_NAME_TASKTYPE, null, mSearchArgs.getLong(TYPE_ID_KEY, -1), true);
            lExpCatZone.setVisibility(View.GONE);
            lStatusZone.setVisibility(View.VISIBLE);
            lDriverZone.setVisibility(View.GONE);
            lTagZone.setVisibility(View.GONE);
        }
        else {
            Utils.initSpinner(mDbAdapter, spnCar, DBAdapter.TABLE_NAME_CAR, null, mCarId, true);
            Utils.initSpinner(mDbAdapter, spnExpType, DBAdapter.TABLE_NAME_EXPENSETYPE, null, mSearchArgs.getLong(TYPE_ID_KEY, -1), true);
            if (mSearchType == SEARCH_TYPE_MILEAGE || mSearchType == SEARCH_TYPE_GPS_TRACK) {
                lExpCatZone.setVisibility(View.GONE);
            }
            else {
                lExpCatZone.setVisibility(View.VISIBLE);
                if (mSearchType == SEARCH_TYPE_REFUEL) {
                    tvExpCategoryLabel.setText(R.string.fillup_category);
                    Utils.initSpinner(mDbAdapter, spnExpCategory, DBAdapter.TABLE_NAME_EXPENSECATEGORY,
                            " AND " + DBAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'Y'", mSearchArgs.getLong(CATEGORY_ID_KEY, -1), true);
                }
                else {
                    Utils.initSpinner(mDbAdapter, spnExpCategory, DBAdapter.TABLE_NAME_EXPENSECATEGORY,
                            " AND " + DBAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'N'", mSearchArgs.getLong(CATEGORY_ID_KEY, -1), true);
                }
            }
            lStatusZone.setVisibility(View.GONE);
            lDriverZone.setVisibility(View.VISIBLE);
            lTagZone.setVisibility(View.VISIBLE);
            setTagAdapters();
        }

        Utils.initSpinner(mDbAdapter, spnDriver, DBAdapter.TABLE_NAME_DRIVER, null, mSearchArgs.getLong(DRIVER_ID_KEY, -1), true);


        spnCar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getTag() != null && adapterView.getTag().equals(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG)) {
                    adapterView.setTag(null);
                    return;
                }
                String spnText = spnCar.getSelectedItem().toString().trim();
                if (spnText.length() > 0) {
                    mCarId = mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_CAR, spnText);
                    setTagAdapters();
                }
                else {
                    mCarId = -1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mCarId = -1;
            }
        });

        if (mSearchType == SEARCH_TYPE_MILEAGE) {
            tvExpTypeLabel.setText(R.string.mileage_type);
        }
        else if (mSearchType == SEARCH_TYPE_REFUEL) {
            tvExpTypeLabel.setText(R.string.fillup_type);
        }

        acUserComment.setText(mSearchArgs.getString(COMMENT_KEY, "%"));

        acTag.setText(mSearchArgs.getString(TAG_KEY, "%"));

        if (savedInstanceState != null && savedInstanceState.containsKey(DATE_FROM_IN_MILLIS_KEY)) {
            mDateFromCalendar.setTimeInMillis(savedInstanceState.getLong(DATE_FROM_IN_MILLIS_KEY));
            tvDateFromSearch.setText(Utils.getDateString(getContext(), mDateFromCalendar));
        }
        else if (mSearchArgs.containsKey(DATE_FROM_IN_MILLIS_KEY)) {
            mDateFromCalendar.setTimeInMillis(mSearchArgs.getLong(DATE_FROM_IN_MILLIS_KEY));
            tvDateFromSearch.setText(Utils.getDateString(getContext(), mDateFromCalendar));
        }
        else {
            mDateFromCalendar = Calendar.getInstance();
            tvDateFromSearch.setText("");
        }

        tvDateFromSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @SuppressLint("WrongConstant") DatePickerDialog dp = new DatePickerDialog(getContext(), onDateChanged,
                        mDateFromCalendar.get(Calendar.YEAR), mDateFromCalendar.get(Calendar.MONTH), mDateFromCalendar.get(Calendar.DAY_OF_MONTH));
                dp.getDatePicker().setTag("dateFromDialog");
                dp.show();
            }
        });

        if (btnClearDateFrom != null) {
            btnClearDateFrom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tvDateFromSearch.setText("");
                }
            });
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(DATE_TO_IN_MILLIS_KEY)) {
            mDateToCalendar.setTimeInMillis(savedInstanceState.getLong(DATE_TO_IN_MILLIS_KEY));
            tvDateToSearch.setText(Utils.getDateString(getContext(), mDateToCalendar));
        }
        else if (mSearchArgs.containsKey(DATE_TO_IN_MILLIS_KEY)) {
            mDateToCalendar.setTimeInMillis(mSearchArgs.getLong(DATE_TO_IN_MILLIS_KEY));
            tvDateToSearch.setText(Utils.getDateString(getContext(), mDateToCalendar));
        }
        else {
            mDateToCalendar = Calendar.getInstance();
            tvDateToSearch.setText("");
        }

        tvDateToSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @SuppressLint("WrongConstant") DatePickerDialog dp = new DatePickerDialog(getContext(), onDateChanged,
                        mDateToCalendar.get(Calendar.YEAR), mDateToCalendar.get(Calendar.MONTH), mDateToCalendar.get(Calendar.DAY_OF_MONTH));
                dp.getDatePicker().setTag("dateToDialog");
                dp.show();
            }
        });

        if (btnClearDateTo != null) {
            btnClearDateTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tvDateToSearch.setText("");
                }
            });
        }

        tvStatusLabel.setText(getString(R.string.todo_is_done) + ":");

        spnStatus.setSelection(mSearchArgs.getInt(STATUS_KEY));

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchDialogListener listener = (SearchDialogListener) getActivity();
                Bundle searchParams = new Bundle();
                String spnText;

                if (mCarId > -1) {
                    searchParams.putLong(CAR_ID_KEY, mCarId);
                }

                spnText = spnDriver.getSelectedItem().toString().trim();
                if (spnText.length() > 0) {
                    searchParams.putLong(DRIVER_ID_KEY, mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_DRIVER, spnText));
                }

                spnText = spnExpType.getSelectedItem().toString().trim();
                if (spnText.length() > 0) {
                    if (mSearchType == SEARCH_TYPE_TODO) {
                        searchParams.putLong(TYPE_ID_KEY, mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_TASKTYPE, spnText));
                    }
                    else {
                        searchParams.putLong(TYPE_ID_KEY, mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_EXPENSETYPE, spnText));
                    }
                }

                if (lExpCatZone.getVisibility() == View.VISIBLE) {
                    spnText = spnExpCategory.getSelectedItem().toString().trim();
                    if (spnText.length() > 0) {
                        searchParams.putLong(CATEGORY_ID_KEY, mDbAdapter.getIdByName(DBAdapter.TABLE_NAME_EXPENSECATEGORY, spnText));
                    }
                }

                if (tvDateFromSearch.getText().toString().length() > 0) {
                    searchParams.putLong(DATE_FROM_IN_MILLIS_KEY, mDateFromCalendar.getTimeInMillis());
                }

                if (tvDateToSearch.getText().toString().length() > 0) {
                    searchParams.putLong(DATE_TO_IN_MILLIS_KEY, mDateToCalendar.getTimeInMillis());
                }

                if (acUserComment.getText().toString().length() > 0) {
                    searchParams.putString(COMMENT_KEY, acUserComment.getText().toString());
                }

                if (acTag.getText().toString().length() > 0) {
                    searchParams.putString(TAG_KEY, acTag.getText().toString());
                }

                if (spnStatus.getSelectedItemPosition() > 0) {
                    searchParams.putInt(STATUS_KEY, spnStatus.getSelectedItemPosition());
                }

                listener.onFinishSearchDialog(searchParams);
                dismiss();
            }
        });

        return view;
    }

    private void setTagAdapters() {
        ArrayAdapter<String> mTagAdapter = null;
        ArrayAdapter<String> mCommentAdapter = null;
        String[] records;
        String tableName;

        //set adapter for records
        records = mDbAdapter.getAutoCompleteText(DBAdapter.TABLE_NAME_TAG, null, null, 0, 0);
        if (records != null) {
            mTagAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, records);
        }
        acTag.setAdapter(mTagAdapter);

        //set adapter for user comment
        if (mSearchType == SEARCH_TYPE_MILEAGE) {
            tableName = DBAdapter.TABLE_NAME_MILEAGE;
        }
        else if (mSearchType == SEARCH_TYPE_REFUEL) {
            tableName = DBAdapter.TABLE_NAME_REFUEL;
        } else if (mSearchType == SEARCH_TYPE_EXPENSE) {
            tableName = DBAdapter.TABLE_NAME_EXPENSE;
        } else if (mSearchType == SEARCH_TYPE_GPS_TRACK) {
            tableName = DBAdapter.TABLE_NAME_GPSTRACK;
        }
        else {
            tableName = "";
        }
        records = mDbAdapter.getAutoCompleteText(tableName, null, null, mCarId, 20);
        if (records != null) {
            mCommentAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, records);
        }
        acUserComment.setAdapter(mCommentAdapter);

    }


    public interface SearchDialogListener {
        void onFinishSearchDialog(Bundle searchParams);
    }
}
