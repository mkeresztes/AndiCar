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

package andicar.n.service;

import android.content.Intent;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;

import java.io.File;
import java.io.IOException;

import andicar.n.utils.AndiCarCrashReporter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.LogFileWriter;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 28.03.2017.
 */

public class FBJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters job) {
        File debugLogFile;
        LogFileWriter debugLogFileWriter = null;

        try {
            FileUtils.createFolderIfNotExists(getApplicationContext(), ConstantValues.LOG_FOLDER);
            debugLogFile = new File(ConstantValues.LOG_FOLDER + "FBJobService.log");
            debugLogFileWriter = new LogFileWriter(debugLogFile, false);

            debugLogFileWriter.appendnl("onStartJob begin");
            Intent intent = new Intent(getApplicationContext(), SecureBackupService.class);
            if (job.getExtras() != null) {
                intent.putExtra("bkFile", job.getExtras().getString("bkFile"));
                intent.putExtra("attachName", job.getExtras().getString("attachName"));

                debugLogFileWriter.appendnl("Starting SecureBackupService for bkFile: ").append(job.getExtras().getString("bkFile"));

                getApplicationContext().startService(intent);
                debugLogFileWriter.appendnl("onStartJob terminated");
                debugLogFileWriter.flush();
                debugLogFileWriter.close();
                debugLogFileWriter = null;
            }
        }
        catch (Exception e) {
            if (!(e.getClass().equals(GoogleAuthIOException.class) || e.getClass().equals(GoogleAuthException.class))) {
                AndiCarCrashReporter.sendCrash(e);
            }

            if (debugLogFileWriter != null) {
                try {
                    debugLogFileWriter.appendnl("Error:").append(e.getMessage())
                            .append("\n\n").append(Utils.getStackTrace(e));
                    debugLogFileWriter.flush();
                    debugLogFileWriter.close();
                }
                catch (IOException ignored) {
                }
            }
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}

