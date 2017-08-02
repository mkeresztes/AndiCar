package andicar.n.persistence.viewadapter;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by miki on 13.03.2017.
 */

public class ExpenseViewAdapter extends BaseViewAdapter {
    public ExpenseViewAdapter(Cursor cursor, AppCompatActivity parentActivity, boolean isTwoPane, int scrollToPosition, long lastSelectedItemId) {
        super(cursor, parentActivity, isTwoPane, scrollToPosition, lastSelectedItemId);
        mViewAdapterType = VIEW_ADAPTER_TYPE_EXPENSE;
    }

    @Override
    protected void cursorViewBinder(DefaultViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String line1Content;
        String line2Content;
        String line3Content;

        mCursor.moveToPosition(position);

        holder.mRecordID = mCursor.getLong(0);

        try {
            line1Content = String.format(mCursor.getString(1), Utils.getFormattedDateTime(mCursor.getLong(4) * 1000, false));
        }
        catch (Exception e) {
            line1Content = "Error#1! Please contact me at andicar.support@gmail.com";
        }

        try {
            line2Content = String.format(mCursor.getString(2),
                Utils.numberToString(mCursor.getDouble(5), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT),
                Utils.numberToString(mCursor.getDouble(6), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT),
                Utils.numberToString(mCursor.getDouble(8), true, ConstantValues.DECIMALS_AMOUNT, ConstantValues.ROUNDING_MODE_AMOUNT),
                Utils.numberToString(mCursor.getDouble(7), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH));
        }
        catch (Exception e) {
            line2Content = "Error#2! Please contact me at andicar.support@gmail.com";
        }

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
            //wider screens => two line lists
            if (line2Content != null && line2Content.length() > 0) {
                holder.mFirstLine.setText(line1Content + "; " + line2Content);
            }
            else {
                holder.mFirstLine.setText(line1Content);
            }
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
