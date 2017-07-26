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

package andicar.n.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import andicar.n.activity.miscellaneous.BackupListActivity;
import andicar.n.activity.preference.PreferenceActivity;
import andicar.n.interfaces.OnAsyncTaskListener;
import andicar.n.utils.AndiCarCrashReporter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.SendGMailTask;
import andicar.n.utils.Utils;
import andicar.n.utils.notification.AndiCarNotification;

public class SecureBackupService extends Service implements OnAsyncTaskListener {
    private static final String LogTag = "AndiCar SecureBackup";
    private static final int RETRY_COUNT_LIMIT = 5;
    private final SharedPreferences mPreferences;
    private String zippedBk;
    private FileWriter debugLogFileWriter;
    private static int retryCount = 0;
    private final ArrayList<String> mFilesToSend = new ArrayList<>();

    public SecureBackupService() {
        mPreferences = AndiCar.getDefaultSharedPreferences();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String bkFileToSend;
        String bkFileName = "";

        try {
            FileUtils.createFolderIfNotExists(getApplicationContext(), ConstantValues.LOG_FOLDER);
            File debugLogFile = new File(ConstantValues.LOG_FOLDER + "SecureBackupService.log");
            debugLogFileWriter = new FileWriter(debugLogFile, false);
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" onStartCommand begin");
            debugLogFileWriter.flush();

            if (intent == null) {
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" intent is null. Terminating process.");
                debugLogFileWriter.flush();
                return START_NOT_STICKY;
            }
            //check if secure backup is enabled
            if (!mPreferences.getBoolean(getString(R.string.pref_key_secure_backup_enabled), false)) {
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" SecureBackup not activated. Terminating process.");
                debugLogFileWriter.flush();
                return START_NOT_STICKY;
            }

            //check if a google account was chosen
            if (mPreferences.getString(getString(R.string.pref_key_google_account), "").length() == 0) {
                AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR, (int) System.currentTimeMillis(),
                        getString(R.string.pref_category_secure_backup), getString(R.string.error_107), PreferenceActivity.class, null);
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" no Google account chosen. Terminating process.");
                debugLogFileWriter.flush();
                return START_NOT_STICKY;
            }
            //check if destination email (sendTo) exists
            if (mPreferences.getString(getString(R.string.pref_key_secure_backup_emailTo), "").length() == 0) {
                AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR, (int) System.currentTimeMillis(),
                        getString(R.string.pref_category_secure_backup), getString(R.string.error_106), PreferenceActivity.class, null);
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" no recipient email. Terminating process.");
                debugLogFileWriter.flush();
                return START_NOT_STICKY;
            }

            bkFileToSend = intent.getExtras().getString("bkFile");
            if (bkFileToSend != null) {
                File bkFile = new File(bkFileToSend);
                if (bkFile.exists()) {
                    bkFileName = bkFile.getName();
                }
            }
            if (bkFileToSend == null || bkFileName.length() == 0) {
                AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR, (int) System.currentTimeMillis(),
                        getString(R.string.pref_category_secure_backup), String.format(getString(R.string.error_100), " (" + bkFileToSend + ")"), BackupListActivity.class, null);
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Backup file not found. Terminating process.");
                debugLogFileWriter.flush();
                Log.e(LogTag, "Backup file not found (" + bkFileToSend + ")");
                return START_NOT_STICKY;
            }

            //check if network available
//            if (!Utils.isNetworkAvailable(this, mPreferences.getBoolean(getString(R.string.pref_key_secure_backup_only_wifi), true))) {
//                //save the backup file for later delivery (when the network will e available)
//                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" No network connection using only Wifi = ")
//                        .append(mPreferences.getBoolean(getString(R.string.pref_key_secure_backup_only_wifi), true) ? "yes" : "no");
//                debugLogFileWriter.flush();
//                SharedPreferences.Editor editor = mPreferences.edit();
//                editor.putString(getString(R.string.pref_key_postponed_secure_backupfile), bkFileToSend);
//                editor.apply();
//
//                if (mPreferences.getBoolean(getString(R.string.pref_key_secure_backup_show_notification), true)) {
//                    AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_INFO, ConstantValues.NOTIF_SECUREBK_POSTPONED_OR_SENT,
//                            getString(R.string.pref_category_secure_backup), getString(R.string.secure_backup_sending_postponed), null, null);
//                }
//                return START_NOT_STICKY;
//            }

            String errorMessage = FileUtils.createFolderIfNotExists(this, ConstantValues.TEMP_FOLDER);
            if (errorMessage != null) {
                AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR, (int) System.currentTimeMillis(),
                        getString(R.string.pref_category_secure_backup), errorMessage, null, null);

                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Error in creating backup folder: ").append(errorMessage);
                debugLogFileWriter.flush();
                return START_NOT_STICKY;
            }
            Bundle fileBundle = new Bundle();

            fileBundle.putString(bkFileName, bkFileToSend);

            if (mPreferences.getBoolean(getString(R.string.pref_key_secure_backup_send_tracks), false)) {
                ArrayList<String> gpsTrackFiles = FileUtils.getFileNames(this, ConstantValues.TRACK_FOLDER, null);
                if (gpsTrackFiles != null && gpsTrackFiles.size() > 0) {
                    debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Preparing gps tracks to send.");
                    debugLogFileWriter.flush();
                    String gpsTrackFile;
                    for (String trackFile : gpsTrackFiles) {
                        gpsTrackFile = trackFile;
                        fileBundle.putString(ConstantValues.TRACK_FOLDER_NAME + "/" + gpsTrackFile, ConstantValues.TRACK_FOLDER + gpsTrackFile);
                    }
                    debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" gps tracks zipped.");
                    debugLogFileWriter.flush();
                }
            }

            zippedBk = ConstantValues.TEMP_FOLDER + bkFileName.replace(".db", "") + ".zi_";
            FileUtils.zipFiles(fileBundle, zippedBk);
            mFilesToSend.add(zippedBk);

            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" calling SendGMailTask.");
            debugLogFileWriter.flush();
            new SendGMailTask(this, mPreferences.getString(getString(R.string.pref_key_google_account), null),
                    mPreferences.getString(getString(R.string.pref_key_secure_backup_emailTo), null),
                    getString(R.string.secure_backup_mail_subject), getString(R.string.secure_backup_mail_body), mFilesToSend, SecureBackupService.this).execute();
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" onStartCommandEnded");
            debugLogFileWriter.flush();
            return START_STICKY;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //called when the SendGMailTask finished with success
    @Override
    public void onTaskCompleted() {
        //remove the postponed backup file if exists
        try {
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" onTaskCompleted start");
            debugLogFileWriter.flush();

            if (mPreferences.getString(getString(R.string.pref_key_postponed_secure_backupfile), "").length() > 0) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString(getString(R.string.pref_key_postponed_secure_backupfile), null);
                editor.apply();
            }

            if (mPreferences.getBoolean(getString(R.string.pref_key_secure_backup_show_notification), true)) {
                AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_INFO, ConstantValues.NOTIF_SECUREBK_POSTPONED_OR_SENT,
                        getString(R.string.pref_category_secure_backup), getString(R.string.secure_backup_success_message), null, null);
            }

            //remove temporary zipped file(s)
            removeTemporaryFiles();

            //stop the service
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" onTaskCompleted ended");
            debugLogFileWriter.flush();
            stopSelf();
        }
        catch (IOException e) {
            AndiCarCrashReporter.sendCrash(e);
        }

    }

    //called when the SendGMailTask cancelled or finished with error
    @Override
    public void onCancelled(Exception e) {
        try {
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" onCancelled start");
            debugLogFileWriter.flush();
            if (e != null) {
                if (e instanceof UserRecoverableAuthIOException) {
                    //no Google authorization
                    mPreferences.edit().putBoolean(getString(R.string.pref_key_secure_backup_enabled), false).apply();
                    AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_WARNING, (int) System.currentTimeMillis(), getString(R.string.pref_category_secure_backup),
                            getString(R.string.error_108), null, null);
                    debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" AndiCar is not authorized.");
                    debugLogFileWriter.flush();
                }
                else {
                    //TODO move condition to instanceof
                    if (e.getMessage() != null && e.getMessage().equals("connect timed out")) {
                        if (retryCount < RETRY_COUNT_LIMIT) {
                            retryCount++;
                            //TODO remove this
                            if (retryCount == 1) {
                                debugLogFileWriter.append("\n").append("====Exception Stack Trace====");
                                debugLogFileWriter.append("\n").append(Utils.getStackTrace(e));
                                debugLogFileWriter.append("\n").append("=======End Stack Trace=======");
                            }
                            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(e.getMessage()).append(" (").append(Integer.toString(retryCount)).append(")");
                            debugLogFileWriter.append("\n").append("retrying...");
                            new SendGMailTask(this, mPreferences.getString(getString(R.string.pref_key_google_account), null),
                                    mPreferences.getString(getString(R.string.pref_key_secure_backup_emailTo), null),
                                    getString(R.string.secure_backup_mail_subject), getString(R.string.secure_backup_mail_body), mFilesToSend, SecureBackupService.this).execute();
                            debugLogFileWriter.flush();
                            return;
                        }
                        else {
                            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(e.getMessage()).append(" (").append(Integer.toString(retryCount)).append(")");
                            debugLogFileWriter.append("\n").append("Max retry count reached. Exiting.");
                        }
                    }
                    else {
                        AndiCarCrashReporter.sendCrash(e);
                        AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_REPORTABLE_ERROR, (int) System.currentTimeMillis(), getString(R.string.pref_category_secure_backup),
                                e.getLocalizedMessage(), null, e);
                        debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Error: ").append(e.getMessage() != null ? e.getMessage() : "");
                        debugLogFileWriter.append("\n").append("====Exception Stack Trace====");
                        debugLogFileWriter.append("\n").append(Utils.getStackTrace(e));
                        debugLogFileWriter.append("\n").append("=======End Stack Trace=======");
                        debugLogFileWriter.flush();
                    }
                }
//                Log.e(LogTag, e.getMessage() != null ? e.getMessage() : "", e);
            }

            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" Removing temporary files");
            debugLogFileWriter.flush();
            //remove temporary zipped file(s)
            removeTemporaryFiles();

            //stop the service
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" onCanceled ended");
            debugLogFileWriter.flush();
            stopSelf();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" onDestroy started.");
            debugLogFileWriter.flush();
            debugLogFileWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void removeTemporaryFiles() {
        File tmpFile;
        if (zippedBk != null) {
            tmpFile = new File(zippedBk);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        }
    }
}
