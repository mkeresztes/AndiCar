package andicar.n.service;

import android.content.SharedPreferences;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.LogFileWriter;
import andicar.n.utils.Utils;
import andicar.n.utils.notification.AndiCarNotification;

/**
 * Created by Miklos Keresztes on 16.10.2017.
 */

public class BackupJob extends JobService {
    private static final SharedPreferences mPreferences = AndiCar.getDefaultSharedPreferences();
    public static String TAG = "BackupJob";
    private File debugLogFile = new File(ConstantValues.LOG_FOLDER + "BackupJob.log");
    private LogFileWriter debugLogFileWriter = null;

    @Override
    public boolean onStartJob(JobParameters jobParams) {
        try {
            debugLogFileWriter = new LogFileWriter(debugLogFile, false);
            debugLogFileWriter.appendnl("Starting BackupService");

            if (!FileUtils.isFileSystemAccessGranted(getApplicationContext())) {
                debugLogFileWriter.appendnl("No access to file system. Terminating job.");
                debugLogFileWriter.flush();
                debugLogFileWriter.close();
                jobFinished(jobParams, false);
                return false;
            }

            if (!mPreferences.getBoolean(getString(R.string.pref_key_backup_service_enabled), false)) {
                if (debugLogFileWriter != null) {
                    debugLogFileWriter.appendnl("Backup service is disabled");
                    debugLogFileWriter.flush();
                    debugLogFileWriter.close();
                }
                jobFinished(jobParams, false);
                return false;
            }
        }
        catch (Exception e) {
            try {
                if (debugLogFileWriter != null) {
                    debugLogFileWriter.appendnl("Exception(2) in BackupService: ").append(e.getMessage()).append("\n").append(Utils.getStackTrace(e));
                }
            }
            catch (Exception ignored) {
            }
            jobFinished(jobParams, false);
            return false;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DBAdapter db = new DBAdapter(getApplicationContext());
                    String dbPath = db.getDatabase().getPath();
                    db.close();
                    String bkFile = FileUtils.backupDb(getApplicationContext(), dbPath, "abk_", false, null);
                    if (bkFile == null) {
                        if (debugLogFileWriter != null) {
                            debugLogFileWriter.appendnl("Backup terminated with error: ").append(FileUtils.mLastErrorMessage)
                                    .append("\n").append(Utils.getStackTrace(FileUtils.mLastException));
                        }
                        AndiCarNotification.showGeneralNotification(getApplicationContext(), AndiCarNotification.NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR,
                                (int) System.currentTimeMillis(), FileUtils.mLastErrorMessage, null, null, null);
                    }
                    else {
                        if (debugLogFileWriter != null) {
                            debugLogFileWriter.appendnl("Backup terminated with success to: ").append(bkFile);
                        }
                        if (mPreferences.getBoolean(getString(R.string.pref_key_backup_service_show_notification), true)) {
                            AndiCarNotification.showGeneralNotification(getApplicationContext(), AndiCarNotification.NOTIFICATION_TYPE_INFO, ConstantValues.NOTIF_BACKUP_SERVICE_SUCCESS,
                                    getString(R.string.pref_backup_service_category), getString(R.string.backup_service_success_message), null, null);
                        }
                    }
                    deleteOldBackups();

                    Utils.setBackupNextRun(getApplicationContext(), true);
                    if (debugLogFileWriter != null) {
                        debugLogFileWriter.appendnl("Backup service terminated");
                        debugLogFileWriter.flush();
                        debugLogFileWriter.close();
                    }
                }
                catch (IOException e) {
                    try {
                        Utils.showNotReportableErrorDialog(getApplicationContext(), e.getMessage(), null, true);
                        if (debugLogFileWriter != null) {
                            debugLogFileWriter.appendnl("Backup service terminated");
                            debugLogFileWriter.flush();
                            debugLogFileWriter.close();
                        }
                    }
                    catch (Exception ignored) {
                    }
                }
            }
        }).start();

        jobFinished(jobParams, false);

        return true;
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

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
