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

package org.andicar2.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.AndiCarCrashReporter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.Utils;
import io.fabric.sdk.android.Fabric;

public class AndiCar extends MultiDexApplication {
    private static Resources appResources;
    private static SharedPreferences appPreferences;
    @SuppressLint("StaticFieldLeak")

    public static Resources getAppResources() {
        return appResources;
    }

    public static SharedPreferences getDefaultSharedPreferences() {
        return appPreferences;
    }

    public static int getAppVersion() {
        return appPreferences.getInt(appResources.getString(R.string.pref_key_app_version), 0);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up Crashlytics, disabled for debug builds
        if (!Utils.isDebugVersion()) {
            Crashlytics crashlyticsKit = new Crashlytics.Builder()
                    .core(new CrashlyticsCore.Builder().build()).build();
            Fabric.with(this, crashlyticsKit);
        } else {
            //subscribe to Debug topic on FBMessaging
            FirebaseMessaging.getInstance().subscribeToTopic("Debug");
        }
        FirebaseMessaging.getInstance().subscribeToTopic("General");

        //subscribe to language specific topics
        String language;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            language = getApplicationContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
        }
        else {
            language = getApplicationContext().getResources().getConfiguration().locale.getLanguage();
        }
        if (language == null) {
            language = "en_US";
        }
        switch (language.toUpperCase().substring(0, 2)) {
            case "HU":
                FirebaseMessaging.getInstance().subscribeToTopic("GeneralHU");
                break;
            case "RO":
                FirebaseMessaging.getInstance().subscribeToTopic("GeneralRO");
                break;
            default:
                FirebaseMessaging.getInstance().subscribeToTopic("GeneralEN");
        }

        AndiCar.appResources = getResources();
        AndiCar.appPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ConstantValues.BASE_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + "/andicar";
        }
        else {
            ConstantValues.BASE_FOLDER = getApplicationContext().getFilesDir().getAbsolutePath();
        }

        ConstantValues.REPORT_FOLDER = ConstantValues.BASE_FOLDER + "/" + ConstantValues.REPORT_FOLDER_NAME + "/";
        ConstantValues.BACKUP_FOLDER = ConstantValues.BASE_FOLDER + "/" + ConstantValues.BACKUP_FOLDER_NAME + "/";
        ConstantValues.SYSTEM_BACKUP_FOLDER = ConstantValues.BASE_FOLDER + "/" + ConstantValues.SYSTEM_BACKUP_FOLDER_NAME + "/";
        ConstantValues.TRACK_FOLDER = ConstantValues.BASE_FOLDER + "/" + ConstantValues.TRACK_FOLDER_NAME + "/";
        ConstantValues.TEMP_FOLDER = ConstantValues.BASE_FOLDER + "/" + ConstantValues.TEMP_FOLDER_NAME + "/";
        //the log folder will be in the internal storage to avoid access permission
        ConstantValues.LOG_FOLDER = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + ConstantValues.LOG_FOLDER_NAME + "/";
        FileUtils.createFolderIfNotExists(getApplicationContext(), ConstantValues.LOG_FOLDER);

        initPreferences();

        //check if the app was updated
        int appVersion;
        int oldAppVersion;
        SharedPreferences.Editor e = appPreferences.edit();

        try {
            appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            oldAppVersion = appPreferences.getInt(getString(R.string.pref_key_app_version), getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
            Log.d("AndiCar", "Internal file storage: " + getApplicationContext().getFilesDir().getAbsolutePath());
            Log.d("AndiCar", "External file storage: " + getApplicationContext().getApplicationInfo().dataDir);
            Log.d("AndiCar", "BASE_FOLDER: " + ConstantValues.BASE_FOLDER);

            if (!appPreferences.contains(getString(R.string.pref_key_app_version))) {
                e.putInt(getString(R.string.pref_key_app_version), appVersion);
                e.putBoolean(getString(R.string.pref_key_show_welcome_screen), true);
            } else {
                if (oldAppVersion != appVersion) {
                    updateApp(oldAppVersion);
                    e.putInt(getString(R.string.pref_key_app_version), appVersion);
                    e.putBoolean(getString(R.string.pref_key_show_whats_new_dialog), true);
                    Utils.setToDoNextRun(getApplicationContext());
                    Utils.setBackupNextRun(getApplicationContext(), appPreferences.getBoolean(getString(R.string.pref_key_backup_service_enabled), false));
                }
            }
            e.apply();
        }
        catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    private void updateApp(int oldAppVersion) {
        SharedPreferences.Editor e = appPreferences.edit();
        //version upgrade
        if (oldAppVersion <= 17092000) {
            //migrate the main screen zones
            e.putString(getString(R.string.pref_key_main_zone10_content), appPreferences.getString(getString(R.string.pref_key_main_zone8_content), "LGT"));
            e.putString(getString(R.string.pref_key_main_zone9_content), appPreferences.getString(getString(R.string.pref_key_main_zone7_content), "CEX"));
            e.putString(getString(R.string.pref_key_main_zone8_content), appPreferences.getString(getString(R.string.pref_key_main_zone6_content), "LEX"));
            e.putString(getString(R.string.pref_key_main_zone7_content), appPreferences.getString(getString(R.string.pref_key_main_zone5_content), "CFV"));
            e.putString(getString(R.string.pref_key_main_zone6_content), appPreferences.getString(getString(R.string.pref_key_main_zone4_content), "CFQ"));
            e.putString(getString(R.string.pref_key_main_zone5_content), appPreferences.getString(getString(R.string.pref_key_main_zone3_content), "LFU"));
            e.putString(getString(R.string.pref_key_main_zone4_content), appPreferences.getString(getString(R.string.pref_key_main_zone2_content), "CTR"));
            e.putString(getString(R.string.pref_key_main_zone3_content), appPreferences.getString(getString(R.string.pref_key_main_zone1_content), "LTR"));
            e.remove(getString(R.string.pref_key_main_zone2_content)); //this will be initialized in the main screen, based on current car volume uom (Fuel Eff or Fuel Cons)
            e.putString(getString(R.string.pref_key_main_zone1_content), "STS");

            //delete the old log files
            try {
                FileUtils.cleanDirectory(new File(ConstantValues.LOG_FOLDER));
            }
            catch (IOException ignored) {
            }
        }
        if (oldAppVersion <= 17100500) {
            //correct the location of the files (log, gpstracks, etc.) if the internal storage is used
            File oldInternalLocation = new File(getApplicationContext().getApplicationInfo().dataDir + "/andicar");
            File newInternalLocation = getApplicationContext().getFilesDir();
            if (oldInternalLocation.exists() && ConstantValues.BASE_FOLDER.equals(newInternalLocation.getAbsolutePath())) {
                try {
                    FileUtils.copyDirectory(getApplicationContext(), oldInternalLocation, newInternalLocation, false);
                }
                catch (IOException ignored) {
                }
            }
        }
        if (oldAppVersion <= 17101200) {
            try {
                //fix duplicate tags with same name
                Log.d("AndiCar", "========== Fixing Duplicate Entries =========");
                DBAdapter db = new DBAdapter(getApplicationContext());
                FileUtils.createFolderIfNotExists(getApplicationContext(), ConstantValues.SYSTEM_BACKUP_FOLDER);
                FileUtils.backupDb(getApplicationContext(), db.getDatabase().getPath(), "sbku_", true, ConstantValues.SYSTEM_BACKUP_FOLDER);
                String sql = "select max(_id), name, count(*) from def_tag group by name having count(*) > 1";
                Cursor c = db.execSelectSql(sql, null);
                long id;
                String name;
                while (c.moveToNext()) {
                    id = c.getLong(0);
                    name = c.getString(1);

                    sql = "update " + DBAdapter.TABLE_NAME_MILEAGE +
                            " set " + DBAdapter.COL_NAME_MILEAGE__TAG_ID + " = " + id +
                            " where " + DBAdapter.COL_NAME_MILEAGE__TAG_ID + " in (select _id from def_tag where name = '" + name + "')";
                    db.execUpdate(sql);

                    sql = "update " + DBAdapter.TABLE_NAME_REFUEL +
                            " set " + DBAdapter.COL_NAME_REFUEL__TAG_ID + " = " + id +
                            " where " + DBAdapter.COL_NAME_REFUEL__TAG_ID + " in (select _id from def_tag where name = '" + name + "')";
                    db.execUpdate(sql);

                    sql = "update " + DBAdapter.TABLE_NAME_EXPENSE +
                            " set " + DBAdapter.COL_NAME_EXPENSE__TAG_ID + " = " + id +
                            " where " + DBAdapter.COL_NAME_EXPENSE__TAG_ID + " in (select _id from def_tag where name = '" + name + "')";
                    db.execUpdate(sql);

                    sql = "update " + DBAdapter.TABLE_NAME_GPSTRACK +
                            " set " + DBAdapter.COL_NAME_GPSTRACK__TAG_ID + " = " + id +
                            " where " + DBAdapter.COL_NAME_GPSTRACK__TAG_ID + " in (select _id from def_tag where name = '" + name + "')";
                    db.execUpdate(sql);

                    //delete duplicate tags
                    sql = "delete from " + DBAdapter.TABLE_NAME_TAG +
                            " where " + DBAdapter.COL_NAME_GEN_NAME + " = '" + name + "' " +
                            " AND " + DBAdapter.COL_NAME_GEN_ROWID + " <> " + id;
                    db.execUpdate(sql);
                }
                c.close();

                //fix duplicate bpartner
                sql = "select max(_id), name, count(*) from DEF_BPARTNER group by name having count(*) > 1";
                c = db.execSelectSql(sql, null);
                while (c.moveToNext()) {
                    id = c.getLong(0);
                    name = c.getString(1);

                    sql = "update " + DBAdapter.TABLE_NAME_REFUEL +
                            " set " + DBAdapter.COL_NAME_REFUEL__BPARTNER_ID + " = " + id +
                            " where " + DBAdapter.COL_NAME_REFUEL__BPARTNER_ID + " in (select _id from DEF_BPARTNER where name = '" + name + "')";
                    db.execUpdate(sql);

                    sql = "update " + DBAdapter.TABLE_NAME_EXPENSE +
                            " set " + DBAdapter.COL_NAME_EXPENSE__BPARTNER_ID + " = " + id +
                            " where " + DBAdapter.COL_NAME_EXPENSE__BPARTNER_ID + " in (select _id from DEF_BPARTNER where name = '" + name + "')";
                    db.execUpdate(sql);

                    sql = "update " + DBAdapter.TABLE_NAME_BPARTNERLOCATION +
                            " set " + DBAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID + " = " + id +
                            " where " + DBAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID + " in (select _id from DEF_BPARTNER where name = '" + name + "')";
                    db.execUpdate(sql);

                    //deactivate duplicate entries
                    sql = "update " + DBAdapter.TABLE_NAME_BPARTNER +
                            " set " + DBAdapter.COL_NAME_GEN_ISACTIVE + " = 'N' " +
                            " where " + DBAdapter.COL_NAME_GEN_NAME + " = '" + name + "' " +
                            " AND " + DBAdapter.COL_NAME_GEN_ROWID + " <> " + id;
                    db.execUpdate(sql);
                }

                //fix duplicate bpartner locations
                sql = "select max(_id), name, DEF_BPARTNER_ID, count(*) from DEF_BPARTNERLOCATION group by name, DEF_BPARTNER_ID having count(*) > 1";
                c = db.execSelectSql(sql, null);
                long bpID;
                while (c.moveToNext()) {
                    id = c.getLong(0);
                    name = c.getString(1);
                    bpID = c.getLong(2);

                    sql = "update " + DBAdapter.TABLE_NAME_REFUEL +
                            " set " + DBAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID + " = " + id +
                            " where " + DBAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID + " in " +
                            " (select _id from DEF_BPARTNERLOCATION where name = '" + name + "' AND DEF_BPARTNER_ID = " + bpID + ")";
                    db.execUpdate(sql);

                    sql = "update " + DBAdapter.TABLE_NAME_EXPENSE +
                            " set " + DBAdapter.COL_NAME_EXPENSE__BPARTNER_LOCATION_ID + " = " + id +
                            " where " + DBAdapter.COL_NAME_EXPENSE__BPARTNER_LOCATION_ID + " in " +
                            " (select _id from DEF_BPARTNERLOCATION where name = '" + name + "' AND DEF_BPARTNER_ID = " + bpID + ")";
                    db.execUpdate(sql);

                    //deactivate duplicate entries
                    sql = "update " + DBAdapter.TABLE_NAME_BPARTNERLOCATION +
                            " set " + DBAdapter.COL_NAME_GEN_ISACTIVE + " = 'N' " +
                            " where " + DBAdapter.COL_NAME_GEN_NAME + " = '" + name + "' " +
                            " AND " + DBAdapter.COL_NAME_BPARTNERLOCATION__BPARTNER_ID + " == " + bpID +
                            " AND " + DBAdapter.COL_NAME_GEN_ROWID + " <> " + id;
                    db.execUpdate(sql);
                }
                db.close();
            }
            catch (Exception ex) {
                AndiCarCrashReporter.sendCrash(ex);
            }
        }
        if (oldAppVersion <= 17101200) {
            //if secure backup is enabled set the method to GMail. Else use GDrive
            if (appPreferences.getBoolean(getString(R.string.pref_key_secure_backup_enabled), false)) {
                e.putString(getString(R.string.pref_key_secure_backup_destination), "1"); //"1" == GMail
            }
            else {
                e.putString(getString(R.string.pref_key_secure_backup_destination), "0"); //"0" == GDrive
            }
        }
        if (oldAppVersion <= 17102700) {
            //delete the old log files
            try {
                FileUtils.deleteDirectory(new File(ConstantValues.BASE_FOLDER + "/log/"));
            }
            catch (IOException e1) {
                Log.d("AndiCar", e1.getMessage(), e1);
            }
        }
        if (oldAppVersion <= 17102700) {
            //migrate the main screen zones
            e.putString(getString(R.string.pref_key_main_zone12_content), appPreferences.getString(getString(R.string.pref_key_main_zone11_content), "DNU"));
            e.putString(getString(R.string.pref_key_main_zone11_content), appPreferences.getString(getString(R.string.pref_key_main_zone10_content), "LGT"));
            e.putString(getString(R.string.pref_key_main_zone10_content), appPreferences.getString(getString(R.string.pref_key_main_zone9_content), "CEX"));
            e.putString(getString(R.string.pref_key_main_zone9_content), appPreferences.getString(getString(R.string.pref_key_main_zone8_content), "LEX"));
            e.putString(getString(R.string.pref_key_main_zone8_content), appPreferences.getString(getString(R.string.pref_key_main_zone7_content), "CFV"));
            e.putString(getString(R.string.pref_key_main_zone7_content), appPreferences.getString(getString(R.string.pref_key_main_zone6_content), "CFQ"));
            e.putString(getString(R.string.pref_key_main_zone6_content), appPreferences.getString(getString(R.string.pref_key_main_zone5_content), "LFU"));
            e.putString(getString(R.string.pref_key_main_zone5_content), appPreferences.getString(getString(R.string.pref_key_main_zone4_content), "CTR"));
            e.putString(getString(R.string.pref_key_main_zone4_content), appPreferences.getString(getString(R.string.pref_key_main_zone3_content), "LTR"));
            e.putString(getString(R.string.pref_key_main_zone3_content), appPreferences.getString(getString(R.string.pref_key_main_zone2_content), "CFE"));
            e.putString(getString(R.string.pref_key_main_zone2_content), appPreferences.getString(getString(R.string.pref_key_main_zone1_content), "STS"));
            e.putString(getString(R.string.pref_key_main_zone1_content), "CFP");
        }
        e.apply();
    }

    @SuppressLint("WrongConstant")
    private void initPreferences() {
        //set up the backup service
        if (!appPreferences.contains(appResources.getString(R.string.pref_key_backup_service_exec_hour))) {
            boolean isActive = true;

//            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            if (!FileUtils.isFileSystemAccessGranted(getApplicationContext())) {
                isActive = false;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(946753200778L);

            SharedPreferences.Editor editor = appPreferences.edit();
            editor.putInt(getString(R.string.pref_key_backup_service_exec_hour), cal.get(Calendar.HOUR_OF_DAY));
            editor.putInt(getString(R.string.pref_key_backup_service_exec_minute), cal.get(Calendar.MINUTE));
            editor.putString(getString(R.string.pref_key_backup_service_schedule_type), ConstantValues.BACKUP_SERVICE_DAILY);
            editor.putInt(getString(R.string.pref_key_backup_service_keep_last_backups_no), 3);
            editor.putBoolean(getString(R.string.pref_key_backup_service_enabled), isActive);
            editor.putString(getString(R.string.pref_key_backup_service_backup_days), "1111111");
            editor.putBoolean(getString(R.string.pref_key_backup_service_show_notification), true);
            editor.apply();
        }
    }
}
