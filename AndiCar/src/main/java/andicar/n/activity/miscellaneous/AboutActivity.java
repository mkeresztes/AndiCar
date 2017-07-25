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

package andicar.n.activity.miscellaneous;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.andicar2.activity.R;

import java.util.Calendar;

/**
 * @author miki
 */
public class AboutActivity extends AppCompatActivity {

    /**
	 * Called when the activity is first created.
     */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_about);

        TextView tvAbout1 = (TextView) findViewById(R.id.tvAbout1);
        tvAbout1.setMovementMethod(LinkMovementMethod.getInstance());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			tvAbout1.setText(Html.fromHtml(getString(R.string.app_about1_html), Html.FROM_HTML_MODE_COMPACT));
		}
		else
		{
			tvAbout1.setText(Html.fromHtml(getString(R.string.app_about1_html)));
		}


        TextView tvAbout2 = (TextView) findViewById(R.id.tvAbout2);
        tvAbout2.setMovementMethod(LinkMovementMethod.getInstance());
        String abt = String.format(getString(R.string.app_about2_html), Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			tvAbout2.setText(Html.fromHtml(abt, Html.FROM_HTML_MODE_COMPACT));
		}
		else
		//noinspection deprecation
		{
			tvAbout2.setText(Html.fromHtml(abt));
		}
//		tvAbout2.setText(abt);
    }
}
