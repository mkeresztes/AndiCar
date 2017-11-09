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

package andicar.n.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;

import org.andicar2.activity.R;

import andicar.n.activity.fragment.BPartnerEditFragment;
import andicar.n.activity.fragment.BPartnerLocationEditFragment;
import andicar.n.activity.fragment.BTCarLinkFragment;
import andicar.n.activity.fragment.BaseEditFragment;
import andicar.n.activity.fragment.CarEditFragment;
import andicar.n.activity.fragment.CurrencyEditFragment;
import andicar.n.activity.fragment.CurrencyRateEditFragment;
import andicar.n.activity.fragment.DriverEditFragment;
import andicar.n.activity.fragment.ExpenseEditFragment;
import andicar.n.activity.fragment.ExpenseFuelCategoryEditFragment;
import andicar.n.activity.fragment.ExpenseTypeEditFragment;
import andicar.n.activity.fragment.GPSTrackControllerFragment;
import andicar.n.activity.fragment.GPSTrackEditFragment;
import andicar.n.activity.fragment.MileageEditFragment;
import andicar.n.activity.fragment.RefuelEditFragment;
import andicar.n.activity.fragment.ReimbursementRateEditFragment;
import andicar.n.activity.fragment.TagEditFragment;
import andicar.n.activity.fragment.TaskEditFragment;
import andicar.n.activity.fragment.TaskTypeEditFragment;
import andicar.n.activity.fragment.ToDoViewFragment;
import andicar.n.activity.fragment.UOMConversionEditFragment;
import andicar.n.activity.fragment.UOMEditFragment;
import andicar.n.utils.Utils;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link CommonListActivity}.
 */
public class CommonDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //prevent keyboard from automatic pop up
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //get the type of activity
        long recordID;
        int activityType;
        Bundle arguments = new Bundle();

        if (getIntent().getExtras() == null) {
            activityType = 0;
            recordID = -1;
        }
        else {
            activityType = getIntent().getExtras().getInt(CommonListActivity.ACTIVITY_TYPE_KEY);
            recordID = getIntent().getExtras().getLong(BaseEditFragment.RECORD_ID_KEY, -1L);
            if (getIntent().getExtras().containsKey(BaseEditFragment.IS_FUEL_KEY)) {
                arguments.putBoolean(BaseEditFragment.IS_FUEL_KEY, getIntent().getExtras().getBoolean(BaseEditFragment.IS_FUEL_KEY));
            }
            if (getIntent().getExtras().containsKey(BaseEditFragment.BPARTNER_ID_KEY)) {
                arguments.putLong(BaseEditFragment.BPARTNER_ID_KEY, getIntent().getExtras().getLong(BaseEditFragment.BPARTNER_ID_KEY));
            }
        }

        setContentView(R.layout.activity_item_detail);

        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        //The solution is to change dynamically the parent activity for up navigation
        if (activityType == CommonListActivity.ACTIVITY_TYPE_EXPENSE || activityType == CommonListActivity.ACTIVITY_TYPE_MILEAGE
                || activityType == CommonListActivity.ACTIVITY_TYPE_REFUEL || activityType == CommonListActivity.ACTIVITY_TYPE_GPS_TRACK) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            if (recordID > 0) {
                arguments.putString(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_EDIT);
            }
            else {
                arguments.putString(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_NEW);
            }
            arguments.putLong(BaseEditFragment.RECORD_ID_KEY, recordID);

            if (activityType == CommonListActivity.ACTIVITY_TYPE_MILEAGE) {
                setTitle(R.string.gen_trip_detail);
                MileageEditFragment fragment = new MileageEditFragment();
                if (getIntent().getExtras().getString(BaseEditFragment.DETAIL_OPERATION_KEY, "").equals(BaseEditFragment.DETAIL_OPERATION_TRACK_TO_MILEAGE)) {
                    arguments.putString(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_TRACK_TO_MILEAGE);
                    arguments.putLong(GPSTrackControllerFragment.GPS_TRACK_ID_FOR_MILEAGE, getIntent().getExtras().getLong(GPSTrackControllerFragment.GPS_TRACK_ID_FOR_MILEAGE));
                    arguments.putString(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_INDEX_FOR_MILEAGE,
                            getIntent().getExtras().getString(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_INDEX_FOR_MILEAGE));
                    arguments.putLong(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_START_TIME_FOR_MILEAGE,
                            getIntent().getExtras().getLong(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_START_TIME_FOR_MILEAGE));
                }
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_REFUEL) {
                setTitle(R.string.gen_fill_up_detail);
                RefuelEditFragment fragment = new RefuelEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_EXPENSE) {
                setTitle(R.string.gen_expense_detail);
                ExpenseEditFragment fragment = new ExpenseEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_GPS_TRACK) {
                if (recordID == -1) {
                    setTitle(R.string.gps_controller_start_track);
                    GPSTrackControllerFragment fragment = new GPSTrackControllerFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.item_detail_container, fragment)
                            .commit();
                }
                else {
                    setTitle(R.string.gen_gps_track_detail);
                    GPSTrackEditFragment fragment = new GPSTrackEditFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.item_detail_container, fragment)
                            .commit();
                }
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_TODO) {
                setTitle(R.string.gen_todo_detail);
//                TaskEditFragment fragment = new TaskEditFragment();
                ToDoViewFragment fragment = new ToDoViewFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_CAR) {
                setTitle(R.string.activity_car_edit);
                CarEditFragment fragment = new CarEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_DRIVER) {
                setTitle(R.string.activity_driver_edit);
                DriverEditFragment fragment = new DriverEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_UOM) {
                setTitle(R.string.activity_uom_edit);
                UOMEditFragment fragment = new UOMEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_UOM_CONVERSION) {
                setTitle(R.string.activity_uom_conversion_edit);
                UOMConversionEditFragment fragment = new UOMConversionEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_EXPENSE_CATEGORY || activityType == CommonListActivity.ACTIVITY_TYPE_FUEL_TYPE) {
                setTitle(activityType == CommonListActivity.ACTIVITY_TYPE_EXPENSE_CATEGORY ? R.string.activity_expense_category_edit : R.string.activity_fuel_category_edit);
                ExpenseFuelCategoryEditFragment fragment = new ExpenseFuelCategoryEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_EXPENSE_TYPE) {
                setTitle(R.string.activity_expense_type_edit);
                ExpenseTypeEditFragment fragment = new ExpenseTypeEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_REIMBURSEMENT_RATE) {
                setTitle(R.string.activity_reimbursement_rate_edit);
                ReimbursementRateEditFragment fragment = new ReimbursementRateEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_CURRENCY) {
                setTitle(R.string.activity__currency_edit);
                CurrencyEditFragment fragment = new CurrencyEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_CURRENCY_RATE) {
                setTitle(R.string.activity_currency_rate_edit);
                CurrencyRateEditFragment fragment = new CurrencyRateEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_BPARTNER) {
                setTitle(R.string.activity_bpartner_edit);
                BPartnerEditFragment fragment = new BPartnerEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_BPARTNER_LOCATION) {
                setTitle(R.string.activity_bpartner_location_edit);
                BPartnerLocationEditFragment fragment = new BPartnerLocationEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_TASK_TYPE) {
                setTitle(R.string.activity_task_type_edit);
                TaskTypeEditFragment fragment = new TaskTypeEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_TASK) {
                setTitle(R.string.gen_todo_detail);
                TaskEditFragment fragment = new TaskEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_BT_CAR_LINK) {
                setTitle(R.string.activity_bt_starter_device_link_edit);
                BTCarLinkFragment fragment = new BTCarLinkFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else if (activityType == CommonListActivity.ACTIVITY_TYPE_TAG) {
                setTitle(R.string.activity_tag_edit);
                TagEditFragment fragment = new TagEditFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, fragment)
                        .commit();
            }
            else {
                Utils.showNotReportableErrorDialog(this, getString(R.string.gen_error),
                        String.format(getString(R.string.error_113), ((Integer) activityType).toString()));
            }
        }
        else {
            setTitle(savedInstanceState.getCharSequence("Title", ""));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("Title", getTitle());
    }
}
