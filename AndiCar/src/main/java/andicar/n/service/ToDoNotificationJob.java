package andicar.n.service;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.andicar2.activity.R;

import andicar.n.activity.fragment.TaskEditFragment;
import andicar.n.persistence.DB;
import andicar.n.persistence.DBAdapter;
import andicar.n.utils.Utils;
import andicar.n.utils.notification.AndiCarNotification;

/**
 * Created by Miklos Keresztes on 17.10.2017.
 */

public class ToDoNotificationJob extends JobService {
    public static final String TAG = "ToDo";
    public static final String TODO_ID_KEY = "ToDoID";
    public static final String CAR_ID_KEY = "CarID";
    public static final int TRIGGERED_BY_MILEAGE = 1;
    public static final int TRIGGERED_BY_TIME = 0;
    private static final String LOG_TAG = "AndiCarToDoNotifJob";

    private DBAdapter mDb = null;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(LOG_TAG, "onStartCommand: begin");

        final Bundle mBundleExtras = jobParameters.getExtras();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mDb = new DBAdapter(getApplicationContext());
                    long mToDoID = -1;
                    long mCarID = -1;
                    if (mBundleExtras != null) {
                        mToDoID = mBundleExtras.getLong(TODO_ID_KEY, -1L);
                        mCarID = mBundleExtras.getLong(CAR_ID_KEY, -1L);
                    }
                    //@formatter:off
                String sql =
                " SELECT * " +
                " FROM " + DBAdapter.TABLE_NAME_TODO +
                " WHERE " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__ISDONE) + "='N' " +
                                " AND " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_GEN_ISACTIVE) + "='Y' ";
                if(mToDoID > 0)
                    sql = sql + " AND " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_GEN_ROWID) + " = " + mToDoID;
                if (mCarID > 0)
                    sql = sql + " AND " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__CAR_ID) + " = " + mCarID;
                //@formatter:on

                    Cursor toDoCursor = mDb.execSelectSql(sql, null);
                    while (toDoCursor.moveToNext()) {
                        checkAndNotifyForToDo(toDoCursor);
//                        Log.d(LOG_TAG, "onStartCommand: cursor move #" + toDoCursor.getPosition());
                    }
                    toDoCursor.close();
                    mDb.close();
                }
                catch (Exception e) {
                    Utils.showReportableErrorDialog(getApplicationContext(), e.getMessage(), null, e, true);
                }
            }
        }).start();

        Log.d(LOG_TAG, "onStartCommand: end");
        jobFinished(jobParameters, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    private void checkAndNotifyForToDo(Cursor toDoCursor) {
        long toDoID = toDoCursor.getLong(DBAdapter.COL_POS_GEN_ROWID);
        //@formatter:off
        String sql = " SELECT * " +
                        " FROM " + DBAdapter.TABLE_NAME_TASK +
                        " WHERE " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TASK, DBAdapter.COL_NAME_GEN_ROWID) + " = ?";
        //@formatter:on
        String argValues[] = {Long.toString(toDoCursor.getLong(DBAdapter.COL_POS_TODO__TASK_ID))};
        Cursor taskCursor = mDb.query(sql, argValues);
        boolean showNotification = false;
        String contentText = "";
        String carUOMCode = "";
        String minutesOrDays;
        long carCurrentOdometer = 0;
        long todoAlarmMileage;
        long todoAlarmDate;
        long currentDateSec;
        int notificationTrigger = -1;

        if (taskCursor != null && taskCursor.moveToNext()) {

            if (toDoCursor.getString(DBAdapter.COL_POS_TODO__CAR_ID) != null) {
                Cursor carCursor = mDb.fetchRecord(DBAdapter.TABLE_NAME_CAR, DBAdapter.COL_LIST_CAR_TABLE,
                        toDoCursor.getLong(DBAdapter.COL_POS_TODO__CAR_ID));
                if (carCursor != null) {
                    contentText = getString(R.string.gen_car_label) + " " + carCursor.getString(DBAdapter.COL_POS_GEN_NAME);
                    carCurrentOdometer = carCursor.getLong(DBAdapter.COL_POS_CAR__INDEXCURRENT);
                    carUOMCode = mDb.getUOMCode(carCursor.getLong(DBAdapter.COL_POS_CAR__UOMLENGTH_ID));
                    carCursor.close();
                }
            }

            if (taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_TIME)
                    || taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_BOTH)) {
                todoAlarmDate = toDoCursor.getLong(DBAdapter.COL_POS_TODO__NOTIFICATIONDATE);
                currentDateSec = System.currentTimeMillis() / 1000;

                if (todoAlarmDate <= currentDateSec) {
                    showNotification = true;
                    notificationTrigger = TRIGGERED_BY_TIME;
                }

            }
            if (taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE)
                    || taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_BOTH)) {
                //				todoDueMileage = toDoCursor.getLong(MainDbAdapter.TODO_COL_DUEMILAGE_POS);
                todoAlarmMileage = toDoCursor.getLong(DBAdapter.COL_POS_TODO__NOTIFICATIONMILEAGE);
                if (todoAlarmMileage <= carCurrentOdometer) {
                    showNotification = true;
                    notificationTrigger = TRIGGERED_BY_MILEAGE;
                }

            }
            if (taskCursor.getInt(DBAdapter.COL_POS_TASK__TIMEFREQUENCYTYPE) == TaskEditFragment.TASK_TIMEFREQUENCYTYPE_DAILY) {
                minutesOrDays = getString(R.string.gen_minutes);
            }
            else {
                minutesOrDays = getString(R.string.gen_days);
            }

            if (showNotification) {
                String contentTitle = getString(R.string.pref_todo_title) + " " + taskCursor.getString(DBAdapter.COL_POS_GEN_NAME);

                AndiCarNotification.showToDoNotification(this, toDoID, contentTitle, contentText, notificationTrigger, carUOMCode, minutesOrDays);
            }
        }
        if (taskCursor != null) {
            taskCursor.close();
        }
    }

}
