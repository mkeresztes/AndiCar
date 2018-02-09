package andicar.n.activity.dialogs;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.andicar2.activity.R;

import andicar.n.persistence.DBAdapter;

/**
 * Created by miki on 24.01.2018.
 */

public class MessageDialog extends AppCompatActivity {

    public static final String MSG_ID_KEY = "MessageID";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFinishOnTouchOutside(false);
        setTitle("New message");

        setContentView(R.layout.dialog_message);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvBody = findViewById(R.id.tvBody);
        Button btnClose = findViewById(R.id.btnClose);

        Bundle extras = getIntent().getExtras();
        if(extras == null){
            tvTitle.setText(getString(R.string.error_127));
            return;
        }

        DBAdapter dbAdapter = new DBAdapter(this);
        long msgRowId = dbAdapter.getMessageRowId(extras.getString(MSG_ID_KEY));
        if(msgRowId == -1){
            tvTitle.setText(getString(R.string.error_127));
            dbAdapter.close();
            return;
        }

        Cursor c = dbAdapter.fetchRecord(DBAdapter.TABLE_NAME_MESSAGES, DBAdapter.COL_LIST_MESSAGES_TABLE, msgRowId);
        if(c == null){
            tvTitle.setText(getString(R.string.error_127));
            dbAdapter.close();
            return;
        }

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the dialog
                MessageDialog.this.finish();
            }
        });


//        tvTitle.setBackgroundResource(R.drawable.ui_message_title_shape);
        tvTitle.setText(c.getString(DBAdapter.COL_POS_GEN_NAME));
        tvBody.setMovementMethod(LinkMovementMethod.getInstance());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvBody.setText(Html.fromHtml(c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT), Html.FROM_HTML_MODE_COMPACT));
        } else {
            tvBody.setText(Html.fromHtml(c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT)));
        }
//        tvBody.setText(c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT));

        c.close();

        //mark the message as read
        ContentValues cvData = new ContentValues();
        cvData.put(DBAdapter.COL_NAME_MESSAGES__IS_READ, "Y");
        dbAdapter.updateRecord(DBAdapter.TABLE_NAME_MESSAGES, msgRowId, cvData);

        dbAdapter.close();
    }
}
