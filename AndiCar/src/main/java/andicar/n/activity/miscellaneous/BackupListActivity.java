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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.andicar2.activity.R;

import java.io.File;
import java.util.ArrayList;

import andicar.n.persistence.AndiCarFileProvider;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;

public class BackupListActivity extends AppCompatActivity {

    private ArrayList<String> bkFileList;
    private ListView lvBackupList;
    private String selectedFile = null;
    private Menu mAppMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_list);

        lvBackupList = (ListView) findViewById(R.id.lvBackupList);

        fillBkList();
    }

    private void fillBkList() {
        bkFileList = FileUtils.listBkFiles(this, false);
        if (bkFileList == null || bkFileList.isEmpty()) {
//            btnRestore.setEnabled(false);
            lvBackupList.setAdapter(null);
            return;
        }

        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, bkFileList);
        lvBackupList.setAdapter(listAdapter);
        lvBackupList.setItemsCanFocus(false);
        lvBackupList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvBackupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedFile = bkFileList.get(i);

                //show the delete option
                mAppMenu.setGroupVisible(R.id.mnu_backup_list, true);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_backup_list, menu);

        mAppMenu = menu;
        //enabled when a backup selected
        mAppMenu.setGroupVisible(R.id.mnu_backup_list, false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.pref_backup_list_delete_confirmation).setTitle(R.string.gen_confirm).setIcon(R.drawable.ic_dialog_question_blue900);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.gen_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id12) {
                    FileUtils.deleteFile(ConstantValues.BACKUP_FOLDER + selectedFile);

                    //update the list of backup files
                    BackupListActivity.this.fillBkList();

                    //nothing selected => hide the delete option
                    mAppMenu.setGroupVisible(R.id.mnu_backup_list, false);
                }
            }).setNegativeButton(R.string.gen_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id1) {
                    // User cancelled the dialog
                }
            });
            builder.create().show();
            return true;
        }
        else if (id == R.id.action_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/html");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Sent by AndiCar (http://www.andicar.org)");
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "AndiCar backup file " + selectedFile);
//            shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse("file://" + ConstantValues.BACKUP_FOLDER + selectedFile));
            shareIntent.putExtra(Intent.EXTRA_STREAM, AndiCarFileProvider.getUriForFile(this, "org.andicar2.provider", new File(ConstantValues.BACKUP_FOLDER + selectedFile)));
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
