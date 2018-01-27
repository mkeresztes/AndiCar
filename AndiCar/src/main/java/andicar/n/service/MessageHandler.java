package andicar.n.service;

import android.content.ContentValues;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import andicar.n.persistence.DBAdapter;
import andicar.n.utils.notification.AndiCarNotification;

/**
 * Created by miki on 24.01.2018.
 */

public class MessageHandler extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //save the message
            ContentValues data = new ContentValues();
            DBAdapter dbAdapter = new DBAdapter(this);
            data.put(DBAdapter.COL_NAME_GEN_NAME, remoteMessage.getData().get("andicar.msg_title"));
            data.put(DBAdapter.COL_NAME_GEN_USER_COMMENT, remoteMessage.getData().get("andicar.msg_body"));
            data.put(DBAdapter.COL_NAME_MESSAGES__MESSAGE_ID, remoteMessage.getMessageId());
            data.put(DBAdapter.COL_NAME_MESSAGES__DATE, System.currentTimeMillis() / 1000);
            data.put(DBAdapter.COL_NAME_MESSAGES__IS_READ, "N");
            data.put(DBAdapter.COL_NAME_MESSAGES__IS_STARRED, "N");
            dbAdapter.createRecord(DBAdapter.TABLE_NAME_MESSAGES, data);

            data.clear();
            data.put(DBAdapter.COL_NAME_GEN_NAME, remoteMessage.getMessageId());
            dbAdapter.createRecord(DBAdapter.TABLE_NAME_DISPLAYED_MESSAGES, data);

            dbAdapter.close();

//            Utils.showMessageDialog(getApplicationContext(), remoteMessage.getMessageId());
            AndiCarNotification.showMessageNotification(getApplicationContext(), remoteMessage.getMessageId(), remoteMessage.getData().get("andicar.msg_title"));
        }
    }
}
