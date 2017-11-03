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

package andicar.n.activity.dialogs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.andicar2.activity.R;

import andicar.n.utils.AndiCarCrashReporter;
import andicar.n.utils.Utils;

public class GeneralNotificationDialogActivity extends AppCompatActivity {

    /**
     * The extras key for dialog type
     */
    public final static String DIALOG_TYPE_KEY = "dialogType";
    /**
     * The extras key for notification message
     */
    public final static String NOTIFICATION_MESSAGE_KEY = "notificationMessage";
    /**
     * The extras key for notification detail
     */
    public final static String NOTIFICATION_DETAIL_KEY = "notificationDetail";
    /**
     * The extras key for notification timestamp
     */
    public final static String NOTIFICATION_DATETIME = "notificationDateTime";
    /**
     * The extras key for the exception string object
     */
    public final static String NOTIFICATION_EXCEPTION_STRING_KEY = "notificationExceptionStr";
    /**
     * The extras key for the exception object
     */
    public final static String NOTIFICATION_EXCEPTION_KEY = "notificationException";
    /**
     * reportable error: show the report to developer button
     */
    public final static String DIALOG_TYPE_REPORTABLE_ERROR = "reportableError";

    /**
     * not reportable error: hide the report to developer button
     */
    public final static String DIALOG_TYPE_NOT_REPORTABLE_ERROR = "notReportableError";

    /**
     * warning: hide the report to developer button
     */
    public final static String DIALOG_TYPE_WARNING = "warning";

    /**
     * just an info. Change title to Info, hide the report to developers button and error sorry message.
     */
    public final static String DIALOG_TYPE_INFO = "info";

    private final static String LogTag = "NotificationAction";

    private Exception e = null;
    private String exceptionStackTrace = null;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //prevent keyboard from automatic pop up
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setFinishOnTouchOutside(false);

        setContentView(R.layout.dialog_general_notification);

//        TextView tvDialogTitle = (TextView) findViewById(R.id.tvDialogTitle);
        ImageView ivIcon = findViewById(R.id.ivIcon);
        TextView tvErrorSorry = findViewById(R.id.tvErrorSorry);
        TextView tvNotifMessage = findViewById(R.id.tvNotificationMessage);
        TextView tvNotifDetail = findViewById(R.id.tvNotificationDetail);
        TextView tvNotifDateTime = findViewById(R.id.tvNotificationDateTime);
        Button btnClose = findViewById(R.id.btnClose);
        Button btnReportIssue = findViewById(R.id.btnReportIssue);

        Bundle extras = getIntent().getExtras();

        if (extras.getString(NOTIFICATION_MESSAGE_KEY) != null) {
            tvNotifMessage.setText(extras.getString(NOTIFICATION_MESSAGE_KEY));
            tvNotifMessage.setVisibility(View.VISIBLE);
        }
        else {
            tvNotifMessage.setText("");
            tvNotifMessage.setVisibility(View.GONE);
        }

        if (extras.getString(NOTIFICATION_DETAIL_KEY) != null) {
            tvNotifDetail.setText(extras.getString(NOTIFICATION_DETAIL_KEY));
            tvNotifDetail.setVisibility(View.VISIBLE);
        }
        else {
            tvNotifDetail.setText("");
            tvNotifDetail.setVisibility(View.GONE);
        }

        if (extras.getLong(NOTIFICATION_DATETIME, 0L) != 0L) {
            tvNotifDateTime.setText(Utils.getFormattedDateTime(extras.getLong(NOTIFICATION_DATETIME), false));
            tvNotifDateTime.setVisibility(View.VISIBLE);
        }
        else {
            tvNotifDateTime.setText("");
            tvNotifDateTime.setVisibility(View.GONE);
        }

        if (extras.getString(NOTIFICATION_EXCEPTION_STRING_KEY) != null) {
            exceptionStackTrace = extras.getString(NOTIFICATION_EXCEPTION_STRING_KEY);
            e = (Exception) extras.getSerializable(NOTIFICATION_EXCEPTION_KEY);
            Log.d(LogTag, exceptionStackTrace);
        }

        if (extras != null && extras.getString(DIALOG_TYPE_KEY) != null) {
            if (extras.getString(DIALOG_TYPE_KEY).equals(DIALOG_TYPE_NOT_REPORTABLE_ERROR)) {
                setTitle(getString(R.string.app_name) + " " + getString(R.string.gen_error));
//                tvDialogTitle.setText(R.string.gen_error);
                ivIcon.setImageResource(R.drawable.ic_dialog_error_red900);
                tvErrorSorry.setVisibility(View.GONE);
                btnReportIssue.setVisibility(View.GONE);
            }
            else if (extras.get(DIALOG_TYPE_KEY).equals(DIALOG_TYPE_REPORTABLE_ERROR)) {
                setTitle(getString(R.string.app_name) + " " + getString(R.string.gen_error));
//                tvDialogTitle.setText(R.string.gen_error);
                ivIcon.setImageResource(R.drawable.ic_dialog_error_red900);
                tvErrorSorry.setVisibility(View.VISIBLE);
                btnReportIssue.setVisibility(View.VISIBLE);
            }
            else if (extras.get(DIALOG_TYPE_KEY).equals(DIALOG_TYPE_WARNING)) {
                setTitle(getString(R.string.app_name) + " " + getString(R.string.gen_warning));
//                tvDialogTitle.setText(R.string.gen_warning);
                ivIcon.setImageResource(R.drawable.ic_dialog_warning_yellow900);
                tvErrorSorry.setVisibility(View.GONE);
                btnReportIssue.setVisibility(View.GONE);
            }
            else if (extras.get(DIALOG_TYPE_KEY).equals(DIALOG_TYPE_INFO)) {
                setTitle(getString(R.string.app_name) + " " + getString(R.string.gen_info));
//                tvDialogTitle.setText(R.string.gen_info);
                ivIcon.setImageResource(R.drawable.ic_dialog_info_blue900);
                tvErrorSorry.setVisibility(View.GONE);
                btnReportIssue.setVisibility(View.GONE);
            }
            else {
                throw new UnsupportedOperationException("Unknown dialog type");
            }
        }

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the dialog
                GeneralNotificationDialogActivity.this.finish();
            }
        });

        btnReportIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (e != null) {
                    AndiCarCrashReporter.sendCrash(e);
                }
                else if (exceptionStackTrace != null) {
                    AndiCarCrashReporter.logCrashMsg(exceptionStackTrace);
                }
                Toast.makeText(getApplication(), R.string.error_report_sent, Toast.LENGTH_LONG).show();
                GeneralNotificationDialogActivity.this.finish();
            }
        });
    }


}
