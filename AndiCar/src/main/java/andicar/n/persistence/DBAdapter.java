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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import org.andicar2.activity.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Locale;

import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;

//@formatter:off
@SuppressWarnings("JavaDoc") public class DBAdapter extends DB {

    public DBAdapter(Context ctx) {
        super(ctx);
    }

    public int getVersion() {
        return mDb.getVersion();
    }

    /**
     * Create a new record in the given table
     *
     * @return -1 * Error code in case of error, the id of the record in case
     * of success. For error codes see errors.xml
     */
    public long createRecord(String tableName, ContentValues content) {
        long retVal = canCreate(tableName, content);
        if (retVal != -1) {
            return -1 * retVal;
        }

        long mCarId = -1;
        BigDecimal stopIndex = null;
        BigDecimal startIndex = null;
        try {
            switch (tableName) {
                case TABLE_NAME_MILEAGE:
                    mCarId = content.getAsLong(COL_NAME_MILEAGE__CAR_ID);
                    if (content.getAsString(COL_NAME_MILEAGE__INDEXSTOP) != null) {
                        stopIndex = new BigDecimal(content.getAsString(COL_NAME_MILEAGE__INDEXSTOP));
                    }
                    startIndex = new BigDecimal(content.getAsString(COL_NAME_MILEAGE__INDEXSTART));
                    break;
                case TABLE_NAME_REFUEL:
                    mCarId = content.getAsLong(COL_NAME_REFUEL__CAR_ID);
                    stopIndex = new BigDecimal(content.getAsString(COL_NAME_REFUEL__INDEX));
                    break;
                case TABLE_NAME_EXPENSE:
                    mCarId = content.getAsLong(COL_NAME_EXPENSE__CAR_ID);
                    String newIndexStr = content.getAsString(COL_NAME_EXPENSE__INDEX);
                    if (newIndexStr != null && newIndexStr.length() > 0) {
                        stopIndex = new BigDecimal(newIndexStr);
                    }
                    break;
            }

            if (mCarId != -1 && stopIndex != null) { //update the car current index
                try {
                    mDb.beginTransaction();
                    retVal = mDb.insertOrThrow(tableName, null, content);
                    updateCarCurrentIndex(mCarId, stopIndex);
                    if (startIndex != null) {
                        updateCarInitIndex(mCarId, startIndex);
                    }
                    else {
                        updateCarInitIndex(mCarId, stopIndex);
                    }
                    mDb.setTransactionSuccessful();
                }
                catch (SQLException | NumberFormatException e) {
                    mErrorMessage = e.getMessage();
                    retVal = -1;
                }
                finally {
                    if (mDb.inTransaction()) //issue no: 84
                    {
                        mDb.endTransaction();
                    }
                }
            }
            else {
                retVal = mDb.insertOrThrow(tableName, null, content);
            }
            if (retVal != -1 && tableName.equals(TABLE_NAME_REFUEL)) { //create expense
                ContentValues expenseContent;
                expenseContent = new ContentValues();

                expenseContent.put(DBAdapter.COL_NAME_GEN_NAME, ConstantValues.EXPENSES_COL_FROM_REFUEL_TABLE_NAME);
                expenseContent.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, content.getAsString(DBAdapter.COL_NAME_GEN_USER_COMMENT));
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__CAR_ID, content.getAsString(DBAdapter.COL_NAME_REFUEL__CAR_ID));
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__DRIVER_ID, content.getAsString(DBAdapter.COL_NAME_REFUEL__DRIVER_ID));
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__EXPENSECATEGORY_ID, content.getAsString(DBAdapter.COL_NAME_REFUEL__EXPENSECATEGORY_ID));
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__EXPENSETYPE_ID, content.getAsString(DBAdapter.COL_NAME_REFUEL__EXPENSETYPE_ID));
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__INDEX, content.getAsString(DBAdapter.COL_NAME_REFUEL__INDEX));

                BigDecimal price = new BigDecimal(content.getAsString(DBAdapter.COL_NAME_REFUEL__PRICEENTERED));
                BigDecimal quantity = new BigDecimal(content.getAsString(DBAdapter.COL_NAME_REFUEL__QUANTITYENTERED));
                BigDecimal amt = (price.multiply(quantity)).setScale(ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT);
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__AMOUNTENTERED, amt.toString());
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__CURRENCYENTERED_ID, content.getAsString(DBAdapter.COL_NAME_REFUEL__CURRENCYENTERED_ID));
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__CURRENCYRATE, content.getAsString(DBAdapter.COL_NAME_REFUEL__CURRENCYRATE));

                BigDecimal conversionRate = new BigDecimal(content.getAsString(DBAdapter.COL_NAME_REFUEL__CURRENCYRATE));
                amt = (amt.multiply(conversionRate)).setScale(ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT);
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__AMOUNT, amt.toString());

                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__CURRENCY_ID, content.getAsString(DBAdapter.COL_NAME_REFUEL__CURRENCY_ID));
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__DATE, content.getAsString(DBAdapter.COL_NAME_REFUEL__DATE));
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__DOCUMENTNO, content.getAsString(DBAdapter.COL_NAME_REFUEL__DOCUMENTNO));
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__FROMTABLE, ConstantValues.EXPENSES_COL_FROM_REFUEL_TABLE_NAME);
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__FROMRECORD_ID, retVal);
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__BPARTNER_ID, content.getAsString(DBAdapter.COL_NAME_REFUEL__BPARTNER_ID));
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__BPARTNER_LOCATION_ID,
                        content.getAsString(DBAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID));
                expenseContent.put(DBAdapter.COL_NAME_EXPENSE__TAG_ID, content.getAsString(DBAdapter.COL_NAME_REFUEL__TAG_ID));
                mDb.insertOrThrow(DBAdapter.TABLE_NAME_EXPENSE, null, expenseContent);
            }
        }
        catch (SQLException ex) {
            mErrorMessage = ex.getMessage();
            mException = ex;
            return -1;
        }
        catch (NumberFormatException ex) {
            mErrorMessage = ex.getMessage();
            mException = ex;
            return -1;
        }
        return retVal;
    }

    /**
     * check create preconditions
     *
     * @return -1 if the row can be inserted, an error code otherwise. For error
     * codes see errors.xml
     */
    private int canCreate(String tableName, ContentValues content) {

        String checkSql;
        Cursor checkCursor;
        //        int retVal = -1;
        switch (tableName) {
            case TABLE_NAME_CURRENCYRATE:
                long currencyFromId = content.getAsLong(COL_NAME_CURRENCYRATE__FROMCURRENCY_ID);
                long currencyToId = content.getAsLong(COL_NAME_CURRENCYRATE__TOCURRENCY_ID);
                if (currencyFromId == currencyToId) {
                    return R.string.error_032;
                }

                //check duplicates
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_CURRENCYRATE + " " + "WHERE (" + COL_NAME_CURRENCYRATE__FROMCURRENCY_ID + " =  " + currencyFromId
                        + " AND " + COL_NAME_CURRENCYRATE__TOCURRENCY_ID + " =  " + currencyToId + " " + ") " + " OR " + "(" + COL_NAME_CURRENCYRATE__TOCURRENCY_ID
                        + " =  " + currencyFromId + " AND " + COL_NAME_CURRENCYRATE__FROMCURRENCY_ID + " =  " + currencyToId + " " + ") " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_029;
                }
                if (!checkCursor.isClosed()) {
                    checkCursor.close();
                }
                break;
            case TABLE_NAME_UOMCONVERSION:
                if (content.getAsLong(COL_NAME_UOMCONVERSION__UOMFROM_ID).equals(content.getAsLong(COL_NAME_UOMCONVERSION__UOMTO_ID))) {
                    return R.string.error_031;
                }
                break;
            case TABLE_NAME_CURRENCY: {
                String checkSelect = "SELECT " + COL_NAME_GEN_ROWID + " " + "FROM " + TABLE_NAME_CURRENCY + " " + "WHERE UPPER( " + COL_NAME_CURRENCY__CODE
                        + ") = ? ";
                String[] selectionArgs = {content.getAsString(COL_NAME_CURRENCY__CODE).toUpperCase(Locale.US)};
                Cursor c = execSelectSql(checkSelect, selectionArgs);
                if (c.moveToFirst()) { //duplicate currency code
                    c.close();
                    return R.string.error_059;
                }
                c.close();
                break;
            }
            case TABLE_NAME_TASK_CAR: {
                String checkSelect = "SELECT " + COL_NAME_GEN_ROWID + " " + "FROM " + TABLE_NAME_TASK_CAR + " " + "WHERE " + COL_NAME_TASK_CAR__CAR_ID + " = ? "
                        + " AND " + COL_NAME_TASK_CAR__TASK_ID + " = ? ";
                String[] selectionArgs = {content.getAsString(COL_NAME_TASK_CAR__CAR_ID), content.getAsString(COL_NAME_TASK_CAR__TASK_ID)};
                Cursor c = execSelectSql(checkSelect, selectionArgs);
                if (c.moveToFirst()) { //duplicate record
                    c.close();
                    return R.string.error_059;
                }
                c.close();
                break;
            }
            case TABLE_NAME_UOM: {
                String checkSelect = "SELECT " + COL_NAME_GEN_ROWID + " " + "FROM " + TABLE_NAME_UOM + " " + "WHERE UPPER( " + COL_NAME_UOM__CODE + ") = ? ";
                String[] selectionArgs = {content.getAsString(COL_NAME_UOM__CODE).toUpperCase(Locale.US)};
                Cursor c = execSelectSql(checkSelect, selectionArgs);
                if (c.moveToFirst()) { //duplicate currency code
                    c.close();
                    return R.string.error_059;
                }
                c.close();
                break;
            }
        }
        return -1;
    }

    /**
     * Update the car current index
     */
    private void updateCarCurrentIndex(long mCarId, BigDecimal newIndex) throws SQLException, NumberFormatException {
        //update car current index
        Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, mCarId);
        BigDecimal carCurrentIndex;
        if ((c != null ? c.getString(COL_POS_CAR__INDEXCURRENT) : null) == null) {
            carCurrentIndex = new BigDecimal(0);
        }
        else {
            carCurrentIndex = new BigDecimal(c.getString(COL_POS_CAR__INDEXCURRENT));
        }
        if (c != null) {
            c.close();
        }
        ContentValues content = new ContentValues();
        if (newIndex.compareTo(carCurrentIndex) > 0) {
            content.put(COL_NAME_CAR__INDEXCURRENT, newIndex.toString());
            if (mDb.update(TABLE_NAME_CAR, content, COL_NAME_GEN_ROWID + "=" + mCarId, null) == 0) {
                throw new SQLException("Car Update error");
            }
        }
        else {
            updateCarCurrentIndex(mCarId);
        }
    }

    /**
     * Update the car init index
     */
    private void updateCarInitIndex(long mCarId, BigDecimal newIndex) throws SQLException {
        //update car current index
        Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, mCarId);
        BigDecimal carInitIndex;
        if(c != null && c.getString(COL_POS_CAR__INDEXSTART) != null && c.getString(COL_POS_CAR__INDEXSTART).length() > 0)
            carInitIndex = new BigDecimal(c.getString(COL_POS_CAR__INDEXSTART));
        else
            carInitIndex = BigDecimal.ZERO;

        if (c != null) {
            c.close();
        }

        ContentValues content = new ContentValues();
        if (newIndex.compareTo(carInitIndex) < 0) {
            content.put(COL_NAME_CAR__INDEXSTART, newIndex.toString());
            if (mDb.update(TABLE_NAME_CAR, content, COL_NAME_GEN_ROWID + "=" + mCarId, null) == 0) {
                throw new SQLException("Car Update error");
            }
        }
    }

    public Cursor execSelectSql(String selectSql, @Nullable String[] selectionArgs) {
        return mDb.rawQuery(selectSql, selectionArgs);
    }

    public void execUpdate(String sql){
        mDb.execSQL(sql);
    }

    /**
     * Return a Cursor positioned at the record that matches the given rowId
     * from the given table
     *
     * @param rowId id of the record to retrieve
     * @return Cursor positioned to matching record, if found. Otherwise null.
     * @throws SQLException if the record could not be found/retrieved
     */
    @Nullable
    public Cursor fetchRecord(String tableName, String[] columns, long rowId) throws SQLException {
        Cursor mCursor = mDb.query(true, tableName, columns, COL_NAME_GEN_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                return mCursor;
            }
            else {
                mCursor.close();
                return null;
            }
        }
        else {
            return null;
        }

    }

    private void updateCarCurrentIndex(long mCarId) throws SQLException, NumberFormatException {
        BigDecimal newStopIndex = BigDecimal.ZERO;
        BigDecimal tmpStopIndex = BigDecimal.ZERO;
        String tmpStr;
        Cursor c;

        String sql = "SELECT MAX(" + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__INDEXSTOP) + ") " + " FROM " + TABLE_NAME_MILEAGE + " WHERE "
                + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID) + " = ? " + " GROUP BY "
                + sqlConcatTableColumn(TABLE_NAME_MILEAGE, COL_NAME_MILEAGE__CAR_ID);
        String[] selectionArgs = {Long.toString(mCarId)};
        c = execSelectSql(sql, selectionArgs);
        if (c.moveToFirst()) {
            try {
                tmpStr = c.getString(0);
                if (tmpStr != null && tmpStr.length() > 0) {
                    tmpStopIndex = new BigDecimal(tmpStr);
                }
            }
            catch (NumberFormatException e) {
                tmpStopIndex = null;
            }
        }
        if (tmpStopIndex == null) {
            tmpStopIndex = BigDecimal.ZERO;
        }
        c.close();
        if (tmpStopIndex.compareTo(newStopIndex) > 0) {
            newStopIndex = tmpStopIndex;
        }

        sql = "SELECT MAX(" + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__INDEX) + ") " + " FROM " + TABLE_NAME_REFUEL + " WHERE "
                + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID) + " = ? " + " GROUP BY "
                + sqlConcatTableColumn(TABLE_NAME_REFUEL, COL_NAME_REFUEL__CAR_ID);

        selectionArgs[0] = Long.toString(mCarId);
        c = execSelectSql(sql, selectionArgs);
        if (c.moveToFirst()) {
            try {
                tmpStr = c.getString(0);
                if (tmpStr != null && tmpStr.length() > 0) {
                    tmpStopIndex = new BigDecimal(tmpStr);
                }
            }
            catch (NumberFormatException e) {
                tmpStopIndex = null;
            }
        }
        if (tmpStopIndex == null) {
            tmpStopIndex = BigDecimal.ZERO;
        }
        c.close();

        if (tmpStopIndex.compareTo(newStopIndex) > 0) {
            newStopIndex = tmpStopIndex;
        }

        sql = "SELECT MAX(" + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + ") " + " FROM " + TABLE_NAME_EXPENSE + " WHERE "
                + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID) + " = ? " + " AND "
                + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__INDEX) + " IS NOT NULL " + " GROUP BY "
                + sqlConcatTableColumn(TABLE_NAME_EXPENSE, COL_NAME_EXPENSE__CAR_ID);
        selectionArgs[0] = Long.toString(mCarId);
        c = execSelectSql(sql, selectionArgs);

        if (c.moveToFirst()) {
            try {
                tmpStr = c.getString(0);
                if (tmpStr != null && tmpStr.length() > 0) {
                    tmpStopIndex = new BigDecimal(tmpStr);
                }
            }
            catch (NumberFormatException e) {
                tmpStopIndex = null;
            }
        }
        if (tmpStopIndex == null) {
            tmpStopIndex = BigDecimal.ZERO;
        }
        c.close();

        if (tmpStopIndex.compareTo(newStopIndex) > 0) {
            newStopIndex = tmpStopIndex;
        }

        if (newStopIndex.signum() == 0) {
            c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, mCarId);
            if(c != null && c.getString(COL_POS_CAR__INDEXSTART) != null && c.getString(COL_POS_CAR__INDEXSTART).length() > 0)
                newStopIndex = new BigDecimal(c.getString(COL_POS_CAR__INDEXSTART));
            else
                newStopIndex = BigDecimal.ZERO;

            if (c != null) {
                c.close();
            }
        }
        ContentValues content = new ContentValues();
        content.put(COL_NAME_CAR__INDEXCURRENT, newStopIndex.toString());
        if (mDb.update(TABLE_NAME_CAR, content, COL_NAME_GEN_ROWID + "=" + mCarId, null) == 0) {
            throw new SQLException("Car Update error");
        }
    }

    @SuppressWarnings("SameParameterValue")
    public void updateRecords(String tableName, String selection, String[] selectionArgs, ContentValues newContent) {
        //    	int checkVal;
        String checkSql = "SELECT * " +
                            " FROM " + tableName;
        if (selection != null) {
            checkSql = checkSql + " WHERE " + selection;
        }
        Cursor c = query(checkSql, selectionArgs);
        while (c.moveToNext()) {
            updateRecord(tableName, c.getLong(COL_POS_GEN_ROWID), newContent);
        }
        c.close();
    }

    public Cursor query(String sql, @Nullable String[] args) {
        return mDb.rawQuery(sql, args);
    }

    /**
     * update the record with recordId = rowId in the given table with the given
     * content
     *
     * @return -1 if the row updated, an error code otherwise. For error codes
     * see errors.xml
     */
    public int updateRecord(String tableName, long rowId, ContentValues content) {
        int retVal = canUpdate(tableName, content, rowId);
        if (retVal != -1) {
            return retVal;
        }

        long mCarId = -1;
        BigDecimal stopIndex = null;
        BigDecimal startIndex = null;
        try {
            switch (tableName) {
                case TABLE_NAME_MILEAGE:
                    mCarId = content.getAsLong(COL_NAME_MILEAGE__CAR_ID);
                    if(content.getAsString(COL_NAME_MILEAGE__INDEXSTOP) != null && content.getAsString(COL_NAME_MILEAGE__INDEXSTOP).trim().length() > 0)
                        stopIndex = new BigDecimal(content.getAsString(COL_NAME_MILEAGE__INDEXSTOP));
                    startIndex = new BigDecimal(content.getAsString(COL_NAME_MILEAGE__INDEXSTART));
                    break;
                case TABLE_NAME_REFUEL:
                    mCarId = content.getAsLong(COL_NAME_REFUEL__CAR_ID);
                    stopIndex = new BigDecimal(content.getAsString(COL_NAME_REFUEL__INDEX));
                    break;
                case TABLE_NAME_EXPENSE:
                    mCarId = content.getAsLong(COL_NAME_EXPENSE__CAR_ID);
                    String newIndexStr = content.getAsString(COL_NAME_EXPENSE__INDEX);
                    if (newIndexStr != null && newIndexStr.length() > 0) {
                        stopIndex = new BigDecimal(newIndexStr);
                    }
                    break;
                case TABLE_NAME_CAR:  //inactivate/activate the related task-car links/todos
                    String[] whereArgs = {Long.toString(rowId)};
                    ContentValues isActiveContent = new ContentValues();
                    isActiveContent.put(COL_NAME_GEN_ISACTIVE, content.getAsString(COL_NAME_GEN_ISACTIVE));
                    mDb.update(TABLE_NAME_TASK_CAR, isActiveContent, COL_NAME_TASK_CAR__CAR_ID + " = ?", whereArgs);
                    mDb.update(TABLE_NAME_TODO, isActiveContent, COL_NAME_TODO__CAR_ID + " = ?", whereArgs);
                    mDb.update(TABLE_NAME_BTDEVICE_CAR, isActiveContent, COL_NAME_BTDEVICECAR__CAR_ID + " = ?", whereArgs);
                    break;
            }

            if (mCarId != -1 && stopIndex != null) {
                try {
                    mDb.beginTransaction();
                    mDb.update(tableName, content, COL_NAME_GEN_ROWID + "=" + rowId, null);
                    if (tableName.equals(TABLE_NAME_REFUEL)) { //update the corresponding expense record
                        long expenseId = -1;
                        String expenseIdSelect = "SELECT " + COL_NAME_GEN_ROWID + " " + "FROM " + TABLE_NAME_EXPENSE + " " + "WHERE "
                                + COL_NAME_EXPENSE__FROMTABLE + " = 'Refuel' " + "AND " + COL_NAME_EXPENSE__FROMRECORD_ID + " = ?";
                        String[] selectionArgs = {Long.toString(rowId)};
                        Cursor c = execSelectSql(expenseIdSelect, selectionArgs);
                        if (c.moveToFirst()) {
                            expenseId = c.getLong(0);
                        }
                        c.close();
                        if (expenseId != -1) {
                            ContentValues expenseContent;
                            expenseContent = new ContentValues();

                            expenseContent.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, content.getAsString(DBAdapter.COL_NAME_GEN_USER_COMMENT));
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__CAR_ID, content.getAsString(DBAdapter.COL_NAME_REFUEL__CAR_ID));
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__DRIVER_ID, content.getAsString(DBAdapter.COL_NAME_REFUEL__DRIVER_ID));
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__EXPENSECATEGORY_ID,
                                    content.getAsString(DBAdapter.COL_NAME_REFUEL__EXPENSECATEGORY_ID));
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__EXPENSETYPE_ID,
                                    content.getAsString(DBAdapter.COL_NAME_REFUEL__EXPENSETYPE_ID));
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__INDEX, content.getAsString(DBAdapter.COL_NAME_REFUEL__INDEX));

                            BigDecimal price = new BigDecimal(content.getAsString(DBAdapter.COL_NAME_REFUEL__PRICEENTERED));
                            BigDecimal quantity = new BigDecimal(content.getAsString(DBAdapter.COL_NAME_REFUEL__QUANTITYENTERED));
                            BigDecimal amt = (price.multiply(quantity)).setScale(ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT);
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__AMOUNTENTERED, amt.toString());
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__CURRENCYENTERED_ID,
                                    content.getAsString(DBAdapter.COL_NAME_REFUEL__CURRENCYENTERED_ID));
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__CURRENCYRATE, content.getAsString(DBAdapter.COL_NAME_REFUEL__CURRENCYRATE));

                            BigDecimal conversionRate = new BigDecimal(content.getAsString(DBAdapter.COL_NAME_REFUEL__CURRENCYRATE));
                            amt = (amt.multiply(conversionRate)).setScale(ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT);
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__AMOUNT, amt.toString());

                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__CURRENCY_ID, content.getAsString(DBAdapter.COL_NAME_REFUEL__CURRENCY_ID));
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__DATE, content.getAsString(DBAdapter.COL_NAME_REFUEL__DATE));
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__DOCUMENTNO, content.getAsString(DBAdapter.COL_NAME_REFUEL__DOCUMENTNO));
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__FROMTABLE, ConstantValues.EXPENSES_COL_FROM_REFUEL_TABLE_NAME);
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__BPARTNER_ID, content.getAsString(DBAdapter.COL_NAME_REFUEL__BPARTNER_ID));
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__BPARTNER_LOCATION_ID,
                                    content.getAsString(DBAdapter.COL_NAME_REFUEL__BPARTNER_LOCATION_ID));
                            expenseContent.put(DBAdapter.COL_NAME_EXPENSE__TAG_ID, content.getAsString(DBAdapter.COL_NAME_REFUEL__TAG_ID));
                            mDb.update(DBAdapter.TABLE_NAME_EXPENSE, expenseContent, COL_NAME_GEN_ROWID + "=" + expenseId, null);
                        }
                    }

                    updateCarCurrentIndex(mCarId, stopIndex);
                    if (startIndex != null) {
                        updateCarInitIndex(mCarId, startIndex);
                    }
                    else {
                        updateCarInitIndex(mCarId, stopIndex);
                    }
                    mDb.setTransactionSuccessful();
                }
                catch (SQLException | NumberFormatException e) {
                    mErrorMessage = e.getMessage();
                    retVal = R.string.error_000;
                }
                finally {
                    if (mDb.inTransaction()) //issue no: 84
                    {
                        mDb.endTransaction();
                    }
                }
            }
            else {
                mDb.update(tableName, content, COL_NAME_GEN_ROWID + "=" + rowId, null);
            }
        }
        catch (SQLException | NumberFormatException e) {
            mErrorMessage = e.getMessage();
            mException = e;
            retVal = R.string.error_000;
        }
        return retVal;
    }

    /**
     * check update preconditions
     *
     * @return -1 if the row can be deleted, an error code otherwise. For error
     * codes see errors.xml
     */
    private int canUpdate(String tableName, ContentValues content, long rowId) {

        String checkSql;
        Cursor checkCursor;
        if (tableName.equals(TABLE_NAME_UOM)) {
            if (content.containsKey(DBAdapter.COL_NAME_GEN_ISACTIVE) && content.getAsString(DBAdapter.COL_NAME_GEN_ISACTIVE).equals("N")) {
                //check if the uom are used in an active car definition
                checkSql = "SELECT * " +
                            " FROM " + TABLE_NAME_CAR +
                            " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' " +
                                    "AND (" + COL_NAME_CAR__LENGTH_UOM_ID + " = " + rowId +
                                            " OR " + COL_NAME_CAR__FUEL_UOM_ID + " = " + rowId + ") " +
                            " LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_025;
                }
                if (!checkCursor.isClosed()) {
                    checkCursor.close();
                }
            }
        }
        else if (tableName.equals(TABLE_NAME_CURRENCY)) {
            if (content.containsKey(DBAdapter.COL_NAME_GEN_ISACTIVE) && content.getAsString(DBAdapter.COL_NAME_GEN_ISACTIVE).equals("N")) {
                //check if the currency are used in an active car definition
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_CAR + " " + "WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' " + "AND " + COL_NAME_CAR__CURRENCY_ID
                        + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_026;
                }
                if (!checkCursor.isClosed()) {
                    checkCursor.close();
                }
            }
        }
        return -1;
    }

    public void deleteRecords(String tableName, String selection, @Nullable String[] selectionArgs) {
        //    	int checkVal;
        String checkSql = "SELECT * " + " FROM " + tableName;
        if (selection != null) {
            checkSql = checkSql + " WHERE " + selection;
        }
        Cursor c = query(checkSql, selectionArgs);
        while (c.moveToNext()) {
            deleteRecord(tableName, c.getLong(COL_POS_GEN_ROWID));
        }
        c.close();
    }

    /**
     * Delete the record with rowId from tableName
     *
     * @return -1 if the row deleted, an error code otherwise. For error codes
     * see errors.xml
     */
    public int deleteRecord(String tableName, long rowId) {
        int retVal;
        try {
            Cursor c;
            retVal = canDelete(tableName, rowId);
            // 1 -> -1
            if (retVal == -1) {
                switch (tableName) {
                    case TABLE_NAME_MILEAGE: { // update the car current index
                        c = fetchRecord(TABLE_NAME_MILEAGE, COL_LIST_MILEAGE_TABLE, rowId);
                        long carId = c != null ? c.getLong(COL_POS_MILEAGE__CAR_ID) : 0;
                        if (c != null) {
                            c.close();
                        }
                        retVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null));
                        if (retVal == -1) {
                            updateCarCurrentIndex(carId);
                        }
                        //set null in gpstrack table col. mileage id
                        String selection = COL_NAME_GPSTRACK__MILEAGE_ID + "= ?";
                        String[] selectionArgs = {Long.toString(rowId)};
                        c = query(TABLE_NAME_GPSTRACK, COL_LIST_GPSTRACK_TABLE, selection, selectionArgs, null);
                        //                            fetchForTable(GPSTRACK_TABLE_NAME, gpsTrackTableColNames, GPSTRACK_COL_MILEAGE_ID_NAME + "=" + rowId, null);
                        ContentValues cv = new ContentValues();
                        cv.put(COL_NAME_GPSTRACK__MILEAGE_ID, (Long) null);
                        while (c.moveToNext()) {
                            updateRecord(TABLE_NAME_GPSTRACK, c.getLong(COL_POS_GEN_ROWID), cv);
                        }
                        c.close();
                        break;
                    }
                    case TABLE_NAME_REFUEL: {
                        long expenseId = -1;
                        c = fetchRecord(TABLE_NAME_REFUEL, COL_LIST_REFUEL_TABLE, rowId);
                        long carId = c != null ? c.getLong(COL_POS_REFUEL__CAR_ID) : 0;
                        if (c != null) {
                            c.close();
                        }
                        retVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null));
                        if (retVal == -1) {
                            String expenseIdSelect = "SELECT " + COL_NAME_GEN_ROWID + " " + "FROM " + TABLE_NAME_EXPENSE + " " + "WHERE "
                                    + COL_NAME_EXPENSE__FROMTABLE + " = 'Refuel' " + "AND " + COL_NAME_EXPENSE__FROMRECORD_ID + " = ? ";
                            String[] selectionArgs = {Long.toString(rowId)};
                            c = execSelectSql(expenseIdSelect, selectionArgs);
                            if (c.moveToFirst()) {
                                expenseId = c.getLong(0);
                            }
                            c.close();
                            if (expenseId != -1) {
                                mDb.delete(TABLE_NAME_EXPENSE, COL_NAME_GEN_ROWID + "=" + expenseId, null);
                            }
                            updateCarCurrentIndex(carId);
                        }
                        break;
                    }
                    case TABLE_NAME_EXPENSE: {
                        c = fetchRecord(TABLE_NAME_EXPENSE, COL_LIST_EXPENSE_TABLE, rowId);
                        long carId = c != null ? c.getLong(COL_POS_EXPENSE__CAR_ID) : 0;
                        if (c != null) {
                            c.close();
                        }
                        retVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null));
                        if (retVal == -1) {
                            updateCarCurrentIndex(carId);
                        }
                        break;
                    }
                    case TABLE_NAME_GPSTRACK: {
                        //delete gps track details
                        String fileName;
                        String selection = COL_NAME_GPSTRACKDETAIL__GPSTRACK_ID + "= ?";
                        String[] selectionArgs = {Long.toString(rowId)};
                        c = query(TABLE_NAME_GPSTRACKDETAIL, COL_LIST_GPSTRACKDETAIL_TABLE, selection, selectionArgs, null);
                        while (c.moveToNext()) {
                            //delete track files
                            fileName = c.getString(COL_POS_GPSTRACKDETAIL__FILE);
                            if (fileName != null) {
                                FileUtils.deleteFile(fileName);
                            }
                            //delete from gpstrack detail
                            deleteRecord(TABLE_NAME_GPSTRACKDETAIL, c.getInt(COL_POS_GEN_ROWID));
                        }
                        c.close();
                        retVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null));
                        break;
                    }
                    case TABLE_NAME_CURRENCY:
                        long currRateId;
                        retVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null));
                        if (retVal == -1) {
                            String currencyRateSelect = "SELECT " + COL_NAME_GEN_ROWID + " " + "FROM " + TABLE_NAME_CURRENCYRATE + " " + "WHERE "
                                    + COL_NAME_CURRENCYRATE__FROMCURRENCY_ID + " = ? " + " OR " + COL_NAME_CURRENCYRATE__TOCURRENCY_ID + " = ? ";
                            String[] selectionArgs = {Long.toString(rowId), Long.toString(rowId)};
                            c = execSelectSql(currencyRateSelect, selectionArgs);
                            while (c.moveToNext()) {
                                currRateId = c.getLong(0);
                                mDb.delete(TABLE_NAME_CURRENCYRATE, COL_NAME_GEN_ROWID + "=" + currRateId, null);
                            }
                            c.close();
                        }
                        break;
                    case TABLE_NAME_BPARTNER:
                        //also delete the locations
                        mDb.delete(TABLE_NAME_BPARTNERLOCATION, COL_NAME_BPARTNERLOCATION__BPARTNER_ID + "=" + rowId, null);
                        retVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null));
                        break;
                    case TABLE_NAME_CAR:
                        //also delete the locations
                        mDb.delete(TABLE_NAME_TASK_CAR, COL_NAME_TASK_CAR__CAR_ID + "=" + rowId, null);
                        mDb.delete(TABLE_NAME_TODO, COL_NAME_TODO__CAR_ID + "=" + rowId, null);
                        mDb.delete(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID + "=" + rowId, null);
                        mDb.delete(TABLE_NAME_BTDEVICE_CAR, COL_NAME_BTDEVICECAR__CAR_ID + "=" + rowId, null);

                        retVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null));
                        break;
                    case TABLE_NAME_TASK:
                        //also delete the locations
                        mDb.delete(TABLE_NAME_TASK_CAR, COL_NAME_TASK_CAR__TASK_ID + "=" + rowId, null);
                        mDb.delete(TABLE_NAME_TODO, COL_NAME_TODO__TASK_ID + "=" + rowId, null);
                        retVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null));
                        break;
                    case TABLE_NAME_EXPENSETYPE:
                        mDb.delete(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID + "=" + rowId, null);

                        retVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null));
                        break;
                    default:
                        retVal = (-1 * mDb.delete(tableName, COL_NAME_GEN_ROWID + "=" + rowId, null));
                        break;
                }
            }
        }
        catch (SQLException | NumberFormatException e) {
            mErrorMessage = e.getMessage();
            mException = e;
            retVal = R.string.error_000;
        }
        return retVal;
    }

    /**
     * check deletion preconditions (referential integrity, etc.)
     *
     * @return -1 if the row can be deleted, an error code otherwise. For error
     * codes see errors.xml
     */
    private int canDelete(String tableName, long rowId) {

        String checkSql;
        Cursor checkCursor;
        switch (tableName) {
            case TABLE_NAME_DRIVER:
                //check if exists mileage for this driver
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_MILEAGE + " " + "WHERE " + COL_NAME_MILEAGE__DRIVER_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_009;
                }
                checkCursor.close();
                //check refuels
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_REFUEL + " " + "WHERE " + COL_NAME_REFUEL__DRIVER_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_010;
                }
                checkCursor.close();
                //check expenses
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_EXPENSE + " " + "WHERE " + COL_NAME_EXPENSE__DRIVER_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_039;
                }
                checkCursor.close();
                break;
            case TABLE_NAME_CAR:
                //check if exists mileage for this driver
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_MILEAGE + " " + "WHERE " + COL_NAME_MILEAGE__CAR_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_011;
                }
                checkCursor.close();
                //check refuels
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_REFUEL + " " + "WHERE " + COL_NAME_REFUEL__CAR_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_012;
                }
                checkCursor.close();
                //check expenses
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_EXPENSE + " " + "WHERE " + COL_NAME_EXPENSE__CAR_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_040;
                }
                checkCursor.close();
                break;
            case TABLE_NAME_UOM:
                //check if exists mileage for this driver
                checkSql = "SELECT * " +
                            " FROM " + TABLE_NAME_MILEAGE +
                            " WHERE " + COL_NAME_MILEAGE__UOMLENGTH_ID + " = " + rowId + " " +
                            " LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_013;
                }
                checkCursor.close();
                //check refuels
                checkSql = "SELECT * " +
                            " FROM " + TABLE_NAME_REFUEL +
                            " WHERE " + COL_NAME_REFUEL__UOMVOLUME_ID + " = " + rowId +
                                    " OR " + COL_NAME_REFUEL__UOMVOLUMEENTERED_ID + " = " + rowId +
                            " LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_014;
                }
                checkCursor.close();
                //check uom conversions
                checkSql = "SELECT * " +
                            " FROM " + TABLE_NAME_UOMCONVERSION +
                            " WHERE " + COL_NAME_UOMCONVERSION__UOMFROM_ID + " = " + rowId +
                                    " OR " + COL_NAME_UOMCONVERSION__UOMTO_ID + " = " + rowId +
                            " LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_015;
                }
                checkCursor.close();

                //check car definitions
                checkSql = "SELECT * " +
                            " FROM " + TABLE_NAME_CAR +
                            " WHERE " + COL_NAME_GEN_ISACTIVE + " = 'Y' " +
                                    "AND (" + COL_NAME_CAR__LENGTH_UOM_ID + " = " + rowId +
                                            " OR " + COL_NAME_CAR__FUEL_UOM_ID + " = " + rowId + ") " +
                            " LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_125;
                }

                break;
            case TABLE_NAME_CURRENCY:
                //check cars
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_CAR + " " + "WHERE " + COL_NAME_CAR__CURRENCY_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_016;
                }
                checkCursor.close();
                //check refuels
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_REFUEL + " " + "WHERE " + COL_NAME_REFUEL__CURRENCY_ID + " = " + rowId + " " + " OR "
                        + COL_NAME_REFUEL__CURRENCYENTERED_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_017;
                }
                checkCursor.close();
                //check expenses
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_EXPENSE + " " + "WHERE " + COL_NAME_EXPENSE__CURRENCY_ID + " = " + rowId + " " + " OR "
                        + COL_NAME_EXPENSE__CURRENCYENTERED_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_038;
                }
                checkCursor.close();
                break;
            case TABLE_NAME_EXPENSETYPE:
                //check if exists mileage for this driver
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_MILEAGE + " " + "WHERE " + COL_NAME_MILEAGE__EXPENSETYPE_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_018;
                }
                checkCursor.close();
                //check refuels
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_REFUEL + " " + "WHERE " + COL_NAME_REFUEL__EXPENSETYPE_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_019;
                }
                checkCursor.close();
                //check expenses
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_EXPENSE + " " + "WHERE " + COL_NAME_EXPENSE__EXPENSETYPE_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_041;
                }
                checkCursor.close();
                break;
            case TABLE_NAME_EXPENSECATEGORY:
                //check refuels
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_REFUEL + " " + "WHERE " + COL_NAME_REFUEL__EXPENSECATEGORY_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_027;
                }
                checkCursor.close();
                //check expenses
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_EXPENSE + " " + "WHERE " + COL_NAME_EXPENSE__EXPENSECATEGORY_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_028;
                }
                checkCursor.close();
                break;
            case TABLE_NAME_EXPENSE:
                //check refuels
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_REFUEL + " " + "WHERE " + COL_NAME_GEN_ROWID + " = " + "( SELECT " + COL_NAME_EXPENSE__FROMRECORD_ID
                        + " " + "FROM " + TABLE_NAME_EXPENSE + " " + "WHERE " + COL_NAME_GEN_ROWID + " = " + rowId + " " + " AND " + COL_NAME_EXPENSE__FROMTABLE
                        + " = '" + ConstantValues.EXPENSES_COL_FROM_REFUEL_TABLE_NAME + "' ) " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_030;
                }
                checkCursor.close();
                break;
            case TABLE_NAME_BPARTNER:
                //check refuels
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_REFUEL + " " + "WHERE " + COL_NAME_REFUEL__BPARTNER_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_042;
                }
                checkCursor.close();
                //check expenses
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_EXPENSE + " " + "WHERE " + COL_NAME_EXPENSE__BPARTNER_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_043;
                }
                checkCursor.close();
                break;
            case TABLE_NAME_BPARTNERLOCATION:
                //check refuels
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_REFUEL + " " + "WHERE " + COL_NAME_REFUEL__BPARTNER_LOCATION_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_044;
                }
                checkCursor.close();
                //check expenses
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_EXPENSE + " " + "WHERE " + COL_NAME_EXPENSE__BPARTNER_LOCATION_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_045;
                }
                checkCursor.close();
                break;
            case TABLE_NAME_TAG:
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_MILEAGE + " " + "WHERE " + COL_NAME_MILEAGE__TAG_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_046;
                }
                checkCursor.close();

                checkSql = "SELECT * " + "FROM " + TABLE_NAME_REFUEL + " " + "WHERE " + COL_NAME_REFUEL__TAG_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_047;
                }
                checkCursor.close();

                checkSql = "SELECT * " + "FROM " + TABLE_NAME_EXPENSE + " " + "WHERE " + COL_NAME_EXPENSE__TAG_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_048;
                }
                checkCursor.close();

                checkSql = "SELECT * " + "FROM " + TABLE_NAME_GPSTRACK + " " + "WHERE " + COL_NAME_GPSTRACK__TAG_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_049;
                }
                checkCursor.close();
                break;
            case TABLE_NAME_TASKTYPE:
                checkSql = "SELECT * " + "FROM " + TABLE_NAME_TASK + " " + "WHERE " + COL_NAME_TASK__TASKTYPE_ID + " = " + rowId + " " + "LIMIT 1";
                checkCursor = mDb.rawQuery(checkSql, null);
                if (checkCursor.moveToFirst()) { //record exists
                    checkCursor.close();
                    return R.string.error_060;
                }
                checkCursor.close();
                break;
        }

        return -1;
    }

    /**
     * check some preconditions for inserting/updating UOM conversions in order
     * to prevent duplicates
     *
     * @return -1 if uom conversion can be added/updated. An error code
     * otherwise. For error codes see errors.xml
     */
    public int canInsertUpdateUOMConversion(Long rowId, long fromId, long toId) {
        //check for duplicates
        if (fromId == -1) {
            return R.string.error_111;
        }
        if (toId == -1) {
            return R.string.error_112;
        }

        String sql = "SELECT * " + " FROM " + DBAdapter.TABLE_NAME_UOMCONVERSION + " WHERE " + DBAdapter.COL_NAME_UOMCONVERSION__UOMFROM_ID + " = "
                + fromId + " AND " + DBAdapter.COL_NAME_UOMCONVERSION__UOMTO_ID + " = " + toId;
        if (rowId != null) {
            sql = sql + " AND " + DBAdapter.COL_NAME_GEN_ROWID + " <> " + rowId.toString();
        }
        Cursor resultCursor = mDb.rawQuery(sql, null);
        if (resultCursor.getCount() > 0) {
            resultCursor.close();
            return R.string.error_005;
        }
        resultCursor.close();
        return -1;
    }

    /**
     * check some preconditions for inserting/updating the car start/stop index
     *
     * @return -1 if index OK. An error code otherwise. For error codes see
     * errors.xml
     */
    public int checkIndex(long rowId, long carId, BigDecimal startIndex, BigDecimal stopIndex) {

        if (stopIndex != null && stopIndex.compareTo(startIndex) <= 0) {
            return R.string.error_004;
        }

        String checkSql;
        checkSql = "SELECT * " + " FROM " + TABLE_NAME_MILEAGE + " WHERE " + COL_NAME_MILEAGE__CAR_ID + "=" + carId + " AND " + COL_NAME_MILEAGE__INDEXSTART
                + " <= " + startIndex.toString() + " AND " + COL_NAME_MILEAGE__INDEXSTOP + " > " + startIndex.toString();
        if (rowId >= 0) {
            checkSql = checkSql + " AND " + COL_NAME_GEN_ROWID + "<>" + rowId;
        }

        Cursor checkCursor = mDb.rawQuery(checkSql, null);
        if (checkCursor.getCount() > 0) {
            checkCursor.close();
            return R.string.error_001;
        }
        checkCursor.close();

        if (stopIndex != null) {
            checkSql = "SELECT * " + " FROM " + TABLE_NAME_MILEAGE + " WHERE " + COL_NAME_MILEAGE__CAR_ID + "=" + carId + " AND " + COL_NAME_MILEAGE__INDEXSTART
                    + " < " + stopIndex.toString() + " AND " + COL_NAME_MILEAGE__INDEXSTOP + " >= " + stopIndex.toString();
            if (rowId >= 0) {
                checkSql = checkSql + " AND " + COL_NAME_GEN_ROWID + "<>" + rowId;
            }
            checkCursor = mDb.rawQuery(checkSql, null);
            if (checkCursor.getCount() > 0) {
                checkCursor.close();
                return R.string.error_002;
            }
            checkCursor.close();

            checkSql = "SELECT * " + " FROM " + TABLE_NAME_MILEAGE + " WHERE " + COL_NAME_MILEAGE__CAR_ID + "=" + carId + " AND " + COL_NAME_MILEAGE__INDEXSTART
                    + " >= " + startIndex.toString() + " AND " + COL_NAME_MILEAGE__INDEXSTOP + " <= " + stopIndex.toString();
            if (rowId >= 0) {
                checkSql = checkSql + " AND " + COL_NAME_GEN_ROWID + "<>" + rowId;
            }

            checkCursor = mDb.rawQuery(checkSql, null);
            if (checkCursor.getCount() > 0) {
                checkCursor.close();
                return R.string.error_003;
            }
            checkCursor.close();
        }

        return -1;
    }

    /**
     * Get a list of the recent user comments used in the fromTable
     *
     * @return a string array containing the last limitCount user comment from
     * fromTable for the carId and DriverId
     * @param  completeWhereClause: the complete where clause for the select (including the WHERE keyword!). If null, default where clauses will be used
     */
    @Nullable
    public String[] getAutoCompleteText(String fromTable, @Nullable String fromColumn, String completeWhereClause, long whereId, int limitCount) {
        String[] retVal;
        ArrayList<String> commentList = new ArrayList<>();
        String selectSql;

        if(completeWhereClause != null)
            completeWhereClause = " " + completeWhereClause;

        switch (fromTable) {
            case TABLE_NAME_MILEAGE:
                selectSql =
                        "SELECT DISTINCT " + COL_NAME_GEN_USER_COMMENT +
                        " FROM " + TABLE_NAME_MILEAGE;
                if(completeWhereClause != null)
                    selectSql = selectSql + completeWhereClause;
                else
                    selectSql = selectSql +
                        " WHERE " +
                                COL_NAME_MILEAGE__CAR_ID + " = " + whereId +
                                " AND " + COL_NAME_GEN_USER_COMMENT + " IS NOT NULL " +
                                " AND LENGTH(TRIM(" + COL_NAME_GEN_USER_COMMENT + ")) > 0 " +
                        " GROUP BY " + COL_NAME_GEN_USER_COMMENT +
                        " ORDER BY MAX(" + COL_NAME_MILEAGE__INDEXSTOP + ") DESC " +
                        " LIMIT " + limitCount;
                break;
            case TABLE_NAME_REFUEL:
                selectSql =
                        "SELECT DISTINCT " + COL_NAME_GEN_USER_COMMENT +
                        " FROM " + TABLE_NAME_REFUEL;
                if(completeWhereClause != null)
                    selectSql = selectSql + completeWhereClause;
                else
                    selectSql = selectSql +
                        " WHERE " +
                                COL_NAME_REFUEL__CAR_ID + " = " + whereId +
                                " AND " + COL_NAME_GEN_USER_COMMENT + " IS NOT NULL " +
                                " AND LENGTH(TRIM(" + COL_NAME_GEN_USER_COMMENT + ")) > 0 " +
                        " GROUP BY " + COL_NAME_GEN_USER_COMMENT +
                        " ORDER BY MAX(" + COL_NAME_REFUEL__DATE + ") DESC " +
                        " LIMIT " + limitCount;
                break;
            case TABLE_NAME_EXPENSE:
                selectSql =
                        "SELECT DISTINCT " + COL_NAME_GEN_USER_COMMENT +
                        " FROM " + TABLE_NAME_EXPENSE;
                if(completeWhereClause != null)
                    selectSql = selectSql + completeWhereClause;
                else
                    selectSql = selectSql +
                        " WHERE " +
                                COL_NAME_EXPENSE__CAR_ID + " = " + whereId +
                                " AND " + COL_NAME_GEN_USER_COMMENT + " IS NOT NULL " +
                                " AND LENGTH(TRIM(" + COL_NAME_GEN_USER_COMMENT + ")) > 0 " +
                        " GROUP BY " + COL_NAME_GEN_USER_COMMENT +
                        " ORDER BY MAX(" + COL_NAME_EXPENSE__DATE + ") DESC " +
                        " LIMIT " + limitCount;
                break;
            case TABLE_NAME_GPSTRACK:
                selectSql =
                        "SELECT DISTINCT " + COL_NAME_GEN_USER_COMMENT +
                        " FROM " + TABLE_NAME_GPSTRACK;
                if(completeWhereClause != null)
                    selectSql = selectSql + completeWhereClause;
                else
                    selectSql = selectSql +
                        " WHERE " +
                                COL_NAME_GPSTRACK__CAR_ID + " = " + whereId +
                                " AND " + COL_NAME_GEN_USER_COMMENT + " IS NOT NULL " +
                                " AND LENGTH(TRIM(" + COL_NAME_GEN_USER_COMMENT + ")) > 0 " +
                        " GROUP BY " + COL_NAME_GEN_USER_COMMENT +
                        " ORDER BY MAX(" + COL_NAME_GPSTRACK__DATE + ") DESC " +
                        " LIMIT " + limitCount;
                break;
            case TABLE_NAME_BPARTNER:
                selectSql =
                        "SELECT DISTINCT " + COL_NAME_GEN_NAME +
                        " FROM " + TABLE_NAME_BPARTNER;
                if(completeWhereClause != null)
                    selectSql = selectSql + completeWhereClause;
                else
                    selectSql = selectSql +
                        " WHERE " +
                                COL_NAME_GEN_ISACTIVE + " = 'Y' " +
                                " AND LENGTH(TRIM(" + COL_NAME_GEN_NAME + ")) > 0 " +
                        " GROUP BY " + COL_NAME_GEN_NAME +
                        " ORDER BY MAX(LOWER(" + COL_NAME_GEN_NAME + "))";
                break;
            case TABLE_NAME_BPARTNERLOCATION:
                selectSql =
                        "SELECT DISTINCT " + fromColumn +
                        " FROM " + TABLE_NAME_BPARTNERLOCATION;
                if(completeWhereClause != null)
                    selectSql = selectSql + completeWhereClause;
                else
                    selectSql = selectSql +
                        " WHERE " +
                                COL_NAME_GEN_ISACTIVE + " = \'Y\' " +
                                " AND LENGTH(TRIM(" + fromColumn + ")) > 0";
                if (whereId != -1) {
                    selectSql = selectSql +
                                " AND " + COL_NAME_BPARTNERLOCATION__BPARTNER_ID + " = " + whereId;
                }
                selectSql = selectSql +
                        " GROUP BY " + fromColumn +
                        " ORDER BY MAX(LOWER(" + fromColumn + "))";

                break;
            case TABLE_NAME_TAG:
                selectSql =
                        "SELECT " + COL_NAME_GEN_NAME +
                        " FROM " + TABLE_NAME_TAG;
                if(completeWhereClause != null)
                    selectSql = selectSql + completeWhereClause;
                else
                    selectSql = selectSql +
                        " WHERE " +
                                COL_NAME_GEN_ISACTIVE + " = \'Y\' " +
                                " AND LENGTH(TRIM(" + COL_NAME_GEN_NAME + ")) > 0 " +
                        " GROUP BY " + COL_NAME_GEN_NAME +
                        " ORDER BY MAX(LOWER(" + COL_NAME_GEN_NAME + "))";
                break;
            default:
                return null;
        }
        Cursor commentCursor = mDb.rawQuery(selectSql, null);
        while (commentCursor.moveToNext()) {
            commentList.add(commentCursor.getString(0));
        }
        commentCursor.close();
        retVal = new String[commentList.size()];
        commentList.toArray(retVal);
        return retVal;
    }

    @Nullable
    public BigDecimal getCurrencyRate(long fromCurrencyId, long toCurrencyId) {
        BigDecimal retVal = null;
        String retValStr = null;
        if (fromCurrencyId == toCurrencyId) {
            return BigDecimal.ONE;
        }
        try {
            String selectSql;
            Cursor selectCursor;

            //@formatter:off
            selectSql = " SELECT * " +
                        " FROM " + TABLE_NAME_CURRENCYRATE +
                        " WHERE " + COL_NAME_GEN_ISACTIVE + "='Y' " +
                                " AND " + COL_NAME_CURRENCYRATE__FROMCURRENCY_ID + " = ? " +
                                " AND " + COL_NAME_CURRENCYRATE__TOCURRENCY_ID + " = ?";
            //@formatter:on
            String[] selectionArgs = {Long.toString(fromCurrencyId), Long.toString(toCurrencyId)};
            selectCursor = execSelectSql(selectSql, selectionArgs);
            if (selectCursor.moveToFirst()) {
                retValStr = selectCursor.getString(COL_POS_CURRENCYRATE__RATE);
            }
            selectCursor.close();
            if (retValStr != null && retValStr.length() > 0) {
                return new BigDecimal(retValStr);
            }

            selectSql = " SELECT * " + " FROM " + TABLE_NAME_CURRENCYRATE + " WHERE " + COL_NAME_GEN_ISACTIVE + "='Y' " + " AND "
                    + COL_NAME_CURRENCYRATE__TOCURRENCY_ID + " = ? " + " AND " + COL_NAME_CURRENCYRATE__FROMCURRENCY_ID + " = ?";
            selectCursor = execSelectSql(selectSql, selectionArgs);
            if (selectCursor.moveToFirst()) {
                retValStr = selectCursor.getString(COL_POS_CURRENCYRATE__RATE);
            }
            selectCursor.close();
            if (retValStr != null && retValStr.length() > 0) {
                retVal = new BigDecimal(retValStr);
                if (retVal.signum() != 0) {
                    return BigDecimal.ONE.divide(retVal, 10, RoundingMode.HALF_UP).setScale(ConstantValues.DECIMALS_RATES, ConstantValues.ROUNDING_MODE_RATES);
                }
                else {
                    return BigDecimal.ZERO;
                }
            }
        }
        catch (NumberFormatException ignored) {
        }
        return retVal;
    }

    @Nullable
    public BigDecimal getUOMConversionRate(long fromId, long toId) {
        String retValStr = null;
        if (fromId == toId) {
            return BigDecimal.ONE;
        }
        try {
            String selectSql;
            Cursor selectCursor;

            selectSql = " SELECT * " + " FROM " + TABLE_NAME_UOMCONVERSION + " WHERE " + COL_NAME_GEN_ISACTIVE + "='Y' " + " AND "
                    + COL_NAME_UOMCONVERSION__UOMFROM_ID + " = ? " + " AND " + COL_NAME_UOMCONVERSION__UOMTO_ID + " = ?";
            String[] selectionArgs = {Long.toString(fromId), Long.toString(toId)};
            selectCursor = execSelectSql(selectSql, selectionArgs);
            if (selectCursor.moveToFirst()) {
                retValStr = selectCursor.getString(COL_POS_UOMCONVERSION__RATE);
            }
            selectCursor.close();

            if (retValStr != null && retValStr.length() > 0) {
                return new BigDecimal(retValStr);
            }
        }
        catch (NumberFormatException ignored) {
        }
        return null;
    }

    /**
     * @param carID     : Car ID
     * @param expTypeID : Expense Type ID
     * @param date      Date in seconds
     */
    public BigDecimal getReimbursementRate(long carID, long expTypeID, long date) {
        BigDecimal retVal = BigDecimal.ZERO;
        Cursor selectCursor = null;

        try {
            String selectSql = "SELECT " + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__RATE) + " FROM "
                    + TABLE_NAME_REIMBURSEMENT_CAR_RATES + " JOIN " + TABLE_NAME_EXPENSETYPE + " ON "
                    + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID) + " = "
                    + sqlConcatTableColumn(TABLE_NAME_EXPENSETYPE, COL_NAME_GEN_ROWID) + " WHERE "
                    + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__CAR_ID) + " = ? " + " AND "
                    + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__EXPENSETYPE_ID) + "=? " + " AND "
                    + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + "<=? " + " AND "
                    + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDTO) + ">=? " + " ORDER BY "
                    + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_REIMBURSEMENT_CAR_RATES__VALIDFROM) + " DESC, "
                    + sqlConcatTableColumn(TABLE_NAME_REIMBURSEMENT_CAR_RATES, COL_NAME_GEN_ROWID) + " DESC" + " LIMIT 1";
            String[] selectionArgs = {Long.toString(carID), Long.toString(expTypeID), Long.toString(date), Long.toString(date)};
            selectCursor = execSelectSql(selectSql, selectionArgs);
            if (selectCursor.moveToFirst()) {
                retVal = new BigDecimal(selectCursor.getString(0));
            }
        }
        catch (NumberFormatException ignored) {
        }
        finally {
            try {
                if (selectCursor != null) {
                    selectCursor.close();
                }
            }
            catch (Exception ignored) {
            }
        }
        return retVal;
    }

    /**
     * returns the ID for the base or alternative fuel
     *
     * @param carID
     * @param baseFuel base or alternative fuel
     * @return ID of ExpensetType (fuel)
     */
    public long getCarUOMFuelID(long carID, boolean baseFuel) {
        long retVal = -1;

        if(!baseFuel && !isAFVCar(carID))
            { return retVal; }

        Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, carID);
        if (c != null) {
            if(baseFuel)
                { retVal = c.getLong(COL_POS_CAR__FUEL_UOM_ID); }
            else
                { retVal = c.getLong(COL_POS_CAR__ALTERNATIVE_FUEL_UOM_ID); }
            c.close();
        }
        return retVal;

    }

    public long getCarUOMLengthID(long carID) {
        Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, carID);
        long retVal = -1;
        if (c != null) {
            retVal = c.getLong(COL_POS_CAR__LENGTH_UOM_ID);
            c.close();
        }
        return retVal;
    }

    public long getCarCurrencyID(long carID) {
        Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, carID);
        long retVal = -1;
        if (c != null) {
            retVal = c.getLong(COL_POS_CAR__CURRENCY_ID);
            c.close();
        }
        return retVal;
    }

    @Nullable
    public String getCarName(long carID) {
        Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, carID);
        String retVal = null;
        if (c != null) {
            retVal = c.getString(COL_POS_GEN_NAME);
            c.close();
        }
        return retVal;
    }

    public boolean isAFVCar(long carID) {
        boolean retVal = false;
        Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, carID);
        if (c != null) {
            retVal = c.getString(COL_POS_CAR__ISAFV).equals("Y");
            c.close();
        }
        return retVal;
    }

//    public long getAlternateFuelID(long carID) {
//        long retVal = -1;
//        //if not alternate fuel vehicle
//        if(!isAFVCar(carID))
//            { return retVal; }
//
//        Cursor c = fetchRecord(TABLE_NAME_CAR, COL_LIST_CAR_TABLE, carID);
//        if (c != null) {
//            retVal = c.getLong(COL_POS_CAR__ALTERNATIVE_FUEL_ID);
//            c.close();
//        }
//        return retVal;
//    }
//
    public String getCarFuelUOMType(long carID, boolean baseFuel) {
        return getUOMType(getCarUOMFuelID(carID, baseFuel));
    }

    public String getUOMType(long uomID) {
        String retVal = ConstantValues.UOM_TYPE_VOLUME_CODE;

        Cursor c = fetchRecord(TABLE_NAME_UOM, COL_LIST_UOM_TABLE, uomID );
        if (c != null) {
            retVal = c.getString(COL_POS_UOM__UOMTYPE);
            c.close();
        }
        return retVal;
    }

    public String getFuelUOMType(long fuelID) {
        String retVal = ConstantValues.UOM_TYPE_VOLUME_CODE;

        Cursor c = fetchRecord(TABLE_NAME_EXPENSECATEGORY, COL_LIST_EXPENSECATEGORY_TABLE, fuelID);
        if (c != null) {
            retVal = c.getString(COL_POS_EXPENSECATEGORY__UOMTYPE);
            c.close();
        }
        return retVal;
    }

    public String getNameById(String tableName, long id) {
        String retVal = null;
        String selection = COL_NAME_GEN_ROWID + " = ?";
        String[] selectionArgs = {Long.toString(id)};
        Cursor c = query(tableName, COL_LIST_GEN_ROWID_NAME, selection, selectionArgs, null);

        if (c.moveToFirst()) {
            retVal = c.getString(1);
        }
        c.close();
        return retVal;
//
//        Cursor c = fetchRecord(tableNAme, {COL_POS_GEN_NAME}, id);
//        String retVal = null;
//        if (c != null) {
//            retVal = c.getString(COL_POS_GEN_NAME);
//            c.close();
//        }
//        return retVal;
    }

    public Cursor query(String table, String[] columns, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String orderBy) {
        //TODO check why mDb become null when rotating the screen while the GPSTrackController dialog is shown
        try {
            return mDb.query(table, columns, selection, selectionArgs, null, null, orderBy);
        } catch (Exception e) {
            Log.d("DBAdapter", e.getMessage());
            return null;
        }
    }

    @Nullable
    public String getCurrencyCode(long currencyID) {
        Cursor c = fetchRecord(DBAdapter.TABLE_NAME_CURRENCY, DBAdapter.COL_LIST_CURRENCY_TABLE, currencyID);
        String retVal = null;
        if (c != null) {
            retVal = c.getString(DBAdapter.COL_POS_CURRENCY__CODE);
            c.close();
        }
        return retVal;
    }

    @Nullable
    public String getUOMCode(long uomID) {
        Cursor c = fetchRecord(DBAdapter.TABLE_NAME_UOM, DBAdapter.COL_LIST_UOM_TABLE, uomID);
        String retVal = null;
        if (c != null) {
            retVal = c.getString(DBAdapter.COL_POS_UOM__CODE);
            c.close();
        }
        return retVal;
    }

    @Nullable
    public String getUOMName(long uomID) {
        Cursor c = fetchRecord(DBAdapter.TABLE_NAME_UOM, DBAdapter.COL_LIST_UOM_TABLE, uomID);
        String retVal = null;
        if (c != null) {
            retVal = c.getString(DBAdapter.COL_POS_GEN_NAME);
            c.close();
        }
        return retVal;
    }

    @SuppressWarnings("SameParameterValue")
    public long getIdByCode(String tableName, String code) {
        String selection = "Code = ?";
        String[] selectionArgs = {code};
        Cursor c = query(tableName, COL_LIST_GEN_ROWID, selection, selectionArgs, null);
        long retVal = -1;
        if (c.moveToFirst()) {
            retVal = c.getLong(0);
        }
        c.close();
        return retVal;
    }

    public long getIdByName(String tableName, String name) {
        String selection = COL_NAME_GEN_NAME + " = ?";
        String[] selectionArgs = {name};
        Cursor c = query(tableName, COL_LIST_GEN_ROWID, selection, selectionArgs, null);
        long retVal = -1;
        if (c.moveToFirst()) {
            retVal = c.getLong(0);
        }
        c.close();
        return retVal;
    }

    /**
     * get the start index for a new mileage record
     */
    public BigDecimal getCarLastMileageIndex(long mCarId) {
        Double mStartIndexStr = null;
        //@formatter:off
        String sql =
                "SELECT MAX( " + DBAdapter.COL_NAME_MILEAGE__INDEXSTOP + "), 1 As Pos " +
                "FROM " + DBAdapter.TABLE_NAME_MILEAGE + " " +
                "WHERE "
                    + DBAdapter.COL_NAME_GEN_ISACTIVE + " = 'Y' " + "AND " + DBAdapter.COL_NAME_MILEAGE__CAR_ID + " = ? " + "UNION " + "SELECT "
                    + DBAdapter.COL_NAME_CAR__INDEXCURRENT + ", 2 As Pos " + "FROM " + DBAdapter.TABLE_NAME_CAR + " " + "WHERE "
                    + DBAdapter.COL_NAME_GEN_ROWID + " = ? " +
                "ORDER BY Pos ASC";
        //@formatter:on
        String[] selectionArgs = {Long.toString(mCarId), Long.toString(mCarId)};
        Cursor c = execSelectSql(sql, selectionArgs);
        if (c.moveToFirst() && c.getString(0) != null) {
            mStartIndexStr = c.getDouble(0);
        }
        if ((mStartIndexStr == null) && c.moveToNext() && c.getString(0) != null) {
            mStartIndexStr = c.getDouble(0);
        }
        if (mStartIndexStr == null) {
            mStartIndexStr = Double.valueOf("0");
        }
        c.close();
        return new BigDecimal(mStartIndexStr).setScale(ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH);
    }

    /**
     * get the current index of the car
     */
    public BigDecimal getCarCurrentIndex(long mCarId) {
        Double mStartIndex = null;
        //@formatter:off
        String sql =
                "SELECT " + DBAdapter.COL_NAME_CAR__INDEXCURRENT +
                " FROM " + DBAdapter.TABLE_NAME_CAR + " " +
                " WHERE " + DBAdapter.COL_NAME_GEN_ROWID + " = ? ";
        //@formatter:on
        String[] selectionArgs = {Long.toString(mCarId)};
        Cursor c = execSelectSql(sql, selectionArgs);
        if (c.moveToFirst() && c.getString(0) != null) {
            mStartIndex = c.getDouble(0);
        }
        c.close();
        if (mStartIndex == null) {
            return null;
        }
        else {
            return new BigDecimal(mStartIndex).setScale(ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH);
        }
    }

    /**
     * @param mCarId
     * @return the first record date in seconds
     */
    public long getCarFirstSeenDate(long mCarId) {
        Long firstSeenInSeconds = null;
        //@formatter:off
        String sql =
                "SELECT MIN(FirstSeen) " +
                " FROM ( " +
                    " SELECT MIN(" + DBAdapter.COL_NAME_MILEAGE__DATE + ") AS FirstSeen" +
                    " FROM " + DBAdapter.TABLE_NAME_MILEAGE + " " +
                    " WHERE " + DBAdapter.COL_NAME_MILEAGE__CAR_ID + " = ? " +
                    " UNION " +
                    " SELECT MIN(" + DBAdapter.COL_NAME_REFUEL__DATE + ") AS FirstSeen" +
                    " FROM " + DBAdapter.TABLE_NAME_REFUEL + " " +
                    " WHERE " + DBAdapter.COL_NAME_REFUEL__CAR_ID + " = ? " +
                    " UNION " +
                    " SELECT MIN(" + DBAdapter.COL_NAME_EXPENSE__DATE + ") AS FirstSeen" +
                    " FROM " + DBAdapter.TABLE_NAME_EXPENSE + " " +
                    " WHERE " + DBAdapter.COL_NAME_EXPENSE__CAR_ID + " = ? " +
                    " )";
        //@formatter:on
        String[] selectionArgs = {Long.toString(mCarId), Long.toString(mCarId), Long.toString(mCarId)};
        Cursor c = execSelectSql(sql, selectionArgs);
        if (c.moveToFirst() && c.getString(0) != null) {
            firstSeenInSeconds = c.getLong(0);
        }
        c.close();
        if (firstSeenInSeconds == null) {
            return -1;
        }
        else {
            return firstSeenInSeconds;
        }
    }

    public BigDecimal getLastDoneTodoMileage(long mCarId, long mTaskId) {
        Double lastDoneIndex = null;
        //@formatter:off
        String sql =
                "SELECT " + DBAdapter.COL_NAME_TODO__DUEMILEAGE +
                        " FROM " + DBAdapter.TABLE_NAME_TODO + " " +
                        " WHERE " + DBAdapter.COL_NAME_TODO__CAR_ID + " = ? " +
                        " AND " + DBAdapter.COL_NAME_TODO__TASK_ID + " = ? " +
                        " AND " + DBAdapter.COL_NAME_TODO__ISDONE + " = 'Y' " +
                        " ORDER BY " + DBAdapter.COL_NAME_TODO__DUEMILEAGE + " DESC " +
                        " LIMIT 1";
        //@formatter:on
        String[] selectionArgs = {Long.toString(mCarId), Long.toString(mTaskId)};
        Cursor c = execSelectSql(sql, selectionArgs);
        if (c.moveToFirst() && c.getString(0) != null) {
            lastDoneIndex = c.getDouble(0);
        }
        c.close();
        if (lastDoneIndex == null) {
            return null;
        } else {
            return new BigDecimal(lastDoneIndex).setScale(ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH);
        }
    }

    /**
     * check if only one active record exist in a given table
     *
     * @param table the table where we look
     * @return if one record exists the id of the record, otherwise -1
     */
    public long isSingleActiveRecord(String table) {
        String selectSql;
        Cursor selectCursor;
        long retVal = -1;

        //if max == min => one single record
        //@formatter:off
        selectSql = " SELECT MAX(" + DBAdapter.COL_NAME_GEN_ROWID + "), MIN(" + DBAdapter.COL_NAME_GEN_ROWID + ") " +
                " FROM " + table +
                " WHERE " + DBAdapter.COL_NAME_GEN_ISACTIVE + "='Y' ";
        //@formatter:on

        selectCursor = execSelectSql(selectSql, null);
        if (selectCursor.moveToFirst()) {
            if (selectCursor.getLong(0) == selectCursor.getLong(1)) { // one single active record
                retVal = selectCursor.getLong(0);
            }
        }
        selectCursor.close();
        return retVal;
    }

    /**
     * @return the first record ID
     */
    public long getFirstActiveID(String table, String optionalWhere, @SuppressWarnings("SameParameterValue") String orderBy) {
        long retVal = -1;
        String selectSql;
        Cursor selectCursor;
        //@formatter:off
        selectSql =
                " SELECT " + DBAdapter.COL_NAME_GEN_ROWID +
                " FROM " + table +
                " WHERE " + DBAdapter.COL_NAME_GEN_ISACTIVE + "='Y' ";
        //@formatter:on
        if (optionalWhere != null) {
            selectSql = selectSql + optionalWhere;
        }

        if (orderBy != null && orderBy.length() > 0) {
            selectSql = selectSql + " ORDER BY " + orderBy;
        }

        selectCursor = execSelectSql(selectSql, null);
        if (selectCursor.moveToFirst()) {
            retVal = selectCursor.getLong(0);
        }
        selectCursor.close();

        return retVal;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isIDActive(String table, long Id) {
        boolean retVal = false;
        String selectSql;
        Cursor selectCursor;
        selectSql = " SELECT " + DBAdapter.COL_NAME_GEN_ISACTIVE + " FROM " + table + " WHERE " + DBAdapter.COL_NAME_GEN_ROWID + " = " + Id;

        selectCursor = execSelectSql(selectSql, null);
        if (selectCursor.moveToFirst()) {
            retVal = selectCursor.getString(0).equals("Y");
        }
        selectCursor.close();

        return retVal;

    }

    @Nullable
    public Double getFuelQtyForCons(long carID, BigDecimal prevIndex, Double currIndex) {
        Double retVal = null;
        String selectSql;
        Cursor selectCursor;
        selectSql = " SELECT SUM( " + COL_NAME_REFUEL__QUANTITY + ") " + " FROM " + TABLE_NAME_REFUEL + " WHERE 1 = 1 " + WHERE_CONDITION_ISACTIVE + " AND "
                + COL_NAME_REFUEL__INDEX + " > " + prevIndex + " AND " + COL_NAME_REFUEL__INDEX + " <= " + currIndex + " AND " + COL_NAME_REFUEL__CAR_ID
                + " = " + carID;

        selectCursor = execSelectSql(selectSql, null);
        if (selectCursor.moveToFirst()) {
            retVal = selectCursor.getDouble(0);
        }
        selectCursor.close();
        return retVal;
    }

    @Nullable
    public ArrayList<Pair<Long, String>> getFuelTypesForCar(long carID) {
        ArrayList<Pair<Long, String>> retVal = null;
        String selectSql;
        Cursor selectCursor;
        selectSql = "SELECT " + DBAdapter.COL_NAME_GEN_ROWID + ", " + DBAdapter.COL_NAME_GEN_NAME +
                " FROM " + TABLE_NAME_EXPENSECATEGORY +
                    " WHERE EXISTS" +
                        " (SELECT * " +
                            "FROM " + DBAdapter.TABLE_NAME_REFUEL +
                            " WHERE " +
                                    sqlConcatTableColumn(DBAdapter.TABLE_NAME_REFUEL, DBAdapter.COL_NAME_REFUEL__CAR_ID) +
                                        " = " + carID +
                " AND " + sqlConcatTableColumn(DBAdapter.TABLE_NAME_REFUEL, DBAdapter.COL_NAME_REFUEL__EXPENSECATEGORY_ID) +
                " = " + sqlConcatTableColumn(DBAdapter.TABLE_NAME_EXPENSECATEGORY, DBAdapter.COL_NAME_GEN_ROWID) + ") ";

        selectCursor = execSelectSql(selectSql, null);
        if(selectCursor.getCount() > 0){
            retVal = new ArrayList<>();
            while (selectCursor.moveToNext())
                { retVal.add(new Pair<>(selectCursor.getLong(0), selectCursor.getString(1))); }
        }

        selectCursor.close();
        return retVal;
    }
}
//@formatter:on