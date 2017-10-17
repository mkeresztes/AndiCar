package andicar.n.service;

import android.content.Context;
import android.os.Bundle;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

/**
 * Created by Miklos Keresztes on 17.10.2017.
 */

public class JobStarter {

    public static final String SERVICE_STARTER_START_TODO_NOTIFICATION_SERVICE = "ToDoNotificationService";
    public static final String SERVICE_STARTER_START_SECURE_BACKUP = "SecureBackup";

    /**
     * Start the services using FirebaseJobDispacher
     *
     * @param context       context
     * @param whatService   see SERVICE_STARTER_... constants
     * @param serviceParams additional params for the job
     */
    public static void startServicesUsingFBJobDispacher(Context context, String whatService, Bundle serviceParams) {
        Bundle dispatcherParams = new Bundle();
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job fbJob;

        if (whatService.equals(SERVICE_STARTER_START_TODO_NOTIFICATION_SERVICE)) {
            dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
            Bundle jobParams = new Bundle();
            jobParams.putLong(ToDoNotificationJob.TODO_ID_KEY, serviceParams.getLong(ToDoNotificationJob.TODO_ID_KEY, -1));
            jobParams.putLong(ToDoNotificationJob.CAR_ID_KEY, serviceParams.getLong(ToDoNotificationJob.CAR_ID_KEY, -1));
            fbJob = dispatcher.newJobBuilder()
                    // the JobService that will be called
                    .setService(ToDoNotificationJob.class)
                    // uniquely identifies the job
                    .setTag(ToDoNotificationJob.TAG + serviceParams.getLong(ToDoNotificationJob.TODO_ID_KEY))
                    // one-off job
                    .setRecurring(false)
                    .setLifetime(Lifetime.FOREVER)
                    // start between 0 and 30 seconds from now
                    .setTrigger(Trigger.NOW)
                    // overwrite an existing job with the same tag
                    .setReplaceCurrent(true)
                    // retry with exponential backoff
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    .setExtras(jobParams)
                    .build();
            dispatcher.mustSchedule(fbJob);
        }

        if (whatService.equals(SERVICE_STARTER_START_SECURE_BACKUP)) {
            dispatcherParams.putString(SecureBackupJob.BK_FILE_KEY, serviceParams.getString(SecureBackupJob.BK_FILE_KEY));

            fbJob = dispatcher.newJobBuilder()
                    // the JobService that will be called
                    .setService(SecureBackupJob.class)
                    // uniquely identifies the job
                    .setTag(SecureBackupJob.TAG)
                    // one-off job
                    .setRecurring(false)
                    .setLifetime(Lifetime.FOREVER)
                    // start between 0 and 30 seconds from now
                    .setTrigger(Trigger.NOW)
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
}
