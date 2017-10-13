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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.io.File;

import andicar.n.service.BackupService;
import andicar.n.service.FBJobService;
import andicar.n.service.ToDoManagementService;
import andicar.n.service.ToDoNotificationService;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.LogFileWriter;

@SuppressWarnings("JavaDoc")
public class ServiceStarter extends BroadcastReceiver {
    private static final String LOG_TAG = "AndiCar";
//    private static final String LOG_TAG = "ServiceStarter";

    /**
     * Start the services using FirebaseJobDispacher
     *  @param context
     * @param whatService see ConstantValues.SERVICE_STARTER_... constants
     * @param serviceParams
     */
    public static void startServicesUsingFBJobDispacher(Context context, String whatService, Bundle serviceParams) {
//        Intent intent;
        Bundle dispatcherParams = new Bundle();
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job fbJob;

        if (whatService.equals(ConstantValues.SERVICE_STARTER_START_ALL) || whatService.equals(ConstantValues.SERVICE_STARTER_START_TODO_MANAGEMENT_SERVICE)) {
            //start TO-DO notification service
            dispatcherParams.putString(FBJobService.JOB_TYPE_KEY, FBJobService.JOB_TYPE_TODO);

            fbJob = dispatcher.newJobBuilder()
                    // the JobService that will be called
                    .setService(FBJobService.class)
                    // uniquely identifies the job
                    .setTag(FBJobService.JOB_TYPE_TODO)
                    // one-off job
                    .setRecurring(false)
                    .setLifetime(Lifetime.FOREVER)
                    // start between 0 and 30 seconds from now
                    .setTrigger(Trigger.executionWindow(0, 30))
                    // overwrite an existing job with the same tag
                    .setReplaceCurrent(true)
                    // retry with exponential backoff
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    // constraints that need to be satisfied for the job to run
                    .setExtras(dispatcherParams)
                    .build();
            dispatcher.mustSchedule(fbJob);
        }

        if (whatService.equals(ConstantValues.SERVICE_STARTER_START_ALL) || whatService.equals(ConstantValues.SERVICE_STARTER_START_BACKUP_SERVICE)) {
            dispatcherParams.putString(FBJobService.JOB_TYPE_KEY, FBJobService.JOB_TYPE_BACKUP);

            fbJob = dispatcher.newJobBuilder()
                    // the JobService that will be called
                    .setService(FBJobService.class)
                    // uniquely identifies the job
                    .setTag(FBJobService.JOB_TYPE_BACKUP)
                    // one-off job
                    .setRecurring(false)
                    .setLifetime(Lifetime.FOREVER)
                    // start between 0 and 30 seconds from now
                    .setTrigger(Trigger.executionWindow(0, 30))
                    // overwrite an existing job with the same tag
                    .setReplaceCurrent(true)
                    // retry with exponential backoff
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    // constraints that need to be satisfied for the job to run
                    .setExtras(dispatcherParams)
                    .build();
            dispatcher.mustSchedule(fbJob);
        }

        if (whatService.equals(ConstantValues.SERVICE_STARTER_START_SECURE_BACKUP)) {
            dispatcherParams.putString(FBJobService.JOB_TYPE_KEY, FBJobService.JOB_TYPE_SECURE_BACKUP);
            dispatcherParams.putBundle(FBJobService.JOB_PARAMS_KEY, serviceParams);

            fbJob = dispatcher.newJobBuilder()
                    // the JobService that will be called
                    .setService(FBJobService.class)
                    // uniquely identifies the job
                    .setTag(FBJobService.JOB_TYPE_SECURE_BACKUP)
                    // one-off job
                    .setRecurring(false)
                    .setLifetime(Lifetime.FOREVER)
                    // start between 0 and 30 seconds from now
                    .setTrigger(Trigger.executionWindow(0, 30))
                    // overwrite an existing job with the same tag
                    .setReplaceCurrent(true)
                    // retry with exponential backoff
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    // constraints that need to be satisfied for the job to run
                    .setExtras(dispatcherParams)
                    .setConstraints(
                            // only run on an unmetered network
                            (AndiCar.getDefaultSharedPreferences().getBoolean(context.getResources().getString(R.string.pref_key_secure_backup_only_wifi), true) ? Constraint.ON_UNMETERED_NETWORK : Constraint.ON_ANY_NETWORK)
                    )
                    .build();
            dispatcher.mustSchedule(fbJob);
        }
    }

    /**
     * Directly start the services using context.startService()<br>
     * Use it only if the app is in foreground to avoid Background Service Limitations on Anroid O+<br>
     * Consult https://developer.android.com/about/versions/oreo/background.html for details
     *
     * @param context
     * @param whatService see ConstantValues.SERVICE_STARTER_... constants
     */
    public static void startServicesDirect(Context context, String whatService) {
        Intent intent;
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
            context.startService(intent);
            Log.i(LOG_TAG, "Done");
        }
    }

    @Override
    public void onReceive(Context context, Intent rIntent) {
        Log.d(LOG_TAG, "onReceive called for: " + rIntent.getAction());
        try {
            try {
                if (FileUtils.isFileSystemAccessGranted(context)) {
                    FileUtils.createFolderIfNotExists(context, ConstantValues.LOG_FOLDER);
                    File debugLogFile = new File(ConstantValues.LOG_FOLDER + "SSBroadcast.log");
                    LogFileWriter debugLogFileWriter = new LogFileWriter(debugLogFile, true);
                    debugLogFileWriter.appendnl("onReceive called for: ").append(rIntent.getAction());
                    debugLogFileWriter.flush();
                    debugLogFileWriter.close();
                }
            }
            catch (Exception ignored) {
            }

            if (rIntent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                    || rIntent.getAction().equals(Intent.ACTION_DATE_CHANGED)
                    || rIntent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
                //start services
                startServicesUsingFBJobDispacher(context, ConstantValues.SERVICE_STARTER_START_ALL, null);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
