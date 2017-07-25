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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 28.03.2017.
 */

public class FBJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters job) {
        try {
            FileUtils.createFolderIfNotExists(getApplicationContext(), ConstantValues.LOG_FOLDER);
            File debugLogFile = new File(ConstantValues.LOG_FOLDER + "FBJobService.log");
            FileWriter debugLogFileWriter = new FileWriter(debugLogFile, false);

            debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" onStartJob begin");
            Intent intent = new Intent(getApplicationContext(), SecureBackupService.class);
            if (job.getExtras() != null) {
                intent.putExtra("bkFile", job.getExtras().getString("bkFile"));
                intent.putExtra("attachName", job.getExtras().getString("attachName"));
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" before StartService SecureBackupService");
                getApplicationContext().startService(intent);
                debugLogFileWriter.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" onStartJob terminated");
                debugLogFileWriter.flush();
                debugLogFileWriter.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
