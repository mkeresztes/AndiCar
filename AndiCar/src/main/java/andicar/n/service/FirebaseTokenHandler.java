package andicar.n.service;

import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

/**
 * Created by Miklos Keresztes on 09.02.2018.
 */

public class FirebaseTokenHandler extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null) {
            SharedPreferences.Editor e = AndiCar.getDefaultSharedPreferences().edit();
            e.putString(AndiCar.getAppResources().getString(R.string.pref_key_fb_message_token), token);
            e.apply();
        }
    }
}
