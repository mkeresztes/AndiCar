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
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.View;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.math.BigDecimal;

import andicar.n.activity.CommonListActivity;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 9/27/16.
 * RecycleViewAdapter for the mileage table
 */

public class MileageViewAdapter extends BaseViewAdapter {

    public MileageViewAdapter(Cursor cursor, CommonListActivity parentActivity, boolean isTwoPane, int scrollToPosition, long lastSelectedItemId) {
        super(cursor, parentActivity, isTwoPane, scrollToPosition, lastSelectedItemId);
        mViewAdapterType = VIEW_ADAPTER_TYPE_MILEAGE;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void cursorViewBinder(DefaultViewHolder holder, @SuppressLint("RecyclerView") int position) {
        BigDecimal reimbursementRate = BigDecimal.ZERO;
        String line1Content;
        String line2Content;

        mCursor.moveToPosition(position);

        holder.mRecordID = mCursor.getLong(0);

        try {
            line1Content = String.format(mCursor.getString(1), Utils.getFormattedDateTime(mCursor.getLong(5) * 1000, false)
                    + (mCursor.getLong(14) != 0L ? " (" + Utils.getDaysHoursMinutesFromSec(mCursor.getLong(14)) + ")" : ""));
        } catch (Exception e) {
            line1Content = "Error#1! Please contact me at andicar.support@gmail.com";
        }

        try {
            reimbursementRate = new BigDecimal(mCursor.getDouble(12));
        } catch (Exception ignored) {
        }

        String stopIndexStr = mCursor.getString(7);
        String mileageStr;
        if (stopIndexStr == null) {
            stopIndexStr = "N/A";
            mileageStr = "Draft";
        } else {
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
        } catch (Exception e) {
            line2Content = "Error#2! Please contact me at andicar.support@gmail.com";
        }

        if (mileageStr.equals("Draft")) {
            line2Content = line2Content.substring(0, line2Content.indexOf("Draft") + "Draft".length());
        }
        if (holder.mSecondLine != null) { //three line lists
            holder.mFirstLine.setText(line1Content);
            if (line2Content != null && line2Content.length() > 0) {
                holder.mSecondLine.setVisibility(View.VISIBLE);
                holder.mSecondLine.setText(line2Content);
                if (mileageStr.equals("Draft")) {
                    holder.mSecondLine.setTextColor(ContextCompat.getColor(holder.mSecondLine.getContext(), android.R.color.holo_red_dark));
                } else {
                    holder.mSecondLine.setTextColor(ContextCompat.getColor(holder.mSecondLine.getContext(), android.R.color.primary_text_light));
                }
            } else {
                holder.mSecondLine.setVisibility(View.GONE);
            }
        } else {
            //wider screens => two line lists
            holder.mFirstLine.setText(line1Content + "; " + line2Content);
            if (mileageStr.equals("Draft")) {
                holder.mFirstLine.setTextColor(ContextCompat.getColor(holder.mFirstLine.getContext(), android.R.color.holo_red_dark));
            } else {
                holder.mFirstLine.setTextColor(ContextCompat.getColor(holder.mFirstLine.getContext(), android.R.color.primary_text_light));
            }
        }

        if (mCursor.getString(3) == null || mCursor.getString(3).length() == 0) {
            holder.mThirdLine.setVisibility(View.GONE);
        } else {
            holder.mThirdLine.setVisibility(View.VISIBLE);
            holder.mThirdLine.setText(mCursor.getString(3));
        }

    }
}
