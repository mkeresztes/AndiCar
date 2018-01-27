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

import andicar.n.activity.dialogs.MessageDialog;

/**
 * Created by Miklos Keresztes on 9/6/16.
 * <p>
 * Helper class for AndiCarNotification.Builder
 */
class MessageNotificationBuilder extends Notification.Builder {
    /**
     * Used for To-Do notification
     *
     * @param context a Context object
     * @param messageId the messageId
     */
    MessageNotificationBuilder(Context context, NotificationManager notificationManager, String messageId, String title) {
        super(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    GeneralNotificationBuilder.NOTIFICATION_CHANEL_GENERAL_ID, GeneralNotificationBuilder.NOTIFICATION_CHANEL_GENERAL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(GeneralNotificationBuilder.NOTIFICATION_CHANEL_GENERAL_NAME.toString());
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
            this.setChannelId(notificationChannel.getId());
        }

        this.setAutoCancel(true);
        //noinspection deprecation
        this.setDefaults(Notification.DEFAULT_ALL);
        this.setSmallIcon(R.drawable.ic_notif_andicar_message);
        this.setTicker("AndiCar message");
        this.setWhen(System.currentTimeMillis());
        this.setContentTitle(title);

        Intent i = new Intent(context, MessageDialog.class);
        i.putExtra(MessageDialog.MSG_ID_KEY, messageId);
        PendingIntent pi = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), i, PendingIntent.FLAG_UPDATE_CURRENT);
        this.setContentIntent(pi);
    }
}
