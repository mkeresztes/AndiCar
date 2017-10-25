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

package andicar.n.utils;

import android.graphics.Color;

import com.google.api.services.gmail.GmailScopes;

import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * Created by Miklós Keresztes on 6/21/16.
 * <p>
 * Application level constant values
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ConstantValues {

    //showing or not the debug info about the screen size
    public static final boolean DEBUG_IS_SHOW_INFO_IN_FRAGMENTS = false;

    public static final int DATABASE_VERSION = 502;
    public static final String DATABASE_NAME = "AndiCar.db";
    public static final String EXPENSES_COL_FROMREFUEL_TABLE_NAME = "Refuel";
    public static final String DAY_OF_WEEK_NAME = "DayOfWeek";
    public static final String BACKUP_PREFIX = "bk_";
    public static final String BACKUP_SUFIX = ".db";
    public static final String BACKUP_SERVICE_DAILY = "D";
    public static final String BACKUP_SERVICE_WEEKLY = "W";
    public static final int REQUEST_ACCESS_EXTERNAL_STORAGE = 1000;
    public static final int REQUEST_GET_ACCOUNTS = 1001;
    public static final int REQUEST_GOOGLE_AUTHORIZATION = 1002;
    public static final int REQUEST_ACCOUNT_PICKER = 1003;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1004;
    public static final int REQUEST_LOCATION_ACCESS = 1005;
    public static final int REQUEST_BACKUP_SERVICE_SCHEDULE = 1006;
    public static final int REQUEST_OPEN_DRIVE_FOLDER = 1007;
    public static final int REQUEST_RESOLVE_CONNECTION = 1008;
    public static final String[] GOOGLE_SCOPES = {GmailScopes.GMAIL_SEND};
    public static final int NOTIF_SECUREBK_NO_EMAIL_TO = 1000;
    public static final int NOTIF_SECUREBK_FILE_NOT_FOUND = 1001;
    public static final int NOTIF_SECUREBK_POSTPONED_OR_SENT = 1002;
    public static final int NOTIF_BACKUP_SERVICE_SUCCESS = 1003;
    public static final long ONE_DAY_IN_MILISECONDS = 86400000;
    public static final int BACKUP_SERVICE_INTENT_REQUEST_CODE = 0;
    public static final String BACKUP_SERVICE_OPERATION = "Operation";
    public static final String BACKUP_SERVICE_OPERATION_SET_NEXT_RUN = "SetNextRun";
    public static final String BACKUP_SERVICE_OPERATION_NORMAL = "Normal";
    //    public static final int DECIMALS_QUANTITY = 2;
//    public static final RoundingMode ROUNDING_MODE_QUANTITY = RoundingMode.HALF_UP;
    public static final int DECIMALS_LENGTH = 2;
    public static final RoundingMode ROUNDING_MODE_LENGTH = RoundingMode.HALF_UP;
    public static final int DECIMALS_AMOUNT = 2;
    public static final RoundingMode ROUNDING_MODE_AMOUNT = RoundingMode.HALF_UP;
    public static final int DECIMALS_RATES = 5;
    public static final RoundingMode ROUNDING_MODE_RATES = RoundingMode.HALF_UP;
    public static final int DECIMALS_PRICE = 3;
    public static final RoundingMode ROUNDING_MODE_PRICE = RoundingMode.HALF_UP;
    public static final String DATE_DECODE_TO_ZERO = "0";
    public static final RoundingMode ROUNDING_MODE_VOLUME = RoundingMode.HALF_UP;
    public static final int DECIMALS_VOLUME = 3;
    public static final RoundingMode ROUNDING_MODE_FUEL_EFF = RoundingMode.HALF_UP;
    public static final int DECIMALS_FUEL_EFF = 2;

    public static final int CONTEXT_MENU_EDIT_ID = 31;
    public static final int CONTEXT_MENU_DELETE_ID = 33;

    /**
     * upper the hour to 23:59.999
     */
    public static final String DATE_DECODE_TO_24 = "24";
    public static final String UOM_LENGTH_TYPE_CODE = "L";
    public static final String UOM_VOLUME_TYPE_CODE = "V";
    public static final String UOM_OTHER_TYPE_CODE = "O";
    public static final String IS_INITIALIZATION_IN_PROGRESS_TAG = "InitializationInProgress";
    public static final String ANALYTICS_IS_MULTI_CURRENCY = "MultiCurrencyUsed";
    public static final String ANALYTICS_IS_TEMPLATE_USED = "TemplateUsed";
    public static final String ANALYTICS_IS_CREATE_MILEAGE = "CreateMileage";
    public static final String ANALYTICS_IS_FROM_BT_CONNECTION = "FromBTConnection";
    public static final String ANALYTICS_IS_MULTI_UOM = "MultiUOMUsed";
    public static String BASE_FOLDER;
    public static String REPORT_FOLDER;
    public static String BACKUP_FOLDER;
    public static String SYSTEM_BACKUP_FOLDER;
    public static String TRACK_FOLDER;
    public static String TEMP_FOLDER;
    public static String LOG_FOLDER;
    public static String REPORT_FOLDER_NAME = "reports";
    public static String BACKUP_FOLDER_NAME = "backups";
    public static String SYSTEM_BACKUP_FOLDER_NAME = "sysbackups";
    public static String TRACK_FOLDER_NAME = "gpstrack";
    public static String TEMP_FOLDER_NAME = "temp";
    public static String LOG_FOLDER_NAME = "log";
    //for the colors see https://material.io/guidelines/style/color.html#color-color-palette
    public static ArrayList<Integer> CHART_COLORS = new ArrayList<Integer>() {{
        add(Color.parseColor("#F44336")); //red:500
        add(Color.parseColor("#4CAF50")); //green:500
        add(Color.parseColor("#2196F3")); //blue:500
        add(Color.parseColor("#FFC107")); //amber:500
        add(Color.parseColor("#E91E63")); //pink:500
        add(Color.parseColor("#009688")); //teal:500
        add(Color.parseColor("#673AB7")); //deep blue:500
        add(Color.parseColor("#FF9800")); //orange:500
        add(Color.parseColor("#8BC34A")); //light green:500
        add(Color.parseColor("#03A9F4")); //light blue:500
    }};
}
