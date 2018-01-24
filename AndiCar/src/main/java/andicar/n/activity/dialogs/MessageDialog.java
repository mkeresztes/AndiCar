package andicar.n.activity.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.andicar2.activity.R;

/**
 * Created by miki on 24.01.2018.
 */

public class MessageDialog extends AppCompatActivity {

    public static final String MSG_ID_KEY = "MessageID";
    public static final String MSG_TITLE_KEY = "MessageTitle";
    public static final String MSG_BODY_KEY = "MessageBody";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_message);

        Bundle extras = getIntent().getExtras();

        TextView title = findViewById(R.id.tvTitle);
        title.setText(extras.getString(MSG_TITLE_KEY));

        TextView body = findViewById(R.id.tvBody);
        body.setText(extras.getString(MSG_BODY_KEY));
    }
}
