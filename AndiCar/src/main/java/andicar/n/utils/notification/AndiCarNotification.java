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
import android.app.NotificationManager;
import android.content.Context;

/**
 * Created by Miklos Keresztes on 9/8/16.
 * Helper class for showing a notification in the notification area
 */
public class AndiCarNotification {
    public static final int NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR = 100;
    public static final int NOTIFICATION_TYPE_REPORTABLE_ERROR = 101;
    public static final int NOTIFICATION_TYPE_WARNING = 102;
    public static final int NOTIFICATION_TYPE_INFO = 103;

    public static final int NOTIF_GPS_TRACKING_STARTED_ID = 1;
    //    public static final int NOTIF_GPS_ACCURACY_WARNING_ID = 2;
//    public static final int NOTIF_FILESYSTEM_ERROR_ID = 3;
//    public static final int NOTIF_GPS_ACCURACY_SHUTDOWN_ID = 4;
    public static final int NOTIF_GPS_DISABLED_ID = 5;
    public static final int NOTIF_GPS_OUTOFSERVICE_ID = 6;
    public static final int NOTIF_GPS_PAUSED_ID = 16;

    /**
     * @param context           context
     * @param notificationType  :
     *                          ERROR (NOTIFICATION_TYPE_NOT_REPORTABLE_ERROR or NOTIFICATION_TYPE_REPORTABLE_ERROR)
     *                          WARNING (NOTIFICATION_TYPE_WARNING)
     *                          INFO (NOTIFICATION_TYPE_INFO)
     * @param notificationId    the unique ID of the notification. used for updating an existing notification
     * @param contentText       notification text
     * @param resultIntentClass result intent for this setContentIntent
     * @param exception         optional an exception object to send a backtrace to developers
     */
    public static void showGeneralNotification(Context context, int notificationType, int notificationId, String contentTitle,
                                               String contentText, Class resultIntentClass, Exception exception) {
        GeneralNotificationBuilder notifBuilder =
                new GeneralNotificationBuilder(context, notificationType, notificationId, contentTitle,
                        contentText, resultIntentClass, exception);
        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        notificationManager.notify(notificationId, notifBuilder.build());
    }

    public static void showToDoNotification(Context context, long toDoID, String notificationTitle, String notificationText, int triggeredBy, String carUOMCode, String minutesOrDays) {
        ToDoNotificationBuilder notifBuilder =
                new ToDoNotificationBuilder(context, toDoID, notificationTitle, notificationText, triggeredBy, carUOMCode, minutesOrDays);
        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        Notification n = notifBuilder.build();
        n.flags |= Notification.FLAG_NO_CLEAR;
        notificationManager.notify(((Long) toDoID).intValue(), n);
    }

    public static Notification showGPSTrackNotification(Context context, int what) {
        GPSTrackNotificationBuilder notificationBuilder =
                new GPSTrackNotificationBuilder(context, what);
        // Gets an instance of the NotificationManager service
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        Notification notification = notificationBuilder.build();

        if (what == AndiCarNotification.NOTIF_GPS_TRACKING_STARTED_ID
                || what == AndiCarNotification.NOTIF_GPS_PAUSED_ID) {
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
        }

        return notification;
    }
}
