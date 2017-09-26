/*
 * AndiCar
 *
 *  Copyright (c) 2016 Miklos Keresztes (miklos.keresztes@gmail.com)
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

package andicar.n.activity.miscellaneous;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.andicar2.activity.R;

import java.util.ArrayList;

import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;

public class LogFilesListActivity extends AppCompatActivity {

    ArrayList<String> mLogFileList;
    private ListView lvLogFileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_file_list);
        lvLogFileList = (ListView) findViewById(R.id.lvLogFileList);

        mLogFileList = FileUtils.listLogFiles(this);
        fillLogFilesList();
    }

    private void fillLogFilesList() {
        if (mLogFileList == null || mLogFileList.isEmpty()) {
//            btnRestore.setEnabled(false);
            lvLogFileList.setAdapter(null);
            return;
        }

        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, mLogFileList);
        lvLogFileList.setAdapter(listAdapter);
        lvLogFileList.setItemsCanFocus(false);
        lvLogFileList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("text/html");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Sent by AndiCar (http://www.andicar.org)");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "AndiCar log files ");
            int len = lvLogFileList.getCount();
            SparseBooleanArray checkedFiles = lvLogFileList.getCheckedItemPositions();
            ArrayList<Uri> logFilesToSend = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                if (checkedFiles.get(i)) {
                    logFilesToSend.add(Uri.parse("file://" + ConstantValues.LOG_FOLDER + mLogFileList.get(i)));
                }
            }

            if (logFilesToSend.size() == 0) {
                Toast.makeText(this, "No file(s) selected. Nothing to share.", Toast.LENGTH_LONG).show();
                return true;
            }

            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, logFilesToSend);
            try {
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.gen_share)));
            }
            catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.error_067, Toast.LENGTH_LONG).show();
            }
            return true;
        }
        else if (id == android.R.id.home) {
            finish();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }


}
