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

package andicar.n.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

import andicar.n.activity.dialogs.SearchDialogFragment;
import andicar.n.activity.dialogs.ShareDialogFragment;
import andicar.n.persistence.DBAdapter;
import andicar.n.persistence.DBReportAdapter;
import andicar.n.persistence.viewadapter.BaseViewAdapter;
import andicar.n.persistence.viewadapter.ExpenseViewAdapter;
import andicar.n.persistence.viewadapter.GPSTrackViewAdapter;
import andicar.n.persistence.viewadapter.MileageViewAdapter;
import andicar.n.persistence.viewadapter.RefuelViewAdapter;
import andicar.n.persistence.viewadapter.SettingsDefaultViewAdapter;
import andicar.n.persistence.viewadapter.ToDoViewAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.Utils;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CommonDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CommonListActivity extends AppCompatActivity
        implements SearchDialogFragment.SearchDialogListener, ShareDialogFragment.ShareDialogListener, BaseViewAdapter.AdapterItemSelectedListener, Runnable {

    public static final String ACTIVITY_TYPE_KEY = "ActivityType";
    public static final String SCROLL_TO_POSITION_KEY = "ScrollTo";
    public static final String IS_SHOW_SEARCH_MENU_KEY = "IsShowSearchMenu";
    public static final String IS_SHOW_SHARE_MENU_KEY = "IsShowShareMenu";
    public static final int ACTIVITY_TYPE_MILEAGE = 1;
    public static final int ACTIVITY_TYPE_REFUEL = 2;
    public static final int ACTIVITY_TYPE_EXPENSE = 3;
    public static final int ACTIVITY_TYPE_GPS_TRACK = 4;
    public static final int ACTIVITY_TYPE_TODO = 5;
    public static final int ACTIVITY_TYPE_CAR = 6;
    public static final int ACTIVITY_TYPE_DRIVER = 7;
    public static final int ACTIVITY_TYPE_UOM = 8;
    public static final int ACTIVITY_TYPE_UOM_CONVERSION = 9;
    public static final int ACTIVITY_TYPE_EXPENSE_CATEGORY = 10;
    public static final int ACTIVITY_TYPE_FUEL_TYPE = 11;
    public static final int ACTIVITY_TYPE_EXPENSE_TYPE = 12;
    public static final int ACTIVITY_TYPE_REIMBURSEMENT_RATE = 13;
    public static final int ACTIVITY_TYPE_CURRENCY = 14;
    public static final int ACTIVITY_TYPE_CURRENCY_RATE = 15;
    public static final int ACTIVITY_TYPE_BPARTNER = 16;
    public static final int ACTIVITY_TYPE_BPARTNER_LOCATION = 17;
    public static final int ACTIVITY_TYPE_TASK_TYPE = 18;
    public static final int ACTIVITY_TYPE_TASK = 19;
    public static final int ACTIVITY_TYPE_BT_CAR_LINK = 20;
    public static final int ACTIVITY_TYPE_TAG = 21;
    private static final String LAST_SELECTED_ITEM_ID_KEY = "LastSelectedItemId";
    private static final String WHERE_CONDITION_FOR_DB_KEY = "WhereConditionsForFB";
    private static final String WHERE_CONDITION_FOR_SEARCH_INIT_KEY = "WhereConditionsForSearchInit";
    private final Handler handler;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean isTwoPane;
    private boolean isShowSearchMenu;
    private boolean isSharable;
    private DBReportAdapter mReportDb;
    private Cursor mCursor;
    private int mScrollToPosition;
    private int mActivityType;
    private int mReportFormat;
    private long mLastSelectedItemId;
    private Bundle mWhereConditionsForDB = null; //to be send for sql where
    private Bundle mWhereConditionsForSearchInit = null; //to be send for search fields initialisation
    private BaseViewAdapter mRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private ProgressDialog progressDialog;

    {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                try {
                    progressDialog.dismiss();
                    Toast toast;
                    if (msg.peekData() == null) {
                        toast = Toast.makeText(CommonListActivity.this, getString(msg.what), Toast.LENGTH_LONG);
                    }
                    else {
                        toast = Toast.makeText(CommonListActivity.this, msg.peekData().getString("ErrorMsg"), Toast.LENGTH_LONG);
                    }
                    toast.show();
                }
                catch (Exception ignored) {
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_list);

        if (savedInstanceState != null) {
            mWhereConditionsForDB = savedInstanceState.getBundle(WHERE_CONDITION_FOR_DB_KEY);
            mWhereConditionsForSearchInit = savedInstanceState.getBundle(WHERE_CONDITION_FOR_SEARCH_INIT_KEY);
            mActivityType = savedInstanceState.getInt(ACTIVITY_TYPE_KEY, 0);
            mScrollToPosition = savedInstanceState.getInt(SCROLL_TO_POSITION_KEY, -1);
            mLastSelectedItemId = savedInstanceState.getLong(LAST_SELECTED_ITEM_ID_KEY, -1);
            isShowSearchMenu = savedInstanceState.getBoolean(IS_SHOW_SEARCH_MENU_KEY, true);
            isSharable = savedInstanceState.getBoolean(IS_SHOW_SHARE_MENU_KEY, false);
        }
        else {
            //get the type of activity
            if (getIntent().getExtras() == null) {
                mActivityType = 0;
                mScrollToPosition = -1;
                mLastSelectedItemId = -1;
                isShowSearchMenu = true;
                isSharable = false;
            }
            else {
                mActivityType = getIntent().getExtras().getInt(ACTIVITY_TYPE_KEY, 0);
                mScrollToPosition = getIntent().getExtras().getInt(SCROLL_TO_POSITION_KEY, -1);
                mLastSelectedItemId = getIntent().getExtras().getLong(LAST_SELECTED_ITEM_ID_KEY, -1);
                isShowSearchMenu = getIntent().getExtras().getBoolean(IS_SHOW_SEARCH_MENU_KEY, true);
                isSharable = getIntent().getExtras().getBoolean(IS_SHOW_SHARE_MENU_KEY, false);
            }
        }

        if (mWhereConditionsForDB == null) {
            //init search with the last used car_id
            mWhereConditionsForDB = new Bundle();
            mWhereConditionsForSearchInit = new Bundle();
            long carId = AndiCar.getDefaultSharedPreferences().getLong(AndiCar.getAppResources().getString(R.string.pref_key_last_selected_car_id), -1);
            if (carId > -1) {
                switch (mActivityType) {
                    case ACTIVITY_TYPE_MILEAGE:
                        mWhereConditionsForDB.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_MILEAGE, DBReportAdapter.COL_NAME_MILEAGE__CAR_ID) + "=",
                                Long.toString(carId));
                        break;
                    case ACTIVITY_TYPE_REFUEL:
                        mWhereConditionsForDB.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_REFUEL, DBReportAdapter.COL_NAME_REFUEL__CAR_ID) + "=",
                                Long.toString(carId));
                        break;
                    case ACTIVITY_TYPE_EXPENSE:
                        mWhereConditionsForDB.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_EXPENSE, DBReportAdapter.COL_NAME_EXPENSE__CAR_ID) + "=",
                                Long.toString(carId));
                        break;
                    case ACTIVITY_TYPE_GPS_TRACK:
                        mWhereConditionsForDB.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_GPSTRACK, DBReportAdapter.COL_NAME_EXPENSE__CAR_ID) + "=",
                                Long.toString(carId));
                        break;
//                    default:
//                        Utils.showReportableErrorDialog(this, "Unexpected error", "Unknown activity type: " + mActivityType, new Exception("Error"));
//                        finish();
//                        return;
                }
                if (mActivityType != ACTIVITY_TYPE_TODO) {
                    mWhereConditionsForSearchInit.putLong(SearchDialogFragment.CAR_ID_KEY, carId);
                }
            }
            if (mActivityType == ACTIVITY_TYPE_TODO) {
                mWhereConditionsForDB.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_TODO, DBReportAdapter.COL_NAME_TODO__ISDONE) + "=", "N");
                mWhereConditionsForSearchInit.putInt(SearchDialogFragment.STATUS_KEY, 2); //status = done
            }
            if (mActivityType == ACTIVITY_TYPE_EXPENSE_CATEGORY) {
                mWhereConditionsForDB.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_EXPENSECATEGORY, DBReportAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL) + "=", "N");
            }
            else if (mActivityType == ACTIVITY_TYPE_FUEL_TYPE) {
                mWhereConditionsForDB.putString(DBReportAdapter.sqlConcatTableColumn(DBReportAdapter.TABLE_NAME_EXPENSECATEGORY, DBReportAdapter.COL_NAME_EXPENSECATEGORY__ISFUEL) + "=", "Y");
            }

        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.removeAllViews();

        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mScrollToPosition = -1;
                mLastSelectedItemId = -1;
                mRecyclerViewAdapter.showDetailView(CommonListActivity.this, true, true);
            }
        });

        // Show the Up button in the action bar only if the activity is not launched from the preference screen.
        //The solution is to change dynamically the parent activity for up navigation
        if (mActivityType == ACTIVITY_TYPE_REFUEL || mActivityType == ACTIVITY_TYPE_EXPENSE
                || mActivityType == ACTIVITY_TYPE_MILEAGE || mActivityType == ACTIVITY_TYPE_TODO
                || mActivityType == ACTIVITY_TYPE_GPS_TRACK) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        if (mActivityType == ACTIVITY_TYPE_TODO) {
            fab.setVisibility(View.GONE);
        }

        // The detail container view will be present only in the
        // large-screen layouts (res/values-w900dp).
        // If this view is present, then the
        // activity should be in two-pane mode.
        isTwoPane = findViewById(R.id.item_detail_container) != null;
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        if (mCursor != null && !mCursor.isClosed()) {
//            mCursor.close();
//            mCursor = null;
//        }
//
//        if (mReportDb != null) {
//            mReportDb.close();
//            mReportDb = null;
//        }
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle(WHERE_CONDITION_FOR_DB_KEY, mWhereConditionsForDB);
        outState.putBundle(WHERE_CONDITION_FOR_SEARCH_INIT_KEY, mWhereConditionsForSearchInit);
        outState.putInt(SCROLL_TO_POSITION_KEY, mScrollToPosition);
        outState.putLong(LAST_SELECTED_ITEM_ID_KEY, mLastSelectedItemId);
        outState.putInt(ACTIVITY_TYPE_KEY, mActivityType);
        outState.putBoolean(IS_SHOW_SEARCH_MENU_KEY, isShowSearchMenu);
        outState.putBoolean(IS_SHOW_SHARE_MENU_KEY, isSharable);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
            mCursor = null;
        }

        if (mReportDb != null) {
            mReportDb.close();
            mReportDb = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mRecyclerView = (RecyclerView) findViewById(R.id.item_list);
        assert mRecyclerView != null;

        getRecordsList();
        setupRecyclerView();
    }

    private void getRecordsList() {
        String title;
        if (mReportDb == null) {
            mReportDb = new DBReportAdapter(this, null, null);
        }

        if (mCursor != null) {
            try {
                mCursor.close();
            } catch (Exception ignored) {
            }
            mCursor = null;
        }

        switch (mActivityType) {
            case ACTIVITY_TYPE_MILEAGE:
                mReportDb.setReportSql(DBReportAdapter.MILEAGE_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.gen_trips);
                break;
            case ACTIVITY_TYPE_REFUEL:
                mReportDb.setReportSql(DBReportAdapter.REFUEL_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.gen_fillups);
                break;
            case ACTIVITY_TYPE_EXPENSE:
                mReportDb.setReportSql(DBReportAdapter.EXPENSE_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.gen_expenses);
                break;
            case ACTIVITY_TYPE_GPS_TRACK:
                mReportDb.setReportSql(DBReportAdapter.GPS_TRACK_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.gen_gps_tracks);
                break;
            case ACTIVITY_TYPE_TODO:
                mReportDb.setReportSql(DBReportAdapter.TODO_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.gen_todo_list);
                break;
            case ACTIVITY_TYPE_CAR:
                mReportDb.setReportSql(DBReportAdapter.CAR_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_car_title);
                break;
            case ACTIVITY_TYPE_DRIVER:
                mReportDb.setReportSql(DBReportAdapter.DRIVER_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_driver_title);
                break;
            case ACTIVITY_TYPE_UOM:
                mReportDb.setReportSql(DBReportAdapter.UOM_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_uom_title);
                break;
            case ACTIVITY_TYPE_UOM_CONVERSION:
                mReportDb.setReportSql(DBReportAdapter.UOM_CONVERSION_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_uom_conversion_title);
                break;
            case ACTIVITY_TYPE_EXPENSE_CATEGORY:
                mReportDb.setReportSql(DBReportAdapter.EXPENSE_FUEL_CATEGORY_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_expense_category_title);
                break;
            case ACTIVITY_TYPE_FUEL_TYPE:
                mReportDb.setReportSql(DBReportAdapter.EXPENSE_FUEL_CATEGORY_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_fuel_type_title);
                break;
            case ACTIVITY_TYPE_EXPENSE_TYPE:
                mReportDb.setReportSql(DBReportAdapter.EXPENSE_TYPE_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_expense_type_title);
                break;
            case ACTIVITY_TYPE_REIMBURSEMENT_RATE:
                mReportDb.setReportSql(DBReportAdapter.REIMBURSEMENT_RATE_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_reimbursement_rate_title);
                break;
            case ACTIVITY_TYPE_CURRENCY:
                mReportDb.setReportSql(DBReportAdapter.CURRENCY_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_currency_title);
                break;
            case ACTIVITY_TYPE_CURRENCY_RATE:
                mReportDb.setReportSql(DBReportAdapter.CURRENCY_RATE_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_currency_rate_title);
                break;
            case ACTIVITY_TYPE_BPARTNER:
                mReportDb.setReportSql(DBReportAdapter.BPARTNER_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_partner_title);
                break;
            case ACTIVITY_TYPE_TASK_TYPE:
                mReportDb.setReportSql(DBReportAdapter.TASK_TYPE_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_todo_type_title);
                break;
            case ACTIVITY_TYPE_TASK:
                mReportDb.setReportSql(DBReportAdapter.TASK_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_todo_title);
                break;
            case ACTIVITY_TYPE_BT_CAR_LINK:
                mReportDb.setReportSql(DBReportAdapter.BT_CAR_LINK_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.bt_car_link_activity_title);
                break;
            case ACTIVITY_TYPE_TAG:
                mReportDb.setReportSql(DBReportAdapter.TAG_LIST_SELECT_NAME, mWhereConditionsForDB);
                title = getString(R.string.pref_tag_title);
                break;
            default:
                Utils.showReportableErrorDialog(this, "Unexpected error", "Unknown activity type: " + mActivityType, new Exception("Unknown activity type: " + mActivityType), false);
                finish();
                return;
        }
        mCursor = mReportDb.fetchReport(-1);
        setTitle(String.format(getString(R.string.title_list_activity), title, mCursor != null ? Utils.numberToString(mCursor.getCount(), true, 0, RoundingMode.HALF_DOWN) : 0));
    }

    private void setupRecyclerView() {
        switch (mActivityType) {
            case ACTIVITY_TYPE_MILEAGE:
                mRecyclerViewAdapter = new MileageViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                break;
            case ACTIVITY_TYPE_REFUEL:
                mRecyclerViewAdapter = new RefuelViewAdapter(this, mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                break;
            case ACTIVITY_TYPE_EXPENSE:
                mRecyclerViewAdapter = new ExpenseViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                break;
            case ACTIVITY_TYPE_GPS_TRACK:
                mRecyclerViewAdapter = new GPSTrackViewAdapter(this, mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                break;
            case ACTIVITY_TYPE_TODO:
                mRecyclerViewAdapter = new ToDoViewAdapter(this, mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                break;
            case ACTIVITY_TYPE_CAR:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_CAR);
                break;
            case ACTIVITY_TYPE_DRIVER:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_DRIVER);
                break;
            case ACTIVITY_TYPE_UOM:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_UOM);
                break;
            case ACTIVITY_TYPE_UOM_CONVERSION:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_UOM_CONVERSION);
                break;
            case ACTIVITY_TYPE_EXPENSE_CATEGORY:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_EXPENSE_CATEGORY);
                break;
            case ACTIVITY_TYPE_FUEL_TYPE:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_FUEL_TYPE);
                break;
            case ACTIVITY_TYPE_EXPENSE_TYPE:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_EXPENSE_TYPE);
                break;
            case ACTIVITY_TYPE_REIMBURSEMENT_RATE:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_REIMBURSEMENT_RATE);
                break;
            case ACTIVITY_TYPE_CURRENCY:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_CURRENCY);
                break;
            case ACTIVITY_TYPE_CURRENCY_RATE:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_CURRENCY_RATE);
                break;
            case ACTIVITY_TYPE_BPARTNER:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_BPARTNER);
                break;
            case ACTIVITY_TYPE_TASK_TYPE:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_TASK_TYPE);
                break;
            case ACTIVITY_TYPE_TASK:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_TASK);
                break;
            case ACTIVITY_TYPE_BT_CAR_LINK:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_BT_CAR_LINK);
                break;
            case ACTIVITY_TYPE_TAG:
                mRecyclerViewAdapter = new SettingsDefaultViewAdapter(mCursor, this, isTwoPane, mScrollToPosition, mLastSelectedItemId);
                mRecyclerViewAdapter.setViewAdapterType(BaseViewAdapter.VIEW_ADAPTER_TYPE_TAG);
                break;
            default:
                Utils.showReportableErrorDialog(this, "Unexpected error", "Unknown activity type: " + mActivityType, new Exception("Unknown activity type: " + mActivityType), false);
                finish();
        }
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        if (isTwoPane && findViewById(R.id.item_detail_container) != null) {
            if (mRecyclerViewAdapter.getItemCount() == 0) {
                findViewById(R.id.item_detail_container).setVisibility(View.GONE);
            }
            else {
                findViewById(R.id.item_detail_container).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null) {
            menu.clear();
        }
        MenuInflater inflater = getMenuInflater();
        if (isSharable) {
            inflater.inflate(R.menu.menu_share, menu);
        }

        if (isTwoPane && mActivityType != ACTIVITY_TYPE_TODO) {
            inflater.inflate(R.menu.menu_delete, menu);
            inflater.inflate(R.menu.menu_edit, menu);
        }
        if (isShowSearchMenu) {
            inflater.inflate(R.menu.menu_search, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        else if (id == R.id.action_edit) {
            if (mRecyclerViewAdapter.getItemCount() == 0) {
                Toast.makeText(this, "Nothing to edit", Toast.LENGTH_SHORT).show();
            }
            else {
                mRecyclerViewAdapter.showDetailView(this, true, false);
            }
            return true;
        }
        else if (id == R.id.action_delete) {
            if (mRecyclerViewAdapter.getItemCount() == 0) {
                Toast.makeText(this, "Nothing to delete", Toast.LENGTH_SHORT).show();
            }
            else {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle(R.string.gen_confirm);
                alertDialog.setMessage(R.string.gen_delete_confirmation);
                alertDialog.setCancelable(false);

                alertDialog.setPositiveButton(R.string.gen_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int deleteResult = mRecyclerViewAdapter.deleteItem();
                                if (deleteResult == -1) {
                                    //need enhancements
                                    mScrollToPosition = mRecyclerViewAdapter.getPosition();
                                    //highlight the next item in the list
                                    //TODO fix required when the last item in the list is deleted
                                    mLastSelectedItemId = mRecyclerViewAdapter.getItemId(mScrollToPosition + 1);
                                    CommonListActivity.this.getRecordsList();
                                    CommonListActivity.this.setupRecyclerView();
                                }
                                else {
                                    Utils.showNotReportableErrorDialog(CommonListActivity.this, CommonListActivity.this.getString(R.string.gen_error), CommonListActivity.this.getString(deleteResult), false);
                                }
                            }
                        });

                alertDialog.setNegativeButton(R.string.gen_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            }

            return true;
        }
        else if (id == R.id.action_search) {
            Bundle searchArg = new Bundle();
            if (mActivityType == ACTIVITY_TYPE_MILEAGE) {
                searchArg.putInt(SearchDialogFragment.SEARCH_TYPE_KEY, SearchDialogFragment.SEARCH_TYPE_MILEAGE);
            }
            else if (mActivityType == ACTIVITY_TYPE_REFUEL) {
                searchArg.putInt(SearchDialogFragment.SEARCH_TYPE_KEY, SearchDialogFragment.SEARCH_TYPE_REFUEL);
            }
            else if (mActivityType == ACTIVITY_TYPE_EXPENSE) {
                searchArg.putInt(SearchDialogFragment.SEARCH_TYPE_KEY, SearchDialogFragment.SEARCH_TYPE_EXPENSE);
            }
            else if (mActivityType == ACTIVITY_TYPE_TODO) {
                searchArg.putInt(SearchDialogFragment.SEARCH_TYPE_KEY, SearchDialogFragment.SEARCH_TYPE_TODO);
            }
            else if (mActivityType == ACTIVITY_TYPE_GPS_TRACK) {
                searchArg.putInt(SearchDialogFragment.SEARCH_TYPE_KEY, SearchDialogFragment.SEARCH_TYPE_GPS_TRACK);
            }

            searchArg.putBundle(SearchDialogFragment.SEARCH_ARGS_KEY, mWhereConditionsForSearchInit);

            FragmentManager fm = getSupportFragmentManager();
            SearchDialogFragment searchDialog = new SearchDialogFragment();
            searchDialog.setArguments(searchArg);
            searchDialog.show(fm, "fragment_search_dialog");

            return true;
        }
        else if (id == R.id.action_share) {
            if (mRecyclerViewAdapter.getItemCount() == 0) {
                Toast.makeText(this, "Nothing to share", Toast.LENGTH_SHORT).show();
            }
            else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ConstantValues.REQUEST_ACCESS_EXTERNAL_STORAGE);
                }
                else {
                    showShareDialog();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showShareDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ShareDialogFragment shareDialog = new ShareDialogFragment();
        shareDialog.show(fm, "fragment_share_dialog");
    }

    //returns from asking permission to access external storage for sharing reports
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ConstantValues.REQUEST_ACCESS_EXTERNAL_STORAGE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.error_070, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onFinishSearchDialog(Bundle searchParams) {
        mWhereConditionsForDB.clear();
        mWhereConditionsForSearchInit.clear();

        if (mActivityType == ACTIVITY_TYPE_MILEAGE) {
            if (searchParams.containsKey(SearchDialogFragment.CAR_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_MILEAGE, DBAdapter.COL_NAME_MILEAGE__CAR_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.CAR_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.CAR_ID_KEY, searchParams.getLong(SearchDialogFragment.CAR_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DRIVER_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_MILEAGE, DBAdapter.COL_NAME_MILEAGE__DRIVER_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.DRIVER_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DRIVER_ID_KEY, searchParams.getLong(SearchDialogFragment.DRIVER_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.TYPE_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_MILEAGE, DBAdapter.COL_NAME_MILEAGE__EXPENSETYPE_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.TYPE_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.TYPE_ID_KEY, searchParams.getLong(SearchDialogFragment.TYPE_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_MILEAGE, DBAdapter.COL_NAME_MILEAGE__DATE) + " >= ",
                        Long.toString(Utils.roundDate(searchParams.getLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY), ConstantValues.DATE_DECODE_TO_ZERO) / 1000));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY, searchParams.getLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_MILEAGE, DBAdapter.COL_NAME_MILEAGE__DATE) + " <= ",
                        Long.toString(Utils.roundDate(searchParams.getLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY), ConstantValues.DATE_DECODE_TO_ZERO) / 1000));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY, searchParams.getLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.COMMENT_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_MILEAGE, DBAdapter.COL_NAME_GEN_USER_COMMENT) + " LIKE ",
                        searchParams.getString(SearchDialogFragment.COMMENT_KEY));
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.COMMENT_KEY, searchParams.getString(SearchDialogFragment.COMMENT_KEY));
            }
            else {
                mWhereConditionsForDB.putString("COALESCE( " + DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_MILEAGE, DBAdapter.COL_NAME_GEN_USER_COMMENT) + ", '') = ",
                        "");
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.COMMENT_KEY, "");
            }

            if (searchParams.containsKey(SearchDialogFragment.TAG_KEY)) {
                mWhereConditionsForDB.putString("COALESCE( " + DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TAG, DBAdapter.COL_NAME_GEN_NAME) + ", '') LIKE ",
                        searchParams.getString(SearchDialogFragment.TAG_KEY));
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.TAG_KEY, searchParams.getString(SearchDialogFragment.TAG_KEY));
            }
            else {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_MILEAGE, DBAdapter.COL_NAME_MILEAGE__TAG_ID) + " IS ",
                        "NULL");
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.TAG_KEY, "");
            }
        }
        else if (mActivityType == ACTIVITY_TYPE_GPS_TRACK) {
            if (searchParams.containsKey(SearchDialogFragment.CAR_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_NAME_GPSTRACK__CAR_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.CAR_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.CAR_ID_KEY, searchParams.getLong(SearchDialogFragment.CAR_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DRIVER_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_NAME_GPSTRACK__DRIVER_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.DRIVER_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DRIVER_ID_KEY, searchParams.getLong(SearchDialogFragment.DRIVER_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.TYPE_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_NAME_GPSTRACK__EXPENSETYPE_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.TYPE_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.TYPE_ID_KEY, searchParams.getLong(SearchDialogFragment.TYPE_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_NAME_GPSTRACK__DATE) + " >= ",
                        Long.toString(Utils.roundDate(searchParams.getLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY), ConstantValues.DATE_DECODE_TO_ZERO) / 1000));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY, searchParams.getLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_NAME_GPSTRACK__DATE) + " <= ",
                        Long.toString(Utils.roundDate(searchParams.getLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY), ConstantValues.DATE_DECODE_TO_ZERO) / 1000));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY, searchParams.getLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.COMMENT_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_NAME_GEN_USER_COMMENT) + " LIKE ",
                        searchParams.getString(SearchDialogFragment.COMMENT_KEY));
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.COMMENT_KEY, searchParams.getString(SearchDialogFragment.COMMENT_KEY));
            }
            else {
                mWhereConditionsForDB.putString("COALESCE( " + DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_NAME_GEN_USER_COMMENT) + ", '') = ",
                        "");
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.COMMENT_KEY, "");
            }

            if (searchParams.containsKey(SearchDialogFragment.TAG_KEY)) {
                mWhereConditionsForDB.putString("COALESCE( " + DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TAG, DBAdapter.COL_NAME_GEN_NAME) + ", '') LIKE ",
                        searchParams.getString(SearchDialogFragment.TAG_KEY));
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.TAG_KEY, searchParams.getString(SearchDialogFragment.TAG_KEY));
            }
            else {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_NAME_GPSTRACK__TAG_ID) + " IS ",
                        "NULL");
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.TAG_KEY, "");
            }
        }
        else if (mActivityType == ACTIVITY_TYPE_REFUEL) {
            if (searchParams.containsKey(SearchDialogFragment.CAR_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_REFUEL, DBAdapter.COL_NAME_REFUEL__CAR_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.CAR_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.CAR_ID_KEY, searchParams.getLong(SearchDialogFragment.CAR_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DRIVER_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_REFUEL, DBAdapter.COL_NAME_REFUEL__DRIVER_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.DRIVER_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DRIVER_ID_KEY, searchParams.getLong(SearchDialogFragment.DRIVER_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.TYPE_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_REFUEL, DBAdapter.COL_NAME_REFUEL__EXPENSETYPE_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.TYPE_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.TYPE_ID_KEY, searchParams.getLong(SearchDialogFragment.TYPE_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.CATEGORY_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_REFUEL, DBAdapter.COL_NAME_REFUEL__EXPENSECATEGORY_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.CATEGORY_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.CATEGORY_ID_KEY, searchParams.getLong(SearchDialogFragment.CATEGORY_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_REFUEL, DBAdapter.COL_NAME_REFUEL__DATE) + " >= ",
                        Long.toString(Utils.roundDate(searchParams.getLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY), ConstantValues.DATE_DECODE_TO_ZERO) / 1000));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY, searchParams.getLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_REFUEL, DBAdapter.COL_NAME_REFUEL__DATE) + " <= ",
                        Long.toString(Utils.roundDate(searchParams.getLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY), ConstantValues.DATE_DECODE_TO_ZERO) / 1000));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY, searchParams.getLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.COMMENT_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_REFUEL, DBAdapter.COL_NAME_GEN_USER_COMMENT) + " LIKE ",
                        searchParams.getString(SearchDialogFragment.COMMENT_KEY));
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.COMMENT_KEY, searchParams.getString(SearchDialogFragment.COMMENT_KEY));
            }
            else {
                mWhereConditionsForDB.putString("COALESCE( " + DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_REFUEL, DBAdapter.COL_NAME_GEN_USER_COMMENT) + ", '') = ",
                        "");
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.COMMENT_KEY, "");
            }

            if (searchParams.containsKey(SearchDialogFragment.TAG_KEY)) {
                mWhereConditionsForDB.putString("COALESCE( " + DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TAG, DBAdapter.COL_NAME_GEN_NAME) + ", '') LIKE ",
                        searchParams.getString(SearchDialogFragment.TAG_KEY));
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.TAG_KEY, searchParams.getString(SearchDialogFragment.TAG_KEY));
            }
            else {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_REFUEL, DBAdapter.COL_NAME_EXPENSE__TAG_ID) + " IS ",
                        "NULL");
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.TAG_KEY, "");
            }
        }
        else if (mActivityType == ACTIVITY_TYPE_EXPENSE) {
            if (searchParams.containsKey(SearchDialogFragment.CAR_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_EXPENSE, DBAdapter.COL_NAME_EXPENSE__CAR_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.CAR_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.CAR_ID_KEY, searchParams.getLong(SearchDialogFragment.CAR_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DRIVER_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_EXPENSE, DBAdapter.COL_NAME_EXPENSE__DRIVER_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.DRIVER_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DRIVER_ID_KEY, searchParams.getLong(SearchDialogFragment.DRIVER_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.TYPE_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_EXPENSE, DBAdapter.COL_NAME_EXPENSE__EXPENSETYPE_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.TYPE_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.TYPE_ID_KEY, searchParams.getLong(SearchDialogFragment.TYPE_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.CATEGORY_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_EXPENSE, DBAdapter.COL_NAME_EXPENSE__EXPENSECATEGORY_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.CATEGORY_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.CATEGORY_ID_KEY, searchParams.getLong(SearchDialogFragment.CATEGORY_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_EXPENSE, DBAdapter.COL_NAME_EXPENSE__DATE) + " >= ",
                        Long.toString(Utils.roundDate(searchParams.getLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY), ConstantValues.DATE_DECODE_TO_ZERO) / 1000));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY, searchParams.getLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_EXPENSE, DBAdapter.COL_NAME_EXPENSE__DATE) + " <= ",
                        Long.toString(Utils.roundDate(searchParams.getLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY), ConstantValues.DATE_DECODE_TO_ZERO) / 1000));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY, searchParams.getLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.COMMENT_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_EXPENSE, DBAdapter.COL_NAME_GEN_USER_COMMENT) + " LIKE ",
                        searchParams.getString(SearchDialogFragment.COMMENT_KEY));
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.COMMENT_KEY, searchParams.getString(SearchDialogFragment.COMMENT_KEY));
            }
            else {
                mWhereConditionsForDB.putString("COALESCE( " + DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_EXPENSE, DBAdapter.COL_NAME_GEN_USER_COMMENT) + ", '') = ",
                        "");
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.COMMENT_KEY, "");
            }

            if (searchParams.containsKey(SearchDialogFragment.TAG_KEY)) {
                mWhereConditionsForDB.putString("COALESCE( " + DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TAG, DBAdapter.COL_NAME_GEN_NAME) + ", '') LIKE ",
                        searchParams.getString(SearchDialogFragment.TAG_KEY));
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.TAG_KEY, searchParams.getString(SearchDialogFragment.TAG_KEY));
            }
            else {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_EXPENSE, DBAdapter.COL_NAME_EXPENSE__TAG_ID) + " IS ",
                        "NULL");
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.TAG_KEY, "");
            }
        }
        ///
        else if (mActivityType == ACTIVITY_TYPE_TODO) {
            if (searchParams.containsKey(SearchDialogFragment.CAR_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__CAR_ID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.CAR_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.CAR_ID_KEY, searchParams.getLong(SearchDialogFragment.CAR_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.TYPE_ID_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TASKTYPE, DBAdapter.COL_NAME_GEN_ROWID) + "=",
                        Long.toString(searchParams.getLong(SearchDialogFragment.TYPE_ID_KEY)));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.TYPE_ID_KEY, searchParams.getLong(SearchDialogFragment.TYPE_ID_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.STATUS_KEY)) {
                String likeVal;
                switch (searchParams.getInt(SearchDialogFragment.STATUS_KEY)) {
                    case 0:
                        likeVal = "%";
                        break;
                    case 1:
                        likeVal = "Y";
                        break;
                    case 2:
                        likeVal = "N";
                        break;
                    default:
                        likeVal = "%";
                }
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_TODO__ISDONE) + " like ", likeVal);
                mWhereConditionsForSearchInit.putInt(SearchDialogFragment.STATUS_KEY, searchParams.getInt(SearchDialogFragment.STATUS_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY)) {
                Calendar now = Calendar.getInstance();
                long estDueDay = (searchParams.getLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY) - now.getTimeInMillis())
                        / ConstantValues.ONE_DAY_IN_MILISECONDS;
                mWhereConditionsForDB.putString("EstDueDays >= ", Long.toString(estDueDay));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY, searchParams.getLong(SearchDialogFragment.DATE_FROM_IN_MILLIS_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY)) {
                Calendar now = Calendar.getInstance();
                long estDueDay = (searchParams.getLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY) - now.getTimeInMillis())
                        / ConstantValues.ONE_DAY_IN_MILISECONDS;
                mWhereConditionsForDB.putString("EstDueDays <= ", Long.toString(estDueDay));
                mWhereConditionsForSearchInit.putLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY, searchParams.getLong(SearchDialogFragment.DATE_TO_IN_MILLIS_KEY));
            }

            if (searchParams.containsKey(SearchDialogFragment.COMMENT_KEY)) {
                mWhereConditionsForDB.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_GEN_USER_COMMENT) + " LIKE ",
                        searchParams.getString(SearchDialogFragment.COMMENT_KEY));
                mWhereConditionsForSearchInit.putString(SearchDialogFragment.COMMENT_KEY, searchParams.getString(SearchDialogFragment.COMMENT_KEY));
            }
        }

        getRecordsList();
        setupRecyclerView();
    }

    @Override
    public void onAdapterItemSelectedListener(int selectedPosition, long selectedItemId) {
        mScrollToPosition = selectedPosition;
        mLastSelectedItemId = selectedItemId;
    }

    @Override
    public void onFinishShareDialog(Bundle params) {

        mReportFormat = params.getInt(ShareDialogFragment.SHARE_FORMAT_KEY, 0);

        FileUtils.createFolderIfNotExists(this, ConstantValues.REPORT_FOLDER);

        progressDialog = ProgressDialog.show(this, "", getString(R.string.report_creation_in_progress_message), true);
        Thread thread = new Thread(this);
        thread.start();
    }

    private void createReport() {
        Cursor c;
        String reportContent = "";
        String reportTitle;
        String reportFileName;

        DBReportAdapter dbReportAdapter;

        if (mActivityType == ACTIVITY_TYPE_MILEAGE) {
            reportTitle = "MileageReport_";
            dbReportAdapter = new DBReportAdapter(this, DBReportAdapter.MILEAGE_LIST_REPORT_SELECT, mWhereConditionsForDB);
            c = dbReportAdapter.fetchReport(-1);
        }
        else if (mActivityType == ACTIVITY_TYPE_REFUEL) {
            reportTitle = "RefuelReport_";
            dbReportAdapter = new DBReportAdapter(this, DBReportAdapter.REFUEL_LIST_REPORT_SELECT, mWhereConditionsForDB);
            c = dbReportAdapter.fetchReport(-1);
        }
        else if (mActivityType == ACTIVITY_TYPE_EXPENSE) {
            reportTitle = "ExpenseReport_";
            dbReportAdapter = new DBReportAdapter(this, DBReportAdapter.EXPENSES_LIST_REPORT_SELECT, mWhereConditionsForDB);
            c = dbReportAdapter.fetchReport(-1);
        }
        else if (mActivityType == ACTIVITY_TYPE_GPS_TRACK) {
            reportTitle = "GPSTrackReport_";
            dbReportAdapter = new DBReportAdapter(this, DBReportAdapter.GPS_TRACK_LIST_REPORT_SELECT, mWhereConditionsForDB);
            c = dbReportAdapter.fetchReport(-1);
        }
        else if (mActivityType == ACTIVITY_TYPE_TODO) {
            reportTitle = "ToDoListReport_";
            dbReportAdapter = new DBReportAdapter(this, DBReportAdapter.TODO_LIST_REPORT_SELECT, mWhereConditionsForDB);
            c = dbReportAdapter.fetchReport(-1);
        }
//        else if (mActivityType == ACTIVITY_TYPE_REIMBURSEMENT_RATE) {
//            reportTitle = "ReimbursementRateReport_";
//            dbReportAdapter = new DBReportAdapter(this, "reimbursementRateListReportSelect", mWhereConditionsForDB);
//            c = dbReportAdapter.fetchReport(-1);
//        }
        else {
            handler.sendEmptyMessage(R.string.error_035);
            return;
        }
        if (c == null) {
            Message msg = new Message();
            Bundle msgBundle = new Bundle();
            msgBundle.putString("ErrorMsg", dbReportAdapter.mErrorMessage);
            msg.setData(msgBundle);
            handler.sendMessage(msg);
        }

        reportTitle = Utils.appendDateTime(reportTitle, false, false, null);
        reportFileName = Utils.appendDateTime(reportTitle, true, true, null);

        if (c != null) {
            if (mReportFormat == 0) {
                reportFileName = reportFileName + ".csv";
                reportContent = createCSVContent(c, dbReportAdapter);
            }
            else if (mReportFormat == 1) {
                reportFileName = reportFileName + ".html";
                reportContent = createHTMLContent(c, dbReportAdapter, reportTitle);
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }
        }

        try {
            dbReportAdapter.close();
        }
        catch (Exception ignored) {
        }

        int i = FileUtils.writeReportFile(this, reportContent, reportFileName);
        if (i != -1) { //error
            handler.sendEmptyMessage(R.string.error_034);
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/html");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, " AndiCar report " + reportTitle + (mReportFormat == 0 ? ".csv" : ".html"));
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Sent by AndiCar (http://www.andicar.org)");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + ConstantValues.REPORT_FOLDER + reportFileName));
        try {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.gen_share)));
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.error_067, Toast.LENGTH_LONG).show();
        }
        handler.sendEmptyMessage(-1);
    }

    private String createCSVContent(Cursor reportCursor, DBReportAdapter dbReportAdapter) {
        String reportContent = "";
        String colVal;
        int i;
        BigDecimal oldFullRefuelIndex;
        BigDecimal distance;
        BigDecimal fuelQty;

        //create header row
        boolean appendComma = false;
        for (i = 0; i < reportCursor.getColumnCount(); i++) {
            if (reportCursor.getColumnName(i).endsWith("DoNotExport")) {
                continue;
            }

            if (appendComma) {
                reportContent = reportContent + ",";
            }
            appendComma = true;

            reportContent = reportContent + "\""
                    + reportCursor.getColumnName(i).replaceAll("_DTypeN", "").replaceAll("_DTypeD", "").replaceAll("_DTypeL", "").replaceAll("_DTypeR", "")
                    + "\"";
        }
        reportContent = reportContent + "\n";

        long currentTime = System.currentTimeMillis();
        long days;
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();

        while (reportCursor.moveToNext()) {
            appendComma = false;
            for (i = 0; i < reportCursor.getColumnCount(); i++) {
                if (reportCursor.getColumnName(i).endsWith("DoNotExport")) {
                    continue;
                }

                if (appendComma) {
                    reportContent = reportContent + ",";
                }
                appendComma = true;

                if (reportCursor.getColumnName(i).endsWith("_DTypeN")) {
                    colVal = Utils.numberToString(reportCursor.getDouble(i), false, 4, ConstantValues.ROUNDING_MODE_LENGTH);
                }
                else if (reportCursor.getColumnName(i).endsWith("_DTypeL")) {
                    colVal = Utils.numberToString(reportCursor.getLong(i), false, 4, ConstantValues.ROUNDING_MODE_LENGTH);
                }
                else if (reportCursor.getColumnName(i).endsWith("_DTypeR")) {
                    colVal = Utils.numberToString(reportCursor.getDouble(i), false, 5, ConstantValues.ROUNDING_MODE_RATES);
                }
                else if (reportCursor.getColumnName(i).endsWith("_DTypeD")) {
                    colVal = DateFormat.getDateFormat(this).format(reportCursor.getLong(i) * 1000);
                }
                else {
                    colVal = reportCursor.getString(i);
                }

                if (colVal == null) {
                    colVal = "";
                }
                colVal = colVal.replace("\"", "''");
                if (mActivityType == ACTIVITY_TYPE_TODO) {
                    colVal = colVal.replace("[#TDR1]", getString(R.string.gen_done)).replace("[#TDR2]", getString(R.string.todo_overdue_label))
                            .replace("[#TDR3]", getString(R.string.todo_scheduled_label)).replace("[#TDR4]", getString(R.string.gen_time))
                            .replace("[#TDR5]", getString(R.string.task_edit_mileage_driven))
                            .replace("[#TDR6]", getString(R.string.gen_time) + " & " + getString(R.string.task_edit_mileage_driven));
                    if ((i == 6 || i == 7 || i == 8) && colVal.equals("0")) {
                        colVal = "N/A";
                    }
                    if ((i == 8 || i == 9) && !colVal.equals("N/A")) {
                        try {
                            days = Long.parseLong(colVal);
                            if (days == 99999999999L) {
                                colVal = getString(R.string.todo_estimated_mileage_date_no_data);
                            }
                            else {
                                cal.setTimeInMillis(currentTime + (days * ConstantValues.ONE_DAY_IN_MILISECONDS));
                                if (cal.get(Calendar.YEAR) - now.get(Calendar.YEAR) > 5) {
                                    colVal = getString(R.string.todo_estimated_mileage_date_too_far);
                                }
                                else {
                                    if (cal.getTimeInMillis() - now.getTimeInMillis() < 365 * ConstantValues.ONE_DAY_IN_MILISECONDS) // 1 year
                                    {
                                        colVal = DateFormat.getDateFormat(this).format(currentTime + (days * ConstantValues.ONE_DAY_IN_MILISECONDS));
                                    }
                                    else {
                                        colVal = DateFormat.format("MMM, yyyy", cal).toString();
                                    }
                                }
                            }
                        }
                        catch (NumberFormatException e) {
                            colVal = getString(R.string.todo_estimated_mileage_date_no_data);
                        }
                    }
                }
                else {
                    if (mActivityType == ACTIVITY_TYPE_REFUEL) {
                        if (colVal.contains("[#rv1]") || colVal.contains("[#rv2]")) {
                            try {
                                oldFullRefuelIndex = new BigDecimal(reportCursor.getDouble(27));
                            }
                            catch (Exception e) {
                                colVal = colVal.replace("[#rv1]", "Error #1! Please contact me at andicar.support@gmail.com").replace("[#rv2]",
                                        "Error #1! Please contact me at andicar.support@gmail.com");
                                reportContent = reportContent + "\"" + colVal + "\"";
                                continue;
                            }
                            if (oldFullRefuelIndex.compareTo(BigDecimal.ZERO) < 0 || reportCursor.getString(6).equals("N")) { //this is not a full refuel
                                colVal = colVal.replace("[#rv1]", "").replace("[#rv2]", "");
                            }
                            // calculate the cons and fuel eff.
                            distance = (new BigDecimal(reportCursor.getString(5))).subtract(oldFullRefuelIndex);
                            try {
                                Double t = dbReportAdapter.getFuelQtyForCons(reportCursor.getLong(28), oldFullRefuelIndex, reportCursor.getDouble(5));
                                fuelQty = (new BigDecimal(t == null ? 0d : t));
                            }
                            catch (NullPointerException e) {
                                colVal = colVal.replace("[#rv1]", "Error#2! Please contact me at andicar.support@gmail.com").replace("[#rv2]",
                                        "Error#2! Please contact me at andicar.support@gmail.com");
                                reportContent = reportContent + "\"" + colVal + "\"";
                                continue;
                            }
                            try {
                                colVal = colVal.replace(
                                        "[#rv1]",
                                        Utils.numberToString(fuelQty.multiply(new BigDecimal("100")).divide(distance, 10, RoundingMode.HALF_UP), false, 2,
                                                RoundingMode.HALF_UP)).replace("[#rv2]",
                                        Utils.numberToString(distance.divide(fuelQty, 10, RoundingMode.HALF_UP), false, 2, RoundingMode.HALF_UP));
                            }
                            catch (Exception e) {
                                colVal = colVal.replace("[#rv1]", "Error#3! Please contact me at andicar.support@gmail.com").replace("[#rv2]",
                                        "Error#3! Please contact me at andicar.support@gmail.com");
                                reportContent = reportContent + "\"" + colVal + "\"";
                                continue;
                            }
                        }
                    }
                    colVal = colVal.replace("[#d0]", getString(R.string.day_of_week_0)).replace("[#d1]", getString(R.string.day_of_week_1))
                            .replace("[#d2]", getString(R.string.day_of_week_2)).replace("[#d3]", getString(R.string.day_of_week_3))
                            .replace("[#d4]", getString(R.string.day_of_week_4)).replace("[#d5]", getString(R.string.day_of_week_5))
                            .replace("[#d6]", getString(R.string.day_of_week_6));
                }
                reportContent = reportContent + "\"" + colVal + "\"";
            }
            reportContent = reportContent + "\n";
        }
        return reportContent;
    }

    @SuppressWarnings("ConstantConditions")
    private String createHTMLContent(Cursor reportCursor, DBReportAdapter dbReportAdapter, String title) {
        BigDecimal oldFullRefuelIndex;
        BigDecimal distance;
        BigDecimal fuelQty;
        int i;
        String colVal;

        String reportContent = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" + "<html>\n" + "<head>\n" + "<title>" + title
                + "</title>\n" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" + "</head>\n" + "<body>\n"
                + "<table  WIDTH=100% BORDER=1 BORDERCOLOR=\"#000000\" CELLPADDING=4 CELLSPACING=0>\n" + "<TR VALIGN=TOP>\n"; //table header
        //create table header
        for (i = 0; i < reportCursor.getColumnCount(); i++) {
            if (reportCursor.getColumnName(i).endsWith("DoNotExport")) {
                continue;
            }

            reportContent = reportContent + "<TH>"
                    + reportCursor.getColumnName(i).replaceAll("_DTypeN", "").replaceAll("_DTypeD", "").replaceAll("_DTypeL", "").replaceAll("_DTypeR", "")
                    + "</TH>\n";
        }
        reportContent = reportContent + "</TR>\n"; //end table header

        long currentTime = System.currentTimeMillis();
        long days;
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        String colValUF = "";
        long date = 0;

        while (reportCursor.moveToNext()) {
            reportContent = reportContent + "<TR VALIGN=TOP>\n";
            for (i = 0; i < reportCursor.getColumnCount(); i++) {

                if (reportCursor.getColumnName(i).endsWith("DoNotExport")) {
                    continue;
                }

                if (reportCursor.getColumnName(i).contains("_DTypeN")) {
                    colVal = Utils.numberToString(reportCursor.getDouble(i), true, 4, ConstantValues.ROUNDING_MODE_LENGTH);
                    colValUF = Utils.numberToString(reportCursor.getDouble(i), false, 4, ConstantValues.ROUNDING_MODE_LENGTH);
                }
                else if (reportCursor.getColumnName(i).endsWith("_DTypeL")) {
                    colVal = Utils.numberToString(reportCursor.getLong(i), true, 4, ConstantValues.ROUNDING_MODE_LENGTH);
                    colValUF = Utils.numberToString(reportCursor.getLong(i), false, 4, ConstantValues.ROUNDING_MODE_LENGTH);
                }
                else if (reportCursor.getColumnName(i).contains("_DTypeR")) {
                    colVal = Utils.numberToString(reportCursor.getDouble(i), true, 5, ConstantValues.ROUNDING_MODE_RATES);
                    colValUF = Utils.numberToString(reportCursor.getDouble(i), false, 5, ConstantValues.ROUNDING_MODE_RATES);
                }
                else if (reportCursor.getColumnName(i).endsWith("_DTypeD")) {
                    date = reportCursor.getLong(i) * 1000;
                    colVal = DateFormat.getDateFormat(this).format(date);
                }
                else {
                    colVal = reportCursor.getString(i);
                }
                if (colVal == null) {
                    colVal = "";
                }

                if (mActivityType == ACTIVITY_TYPE_TODO) {
                    colVal = colVal.replace("[#TDR1]", getString(R.string.gen_done)).replace("[#TDR2]", getString(R.string.todo_overdue_label))
                            .replace("[#TDR3]", getString(R.string.todo_scheduled_label)).replace("[#TDR4]", getString(R.string.gen_time))
                            .replace("[#TDR5]", getString(R.string.task_edit_mileage_driven))
                            .replace("[#TDR6]", getString(R.string.gen_time) + " & " + getString(R.string.task_edit_mileage_driven));
                    if ((i == 6 && date == 0) || ((i == 7 || i == 8) && colVal.equals("0"))) {
                        colVal = "N/A";
                    }
                    if ((i == 8 || i == 9) && !colVal.equals("N/A")) {
                        try {
                            days = Long.parseLong(colValUF);
                        }
                        catch (NumberFormatException e) {
                            days = 0;
                        }
                        if (days == 99999999999L) {
                            colVal = getString(R.string.todo_estimated_mileage_date_no_data);
                        }
                        else {
                            cal.setTimeInMillis(currentTime + (days * ConstantValues.ONE_DAY_IN_MILISECONDS));
                            if (cal.get(Calendar.YEAR) - now.get(Calendar.YEAR) > 5) {
                                colVal = getString(R.string.todo_estimated_mileage_date_too_far);
                            }
                            else {
                                if (cal.getTimeInMillis() - now.getTimeInMillis() < 365 * ConstantValues.ONE_DAY_IN_MILISECONDS) // 1 year
                                {
                                    colVal = DateFormat.getDateFormat(this).format(currentTime + (days * ConstantValues.ONE_DAY_IN_MILISECONDS));
                                }
                                else {
                                    colVal = DateFormat.format("MMM, yyyy", cal).toString();
                                }
                            }
                        }
                    }
                }
                else {
                    if (mActivityType == ACTIVITY_TYPE_REFUEL) {
                        if (colVal.contains("[#rv1]") || colVal.contains("[#rv2]")) {
                            try {
                                oldFullRefuelIndex = new BigDecimal(reportCursor.getDouble(27));
                            }
                            catch (Exception e) {
                                colVal = colVal.replace("[#rv1]", "Error #1! Please contact me at andicar.support@gmail.com").replace("[#rv2]",
                                        "Error #1! Please contact me at andicar.support@gmail.com");
                                reportContent = reportContent + "<TD>" + colVal + "</TD>\n";
                                continue;
                            }
                            if (oldFullRefuelIndex.compareTo(BigDecimal.ZERO) < 0 || reportCursor.getString(6).equals("N")) { //this is not a full refuel
                                colVal = colVal.replace("[#rv1]", "").replace("[#rv2]", "");
                            }
                            // calculate the cons and fuel eff.
                            distance = (new BigDecimal(reportCursor.getString(5))).subtract(oldFullRefuelIndex);
                            try {
                                Double t = dbReportAdapter.getFuelQtyForCons(reportCursor.getLong(28), oldFullRefuelIndex, reportCursor.getDouble(5));
                                fuelQty = (new BigDecimal(t == null ? 0d : t));
                            }
                            catch (NullPointerException e) {
                                colVal = colVal.replace("[#rv1]", "Error#2! Please contact me at andicar.support@gmail.com").replace("[#rv2]",
                                        "Error#2! Please contact me at andicar.support@gmail.com");
                                reportContent = reportContent + "<TD>" + colVal + "</TD>\n";
                                continue;
                            }
                            try {
                                colVal = colVal.replace(
                                        "[#rv1]",
                                        Utils.numberToString(fuelQty.multiply(new BigDecimal("100")).divide(distance, 10, RoundingMode.HALF_UP), false, 2,
                                                RoundingMode.HALF_UP)).replace("[#rv2]",
                                        Utils.numberToString(distance.divide(fuelQty, 10, RoundingMode.HALF_UP), false, 2, RoundingMode.HALF_UP));
                            }
                            catch (Exception e) {
                                colVal = colVal.replace("[#rv1]", "Error#3! Please contact me at andicar.support@gmail.com").replace("[#rv2]",
                                        "Error#3! Please contact me at andicar.support@gmail.com");
                                reportContent = reportContent + "<TD>" + colVal + "</TD>\n";
                                continue;
                            }
                        }
                    }
                    colVal = colVal.replace("[#d0]", getString(R.string.day_of_week_0)).replace("[#d1]", getString(R.string.day_of_week_1))
                            .replace("[#d2]", getString(R.string.day_of_week_2)).replace("[#d3]", getString(R.string.day_of_week_3))
                            .replace("[#d4]", getString(R.string.day_of_week_4)).replace("[#d5]", getString(R.string.day_of_week_5))
                            .replace("[#d6]", getString(R.string.day_of_week_6));
                }
                reportContent = reportContent + "<TD>" + colVal + "</TD>\n";
            }
            reportContent = reportContent + "</TR>\n";
        }
        reportContent = reportContent + "</table>\n"
                + "<br><br><p align=\"center\"> Created with <a href=\"http://www.andicar.org\" target=\"new\">AndiCar</a>\n" + "</body>\n" + "</html>";

        return reportContent;
    }


    @Override
    public void run() {
        createReport();
    }
}
