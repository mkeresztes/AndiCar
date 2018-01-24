package andicar.n.service;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import andicar.n.utils.Utils;

/**
 * Created by miki on 24.01.2018.
 */

public class MessageHandler extends FirebaseMessagingService {

    private static final String TAG = "AndiCar";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "MessageId: " + remoteMessage.getMessageId());
        Log.d(TAG, "MessageType: " + remoteMessage.getMessageType());
        Log.d(TAG, "CollapseKey: " + remoteMessage.getCollapseKey());
        Log.d(TAG, "To: " + remoteMessage.getTo());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Utils.showMessageDialog(getApplicationContext(), remoteMessage.getMessageId(),
                    remoteMessage.getData().get("andicar.msg_title"), remoteMessage.getData().get("andicar.msg_body"));
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            //this is the message text from FB Console
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Log.d(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
        }
    }
}
