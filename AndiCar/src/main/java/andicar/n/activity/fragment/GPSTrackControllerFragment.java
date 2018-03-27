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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import andicar.n.persistence.DBAdapter;
import andicar.n.service.GPSTrackService;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 15.03.2017.
 */

public class GPSTrackControllerFragment extends BaseEditFragment {

    public static final String GPS_TRACK_FROM_BT_CONNECTION = "BT";
    public static final String GPS_TRACK_BT_CAR_ID_KEY = "CarID";

    public static final String GPS_TRACK_ARGUMENT_CAR_ID = "GPSTrackCarID";
    public static final String GPS_TRACK_ARGUMENT_DRIVER_ID = "GPSTrackDriverID";
    public static final String GPS_TRACK_ARGUMENT_EXPENSE_TYPE_ID = "GPSTrackExpenseTypeID";
    public static final String GPS_TRACK_ARGUMENT_NAME = "GPSTrackName";
    public static final String GPS_TRACK_ARGUMENT_COMMENT = "GPSTrackComment";
    public static final String GPS_TRACK_ARGUMENT_TAG = "GPSTrackTag";
    public static final String GPS_TRACK_ARGUMENT_CREATE_MILEAGE = "GPSTrackIsCreateMileage";
    public static final String GPS_TRACK_ARGUMENT_INDEX_FOR_MILEAGE = "GPSTrackIndex";
    public static final String GPS_TRACK_ARGUMENT_START_TIME_FOR_MILEAGE = "GPSTrackStartTime";
    public static final String GPS_TRACK_ID_FOR_MILEAGE = "Track_ID";

    private boolean mIsCreateMileage = true;
    //    private EditText etIndex;
    private CheckBox ckIsCreateMileage;

    private LinearLayout llIndexStartZone;
    private EditText etIndexStart;

    private Button btnGPSTrackStartStop;
    private Button btnGPSTrackPauseResume;
//    private RelativeLayout llIndexStartZone;

    /**
     * Flag indicating whether we have called bind on the service.
     */
    private boolean isBound;

    private GPSTrackService mGPSTrackService;
    /**
     * Class for interacting with the main interface of the service.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.

            GPSTrackService.GPSTrackServiceBinder binder = (GPSTrackService.GPSTrackServiceBinder) service;
            mGPSTrackService = binder.getService();
            isBound = true;
            restoreState();
            setSpecificLayout();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mGPSTrackService = null;
            isBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this fragment can uses templates for filling data
        isUseTemplate = true;

        initDefaultValues();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        if (mOperationType != null && mOperationType.equals(GPS_TRACK_FROM_BT_CONNECTION)) {
            setCarId(mArgumentsBundle.getLong(GPS_TRACK_BT_CAR_ID_KEY));
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        mName = mResource.getString(R.string.gen_created_on) + " " +
                DateFormat.getDateFormat(getActivity()).format(System.currentTimeMillis()) + " " +
                DateFormat.getTimeFormat(getActivity()).format(System.currentTimeMillis());

        mIsCreateMileage = mPreferences.getBoolean(getString(R.string.pref_key_gps_track_create_mileage), true);

    }

    @Override
    public void setCarId(long carId) {
        super.setCarId(carId);
        if (viewsLoaded)
            etIndexStart.setText(
                    Utils.numberToString(mDbAdapter.getCarLastMileageIndex(mCarId), false, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (getActivity() != null) {
            getActivity().bindService(new Intent(getActivity(), GPSTrackService.class), mServiceConnection, Context.BIND_WAIVE_PRIORITY);
        }
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
//        etIndex = (EditText) mRootView.findViewById(R.id.etIndex);
        llIndexStartZone = mRootView.findViewById(R.id.llIndexStartZone);
        etIndexStart = mRootView.findViewById(R.id.etIndexStart);

        ckIsCreateMileage = mRootView.findViewById(R.id.ckIsCreateMileage);
        ckIsCreateMileage.setTag(R.string.pref_key_view_is_always_enabled, true);
        ckIsCreateMileage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    llIndexStartZone.setVisibility(View.VISIBLE);
                    etIndexStart.setText(Utils.numberToString(mDbAdapter.getCarCurrentIndex(mCarId), false, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH));
                    if (mGPSTrackService != null && isBound) {
                        mGPSTrackService.setIsCreateMileage(true, etIndexStart.getText().toString());
                    }
                }
                else {
                    llIndexStartZone.setVisibility(View.GONE);
                    if (mGPSTrackService != null && isBound) {
                        mGPSTrackService.setIsCreateMileage(false, null);
                    }
                }
            }
        });
        ckIsCreateMileage.setChecked(mIsCreateMileage);
        if (mIsCreateMileage) {
            llIndexStartZone.setVisibility(View.VISIBLE);
            etIndexStart.setText(Utils.numberToString(mDbAdapter.getCarCurrentIndex(mCarId), false, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH));
        }
        else {
            llIndexStartZone.setVisibility(View.GONE);
        }

        btnGPSTrackStartStop = mRootView.findViewById(R.id.btnGPSTrackStartStop);
        btnGPSTrackStartStop.setTag(R.string.pref_key_view_is_always_enabled, true);
        btnGPSTrackStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!viewsLoaded) {
                    return;
                }

                if (mGPSTrackService != null && (mGPSTrackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_RUNNING
                        || mGPSTrackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_PAUSED)) {
                    //if the service is active => stop it
                    SharedPreferences.Editor prefEditor = mPreferences.edit();
                    prefEditor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_gps_track_last_selected_expense_type_id), mExpTypeId);
                    prefEditor.apply();

                    mGPSTrackService.setServiceStatus(GPSTrackService.GPS_TRACK_SERVICE_STOPPED);

                    if (GPSTrackControllerFragment.this.getActivity() != null) {
                        GPSTrackControllerFragment.this.getActivity().finish();
                    }
                }
                else {
                    String strRetVal = GPSTrackControllerFragment.this.checkNumeric(vgRoot, false);
                    if (strRetVal != null) {
                        Toast toast = Toast.makeText(GPSTrackControllerFragment.this.getActivity(), mResource.getString(R.string.gen_invalid_number) + ": " + strRetVal,
                                Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }

                    if (etName.getText().toString().length() == 0) {
                        etName.setText(etName.getHint());
                    }

                    FileUtils.createFolderIfNotExists(GPSTrackControllerFragment.this.getActivity(), ConstantValues.TRACK_FOLDER);

                    Intent gpsTrackIntent = new Intent(GPSTrackControllerFragment.this.getContext(), GPSTrackService.class);
                    gpsTrackIntent.putExtra(GPS_TRACK_ARGUMENT_NAME, etName.getText().toString());
                    gpsTrackIntent.putExtra(GPS_TRACK_ARGUMENT_COMMENT, acUserComment.getText().toString());
                    gpsTrackIntent.putExtra(GPS_TRACK_ARGUMENT_TAG, acTag.getText().toString());
                    gpsTrackIntent.putExtra(GPS_TRACK_ARGUMENT_CAR_ID, mCarId);
                    gpsTrackIntent.putExtra(GPS_TRACK_ARGUMENT_DRIVER_ID, mDriverId);
                    gpsTrackIntent.putExtra(GPS_TRACK_ARGUMENT_EXPENSE_TYPE_ID, mExpTypeId);
                    gpsTrackIntent.putExtra(GPS_TRACK_ARGUMENT_START_TIME_FOR_MILEAGE, System.currentTimeMillis());
                    gpsTrackIntent.putExtra(GPS_TRACK_ARGUMENT_CREATE_MILEAGE, ckIsCreateMileage.isChecked());
                    gpsTrackIntent.putExtra(GPS_TRACK_ARGUMENT_INDEX_FOR_MILEAGE, etIndexStart.getText().toString());
                    if (GPSTrackControllerFragment.this.getContext() != null) {
                        GPSTrackControllerFragment.this.getContext().startService(gpsTrackIntent);
                    }

                    Bundle analyticsParams = new Bundle();
                    analyticsParams.putInt(ConstantValues.ANALYTICS_IS_TEMPLATE_USED, (isTemplateUsed ? 1 : 0));
                    analyticsParams.putInt(ConstantValues.ANALYTICS_IS_CREATE_MILEAGE, (ckIsCreateMileage.isChecked() ? 1 : 0));
                    analyticsParams.putInt(ConstantValues.ANALYTICS_IS_FROM_BT_CONNECTION, (mOperationType != null && mOperationType.equals(GPS_TRACK_FROM_BT_CONNECTION) ? 1 : 0));
                    Utils.sendAnalyticsEvent(getActivity(), "GPSTrack", analyticsParams, false);

                    if (GPSTrackControllerFragment.this.getActivity() != null) {
                        GPSTrackControllerFragment.this.getActivity().finish();
                    }
                }
            }
        });
        btnGPSTrackPauseResume = mRootView.findViewById(R.id.btnGPSTrackPauseResume);
        btnGPSTrackPauseResume.setTag(R.string.pref_key_view_is_always_enabled, true);
        btnGPSTrackPauseResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isBound || mGPSTrackService == null || !viewsLoaded) {
                    return;
                }
                if (mGPSTrackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_PAUSED) {
                    mGPSTrackService.setServiceStatus(GPSTrackService.GPS_TRACK_SERVICE_RUNNING);
                    btnGPSTrackPauseResume.setText(R.string.gps_track_pause); //setImageDrawable(Utils.getDrawable(mResource, R.drawable.ic_button_gps_pause_black_24dp_pad4dp));
                }
                else {
                    mGPSTrackService.setServiceStatus(GPSTrackService.GPS_TRACK_SERVICE_PAUSED);
                    btnGPSTrackPauseResume.setText(R.string.gps_track_resume); // setImageDrawable(Utils.getDrawable(mResource, R.drawable.ic_button_gps_play_black_24dp_pad4dp));
                }
                if (GPSTrackControllerFragment.this.getActivity() != null) {
                    GPSTrackControllerFragment.this.getActivity().finish();
                }
            }
        });

        Button btnCancel = mRootView.findViewById(R.id.btnCancel);
        btnCancel.setTag(R.string.pref_key_view_is_always_enabled, true);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GPSTrackControllerFragment.this.getActivity() != null) {
                    GPSTrackControllerFragment.this.getActivity().finish();
                }
            }
        });

        viewsLoaded = true;
        setSpecificLayout();
    }

    @Override
    protected void initSpecificControls() {
    }

    @Override
    protected void showValuesInUI() {
        if (mCarId <= 0) {
            Utils.showWarningDialog(getActivity(), getString(R.string.gps_track_no_car), null);
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
        etName.setHint(mName);
    }

    @Override
    public void setSpecificLayout() {
        if (mGPSTrackService != null && (mGPSTrackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_RUNNING ||
                mGPSTrackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_PAUSED)) { //tracking is running or paused

            if (getActivity() != null) {
                getActivity().setTitle(getString(R.string.gps_track_service_track_in_progress_message));
            }

            btnGPSTrackStartStop.setText(R.string.gps_track_stop); //setImageDrawable(Utils.getDrawable(mResource, R.drawable.ic_button_gps_stop_black_24dp_pad4dp));

            if (mGPSTrackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_PAUSED) {
                btnGPSTrackPauseResume.setText(R.string.gps_track_resume); // setImageDrawable(Utils.getDrawable(mResource, R.drawable.ic_button_gps_play_black_24dp_pad4dp));
            }
            else {
                btnGPSTrackPauseResume.setText(R.string.gps_track_pause); // setImageDrawable(Utils.getDrawable(mResource, R.drawable.ic_button_gps_pause_black_24dp_pad4dp));
            }

            btnGPSTrackPauseResume.setVisibility(View.VISIBLE);
            setViewsEditable(vgRoot, false);
        }
        else {
            if (getActivity() != null) {
                getActivity().setTitle(getString(R.string.gps_controller_start_track));
            }
            if (mCarId <= 0) {
                setCarId(mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_car_id), -1));
            }
            btnGPSTrackStartStop.setText(R.string.gps_track_start); // setImageDrawable(Utils.getDrawable(mResource, R.drawable.ic_button_gps_record_black_24dp_pad4dp));
            btnGPSTrackPauseResume.setVisibility(View.GONE);
            setViewsEditable(vgRoot, true);
        }
    }

    @Override
    protected boolean saveData() {
        return false;
    }

    @Override
    public void onStop() {
        try {
            super.onStop();
            if (getActivity() != null) {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getActivity().unbindService(mServiceConnection);
            }
        }
        catch (Exception ignored) {
        }
    }

    private void restoreState() {
        if (mGPSTrackService == null || mGPSTrackService.getArguments() == null || !viewsLoaded) {
            return;
        }

        if (mDbAdapter == null || mDbAdapter.getDb() == null) {
            mDbAdapter = new DBAdapter(getContext());
        }

        Bundle serviceArguments = mGPSTrackService.getArguments();
        setCarId(serviceArguments.getLong(GPS_TRACK_ARGUMENT_CAR_ID, mCarId));
        mCarId = Utils.initSpinner(mDbAdapter, spnCar, DBAdapter.TABLE_NAME_CAR, DBAdapter.WHERE_CONDITION_ISACTIVE, mCarId, false, false);
        setDriverId(serviceArguments.getLong(GPS_TRACK_ARGUMENT_DRIVER_ID, mDriverId));
        mDriverId = Utils.initSpinner(mDbAdapter, spnDriver, DBAdapter.TABLE_NAME_DRIVER, DBAdapter.WHERE_CONDITION_ISACTIVE, mDriverId, false, false);
        setExpTypeId(serviceArguments.getLong(GPS_TRACK_ARGUMENT_EXPENSE_TYPE_ID, mExpTypeId));
        mExpTypeId = Utils.initSpinner(mDbAdapter, spnExpType, DBAdapter.TABLE_NAME_EXPENSETYPE, DBAdapter.WHERE_CONDITION_ISACTIVE, mExpTypeId, false, false);
        etName.setText(serviceArguments.getString(GPS_TRACK_ARGUMENT_NAME, ""));
        acUserComment.setText(serviceArguments.getString(GPS_TRACK_ARGUMENT_COMMENT, ""));
        acTag.setText(serviceArguments.getString(GPS_TRACK_ARGUMENT_TAG, null));
        mIsCreateMileage = serviceArguments.getBoolean(GPS_TRACK_ARGUMENT_CREATE_MILEAGE);
        ckIsCreateMileage.setChecked(mIsCreateMileage);
        if (mIsCreateMileage) {
            etIndexStart.setText(serviceArguments.getString(GPS_TRACK_ARGUMENT_INDEX_FOR_MILEAGE));
            llIndexStartZone.setVisibility(View.VISIBLE);
        }
        else {
            llIndexStartZone.setVisibility(View.GONE);
        }
    }
}
