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
import andicar.n.utils.Utils;
import andicar.n.utils.notification.AndiCarNotification;

public class BTConnectionListener extends BroadcastReceiver {
    //    GPSTrackService.GPSTrackServiceBinder binder;
    private static final String LOG_TAG = "AndiCar";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preference = AndiCar.getDefaultSharedPreferences();
        LogFileWriter debugLogFileWriter = null;

        try {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            IBinder binder;
            GPSTrackService trackService = null;

            long carId = getCarIdForBTDevice(context, device);

            //no linked car
            try {
                    FileUtils.createFolderIfNotExists(context, ConstantValues.LOG_FOLDER);
                    File debugLogFile = new File(ConstantValues.LOG_FOLDER + "BTCBroadcast.log");
                    debugLogFileWriter = new LogFileWriter(debugLogFile, true);
                    debugLogFileWriter.appendnl("onReceive called for: ").append(intent.getAction());
            }
            catch (Exception ignored) {
            }

            if (carId == -1) {
                if (debugLogFileWriter != null) {
                    debugLogFileWriter.appendnl("No car linked to this device. Exiting.");
                    debugLogFileWriter.flush();
                    debugLogFileWriter.close();
                }
                return;
            }

            binder = peekService(context, new Intent(context, GPSTrackService.class));
            if (binder != null) {
                if (debugLogFileWriter != null) {
                    debugLogFileWriter.appendnl("Binder exists.");
                }
                trackService = ((GPSTrackService.GPSTrackServiceBinder) binder).getService();
            }
//            else {
//                if (debugLogFileWriter != null)
//                    debugLogFileWriter.appendnl("Binder is null. Exiting.");
//                return;
//            }

            if (trackService != null) {
                if (debugLogFileWriter != null) {
                    debugLogFileWriter.appendnl("trackService exists.");
                }
//                return;
            }

            boolean isGpsTrackOn = trackService != null && (trackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_RUNNING
                    || trackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_PAUSED);

            if (intent.getAction() != null) {
                if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                    //check if GPS track active
                    if (isGpsTrackOn) {
                        if (trackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_PAUSED //gps tracking is paused
                                && preference.getString(context.getString(R.string.pref_key_bt_on_disconnect), "1").equals("1")) { //the preference is auto pause/resume
                            //resume tracking
                            if (debugLogFileWriter != null)
                                debugLogFileWriter.appendnl("Tracking is paused. Resuming.");
                            trackService.setServiceStatus(GPSTrackService.GPS_TRACK_SERVICE_RUNNING);
                        }
                    } else {
                        //gps tracking not started => launch the GPSTrackingControl activity (if linked car exists)
                        if (debugLogFileWriter != null)
                            debugLogFileWriter.appendnl("Launching track controller activity");
                        Intent i = new Intent(context, GPSTrackControllerDialogActivity.class);
                        i.putExtra(BaseEditFragment.DETAIL_OPERATION_KEY, GPSTrackControllerFragment.GPS_TRACK_FROM_BT_CONNECTION);
                        i.putExtra(GPSTrackControllerFragment.GPS_TRACK_BT_CAR_ID_KEY, carId);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                    }
                }
                else if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                    if (isGpsTrackOn) {
                        if (preference.getString(context.getString(R.string.pref_key_bt_on_disconnect), "1").equals("1") //the preference is auto pause/resume
                                && trackService.getServiceStatus() == GPSTrackService.GPS_TRACK_SERVICE_RUNNING) {
                            if (debugLogFileWriter != null)
                                debugLogFileWriter.appendnl("Tracking is on. Pausing it.");
                            trackService.setServiceStatus(GPSTrackService.GPS_TRACK_SERVICE_PAUSED);
                        }
                        else if (preference.getString(context.getString(R.string.pref_key_bt_on_disconnect), "1").equals("2") //the preference is stop
                                && trackService.getServiceStatus() != GPSTrackService.GPS_TRACK_SERVICE_STOPPED) {
                            if (debugLogFileWriter != null)
                                debugLogFileWriter.appendnl("Tracking is on. Stopping it.");
                            trackService.setServiceStatus(GPSTrackService.GPS_TRACK_SERVICE_STOPPED);
                        }
                    }
                }
            }
            if (debugLogFileWriter != null) {
                try {
                    debugLogFileWriter.flush();
                    debugLogFileWriter.close();
                }
                catch (Exception ignored) {
                }
            }
        }
        catch (Exception e) {
            if (debugLogFileWriter != null) {
                try {
                    debugLogFileWriter.appendnl("Unexpected error: \n" + Utils.getStackTrace(e));
                    debugLogFileWriter.flush();
                    debugLogFileWriter.close();
                }
                catch (Exception ignored) {
                }
            }
            AndiCarNotification.showGeneralNotification(context, AndiCarNotification.NOTIFICATION_TYPE_REPORTABLE_ERROR,
                    (int) System.currentTimeMillis(), e.getMessage(), null, null, e);
            Log.e(LOG_TAG, e.getMessage(), e);
        }

    }

    /**
     * Check if the bluetooth device is linked with a car.
     *
     * @param ctx    the context
     * @param device the bluetooth device
     * @return -1 if no linked car, DEF_CAR._id if a linked car exists
     */
    private long getCarIdForBTDevice(Context ctx, BluetoothDevice device) {
        long retVal = -1L;
        if (device != null) {
            String deviceMAC = device.getAddress();
            //check if this device is linked with a car
            if (deviceMAC == null || deviceMAC.length() == 0) {
                return retVal;
            }
            String[] selArgs = {deviceMAC};
            DBAdapter mDb = new DBAdapter(ctx);
            Cursor c = mDb.query(
                    DBAdapter.TABLE_NAME_BTDEVICE_CAR,
                    DBAdapter.COL_LIST_BTDEVICECAR_TABLE,
                    "1 = 1 " + DBAdapter.WHERE_CONDITION_ISACTIVE + " AND "
                            + DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_BTDEVICE_CAR, DBAdapter.COL_NAME_BTDEVICECAR__MACADDR)
                            + " = ?", selArgs, DBAdapter.COL_NAME_GEN_ROWID + " DESC");
            if (c == null) {
                return retVal;
            }
            if (c.moveToFirst()) { //linked car exist
                retVal = c.getLong(DBAdapter.COL_POS_BTDEVICECAR__CAR_ID);
            }
            try {
                c.close();
                mDb.close();
            }
            catch (Exception ignored) {
            }
        }
        return retVal;
    }
}
