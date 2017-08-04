/*
 *  AndiCar - a car management software for Android powered devices.
 *
 *  Copyright (C) 2013 Miklos Keresztes (miklos.keresztes@gmail.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package andicar.n.broadcastreceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;

import andicar.n.service.BackupService;
import andicar.n.service.ToDoManagementService;
import andicar.n.service.ToDoNotificationService;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.Utils;

public class ServiceStarter extends BroadcastReceiver {

    private static final String LOG_TAG = "ServiceStarter";


    public static void startServices(Context context, String whatService) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent;
        PendingIntent pIntent;

        if (whatService.equals(ConstantValues.SERVICE_STARTER_START_ALL) || whatService.equals(ConstantValues.SERVICE_STARTER_START_TODO_MANAGEMENT_SERVICE)) {
            //start TO-DO notification service
            Log.i(LOG_TAG, "Starting To-Do Notification Service...");
            intent = new Intent(context, ToDoNotificationService.class);
            intent.putExtra(ToDoManagementService.SET_JUST_NEXT_RUN_KEY, false);
            context.startService(intent);
            Log.i(LOG_TAG, "Done");
        }

        if (whatService.equals(ConstantValues.SERVICE_STARTER_START_ALL) || whatService.equals(ConstantValues.SERVICE_STARTER_START_BACKUP_SERVICE)) {
            //start backup service
            Log.i(LOG_TAG, "Starting Backup Service...");
            intent = new Intent(context, BackupService.class);
            intent.putExtra(ConstantValues.BACKUP_SERVICE_OPERATION, ConstantValues.BACKUP_SERVICE_OPERATION_SET_NEXT_RUN);
            pIntent = PendingIntent.getService(context, ConstantValues.BACKUP_SERVICE_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pIntent);
            Log.i(LOG_TAG, "Done");
        }
    }

    @Override
    public void onReceive(Context context, Intent rIntent) {
        try {
            try {
                FileUtils.createFolderIfNotExists(context, ConstantValues.LOG_FOLDER);
                File debugLogFile = new File(ConstantValues.LOG_FOLDER + "SSBroadcast.log");
                FileWriter debugLogFileWriter = new FileWriter(debugLogFile, true);
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" onReceive called for: ").append(rIntent.getAction());
                debugLogFileWriter.flush();
                debugLogFileWriter.close();
            }
            catch (Exception ignored) {
            }

            if (rIntent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                    || rIntent.getAction().equals(Intent.ACTION_DATE_CHANGED)
                    || rIntent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
                //start services
                startServices(context, ConstantValues.SERVICE_STARTER_START_ALL);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
