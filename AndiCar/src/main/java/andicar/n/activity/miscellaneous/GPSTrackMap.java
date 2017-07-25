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

package andicar.n.activity.miscellaneous;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.andicar2.activity.R;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import andicar.n.persistence.DBAdapter;
import andicar.n.service.GPSTrackService;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.Utils;

public class GPSTrackMap extends FragmentActivity {

    public static final String GPS_TRACK_ID = "gpsTrackId";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private long mTrackId;
    private PolylineOptions mTrackLine = null;
    private LatLngBounds.Builder mTrackBoundBuilder = null;
    private Resources mResource = null;
    private String mErrMsg = "Unknown error";

    private String mInfoName = "";
    private String mInfoDateTimeStart;
    private String mInfoDateTimeEnd;
    private String mCarUOMLengthCode;
    private long mInfoTotalTime;
    private long mInfoMovingTime;
    private long mInfoNonMovingTime;
    private long mInfoTotalPauseTime;
    private double mInfoDistance;
    private double mInfoMaxSpeed;
    private double mInfoAvgSpeed;
    private double mInfoAvgMovingSpeed;
    private int trackLineWidth = 10;
    private boolean loadTrackFiles = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpstrack_map);

        Bundle mExtras = getIntent().getExtras();
        mResource = getResources();
        mErrMsg = mResource.getString(R.string.error_056);
        //get the the track id
        mTrackId = mExtras.getLong(GPS_TRACK_ID);

        DBAdapter dbAdapter = new DBAdapter(this);
        Cursor c = dbAdapter.fetchRecord(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_LIST_GPSTRACK_TABLE, mTrackId);
        mInfoName = c != null ? c.getString(DBAdapter.COL_POS_GEN_NAME) : "";
        mInfoDateTimeStart = Utils.getFormattedDateTime((c != null ? c.getLong(DBAdapter.COL_POS_GPSTRACK__DATE) : 0) * 1000, false); //format[0].format(new Date(c.getLong(DBAdapter.COL_POS_GPSTRACK__DATE) * 1000));
        mInfoDateTimeEnd = Utils.getFormattedDateTime(((c != null ? c.getLong(DBAdapter.COL_POS_GPSTRACK__DATE) : 0) * 1000) + ((c != null ? c.getLong(DBAdapter.COL_POS_GPSTRACK__TOTALTIME) : 0) * 1000), false);

        mInfoTotalTime = c != null ? c.getLong(DBAdapter.COL_POS_GPSTRACK__TOTALTIME) : 0;
        mInfoMovingTime = c != null ? c.getLong(DBAdapter.COL_POS_GPSTRACK__MOVINGTIME) : 0;
        mInfoTotalPauseTime = c != null ? c.getLong(DBAdapter.COL_POS_GPSTRACK__TOTALPAUSETIME) : 0;
        mInfoNonMovingTime = mInfoTotalTime - mInfoTotalPauseTime - mInfoMovingTime;
        mInfoDistance = c != null ? c.getDouble(DBAdapter.COL_POS_GPSTRACK__DISTANCE) : 0;
        mInfoMaxSpeed = c != null ? c.getDouble(DBAdapter.COL_POS_GPSTRACK__MAXSPEED) : 0;
        mInfoAvgSpeed = c != null ? c.getDouble(DBAdapter.COL_POS_GPSTRACK__AVGSPEED) : 0;
        mInfoAvgMovingSpeed = c != null ? c.getDouble(DBAdapter.COL_POS_GPSTRACK__AVGMOVINGSPEED) : 0;
        mCarUOMLengthCode = dbAdapter.getUOMCode(dbAdapter.getCarUOMLengthID(c != null ? c.getLong(DBAdapter.COL_POS_GPSTRACK__CAR_ID) : -1));
        if (c != null) {
            c.close();
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if (metrics.densityDpi <= DisplayMetrics.DENSITY_HIGH) {
            trackLineWidth = 3;
        }

        loadTrackFiles = true;
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(mInfoName);
        setUpMapIfNeeded();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length == 0
                || grantResults[0] == PackageManager.PERMISSION_DENIED) {
            finish();
        }
        else {
            loadTrackFiles = true;
            mMap = null;
            setUpMapIfNeeded();
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
//            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    if (mMap != null) {
                        return;
                    }

                    mMap = googleMap;
                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Nullable
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {
                            @SuppressLint("InflateParams") View v = getLayoutInflater().inflate(R.layout.gpstrack_map_info_window, null);

                            TextView title = v.findViewById(R.id.title);
                            title.setText(marker.getTitle());

                            TextView info = v.findViewById(R.id.info);
                            info.setText(marker.getSnippet());
                            return v;
                        }
                    });
                    GPSTrackMap.this.setUpMap();
                }
            });
        }
    }

    private void setUpMap() {
        if (!loadTrackFiles) {
            return;
        }

        if (!loadTrackAndBounds()) {
            Utils.showNotReportableErrorDialog(this, getString(R.string.gen_error), mErrMsg, false);
            return;
        }

        mMap.addPolyline(mTrackLine).setWidth(trackLineWidth);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition arg0) {
                // Move camera.
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mTrackBoundBuilder.build(), 100));
                // Remove listener to prevent position reset on camera move.
                mMap.setOnCameraChangeListener(null);
            }
        });
    }

    private boolean loadTrackAndBounds() {

        if (!loadTrackFiles) {
            return false;
        }

        FileInputStream trackInputStream;
        DataInputStream trackData;
        BufferedReader trackBufferedReader;
        String trackLine;
        ArrayList<String> trackFiles;
        double latitude = 0;
        double longitude = 0;
        double pauseStartLatitude = 0;
        double pauseStartLongitude = 0;
        String pointType;
        String[] gopData;
        boolean isFirst = true;
        int boundCheck = 0; //add to bounds only some points

        trackFiles = FileUtils.getFileNames(this, ConstantValues.TRACK_FOLDER, mTrackId + "_[0-9][0-9][0-9].gop");

        if (trackFiles == null || trackFiles.isEmpty()) {
            mErrMsg = mResource.getString(R.string.error_036);
            return false;
        }

        if (mTrackLine != null) {
            mTrackLine = null;
        }
        mTrackLine = new PolylineOptions();

        if (mTrackBoundBuilder != null) {
            mTrackBoundBuilder = null;
        }
        mTrackBoundBuilder = new LatLngBounds.Builder();

        try {
            for (String trackFile : trackFiles) {
                trackFile = ConstantValues.TRACK_FOLDER + trackFile;
                trackInputStream = new FileInputStream(trackFile);
                trackData = new DataInputStream(trackInputStream);
                trackBufferedReader = new BufferedReader(new InputStreamReader(trackData));
                while ((trackLine = trackBufferedReader.readLine()) != null) {
                    if (trackLine.length() == 0 || trackLine.contains("Latitude")) //header line
                    {
                        continue;
                    }

                    boundCheck++;
                    gopData = trackLine.split(",");
                    latitude = Double.parseDouble(gopData[0]) / 1000000d;
                    longitude = Double.parseDouble(gopData[1]) / 1000000d;
                    if (gopData.length > 2) {
                        pointType = gopData[2];
                    }
                    else {
                        pointType = GPSTrackService.GPS_TRACK_POINT__NORMAL; //Normal Point
                    }


                    if (isFirst) {
                        mTrackBoundBuilder.include(new LatLng(latitude, longitude));
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.gps_track_map_start_point))
                                .alpha(0.7f).title(mResource.getString(R.string.gps_track_map_trip_start_title))
                                .snippet(mResource.getString(R.string.gps_track_map_trip_start_date) + mInfoDateTimeStart));
                        isFirst = false;
                    }

                    if (pointType.equals(GPSTrackService.GPS_TRACK_POINT__PAUSE_START)) {
                        pauseStartLatitude = latitude;
                        pauseStartLongitude = longitude;
                    }
                    else if (pointType.equals(GPSTrackService.GPS_TRACK_POINT__PAUSE_END)) {
                        String pauseInfo;
                        if (gopData.length > 3) {
                            pauseInfo = String.format(mResource.getString(R.string.gps_track_map_pause_info),
                                    Utils.getFormattedDateTime((Long.parseLong(gopData[3]) - Long.parseLong(gopData[4])), false),
                                    Utils.getFormattedDateTime(Long.parseLong(gopData[3]), false),
                                    Utils.getTimeString(Long.parseLong(gopData[4]) / 1000));
                        }
                        else {
                            pauseInfo = "";
                        }
                        mMap.addMarker(new MarkerOptions().position(new LatLng(pauseStartLatitude, pauseStartLongitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.gps_track_map_pause_point))
                                .alpha(0.7f).title(mResource.getString(R.string.gps_track_map_trip_pause_title))
                                .snippet(pauseInfo));
                        pauseStartLatitude = 0;
                        pauseStartLongitude = 0;
                    }
                    if (boundCheck % 10 == 0) {
                        mTrackBoundBuilder.include(new LatLng(latitude, longitude));
                    }
                    mTrackLine.add(new LatLng(latitude, longitude));
                }
                trackInputStream.close();
                trackData.close();
                trackBufferedReader.close();
            }
            mTrackBoundBuilder.include(new LatLng(latitude, longitude));

            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.gps_track_map_stop_point))
                    .alpha(0.7f).title(mResource.getString(R.string.gps_track_map_trip_end_title))
                    .snippet(
                            mResource.getString(R.string.gps_track_map_trip_start_date) + " " + mInfoDateTimeStart + "\n" +
                                    mResource.getString(R.string.gps_track_map_trip_end_date) + " " + mInfoDateTimeEnd + "\n" +
                                    mResource.getString(R.string.gps_track_detail_var_1) + " " + mInfoDistance + " " + mCarUOMLengthCode + "\n\n" +
                                    mResource.getString(R.string.gps_track_detail_var_5) + " " + Utils.getTimeString(mInfoTotalTime) + "\n" +
                                    mResource.getString(R.string.gps_track_detail_var_6) + " " + Utils.getTimeString(mInfoMovingTime) + "\n" +
                                    mResource.getString(R.string.gps_track_detail_var_13) + " " + Utils.getTimeString(mInfoNonMovingTime) + "\n" +
                                    mResource.getString(R.string.gps_track_detail_var_12) + " " + Utils.getTimeString(mInfoTotalPauseTime) + "\n\n" +
                                    mResource.getString(R.string.gps_track_detail_var_2) + " " + (new BigDecimal(mInfoMaxSpeed)).setScale(2, RoundingMode.HALF_UP).toString() + " "
                                    + mCarUOMLengthCode + "/h\n" +
                                    mResource.getString(R.string.gps_track_detail_var_3) + " " + (new BigDecimal(mInfoAvgSpeed)).setScale(2, RoundingMode.HALF_UP).toString() + " "
                                    + mCarUOMLengthCode + "/h\n" +
                                    mResource.getString(R.string.gps_track_detail_var_4) + " " + (new BigDecimal(mInfoAvgMovingSpeed)).setScale(2, RoundingMode.HALF_UP).toString() + " "
                                    + mCarUOMLengthCode + "/h"
                    ));
            return true;
        }
        catch (IOException e) {
            mErrMsg = e.getLocalizedMessage();
            return false;
        }
    }
}
