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
package andicar.n.persistence;

import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TaskCarLinkDataBinder implements SimpleCursorAdapter.ViewBinder {
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (columnIndex == 2) {
            if (cursor != null && cursor.getString(2) != null) {
                ((TextView) view).setText(cursor.getString(2).replace(
                        "[#1]",
                        DateFormat.getDateFormat(view.getContext().getApplicationContext()).format(cursor.getLong(3) * 1000) + " "
                                + DateFormat.getTimeFormat(view.getContext().getApplicationContext()).format(cursor.getLong(3) * 1000)));
                return true;
            }
        }
        return false;
    }
}
