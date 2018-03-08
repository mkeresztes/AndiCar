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

package andicar.n.persistence.viewadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.math.BigDecimal;
import java.math.RoundingMode;

import andicar.n.activity.CommonListActivity;
import andicar.n.persistence.DBAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 9/27/16.
 * RecycleViewAdapter for the refuel table
 */

public class RefuelViewAdapter extends BaseViewAdapter {
    private DBAdapter mDBAdapter;

    public RefuelViewAdapter(Context ctx, Cursor cursor, CommonListActivity parentActivity, boolean isTwoPane, int scrollToPosition, long lastSelectedItemId) {
        super(cursor, parentActivity, isTwoPane, scrollToPosition, lastSelectedItemId);
        mViewAdapterType = VIEW_ADAPTER_TYPE_REFUEL;
        mDBAdapter = new DBAdapter(ctx);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (mDBAdapter != null) {
            mDBAdapter.close();
            mDBAdapter = null;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void cursorViewBinder(DefaultViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String line1Content;
        String line2Content;

        mCursor.moveToPosition(position);

        holder.mRecordID = mCursor.getLong(0);

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

        if (holder.mSecondLine != null) { //three line lists
            holder.mFirstLine.setText(line1Content);
            if (line2Content != null && line2Content.length() > 0) {
                holder.mSecondLine.setVisibility(View.VISIBLE);
                holder.mSecondLine.setText(line2Content);
            }
            else {
                holder.mSecondLine.setVisibility(View.GONE);
            }
        }
        else {
            //wider screens => two line lists
            holder.mFirstLine.setText(line1Content + "; " + line2Content);
        }

        if (mCursor.getString(3) == null || mCursor.getString(3).trim().length() == 0) {
            holder.mThirdLine.setVisibility(View.GONE);
        }
        else {
            holder.mThirdLine.setVisibility(View.VISIBLE);
            String text = mCursor.getString(3);
            BigDecimal oldFullRefuelIndex;
            try {
                oldFullRefuelIndex = new BigDecimal(mCursor.getDouble(13));
            }
            catch (Exception e) {
                holder.mThirdLine.setText("Error#1! Please contact me at andicar.support@gmail.com");
                return;
            }
            if (oldFullRefuelIndex.compareTo(BigDecimal.ZERO) < 0 //this is the first full refuel => can't calculate fuel eff
                    || mCursor.getString(12).equals("N") //this is not a full refuel
                    || mCursor.getString(17).equals("Y")) { //alternate fuel vehicle => can't calculate fuel eff
                try {
                    //do not use String.format... ! See: https://github.com/mkeresztes/AndiCar/issues/10
                    text = text.replace("[#01]", "");
                    holder.mThirdLine.setText(text);
                }
                catch (Exception e) {
                    holder.mThirdLine.setText("Error#4! Please contact me at andicar.support@gmail.com");
                }
            } else {
                // calculate the cons and fuel eff.
                BigDecimal distance = (new BigDecimal(mCursor.getString(11))).subtract(oldFullRefuelIndex);
                BigDecimal fuelQty;
                try {
                    Double t = mDBAdapter.getFuelQtyForCons(mCursor.getLong(16), oldFullRefuelIndex, mCursor.getDouble(11));
                    fuelQty = new BigDecimal(t == null ? 0d : t);
                } catch (NullPointerException e) {
                    holder.mThirdLine.setText("Error#2! Please contact me at andicar.support@gmail.com");
                    return;
                }
                String consStr;
                try {
                    consStr = Utils.numberToString(fuelQty.multiply(new BigDecimal("100")).divide(distance, 10, RoundingMode.HALF_UP), true,
                            ConstantValues.DECIMALS_FUEL_EFF, ConstantValues.ROUNDING_MODE_FUEL_EFF) + " "
                            + mCursor.getString(14) + "/100"
                            + mCursor.getString(15) + "; "
                            + Utils.numberToString(distance.divide(fuelQty, 10, RoundingMode.HALF_UP), true, ConstantValues.DECIMALS_FUEL_EFF,
                            ConstantValues.ROUNDING_MODE_FUEL_EFF) + " " + mCursor.getString(15) + "/" + mCursor.getString(14);
                } catch (Exception e) {
                    //do not use String.format... ! See: https://github.com/mkeresztes/AndiCar/issues/10
                    holder.mThirdLine.setText("Error#3! Please contact me at andicar.support@gmail.com");
                    return;
                }

                try {
                    //do not use String.format... ! See: https://github.com/mkeresztes/AndiCar/issues/10
                    text = text.replace("[#01]", "\n" + AndiCar.getAppResources().getString(R.string.gen_fuel_efficiency) + " " + consStr);
                } catch (Exception e) {
                    holder.mThirdLine.setText("Error#5! Please contact me at andicar.support@gmail.com");
                    return;
                }
            }

            if (text.trim().length() > 0) {
                holder.mThirdLine.setText(text.trim());
            }
            else {
                holder.mThirdLine.setVisibility(View.GONE);
            }
        }

    }
}
