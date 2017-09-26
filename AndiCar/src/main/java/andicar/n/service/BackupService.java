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

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.Utils;
import andicar.n.utils.notification.AndiCarNotification;

public class BackupService extends Service {

    private static final SharedPreferences mPreferences = AndiCar.getDefaultSharedPreferences();
    private File debugLogFile = new File(ConstantValues.LOG_FOLDER + "BackupService.log");
    private FileWriter debugLogFileWriter = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            debugLogFileWriter = new FileWriter(debugLogFile, false);
            debugLogFileWriter.append(Utils.getCurrentDateTimeForLog()).append(" App version: ").append(Integer.toString(AndiCar.getAppVersion()));
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Starting BackupService");

            if (!mPreferences.getBoolean(getString(R.string.pref_key_backup_service_enabled), false)) {
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Backup service is disabled");
                debugLogFileWriter.flush();
                debugLogFileWriter.close();
                return START_NOT_STICKY;
            }

            String operation = intent.getExtras().getString(ConstantValues.BACKUP_SERVICE_OPERATION);
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Service operation: ").append(operation);
            if (operation != null && operation.equals(ConstantValues.BACKUP_SERVICE_OPERATION_SET_NEXT_RUN)) {
                setNextRun();
            }
            else {
                try {
                    DBAdapter db = new DBAdapter(getApplicationContext());
                    String dbPath = db.getDatabase().getPath();
                    db.close();
                    String bkFile = FileUtils.backupDb(this, dbPath, "abk_", false);
                    if (bkFile == null) {
                        debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Backup terminated with error: ").append(FileUtils.mLastErrorMessage)
                                .append("\n").append(Utils.getStackTrace(FileUtils.mLastException));
                        AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR,
                                (int) System.currentTimeMillis(), FileUtils.mLastErrorMessage, null, null, null);
                    }
                    else {
                        debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Backup terminated with success to: ").append(bkFile);
                        if (mPreferences.getBoolean(getString(R.string.pref_key_backup_service_show_notification), true)) {
                            AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_INFO, ConstantValues.NOTIF_BACKUP_SERVICE_SUCCESS,
                                    getString(R.string.pref_backup_service_category), getString(R.string.backup_service_success_message), null, null);
                        }
                    }
                }
                catch (Exception e) {
                    debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Exception(1) in BackupService: ").append(e.getMessage()).append("\n").append(Utils.getStackTrace(e));
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
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Backup service terminated");
            debugLogFileWriter.flush();
            debugLogFileWriter.close();
        }
        catch (Exception e) {
            try {
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Exception(2) in BackupService: ").append(e.getMessage()).append("\n").append(Utils.getStackTrace(e));
            }
            catch (Exception ignored) {
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
        try {
            String LogTag = "AndiCarBKService";
            Log.d(LogTag, "========== setNextRun begin ==========");
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" ========== setNextRun begin ==========");
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
                if (mPreferences.getString(getString(R.string.pref_key_backup_service_schedule_type), ConstantValues.BACKUP_SERVICE_DAILY).equals(ConstantValues.BACKUP_SERVICE_DAILY)) { //daily schedule
                    debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Backup schedule is daily");
                    if (nextSchedule.compareTo(currentDate) < 0) { //current hour > scheduled hour => next run tomorrow
                        nextSchedule.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }
                else { //weekly schedule
                    scheduleDays = mPreferences.getString(getString(R.string.pref_key_backup_service_backup_days), "1111111");
                    debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Backup schedule is weekly. Schedule days: ").append(scheduleDays);
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
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" BackupService scheduled. Next start: ").append(DateFormat.getDateFormat(this).format(triggerTime))
                        .append(" ").append(DateFormat.getTimeFormat(this).format(triggerTime));
            }
            else { //no active schedule exists => remove scheduled runs
                am.cancel(pIntent);
                Log.i(LogTag, "BackupService not scheduled. No active schedule found.");
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" BackupService not scheduled. No active schedule found.");
            }
            Log.d(LogTag, "========== setNextRun finished ==========");
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" ========== setNextRun finished ==========");
        }
        catch (Exception e) {
            try {
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Exception in setNextRun: ").append(e.getMessage()).append("\n").append(Utils.getStackTrace(e));
            }
            catch (Exception ignored) {
            }
        }
    }

    private void deleteOldBackups() {
        int noOfBk;

        noOfBk = mPreferences.getInt(getString(R.string.pref_key_backup_service_keep_last_backups_no), 3);
        if (noOfBk <= 0) {
            return;
        }

        //because the file name pattern was changed, additional logic need for sorting the files correctly
        ArrayList<String> oldAutoBkFileNames = FileUtils.getFileNames(this, ConstantValues.BACKUP_FOLDER, "abk_\\d{5,}\\S+[.]db");
        ArrayList<String> newAutoBkFileNames = FileUtils.getFileNames(this, ConstantValues.BACKUP_FOLDER, "abk_\\d{4}[-]\\S+[.]db");
        ArrayList<String> autoBkFileNames = new ArrayList<>();

        if (newAutoBkFileNames != null) {
            Collections.sort(newAutoBkFileNames, String.CASE_INSENSITIVE_ORDER);
            Collections.reverse(newAutoBkFileNames);
            autoBkFileNames.addAll(newAutoBkFileNames);
        }

        if (oldAutoBkFileNames != null) {
            Collections.sort(oldAutoBkFileNames, String.CASE_INSENSITIVE_ORDER);
            Collections.reverse(oldAutoBkFileNames);
            autoBkFileNames.addAll(oldAutoBkFileNames);
        }

        if (autoBkFileNames.size() > noOfBk) {
            for (int i = noOfBk; i < autoBkFileNames.size(); i++) {
                FileUtils.deleteFile(ConstantValues.BACKUP_FOLDER + autoBkFileNames.get(i));
            }
        }

    }
}
