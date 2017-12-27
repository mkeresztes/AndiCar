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
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;

import org.andicar2.activity.R;

import andicar.n.activity.dialogs.GPSTrackControllerDialogActivity;
import andicar.n.service.GPSTrackService;

import static android.app.Notification.VISIBILITY_PUBLIC;

/**
 * Created by Miklos Keresztes on 16.03.2017.
 */

class GPSTrackNotificationBuilder extends Notification.Builder {

    private static final String NOTIFICATION_CHANEL_GPSTRACK_ID_OLD = "gpsTrackNotifications";
    private static final String NOTIFICATION_CHANEL_GPSTRACK_ID = "gpsTrackNotification";
    private static final CharSequence NOTIFICATION_CHANEL_GPSTRACK_NAME = "AndiCar GPS Tracking";
    private static final String NOTIFICATION_CHANEL_GPSTRACK_ERROR_ID = "gpsTrackNotification_Error";
    private static final CharSequence NOTIFICATION_CHANEL_GPSTRACK_ERROR_NAME = "AndiCar GPS Tracking - errors";

    GPSTrackNotificationBuilder(Context context, int what) {
        super(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            try {
                //force recreating the chanel without vibration
                if (context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode <= 17110200) {
                    if (notificationManager != null) {
                        notificationManager.deleteNotificationChannel(NOTIFICATION_CHANEL_GPSTRACK_ID_OLD);
                    }
                }
            }
            catch (PackageManager.NameNotFoundException ignored) {
            }

            if (notificationManager != null) {
                NotificationChannel notificationChanel;

                if (what == AndiCarNotification.GPS_DISABLED_ID || what == AndiCarNotification.GPS_OUT_OF_SERVICE_ID) {
                    notificationChanel = new NotificationChannel(NOTIFICATION_CHANEL_GPSTRACK_ERROR_ID, NOTIFICATION_CHANEL_GPSTRACK_ERROR_NAME, NotificationManager.IMPORTANCE_HIGH);
                    notificationChanel.setDescription(NOTIFICATION_CHANEL_GPSTRACK_ERROR_NAME.toString());
                    notificationChanel.enableLights(true);
                    notificationChanel.setLightColor(Color.RED);
                    notificationChanel.enableVibration(true);
                }
                else {
                    notificationChanel = new NotificationChannel(NOTIFICATION_CHANEL_GPSTRACK_ID, NOTIFICATION_CHANEL_GPSTRACK_NAME, NotificationManager.IMPORTANCE_HIGH);
                    notificationChanel.setDescription(NOTIFICATION_CHANEL_GPSTRACK_NAME.toString());
                    notificationChanel.enableLights(false);
                    notificationChanel.enableVibration(false);

                }
                notificationChanel.setLockscreenVisibility(VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(notificationChanel);
                this.setChannelId(notificationChanel.getId());
            }
        }

        if (what == AndiCarNotification.GPS_TRACKING_IN_PROGRESS_ID) {
            this.setContentTitle(context.getText(R.string.gps_track_service_track_in_progress_title));
            this.setContentText(context.getString(R.string.gps_track_service_track_in_progress_message));
            this.setSmallIcon(R.drawable.ic_notif_gpstrack);
            this.setTicker(context.getString(R.string.gps_track_service_track_in_progress_message));
            this.setWhen(System.currentTimeMillis());
            this.setAutoCancel(false);
            this.setOngoing(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                Intent i = new Intent(context, GPSTrackService.class);
                i.setAction(GPSTrackService.ACTION_PAUSE);
                PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                //noinspection deprecation
                Notification.Action actionPause = new Notification.Action.Builder(R.drawable.ic_button_gps_pause_black_24dp_pad4dp, context.getString(R.string.gps_track_pause), pi).build();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    actionPause = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_button_gps_pause_black_24dp_pad4dp),
                            context.getString(R.string.gps_track_pause), pi).build();
                }

                i.setAction(GPSTrackService.ACTION_STOP);
                pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                //noinspection deprecation
                Notification.Action actionStop = new Notification.Action.Builder(R.drawable.ic_button_gps_stop_black_24dp_pad4dp,
                        context.getString(R.string.gps_track_stop), pi).build();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    actionStop = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_button_gps_stop_black_24dp_pad4dp),
                            context.getString(R.string.gps_track_stop), pi).build();
                }
                this.addAction(actionPause);
                this.addAction(actionStop);
            }
        }
        else if (what == AndiCarNotification.GPS_TRACKING_PAUSED_ID) {
            this.setContentTitle(context.getText(R.string.gps_track_service_tracking_paused_title));
            this.setContentText(context.getString(R.string.gps_track_service_tracking_paused_message));
            this.setSmallIcon(R.drawable.ic_notif_pause);
            this.setTicker(context.getString(R.string.gps_track_service_tracking_paused_message));
            this.setWhen(System.currentTimeMillis());
            this.setAutoCancel(false);
            this.setOngoing(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                Intent i = new Intent(context, GPSTrackService.class);
                i.setAction(GPSTrackService.ACTION_RESUME);
                PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                //noinspection deprecation
                Notification.Action actionResume = new Notification.Action.Builder(R.drawable.ic_button_gps_play_black_24dp_pad4dp,
                        context.getString(R.string.gps_track_resume), pi).build();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    actionResume = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_button_gps_play_black_24dp_pad4dp),
                            context.getString(R.string.gps_track_resume), pi).build();
                }
                i.setAction(GPSTrackService.ACTION_STOP);
                pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                //noinspection deprecation
                Notification.Action actionStop = new Notification.Action.Builder(R.drawable.ic_button_gps_stop_black_24dp_pad4dp,
                        context.getString(R.string.gps_track_stop), pi).build();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    actionStop = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_button_gps_stop_black_24dp_pad4dp),
                            context.getString(R.string.gps_track_stop), pi).build();
                }
                this.addAction(actionResume);
                this.addAction(actionStop);
            }
        } else if (what == AndiCarNotification.GPS_DISABLED_ID) {
            this.setContentTitle(context.getText(R.string.gps_track_service_auto_shut_down_title));
            this.setContentText(context.getString(R.string.gps_track_service_gps_disabled_message));
            this.setSmallIcon(R.drawable.ic_notif_andicar_error);
            this.setTicker(context.getString(R.string.gps_track_service_gps_disabled_message));
            this.setWhen(System.currentTimeMillis());
            //noinspection deprecation
            this.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        } else if (what == AndiCarNotification.GPS_OUT_OF_SERVICE_ID) {
            this.setContentTitle(context.getText(R.string.gps_track_service_auto_shut_down_title));
            this.setContentText(context.getString(R.string.gps_track_service_gps_out_of_service));
            this.setSmallIcon(R.drawable.ic_notif_andicar_error);
            this.setTicker(context.getString(R.string.gps_track_service_gps_out_of_service));
            this.setWhen(System.currentTimeMillis());
            //noinspection deprecation
            this.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        }

        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, GPSTrackControllerDialogActivity.class), 0);
        this.setContentIntent(pi);
    }
}
