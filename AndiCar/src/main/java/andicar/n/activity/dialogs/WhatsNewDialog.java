package andicar.n.activity.dialogs;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import andicar.n.utils.Utils;

/**
 * Created by miki on 18.09.2017.
 */

public class WhatsNewDialog extends AppCompatActivity {
    public static final String IS_SHOW_FIVE_STARS_BUTTON_KEY = "ShowFiveStars";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_whats_new);
        TextView tvWhatsNew = (TextView) findViewById(R.id.whatsNew);
        tvWhatsNew.setMovementMethod(LinkMovementMethod.getInstance());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvWhatsNew.setText(Html.fromHtml(getString(R.string.whats_new), Html.FROM_HTML_MODE_COMPACT));
        } else {
            tvWhatsNew.setText(Html.fromHtml(getString(R.string.whats_new)));
        }

        Button btnClose = (Button) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button btnFiveStars = (Button) findViewById(R.id.btnFiveStars);
        if (getIntent().getExtras().getBoolean(IS_SHOW_FIVE_STARS_BUTTON_KEY, false)
                && Utils.isCanShowRateApp(getApplicationContext()) //the user have enough records to be able to evaluate the app
                && !AndiCar.getDefaultSharedPreferences().getBoolean(getString(R.string.pref_key_user_pressed_5_star_button), false) //the user not pressed the 5 star button until now
                ) {
            Utils.sendAnalyticsEvent(getApplicationContext(), "WhatsNewDialogShowed", null, true);
            btnFiveStars.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.sendAnalyticsEvent(getApplicationContext(), "btnFiveStarPressed", null, true);

                    //retain the fact than the user pressed the five star button
                    SharedPreferences.Editor editor = AndiCar.getDefaultSharedPreferences().edit();
                    editor.putBoolean(getString(R.string.pref_key_user_pressed_5_star_button), true);
                    editor.apply();

                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    // To count with Play market backstack, After pressing back button,
                    // to taken back to our application, we need to add following flags to intent.
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    }
                    finish();
                }
            });
        } else
            btnFiveStars.setVisibility(View.GONE);

    }
}
