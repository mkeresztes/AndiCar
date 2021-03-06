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
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuPopup;
import com.shehabic.droppy.animations.DroppyFadeInAnimation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;

import andicar.n.activity.CommonDetailActivity;
import andicar.n.activity.CommonListActivity;
import andicar.n.activity.dialogs.DateRangeSearchDialogFragment;
import andicar.n.activity.dialogs.GPSTrackControllerDialogActivity;
import andicar.n.activity.dialogs.ToDoNotificationDialogActivity;
import andicar.n.activity.dialogs.WelcomeDialog;
import andicar.n.activity.dialogs.WhatsNewDialog;
import andicar.n.activity.fragment.BaseEditFragment;
import andicar.n.activity.fragment.TaskEditFragment;
import andicar.n.activity.miscellaneous.AboutActivity;
import andicar.n.activity.miscellaneous.GPSTrackMap;
import andicar.n.activity.preference.PreferenceActivity;
import andicar.n.components.LineChartComponent;
import andicar.n.components.PieChartsComponent;
import andicar.n.components.RecordComponent;
import andicar.n.components.StatisticsComponent;
import andicar.n.persistence.DB;
import andicar.n.persistence.DBAdapter;
import andicar.n.persistence.DBReportAdapter;
import andicar.n.service.ToDoNotificationJob;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;
import andicar.n.view.MainNavigationView;


/**
 * @author miki
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DateRangeSearchDialogFragment.DateRangeSearchDialogFragmentListener {

    private static final int REQUEST_CODE_ADD_CAR = 1;
    private static final int REQUEST_CODE_SETTINGS = 2;
    //    private static final int REQUEST_CODE_CHART_DETAIL = 3;
    private static final int CHART_FILTER_ALL = 1;
    private static final int CHART_FILTER_CURRENT_MONTH = 2;
    private static final int CHART_FILTER_PREVIOUS_MONTH = 3;
    private static final int CHART_FILTER_CURRENT_YEAR = 4;
    private static final int CHART_FILTER_PREVIOUS_YEAR = 5;
    private static final int CHART_FILTER_CUSTOM_PERIOD = 6;

    private static final String DO_NOT_USE = "DNU";
    private static final String LAST_TRIP_RECORD = "LTR";
    private static final String TRIPS_PIE_CHART = "CTR";
    private static final String LAST_FILL_UP_RECORD = "LFU";
    private static final String FUEL_QTY_PIE_CHART = "CFQ";
    private static final String FUEL_VALUE_PIE_CHART = "CFV";
    private static final String LAST_EXPENSE_RECORD = "LEX";
    private static final String EXPENSES_PIE_CHART = "CEX";
    private static final String LAST_GPS_TRACK_RECORD = "LGT";
    private static final String FUEL_EFF_LINE_CHART = "CFE";
    private static final String FUEL_CONS_LINE_CHART = "CFC";
    private static final String FUEL_PRICE_LINE_CHART = "CFP";
    private static final String STATISTICS_ZONE = "STS";
    private final View.OnClickListener btnEditClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int type = (Integer) view.getTag(R.string.record_component_table_key);
            Long recordID = (Long) view.getTag(R.string.record_component_record_id_key);
            showCreateEditRecordActivity(type, recordID);
        }
    };

    private final View.OnClickListener btnNewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int type = (Integer) view.getTag(R.string.record_component_table_key);
            showCreateEditRecordActivity(type, -1L);
        }
    };

    private final View.OnClickListener btnMapClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Long recordID = (Long) view.getTag(R.string.record_component_record_id_key);
            Intent gpstrackShowMapIntent = new Intent(getApplicationContext(), GPSTrackMap.class);
            gpstrackShowMapIntent.putExtra(GPSTrackMap.GPS_TRACK_ID, recordID);
            startActivity(gpstrackShowMapIntent);
        }
    };

    private final View.OnClickListener btnListListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int type = (Integer) view.getTag(R.string.record_component_table_key);
            showListActivity(type);
        }
    };

    private boolean mErrorInDrawCharts = false;
    //used to determine if the option menu need or not (for filtering chart data)
    private boolean mIsCanShowFilterMenu = false;
    private boolean mIsAlternateFuelVehicle = false;
    private Menu mMenu;
    private int mChartFilterType = 1;
    private long mChartPeriodStartInSeconds = -1;
    private long mChartPeriodEndInSeconds = -1;
    private long mLastSelectedCarID = -1;
    private String mDbVersion;
    private String mCarUOMVolumeCode;
    private String mCarUOMLengthCode;
    private SharedPreferences mPreferences;
    private MainNavigationView mNavigationView;
    private DrawerLayout mNavViewDrawer;
    private long mLastToDoId = -1;
    private long mLastToDoTaskId = -1;
    private long mLastToDoCarId = -1;
    //    private View llStatisticsZone;
    private View llToDoZone;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //needed for inflating xml image resources for main_activity_popup_menu in 4x platform
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.main_activity);

//        llStatisticsZone = findViewById(R.id.llStatisticsZone);
        llToDoZone = findViewById(R.id.llToDoZone);

        if (savedInstanceState != null) {
            mChartFilterType = savedInstanceState.getInt("mChartFilterType", 1);
            mChartPeriodStartInSeconds = savedInstanceState.getLong("mChartPeriodStartInSeconds", -1);
            mChartPeriodEndInSeconds = savedInstanceState.getLong("mChartPeriodEndInSeconds", -1);
        }
        //set up the main toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNavViewDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, mNavViewDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert mNavViewDrawer != null;
        mNavViewDrawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        assert mNavigationView != null;
        mNavigationView.setNavigationItemSelectedListener(this);
        LinearLayout carInfo = mNavigationView.getHeaderView(0).findViewById(R.id.car_info);
        carInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNavigationView.changeMenuLayout();
            }
        });

        //open the preferences file
        mPreferences = AndiCar.getDefaultSharedPreferences();

        //get the last selected car
        try {
            mLastSelectedCarID = mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_car_id), -1);
        }
        catch (ClassCastException e) {
            mLastSelectedCarID = mPreferences.getInt(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_car_id), -1);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.remove(getString(R.string.pref_key_last_selected_car_id));
            editor.putLong(getString(R.string.pref_key_last_selected_car_id), mLastSelectedCarID);
            editor.apply();
        }


        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("google.message_id")) {
            try{
                DBAdapter db = new DBAdapter(this);
                if(db.getIdByName(DB.TABLE_NAME_DISPLAYED_MESSAGES, getIntent().getExtras().getString("google.message_id")) < 0){ //new message
                    ContentValues data = new ContentValues();
                    data.put(DBAdapter.COL_NAME_GEN_NAME, getIntent().getExtras().getString("andicar.msg_title"));
                    data.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, getIntent().getExtras().getString("andicar.msg_body"));
                    data.put(DBAdapter.COL_NAME_MESSAGES__MESSAGE_ID, getIntent().getExtras().getString("google.message_id"));
                    data.put(DBAdapter.COL_NAME_MESSAGES__DATE, System.currentTimeMillis() / 1000);
                    data.put(DBAdapter.COL_NAME_MESSAGES__IS_READ, "N");
                    data.put(DBAdapter.COL_NAME_MESSAGES__IS_STARRED, "N");
                    db.createRecord(DBAdapter.TABLE_NAME_MESSAGES, data);

                    data.clear();
                    data.put(DBAdapter.COL_NAME_GEN_NAME, getIntent().getExtras().getString("google.message_id"));
                    db.createRecord(DBAdapter.TABLE_NAME_DISPLAYED_MESSAGES, data);

                    Utils.showMessageDialog(getApplicationContext(), getIntent().getExtras().getString("google.message_id"));
                }
                db.close();
            }
            catch (Exception ignored){}
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();

        //clear the secondary menu to avoid duplicate items
        mNavigationView.clearSecondaryMenuEntries();
        mNavigationView.mForceSecondary = false;

        if (mNavigationView.getMenu().findItem(R.id.nav_rate) != null && Utils.isCanShowRateApp(this)) {
            mNavigationView.getMenu().findItem(R.id.nav_rate).setVisible(true);
        }
        if (mNavigationView.getMenu().findItem(R.id.nav_messages) != null && Utils.isCanShowMessagesMenu(this)) {
            mNavigationView.getMenu().findItem(R.id.nav_messages).setVisible(true);
        }

        //
        // get the list of active cars from the database
        //
        String columnsToGet[] = {DBAdapter.COL_NAME_GEN_ROWID, DBAdapter.COL_NAME_GEN_NAME, DBAdapter.COL_NAME_GEN_USER_COMMENT, DBAdapter.COL_NAME_CAR__REGISTRATIONNO};
        String selectionCondition = DBAdapter.COL_NAME_GEN_ISACTIVE + "=?";
        String[] selectionArgs = {"Y"};
        DBAdapter db = new DBAdapter(this);

        mDbVersion = Integer.toString(db.getVersion());
        fillShortAbout();

        Cursor c = db.query(DBAdapter.TABLE_NAME_CAR, columnsToGet, selectionCondition, selectionArgs, DBAdapter.COL_NAME_GEN_NAME);
        boolean tmpCheck = false;

        //get the last selected car
        mLastSelectedCarID = mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_car_id), -1);


        //add the cars to the secondary menu
        while (c.moveToNext()) {
            mNavigationView.addSecondaryMenuEntry(c.getInt(0), c.getString(1), R.drawable.ic_menu_car_black_24dp_pad4dp);
            //check if the cursor is on the last selected car for update the menu header labels
            if (c.getInt(0) == mLastSelectedCarID) {
                setSelectedCar(c.getInt(0), false);
                tmpCheck = true;
                mNavigationView.mForceSecondary = false;
            }
        }

        if (!tmpCheck) {
            //go to the first record (if exists) if the last used car not in the actual list of cars (db restore, inactivated, etc)
            if (c.moveToFirst()) {
                //save as lastUsedCar
                setSelectedCar(c.getLong(0), false);
                if (mNavigationView.getMenuType() == MainNavigationView.MENU_TYPE_SECONDARY) {
                    mNavigationView.changeMenuLayout();
                }
                mNavigationView.mForceSecondary = false;
            }
            else {
                //no active car exists
                setSelectedCar(-1, false);
                mNavigationView.changeMenuLayout(MainNavigationView.MENU_TYPE_SECONDARY);
                mNavigationView.mForceSecondary = true;
            }
        }
        c.close();

        mCarUOMVolumeCode = db.getUOMCode(db.getCarUOMFuelID(mLastSelectedCarID, true));
        mCarUOMLengthCode = db.getUOMCode(db.getCarUOMLengthID(mLastSelectedCarID));
        db.close();

        //if first use, set FuelEff or FuelCons based on the first car definition
        if (!mPreferences.contains(getString(R.string.pref_key_main_zone3_content))
                && mLastSelectedCarID > 0) {
            SharedPreferences.Editor e = mPreferences.edit();
            if (mCarUOMVolumeCode != null &&
                    (mCarUOMVolumeCode.equals("gal US") || mCarUOMVolumeCode.equals("gal GB"))) {
                e.putString(getString(R.string.pref_key_main_zone3_content), FUEL_EFF_LINE_CHART);
            }
            else {
                e.putString(getString(R.string.pref_key_main_zone3_content), FUEL_CONS_LINE_CHART);
            }
            e.apply();
        }



        //force a redraw of the menu if the secondary is active
        if (mNavigationView.getMenuType() == MainNavigationView.MENU_TYPE_SECONDARY) {
            mNavigationView.changeMenuLayout(MainNavigationView.MENU_TYPE_SECONDARY);
        }

        //for debug
        TextView tvDebugInfo = findViewById(R.id.tvDebugInfo);
        if (tvDebugInfo != null) {
            if (Utils.isDebugVersion() && ConstantValues.DEBUG_IS_SHOW_INFO_IN_FRAGMENTS) {
                //noinspection UnusedAssignment
                Display display = getWindowManager().getDefaultDisplay();
                //noinspection UnusedAssignment
                float density = getResources().getDisplayMetrics().density;
                //noinspection UnusedAssignment
                Point size = new Point();
                display.getSize(size);
                //noinspection UnusedAssignment
                int width = size.x;
                //noinspection UnusedAssignment
                int height = size.y;
                //noinspection UnusedAssignment
                String debugInfo = tvDebugInfo.getText().toString();
                //noinspection UnusedAssignment
                debugInfo = debugInfo.substring(0, debugInfo.indexOf(";", 0) + 1) + " Size in pixel: " + width + " x " + height + "; Size in dp: " + (width / density) + " x " + (height / density) + "; Density: " + density;
                tvDebugInfo.setText(debugInfo);
            }
            else {
                tvDebugInfo.setVisibility(View.GONE);
            }
        }

        ImageButton btnAdd = findViewById(R.id.btnAdd);
        if (mLastSelectedCarID > -1) {
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (mPreferences.getString(getString(R.string.pref_key_main_btn_add), "")) {
                        case "0":
                            MainActivity.this.showPopup(view);
                            break;
                        case "1":
                            showCreateEditRecordActivity(R.id.mnuTrip, -1L);
                            break;
                        case "2":
                            showCreateEditRecordActivity(R.id.mnuRefuel, -1L);
                            break;
                        case "3":
                            showCreateEditRecordActivity(R.id.mnuExpense, -1L);
                            break;
                        case "4":
                            showCreateEditRecordActivity(R.id.mnuGPSTrack, -1L);
                            break;
                        default:
                            MainActivity.this.showPopup(view);
                    }
                }
            });

            btnAdd.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
//                    if (Utils.isDebugVersion()) {
//                        startActivity(new Intent(MainActivity.this, TestActivity.class));
//                    } else {
                        MainActivity.this.showPopup(view);
//                    }
                    return true;
                }
            });
        }
        else {
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDefineCar();
                }
            });
        }

        //charting
        fillContent();
        try {
            if (mPreferences.getBoolean(getString(R.string.pref_key_show_welcome_screen), false)) {
                SharedPreferences.Editor e = mPreferences.edit();
                e.putBoolean(getString(R.string.pref_key_show_welcome_screen), false);
                e.apply();
                WelcomeDialog dialog = new WelcomeDialog(this);
                dialog.show();
            } else if (mPreferences.getBoolean(getString(R.string.pref_key_show_whats_new_dialog), false)) {
                SharedPreferences.Editor e = mPreferences.edit();
                e.putBoolean(getString(R.string.pref_key_show_whats_new_dialog), false);
                e.apply();
                Intent whatsNewIntent = new Intent(this, WhatsNewDialog.class);
                whatsNewIntent.putExtra(WhatsNewDialog.IS_SHOW_FIVE_STARS_BUTTON_KEY, true);
                startActivity(whatsNewIntent);
            }
        } catch (Exception ignored) {
        }
    }

    private String getChartDataPeriodText() {
        switch (mChartFilterType) {
            case CHART_FILTER_ALL:
                long firstSeen = -1;
                try {
                    DBAdapter db = new DBAdapter(this);
                    firstSeen = db.getCarFirstSeenDate(mLastSelectedCarID);
                    db.close();
                }
                catch (Exception ignored) {
                }
                if (firstSeen > 0) {
                    return
                            String.format(getString(R.string.chart_filter_custom_period_text),
                                    Utils.getFormattedDateTime(firstSeen * 1000, true),
                                    getString(R.string.chart_filter_custom_period_now_text));
                }
                else {
                    return getString(R.string.chart_filter_all_text);
                }
            case CHART_FILTER_CURRENT_MONTH:
                return getString(R.string.chart_filter_current_month_text);
            case CHART_FILTER_PREVIOUS_MONTH:
                return getString(R.string.chart_filter_previous_month_text);
            case CHART_FILTER_CURRENT_YEAR:
                return getString(R.string.chart_filter_current_year_text);
            case CHART_FILTER_PREVIOUS_YEAR:
                return getString(R.string.chart_filter_previous_year_text);
            case CHART_FILTER_CUSTOM_PERIOD:
                return
                        String.format(getString(R.string.chart_filter_custom_period_text),
                                mChartPeriodStartInSeconds > 0 ?
                                        Utils.getFormattedDateTime(mChartPeriodStartInSeconds * 1000, true) :
                                        getString(R.string.chart_filter_custom_period_beginning_text),
                                mChartPeriodEndInSeconds > 0 ?
                                        Utils.getFormattedDateTime(mChartPeriodEndInSeconds * 1000, true) :
                                        getString(R.string.chart_filter_custom_period_now_text));
            default:
                return getString(R.string.chart_filter_all_text);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mChartFilterType", mChartFilterType);
        outState.putLong("mChartPeriodStartInSeconds", mChartPeriodStartInSeconds);
        outState.putLong("mChartPeriodEndInSeconds", mChartPeriodEndInSeconds);
    }

    private void showPopup(View v) {
        DroppyMenuPopup.Builder droppyBuilder = new DroppyMenuPopup.Builder(this, v);
        DroppyMenuPopup droppyMenu = droppyBuilder.fromMenu(R.menu.main_activity_popup_menu)
                .triggerOnAnchorClick(false)
                .setOnClick(new DroppyClickCallbackInterface() {
                    @Override
                    public void call(View v1, int id) {
                        showCreateEditRecordActivity(id, -1L);
                    }
                })
                .setPopupAnimation(new DroppyFadeInAnimation())
                .setXOffset(5)
                .setYOffset(5)
                .build();
        droppyMenu.show();
    }

    private void showCreateEditRecordActivity(int type, long recordID) {
        Intent i = null;
        if (type == R.id.mnuTrip) {
            i = new Intent(MainActivity.this, CommonDetailActivity.class);
            i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_MILEAGE);
        }
        else if (type == R.id.mnuRefuel) {
            i = new Intent(MainActivity.this, CommonDetailActivity.class);
            i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_REFUEL);
        }
        else if (type == R.id.mnuExpense) {
            i = new Intent(MainActivity.this, CommonDetailActivity.class);
            i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_EXPENSE);
        }
        else if (type == R.id.mnuGPSTrack) {
            if (recordID == -1L) {
                i = new Intent(MainActivity.this, GPSTrackControllerDialogActivity.class);
            }
            else {
                i = new Intent(MainActivity.this, CommonDetailActivity.class);
                i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_GPS_TRACK);
            }
        }

        if (i != null) {
            i.putExtra(BaseEditFragment.RECORD_ID_KEY, recordID);
            if (recordID == -1L) {
                i.putExtra(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_NEW);
            }
            else {
                i.putExtra(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_EDIT);
            }

            MainActivity.this.startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        if (mNavViewDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavViewDrawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    private void showDefineCar() {
        Intent i = new Intent(MainActivity.this, CommonDetailActivity.class);
        i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_CAR);
        i.putExtra(BaseEditFragment.RECORD_ID_KEY, -1L);
        i.putExtra(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_NEW);
        startActivityForResult(i, REQUEST_CODE_ADD_CAR);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_trip || id == R.id.nav_refuel || id == R.id.nav_expense
                || id == R.id.nav_gpstrack || id == R.id.nav_todo
                || id == R.id.nav_messages) {
            showListActivity(id);
        }
        else if (id == R.id.nav_settings) {
            Intent i = new Intent(this, PreferenceActivity.class);
            startActivityForResult(i, REQUEST_CODE_SETTINGS);
        }
        else if (id == R.id.nav_about) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
        }
        else if (id == R.id.nav_report_issue) {
            try {
                Intent actionIntent = new Intent(Intent.ACTION_SENDTO);
                actionIntent.setData(Uri.parse("mailto:"));
                String to[] = {"andicar.support@gmail.com"};
                actionIntent.putExtra(Intent.EXTRA_EMAIL, to);
                actionIntent.putExtra(Intent.EXTRA_SUBJECT, "AndiCar issue");
                startActivity(actionIntent);
            }
            catch (Exception e) {
                Utils.showNotReportableErrorDialog(this, e.getMessage(), null);
            }
        }
        else if (id == R.id.nav_rate) {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market back stack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            }
            catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        }
        else if (mNavigationView.getMenuType() == MainNavigationView.MENU_TYPE_SECONDARY) {
            //an entry selected from the secondary menu
            if (id == MainNavigationView.MENU_ADD_ID) {
                showDefineCar();
            }
            else {
                //a car was touched. the menu item id is the car id
                //save the selected car as Last Used
                setSelectedCar(id, true);
                //switch back the navigation menu to the primary layout
                mNavigationView.changeMenuLayout();
            }
        }
        assert mNavViewDrawer != null;
        mNavViewDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showListActivity(int id) {
        if (id == R.id.nav_trip) {
            Intent i = new Intent(MainActivity.this, CommonListActivity.class);
            i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_MILEAGE);
            i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
            i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, true);
            i.putExtra(CommonListActivity.IS_SHOW_SHARE_MENU_KEY, true);
            startActivity(i);
        }
        else if (id == R.id.nav_refuel) {
            Intent i = new Intent(MainActivity.this, CommonListActivity.class);
            i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_REFUEL);
            i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
            i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, true);
            i.putExtra(CommonListActivity.IS_SHOW_SHARE_MENU_KEY, true);
            startActivity(i);
        }
        else if (id == R.id.nav_expense) {
            Intent i = new Intent(MainActivity.this, CommonListActivity.class);
            i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_EXPENSE);
            i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
            i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, true);
            i.putExtra(CommonListActivity.IS_SHOW_SHARE_MENU_KEY, true);
            startActivity(i);
        }
        else if (id == R.id.nav_gpstrack) {
            Intent i = new Intent(MainActivity.this, CommonListActivity.class);
            i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_GPS_TRACK);
            i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
            i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, true);
            i.putExtra(CommonListActivity.IS_SHOW_SHARE_MENU_KEY, true);
            startActivity(i);
        }
        else if (id == R.id.nav_todo) {
            Intent i = new Intent(MainActivity.this, CommonListActivity.class);
            i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_TODO);
            i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
            i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, true);
            i.putExtra(CommonListActivity.IS_SHOW_SHARE_MENU_KEY, true);
            startActivity(i);
        }
        else if (id == R.id.nav_messages) {
            Intent i = new Intent(MainActivity.this, CommonListActivity.class);
            i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_MESSAGE);
            i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
            i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
            i.putExtra(CommonListActivity.IS_SHOW_SHARE_MENU_KEY, false);
            startActivity(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null) {
            menu.clear();
        }
        mMenu = menu;
        if (mIsCanShowFilterMenu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_activity_pie_chart_filter_menu, mMenu);
        }
        return true;
    }

    @SuppressLint("WrongConstant")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Calendar cal = Calendar.getInstance();
        boolean isCustomRangeSelected = false;

        switch (id) {
            case R.id.chart_filter_all:
                mChartFilterType = CHART_FILTER_ALL;
                mChartPeriodStartInSeconds = -1;
                mChartPeriodEndInSeconds = -1;
                break;
            case R.id.chart_filter_current_month:
                mChartFilterType = CHART_FILTER_CURRENT_MONTH;
                cal.set(Calendar.DAY_OF_MONTH, 1);
                mChartPeriodStartInSeconds = Utils.roundDate(cal.getTimeInMillis(), ConstantValues.DATE_DECODE_TO_ZERO) / 1000;
                mChartPeriodEndInSeconds = -1;
                break;
            case R.id.chart_filter_previous_month:
                mChartFilterType = CHART_FILTER_PREVIOUS_MONTH;
                cal.add(Calendar.MONTH, -1);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                mChartPeriodStartInSeconds = Utils.roundDate(cal.getTimeInMillis(), ConstantValues.DATE_DECODE_TO_ZERO) / 1000;
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                mChartPeriodEndInSeconds = Utils.roundDate(cal.getTimeInMillis(), ConstantValues.DATE_DECODE_TO_24) / 1000;
                break;
            case R.id.chart_filter_current_year:
                mChartFilterType = CHART_FILTER_CURRENT_YEAR;
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                mChartPeriodStartInSeconds = Utils.roundDate(cal.getTimeInMillis(), ConstantValues.DATE_DECODE_TO_ZERO) / 1000;
                mChartPeriodEndInSeconds = -1;
                break;
            case R.id.chart_filter_previous_year:
                mChartFilterType = CHART_FILTER_PREVIOUS_YEAR;
                cal.add(Calendar.YEAR, -1);
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                mChartPeriodStartInSeconds = Utils.roundDate(cal.getTimeInMillis(), ConstantValues.DATE_DECODE_TO_ZERO) / 1000;
                cal.set(Calendar.MONTH, Calendar.DECEMBER);
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                mChartPeriodEndInSeconds = Utils.roundDate(cal.getTimeInMillis(), ConstantValues.DATE_DECODE_TO_24) / 1000;
                break;
            case R.id.chart_filter_custom_period:
//                mChartFilterType = CHART_FILTER_CUSTOM_PERIOD;
                isCustomRangeSelected = true;
                Bundle searchArg = new Bundle();
                searchArg.putLong(DateRangeSearchDialogFragment.DATE_FROM_IN_MILLIS_KEY, mChartPeriodStartInSeconds * 1000);
                searchArg.putLong(DateRangeSearchDialogFragment.DATE_TO_IN_MILLIS_KEY, mChartPeriodEndInSeconds * 1000);
                FragmentManager fm = getSupportFragmentManager();
                DateRangeSearchDialogFragment searchDialog = new DateRangeSearchDialogFragment();
                searchDialog.setArguments(searchArg);
                searchDialog.show(fm, "fragment_search_dialog");
                break;
            default:
                mChartFilterType = CHART_FILTER_ALL;
                mChartPeriodStartInSeconds = -1;
                mChartPeriodEndInSeconds = -1;
        }
        if (!isCustomRangeSelected) {
            fillContent();
        }
        Toast.makeText(this, getString(R.string.main_screen_filters), Toast.LENGTH_SHORT).show();
        return true;
    }

    private void setSelectedCar(long id, boolean needCallRun) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_car_id), id);
        editor.apply();

        mLastSelectedCarID = id;

        //get the car details
        String columnsToGet[] = {DBAdapter.COL_NAME_GEN_ROWID, DBAdapter.COL_NAME_GEN_NAME, DBAdapter.COL_NAME_GEN_USER_COMMENT,
                DBAdapter.COL_NAME_CAR__REGISTRATIONNO, DBAdapter.COL_NAME_CAR__INDEXCURRENT};

        String selectionCondition = DBAdapter.COL_NAME_GEN_ROWID + "=?";
        String[] selectionArgs = {String.valueOf(id)};
        DBAdapter db = new DBAdapter(this);
        Cursor c = db.query(DBAdapter.TABLE_NAME_CAR, columnsToGet, selectionCondition, selectionArgs, DBAdapter.COL_NAME_GEN_NAME);
        if (c.moveToFirst()) {
            mNavigationView.setHeaderLabels(c.getString(1), c.getString(3), c.getString(2));
            setTitle(c.getString(1) + " - " +
                    Utils.numberToString(c.getDouble(4), true, 0, RoundingMode.HALF_UP) + " " + db.getUOMCode(db.getCarUOMLengthID(c.getLong(0))));
        }
        else {
            mNavigationView.setHeaderLabels(getString(R.string.main_activity_no_car_title), "", "");
            setTitle(getString(R.string.main_activity_no_car_title));
        }
        c.close();

        mIsAlternateFuelVehicle = db.isAFVCar(mLastSelectedCarID);
        db.close();

        if (needCallRun) {
            fillContent();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_CAR && resultCode == Activity.RESULT_OK && data.getExtras() != null) {
            setSelectedCar(data.getExtras().getInt(DBAdapter.COL_NAME_GEN_ROWID), false);
            //switch back the navigation menu to the primary layout
            mNavigationView.mForceSecondary = false;
            mNavigationView.changeMenuLayout();
        }
    }

    @SuppressLint("SetTextI18n")
    private void fillLastRecord(RecordComponent recordComponent, String recordSource) {
        try {
            recordComponent.setEditButtonOnClickListener(btnEditClickListener);
            recordComponent.setAddNewButtonOnClickListener(btnNewClickListener);
            recordComponent.setShowListButtonOnClickListener(btnListListener);
            recordComponent.setMapButtonOnClickListener(btnMapClickListener);

            DBReportAdapter dbReportAdapter = new DBReportAdapter(getApplicationContext(), null, null);
            Bundle sqlWWhereCondition = new Bundle();

            switch (recordSource) {
                case LAST_TRIP_RECORD: { //Last trip
                    recordComponent.setHeaderText(R.string.main_activity_mileage_header_caption);

                    sqlWWhereCondition.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_MILEAGE, DBReportAdapter.COL_NAME_MILEAGE__CAR_ID) + "=",
                            Long.toString(mLastSelectedCarID));
                    dbReportAdapter.setReportSql(DBReportAdapter.MILEAGE_LIST_SELECT_NAME, sqlWWhereCondition);
                    Cursor mCursor = dbReportAdapter.fetchReport(1);
                    BigDecimal reimbursementRate = BigDecimal.ZERO;
                    String line1Content;
                    String line2Content;

                    if (mCursor != null && mCursor.moveToFirst()) {
                        recordComponent.setButtonsLineVisibility(View.VISIBLE);
                        recordComponent.setMapButtonVisibility(View.GONE);

                        recordComponent.setRecordId(mCursor.getLong(0));
                        recordComponent.setWhatEditAdd(R.id.mnuTrip);
                        recordComponent.setWhatList(R.id.nav_trip);

                        try {
                            line1Content = String.format(mCursor.getString(1), Utils.getFormattedDateTime(mCursor.getLong(5) * 1000, false)
                                    + (mCursor.getLong(14) != 0L ? " (" + Utils.getDaysHoursMinutesFromSec(mCursor.getLong(14)) + ")" : ""));
                        }
                        catch (Exception e) {
                            line1Content = "Error#1! Please contact me at andicar.support@gmail.com";
                        }

                        try {
                            reimbursementRate = new BigDecimal(mCursor.getDouble(12));
                        }
                        catch (Exception ignored) {
                        }

                        String stopIndexStr = mCursor.getString(7);
                        String mileageStr;
                        if (stopIndexStr == null) {
                            stopIndexStr = "N/A";
                            mileageStr = "Draft";
                        }
                        else {
                            stopIndexStr = Utils.numberToString(mCursor.getDouble(7), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH);
                            mileageStr = Utils.numberToString(mCursor.getDouble(8), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH);
                        }

                        try {
                            line2Content = String.format(mCursor.getString(2),
                                    Utils.numberToString(mCursor.getDouble(6), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH),
                                    stopIndexStr,
                                    mileageStr,
                                    (reimbursementRate.compareTo(BigDecimal.ZERO) == 0) ? "" : "("
                                            + AndiCar.getAppResources().getText(R.string.gen_reimbursement).toString()
                                            + " "
                                            + Utils.numberToString(reimbursementRate.multiply(new BigDecimal(mCursor.getDouble(8))), true,
                                            ConstantValues.DECIMALS_RATES, ConstantValues.ROUNDING_MODE_RATES) + " " + mCursor.getString(11) + ")");
                        }
                        catch (Exception e) {
                            line2Content = "Error#2! Please contact me at andicar.support@gmail.com";
                        }

                        if (mileageStr.equals("Draft")) {
                            line2Content = line2Content.substring(0, line2Content.indexOf("Draft") + "Draft".length());
                        }

                        if (recordComponent.isSecondLineExists()) { //three line lists
                            recordComponent.setFirstLineText(line1Content);
                            recordComponent.setSecondLineText(line2Content);
                        }
                        else {
                            //wider screens => two line lists
                            recordComponent.setFirstLineText(line1Content + "; " + line2Content);
                        }

                        if (recordComponent.isThirdLineExists()) {
                            recordComponent.setThirdLineText(mCursor.getString(3));
                        }

                        try {
                            mCursor.close();
                        }
                        catch (Exception ignored) {
                        }
                    }
                    else {
                        recordComponent.setFirstLineText(R.string.main_activity_list_no_data);
                        recordComponent.setSecondLineText(null);
                        recordComponent.setThirdLineText(null);
                        recordComponent.setButtonsLineVisibility(View.GONE);
                    }
                    break;
                }
                case LAST_FILL_UP_RECORD: { //Last fill-up
                    recordComponent.setHeaderText(R.string.main_activity_refuel_header_caption);

                    sqlWWhereCondition.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_REFUEL, DBReportAdapter.COL_NAME_REFUEL__CAR_ID) + "=",
                            Long.toString(mLastSelectedCarID));
                    dbReportAdapter.setReportSql(DBReportAdapter.REFUEL_LIST_SELECT_NAME, sqlWWhereCondition);
                    Cursor mCursor = dbReportAdapter.fetchReport(1);
                    String line1Content;
                    String line2Content;

                    if (mCursor != null && mCursor.moveToFirst()) {
                        recordComponent.setButtonsLineVisibility(View.VISIBLE);
                        recordComponent.setMapButtonVisibility(View.GONE);

                        recordComponent.setRecordId(mCursor.getLong(0));
                        recordComponent.setWhatEditAdd(R.id.mnuRefuel);
                        recordComponent.setWhatList(R.id.nav_refuel);

                        try {
                            line1Content = String.format(mCursor.getString(1), Utils.getFormattedDateTime(mCursor.getLong(4) * 1000, false));
                        }
                        catch (Exception e) {
                            line1Content = "Error#6! Please contact me at andicar.support@gmail.com";
                        }

                        try {
                            line2Content = String.format(mCursor.getString(2),
                                    Utils.numberToString(mCursor.getDouble(5), true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME),
                                    Utils.numberToString(mCursor.getDouble(6), true, ConstantValues.DECIMALS_VOLUME, ConstantValues.ROUNDING_MODE_VOLUME),
                                    Utils.numberToString(mCursor.getDouble(7), true, ConstantValues.DECIMALS_PRICE, ConstantValues.ROUNDING_MODE_PRICE),
                                    Utils.numberToString(mCursor.getDouble(8), true, ConstantValues.DECIMALS_PRICE, ConstantValues.ROUNDING_MODE_PRICE),
                                    Utils.numberToString(mCursor.getDouble(9), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT),
                                    Utils.numberToString(mCursor.getDouble(10), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT),
                                    Utils.numberToString(mCursor.getDouble(11), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH));
                        }
                        catch (Exception e) {
                            line2Content = "Error#7! Please contact me at andicar.support@gmail.com";
                        }

                        if (recordComponent.isSecondLineExists()) { //three line lists
                            recordComponent.setFirstLineText(line1Content);
                            recordComponent.setSecondLineText(line2Content);
                        }
                        else {
                            //wider screens => two line lists
                            recordComponent.setFirstLineText(line1Content + "; " + line2Content);
                        }

                        if (mCursor.getString(3) == null || mCursor.getString(3).trim().length() == 0) {
                            recordComponent.setThirdLineText(null);
                        }
                        else {
                            String text = mCursor.getString(3);
                            BigDecimal oldFullRefuelIndex;
                            try {
                                oldFullRefuelIndex = new BigDecimal(mCursor.getDouble(13));
                            }
                            catch (Exception e) {
                                recordComponent.setThirdLineText("Error#1! Please contact me at andicar.support@gmail.com");
                                return;
                            }
                            if (oldFullRefuelIndex.compareTo(BigDecimal.ZERO) < 0 //this is the first full refuel => can't calculate fuel eff
                                    || mCursor.getString(12).equals("N") //this is not a full refuel
                                    || mCursor.getString(17).equals("Y")) { //alternate fuel vehicle => can't calculate fuel eff
                                try {
                                    //do not use String.format... ! See: https://github.com/mkeresztes/AndiCar/issues/10
                                    text = text.replace("[#01]", "");
                                    recordComponent.setThirdLineText(text);
                                }
                                catch (Exception e) {
                                    recordComponent.setThirdLineText("Error#4! Please contact me at andicar.support@gmail.com");
                                }
                            } else {
                                // calculate the cons and fuel eff.
                                BigDecimal distance = (new BigDecimal(mCursor.getString(11))).subtract(oldFullRefuelIndex);
                                BigDecimal fuelQty;
                                try {
                                    Double t = dbReportAdapter.getFuelQtyForCons(mCursor.getLong(16), oldFullRefuelIndex, mCursor.getDouble(11));
                                    fuelQty = new BigDecimal(t == null ? 0d : t);
                                } catch (NullPointerException e) {
                                    recordComponent.setThirdLineText("Error#2! Please contact me at andicar.support@gmail.com");
                                    return;
                                }
                                String consStr;
                                try {
                                    consStr = Utils.numberToString(fuelQty.multiply(new BigDecimal("100")).divide(distance, 10, RoundingMode.HALF_UP), true,
                                            ConstantValues.DECIMALS_FUEL_EFF, ConstantValues.ROUNDING_MODE_FUEL_EFF) + " " +
                                            mCursor.getString(14) + "/100" +
                                            mCursor.getString(15) + "; " +
                                            Utils.numberToString(distance.divide(fuelQty, 10, RoundingMode.HALF_UP), true, ConstantValues.DECIMALS_FUEL_EFF,
                                                    ConstantValues.ROUNDING_MODE_FUEL_EFF) + " " + mCursor.getString(15) + "/" + mCursor.getString(14);
                                } catch (Exception e) {
                                    //do not use String.format... ! See: https://github.com/mkeresztes/AndiCar/issues/10
                                    recordComponent.setThirdLineText("Error#3! Please contact me at andicar.support@gmail.com");
                                    return;
                                }

                                try {
                                    //do not use String.format... ! See: https://github.com/mkeresztes/AndiCar/issues/10
                                    text = text.replace("[#01]", "\n" + AndiCar.getAppResources().getString(R.string.gen_fuel_efficiency) + " " + consStr);
                                } catch (Exception e) {
                                    recordComponent.setThirdLineText("Error#5! Please contact me at andicar.support@gmail.com");
                                    return;
                                }
                            }

                            recordComponent.setThirdLineText(text.trim());
                        }

                        try {
                            mCursor.close();
                        }
                        catch (Exception ignored) {
                        }
                    }
                    else {
                        recordComponent.setFirstLineText(R.string.main_activity_list_no_data);
                        recordComponent.setSecondLineText(null);
                        recordComponent.setThirdLineText(null);
                        recordComponent.setButtonsLineVisibility(View.GONE);
                    }
                    break;
                }
                case LAST_EXPENSE_RECORD: { //Last expense
                    recordComponent.setHeaderText(R.string.main_activity_expense_header_caption);

                    sqlWWhereCondition.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_EXPENSE, DBReportAdapter.COL_NAME_EXPENSE__CAR_ID) + "=",
                            Long.toString(mLastSelectedCarID));
                    dbReportAdapter.setReportSql(DBReportAdapter.EXPENSE_LIST_SELECT_NAME, sqlWWhereCondition);
                    Cursor mCursor = dbReportAdapter.fetchReport(1);
                    String line1Content;
                    String line2Content;
                    String line3Content;

                    if (mCursor != null && mCursor.moveToFirst()) {
                        recordComponent.setButtonsLineVisibility(View.VISIBLE);
                        recordComponent.setMapButtonVisibility(View.GONE);

                        recordComponent.setRecordId(mCursor.getLong(0));
                        recordComponent.setWhatEditAdd(R.id.mnuExpense);
                        recordComponent.setWhatList(R.id.nav_expense);

                        try {
                            line1Content = String.format(mCursor.getString(1), Utils.getFormattedDateTime(mCursor.getLong(4) * 1000, false));
                        }
                        catch (Exception e) {
                            line1Content = "Error#1! Please contact me at andicar.support@gmail.com";
                        }

                        try {
                            line2Content = String.format(mCursor.getString(2),
                                    Utils.numberToString(mCursor.getDouble(5), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT),
                                    Utils.numberToString(mCursor.getDouble(6), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT),
                                    Utils.numberToString(mCursor.getDouble(8), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT),
                                    Utils.numberToString(mCursor.getDouble(7), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH));
                        }
                        catch (Exception e) {
                            line2Content = "Error#2! Please contact me at andicar.support@gmail.com";
                        }

                        line3Content = mCursor.getString(3);

                        if (recordComponent.isSecondLineExists()) { //three line lists
                            recordComponent.setFirstLineText(line1Content);
                            recordComponent.setSecondLineText(line2Content);
                        }
                        else {
                            //wider screens => two line lists
                            if (line2Content != null && line2Content.length() > 0) {
                                recordComponent.setFirstLineText(line1Content + "; " + line2Content);
                            }
                            else {
                                recordComponent.setFirstLineText(line1Content);
                            }
                        }

                        recordComponent.setThirdLineText(line3Content);

                        try {
                            mCursor.close();
                        }
                        catch (Exception ignored) {
                        }
                    }
                    else {
                        recordComponent.setFirstLineText(R.string.main_activity_list_no_data);
                        recordComponent.setSecondLineText(null);
                        recordComponent.setThirdLineText(null);
                        recordComponent.setButtonsLineVisibility(View.GONE);
                    }
                    break;
                }
                case LAST_GPS_TRACK_RECORD: { //Last GPS Track
                    recordComponent.setHeaderText(R.string.main_activity_gps_track_header_caption);

                    sqlWWhereCondition.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_GPSTRACK, DBReportAdapter.COL_NAME_GPSTRACK__CAR_ID) + "=",
                            Long.toString(mLastSelectedCarID));
                    dbReportAdapter.setReportSql(DBReportAdapter.GPS_TRACK_LIST_SELECT_NAME, sqlWWhereCondition);
                    Cursor mCursor = dbReportAdapter.fetchReport(1);
                    String line1Content;
                    String line2Content;
                    String line3Content;

                    if (mCursor != null && mCursor.moveToFirst()) {
                        recordComponent.setButtonsLineVisibility(View.VISIBLE);
                        recordComponent.setMapButtonVisibility(View.VISIBLE);

                        recordComponent.setRecordId(mCursor.getLong(0));
                        recordComponent.setWhatEditAdd(R.id.mnuGPSTrack);
                        recordComponent.setWhatList(R.id.nav_gpstrack);

                        try {
                            line1Content = String.format(mCursor.getString(1), Utils.getFormattedDateTime(mCursor.getLong(7) * 1000, false));
                        }
                        catch (Exception e) {
                            line1Content = "Error#1! Please contact me at andicar.support@gmail.com";
                        }

                        try {
                            line2Content = String.format(mCursor.getString(2),
                                    getString(R.string.gps_track_detail_var_1),
                                    getString(R.string.gps_track_detail_var_2),
                                    getString(R.string.gps_track_detail_var_3),
                                    getString(R.string.gps_track_detail_var_4),
                                    getString(R.string.gps_track_detail_var_5) + " " + Utils.getTimeString(mCursor.getLong(4)),
                                    getString(R.string.gps_track_detail_var_6) + " " + Utils.getTimeString(mCursor.getLong(5)),
                                    getString(R.string.gps_track_detail_var_7),
                                    getString(R.string.gps_track_detail_var_8),
                                    getString(R.string.gps_track_detail_var_9),
                                    getString(R.string.gps_track_detail_var_10),
                                    getString(R.string.gps_track_detail_var_11),
                                    getString(R.string.gps_track_detail_var_12) + " " + Utils.getTimeString(mCursor.getLong(8)),
                                    getString(R.string.gps_track_detail_var_13) + " " + Utils.getTimeString(mCursor.getLong(4) - mCursor.getLong(8) - mCursor.getLong(5)));
                        }
                        catch (Exception e) {
                            line2Content = "Error#2! Please contact me at andicar.support@gmail.com";
                        }
                        line3Content = mCursor.getString(3);

                        if (recordComponent.isSecondLineExists()) { //three line lists
                            recordComponent.setFirstLineText(line1Content);
                            recordComponent.setSecondLineText(line2Content);
                        }
                        else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                recordComponent.getFirstLine().setTextAppearance(R.style.ListItem_SecondLine);
                            }
                            else {
                                recordComponent.getFirstLine().setTypeface(null, Typeface.NORMAL);
                            }

                            CharSequence text;
                            //wider screens => two line lists
                            if (line2Content != null && line2Content.length() > 0) {
                                //noinspection deprecation
                                text = Html.fromHtml("<b>" + line1Content + "</b><br>" + line2Content);
                            }
                            else {
                                //noinspection deprecation
                                text = Html.fromHtml("<b>" + line1Content + "</b>");
                            }
                            recordComponent.setFirstLineText(text);
                        }

                        recordComponent.setThirdLineText(line3Content);

                        try {
                            mCursor.close();
                        }
                        catch (Exception ignored) {
                        }
                    }
                    else {
                        recordComponent.setFirstLineText(R.string.main_activity_list_no_data);
                        recordComponent.setSecondLineText(null);
                        recordComponent.setThirdLineText(null);
                        recordComponent.setButtonsLineVisibility(View.GONE);
                    }
                    break;
                }
            }

            try {
                dbReportAdapter.close();
            }
            catch (Exception ignored) {
            }
        }
        catch (Exception e) {
            mErrorInDrawCharts = true;
            Utils.showReportableErrorDialog(this, null, e.getMessage(), e);
        }

    }

    private void drawPieCharts(PieChartsComponent pieChartsComponent, String chartSource) {
        if (mErrorInDrawCharts) {
            return;
        }

        String title1;
        String title2;
        String title3;

        DBReportAdapter dbReportAdapter = new DBReportAdapter(getApplicationContext(), null, null);

        ArrayList<String> selArgs = new ArrayList<>();
        selArgs.add(Long.toString(mLastSelectedCarID));
        if (mChartPeriodStartInSeconds > -1) {
            selArgs.add(Long.toString(mChartPeriodStartInSeconds));
        }
        else {
            selArgs.add(Long.toString(0));
        }

        if (mChartPeriodEndInSeconds > -1) {
            selArgs.add(Long.toString(mChartPeriodEndInSeconds));
        }
        else {
            selArgs.add(Long.toString(0));
        }

        String[] chartArguments = selArgs.toArray(new String[0]);

        ArrayList<DBReportAdapter.chartData> chartData;
        String chartFooterText;

        try {
            String carCurrencyCode = dbReportAdapter.getCurrencyCode(dbReportAdapter.getCarCurrencyID(mLastSelectedCarID));
            switch (chartSource) {
                case TRIPS_PIE_CHART:  //trip charts
                    title1 = getString(R.string.tripChart1Title);
                    title2 = getString(R.string.tripChart2Title);
                    title3 = getString(R.string.tripChart3Title);

                    String carUOMLengthCode = dbReportAdapter.getUOMCode(dbReportAdapter.getCarUOMLengthID(mLastSelectedCarID));
                    chartData = dbReportAdapter.getMileageByTypeChartData(chartArguments);
                    if (chartData.size() > 0) {
                        pieChartsComponent.setChartsLineHeight(false);
                        chartFooterText =
                                String.format(getString(R.string.chartFooterText), Utils.numberToString(new BigDecimal(chartData.get(0).totalValue), true, 2, RoundingMode.HALF_UP)
                                        + " " + carUOMLengthCode) + " (" + getChartDataPeriodText() + ")";
                    }
                    else {
                        pieChartsComponent.setChartsLineHeight(true);
                        chartFooterText = null;
                    }
                    pieChartsComponent.setChartFooterText(chartFooterText);
                    pieChartsComponent.setChart1TitleText(title1);
                    title1 = title1.substring(0, title1.indexOf("(") - 1) + " [" + carUOMLengthCode + "]";
                    pieChartsComponent.drawChart(1, chartData, title1);
                    pieChartsComponent.setChart2TitleText(title2);
                    title2 = title2.substring(0, title2.indexOf("(") - 1) + " [" + carUOMLengthCode + "]";
                    pieChartsComponent.drawChart(2, dbReportAdapter.getMileageByTagsChartData(chartArguments), title2);
                    pieChartsComponent.setChart3TitleText(title3);
                    title3 = title3.substring(0, title3.indexOf("(") - 1) + " [" + carUOMLengthCode + "]";
                    pieChartsComponent.drawChart(3, dbReportAdapter.getMileageByDriverChartData(chartArguments), title3);

                    break;
                case FUEL_QTY_PIE_CHART:  //Fill-ups charts (quantity)
                    String carUOMVolume = dbReportAdapter.getUOMCode(dbReportAdapter.getCarUOMFuelID(mLastSelectedCarID, true));
                    if (carUOMVolume == null || carUOMVolume.length() <= 1) {
                        carUOMVolume = dbReportAdapter.getUOMName(dbReportAdapter.getCarUOMFuelID(mLastSelectedCarID, true));
                    }

                    chartData = dbReportAdapter.getRefuelsByTypeChartData(chartArguments, false);
                    if (chartData.size() > 0) {
                        pieChartsComponent.setChartsLineHeight(false);
                        chartFooterText = String.format(getString(R.string.chartFooterText), Utils.numberToString(new BigDecimal(chartData.get(0).totalValue), true, 2, RoundingMode.HALF_UP)
                                + " " + carUOMVolume) + " (" + getChartDataPeriodText() + ")";
                    }
                    else {
                        pieChartsComponent.setChartsLineHeight(true);
                        chartFooterText = null;
                    }

                    pieChartsComponent.setChartFooterText(chartFooterText);

                    title1 = getString(R.string.fillUpQuantityChart1Title);
                    pieChartsComponent.setChart1TitleText(title1);
                    title1 = title1.substring(0, title1.indexOf("(") - 1) + " [" + carUOMVolume + "]";
                    pieChartsComponent.drawChart(1, chartData, title1);

                    title2 = getString(R.string.fillUpQuantityChart2Title);
                    pieChartsComponent.setChart2TitleText(title2);
                    title2 = title2.substring(0, title2.indexOf("(") - 1) + " [" + carUOMVolume + "]";
                    pieChartsComponent.drawChart(2, dbReportAdapter.getRefuelsByTagChartData(chartArguments, false), title2);

                    title3 = getString(R.string.fillUpQuantityChart3Title);
                    pieChartsComponent.setChart3TitleText(title3);
                    title3 = title3.substring(0, title3.indexOf("(") - 1) + " [" + carUOMVolume + "]";
                    pieChartsComponent.drawChart(3, dbReportAdapter.getRefuelsByFuelTypeChartData(chartArguments, false), title3);
                    break;
                case FUEL_VALUE_PIE_CHART:  //Fill-ups charts (value)
                    //fill-up charts (value)
                    chartData = dbReportAdapter.getRefuelsByTypeChartData(chartArguments, true);
                    if (chartData.size() > 0) {
                        pieChartsComponent.setChartsLineHeight(false);
                        chartFooterText = String.format(getString(R.string.chartFooterText), Utils.numberToString(new BigDecimal(chartData.get(0).totalValue), true, 2, RoundingMode.HALF_UP)
                                + " " + carCurrencyCode) + " (" + getChartDataPeriodText() + ")";
                    }
                    else {
                        pieChartsComponent.setChartsLineHeight(true);
                        chartFooterText = null;
                    }

                    pieChartsComponent.setChartFooterText(chartFooterText);

                    title1 = getString(R.string.fillUpValueChart1Title);
                    pieChartsComponent.setChart1TitleText(title1);
                    title1 = title1.substring(0, title1.indexOf("(") - 1) + " [" + carCurrencyCode + "]";
                    pieChartsComponent.drawChart(1, chartData, title1);

                    title2 = getString(R.string.fillUpValueChart2Title);
                    pieChartsComponent.setChart2TitleText(title2);
                    title2 = title2.substring(0, title2.indexOf("(") - 1) + " [" + carCurrencyCode + "]";
                    pieChartsComponent.drawChart(2, dbReportAdapter.getRefuelsByTagChartData(chartArguments, true), title2);

                    title3 = getString(R.string.fillUpValueChart3Title);
                    pieChartsComponent.setChart3TitleText(title3);
                    title3 = title3.substring(0, title3.indexOf("(") - 1) + " [" + carCurrencyCode + "]";
                    pieChartsComponent.drawChart(3, dbReportAdapter.getRefuelsByFuelTypeChartData(chartArguments, true), title3);
                    break;
                case EXPENSES_PIE_CHART:  //Expense charts
                    chartData = dbReportAdapter.getExpensesByTypeChartData(chartArguments);
                    if (chartData.size() > 0) {
                        pieChartsComponent.setChartsLineHeight(false);
                        chartFooterText = String.format(getString(R.string.chartFooterText), Utils.numberToString(new BigDecimal(chartData.get(0).totalValue), true, 2, RoundingMode.HALF_UP)
                                + " " + carCurrencyCode) + " (" + getChartDataPeriodText() + ")";
                    }
                    else {
                        pieChartsComponent.setChartsLineHeight(true);
                        chartFooterText = null;
                    }
                    pieChartsComponent.setChartFooterText(chartFooterText);

                    title1 = getString(R.string.expenseChart1Title);
                    pieChartsComponent.setChart1TitleText(title1);
                    title1 = title1.substring(0, title1.indexOf("(") - 1) + " [" + carCurrencyCode + "]";
                    pieChartsComponent.drawChart(1, chartData, title1);

                    title2 = getString(R.string.expenseChart2Title);
                    pieChartsComponent.setChart2TitleText(title2);
                    title2 = title2.substring(0, title2.indexOf("(") - 1) + " [" + carCurrencyCode + "]";
                    pieChartsComponent.drawChart(2, dbReportAdapter.getExpensesByCategoryChartData(chartArguments), title2);

                    title3 = getString(R.string.expenseChart3Title);
                    pieChartsComponent.setChart3TitleText(title3);
                    title3 = title3.substring(0, title3.indexOf("(") - 1) + " [" + carCurrencyCode + "]";
                    pieChartsComponent.drawChart(3, dbReportAdapter.getExpensesByTagChartData(chartArguments), title3);
                    break;
            }

        }
        catch (Exception e) {
            mErrorInDrawCharts = true;
            Utils.showReportableErrorDialog(this, null, e.getMessage(), e);
        }

        try {
            dbReportAdapter.close();
        }
        catch (Exception ignored) {
        }


    }

    private void fillContent() {
        if (mPreferences.getBoolean(getString(R.string.pref_key_main_show_next_todo), true)) {
            llToDoZone.setVisibility(View.VISIBLE);
            fillToDoZone();
        }
        else {
            llToDoZone.setVisibility(View.GONE);
        }

        RecordComponent recordComponent;
        PieChartsComponent pieChartComponent;
        LineChartComponent lineChartComponent;
        StatisticsComponent statisticsComponent;
        String zoneContent;
        LinearLayout zoneContainer = findViewById(R.id.zoneContainer);
        if (zoneContainer == null) {
            return;
        }

        zoneContainer.removeAllViews();

        mIsCanShowFilterMenu = false;

        for (int i = 1; i <= 12; i++) {
            switch (i) {
                case 1:
                    zoneContent = mPreferences.getString(getString(R.string.pref_key_main_zone1_content), STATISTICS_ZONE);
                    break;
                case 2:
                    zoneContent = mPreferences.getString(getString(R.string.pref_key_main_zone2_content), FUEL_PRICE_LINE_CHART);
                    break;
                case 3:
                    zoneContent = mPreferences.getString(getString(R.string.pref_key_main_zone3_content), FUEL_EFF_LINE_CHART);
                    break;
                case 4:
                    zoneContent = mPreferences.getString(getString(R.string.pref_key_main_zone4_content), LAST_TRIP_RECORD);
                    break;
                case 5:
                    zoneContent = mPreferences.getString(getString(R.string.pref_key_main_zone5_content), TRIPS_PIE_CHART);
                    break;
                case 6:
                    zoneContent = mPreferences.getString(getString(R.string.pref_key_main_zone6_content), LAST_FILL_UP_RECORD);
                    break;
                case 7:
                    zoneContent = mPreferences.getString(getString(R.string.pref_key_main_zone7_content), FUEL_QTY_PIE_CHART);
                    break;
                case 8:
                    zoneContent = mPreferences.getString(getString(R.string.pref_key_main_zone8_content), FUEL_VALUE_PIE_CHART);
                    break;
                case 9:
                    zoneContent = mPreferences.getString(getString(R.string.pref_key_main_zone9_content), LAST_EXPENSE_RECORD);
                    break;
                case 10:
                    zoneContent = mPreferences.getString(getString(R.string.pref_key_main_zone10_content), EXPENSES_PIE_CHART);
                    break;
                case 11:
                    zoneContent = mPreferences.getString(getString(R.string.pref_key_main_zone11_content), LAST_GPS_TRACK_RECORD);
                    break;
                case 12:
                    zoneContent = mPreferences.getString(getString(R.string.pref_key_main_zone12_content), DO_NOT_USE);
                    break;
                default:
                    continue;
            }

            if (!zoneContent.equals(DO_NOT_USE)) {
                switch (zoneContent) {
                    case TRIPS_PIE_CHART:
                    case FUEL_QTY_PIE_CHART:
                        if (!mIsAlternateFuelVehicle) {
                            mIsCanShowFilterMenu = true;
                            pieChartComponent = new PieChartsComponent(this);
                            zoneContainer.addView(pieChartComponent);
                            drawPieCharts(pieChartComponent, zoneContent);
                        }
                        break;
                    case FUEL_VALUE_PIE_CHART:
                    case EXPENSES_PIE_CHART:  //pie charts
                        mIsCanShowFilterMenu = true;
                        pieChartComponent = new PieChartsComponent(this);
                        zoneContainer.addView(pieChartComponent);
                        drawPieCharts(pieChartComponent, zoneContent);
                        break;
                    case FUEL_CONS_LINE_CHART:
                    case FUEL_EFF_LINE_CHART:
                        if (!mIsAlternateFuelVehicle) {
                            String title;
                            if (mCarUOMVolumeCode != null) {
                                title = zoneContent.equals(FUEL_CONS_LINE_CHART) ? mCarUOMVolumeCode + " / 100 " + mCarUOMLengthCode : mCarUOMLengthCode + " / " + mCarUOMVolumeCode;
                            } else {
                                title = zoneContent.equals(FUEL_CONS_LINE_CHART) ? getString(R.string.gen_fuel_cons) : getString(R.string.gen_fuel_efficiency_long);
                            }

                            lineChartComponent = new LineChartComponent(this, zoneContent.equals(FUEL_CONS_LINE_CHART) ? LineChartComponent.SHOW_FUEL_CONS : LineChartComponent.SHOW_FUEL_EFF,
                                    title);
                            zoneContainer.addView(lineChartComponent);
                        }
                        break;
                    case FUEL_PRICE_LINE_CHART:
                        lineChartComponent = new LineChartComponent(this, LineChartComponent.SHOW_FUEL_PRICE_EVOLUTION, null);
                        zoneContainer.addView(lineChartComponent);
                        break;
                    case STATISTICS_ZONE:
                        mIsCanShowFilterMenu = true;
                        statisticsComponent = new StatisticsComponent(this);
                        zoneContainer.addView(statisticsComponent);
                        fillStatisticsZone(statisticsComponent);
                        break;
                    default:
                        recordComponent = new RecordComponent(this);
                        zoneContainer.addView(recordComponent);
                        fillLastRecord(recordComponent, zoneContent);
                        break;
                }
            }
        }

        if (mMenu != null) {
            mMenu.clear();
            if (mIsCanShowFilterMenu) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.main_activity_pie_chart_filter_menu, mMenu);
            }
        }
    }

    @Override
    public void onFinishSearchDialog(Bundle searchParams) {
        mChartFilterType = CHART_FILTER_CUSTOM_PERIOD;

        if (searchParams.containsKey(DateRangeSearchDialogFragment.DATE_FROM_IN_MILLIS_KEY)) {
            mChartPeriodStartInSeconds = searchParams.getLong(DateRangeSearchDialogFragment.DATE_FROM_IN_MILLIS_KEY) / 1000;
        }
        else {
            mChartPeriodStartInSeconds = -1;
        }
        if (searchParams.containsKey(DateRangeSearchDialogFragment.DATE_TO_IN_MILLIS_KEY)) {
            mChartPeriodEndInSeconds = searchParams.getLong(DateRangeSearchDialogFragment.DATE_TO_IN_MILLIS_KEY) / 1000;
        }
        else {
            mChartPeriodEndInSeconds = -1;
        }

        if (mChartPeriodStartInSeconds == -1 && mChartPeriodEndInSeconds == -1) {
            mChartFilterType = CHART_FILTER_ALL;
        }

        fillContent();
    }

    @SuppressLint("WrongConstant")
    private void fillToDoZone() {
        final boolean toDoExists;
        Bundle whereConditions = new Bundle();
        TextView tvToDoText1;
        TextView tvToDoText2;

        tvToDoText1 = findViewById(R.id.tvToDoText1);
        tvToDoText2 = findViewById(R.id.tvToDoText2);

        whereConditions.putString(DBReportAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__ISDONE) + "=", "N");
        DBReportAdapter reportDb = new DBReportAdapter(this, DBReportAdapter.TODO_LIST_SELECT_NAME, whereConditions);
        Cursor listCursor = reportDb.fetchReport(1);
        if (listCursor != null && listCursor.moveToFirst()) {
            toDoExists = true;
            tvToDoText2.setVisibility(View.VISIBLE);

            mLastToDoId = listCursor.getLong(0);
            mLastToDoTaskId = listCursor.getLong(11);
            mLastToDoCarId = listCursor.getLong(13);
            String dataString = listCursor.getString(1);
            if (dataString == null) {
                dataString = "";
            }

            if (dataString.contains("[#5]")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvToDoText1.setTextColor(getResources().getColor(R.color.todo_overdue_text_color, getApplicationContext().getTheme()));
                }
                else {
                    //noinspection deprecation
                    tvToDoText1.setTextColor(getResources().getColor(R.color.todo_overdue_text_color));
                }
            }
            else if (dataString.contains("[#15]")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvToDoText1.setTextColor(getResources().getColor(R.color.todo_done_text_color, getApplicationContext().getTheme()));
                }
                else {
                    //noinspection deprecation
                    tvToDoText1.setTextColor(getResources().getColor(R.color.todo_done_text_color));
                }
            }
            else {
                tvToDoText1.setTextColor(Color.BLACK);
            }

            tvToDoText1.setText(dataString.replace("[#1]", "")
                    .replace("[#2]", ": ").replace("[#3]", getString(R.string.gen_car_label))
                    .replace("; [#4] [#6]", "")
                    .replace("[#4]", getString(R.string.todo_status_label))
                    .replace("[#5]", getString(R.string.todo_overdue_label))
                    .replace("[#6]", getString(R.string.todo_scheduled_label))
                    .replace("[#15]", getString(R.string.todo_done_label)));

            long time = System.currentTimeMillis();
            Calendar now = Calendar.getInstance();
            Calendar cal = Calendar.getInstance();

            long estMileageDueDays = listCursor.getLong(7);
            String timeStr = "";
            if (estMileageDueDays >= 0) {
                if (estMileageDueDays == 99999999999L) {
                    timeStr = getString(R.string.todo_estimated_mileage_date_no_data);
                }
                else {
                    if (listCursor.getString(1).contains("[#5]")) {
                        timeStr = getString(R.string.todo_overdue_label);
                    }
                    else {
                        cal.setTimeInMillis(time + (estMileageDueDays * ConstantValues.ONE_DAY_IN_MILLISECONDS));
                        if (cal.get(Calendar.YEAR) - now.get(Calendar.YEAR) > 5) {
                            timeStr = getString(R.string.todo_estimated_mileage_date_too_far);
                        }
                        else {
                            if (cal.getTimeInMillis() - now.getTimeInMillis() < 365 * ConstantValues.ONE_DAY_IN_MILLISECONDS) // 1 year
                            {
                                timeStr = Utils.getFormattedDateTime(time + (estMileageDueDays * ConstantValues.ONE_DAY_IN_MILLISECONDS), false);
                            }
                            else {
                                timeStr = DateFormat.format("MMM, yyyy", cal).toString();
                            }

                        }
                    }
                }
            }
            if (listCursor.getString(2) != null) {
                tvToDoText2.setText(listCursor
                        .getString(2)
                        .replace("[#7]", getString(R.string.todo_scheduled_date_label))
                        .replace(
                                "[#8]",
                                Utils.getFormattedDateTime(listCursor.getLong(4) * 1000, false))
                        .replace("[#9]", getString(R.string.gen_or)).replace("[#10]", getString(R.string.todo_scheduled_mileage_label))
                        .replace("[#11]", Utils.numberToString(listCursor.getDouble(5), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH))
                        .replace("[#12]", getString(R.string.todo_mileage)).replace("[#13]", getString(R.string.todo_estimated_mileage_date))
                        .replace("[#14]", timeStr));
            }
        }
        else {
            toDoExists = false;
            tvToDoText1.setText(R.string.main_activity_no_to_do);
            tvToDoText2.setVisibility(View.GONE);
        }

        llToDoZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toDoExists) {
                    String sql = " SELECT * " + " FROM " + DBAdapter.TABLE_NAME_TASK + " WHERE "
                            + DB.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TASK, DBAdapter.COL_NAME_GEN_ROWID) + " = ?";
                    String argValues[] = {Long.toString(mLastToDoTaskId)};
                    DBAdapter dbAdapter = new DBAdapter(MainActivity.this);
                    Cursor taskCursor = dbAdapter.query(sql, argValues);
                    int notificationTrigger = -1;
                    String minutesOrDays;

                    if (!taskCursor.moveToFirst()) {
                        Toast.makeText(MainActivity.this, "Unknown error", Toast.LENGTH_LONG).show();
                        try {
                            taskCursor.close();
                            dbAdapter.close();
                        }
                        catch (Exception ignored) {
                        }
                        return;
                    }

                    if (taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_TIME)
                            || taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_BOTH)) {
                        notificationTrigger = ToDoNotificationJob.TRIGGERED_BY_TIME;
                    }
                    if (taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE)
                            || taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_BOTH)) {
                        notificationTrigger = ToDoNotificationJob.TRIGGERED_BY_MILEAGE;
                    }

                    if (taskCursor.getInt(DBAdapter.COL_POS_TASK__TIMEFREQUENCYTYPE) == TaskEditFragment.TASK_TIME_FREQUENCY_TYPE_DAILY) {
                        minutesOrDays = MainActivity.this.getString(R.string.gen_minutes);
                    }
                    else {
                        minutesOrDays = MainActivity.this.getString(R.string.gen_days);
                    }

                    Intent i = new Intent(MainActivity.this, ToDoNotificationDialogActivity.class);
                    i.putExtra(ToDoNotificationJob.TODO_ID_KEY, mLastToDoId);
                    i.putExtra(ToDoNotificationDialogActivity.TRIGGERED_BY_KEY, notificationTrigger);
                    i.putExtra(ToDoNotificationDialogActivity.CAR_UOM_CODE_KEY, dbAdapter.getUOMCode(dbAdapter.getCarUOMLengthID(mLastToDoCarId)));
                    i.putExtra(ToDoNotificationDialogActivity.MINUTES_OR_DAYS_KEY, minutesOrDays);
                    i.putExtra(ToDoNotificationDialogActivity.STARTED_FROM_NOTIFICATION_KEY, false);

                    MainActivity.this.startActivity(i);

                    try {
                        taskCursor.close();
                        dbAdapter.close();
                    }
                    catch (Exception ignored) {
                    }
                }
                else {
                    Intent intent = new Intent(MainActivity.this, CommonDetailActivity.class);
                    intent.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_TODO);
                    intent.putExtra(BaseEditFragment.RECORD_ID_KEY, -1L);
                    MainActivity.this.startActivity(intent);
                }
            }
        });


        try {
            if (listCursor != null) {
                listCursor.close();
            }
            reportDb.close();
        }
        catch (Exception ignored) {
        }
    }

    @SuppressLint("SetTextI18n")
    private void fillStatisticsZone(StatisticsComponent statisticsComponent) {
        Cursor listCursor;
        Bundle whereConditions = new Bundle();
        whereConditions.putString(DBReportAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_CAR, DBAdapter.COL_NAME_GEN_ROWID) + "=", String.valueOf(mLastSelectedCarID));
        if (mChartPeriodStartInSeconds > -1) {
            whereConditions.putString("DateFrom", Long.toString(mChartPeriodStartInSeconds));
        }
        if (mChartPeriodEndInSeconds > -1) {
            whereConditions.putString("DateTo", Long.toString(mChartPeriodEndInSeconds));
        }

        DBReportAdapter reportDb = new DBReportAdapter(this, DBReportAdapter.STATISTICS_SELECT_NAME, whereConditions);
        listCursor = reportDb.fetchReport(1);
        if (listCursor != null && listCursor.moveToFirst()) {

//            llStatisticsZone.setVisibility(View.VISIBLE);

            BigDecimal startIndex = null;
            BigDecimal stopIndex = null;
            BigDecimal mileage;
            BigDecimal expenses = null;
            try {
                startIndex = new BigDecimal(listCursor.getDouble(1)).setScale(ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH);
                stopIndex = new BigDecimal(listCursor.getDouble(2)).setScale(ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH);
                expenses = new BigDecimal(listCursor.getDouble(4)).setScale(ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT);

            }
            catch (NumberFormatException ignored) {
            }
            String carUOMLengthCode = listCursor.getString(3);
            String carUOMVolumeCode = listCursor.getString(7);
            String carCurrencyCode = listCursor.getString(6);

            if (startIndex != null && stopIndex != null) {
                mileage = stopIndex.subtract(startIndex);
            }
            else {
                mileage = BigDecimal.ZERO;
            }

            statisticsComponent.setHeaderText(getString(R.string.main_activity_statistics_header_caption) + " "
                    + Utils.numberToString(mileage, true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) + " " + carUOMLengthCode + " (" + getChartDataPeriodText() + ")");
            statisticsComponent.setLastKnownOdometerText(getString(R.string.main_activity_statistics_last_odometer_label) + " "
                    + Utils.numberToString(stopIndex, true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) + " " + carUOMLengthCode);
            statisticsComponent.setTotalExpensesText(getString(R.string.main_activity_statistics_total_expense_label) + " "
                    + (expenses != null ? Utils.numberToString(expenses, true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) : "0") + " "
                    + carCurrencyCode);

            // mileage expense
            BigDecimal mileageExpense;
            BigDecimal mileageEff;
            String mileageExpenseStr = null;
            if (listCursor.getString(5) != null) {
                try {
                    expenses = new BigDecimal(listCursor.getDouble(5)).setScale(ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT);
                }
                catch (NumberFormatException ignored) {
                }
            }
            if (expenses != null && mileage != null && mileage.signum() != 0) {
                mileageExpense = expenses.multiply(new BigDecimal("100"));
                mileageExpense = mileageExpense.divide(mileage, 10, RoundingMode.HALF_UP).setScale(ConstantValues.DECIMALS_AMOUNT,
                        ConstantValues.ROUNDING_MODE_AMOUNT);
                if (mileageExpense != null && (mileageExpense.setScale(10, RoundingMode.HALF_UP)).signum() != 0) {
                    mileageExpenseStr = Utils.numberToString(mileageExpense, true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " "
                            + carCurrencyCode + "/100 " + carUOMLengthCode;
                    if (mileageExpense.signum() != 0) {
                        mileageEff = ((new BigDecimal("100")).divide(mileageExpense, 10, RoundingMode.HALF_UP)).setScale(ConstantValues.DECIMALS_AMOUNT,
                                ConstantValues.ROUNDING_MODE_AMOUNT);
                        if (mileageEff != null) {
                            mileageExpenseStr = mileageExpenseStr + "; "
                                    + Utils.numberToString(mileageEff, true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT) + " "
                                    + carUOMLengthCode + "/" + carCurrencyCode;
                        }
                    }
                }
            }
            String mileageExpenseText = getString(R.string.main_activity_statistics_mileage_expense_label) + " "
                    + (mileageExpenseStr != null ? mileageExpenseStr : "N/A");
            statisticsComponent.setMileageExpenseText(mileageExpenseText);

            if (!mIsAlternateFuelVehicle) {
                // fuel efficiency
                Cursor c;
                String fuelEffStr = "";
                String lastFuelEffStr = "";
                String sql;
                BigDecimal tmpFullRefuelIndex;
                BigDecimal lastFullRefuelIndex;
                BigDecimal totalFuelQty = null;

                // select first full refuel index
                //@formatter:off
            sql = "SELECT " + DBAdapter.COL_NAME_REFUEL__INDEX +
                    " FROM " + DBAdapter.TABLE_NAME_REFUEL +
                    " WHERE " +
                        DBAdapter.COL_NAME_REFUEL__CAR_ID + " = " + mLastSelectedCarID + " " + " AND " + DBAdapter.COL_NAME_GEN_ISACTIVE + " = \'Y\' " +
                        " AND " + DBAdapter.COL_NAME_REFUEL__ISFULLREFUEL + " = \'Y\' " +
                            (mChartPeriodStartInSeconds > 0 ? " AND Date >= " + mChartPeriodStartInSeconds : "") +
                            (mChartPeriodEndInSeconds > 0 ? " AND Date <= " + mChartPeriodEndInSeconds : "") +
                    " ORDER BY " + DBAdapter.COL_NAME_REFUEL__INDEX + " ASC " +
                    " LIMIT 1";
            //@formatter:on
                c = reportDb.execSelectSql(sql, null);

                if (c.moveToFirst()) {
                    tmpFullRefuelIndex = new BigDecimal(c.getDouble(0)).setScale(ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH);

                    c.close();
                    // get the last full refuel index
                    //@formatter:off
                sql = "SELECT " + DBAdapter.COL_NAME_REFUEL__INDEX +
                        " FROM " + DBAdapter.TABLE_NAME_REFUEL +
                        " WHERE " +
                            DBAdapter.COL_NAME_REFUEL__CAR_ID + " = " + mLastSelectedCarID + " " +
                            " AND " + DBAdapter.COL_NAME_GEN_ISACTIVE + " = \'Y\' " +
                            " AND " + DBAdapter.COL_NAME_REFUEL__ISFULLREFUEL + " = \'Y\' " +
                            " AND " + DBAdapter.COL_NAME_REFUEL__INDEX + " <> " + tmpFullRefuelIndex.toPlainString() +
                            (mChartPeriodStartInSeconds > 0 ? " AND Date >= " + mChartPeriodStartInSeconds : "") +
                            (mChartPeriodEndInSeconds > 0 ? " AND Date <= " + mChartPeriodEndInSeconds : "") +
                        " ORDER BY " + DBAdapter.COL_NAME_REFUEL__INDEX + " DESC " + " LIMIT 1";
                //@formatter:on
                    c = reportDb.execSelectSql(sql, null);
                    if (c.moveToFirst()) {
                        lastFullRefuelIndex = new BigDecimal(c.getDouble(0)).setScale(ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH);
                        c.close();
                        if (lastFullRefuelIndex != null && lastFullRefuelIndex.subtract(tmpFullRefuelIndex).signum() != 0) {
                            // get the total fuel quantity between the first and last refuels
                            //@formatter:off
                        sql = "SELECT SUM(" + DBAdapter.COL_NAME_REFUEL__QUANTITY + ") " +
                                " FROM " + DBAdapter.TABLE_NAME_REFUEL +
                                " WHERE " +
                                    DBAdapter.COL_NAME_REFUEL__CAR_ID + " = " + mLastSelectedCarID + " " +
                                    (mChartPeriodStartInSeconds > 0 ? " AND Date >= " + mChartPeriodStartInSeconds : "") +
                                    (mChartPeriodEndInSeconds > 0 ? " AND Date <= " + mChartPeriodEndInSeconds : "") +
                                    " AND " + DBAdapter.COL_NAME_GEN_ISACTIVE + " = \'Y\' " +
                                    " AND " + DBAdapter.COL_NAME_REFUEL__INDEX + " > " + tmpFullRefuelIndex.toPlainString() +
                                    " AND " + DBAdapter.COL_NAME_REFUEL__INDEX + " <= " + lastFullRefuelIndex.toPlainString();
                        //@formatter:on
                            c = reportDb.execSelectSql(sql, null);
                            if (c.moveToFirst()) {
                                try {
                                    totalFuelQty = new BigDecimal(c.getDouble(0)).setScale(10, ConstantValues.ROUNDING_MODE_VOLUME);
                                    // new BigDecimal(c.getString(0));
                                } catch (NumberFormatException ignored) {
                                }
                            }

                            c.close();
                            if (totalFuelQty != null) {
                                // calculate the avg cons and fuel eff.
                                BigDecimal avgCons;
                                avgCons = totalFuelQty.multiply(new BigDecimal("100"));
                                avgCons = avgCons.divide(lastFullRefuelIndex.subtract(tmpFullRefuelIndex), 10, RoundingMode.HALF_UP).setScale(10,
                                        ConstantValues.ROUNDING_MODE_FUEL_EFF);
                                fuelEffStr = Utils.numberToString(avgCons, true, ConstantValues.DECIMALS_FUEL_EFF, ConstantValues.ROUNDING_MODE_FUEL_EFF) + " "
                                        + carUOMVolumeCode + "/100" + carUOMLengthCode;
                                // efficiency: x uom length (km or mi) / uom volume (l or gallon)
                                if (avgCons != null && avgCons.signum() != 0) {
                                    BigDecimal avgEff = (new BigDecimal("100")).divide(avgCons, 10, RoundingMode.HALF_UP).setScale(10,
                                            ConstantValues.ROUNDING_MODE_FUEL_EFF);
                                    fuelEffStr = fuelEffStr + "; "
                                            + Utils.numberToString(avgEff, true, ConstantValues.DECIMALS_FUEL_EFF, ConstantValues.ROUNDING_MODE_FUEL_EFF) + " "
                                            + carUOMLengthCode + "/" + carUOMVolumeCode;
                                }
                            }

                            // calculate the last fuel eff (for the last two full refuels)

                            // get the second last full refuel
                            //@formatter:off
                        sql = "SELECT " + DBAdapter.COL_NAME_REFUEL__INDEX +
                                " FROM " + DBAdapter.TABLE_NAME_REFUEL +
                                " WHERE " +
                                    DBAdapter.COL_NAME_REFUEL__CAR_ID + " = " + mLastSelectedCarID + " " +
                                    " AND " + DBAdapter.COL_NAME_GEN_ISACTIVE + " = \'Y\' " +
                                    " AND " + DBAdapter.COL_NAME_REFUEL__ISFULLREFUEL + " = \'Y\' " +
                                    " AND " + DBAdapter.COL_NAME_REFUEL__INDEX + " < " + lastFullRefuelIndex.toPlainString() +
                                    (mChartPeriodStartInSeconds > 0 ? " AND Date >= " + mChartPeriodStartInSeconds : "") +
                                    (mChartPeriodEndInSeconds > 0 ? " AND Date <= " + mChartPeriodEndInSeconds : "") +
                                " ORDER BY " + DBAdapter.COL_NAME_REFUEL__INDEX + " DESC " +
                                " LIMIT 1";
                        //@formatter:on
                            c = reportDb.execSelectSql(sql, null);
                            if (c.moveToFirst()) {
                                tmpFullRefuelIndex = (new BigDecimal(c.getDouble(0)).setScale(10, ConstantValues.ROUNDING_MODE_LENGTH));
                                c.close();
                                // get the total fuel qty between the last two full refuels
                                //@formatter:off
                            sql = "SELECT SUM(" + DBAdapter.COL_NAME_REFUEL__QUANTITY + ") " +
                                    " FROM " + DBAdapter.TABLE_NAME_REFUEL +
                                    " WHERE " +
                                        DBAdapter.COL_NAME_REFUEL__CAR_ID + " = " + mLastSelectedCarID + " " +
                                        (mChartPeriodStartInSeconds > 0 ? " AND Date >= " + mChartPeriodStartInSeconds : "") +
                                        (mChartPeriodEndInSeconds > 0 ? " AND Date <= " + mChartPeriodEndInSeconds : "") +
                                        " AND " + DBAdapter.COL_NAME_GEN_ISACTIVE + " = \'Y\' " +
                                        " AND " + DBAdapter.COL_NAME_REFUEL__INDEX + " > " + tmpFullRefuelIndex.toPlainString() +
                                        " AND " + DBAdapter.COL_NAME_REFUEL__INDEX + " <= " + lastFullRefuelIndex.toPlainString();
                            //@formatter:on
                                c = reportDb.execSelectSql(sql, null);
                                if (c.moveToFirst()) {
                                    if (c.getString(0) != null)
                                        totalFuelQty = new BigDecimal(c.getString(0));
                                    else
                                        totalFuelQty = null;
                                }
                                c.close();
                                if (totalFuelQty != null) {
                                    // calculate the avg cons and fuel eff.
                                    BigDecimal avgCons;
                                    avgCons = totalFuelQty.multiply(new BigDecimal("100"));
                                    avgCons = avgCons.divide(lastFullRefuelIndex.subtract(tmpFullRefuelIndex), 10, RoundingMode.HALF_UP).setScale(10,
                                            ConstantValues.ROUNDING_MODE_FUEL_EFF);
                                    lastFuelEffStr = Utils.numberToString(avgCons, true, ConstantValues.DECIMALS_FUEL_EFF, ConstantValues.ROUNDING_MODE_FUEL_EFF) + " "
                                            + carUOMVolumeCode + "/100" + carUOMLengthCode;
                                    // //efficiency: x uom length (km or mi) / uom volume (l or gallon)
                                    if (avgCons != null && avgCons.signum() != 0) {
                                        BigDecimal avgEff = (new BigDecimal("100")).divide(avgCons, 10, RoundingMode.HALF_UP).setScale(10,
                                                ConstantValues.ROUNDING_MODE_FUEL_EFF);
                                        lastFuelEffStr = lastFuelEffStr + "; "
                                                + Utils.numberToString(avgEff, true, ConstantValues.DECIMALS_FUEL_EFF, ConstantValues.ROUNDING_MODE_FUEL_EFF) + " "
                                                + carUOMLengthCode + "/" + carUOMVolumeCode;
                                    }
                                }
                                c.close();
                            } else
                                c.close();
                        }
                    } else
                        c.close(); // no last full refuel => no 2 full refuels => cannot calculate fuel eff.
                } else { // no full refuel recorded
                    c.close();
                }

                if (mChartPeriodEndInSeconds > 0)
                    lastFuelEffStr = ""; //do not show last fuel eff when the period is not current month or current year

                if (fuelEffStr.length() > 0) {
                    statisticsComponent.setAvgFuelEffText(getString(R.string.main_activity_statistics_avg_cons_label) + " " + fuelEffStr);
                } else
                    statisticsComponent.setAvgFuelEffText(null);

                if (lastFuelEffStr.length() > 0) {
                    statisticsComponent.setLastFuelEffText(getString(R.string.main_activity_statistics_last_cons_label) + " " + lastFuelEffStr);
                } else
                    statisticsComponent.setLastFuelEffText(null);
            }
        } else {
            statisticsComponent.setHeaderText(getString(R.string.statistics_no_data));
        }

        try {
            if (listCursor != null)
                listCursor.close();
            reportDb.close();
        }
        catch (Exception ignored){}
    }

    @SuppressLint("WrongConstant")
    private void fillShortAbout() {
        String appVersion = null;

        try {
            appVersion = Utils.getAppVersion(this);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String abt = String.format(getString(R.string.app_short_about), Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));

        TextView tvShortAboutLbl = findViewById(R.id.tvShortAboutLbl);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvShortAboutLbl.setText(Html.fromHtml(abt, Html.FROM_HTML_MODE_COMPACT));
        }
        else
            //noinspection deprecation
            tvShortAboutLbl.setText(Html.fromHtml(abt));

        ((TextView) findViewById(R.id.tvShortAboutAppVersion)).setText(
                String.format(getString(R.string.main_activity_app_version), appVersion, mDbVersion));
    }
}
