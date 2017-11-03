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

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import org.andicar2.activity.R;

import java.util.Calendar;

import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 02.12.2016.
 */

public class DateRangeSearchDialogFragment extends DialogFragment {

    public static final String DATE_FROM_IN_MILLIS_KEY = "DATE_FROM";
    public static final String DATE_TO_IN_MILLIS_KEY = "DATE_TO";
    private static Calendar mDateFromCalendar = Calendar.getInstance();
    private static Calendar mDateToCalendar = Calendar.getInstance();
    private Bundle mSearchArgs;
    private TextView tvDateFromSearch;
    private TextView tvDateToSearch;

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
        mSearchArgs = getArguments();
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_search_date_range, container);

        getDialog().setTitle(R.string.search_dialog_title);

        setCancelable(false);

        View fakeFocus = view.findViewById(R.id.fakeFocus);
        if (fakeFocus != null) {
            fakeFocus.requestFocus();
        }

        tvDateFromSearch = view.findViewById(R.id.tvDateFromSearch);
        if (savedInstanceState != null && savedInstanceState.containsKey(DATE_FROM_IN_MILLIS_KEY)) {
            mDateFromCalendar.setTimeInMillis(savedInstanceState.getLong(DATE_FROM_IN_MILLIS_KEY));
            tvDateFromSearch.setText(Utils.getDateString(getContext(), mDateFromCalendar));
        }
        else if (mSearchArgs.containsKey(DATE_FROM_IN_MILLIS_KEY) && mSearchArgs.getLong(DATE_FROM_IN_MILLIS_KEY) > -1) {
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
                if (getContext() == null)
                    return;
                DatePickerDialog dp = new DatePickerDialog(getContext(), onDateChanged,
                        mDateFromCalendar.get(Calendar.YEAR), mDateFromCalendar.get(Calendar.MONTH), mDateFromCalendar.get(Calendar.DAY_OF_MONTH));
                dp.getDatePicker().setTag("dateFromDialog");
                dp.show();
            }
        });

        ImageButton btnClearDateFrom = view.findViewById(R.id.btnClearDateFrom);
        if (btnClearDateFrom != null) {
            btnClearDateFrom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tvDateFromSearch.setText("");
                }
            });
        }


        tvDateToSearch = view.findViewById(R.id.tvDateToSearch);
        if (savedInstanceState != null && savedInstanceState.containsKey(DATE_TO_IN_MILLIS_KEY)) {
            mDateToCalendar.setTimeInMillis(savedInstanceState.getLong(DATE_TO_IN_MILLIS_KEY));
            tvDateToSearch.setText(Utils.getDateString(getContext(), mDateToCalendar));
        }
        else if (mSearchArgs.containsKey(DATE_TO_IN_MILLIS_KEY) && mSearchArgs.getLong(DATE_TO_IN_MILLIS_KEY) > -1) {
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
                if (getContext() == null)
                    return;

                DatePickerDialog dp = new DatePickerDialog(getContext(), onDateChanged,
                        mDateToCalendar.get(Calendar.YEAR), mDateToCalendar.get(Calendar.MONTH), mDateToCalendar.get(Calendar.DAY_OF_MONTH));
                dp.getDatePicker().setTag("dateToDialog");
                dp.show();
            }
        });

        ImageButton btnClearDateTo = view.findViewById(R.id.btnClearDateTo);
        if (btnClearDateTo != null) {
            btnClearDateTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tvDateToSearch.setText("");
                }
            });
        }

        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Button btnDone = view.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateRangeSearchDialogFragmentListener listener = (DateRangeSearchDialogFragmentListener) getActivity();
                Bundle searchParams = new Bundle();

                if (tvDateFromSearch.getText().toString().length() > 0) {
                    searchParams.putLong(DATE_FROM_IN_MILLIS_KEY, mDateFromCalendar.getTimeInMillis());
                }

                if (tvDateToSearch.getText().toString().length() > 0) {
                    searchParams.putLong(DATE_TO_IN_MILLIS_KEY, mDateToCalendar.getTimeInMillis());
                }

                if (listener != null)
                    listener.onFinishSearchDialog(searchParams);
                dismiss();
            }
        });

        return view;
    }

    public interface DateRangeSearchDialogFragmentListener {
        void onFinishSearchDialog(Bundle searchParams);
    }
}
