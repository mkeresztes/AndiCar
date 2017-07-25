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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.andicar2.activity.R;

import andicar.n.activity.fragment.TaskEditFragment;
import andicar.n.persistence.DB;
import andicar.n.persistence.DBAdapter;
import andicar.n.utils.notification.AndiCarNotification;

/**
 * @author Miklos Keresztes
 */
public class ToDoNotificationService extends Service {

    public static final String TODO_ID_KEY = "ToDoID";
    public static final int TRIGGERED_BY_MILEAGE = 1;
    public static final int TRIGGERED_BY_TIME = 0;
    private static final String LOG_TAG = "AndiCar";

    private DBAdapter mDb = null;

    /**
     *
     */
    public ToDoNotificationService() {
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onStart(android.content.Intent, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(LOG_TAG, "onStartCommand: begin");

        Bundle mBundleExtras = intent.getExtras();
        mDb = new DBAdapter(this);
        if (mBundleExtras != null) {
            if (!mBundleExtras.getBoolean(ToDoManagementService.SET_JUST_NEXT_RUN_KEY)) {
                long mToDoID = mBundleExtras.getLong(TODO_ID_KEY);
                long mCarID = mBundleExtras.getLong(ToDoManagementService.CAR_ID_KEY);
                //@formatter:off
                String sql =
                        " SELECT * " +
                        " FROM " + DBAdapter.TABLE_NAME_TODO +
                        " WHERE " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__ISDONE) + "='N' " +
                                        " AND " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_GEN_ISACTIVE) + "='Y' ";

                if (mToDoID > 0) {
                    sql = sql + " AND " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_GEN_ROWID) + " = " + mToDoID;
                }
                if (mCarID > 0) {
                    sql = sql + " AND " + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__CAR_ID) + " = " + mCarID;
                }
                //@formatter:on

                Cursor toDoCursor = mDb.execSelectSql(sql, null);
                while (toDoCursor.moveToNext()) {
                    checkAndNotifyForToDo(toDoCursor);
                    Log.d(LOG_TAG, "onStartCommand: cursor move #" + toDoCursor.getPosition());
                }
                toDoCursor.close();
            }

            setNextRunForDate();
        }
        mDb.close();

        Log.d(LOG_TAG, "onStartCommand: end");
        return START_NOT_STICKY;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkAndNotifyForToDo(Cursor toDoCursor) {
        long toDoID = toDoCursor.getLong(DBAdapter.COL_POS_GEN_ROWID);
        String sql = " SELECT * " + " FROM " + DBAdapter.TABLE_NAME_TASK + " WHERE "
                + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TASK, DBAdapter.COL_NAME_GEN_ROWID) + " = ?";
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

    private void setNextRunForDate() {
        //@formatter:off
        String sql =
                " SELECT * " +
                " FROM " + DBAdapter.TABLE_NAME_TODO +
                " WHERE "
                        + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_GEN_ISACTIVE) + "='Y' " + " AND "
                        + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__ISDONE) + "='N' " + " AND "
                        + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__NOTIFICATIONDATE) + " >= ? " + " AND "
                        + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__NOTIFICATIONDATE) + " IS NOT NULL " + " ORDER BY "
                        + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__NOTIFICATIONDATE) + " ASC ";
        //@formatter:on
        long currentSec = System.currentTimeMillis() / 1000;
        String selArgs[] = {Long.toString(currentSec)};
        Cursor c = mDb.execSelectSql(sql, selArgs);
        if (c.moveToNext()) {
            long notificationDate = c.getLong(DBAdapter.COL_POS_TODO__NOTIFICATIONDATE);
            Intent i = new Intent(this, ToDoNotificationService.class);
            i.putExtra(ToDoManagementService.SET_JUST_NEXT_RUN_KEY, false);
            PendingIntent pIntent = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, notificationDate * 1000, pIntent);
        }
        c.close();
    }
}
