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

package andicar.n.utils.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import org.andicar2.activity.R;

import andicar.n.activity.dialogs.ToDoNotificationDialogActivity;
import andicar.n.service.ToDoNotificationJob;

/**
 * Created by Miklos Keresztes on 9/6/16.
 * <p>
 * Helper class for AndiCarNotification.Builder
 */
class ToDoNotificationBuilder extends Notification.Builder {
    private static final String NOTIF_CHANEL_TODO_ID = "toDoNotifID";
    private static final CharSequence NOTIF_CHANEL_TODO_NAME = "AndiCar to-do notifications";

    /**
     * Used for To-Do notification
     *
     * @param context
     * @param toDoID
     * @param notificationTitle
     * @param notificationText
     * @param triggeredBy
     * @param carUOMCode
     * @param minutesOrDays
     */
    @SuppressWarnings("JavaDoc")
    ToDoNotificationBuilder(Context context, NotificationManager notificationManager, long toDoID, String notificationTitle, String notificationText, int triggeredBy, String carUOMCode, String minutesOrDays) {
        super(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notifChanel = new NotificationChannel(NOTIF_CHANEL_TODO_ID, NOTIF_CHANEL_TODO_NAME, NotificationManager.IMPORTANCE_HIGH);
            notifChanel.setDescription(NOTIF_CHANEL_TODO_NAME.toString());
            notifChanel.enableLights(true);
            notifChanel.setLightColor(Color.RED);
            notifChanel.enableVibration(true);
            notificationManager.createNotificationChannel(notifChanel);
            this.setChannelId(notifChanel.getId());
        }

        this.setAutoCancel(true);
        //noinspection deprecation
        this.setDefaults(Notification.DEFAULT_ALL);
        this.setSmallIcon(R.drawable.ic_notif_alarm);
        this.setTicker("AndiCar " + context.getString(R.string.todo_alert_title));
        this.setWhen(System.currentTimeMillis());
        this.setContentTitle(notificationTitle);
        this.setContentText(notificationText);

        Intent i = new Intent(context, ToDoNotificationDialogActivity.class);
        i.putExtra(ToDoNotificationJob.TODO_ID_KEY, toDoID);
        i.putExtra(ToDoNotificationDialogActivity.TRIGGERED_BY_KEY, triggeredBy);
        i.putExtra(ToDoNotificationDialogActivity.CAR_UOM_CODE_KEY, carUOMCode);
        i.putExtra(ToDoNotificationDialogActivity.MINUTES_OR_DAYS_KEY, minutesOrDays);
        i.putExtra(ToDoNotificationDialogActivity.STARTED_FROM_NOTIFICATION_KEY, true);

        PendingIntent pi = PendingIntent.getActivity(context, ((Long) toDoID).intValue(), i, PendingIntent.FLAG_UPDATE_CURRENT);
        this.setContentIntent(pi);
    }
}
