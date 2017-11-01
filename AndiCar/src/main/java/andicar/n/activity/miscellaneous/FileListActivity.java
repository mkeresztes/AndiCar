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

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.obsez.android.lib.filechooser.ChooserDialog;

import org.andicar2.activity.R;

import java.io.File;
import java.util.ArrayList;

import andicar.n.persistence.AndiCarFileProvider;
import andicar.n.persistence.DBAdapter;
import andicar.n.utils.AndiCarCrashReporter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.Utils;

public class FileListActivity extends AppCompatActivity implements Runnable {
    public final static int LIST_TYPE_LOG = 0;
    public final static int LIST_TYPE_BACKUP = 1;
    public final static String list_type_extras_key = "list_type";

    ArrayList<String> mFileList;
    ArrayAdapter<String> mListAdapter;
    private ListView lvFileList;
    private int mListType;
    static ProgressDialog mProgress;
    private String mBackupFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);

        lvFileList = findViewById(R.id.lvFileList);
        lvFileList.setItemsCanFocus(false);

        if (getIntent().getExtras() == null)
            return;
        else {
            mListType = getIntent().getExtras().getInt(list_type_extras_key);
        }

        if (mListType == LIST_TYPE_LOG) {
            setTitle(R.string.pref_log_files_title);
            fillFileList();
        }
        else if (mListType == LIST_TYPE_BACKUP) {
            setTitle(R.string.pref_backup_list_title);
            if (!FileUtils.isFileSystemAccessGranted(this)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ConstantValues.REQUEST_ACCESS_EXTERNAL_STORAGE);
            }
            else {
                fillFileList();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fillFileList();
        }
        else {
            Toast.makeText(this, R.string.error_070, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mListType == LIST_TYPE_LOG) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_share, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("text/html");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Sent by AndiCar (http://www.andicar.org)");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "AndiCar log files ");
            int len = lvFileList.getCount();
            SparseBooleanArray checkedFiles = lvFileList.getCheckedItemPositions();
            ArrayList<Uri> logFilesToSend = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                if (checkedFiles.get(i)) {
                    logFilesToSend.add(AndiCarFileProvider.getUriForFile(this, "org.andicar2.provider", new File(ConstantValues.LOG_FOLDER + mFileList.get(i))));
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

    private void fillFileList() {
        if (mListType == LIST_TYPE_LOG) {
            lvFileList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            mFileList = FileUtils.listLogFiles(this);
            if (mFileList == null || mFileList.isEmpty()) {
                lvFileList.setAdapter(null);
            }
            else {
                mListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, mFileList);
                lvFileList.setAdapter(mListAdapter);
            }
        }
        else if (mListType == LIST_TYPE_BACKUP) {
            lvFileList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            mFileList = FileUtils.listBkFiles(this, true);
            if (mFileList == null || mFileList.isEmpty()) {
                lvFileList.setAdapter(null);
            }
            else {
                mListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, mFileList);
                lvFileList.setAdapter(mListAdapter);
            }

            lvFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d("AndiCar", adapterView.getItemAtPosition(i).toString() + " (" + i + ")");
                    if (i > 0) { //regular backup
                        DBAdapter db = new DBAdapter(getApplicationContext());
                        String dbPath = db.getDatabase().getPath();
                        db.close();
                        mBackupFile = ConstantValues.BACKUP_FOLDER + adapterView.getItemAtPosition(i).toString();
                        if (FileUtils.restoreDb(getApplicationContext(), mBackupFile, dbPath)) {
                            try {
                                Utils.setToDoNextRun(getApplicationContext());
                            }
                            catch (Exception e) {
                                AndiCarCrashReporter.sendCrash(e);
                                Log.d("AndiCar", e.getMessage(), e);
                            }
                            Utils.showInfoDialog(getApplicationContext(), getString(R.string.pref_restore_success_message), null);
                        }
                        else {
                            Utils.showNotReportableErrorDialog(getApplicationContext(), FileUtils.mLastErrorMessage, null, false);
                        }
                        finish();
                    }
                    else { //other location
                        new ChooserDialog().with(FileListActivity.this)
                                .withFilter(false, false, "db", "zip", "zi_")
                                .withStartFile(Environment.getExternalStorageDirectory().getAbsolutePath())
                                .withChosenListener(new ChooserDialog.Result() {
                                    @Override
                                    public void onChoosePath(String path, File pathFile) {
                                        mProgress = ProgressDialog.show(FileListActivity.this, "",
                                                getString(R.string.progress_restoring_data), true);
                                        mBackupFile = path;
                                        Thread thread = new Thread(FileListActivity.this);
                                        thread.start();
                                    }
                                })
                                .build()
                                .show();
                    }
                }
            });
        }
    }

    @Override
    public void run() {
        DBAdapter db = new DBAdapter(this);
        String dbPath = db.getDatabase().getPath();
        db.close();
        if (FileUtils.restoreDb(this, mBackupFile, dbPath)) {
            try {
                Utils.setToDoNextRun(this);
            }
            catch (Exception e) {
                AndiCarCrashReporter.sendCrash(e);
                Log.d("AndiCar", e.getMessage(), e);
            }
            mProgress.dismiss();
            Utils.showInfoDialog(getApplicationContext(), getString(R.string.pref_restore_success_message), null);

        }
        else {
            mProgress.dismiss();
            Utils.showNotReportableErrorDialog(getApplicationContext(), FileUtils.mLastErrorMessage, null, false);
        }
        finish();
    }
}
