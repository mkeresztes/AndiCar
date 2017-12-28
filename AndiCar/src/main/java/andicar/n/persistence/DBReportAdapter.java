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

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import andicar.n.activity.fragment.TaskEditFragment;
import andicar.n.utils.ConstantValues;

/**
 * @author miki
 */

//@formatter:off
public class DBReportAdapter extends DBAdapter {
    public static final String FIRST_LINE_LIST_NAME = "FIRSTLINE";
    public static final String SECOND_LINE_LIST_NAME = "SECONDLINE";
    public static final String THIRD_LINE_LIST_NAME = "THIRDLINE";

    public static final String MILEAGE_LIST_SELECT_NAME = "mileageListViewSelect";
    public static final String MILEAGE_LIST_REPORT_SELECT = "mileageListReportSelect";
    public static final String LIST_STATISTICS_MILEAGE_TOTAL = "listStatisticsMileageTotal";
    public static final String LIST_STATISTICS_MILEAGE_BY_TYPES = "listStatisticsMileageByType";
    public static final String LIST_STATISTICS_MILEAGE_BY_TAGS = "listStatisticsMileageByTag";
    public static final String LIST_STATISTICS_MILEAGE_BY_DRIVERS = "listStatisticsMileageByDriver";
    public static final String LIST_STATISTICS_MILEAGE_BY_CARS = "listStatisticsMileageByCars";

    public static final String REFUEL_LIST_SELECT_NAME = "refuelListViewSelect";
    public static final String REFUEL_LIST_REPORT_SELECT = "refuelListReportSelect";
    public static final String LIST_STATISTICS_REFUEL_TOTAL = "listStatisticsRefuelTotal";
    public static final String LIST_STATISTICS_REFUEL_BY_TYPES = "listStatisticsRefuelByType";
    public static final String LIST_STATISTICS_REFUEL_BY_FUELTYPES = "listStatisticsRefuelByFuelType";
    public static final String LIST_STATISTICS_REFUEL_BY_TAGS = "listStatisticsRefuelByTag";
    public static final String LIST_STATISTICS_REFUEL_BY_DRIVERS = "listStatisticsRefuelByDriver";
    public static final String LIST_STATISTICS_REFUEL_BY_CARS = "listStatisticsRefuelByCars";

    public static final String EXPENSE_LIST_SELECT_NAME = "expenseListViewSelect";
    public static final String EXPENSES_LIST_REPORT_SELECT = "expensesListReportSelect";
    public static final String LIST_STATISTICS_EXPENSE_TOTAL = "listStatisticsExpenseTotal";
    public static final String LIST_STATISTICS_EXPENSE_BY_TYPES = "listStatisticsExpenseByType";
    public static final String LIST_STATISTICS_EXPENSE_BY_CATEGORIES = "listStatisticsExpenseByCategory";
    public static final String LIST_STATISTICS_EXPENSE_BY_TAGS = "listStatisticsExpenseByTag";
    public static final String LIST_STATISTICS_EXPENSE_BY_DRIVERS = "listStatisticsExpenseByDriver";
    public static final String LIST_STATISTICS_EXPENSE_BY_CARS = "listStatisticsExpenseByCars";

    public static final String GPS_TRACK_LIST_SELECT_NAME = "gpsTrackListViewSelect";
    public static final String GPS_TRACK_LIST_REPORT_SELECT = "gpsTrackListReportSelect";

    public static final String TODO_LIST_SELECT_NAME = "todoListViewSelect";
    public static final String TODO_LIST_REPORT_SELECT = "todoListReportSelect";

    public static final String CAR_LIST_SELECT_NAME = "carListViewSelect";
    public static final String DRIVER_LIST_SELECT_NAME = "driverListViewSelect";
    public static final String UOM_LIST_SELECT_NAME = "uomListViewSelect";
    public static final String UOM_CONVERSION_LIST_SELECT_NAME = "uomConversionListViewSelect";
    public static final String EXPENSE_FUEL_CATEGORY_LIST_SELECT_NAME = "expenseFuelCategoryListViewSelect";
    public static final String EXPENSE_TYPE_LIST_SELECT_NAME = "expenseTypeListViewSelect";
    public static final String REIMBURSEMENT_RATE_LIST_SELECT_NAME = "reimbursementRateListViewSelect";
    public static final String CURRENCY_LIST_SELECT_NAME = "currencyListViewSelect";
    public static final String CURRENCY_RATE_LIST_SELECT_NAME = "currencyRateListViewSelect";
    public static final String BPARTNER_LIST_SELECT_NAME = "bpartnerListViewSelect";
    public static final String TASK_TYPE_LIST_SELECT_NAME = "taskTypeListViewSelect";
    public static final String TASK_LIST_SELECT_NAME = "taskListViewSelect";
    public static final String BT_CAR_LINK_LIST_SELECT_NAME = "btCarLinkListViewSelect";
    public static final String TAG_LIST_SELECT_NAME = "tagListViewSelect";
    public static final String STATISTICS_SELECT_NAME = "statisticsMainViewSelect";


    private static final String FOURTH_LINE_LIST_NAME = "FOURTHLINE";
    private static final String FIFTH_LINE_LIST_NAME = "FIFTHLINE";

    //list view selects
    private static final String carListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + ", " +
                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " || '; ' || " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__REGISTRATIONNO) + " AS " + FIRST_LINE_LIST_NAME +
                    ", " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_USER_COMMENT) + " AS " + SECOND_LINE_LIST_NAME +
                    ", " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ISACTIVE) + " AS " + THIRD_LINE_LIST_NAME +
            " FROM " + TABLE_NAME_CAR +
            " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ISACTIVE) + " DESC, lower(" + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + ")";

    private static final String driverListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) + ", " +
                    sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " AS " + FIRST_LINE_LIST_NAME +
                    ", " + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_USER_COMMENT) + " AS " + SECOND_LINE_LIST_NAME +
                    ", " + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ISACTIVE) + " AS " + THIRD_LINE_LIST_NAME +
            " FROM " + TABLE_NAME_DRIVER +
            " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ISACTIVE) + " DESC, lower(" + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + ")";

    private static final String uomListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) + ", " +
                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_NAME) + " AS " + FIRST_LINE_LIST_NAME +
                    ", " + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_USER_COMMENT) + " AS " + SECOND_LINE_LIST_NAME +
                    ", " + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ISACTIVE) + " AS " + THIRD_LINE_LIST_NAME +
            " FROM " + TABLE_NAME_UOM +
            " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ISACTIVE) + " DESC, lower(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_NAME) + ")";

    private static final String uomConversionListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_UOMCONVERSION, COL_NAME_GEN_ROWID) + ", " +
                    sqlConcatTableColumn(TABLE_NAME_UOMCONVERSION, COL_NAME_GEN_NAME) + " || ': ' || " + sqlConcatTableColumn(TABLE_NAME_UOMCONVERSION, COL_NAME_UOMCONVERSION__RATE) +
                    " AS " + FIRST_LINE_LIST_NAME +
                    ", " + sqlConcatTableColumn(TABLE_NAME_UOMCONVERSION, COL_NAME_GEN_USER_COMMENT) + " AS " + SECOND_LINE_LIST_NAME +
                    ", " + sqlConcatTableColumn(TABLE_NAME_UOMCONVERSION, COL_NAME_GEN_ISACTIVE) + " AS " + THIRD_LINE_LIST_NAME +
            " FROM " + TABLE_NAME_UOMCONVERSION +
            " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_UOMCONVERSION, COL_NAME_GEN_ISACTIVE) + " DESC, lower(" + sqlConcatTableColumn(TABLE_NAME_UOMCONVERSION, COL_NAME_GEN_NAME) + ")";

    private static final String expenseFuelCategoryListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) + ", " +
                    sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) + " AS " + FIRST_LINE_LIST_NAME +
                    ", " + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_USER_COMMENT) + " AS " + SECOND_LINE_LIST_NAME +
                    ", " + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ISACTIVE) + " AS " + THIRD_LINE_LIST_NAME +
            " FROM " + TABLE_NAME_EXPENSECATEGORY +
            " WHERE 1=1 ";

    private static final String expenseTypeListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) + ", " +
                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + " AS " + FIRST_LINE_LIST_NAME +
                    ", " + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_USER_COMMENT) + " AS " + SECOND_LINE_LIST_NAME +
                    ", " + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ISACTIVE) + " AS " + THIRD_LINE_LIST_NAME +
            " FROM " + TABLE_NAME_EXPENSETYPE +
            " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ISACTIVE) + " DESC, lower(" + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + ")";

    private static final String reimbursementRateViewSelect =
            "SELECT "
                    + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_GEN_ROWID) + //#0
                    ", " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " || '; ' || " +
                                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + " AS " + FIRST_LINE_LIST_NAME + //#1
                    ", " + " '%1$s -> %2$s = %3$s ' || " + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " || '/' || " +
                                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " AS " + SECOND_LINE_LIST_NAME + //#2
                    ", " + "null AS " + THIRD_LINE_LIST_NAME + //#3
                    ", " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + //#4
                    ", " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO) + //#5
                    ", " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__RATE) + //#6
            " FROM " + TABLE_NAME_REIMBURSEMENT_CAR_RATES +
                " JOIN " + TABLE_NAME_EXPENSETYPE + " ON " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID) +
                                                            "=" + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_CAR + " ON " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_MILEAGE__CAR_ID) +
                                                            "=" + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_UOM + " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) +
                                                            "=" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CURRENCY + " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) +
                                                            "=" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
            " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_GEN_ISACTIVE) + " DESC, " +
                    sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO) + " DESC, " +
                    "lower(" + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + ")";

    private static final String currencyListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) + ", " +
                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_NAME) + " AS " + FIRST_LINE_LIST_NAME + ", " +
                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " AS " + SECOND_LINE_LIST_NAME + ", " +
                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ISACTIVE) + " AS " + THIRD_LINE_LIST_NAME +
            " FROM " + TABLE_NAME_CURRENCY +
            " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ISACTIVE) + " DESC, lower(" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_NAME) + ")";

    private static final String currencyRateListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_CURRENCYRATE, COL_NAME_GEN_ROWID) + ", " +
                    sqlConcatTableColumn(TABLE_NAME_CURRENCYRATE, COL_NAME_GEN_NAME) + " AS " + FIRST_LINE_LIST_NAME + ", " +
                    sqlConcatTableColumn(TABLE_NAME_CURRENCYRATE, COL_NAME_CURRENCYRATE__RATE) +
                    " || ' / ' || " + sqlConcatTableColumn(TABLE_NAME_CURRENCYRATE, COL_NAME_CURRENCYRATE__INVERSERATE) + " AS " + SECOND_LINE_LIST_NAME + ", " +
                    "null AS " + THIRD_LINE_LIST_NAME +
            " FROM " + TABLE_NAME_CURRENCYRATE +
            " ORDER BY lower(" + sqlConcatTableColumn(TABLE_NAME_CURRENCYRATE, COL_NAME_GEN_NAME) + ")";

    private static final String bpartnerListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_ROWID) + ", " +
                    sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_NAME) + " AS " + FIRST_LINE_LIST_NAME + ", " +
                    sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_USER_COMMENT) + " AS " + SECOND_LINE_LIST_NAME + ", " +
                    sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_ISACTIVE) + " AS " + THIRD_LINE_LIST_NAME +
            " FROM " + TABLE_NAME_BPARTNER +
            " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_ISACTIVE) + " DESC, lower(" + sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_NAME) + ")";

    private static final String taskTypeListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_ROWID) + ", " +
                    sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_NAME) + " AS " + FIRST_LINE_LIST_NAME + ", " +
                    sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_USER_COMMENT) + " AS " + SECOND_LINE_LIST_NAME + ", " +
                    sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_ISACTIVE) + " AS " + THIRD_LINE_LIST_NAME +
            " FROM " + TABLE_NAME_TASKTYPE +
            " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_ISACTIVE) + " DESC, lower(" + sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_NAME) + ")";

    private static final String taskListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_ROWID) + ", " +
                    sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_NAME) + " AS " + FIRST_LINE_LIST_NAME + ", " +
                    sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_USER_COMMENT) + " AS " + SECOND_LINE_LIST_NAME + ", " +
                    sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_ISACTIVE) + " AS " + THIRD_LINE_LIST_NAME +
            " FROM " + TABLE_NAME_TASK +
            " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_ISACTIVE) + " DESC, lower(" + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_NAME) + ")";

    private static final String btCarLinkListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_BTDEVICE_CAR, COL_NAME_GEN_ROWID) + ", " +
                    sqlConcatTableColumn(TABLE_NAME_BTDEVICE_CAR, COL_NAME_GEN_NAME) + " || ' (' || " + sqlConcatTableColumn(TABLE_NAME_BTDEVICE_CAR, COL_NAME_BTDEVICECAR__MACADDR) + " || ')' " +
                    " AS " + FIRST_LINE_LIST_NAME + ", " +
                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " AS " + SECOND_LINE_LIST_NAME + ", " +
                    sqlConcatTableColumn(TABLE_NAME_BTDEVICE_CAR, COL_NAME_GEN_ISACTIVE) + " AS " + THIRD_LINE_LIST_NAME +
            " FROM " + TABLE_NAME_BTDEVICE_CAR +
                " JOIN " + TABLE_NAME_CAR + " ON " + sqlConcatTableColumn(TABLE_NAME_BTDEVICE_CAR, COL_NAME_BTDEVICECAR__CAR_ID) +
                                                    " = " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
            " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_BTDEVICE_CAR, COL_NAME_GEN_ISACTIVE) + " DESC, lower(" + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + ")";

    private static final String tagListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) + ", " +
                    sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " AS " + FIRST_LINE_LIST_NAME + ", " +
                    "null AS " + SECOND_LINE_LIST_NAME + ", " +
                    sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ISACTIVE) + " AS " + THIRD_LINE_LIST_NAME +
            " FROM " + TABLE_NAME_TAG +
            " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ISACTIVE) + " DESC, lower(" + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + ")";

    //used in main activity and mileage list activity
    private static final String mileageListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_GEN_ROWID) + ", " + //#0

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " || '; ' || " + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) +
                        " || '; ' ||  '%1$s' AS " + FIRST_LINE_LIST_NAME + ", " + //#1

                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + " || ': ' || '%1$s -> %2$s = %3$s ' || " +
                        sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " || ' %4$s' AS " + SECOND_LINE_LIST_NAME + ", " + //#2

                    " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') || " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_GEN_USER_COMMENT) + " AS " + THIRD_LINE_LIST_NAME + ", " + //#3

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GEN_ROWID) + " AS gpsTrackId, " + //#4

                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) + " AS Seconds, " + //#5

                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + ", " + //#6

                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + ", " + //#7

                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " - " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + " AS Mileage, " + //#8

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + " AS CarID, " + //#9

                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) + " AS ExpenseTypeID, " + //#10

                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " AS CurrencyCode, " + //#11

                    "( SELECT " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__RATE) +
                    " FROM " + TABLE_NAME_REIMBURSEMENT_CAR_RATES +
                    " WHERE " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID) + " = " +
                                        sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                        " AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID) + " = " +
                                        sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                        " AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " <= " +
                                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
                        " AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO) + " >= " +
                                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
                    " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " DESC, " +
                                    sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_GEN_ROWID) + " DESC " +
                    " LIMIT 1 ) AS ReimbursementRate, " + //#12

                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE_TO) + " AS SecondsTo, " + //#13

                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE_TO) + " - " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) + " AS TripTime " + //#14

            " FROM " + TABLE_NAME_MILEAGE +
                        " JOIN " + TABLE_NAME_EXPENSETYPE +
                            " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__EXPENSETYPE_ID) + "=" +
                                        sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                        " JOIN " + TABLE_NAME_DRIVER +
                            " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DRIVER_ID) + "=" +
                                        sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                        " JOIN " + TABLE_NAME_UOM +
                            " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__UOMLENGTH_ID) + "=" +
                                        sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                        " JOIN " + TABLE_NAME_CAR +
                            " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + "=" +
                                        sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY +
                                " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + "=" +
                                            sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                        " LEFT OUTER JOIN " + TABLE_NAME_TAG +
                            " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__TAG_ID) + "=" +
                                        sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
                        " LEFT OUTER JOIN " + TABLE_NAME_GPSTRACK +
                            " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_GEN_ROWID) + "=" +
                                        sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MILEAGE_ID)
                    + " WHERE 1=1 ";

    //used in exported report
    private static final String mileageListReportSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_GEN_ROWID) + " AS MileageId, " + //#0

                    "DATETIME(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) + ", 'unixepoch', 'localtime') AS " + COL_NAME_MILEAGE__DATE + ", " + //#1

                    "CASE strftime(\"%w\", " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) + ", 'unixepoch', 'localtime') " +
                        "WHEN \"0\" THEN '[#d0]' " +
                        "WHEN \"1\" THEN '[#d1]' " +
                        "WHEN \"2\" THEN '[#d2]' " +
                        "WHEN \"3\" THEN '[#d3]' " +
                        "WHEN \"4\" THEN '[#d4]' " +
                        "WHEN \"5\" THEN '[#d5]' " +
                        "WHEN \"6\" THEN '[#d6]' " +
                    "END AS " + ConstantValues.DAY_OF_WEEK_NAME + ", " + //#2

                    "DATETIME(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE_TO) + ", 'unixepoch', 'localtime') AS " + COL_NAME_MILEAGE__DATE_TO + ", " + //#3

                    "CASE strftime(\"%w\", " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE_TO) + ", 'unixepoch', 'localtime') " +
                        "WHEN \"0\" THEN '[#d0]' " +
                        "WHEN \"1\" THEN '[#d1]' " +
                        "WHEN \"2\" THEN '[#d2]' " +
                        "WHEN \"3\" THEN '[#d3]' " +
                        "WHEN \"4\" THEN '[#d4]' " +
                        "WHEN \"5\" THEN '[#d5]' " +
                        "WHEN \"6\" THEN '[#d6]' " +
                    "END AS " + ConstantValues.DAY_OF_WEEK_NAME + ", " + //#4

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " AS CarName, " + //#5

                    sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " AS DriverName, " + //#6

                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + " AS " + COL_NAME_MILEAGE__INDEXSTART + "_DTypeN, " + //#7

                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " AS " + COL_NAME_MILEAGE__INDEXSTOP + "_DTypeN, " + //#8

                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " - " +
                            sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + " AS Distance_CalcSUM_DTypeN, "  + //#9

                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " AS UomCode, " + //#10

                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + " AS TripType, " + //#11

                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_GEN_USER_COMMENT) + ", " + //#12

                    " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') AS Tag, " + //#13

                    "( SELECT " +
                            sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__RATE) +
                        " FROM " +
                            TABLE_NAME_REIMBURSEMENT_CAR_RATES +
                        " WHERE " +
                            sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID) + " = " +
                                        sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID) + " = " +
                                        sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                            " AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " <= " +

                                         sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
                            " AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO) + " >= " +
                                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
                        " ORDER BY " +
                            sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " DESC, " +
                            sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_GEN_ROWID) + " DESC " +
                        " LIMIT 1" + ") AS ReimbursementRate_DTypeR, " + //#14

                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " || '/' || " +
                            sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " AS '', " + //#15

                    "(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " - " +
                            sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + ") " +
                    " * " +
                    "( SELECT " +
                        sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__RATE) +
                    " FROM " +
                        TABLE_NAME_REIMBURSEMENT_CAR_RATES +
                    " WHERE " +
                        sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                        " AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                        " AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " <= " +
                                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
                        " AND " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO) + " >= " +
                                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) +
                    " ORDER BY " +
                        sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " DESC, " +
                        sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_GEN_ROWID) + " DESC " +
                    " LIMIT 1 " + ") AS ReimbursementValue_CalcSUM_DTypeR, " + //#16

                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " AS '' " + //#17

            " FROM " +
                    TABLE_NAME_MILEAGE +
                        " JOIN " + TABLE_NAME_EXPENSETYPE +
                                    " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__EXPENSETYPE_ID) + "=" +
                                                sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                        " JOIN " + TABLE_NAME_DRIVER +
                                    " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DRIVER_ID) + "=" +
                                                sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                        " JOIN " + TABLE_NAME_UOM +
                                    " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__UOMLENGTH_ID) + "=" +
                                                sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                        " JOIN " + TABLE_NAME_CAR +
                                    " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + "=" +
                                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY +
                                        " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + "=" +
                                                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                        " LEFT OUTER JOIN " + TABLE_NAME_TAG +
                                    " ON " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__TAG_ID) + "=" +
                                                sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID)
            + " WHERE 1=1 ";

    //used in main activity and refuel list activity
    private static final String refuelListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_GEN_ROWID) + ", " + //#0

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " || '; ' || " + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) +
                             " || '; %1$s'" + " AS " + FIRST_LINE_LIST_NAME + ", " + //#1

                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + " || '; %1$s ' || " + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " || " +
                                " CASE " +
                                    "WHEN " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUME_ID) +
                                                " <> " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUMEENTERED_ID) +
                                        " THEN " +
                                            "' (%2$s' || ' ' || " + sqlConcatTableColumn("DefaultVolumeUOM", COL_NAME_UOM__CODE) + " || ')' " +
                                    " ELSE '' " +
                                " END " +
                                " || ' x %3$s ' || " + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " || " +
                                " CASE " +
                                    "WHEN " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCY_ID) +
                                                " <> " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCYENTERED_ID) +
                                        " THEN ' (%4$s ' || " + sqlConcatTableColumn("DefaultCurrency", COL_NAME_CURRENCY__CODE) + " || ')' " +
                                        " ELSE '' " +
                                " END || ' = %5$s ' || " + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " || " +
                                " CASE " +
                                    "WHEN " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCY_ID) +
                                                " <> " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCYENTERED_ID) +
                                        " THEN ' (%6$s ' || " + sqlConcatTableColumn("DefaultCurrency", COL_NAME_CURRENCY__CODE) + " || ')' " +
                                    " ELSE '' " +
                                " END || ' at %7$s ' || " + sqlConcatTableColumn("CarLengthUOM", COL_NAME_UOM__CODE) +
                                " || ' (' || " + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) + " || ')' " + " AS " + SECOND_LINE_LIST_NAME + ", " + //#2

                    " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') || " +
                            sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_GEN_USER_COMMENT) + " || '[#01]' AS " + THIRD_LINE_LIST_NAME + ", " + //#3

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) + " AS Seconds, " + //#4

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITYENTERED) + " AS QtyEntered, " + //#5

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITY) + " AS Qty, " + //#6

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__PRICEENTERED) + " AS PriceEntered, " + //#7

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__PRICE) + " AS Price, " + //#8

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__AMOUNTENTERED) + " AS AmountEntered, " + //#9

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__AMOUNT) + " AS Amount, " + //#10

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__INDEX) + " AS CarIndex, " + //#11

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__ISFULLREFUEL) + ", " + //#12

                    "COALESCE( (SELECT " + COL_NAME_REFUEL__INDEX + " " +
                                " FROM " + TABLE_NAME_REFUEL + " AS pr " +
                                " WHERE 1 = 1 " + WHERE_CONDITION_ISACTIVE +
                                        " AND pr." + COL_NAME_REFUEL__ISFULLREFUEL + " = 'Y' " +
                                        " AND pr." + COL_NAME_REFUEL__CAR_ID + " = " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) +
                                        " AND pr." + COL_NAME_REFUEL__INDEX + " < " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__INDEX) +
                                " ORDER BY pr." + COL_NAME_REFUEL__INDEX + " DESC " +
                                " LIMIT 1 ), -1) AS PreviousFullRefuelIndex," + //#13

                    sqlConcatTableColumn("DefaultVolumeUOM", COL_NAME_UOM__CODE) + " AS CarUOMVolume, " + //#14

                    sqlConcatTableColumn("CarLengthUOM", COL_NAME_UOM__CODE) + " AS CarUOMLength, " + //#15

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + //#16

        " FROM " + TABLE_NAME_REFUEL +
                " JOIN " + TABLE_NAME_EXPENSECATEGORY + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__EXPENSECATEGORY_ID) +
                                    "=" + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_EXPENSETYPE + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__EXPENSETYPE_ID) +
                                    "=" + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_DRIVER + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DRIVER_ID) +
                                    "=" + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_UOM + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUMEENTERED_ID) +
                                    "=" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_UOM + " AS DefaultVolumeUOM ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUME_ID) +
                                "=" + sqlConcatTableColumn("DefaultVolumeUOM", COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_CAR + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_UOM + " AS CarLengthUOM " + " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) +
                                    "=" + sqlConcatTableColumn("CarLengthUOM", COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_CURRENCY + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCYENTERED_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_CURRENCY + " AS DefaultCurrency " + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCY_ID) +
                                "=" + sqlConcatTableColumn("DefaultCurrency", COL_NAME_GEN_ROWID) +
                " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__TAG_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
        " WHERE 1=1 ";

    //used in exported report
    private static final String refuelListReportSelect =
        "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_GEN_ROWID) + " AS RefuelId, " + //#0

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " AS CarName, " + //#1

                    sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " AS DriverName, " + //#2

                    sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) + " AS FuelCategory, " + //#3

                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + " AS FillUpType, " + //#4

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__INDEX) + " AS " + COL_NAME_REFUEL__INDEX + "_DTypeN, " + //#5

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__ISFULLREFUEL) + ", " + //#6

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITY) + " AS " + COL_NAME_REFUEL__QUANTITY + "_DTypeN, " + //#7

                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " AS UOM, " + //#8

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__PRICE) + " AS " + COL_NAME_REFUEL__PRICE + "_DTypeN, " + //#9

                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " AS Currency, " + //#10

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITY) + " * " +
                            sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__PRICE) + " AS Amount_CalcSUM_DTypeN, " + //11

                    "DATETIME(" + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) + ", 'unixepoch', 'localtime') AS Date, " + //#12

                    "CASE strftime(\"%w\", " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) + ", 'unixepoch', 'localtime') " +
                        "WHEN \"0\" THEN '[#d0]' " +
                        "WHEN \"1\" THEN '[#d1]' " +
                        "WHEN \"2\" THEN '[#d2]' " +
                        "WHEN \"3\" THEN '[#d3]' " +
                        "WHEN \"4\" THEN '[#d4]' " +

                        "WHEN \"5\" THEN '[#d5]' " +
                        "WHEN \"6\" THEN '[#d6]' " +
                    "END AS " + ConstantValues.DAY_OF_WEEK_NAME + ", " + //#13

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITYENTERED) + " AS " + COL_NAME_REFUEL__QUANTITYENTERED + "_DTypeN, " + //#14

                    sqlConcatTableColumn("UomVolEntered", COL_NAME_UOM__CODE) + " AS UomEntered, " + //#15

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLCONVERSIONRATE) + ", " + //#16

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__PRICEENTERED) + " AS " + COL_NAME_REFUEL__PRICEENTERED + "_DTypeN, " + //#17

                    sqlConcatTableColumn("CurrencyEntered", COL_NAME_CURRENCY__CODE) + " AS CurrencyEntered, " + //#18

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCYRATE) + " AS " + COL_NAME_REFUEL__CURRENCYRATE + "_DTypeN, " + //#19

                    sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_NAME) + " AS Vendor, " + //#20

                    sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_GEN_NAME) +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__ADDRESS) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__CITY) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__REGION) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__COUNTRY) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__POSTAL) + ", '') "
                        + " AS Location, " + //#21

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_GEN_USER_COMMENT) + ", " + //#22

                    " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') AS Tag, " + //#23

                    " '[#rv1]' AS FuelCons, " + //#24

                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " || '/100' || " + sqlConcatTableColumn("CarLengthUOM", COL_NAME_UOM__CODE) + " AS '', " + //#25

                    " '[#rv2]' AS FuelEff, " + //#26

                    sqlConcatTableColumn("CarLengthUOM", COL_NAME_UOM__CODE) + " || '/' || " + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " AS '', " + //#27

                    " COALESCE(" +
                        "(SELECT " + COL_NAME_REFUEL__INDEX +
                        " FROM " + TABLE_NAME_REFUEL + " AS pr " +
                        " WHERE 1 = 1 " + WHERE_CONDITION_ISACTIVE +
                            " AND pr." + COL_NAME_REFUEL__ISFULLREFUEL + " = 'Y' " +
                            " AND pr." + COL_NAME_REFUEL__CAR_ID + " = " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) +
                            " AND pr." + COL_NAME_REFUEL__INDEX + " < " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__INDEX) +
                        " ORDER BY pr." + COL_NAME_REFUEL__INDEX + " DESC " +
                        " LIMIT 1 " + "), -1) AS PreviousFullRefuelIndex_DoNotExport, " + //#28

                    sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + " AS CarID_DoNotExport " + //#29
        " FROM " + TABLE_NAME_REFUEL +
                " JOIN " + TABLE_NAME_EXPENSETYPE + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__EXPENSETYPE_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_EXPENSECATEGORY + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__EXPENSECATEGORY_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_DRIVER + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DRIVER_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_UOM + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUME_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_UOM + " AS UomVolEntered " +  " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__UOMVOLUMEENTERED_ID) +
                                "=" + sqlConcatTableColumn("UomVolEntered", COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_CAR + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                        " JOIN " + TABLE_NAME_UOM + " AS CarLengthUOM " + " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) +
                                    "=" + sqlConcatTableColumn("CarLengthUOM", COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_CURRENCY + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCY_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                " JOIN " + TABLE_NAME_CURRENCY + " AS CurrencyEntered " + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CURRENCYENTERED_ID) +
                                "=" + sqlConcatTableColumn("CurrencyEntered", COL_NAME_GEN_ROWID) +
                " LEFT OUTER JOIN " + TABLE_NAME_BPARTNER + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__BPARTNER_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_ROWID) +
                " LEFT OUTER JOIN " + TABLE_NAME_BPARTNERLOCATION + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__BPARTNER_LOCATION_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_GEN_ROWID) +
                " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__TAG_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) + " WHERE 1=1 ";

    //used in main activity & list view
    private static final String expenseListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_GEN_ROWID) + ", " + //#0

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " || '; ' || " +
                    sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " || '; ' || " + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) +
                            " || '; %1$s' AS " + FIRST_LINE_LIST_NAME + ", " + //#1

                    sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) + " || '; %1$s ' || " +
                            sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " || " +
                                " CASE " +
                                    " WHEN " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCY_ID) + " <> " +
                                                                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCYENTERED_ID) + " " +
                                        " THEN ' (%2$s ' || " + sqlConcatTableColumn("DefaultCurrency", COL_NAME_CURRENCY__CODE) + " || ')' " +
                                    " ELSE " + "'' " +
                                " END " +
                                " || " +
                                " CASE " +
                                    " WHEN COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__QUANTITY) + ", '') <> '' " +
                                        " THEN ' for %3$s ' || " +
                                            " CASE " +
                                                " WHEN COALESCE (" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__UOM_ID) + ", 0) > 0 " +
                                                    " THEN " + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) +
                                                " ELSE " + "'' " +
                                            " END " +
                                    " ELSE " + "'' " +
                                " END " +
                                " || " +
                                " CASE " +
                                    "WHEN COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + ", '') <> '' " +
                                        " THEN ' at %4$s ' || " + sqlConcatTableColumn("CarDefaultLengthUOM", COL_NAME_UOM__CODE) +
                                    " ELSE ''" +
                                " END AS " +



                    SECOND_LINE_LIST_NAME + ", " + //#2

                    " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') || " +
                                sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_GEN_USER_COMMENT) + " AS " + THIRD_LINE_LIST_NAME + ", " +//#3

                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " AS Second, " + //#4
                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNTENTERED) + " AS AmountEntered, " +//#5
                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + " AS Amount, " + //#6
                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + " AS CarIndex, " + //#7
                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__QUANTITY) + " AS Qty " //#8
            + " FROM " + TABLE_NAME_EXPENSE +
                    " JOIN " + TABLE_NAME_EXPENSETYPE + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__EXPENSETYPE_ID) + "=" +
                                                                        sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_EXPENSECATEGORY + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__EXPENSECATEGORY_ID) + "=" +
                                                                        sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_UOM + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__UOM_ID) + "=" +
                                                                sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_DRIVER + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DRIVER_ID) + "=" +
                                                                sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + "=" +
                                                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                        " JOIN " + TABLE_NAME_UOM + " AS CarDefaultLengthUOM ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) + "=" +
                                                                    sqlConcatTableColumn("CarDefaultLengthUOM", COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CURRENCY + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCYENTERED_ID) + "=" +
                                                                sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CURRENCY + " AS DefaultCurrency " + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCY_ID) + "=" +
                                                                                            sqlConcatTableColumn("DefaultCurrency", COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__TAG_ID) + "=" +
                                                                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE " +
                    "COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMTABLE) + ", '') = ''";

    //used in exported report
    private static final String expensesListReportSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_GEN_ROWID) + " AS ExpenseId, " + //#0

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " AS CarName, " + //#1

                    "DATETIME(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + ", 'unixepoch', 'localtime') AS Date, " + //#2

                    "CASE strftime(\"%w\", " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + ", 'unixepoch', 'localtime') " +
                        "WHEN \"0\" THEN '[#d0]' " +
                        "WHEN \"1\" THEN '[#d1]' " +
                        "WHEN \"2\" THEN '[#d2]' " +
                        "WHEN \"3\" THEN '[#d3]' " +
                        "WHEN \"4\" THEN '[#d4]' " +
                        "WHEN \"5\" THEN '[#d5]' " +
                        "WHEN \"6\" THEN '[#d6]' " +
                    "END AS " + ConstantValues.DAY_OF_WEEK_NAME + ", " + //#3

                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DOCUMENTNO) + ", " + //#4

                    sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) + " AS Category, " + //#5

                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + " AS Type, " + //#6

                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + " AS " + COL_NAME_EXPENSE__AMOUNT + "_CalcSUM_DTypeN, " +//#7

                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + " AS CurrencyCode, " + //#8

                    sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " AS DriverName, " + //#9

                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_GEN_USER_COMMENT) + ", " + //#10

                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMTABLE) + " AS BaseExpense, " + //#11

                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMRECORD_ID) + " AS BaseExpenseId, " + //#12

                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + " AS " + COL_NAME_EXPENSE__INDEX + "_DTypeN, " + //#13

                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNTENTERED) + " AS " + COL_NAME_EXPENSE__AMOUNTENTERED + "_DTypeN, " + //#14

                    sqlConcatTableColumn("CurrEntered", COL_NAME_CURRENCY__CODE) + " AS CurrencyEnteredCode, " + //#15

                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCYRATE) + " AS " + COL_NAME_EXPENSE__CURRENCYRATE + "_DTypeN, " + //#16

                    sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_NAME) + " AS Vendor, " + //#17

                    sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_GEN_NAME) +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__ADDRESS) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__CITY) + ", '') " + " " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__REGION) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__COUNTRY) + ", '') " +
                        " || COALESCE( '; ' || " + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__POSTAL) + ", '') " + " AS Location, " + //#18

                    " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') AS Tag " + //#19
            " FROM " +
                    TABLE_NAME_EXPENSE +
                    " JOIN " + TABLE_NAME_EXPENSETYPE + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__EXPENSETYPE_ID) +
                                    "=" + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_EXPENSECATEGORY + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__EXPENSECATEGORY_ID) +
                                    "=" + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_DRIVER + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DRIVER_ID) +
                                    "=" + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) +
                                    "=" + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CURRENCY + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCY_ID) +
                                    "=" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CURRENCY + " AS CurrEntered " + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CURRENCYENTERED_ID) +
                                    "=" + sqlConcatTableColumn("CurrEntered", COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_BPARTNER + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__BPARTNER_ID) +
                                    "=" + sqlConcatTableColumn(TABLE_NAME_BPARTNER, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_BPARTNERLOCATION + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__BPARTNER_LOCATION_ID) +
                                    "=" + sqlConcatTableColumn(TABLE_NAME_BPARTNERLOCATION, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__TAG_ID) +
                                    "=" + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
                    " WHERE " +
                        "COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMTABLE) + ", '') = '' ";

    //used in main activity and GPS Track list activity
    private static final String gpsTrackListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GEN_ROWID) + ", " + //#0

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " || '; ' || " + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) +
                            " || '; ' || COALESCE( " + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + ", '') " +
                                " || '; ' || '%1$s' AS " + FIRST_LINE_LIST_NAME + ", " + //#1

                    "'%1$s ' || ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DISTANCE) + ", " + ConstantValues.DECIMALS_LENGTH + ") || ' ' || " +
                            sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " || '; ' || '%2$s ' || ROUND("  + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MAXSPEED) + ", 2) || ' ' || " +
                            sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " || '/h; ' || '%3$s ' || ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__AVGSPEED) + ", 2) || ' ' || " +
                            sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " || '/h; ' || '%4$s ' || ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__AVGMOVINGSPEED) + ", 2) || ' ' || " +
                            sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + " || '/h; ' || '%5$s ' || '; ' || '%6$s ' || '; ' || '%13$s ' || '; ' || " +
                                "'%12$s ' || '; ' || '%7$s ' || ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MINACCURACY) + ", 2) || " +
                                " CASE " +
                                    "WHEN UPPER(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") == 'KM' " +
                                        " THEN ' m; ' " +
                                    " ELSE ' yd; ' " +
                                " END || '%8$s ' || ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MAXACCURACY) + ", 2) || " +
                                " CASE " +
                                    "WHEN UPPER(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") == 'KM' " +
                                        " THEN ' m; ' " +
                                    " ELSE ' yd; ' " +
                                " END || '%9$s ' || ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__AVGACCURACY) + ", 2) || " +
                                " CASE " +
                                    "WHEN UPPER(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") == 'KM' " +
                                        " THEN ' m; ' " +
                                    " ELSE ' yd; ' " +
                                " END || '%10$s ' || " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MINALTITUDE) + " || " +
                                " CASE " +
                                    "WHEN UPPER(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") == 'KM' " +
                                        " THEN ' m; ' " +
                                    " ELSE ' yd; ' " +
                                " END || '%11$s ' || " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MAXALTITUDE) + " || " +
                                " CASE " +
                                    "WHEN UPPER(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") == 'KM' " +
                                        " THEN ' m; ' " +
                                    " ELSE ' yd; ' " +
                                " END " + " AS " + SECOND_LINE_LIST_NAME + ", " + //#2

                    " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') || " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GEN_NAME) +
                            " || '; ' || " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GEN_USER_COMMENT) + " AS " + THIRD_LINE_LIST_NAME + ", " + //#3

                    "ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TOTALTIME) + ", 2) AS " + FOURTH_LINE_LIST_NAME + ", " + //#4

                    "ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MOVINGTIME) + ", 2) AS " + FIFTH_LINE_LIST_NAME + ", " + //#5

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GEN_NAME) + ", " + //#6

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DATE) + " AS Seconds, " + //#7

                    "ROUND(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TOTALPAUSETIME) + ", 2) AS " + COL_NAME_GPSTRACK__TOTALPAUSETIME + //#8
            " FROM " + TABLE_NAME_GPSTRACK +
                    " JOIN " + TABLE_NAME_DRIVER + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DRIVER_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__CAR_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_UOM + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TAG_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_EXPENSETYPE + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__EXPENSETYPE_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                    //exclude the track in progress (the no. of trackpoints is updated after terminating the tracking)
            " WHERE " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TOTALTRACKPOINTS) + " IS NOT NULL ";

    //used in exported report
    private static final String gpsTrackListReportSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GEN_ROWID) + " AS TrackId, " + //#0

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " AS CarName, " + //#1

                    sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + " AS DriverName, " + //#2

                    "DATETIME(" + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DATE) + ", 'unixepoch', 'localtime') AS Date, " + //#3

                    "CASE strftime(\"%w\", " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DATE) + ", 'unixepoch', 'localtime') " +
                        "WHEN \"0\" THEN '[#d0]' " +
                        "WHEN \"1\" THEN '[#d1]' " +
                        "WHEN \"2\" THEN '[#d2]' " +
                        "WHEN \"3\" THEN '[#d3]' " +
                        "WHEN \"4\" THEN '[#d4]' " +
                        "WHEN \"5\" THEN '[#d5]' " +
                        "WHEN \"6\" THEN '[#d6]' " +
                    "END AS " + ConstantValues.DAY_OF_WEEK_NAME + ", " + //#4

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MAXACCURACY) + " AS " + COL_NAME_GPSTRACK__MINACCURACY + ", " + //#5

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MINACCURACY) + " AS " + COL_NAME_GPSTRACK__MAXACCURACY + ", " + //#6

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__AVGACCURACY) + ", " + //#7

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DISTANCE) + ", " + //#8

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MAXSPEED) + ", " + //#9

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__AVGMOVINGSPEED) + ", " + //#10

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__AVGSPEED) + ", " + //#11

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MAXALTITUDE) + ", " + //#12

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MINALTITUDE) + ", " + //#13

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TOTALTIME) + " AS '" + COL_NAME_GPSTRACK__TOTALTIME + " [s]', " + //#14

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MOVINGTIME) + " AS '" + COL_NAME_GPSTRACK__MOVINGTIME + " [s]', " + //#15

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TOTALPAUSETIME) + " AS '" + COL_NAME_GPSTRACK__TOTALPAUSETIME + " [s]', " + //#16

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TOTALTRACKPOINTS) + ", " + //#17

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__INVALIDTRACKPOINTS) + ", " + //#18

                    sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__MILEAGE_ID) + ", " + //#19

                    " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + " || '; ', '') AS Tag " //#20
            + " FROM " + TABLE_NAME_GPSTRACK +
                    " JOIN " + TABLE_NAME_DRIVER + " ON " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DRIVER_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR + " ON " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__CAR_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__TAG_ID) +
                                "=" + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1=1 ";

    ////used in main activity and to-do list activity
    private static final String todoListViewSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_GEN_ROWID) + ", " + // #0

                    "'[#1]' || " + sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_NAME) + " || " + //type - GEN_TypeLabel
                        "'[#2]' || " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_NAME) + " || '; ' || " + //task - GEN_TaskLabel
                        " CASE " +
                            " WHEN " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " IS NOT NULL " +
                                " THEN " + "'[#3] ' || COALESCE(" + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + ", '') || '; ' " + //car - GEN_CarLabel
                            " ELSE '' " +
                        " END || " + "'[#4] ' || " + //task status - GEN_StatusLabel
                        " CASE " +
                            " WHEN " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__ISDONE) + " == 'Y' " +
                                " THEN '[#15]' " + //done - ToDo_DoneLabel
                            " WHEN (" + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + TaskEditFragment.TASK_SCHEDULED_FOR_TIME + "' " +
                                        " OR " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + TaskEditFragment.TASK_SCHEDULED_FOR_BOTH + "' )" +
                                    " AND " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + " < strftime('%s','now') " +
                                " THEN '[#5]' " + //overdue - Todo_OverdueLabel date
                            " WHEN (" + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE + "' " +
                                        " OR " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + TaskEditFragment.TASK_SCHEDULED_FOR_BOTH + "' )" +
                                    " AND " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " < " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) +
                                " THEN '[#5]' " + //overdue - Todo_OverdueLabel mileage
                            " ELSE '[#6]' " + //scheduled - Todo_ScheduledLabel
                        " END AS " + FIRST_LINE_LIST_NAME + ", " + //datetime(task_todo.DueDate, 'unixepoch', 'localtime') // #1

                    " CASE " +
                        " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + TaskEditFragment.TASK_SCHEDULED_FOR_TIME + "' " +
                    " THEN '[#7] [#8]' " + //due date label/ToDo_ScheduledDateLabel + date
                        " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE + "' " +
                                " THEN '[#10] [#11] ' || COALESCE (" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ", '') || " + " ' ([#13] [#14])' " + //duemileage label/ToDo_ScheduledMileageLabel + mileage + (estimated date)
                        " ELSE '[#7] [#8] [#9] [#11] ' || COALESCE (" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ", '') || " + "' ([#13] [#14])' " +
                    " END AS " + SECOND_LINE_LIST_NAME + ", " + // #2

                    " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_GEN_USER_COMMENT) + ", '') AS " + THIRD_LINE_LIST_NAME + ", " + // #3

                    sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", " + // #4

                    sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + ", " + // #5

                    sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + ", " + // #6

                    " CASE " + " " +
                        " WHEN Minimums.Mileage IS NOT NULL " +
                            " THEN " +
                                    "( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) +
                                        " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) +
                                    ") " + //mileage until the to-do
                                    " / " +
                                    "( " + //avg. daily mileage
                                        "( " +
                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage " +
                                        ") " +
                                        "/ " +
                                        "(" +
                                            "strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) " +
                                        ") " +
                                    ") " +
                        " ELSE 99999999999 " +
                    " END " + " AS EstDaysUntilDueMileage, " + //Estimated days until the due mileage #7

                    "( COALESCE(strftime('%J', datetime(" + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) " +
                            " -  strftime('%J','now', 'localtime') ) AS DaysUntilDueDate, " + // #8

                    " CASE " +
                        " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + TaskEditFragment.TASK_SCHEDULED_FOR_BOTH + "' " +
                                        " AND Minimums.Mileage IS NOT NULL " +
                                        " AND Minimums.Date IS NOT NULL " +
                                        " AND " +
                                            "( " +
                                                "( " +
                                                    "( " +
                                                        sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) +
                                                            " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) +
                                                    ") " + //mileage until the to-do
                                                    "/ " +
                                                    "( " +
                                                        "( " +
                                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage " +
                                                        ") " +
                                                        "/ " +
                                                        "( " +
                                                            "strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) " +
                                                        ") " +
                                                    ") " +
                                                ") " +
                                                " < " +
                                                "( " +
                                                    "COALESCE(strftime('%J', datetime(" + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) " +
                                                        " - strftime('%J','now', 'localtime') " +
                                                ") " +
                                            ") " +
                            " THEN " +
                                "( " +
                                    "( " +
                                        sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) +
                                    ") " + //no of mileages until the to-do
                                    "/ " +
                                    "( " +
                                        "( " +
                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage " +
                                        ") " +
                                        "/ " +
                                        "( " +
                                            "strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) " +
                                        ") " +
                                    ") " +
                                ") " +
                        " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + TaskEditFragment.TASK_SCHEDULED_FOR_BOTH + "' " +
                                        "AND Minimums.Mileage IS NOT NULL " +
                                        "AND Minimums.Date IS NOT NULL " +
                                        "AND " +
                                            "( " +
                                                "( " +
                                                    "( " +
                                                        sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) +
                                                    ") " + //no of mileages until the to-do
                                                    "/ " +
                                                    "( " +
                                                        "( " +
                                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage " +
                                                        ") " +
                                                        "/ " +
                                                        "( " +
                                                            "strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) " +
                                                        ") " +
                                                    ") " +
                                                ") " +
                                                " > " +
                                                "( " +
                                                    "COALESCE(strftime('%J', datetime(" + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) " +
                                                        " - strftime('%J','now', 'localtime') " +
                                                ") " +
                                            ") " +
                            " THEN " +
                                "( COALESCE(strftime('%J', datetime(" + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) " +
                                    " - strftime('%J','now', 'localtime') ) " +
                        " WHEN " +
                                "(" +
                                    sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + TaskEditFragment.TASK_SCHEDULED_FOR_BOTH + "' " +
                                        " OR " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE + "' " +
                                ") " +
                                " AND  Minimums.Mileage IS NULL " +
                            " THEN 99999999999 " +
                        " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE + "' " +
                                        " AND Minimums.Mileage IS NOT NULL " +
                            " THEN " +
                                "( " +
                                    "( " +
                                        sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) +
                                    ") " + //mileage until the to-do
                                    "/" +
                                    "( " +
                                        "( " +
                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage " +
                                        ") " +
                                        "/ " +
                                        "( " +
                                            "strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) " +
                                        ") " +
                                    ") " +
                                ") " +
                        " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + TaskEditFragment.TASK_SCHEDULED_FOR_TIME + "' " +
                            " THEN " +
                                "( " +
                                    "COALESCE(strftime('%J', datetime(" + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) " +
                                        " - strftime('%J','now', 'localtime') " +
                                ") " +
                    " END AS EstDueDays, " + //#9

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " AS CarName, " + //#10

                    sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_ROWID) + " AS TaskID, " + //#11

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " AS CarCurrentIndex, " + //#12

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + " AS CarId " + //#13
            " FROM "
                    + TABLE_NAME_TODO +
                    " JOIN " + TABLE_NAME_TASK + " ON " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__TASK_ID) +

                                    " = " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_TASKTYPE + " ON " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__TASKTYPE_ID) +
                                    " = " + sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_CAR + " ON " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__CAR_ID) +
                                    " = " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_UOM + " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) +
                                    " = " + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " +
                            "( " +
                                "SELECT " +
                                    "MIN(Date) AS Date, " +
                                    "MIN(Mileage) AS Mileage, " +
                                    "CAR_ID " +
                                "FROM " +
                                    "( " +
                                        " SELECT " +
                                            "MIN(" + COL_NAME_MILEAGE__DATE + ") AS Date, " +
                                            "MIN(" + COL_NAME_MILEAGE__INDEXSTART + ") AS Mileage, " +
                                            COL_NAME_MILEAGE__CAR_ID + " AS CAR_ID " +
                                        " FROM " + TABLE_NAME_MILEAGE +
                                        " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' " +
                                        " GROUP BY " + COL_NAME_MILEAGE__CAR_ID +
                                        " UNION " +
                                        " SELECT " +
                                            "MIN(" + COL_NAME_REFUEL__DATE + ") AS Date, " +
                                            "MIN(" + COL_NAME_REFUEL__INDEX + ") AS Mileage, " +
                                            COL_NAME_REFUEL__CAR_ID + " AS CAR_ID " +
                                        " FROM " + TABLE_NAME_REFUEL +
                                        " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' " +
                                        " GROUP BY " + COL_NAME_REFUEL__CAR_ID +
                                        " UNION " +
                                        " SELECT " +
                                            "MIN(" + COL_NAME_EXPENSE__DATE + ") AS Date, " +
                                            " MIN(" + COL_NAME_EXPENSE__INDEX + ") AS Mileage, " +
                                            COL_NAME_EXPENSE__CAR_ID + " AS CAR_ID " +
                                        " FROM " + TABLE_NAME_EXPENSE +
                                        " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' " +
                                        " GROUP BY " + COL_NAME_EXPENSE__CAR_ID +
                                    " ) " +
                                " GROUP BY CAR_ID ) AS Minimums " +
                                    "ON Minimums.CAR_ID = " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__CAR_ID) +
            " WHERE 1=1 ";

    //used in exported reports
    private static final String todoListReportSelect =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_GEN_ROWID) + " AS ToDoID, " + //#0

                    sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_NAME) + " AS TaskType, " + //#1

                    sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_NAME) + " AS Task, " + //#2

                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + " AS Car, " + //#3

                    " CASE " +
                        " WHEN " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__ISDONE) + " == 'Y' " +
                            " THEN '[#TDR1]' " + //done - ToDo_DoneLabel
                        " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + TaskEditFragment.TASK_SCHEDULED_FOR_TIME + "' " +
                                " AND " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + " < strftime('%s','now') " +
                            " THEN '[#TDR2]' " + //overdue - Todo_OverdueLabel
                        " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE + "' " +
                                " AND " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " < " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) +
                            " THEN '[#TDR2]' " + //overdue - Todo_OverdueLabel
                        " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " == '" + TaskEditFragment.TASK_SCHEDULED_FOR_BOTH + "' " +
                                " AND " +
                                    "( " +
                                        sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " < " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) +
                                    " OR " +
                                        sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + " < strftime('%s','now')" +
                                    ") " +
                            " THEN '[#TDR2]' " + //overdue - Todo_OverdueLabel
                        " ELSE '[#TDR3]' " + //scheduled - Todo_ScheduledLabel
                    " END AS Status, " + //#4

                    " CASE " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) +
                        " WHEN '" + TaskEditFragment.TASK_SCHEDULED_FOR_TIME + "' " +
                            " THEN '[#TDR4]'" + //time
                        " WHEN '" + TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE + "' " +
                            " THEN '[#TDR5]'" + //mileage
                        " ELSE '[#TDR6]'" +
                    " END AS ScheduledFor, " + //#5

                    sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + " AS ScheduledDate_DTypeD, " + //#6

                    sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " AS ScheduledMileage_DTypeN, " + //#7

                    " CASE " +
                        " WHEN Minimums.Mileage IS NOT NULL " +
                            " THEN " +
                                "( " +
                                    sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) +
                                ") " + //no of mileages until the to-do
                                "/ " +
                                "( " +
                                    //avg. daily mileage
                                    "( " +
                                        sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage " +
                                    ") " +
                                    "/ " +
                                    "( " +
                                        "strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) " +
                                    ") " +
                                ") " +
                        " ELSE 99999999999 " +
                    " END " + " AS EstimatedScheduledMileageDate_DTypeL, " + //#8 Estimated days until the due mileage

                    " CASE " +
                            " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + TaskEditFragment.TASK_SCHEDULED_FOR_BOTH + "' " +
                                        " AND Minimums.Mileage IS NOT NULL " +
                                        " AND Minimums.Date IS NOT NULL " +
                                        " AND " +
                                            "( " +
                                                "( " +
                                                    "( " +
                                                        sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) +
                                                            " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) +
                                                    ") " + //no of mileages until the to-do
                                                    "/ " +
                                                    "(" +
                                                        "( " +
                                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage " +
                                                        ") " +
                                                        "/ " +
                                                        "( " +
                                                            "strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) " +
                                                        ") " +
                                                    ") " +
                                                ") " +
                                                " < " +
                                                "( " +
                                                    " COALESCE(strftime('%J', datetime(" + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) " +
                                                        " - " +
                                                    " strftime('%J','now', 'localtime') " +
                                                ") " +
                                            ") " +
                                " THEN " +
                                    "( " +
                                        "( " +
                                            sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) + " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) +
                                        ") " + //no of mileages until the to-do
                                        "/ " +
                                        "( " +
                                            "( " +
                                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage " +
                                            ") " +
                                            "/ " +
                                            "( " +
                                                "strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) " +
                                            ") " +
                                        ") " +
                                    ") " +
                            " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + TaskEditFragment.TASK_SCHEDULED_FOR_BOTH + "' " +
                                        " AND Minimums.Mileage IS NOT NULL " +
                                        " AND Minimums.Date IS NOT NULL " +
                                        " AND " +
                                            "( " +
                                                "( " +
                                                    "( " +
                                                        sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) +
                                                            " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) +
                                                    ") " + //no of mileages until the to-do
                                                    "/ " +
                                                    "(" +
                                                        "( " +
                                                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage " +
                                                        ") " +
                                                        "/ " +
                                                        "( " +
                                                            "strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) " +
                                                        ") " +
                                                    ") " +
                                                ") " +
                                                " > " +
                                                "( " +
                                                    " COALESCE(strftime('%J', datetime(" + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) " +
                                                        " - " + " strftime('%J','now', 'localtime') " +
                                                ") " +
                                            ") " +
                                    " THEN " +
                                        "( " +
                                            " COALESCE(strftime('%J', datetime(" + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) " +
                                                " - " +
                                            " strftime('%J','now', 'localtime') " +
                                        ") " +
                            " WHEN " +
                                    "(" +
                                        sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + TaskEditFragment.TASK_SCHEDULED_FOR_BOTH + "' " +
                                        " OR " +
                                        sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE + "'" +
                                    ") " +
                                    " AND " +
                                    " Minimums.Mileage IS NULL " +
                                " THEN 99999999999 " +
                            " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + TaskEditFragment.TASK_SCHEDULED_FOR_MILEAGE + "' " +
                                        " AND Minimums.Mileage IS NOT NULL " +
                                " THEN " +
                                    "( " +
                                        "( " +
                                            sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE) +
                                                " - " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) +
                                        ") " + //no of mileages until the to-do
                                        "/ " +
                                        "(" +
                                            "( " +
                                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + " - Minimums.Mileage " +
                                            ") " +
                                            "/ " +
                                            "( " +
                                                "strftime('%J','now', 'localtime') - COALESCE(strftime('%J', datetime(Minimums.Date, 'unixepoch'), 'localtime'), 0) " +
                                            ") " +
                                        ") " +
                                    ") " +
                            " WHEN " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__SCHEDULEDFOR) + " = '" + TaskEditFragment.TASK_SCHEDULED_FOR_TIME + "' " +
                                " THEN " +
                                    "( " +
                                        " COALESCE(strftime('%J', datetime(" + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEDATE) + ", 'unixepoch'), 'localtime'), 0) " +
                                            " - " + " strftime('%J','now', 'localtime') " +
                                    ") " +
                    " END AS EstimatedDueDate_DTypeL, " + //#9

                    " COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_GEN_USER_COMMENT) + ", '') AS Description "  + //#10
            " FROM " + TABLE_NAME_TODO +
                    " JOIN " + TABLE_NAME_TASK + " ON " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__TASK_ID) +
                                " = " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_TASKTYPE + " ON " + sqlConcatTableColumn(TABLE_NAME_TASK, COL_NAME_TASK__TASKTYPE_ID) +
                                " = " + sqlConcatTableColumn(TABLE_NAME_TASKTYPE, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_CAR + " ON " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__CAR_ID) +
                                " = " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_UOM + " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) +
                                " = " + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " +
                            "( " +
                                " SELECT " +
                                    "MIN(Date) AS Date, " +
                                    "MIN(Mileage) AS Mileage, " +
                                    "CAR_ID " +
                                " FROM " +
                                    " (" +
                                        " SELECT " +
                                            "MIN(" + COL_NAME_MILEAGE__DATE + ") AS Date, " +
                                            "MIN(" + COL_NAME_MILEAGE__INDEXSTART + ") AS Mileage, " +
                                            COL_NAME_MILEAGE__CAR_ID + " AS CAR_ID " +
                                        " FROM " + TABLE_NAME_MILEAGE +
                                        " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' " +
                                        " GROUP BY " + COL_NAME_MILEAGE__CAR_ID +
                                        " UNION " +
                                        " SELECT " +
                                            "MIN(" + COL_NAME_REFUEL__DATE + ") AS Date, " +
                                            "MIN(" + COL_NAME_REFUEL__INDEX + ") AS Mileage, " +
                                            COL_NAME_REFUEL__CAR_ID + " AS CAR_ID " +
                                        " FROM " + TABLE_NAME_REFUEL +
                                        " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' " +
                                        " GROUP BY " + COL_NAME_REFUEL__CAR_ID +
                                        " UNION " +
                                        " SELECT " +
                                            "MIN(" + COL_NAME_EXPENSE__DATE + ") AS Date, " +
                                            "MIN(" + COL_NAME_EXPENSE__INDEX + ") AS Mileage, " +
                                            COL_NAME_EXPENSE__CAR_ID + " AS CAR_ID " +
                                        " FROM " + TABLE_NAME_EXPENSE +
                                        " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' " +
                                        " GROUP BY " + COL_NAME_EXPENSE__CAR_ID +
                                    " ) " +
                                " GROUP BY CAR_ID ) AS Minimums " +
                        "ON Minimums.CAR_ID = " + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__CAR_ID) +
            " WHERE 1=1 ";

    private final String statisticsMainViewSelect =
            "SELECT " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + ", " + //#0
                " COALESCE( " + sqlConcatTableColumn("CarIndex", "CarMinIndex") + ", " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXSTART) + "), " + //#1
                " COALESCE( " + sqlConcatTableColumn("CarIndex", "CarMaxIndex") + ", " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__INDEXCURRENT) + "), " + //#2
                sqlConcatTableColumn("UomLength", COL_NAME_UOM__CODE) + " AS UOMLength, " + //#3
                sqlConcatTableColumn("TotalExpenses", "Expense") + " AS TotalExpense, " + //#4
                sqlConcatTableColumn("TotalMileageExpenses", "Expense") + " AS TotalMileageExpense, " + //#5
                sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ", " + //#6
                sqlConcatTableColumn("UomVolume", COL_NAME_UOM__CODE) + " AS UOMVolume " + //#7

            " FROM " +
                    TABLE_NAME_CAR + " " +
                        "JOIN " + TABLE_NAME_UOM + " AS UomLength " + " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) +
                                        "=" + sqlConcatTableColumn("UomLength", COL_NAME_GEN_ROWID) +
                        " JOIN " + TABLE_NAME_UOM + " AS UomVolume " + " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMVOLUME_ID) +
                                        "=" + sqlConcatTableColumn("UomVolume", COL_NAME_GEN_ROWID) +
                        " JOIN " + TABLE_NAME_CURRENCY + " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) +
                                        "=" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                        //total expenses
                        " LEFT OUTER JOIN ( SELECT " +
                                                "SUM( " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + ") AS Expense, " +
                                                COL_NAME_EXPENSE__CAR_ID + " " +
                                            " FROM " + TABLE_NAME_EXPENSE + " " +
                                            " WHERE " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_GEN_ISACTIVE) + " = 'Y' " +
                                                    "mExpenseStatisticsPeriodCondition" +
                                            " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " ) AS TotalExpenses " +
                                " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + "=" +
                                    sqlConcatTableColumn("TotalExpenses", COL_NAME_EXPENSE__CAR_ID) +

                        //total expenses for mileage cost (exclude exp. category which have "Is exclude from mileage cost" attribute
                        " LEFT OUTER JOIN ( SELECT SUM( " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + ") AS Expense, " +
                                                COL_NAME_EXPENSE__CAR_ID + " " +
                                            " FROM " + TABLE_NAME_EXPENSE +
                                                " JOIN " + TABLE_NAME_EXPENSECATEGORY + " ON " +
                                                    sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__EXPENSECATEGORY_ID) + " = " +
                                                        sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                                            " WHERE " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_GEN_ISACTIVE) + " = 'Y' " +
                                                " AND " + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_EXPENSECATEGORY__ISEXCLUDEFROMMILEAGECOST) + " = 'N' " +
                                                    "mExpenseStatisticsPeriodCondition" +
                                            " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " ) AS TotalMileageExpenses " +
                                " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + "=" + sqlConcatTableColumn("TotalMileageExpenses", COL_NAME_EXPENSE__CAR_ID) +
                        " JOIN ( SELECT MIN (CarMinIndex) AS CarMinIndex, MAX (CarMaxIndex) AS CarMaxIndex, MCarID " +
                                            " FROM " +
                                                    "( SELECT MIN( " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + ") AS CarMinIndex, " +
                                                            "MAX( " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + ") AS CarMaxIndex, " +
                                                            sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + " AS MCarID " +
                                                    " FROM " + TABLE_NAME_MILEAGE +
                                                    " WHERE 1=1 " + "mMinIndexStatisticsPeriodCondition" +
                                                    " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) +
                                                    " UNION " +
                                                    " SELECT MIN( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__INDEX) + "),  " +
                                                            "MAX( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__INDEX) + "), " +
                                                            sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) +
                                                    " FROM " + TABLE_NAME_REFUEL +
                                                    " WHERE 1=1 " + "mMinIndexStatisticsPeriodCondition" +
                                                    " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) +
                                                    " UNION " +
                                                    " SELECT MIN( " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + "),  " +
                                                            "MAX( " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + "), " +
                                                            sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) +
                                                    " FROM " + TABLE_NAME_EXPENSE +
                                                    " WHERE " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + " IS NOT NULL " +
                                                            " AND " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + " <> '' " +
                                                            "mMinIndexStatisticsPeriodCondition" +
                                                    " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + ") " +
                                            " GROUP BY MCarID " + ") AS CarIndex " +
                                " ON " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) + "=" + sqlConcatTableColumn("CarIndex", "MCarID") +
            " WHERE 1=1 ";

    //statistics selects
    private String listStatisticsMileageTotal =
            "SELECT " +
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " - " +
                                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + "), " + //#0 - total mileage
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") " + //#1 uom
            " FROM " + TABLE_NAME_MILEAGE +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_UOM + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1 = 1 #WhereConditions# " +
            " ORDER BY 1 DESC";

    private String listStatisticsMileageByType =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + ", " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " - " +
                                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + "), " + //#1 - total mileage
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") " + //#2 uom
            " FROM " + TABLE_NAME_MILEAGE +
                    " JOIN " + TABLE_NAME_EXPENSETYPE + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__EXPENSETYPE_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_UOM + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1 = 1 #WhereConditions# " +
            " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) +
            " ORDER BY 2 DESC";

    private String listStatisticsMileageByTag =
            "SELECT " +
                    "COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + ", 'N/A') , " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " - " +
                                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + "), " + //#1 - total mileage
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") " + //#2 uom
            " FROM " + TABLE_NAME_MILEAGE +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_UOM + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1 = 1 #WhereConditions# " +
            " GROUP BY COALESCE(" + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + ", 'N/A')" +
            " ORDER BY 2 DESC";

    private String listStatisticsMileageByDriver =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + ", " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " - " +
                                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + "), " + //#1 - total mileage
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") " + //#2 uom
            " FROM " + TABLE_NAME_MILEAGE +
                    " JOIN " + TABLE_NAME_DRIVER + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DRIVER_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_UOM + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1 = 1 #WhereConditions# " +
            " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) +
            " ORDER BY 2 DESC";

    private String listStatisticsMileageByCars =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + ", " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " - " +
                                    sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + "), " + //#1 - total mileage
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_UOM__CODE) + ") " + //#2 uom
            " FROM " + TABLE_NAME_MILEAGE +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_UOM + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMLENGTH_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1 = 1 #WhereConditions# " +
            " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) +
            " ORDER BY 2 DESC";

    private String listStatisticsRefuelTotal =
            "SELECT " +
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITY) + "), " + //#0 - total quantity
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__AMOUNT) + "), " + //#1 - total value
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_NAME) + "), " + //#2 uom volume
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ") " + //#3 currency
            " FROM " + TABLE_NAME_REFUEL +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_UOM + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMVOLUME_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1 = 1 #WhereConditions# " +
            " ORDER BY 1 DESC";

    private String listStatisticsRefuelByType =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + ", " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITY) + "), " + //#1 - total quantity
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_NAME) + "), " + //#2 uom
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__AMOUNT) + "), " + //#3 - total amount
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ") " + //#4 currency
            " FROM " + TABLE_NAME_REFUEL +
                    " JOIN " + TABLE_NAME_EXPENSETYPE + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__EXPENSETYPE_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_UOM + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMVOLUME_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1 = 1 #WhereConditions# " +
            " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) +
            " ORDER BY 2 DESC";

    private String listStatisticsRefuelByFuelType =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) + ", " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITY) + "), " + //#1 - total quantity
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_NAME) + "), " + //#2 uom
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__AMOUNT) + "), " + //#3 - total amount
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ") " + //#4 currency
            " FROM " + TABLE_NAME_REFUEL +
                    " JOIN " + TABLE_NAME_EXPENSECATEGORY + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__EXPENSECATEGORY_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_UOM + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMVOLUME_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1 = 1 #WhereConditions# " +
            " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) +
            " ORDER BY 2 DESC";

    private String listStatisticsRefuelByTag =
            "SELECT " +
                    "COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + ", 'N/A') , " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITY) + "), " + //#1 - total quantity
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_NAME) + "), " + //#2 uom
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__AMOUNT) + "), " + //#3 - total amount
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ") " + //#4 currency
            " FROM " + TABLE_NAME_REFUEL +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_UOM + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMVOLUME_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1 = 1 #WhereConditions# " +
            " GROUP BY COALESCE(" + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + ", 'N/A')" +
            " ORDER BY 2 DESC";

    private String listStatisticsRefuelByDriver =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + ", " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITY) + "), " + //#1 - total quantity
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_NAME) + "), " + //#2 uom
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__AMOUNT) + "), " + //#3 - total amount
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ") " + //#4 currency
            " FROM " + TABLE_NAME_REFUEL +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_UOM + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMVOLUME_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_DRIVER + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DRIVER_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1 = 1 #WhereConditions# " +
            " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) +
            " ORDER BY 2 DESC";

    private String listStatisticsRefuelByCars =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + ", " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__QUANTITY) + "), " + //#1 - total quantity
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_NAME) + "), " + //#2 uom
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__AMOUNT) + "), " + //#3 - total amount
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ") " + //#4 currency
            " FROM " + TABLE_NAME_REFUEL +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_UOM + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__UOMVOLUME_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_UOM, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE 1 = 1 #WhereConditions# " +
            " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) +
            " ORDER BY 2 DESC";

    private String listStatisticsExpenseTotal =
            "SELECT " +
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + "), " + //#0 - total value
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ") " + //#1 currency
            " FROM " + TABLE_NAME_EXPENSE +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMTABLE) + ", '') = '' #WhereConditions# " +
            " ORDER BY 1 DESC";

    private String listStatisticsExpenseByType =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) + ", " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + "), " + //#1 - total amount
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ") " + //#2 currency
            " FROM " + TABLE_NAME_EXPENSE +
                    " JOIN " + TABLE_NAME_EXPENSETYPE + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_REFUEL__EXPENSETYPE_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMTABLE) + ", '') = '' #WhereConditions# " +
            " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) +
            " ORDER BY 2 DESC";

    private String listStatisticsExpenseByCategory =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) + ", " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + "), " + //#1 - total amount
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ") " + //#2 currency
            " FROM " + TABLE_NAME_EXPENSE +
                    " JOIN " + TABLE_NAME_EXPENSECATEGORY + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__EXPENSECATEGORY_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMTABLE) + ", '') = '' #WhereConditions# " +
            " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) +
            " ORDER BY 2 DESC";

    private String listStatisticsExpenseByTag =
            "SELECT " +
                    "COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + ", 'N/A') , " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + "), " + //#1 - total amount
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ") " + //#2 currency
            " FROM " + TABLE_NAME_EXPENSE +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMTABLE) + ", '') = '' #WhereConditions# " +
            " GROUP BY COALESCE(" + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + ", 'N/A')" +
            " ORDER BY 2 DESC";

    private String listStatisticsExpenseByDriver =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + ", " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + "), " + //#1 - total amount
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ") " + //#2 currency
            " FROM " + TABLE_NAME_EXPENSE +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " JOIN " + TABLE_NAME_DRIVER + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DRIVER_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMTABLE) + ", '') = '' #WhereConditions# " +
            " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) +
            " ORDER BY 2 DESC";

    private String listStatisticsExpenseByCars =
            "SELECT " +
                    sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) + ", " + //#0
                    " SUM( " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + "), " + //#1 - total amount
                    " MAX(" + sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_CURRENCY__CODE) + ") " + //#2 currency
            " FROM " + TABLE_NAME_EXPENSE +
                    " JOIN " + TABLE_NAME_CAR + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_ROWID) +
                            " JOIN " + TABLE_NAME_CURRENCY + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_CAR__CURRENCY_ID) + " = " +
                                    sqlConcatTableColumn(TABLE_NAME_CURRENCY, COL_NAME_GEN_ROWID) +
                    " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__TAG_ID) + " = " +
                            sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
            " WHERE COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMTABLE) + ", '') = '' #WhereConditions# " +
            " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_CAR, COL_NAME_GEN_NAME) +
            " ORDER BY 2 DESC";

    private String mReportSqlName;
    private Bundle mSearchCondition;

    public DBReportAdapter(Context ctx, @Nullable String reportSqlName, @Nullable Bundle searchCondition) {
        super(ctx);
        mReportSqlName = reportSqlName;
        mSearchCondition = searchCondition;
    }

    public void setReportSql(String reportSqlName, Bundle searchCondition) {
        mReportSqlName = reportSqlName;
        mSearchCondition = searchCondition;
    }

    @SuppressWarnings("StringConcatenationInLoop")@Nullable
    public Cursor fetchReport(int limitCount) {
        if (mReportSqlName == null) {
            return null;
        }
        Set<String> whereColumns = null;
        String whereColumn;
        String reportSql = "";
        String whereCondition = "";
        if (mSearchCondition != null) {
            whereColumns = mSearchCondition.keySet();
        }

        if (whereColumns != null && whereColumns.size() > 0 && !mReportSqlName.equals(STATISTICS_SELECT_NAME)) {
            for (String whereColumn1 : whereColumns) {
                whereColumn = whereColumn1;
                if (whereColumn.startsWith("EstDueDays")) {
                    whereCondition = whereCondition + " AND " + whereColumn + " " + mSearchCondition.getString(whereColumn);
                }
                else {
                    String t = mSearchCondition.getString(whereColumn);
                    if (t != null && !t.toUpperCase().equals("NULL")) {
                        whereCondition = whereCondition + " AND " + whereColumn + " '" + mSearchCondition.getString(whereColumn) + "'";
                    }
                    else {
                        whereCondition = whereCondition + " AND " + whereColumn + " " + mSearchCondition.getString(whereColumn);
                    }
                }
            }
        }

        switch (mReportSqlName) {
            case MILEAGE_LIST_SELECT_NAME:
                reportSql = mileageListViewSelect;
                if (whereCondition.length() > 0) {
                    reportSql = reportSql + whereCondition;
                }

                reportSql = reportSql + " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + " DESC";
                break;
            case MILEAGE_LIST_REPORT_SELECT:
                reportSql = mileageListReportSelect;
                if (whereCondition.length() > 0) {
                    reportSql = reportSql + whereCondition;
                }

                reportSql = reportSql + " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + " ASC";
                break;
            case REFUEL_LIST_SELECT_NAME:
                reportSql = refuelListViewSelect;
                if (whereCondition.length() > 0) {
                    reportSql = reportSql + whereCondition;
                }

                reportSql = reportSql + " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__INDEX) + " DESC";
                break;
            case REFUEL_LIST_REPORT_SELECT:
                reportSql = refuelListReportSelect;
                if (whereCondition.length() > 0) {
                    reportSql = reportSql + whereCondition;
                }

                reportSql = reportSql + " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__INDEX) + " ASC";
                break;
            case EXPENSE_LIST_SELECT_NAME:
                reportSql = expenseListViewSelect;
                if (whereCondition.length() > 0) {
                    reportSql = reportSql + whereCondition;
                }

                reportSql = reportSql + " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " DESC";
                break;
            case EXPENSES_LIST_REPORT_SELECT:
                reportSql = expensesListReportSelect;
                if (whereCondition.length() > 0) {
                    reportSql = reportSql + whereCondition;
                }

                reportSql = reportSql + " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " ASC";
                break;
            case GPS_TRACK_LIST_SELECT_NAME:
                reportSql = gpsTrackListViewSelect;
                if (whereCondition.length() > 0) {
                    reportSql = reportSql + whereCondition;
                }

                reportSql = reportSql + " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DATE) + " DESC";
                break;
            case GPS_TRACK_LIST_REPORT_SELECT:
                reportSql = gpsTrackListReportSelect;
                if (whereCondition.length() > 0) {
                    reportSql = reportSql + whereCondition;
                }

                reportSql = reportSql + " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_GPSTRACK, COL_NAME_GPSTRACK__DATE) + " ASC";
                break;
            case TODO_LIST_SELECT_NAME:
                reportSql = todoListViewSelect;
                if (whereCondition.length() > 0) {
                    reportSql = reportSql + whereCondition;
                }

                reportSql = reportSql + " ORDER BY EstDueDays ASC, " + "COALESCE (" + sqlConcatTableColumn(TABLE_NAME_TODO, COL_NAME_TODO__DUEMILEAGE)
                        + ", 0) ASC ";
                break;
            case TODO_LIST_REPORT_SELECT:
                reportSql = todoListReportSelect;
                if (whereCondition.length() > 0) {
                    reportSql = reportSql + whereCondition;
                }

                reportSql = reportSql + " ORDER BY EstimatedDueDate_DTypeL ASC, " + "COALESCE (ScheduledDate_DTypeD , 99999999999) ASC, "
                        + "COALESCE(EstimatedScheduledMileageDate_DTypeL, 99999999999) ASC ";
                break;
            case CAR_LIST_SELECT_NAME:
                reportSql = carListViewSelect;
                break;
            case DRIVER_LIST_SELECT_NAME:
                reportSql = driverListViewSelect;
                break;
            case UOM_LIST_SELECT_NAME:
                reportSql = uomListViewSelect;
                break;
            case UOM_CONVERSION_LIST_SELECT_NAME:
                reportSql = uomConversionListViewSelect;
                break;
            case EXPENSE_FUEL_CATEGORY_LIST_SELECT_NAME:
                reportSql = expenseFuelCategoryListViewSelect;
                if (whereCondition.length() > 0) {
                    reportSql = reportSql + whereCondition;
                }
                reportSql = reportSql + " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ISACTIVE) + " DESC, " +
                        "lower(" + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) + ")";
                break;
            case EXPENSE_TYPE_LIST_SELECT_NAME:
                reportSql = expenseTypeListViewSelect;
                break;
            case REIMBURSEMENT_RATE_LIST_SELECT_NAME:
                reportSql = reimbursementRateViewSelect;
                break;
            case CURRENCY_LIST_SELECT_NAME:
                reportSql = currencyListViewSelect;
                break;
            case CURRENCY_RATE_LIST_SELECT_NAME:
                reportSql = currencyRateListViewSelect;
                break;
            case BPARTNER_LIST_SELECT_NAME:
                reportSql = bpartnerListViewSelect;
                break;
            case TASK_TYPE_LIST_SELECT_NAME:
                reportSql = taskTypeListViewSelect;
                break;
            case TASK_LIST_SELECT_NAME:
                reportSql = taskListViewSelect;
                break;
            case BT_CAR_LINK_LIST_SELECT_NAME:
                reportSql = btCarLinkListViewSelect;
                break;
            case TAG_LIST_SELECT_NAME:
                reportSql = tagListViewSelect;
                break;
            case STATISTICS_SELECT_NAME:
                reportSql = statisticsMainViewSelect;
                String mExpenseStatisticsPeriodCondition = "";
                String mMinIndexStatisticsPeriodCondition = "";

                assert mSearchCondition != null;
                whereColumns = mSearchCondition.keySet();
                for(String whereColumn1 : whereColumns){
                    if(whereColumn1.startsWith(DBReportAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_CAR, DBAdapter.COL_NAME_GEN_ROWID))){
                        whereCondition = whereCondition + " AND " + whereColumn1 + " " + mSearchCondition.getString(whereColumn1);
                    }
                    else if(whereColumn1.equals("DateFrom")){
                        mExpenseStatisticsPeriodCondition = mExpenseStatisticsPeriodCondition +
                                " AND " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " >= " + mSearchCondition.getString(whereColumn1);
                        mMinIndexStatisticsPeriodCondition = mMinIndexStatisticsPeriodCondition +
                                " AND Date >= " + mSearchCondition.getString(whereColumn1);
                    }
                    else if(whereColumn1.equals("DateTo")){
                        mExpenseStatisticsPeriodCondition = mExpenseStatisticsPeriodCondition +
                                " AND " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " <= " + mSearchCondition.getString(whereColumn1);
                        mMinIndexStatisticsPeriodCondition = mMinIndexStatisticsPeriodCondition +
                                " AND Date <= " + mSearchCondition.getString(whereColumn1);
                    }
                }
                reportSql = reportSql.replace("mExpenseStatisticsPeriodCondition", mExpenseStatisticsPeriodCondition)
                                .replace("mMinIndexStatisticsPeriodCondition", mMinIndexStatisticsPeriodCondition);
                if (whereCondition.length() > 0) {
                    reportSql = reportSql + whereCondition;
                }
                break;
            case LIST_STATISTICS_MILEAGE_TOTAL:
                reportSql = listStatisticsMileageTotal;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_MILEAGE_BY_TYPES:
                reportSql = listStatisticsMileageByType;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_MILEAGE_BY_TAGS:
                reportSql = listStatisticsMileageByTag;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_MILEAGE_BY_DRIVERS:
                reportSql = listStatisticsMileageByDriver;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_MILEAGE_BY_CARS:
                reportSql = listStatisticsMileageByCars;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_REFUEL_TOTAL:
                reportSql = listStatisticsRefuelTotal;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_REFUEL_BY_TYPES:
                reportSql = listStatisticsRefuelByType;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_REFUEL_BY_FUELTYPES:
                reportSql = listStatisticsRefuelByFuelType;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_REFUEL_BY_TAGS:
                reportSql = listStatisticsRefuelByTag;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_REFUEL_BY_DRIVERS:
                reportSql = listStatisticsRefuelByDriver;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_REFUEL_BY_CARS:
                reportSql = listStatisticsRefuelByCars;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_EXPENSE_TOTAL:
                reportSql = listStatisticsExpenseTotal;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_EXPENSE_BY_TYPES:
                reportSql = listStatisticsExpenseByType;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_EXPENSE_BY_CATEGORIES:
                reportSql = listStatisticsExpenseByCategory;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_EXPENSE_BY_TAGS:
                reportSql = listStatisticsExpenseByTag;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_EXPENSE_BY_DRIVERS:
                reportSql = listStatisticsExpenseByDriver;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
            case LIST_STATISTICS_EXPENSE_BY_CARS:
                reportSql = listStatisticsExpenseByCars;
                if (whereCondition.length() > 0)
                    reportSql = reportSql.replace("#WhereConditions#", whereCondition);
                else
                    reportSql = reportSql.replace("#WhereConditions#", "");
                break;
        }

        if (limitCount != -1) {
            reportSql = reportSql + " LIMIT " + limitCount;
        }

        return mDb.rawQuery(reportSql, null);
    }

    public ArrayList<chartData> getMileageByTypeChartData(String[] selectionArgs) {
        ArrayList<chartData> retVal = new ArrayList<>();
        float totalValue = 0f;

        String sqlWhere = " WHERE " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " IS NOT NULL AND " +
                                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + " = ?";
        if(selectionArgs.length > 1 && !selectionArgs[1].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) + " >= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        if(selectionArgs.length > 2 && !selectionArgs[2].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) + " <= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        //get the sum of the mileage for calculating the percent of each type
        String selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) +
                        "-" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + ") " +
                " FROM " + TABLE_NAME_MILEAGE +
                sqlWhere;

        Cursor c = mDb.rawQuery(selectSql, selectionArgs);
        if(c.moveToFirst()){
            totalValue = c.getFloat(0);
        }
        c.close();
        if (totalValue == 0f)
            return retVal; //no entries

        selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) +
                                        "-" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + "), " +
                        "SUM(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) +
                        "-" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + ") * 100 / " + totalValue + ", " + //in percent
                        sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) +
                " FROM " + TABLE_NAME_MILEAGE +
                "   JOIN " + TABLE_NAME_EXPENSETYPE + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__EXPENSETYPE_ID) +
                        "=" + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                sqlWhere +
                " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) +
                " ORDER BY 1 DESC";
//                " ORDER BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME);
        c = mDb.rawQuery(selectSql, selectionArgs);
        while (c.moveToNext()){
            chartData cd = new chartData();
            cd.value = c.getFloat(0);
            cd.value2 = c.getFloat(1);
            cd.totalValue = totalValue;
            cd.label = c.getString(2);
            retVal.add(cd);
        }
        c.close();
        return retVal;
    }

    public ArrayList<chartData> getMileageByTagsChartData(String[] selectionArgs) {
        ArrayList<chartData> retVal = new ArrayList<>();
        float totalValue = 0f;

        String sqlWhere = " WHERE " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " IS NOT NULL AND " +
                                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + " = ?";
        if(selectionArgs.length > 1 && !selectionArgs[1].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) + " >= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        if(selectionArgs.length > 2 && !selectionArgs[2].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) + " <= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        //get the total for calculating the percent of each type
        String selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) +
                        "-" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + ") " +
                " FROM " + TABLE_NAME_MILEAGE +
                sqlWhere;

        Cursor c = mDb.rawQuery(selectSql, selectionArgs);
        if (c.moveToFirst()) {
            totalValue = c.getFloat(0);
        }
        c.close();
        if (totalValue == 0f)
            return retVal; //no entries

        selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) +
                        "-" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + "), " +
                        "SUM(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) +
                        "-" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + ") * 100 / " + totalValue + ", " + //in percent
                        "COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + ", 'N/A')" +
                " FROM " + TABLE_NAME_MILEAGE +
                        "  LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__TAG_ID) +
                        "=" + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
                sqlWhere +
                " GROUP BY COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + ", 'N/A')" +
                " ORDER BY 1 DESC";

        c = mDb.rawQuery(selectSql, selectionArgs);
        while (c.moveToNext()) {
            chartData cd = new chartData();
            cd.value = c.getFloat(0);
            cd.value2 = c.getFloat(1);
            cd.totalValue = totalValue;
            cd.label = c.getString(2);
            retVal.add(cd);
        }
        c.close();
        return retVal;
    }

    public ArrayList<chartData> getMileageByDriverChartData(String[] selectionArgs) {
        ArrayList<chartData> retVal = new ArrayList<>();
        float totalValue = 0f;
        String sqlWhere = " WHERE " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + " IS NOT NULL AND " +
                                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + " = ?";
        if(selectionArgs.length > 1 && !selectionArgs[1].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) + " >= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        if(selectionArgs.length > 2 && !selectionArgs[2].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DATE) + " <= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        //get the total for calculating the percent of each type
        String selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) +
                        "-" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + ") " +
                " FROM " + TABLE_NAME_MILEAGE +
                sqlWhere;
        Cursor c = mDb.rawQuery(selectSql, selectionArgs);
        if (c.moveToFirst()) {
            totalValue = c.getFloat(0);
        }
        c.close();
        if (totalValue == 0f)
            return retVal; //no entries

        selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) +
                        "-" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + "), " +
                        "SUM(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) +
                        "-" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTART) + ") * 100 / " + totalValue + ", " + //in percent
                        "COALESCE( " + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + ", 'N/A')" +
                " FROM " + TABLE_NAME_MILEAGE +
                        "  JOIN " + TABLE_NAME_DRIVER + " ON " +
                                sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__DRIVER_ID) +
                                    "=" + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_ROWID) +
                sqlWhere +
                " GROUP BY COALESCE( " + sqlConcatTableColumn(TABLE_NAME_DRIVER, COL_NAME_GEN_NAME) + ", 'N/A')" +
                " ORDER BY 1 DESC";
        c = mDb.rawQuery(selectSql, selectionArgs);
        while (c.moveToNext()) {
            chartData cd = new chartData();
            cd.value = c.getFloat(0);
            cd.value2 = c.getFloat(1);
            cd.totalValue = totalValue;
            cd.label = c.getString(2);
            retVal.add(cd);
        }
        c.close();
        return retVal;
    }

    public ArrayList<chartData> getRefuelsByTypeChartData(String[] selectionArgs, boolean getValue) {
        ArrayList<chartData> retVal = new ArrayList<>();
        float totalValue = 0f;
        String sqlWhere = " WHERE " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + " = ?";
        if(selectionArgs.length > 1 && !selectionArgs[1].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) + " >= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        if(selectionArgs.length > 2 && !selectionArgs[2].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) + " <= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        String selColName;
        if(getValue)
            selColName = COL_NAME_REFUEL__AMOUNT;
        else
            selColName = COL_NAME_REFUEL__QUANTITY;
        //get the sum for calculating the percent of each type
        String selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_REFUEL, selColName) + ") " +
                " FROM " + TABLE_NAME_REFUEL +
                sqlWhere;
        Cursor c = mDb.rawQuery(selectSql, selectionArgs);
        if (c.moveToFirst()) {
            totalValue = c.getFloat(0);
        }
        c.close();
        if (totalValue == 0f)
            return retVal; //no entries

        selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_REFUEL, selColName) + "), " +
                        "SUM(" + sqlConcatTableColumn(TABLE_NAME_REFUEL, selColName) + ") * 100 / " + totalValue + ", " + //in percent
                        sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) +
                " FROM " + TABLE_NAME_REFUEL +
                "   JOIN " + TABLE_NAME_EXPENSETYPE + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__EXPENSETYPE_ID) +
                        "=" + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                sqlWhere +
                " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) +
                " ORDER BY 1 DESC";
        c = mDb.rawQuery(selectSql, selectionArgs);
        while (c.moveToNext()) {
            chartData cd = new chartData();
            cd.value = c.getFloat(0);
            cd.value2 = c.getFloat(1);
            cd.totalValue = totalValue;
            cd.label = c.getString(2);
            retVal.add(cd);
        }
        c.close();
        return retVal;
    }

    public ArrayList<chartData> getRefuelsByTagChartData(String[] selectionArgs, boolean getValue) {
        ArrayList<chartData> retVal = new ArrayList<>();
        float totalValue = 0f;

        String sqlWhere = " WHERE " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + " = ?";
        if(selectionArgs.length > 1 && !selectionArgs[1].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) + " >= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        if(selectionArgs.length > 2 && !selectionArgs[2].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) + " <= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        String selColName;
        if(getValue)
            selColName = COL_NAME_REFUEL__AMOUNT;
        else
            selColName = COL_NAME_REFUEL__QUANTITY;

        //get the sum for calculating the percent of each type
        String selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_REFUEL, selColName) + ") " +
                " FROM " + TABLE_NAME_REFUEL +
                sqlWhere;

        Cursor c = mDb.rawQuery(selectSql, selectionArgs);
        if (c.moveToFirst()) {
            totalValue = c.getFloat(0);
        }
        c.close();
        if (totalValue == 0f)
            return retVal; //no entries

        selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_REFUEL, selColName) + "), " +
                        "SUM(" + sqlConcatTableColumn(TABLE_NAME_REFUEL, selColName) + ") * 100 / " + totalValue + ", " + //in percent
                        "COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + ", 'N/A')" +
                " FROM " + TABLE_NAME_REFUEL +
                " LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__TAG_ID) +
                        "=" + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
                sqlWhere +
                " GROUP BY COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + ", 'N/A')" +
                " ORDER BY 1 DESC";
        c = mDb.rawQuery(selectSql, selectionArgs);
        while (c.moveToNext()) {
            chartData cd = new chartData();
            cd.value = c.getFloat(0);
            cd.value2 = c.getFloat(1);
            cd.totalValue = totalValue;
            cd.label = c.getString(2);
            retVal.add(cd);
        }
        c.close();
        return retVal;
    }

    public ArrayList<chartData> getRefuelsByFuelTypeChartData(String[] selectionArgs, boolean getValue) {
        ArrayList<chartData> retVal = new ArrayList<>();
        float totalValue = 0f;
        String sqlWhere = " WHERE " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + " = ?";
        if(selectionArgs.length > 1 && !selectionArgs[1].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) + " >= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        if(selectionArgs.length > 2 && !selectionArgs[2].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__DATE) + " <= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        String selColName;
        if(getValue)
            selColName = COL_NAME_REFUEL__AMOUNT;
        else
            selColName = COL_NAME_REFUEL__QUANTITY;

        //get the sum for calculating the percent of each type
        String selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_REFUEL, selColName) + ") " +
                " FROM " + TABLE_NAME_REFUEL +
                sqlWhere;

        Cursor c = mDb.rawQuery(selectSql, selectionArgs);
        if (c.moveToFirst()) {
            totalValue = c.getFloat(0);
        }
        c.close();
        if (totalValue == 0f)
            return retVal; //no entries

        selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_REFUEL, selColName) + "), " +
                        "SUM(" + sqlConcatTableColumn(TABLE_NAME_REFUEL, selColName) + ") * 100 / " + totalValue + ", " + //in percent
                        sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) +
                " FROM " + TABLE_NAME_REFUEL +
                        "   JOIN " + TABLE_NAME_EXPENSECATEGORY + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__EXPENSECATEGORY_ID) +
                        "=" + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                sqlWhere +
                " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) +
                " ORDER BY 1 DESC";
        c = mDb.rawQuery(selectSql, selectionArgs);
        while (c.moveToNext()) {
            chartData cd = new chartData();
            cd.value = c.getFloat(0);
            cd.value2 = c.getFloat(1);
            cd.totalValue = totalValue;
            cd.label = c.getString(2);
            retVal.add(cd);
        }
        c.close();
        return retVal;
    }

    public ArrayList<chartData> getExpensesByTypeChartData(String[] selectionArgs) {
        ArrayList<chartData> retVal = new ArrayList<>();
        float totalValue = 0f;
        String sqlWhere = " WHERE " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " = ?" +
                                " AND COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMTABLE) + ", '') = ''";
        if(selectionArgs.length > 1 && !selectionArgs[1].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " >= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        if(selectionArgs.length > 2 && !selectionArgs[2].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " <= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        //get the sum for calculating the percent of each type
        String selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + ") " +
                " FROM " + TABLE_NAME_EXPENSE +
                sqlWhere;

        Cursor c = mDb.rawQuery(selectSql, selectionArgs);
        if (c.moveToFirst()) {
            totalValue = c.getFloat(0);
        }
        c.close();
        if (totalValue == 0f)
            return retVal; //no entries

        selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + "), " +
                        "SUM(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + ") * 100 / " + totalValue + ", " + //in percent
                        sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) +
                " FROM " + TABLE_NAME_EXPENSE +
                "   JOIN " + TABLE_NAME_EXPENSETYPE + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__EXPENSETYPE_ID) +
                        "=" + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) +
                sqlWhere +
                " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_NAME) +
                " ORDER BY 1 DESC";
        c = mDb.rawQuery(selectSql, selectionArgs);
        while (c.moveToNext()) {
            chartData cd = new chartData();
            cd.value = c.getFloat(0);
            cd.value2 = c.getFloat(1);
            cd.totalValue = totalValue;
            cd.label = c.getString(2);
            retVal.add(cd);
        }
        c.close();
        return retVal;
    }

    public ArrayList<chartData> getExpensesByCategoryChartData(String[] selectionArgs) {
        ArrayList<chartData> retVal = new ArrayList<>();
        float totalValue = 0f;
        String sqlWhere = " WHERE " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " = ?" +
                                " AND COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMTABLE) + ", '') = ''";
        if(selectionArgs.length > 1 && !selectionArgs[1].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " >= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        if(selectionArgs.length > 2 && !selectionArgs[2].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " <= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        //get the sum for calculating the percent of each type
        String selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + ") " +
                " FROM " + TABLE_NAME_EXPENSE +
                sqlWhere;

        Cursor c = mDb.rawQuery(selectSql, selectionArgs);
        if (c.moveToFirst()) {
            totalValue = c.getFloat(0);
        }
        c.close();
        if (totalValue == 0f)
            return retVal; //no entries

        selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + "), " +
                        "SUM(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + ") * 100 / " + totalValue + ", " + //in percent
                        sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) +
                " FROM " + TABLE_NAME_EXPENSE +
                "   JOIN " + TABLE_NAME_EXPENSECATEGORY + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__EXPENSECATEGORY_ID) +
                        "=" + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_ROWID) +
                sqlWhere +
                " GROUP BY " + sqlConcatTableColumn(TABLE_NAME_EXPENSECATEGORY, COL_NAME_GEN_NAME) +
                " ORDER BY 1 DESC";
        c = mDb.rawQuery(selectSql, selectionArgs);
        while (c.moveToNext()) {
            chartData cd = new chartData();
            cd.value = c.getFloat(0);
            cd.value2 = c.getFloat(1);
            cd.totalValue = totalValue;
            cd.label = c.getString(2);
            retVal.add(cd);
        }
        c.close();
        return retVal;
    }

    public ArrayList<chartData> getExpensesByTagChartData(String[] selectionArgs) {
        ArrayList<chartData> retVal = new ArrayList<>();
        float totalValue = 0f;
        String sqlWhere = " WHERE " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " = ?" +
                                " AND COALESCE(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__FROMTABLE) + ", '') = ''";
        if(selectionArgs.length > 1 && !selectionArgs[1].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " >= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        if(selectionArgs.length > 2 && !selectionArgs[2].equals("0"))
            sqlWhere = sqlWhere + " AND " + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__DATE) + " <= ?";
        else
            sqlWhere = sqlWhere + " AND '0' = ? ";

        //get the sum for calculating the percent of each type
        String selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + ") " +
                " FROM " + TABLE_NAME_EXPENSE +
                sqlWhere;

        Cursor c = mDb.rawQuery(selectSql, selectionArgs);
        if (c.moveToFirst()) {
            totalValue = c.getFloat(0);
        }
        c.close();
        if (totalValue == 0f)
            return retVal; //no entries

        selectSql =
                " SELECT SUM(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + "), " +
                        "SUM(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__AMOUNT) + ") * 100 / " + totalValue + ", " + //in percent
                        "COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + ", 'N/A')" +
                " FROM " + TABLE_NAME_EXPENSE +
                "   LEFT OUTER JOIN " + TABLE_NAME_TAG + " ON " +
                        sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__TAG_ID) +
                        "=" + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_ROWID) +
                sqlWhere +
                " GROUP BY COALESCE( " + sqlConcatTableColumn(TABLE_NAME_TAG, COL_NAME_GEN_NAME) + ", 'N/A')" +
                " ORDER BY 1 DESC";
        c = mDb.rawQuery(selectSql, selectionArgs);
        while (c.moveToNext()) {
            chartData cd = new chartData();
            cd.value = c.getFloat(0);
            cd.value2 = c.getFloat(1);
            cd.totalValue = totalValue;
            cd.label = c.getString(2);
            retVal.add(cd);
        }
        c.close();
        return retVal;
    }

    //selects used in charts
    public static class chartData implements Serializable{
        public float value; //base value
        public float value2; //another representation of the base value. for ex. percent
        public float totalValue;
        public String label;
    }
}
//@formatter:on
