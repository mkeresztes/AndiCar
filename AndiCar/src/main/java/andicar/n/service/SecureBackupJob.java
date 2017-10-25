package andicar.n.service;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import andicar.n.activity.miscellaneous.BackupListActivity;
import andicar.n.activity.preference.PreferenceActivity;
import andicar.n.interfaces.OnAsyncTaskListener;
import andicar.n.utils.AndiCarCrashReporter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.LogFileWriter;
import andicar.n.utils.Utils;
import andicar.n.utils.notification.AndiCarNotification;

/**
 * Created by Miklos Keresztes on 17.10.2017.
 */

public class SecureBackupJob extends JobService implements OnAsyncTaskListener {
    public static final String BK_FILE_KEY = "bkFile";
    private static final String LogTag = "AndiCar SecureBackupJob";
    private static final int RETRY_COUNT_LIMIT = 5;
    public static String TAG = "SecureBackupJob";
    private static int retryCount = 0;
    private final SharedPreferences mPreferences = AndiCar.getDefaultSharedPreferences();
    private final ArrayList<String> mFilesToSend = new ArrayList<>();
    private String zippedBk;
    private LogFileWriter debugLogFileWriter = null;

    @Override
    public boolean onStartJob(JobParameters jobParams) {
        final String bkFileToSend;
        final String bkFileName;

        try {
            if (FileUtils.isFileSystemAccessGranted(getApplicationContext())) {
                FileUtils.createFolderIfNotExists(getApplicationContext(), ConstantValues.LOG_FOLDER);
                File debugLogFile = new File(ConstantValues.LOG_FOLDER + "SecureBackupJob.log");
                debugLogFileWriter = new LogFileWriter(debugLogFile, false);
                debugLogFileWriter.appendnl("onStartCommand begin");
                debugLogFileWriter.flush();
            }

            if (jobParams.getExtras() == null) {
                if (debugLogFileWriter != null) {
                    debugLogFileWriter.appendnl("no params. Terminating process.");
                    debugLogFileWriter.flush();
                }
                jobFinished(jobParams, false);
                return false;
            }
            //check if secure backup is enabled
            if (!mPreferences.getBoolean(getString(R.string.pref_key_secure_backup_enabled), false)) {
                if (debugLogFileWriter != null) {
                    debugLogFileWriter.appendnl("SecureBackup not activated. Terminating process.");
                    debugLogFileWriter.flush();
                }
                jobFinished(jobParams, false);
                return false;
            }

            //check if a google account was chosen
            if (mPreferences.getString(getString(R.string.pref_key_google_account), "").length() == 0) {
                AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR, (int) System.currentTimeMillis(),
                        getString(R.string.pref_category_secure_backup), getString(R.string.error_107), PreferenceActivity.class, null);
                if (debugLogFileWriter != null) {
                    debugLogFileWriter.appendnl("no Google account chosen. Terminating process.");
                    debugLogFileWriter.flush();
                }
                jobFinished(jobParams, false);
                return false;
            }
            //check if destination email (sendTo) exists
            if (mPreferences.getString(getString(R.string.pref_key_secure_backup_emailTo), "").length() == 0) {
                AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR, (int) System.currentTimeMillis(),
                        getString(R.string.pref_category_secure_backup), getString(R.string.error_106), PreferenceActivity.class, null);
                if (debugLogFileWriter != null) {
                    debugLogFileWriter.appendnl("No recipient email. Terminating process.");
                    debugLogFileWriter.flush();
                }
                jobFinished(jobParams, false);
                return false;
            }

            bkFileToSend = jobParams.getExtras().getString(BK_FILE_KEY);
            if (bkFileToSend != null) {
                File bkFile = new File(bkFileToSend);
                if (bkFile.exists()) {
                    bkFileName = bkFile.getName();
                }
                else {
                    bkFileName = "";
                }
            }
            else {
                bkFileName = "";
            }

            if (bkFileToSend == null || bkFileName.length() == 0) {
                AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR, (int) System.currentTimeMillis(),
                        getString(R.string.pref_category_secure_backup), String.format(getString(R.string.error_100), " (" + bkFileToSend + ")"), BackupListActivity.class, null);
                if (debugLogFileWriter != null) {
                    debugLogFileWriter.appendnl("Backup file not found. Terminating process.");
                    debugLogFileWriter.flush();
                }
                Log.e(LogTag, "Backup file not found (" + bkFileToSend + ")");
                jobFinished(jobParams, false);
                return false;
            }

            String errorMessage = FileUtils.createFolderIfNotExists(this, ConstantValues.TEMP_FOLDER);
            if (errorMessage != null) {
                AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR, (int) System.currentTimeMillis(),
                        getString(R.string.pref_category_secure_backup), errorMessage, null, null);

                if (debugLogFileWriter != null) {
                    debugLogFileWriter.appendnl("Error in creating temporary folder: ").append(errorMessage);
                    debugLogFileWriter.flush();
                }
                jobFinished(jobParams, false);
                return false;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bundle fileBundle = new Bundle();
                    fileBundle.putString(bkFileName, bkFileToSend);

                    try {
                        if (mPreferences.getBoolean(getString(R.string.pref_key_secure_backup_send_tracks), false)) {
                            if (debugLogFileWriter != null) {
                                debugLogFileWriter.appendnl("Send gps track files option is: ON");
                            }
                            ArrayList<String> gpsTrackFiles = FileUtils.getFileNames(getApplicationContext(), ConstantValues.TRACK_FOLDER, null);
                            if (gpsTrackFiles != null && gpsTrackFiles.size() > 0) {
                                if (debugLogFileWriter != null) {
                                    debugLogFileWriter.appendnl("Preparing gps tracks to send.");
                                }
                                String gpsTrackFile;
                                for (String trackFile : gpsTrackFiles) {
                                    gpsTrackFile = trackFile;
                                    fileBundle.putString(ConstantValues.TRACK_FOLDER_NAME + "/" + gpsTrackFile, ConstantValues.TRACK_FOLDER + gpsTrackFile);
                                }
                            }
                            else {
                                if (debugLogFileWriter != null) {
                                    debugLogFileWriter.appendnl("No gps track files found.");
                                }
                            }
                        }
                        else {
                            if (debugLogFileWriter != null) {
                                debugLogFileWriter.appendnl("Send gps track files option is: OFF");
                            }
                        }
                        if (debugLogFileWriter != null) {
                            debugLogFileWriter.flush();
                        }

                        zippedBk = ConstantValues.TEMP_FOLDER + bkFileName.replace(".db", "") + ".zi_";
                        FileUtils.zipFiles(getApplicationContext(), fileBundle, zippedBk);
                        if (FileUtils.mLastException != null) {
                            Utils.showNotReportableErrorDialog(getApplicationContext(), FileUtils.mLastErrorMessage, null, true);
                            return;
                        }
                        mFilesToSend.add(zippedBk);

                        if (debugLogFileWriter != null) {
                            debugLogFileWriter.appendnl("calling SendGMailTask.");
                            debugLogFileWriter.flush();
                        }
                        new SendGMailTask(getApplicationContext(), mPreferences.getString(getString(R.string.pref_key_google_account), null),
                                mPreferences.getString(getString(R.string.pref_key_secure_backup_emailTo), null),
                                getString(R.string.secure_backup_mail_subject), getString(R.string.secure_backup_mail_body), mFilesToSend, SecureBackupJob.this).execute();
                        if (debugLogFileWriter != null) {
                            debugLogFileWriter.appendnl("onStartCommandEnded");
                            debugLogFileWriter.flush();
                        }
                    }
                    catch (Exception e) {
                        try {
                            if (debugLogFileWriter != null) {
                                debugLogFileWriter.appendnl(e.getMessage());
                                debugLogFileWriter.appendnl(Utils.getStackTrace(e));
                                debugLogFileWriter.flush();
                            }
                        }
                        catch (Exception ignored) {
                        }
                    }
                }
            }).start();
        }
        catch (Exception e) {
            try {
                if (debugLogFileWriter != null) {
                    debugLogFileWriter.append("\n").append("====Exception Catches on onStartCommand() method====");
                    debugLogFileWriter.append("\n").append("====Stack Trace====");
                    debugLogFileWriter.append("\n").append(Utils.getStackTrace(e));
                    debugLogFileWriter.append("\n").append("=======End Stack Trace=======");
                    debugLogFileWriter.flush();
                }
                Utils.showReportableErrorDialog(this, null, e.getMessage(), e, true);
            }
            catch (IOException ignored) {
            }
        }

        jobFinished(jobParams, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    @Override
    public void onTaskCompleted() {
        //remove the postponed backup file if exists
        try {
            if (debugLogFileWriter != null) {
                debugLogFileWriter.appendnl("onTaskCompleted start");
                debugLogFileWriter.flush();
            }

            if (mPreferences.getBoolean(getString(R.string.pref_key_secure_backup_show_notification), true)) {
                AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_INFO, ConstantValues.NOTIF_SECUREBK_POSTPONED_OR_SENT,
                        getString(R.string.pref_category_secure_backup), getString(R.string.secure_backup_success_message), null, null);
            }

            //remove temporary zipped file(s)
            removeTemporaryFiles();

            //stop the service
            if (debugLogFileWriter != null) {
                debugLogFileWriter.appendnl("onTaskCompleted ended");
                debugLogFileWriter.flush();
            }
        }
        catch (IOException e) {
            AndiCarCrashReporter.sendCrash(e);
        }
    }

    @Override
    public void onCancelled(Exception e) {
        try {
            if (debugLogFileWriter != null) {
                debugLogFileWriter.appendnl("onCancelled start");
                debugLogFileWriter.flush();
            }
            if (e != null) {
                if (e instanceof UserRecoverableAuthIOException) {
                    //no Google authorization
                    mPreferences.edit().putBoolean(getString(R.string.pref_key_secure_backup_enabled), false).apply();
                    AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_WARNING, (int) System.currentTimeMillis(), getString(R.string.pref_category_secure_backup),
                            getString(R.string.error_108), null, null);
                    if (debugLogFileWriter != null) {
                        debugLogFileWriter.appendnl("AndiCar is not authorized.");
                        debugLogFileWriter.flush();
                    }
                }
                else {
                    //TODO move condition to instanceof
                    if (e.getMessage() != null && e.getMessage().equals("connect timed out")) {
                        if (retryCount < RETRY_COUNT_LIMIT) {
                            retryCount++;
                            //TODO remove this
                            if (retryCount == 1 && debugLogFileWriter != null) {
                                debugLogFileWriter.append("\n").append("====Exception Stack Trace====");
                                debugLogFileWriter.append("\n").append(Utils.getStackTrace(e));
                                debugLogFileWriter.append("\n").append("=======End Stack Trace=======");
                            }
                            if (debugLogFileWriter != null) {
                                debugLogFileWriter.appendnl(e.getMessage()).append(" (").append(Integer.toString(retryCount)).append(")");
                                debugLogFileWriter.append("\n").append("retrying...");
                            }
                            new SendGMailTask(getApplicationContext(), mPreferences.getString(getString(R.string.pref_key_google_account), null),
                                    mPreferences.getString(getString(R.string.pref_key_secure_backup_emailTo), null),
                                    getString(R.string.secure_backup_mail_subject), getString(R.string.secure_backup_mail_body), mFilesToSend, SecureBackupJob.this).execute();
                            if (debugLogFileWriter != null) {
                                debugLogFileWriter.flush();
                            }
                            return;
                        }
                        else {
                            if (debugLogFileWriter != null) {
                                debugLogFileWriter.appendnl(e.getMessage()).append(" (").append(Integer.toString(retryCount)).append(")");
                                debugLogFileWriter.append("\n").append("Max retry count reached. Exiting.");
                            }
                        }
                    }
                    else {
                        AndiCarCrashReporter.sendCrash(e);
                        AndiCarNotification.showGeneralNotification(this, AndiCarNotification.NOTIFICATION_TYPE_REPORTABLE_ERROR, (int) System.currentTimeMillis(), getString(R.string.pref_category_secure_backup),
                                e.getLocalizedMessage(), null, e);
                        if (debugLogFileWriter != null) {
                            debugLogFileWriter.appendnl("Error: ").append(e.getMessage() != null ? e.getMessage() : "");
                            debugLogFileWriter.append("\n").append("====Exception Stack Trace====");
                            debugLogFileWriter.append("\n").append(Utils.getStackTrace(e));
                            debugLogFileWriter.append("\n").append("=======End Stack Trace=======");
                            debugLogFileWriter.flush();
                        }
                    }
                }
//                Log.e(LogTag, e.getMessage() != null ? e.getMessage() : "", e);
            }

            if (debugLogFileWriter != null) {
                debugLogFileWriter.appendnl("Removing temporary files");
                debugLogFileWriter.flush();
            }
            //remove temporary zipped file(s)
            removeTemporaryFiles();

            //stop the service
            if (debugLogFileWriter != null) {
                debugLogFileWriter.appendnl("onCanceled ended");
                debugLogFileWriter.flush();
            }
        }
        catch (Exception e1) {
            try {
                if (debugLogFileWriter != null) {
                    debugLogFileWriter.append("\n").append("====Exception Catches on onCancelled() method====");
                    debugLogFileWriter.append("\n").append("====Stack Trace====");
                    debugLogFileWriter.append("\n").append(Utils.getStackTrace(e1));
                    debugLogFileWriter.append("\n").append("=======End Stack Trace=======");
                    debugLogFileWriter.flush();
                }
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
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
