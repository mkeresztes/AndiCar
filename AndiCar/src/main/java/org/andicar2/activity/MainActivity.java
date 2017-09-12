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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuPopup;
import com.shehabic.droppy.animations.DroppyFadeInAnimation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import andicar.n.activity.CommonDetailActivity;
import andicar.n.activity.CommonListActivity;
import andicar.n.activity.dialogs.ChartDetailDialog;
import andicar.n.activity.dialogs.DateRangeSearchDialogFragment;
import andicar.n.activity.dialogs.GPSTrackControllerDialogActivity;
import andicar.n.activity.dialogs.ToDoNotificationDialogActivity;
import andicar.n.activity.fragment.BaseEditFragment;
import andicar.n.activity.fragment.TaskEditFragment;
import andicar.n.activity.miscellaneous.AboutActivity;
import andicar.n.activity.preference.PreferenceActivity;
import andicar.n.persistence.DB;
import andicar.n.persistence.DBAdapter;
import andicar.n.persistence.DBReportAdapter;
import andicar.n.service.ToDoNotificationService;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;
import andicar.n.view.AndiCarPieChart;
import andicar.n.view.MainNavigationView;


/**
 * @author miki
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DateRangeSearchDialogFragment.DateRangeSearchDialogFragmentListener {

    private static final int CLICK_ACTION_THRESHOLD = 200;
    private static final int REQUEST_CODE_ADD_CAR = 1;
    private static final int REQUEST_CODE_SETTINGS = 2;
    private static final int REQUEST_CODE_CHART_DETAIL = 3;
    private static final int CHART_FILTER_ALL = 1;
    private static final int CHART_FILTER_CURRENT_MONTH = 2;
    private static final int CHART_FILTER_PREVIOUS_MONTH = 3;
    private static final int CHART_FILTER_CURRENT_YEAR = 4;
    private static final int CHART_FILTER_PREVIOUS_YEAR = 5;
    private static final int CHART_FILTER_CUSTOM_PERIOD = 6;
    //used to simulate onClick via onTouch (pie chart not detect the standard onClick event)
    private long mLastTouchDown;
    private boolean mRedrawCharts = true;
    private boolean mErrorInDrawCharts = false;
    private int mChartFilterType = 1;
    private long mChartPeriodStartInSeconds = -1;
    private long mChartPeriodEndInSeconds = -1;
    private long mLastSelectedCarID = -1;
    private String mDbVersion;
    private SharedPreferences mPreferences;
    private MainNavigationView mNavigationView;
    private DrawerLayout mNavViewDrawer;
    private TextView tvChartsHdr;
    private long mLastToDoId = -1;
    private long mLastToDoTaskId = -1;
    private long mLastToDoCarId = -1;

    View llStatisticsZone;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //needed for inflating xml image resources for main_activity_popup_menu in 4x platform
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.main_activity);

        tvChartsHdr = (TextView) findViewById(R.id.tvChartsHdr);
        llStatisticsZone = findViewById(R.id.llStatisticsZone);

        if (savedInstanceState != null) {
            mChartFilterType = savedInstanceState.getInt("mChartFilterType", 1);
            mChartPeriodStartInSeconds = savedInstanceState.getLong("mChartPeriodStartInSeconds", -1);
            mChartPeriodEndInSeconds = savedInstanceState.getLong("mChartPeriodEndInSeconds", -1);
            fillChartHeaderCaption();
        }
        else {
            tvChartsHdr.setText(R.string.chart_filter_all_header_caption);
        }

        //set up the main toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNavViewDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, mNavViewDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert mNavViewDrawer != null;
        mNavViewDrawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        mNavigationView = (MainNavigationView) findViewById(R.id.nav_view);
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


        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("google.message_id")
                && getIntent().getExtras().getString("msg.title", "").length() > 0
                && getIntent().getExtras().getString("msg.body", "").length() > 0) {
            Utils.showInfoDialog(this, getIntent().getExtras().getString("msg.title", ""), getIntent().getExtras().getString("msg.body", ""));
//            Intent notif = new Intent(this, GeneralNotificationDialogActivity.class);
//            notif.putExtra(GeneralNotificationDialogActivity.NOTIF_MESSAGE_KEY, getIntent().getExtras().getString("msg.title", ""));
//            notif.putExtra(GeneralNotificationDialogActivity.NOTIF_DETAIL_KEY, getIntent().getExtras().getString("msg.body", ""));
//            notif.putExtra(GeneralNotificationDialogActivity.DIALOG_TYPE_KEY, GeneralNotificationDialogActivity.DIALOG_TYPE_INFO);
//            startActivity(notif);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();

        //clear the secondary menu to avoid duplicate items
        mNavigationView.clearSecondaryMenuEntries();
        mNavigationView.mForceSecondary = false;

        if (mNavigationView.getMenu().findItem(R.id.nav_rate) != null && Utils.isCanSHowRateApp(this)) {
            mNavigationView.getMenu().findItem(R.id.nav_rate).setVisible(true);
        }

        //
        // get the list of active cars from the database
        //
        String columnsToGet[] = {DBAdapter.COL_NAME_GEN_ROWID, DBAdapter.COL_NAME_GEN_NAME, DBAdapter.COL_NAME_GEN_USER_COMMENT, DBAdapter.COL_NAME_CAR__REGISTRATIONNO};
        String selectionCondition = DBAdapter.COL_NAME_GEN_ISACTIVE + "=?";
        String[] selectionArgs = {"Y"};
        DBAdapter db = new DBAdapter(this);

        mDbVersion = Integer.toString(db.getVersion());
        setShortAbout();

        Cursor c = db.query(DBAdapter.TABLE_NAME_CAR, columnsToGet, selectionCondition, selectionArgs, DBAdapter.COL_NAME_GEN_NAME);
        boolean tmpCheck = false;

        //get the last selected car
        mLastSelectedCarID = mPreferences.getLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_car_id), -1);


        //add the cars to the secondary menu
        while (c.moveToNext()) {
            mNavigationView.addSecondaryMenuEntry(c.getInt(0), c.getString(1), R.drawable.ic_menu_car_black);
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
        db.close();

        //force a redraw of the menu if the secondary is active
        if (mNavigationView.getMenuType() == MainNavigationView.MENU_TYPE_SECONDARY) {
            mNavigationView.changeMenuLayout(MainNavigationView.MENU_TYPE_SECONDARY);
        }

        //for debug
        TextView tvDebugInfo = (TextView) findViewById(R.id.tvDebugInfo);
        if (tvDebugInfo != null) {
            if (BuildConfig.DEBUG && ConstantValues.DEBUG_IS_SHOW_INFO_IN_FRAGMENTS) {
                Display display = getWindowManager().getDefaultDisplay();
                float density = getResources().getDisplayMetrics().density;
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                String debugInfo = tvDebugInfo.getText().toString();
                debugInfo = debugInfo.substring(0, debugInfo.indexOf(";", 0) + 1) + " Size in pixel: " + width + " x " + height + "; Size in dp: " + (width / density) + " x " + (height / density) + "; Density: " + density;
                tvDebugInfo.setText(debugInfo);
            }
            else {
                tvDebugInfo.setVisibility(View.GONE);
            }
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (mLastSelectedCarID > -1) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (mPreferences.getString(getString(R.string.pref_key_main_addbtn), "")) {
                        case "0":
                            MainActivity.this.showPopup(v);
                            break;
                        case "1":
                            showNewRecordActivity(R.id.mnuTrip);
                            break;
                        case "2":
                            showNewRecordActivity(R.id.mnuRefuel);
                            break;
                        case "3":
                            showNewRecordActivity(R.id.mnuExpense);
                            break;
                        case "4":
                            showNewRecordActivity(R.id.mnuGPSTrack);
                            break;
                        default:
                            MainActivity.this.showPopup(v);
                    }
                }
            });

            fab.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    MainActivity.this.showPopup(view);
                    return true;
                }
            });
        }
        else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDefineCar();
                }
            });
        }

        //charting
        fillContent();
    }

    private void fillChartHeaderCaption() {
        switch (mChartFilterType) {
            case CHART_FILTER_ALL:
                tvChartsHdr.setText(R.string.chart_filter_all_header_caption);
                break;
            case CHART_FILTER_CURRENT_MONTH:
                tvChartsHdr.setText(R.string.chart_filter_current_month_header_caption);
                break;
            case CHART_FILTER_PREVIOUS_MONTH:
                tvChartsHdr.setText(R.string.chart_filter_previous_month_header_caption);
                break;
            case CHART_FILTER_CURRENT_YEAR:
                tvChartsHdr.setText(R.string.chart_filter_current_year_header_caption);
                break;
            case CHART_FILTER_PREVIOUS_YEAR:
                tvChartsHdr.setText(R.string.chart_filter_previous_year_header_caption);
                break;
            case CHART_FILTER_CUSTOM_PERIOD:
                tvChartsHdr.setText(
                        String.format(getString(R.string.chart_filter_custom_period_header_caption),
                                mChartPeriodStartInSeconds > 0 ?
                                        Utils.getFormattedDateTime(mChartPeriodStartInSeconds * 1000, true) :
                                        getString(R.string.chart_filter_custom_period_header_caption_value_beginning),
                                mChartPeriodEndInSeconds > 0 ?
                                        Utils.getFormattedDateTime(mChartPeriodEndInSeconds * 1000, true) :
                                        getString(R.string.chart_filter_custom_period_header_caption_value_now)));
                break;
            default:
                tvChartsHdr.setText(R.string.chart_filter_all_header_caption);
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
                        showNewRecordActivity(id);
                    }
                })
                .setPopupAnimation(new DroppyFadeInAnimation())
                .setXOffset(5)
                .setYOffset(5)
                .build();
        droppyMenu.show();
    }

    private void showNewRecordActivity(int id) {
        Intent i = null;
        if (id == R.id.mnuTrip) {
            i = new Intent(MainActivity.this, CommonDetailActivity.class);
            i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_MILEAGE);
        } else if (id == R.id.mnuRefuel) {
            i = new Intent(MainActivity.this, CommonDetailActivity.class);
            i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_REFUEL);
        } else if (id == R.id.mnuExpense) {
            i = new Intent(MainActivity.this, CommonDetailActivity.class);
            i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_EXPENSE);
        } else if (id == R.id.mnuGPSTrack) {
            i = new Intent(MainActivity.this, GPSTrackControllerDialogActivity.class);
            i.putExtra(BaseEditFragment.RECORD_ID_KEY, -1L);
        }
        if (i != null) {
            i.putExtra(BaseEditFragment.RECORD_ID_KEY, -1L);
            i.putExtra(BaseEditFragment.DETAIL_OPERATION_KEY, BaseEditFragment.DETAIL_OPERATION_NEW);
            MainActivity.this.startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        if (mNavViewDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavViewDrawer.closeDrawer(GravityCompat.START);
        } else {
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
                Utils.showNotReportableErrorDialog(this, e.getMessage(), null, false);
            }
        }
        else if (id == R.id.nav_rate) {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null) {
            menu.clear();
        }

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_chart_filter_menu, menu);
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
            fillChartHeaderCaption();
            fillContent();
        }
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
            mNavigationView.setHeaderLabels(getString(R.string.main_activity_no_car), "", "");
            setTitle(getString(R.string.main_activity_no_car));
        }
        c.close();
        db.close();

        if (needCallRun) {
            fillContent();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_CAR && resultCode == Activity.RESULT_OK) {
            setSelectedCar(data.getExtras().getInt(DBAdapter.COL_NAME_GEN_ROWID), false);
            //switch back the navigation menu to the primary layout
            mNavigationView.mForceSecondary = false;
            mNavigationView.changeMenuLayout();
        }
        else if (requestCode == REQUEST_CODE_CHART_DETAIL) {
            mRedrawCharts = false;
        }
    }

    private void drawCharts(int zone, String chartSource) {
        if (mErrorInDrawCharts) {
            return;
        }

        LinearLayout chartLine;
        LinearLayout chartContainer;
        AndiCarPieChart pChart1;
        AndiCarPieChart pChart2;
        AndiCarPieChart pChart3;
        TextView chartFooterText;
        String title1;
        String title2;
        String title3;

        switch (zone) {
            case 1:
                pChart1 = (AndiCarPieChart) findViewById(R.id.chart11);
                pChart2 = (AndiCarPieChart) findViewById(R.id.chart12);
                pChart3 = (AndiCarPieChart) findViewById(R.id.chart13);
                chartFooterText = (TextView) findViewById(R.id.line1ChartFooterText);
                title1 = getString(R.string.chart11Title);
                title2 = getString(R.string.chart12Title);
                title3 = getString(R.string.chart13Title);
                chartLine = (LinearLayout) findViewById(R.id.line1Charts);
                chartContainer = (LinearLayout) findViewById(R.id.line1ChartsZone);
                break;
            case 2:
                pChart1 = (AndiCarPieChart) findViewById(R.id.chart21);
                pChart2 = (AndiCarPieChart) findViewById(R.id.chart22);
                pChart3 = (AndiCarPieChart) findViewById(R.id.chart23);
                chartFooterText = (TextView) findViewById(R.id.line2ChartFooterText);
                title1 = getString(R.string.chart21Title);
                title2 = getString(R.string.chart22Title);
                title3 = getString(R.string.chart23Title);
                chartLine = (LinearLayout) findViewById(R.id.line2Charts);
                chartContainer = (LinearLayout) findViewById(R.id.line2ChartsZone);
                break;
            case 3:
                pChart1 = (AndiCarPieChart) findViewById(R.id.chart31);
                pChart2 = (AndiCarPieChart) findViewById(R.id.chart32);
                pChart3 = (AndiCarPieChart) findViewById(R.id.chart33);
                chartFooterText = (TextView) findViewById(R.id.line3ChartFooterText);
                title1 = getString(R.string.chart31Title);
                title2 = getString(R.string.chart32Title);
                title3 = getString(R.string.chart33Title);
                chartLine = (LinearLayout) findViewById(R.id.line3Charts);
                chartContainer = (LinearLayout) findViewById(R.id.line3ChartsZone);
                break;
            case 4:
                pChart1 = (AndiCarPieChart) findViewById(R.id.chart41);
                pChart2 = (AndiCarPieChart) findViewById(R.id.chart42);
                pChart3 = (AndiCarPieChart) findViewById(R.id.chart43);
                chartFooterText = (TextView) findViewById(R.id.line4ChartFooterText);
                title1 = getString(R.string.chart41Title);
                title2 = getString(R.string.chart42Title);
                title3 = getString(R.string.chart43Title);
                chartLine = (LinearLayout) findViewById(R.id.line4Charts);
                chartContainer = (LinearLayout) findViewById(R.id.line3ChartsZone);
                break;
            default:
                return;
        }

        if (chartLine == null) {
            //TODO show an error
            return;
        }

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
        String chartFooter;

        if (chartSource.equals("DNU")) {
            chartContainer.setVisibility(View.GONE);
        } else {
            chartContainer.setVisibility(View.VISIBLE);
            try {
                if (chartSource.equals("TRC")) { //trip charts
                    String carUOMLengthCode = dbReportAdapter.getUOMCode(dbReportAdapter.getCarUOMLengthID(mLastSelectedCarID));
                    chartData = dbReportAdapter.getMileageByTypeChartData(chartArguments);
                    if (chartData.size() > 0) {
                        setChartBandHeight(chartLine, false);
                        chartFooter =
                                String.format(getString(R.string.chartFooterText), Utils.numberToString(new BigDecimal(chartData.get(0).totalValue), true, 2, RoundingMode.HALF_UP)
                                        + " " + carUOMLengthCode);
                    } else {
                        setChartBandHeight(chartLine, true);
                        chartFooter = null;
                    }
                    if (pChart1 != null) {
                        title1 = title1.substring(0, title1.indexOf("(") - 1) + " [" + carUOMLengthCode + "]";
                        drawPieChart(chartLine, pChart1, chartData, title1, chartFooterText, chartFooter);
                    }

                    if (pChart2 != null) {
                        title2 = title2.substring(0, title2.indexOf("(") - 1) + " [" + carUOMLengthCode + "]";
                        drawPieChart(chartLine, pChart2, dbReportAdapter.getMileageByTagsChartData(chartArguments), title2, null, null);
                    }

                    if (pChart3 != null) {
                        title3 = title3.substring(0, title3.indexOf("(") - 1) + " [" + carUOMLengthCode + "]";
                        drawPieChart(chartLine, pChart3, dbReportAdapter.getMileageByDriverChartData(chartArguments), title3, null, null);
                    }
                }
                else {
                    chartContainer.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                mErrorInDrawCharts = true;
                Utils.showReportableErrorDialog(this, null, e.getMessage(), e, false);
            }
        }

        try {
            dbReportAdapter.close();
        } catch (Exception ignored) {
        }


    }

    private void drawCharts() {
        if (mErrorInDrawCharts) {
            return;
        }

        DBReportAdapter dbReportAdapter = new DBReportAdapter(getApplicationContext(), null, null);

        ArrayList<String> selArgs = new ArrayList<>();
        selArgs.add(Long.toString(mLastSelectedCarID));
        if (mChartPeriodStartInSeconds > -1) {
            selArgs.add(Long.toString(mChartPeriodStartInSeconds));
        } else {
            selArgs.add(Long.toString(0));
        }

        if (mChartPeriodEndInSeconds > -1) {
            selArgs.add(Long.toString(mChartPeriodEndInSeconds));
        } else {
            selArgs.add(Long.toString(0));
        }

        String[] chartArguments = selArgs.toArray(new String[0]);

        ArrayList<DBReportAdapter.chartData> chartData;
        String chartFooter;

        try {
            LinearLayout ll;
            AndiCarPieChart pChart;
            String title;
//            //mileage charts
//            String carUOMLengthCode = dbReportAdapter.getUOMCode(dbReportAdapter.getCarUOMLengthID(mLastSelectedCarID));
//            if (ll != null) {
//                chartData = dbReportAdapter.getMileageByTypeChartData(chartArguments);
//                if (chartData.size() > 0) {
//                    setChartBandHeight(ll, false);
//                    chartFooter =
//                            String.format(getString(R.string.chartFooterText), Utils.numberToString(new BigDecimal(chartData.get(0).totalValue), true, 2, RoundingMode.HALF_UP)
//                                    + " " + carUOMLengthCode);
//                }
//                else {
//                    setChartBandHeight(ll, true);
//                    chartFooter = null;
//                }
//                pChart = (AndiCarPieChart) findViewById(R.id.chart11);
//                if (pChart != null) {
//                    title = getString(R.string.chart11Title);
//                    title = title.substring(0, title.indexOf("(") - 1) + " [" + carUOMLengthCode + "]";
//                    drawPieChart(ll, pChart, chartData, title, (TextView) findViewById(R.id.line1ChartFooterText), chartFooter);
//                }
//
//                pChart = (AndiCarPieChart) findViewById(R.id.chart12);
//                if (pChart != null) {
//                    title = getString(R.string.chart12Title);
//                    title = title.substring(0, title.indexOf("(") - 1) + " [" + carUOMLengthCode + "]";
//                    drawPieChart(ll, pChart, dbReportAdapter.getMileageByTagsChartData(chartArguments), title, null, null);
//                }
//
//                pChart = (AndiCarPieChart) findViewById(R.id.chart13);
//                if (pChart != null) {
//                    title = getString(R.string.chart13Title);
//                    title = title.substring(0, title.indexOf("(") - 1) + " [" + carUOMLengthCode + "]";
//                    drawPieChart(ll, pChart, dbReportAdapter.getMileageByDriverChartData(chartArguments), title, null, null);
//                }
//            }

            //fill-up charts (quantity)
            String carUOMVolume = dbReportAdapter.getUOMCode(dbReportAdapter.getCarUOMVolumeID(mLastSelectedCarID));
            if (carUOMVolume == null || carUOMVolume.length() <= 1) {
                carUOMVolume = dbReportAdapter.getUOMName(dbReportAdapter.getCarUOMVolumeID(mLastSelectedCarID));
            }

            ll = (LinearLayout) findViewById(R.id.line2Charts);
            if (ll != null) {
                chartData = dbReportAdapter.getRefuelsByTypeChartData(chartArguments, false);
                if (chartData.size() > 0) {
                    setChartBandHeight(ll, false);
                    chartFooter = String.format(getString(R.string.chartFooterText), Utils.numberToString(new BigDecimal(chartData.get(0).totalValue), true, 2, RoundingMode.HALF_UP)
                            + " " + carUOMVolume);
                }
                else {
                    setChartBandHeight(ll, true);
                    chartFooter = null;
                }
                pChart = (AndiCarPieChart) findViewById(R.id.chart21);
                if (pChart != null) {
                    title = getString(R.string.chart21Title);
                    title = title.substring(0, title.indexOf("(") - 1) + " [" + carUOMVolume + "]";
                    drawPieChart(ll, pChart, chartData, title, (TextView) findViewById(R.id.line2ChartFooterText), chartFooter);
                }

                pChart = (AndiCarPieChart) findViewById(R.id.chart22);
                if (pChart != null) {
                    title = getString(R.string.chart22Title);
                    title = title.substring(0, title.indexOf("(") - 1) + " [" + carUOMVolume + "]";
                    drawPieChart(ll, pChart, dbReportAdapter.getRefuelsByTagChartData(chartArguments, false), title, null, null);
                }

                pChart = (AndiCarPieChart) findViewById(R.id.chart23);
                if (pChart != null) {
                    title = getString(R.string.chart23Title);
                    title = title.substring(0, title.indexOf("(") - 1) + " [" + carUOMVolume + "]";
                    drawPieChart(ll, pChart, dbReportAdapter.getRefuelsByFuelTypeChartData(chartArguments, false), title, null, null);
                }
            }

            String carCurrencyCode = dbReportAdapter.getCurrencyCode(dbReportAdapter.getCarCurrencyID(mLastSelectedCarID));
            //fill-up charts (value)
            ll = (LinearLayout) findViewById(R.id.line3Charts);
            if (ll != null) {
                chartData = dbReportAdapter.getRefuelsByTypeChartData(chartArguments, true);
                if (chartData.size() > 0) {
                    setChartBandHeight(ll, false);
                    chartFooter = String.format(getString(R.string.chartFooterText), Utils.numberToString(new BigDecimal(chartData.get(0).totalValue), true, 2, RoundingMode.HALF_UP)
                            + " " + carCurrencyCode);
                }
                else {
                    setChartBandHeight(ll, true);
                    chartFooter = null;
                }

                pChart = (AndiCarPieChart) findViewById(R.id.chart31);
                if (pChart != null) {
                    title = getString(R.string.chart31Title);
                    title = title.substring(0, title.indexOf("(") - 1) + " [" + carCurrencyCode + "]";
                    drawPieChart(ll, pChart, chartData, title, (TextView) findViewById(R.id.line3ChartFooterText), chartFooter);
                }

                pChart = (AndiCarPieChart) findViewById(R.id.chart32);
                if (pChart != null) {
                    title = getString(R.string.chart32Title);
                    title = title.substring(0, title.indexOf("(") - 1) + " [" + carCurrencyCode + "]";
                    drawPieChart(ll, pChart, dbReportAdapter.getRefuelsByTagChartData(chartArguments, true), title, null, null);
                }

                pChart = (AndiCarPieChart) findViewById(R.id.chart33);
                if (pChart != null) {
                    title = getString(R.string.chart33Title);
                    title = title.substring(0, title.indexOf("(") - 1) + " [" + carCurrencyCode + "]";
                    drawPieChart(ll, pChart, dbReportAdapter.getRefuelsByFuelTypeChartData(chartArguments, true), title, null, null);
                }
            }

            //expense charts
            ll = (LinearLayout) findViewById(R.id.line4Charts);
            if (ll != null) {
                chartData = dbReportAdapter.getExpensesByTypeChartData(chartArguments);
                if (chartData.size() > 0) {
                    setChartBandHeight(ll, false);
                    chartFooter = String.format(getString(R.string.chartFooterText), Utils.numberToString(new BigDecimal(chartData.get(0).totalValue), true, 2, RoundingMode.HALF_UP)
                            + " " + carCurrencyCode);
                }
                else {
                    setChartBandHeight(ll, true);
                    chartFooter = null;
                }

                pChart = (AndiCarPieChart) findViewById(R.id.chart41);
                if (pChart != null) {
                    title = getString(R.string.chart41Title);
                    title = title.substring(0, title.indexOf("(") - 1) + " [" + carCurrencyCode + "]";
                    drawPieChart(ll, pChart, chartData, title, (TextView) findViewById(R.id.line4ChartFooterText), chartFooter);
                }

                pChart = (AndiCarPieChart) findViewById(R.id.chart42);
                if (pChart != null) {
                    title = getString(R.string.chart42Title);
                    title = title.substring(0, title.indexOf("(") - 1) + " [" + carCurrencyCode + "]";
                    drawPieChart(ll, pChart, dbReportAdapter.getExpensesByCategoryChartData(chartArguments), title, null, null);
                }

                pChart = (AndiCarPieChart) findViewById(R.id.chart43);
                if (pChart != null) {
                    title = getString(R.string.chart43Title);
                    title = title.substring(0, title.indexOf("(") - 1) + " [" + carCurrencyCode + "]";
                    drawPieChart(ll, pChart, dbReportAdapter.getExpensesByTagChartData(chartArguments), title, null, null);
                }
            }
        }
        catch (Exception e) {
            mErrorInDrawCharts = true;
            Utils.showReportableErrorDialog(this, null, e.getMessage(), e, false);
        }

        dbReportAdapter.close();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void drawPieChart(View parent, AndiCarPieChart pieChart, ArrayList<DBReportAdapter.chartData> chartData, String title, TextView footerTextView, String footerText) throws Exception {
        if (pieChart == null) {
            return;
        }

        pieChart.clear();
        if (chartData.size() == 0) {
            if (footerTextView != null) {
                footerTextView.setText("");
            }
            return;
            }

        setDefaultPieChartProperties(parent, pieChart);
        setChartData(pieChart, title, chartData);
        pieChart.getDescription().setEnabled(false);
        if (footerText != null && footerTextView != null) {
            footerTextView.setVisibility(View.VISIBLE);
            footerTextView.setText(footerText);
        }

        //simulate the onClick action (becouse PieChart does not receive this action).
        //the implemntation based on https://stackoverflow.com/questions/17831395/how-can-i-detect-a-click-in-an-ontouch-listener
        pieChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mLastTouchDown = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - mLastTouchDown < CLICK_ACTION_THRESHOLD) {
                            AndiCarPieChart apc = (AndiCarPieChart) view;
                            if (apc.getChartDataEntries() == null || apc.getChartDataEntries().size() == 0) {
                                Toast.makeText(MainActivity.this, getString(R.string.chart_no_data), Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            else {
                                Intent i = new Intent(MainActivity.this, ChartDetailDialog.class);
                                i.putExtra(ChartDetailDialog.CHART_DATA_EXTRAS_KEY, apc.getChartDataEntries());
                                i.putExtra(ChartDetailDialog.CHART_TITLE_KEY, apc.getTitle());
                                startActivityForResult(i, REQUEST_CODE_CHART_DETAIL);
                                return true;
                            }
                            }
                        break;
                }
                return true;

            }
        });
    }


    private void setChartBandHeight(LinearLayout ll, boolean forNoData) {
        Float multiplicand;
        if (forNoData) {
            multiplicand = 0.1f;
        }
        else {
            if (ll.getTag() != null && ll.getTag().equals(getResources().getString(R.string.chart_key_legendAtRight))) {
                multiplicand = 0.6f; //legend at right of the chart
            }
            else {
                multiplicand = 1.3f;
            }
        }
        ll.getLayoutParams().height = Math.round(Utils.getScreenWidthInPixel(this) / ll.getChildCount() * multiplicand);
        ll.requestLayout();
    }

    private void setDefaultPieChartProperties(View parent, AndiCarPieChart pieChart) {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setRotationAngle(0);
        //disable rotation of the chart by touch
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);

        pieChart.animateY(700, Easing.EasingOption.EaseInOutQuad);
        // entry label styling
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setTouchEnabled(true);

        Legend l = pieChart.getLegend();
        if (parent.getTag() != null && parent.getTag().equals(getResources().getString(R.string.chart_key_legendAtRight))) {
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        }
        else {
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        }
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(10f);
    }

    private void setChartData(AndiCarPieChart pieChart, String title, ArrayList<DBReportAdapter.chartData> chartDataEntries) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        int i;
        int mChartTop = 3;

        pieChart.setChartData(chartDataEntries);
        pieChart.setTitle(title);

        for (i = 0; i < (chartDataEntries.size() <= mChartTop ? chartDataEntries.size() : mChartTop); i++) {
            entries.add(new PieEntry(chartDataEntries.get(i).value, chartDataEntries.get(i).label + " (" +
                    String.format(Locale.getDefault(), "%.2f", chartDataEntries.get(i).value2) + "%)"));
        }

        float otherValues = 0;
        float otherValues2 = 0;
        int j;
        for (j = i; j < chartDataEntries.size(); j++) {
            otherValues = otherValues + chartDataEntries.get(j).value;
            otherValues2 = otherValues2 + chartDataEntries.get(j).value2;
        }
        if (j > i) {
            entries.add(new PieEntry(otherValues, String.format(getString(R.string.chart_label_others), otherValues2, "%")));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
//        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add the colors
        dataSet.setColors(ConstantValues.CHART_COLORS);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
//        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);

        data.setValueTextColor(Color.WHITE);
//        data.setValueTypeface(mTfLight);
        pieChart.setData(data);

        // undo all highlights
//        pieChart.highlightValues(null);

//        pieChart.setBackgroundColor(ConstantValues.CHART_COLORS.get(ConstantValues.CHART_COLORS.size()-1));

        pieChart.notifyDataSetChanged(); // let the chart know it's data changed
        pieChart.invalidate();
    }

    private void fillContent() {
        if (mRedrawCharts) {
            fillToDoZone();
            drawCharts(1, mPreferences.getString(getString(R.string.pref_key_main_zone1_content), "TRC"));
            drawCharts();
            if (mPreferences.getBoolean(getString(R.string.pref_key_main_show_statistics), true)) {
                fillStatisticsZone();
                llStatisticsZone.setVisibility(View.VISIBLE);
            } else
                llStatisticsZone.setVisibility(View.GONE);

        }
        else {
            mRedrawCharts = true; //reset to default value
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

        fillChartHeaderCaption();
        fillContent();
    }

    @SuppressLint("WrongConstant")
    private void fillToDoZone() {
        final boolean toDoExists;
        Bundle whereConditions = new Bundle();
        TextView tvToDoText1;
        TextView tvToDoText2;
//        TextView tvToDoText3;
        View llToDoZone;

        llToDoZone = findViewById(R.id.llToDoZone);
        tvToDoText1 = (TextView) findViewById(R.id.tvToDoText1);
        tvToDoText2 = (TextView) findViewById(R.id.tvToDoText2);
//        tvToDoText3 = (TextView) findViewById(R.id.tvToDoText3);

        whereConditions.putString(DBReportAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__ISDONE) + "=", "N");
        DBReportAdapter reportDb = new DBReportAdapter(this, DBReportAdapter.TODO_LIST_SELECT_NAME, whereConditions);
        Cursor listCursor = reportDb.fetchReport(1);
        if (listCursor != null && listCursor.moveToFirst()) {
            toDoExists = true;
            tvToDoText2.setVisibility(View.VISIBLE);
//            tvToDoText3.setVisibility(View.VISIBLE);

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
                        cal.setTimeInMillis(time + (estMileageDueDays * ConstantValues.ONE_DAY_IN_MILISECONDS));
                        if (cal.get(Calendar.YEAR) - now.get(Calendar.YEAR) > 5) {
                            timeStr = getString(R.string.todo_estimated_mileage_date_too_far);
                        }
                        else {
                            if (cal.getTimeInMillis() - now.getTimeInMillis() < 365 * ConstantValues.ONE_DAY_IN_MILISECONDS) // 1 year
                            {
                                timeStr = Utils.getFormattedDateTime(time + (estMileageDueDays * ConstantValues.ONE_DAY_IN_MILISECONDS), false);
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
//            if (listCursor.getString(listCursor.getColumnIndex(DBReportAdapter.THIRD_LINE_LIST_NAME)) != null &&
//                    listCursor.getString(listCursor.getColumnIndex(DBReportAdapter.THIRD_LINE_LIST_NAME)).trim().length() > 0) {
//                tvToDoText3.setText(listCursor.getString(listCursor.getColumnIndex(DBReportAdapter.THIRD_LINE_LIST_NAME)));
//            }
//            else {
//                tvToDoText3.setVisibility(View.GONE);
//            }

        }
        else {
            toDoExists = false;
            tvToDoText1.setText(R.string.main_activity_no_to_do);
            tvToDoText2.setVisibility(View.GONE);
//            tvToDoText3.setVisibility(View.GONE);
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
                        notificationTrigger = ToDoNotificationService.TRIGGERED_BY_TIME;
                    }
                    if (taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE)
                            || taskCursor.getString(DBAdapter.COL_POS_TASK__SCHEDULEDFOR).equals(TaskEditFragment.TASK_SCHEDULED_FOR_BOTH)) {
                        notificationTrigger = ToDoNotificationService.TRIGGERED_BY_MILEAGE;
                    }

                    if (taskCursor.getInt(DBAdapter.COL_POS_TASK__TIMEFREQUENCYTYPE) == TaskEditFragment.TASK_TIMEFREQUENCYTYPE_DAILY) {
                        minutesOrDays = MainActivity.this.getString(R.string.gen_minutes);
                    }
                    else {
                        minutesOrDays = MainActivity.this.getString(R.string.gen_days);
                    }

                    Intent i = new Intent(MainActivity.this, ToDoNotificationDialogActivity.class);
                    i.putExtra(ToDoNotificationService.TODO_ID_KEY, mLastToDoId);
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
    private void fillStatisticsZone() {
        TextView tvStatisticsHdr;
        TextView tvStatisticsLastKnownOdometer;
        TextView tvStatisticsAvgFuelEff;
        TextView tvStatisticsLastFuelEff;
        TextView tvStatisticsTotalExpenses;
        TextView tvStatisticsMileageExpense;

        tvStatisticsHdr = (TextView) findViewById(R.id.tvStatisticsHdr);
        tvStatisticsLastKnownOdometer = (TextView) findViewById(R.id.tvStatisticsLastKnownOdometer);
        tvStatisticsAvgFuelEff = (TextView) findViewById(R.id.tvStatisticsAvgFuelEff);
        tvStatisticsLastFuelEff = (TextView) findViewById(R.id.tvStatisticsLastFuelEff);
        tvStatisticsTotalExpenses = (TextView) findViewById(R.id.tvStatisticsTotalExpenses);
        tvStatisticsMileageExpense = (TextView) findViewById(R.id.tvStatisticsMileageExpense);

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

            llStatisticsZone.setVisibility(View.VISIBLE);

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

            tvStatisticsHdr.setText(getString(R.string.main_activity_statistics_header_caption) + " "
                    + Utils.numberToString(mileage, true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) + " " + carUOMLengthCode);
            tvStatisticsLastKnownOdometer.setText(getString(R.string.main_activity_statistics_last_odometer_label) + " "
                    + Utils.numberToString(stopIndex, true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH) + " " + carUOMLengthCode);
            tvStatisticsTotalExpenses.setText(getString(R.string.main_activity_statistics_total_expense_label) + " "
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
            if (tvStatisticsMileageExpense != null) //small screen
            {
                tvStatisticsMileageExpense.setText(mileageExpenseText);
            }
            else {
                tvStatisticsTotalExpenses.setText(tvStatisticsTotalExpenses.getText() + "; " + mileageExpenseText);
            }

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
                    llStatisticsZone.setVisibility(View.VISIBLE);
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
                            }
                            catch (NumberFormatException ignored) {
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
                            //@formatter:off
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

            if(mChartPeriodEndInSeconds > 0)
                lastFuelEffStr = ""; //do not show last fuel eff when the period is not current month or current year

            if(tvStatisticsLastFuelEff != null) { //small screen
                if (fuelEffStr.length() > 0) {
                    tvStatisticsAvgFuelEff.setText(getString(R.string.main_activity_statistics_avg_cons_label) + " " + fuelEffStr);
                    tvStatisticsAvgFuelEff.setVisibility(View.VISIBLE);
                }
                else
                    tvStatisticsAvgFuelEff.setVisibility(View.GONE);

                if (lastFuelEffStr.length() > 0) {
                    tvStatisticsLastFuelEff.setText(getString(R.string.main_activity_statistics_last_cons_label) + " " + lastFuelEffStr);
                    tvStatisticsLastFuelEff.setVisibility(View.VISIBLE);
                }
                else
                    tvStatisticsLastFuelEff.setVisibility(View.GONE);
            }
            else{
                if (lastFuelEffStr.length() > 0) {
                    fuelEffStr = fuelEffStr + "; " + getString(R.string.main_activity_statistics_last_cons_label) + " " + lastFuelEffStr;
                }

                if(fuelEffStr.length() > 0) {
                    tvStatisticsAvgFuelEff.setVisibility(View.VISIBLE);
                    tvStatisticsAvgFuelEff.setText(getString(R.string.main_activity_statistics_avg_cons_label) + " " + fuelEffStr);
                }
                else
                    tvStatisticsAvgFuelEff.setVisibility(View.GONE);
            }

        } else {
            llStatisticsZone.setVisibility(View.GONE);
        }

        try {
            if (listCursor != null)
                listCursor.close();
            reportDb.close();
        }
        catch (Exception ignored){}
    }

    @SuppressLint("WrongConstant")
    private void setShortAbout() {
        String appVersion = null;

        try {
            appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            if (BuildConfig.DEBUG)
                appVersion = appVersion + " (Debug)";
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String abt = String.format(getString(R.string.app_short_about), Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));

        TextView tvShortAboutLbl = (TextView) findViewById(R.id.tvShortAboutLbl);
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
