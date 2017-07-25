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

package andicar.n.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.notification.AndiCarNotification;

public class BackupService extends Service {

    private static final SharedPreferences mPreferences = AndiCar.getDefaultSharedPreferences();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mPreferences.getBoolean(getString(R.string.pref_key_backup_service_enabled), false)) {
            return START_NOT_STICKY;
        }

        String operation = intent.getExtras().getString(ConstantValues.BACKUP_SERVICE_OPERATION);
        if (operation != null && operation.equals(ConstantValues.BACKUP_SERVICE_OPERATION_SET_NEXT_RUN)) {
            setNextRun();
        }
        else {
            try {
                DBAdapter db = new DBAdapter(getApplicationContext());
                String dbPath = db.getDatabase().getPath();
                db.close();
                if (FileUtils.backupDb(this, dbPath, "abk_", false) == null) {
                    AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR,
                            (int) System.currentTimeMillis(), FileUtils.mLastErrorMessage, null, null, null);
                }
                else {
                    if (mPreferences.getBoolean(getString(R.string.pref_key_backup_service_show_notification), true)) {
                        AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_INFO, ConstantValues.NOTIF_BACKUP_SERVICE_SUCCESS,
                                getString(R.string.pref_backup_service_category), getString(R.string.backup_service_success_message), null, null);
                    }
                }
            }
            catch (Exception e) {
                AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_REPORTABLE_ERROR,
                        (int) System.currentTimeMillis(), e.getMessage(), null, null, e);
            }
            finally {
                setNextRun();
                try {
                    deleteOldBackups();
                }
                catch (Exception e) {
                    AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR,
                            (int) System.currentTimeMillis(), e.getMessage(), null, null, e);
                }
            }
        }
//		stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * set the next run date
     */
    @SuppressLint("WrongConstant")
    private void setNextRun() {
        String LogTag = "AndiCarBKService";
        Log.d(LogTag, "========== setNextRun begin ==========");
        Calendar nextSchedule = Calendar.getInstance();
        Calendar currentDate = Calendar.getInstance();
        Log.d(LogTag, "currentDate = " + currentDate.get(Calendar.YEAR) + "-" + currentDate.get(Calendar.MONTH) + "-" + currentDate.get(Calendar.DAY_OF_MONTH)
                + " " + currentDate.get(Calendar.HOUR_OF_DAY) + ":" + currentDate.get(Calendar.MINUTE));

        long timeInMillisecondsToNextRun;
        String scheduleDays;

        Intent intent = new Intent(this, BackupService.class);
        intent.putExtra(ConstantValues.BACKUP_SERVICE_OPERATION, ConstantValues.BACKUP_SERVICE_OPERATION_NORMAL);
        PendingIntent pIntent = PendingIntent.getService(this, ConstantValues.BACKUP_SERVICE_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (mPreferences.getBoolean(getString(R.string.pref_key_backup_service_enabled), false)) { //active schedule exists
            nextSchedule.set(Calendar.HOUR_OF_DAY, mPreferences.getInt(getString(R.string.pref_key_backup_service_exec_hour), 21));
            nextSchedule.set(Calendar.MINUTE, mPreferences.getInt(getString(R.string.pref_key_backup_service_exec_minute), 21));
            nextSchedule.set(Calendar.SECOND, 0);
            nextSchedule.set(Calendar.MILLISECOND, 0);
            //set date to current day
            nextSchedule.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
            Log.d(LogTag,
                    "nextSchedule = " + nextSchedule.get(Calendar.YEAR) + "-" + nextSchedule.get(Calendar.MONTH) + "-"
                            + nextSchedule.get(Calendar.DAY_OF_MONTH) + " " + nextSchedule.get(Calendar.HOUR_OF_DAY) + ":" + nextSchedule.get(Calendar.MINUTE));
            if (mPreferences.getString(getString(R.string.pref_key_backup_service_schedule_type), ConstantValues.BACKUP_SERVICE_DAILY)
                    .equals(ConstantValues.BACKUP_SERVICE_DAILY)) { //daily schedule
                if (nextSchedule.compareTo(currentDate) < 0) { //current hour > scheduled hour => next run tomorrow
                    nextSchedule.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
            else { //weekly schedule
                scheduleDays = mPreferences.getString(getString(R.string.pref_key_backup_service_backup_days), "1111111");
                Log.d(LogTag, "scheduleDays = " + scheduleDays);
                int daysToAdd = -1;
                Log.d(LogTag, "Calendar.DAY_OF_WEEK = " + currentDate.get(Calendar.DAY_OF_WEEK));
                for (int i = currentDate.get(Calendar.DAY_OF_WEEK) - 1; i < 7; i++) {
                    Log.d(LogTag, "i = " + i);
                    if (scheduleDays.substring(i, i + 1).equals("1")) {
                        Log.d(LogTag, scheduleDays.substring(i, i + 1));
                        if (i == (currentDate.get(Calendar.DAY_OF_WEEK) - 1) && nextSchedule.compareTo(currentDate) < 0) { //current hour > scheduled hour => get next run day
                            Log.d(LogTag,
                                    "currentDate = " + currentDate.get(Calendar.YEAR) + "-" + currentDate.get(Calendar.MONTH) + "-"
                                            + currentDate.get(Calendar.DAY_OF_MONTH) + " " + currentDate.get(Calendar.HOUR_OF_DAY) + ":"
                                            + currentDate.get(Calendar.MINUTE));
                            Log.d(LogTag,
                                    "nextSchedule = " + nextSchedule.get(Calendar.YEAR) + "-" + nextSchedule.get(Calendar.MONTH) + "-"
                                            + nextSchedule.get(Calendar.DAY_OF_MONTH) + " " + nextSchedule.get(Calendar.HOUR_OF_DAY) + ":"
                                            + nextSchedule.get(Calendar.MINUTE));
                        }
                        else {
                            daysToAdd = i - (currentDate.get(Calendar.DAY_OF_WEEK) - 1);
                            Log.d(LogTag, "daysToAdd = " + daysToAdd);
                            break;
                        }
                    }
                }
                if (daysToAdd == -1) { //no next run day in this week
                    for (int j = 0; j < currentDate.get(Calendar.DAY_OF_WEEK); j++) {
                        if (scheduleDays.substring(j, j + 1).equals("1")) {
                            daysToAdd = (7 - currentDate.get(Calendar.DAY_OF_WEEK)) + j + 1;
                            break;
                        }
                    }
                }
                Log.d(LogTag, "daysToAdd = " + daysToAdd);
                nextSchedule.add(Calendar.DAY_OF_MONTH, daysToAdd);
                Log.d(LogTag,
                        "nextSchedule = " + nextSchedule.get(Calendar.YEAR) + "-" + nextSchedule.get(Calendar.MONTH) + "-"
                                + nextSchedule.get(Calendar.DAY_OF_MONTH) + " " + nextSchedule.get(Calendar.HOUR_OF_DAY) + ":"
                                + nextSchedule.get(Calendar.MINUTE));
            }
            timeInMillisecondsToNextRun = nextSchedule.getTimeInMillis() - currentDate.getTimeInMillis();
            Log.d(LogTag, "nextSchedule.getTimeInMillis() = " + nextSchedule.getTimeInMillis());
            Log.d(LogTag, "currentDate.getTimeInMillis() = " + currentDate.getTimeInMillis());
            Log.d(LogTag, "timeInMillisecondsToNextRun = " + timeInMillisecondsToNextRun);
            //set next run of the service
            long triggerTime = System.currentTimeMillis() + timeInMillisecondsToNextRun;
            Log.d(LogTag, "triggerTime = " + triggerTime);
            am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, AlarmManager.INTERVAL_DAY, pIntent);
            Log.i(LogTag, "BackupService scheduled. Next start:" + DateFormat.getDateFormat(this).format(triggerTime) + " "
                    + DateFormat.getTimeFormat(this).format(triggerTime));
        }
        else { //no active schedule exists => remove scheduled runs
            am.cancel(pIntent);
            Log.i(LogTag, "BackupService not scheduled. No active schedule found.");
        }
        Log.d(LogTag, "========== setNextRun finished ==========");
    }

    private void deleteOldBackups() {
        int noOfBk;

        noOfBk = mPreferences.getInt(getString(R.string.pref_key_backup_service_keep_last_backups_no), 3);
        if (noOfBk <= 0) {
            return;
        }

        ArrayList<String> autoBkFileNames = FileUtils.getFileNames(this, ConstantValues.BACKUP_FOLDER, "abk_\\S+[.]db");
        if (autoBkFileNames != null && autoBkFileNames.size() > noOfBk) {
            Collections.sort(autoBkFileNames, String.CASE_INSENSITIVE_ORDER);
            Collections.reverse(autoBkFileNames);
            for (int i = noOfBk; i < autoBkFileNames.size(); i++) {
                FileUtils.deleteFile(ConstantValues.BACKUP_FOLDER + autoBkFileNames.get(i));
            }
        }

    }
}
