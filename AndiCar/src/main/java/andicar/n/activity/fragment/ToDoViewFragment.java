/*
 *
 * AndiCar
 *
 * Copyright (c) 2017 Miklos Keresztes (miklos.keresztes@gmail.com)
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
 *
 */

package andicar.n.activity.fragment;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import org.andicar2.activity.R;

import andicar.n.persistence.DBAdapter;
import andicar.n.persistence.DBReportAdapter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 23.06.2017.
 */

public class ToDoViewFragment extends BaseEditFragment {
    private TextView tvText1;
    private TextView tvText2;
    private TextView tvText3;
    private TextView tvText4;

    @Override
    protected boolean saveData() {
        return true;
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        tvText1 = vgRoot.findViewById(R.id.tvText1);
        tvText2 = vgRoot.findViewById(R.id.tvText2);
        tvText3 = vgRoot.findViewById(R.id.tvText3);
        tvText4 = vgRoot.findViewById(R.id.tvText4);
    }

    @Override
    protected void initSpecificControls() {

    }

    @SuppressWarnings("deprecation")
    @Override
    protected void showValuesInUI() {
        Bundle whereConditions = new Bundle();
        whereConditions.putString(DBAdapter.sqlConcatTableColumn(DBAdapter.TABLE_NAME_TODO, DBAdapter.COL_NAME_GEN_ROWID) + "= ",
                Long.toString(mRowId));

        DBReportAdapter reportDb = new DBReportAdapter(getContext(), DBReportAdapter.TODO_LIST_SELECT_NAME, whereConditions);
        Cursor todoReportCursor = reportDb.fetchReport(1);

        if (todoReportCursor != null && todoReportCursor.moveToFirst()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tvText2.setTextColor(getResources().getColor(R.color.todo_overdue_text_color, getContext().getTheme()));
            }
            else {
                tvText2.setTextColor(getResources().getColor(R.color.todo_overdue_text_color));
            }

            String dataString = todoReportCursor.getString(1);
            if (dataString.contains("[#5]")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvText1.setTextColor(getResources().getColor(R.color.todo_overdue_text_color, getContext().getTheme()));
                }
                else {
                    tvText1.setTextColor(getResources().getColor(R.color.todo_overdue_text_color));
                }
            }
            else if (dataString.contains("[#15]")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvText1.setTextColor(getResources().getColor(R.color.todo_done_text_color, getContext().getTheme()));
                }
                else {
                    tvText1.setTextColor(getResources().getColor(R.color.todo_done_text_color));
                }
            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvText1.setTextColor(getResources().getColor(R.color.todo_blue_text_color, getContext().getTheme()));
                }
                else {
                    tvText1.setTextColor(getResources().getColor(R.color.todo_blue_text_color));
                }
            }

            String text = dataString
                    .replace("[#1]", getString(R.string.gen_type_label) + " ")
                    .replace("[#2]", "; " + getString(R.string.gen_todo) + ": ")
                    .replace("[#3]", "\n" + getString(R.string.gen_car_label))
                    .replace("[#4]", getString(R.string.todo_status_label))
                    .replace("[#5]", getString(R.string.todo_overdue_label))
                    .replace("[#6]", getString(R.string.todo_scheduled_label))
                    .replace("[#15]", getString(R.string.todo_done_label));

            tvText1.setText(text);

            text = todoReportCursor.getString(2);
            text = text
                    .replace("[#7]", getString(R.string.todo_scheduled_date_label))
                    .replace(
                            "[#8]",
                            DateFormat.getDateFormat(getContext()).format(todoReportCursor.getLong(4) * 1000) + " "
                                    + DateFormat.getTimeFormat(getContext()).format(todoReportCursor.getLong(4) * 1000))
                    .replace("[#9]", getString(R.string.gen_or))
                    .replace("[#10]", getString(R.string.todo_scheduled_mileage_label))
                    .replace("[#11]",
                            Utils.numberToString(todoReportCursor.getDouble(5), true, ConstantValues.DECIMALS_LENGTH, ConstantValues.ROUNDING_MODE_LENGTH))
                    .replace("[#12]", getString(R.string.todo_mileage)).replace("([#13] [#14])", "");

            tvText3.setText(text);
            text = todoReportCursor.getString(todoReportCursor.getColumnIndex(DBReportAdapter.THIRD_LINE_LIST_NAME));
            if (text != null && text.trim().length() > 0) {
                tvText4.setText(text);
            }
            else {
                tvText4.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setSpecificLayout() {

    }
}
