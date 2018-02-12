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

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.andicar2.activity.R;

import andicar.n.persistence.DBAdapter;

/**
 * Created by Miklos Keresztes on 26.06.2017.
 */

public class MessageViewFragment extends BaseEditFragment {
    private TextView tvTitle;
    private TextView tvBody;
    private boolean isMessageUnRead = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            switch (mOperationType) {
                case BaseEditFragment.DETAIL_OPERATION_EDIT: {
                    loadDataFromDB();
                    break;
                }
                default: //new record
                    initDefaultValues();
                    break;
            }
        }
    }

    private void loadDataFromDB() {
        Cursor c = mDbAdapter.fetchRecord(DBAdapter.TABLE_NAME_MESSAGES, DBAdapter.COL_LIST_MESSAGES_TABLE, mRowId);

        assert c != null;

        mName = c.getString(DBAdapter.COL_POS_GEN_NAME);
        mUserComment = c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT);
        isMessageUnRead = c.getString(DBAdapter.COL_POS_MESSAGES__IS_READ).equals("N");
        c.close();
    }

    @Override
    protected void loadSpecificViewsFromLayoutXML() {
        tvTitle = mRootView.findViewById(R.id.tvTitle);
//        tvTitle.setBackgroundResource(R.drawable.ui_message_title_shape);
        tvBody = mRootView.findViewById(R.id.tvBody);
//        tvBody.setBackgroundResource(R.drawable.ui_message_body_shape);
        mRootView.findViewById(R.id.tvSeparatorH).setVisibility(View.GONE);
        mRootView.findViewById(R.id.tvSeparatorF).setVisibility(View.GONE);
        mRootView.findViewById(R.id.llButtonBar).setVisibility(View.GONE);
    }

    @Override
    protected void initSpecificControls() {

    }

    @Override
    protected void showValuesInUI() {
        tvTitle.setText(mName);
        tvBody.setMovementMethod(LinkMovementMethod.getInstance());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvBody.setText(Html.fromHtml(mUserComment, Html.FROM_HTML_MODE_COMPACT));
        }
        else {
            tvBody.setText(Html.fromHtml(mUserComment));
        }

        //mark the message as read
        if (isMessageUnRead) {
            ContentValues cvData = new ContentValues();
            cvData.put(DBAdapter.COL_NAME_MESSAGES__IS_READ, "Y");
            mDbAdapter.updateRecord(DBAdapter.TABLE_NAME_MESSAGES, mRowId, cvData);
        }
    }

    @Override
    public void setSpecificLayout() {

    }

    @Override
    protected boolean saveData() {
        return true;
    }
}
