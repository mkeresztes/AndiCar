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

package andicar.n.broadcastreceiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.io.File;

import andicar.n.activity.dialogs.GPSTrackControllerDialogActivity;
import andicar.n.activity.fragment.BaseEditFragment;
import andicar.n.activity.fragment.GPSTrackControllerFragment;
import andicar.n.persistence.DBAdapter;
import andicar.n.service.GPSTrackService;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.LogFileWriter;
import andicar.n.utils.notification.AndiCarNotification;

public class BTConnectionListener extends BroadcastReceiver {
    //    GPSTrackService.GPSTrackServiceBinder binder;
    private static final String LOG_TAG = "AndiCar";
    private GPSTrackService mGPSTrackService;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preference = AndiCar.getDefaultSharedPreferences();
        LogFileWriter debugLogFileWriter;

        Log.d(LOG_TAG, "onReceive: BTConnectionReceiver started");

        try {
            try {
                if (FileUtils.isFileSystemAccessGranted(context)) {
                    FileUtils.createFolderIfNotExists(context, ConstantValues.LOG_FOLDER);
                    File debugLogFile = new File(ConstantValues.LOG_FOLDER + "BTCBroadcast.log");
                    debugLogFileWriter = new LogFileWriter(debugLogFile, true);
                    debugLogFileWriter.appendnl("onReceive called for: ").append(intent.getAction());
                    debugLogFileWriter.flush();
                    debugLogFileWriter.close();
                }
            }
            catch (Exception ignored) {
            }

            DBAdapter mDb = new DBAdapter(context);

            IBinder binder = peekService(context, new Intent(context, GPSTrackService.class));
//            Toast.makeText(context, "Connected", 1000).show();
            if (binder != null) {
                mGPSTrackService = ((GPSTrackService.GPSTrackServiceBinder) binder).getService();
            }

//            context.bindService(new Intent(context, GPSTrackService.class), mServiceConnection, Context.BIND_WAIVE_PRIORITY);

            boolean isGpsTrackOn = (mGPSTrackService != null &&
                    (mGPSTrackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_RUNNING
                            || mGPSTrackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_PAUSED));

            if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                //check if GPS track active
                if (isGpsTrackOn) {
                    if (mGPSTrackService != null
                            && mGPSTrackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_PAUSED //gps tracking is paused
                            && preference.getString(context.getString(R.string.pref_key_bt_on_disconnect), "1").equals("1")) { //the preference is auto pause/resume
                        //resume tracking
                        mGPSTrackService.setServiceStatus(GPSTrackService.GPS_TRACK_SERVICE_RUNNING);
                    }
                }
                else {
                    //gps tracking not started => launch the GPSTrackingControl activity (if linked car exists)
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) {
                        String deviceMAC = device.getAddress();
                        //check if this device is linked with a car
                        if (deviceMAC == null || deviceMAC.length() == 0) {
                            return;
                        }
                        String[] selArgs = {deviceMAC};
                        Cursor c = mDb.query(
                                DBAdapter.TABLE_NAME_BTDEVICE_CAR,
                                DBAdapter.COL_LIST_BTDEVICECAR_TABLE,
                                "1 = 1 " + DBAdapter.WHERE_CONDITION_ISACTIVE + " AND "
                                        + DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_BTDEVICE_CAR, DBAdapter.COL_NAME_BTDEVICECAR__MACADDR)
                                        + " = ?", selArgs, DBAdapter.COL_NAME_GEN_ROWID + " DESC");
                        if (c == null) {
                            return;
                        }
                        if (c.moveToFirst()) { //linked car exist

                            Intent i = new Intent(context, GPSTrackControllerDialogActivity.class);
                            i.putExtra(BaseEditFragment.DETAIL_OPERATION_KEY, GPSTrackControllerFragment.GPS_TRACK_FROM_BT_CONNECTION);
                            Long carId = c.getLong(DBAdapter.COL_POS_BTDEVICECAR__CAR_ID);
                            i.putExtra(GPSTrackControllerFragment.GPS_TRACK_BT_CAR_ID_KEY, carId);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(i);
                        }
                        c.close();
                        mDb.close();
                    }
                }
            }
            else if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                if (isGpsTrackOn && mGPSTrackService != null) {
                    if (preference.getString(context.getString(R.string.pref_key_bt_on_disconnect), "1").equals("1") //the preference is auto pause/resume
                            && mGPSTrackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_RUNNING) {
                        mGPSTrackService.setServiceStatus(GPSTrackService.GPS_TRACK_SERVICE_PAUSED);
                    }
                    else if (preference.getString(context.getString(R.string.pref_key_bt_on_disconnect), "1").equals("2") //the preference is stop
                            && mGPSTrackService.getServiceStatus() != GPSTrackService.GPS_TRACK_SERVICE_STOPPED) {
                        mGPSTrackService.setServiceStatus(GPSTrackService.GPS_TRACK_SERVICE_STOPPED);
                    }
                }
            }
        }
        catch (Exception e) {
            AndiCarNotification.showGeneralNotification(context, AndiCarNotification.NOTIFICATION_TYPE_REPORTABLE_ERROR,
                    (int) System.currentTimeMillis(), e.getMessage(), null, null, e);
            Log.e(LOG_TAG, e.getMessage(), e);
        }

    }
}
