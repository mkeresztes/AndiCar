/*
 *  AndiCar - a car management software for Android powered devices.
 *
 *  Copyright (C) 2013 Miklos Keresztes (miklos.keresztes@gmail.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package andicar.n.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.io.File;

import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.LogFileWriter;
import andicar.n.utils.Utils;

@SuppressWarnings("JavaDoc")
public class ServiceStarter extends BroadcastReceiver {
    private static final String LOG_TAG = "AndiCar";

    @Override
    public void onReceive(Context context, Intent rIntent) {
        Log.d(LOG_TAG, "onReceive called for: " + rIntent.getAction());
        try {
//            if (FileUtils.isFileSystemAccessGranted(context)) {
                FileUtils.createFolderIfNotExists(context, ConstantValues.LOG_FOLDER);
                File debugLogFile = new File(ConstantValues.LOG_FOLDER + "SSBroadcast.log");
                LogFileWriter debugLogFileWriter = new LogFileWriter(debugLogFile, true);
                debugLogFileWriter.appendnl("onReceive called for: ").append(rIntent.getAction());
                debugLogFileWriter.flush();
                debugLogFileWriter.close();
//            }
        }
        catch (Exception ignored) {
        }

        if (rIntent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                || rIntent.getAction().equals(Intent.ACTION_DATE_CHANGED)
                || rIntent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            Utils.setBackupNextRun(context, AndiCar.getDefaultSharedPreferences().getBoolean(context.getString(R.string.pref_key_backup_service_enabled), false));
            Utils.setToDoNextRun(context);
        }
    }
}
