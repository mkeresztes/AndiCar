package andicar.n.activity.dialogs;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
        setContentView(R.layout.dialog_message);
        TextView title = findViewById(R.id.tvTitle);
        TextView body = findViewById(R.id.tvBody);

        Bundle extras = getIntent().getExtras();
        if(extras == null){
            title.setText(getString(R.string.error_127));
            return;
        }

        DBAdapter dbAdapter = new DBAdapter(this);
        long msgRowId = dbAdapter.getMessageRowId(extras.getString(MSG_ID_KEY));
        if(msgRowId == -1){
            title.setText(getString(R.string.error_127));
            dbAdapter.close();
            return;
        }

        Cursor c = dbAdapter.fetchRecord(DBAdapter.TABLE_NAME_MESSAGES, DBAdapter.COL_LIST_MESSAGES_TABLE, msgRowId);
        if(c == null){
            title.setText(getString(R.string.error_127));
            dbAdapter.close();
            return;
        }

        title.setText(c.getString(DBAdapter.COL_POS_GEN_NAME));
        body.setText(c.getString(DBAdapter.COL_POS_GEN_USER_COMMENT));

        c.close();

        //mark the message as read
        ContentValues cvData = new ContentValues();
        cvData.put(DBAdapter.COL_NAME_MESSAGES__IS_READ, "Y");
        dbAdapter.updateRecord(DBAdapter.TABLE_NAME_MESSAGES, msgRowId, cvData);

        dbAdapter.close();
    }
}
