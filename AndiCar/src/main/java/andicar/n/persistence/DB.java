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
package andicar.n.persistence;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.util.Calendar;
import java.util.Currency;

import andicar.n.utils.AndiCarCrashReporter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Database object names and database creation/update
 *
 * @author miki
 */
@SuppressWarnings({"unused", "SpellCheckingInspection", "WeakerAccess"})
public class DB {
    private static final String TAG = "DBAdapter";

    //@formatter:off

    // drivers
    public static final String TABLE_NAME_DRIVER = "DEF_DRIVER";
    // cars
    public static final String TABLE_NAME_CAR = "DEF_CAR";
    // uom
    public static final String TABLE_NAME_UOM = "DEF_UOM";
    // expense types
    public static final String TABLE_NAME_EXPENSETYPE = "DEF_EXPENSETYPE";
    // uom conversion rates
    public static final String TABLE_NAME_UOMCONVERSION = "DEF_UOMCONVERTIONRATE";
    // currencies
    public static final String TABLE_NAME_CURRENCY = "DEF_CURRENCY";
    // mileages
    public static final String TABLE_NAME_MILEAGE = "CAR_MILEAGE";
    // refuel
    public static final String TABLE_NAME_REFUEL = "CAR_REFUEL";
    // expense categories (eg. Refuel, Service, Insurance, etc.
    public static final String TABLE_NAME_EXPENSECATEGORY = "DEF_EXPENSECATEGORY";
    // car expenses
    public static final String TABLE_NAME_EXPENSE = "CAR_EXPENSE";
    // currency rate
    public static final String TABLE_NAME_CURRENCYRATE = "DEF_CURRENCYRATE";
    // gps track table
    public static final String TABLE_NAME_GPSTRACK = "GPS_TRACK";
    public static final String TABLE_NAME_GPSTRACKDETAIL = "GPS_TRACKDETAIL";
    // business partner table
    public static final String TABLE_NAME_BPARTNER = "DEF_BPARTNER";
    // business partner locations table
    public static final String TABLE_NAME_BPARTNERLOCATION = "DEF_BPARTNERLOCATION";
    // tags table
    public static final String TABLE_NAME_TAG = "DEF_TAG";
    // tasks/reminders/to-do tables
    public static final String TABLE_NAME_TASKTYPE = "DEF_TASKTYPE";
    public static final String TABLE_NAME_TASK = "DEF_TASK";
    public static final String TABLE_NAME_TASK_CAR = "TASK_CAR";
    public static final String TABLE_NAME_TODO = "TASK_TODO";
    // link table between cars and reimbursement rates
    public static final String TABLE_NAME_REIMBURSEMENT_CAR_RATES = "REIMBURSEMENT_CAR";
    @SuppressWarnings("WeakerAccess")
    public static final String TABLE_NAME_SECURE_BK_SETTINGS = "AO_SECURE_BK_SETTINGS";
    public static final String TABLE_NAME_DATA_TEMPLATE = "AO_DATA_TEMPLATE";
    public static final String TABLE_NAME_DATA_TEMPLATE_VALUES = "AO_DATA_TEMPLATE_VALUES";
    /**
     * links between bluetooth devices and cars
     */
    public static final String TABLE_NAME_BTDEVICE_CAR = "AO_BTDEVICE_CAR";

    /**
     * used in MainActivity to prevent multiple display of the same message.
     * mappings:
     *  Name -> the message id
     */
    public static final String TABLE_NAME_DISPLAYED_MESSAGES = "MSG_DISPLAYED_MESSAGES";
    /**
     * mappings:
     *  Name -> message title
     *  UserComment -> message body
     */
    public static final String TABLE_NAME_MESSAGES = "MSG_MESSAGES";

    // column names. Some is general (GEN_) some is particular
    // generic columns must be first and must be created for ALL TABLES
    public static final String COL_NAME_GEN_ROWID = "_id";
    public static final String COL_NAME_GEN_NAME = "Name";
    public static final String COL_NAME_GEN_ISACTIVE = "IsActive";
    public static final String COL_NAME_GEN_USER_COMMENT = "UserComment";
    // driver specific column names
    public static final String COL_NAME_DRIVER__LICENSE_NO = "LicenseNo";
    // car specific column names
    public static final String COL_NAME_CAR__MODEL = "Model";
    public static final String COL_NAME_CAR__REGISTRATIONNO = "RegistrationNo";
    public static final String COL_NAME_CAR__INDEXSTART = "IndexStart";
    public static final String COL_NAME_CAR__INDEXCURRENT = "IndexCurrent";
    public static final String COL_NAME_CAR__LENGTH_UOM_ID = TABLE_NAME_UOM + "_Length_ID";
    public static final String COL_NAME_CAR__FUEL_UOM_ID = TABLE_NAME_UOM + "_Volume_ID";
    public static final String COL_NAME_CAR__CURRENCY_ID = TABLE_NAME_CURRENCY + "_ID";
    public static final String COL_NAME_CAR__ISAFV = "IsAFV"; //Alternative fuel vehicle
    public static final String COL_NAME_CAR__ALTERNATIVE_FUEL_UOM_ID = TABLE_NAME_UOM + "_AF_ID";
    // uom specific column names
    public static final String COL_NAME_UOM__CODE = "Code";
    public static final String COL_NAME_UOM__UOMTYPE = "UOMType"; // V - Volume
    // or L -
    // Length
    // uom conversion specific column names
    public static final String COL_NAME_UOMCONVERSION__UOMFROM_ID = TABLE_NAME_UOM + "_From_ID";
    public static final String COL_NAME_UOMCONVERSION__UOMTO_ID = TABLE_NAME_UOM + "_To_ID";
    public static final String COL_NAME_UOMCONVERSION__RATE = "ConvertionRate";
    // mileage specific columns
    public static final String COL_NAME_MILEAGE__DATE = "Date";
    public static final String COL_NAME_MILEAGE__CAR_ID = TABLE_NAME_CAR + "_ID";
    public static final String COL_NAME_MILEAGE__DRIVER_ID = TABLE_NAME_DRIVER + "_ID";
    public static final String COL_NAME_MILEAGE__INDEXSTART = "IndexStart";
    public static final String COL_NAME_MILEAGE__INDEXSTOP = "IndexStop";
    public static final String COL_NAME_MILEAGE__UOMLENGTH_ID = TABLE_NAME_UOM + "_Length_ID";
    public static final String COL_NAME_MILEAGE__EXPENSETYPE_ID = TABLE_NAME_EXPENSETYPE + "_ID";
    public static final String COL_NAME_MILEAGE__GPSTRACKLOG = "GPSTrackLog";
    public static final String COL_NAME_MILEAGE__TAG_ID = TABLE_NAME_TAG + "_ID";
    public static final String COL_NAME_MILEAGE__DATE_TO = "DateTo";
    // currencies
    public static final String COL_NAME_CURRENCY__CODE = "Code";
    // refuel
    public static final String COL_NAME_REFUEL__CAR_ID = TABLE_NAME_CAR + "_ID";
    public static final String COL_NAME_REFUEL__DRIVER_ID = TABLE_NAME_DRIVER + "_ID";
    public static final String COL_NAME_REFUEL__EXPENSETYPE_ID = TABLE_NAME_EXPENSETYPE + "_ID";
    public static final String COL_NAME_REFUEL__INDEX = "CarIndex";
    public static final String COL_NAME_REFUEL__QUANTITY = "Quantity";
    public static final String COL_NAME_REFUEL__UOMVOLUME_ID = TABLE_NAME_UOM + "_Volume_ID";
    public static final String COL_NAME_REFUEL__PRICE = "Price";
    public static final String COL_NAME_REFUEL__CURRENCY_ID = TABLE_NAME_CURRENCY + "_ID";
    public static final String COL_NAME_REFUEL__DATE = "Date";
    public static final String COL_NAME_REFUEL__DOCUMENTNO = "DocumentNo";
    public static final String COL_NAME_REFUEL__EXPENSECATEGORY_ID = TABLE_NAME_EXPENSECATEGORY + "_ID";
    public static final String COL_NAME_REFUEL__ISFULLREFUEL = "IsFullRefuel";
    public static final String COL_NAME_REFUEL__QUANTITYENTERED = "QuantityEntered";
    public static final String COL_NAME_REFUEL__UOMVOLUMEENTERED_ID = TABLE_NAME_UOM + "_EnteredVolume_ID";
    public static final String COL_NAME_REFUEL__PRICEENTERED = "PriceEntered";
    public static final String COL_NAME_REFUEL__CURRENCYENTERED_ID = TABLE_NAME_CURRENCY + "_Entered_ID";
    public static final String COL_NAME_REFUEL__CURRENCYRATE = "CurrencyRate"; // CurrencyEntered -> Car Base Currency
    public static final String COL_NAME_REFUEL__UOMVOLCONVERSIONRATE = "UOMVolumeConversionRate";
    public static final String COL_NAME_REFUEL__AMOUNT = "Amount";
    public static final String COL_NAME_REFUEL__AMOUNTENTERED = "AmountEntered";
    public static final String COL_NAME_REFUEL__BPARTNER_ID = TABLE_NAME_BPARTNER + "_ID";
    public static final String COL_NAME_REFUEL__BPARTNER_LOCATION_ID = TABLE_NAME_BPARTNERLOCATION + "_ID";
    public static final String COL_NAME_REFUEL__TAG_ID = TABLE_NAME_TAG + "_ID";
    public static final String COL_NAME_REFUEL__ISALTERNATIVEFUEL = "IsAlternativeFuel";
    // expense category
    public static final String COL_NAME_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST = "IsExcludefromMileagecost";
    public static final String COL_NAME_EXPENSECATEGORY__ISFUEL = "IsFuel";
    public static final String COL_NAME_EXPENSECATEGORY__UOMTYPE = "UOMType"; //see ConstantValues UOM_TYPE_ ... codes
    // car expenses
    public static final String COL_NAME_EXPENSE__CAR_ID = TABLE_NAME_CAR + "_ID";
    public static final String COL_NAME_EXPENSE__DRIVER_ID = TABLE_NAME_DRIVER + "_ID";
    public static final String COL_NAME_EXPENSE__EXPENSECATEGORY_ID = TABLE_NAME_EXPENSECATEGORY + "_ID";
    public static final String COL_NAME_EXPENSE__EXPENSETYPE_ID = TABLE_NAME_EXPENSETYPE + "_ID";
    public static final String COL_NAME_EXPENSE__AMOUNT = "Amount";
    public static final String COL_NAME_EXPENSE__CURRENCY_ID = TABLE_NAME_CURRENCY + "_ID";
    public static final String COL_NAME_EXPENSE__DATE = "Date";
    public static final String COL_NAME_EXPENSE__DOCUMENTNO = "DocumentNo";
    public static final String COL_NAME_EXPENSE__INDEX = "CarIndex";
    @SuppressWarnings("WeakerAccess")
    public static final String COL_NAME_EXPENSE__FROMTABLE = "FromTable";
    @SuppressWarnings("WeakerAccess")
    public static final String COL_NAME_EXPENSE__FROMRECORD_ID = "FromRecordId";
    public static final String COL_NAME_EXPENSE__AMOUNTENTERED = "AmountEntered";
    public static final String COL_NAME_EXPENSE__CURRENCYENTERED_ID = TABLE_NAME_CURRENCY + "_Entered_ID";
    public static final String COL_NAME_EXPENSE__CURRENCYRATE = "CurrencyRate";
    public static final String COL_NAME_EXPENSE__QUANTITY = "Quantity";
    public static final String COL_NAME_EXPENSE__PRICE = "Price";
    public static final String COL_NAME_EXPENSE__PRICEENTERED = "PriceEntered";
    public static final String COL_NAME_EXPENSE__UOM_ID = TABLE_NAME_UOM + "_ID";
    public static final String COL_NAME_EXPENSE__BPARTNER_ID = TABLE_NAME_BPARTNER + "_ID";
    public static final String COL_NAME_EXPENSE__BPARTNER_LOCATION_ID = TABLE_NAME_BPARTNERLOCATION + "_ID";
    public static final String COL_NAME_EXPENSE__TAG_ID = TABLE_NAME_TAG + "_ID";
    // currency rate
    public static final String COL_NAME_CURRENCYRATE__FROMCURRENCY_ID = TABLE_NAME_CURRENCYRATE + "_From_ID";
    public static final String COL_NAME_CURRENCYRATE__TOCURRENCY_ID = TABLE_NAME_CURRENCYRATE + "_To_ID";
    public static final String COL_NAME_CURRENCYRATE__RATE = "Rate";
    public static final String COL_NAME_CURRENCYRATE__INVERSERATE = "InverseRate";
    // gps track
    public static final String COL_NAME_GPSTRACK__CAR_ID = TABLE_NAME_CAR + "_ID";
    public static final String COL_NAME_GPSTRACK__DRIVER_ID = TABLE_NAME_DRIVER + "_ID";
    public static final String COL_NAME_GPSTRACK__MILEAGE_ID = TABLE_NAME_MILEAGE + "_ID";
    public static final String COL_NAME_GPSTRACK__DATE = "Date";
    public static final String COL_NAME_GPSTRACK__MINACCURACY = "MinAccuracy";
    public static final String COL_NAME_GPSTRACK__AVGACCURACY = "AvgAccuracy";
    public static final String COL_NAME_GPSTRACK__MAXACCURACY = "MaxAccuracy";
    public static final String COL_NAME_GPSTRACK__MINALTITUDE = "MinAltitude";
    public static final String COL_NAME_GPSTRACK__MAXALTITUDE = "MaxAltitude";
    public static final String COL_NAME_GPSTRACK__TOTALTIME = "TotalTime"; // in seconds
    public static final String COL_NAME_GPSTRACK__MOVINGTIME = "MovingTime"; // in seconds
    public static final String COL_NAME_GPSTRACK__DISTANCE = "Distance";
    public static final String COL_NAME_GPSTRACK__MAXSPEED = "MaxSpeed";
    public static final String COL_NAME_GPSTRACK__AVGSPEED = "AvgSpeed";
    public static final String COL_NAME_GPSTRACK__AVGMOVINGSPEED = "AvgMovingSpeed";
    public static final String COL_NAME_GPSTRACK__TOTALTRACKPOINTS = "TotalTrackPoints";
    public static final String COL_NAME_GPSTRACK__INVALIDTRACKPOINTS = "InvalidTrackPoints";
    public static final String COL_NAME_GPSTRACK__TAG_ID = TABLE_NAME_TAG + "_ID";
    public static final String COL_NAME_GPSTRACK__TOTALPAUSETIME = "TotalPauseTime"; // in seconds
    public static final String COL_NAME_GPSTRACK__EXPENSETYPE_ID = TABLE_NAME_EXPENSETYPE + "_ID";

    // gps track detail
    public static final String COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID = TABLE_NAME_GPSTRACK + "_ID";
    public static final String COL_NAME_GPSTRACKDETAIL__FILE = "File";
    public static final String COL_NAME_GPSTRACKDETAIL__FILEFORMAT = "Format"; // see StaticValues.gpsTrackFormat...

    //business partner
    public static final String COL_NAME_BPARTNER__ISGASSTATION = "IsGasStation";
    // business partner location
    public static final String COL_NAME_BPARTNERLOCATION__BPARTNER_ID = TABLE_NAME_BPARTNER + "_ID";
    public static final String COL_NAME_BPARTNERLOCATION__ADDRESS = "Address";
    public static final String COL_NAME_BPARTNERLOCATION__POSTAL = "Postal";
    public static final String COL_NAME_BPARTNERLOCATION__CITY = "City";
    public static final String COL_NAME_BPARTNERLOCATION__REGION = "Region";
    public static final String COL_NAME_BPARTNERLOCATION__COUNTRY = "Country";
    public static final String COL_NAME_BPARTNERLOCATION__PHONE = "Phone";
    public static final String COL_NAME_BPARTNERLOCATION__PHONE2 = "Phone2";
    public static final String COL_NAME_BPARTNERLOCATION__FAX = "Fax";
    public static final String COL_NAME_BPARTNERLOCATION__EMAIL = "Email";
    public static final String COL_NAME_BPARTNERLOCATION__CONTACTPERSON = "ContactPerson";


    public static final String COL_NAME_TASK__TASKTYPE_ID = TABLE_NAME_TASKTYPE + "_ID";
    /**
     * Time|Mileage|Both (StaticValues.TASK_SCHEDULED_FOR_{TIME|MILEAGE|BOTH})
     */
    public static final String COL_NAME_TASK__SCHEDULEDFOR = "ScheduledFor";
    /**
     * recurrent or one time task {Y|N}
     */
    public static final String COL_NAME_TASK__ISRECURRENT = "IsRecurrent";
    public static final String COL_NAME_TASK__ISDIFFERENTSTARTINGTIME = "IsDifferentSTime";
    /**
     * recurrently (every X days/weeks/months/years depending on
     * TASK_COL_TIMEFREQUENCYTYPE_NAME)
     */
    public static final String COL_NAME_TASK__TIMEFREQUENCY = "TimeFrequency";
    /**
     * Type integer<br>
     * Frequency type: 0 = One time, 1 = Daily, 2 = Weekly, 3 = Monthly, 4 =
     * Yearly (StaticValues.TASK_TIMEFREQUENCYTYPE_...
     */
    public static final String COL_NAME_TASK__TIMEFREQUENCYTYPE = "TimeFrequencyType";
    /**
     * Type Date<br>
     * <br>
     * If IsRecurrent = 'Y': <li>The starting date<br>
     * <br>
     * If IsRecurrent = 'N': <li>The run date <li>1970-mm-01 hh:mm if
     * TASK_COL_TIMEFREQUENCYTYPE_NAME = Month and if is LastDay of the month
     */
    public static final String COL_NAME_TASK__STARTINGTIME = "StartingTime";
    /**
     * Type integer <br>
     * <li>No. of days to start reminders if TASK_COL_TIMEFREQUENCYTYPE_NAME !=
     * Day <li>No. of minutes to start reminders if
     * TASK_COL_TIMEFREQUENCYTYPE_NAME == Day
     */
    public static final String COL_NAME_TASK__TIMEREMINDERSTART = "TimeReminderStart";
    /**
     * If IsRecurrent = 'Y': <li>Run on every mileage<br>
     * else <li>Run on mileage
     */
    public static final String COL_NAME_TASK__RUNMILEAGE = "RunMileage";
    /**
     * No. of km|mi to start reminders
     */
    public static final String COL_NAME_TASK__MILEAGEREMINDERSTART = "MileageReminderStart";
    /**
     * How many todos will be generated for this task
     */
    public static final String COL_NAME_TASK__TODOCOUNT = "TodoCount";
    /**
     * the task from where this to-do come
     */
    public static final String COL_NAME_TODO__TASK_ID = TABLE_NAME_TASK + "_ID";
    /**
     * the linked car to the task (if exist)
     */
    public static final String COL_NAME_TODO__CAR_ID = TABLE_NAME_CAR + "_ID";
    /**
     * the due date based on start time and recurrent settings
     */
    public static final String COL_NAME_TODO__DUEDATE = "DueDate";
    /**
     * the due mileage based on starting mileage and recurrent mileage
     */
    public static final String COL_NAME_TODO__DUEMILEAGE = "DueMileage";
    /**
     * show the notification at this date
     */
    public static final String COL_NAME_TODO__NOTIFICATIONDATE = "NotificationDate";
    /**
     * show the notification at this mileage
     */
    public static final String COL_NAME_TODO__NOTIFICATIONMILEAGE = "NotificationMileage";
    /**
     * if this to-do is done {Y|N}
     */
    public static final String COL_NAME_TODO__ISDONE = "IsDone";
    /**
     * the date when this to-do was done
     */
    private static final String COL_NAME_TODO__DONEDATE = "DoneDate";
    /**
     * stop the notification for this to-do, even if is not done
     */
    private static final String COL_NAME_TODO__ISSTOPNOTIFICATION = "IsStopNotification";

    public static final String COL_NAME_TASK_CAR__TASK_ID = TABLE_NAME_TASK + "_ID";
    public static final String COL_NAME_TASK_CAR__CAR_ID = TABLE_NAME_CAR + "_ID";
    public static final String COL_NAME_TASK_CAR__FIRSTRUN_DATE = "FirstRunDate";
    public static final String COL_NAME_TASK_CAR__FIRSTRUN_MILEAGE = "FirstRunMileage";

    public static final String COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID = TABLE_NAME_CAR + "_ID";
    public static final String COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID = TABLE_NAME_EXPENSE + "_ID";
    public static final String COL_NAME_REIMBURSEMENT_CAR_RATES__RATE = "Rate";
    public static final String COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM = "ValidFrom";
    public static final String COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO = "ValidTo";
    @SuppressWarnings("WeakerAccess")
    public static final String COL_NAME_SECUREBK__ISINCLUDEGPS = "IsIncludeGPSTrack";
    @SuppressWarnings("WeakerAccess")
    public static final String COL_NAME_SECUREBK__ISINCLUDEREPORTS = "IsIncludeReports";
    @SuppressWarnings("WeakerAccess")
    public static final String COL_NAME_SECUREBK__ISSHOWNOTIF = "IsShowNotification";

    public static final String COL_NAME_DATATEMPLATE__CLASS = "ForClass";
    public static final String COL_NAME_DATATEMPLATEVALUES__TEMPLATE_ID = TABLE_NAME_DATA_TEMPLATE + "_ID";
    public static final String COL_NAME_DATATEMPLATEVALUES__VALUE = "Value";

    public static final String COL_NAME_BTDEVICECAR__MACADDR = "DeviceMACAddress";
    public static final String COL_NAME_BTDEVICECAR__CAR_ID = TABLE_NAME_CAR + "_ID";

    public static final String COL_NAME_MESSAGES__MESSAGE_ID = "MessageID";
    public static final String COL_NAME_MESSAGES__DATE = "MessageDate";
    public static final String COL_NAME_MESSAGES__IS_STARRED = "IsStarred";
    public static final String COL_NAME_MESSAGES__IS_READ = "IsRead";


    // column positions. Some is general (GEN_) some is particular
    // generic columns must be first and must be created for ALL TABLES
    public static final int COL_POS_GEN_ROWID = 0;
    public static final int COL_POS_GEN_NAME = 1;
    public static final int COL_POS_GEN_ISACTIVE = 2;
    public static final int COL_POS_GEN_USER_COMMENT = 3;
    // driver specific column positions
    public static final int COL_POS_DRIVER__LICENSE_NO = 4;
    // car specific column positions
    public static final int COL_POS_CAR__MODEL = 4;
    public static final int COL_POS_CAR__REGISTRATIONNO = 5;
    public static final int COL_POS_CAR__INDEXSTART = 6;
    public static final int COL_POS_CAR__INDEXCURRENT = 7;
    public static final int COL_POS_CAR__LENGTH_UOM_ID = 8;
    public static final int COL_POS_CAR__FUEL_UOM_ID = 9;
    public static final int COL_POS_CAR__CURRENCY_ID = 10;
    public static final int COL_POS_CAR__ISAFV = 11;
    public static final int COL_POS_CAR__ALTERNATIVE_FUEL_UOM_ID = 12;
    // uom specific column positions
    public static final int COL_POS_UOM__CODE = 4;
    public static final int COL_POS_UOM__UOMTYPE = 5;
    // uom conversion specific column positions
    public static final int COL_POS_UOMCONVERSION__UOMFROM_ID = 4;
    public static final int COL_POS_UOMCONVERSION__UOMTO_ID = 5;
    public static final int COL_POS_UOMCONVERSION__RATE = 6;
    // mileage specific column positions
    public static final int COL_POS_MILEAGE__DATE = 4;
    public static final int COL_POS_MILEAGE__CAR_ID = 5;
    public static final int COL_POS_MILEAGE__DRIVER_ID = 6;
    public static final int COL_POS_MILEAGE__INDEXSTART = 7;
    public static final int COL_POS_MILEAGE__INDEXSTOP = 8;
    public static final int COL_POS_MILEAGE__UOMLENGTH_ID = 9;
    public static final int COL_POS_MILEAGE__EXPENSETYPE_ID = 10;
    public static final int COL_POS_MILEAGE__GPSTRACKLOG = 11;
    public static final int COL_POS_MILEAGE__TAG_ID = 12;
    public static final int COL_POS_MILEAGE__DATE_TO = 13;
    // currencies
    public static final int COL_POS_CURRENCY__CODE = 4;
    // refuel
    public static final int COL_POS_REFUEL__CAR_ID = 4;
    public static final int COL_POS_REFUEL__DRIVER_ID = 5;
    public static final int COL_POS_REFUEL__EXPENSETYPE_ID = 6;
    public static final int COL_POS_REFUEL__INDEX = 7;
    public static final int COL_POS_REFUEL__QUANTITY = 8;
    public static final int COL_POS_REFUEL__UOMVOLUME_ID = 9;
    public static final int COL_POS_REFUEL__PRICE = 10;
    public static final int COL_POS_REFUEL__CURRENCY_ID = 11;
    public static final int COL_POS_REFUEL__DATE = 12;
    public static final int COL_POS_REFUEL__DOCUMENTNO = 13;
    public static final int COL_POS_REFUEL__EXPENSECATEGORY_ID = 14;
    public static final int COL_POS_REFUEL__ISFULLREFUEL = 15;
    public static final int COL_POS_REFUEL__QUANTITYENTERED = 16;
    public static final int COL_POS_REFUEL__UOMVOLUMEENTERED_ID = 17;
    public static final int COL_POS_REFUEL__PRICEENTERED = 18;
    public static final int COL_POS_REFUEL__CURRENCYENTERED_ID = 19;
    public static final int COL_POS_REFUEL__CURRENCYRATE = 20;
    public static final int COL_POS_REFUEL__UOMVOLCONVERSIONRATE = 21;
    public static final int COL_POS_REFUEL__AMOUNT = 22;
    public static final int COL_POS_REFUEL__AMOUNTENTERED = 23;
    public static final int COL_POS_REFUEL__BPARTNER_ID = 24;
    public static final int COL_POS_REFUEL__BPARTNER_LOCATION_ID = 25;
    public static final int COL_POS_REFUEL__TAG_ID = 26;
    public static final int COL_POS_REFUEL__ISALTERNATIVEFUEL = 27;
    // expense category
    public static final int COL_POS_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST = 4;
    public static final int COL_POS_EXPENSECATEGORY__ISFUEL = 5;
    public static final int COL_POS_EXPENSECATEGORY__UOMTYPE = 6;
    // car expenses
    public static final int COL_POS_EXPENSE__CAR_ID = 4;
    public static final int COL_POS_EXPENSE__DRIVER_ID = 5;
    public static final int COL_POS_EXPENSE__EXPENSECATEGORY = 6;
    public static final int COL_POS_EXPENSE__EXPENSETYPE_ID = 7;
    public static final int COL_POS_EXPENSE__AMOUNT = 8;
    public static final int COL_POS_EXPENSE__CURRENCY_ID = 9;
    public static final int COL_POS_EXPENSE__DATE = 10;
    public static final int COL_POS_EXPENSE__DOCUMENTNO = 11;
    public static final int COL_POS_EXPENSE__INDEX = 12;
    public static final int COL_POS_EXPENSE__FROMTABLE = 13;
    public static final int COL_POS_EXPENSE__FROMRECORD = 14;
    public static final int COL_POS_EXPENSE__AMOUNTENTERED = 15;
    public static final int COL_POS_EXPENSE__CURRENCYENTERED_ID = 16;
    public static final int COL_POS_EXPENSE__CURRENCYRATE = 17;
    public static final int COL_POS_EXPENSE__QUANTITY = 18;
    public static final int COL_POS_EXPENSE__PRICE = 19;
    public static final int COL_POS_EXPENSE__PRICEENTERED = 20;
    public static final int COL_POS_EXPENSE__UOM_ID = 21;
    public static final int COL_POS_EXPENSE__BPARTNER_ID = 22;
    public static final int COL_POS_EXPENSE__BPARTNER_LOCATION_ID = 23;
    public static final int COL_POS_EXPENSE__TAG_ID = 24;
    // currency rate
    public static final int COL_POS_CURRENCYRATE__FROMCURRENCY_ID = 4;
    public static final int COL_POS_CURRENCYRATE__TOCURRENCY_ID = 5;
    public static final int COL_POS_CURRENCYRATE__RATE = 6;
    public static final int COL_POS_CURRENCYRATE__INVERSERATE = 7;
    // gps track
    public static final int COL_POS_GPSTRACK__CAR_ID = 4;
    public static final int COL_POS_GPSTRACK__DRIVER_ID = 5;
    public static final int COL_POS_GPSTRACK__MILEAGE_ID = 6;
    public static final int COL_POS_GPSTRACK__DATE = 7;
    public static final int COL_POS_GPSTRACK__MINACCURACY = 8;
    public static final int COL_POS_GPSTRACK__AVGACCURACY = 9;
    public static final int COL_POS_GPSTRACK__MAXACCURACY = 10;
    public static final int COL_POS_GPSTRACK__MINALTITUDE = 11;
    public static final int COL_POS_GPSTRACK__MAXALTITUDE = 12;
    public static final int COL_POS_GPSTRACK__TOTALTIME = 13;
    public static final int COL_POS_GPSTRACK__MOVINGTIME = 14;
    public static final int COL_POS_GPSTRACK__DISTANCE = 15;
    public static final int COL_POS_GPSTRACK__MAXSPEED = 16;
    public static final int COL_POS_GPSTRACK__AVGSPEED = 17;
    public static final int COL_POS_GPSTRACK__AVGMOVINGSPEED = 18;
    public static final int COL_POS_GPSTRACK__TOTALTRACKPOINTS = 19;
    public static final int COL_POS_GPSTRACK__INVALIDTRACKPOINTS = 20;
    public static final int COL_POS_GPSTRACK__TAG_ID = 21;
    public static final int COL_POS_GPSTRACK__TOTALPAUSETIME = 22;
    public static final int COL_POS_GPSTRACK__EXPENSETYPE_ID = 23;
    // gps track detail
    public static final int COL_POS_GPSTRACKDETAIL__GPSTRACK_ID = 4;
    public static final int COL_POS_GPSTRACKDETAIL__FILE = 5;
    public static final int COL_POS_GPSTRACKDETAIL__FILEFORMAT = 6;
    //business partner
    public static final int COL_POS_BPARTNER__ISGASSTATION = 4;
    // business partner location
    public static final int COL_POS_BPARTNERLOCATION__BPARTNER_ID = 4;
    public static final int COL_POS_BPARTNERLOCATION__ADDRESS = 5;
    public static final int COL_POS_BPARTNERLOCATION__POSTAL = 6;
    public static final int COL_POS_BPARTNERLOCATION__CITY = 7;
    public static final int COL_POS_BPARTNERLOCATION__REGION = 8;
    public static final int COL_POS_BPARTNERLOCATION__COUNTRY = 9;
    public static final int COL_POS_BPARTNERLOCATION__PHONE = 10;
    public static final int COL_POS_BPARTNERLOCATION__PHONE2 = 11;
    public static final int COL_POS_BPARTNERLOCATION__FAX = 12;
    public static final int COL_POS_BPARTNERLOCATION__EMAIL = 13;
    public static final int COL_POS_BPARTNERLOCATION__CONTACTPERSON = 14;
    public static final int COL_POS_TASK__TASKTYPE_ID = 4;
    public static final int COL_POS_TASK__SCHEDULEDFOR = 5;
    public static final int COL_POS_TASK__ISRECURRENT = 6;
    public static final int COL_POS_TASK__ISDIFFERENTSTARTINGTIME = 7;
    public static final int COL_POS_TASK__TIMEFREQUENCY = 8;
    public static final int COL_POS_TASK__TIMEFREQUENCYTYPE = 9;
    public static final int COL_POS_TASK__STARTINGTIME = 10;
    public static final int COL_POS_TASK__TIMEREMINDERSTART = 11;
    public static final int COL_POS_TASK__RUNMILEAGE = 12;
    public static final int COL_POS_TASK__MILEAGEREMINDERSTART = 13;
    public static final int COL_POS_TASK__TODOCOUNT = 14;
    public static final int COL_POS_TASK_CAR__TASK_ID = 4;
    public static final int COL_POS_TASK_CAR__CAR_ID = 5;
    public static final int COL_POS_TASK_CAR__FIRSTRUN_DATE = 6;
    public static final int COL_POS_TASK_CAR__FIRSTRUN_MILEAGE = 7;
    public static final int COL_POS_TODO__TASK_ID = 4;
    public static final int COL_POS_TODO__CAR_ID = 5;
    public static final int COL_POS_TODO__DUEDATE = 6;
    public static final int COL_POS_TODO__DUEMILAGE = 7;
    public static final int COL_POS_TODO__NOTIFICATIONDATE = 8;
    public static final int COL_POS_TODO__NOTIFICATIONMILEAGE = 9;
    public static final int COL_POS_TODO__ISDONE = 10;
    public static final int COL_POS_TODO__DONEDATE = 11;
    public static final int COL_POS_TODO__ISSTOPNOTIFICATION = 12;
    public static final int COL_POS_REIMBURSEMENT_CAR_RATES__CAR_ID = 4;
    public static final int COL_POS_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID = 5;
    public static final int COL_POS_REIMBURSEMENT_CAR_RATES__RATE = 6;
    public static final int COL_POS_REIMBURSEMENT_CAR_RATES__VALIDFROM = 7;
    public static final int COL_POS_REIMBURSEMENT_CAR_RATES__VALIDTO = 8;
    public static final int COL_POS_SECUREBK__ISINCLUDEGPS = 4;
    public static final int COL_POS_SECUREBK__ISINCLUDEREPORTS = 5;
    public static final int COL_POS_SECUREBK__ISSHOWNOTIF = 6;
    public static final int COL_POS_DATATEMPLATEVALUES__VALUE = 5;
    public static final int COL_POS_BTDEVICECAR__MACADDR = 4;
    public static final int COL_POS_BTDEVICECAR__CAR_ID = 5;

    public static final int COL_POS_MESSAGES__MESSAGE_ID = 4;
    public static final int COL_POS_MESSAGES__DATE = 5;
    public static final int COL_POS_MESSAGES__IS_STARRED = 6;
    public static final int COL_POS_MESSAGES__IS_READ = 7;

    public static final String[] COL_LIST_DRIVER_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_DRIVER__LICENSE_NO};
    public static final String[] COL_LIST_CAR_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_CAR__MODEL, COL_NAME_CAR__REGISTRATIONNO, COL_NAME_CAR__INDEXSTART, COL_NAME_CAR__INDEXCURRENT, COL_NAME_CAR__LENGTH_UOM_ID,
            COL_NAME_CAR__FUEL_UOM_ID, COL_NAME_CAR__CURRENCY_ID, COL_NAME_CAR__ISAFV, COL_NAME_CAR__ALTERNATIVE_FUEL_UOM_ID};

    public static final String[] COL_LIST_UOM_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_UOM__CODE, COL_NAME_UOM__UOMTYPE};
    public static final String[] COL_LIST_UOMCONVERSION_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_UOMCONVERSION__UOMFROM_ID, COL_NAME_UOMCONVERSION__UOMTO_ID, COL_NAME_UOMCONVERSION__RATE};
    public static final String[] COL_LIST_EXPENSETYPE_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT};
    public static final String[] COL_LIST_MILEAGE_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_MILEAGE__DATE, COL_NAME_MILEAGE__CAR_ID, COL_NAME_MILEAGE__DRIVER_ID, COL_NAME_MILEAGE__INDEXSTART, COL_NAME_MILEAGE__INDEXSTOP,
            COL_NAME_MILEAGE__UOMLENGTH_ID, COL_NAME_MILEAGE__EXPENSETYPE_ID, COL_NAME_MILEAGE__GPSTRACKLOG, COL_NAME_MILEAGE__TAG_ID,
            COL_NAME_MILEAGE__DATE_TO};
    public static final String[] COL_LIST_CURRENCY_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_CURRENCY__CODE};
    public static final String[] COL_LIST_REFUEL_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_REFUEL__CAR_ID, COL_NAME_REFUEL__DRIVER_ID, COL_NAME_REFUEL__EXPENSETYPE_ID, COL_NAME_REFUEL__INDEX, COL_NAME_REFUEL__QUANTITY,
            COL_NAME_REFUEL__UOMVOLUME_ID, COL_NAME_REFUEL__PRICE, COL_NAME_REFUEL__CURRENCY_ID, COL_NAME_REFUEL__DATE, COL_NAME_REFUEL__DOCUMENTNO,
            COL_NAME_REFUEL__EXPENSECATEGORY_ID, COL_NAME_REFUEL__ISFULLREFUEL, COL_NAME_REFUEL__QUANTITYENTERED, COL_NAME_REFUEL__UOMVOLUMEENTERED_ID,
            COL_NAME_REFUEL__PRICEENTERED, COL_NAME_REFUEL__CURRENCYENTERED_ID, COL_NAME_REFUEL__CURRENCYRATE, COL_NAME_REFUEL__UOMVOLCONVERSIONRATE,
            COL_NAME_REFUEL__AMOUNT, COL_NAME_REFUEL__AMOUNTENTERED, COL_NAME_REFUEL__BPARTNER_ID, COL_NAME_REFUEL__BPARTNER_LOCATION_ID,
            COL_NAME_REFUEL__TAG_ID, COL_NAME_REFUEL__ISALTERNATIVEFUEL};
    public static final String[] COL_LIST_EXPENSECATEGORY_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST, COL_NAME_EXPENSECATEGORY__ISFUEL, COL_NAME_EXPENSECATEGORY__UOMTYPE};
    public static final String[] COL_LIST_EXPENSE_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_EXPENSE__CAR_ID, COL_NAME_EXPENSE__DRIVER_ID, COL_NAME_EXPENSE__EXPENSECATEGORY_ID, COL_NAME_EXPENSE__EXPENSETYPE_ID,
            COL_NAME_EXPENSE__AMOUNT, COL_NAME_EXPENSE__CURRENCY_ID, COL_NAME_EXPENSE__DATE, COL_NAME_EXPENSE__DOCUMENTNO, COL_NAME_EXPENSE__INDEX,
            COL_NAME_EXPENSE__FROMTABLE, COL_NAME_EXPENSE__FROMRECORD_ID, COL_NAME_EXPENSE__AMOUNTENTERED, COL_NAME_EXPENSE__CURRENCYENTERED_ID,
            COL_NAME_EXPENSE__CURRENCYRATE, COL_NAME_EXPENSE__QUANTITY, COL_NAME_EXPENSE__PRICE, COL_NAME_EXPENSE__PRICEENTERED, COL_NAME_EXPENSE__UOM_ID,
            COL_NAME_EXPENSE__BPARTNER_ID, COL_NAME_EXPENSE__BPARTNER_LOCATION_ID, COL_NAME_EXPENSE__TAG_ID};
    public static final String[] COL_LIST_CURRENCYRATE_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_CURRENCYRATE__FROMCURRENCY_ID, COL_NAME_CURRENCYRATE__TOCURRENCY_ID, COL_NAME_CURRENCYRATE__RATE, COL_NAME_CURRENCYRATE__INVERSERATE};
    public static final String[] COL_LIST_GPSTRACK_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_GPSTRACK__CAR_ID, COL_NAME_GPSTRACK__DRIVER_ID, COL_NAME_GPSTRACK__MILEAGE_ID, COL_NAME_GPSTRACK__DATE, COL_NAME_GPSTRACK__MINACCURACY,
            COL_NAME_GPSTRACK__AVGACCURACY, COL_NAME_GPSTRACK__MAXACCURACY, COL_NAME_GPSTRACK__MINALTITUDE, COL_NAME_GPSTRACK__MAXALTITUDE,
            COL_NAME_GPSTRACK__TOTALTIME, COL_NAME_GPSTRACK__MOVINGTIME, COL_NAME_GPSTRACK__DISTANCE, COL_NAME_GPSTRACK__MAXSPEED, COL_NAME_GPSTRACK__AVGSPEED,
            COL_NAME_GPSTRACK__AVGMOVINGSPEED, COL_NAME_GPSTRACK__TOTALTRACKPOINTS, COL_NAME_GPSTRACK__INVALIDTRACKPOINTS, COL_NAME_GPSTRACK__TAG_ID,
            COL_NAME_GPSTRACK__TOTALPAUSETIME, COL_NAME_GPSTRACK__EXPENSETYPE_ID};
    public static final String[] COL_LIST_GPSTRACKDETAIL_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID, COL_NAME_GPSTRACKDETAIL__FILE, COL_NAME_GPSTRACKDETAIL__FILEFORMAT};
    public static final String[] COL_LIST_BPARTNER_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT, COL_NAME_BPARTNER__ISGASSTATION};
    // business partner location
    public static final String[] COL_LIST_BPARTNERLOCATION_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_BPARTNERLOCATION__BPARTNER_ID, COL_NAME_BPARTNERLOCATION__ADDRESS, COL_NAME_BPARTNERLOCATION__POSTAL, COL_NAME_BPARTNERLOCATION__CITY,
            COL_NAME_BPARTNERLOCATION__REGION, COL_NAME_BPARTNERLOCATION__COUNTRY, COL_NAME_BPARTNERLOCATION__PHONE, COL_NAME_BPARTNERLOCATION__PHONE2,
            COL_NAME_BPARTNERLOCATION__FAX, COL_NAME_BPARTNERLOCATION__EMAIL, COL_NAME_BPARTNERLOCATION__CONTACTPERSON};
    public static final String[] COL_LIST_TAG_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT};
    // tasks/reminders tables
    public static final String[] COL_LIST_TASKTYPE_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT};
    public static final String[] COL_LIST_TASK_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_TASK__TASKTYPE_ID, COL_NAME_TASK__SCHEDULEDFOR, COL_NAME_TASK__ISRECURRENT, COL_NAME_TASK__ISDIFFERENTSTARTINGTIME,
            COL_NAME_TASK__TIMEFREQUENCY, COL_NAME_TASK__TIMEFREQUENCYTYPE, COL_NAME_TASK__STARTINGTIME, COL_NAME_TASK__TIMEREMINDERSTART,
            COL_NAME_TASK__RUNMILEAGE, COL_NAME_TASK__MILEAGEREMINDERSTART, COL_NAME_TASK__TODOCOUNT};
    public static final String[] COL_LIST_TASK_CAR_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_TASK_CAR__TASK_ID, COL_NAME_TASK_CAR__CAR_ID, COL_NAME_TASK_CAR__FIRSTRUN_DATE, COL_NAME_TASK_CAR__FIRSTRUN_MILEAGE};
    public static final String[] COL_LIST_REIMBURSEMENT_CAR_RATES_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
            COL_NAME_GEN_USER_COMMENT, COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID,
            COL_NAME_REIMBURSEMENT_CAR_RATES__RATE, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO};
    public static final String[] COL_LIST_GEN_ROWID_NAME = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME};
    public static final String[] COL_LIST_GEN_ROWID = {COL_NAME_GEN_ROWID};
    public static final String[] COL_LIST_SECUREBK_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
            COL_NAME_GEN_USER_COMMENT, COL_NAME_SECUREBK__ISINCLUDEGPS, COL_NAME_SECUREBK__ISINCLUDEREPORTS,
            COL_NAME_SECUREBK__ISSHOWNOTIF};
    public static final String[] COL_LIST_DATATEMPLATE_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
            COL_NAME_GEN_USER_COMMENT, COL_NAME_DATATEMPLATE__CLASS};
    public static final String[] COL_LIST_DATATEMPLATEVALUES_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
            COL_NAME_GEN_USER_COMMENT, COL_NAME_DATATEMPLATEVALUES__TEMPLATE_ID, COL_NAME_DATATEMPLATEVALUES__VALUE};
    public static final String[] COL_LIST_BTDEVICECAR_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE,
            COL_NAME_GEN_USER_COMMENT, COL_NAME_BTDEVICECAR__MACADDR, COL_NAME_BTDEVICECAR__CAR_ID};
    public static final String[] COL_LIST_TODO_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_TODO__TASK_ID, COL_NAME_TODO__CAR_ID, COL_NAME_TODO__DUEDATE, COL_NAME_TODO__DUEMILEAGE, COL_NAME_TODO__NOTIFICATIONDATE,
            COL_NAME_TODO__NOTIFICATIONMILEAGE, COL_NAME_TODO__ISDONE, COL_NAME_TODO__DONEDATE, COL_NAME_TODO__ISSTOPNOTIFICATION};

    public static final String[] COL_LIST_DISPLAYED_MESSAGES_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT};
    public static final String[] COL_LIST_MESSAGES_TABLE = {COL_NAME_GEN_ROWID, COL_NAME_GEN_NAME, COL_NAME_GEN_ISACTIVE, COL_NAME_GEN_USER_COMMENT,
            COL_NAME_MESSAGES__MESSAGE_ID, COL_NAME_MESSAGES__DATE, COL_NAME_MESSAGES__IS_STARRED, COL_NAME_MESSAGES__IS_READ};

    public static final String WHERE_CONDITION_ISACTIVE = " AND " + COL_NAME_GEN_ISACTIVE + "='Y' ";
    /**
     * Database creation sql statements
     */
    //@formatter:off
    private static final String CREATE_SQL_DRIVER_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_DRIVER +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_DRIVER__LICENSE_NO + " TEXT NULL " +
                    ");";

    private static final String CREATE_SQL_CAR_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_CAR +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_CAR__MODEL + " TEXT NULL, " +
                        COL_NAME_CAR__REGISTRATIONNO + " TEXT NULL, " +
                        COL_NAME_CAR__INDEXSTART + " NUMERIC, " +
                        COL_NAME_CAR__INDEXCURRENT + " NUMERIC, " +
                        COL_NAME_CAR__LENGTH_UOM_ID + " INTEGER, " +
                        COL_NAME_CAR__FUEL_UOM_ID + " INTEGER, " +
                        COL_NAME_CAR__CURRENCY_ID + " INTEGER, " +
                        COL_NAME_CAR__ISAFV + " TEXT DEFAULT 'N', " +
                        COL_NAME_CAR__ALTERNATIVE_FUEL_UOM_ID + " INTEGER NULL " +
                    ");";

    private static final String CREATE_SQL_UOM_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_UOM +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_UOM__CODE + " TEXT NOT NULL, " +
                        COL_NAME_UOM__UOMTYPE + " TEXT NOT NULL " +
                    ");";

    private static final String CREATE_SQL_UOMCONVERSION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_UOMCONVERSION +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_UOMCONVERSION__UOMFROM_ID + " INTEGER NOT NULL, " +
                        COL_NAME_UOMCONVERSION__UOMTO_ID + " INTEGER NOT NULL, " +
                        COL_NAME_UOMCONVERSION__RATE + " NUMERIC NOT NULL " +
                    ");";

    private static final String CREATE_SQL_EXPENSETYPE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_EXPENSETYPE +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL " +
                    ");";

    private static final String CREATE_SQL_MILEAGE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_MILEAGE +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_MILEAGE__DATE + " DATE NOT NULL, " +
                        COL_NAME_MILEAGE__CAR_ID + " INTEGER NOT NULL, " +
                        COL_NAME_MILEAGE__DRIVER_ID + " INTEGER NOT NULL, " +
                        COL_NAME_MILEAGE__INDEXSTART + " NUMERIC NOT NULL, " +
                        COL_NAME_MILEAGE__INDEXSTOP + " NUMERIC NULL, " +
                        COL_NAME_MILEAGE__UOMLENGTH_ID + " INTEGER NOT NULL, " +
                        COL_NAME_MILEAGE__EXPENSETYPE_ID + " INTEGER NOT NULL, " +
                        COL_NAME_MILEAGE__GPSTRACKLOG + " TEXT NULL, " +
                        COL_NAME_MILEAGE__TAG_ID + " INTEGER NULL, " +
                        COL_NAME_MILEAGE__DATE_TO + " DATE NULL " +
                    ");";

    private static final String CREATE_SQL_CURRENCY_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_CURRENCY +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_CURRENCY__CODE + " TEXT NOT NULL " +
                    ");";

    private static final String CREATE_SQL_REFUEL_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_REFUEL +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_REFUEL__CAR_ID + " INTEGER, " +
                        COL_NAME_REFUEL__DRIVER_ID + " INTEGER, " +
                        COL_NAME_REFUEL__EXPENSETYPE_ID + " INTEGER, " +
                        COL_NAME_REFUEL__INDEX + " NUMERIC, " +
                        COL_NAME_REFUEL__QUANTITY + " NUMERIC, " +
                        COL_NAME_REFUEL__UOMVOLUME_ID + " INTEGER, " +
                        COL_NAME_REFUEL__PRICE + " NUMERIC, " +
                        COL_NAME_REFUEL__CURRENCY_ID + " INTEGER, " +
                        COL_NAME_REFUEL__DATE + " DATE NULL, " +
                        COL_NAME_REFUEL__DOCUMENTNO + " TEXT NULL, " +
                        COL_NAME_REFUEL__EXPENSECATEGORY_ID + " INTEGER, " +
                        COL_NAME_REFUEL__ISFULLREFUEL + " TEXT DEFAULT 'N', " +
                        COL_NAME_REFUEL__QUANTITYENTERED + " NUMERIC NULL, " +
                        COL_NAME_REFUEL__UOMVOLUMEENTERED_ID + " INTEGER NULL, " +
                        COL_NAME_REFUEL__PRICEENTERED + " NUMERIC NULL, " +
                        COL_NAME_REFUEL__CURRENCYENTERED_ID + " INTEGER NULL, " +
                        COL_NAME_REFUEL__CURRENCYRATE + " NUMERIC NULL, " +
                        COL_NAME_REFUEL__UOMVOLCONVERSIONRATE + " NUMERIC NULL, " +
                        COL_NAME_REFUEL__AMOUNT + " NUMERIC NULL, " +
                        COL_NAME_REFUEL__AMOUNTENTERED + " NUMERIC NULL, " +
                        COL_NAME_REFUEL__BPARTNER_ID + " INTEGER NULL, " +
                        COL_NAME_REFUEL__BPARTNER_LOCATION_ID + " INTEGER NULL, " +
                        COL_NAME_REFUEL__TAG_ID + " INTEGER NULL, " +
                        COL_NAME_REFUEL__ISALTERNATIVEFUEL + " TEXT DEFAULT 'N' " +
                    ");";

    private static final String CREATE_SQL_EXPENSECATEGORY_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_EXPENSECATEGORY +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST + " TEXT DEFAULT 'N', " +
                        COL_NAME_EXPENSECATEGORY__ISFUEL + " TEXT DEFAULT 'N', " +
                        COL_NAME_EXPENSECATEGORY__UOMTYPE + " TEXT NOT NULL DEFAULT 'N' " +
                    ");";

    private static final String CREATE_SQL_EXPENSE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_EXPENSE +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_EXPENSE__CAR_ID + " INTEGER, " +
                        COL_NAME_EXPENSE__DRIVER_ID + " INTEGER, " +
                        COL_NAME_EXPENSE__EXPENSECATEGORY_ID + " INTEGER, " +
                        COL_NAME_EXPENSE__EXPENSETYPE_ID + " INTEGER, " +
                        COL_NAME_EXPENSE__AMOUNT + " NUMERIC, " +
                        COL_NAME_EXPENSE__CURRENCY_ID + " INTEGER, " +
                        COL_NAME_EXPENSE__DATE + " DATE NULL, " +
                        COL_NAME_EXPENSE__DOCUMENTNO + " TEXT NULL, " +
                        COL_NAME_EXPENSE__INDEX + " NUMERIC, " +
                        COL_NAME_EXPENSE__FROMTABLE + " TEXT NULL, " +
                        COL_NAME_EXPENSE__FROMRECORD_ID + " INTEGER, " +
                        COL_NAME_EXPENSE__AMOUNTENTERED + " NUMERIC NULL, " +
                        COL_NAME_EXPENSE__CURRENCYENTERED_ID + " INTEGER NULL, " +
                        COL_NAME_EXPENSE__CURRENCYRATE + " NUMERIC NULL, " +
                        COL_NAME_EXPENSE__QUANTITY + " NUMERIC NULL, " +
                        COL_NAME_EXPENSE__PRICE + " NUMERIC NULL, " +
                        COL_NAME_EXPENSE__PRICEENTERED + " NUMERIC NULL, " +
                        COL_NAME_EXPENSE__UOM_ID + " INTEGER NULL, " +
                        COL_NAME_EXPENSE__BPARTNER_ID + " INTEGER NULL, " +
                        COL_NAME_EXPENSE__BPARTNER_LOCATION_ID + " INTEGER NULL, " +
                        COL_NAME_EXPENSE__TAG_ID + " INTEGER NULL " +
                    ");";

    private static final String CREATE_SQL_CURRENCYRATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_CURRENCYRATE +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_CURRENCYRATE__FROMCURRENCY_ID + " INTEGER, " +
                        COL_NAME_CURRENCYRATE__TOCURRENCY_ID + " INTEGER, " +
                        COL_NAME_CURRENCYRATE__RATE + " NUMERIC, " +
                        COL_NAME_CURRENCYRATE__INVERSERATE + " NUMERIC " +
                    ");";

    private static final String CREATE_SQL_GPSTRACK_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_GPSTRACK +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_GPSTRACK__CAR_ID + " INTEGER NULL, " +
                        COL_NAME_GPSTRACK__DRIVER_ID + " INTEGER NULL, " +
                        COL_NAME_GPSTRACK__MILEAGE_ID + " INTEGER NULL, " +
                        COL_NAME_GPSTRACK__DATE + " DATE NULL, " +
                        COL_NAME_GPSTRACK__MINACCURACY + " NUMERIC NULL, " +
                        COL_NAME_GPSTRACK__AVGACCURACY + " NUMERIC NULL, " +
                        COL_NAME_GPSTRACK__MAXACCURACY + " NUMERIC NULL, " +
                        COL_NAME_GPSTRACK__MINALTITUDE + " NUMERIC NULL, " +
                        COL_NAME_GPSTRACK__MAXALTITUDE + " NUMERIC NULL, " +
                        COL_NAME_GPSTRACK__TOTALTIME + " NUMERIC NULL, " +
                        COL_NAME_GPSTRACK__MOVINGTIME + " NUMERIC NULL, " +
                        COL_NAME_GPSTRACK__DISTANCE + " NUMERIC NULL, " +
                        COL_NAME_GPSTRACK__MAXSPEED + " NUMERIC NULL, " +
                        COL_NAME_GPSTRACK__AVGSPEED + " NUMERIC NULL, " +
                        COL_NAME_GPSTRACK__AVGMOVINGSPEED + " NUMERIC NULL, " +
                        COL_NAME_GPSTRACK__TOTALTRACKPOINTS + " INTEGER NULL, " +
                        COL_NAME_GPSTRACK__INVALIDTRACKPOINTS + " INTEGER NULL, " +
                        COL_NAME_GPSTRACK__TAG_ID + " INTEGER NULL, " +
                        COL_NAME_GPSTRACK__TOTALPAUSETIME + " INTEGER NULL, " +
                        COL_NAME_GPSTRACK__EXPENSETYPE_ID + " INTEGER NULL " +
                    ");";

    private static final String CREATE_SQL_GPSTRACKDETAIL_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_GPSTRACKDETAIL +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID + " INTEGER NOT NULL, " +
                        COL_NAME_GPSTRACKDETAIL__FILE + " TEXT NULL, " +
                        COL_NAME_GPSTRACKDETAIL__FILEFORMAT + " TEXT NULL " +
                    ");";

    private static final String CREATE_SQL_BPARTNER_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_BPARTNER +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_BPARTNER__ISGASSTATION + " TEXT DEFAULT 'N' " +
                    ");";

    private static final String CREATE_SQL_BPARTNERLOCATION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_BPARTNERLOCATION +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_BPARTNERLOCATION__BPARTNER_ID + " INTEGER NOT NULL, " +
                        COL_NAME_BPARTNERLOCATION__ADDRESS + " TEXT NULL, " +
                        COL_NAME_BPARTNERLOCATION__POSTAL + " TEXT NULL, " +
                        COL_NAME_BPARTNERLOCATION__CITY + " TEXT NULL, " +
                        COL_NAME_BPARTNERLOCATION__REGION + " TEXT NULL, " +
                        COL_NAME_BPARTNERLOCATION__COUNTRY + " TEXT NULL, " +
                        COL_NAME_BPARTNERLOCATION__PHONE + " TEXT NULL, " +
                        COL_NAME_BPARTNERLOCATION__PHONE2 + " TEXT NULL, " +
                        COL_NAME_BPARTNERLOCATION__FAX + " TEXT NULL, " +
                        COL_NAME_BPARTNERLOCATION__EMAIL + " TEXT NULL, " +
                        COL_NAME_BPARTNERLOCATION__CONTACTPERSON + " TEXT NULL " +
                    ");";

    private static final String CREATE_SQL_TAG_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TAG +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL " +
                    ");";

    private static final String CREATE_SQL_TASKTYPE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TASKTYPE +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL " +
                    ");";

    private static final String CREATE_SQL_TASK_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TASK +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_TASK__TASKTYPE_ID + " INTEGER NOT NULL, " +
                        COL_NAME_TASK__SCHEDULEDFOR + " TEXT NULL, " +
                        COL_NAME_TASK__ISRECURRENT + " TEXT NOT NULL, " +
                        COL_NAME_TASK__ISDIFFERENTSTARTINGTIME + " TEXT NULL, " +
                        COL_NAME_TASK__TIMEFREQUENCY + " INTEGER NULL, " +
                        COL_NAME_TASK__TIMEFREQUENCYTYPE + " INTEGER NULL, " +
                        COL_NAME_TASK__STARTINGTIME + " DATE NULL, " +
                        COL_NAME_TASK__TIMEREMINDERSTART + " INTEGER NULL, " +
                        COL_NAME_TASK__RUNMILEAGE + " INTEGER NULL, " +
                        COL_NAME_TASK__MILEAGEREMINDERSTART + " INTEGER NULL, " +
                        COL_NAME_TASK__TODOCOUNT + " INTEGER NOT NULL, " +
                        " FOREIGN KEY(" + COL_NAME_TASK__TASKTYPE_ID + ") REFERENCES " + TABLE_NAME_TASKTYPE + "(" + COL_NAME_GEN_ROWID + ")" +
                    ");";

    private static final String CREATE_SQL_TASK_CAR_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TASK_CAR +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_TASK_CAR__TASK_ID + " INTEGER NOT NULL, " +
                        COL_NAME_TASK_CAR__CAR_ID + " INTEGER NOT NULL, " +
                        COL_NAME_TASK_CAR__FIRSTRUN_DATE + " DATE NULL, " +
                        COL_NAME_TASK_CAR__FIRSTRUN_MILEAGE + " INTEGER NULL " +
                    ");";

    private static final String CREATE_SQL_TODO_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TODO +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_TODO__TASK_ID + " INTEGER NOT NULL, " +
                        COL_NAME_TODO__CAR_ID + " INTEGER NULL, " +
                        COL_NAME_TODO__DUEDATE + " DATE NULL, " +
                        COL_NAME_TODO__DUEMILEAGE + " INTEGER NULL, " +
                        COL_NAME_TODO__NOTIFICATIONDATE + " DATE NULL, " +
                        COL_NAME_TODO__NOTIFICATIONMILEAGE + " INTEGER NULL, " +
                        COL_NAME_TODO__ISDONE + " TEXT DEFAULT 'N', " +
                        COL_NAME_TODO__DONEDATE + " DATE NULL, " +
                        COL_NAME_TODO__ISSTOPNOTIFICATION + " TEXT DEFAULT 'N' " +
                    ");";

    private static final String CREATE_SQL_REIMBURSEMENT_CAR_RATES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_REIMBURSEMENT_CAR_RATES +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID + " INTEGER NOT NULL, " +
                        COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID + " INTEGER NOT NULL, " +
                        COL_NAME_REIMBURSEMENT_CAR_RATES__RATE + " NUMBER NOT NULL DEFAULT 0, " +
                        COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM + " DATE NOT NULL, " +
                        COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO + " DATE NOT NULL " +
                    ");";

    private static final String CREATE_SQL_SECUREBK_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_SECURE_BK_SETTINGS +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_SECUREBK__ISINCLUDEGPS + " TEXT NOT NULL DEFAULT 'N', " +
                        COL_NAME_SECUREBK__ISINCLUDEREPORTS + " TEXT NOT NULL DEFAULT 'N', " +
                        COL_NAME_SECUREBK__ISSHOWNOTIF + " TEXT NOT NULL DEFAULT 'Y'" +
                    ");";

    private static final String CREATE_SQL_DATATEMPLATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_DATA_TEMPLATE +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_DATATEMPLATE__CLASS + " TEXT NOT NULL" +
                    ");";

    private static final String CREATE_SQL_DATATEMPLATEVALUES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_DATA_TEMPLATE_VALUES +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_DATATEMPLATEVALUES__TEMPLATE_ID + " INTEGER NOT NULL, " +
                        COL_NAME_DATATEMPLATEVALUES__VALUE + " TEXT NULL" +
                    ");";

    private static final String CREATE_SQL_BTDEVICECAR_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_BTDEVICE_CAR +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_BTDEVICECAR__MACADDR + " TEXT NOT NULL, " +
                        COL_NAME_BTDEVICECAR__CAR_ID + " INTEGER NOT NULL " +
                    ");";

    private static final String CREATE_SQL_DISPLAYED_MESSAGES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_DISPLAYED_MESSAGES +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL" +
                    ");";

    private static final String CREATE_SQL_MESSAGES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_MESSAGES +
                    " ( " +
                        COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME_GEN_NAME + " TEXT NOT NULL, " +
                        COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
                        COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
                        COL_NAME_MESSAGES__MESSAGE_ID + " TEXT NULL," +
                        COL_NAME_MESSAGES__DATE + " DATE NOT NULL, " +
                        COL_NAME_MESSAGES__IS_STARRED + " TEXT DEFAULT 'N', " +
                        COL_NAME_MESSAGES__IS_READ + " TEXT DEFAULT 'N' " +
                    ");";
    //@formatter:on

    private final Context mCtx;
    public String mErrorMessage = null;
    public Exception mException;
    SQLiteDatabase mDb = null;
    private DatabaseHelper mDbHelper = null;


    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    DB(Context ctx) {
        this.mCtx = ctx;
        if (mDb == null) {
            open();
        }
    }

    public static String sqlConcatTableColumn(String tableName, String columnName) {
        return tableName + "." + columnName;
    }

    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @throws SQLException if the database could be neither opened or created
     */
    private void open() throws SQLException {
        if (mDbHelper == null) {
            mDbHelper = new DatabaseHelper(mCtx);
            if (mDb == null || !mDb.isOpen()) {
                mDb = mDbHelper.getWritableDatabase();
            }
        }
    }

    public void close() {
        try {
            mDbHelper.close();
            mDbHelper = null;
            mDb.close();
            mDb = null;
        }
        catch (SQLiteException ignored) {
        }
    }

    private void createIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_GPSTRACK + "_IX1 " + "ON " + TABLE_NAME_GPSTRACK + " (" + COL_NAME_GPSTRACK__CAR_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_GPSTRACK + "_IX2 " + "ON " + TABLE_NAME_GPSTRACK + " (" + COL_NAME_GPSTRACK__DRIVER_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_GPSTRACK + "_IX3 " + "ON " + TABLE_NAME_GPSTRACK + " (" + COL_NAME_GPSTRACK__MILEAGE_ID
                + " DESC )");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_GPSTRACK + "_IX4 " + "ON " + TABLE_NAME_GPSTRACK + " (" + COL_NAME_GPSTRACK__DATE + " DESC )");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_GPSTRACK + "_IX5 " + "ON " + TABLE_NAME_GPSTRACK + " (" + COL_NAME_GPSTRACK__EXPENSETYPE_ID + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_GPSTRACKDETAIL + "_IX1 " + "ON " + TABLE_NAME_GPSTRACKDETAIL + " ("
                + COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID + ")");
        // create indexes on mileage table
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_MILEAGE + "_IX1 " + "ON " + TABLE_NAME_MILEAGE + " (" + COL_NAME_MILEAGE__CAR_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_MILEAGE + "_IX2 " + "ON " + TABLE_NAME_MILEAGE + " (" + COL_NAME_MILEAGE__DRIVER_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_MILEAGE + "_IX3 " + "ON " + TABLE_NAME_MILEAGE + " (" + COL_NAME_MILEAGE__DATE + " DESC )");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_MILEAGE + "_IX4 " + "ON " + TABLE_NAME_MILEAGE + " (" + COL_NAME_MILEAGE__INDEXSTOP + " DESC )");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_MILEAGE + "_IX5 " + "ON " + TABLE_NAME_MILEAGE + " (" + COL_NAME_GEN_USER_COMMENT + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_REFUEL + "_IX1 " + "ON " + TABLE_NAME_REFUEL + " (" + COL_NAME_REFUEL__DATE + " DESC )");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_REFUEL + "_IX2 " + "ON " + TABLE_NAME_REFUEL + " (" + COL_NAME_GEN_USER_COMMENT + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_REFUEL + "_IX3 " + "ON " + TABLE_NAME_REFUEL + " (" + COL_NAME_REFUEL__ISFULLREFUEL + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_REFUEL + "_IX4 " + "ON " + TABLE_NAME_REFUEL + " (" + COL_NAME_REFUEL__INDEX + " DESC )");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_REFUEL + "_IX5 " + "ON " + TABLE_NAME_REFUEL + " (" + COL_NAME_GEN_ISACTIVE + ")");

        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS " + TABLE_NAME_TASK_CAR + "_UK1 " + "ON " + TABLE_NAME_TASK_CAR + " (" + COL_NAME_TASK_CAR__CAR_ID + ", "
                + COL_NAME_TASK_CAR__TASK_ID + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_TODO + "_IX1 " + "ON " + TABLE_NAME_TODO + " (" + COL_NAME_TODO__TASK_ID + ")");
    }

    public SQLiteDatabase getDatabase() {
        return mDb;
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        Resources mResource = null;
        Context mCtx;

        DatabaseHelper(Context context) {
            super(context, ConstantValues.DATABASE_NAME, null, ConstantValues.DATABASE_VERSION);
            mResource = context.getResources();
            mCtx = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createUOMTable(db);
            createUOMConversionTable(db);
            createExpenseTypeTable(db);
            createCurrencyTable(db);
            createCurrencyRateTable(db);
            createExpenseCategory(db);

            createDriverTable(db);
            createCarTable(db);
            createBPartnerTable(db);
            createTagTable(db);
            createSecureBKSettingsTable(db);
            createDataTemplateTables(db);
            createBTDeviceCarTable(db);
            createReimbursementCarRatesTable(db);
            createTaskToDoTables(db);

            createMileageTable(db);
            createRefuelTable(db);
            createExpenses(db, false);
            createGPSTrackTables(db);

            createMessageTables(db);

            createIndexes(db);
        }

        private void createDriverTable(SQLiteDatabase db) {
            db.execSQL(CREATE_SQL_DRIVER_TABLE);
            String sql = "INSERT INTO " + TABLE_NAME_DRIVER + "( " + COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", " + COL_NAME_GEN_USER_COMMENT + ") "
                    + " VALUES( 'I', 'Y', 'Customize me')";
            db.execSQL(sql);
        }

        private void createCarTable(SQLiteDatabase db) {
            db.execSQL(CREATE_SQL_CAR_TABLE);
            //insert a default car
            try {
                String sql =
                        "INSERT INTO " + TABLE_NAME_CAR + " " +
                                "( " +
                                COL_NAME_GEN_NAME + ", " +
                                COL_NAME_CAR__LENGTH_UOM_ID + ", " +
                                COL_NAME_CAR__FUEL_UOM_ID + ", " +
                                COL_NAME_CAR__CURRENCY_ID + " " +
                                ") " +
                                "VALUES ('" + mResource.getString(R.string.DB_InitCar_Name) + "', ?, ?, ?)";
                SharedPreferences.Editor e = AndiCar.getDefaultSharedPreferences().edit();
                String appCreatedKey = mResource.getString(R.string.pref_key_car_created);
                switch (Utils.getDeviceCountryCode(mCtx).toUpperCase()) {
                    case "US":
                        db.execSQL(sql, new Integer[]{2, 4, 2}); //US
                        e.putBoolean(appCreatedKey, true);
                        break;
                    case "CA":
                        db.execSQL(sql, new Integer[]{2, 4, 5}); //CA
                        e.putBoolean(appCreatedKey, true);
                        break;
                    case "HU":
                        db.execSQL(sql, new Integer[]{1, 3, 3}); //HU
                        e.putBoolean(appCreatedKey, true);
                        break;
                    case "RO":
                        db.execSQL(sql, new Integer[]{1, 3, 4}); //RO
                        e.putBoolean(appCreatedKey, true);
                        break;
                    case "GB":
                        db.execSQL(sql, new Integer[]{2, 5, 6}); //GB
                        e.putBoolean(appCreatedKey, true);
                        break;
                    case "AU":
                        db.execSQL(sql, new Integer[]{1, 3, 8}); //Australia
                        e.putBoolean(appCreatedKey, true);
                        break;
                    case "ZA":
                        db.execSQL(sql, new Integer[]{1, 3, 7}); //South Africa
                        e.putBoolean(appCreatedKey, true);
                        break;
                    case "MX":
                        db.execSQL(sql, new Integer[]{1, 3, 9}); //Mexico
                        e.putBoolean(appCreatedKey, true);
                        break;
                    default:
                        if (Currency.getInstance(Utils.getDeviceDefaultLocale(mCtx)).getCurrencyCode().toUpperCase().equals("EUR")) {
                            db.execSQL(sql, new Integer[]{1, 3, 1}); //EU
                            e.putBoolean(appCreatedKey, true);
                        }
                }
                e.apply();
            } catch (Exception ignored) {
            }

            /*
            COL_NAME_GEN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME_GEN_ISACTIVE + " TEXT DEFAULT 'Y', " +
            COL_NAME_GEN_USER_COMMENT + " TEXT NULL, " +
            COL_NAME_CAR__MODEL + " TEXT NULL, " +
            COL_NAME_CAR__REGISTRATIONNO + " TEXT NULL, " +
            COL_NAME_CAR__INDEXSTART + " NUMERIC, " +
            COL_NAME_CAR__INDEXCURRENT + " NUMERIC, " +
            COL_NAME_CAR__ISAFV + " TEXT DEFAULT 'N', " +
            COL_NAME_CAR__ALTERNATIVE_FUEL_UOM_ID + " INTEGER NULL " +

             */
        }

        private void createUOMTable(SQLiteDatabase db) throws SQLException {
            // create uom table
            db.execSQL(CREATE_SQL_UOM_TABLE);
            // init uom
            String colPart = "INSERT INTO " + TABLE_NAME_UOM + " ( " + COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", " + COL_NAME_GEN_USER_COMMENT +
                    ", " + COL_NAME_UOM__CODE + ", " + COL_NAME_UOM__UOMTYPE + ") ";
            db.execSQL(colPart + "VALUES ( '" + mResource.getString(R.string.DB_UOM_KmName) + "', 'Y', " +
                    "'" + mResource.getString(R.string.DB_UOM_KmComment) + "', 'km', '" + ConstantValues.UOM_TYPE_LENGTH_CODE + "' )"); // _id = 1
            db.execSQL(colPart + "VALUES ( '" + mResource.getString(R.string.DB_UOM_MiName) + "', 'Y', " +
                    "'" + mResource.getString(R.string.DB_UOM_MiComment) + "', 'mi', '" + ConstantValues.UOM_TYPE_LENGTH_CODE + "' )"); // _id = 2 1609,344 m
            db.execSQL(colPart + "VALUES ( '" + mResource.getString(R.string.DB_UOM_LName) + "', 'Y', " +
                    "'" + mResource.getString(R.string.DB_UOM_LComment) + "', 'l', '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "' )"); // _id = 3
            db.execSQL(colPart + "VALUES ( '" + mResource.getString(R.string.DB_UOM_USGName) + "', 'Y', " +
                    "'" + mResource.getString(R.string.DB_UOM_USGComment) + "', 'gal US', '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "' )"); // _id = 4 3,785 411 784 l
            db.execSQL(colPart + "VALUES ( '" + mResource.getString(R.string.DB_UOM_GBGName) + "', 'Y', " +
                    "'" + mResource.getString(R.string.DB_UOM_GBGComment) + "', 'gal GB', '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "' )"); // _id = 5 4,546 09 l
            db.execSQL(colPart + "VALUES ( '" + mResource.getString(R.string.DB_UOM_KgName) + "', 'Y', " +
                    "'" + mResource.getString(R.string.DB_UOM_KgComment) + "', 'kg', '" + ConstantValues.UOM_TYPE_WEIGHT_CODE + "' )"); // _id = 6
            db.execSQL(colPart + "VALUES ( '" + mResource.getString(R.string.DB_UOM_GGEName) + "', 'Y', " +
                    "'" + mResource.getString(R.string.DB_UOM_GGEComment) + "', 'gge', '" + ConstantValues.UOM_TYPE_WEIGHT_CODE + "' )"); // _id = 7
            db.execSQL(colPart + "VALUES ( '" + mResource.getString(R.string.DB_UOM_LBName) + "', 'Y', " +
                    "'" + mResource.getString(R.string.DB_UOM_LBComment) + "', 'lb', '" + ConstantValues.UOM_TYPE_WEIGHT_CODE + "' )"); // _id = 8
            db.execSQL(colPart + "VALUES ( '" + mResource.getString(R.string.DB_UOM_KWhName) + "', 'Y', " +
                    "'" + mResource.getString(R.string.DB_UOM_KWhComment) + "', 'kW⋅h', '" + ConstantValues.UOM_TYPE_ENERGY_CODE + "' )"); // _id = 9
        }

        private void createUOMConversionTable(SQLiteDatabase db) throws SQLException {
            db.execSQL(CREATE_SQL_UOMCONVERSION_TABLE);
            // init default uom conversions
            String colPart = "INSERT INTO " + TABLE_NAME_UOMCONVERSION + " ( " + COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", "
                    + COL_NAME_GEN_USER_COMMENT + ", " + COL_NAME_UOMCONVERSION__UOMFROM_ID + ", " + COL_NAME_UOMCONVERSION__UOMTO_ID + ", "
                    + COL_NAME_UOMCONVERSION__RATE + " " + ") ";
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_MiToKmName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_MiToKmComment) + "', " + "2, " + "1, " + "1.609344 )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_KmToMiName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_KmToMiComment) + "', " + "1, " + "2, " + "0.621371 )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_USGToLName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_USGToLComment) + "', " + "4, " + "3, " + "3.785412 )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_LToUSGName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_LToUSGComment) + "', " + "3, " + "4, " + "0.264172 )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_GBGToLName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_GBGToLComment) + "', " + "5, " + "3, " + "4.54609 )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_LToGBGName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_LToGBGComment) + "', " + "3, " + "5, " + "0.219969 )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_GBGToUSGName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_GBGToUSGComment) + "', " + "5, " + "4, " + "1.200950 )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_USGToGBGName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_USGToGBGComment) + "', " + "4, " + "5, " + "0.832674 )");

            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_KgToLbName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_KgToLbComment) + "', " + "6, " + "8, " + "2.204624 )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_LbToKgName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_LbToKgComment) + "', " + "8, " + "6, " + "0.453592 )");

            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_KgToGGEName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_KgToGGEComment) + "', " + "6, " + "7, " + "0.389559 )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_GGEToKgName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_GGEToKgComment) + "', " + "7, " + "6, " + "2.567 )");

            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_LbToGGEName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_LbToGGEComment) + "', " + "8, " + "7, " + "0.176678 )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_UOMConversion_GGEToLbName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_UOMConversion_GGEToLbComment) + "', " + "7, " + "8, " + "5.660 )");
        }

        private void createExpenseTypeTable(SQLiteDatabase db) throws SQLException {
            // create expense types table
            db.execSQL(CREATE_SQL_EXPENSETYPE_TABLE);
            // init some standard expenses
            String colPart = "INSERT INTO " + TABLE_NAME_EXPENSETYPE + " ( " + COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", "
                    + COL_NAME_GEN_USER_COMMENT + " " + ") ";
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_ExpType_PersonalName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_ExpType_PersonalComment) + "' " + ")");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_ExpType_EmployerName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_ExpType_EmployerComment) + "' " + ")");
        }

        private void createMileageTable(SQLiteDatabase db) throws SQLException {
            // create mileage table
            db.execSQL(CREATE_SQL_MILEAGE_TABLE);
        }

        private void createCurrencyTable(SQLiteDatabase db) throws SQLException {
            // currency table name
            db.execSQL(CREATE_SQL_CURRENCY_TABLE);
            // insert some currencies
            String colPart = "INSERT INTO " + TABLE_NAME_CURRENCY + " ( " + COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", " + COL_NAME_GEN_USER_COMMENT
                    + ", " + COL_NAME_CURRENCY__CODE + ") ";
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_Curr_EUR) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_Curr_EUR) + "', " + "'EUR' )"); // #1
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_Curr_USD) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_Curr_USD) + "', " + "'USD' )"); // #2
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_Curr_HUF) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_Curr_HUF) + "', " + "'HUF' )"); // #3
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_Curr_RON) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_Curr_RON) + "', " + "'RON' )"); // #4
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_Curr_CAD) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_Curr_CAD) + "', " + "'CAD' )"); // #5
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_Curr_GBP) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_Curr_GBP) + "', " + "'GBP' )"); // #6
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_Curr_ZAR) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_Curr_ZAR) + "', " + "'ZAR' )"); // #7
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_Curr_AUD) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_Curr_AUD) + "', " + "'AUD' )"); // #8
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_Curr_MXN) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_Curr_MXN) + "', " + "'MXN' )"); // #9
        }

        private void createRefuelTable(SQLiteDatabase db) throws SQLException {
            db.execSQL(CREATE_SQL_REFUEL_TABLE);
        }

        private void createExpenseCategory(SQLiteDatabase db) throws SQLException {
            // expense category
            db.execSQL(CREATE_SQL_EXPENSECATEGORY_TABLE);
            String colPart = "INSERT INTO " + TABLE_NAME_EXPENSECATEGORY + " ( " + COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", " +
                    COL_NAME_GEN_USER_COMMENT + ", " + COL_NAME_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST + ", " +
                    COL_NAME_EXPENSECATEGORY__ISFUEL + ", " + COL_NAME_EXPENSECATEGORY__UOMTYPE + " "
                    + ") ";

            // fuel types
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_Diesel1D) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_FuelType_Diesel1DComment) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "'" +
                    " )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_Diesel2D) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_FuelType_Diesel2DComment) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "'" +
                    " )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_DieselBio) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_FuelType_DieselBioComment) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "'" +
                    " )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_DieselSynthetic) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_FuelType_DieselSyntheticComment) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "'" +
                    " )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_GasolineRegular) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_FuelType_GasolineRegularComment) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "'" +
                    " )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_GasolineMidgrade) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_FuelType_GasolineMidgradeComment) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "'" +
                    " )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_GasolinePremium) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_FuelType_GasolinePremiumComment) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "'" +
                    " )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_GasolinePremium2) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_FuelType_GasolinePremiumComment2) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "'" +
                    " )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_LPG) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_FuelType_LPGComment) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "'" +
                    " )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_CNG) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_FuelType_CNGComment) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_WEIGHT_CODE + "'" +
                    " )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_Electric) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_FuelType_ElectricComment) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_ENERGY_CODE + "'" +
                    " )");

            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_ExpCat_ServiceName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_ExpCat_ServiceComment) + "', " + "'N', 'N', '" + ConstantValues.UOM_TYPE_NONE_CODE + "'" +
                    " )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_ExpCat_RoadTollName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_ExpCat_RoadTollComment) + "', " + "'N', 'N', '" + ConstantValues.UOM_TYPE_NONE_CODE + "'" +
                    " )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_ExpCat_InsuranceName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_ExpCat_InsuranceComment) + "', " + "'N', 'N', '" + ConstantValues.UOM_TYPE_NONE_CODE + "'" +
                    " )");
            db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_ExpCat_PenaltyName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_ExpCat_PenaltyComment) + "', " + "'Y', 'N', '" + ConstantValues.UOM_TYPE_NONE_CODE + "'" +
                    " )");
        }

        private void createExpenses(SQLiteDatabase db, boolean isUpdate) throws SQLException {
            // expenses table
            db.execSQL(CREATE_SQL_EXPENSE_TABLE);
            if (!isUpdate) {
                return;
            }
            // initialize refuel expenses
            String sql = "INSERT INTO " + TABLE_NAME_EXPENSE + "( " + COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_USER_COMMENT + ", " + COL_NAME_GEN_ISACTIVE
                    + ", " + COL_NAME_EXPENSE__CAR_ID + ", " + COL_NAME_EXPENSE__DRIVER_ID + ", " + COL_NAME_EXPENSE__EXPENSECATEGORY_ID + ", "
                    + COL_NAME_EXPENSE__EXPENSETYPE_ID + ", " + COL_NAME_EXPENSE__AMOUNT + ", " + COL_NAME_EXPENSE__CURRENCY_ID + ", " + COL_NAME_EXPENSE__DATE
                    + ", " + COL_NAME_EXPENSE__DOCUMENTNO + ", " + COL_NAME_EXPENSE__INDEX + ", " + COL_NAME_EXPENSE__FROMTABLE + ", "
                    + COL_NAME_EXPENSE__FROMRECORD_ID + " " + ") " + "SELECT " + COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_USER_COMMENT + ", "
                    + COL_NAME_GEN_ISACTIVE + ", " + COL_NAME_REFUEL__CAR_ID + ", " + COL_NAME_REFUEL__DRIVER_ID + ", " + COL_NAME_REFUEL__EXPENSECATEGORY_ID
                    + ", " + COL_NAME_REFUEL__EXPENSETYPE_ID + ", " + COL_NAME_REFUEL__QUANTITY + " * " + COL_NAME_REFUEL__PRICE + ", "
                    + COL_NAME_REFUEL__CURRENCY_ID + ", " + COL_NAME_REFUEL__DATE + ", " + COL_NAME_REFUEL__DOCUMENTNO + ", " + COL_NAME_REFUEL__INDEX + ", "
                    + "'Refuel' " + ", " + COL_NAME_GEN_ROWID + " " + "FROM " + TABLE_NAME_REFUEL;
            db.execSQL(sql);
        }

        private void createCurrencyRateTable(SQLiteDatabase db) throws SQLException {
            // create currency rate table
            db.execSQL(CREATE_SQL_CURRENCYRATE_TABLE);
        }

        private void createGPSTrackTables(SQLiteDatabase db) throws SQLException {
            db.execSQL(CREATE_SQL_GPSTRACK_TABLE);
            db.execSQL(CREATE_SQL_GPSTRACKDETAIL_TABLE);
        }

        private void createBPartnerTable(SQLiteDatabase db) throws SQLException {
            // business partner
            db.execSQL(CREATE_SQL_BPARTNER_TABLE);
            db.execSQL(CREATE_SQL_BPARTNERLOCATION_TABLE);
        }

        private void createTagTable(SQLiteDatabase db) throws SQLException {
            // business partner
            db.execSQL(CREATE_SQL_TAG_TABLE);
        }

        private void createSecureBKSettingsTable(SQLiteDatabase db) throws SQLException {
            //create addon table
            db.execSQL(CREATE_SQL_SECUREBK_TABLE);
            String initSQL = "INSERT INTO " + TABLE_NAME_SECURE_BK_SETTINGS + " ( " + COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", "
                    + COL_NAME_GEN_USER_COMMENT + ", " + COL_NAME_SECUREBK__ISINCLUDEGPS + ", "
                    + COL_NAME_SECUREBK__ISINCLUDEREPORTS + ", " + COL_NAME_SECUREBK__ISSHOWNOTIF + " ) " + "VALUES ( "
                    + "'Default', " + "'Y', " + "'Default', " + "'N', " + "'N', " + "'Y' " + " )";
            db.execSQL(initSQL);
        }

        private void createTaskToDoTables(SQLiteDatabase db) throws SQLException {
            // create task/reminder
            db.execSQL(CREATE_SQL_TASKTYPE_TABLE);
            db.execSQL(CREATE_SQL_TASK_TABLE);
            db.execSQL(CREATE_SQL_TASK_CAR_TABLE);
            db.execSQL(CREATE_SQL_TODO_TABLE);

            String sql = "INSERT INTO " + TABLE_NAME_TASKTYPE + "( " + COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", " + COL_NAME_GEN_USER_COMMENT
                    + ") " + " VALUES( " + "'" + mResource.getString(R.string.DB_TaskType_ServiceName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_TaskType_ServiceComment) + "')";
            db.execSQL(sql);
            sql = "INSERT INTO " + TABLE_NAME_TASKTYPE + "( " + COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", " + COL_NAME_GEN_USER_COMMENT + ") "
                    + " VALUES( " + "'" + mResource.getString(R.string.DB_TaskType_ReminderName) + "', " + "'Y', " + "'"
                    + mResource.getString(R.string.DB_TaskType_ReminderComment) + "')";
            db.execSQL(sql);
        }

        private void createDataTemplateTables(SQLiteDatabase db) throws SQLException {
            //create addon table
            db.execSQL(CREATE_SQL_DATATEMPLATE_TABLE);
            db.execSQL(CREATE_SQL_DATATEMPLATEVALUES_TABLE);
            db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_DATA_TEMPLATE + "_IX1 " + "ON " + TABLE_NAME_DATA_TEMPLATE + " ("
                    + COL_NAME_DATATEMPLATE__CLASS + " )");
            db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_DATA_TEMPLATE_VALUES + "_IX1 " + "ON " + TABLE_NAME_DATA_TEMPLATE_VALUES + " ("
                    + COL_NAME_DATATEMPLATEVALUES__TEMPLATE_ID + " )");
        }

        private void createBTDeviceCarTable(SQLiteDatabase db) throws SQLException {
            //create addon table
            db.execSQL(CREATE_SQL_BTDEVICECAR_TABLE);
        }

        private void createReimbursementCarRatesTable(SQLiteDatabase db) throws SQLException {
            db.execSQL(CREATE_SQL_REIMBURSEMENT_CAR_RATES_TABLE);
        }

        private void createMessageTables(SQLiteDatabase db) throws SQLException {
            db.execSQL(CREATE_SQL_DISPLAYED_MESSAGES_TABLE);
            db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_DISPLAYED_MESSAGES + "_IX1 " + " ON " +
                    TABLE_NAME_DISPLAYED_MESSAGES + " (" + COL_NAME_GEN_NAME + ")");

            db.execSQL(CREATE_SQL_MESSAGES_TABLE);
            db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_MESSAGES + "_IX1 " + " ON " +
                    TABLE_NAME_MESSAGES + " (" + COL_NAME_MESSAGES__DATE + " DESC )");
            db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_MESSAGES + "_IX2 " + " ON " +
                    TABLE_NAME_MESSAGES + " (" + COL_NAME_MESSAGES__IS_READ + " ASC )");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            // !!!!!!!!!!!!!!DON'T FORGET onCREATE !!!!!!!!!!!!!!!!

            if (oldVersion == 1) {
                upgradeDbTo200(db, oldVersion);
            }
            else if (oldVersion == 200) {
                upgradeDbTo210(db, oldVersion);
            }
            else if (oldVersion == 210) {
                upgradeDbTo300(db, oldVersion);
            }
            else if (oldVersion == 300) {
                upgradeDbTo310(db, oldVersion);
            }
            else if (oldVersion == 310) {
                upgradeDbTo330(db, oldVersion);
            }
            else if (oldVersion == 330) {
                upgradeDbTo340(db, oldVersion);
            }
            else if (oldVersion == 340 || oldVersion == 350) {
                upgradeDbTo350(db, oldVersion);
            }
            else if (oldVersion == 351) {
                upgradeDbTo355(db, oldVersion);
            }
            else if (oldVersion == 353) {
                upgradeDbTo355(db, oldVersion);
            }
            else if (oldVersion == 355) {
                upgradeDbTo356(db, oldVersion);
            }
            else if (oldVersion == 356) {
                upgradeDbTo357(db, oldVersion);
            }
            else if (oldVersion == 357) {
                upgradeDbTo358(db, oldVersion);
            }
            else if (oldVersion == 358) {
                upgradeDbTo359(db, oldVersion);
            }
            else if (oldVersion == 359) {
                upgradeDbTo400(db, oldVersion);
            }
            else if (oldVersion == 400) {
                upgradeDbTo401(db, oldVersion);
            }
            else if (oldVersion == 401) {
                upgradeDbTo430(db, oldVersion);
            }
            else if (oldVersion == 430) {
                upgradeDbTo500(db, oldVersion);
            }
            else if(oldVersion == 500) {
                upgradeDbTo501(db, oldVersion);
            } else if (oldVersion == 501) {
                upgradeDbTo502(db, oldVersion);
            }
            else if (oldVersion == 502) {
                upgradeDbTo503(db, oldVersion);
            }
            else if (oldVersion == 503) {
                upgradeDbTo510(db, oldVersion);
            }
            else if (oldVersion == 510) {
                upgradeDbTo511(db, oldVersion);
            }

            // !!!!!!!!!!!!!!DON'T FORGET onCREATE !!!!!!!!!!!!!!!!

            // create indexes
            createIndexes(db);
        }

        private void upgradeDbTo200(SQLiteDatabase db, int oldVersion) throws SQLException {
            createExpenseCategory(db);
            String updateSql;
            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__EXPENSECATEGORY_ID)) {
                updateSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__EXPENSECATEGORY_ID + " INTEGER";
                db.execSQL(updateSql);
                updateSql = "UPDATE " + TABLE_NAME_REFUEL + " SET " + COL_NAME_REFUEL__EXPENSECATEGORY_ID + " = 1";
                db.execSQL(updateSql);
            }
            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__ISFULLREFUEL)) {
                db.execSQL("ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__ISFULLREFUEL + " TEXT DEFAULT 'N' ");
            }
            createExpenses(db, true);

            upgradeDbTo210(db, oldVersion);
        }

        private void upgradeDbTo210(SQLiteDatabase db, int oldVersion) throws SQLException {
            String updSql;

            createCurrencyRateTable(db);
            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITYENTERED)) {
                updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__QUANTITYENTERED + " NUMERIC NULL ";
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET " + COL_NAME_REFUEL__QUANTITYENTERED + " = " + COL_NAME_REFUEL__QUANTITY;
                db.execSQL(updSql);
            }
            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUMEENTERED_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__UOMVOLUMEENTERED_ID + " INTEGER NULL ";
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET " + COL_NAME_REFUEL__UOMVOLUMEENTERED_ID + " = " + COL_NAME_REFUEL__UOMVOLUME_ID;
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET " + COL_NAME_REFUEL__UOMVOLUME_ID + " = " + "(SELECT " + COL_NAME_CAR__FUEL_UOM_ID + " "
                        + "FROM " + TABLE_NAME_CAR + " " + "WHERE " + COL_NAME_GEN_ROWID + " = "
                        + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + ") ";
                db.execSQL(updSql);
            }
            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__PRICEENTERED)) {
                updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__PRICEENTERED + " NUMERIC NULL ";
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET " + COL_NAME_REFUEL__PRICEENTERED + " = " + COL_NAME_REFUEL__PRICE;
                db.execSQL(updSql);
            }
            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCYENTERED_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__CURRENCYENTERED_ID + " INTEGER NULL ";
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET " + COL_NAME_REFUEL__CURRENCYENTERED_ID + " = " + COL_NAME_REFUEL__CURRENCY_ID;
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET " + COL_NAME_REFUEL__CURRENCY_ID + " = " + "(SELECT " + COL_NAME_CAR__CURRENCY_ID + " FROM "
                        + TABLE_NAME_CAR + " WHERE " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + " = "
                        + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + ") ";
                db.execSQL(updSql);
            }
            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCYRATE)) {
                updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__CURRENCYRATE + " NUMERIC NULL ";
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET " + COL_NAME_REFUEL__CURRENCYRATE + " = 1 ";
                db.execSQL(updSql);
            }
            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLCONVERSIONRATE)) {
                updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__UOMVOLCONVERSIONRATE + " NUMERIC NULL ";
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET " + COL_NAME_REFUEL__UOMVOLCONVERSIONRATE + " = 1 ";
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET " + COL_NAME_REFUEL__UOMVOLCONVERSIONRATE + " = " + "(SELECT " + COL_NAME_UOMCONVERSION__RATE
                        + " " + "FROM " + TABLE_NAME_UOMCONVERSION + " " + "WHERE " + COL_NAME_UOMCONVERSION__UOMFROM_ID + " = "
                        + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUMEENTERED_ID) + " " + "AND " + COL_NAME_UOMCONVERSION__UOMTO_ID
                        + " = " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUME_ID) + "), " + COL_NAME_REFUEL__QUANTITY + " = "
                        + "ROUND( " + COL_NAME_REFUEL__QUANTITYENTERED + " * " + "(SELECT " + COL_NAME_UOMCONVERSION__RATE + " " + "FROM "
                        + TABLE_NAME_UOMCONVERSION + " " + "WHERE " + COL_NAME_UOMCONVERSION__UOMFROM_ID + " = "
                        + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUMEENTERED_ID) + " " + "AND " + COL_NAME_UOMCONVERSION__UOMTO_ID
                        + " = " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUME_ID) + "), 2 ) " + "WHERE " + COL_NAME_REFUEL__UOMVOLUME_ID
                        + " <> " + COL_NAME_REFUEL__UOMVOLUMEENTERED_ID;
                db.execSQL(updSql);
            }

            if (!columnExists(db, TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNTENTERED)) {
                updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD " + COL_NAME_EXPENSE__AMOUNTENTERED + " NUMERIC NULL ";
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_EXPENSE + " SET " + COL_NAME_EXPENSE__AMOUNTENTERED + " = " + COL_NAME_EXPENSE__AMOUNT;
                db.execSQL(updSql);
            }

            if (!columnExists(db, TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCYENTERED_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD " + COL_NAME_EXPENSE__CURRENCYENTERED_ID + " INTEGER NULL ";
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_EXPENSE + " SET " + COL_NAME_EXPENSE__CURRENCYENTERED_ID + " = " + COL_NAME_EXPENSE__CURRENCY_ID;
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_EXPENSE + " SET " + COL_NAME_EXPENSE__CURRENCY_ID + " = " + "(SELECT " + COL_NAME_CAR__CURRENCY_ID + " FROM "
                        + TABLE_NAME_CAR + " WHERE " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + " = "
                        + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + ") ";
                db.execSQL(updSql);
            }

            if (!columnExists(db, TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCYRATE)) {
                updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD " + COL_NAME_EXPENSE__CURRENCYRATE + " NUMERIC NULL ";
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_EXPENSE + " SET " + COL_NAME_EXPENSE__CURRENCYRATE + " = 1";
                db.execSQL(updSql);
            }

            upgradeDbTo300(db, oldVersion);

        }

        private void upgradeDbTo300(SQLiteDatabase db, int oldVersion) throws SQLException {
            createGPSTrackTables(db);

            upgradeDbTo310(db, oldVersion);
        }

        private void upgradeDbTo310(SQLiteDatabase db, int oldVersion) throws SQLException {
            String updSql;
            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__AMOUNT)) {
                updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__AMOUNT + " NUMERIC NULL ";
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET " + COL_NAME_REFUEL__AMOUNT + " = " + COL_NAME_REFUEL__QUANTITYENTERED + " * "
                        + COL_NAME_REFUEL__PRICE;
                db.execSQL(updSql);
            }
            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__AMOUNTENTERED)) {
                updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__AMOUNTENTERED + " NUMERIC NULL ";
                db.execSQL(updSql);
                updSql = "UPDATE " + TABLE_NAME_REFUEL + " SET " + COL_NAME_REFUEL__AMOUNTENTERED + " = " + COL_NAME_REFUEL__QUANTITYENTERED + " * "
                        + COL_NAME_REFUEL__PRICEENTERED;
                db.execSQL(updSql);
            }

            upgradeDbTo330(db, oldVersion);
        }

        private void upgradeDbTo330(SQLiteDatabase db, int oldVersion) throws SQLException {
            String updSql;
            createBPartnerTable(db);

            if (!columnExists(db, TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__QUANTITY)) {
                updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD " + COL_NAME_EXPENSE__QUANTITY + " NUMERIC NULL ";
                db.execSQL(updSql);
            }
            if (!columnExists(db, TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__PRICE)) {
                updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD " + COL_NAME_EXPENSE__PRICE + " NUMERIC NULL ";
                db.execSQL(updSql);
            }
            if (!columnExists(db, TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__PRICEENTERED)) {
                updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD " + COL_NAME_EXPENSE__PRICEENTERED + " NUMERIC NULL ";
                db.execSQL(updSql);
            }
            if (!columnExists(db, TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__UOM_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD " + COL_NAME_EXPENSE__UOM_ID + " INTEGER NULL ";
                db.execSQL(updSql);
            }

            if (!columnExists(db, TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__BPARTNER_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD " + COL_NAME_EXPENSE__BPARTNER_ID + " INTEGER NULL ";
                db.execSQL(updSql);
            }
            if (!columnExists(db, TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__BPARTNER_LOCATION_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD " + COL_NAME_EXPENSE__BPARTNER_LOCATION_ID + " INTEGER NULL ";
                db.execSQL(updSql);
            }

            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__BPARTNER_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__BPARTNER_ID + " INTEGER NULL ";
                db.execSQL(updSql);
            }
            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__BPARTNER_LOCATION_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__BPARTNER_LOCATION_ID + " INTEGER NULL ";
                db.execSQL(updSql);
            }

            upgradeDbTo340(db, oldVersion);
        }

        private void upgradeDbTo340(SQLiteDatabase db, int oldVersion) throws SQLException {
            // createAddOnTable(db);
            createTagTable(db);
            String updSql;
            if (!columnExists(db, TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__TAG_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_MILEAGE + " ADD " + COL_NAME_MILEAGE__TAG_ID + " INTEGER NULL ";
                db.execSQL(updSql);
            }
            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__TAG_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__TAG_ID + " INTEGER NULL ";

                db.execSQL(updSql);
            }
            if (!columnExists(db, TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__TAG_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD " + COL_NAME_EXPENSE__TAG_ID + " INTEGER NULL ";
                db.execSQL(updSql);

            }
            if (!columnExists(db, TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TAG_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_GPSTRACK + " ADD " + COL_NAME_GPSTRACK__TAG_ID + " INTEGER NULL ";
                db.execSQL(updSql);
            }

            upgradeDbTo350(db, oldVersion);
        }

        private void upgradeDbTo350(SQLiteDatabase db, int oldVersion) throws SQLException {
            upgradeDbTo355(db, oldVersion);
        }

        private void upgradeDbTo355(SQLiteDatabase db, int oldVersion) throws SQLException {
            String updSql;
            if (!columnExists(db, TABLE_NAME_EXPENSECATEGORY, COL_NAME_EXPENSECATEGORY__ISFUEL)) {
                updSql = "ALTER TABLE " + TABLE_NAME_EXPENSECATEGORY + " ADD " + COL_NAME_EXPENSECATEGORY__ISFUEL + " TEXT DEFAULT 'N' ";
                db.execSQL(updSql);

                updSql = "UPDATE " + TABLE_NAME_EXPENSECATEGORY + " SET " + COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'Y' " + " WHERE " + COL_NAME_GEN_ROWID
                        + " = 1";

                db.execSQL(updSql);
            }

            upgradeDbTo356(db, oldVersion);
        }

        private void upgradeDbTo356(SQLiteDatabase db, int oldVersion) throws SQLException {
            createSecureBKSettingsTable(db);
            upgradeDbTo357(db, oldVersion);
        }

        private void upgradeDbTo357(SQLiteDatabase db, int oldVersion) throws SQLException {
            String sql = "DROP TABLE IF EXISTS " + TABLE_NAME_TASKTYPE;
            db.execSQL(sql);
            sql = "DROP TABLE IF EXISTS " + TABLE_NAME_TASK_CAR;
            db.execSQL(sql);
            sql = "DROP TABLE IF EXISTS " + TABLE_NAME_TASK;
            db.execSQL(sql);
            sql = "DROP TABLE IF EXISTS " + TABLE_NAME_TODO;
            db.execSQL(sql);
            createTaskToDoTables(db);

            upgradeDbTo358(db, oldVersion);
        }

        private void upgradeDbTo358(SQLiteDatabase db, int oldVersion) throws SQLException {
            createDataTemplateTables(db);

            upgradeDbTo359(db, oldVersion);
        }

        private void upgradeDbTo359(SQLiteDatabase db, int oldVersion) throws SQLException {
            createBTDeviceCarTable(db);

            upgradeDbTo400(db, oldVersion);
        }

        private void upgradeDbTo400(SQLiteDatabase db, int oldVersion) throws SQLException {
            String updSql;
            if (!columnExists(db, TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TOTALPAUSETIME)) {
                updSql = "ALTER TABLE " + TABLE_NAME_GPSTRACK + " ADD " + COL_NAME_GPSTRACK__TOTALPAUSETIME + " NUMBER NULL ";
                db.execSQL(updSql);

                updSql = "UPDATE " + TABLE_NAME_GPSTRACK + " SET " + COL_NAME_GPSTRACK__TOTALPAUSETIME + " = 0";

                db.execSQL(updSql);
            }

            upgradeDbTo401(db, oldVersion);
        }

        private void upgradeDbTo401(SQLiteDatabase db, int oldVersion) throws SQLException {
            createReimbursementCarRatesTable(db);
            upgradeDbTo430(db, oldVersion);
        }

        private void upgradeDbTo430(SQLiteDatabase db, int oldVersion) throws SQLException {
            String updSql;
            if (!columnExists(db, TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE_TO)) {
                updSql = "ALTER TABLE " + TABLE_NAME_MILEAGE + " ADD " + COL_NAME_MILEAGE__DATE_TO + " DATE NULL ";
                db.execSQL(updSql);

                updSql = "UPDATE " + TABLE_NAME_MILEAGE + " SET " + COL_NAME_MILEAGE__DATE_TO + " = " + COL_NAME_MILEAGE__DATE;

                db.execSQL(updSql);
            }

            upgradeDbTo500(db, oldVersion);
        }

        @SuppressLint("WrongConstant") private void upgradeDbTo500(SQLiteDatabase db, int oldVersion) throws SQLException {

            //transfer backup service settings from DB to preference
            long initTime = 946753200778L;
            boolean isActive = true;
            String scheduleType = ConstantValues.BACKUP_SERVICE_DAILY;
            String activeDaysBitmap = "1111111";
            String keepLastNo = "10";
            boolean notifyIfSuccess = true;

            SharedPreferences appPreferences = AndiCar.getDefaultSharedPreferences();
            Resources appResources = AndiCar.getAppResources();

            String[] bkScheduleTableColNames = {DB.COL_NAME_GEN_ROWID, DB.COL_NAME_GEN_NAME, DB.COL_NAME_GEN_ISACTIVE,
                    DB.COL_NAME_GEN_USER_COMMENT, "Frequency", "Days"};

            String logTag = "AndiCarDB";
            try {
                Cursor c = db.query("AO_BK_SCHEDULE", bkScheduleTableColNames, null, null, null, null, null);

                if (c.moveToFirst()) { //record exists
                    initTime = c.getLong(DB.COL_POS_GEN_NAME);
                    isActive = c.getString(DB.COL_POS_GEN_ISACTIVE).equals("Y");
                    if (c.getString(4).equals("D")) //frequency col pos
                    {
                        scheduleType = ConstantValues.BACKUP_SERVICE_DAILY;
                    }
                    else {
                        scheduleType = ConstantValues.BACKUP_SERVICE_WEEKLY;
                    }
                    activeDaysBitmap = c.getString(5);
                    keepLastNo = c.getString(DB.COL_POS_GEN_USER_COMMENT);
                    notifyIfSuccess = appPreferences.getBoolean("AddOn_AutoBackupService_NotifyIfSuccess", true);
                }
                else {
                    initTime = 946753200778L; //just for time part (01-01-2000 21:00)
                    isActive = true;
                    scheduleType = ConstantValues.BACKUP_SERVICE_DAILY;
                    activeDaysBitmap = "1111111";
                    keepLastNo = "10";
                    notifyIfSuccess = true;
                }
                c.close();
                //drop the table
                db.execSQL("DROP TABLE IF EXISTS AO_BK_SCHEDULE");
            }
            catch (Exception e) {
                AndiCarCrashReporter.sendCrash(e);
                Log.d(logTag, e.getMessage(), e);
            }
            finally {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(initTime);

                SharedPreferences.Editor editor = appPreferences.edit();
                editor.putInt(appResources.getString(R.string.pref_key_backup_service_exec_hour), cal.get(Calendar.HOUR_OF_DAY));
                editor.putInt(appResources.getString(R.string.pref_key_backup_service_exec_minute), cal.get(Calendar.MINUTE));
                editor.putString(appResources.getString(R.string.pref_key_backup_service_schedule_type), scheduleType);
                editor.putInt(appResources.getString(R.string.pref_key_backup_service_keep_last_backups_no), Integer.parseInt(keepLastNo));
                editor.putBoolean(appResources.getString(R.string.pref_key_backup_service_enabled), isActive);
                editor.putString(appResources.getString(R.string.pref_key_backup_service_backup_days), activeDaysBitmap);
                editor.putBoolean(appResources.getString(R.string.pref_key_backup_service_show_notification), notifyIfSuccess);
                editor.apply();

                try {
//                    ServiceStarter.startServicesUsingFBJobDispatcher(mCtx, ConstantValues.SERVICE_STARTER_START_BACKUP_SERVICE, null);
                    Utils.setBackupNextRun(mCtx, AndiCar.getDefaultSharedPreferences().getBoolean(mCtx.getString(R.string.pref_key_backup_service_enabled), false));
                }
                catch (Exception e) {
                    AndiCarCrashReporter.sendCrash(e);
                    Log.d(logTag, e.getMessage(), e);
                }
            }

            //make COL_NAME_MILEAGE__INDEXSTOP nullable (for two phase recording)
            String updSql;
            updSql = "ALTER TABLE " + TABLE_NAME_MILEAGE + " RENAME TO " + TABLE_NAME_MILEAGE + "_tmp";
            db.execSQL(updSql);

            //recreate the mileage table
            db.execSQL(CREATE_SQL_MILEAGE_TABLE);

            //insert the data
            updSql = "INSERT INTO " + TABLE_NAME_MILEAGE + " SELECT * FROM " + TABLE_NAME_MILEAGE + "_tmp";
            db.execSQL(updSql);

            upgradeDbTo501(db, oldVersion);
        }

        private void upgradeDbTo501(SQLiteDatabase db, int oldVersion) throws SQLException {
            //add expense type to gps track
            String updSql;
            if (!columnExists(db, TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__EXPENSETYPE_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_GPSTRACK + " ADD " + COL_NAME_GPSTRACK__EXPENSETYPE_ID + " INTEGER NULL ";
                db.execSQL(updSql);

                updSql = "UPDATE " + TABLE_NAME_GPSTRACK +
                        " SET " + COL_NAME_GPSTRACK__EXPENSETYPE_ID +
                        " = (SELECT " + COL_NAME_MILEAGE__EXPENSETYPE_ID +
                        " FROM " + TABLE_NAME_MILEAGE +
                        " WHERE " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_GEN_ROWID) +
                        " = " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MILEAGE_ID) + ")";

                db.execSQL(updSql);

                db.execSQL("CREATE INDEX IF NOT EXISTS " + TABLE_NAME_GPSTRACK + "_IX5 " + "ON " + TABLE_NAME_GPSTRACK + " (" + COL_NAME_GPSTRACK__EXPENSETYPE_ID + ")");
            }

            updSql = "UPDATE " + TABLE_NAME_EXPENSE +
                    " SET " + COL_NAME_EXPENSE__INDEX + " = NULL " +
                    " WHERE " + COL_NAME_EXPENSE__INDEX + " = ''";

            db.execSQL(updSql);
            upgradeDbTo502(db, oldVersion);
        }

        private void upgradeDbTo502(SQLiteDatabase db, int oldVersion) throws SQLException {
            String updSql;
            updSql = "DROP TABLE IF EXISTS " + TABLE_NAME_MILEAGE + "_tmp";
            db.execSQL(updSql);

            updSql = "UPDATE " + TABLE_NAME_TASK +
                    " SET " + COL_NAME_GEN_ISACTIVE + " = 'N' " +
                    " WHERE " + COL_NAME_TASK__TODOCOUNT + " = 0";
            db.execSQL(updSql);

            updSql = "UPDATE " + TABLE_NAME_TASK +
                    " SET " + COL_NAME_TASK__TODOCOUNT + " = 2 " +
                    " WHERE " + COL_NAME_TASK__TODOCOUNT + " < 2 " +
                    " AND " + COL_NAME_GEN_ISACTIVE + " = 'Y' ";
            db.execSQL(updSql);

            upgradeDbTo503(db, oldVersion);
        }

        private void upgradeDbTo503(SQLiteDatabase db, int oldVersion) throws SQLException {
            String updSql;
            updSql = "ALTER TABLE " + TABLE_NAME_BPARTNER + " ADD " + COL_NAME_BPARTNER__ISGASSTATION + " TEXT DEFAULT 'N' ";
            db.execSQL(updSql);

            updSql = "UPDATE " + TABLE_NAME_BPARTNER +
                    " SET " + COL_NAME_BPARTNER__ISGASSTATION + " = 'Y' " +
                    " WHERE " + COL_NAME_GEN_ROWID + " IN " +
                    "( " +
                    "SELECT DISTINCT " + COL_NAME_REFUEL__BPARTNER_ID +
                    " FROM " + TABLE_NAME_REFUEL +
                    ")";
            db.execSQL(updSql);
            upgradeDbTo510(db, oldVersion);
        }

        private void upgradeDbTo510(SQLiteDatabase db, int oldVersion) throws SQLException {
            String updSql;
            if (!columnExists(db, TABLE_NAME_EXPENSECATEGORY, COL_NAME_EXPENSECATEGORY__UOMTYPE)) {
                updSql = "ALTER TABLE " + TABLE_NAME_EXPENSECATEGORY + " ADD " + COL_NAME_EXPENSECATEGORY__UOMTYPE + " TEXT NOT NULL DEFAULT 'N' ";
                db.execSQL(updSql);

                updSql = "UPDATE " + TABLE_NAME_EXPENSECATEGORY +
                        " SET " + COL_NAME_EXPENSECATEGORY__UOMTYPE + " = '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "' " +
                        " WHERE " + COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'Y'";
                db.execSQL(updSql);

                updSql = "UPDATE " + TABLE_NAME_EXPENSECATEGORY +
                        " SET " + COL_NAME_EXPENSECATEGORY__UOMTYPE + " = '" + ConstantValues.UOM_TYPE_NONE_CODE + "' " +
                        " WHERE " + COL_NAME_EXPENSECATEGORY__ISFUEL + " = 'N'";
                db.execSQL(updSql);
            }

            String colPart = "INSERT INTO " + TABLE_NAME_EXPENSECATEGORY + " ( " + COL_NAME_GEN_NAME + ", " + COL_NAME_GEN_ISACTIVE + ", " +
                    COL_NAME_GEN_USER_COMMENT + ", " + COL_NAME_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST + ", " +
                    COL_NAME_EXPENSECATEGORY__ISFUEL + ", " + COL_NAME_EXPENSECATEGORY__UOMTYPE + " "
                    + ") ";
            Cursor c = db.rawQuery(
                    "SELECT * FROM " + TABLE_NAME_EXPENSECATEGORY +
                            " WHERE UPPER(" + COL_NAME_GEN_NAME + ") == UPPER('" + mResource.getString(R.string.DB_FuelType_LPG) + "')", null);
            if (c.getCount() == 0) {
                db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_LPG) + "', " + "'Y', " + "'"
                        + mResource.getString(R.string.DB_FuelType_LPGComment) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "'" +
                        " )");
            } else {
                db.execSQL("UPDATE " + TABLE_NAME_EXPENSECATEGORY +
                        " SET " + COL_NAME_GEN_USER_COMMENT + " = '" + mResource.getString(R.string.DB_FuelType_LPGComment) + "', " +
                        COL_NAME_EXPENSECATEGORY__UOMTYPE + " = '" + ConstantValues.UOM_TYPE_VOLUME_CODE + "' " +
                        "WHERE UPPER(" + COL_NAME_GEN_NAME + ") == UPPER('" + mResource.getString(R.string.DB_FuelType_LPG) + "')");
            }
            try {
                c.close();
            } catch (Exception ignored) {
            }

            c = db.rawQuery(
                    "SELECT * FROM " + TABLE_NAME_EXPENSECATEGORY +
                            " WHERE UPPER(" + COL_NAME_GEN_NAME + ") == UPPER('" + mResource.getString(R.string.DB_FuelType_CNG) + "')", null);
            if (c.getCount() == 0) {
                db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_CNG) + "', " + "'Y', " + "'"
                        + mResource.getString(R.string.DB_FuelType_CNGComment) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_WEIGHT_CODE + "'" +
                        " )");
            } else {
                db.execSQL("UPDATE " + TABLE_NAME_EXPENSECATEGORY +
                        " SET " + COL_NAME_GEN_USER_COMMENT + " = '" + mResource.getString(R.string.DB_FuelType_CNGComment) + "', " +
                        COL_NAME_EXPENSECATEGORY__UOMTYPE + " = '" + ConstantValues.UOM_TYPE_WEIGHT_CODE + "' " +
                        "WHERE UPPER(" + COL_NAME_GEN_NAME + ") == UPPER('" + mResource.getString(R.string.DB_FuelType_CNG) + "')");
            }
            try {
                c.close();
            } catch (Exception ignored) {
            }

            c = db.rawQuery(
                    "SELECT * FROM " + TABLE_NAME_EXPENSECATEGORY +
                            " WHERE UPPER(" + COL_NAME_GEN_NAME + ") == UPPER('" + mResource.getString(R.string.DB_FuelType_Electric) + "')", null);
            if (c.getCount() == 0) {
                db.execSQL(colPart + "VALUES ( " + "'" + mResource.getString(R.string.DB_FuelType_Electric) + "', " + "'Y', " + "'"
                        + mResource.getString(R.string.DB_FuelType_ElectricComment) + "', " + "'N', 'Y', '" + ConstantValues.UOM_TYPE_ENERGY_CODE + "'" +
                        " )");
            } else {
                db.execSQL("UPDATE " + TABLE_NAME_EXPENSECATEGORY +
                        " SET " + COL_NAME_GEN_USER_COMMENT + " = '" + mResource.getString(R.string.DB_FuelType_ElectricComment) + "', " +
                        COL_NAME_EXPENSECATEGORY__UOMTYPE + " = '" + ConstantValues.UOM_TYPE_ENERGY_CODE + "' " +
                        "WHERE UPPER(" + COL_NAME_GEN_NAME + ") == UPPER('" + mResource.getString(R.string.DB_FuelType_Electric) + "')");
            }
            try {
                c.close();
            } catch (Exception ignored) {
            }


            //new uom's
            long kgID; //kg
            long ggeID; //Gasoline gallon equivalent
            long lbID; //pound

            ContentValues cv = new ContentValues();

            c = db.rawQuery(
                    "SELECT * FROM " + TABLE_NAME_UOM +
                            " WHERE UPPER(" + COL_NAME_UOM__CODE + ") == 'KG'", null);
            if (c.moveToFirst()) {
                kgID = c.getLong(0);
                db.execSQL("UPDATE " + TABLE_NAME_UOM +
                        " SET " + COL_NAME_UOM__UOMTYPE + " = '" + ConstantValues.UOM_TYPE_WEIGHT_CODE + "'" +
                        "WHERE UPPER(" + COL_NAME_UOM__CODE + ") == 'KG'");
            }
            else {
                cv.put(COL_NAME_GEN_NAME, mResource.getString(R.string.DB_UOM_KgName));
                cv.put(COL_NAME_GEN_USER_COMMENT, mResource.getString(R.string.DB_UOM_KgComment));
                cv.put(COL_NAME_UOM__CODE, "kg");
                cv.put(COL_NAME_UOM__UOMTYPE, ConstantValues.UOM_TYPE_WEIGHT_CODE);
                cv.put(COL_NAME_GEN_ISACTIVE, "Y");
                kgID = db.insert(TABLE_NAME_UOM, null, cv);
                cv.clear();
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }

            c = db.rawQuery(
                    "SELECT * FROM " + TABLE_NAME_UOM +
                            " WHERE UPPER(" + COL_NAME_UOM__CODE + ") == 'GGE'", null);
            if (c.moveToFirst()) {
                ggeID = c.getLong(0);
                db.execSQL("UPDATE " + TABLE_NAME_UOM +
                        " SET " + COL_NAME_UOM__UOMTYPE + " = '" + ConstantValues.UOM_TYPE_WEIGHT_CODE + "'" +
                        "WHERE UPPER(" + COL_NAME_UOM__CODE + ") == 'GGE'");
            }
            else {
                cv.put(COL_NAME_GEN_NAME, mResource.getString(R.string.DB_UOM_GGEName));
                cv.put(COL_NAME_GEN_USER_COMMENT, mResource.getString(R.string.DB_UOM_GGEComment));
                cv.put(COL_NAME_UOM__CODE, "gge");
                cv.put(COL_NAME_UOM__UOMTYPE, ConstantValues.UOM_TYPE_WEIGHT_CODE);
                cv.put(COL_NAME_GEN_ISACTIVE, "Y");
                ggeID = db.insert(TABLE_NAME_UOM, null, cv);
                cv.clear();
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }

            c = db.rawQuery(
                    "SELECT * FROM " + TABLE_NAME_UOM +
                            " WHERE UPPER(" + COL_NAME_UOM__CODE + ") == 'LB'", null);
            if (c.moveToFirst()) {
                lbID = c.getLong(0);
                db.execSQL("UPDATE " + TABLE_NAME_UOM +
                        " SET " + COL_NAME_UOM__UOMTYPE + " = '" + ConstantValues.UOM_TYPE_WEIGHT_CODE + "'" +
                        "WHERE UPPER(" + COL_NAME_UOM__CODE + ") == 'LB'");
            }
            else {
                cv.put(COL_NAME_GEN_NAME, mResource.getString(R.string.DB_UOM_LBName));
                cv.put(COL_NAME_GEN_USER_COMMENT, mResource.getString(R.string.DB_UOM_LBComment));
                cv.put(COL_NAME_UOM__CODE, "lb");
                cv.put(COL_NAME_UOM__UOMTYPE, ConstantValues.UOM_TYPE_WEIGHT_CODE);
                cv.put(COL_NAME_GEN_ISACTIVE, "Y");
                lbID = db.insert(TABLE_NAME_UOM, null, cv);
                cv.clear();
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }

            c = db.rawQuery(
                    "SELECT * FROM " + TABLE_NAME_UOM +
                            " WHERE UPPER(" + COL_NAME_UOM__CODE + ") IN ('KW⋅H', 'KWH', 'KW H')", null);
            if (c.getCount() == 0) {
                cv.put(COL_NAME_GEN_NAME, mResource.getString(R.string.DB_UOM_KWhName));
                cv.put(COL_NAME_GEN_USER_COMMENT, mResource.getString(R.string.DB_UOM_KWhComment));
                cv.put(COL_NAME_UOM__CODE, "kW⋅h");
                cv.put(COL_NAME_UOM__UOMTYPE, ConstantValues.UOM_TYPE_ENERGY_CODE);
                cv.put(COL_NAME_GEN_ISACTIVE, "Y");
                db.insert(TABLE_NAME_UOM, null, cv);
                cv.clear();
            }
            else {
                db.execSQL("UPDATE " + TABLE_NAME_UOM +
                        " SET " + COL_NAME_UOM__UOMTYPE + " = '" + ConstantValues.UOM_TYPE_ENERGY_CODE + "'" +
                        "WHERE UPPER(" + COL_NAME_UOM__CODE + ") IN ('KW⋅H', 'KWH', 'KW H')");
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }

            //uom conversions
//            1 kg = 2.204624 lb
//            1 lb = 0.453592 kg
//            1 kg = 0.389559 gge
//            1 gge = 2.567 kg
//            1 lb = 0.176678 gge
//            1 gge = 5.660 lb

            String checkSql = " SELECT * " +
                    " FROM " + TABLE_NAME_UOMCONVERSION +
                    " WHERE " + COL_NAME_GEN_ISACTIVE + "='Y' " +
                    " AND " + COL_NAME_UOMCONVERSION__UOMFROM_ID + " = ? " +
                    " AND " + COL_NAME_UOMCONVERSION__UOMTO_ID + " = ?";

            String[] selectionArgs = {Long.toString(kgID), Long.toString(lbID)};
            c = db.rawQuery(checkSql, selectionArgs);
            if (c.getCount() == 0) {
                cv.put(COL_NAME_GEN_NAME, mResource.getString(R.string.DB_UOMConversion_KgToLbName));
                cv.put(COL_NAME_GEN_USER_COMMENT, mResource.getString(R.string.DB_UOMConversion_KgToLbComment));
                cv.put(COL_NAME_UOMCONVERSION__UOMFROM_ID, kgID);
                cv.put(COL_NAME_UOMCONVERSION__UOMTO_ID, lbID);
                cv.put(COL_NAME_UOMCONVERSION__RATE, 2.204624);
                cv.put(COL_NAME_GEN_ISACTIVE, "Y");
                db.insert(TABLE_NAME_UOMCONVERSION, null, cv);
                cv.clear();
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }

            selectionArgs[0] = Long.toString(lbID);
            selectionArgs[1] = Long.toString(kgID);
            c = db.rawQuery(checkSql, selectionArgs);
            if (c.getCount() == 0) {
                cv.put(COL_NAME_GEN_NAME, mResource.getString(R.string.DB_UOMConversion_LbToKgName));
                cv.put(COL_NAME_GEN_USER_COMMENT, mResource.getString(R.string.DB_UOMConversion_LbToKgComment));
                cv.put(COL_NAME_UOMCONVERSION__UOMFROM_ID, lbID);
                cv.put(COL_NAME_UOMCONVERSION__UOMTO_ID, kgID);
                cv.put(COL_NAME_UOMCONVERSION__RATE, 0.453592);
                cv.put(COL_NAME_GEN_ISACTIVE, "Y");
                db.insert(TABLE_NAME_UOMCONVERSION, null, cv);
                cv.clear();
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }

            selectionArgs[0] = Long.toString(kgID);
            selectionArgs[1] = Long.toString(ggeID);
            c = db.rawQuery(checkSql, selectionArgs);
            if (c.getCount() == 0) {
                cv.put(COL_NAME_GEN_NAME, mResource.getString(R.string.DB_UOMConversion_KgToGGEName));
                cv.put(COL_NAME_GEN_USER_COMMENT, mResource.getString(R.string.DB_UOMConversion_KgToGGEComment));
                cv.put(COL_NAME_UOMCONVERSION__UOMFROM_ID, kgID);
                cv.put(COL_NAME_UOMCONVERSION__UOMTO_ID, ggeID);
                cv.put(COL_NAME_UOMCONVERSION__RATE, 0.389559);
                cv.put(COL_NAME_GEN_ISACTIVE, "Y");
                db.insert(TABLE_NAME_UOMCONVERSION, null, cv);
                cv.clear();
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }

            selectionArgs[0] = Long.toString(ggeID);
            selectionArgs[1] = Long.toString(kgID);
            c = db.rawQuery(checkSql, selectionArgs);
            if (c.getCount() == 0) {
                cv.put(COL_NAME_GEN_NAME, mResource.getString(R.string.DB_UOMConversion_GGEToKgName));
                cv.put(COL_NAME_GEN_USER_COMMENT, mResource.getString(R.string.DB_UOMConversion_GGEToKgComment));
                cv.put(COL_NAME_UOMCONVERSION__UOMFROM_ID, ggeID);
                cv.put(COL_NAME_UOMCONVERSION__UOMTO_ID, kgID);
                cv.put(COL_NAME_UOMCONVERSION__RATE, 2.567);
                cv.put(COL_NAME_GEN_ISACTIVE, "Y");
                db.insert(TABLE_NAME_UOMCONVERSION, null, cv);
                cv.clear();
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }

            selectionArgs[0] = Long.toString(lbID);
            selectionArgs[1] = Long.toString(ggeID);
            c = db.rawQuery(checkSql, selectionArgs);
            if (c.getCount() == 0) {
                cv.put(COL_NAME_GEN_NAME, mResource.getString(R.string.DB_UOMConversion_LbToGGEName));
                cv.put(COL_NAME_GEN_USER_COMMENT, mResource.getString(R.string.DB_UOMConversion_LbToGGEComment));
                cv.put(COL_NAME_UOMCONVERSION__UOMFROM_ID, lbID);
                cv.put(COL_NAME_UOMCONVERSION__UOMTO_ID, ggeID);
                cv.put(COL_NAME_UOMCONVERSION__RATE, 0.176678);
                cv.put(COL_NAME_GEN_ISACTIVE, "Y");
                db.insert(TABLE_NAME_UOMCONVERSION, null, cv);
                cv.clear();
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }

            selectionArgs[0] = Long.toString(ggeID);
            selectionArgs[1] = Long.toString(lbID);
            c = db.rawQuery(checkSql, selectionArgs);
            if (c.getCount() == 0) {
                cv.put(COL_NAME_GEN_NAME, mResource.getString(R.string.DB_UOMConversion_GGEToLbName));
                cv.put(COL_NAME_GEN_USER_COMMENT, mResource.getString(R.string.DB_UOMConversion_GGEToLbComment));
                cv.put(COL_NAME_UOMCONVERSION__UOMFROM_ID, ggeID);
                cv.put(COL_NAME_UOMCONVERSION__UOMTO_ID, lbID);
                cv.put(COL_NAME_UOMCONVERSION__RATE, 0.176678);
                cv.put(COL_NAME_GEN_ISACTIVE, "Y");
                db.insert(TABLE_NAME_UOMCONVERSION, null, cv);
                cv.clear();
            }
            try {
                c.close();
            }
            catch (Exception ignored) {
            }


            db.execSQL("UPDATE " + TABLE_NAME_DATA_TEMPLATE_VALUES +
                    " SET " + COL_NAME_GEN_NAME + " = 'spnUomFuel'" +
                    "WHERE " + COL_NAME_GEN_NAME + " = 'spnUomVolume'");

            db.execSQL("UPDATE " + TABLE_NAME_DATA_TEMPLATE_VALUES +
                    " SET " + COL_NAME_GEN_NAME + " = 'spnExpCatOrFuelType'" +
                    "WHERE " + COL_NAME_GEN_NAME + " = 'spnExpCategory'");

            if (!columnExists(db, TABLE_NAME_CAR, COL_NAME_CAR__ISAFV)) {
                updSql = "ALTER TABLE " + TABLE_NAME_CAR + " ADD " + COL_NAME_CAR__ISAFV + " TEXT DEFAULT 'N' ";
                db.execSQL(updSql);
            }

            if (!columnExists(db, TABLE_NAME_CAR, COL_NAME_CAR__ALTERNATIVE_FUEL_UOM_ID)) {
                updSql = "ALTER TABLE " + TABLE_NAME_CAR + " ADD " + COL_NAME_CAR__ALTERNATIVE_FUEL_UOM_ID + " INTEGER NULL ";
                db.execSQL(updSql);
            }

            if (!columnExists(db, TABLE_NAME_REFUEL, COL_NAME_REFUEL__ISALTERNATIVEFUEL)) {
                updSql = "ALTER TABLE " + TABLE_NAME_REFUEL + " ADD " + COL_NAME_REFUEL__ISALTERNATIVEFUEL + " TEXT DEFAULT 'N' ";
                db.execSQL(updSql);
            }

            upgradeDbTo511(db, oldVersion);
        }

        private void upgradeDbTo511(SQLiteDatabase db, int oldVersion) throws SQLException {
            createMessageTables(db);
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean columnExists(SQLiteDatabase db, String table, String column) {
            String testSql = "SELECT " + column + " FROM " + table + " WHERE 1=2";
            try {
                db.rawQuery(testSql, null);
                return true;
            }
            catch (SQLiteException e) {
                return false;
            }
        }
    }
    //@formatter:on
}
