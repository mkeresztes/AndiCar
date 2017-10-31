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

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.math.BigDecimal;
import java.util.Calendar;

import andicar.n.activity.fragment.TaskEditFragment;
import andicar.n.persistence.DBAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.LogFileWriter;
import andicar.n.utils.Utils;

/**
 * @author Miklos Keresztes
 *         <p>
 *         <hr>
 *         <br>
 *         <b> Description:</b> <br>
 *         This service maintain the to-do entries based on the task properties
 *         (due date, mileage, linked cars etc.) <br>
 *         All <b>recurent</b> tasks have a number of to-do entries for each
 *         linked car. <br>
 *         A new to-do is automatically created when an existing to-do is done or
 *         deactivated.
 */
@SuppressWarnings("WrongConstant")
public class ToDoManagementService extends Service {
    public static final String TASK_ID_KEY = "TaskID";

    private DBAdapter mDb = null;
    private long mTaskID = 0;
    private long mCarID = 0;
//    private boolean isSetJustNextRun = false;

    private File debugLogFile = new File(ConstantValues.LOG_FOLDER + "ToDoManagementService.log");
    private LogFileWriter debugLogFileWriter = null;

    //	private static int mTodoCount = 3;

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onStart(android.content.Intent, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
//            if (FileUtils.isFileSystemAccessGranted(getApplicationContext())) {
                debugLogFileWriter = new LogFileWriter(debugLogFile, false);
            debugLogFileWriter.appendnl("Starting ToDoManagementService");
//            }

            Bundle mBundleExtras = intent.getExtras();
            if (mBundleExtras != null) {
                mTaskID = mBundleExtras.getLong(ToDoManagementService.TASK_ID_KEY);
                mCarID = mBundleExtras.getLong(ToDoNotificationJob.CAR_ID_KEY);
            }
            if (debugLogFileWriter != null) {
                debugLogFileWriter.appendnl("TaskID = " + mTaskID);
                debugLogFileWriter.appendnl("CarID = " + mCarID);
            }

            mDb = new DBAdapter(this);
            createTaskTodos();
            mDb.close();

            Utils.setToDoNextRun(this);

            if (debugLogFileWriter != null) {
                debugLogFileWriter.appendnl("Service terminated");
                debugLogFileWriter.flush();
                debugLogFileWriter.close();
            }

        } catch (Exception e) {
            try {
                if (debugLogFileWriter != null) {
                    debugLogFileWriter.appendnl("Exception in service: ").append(e.getMessage()).append("\n").append(Utils.getStackTrace(e));
                }
                Log.d("AndiCar", e.getMessage(), e);
            } catch (Exception ignored) {
            }
        }
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

    private void createTaskTodos() {
        Cursor taskCursor;
        Cursor todoCursor;
        Cursor taskCarCursor;
        String taskSelection;
        String taskCarSelection;
        String todoSelection;
        String[] taskSelectionArgs;
        String[] todoSelectionArgs;
        String[] taskCarSelectArgs;
        int todoCount;
        boolean isRecurrentTask;

        taskSelection = "1 = 1 " + DBAdapter.WHERE_CONDITION_ISACTIVE;
        taskSelectionArgs = null;
        if (mTaskID > 0) {
            taskSelection = taskSelection + " AND " + DBAdapter.COL_NAME_GEN_ROWID + " = ?";
            taskSelectionArgs = new String[1];
            taskSelectionArgs[0] = Long.toString(mTaskID);
        }

        taskCarSelection = DBAdapter.COL_NAME_TASK_CAR__TASK_ID + " =? ";
        taskCursor = mDb.query(DBAdapter.TABLE_NAME_TASK, DBAdapter.COL_LIST_TASK_TABLE, taskSelection, taskSelectionArgs, null);

        int taskToDoCount;
        while (taskCursor.moveToNext()) {
            isRecurrentTask = taskCursor.getString(DBAdapter.COL_POS_TASK__ISRECURRENT).equals("Y");
            todoSelection = DBAdapter.COL_NAME_TODO__TASK_ID + "=? AND " + DBAdapter.COL_NAME_TODO__ISDONE + "='N'";
            taskToDoCount = taskCursor.getInt(DBAdapter.COL_POS_TASK__TODOCOUNT);

            if (mCarID > 0) {
                taskCarSelection = taskCarSelection + " AND " + DBAdapter.COL_NAME_TASK_CAR__CAR_ID + " =? ";
                taskCarSelectArgs = new String[2];
                taskCarSelectArgs[0] = taskCursor.getString(DBAdapter.COL_POS_GEN_ROWID);
                taskCarSelectArgs[1] = Long.toString(mCarID);

            }
            else {
                taskCarSelectArgs = new String[1];
                taskCarSelectArgs[0] = taskCursor.getString(DBAdapter.COL_POS_GEN_ROWID);
            }

            taskCarCursor = mDb.query(DBAdapter.TABLE_NAME_TASK_CAR, DBAdapter.COL_LIST_TASK_CAR_TABLE, taskCarSelection, taskCarSelectArgs,
                    null);

            if (taskCarCursor.getCount() > 0) {//cars are linked to task
                todoSelection = todoSelection + " AND " + DBAdapter.COL_NAME_TODO__CAR_ID + "=?";
                todoSelectionArgs = new String[2];
                todoSelectionArgs[0] = taskCursor.getString(DBAdapter.COL_POS_GEN_ROWID);
                while (taskCarCursor.moveToNext()) {
                    if (isRecurrentTask) {
                        todoSelectionArgs[1] = taskCarCursor.getString(DBAdapter.COL_POS_TASK_CAR__CAR_ID);
                        todoCursor = mDb.query(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_LIST_TODO_TABLE, todoSelection, todoSelectionArgs,
                                null);
                        todoCount = todoCursor.getCount();
                        todoCursor.close();
                        if (todoCount < taskToDoCount) {
                            for (int i = todoCount; i < taskToDoCount; i++) {
                                createToDo(taskCursor, taskCarCursor);
                            }
                        }
                    }
                    else {
                        createToDo(taskCursor, taskCarCursor);
                    }
                }
            }
            else { //no cars are linked to task
                if (isRecurrentTask) {
                    todoSelectionArgs = new String[1];
                    todoSelectionArgs[0] = taskCursor.getString(DBAdapter.COL_POS_GEN_ROWID);
                    todoCursor = mDb
                            .query(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_LIST_TODO_TABLE, todoSelection, todoSelectionArgs, null);
                    todoCount = todoCursor.getCount();
                    todoCursor.close();
                    if (todoCount < taskToDoCount) {
                        for (int i = todoCount; i < taskToDoCount; i++) {
                            createToDo(taskCursor, null);
                        }
                    }
                }
                else {
                    createToDo(taskCursor, null);
                }
            }
            taskCarCursor.close();
        }
        taskCursor.close();
    }

    @SuppressLint("WrongConstant")
    private void createToDo(Cursor taskCursor, @Nullable Cursor taskCarCursor) {
        Calendar nextToDoCalendar = Calendar.getInstance();
        long nextToDoMileage;
        long firstRunMileage;
        long todoStartIndex;
        ContentValues nextToDoContent = new ContentValues();
        Cursor lastTodoCursor; //base time for calculating next run time
        String todoSelectCondition;
        String[] todoSelectArgs;
        String todoSelectOrderBy;
        long taskId = taskCursor.getLong(DBAdapter.COL_POS_GEN_ROWID);
        long carId = 0;
        boolean isRecurrentTask = taskCursor.getString(DBAdapter.COL_POS_TASK__ISRECURRENT).equals("Y");
        boolean isDiffStartingTime;
        isDiffStartingTime = !(taskCursor.getString(DBAdapter.COL_POS_TASK__ISDIFFERENTSTARTINGTIME) == null
                || taskCursor.getString(DBAdapter.COL_POS_TASK__ISDIFFERENTSTARTINGTIME).equals("N"));

        if (taskCarCursor != null) {
            carId = taskCarCursor.getLong(DBAdapter.COL_POS_TASK_CAR__CAR_ID);
        }

        String taskScheduledFor = taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR);
        int frequencyType = taskCursor.getInt(DBAdapter.COL_POS_TASK__TIMEFREQUENCYTYPE);
        int timeFrequency = taskCursor.getInt(DBAdapter.COL_POS_TASK__TIMEFREQUENCY);
        int mileageFrequency = taskCursor.getInt(DBAdapter.COL_POS_TASK__RUNMILEAGE);

        nextToDoContent.put(DBAdapter.COL_NAME_GEN_NAME, taskCursor.getString(DBAdapter.COL_POS_GEN_NAME));
        nextToDoContent.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, taskCursor.getString(DBAdapter.COL_POS_GEN_USER_COMMENT));
        nextToDoContent.put(DBAdapter.COL_NAME_TODO__TASK_ID, taskId);

        //select the last to-do
        todoSelectCondition = "1 = 1 " + DBAdapter.WHERE_CONDITION_ISACTIVE + " AND " + DBAdapter.COL_NAME_TODO__ISDONE + " = 'N' " + " AND "
                + DBAdapter.COL_NAME_TODO__TASK_ID + " = ? ";

        if (taskCarCursor == null) {
            todoSelectArgs = new String[1];
            todoSelectArgs[0] = Long.toString(taskId);
        }
        else {
            todoSelectCondition = todoSelectCondition + " AND " + DBAdapter.COL_NAME_TODO__CAR_ID + " = ? ";
            todoSelectArgs = new String[2];
            todoSelectArgs[0] = Long.toString(taskId);
            todoSelectArgs[1] = Long.toString(carId);
        }

        if (taskScheduledFor.equals(TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE)) {
            todoSelectOrderBy = DBAdapter.COL_NAME_TODO__DUEMILEAGE + " DESC";
        }
        else {
            todoSelectOrderBy = DBAdapter.COL_NAME_TODO__DUEDATE + " DESC";
        }

        lastTodoCursor = mDb.query(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_LIST_TODO_TABLE, todoSelectCondition, todoSelectArgs,
                todoSelectOrderBy);

        if (lastTodoCursor.moveToNext()) { //an existing to-do exist => this is the reference for the next to-do
            if (taskCarCursor != null) {
                nextToDoContent.put(DBAdapter.COL_NAME_TODO__CAR_ID, taskCarCursor.getLong(DBAdapter.COL_POS_TASK_CAR__CAR_ID));
            }
            else {
                nextToDoContent.put(DBAdapter.COL_NAME_TODO__CAR_ID, (Long) null);
            }

            if (taskScheduledFor.equals(TaskEditFragment.TASK_SCHEDULED_FOR_TIME) || taskScheduledFor.equals(TaskEditFragment.TASK_SCHEDULED_FOR_BOTH)) {

                if (taskCarCursor != null && isRecurrentTask && isDiffStartingTime) {
                    nextToDoCalendar.setTimeInMillis(taskCarCursor.getLong(DBAdapter.COL_POS_TASK_CAR__FIRSTRUN_DATE) * 1000);
                }
                else {
                    if (taskCursor.getString(DBAdapter.COL_POS_TASK__STARTINGTIME) != null) {
                        nextToDoCalendar.setTimeInMillis(taskCursor.getLong(DBAdapter.COL_POS_TASK__STARTINGTIME) * 1000);
                    }
                    else {
                        nextToDoCalendar.setTimeInMillis(System.currentTimeMillis());
                    }
                }
                boolean isLastDayOfMonth = (nextToDoCalendar.get(Calendar.YEAR) == 1970);

                //calculate the next to-do date
                nextToDoCalendar.setTimeInMillis(lastTodoCursor.getLong(DBAdapter.COL_POS_TODO__DUEDATE) * 1000);

                //set the calendar for next to-do based on task definition
                setNextToDoCalendar(nextToDoCalendar, frequencyType, timeFrequency, isLastDayOfMonth);
                nextToDoContent.put(DBAdapter.COL_NAME_TODO__DUEDATE, nextToDoCalendar.getTimeInMillis() / 1000);
                //set the alarm date
                if (taskCursor.getInt(DBAdapter.COL_POS_TASK__TIMEFREQUENCYTYPE) == TaskEditFragment.TASK_TIMEFREQUENCYTYPE_DAILY) {
                    nextToDoCalendar.add(Calendar.MINUTE, -1 * taskCursor.getInt(DBAdapter.COL_POS_TASK__TIMEREMINDERSTART));
                }
                else {
                    nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1 * taskCursor.getInt(DBAdapter.COL_POS_TASK__TIMEREMINDERSTART));
                }
                if (nextToDoCalendar.getTimeInMillis() < System.currentTimeMillis()) {
                    nextToDoContent.put(DBAdapter.COL_NAME_TODO__NOTIFICATIONDATE, (System.currentTimeMillis() / 1000) + 60);
                }
                else {
                    nextToDoContent.put(DBAdapter.COL_NAME_TODO__NOTIFICATIONDATE, nextToDoCalendar.getTimeInMillis() / 1000);
                }
            }
            if (taskScheduledFor.equals(TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE) || taskScheduledFor.equals(TaskEditFragment.TASK_SCHEDULED_FOR_BOTH)) {
                nextToDoMileage = lastTodoCursor.getLong(DBAdapter.COL_POS_TODO__DUEMILAGE) + mileageFrequency;
                nextToDoContent.put(DBAdapter.COL_NAME_TODO__DUEMILEAGE, nextToDoMileage);
                nextToDoContent.put(DBAdapter.COL_NAME_TODO__NOTIFICATIONMILEAGE,
                        nextToDoMileage - taskCursor.getLong(DBAdapter.COL_POS_TASK__MILEAGEREMINDERSTART));
            }

            if (taskScheduledFor.equals(TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE)) {
                nextToDoContent.put(DBAdapter.COL_NAME_TODO__DUEDATE, (Long) null);
                nextToDoContent.put(DBAdapter.COL_NAME_TODO__NOTIFICATIONDATE, (Long) null);
            }
            if (taskScheduledFor.equals(TaskEditFragment.TASK_SCHEDULED_FOR_TIME)) {
                nextToDoContent.put(DBAdapter.COL_NAME_TODO__DUEMILEAGE, (Long) null);
                nextToDoContent.put(DBAdapter.COL_NAME_TODO__NOTIFICATIONMILEAGE, (Long) null);
            }
        } else { //this is the first to-do or the to-do list is generated for only one to-do, which was marked as done
            if (taskCarCursor != null) {
                nextToDoContent.put(DBAdapter.COL_NAME_TODO__CAR_ID, taskCarCursor.getLong(DBAdapter.COL_POS_TASK_CAR__CAR_ID));
            }
            else {
                nextToDoContent.put(DBAdapter.COL_NAME_TODO__CAR_ID, (Long) null);
            }

            if (taskScheduledFor.equals(TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE)) {
                if (taskCarCursor != null) {
                    if (isRecurrentTask) {
                        firstRunMileage = taskCarCursor.getLong(DBAdapter.COL_POS_TASK_CAR__FIRSTRUN_MILEAGE);
                        if (mDb.getCarCurrentIndex(carId) != null) {
                            todoStartIndex = mDb.getCarCurrentIndex(carId).longValue();
                        }
                        else {
                            todoStartIndex = 0;
                        }

                        //check the last to-do marked as done
                        BigDecimal lastDoneToDoIndex = mDb.getLastDoneTodoMileage(carId, mTaskID);
                        Long lastDoneToDoIndexL = -1L;
                        if (lastDoneToDoIndex != null) {
                            lastDoneToDoIndexL = lastDoneToDoIndex.longValue() + 1; //+ 1 to skip this index
                        }

                        if (todoStartIndex < lastDoneToDoIndexL)
                            todoStartIndex = lastDoneToDoIndexL;

                        while (firstRunMileage < todoStartIndex) {
                            firstRunMileage = firstRunMileage + mileageFrequency;
                        }

                    }
                    else {
                        firstRunMileage = taskCursor.getLong(DBAdapter.COL_POS_TASK__RUNMILEAGE);
                    }

                    nextToDoContent.put(DBAdapter.COL_NAME_TODO__DUEMILEAGE, firstRunMileage);
                    nextToDoContent.put(DBAdapter.COL_NAME_TODO__NOTIFICATIONMILEAGE,
                            firstRunMileage - taskCursor.getLong(DBAdapter.COL_POS_TASK__MILEAGEREMINDERSTART));
                    nextToDoContent.put(DBAdapter.COL_NAME_TODO__DUEDATE, (Long) null);
                    nextToDoContent.put(DBAdapter.COL_NAME_TODO__NOTIFICATIONDATE, (Long) null);
                }
            }
            else if (taskScheduledFor.equals(TaskEditFragment.TASK_SCHEDULED_FOR_BOTH) || taskScheduledFor.equals(TaskEditFragment.TASK_SCHEDULED_FOR_TIME)) {

                //at this point the nextToDoCalendar is initialized with the current time.
                int currentYear = nextToDoCalendar.get(Calendar.YEAR);
                int currentMonth = nextToDoCalendar.get(Calendar.MONTH);
                int currentDay = nextToDoCalendar.get(Calendar.DAY_OF_MONTH);
                nextToDoCalendar.add(Calendar.DAY_OF_YEAR, 1);
                boolean isLastDayOfCurrentMonth = (currentMonth != nextToDoCalendar.get(Calendar.MONTH));
                nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1);

                if (taskCarCursor != null && isRecurrentTask && isDiffStartingTime) {
                    nextToDoCalendar.setTimeInMillis(taskCarCursor.getLong(DBAdapter.COL_POS_TASK_CAR__FIRSTRUN_DATE) * 1000);
                }
                else {
                    if (taskCursor.getString(DBAdapter.COL_POS_TASK__STARTINGTIME) != null) {
                        nextToDoCalendar.setTimeInMillis(taskCursor.getLong(DBAdapter.COL_POS_TASK__STARTINGTIME) * 1000);
                    }
                    else {
                        nextToDoCalendar.setTimeInMillis(System.currentTimeMillis());
                    }
                }

                boolean isLastDayOfMonth = (nextToDoCalendar.get(Calendar.YEAR) == 1970);
                if (isLastDayOfMonth) {
                    nextToDoCalendar.set(Calendar.YEAR, currentYear);
                }

                if (isLastDayOfMonth) {
                    if (isLastDayOfCurrentMonth)
                    //wee need to compare the hour:minute of the task with the current hour:minute
                    {
                        nextToDoCalendar.set(Calendar.DAY_OF_MONTH, currentDay);
                    }
                    else
                    //wee not need to compare the hour:minute of the task with the current hour:minute
                    {
                        nextToDoCalendar.set(Calendar.DAY_OF_MONTH, currentDay + 1);
                    }

                    if (nextToDoCalendar.getTimeInMillis() > System.currentTimeMillis()) {
                        //set the first run at the end of the month specified in the task definition
                        nextToDoCalendar.set(Calendar.DAY_OF_MONTH, 1);
                        nextToDoCalendar.add(Calendar.MONTH, 1); //go to the first day of the next month
                        nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1); //go back to the last day of the previous month
                    }
                }
                while (nextToDoCalendar.getTimeInMillis() < System.currentTimeMillis()) {
                    setNextToDoCalendar(nextToDoCalendar, frequencyType, timeFrequency, isLastDayOfMonth);
                }

                nextToDoContent.put(DBAdapter.COL_NAME_TODO__DUEDATE, nextToDoCalendar.getTimeInMillis() / 1000);
                //set the alarm date
                if (taskCursor.getInt(DBAdapter.COL_POS_TASK__TIMEFREQUENCYTYPE) == TaskEditFragment.TASK_TIMEFREQUENCYTYPE_DAILY) {
                    nextToDoCalendar.add(Calendar.MINUTE, -1 * taskCursor.getInt(DBAdapter.COL_POS_TASK__TIMEREMINDERSTART));
                }
                else {
                    nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1 * taskCursor.getInt(DBAdapter.COL_POS_TASK__TIMEREMINDERSTART));
                }
                if (nextToDoCalendar.getTimeInMillis() < System.currentTimeMillis()) {
                    nextToDoContent.put(DBAdapter.COL_NAME_TODO__NOTIFICATIONDATE, (System.currentTimeMillis() / 1000) + 60);
                }
                else {
                    nextToDoContent.put(DBAdapter.COL_NAME_TODO__NOTIFICATIONDATE, nextToDoCalendar.getTimeInMillis() / 1000);
                }

                if (taskScheduledFor.equals(TaskEditFragment.TASK_SCHEDULED_FOR_BOTH)) {
                    if (taskCarCursor != null) {
                        if (isRecurrentTask) {
                            firstRunMileage = taskCarCursor.getLong(DBAdapter.COL_POS_TASK_CAR__FIRSTRUN_MILEAGE);
                            if (mDb.getCarCurrentIndex(carId) != null) {
                                todoStartIndex = mDb.getCarCurrentIndex(carId).longValue();
                            }
                            else {
                                todoStartIndex = 0;
                            }

                            //check the last to-do marked as done
                            BigDecimal lastDoneToDoIndex = mDb.getLastDoneTodoMileage(carId, mTaskID);
                            Long lastDoneToDoIndexL = -1L;
                            if (lastDoneToDoIndex != null) {
                                lastDoneToDoIndexL = lastDoneToDoIndex.longValue() + 1; //+ 1 to skip this index
                            }

                            if (todoStartIndex < lastDoneToDoIndexL)
                                todoStartIndex = lastDoneToDoIndexL;

                            while (firstRunMileage <= todoStartIndex) {
                                firstRunMileage = firstRunMileage + mileageFrequency;
                            }
                        }
                        else {
                            firstRunMileage = taskCursor.getLong(DBAdapter.COL_POS_TASK__RUNMILEAGE);
                        }
                        nextToDoContent.put(DBAdapter.COL_NAME_TODO__DUEMILEAGE, firstRunMileage);
                        nextToDoContent.put(DBAdapter.COL_NAME_TODO__NOTIFICATIONMILEAGE,
                                firstRunMileage - taskCursor.getLong(DBAdapter.COL_POS_TASK__MILEAGEREMINDERSTART));
                    }
                }
                else {
                    nextToDoContent.put(DBAdapter.COL_NAME_TODO__DUEMILEAGE, (Long) null);
                    nextToDoContent.put(DBAdapter.COL_NAME_TODO__NOTIFICATIONMILEAGE, (Long) null);
                }
            }

        }
        mDb.createRecord(DBAdapter.TABLE_NAME_TODO, nextToDoContent);
        lastTodoCursor.close();
    }

    @SuppressLint("WrongConstant")
    private void setNextToDoCalendar(Calendar nextToDoCalendar, int frequencyType, int timeFrequency, boolean isLastDayOfMonth) {
        if (frequencyType == TaskEditFragment.TASK_TIMEFREQUENCYTYPE_DAILY) {
            nextToDoCalendar.add(Calendar.DAY_OF_YEAR, timeFrequency);
        }
        else if (frequencyType == TaskEditFragment.TASK_TIMEFREQUENCYTYPE_WEEKLY) {
            nextToDoCalendar.add(Calendar.WEEK_OF_YEAR, timeFrequency);
        }
        else if (frequencyType == TaskEditFragment.TASK_TIMEFREQUENCYTYPE_MONTHLY) {
            if (isLastDayOfMonth) { //last day of the month
                nextToDoCalendar.set(Calendar.DAY_OF_MONTH, 1);
                nextToDoCalendar.add(Calendar.MONTH, timeFrequency + 1); //go to the first day of the next month
                nextToDoCalendar.add(Calendar.DAY_OF_YEAR, -1); //go back to the last day of the previous month
            }
            else {
                nextToDoCalendar.add(Calendar.MONTH, timeFrequency);
            }
        }
        else if (frequencyType == TaskEditFragment.TASK_TIMEFREQUENCYTYPE_YEARLY) {
            nextToDoCalendar.add(Calendar.YEAR, timeFrequency);
        }
    }
}
