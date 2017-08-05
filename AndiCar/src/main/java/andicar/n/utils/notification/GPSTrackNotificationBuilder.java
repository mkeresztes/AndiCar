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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import org.andicar2.activity.R;

import andicar.n.activity.dialogs.GPSTrackControllerDialogActivity;
import andicar.n.service.GPSTrackService;

/**
 * Created by Miklos Keresztes on 16.03.2017.
 */

class GPSTrackNotificationBuilder extends NotificationCompat.Builder {

    GPSTrackNotificationBuilder(Context context, int what) {
        super(context);

        if (what == AndiCarNotification.NOTIF_GPS_TRACKING_STARTED_ID) {
            this.setContentTitle(context.getText(R.string.gps_track_service_track_in_progress_title));
            this.setContentText(context.getString(R.string.gps_track_service_track_in_progress_message));
            this.setSmallIcon(R.drawable.ic_notif_gpstrack);
            this.setTicker(context.getString(R.string.gps_track_service_track_in_progress_message));
            this.setWhen(System.currentTimeMillis());

            Intent i = new Intent(context, GPSTrackService.class);
            i.setAction(GPSTrackService.ACTION_PAUSE);
            PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
            NotificationCompat.Action actionPause = new NotificationCompat.Action.Builder(R.drawable.ic_button_gps_pause_black_24dp_pad4dp,
                    context.getString(R.string.gps_track_pause), pi).build();

            i.setAction(GPSTrackService.ACTION_STOP);
            pi = PendingIntent.getService(context, 0, i, 0);
            NotificationCompat.Action actionStop = new NotificationCompat.Action.Builder(R.drawable.ic_button_gps_stop_black_24dp_pad4dp,
                    context.getString(R.string.gps_track_stop), pi).build();

            this.addAction(actionPause);
            this.addAction(actionStop);
        }
        else if (what == AndiCarNotification.NOTIF_GPS_PAUSED_ID) {
            this.setContentTitle(context.getText(R.string.gps_track_service_tracking_paused_title));
            this.setContentText(context.getString(R.string.gps_track_service_tracking_paused_message));
            this.setSmallIcon(R.drawable.ic_notif_pause);
            this.setTicker(context.getString(R.string.gps_track_service_tracking_paused_message));
            this.setWhen(System.currentTimeMillis());

            Intent i = new Intent(context, GPSTrackService.class);
            i.setAction(GPSTrackService.ACTION_RESUME);
            PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
            NotificationCompat.Action actionResume = new NotificationCompat.Action.Builder(R.drawable.ic_button_gps_play_black_24dp_pad4dp,
                    context.getString(R.string.gps_track_resume), pi).build();

            i.setAction(GPSTrackService.ACTION_STOP);
            pi = PendingIntent.getService(context, 0, i, 0);
            NotificationCompat.Action actionStop = new NotificationCompat.Action.Builder(R.drawable.ic_button_gps_stop_black_24dp_pad4dp,
                    context.getString(R.string.gps_track_stop), pi).build();

            this.addAction(actionResume);
            this.addAction(actionStop);
        }
        else if (what == AndiCarNotification.NOTIF_GPS_DISABLED_ID) {
            this.setContentTitle(context.getText(R.string.gps_track_service_auto_shut_down_title));
            this.setContentText(context.getString(R.string.gps_track_service_gps_disabled_message));
            this.setSmallIcon(R.drawable.ic_notif_andicar_error);
            this.setTicker(context.getString(R.string.gps_track_service_gps_disabled_message));
            this.setWhen(System.currentTimeMillis());
            this.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);
        }
        else if (what == AndiCarNotification.NOTIF_GPS_OUTOFSERVICE_ID) {
            this.setContentTitle(context.getText(R.string.gps_track_service_auto_shut_down_title));
            this.setContentText(context.getString(R.string.gps_track_service_gps_out_of_service));
            this.setSmallIcon(R.drawable.ic_notif_andicar_error);
            this.setTicker(context.getString(R.string.gps_track_service_gps_out_of_service));
            this.setWhen(System.currentTimeMillis());
            this.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);
        }

        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, GPSTrackControllerDialogActivity.class), 0);
        this.setContentIntent(pi);
    }
}
