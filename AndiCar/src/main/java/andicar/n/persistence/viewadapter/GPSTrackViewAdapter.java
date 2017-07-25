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
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;

import org.andicar2.activity.R;

import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 15.03.2017.
 */

public class GPSTrackViewAdapter extends BaseViewAdapter {

    private final Context mCtx;

    public GPSTrackViewAdapter(Context ctx, Cursor cursor, AppCompatActivity parentActivity, boolean isTwoPane, int scrollToPosition, long lastSelectedItemId) {
        super(cursor, parentActivity, isTwoPane, scrollToPosition, lastSelectedItemId);
        mViewAdapterType = VIEW_ADAPTER_TYPE_GPS_TRACK;
        mCtx = ctx;
    }

    @Override
    protected void cursorViewBinder(DefaultViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String line1Content;
        String line2Content;
        String line3Content;

        if (mCursor.isClosed()) {
            return;
        }

        mCursor.moveToPosition(position);

        holder.mRecordID = mCursor.getLong(0);
        line1Content = String.format(mCursor.getString(1), Utils.getFormattedDateTime(mCursor.getLong(7) * 1000, false));
        line2Content = String.format(mCursor.getString(2),
                mCtx.getString(R.string.gps_track_detail_var_1),
                mCtx.getString(R.string.gps_track_detail_var_2),
                mCtx.getString(R.string.gps_track_detail_var_3),
                mCtx.getString(R.string.gps_track_detail_var_4),
                mCtx.getString(R.string.gps_track_detail_var_5) + " " + Utils.getTimeString(mCursor.getLong(4)),
                mCtx.getString(R.string.gps_track_detail_var_6) + " " + Utils.getTimeString(mCursor.getLong(5)),
                mCtx.getString(R.string.gps_track_detail_var_7),
                mCtx.getString(R.string.gps_track_detail_var_8),
                mCtx.getString(R.string.gps_track_detail_var_9),
                mCtx.getString(R.string.gps_track_detail_var_10),
                mCtx.getString(R.string.gps_track_detail_var_11),
                mCtx.getString(R.string.gps_track_detail_var_12) + " " + Utils.getTimeString(mCursor.getLong(8)),
                mCtx.getString(R.string.gps_track_detail_var_13) + " " + Utils.getTimeString(mCursor.getLong(4) - mCursor.getLong(8) - mCursor.getLong(5)));
        line3Content = mCursor.getString(3);

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.mFirstLine.setTextAppearance(R.style.ListItem_SecondLine);
            }
            else {
                holder.mFirstLine.setTypeface(null, Typeface.NORMAL);
            }

            CharSequence text;
            //wider screens => two line lists
            if (line2Content != null && line2Content.length() > 0) {
                //noinspection deprecation
                text = Html.fromHtml("<b>" + line1Content + "</b><br>" + line2Content);
            }
            else
            {
                //noinspection deprecation
                text = Html.fromHtml("<b>" + line1Content + "</b>");
            }
            holder.mFirstLine.setText(text);
        }

        if (line3Content == null) {
            holder.mThirdLine.setVisibility(View.GONE);
        }
        else {
            holder.mThirdLine.setVisibility(View.VISIBLE);
            holder.mThirdLine.setText(line3Content);
        }
    }
}
