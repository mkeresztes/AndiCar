/*
 * AndiCar
 *
 *  Copyright (c) 2017 Miklos Keresztes (miklos.keresztes@gmail.com)
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
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 09.01.2017.
 */

public class SettingsDefaultViewAdapter extends BaseViewAdapter {

    public SettingsDefaultViewAdapter(Cursor cursor, AppCompatActivity parentActivity, boolean isTwoPane, int scrollToPosition, long lastSelectedItemId) {
        super(cursor, parentActivity, isTwoPane, scrollToPosition, lastSelectedItemId);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void cursorViewBinder(DefaultViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String line1Content;
        String line2Content;
        String line3Content;

        //TODO see why this can be happened https://github.com/mkeresztes/AndiCar/issues/1
        if(mCursor == null || mCursor.isClosed())
            return;

        mCursor.moveToPosition(position);

        holder.mRecordID = mCursor.getLong(0);

        line1Content = mCursor.getString(1);
        line2Content = mCursor.getString(2);
        line3Content = mCursor.getString(3);
        if (line3Content != null) {
            line3Content = line3Content.equals("Y") ? "Active" : "Inactive";
        }

        if (mViewAdapterType == VIEW_ADAPTER_TYPE_REIMBURSEMENT_RATE) {
            line2Content = String.format(line2Content, Utils.getFormattedDateTime(mCursor.getLong(4) * 1000, true), Utils.getFormattedDateTime(mCursor.getLong(5) * 1000, true),
                    Utils.numberToString(mCursor.getDouble(6), true, ConstantValues.DECIMALS_RATES, ConstantValues.ROUNDING_MODE_RATES));
        }

        if (holder.mSecondLine != null) { //three line lists
            holder.mFirstLine.setText(line1Content);
            if (line2Content != null && line2Content.length() > 0) {
                holder.mSecondLine.setVisibility(View.VISIBLE);
                holder.mSecondLine.setText(line2Content);
            } else {
                holder.mSecondLine.setVisibility(View.GONE);
            }
        } else {
            //wider screens => two line lists
            if (line2Content != null && line2Content.length() > 0) {
                holder.mFirstLine.setText(line1Content + "; " + line2Content);
            } else {
                holder.mFirstLine.setText(line1Content);
            }
        }

        if (line3Content == null) {
            holder.mThirdLine.setVisibility(View.GONE);
        } else {
            holder.mThirdLine.setVisibility(View.VISIBLE);
            holder.mThirdLine.setText(line3Content);
        }
    }
}
