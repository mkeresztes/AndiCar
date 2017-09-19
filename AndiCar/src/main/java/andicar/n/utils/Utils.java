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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.BuildConfig;
import org.andicar2.activity.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import andicar.n.activity.dialogs.GeneralNotificationDialogActivity;
import andicar.n.persistence.DBAdapter;
import andicar.n.persistence.DBReportAdapter;

/**
 * @author miki
 */
public class Utils {
    /**
     * Round Up (24:00) or Down (00:00) a datetime
     *
     * @param dateTimeInMillis the datetime in milliseconds
     * @param decodeType       type of decode. See StaticValues.dateDecodeType...
     */
    //old name: decodeDateStr
    @SuppressLint("WrongConstant")
    @SuppressWarnings("SameParameterValue")
    public static long roundDate(long dateTimeInMillis, String decodeType) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateTimeInMillis);
        if (decodeType.equals(ConstantValues.DATE_DECODE_TO_ZERO)) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        else if (decodeType.equals(ConstantValues.DATE_DECODE_TO_24)) {
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
        }
        return cal.getTimeInMillis();
    }

    @SuppressWarnings("SameParameterValue")
    public static String pad(long value, int length) {
        return pad(Long.toString(value), length);
    }

    private static String pad(String value, int length) {
        if (value.length() >= length) {
            return value;
        }
        else {
            return pad("0" + value, length);
        }

    }

    /**
     * Append a datetime identifier to the input string
     *
     * @param inStr                   the input string
     * @param appendMinute            append or not minutes
     * @param appendSecondMillisecond append or not seconds+miliseconds
     * @param separator               separator for date part of the string
     * @return the completed input string with the datetime string
     */
    @SuppressLint("WrongConstant")
    @SuppressWarnings({"SameParameterValue", "WeakerAccess"})
    public static String appendDateTime(String inStr, boolean appendMinute, boolean appendSecondMillisecond, String separator) {
        Calendar cal = Calendar.getInstance();
        inStr = inStr + cal.get(Calendar.YEAR) + (separator != null ? separator : "")
                + pad(cal.get(Calendar.MONTH) + 1, 2) + (separator != null ? separator : "")
                + pad(cal.get(Calendar.DAY_OF_MONTH), 2) + (separator != null ? separator : "")
                + pad(cal.get(Calendar.HOUR_OF_DAY), 2);
        if (appendMinute) {
            inStr = inStr + pad(cal.get(Calendar.MINUTE), 2);
        }
        if (appendSecondMillisecond) {
            inStr = inStr + pad(cal.get(Calendar.SECOND), 2) + cal.get(Calendar.MILLISECOND);
        }
        return inStr;
    }

    /**
     * Used in log files
     *
     * @return a string representation of the current date/time
     */
    @SuppressLint("WrongConstant")
    public static String getCurrentDateTimeForLog() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) + "-"
                + pad(cal.get(Calendar.MONTH) + 1, 2) + "-"
                + pad(cal.get(Calendar.DAY_OF_MONTH), 2) + " "
                + pad(cal.get(Calendar.HOUR_OF_DAY), 2) + ":"
                + pad(cal.get(Calendar.MINUTE), 2) + ":"
                + pad(cal.get(Calendar.SECOND), 2) + "." + cal.get(Calendar.MILLISECOND);
    }

    /**
     * @param number       : the number which will be converted to string
     * @param localeFormat : also format the returned string according to locale formats
     * @return the string representation of the number
     */
    public static String numberToString(@Nullable Object number, boolean localeFormat, int scale, RoundingMode roundingMode) {
        if (number == null) {
            return "";
        }
        try {
            BigDecimal bdNumber = null;

            if (number instanceof Double) {
                bdNumber = new BigDecimal((Double) number);
            }
            else if (number instanceof Float) {
                bdNumber = new BigDecimal((Float) number);
            }
            else if (number instanceof Integer) {
                bdNumber = new BigDecimal((Integer) number);
            }
            else if (number instanceof Long) {
                bdNumber = new BigDecimal((Long) number);
            }
            else if (number instanceof Short) {
                bdNumber = new BigDecimal((Short) number);
            }
            else if (number instanceof BigDecimal) {
                bdNumber = (BigDecimal) number;
            }
            else if (number instanceof String) {
                bdNumber = new BigDecimal((String) number);
            }

            assert bdNumber != null;
            bdNumber = bdNumber.setScale(scale, roundingMode);
            bdNumber = bdNumber.stripTrailingZeros();

            if (localeFormat) {
                NumberFormat nf = NumberFormat.getInstance();
                if (nf instanceof DecimalFormat) {
                    DecimalFormatSymbols dfs = ((DecimalFormat) nf).getDecimalFormatSymbols();
                    nf.setMinimumFractionDigits(scale);
                    String retVal = nf.format(bdNumber);
                    if (retVal.contains("" + dfs.getDecimalSeparator())) {
                        //strip trailing zeroes
                        while ((retVal.endsWith("0") || retVal.endsWith("" + dfs.getDecimalSeparator()) || retVal.endsWith("" + dfs.getGroupingSeparator()))
                                && retVal.length() > 1) {
                            if (retVal.endsWith("" + dfs.getDecimalSeparator())) {
                                retVal = retVal.substring(0, retVal.length() - 1);
                                break;
                            }
                            retVal = retVal.substring(0, retVal.length() - 1);
                        }
                    }
                    return retVal;
                }
                else {
                    return numberToStringOld(number, true, scale, roundingMode);
                }
            }
            return bdNumber.toPlainString();
        }
        catch (Exception e) {
            return numberToStringOld(number, localeFormat, scale, roundingMode);
        }
    }

    private static String numberToStringOld(Object number, boolean localeFormat, int scale, RoundingMode roundingMode) {
        BigDecimal bdNumber = null;

        if (number instanceof Double) {
            bdNumber = new BigDecimal((Double) number);
        }
        else if (number instanceof Float) {
            bdNumber = new BigDecimal((Float) number);
        }
        else if (number instanceof Integer) {
            bdNumber = new BigDecimal((Integer) number);
        }
        else if (number instanceof Long) {
            bdNumber = new BigDecimal((Long) number);
        }
        else if (number instanceof Short) {
            bdNumber = new BigDecimal((Short) number);
        }
        else if (number instanceof BigDecimal) {
            bdNumber = (BigDecimal) number;
        }

        assert bdNumber != null;
        bdNumber = bdNumber.setScale(scale, roundingMode);
        bdNumber = bdNumber.stripTrailingZeros();
        if (localeFormat) {
            return NumberFormat.getInstance().format(bdNumber);
        }

        return bdNumber.toPlainString();
    }

    public static String getDaysHoursMinutesFromSec(long sec) {
        int days = (int) (sec / (86400d));
        sec = sec - (days * 86400);
        int hours = (int) (sec / 3600d);
        sec = sec - (hours * 3600);
        int minutes = (int) (sec / 60d);
        return (days != 0 ? days + "d " : "") + pad(hours, 2) + ":" + pad(minutes, 2);
//				(hours < 10 ? "0" : "") + hours + ":" + minutes;
    }

    public static boolean isNetworkAvailable(Context ctx, boolean onlyWiFi) {
        ConnectivityManager cm =
                (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (onlyWiFi) {
            return netInfo != null && netInfo.isConnectedOrConnecting() && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
        else {
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
    }

    /**
     * The rate app menu is shown only if the user entered a min 15 records.
     *
     * @param ctx Context
     * @return true if a minimum of 15 records found, false otherwise
     */
    public static boolean isCanShowRateApp(Context ctx) {
        boolean retVal = false;
        DBAdapter mDbAdapter = new DBAdapter(ctx);
        //@formatter:off
        String sql =
                "SELECT SUM(chk) " +
                "FROM ( " +
                    " SELECT COUNT(*) as chk " +
                    " FROM " + DBAdapter.TABLE_NAME_EXPENSE + //this include also the records from refuel
                    " UNION " +
                    " SELECT COUNT(*) as chk " +
                    " FROM " + DBAdapter.TABLE_NAME_MILEAGE +
                ")";
        //@formatter:on
        try {
            Cursor c = mDbAdapter.execSelectSql(sql, null);
            if (c.moveToNext()) {
                retVal = c.getInt(0) >= 15;
            }
            c.close();
            mDbAdapter.close();
        }
        catch (Exception ignored) {
        }

        return retVal;
    }

    /**
     * @param hourOfDay hour of the day in 24h format [0-23]
     * @param minute    minute
     * @return a string represented the formatted date and time
     */
    public static String getTimeString(Context ctx, int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(1970, Calendar.JANUARY, 1, hourOfDay, minute, 0);
        return DateFormat.getTimeFormat(ctx).format(cal.getTime());
    }

    public static String getDateString(Context ctx, Calendar cal) {
//		Calendar cal = Calendar.getInstance();
//		cal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
//		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
//		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
        return DateFormat.getMediumDateFormat(ctx).format(cal.getTime());
    }

    /**
     * Check mandatory EditText views. The checks are based on the type of the child view (must be EditText), the tag of the view must be R.string.GEN_Required,
     * the view must be visible (isShown() == true)
     *
     * @param root the root VieGroup to be checked
     * @return -1 if empty mandatory EditText not found, the id of the View otherwise.
     */
    public static int checkMandatoryFields(ViewGroup root) {
        View vwChild;
        EditText etChild;
        int retVal;
        if (root == null) {
            return -1;
        }

        for (int i = 0; i < root.getChildCount(); i++) {
            vwChild = root.getChildAt(i);
            if (vwChild instanceof ViewGroup) {
                retVal = checkMandatoryFields((ViewGroup) vwChild);
                if (retVal > -1) {
                    return retVal;
                }
            }
            else if (vwChild instanceof EditText) {
                etChild = (EditText) vwChild;
                if (etChild.getTag() != null
                        && etChild.getTag().equals(AndiCar.getAppResources().getString(R.string.gen_required))
                        && etChild.isShown()
                        && etChild.getText().toString().length() == 0) {
                    return etChild.getId();
                }
            }
        }
        return -1;
    }

    public static void showInfoDialog(Context ctx, String message, String detail) {
        Intent notif = new Intent(ctx, GeneralNotificationDialogActivity.class);
        notif.putExtra(GeneralNotificationDialogActivity.NOTIF_MESSAGE_KEY, message);
        notif.putExtra(GeneralNotificationDialogActivity.NOTIF_DETAIL_KEY, detail);
        notif.putExtra(GeneralNotificationDialogActivity.DIALOG_TYPE_KEY, GeneralNotificationDialogActivity.DIALOG_TYPE_INFO);
        ctx.startActivity(notif);
    }

    public static void showNotReportableErrorDialog(Context ctx, String message, String detail, boolean fromService) {
        try {
            Intent notif = new Intent(ctx, GeneralNotificationDialogActivity.class);
            notif.putExtra(GeneralNotificationDialogActivity.NOTIF_MESSAGE_KEY, message);
            notif.putExtra(GeneralNotificationDialogActivity.NOTIF_DETAIL_KEY, detail);
            notif.putExtra(GeneralNotificationDialogActivity.DIALOG_TYPE_KEY, GeneralNotificationDialogActivity.DIALOG_TYPE_NOT_REPORTABLE_ERROR);
            if (fromService) {
                notif.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            ctx.startActivity(notif);
        }
        catch (Exception ignored) {
        }
    }

    public static void showReportableErrorDialog(Context ctx, String message, String detail, Exception e, boolean fromService) {
        try {
            Intent notif = new Intent(ctx, GeneralNotificationDialogActivity.class);
            if (e != null) {
                notif.putExtra(GeneralNotificationDialogActivity.NOTIF_EXCEPTION_STRING_KEY, Utils.getStackTrace(e));
                notif.putExtra(GeneralNotificationDialogActivity.NOTIF_EXCEPTION_KEY, e);
            }
            else {
                notif.putExtra(GeneralNotificationDialogActivity.NOTIF_EXCEPTION_STRING_KEY, "");
            }
            notif.putExtra(GeneralNotificationDialogActivity.NOTIF_MESSAGE_KEY, message);
            notif.putExtra(GeneralNotificationDialogActivity.NOTIF_DETAIL_KEY, detail);
            notif.putExtra(GeneralNotificationDialogActivity.DIALOG_TYPE_KEY, GeneralNotificationDialogActivity.DIALOG_TYPE_REPORTABLE_ERROR);
            if (fromService) {
                notif.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            ctx.startActivity(notif);
        }
        catch (Exception ignored) {
        }
    }

    public static void initSpinner(DBAdapter dbAdapter, View pSpinner,
                                   String tableName, String selection, long selectedId, boolean addEmptyValue) {
        try {
            Spinner spnCurrentSpinner = (Spinner) pSpinner;
            spnCurrentSpinner.setTag(ConstantValues.IS_INITIALIZATION_IN_PROGRESS_TAG);

            Cursor dbcRecordCursor;
            //@formatter:off
            if (addEmptyValue) {
                String selectSql =
                        "SELECT " +
                                "-1 AS " + DBAdapter.COL_NAME_GEN_ROWID + ", " +
                                "' ' AS " + DBAdapter.COL_NAME_GEN_NAME +
                        " UNION " +
                        " SELECT " +
                                DBAdapter.COL_NAME_GEN_ROWID +
                                ", " + DBAdapter.COL_NAME_GEN_NAME +
                        " FROM " + tableName +
                        " WHERE 1 = 1 ";
                if (selection != null && selection.length() > 0) {
                    selectSql = selectSql + selection;
                }
                selectSql = selectSql + " ORDER BY " + DBAdapter.COL_NAME_GEN_NAME;

                dbcRecordCursor = dbAdapter.execSelectSql(selectSql, null);
            }
            else {
                dbcRecordCursor = dbAdapter.query(tableName, DBAdapter.COL_LIST_GEN_ROWID_NAME, "1 = 1 " + selection, null, DBAdapter.COL_NAME_GEN_NAME);
            }
            //@formatter:on

            if (dbcRecordCursor == null)
                return;

            List<String> recordsList = new ArrayList<>();
            while (dbcRecordCursor.moveToNext()) {
                recordsList.add(dbcRecordCursor.getString(1));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(pSpinner.getContext(), R.layout.ui_element_spinner_item, recordsList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnCurrentSpinner.setAdapter(adapter);

            if (selectedId >= 0) {
                //set the spinner to this id
                dbcRecordCursor.moveToFirst();
                for (int i = 0; i < dbcRecordCursor.getCount(); i++) {
                    if (dbcRecordCursor.getLong(DBAdapter.COL_POS_GEN_ROWID) == selectedId) {
                        spnCurrentSpinner.setSelection(i);
                        break;
                    }
                    dbcRecordCursor.moveToNext();
                }
            }
            dbcRecordCursor.close();
        }
        catch (Exception e) {
            Utils.showReportableErrorDialog(pSpinner.getContext(), AndiCar.getAppResources().getString(R.string.error_sorry), e.getMessage(), e, false);
        }
    }

    /**
     * @param lTimeInMilliseconds time in millisecond to be formatted
     * @param bDateOnly           return only the date part
     * @return the date / time formatted
     */
    public static String getFormattedDateTime(long lTimeInMilliseconds, boolean bDateOnly) {
        java.text.DateFormat format[] = new java.text.DateFormat[]{java.text.DateFormat.getDateInstance(), java.text.DateFormat.getDateTimeInstance()};

        if (bDateOnly) {
            return format[0].format(new Date(lTimeInMilliseconds));
        }
        else {
            String result = format[1].format(new Date(lTimeInMilliseconds));
            if (result.endsWith(":00")) {
                result = result.substring(0, result.length() - 3);
            }
            return result;
        }
//
//        Calendar cal = Calendar.getInstance();
//        return
//                cal.get(Calendar.YEAR) + "-" + pad(cal.get(Calendar.MONTH) + 1, 2) + "-" + pad(cal.get(Calendar.DAY_OF_MONTH), 2)
//                        + " " + pad(cal.get(Calendar.HOUR_OF_DAY), 2)
//                        + ":" + pad(cal.get(Calendar.MINUTE), 2)
//                        + " " + pad(cal.get(Calendar.SECOND), 2);
    }

    /**
     * Convert seconds in format X Days Y h Z min [S s]
     *
     * @param lSeconds the seconds to be converted
     * @return a string representing the time in format X Days Y h Z min [S s]
     */
    public static String getTimeString(long lSeconds) {
        String retVal;
        long days = lSeconds / 86400;
        //get the remaining seconds
        long remaining = lSeconds - (days * 86400);
        long hours = remaining / 3600;
        remaining = remaining - (hours * 3600);
        long minutes = remaining / 60;
        retVal = (days > 0 ? days + " d " : "") + (hours > 0 ? hours + " h " : "") + minutes + " min";
        return retVal;
    }

    public static float getScreenWidthInPixel(Context ctx) {
        Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static float getScreenDensity(Context ctx) {
        return ctx.getResources().getDisplayMetrics().density;
    }

    //copied from org/apache/commons/lang3/exception/ExceptionUtils.java (https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/exception/ExceptionUtils.java)
    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static void sendAnalyticsEvent(Context ctx, String screenName, Bundle params, boolean sendAlways) {
        if (!BuildConfig.DEBUG || sendAlways) {
            FirebaseAnalytics.getInstance(ctx).logEvent(screenName, params);
        }
    }

    public void shareGPSTrack(Context ctx, Resources mRes, long gpsTrackID) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/html");
        Bundle b = new Bundle();
        DBAdapter mDbAdapter = new DBAdapter(ctx);
        String emailSubject = "AndiCar GPS Track";

        b.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_GPSTRACK, DBAdapter.COL_NAME_GEN_ROWID) + "=", Long.toString(gpsTrackID));
        DBReportAdapter dbReportAdapter = new DBReportAdapter(ctx, DBReportAdapter.GPS_TRACK_LIST_SELECT_NAME, b);
        Cursor c = dbReportAdapter.fetchReport(1);
        if (c != null && c.moveToFirst()) {
            String emailText = String.format(c.getString(c.getColumnIndex(DBReportAdapter.FIRST_LINE_LIST_NAME)), Utils.getFormattedDateTime(c.getLong(7) * 1000, false))
                    + "\n" +
                    String.format(c.getString(c.getColumnIndex(DBReportAdapter.SECOND_LINE_LIST_NAME)),
                            mRes.getString(R.string.gps_track_detail_var_1),
                            mRes.getString(R.string.gps_track_detail_var_2),
                            mRes.getString(R.string.gps_track_detail_var_3),
                            mRes.getString(R.string.gps_track_detail_var_4),
                            mRes.getString(R.string.gps_track_detail_var_5) + " " + Utils.getTimeString(c.getLong(4)),
                            mRes.getString(R.string.gps_track_detail_var_6) + " " + Utils.getTimeString(c.getLong(5)),
                            mRes.getString(R.string.gps_track_detail_var_7),
                            mRes.getString(R.string.gps_track_detail_var_8),
                            mRes.getString(R.string.gps_track_detail_var_9),
                            mRes.getString(R.string.gps_track_detail_var_10),
                            mRes.getString(R.string.gps_track_detail_var_11),
                            mRes.getString(R.string.gps_track_detail_var_12) + " " + Utils.getTimeString(c.getLong(8)),
                            mRes.getString(R.string.gps_track_detail_var_13) + " " + Utils.getTimeString(c.getLong(4) - c.getLong(8) - c.getLong(5)))
                    + "\n" +
                    c.getString(c.getColumnIndex(DBReportAdapter.THIRD_LINE_LIST_NAME));
            emailSubject = emailSubject + " - " + c.getString(c.getColumnIndex(DBReportAdapter.COL_NAME_GEN_NAME));
            c.close();
            dbReportAdapter.close();

            emailIntent.putExtra(Intent.EXTRA_TEXT, emailText + "\nSent by AndiCar (http://www.andicar.org)");
        }

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);

        //get the track files
        FileUtils.createFolderIfNotExists(ctx, ConstantValues.TRACK_FOLDER);
        String selection = DBAdapter.COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID + "= ? ";
        String[] selectionArgs = {Long.toString(gpsTrackID)};
        c = mDbAdapter.query(DBAdapter.TABLE_NAME_GPSTRACKDETAIL, DBAdapter.COL_LIST_GPSTRACKDETAIL_TABLE, selection, selectionArgs,
                DBAdapter.COL_NAME_GPSTRACKDETAIL__FILE);

        Bundle trackFiles = new Bundle();
        String trackFile;
        while (c.moveToNext()) {
            trackFile = c.getString(DBAdapter.COL_POS_GPSTRACKDETAIL__FILE);
            trackFiles.putString(trackFile.replace(ConstantValues.TRACK_FOLDER, ""), trackFile);
        }

        //create the zip file
        Uri trackFileZip = FileUtils.zipFiles(trackFiles, ConstantValues.TRACK_FOLDER + "AndiCarGPSTrack.zip");
        if (trackFileZip != null) {
            emailIntent.putExtra(Intent.EXTRA_STREAM, trackFileZip);
        }
        ctx.startActivity(Intent.createChooser(emailIntent, mRes.getString(R.string.gen_share)));
    }

}
