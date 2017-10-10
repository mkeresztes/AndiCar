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
package andicar.n.activity.miscellaneous;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * @author miki
 */
public class BackupScheduleActivity extends AppCompatActivity {
    private static final SharedPreferences mPreference = AndiCar.getDefaultSharedPreferences();
    private static int mHourOfDay = 22;
    private static int mMinute = 0;
    private static String mScheduleType;
    private final int[] ckDayOfWeekIDs = {R.id.ckDayOfWeek0, R.id.ckDayOfWeek1, R.id.ckDayOfWeek2, R.id.ckDayOfWeek3, R.id.ckDayOfWeek4, R.id.ckDayOfWeek5,
            R.id.ckDayOfWeek6};
    private final String LogTag = "BackupScheduleActivity";
    private Spinner spnScheduleFrequency = null;
    private LinearLayout llDayList = null;
    private EditText etKeepLastNo = null;
    private TextView tvDateTimeValue = null;
    private Drawable editTextStandardBackground = null;
    private String backupDays = "";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setFinishOnTouchOutside(false);

        setContentView(R.layout.activity_backup_schedule);

        llDayList = (LinearLayout) findViewById(R.id.llDayList);
        llDayList.setVisibility(View.GONE);

        spnScheduleFrequency = (Spinner) findViewById(R.id.spnScheduleFrequency);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.backup_schedule_type_entries, R.layout.ui_element_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnScheduleFrequency.setAdapter(adapter);
        spnScheduleFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
//				if (spnScheduleFrequency.getSelectedItemPosition() == 0) {
                if (spnScheduleFrequency.getSelectedItemPosition() == 0) { //daily
                    llDayList.setVisibility(View.GONE);
                    mScheduleType = ConstantValues.BACKUP_SERVICE_DAILY;
                }
                else {
                    llDayList.setVisibility(View.VISIBLE);
                    mScheduleType = ConstantValues.BACKUP_SERVICE_WEEKLY;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (mPreference.getString(getString(R.string.pref_key_backup_service_schedule_type), ConstantValues.BACKUP_SERVICE_DAILY).equals(ConstantValues.BACKUP_SERVICE_DAILY)) //daily frequency
        {
            spnScheduleFrequency.setSelection(0);
        }
        else {
            spnScheduleFrequency.setSelection(1);
        }


        etKeepLastNo = (EditText) findViewById(R.id.etKeepLastNo);
        etKeepLastNo.setText(String.valueOf(mPreference.getInt(getString(R.string.pref_key_backup_service_keep_last_backups_no), 3)));
        etKeepLastNo.setSelection(etKeepLastNo.getText().length());
        editTextStandardBackground = etKeepLastNo.getBackground();
        etKeepLastNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                etKeepLastNo.setBackground(editTextStandardBackground);
            }
        });

        tvDateTimeValue = (TextView) findViewById(R.id.tvDateTimeValue);
        tvDateTimeValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.show(BackupScheduleActivity.this.getSupportFragmentManager(), "timePicker");
            }
        });

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackupScheduleActivity.this.finish();
            }
        });

        backupDays = mPreference.getString(getString(R.string.pref_key_backup_service_backup_days), "1111111");
        for (int i = 0; i < 7; i++) {
            ((CheckBox) findViewById(ckDayOfWeekIDs[i])).setChecked(backupDays.charAt(i) == '1');
        }

        Button btnDone = (Button) findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int notFilledViewID = Utils.checkMandatoryFields((ViewGroup) BackupScheduleActivity.this.findViewById(R.id.vgRoot));
                if (notFilledViewID > -1) {
                    BackupScheduleActivity.this.findViewById(notFilledViewID).setBackgroundResource(R.drawable.ui_mandatory_border_edittext);
                    Toast.makeText(BackupScheduleActivity.this.getApplicationContext(), BackupScheduleActivity.this.getString(R.string.gen_fill_mandatory), Toast.LENGTH_LONG).show();
                    return;
                }
                backupDays = "";
                for (int i = 0; i < 7; i++) {
                    if (((CheckBox) BackupScheduleActivity.this.findViewById(ckDayOfWeekIDs[i])).isChecked()) {
                        backupDays = backupDays + "1";
                    }
                    else {
                        backupDays = backupDays + "0";
                    }
                }
                //schedule type is weekly but no day selected
                if (mScheduleType.equals(ConstantValues.BACKUP_SERVICE_WEEKLY) && !backupDays.contains("1")) {
                    Toast.makeText(BackupScheduleActivity.this.getApplicationContext(), BackupScheduleActivity.this.getString(R.string.pref_backup_service_choose_a_day), Toast.LENGTH_LONG).show();
                    return;
                }

                //all days are selected => change to daily backup
                if (mScheduleType.equals(ConstantValues.BACKUP_SERVICE_WEEKLY) && !backupDays.contains("0")) {
                    mScheduleType = ConstantValues.BACKUP_SERVICE_DAILY;
                }
                SharedPreferences.Editor editor = mPreference.edit();
                editor.putInt(BackupScheduleActivity.this.getString(R.string.pref_key_backup_service_exec_hour), mHourOfDay);
                editor.putInt(BackupScheduleActivity.this.getString(R.string.pref_key_backup_service_exec_minute), mMinute);
                editor.putString(BackupScheduleActivity.this.getString(R.string.pref_key_backup_service_schedule_type), mScheduleType);
                editor.putInt(BackupScheduleActivity.this.getString(R.string.pref_key_backup_service_keep_last_backups_no), Integer.parseInt(etKeepLastNo.getText().toString()));
                editor.putString(BackupScheduleActivity.this.getString(R.string.pref_key_backup_service_backup_days), backupDays);

                editor.apply();

//                //start the service to update the next run
//                try {
//                    ServiceStarter.startServicesUsingFBJobDispacher(BackupScheduleActivity.this.getApplicationContext(), ConstantValues.SERVICE_STARTER_START_BACKUP_SERVICE);
//                }
//                catch (Exception e) {
//                    AndiCarCrashReporter.sendCrash(e);
//                    Log.d(LogTag, e.getMessage(), e);
//                }

                BackupScheduleActivity.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHourOfDay = mPreference.getInt(getString(R.string.pref_key_backup_service_exec_hour), 22);
        mMinute = mPreference.getInt(getString(R.string.pref_key_backup_service_exec_minute), 0);
        tvDateTimeValue.setText(Utils.getTimeString(this, mHourOfDay, mMinute));
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker

            int hour = mPreference.getInt(getString(R.string.pref_key_backup_service_exec_hour), 22);
            int minute = mPreference.getInt(getString(R.string.pref_key_backup_service_exec_minute), 0);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHourOfDay = hourOfDay;
            mMinute = minute;
            ((TextView) getActivity().findViewById(R.id.tvDateTimeValue)).setText(Utils.getTimeString(getContext(), hourOfDay, minute));
        }
    }
}
