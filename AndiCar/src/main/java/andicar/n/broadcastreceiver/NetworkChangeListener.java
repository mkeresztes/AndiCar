/*
 * AndiCar
 *
 *  Copyright (c) 2016 Miklos Keresztes (miklos.keresztes@gmail.com)
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

package andicar.n.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.io.File;

import andicar.n.service.SecureBackupService;
import andicar.n.utils.AndiCarCrashReporter;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 9/2/16.
 * <p>
 * Listen for network availability
 */
public class NetworkChangeListener extends BroadcastReceiver {

    private static final String LogTag = "NetworkChangeListener";

    @Override
    public void onReceive(Context context, Intent i) {
        if (!i.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")) {
            return;
        }

        SharedPreferences preference = AndiCar.getDefaultSharedPreferences();
        Log.d(LogTag, "Network status changed");
        //check network status if secure backup is enabled and postponed backups exists
        if (Utils.isNetworkAvailable(context, preference.getBoolean(AndiCar.getAppResources().getString(R.string.pref_key_secure_backup_only_wifi), true)) &&
                preference.getBoolean("secure_backup_enabled", false) &&
                preference.getString(AndiCar.getAppResources().getString(R.string.pref_key_postponed_secure_backupfile), "").length() > 0) {
            try {
                String bkFileName = (new File(preference.getString(AndiCar.getAppResources().getString(R.string.pref_key_postponed_secure_backupfile), ""))).getName();
                Intent intent = new Intent(context, SecureBackupService.class);
                intent.putExtra("bkFile", preference.getString(AndiCar.getAppResources().getString(R.string.pref_key_postponed_secure_backupfile), ""));
                intent.putExtra("attachName", bkFileName);
                context.startService(intent);
            }
            catch (Exception e) {
                if (!(e.getClass().equals(GoogleAuthIOException.class) || e.getClass().equals(GoogleAuthException.class))) {
                    AndiCarCrashReporter.sendCrash(e);
                }
            }
        }
    }
}
