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

package andicar.n.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import andicar.n.activity.CommonDetailActivity;
import andicar.n.activity.CommonListActivity;
import andicar.n.activity.fragment.BaseEditFragment;
import andicar.n.activity.fragment.GPSTrackControllerFragment;
import andicar.n.persistence.DBAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.Utils;
import andicar.n.utils.notification.AndiCarNotification;

/**
 * @author miki
 */
public class GPSTrackService extends Service {
    public static final String ACTION_PAUSE = "Pause";
    public static final String ACTION_RESUME = "Resume";
    public static final String ACTION_STOP = "Stop";

    public static final String GPS_TRACK_POINT__NORMAL = "NP";
    public static final String GPS_TRACK_POINT__PAUSE_END = "PEP";
    public static final String GPS_TRACK_POINT__PAUSE_START = "PSP";
    public static final int GPS_TRACK_SERVICE_STOPPED = 1;
    public static final int GPS_TRACK_SERVICE_PAUSED = 2;
    public static final int GPS_TRACK_SERVICE_RUNNING = 3;
    private static final String CSV_FORMAT = "csv";
    private static final String KML_FORMAT = "kml";//Keyhole Markup Language (http://en.wikipedia.org/wiki/KML)
    private static final String GPX_FORMAT = "gpx";//GPS eXchange Format (http://en.wikipedia.org/wiki/Gpx)
    private static final String GOP_FORMAT = "gop"; //geopoint coordinates. Used to draw the track on the map
    private static final String GPS_TRACK_POINT__START = "SP";
    private static final String LOG_TAG = "AndiCar";
    private final Calendar currentLocationDateTime = Calendar.getInstance();
    private final float[] fDistanceArray = new float[1];
    private final long mlStartTimeForMileage = System.currentTimeMillis();
    private final IBinder mBinder = new GPSTrackServiceBinder();
    //the current service status
    private int mGPSTrackServiceStatus = 1;
    private Bundle mArguments;
    private SharedPreferences mPreferences;
    private DBAdapter mDbAdapter = null;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private double dOldLocationLatitude = 0;
    private double dOldLocationLongitude = 0;
    private double dCurrentLocationLatitude = 0;
    private double dCurrentLocationLongitude = 0;
    private double dCurrentLocationAltitude = 0;
    private double dCurrentLocationBearing = 0;

    private double lastGoodLocationLatitude = 0;
    private double lastGoodLocationLongitude = 0;
    private double lastGoodLocationAltitude = 0;
    private double dCurrentAccuracy = 0;
    private float fSensorCurrentSpeed = 0f;
    private float fSensorMaxSpeed = 0f;
    private long lCurrentLocationTime = 0;
    private long lOldLocationTime = 0;
    private double dDistanceBetweenLocations = 0;
    private long gpsTrackId = 0;
    private FileWriter gpsTrackDetailGOPFileWriter = null;
    private FileWriter gpsTrackDetailCSVFileWriter = null;
    private File gpsTrackDetailCSVFile = null;
    private FileWriter gpsTrackDetailKMLFileWriter = null;
    private File gpsTrackDetailKMLFile = null;
    private FileWriter gpsTrackDetailGPXFileWriter = null;
    private File gpsTrackDetailGPXFile = null;
    /* tmp values */
    private String sName = null;
    private boolean isUseKML = false;
    private boolean isUseGPX = false;
    private boolean isUseCSV = false;
    //statistics
    private double dMinAccuracy = 9999;
    private double dAvgAccuracy = 0;
    private double dMaxAccuracy = 0;
    private double dMinAltitude = 99999;
    private double dMaxAltitude = 0;
    private long lStartTime = 0;
    private long lStopTime = 0;
    private double dTotalDistance = 0;
    private double dAvgSpeed = 0;
    private double dAvgMovingSpeed = 0;
    private long lTotalTime = 0;
    private long lTotalMovingTime = 0;
    private long lTotalPauseTime = 0;
    private long lCurrentPauseStartTime = 0;
    private long lCurrentPauseEndTime = 0;
    private long lCurrentPauseTime = 0;
    private boolean isFirstPoint = true;
    private boolean isFirstPointAfterResume = false;
    private long lFirstNonMovingTime = 0;
    private long lLastNonMovingTime = 0;
    private long lTotalNonMovingTime = 0;
    private int iMinAccuracy = 9999999;
    private double dTotalTrackPoints = 0;
    private double dTotalSkippedTrackPoints = 0;
    private double dTotalValidTrackPoints = 0;
    //    private double skippedPointPercentage = 0;
    private boolean isUseMetricUnits = true;
    private boolean isEnableDebugLog = false;
    private File debugLogFile;
    private long csvPointsCount = 0;
    private long kmlPointsCount = 0;
    private long gpxPointsCount = 0;
    private long gopPointsCount = 0;

    private static void logDebugInfo(File debugLogFile, String msg, @Nullable Throwable t) {
        Log.d(LOG_TAG, msg);

        try {
            FileWriter debugLogFileWriter = new FileWriter(debugLogFile, true);
            msg = Utils.getFormattedDateTime(System.currentTimeMillis(), true) + " -> " + msg;
            debugLogFileWriter.append("\n").append(msg).append("\n");
            if (t != null) {
                debugLogFileWriter.append(Utils.getStackTrace(t));
            }
            debugLogFileWriter.flush();
            debugLogFileWriter.close();
        }
        catch (IOException ignored) {
        }
    }

    @Override
    public void onCreate() {

        mPreferences = AndiCar.getDefaultSharedPreferences();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (mLocationManager == null || !mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, R.string.gps_track_service_gps_disabled_message, Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }

        mLocationListener = new AndiCarLocationListener();
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                logDebugInfo(debugLogFile, "onCreate() terminated due location permissions restrictions", null);
                Toast.makeText(this, R.string.error_069, Toast.LENGTH_LONG).show();
                return;
            }

//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            if (!FileUtils.isFileSystemAccessGranted(this)) {
                Toast.makeText(this, R.string.error_070, Toast.LENGTH_LONG).show();
                stopSelf();
                return;
            }

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    Long.parseLong(mPreferences.getString(AndiCar.getAppResources().getString(R.string.pref_key_gps_track_min_time), "0")) * 1000, 0, mLocationListener);
        }
        catch (Exception e) {
            Toast.makeText(this, R.string.error_068, Toast.LENGTH_LONG).show();
            stopSelf();
        }


        //send an event for statistics
//        if(mPreferences.getBoolean("SendUsageStatistics", true)) {
//            AndiCar application = (AndiCar) getApplication();
//            application.sendGAEvent("GPSTrackService", "ServiceStart", null);
//        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_PAUSE)) {
                setServiceStatus(GPSTrackService.GPS_TRACK_SERVICE_PAUSED);
            }
            else if (intent.getAction().equals(ACTION_RESUME)) {
                setServiceStatus(GPSTrackService.GPS_TRACK_SERVICE_RUNNING);
            }
            else if (intent.getAction().equals(ACTION_STOP)) {
                setServiceStatus(GPSTrackService.GPS_TRACK_SERVICE_STOPPED);
            }
        }
        else {
            if (intent == null) {
                Utils.showReportableErrorDialog(this, "GPS Track Service error", "Intent is null", null);
                stopSelf();
                return START_NOT_STICKY;
            }
            mArguments = intent.getExtras();
            if (mArguments == null) {
                Utils.showReportableErrorDialog(this, "GPS Track Service error", "No argument", null);
                stopSelf();
                return START_NOT_STICKY;
            }
            sName = mArguments.getString(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_NAME, null);

            String sUserComment = mArguments.getString(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_COMMENT, null);
            //enable extended logging for debug
            isEnableDebugLog = (sUserComment != null && sUserComment.trim().toLowerCase().contains("debug"));

            if (isEnableDebugLog) {
                FileUtils.createFolderIfNotExists(this, ConstantValues.LOG_FOLDER);
                debugLogFile = new File(ConstantValues.LOG_FOLDER + "gpsDBG" + System.currentTimeMillis() + ".log");
                logDebugInfo(debugLogFile, "onCreate() started", null);
            }

            String sTag = mArguments.getString(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_TAG, null);
            long mCarId = mArguments.getLong(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_CAR_ID,
                    mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_car_id), 1));
            long mDriverId = mArguments.getLong(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_DRIVER_ID,
                    mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_driver_id), 1));
            long mExpenseTypeId = mArguments.getLong(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_EXPENSE_TYPE_ID,
                    mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_mileage_last_selected_expense_type_id), 1));
            isUseKML = mPreferences.getBoolean(AndiCar.getAppResources().getString(R.string.pref_key_gps_track_is_use_kml), false);
            isUseGPX = mPreferences.getBoolean(AndiCar.getAppResources().getString(R.string.pref_key_gps_track_is_use_gpx), false);
            isUseCSV = mPreferences.getBoolean(AndiCar.getAppResources().getString(R.string.pref_key_gps_track_is_use_csv), false);
            iMinAccuracy = Integer.parseInt(mPreferences.getString(AndiCar.getAppResources().getString(R.string.pref_key_gps_track_min_accuracy), "20"));

            mDbAdapter = new DBAdapter(this);

            //create the master record
            //use direct table insert for increasing the speed of the DB operation
            ContentValues cvData = new ContentValues();
            cvData.put(DBAdapter.COL_NAME_GEN_NAME, sName);
            cvData.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, sUserComment);
            cvData.put(DBAdapter.COL_NAME_GPSTRACK__CAR_ID, mCarId);
            cvData.put(DBAdapter.COL_NAME_GPSTRACK__DRIVER_ID, mDriverId);
            cvData.put(DBAdapter.COL_NAME_GPSTRACK__EXPENSETYPE_ID, mExpenseTypeId);
            cvData.put(DBAdapter.COL_NAME_GPSTRACK__DATE, (System.currentTimeMillis() / 1000));
            if (sTag != null && sTag.length() > 0) {
                long mTagId;
                String selection = "UPPER (" + DBAdapter.COL_NAME_GEN_NAME + ") = ?";
                String[] selectionArgs = {sTag.toUpperCase(Locale.US)};
                Cursor c = mDbAdapter.query(DBAdapter.TABLE_NAME_TAG, DBAdapter.COL_LIST_GEN_ROWID_NAME, selection, selectionArgs, null);
                String tagIdStr = null;
                if (c.moveToFirst()) {
                    tagIdStr = c.getString(DBAdapter.COL_POS_GEN_ROWID);
                }
                c.close();
                if (tagIdStr != null && tagIdStr.length() > 0) {
                    mTagId = Long.parseLong(tagIdStr);
                    cvData.put(DBAdapter.COL_NAME_GPSTRACK__TAG_ID, mTagId);
                } else {
                    ContentValues tmpData = new ContentValues();
                    tmpData.put(DBAdapter.COL_NAME_GEN_NAME, sTag);
                    mTagId = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_TAG, tmpData);
                    if (mTagId >= 0) {
                        cvData.put(DBAdapter.COL_NAME_GPSTRACK__TAG_ID, mTagId);
                    }
                }
            } else {
                cvData.put(DBAdapter.COL_NAME_GPSTRACK__TAG_ID, (String) null);
            }

            gpsTrackId = mDbAdapter.createRecord(DBAdapter.TABLE_NAME_GPSTRACK, cvData);
            if (gpsTrackId < 0) {
                Utils.showReportableErrorDialog(this, getString(R.string.error_sorry), mDbAdapter.mErrorMessage, mDbAdapter.mException);
                stopSelf();
            }

            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "onCreate(): Track saved in DB gpsTrackId = " + gpsTrackId, null);
            }

            long lMileId = mDbAdapter.getIdByCode(DBAdapter.TABLE_NAME_UOM, "mi");
            long lCarUomId = mDbAdapter.getCarUOMLengthID(mCarId);
            isUseMetricUnits = lCarUomId != lMileId;

            //create the track detail file(s)
            try {
                createFiles();

                // Display a notification about starting the service.
                showNotification(AndiCarNotification.GPS_TRACKING_IN_PROGRESS_ID);
            } catch (IOException ex) {
                Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
                Utils.showNotReportableErrorDialog(getApplicationContext(), getString(R.string.error_034), ex.getMessage());
                if (isEnableDebugLog) {
                    logDebugInfo(debugLogFile, "File system error", ex);
                }
                stopSelf();
            }
            //close the database
            if (mDbAdapter != null) {
                mDbAdapter.close();
                mDbAdapter = null;
            }

            mGPSTrackServiceStatus = GPS_TRACK_SERVICE_RUNNING;
        }
        return START_STICKY;
    }

    private void createFiles() throws IOException {

        if (isEnableDebugLog) {
            logDebugInfo(debugLogFile, "createFiles() started", null);
        }

        String fileName = gpsTrackId + "_001";
        if (mDbAdapter == null) {
            mDbAdapter = new DBAdapter(this);
        }

        createGOPFile(fileName);
        if (isUseKML) {
            createKMLFile(fileName);
        }
        else {
            gpsTrackDetailKMLFile = null;
        }
        if (isUseGPX) {
            createGPXFile(fileName);
        }
        else {
            gpsTrackDetailGPXFile = null;
        }
        if (isUseCSV) {
            createCSVFile(fileName);
        }
        else {
            gpsTrackDetailCSVFile = null;
        }

        if (mDbAdapter != null) {
            mDbAdapter.close();
            mDbAdapter = null;
        }

        if (isEnableDebugLog) {
            logDebugInfo(debugLogFile, "createFiles() terminated", null);
        }
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(int what) {
        Notification notif = AndiCarNotification.showGPSTrackNotification(this, what);
        startForeground(what, notif);
        if (isEnableDebugLog) {
            logDebugInfo(debugLogFile, "Notification with id = " + what + " called from:\n" +
                    Arrays.toString(Thread.currentThread().getStackTrace()).replace(",", "\n"), null);
        }
    }

    private void createCSVFile(String fileName) throws IOException {
        gpsTrackDetailCSVFile = FileUtils.createGpsTrackDetailFile(CSV_FORMAT, fileName);
        gpsTrackDetailCSVFileWriter = new FileWriter(gpsTrackDetailCSVFile);

        //create the header
        gpsTrackDetailCSVFileWriter.append(DBAdapter.COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID)
                .append(",Accuracy").append(isUseMetricUnits ? " [m]" : " [yd]")
                .append(",Altitude").append(isUseMetricUnits ? " [m]" : " [yd]")
                .append(",Latitude")
                .append(",Longitude")
//                .append(",CalculatedSpeed").append(isUseMetricUnits ? " [km/h]" : " [mi/h]")
                .append(",SpeedFromSensor").append(isUseMetricUnits ? " [km/h]" : " [mi/h]")
//                .append(",SensorHasSpeed")
                .append(",Time").append(",Distance").append(isUseMetricUnits ? " [m]" : " [yd]")
                .append(",Bearing")
                .append(",TrackPointCount")
                .append(",InvalidTrackPointCount")
                .append(",IsValidPoint")
                .append("\n");

        ContentValues cvData = new ContentValues();
        cvData.put(DBAdapter.COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID, gpsTrackId);
        cvData.put(DBAdapter.COL_NAME_GPSTRACKDETAIL__FILEFORMAT, CSV_FORMAT);
        cvData.put(DBAdapter.COL_NAME_GPSTRACKDETAIL__FILE, gpsTrackDetailCSVFile.getAbsolutePath());
        mDbAdapter.createRecord(DBAdapter.TABLE_NAME_GPSTRACKDETAIL, cvData);
        gpsTrackDetailCSVFileWriter.flush();

        if (isEnableDebugLog) {
            logDebugInfo(debugLogFile, "createCSVFile: File created. gpsTrackDetailCSVFile = " + fileName, null);
        }

    }

    private void createGOPFile(String fileName) throws IOException {
        File gpsTrackDetailGOPFile = FileUtils.createGpsTrackDetailFile(GOP_FORMAT, fileName);
        gpsTrackDetailGOPFileWriter = new FileWriter(gpsTrackDetailGOPFile);

        //create the header
        gpsTrackDetailGOPFileWriter.append("LatitudeE6,LongitudeE6,PointType\n");

        ContentValues cvData = new ContentValues();
        cvData.put(DBAdapter.COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID, gpsTrackId);
        cvData.put(DBAdapter.COL_NAME_GPSTRACKDETAIL__FILEFORMAT, GOP_FORMAT);
        cvData.put(DBAdapter.COL_NAME_GPSTRACKDETAIL__FILE, gpsTrackDetailGOPFile.getAbsolutePath());
        mDbAdapter.createRecord(DBAdapter.TABLE_NAME_GPSTRACKDETAIL, cvData);

        gpsTrackDetailGOPFileWriter.flush();

        if (isEnableDebugLog) {
            logDebugInfo(debugLogFile, "createGOPFile: File created. gpsTrackDetailGOPFile = " + fileName, null);
        }
    }

    private void createKMLFile(String fileName) throws IOException {
        gpsTrackDetailKMLFile = FileUtils.createGpsTrackDetailFile(KML_FORMAT, fileName);
        gpsTrackDetailKMLFileWriter = new FileWriter(gpsTrackDetailKMLFile);

        ContentValues cvData = new ContentValues();
        cvData.put(DBAdapter.COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID, gpsTrackId);
        cvData.put(DBAdapter.COL_NAME_GPSTRACKDETAIL__FILEFORMAT, KML_FORMAT);
        cvData.put(DBAdapter.COL_NAME_GPSTRACKDETAIL__FILE, gpsTrackDetailKMLFile.getAbsolutePath());
        mDbAdapter.createRecord(DBAdapter.TABLE_NAME_GPSTRACKDETAIL, cvData);

        //initialize the file header

        gpsTrackDetailKMLFileWriter.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<kml xmlns=\"http://earth.google.com/kml/2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
                + "<Document>\n"
                + "<atom:author><atom:name>AndiCar</atom:name></atom:author>\n"
                + "<name><![CDATA[").append(sName).append("]]></name>\n")
                .append("<description><![CDATA[Recorded with <a href='http://www.andicar.org'>AndiCar</a>]]></description>\n")
                .append("<Style id=\"track\"><LineStyle><color>7f0000ff</color><width>4</width></LineStyle></Style>\n")
                .append("<Style id=\"sh_green-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/grn-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n")
                .append("<Style id=\"sh_red-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n")
                .append("<Style id=\"icon28\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/pal4/icon28.png</href></Icon><hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n")
                .append("<Style id=\"icon29\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/pal4/icon29.png</href></Icon><hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>");
        gpsTrackDetailKMLFileWriter.flush();

        if (isEnableDebugLog) {
            logDebugInfo(debugLogFile, "createKMLFile: File created. gpsTrackDetailKMLFile = " + fileName, null);
        }

    }

    private void createGPXFile(String fileName) throws IOException {
        gpsTrackDetailGPXFile = FileUtils.createGpsTrackDetailFile(GPX_FORMAT, fileName);
        gpsTrackDetailGPXFileWriter = new FileWriter(gpsTrackDetailGPXFile);

        ContentValues cvData = new ContentValues();
        cvData.put(DBAdapter.COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID, gpsTrackId);
        cvData.put(DBAdapter.COL_NAME_GPSTRACKDETAIL__FILEFORMAT, GPX_FORMAT);
        cvData.put(DBAdapter.COL_NAME_GPSTRACKDETAIL__FILE, gpsTrackDetailGPXFile.getAbsolutePath());
        mDbAdapter.createRecord(DBAdapter.TABLE_NAME_GPSTRACKDETAIL, cvData);

        gpsTrackDetailGPXFileWriter.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>\n"
                + "<?xml-stylesheet type=\"text/xsl\" href=\"details.xsl\"?>\n"
                + "<gpx\n"
                + "version=\"1.0\"\n"
                + "creator=\"AndiCar\"\n"
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "xmlns=\"http://www.topografix.com/GPX/1/0\"\n"
                + "xmlns:topografix=\"http://www.topografix.com/GPX/Private/TopoGrafix/0/1\" "
                + "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd http://www.topografix.com/GPX/Private/TopoGrafix/0/1 http://www.topografix.com/GPX/Private/TopoGrafix/0/1/topografix.xsd\">\n"
                + "<trk>\n" + "<name><![CDATA[Trip record for '").append(sName).append("']]></name>\n")
                .append("<desc><![CDATA[Recorded with <a href='http://www.andicar.org'>AndiCar</a><br>")
                .append(Utils.getFormattedDateTime(System.currentTimeMillis(), true)).append("]]></desc>\n").
                append("<topografix:color>c0c0c0</topografix:color>\n")
                .append("<trkseg>\n");
        gpsTrackDetailGPXFileWriter.flush();

        if (isEnableDebugLog) {
            logDebugInfo(debugLogFile, "createGPXFile: File created. gpsTrackDetailGPXFile = " + fileName, null);
        }

    }

    @Override
    public void onDestroy() {

        if (mLocationManager == null || mLocationListener == null) {
            return;
        }

        // Cancel the persistent notifications.
        if (isEnableDebugLog) {
            logDebugInfo(debugLogFile, "onDestroy() started", null);
        }

        if (android.support.v4.content.ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            logDebugInfo(debugLogFile, "onDestroy() terminated due permissions restrictions", null);
            Toast.makeText(GPSTrackService.this, R.string.error_069, Toast.LENGTH_LONG).show();
            return;
        }
        mLocationManager.removeUpdates(mLocationListener);
        mLocationManager = null;

        stopForeground(true);
        lCurrentPauseStartTime = 0;
        lCurrentPauseEndTime = 0;
        //update the statistics for the track
        //close the database
        if (mDbAdapter != null) {
            mDbAdapter.close();
            mDbAdapter = null;
        }
        closeFiles();

        if (isEnableDebugLog) {
            logDebugInfo(debugLogFile, "onDestroy() terminated", null);
        }
    }

    //	private void closeFiles(boolean isLastFile) {
    private void closeFiles() {
        try {
            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "closeFiles started", null);
            }

            if (gpsTrackDetailCSVFileWriter != null) {
                gpsTrackDetailCSVFileWriter.flush();
                gpsTrackDetailCSVFileWriter.close();
            }
            if (gpsTrackDetailGOPFileWriter != null) {
                gpsTrackDetailGOPFileWriter.flush();
                gpsTrackDetailGOPFileWriter.close();
            }
            if (gpsTrackDetailKMLFileWriter != null) {
                appendKMLFooter();
                gpsTrackDetailKMLFileWriter.flush();
                gpsTrackDetailKMLFileWriter.close();
            }
            if (gpsTrackDetailGPXFileWriter != null) {
                appendGPXFooter();
                gpsTrackDetailGPXFileWriter.flush();
                gpsTrackDetailGPXFileWriter.close();
            }
            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "closeFiles terminated", null);
            }
        }
        catch (IOException ex) {
            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "closeFiles exception: " + ex.getMessage(), ex);
            }
            Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
            Utils.showNotReportableErrorDialog(this, getString(R.string.error_034), ex.getMessage());
//            showNotification(AndiCarNotification.NOTIFICATION_FILESYSTEM_ERROR_ID, true);
        }
    }

    private void appendKMLFooter(/*boolean isLastFile*/) {
        if (gpsTrackDetailKMLFileWriter == null) {
            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "appendKMLFooter: File writer is NULL!", null);
            }
            return;
        }
        try {
            String pointName;
            String pointStyle;
            pointName = "End trip";
            pointStyle = "#sh_red-circle";

            String footerTxt = "";
            if (dTotalValidTrackPoints == 0) {
                dCurrentLocationLongitude = 0;
                dCurrentLocationLatitude = 0;
                dCurrentLocationAltitude = 0;
                appendKMLStartPoint(); //for kml file consistency. coordinates is 0,0,0
            }

            footerTxt = footerTxt + "\n</coordinates>\n" + "</LineString>\n" + "</MultiGeometry>\n" + "</Placemark>\n" + "<Placemark>\n" + "<name><![CDATA["
                    + pointName + "]]></name>\n";
            footerTxt = footerTxt + "<description>\n<![CDATA[End of trip '" + sName + "'<br>" + Utils.getFormattedDateTime(System.currentTimeMillis(), true);
            try {
                footerTxt = footerTxt + "\n<br>Start at: " +
                        Utils.getFormattedDateTime(lStartTime * 1000, false);
            }
            catch (Exception ignored) {
            }
            try {
                footerTxt = footerTxt + "\n<br>End at: " +
                        Utils.getFormattedDateTime(lStopTime * 1000, false);
            }
            catch (Exception ignored) {
            }
            footerTxt = footerTxt + "\n<hr>";
            try {
                footerTxt = footerTxt + "\nDistance: " + (BigDecimal.valueOf(dTotalDistance).setScale(2, BigDecimal.ROUND_HALF_DOWN).toString())
                        + (isUseMetricUnits ? " km" : " mi");
            }
            catch (Exception ignored) {
            }
            try {
                footerTxt = footerTxt + "\n<br>Max. speed: " + (BigDecimal.valueOf(fSensorMaxSpeed).setScale(2, BigDecimal.ROUND_HALF_DOWN).toString())
                        + (isUseMetricUnits ? " km/h" : " mi/h");
            }
            catch (Exception e) {
                if (isEnableDebugLog) {
                    logDebugInfo(debugLogFile, "appendKMLFooter NaxSpeed: \n" + e.getMessage(), e);
                }
            }
            try {
                footerTxt = footerTxt + "\n<br>Avg. speed: " + (BigDecimal.valueOf(dAvgSpeed).setScale(2, BigDecimal.ROUND_HALF_DOWN).toString())
                        + (isUseMetricUnits ? " km/h" : " mi/h");
            }
            catch (Exception ignored) {
            }
            try {
                footerTxt = footerTxt + "\n<br>Avg. moving speed: "
                        + (BigDecimal.valueOf(dAvgMovingSpeed).setScale(2, BigDecimal.ROUND_HALF_DOWN).toString()) + (isUseMetricUnits ? " km/h" : " mi/h");
            }
            catch (Exception ignored) {
            }
            try {
                footerTxt = footerTxt + "\n<br>Total time: " + Utils.getTimeString(lTotalTime);
            }
            catch (Exception ignored) {
            }
            try {
                footerTxt = footerTxt + "\n<br>Total moving time: " + Utils.getTimeString(lTotalMovingTime);
            }
            catch (Exception ignored) {
            }
            try {
                footerTxt = footerTxt + "\n<br>Waiting time: " + Utils.getTimeString(lTotalTime - lTotalMovingTime - lTotalPauseTime);
            }
            catch (Exception ignored) {
            }
            try {
                footerTxt = footerTxt + "\n<br>Pause: " + Utils.getTimeString(lTotalPauseTime);
            }
            catch (Exception ignored) {
            }
            footerTxt = footerTxt + "\n<hr>" + "\nRecorded with <a href='http://www.andicar.org'><b>AndiCar</b></a>"
                    + "]]>\n</description>\n" + "<styleUrl>" + pointStyle + "</styleUrl>\n" + "<Point>\n" + "<coordinates>"
                    + lastGoodLocationLongitude + "," + lastGoodLocationLatitude + "," + lastGoodLocationAltitude + "</coordinates>\n" + "</Point>\n"
                    + "</Placemark>\n"
                    + "</Document>\n" + "</kml>\n";
            gpsTrackDetailKMLFileWriter.append(footerTxt);

            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "appendKMLFooter: Footer added", null);
            }

        }
        catch (IOException e) {
            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "appendKMLFooter: Exception = " + e.getMessage(), e);
            }
        }
    }

    private void appendGPXFooter() {
        if (gpsTrackDetailGPXFileWriter == null) {
            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "appendGPXFooter: File writer is NULL!", null);
            }
            return;
        }
        try {
            gpsTrackDetailGPXFileWriter.append("</trkseg>\n" + "</trk>\n" + "</gpx>");

            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "appendGPXFooter: Footer added", null);
            }
        }
        catch (IOException e) {
            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "appendGPXFooter: Exception = " + e.getMessage(), e);
            }
        }
    }

    private void appendKMLStartPoint() {
        if (gpsTrackDetailKMLFileWriter == null) {
            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "appendKMLStartPoint: File writer is NULL!", null);
            }
            return;
        }
        try {
            String pointName;
            String pointStyle;
            String pointDescription;

            pointName = "Start trip";
            pointStyle = "#sh_green-circle";
            pointDescription = "Start of trip '" + sName + "'<br>"
                    + Utils.getFormattedDateTime(lStartTime, false);

            gpsTrackDetailKMLFileWriter.append("<Placemark>\n" + "<name><![CDATA[").append(pointName).append("]]></name>\n").append("<description><![CDATA[")
                    .append(pointDescription).append("]]></description>\n").append("<styleUrl>").append(pointStyle).append("</styleUrl>\n").append("<Point>\n").append("<coordinates>")
                    .append(Double.toString(dCurrentLocationLongitude)).append(",")
                    .append(Double.toString(dCurrentLocationLatitude)).append(",")
                    .append(Double.toString(dCurrentLocationAltitude)).append("</coordinates>\n").append("</Point>\n").append("</Placemark>\n").append("<Placemark>\n")
                    .append("<description><![CDATA[]]></description>\n")
                    .append("<styleUrl>#track</styleUrl>\n").append("<MultiGeometry>\n").append("<LineString>\n").append("<coordinates>\n");

            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "appendKMLStartPoint: Start point added", null);
            }

        }
        catch (IOException e) {
            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "appendKMLStartPoint: Exception = " + e.getMessage(), e);
            }
        }
    }

    /**
     * When binding to the service, we return an interface to our messenger for
     * sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
//		return mMessenger.getBinder();
        return mBinder;
    }

    /**
     * Create a pause placemark
     */
    private void appendKMLPausePoint() {
        if (gpsTrackDetailKMLFileWriter == null) {
            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "appendKMLPausePoint: File writer is NULL!", null);
            }
            return;
        }
        try {

            String kmlTxt;
            if (dDistanceBetweenLocations <= 10) // if the distance between the pause starting point and ending point is less than 10m create a single pause point
            {
                kmlTxt = "\n</coordinates>\n" + "</LineString>\n" + "</MultiGeometry>\n" + "</Placemark>\n"

                        + "<Placemark>\n" + "<name><![CDATA[Pause]]></name>\n" + "<description>\n<![CDATA[Pause for "
                        + Utils.getTimeString(lCurrentPauseTime / 1000) + "\n<br>From: "
                        + Utils.getFormattedDateTime(lCurrentPauseStartTime, false) + "\n<br>To: "
                        + Utils.getFormattedDateTime(lCurrentPauseEndTime, false) + "]]>\n</description>\n"
                        + "<styleUrl>#icon29</styleUrl>\n" + "<Point>\n" + "<coordinates>" + lastGoodLocationLongitude + "," + lastGoodLocationLatitude + ","
                        + lastGoodLocationAltitude + "</coordinates>\n" + "</Point>\n" + "</Placemark>\n"
                        + "<Placemark>\n" + "<name><![CDATA[Tracking resumed]]></name>\n"
                        + "<description><![CDATA[]]></description>\n" + "<styleUrl>#track</styleUrl>\n" + "<MultiGeometry>\n" + "<LineString>\n"
                        + "<coordinates>\n";

                gpsTrackDetailKMLFileWriter.append(kmlTxt);
            }
            else { //create two points for pause (Start and End)
                kmlTxt = "\n</coordinates>\n" + "</LineString>\n" + "</MultiGeometry>\n" + "</Placemark>\n"

                        + "<Placemark>\n" + "<name><![CDATA[Pause start]]></name>\n" + "<description>\n<![CDATA[Pause start at: "
                        + Utils.getFormattedDateTime(lCurrentPauseStartTime, false) + "]]>\n</description>\n"
                        + "<styleUrl>#icon29</styleUrl>\n" + "<Point>\n" + "<coordinates>" + lastGoodLocationLongitude + "," + lastGoodLocationLatitude + ","
                        + lastGoodLocationAltitude + "</coordinates>\n" + "</Point>\n" + "</Placemark>\n"

                        + "<Placemark>\n" + "<name><![CDATA[Pause end]]></name>\n" + "<description>\n<![CDATA[" + "\nPause duration: "
                        + Utils.getTimeString(lCurrentPauseTime / 1000) + "\n\n<br><br>From: "
                        + Utils.getFormattedDateTime(lCurrentPauseStartTime, false) + "\n<br>To: "
                        + Utils.getFormattedDateTime(lCurrentPauseEndTime, false) + "]]>\n</description>\n"
                        + "<styleUrl>#icon29</styleUrl>\n" + "<Point>\n" + "<coordinates>" + dCurrentLocationLongitude + "," + dCurrentLocationLatitude + ","
                        + dCurrentLocationAltitude + "</coordinates>\n" + "</Point>\n" + "</Placemark>\n"
                        + "<Placemark>\n" + "<name><![CDATA[Tracking resumed]]></name>\n"
                        + "<description><![CDATA[]]></description>\n" + "<styleUrl>#track</styleUrl>\n" + "<MultiGeometry>\n" + "<LineString>\n"
                        + "<coordinates>\n";

                gpsTrackDetailKMLFileWriter.append(kmlTxt);
            }
            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "appendKMLPausePoint: Point added", null);
            }

        }
        catch (IOException e) {
            if (isEnableDebugLog) {
                logDebugInfo(debugLogFile, "appendKMLPausePoint: Exception = " + e.getMessage(), e);
            }
        }
    }

    private void appendCSVTrackPoint(boolean isValid) throws IOException {
        gpsTrackDetailCSVFileWriter.append(Long.toString(gpsTrackId)).append(",")
                .append(isUseMetricUnits ? Double.toString(dCurrentAccuracy) : Double.toString(dCurrentAccuracy * 1.093613)).append(",")
                .append(isUseMetricUnits ? Double.toString(dCurrentLocationAltitude) : Double.toString(dCurrentLocationAltitude * 1.093613)).append(",")
                .append(Double.toString(dCurrentLocationLatitude)).append(",").append(Double.toString(dCurrentLocationLongitude)).append(",")
                .append(isUseMetricUnits ? Float.toString(fSensorCurrentSpeed * 3.6f) : Float.toString(fSensorCurrentSpeed * 2.2369f)).append(",")
                .append(Long.toString(lCurrentLocationTime)).append(",")
                .append(isUseMetricUnits ? Double.toString(dDistanceBetweenLocations) : Double.toString(dDistanceBetweenLocations * 1.093613)).append(",")
                .append(Double.toString(dCurrentLocationBearing)).append(",").append(Double.toString(dTotalTrackPoints)).append(",")
                .append(Double.toString(dTotalSkippedTrackPoints)).append(",").append(isValid ? "Yes" : "No").append("\n");

        csvPointsCount++;
        if (csvPointsCount == 20) {
            gpsTrackDetailCSVFileWriter.flush();
            csvPointsCount = 0;
        }

    }

    private void appendGPXTrackPoint() throws IOException {
        double tmpSpeed;
        BigDecimal speed;
        if (!isUseMetricUnits) {
            tmpSpeed = fSensorCurrentSpeed * 2.23693; //m/s to mi/h
        }
        else { // only m/s need to be converted to km/h
            tmpSpeed = fSensorCurrentSpeed * 3.6; //m/s to mi/h
        }

        try {
            speed = BigDecimal.valueOf(tmpSpeed).setScale(1, BigDecimal.ROUND_HALF_DOWN);
        }
        catch (NumberFormatException e) {
            return;
        }

        currentLocationDateTime.setTimeInMillis(lCurrentLocationTime);
        @SuppressLint("WrongConstant") String currentLocationDateTimeGPXStr = currentLocationDateTime.get(Calendar.YEAR) + "-" + Utils.pad(currentLocationDateTime.get(Calendar.MONTH) + 1, 2) + "-"
                + Utils.pad(currentLocationDateTime.get(Calendar.DAY_OF_MONTH), 2) + "T" + Utils.pad(currentLocationDateTime.get(Calendar.HOUR_OF_DAY), 2)
                + ":" + Utils.pad(currentLocationDateTime.get(Calendar.MINUTE), 2) + ":" + Utils.pad(currentLocationDateTime.get(Calendar.SECOND), 2) + "Z";
        gpsTrackDetailGPXFileWriter.append("<trkpt lat=\"").append(Double.toString(dCurrentLocationLatitude)).append("\" lon=\"")
                .append(Double.toString(dCurrentLocationLongitude)).append("\">\n").append("<ele>").append(Double.toString(dCurrentLocationAltitude)).append("</ele>\n")
                .append("<time>").append(currentLocationDateTimeGPXStr).append("</time>\n").append("<cmt>Current speed: ").append(speed.toPlainString())
                .append(isUseMetricUnits ? " km/h" : " mi/h").append("</cmt>\n").append("</trkpt>\n");

        gpxPointsCount++;
        if (gpxPointsCount == 20) {
            gpsTrackDetailGPXFileWriter.flush();
            gpxPointsCount = 0;
        }

    }

    private void appendKMLTrackPoint() throws IOException {
        gpsTrackDetailKMLFileWriter.append(Double.toString(dCurrentLocationLongitude)).append(",")
                .append(Double.toString(dCurrentLocationLatitude))
                .append(",").append(Double.toString(dCurrentLocationAltitude)).append(" \n");

        kmlPointsCount++;
        if (kmlPointsCount == 20) {
            gpsTrackDetailKMLFileWriter.flush();
            kmlPointsCount = 0;
        }
    }

    public int getServiceStatus() {
        return mGPSTrackServiceStatus;
    }

    /**
     * Set the status of the service
     *
     * @param status the new status.
     */
    public void setServiceStatus(int status) {
        if (android.support.v4.content.ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            logDebugInfo(debugLogFile, "handleMessage() terminated due permissions restrictions", null);
            Toast.makeText(this, R.string.error_069, Toast.LENGTH_LONG).show();
            mGPSTrackServiceStatus = GPS_TRACK_SERVICE_STOPPED;
        }
        mGPSTrackServiceStatus = status;
        switch (status) {
            case GPS_TRACK_SERVICE_PAUSED:
                mLocationManager.removeUpdates(mLocationListener);
                showNotification(AndiCarNotification.GPS_TRACKING_PAUSED_ID);
                try {
                    appendGOPTrackPoint(GPS_TRACK_POINT__PAUSE_START); //Pause Start Point
                }
                catch (IOException ignored) {
                }
                lCurrentPauseStartTime = lCurrentLocationTime;
                if (lFirstNonMovingTime != 0) {
                    lTotalNonMovingTime = lTotalNonMovingTime + (lLastNonMovingTime - lFirstNonMovingTime);
                    //reset
                    lLastNonMovingTime = 0;
                    lFirstNonMovingTime = 0;
                }
                break;
            case GPS_TRACK_SERVICE_RUNNING:
                isFirstPointAfterResume = true;
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        Long.parseLong(mPreferences.getString(AndiCar.getAppResources().getString(R.string.pref_key_gps_track_min_time), "0")) * 1000, 0, mLocationListener);

                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showNotification(AndiCarNotification.GPS_DISABLED_ID);
                }
                else {
                    showNotification(AndiCarNotification.GPS_TRACKING_IN_PROGRESS_ID);
                }
                break;
            case GPS_TRACK_SERVICE_STOPPED:
                updateStatistics();
                if (mArguments.getBoolean(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_CREATE_MILEAGE, false)) {
                    Intent intent = new Intent(this, CommonDetailActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_MILEAGE);
                    intent.putExtra(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_TRACK_TO_MILEAGE);
                    intent.putExtra(BaseEditFragment.RECORD_ID_KEY, -1L);
                    intent.putExtra(GPSTrackControllerFragment.GPS_TRACK_ID_FOR_MILEAGE, gpsTrackId);
                    intent.putExtra(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_START_TIME_FOR_MILEAGE, mlStartTimeForMileage);
                    intent.putExtra(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_INDEX_FOR_MILEAGE,
                            mArguments.getString(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_INDEX_FOR_MILEAGE));
                    startActivity(intent);
                }
                stopSelf();
                break;
            default:
                Utils.showReportableErrorDialog(this, getString(R.string.error_sorry), "Unknown GPSTrackStatus: " + status, null);
        }
    }

    public void setIsCreateMileage(boolean isCreateMileage, String indexForMileage) {
        mArguments.putBoolean(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_CREATE_MILEAGE, isCreateMileage);
        mArguments.putString(GPSTrackControllerFragment.GPS_TRACK_ARGUMENT_INDEX_FOR_MILEAGE, indexForMileage);
    }

    private void appendGOPTrackPoint(String pointType) throws IOException {

        String gopLine = (int) (dCurrentLocationLatitude * 1E6) + "," + (int) (dCurrentLocationLongitude * 1E6) + "," + pointType;
        if (pointType.equals(GPS_TRACK_POINT__PAUSE_START)) {
            gopLine += "," + lCurrentLocationTime;
        }
        else if (pointType.equals(GPS_TRACK_POINT__PAUSE_END)) {
            gopLine += "," + lCurrentLocationTime + "," + lCurrentPauseTime;
        }

        gpsTrackDetailGOPFileWriter.append(gopLine).append("\n");

        gopPointsCount++;
        if (gopPointsCount == 20) {
            gpsTrackDetailGOPFileWriter.flush();
            gopPointsCount = 0;
        }
    }

    private void updateStatistics() {

        if (isEnableDebugLog) {
            logDebugInfo(debugLogFile, "updateStatistics() started", null);
        }

        lStartTime = lStartTime / 1000; //convert to second
        lStopTime = lStopTime / 1000; //convert to second
        lTotalTime = lStopTime - lStartTime;

        if (lLastNonMovingTime != 0 && lFirstNonMovingTime != 0) {
            lTotalNonMovingTime = lTotalNonMovingTime + (lLastNonMovingTime - lFirstNonMovingTime);
        }

        if (lCurrentPauseStartTime > 0) {
            lTotalPauseTime = lTotalPauseTime + (lCurrentLocationTime - lCurrentPauseStartTime);
        }

        lTotalNonMovingTime = lTotalNonMovingTime / 1000; //convert to second
        lTotalPauseTime = lTotalPauseTime / 1000; //convert to second

        lTotalMovingTime = lTotalTime - lTotalPauseTime - lTotalNonMovingTime;

        if (dTotalValidTrackPoints != 0)
        //at this moment dAvgAccuracy = SUM(CurrentAccuracy)
        {
            dAvgAccuracy = dAvgAccuracy / dTotalValidTrackPoints;
        }
        else {
            dAvgAccuracy = 0;
        }

        if (lStopTime - lStartTime != 0) {
            dAvgSpeed = dTotalDistance / (lTotalTime - lTotalPauseTime); // m/s
        }
        else {
            dAvgSpeed = 0;
        }

        if (lTotalMovingTime != 0) {
            dAvgMovingSpeed = dTotalDistance / lTotalMovingTime; // m/s
        }
        else {
            dAvgMovingSpeed = 0;
        }
        if (!isUseMetricUnits) {
            dMinAccuracy = dMinAccuracy * 1.093613; //m to yd
            dMaxAccuracy = dMaxAccuracy * 1.093613; //m to yd
            dAvgAccuracy = dAvgAccuracy * 1.093613; //m to yd
            dMinAltitude = dMinAltitude * 1.093613; //m to yd
            dMaxAltitude = dMaxAltitude * 1.093613; //m to yd
            dTotalDistance = dTotalDistance * 0.000621371; //m to mi
            dAvgSpeed = dAvgSpeed * 2.23693; //m/s to mi/h
            dAvgMovingSpeed = dAvgMovingSpeed * 2.23693; //m/s to mi/h
//            dCalculatedMaxSpeed = dCalculatedMaxSpeed * 2.23693;
            fSensorMaxSpeed = fSensorMaxSpeed * 2.23693f;
        }
        else { // only m/s need to be converted to km/h
            dTotalDistance = dTotalDistance * 0.001; //m to km
            dAvgSpeed = dAvgSpeed * 3.6; //m/s to km/h
            dAvgMovingSpeed = dAvgMovingSpeed * 3.6; //m/s to km/h
//            dCalculatedMaxSpeed = dCalculatedMaxSpeed * 3.6;
            fSensorMaxSpeed = fSensorMaxSpeed * 3.6f;
        }

        ContentValues cvData = new ContentValues();
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__MINACCURACY, (Math.round(dMinAccuracy * 100) * 1d) / 100); //round to 2 decimals
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__MAXACCURACY, (Math.round(dMaxAccuracy * 100) * 1d) / 100);
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__AVGACCURACY, (Math.round(dAvgAccuracy * 100) * 1d) / 100);
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__MINALTITUDE, (Math.round(dMinAltitude * 100) * 1d) / 100);
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__MAXALTITUDE, (Math.round(dMaxAltitude * 100) * 1d) / 100);
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__TOTALTIME, lTotalTime);
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__MOVINGTIME, lTotalMovingTime);
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__DISTANCE, (Math.round(dTotalDistance * 100) * 1d) / 100);
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__AVGSPEED, (Math.round(dAvgSpeed * 100) * 1d) / 100);
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__AVGMOVINGSPEED, (Math.round(dAvgMovingSpeed * 100) * 1d) / 100);
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__MAXSPEED, (Math.round(fSensorMaxSpeed * 100) * 1f) / 100);
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__TOTALTRACKPOINTS, dTotalTrackPoints);
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__INVALIDTRACKPOINTS, dTotalSkippedTrackPoints);
        cvData.put(DBAdapter.COL_NAME_GPSTRACK__TOTALPAUSETIME, lTotalPauseTime);

        if (mDbAdapter == null) {
            mDbAdapter = new DBAdapter(this);
        }
        mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_GPSTRACK, gpsTrackId, cvData);

        if (isEnableDebugLog) {
            logDebugInfo(debugLogFile, "updateStatistics() terminated", null);
        }
    }

    public Bundle getArguments() {
        return mArguments;
    }

    private class AndiCarLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {

            boolean isValid = true;
            double gopDistance = 0;

//            if (gpsTrackDetailCSVFileWriter == null) {
//                if (isEnableDebugLog) {
//                    logDebugInfo(debugLogFile, "onLocationChanged: Error: gpsTrackDetailCSVFileWriter == null", null);
//                }
//
//                Toast.makeText(GPSTrackService.this, "No File Writer!", Toast.LENGTH_LONG).show();
//                stopSelf();
//                return;
//            }
            //            Log.w("GPSTrackService", "onLocationChanged: dTotalTrackPoints = " + dTotalTrackPoints);

            if (loc == null) {
                if (isEnableDebugLog) {
                    logDebugInfo(debugLogFile, "onLocationChanged: Error: loc == null", null);
                }
                return;
            }

            try {
                dTotalTrackPoints++;
                dCurrentLocationLatitude = loc.getLatitude();
                dCurrentLocationLongitude = loc.getLongitude();
                dCurrentLocationAltitude = loc.getAltitude();
                lCurrentLocationTime = loc.getTime();
                dCurrentAccuracy = loc.getAccuracy();
                dCurrentLocationBearing = loc.getBearing();
                fSensorCurrentSpeed = loc.getSpeed();

                if (isFirstPoint) {
                    lStartTime = lCurrentLocationTime;
                    lOldLocationTime = lCurrentLocationTime;
                }

                if (dCurrentAccuracy > iMinAccuracy) {
                    isValid = false;
                    if (lCurrentLocationTime - lStartTime > 60000) { //leave time for GPS initialization (1 min)
                        dTotalSkippedTrackPoints++;
                    }
//                    skippedPointPercentage = (dTotalSkippedTrackPoints / dTotalTrackPoints) * 100;
                }

                if (isValid) {
                    dTotalValidTrackPoints++;
                    if (isFirstPoint) {
                        //the first valid location => write the starting point
                        if (gpsTrackDetailKMLFileWriter != null) { //this is the first track file (multiple track file can be used)
                            appendKMLStartPoint();
                        }
                        appendGOPTrackPoint(GPS_TRACK_POINT__START); //Start Point
                        isFirstPoint = false;
                    }
                    else {
                        //get the distance between the current and previous location
                        Location.distanceBetween(dOldLocationLatitude, dOldLocationLongitude, dCurrentLocationLatitude, dCurrentLocationLongitude,
                                fDistanceArray);

                        dDistanceBetweenLocations = fDistanceArray[0];
                    }
                }


                if (gpsTrackDetailCSVFileWriter != null) {
                    appendCSVTrackPoint(isValid);
                }

                if (!isValid) {
                    return;
                }

                //statistics
                if (isFirstPointAfterResume) {
                    lCurrentPauseEndTime = lCurrentLocationTime;
                    lCurrentPauseTime = lCurrentPauseEndTime - lCurrentPauseStartTime;
                    lTotalPauseTime = lTotalPauseTime + lCurrentPauseTime;

                    appendGOPTrackPoint(GPS_TRACK_POINT__PAUSE_END); //Pause End Point

                    if (gpsTrackDetailKMLFileWriter != null) {
                        appendKMLPausePoint();
                    }

                    lCurrentPauseStartTime = 0;
                    lCurrentPauseEndTime = 0;
                    isFirstPointAfterResume = false;
                }
                else {
                    //for drawing on the map add only a minimum 3 m distanced point from the previous point - performance reason
                    gopDistance = gopDistance + dDistanceBetweenLocations;
                    if (gopDistance >= 3) {
                        appendGOPTrackPoint(GPS_TRACK_POINT__NORMAL); //Normal Point
                    }

                    //non moving time
                    if (fSensorCurrentSpeed == 0f) {
                        if (lFirstNonMovingTime == 0) {
                            lFirstNonMovingTime = lCurrentLocationTime;
                        }
                        lLastNonMovingTime = lCurrentLocationTime;
                    }
                    else { //currentSpeed > 0
                        if (lFirstNonMovingTime != 0) {
                            lTotalNonMovingTime = lTotalNonMovingTime + (lLastNonMovingTime - lFirstNonMovingTime);
                            //reset
                            lLastNonMovingTime = 0;
                            lFirstNonMovingTime = 0;
                        }
                    }
                }

                if (dCurrentAccuracy < dMinAccuracy) {
                    dMinAccuracy = dCurrentAccuracy;
                }
                if (dCurrentAccuracy > dMaxAccuracy) {
                    dMaxAccuracy = dCurrentAccuracy;
                }

                //at the end of the tracking fAvgAccuracy will be fAvgAccuracy / iTrackPointCount
                dAvgAccuracy = dAvgAccuracy + dCurrentAccuracy;

                if (dCurrentLocationAltitude < dMinAltitude) {
                    dMinAltitude = dCurrentLocationAltitude;
                }
                if (dCurrentLocationAltitude > dMaxAltitude) {
                    dMaxAltitude = dCurrentLocationAltitude;
                }

                if (fSensorCurrentSpeed > fSensorMaxSpeed) {
                    if (isEnableDebugLog) {
                        logDebugInfo(debugLogFile,
                                "onLocationChanged: Max speed changed from " + fSensorMaxSpeed + " to " + fSensorCurrentSpeed + "\n" +
                                        "Details:" +
                                        "\nTrack point#: " + dTotalTrackPoints +
                                        "\nlat/long: " + dCurrentLocationLatitude + "/" + dCurrentLocationLongitude +
                                        "\ndDistanceBetweenLocations: " + dDistanceBetweenLocations +
                                        "\nlCurrentLocationTime: " + lCurrentLocationTime +
                                        "\nlOldLocationTime: " + lOldLocationTime +
                                        "\nSpeed from sensor: " + fSensorCurrentSpeed
                                , null);
                    }
                    fSensorMaxSpeed = fSensorCurrentSpeed;
                }

                if (gpsTrackDetailKMLFileWriter != null && dDistanceBetweenLocations != 0) {
                    appendKMLTrackPoint();
                }

                if (gpsTrackDetailGPXFileWriter != null && dDistanceBetweenLocations != 0) {
                    appendGPXTrackPoint();
                }

                dOldLocationLatitude = dCurrentLocationLatitude;
                dOldLocationLongitude = dCurrentLocationLongitude;
                lastGoodLocationLatitude = dCurrentLocationLatitude;
                lastGoodLocationLongitude = dCurrentLocationLongitude;
                lastGoodLocationAltitude = dCurrentLocationAltitude;
                lOldLocationTime = lCurrentLocationTime;
                dTotalDistance = dTotalDistance + dDistanceBetweenLocations;
                //set the stop time on each location change => the last will be the final lTotalTimeStop
                lStopTime = lCurrentLocationTime;

            }
            catch (IOException ex) {
                if (isEnableDebugLog) {
                    logDebugInfo(debugLogFile, "onLocationChanged: Exception: " + ex.getMessage(), ex);
                }

                Logger.getLogger(GPSTrackService.class.getName()).log(Level.SEVERE, null, ex);
                Toast.makeText(GPSTrackService.this, "File error!\n" + ex.getMessage(), Toast.LENGTH_LONG).show();
                stopSelf();
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
//            if (provider.equals(LocationManager.GPS_PROVIDER)) {
//                if (status == LocationProvider.OUT_OF_SERVICE) {
//                    showNotification(AndiCarNotification.GPS_OUT_OF_SERVICE_ID);
//                }
//                if (status == LocationProvider.AVAILABLE) {
//                    showNotification(AndiCarNotification.GPS_TRACKING_IN_PROGRESS_ID);
//                }
//            }
        }

        @Override
        public void onProviderEnabled(String provider) {
//            if (lCurrentPauseStartTime > 0) //tracking in pause
            if (mGPSTrackServiceStatus == GPS_TRACK_SERVICE_PAUSED) //tracking in pause
            {
                return;
            }
            showNotification(AndiCarNotification.GPS_TRACKING_IN_PROGRESS_ID);
        }

        @Override
        public void onProviderDisabled(String provider) {
//            if (lCurrentPauseStartTime > 0) //tracking in pause
            if (mGPSTrackServiceStatus == GPS_TRACK_SERVICE_PAUSED) //tracking in pause
            {
                return;
            }

            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                showNotification(AndiCarNotification.GPS_DISABLED_ID);
            }
        }
    }

    public class GPSTrackServiceBinder extends Binder {
        public GPSTrackService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GPSTrackService.this;
        }
    }
}
