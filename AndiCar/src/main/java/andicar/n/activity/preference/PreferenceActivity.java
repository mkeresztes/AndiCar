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

package andicar.n.activity.preference;


import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.obsez.android.lib.filechooser.ChooserDialog;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import andicar.n.activity.CommonListActivity;
import andicar.n.activity.miscellaneous.BackupListActivity;
import andicar.n.activity.miscellaneous.BackupScheduleActivity;
import andicar.n.activity.miscellaneous.LogFilesListActivity;
import andicar.n.interfaces.AndiCarAsyncTaskListener;
import andicar.n.persistence.DBAdapter;
import andicar.n.service.SendGMailTask;
import andicar.n.utils.AndiCarCrashReporter;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.Utils;

//import android.preference.PreferenceFragment;

/**
 * A {@link android.preference.PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
@SuppressWarnings("JavaDoc")
public class PreferenceActivity extends AppCompatPreferenceActivity {
    static GoogleApiClient mGoogleApiClient;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static final Preference.OnPreferenceChangeListener sDefaultPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            setPreferenceSummaryByValue(preference, value);
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     */

    private static void setDefaultPreferenceChangeListener(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sDefaultPreferenceChangeListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sDefaultPreferenceChangeListener.onPreferenceChange(preference,
                AndiCar.getDefaultSharedPreferences().getString(preference.getKey(), ""));
    }

    /**
     * Sets the summary of the preference according to its value
     *
     * @param preference
     * @param value
     */
    private static void setPreferenceSummaryByValue(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);
        }
        else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }
    }

    /**
     * Helper method to determine if the device has an large screen.
     * Large screens are with width >= 900dp
     */
    private static boolean isLargeScreen(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return dpWidth >= 900;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isLargeScreen(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || FinancialPreferenceFragment.class.getName().equals(fragmentName)
                || BackupRestorePreferenceFragment.class.getName().equals(fragmentName)
                || GPSTrackPreferenceFragment.class.getName().equals(fragmentName)
                || MainScreenPreferenceFragment.class.getName().equals(fragmentName);
    }

    //this is called after the GDrive authorization screen dismissed. See secureBkGDriveFolderPreference.setOnPreferenceClickListener
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ConstantValues.REQUEST_OPEN_DRIVE_FOLDER:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);//this extra contains the drive id of the selected file
                    SharedPreferences.Editor e = AndiCar.getDefaultSharedPreferences().edit();
                    e.putString(getString(R.string.pref_key_secure_backup_gdrive_folder_id), driveId.getResourceId());
                    e.apply();

                    final DriveFolder selectedFolder = driveId.asDriveFolder();
                    final PendingResult selectedFolderMetadata = selectedFolder.getMetadata(mGoogleApiClient);

                    selectedFolderMetadata.setResultCallback(new ResultCallback() {
                        @Override
                        public void onResult(@NonNull Result result) {
                            Metadata fileMetadata = ((DriveResource.MetadataResult) result).getMetadata();
                            SharedPreferences.Editor e = AndiCar.getDefaultSharedPreferences().edit();
                            e.putString(getString(R.string.pref_key_secure_backup_gdrive_folder_name), fileMetadata.getTitle());
                            e.apply();
                            BackupRestorePreferenceFragment.secureBkGDriveFolderPreference.
                                    setSummary(AndiCar.getDefaultSharedPreferences().getString(getString(R.string.pref_key_secure_backup_gdrive_folder_name), ""));
                        }
                    });
                }
                break;
            case ConstantValues.REQUEST_GDRIVE_AUTHORIZATION:
                BackupRestorePreferenceFragment.setGDriveAccessGranted(this, resultCode == RESULT_OK);
                break;
            case ConstantValues.REQUEST_GMAIL_AUTHORIZATION:
                BackupRestorePreferenceFragment.setGMailAccessGranted(this, resultCode == RESULT_OK);
                break;
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        //        private Context mXtx = getActivity();
        Preference carPreference;
        Preference fuelTypePreference;
        Preference driverPreference;
        Preference uomPreference;
        Preference uomConversionPreference;
        Preference taskTypePreference;
        Preference taskPreference;
        Preference tagPreference;
        Preference listLogFilesPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            carPreference = findPreference(getString(R.string.pref_key_cars));
            carPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(GeneralPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_CAR);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    GeneralPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            fuelTypePreference = findPreference(getString(R.string.pref_key_fuel_type));
            fuelTypePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(GeneralPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_FUEL_TYPE);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    GeneralPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            driverPreference = findPreference(getString(R.string.pref_key_drivers));
            driverPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(GeneralPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_DRIVER);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    GeneralPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            uomPreference = findPreference(getString(R.string.pref_key_uom));
            uomPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(GeneralPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_UOM);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    GeneralPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            uomConversionPreference = findPreference(getString(R.string.pref_key_uom_conversions));
            uomConversionPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(GeneralPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_UOM_CONVERSION);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    GeneralPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            taskTypePreference = findPreference(getString(R.string.pref_key_task_type));
            taskTypePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(GeneralPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_TASK_TYPE);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    GeneralPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            taskPreference = findPreference(getString(R.string.pref_key_task));
            taskPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(GeneralPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_TASK);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    GeneralPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            tagPreference = findPreference(getString(R.string.pref_key_tag));
            tagPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(GeneralPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_TAG);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    GeneralPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            listLogFilesPreference = findPreference(getString(R.string.pref_key_log_files));
            listLogFilesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(GeneralPreferenceFragment.this.getActivity(), LogFilesListActivity.class);
                    GeneralPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });


            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
//            setDefaultPreferenceChangeListener(findPreference("example_text"));
//            setDefaultPreferenceChangeListener(findPreference("example_list"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                if (!isLargeScreen(getActivity())) {
                    startActivity(new Intent(getActivity(), PreferenceActivity.class));
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class FinancialPreferenceFragment extends PreferenceFragment {
        //        private Context mXtx = getActivity();
        Preference expenseCategoryPreference;
        Preference expenseTypePreference;
        Preference reimbursementRatePreference;
        Preference currencyPreference;
        Preference currencyRatePreference;
        Preference bpartnerPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_financial);
            setHasOptionsMenu(true);

            expenseCategoryPreference = findPreference(getString(R.string.pref_key_expense_category));
            expenseCategoryPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(FinancialPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_EXPENSE_CATEGORY);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    FinancialPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            expenseTypePreference = findPreference(getString(R.string.pref_key_expense_type));
            expenseTypePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(FinancialPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_EXPENSE_TYPE);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    FinancialPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            reimbursementRatePreference = findPreference(getString(R.string.pref_key_reimbursement_rate));
            reimbursementRatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(FinancialPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_REIMBURSEMENT_RATE);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    FinancialPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            currencyPreference = findPreference(getString(R.string.pref_key_currency));
            currencyPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(FinancialPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_CURRENCY);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    FinancialPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            currencyRatePreference = findPreference(getString(R.string.pref_key_currency_rate));
            currencyRatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(FinancialPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_CURRENCY_RATE);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    FinancialPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            bpartnerPreference = findPreference(getString(R.string.pref_key_bpartner));
            bpartnerPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(FinancialPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_BPARTNER);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    FinancialPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });


            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
//            setDefaultPreferenceChangeListener(findPreference("example_text"));
//            setDefaultPreferenceChangeListener(findPreference("example_list"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                if (!isLargeScreen(getActivity())) {
                    startActivity(new Intent(getActivity(), PreferenceActivity.class));
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows backup & restore preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressLint("HandlerLeak")
    public static class BackupRestorePreferenceFragment extends PreferenceFragment implements AndiCarAsyncTaskListener, Runnable, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        static ProgressDialog mProgress;
        GoogleAccountCredential mGoogleCredential;

        public static final String SUCCESS_MSG_KEY = "Success";
        public static final String ERROR_MSG_KEY = "ErrorMsg";
        private static final String LogTag = "BackupRestorePref";
        private final Handler handler;
        private static boolean isGMailAccessGranted = true;
        private static boolean isShouldShowGDriveFolderSelector = false;
        private static boolean isShouldShowGDriveDoneToast = false;
        private boolean isAccessToStorageJustAsked = false;
        private boolean isAccessToAccountsJustAsked = false;
        private boolean isAccountChooserIsShown = false;
        private String fPath;

        SwitchPreference backupService;
        Preference backupServiceSchedule;
        SwitchPreference backupServiceShowNotification;

        PreferenceCategory secureBkCategory;
        static SwitchPreference secureBkPreference;
        Preference secureBkGoogleAccountPreference;
        ListPreference secureBkMethodPreference;
        static Preference secureBkGDriveFolderPreference;
        static EditTextPreference secureBkEmailToPreference;
        SwitchPreference secureBkOnlyWiFiPreference;
        SwitchPreference secureBkSendTrackFilesPreference;
        SwitchPreference secureBkShowNotification;
        Preference revalidateAccountPreference;

        Preference backupPreference;
        ListPreference restorePreference;
        Preference listBackupsPreference;

        {
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    try {
                        mProgress.dismiss();
                        if (msg.peekData() != null) {
                            if (msg.peekData().containsKey(ERROR_MSG_KEY)) {
                                Utils.showNotReportableErrorDialog(getActivity(), msg.peekData().getString(ERROR_MSG_KEY), null, false);
                            }
                            else if (msg.peekData().containsKey(SUCCESS_MSG_KEY)) {
                                Utils.showInfoDialog(getActivity(), msg.peekData().getString(SUCCESS_MSG_KEY), null);
                            }
                        }
                    }
                    catch (Exception ignored) {
                    }
                }
            };
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_backup_restore);
            setHasOptionsMenu(true);

            // Initialize credentials and service object.
            mGoogleCredential = GoogleAccountCredential.usingOAuth2(getActivity(), Arrays.asList(ConstantValues.GOOGLE_SCOPES)).setBackOff(new ExponentialBackOff());
            mGoogleCredential.setSelectedAccountName(getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_google_account), ""));
            if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_google_account), null) != null) {
                setDefaultPreferenceChangeListener(findPreference(getString(R.string.pref_key_google_account)));
            }

            if (getPreferenceManager().getSharedPreferences().getBoolean(getString(R.string.pref_key_secure_backup_enabled), false) &&
                    getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_secure_backup_method), "0").equals("0")) {
                mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                        .addApi(Drive.API)
                        .addScope(Drive.SCOPE_FILE)
                        .addConnectionCallbacks(BackupRestorePreferenceFragment.this)
                        .addOnConnectionFailedListener(BackupRestorePreferenceFragment.this)
                        .setAccountName(mGoogleCredential.getSelectedAccountName())
                        .build();

                mGoogleApiClient.connect();
            }

            backupService = (SwitchPreference) findPreference(getString(R.string.pref_key_backup_service_enabled));
            backupServiceSchedule = findPreference(getString(R.string.pref_key_backup_service_schedule));
            backupServiceShowNotification = (SwitchPreference) findPreference(getString(R.string.pref_key_backup_service_show_notification));

            secureBkCategory = (PreferenceCategory) findPreference(getString(R.string.pref_key_secure_backup_category));
            secureBkPreference = (SwitchPreference) findPreference(getString(R.string.pref_key_secure_backup_enabled));
            secureBkGoogleAccountPreference = findPreference(getString(R.string.pref_key_google_account));
            secureBkMethodPreference = (ListPreference) findPreference(getString(R.string.pref_key_secure_backup_method));
            setPreferenceSummaryByValue(secureBkMethodPreference, secureBkMethodPreference.getValue());
//            setDefaultPreferenceChangeListener(secureBkMethodPreference);
            secureBkGDriveFolderPreference = findPreference(getString(R.string.pref_key_secure_backup_gdrive_folder_id));

            secureBkEmailToPreference = (EditTextPreference) findPreference(getString(R.string.pref_key_secure_backup_emailTo));
            secureBkOnlyWiFiPreference = (SwitchPreference) findPreference(getString(R.string.pref_key_secure_backup_only_wifi));
            secureBkShowNotification = (SwitchPreference) findPreference(getString(R.string.pref_key_secure_backup_show_notification));
            secureBkSendTrackFilesPreference = (SwitchPreference) findPreference(getString(R.string.pref_key_secure_backup_send_tracks));
            revalidateAccountPreference = findPreference(getString(R.string.pref_key_revalidate_google_account));

            backupPreference = findPreference(getString(R.string.pref_key_backup_now));
            restorePreference = (ListPreference) findPreference(getString(R.string.pref_key_restore_data));
            listBackupsPreference = findPreference(getString(R.string.pref_key_list_backups));

            setDefaultPreferenceChangeListener(findPreference(getString(R.string.pref_key_secure_backup_emailTo)));

            backupPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DBAdapter db = new DBAdapter(BackupRestorePreferenceFragment.this.getActivity());
                    String dbPath = db.getDatabase().getPath();
                    db.close();
                    if (FileUtils.backupDb(BackupRestorePreferenceFragment.this.getActivity(), dbPath, null, false, null) != null) {
                        BackupRestorePreferenceFragment.this.fillBKFileList();
                        Toast toast = Toast.makeText(BackupRestorePreferenceFragment.this.getActivity(), R.string.pref_backup_success_message, Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(BackupRestorePreferenceFragment.this.getActivity());
                        builder.setMessage(FileUtils.mLastErrorMessage).setTitle(R.string.gen_error).setIcon(R.drawable.ic_dialog_error_red900);
                        builder.setPositiveButton(R.string.gen_ok, null);
                        builder.create().show();
                    }
                    return true;
                }
            });

            listBackupsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(BackupRestorePreferenceFragment.this.getActivity(), BackupListActivity.class);
                    BackupRestorePreferenceFragment.this.startActivity(i);
                    return true;
                }
            });

            restorePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final String bkFile = (String) newValue;
                    //show a confirmation dialog

                    if (!bkFile.equals(getString(R.string.pref_restore_other))) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(BackupRestorePreferenceFragment.this.getActivity());
                        builder.setMessage(R.string.pref_restore_confirmation).setTitle(R.string.gen_confirm).setIcon(R.drawable.ic_dialog_question_blue900);
                        builder.setCancelable(false);
                        builder.setNegativeButton(R.string.gen_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                        builder.setPositiveButton(R.string.gen_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                DBAdapter db = new DBAdapter(BackupRestorePreferenceFragment.this.getActivity());
                                String dbPath = db.getDatabase().getPath();
                                db.close();
                                if (FileUtils.restoreDb(BackupRestorePreferenceFragment.this.getActivity(), ConstantValues.BACKUP_FOLDER + bkFile, dbPath)) {
                                    try {
//                                        ServiceStarter.startServicesDirect(BackupRestorePreferenceFragment.this.getActivity(), ConstantValues.SERVICE_STARTER_START_ALL);
                                        Utils.setToDoNextRun(getActivity());
                                    } catch (Exception e) {
                                        AndiCarCrashReporter.sendCrash(e);
                                        Log.d(LogTag, e.getMessage(), e);
                                    }
                                    Utils.showInfoDialog(getActivity(), getString(R.string.pref_restore_success_message), null);
                                } else {
                                    Utils.showNotReportableErrorDialog(getActivity(), FileUtils.mLastErrorMessage, null, false);
                                }
                            }
                        });

                        builder.create().show();
                    } else {
                        new ChooserDialog().with(getActivity())
                                .withFilter(false, false, "db", "zip", "zi_")
                                .withStartFile(Environment.getExternalStorageDirectory().getAbsolutePath())
                                .withChosenListener(new ChooserDialog.Result() {
                                    @Override
                                    public void onChoosePath(String path, File pathFile) {
                                        fPath = path;
                                        AlertDialog.Builder builder = new AlertDialog.Builder(BackupRestorePreferenceFragment.this.getActivity());
                                        builder.setMessage(R.string.pref_restore_confirmation).setTitle(R.string.gen_confirm).setIcon(R.drawable.ic_dialog_question_blue900);
                                        builder.setCancelable(false);
                                        builder.setNegativeButton(R.string.gen_no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                // User cancelled the dialog
                                            }
                                        });
                                        builder.setPositiveButton(R.string.gen_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                mProgress = ProgressDialog.show(BackupRestorePreferenceFragment.this.getActivity(), "",
                                                        getString(R.string.progress_restoring_data), true);
                                                Thread thread = new Thread(BackupRestorePreferenceFragment.this);
                                                thread.start();
                                            }
                                        });
                                        builder.create().show();
                                    }
                                })
                                .build()
                                .show();
                    }
                    return false;
                }
            });

            backupService.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    backupServiceSchedule.setEnabled((Boolean) newValue);
                    backupServiceShowNotification.setEnabled((Boolean) newValue);

                    BackupRestorePreferenceFragment.this.setBackupServiceScheduleSummary((Boolean) newValue);

                    try {
                        Utils.setBackupNextRun(BackupRestorePreferenceFragment.this.getActivity(), (Boolean) newValue);
                    }
                    catch (Exception e) {
                        AndiCarCrashReporter.sendCrash(e);
                        Log.d(LogTag, e.getMessage(), e);
                    }
                    return true;
                }
            });

            backupServiceSchedule.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent backupSchedule = new Intent(BackupRestorePreferenceFragment.this.getActivity(), BackupScheduleActivity.class);
                    BackupRestorePreferenceFragment.this.startActivityForResult(backupSchedule, ConstantValues.REQUEST_BACKUP_SERVICE_SCHEDULE);
                    return true;
                }
            });

            secureBkPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ((Boolean) newValue) {
                        if (ContextCompat.checkSelfPermission(BackupRestorePreferenceFragment.this.getActivity(), Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_DENIED) {
                            isAccessToAccountsJustAsked = true;
                            ActivityCompat.requestPermissions(BackupRestorePreferenceFragment.this.getActivity(),
                                    new String[]{Manifest.permission.GET_ACCOUNTS}, ConstantValues.REQUEST_GET_ACCOUNTS);
                        } else {
                            if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_google_account), "").length() == 0) {
                                showGoogleAccountChooser();
                            }
                        }
                        secureBkGoogleAccountPreference.setEnabled(true);
                        secureBkMethodPreference.setEnabled(true);
                        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                            secureBkGDriveFolderPreference.setEnabled(true);
                        if (isGMailAccessGranted) {
                            secureBkEmailToPreference.setEnabled(true);
                        }
                        secureBkOnlyWiFiPreference.setEnabled(true);
                        secureBkShowNotification.setEnabled(true);
                        secureBkSendTrackFilesPreference.setEnabled(true);
                        revalidateAccountPreference.setEnabled(true);
                        isShouldShowGDriveFolderSelector = true;
                    } else {
                        SharedPreferences.Editor editor = BackupRestorePreferenceFragment.this.getPreferenceManager().getSharedPreferences().edit();
                        editor.putString(BackupRestorePreferenceFragment.this.getString(R.string.pref_key_google_account), null);
                        editor.apply();
                        secureBkGoogleAccountPreference.setSummary(R.string.pref_google_account_description);
                        secureBkGoogleAccountPreference.setEnabled(false);
                        secureBkMethodPreference.setEnabled(false);
                        secureBkGDriveFolderPreference.setEnabled(false);
                        secureBkEmailToPreference.setEnabled(false);
                        secureBkOnlyWiFiPreference.setEnabled(false);
                        secureBkShowNotification.setEnabled(false);
                        secureBkSendTrackFilesPreference.setEnabled(false);
                        revalidateAccountPreference.setEnabled(false);
                    }
                    return true;
                }
            });

            secureBkGoogleAccountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //check network connectivity
                    if (!Utils.isNetworkAvailable(BackupRestorePreferenceFragment.this.getActivity())) {
                        //no network. cannot be configure => show a warning
                        AlertDialog.Builder builder = new AlertDialog.Builder(BackupRestorePreferenceFragment.this.getActivity());
                        builder.setMessage(R.string.gen_internet_access_required).setTitle(R.string.gen_info).setIcon(R.drawable.ic_dialog_warning_yellow900);

                        builder.setPositiveButton(R.string.gen_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        builder.create().show();
                        return true;
                    }
                    showGoogleAccountChooser();
                    return true;
                }
            });

            secureBkMethodPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setPreferenceSummaryByValue(preference, newValue);
                    String method = (String) newValue;
                    if (method.equals("0")) { //GDrive
                        secureBkCategory.removePreference(secureBkEmailToPreference);
                        secureBkCategory.addPreference(secureBkGDriveFolderPreference);
                    }
                    else {
                        secureBkCategory.removePreference(secureBkGDriveFolderPreference);
                        secureBkCategory.addPreference(secureBkEmailToPreference);
                    }

                    validateGoogleAccount(method);

                    return true;
                }
            });

            secureBkGDriveFolderPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                //                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showGDriveFolderSelector(getActivity());
                    return false;
                }
            });

            revalidateAccountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    validateGoogleAccount(getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_secure_backup_method), "0"));
                    return true;
                }
            });

            if (!FileUtils.isFileSystemAccessGranted(getActivity())) {
                isAccessToStorageJustAsked = true;
                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ConstantValues.REQUEST_ACCESS_EXTERNAL_STORAGE);
            }
        }

        private static void showGDriveFolderSelector(Activity activity) {
            IntentSender intentSender = Drive.DriveApi
                    .newOpenFileActivityBuilder()
//                    .setMimeType(new String[]{DriveFolder.MIME_TYPE})
                    .setActivityTitle(activity.getString(R.string.pref_securebackup_gdrive_folder_selector_title))
                    .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE))
                    .build(mGoogleApiClient);
            try {
                activity.startIntentSenderForResult(
                        intentSender, ConstantValues.REQUEST_OPEN_DRIVE_FOLDER, null, 0, 0, 0);
            }
            catch (IntentSender.SendIntentException e) {
                Log.i("AndiCar", "Failed to launch file chooser.");
            }
        }

        @Override
        public void onResume() {
            super.onResume();

//            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            if (!FileUtils.isFileSystemAccessGranted(getActivity())) {
                backupPreference.setSummary(R.string.error_109);
                backupPreference.setEnabled(false);

                listBackupsPreference.setSummary(R.string.error_109);
                listBackupsPreference.setEnabled(false);

                restorePreference.setSummary(R.string.error_109);
                restorePreference.setEnabled(false);

                backupService.setEnabled(false);
                backupService.setSummary(R.string.error_109);
                backupServiceSchedule.setEnabled(false);
                backupServiceSchedule.setSummary("");
                backupServiceShowNotification.setEnabled(false);

                secureBkPreference.setChecked(false);
                secureBkCategory.setEnabled(false);
                secureBkPreference.setSummary(R.string.error_109);

                return;
            } else {
                backupPreference.setSummary(R.string.pref_backup_now_description);
                backupPreference.setEnabled(true);

                fillBKFileList();

                restorePreference.setSummary(R.string.pref_restore_description);
                restorePreference.setEnabled(true);

                backupService.setEnabled(true);
                backupService.setSummary(R.string.pref_backup_service_description);
                setBackupServiceScheduleSummary(null);
                if (getPreferenceManager().getSharedPreferences().getBoolean(getString(R.string.pref_key_backup_service_enabled), false) || isAccessToStorageJustAsked) {
                    if (isAccessToStorageJustAsked) {
                        SharedPreferences.Editor e = getPreferenceManager().getSharedPreferences().edit();
                        e.putBoolean(getString(R.string.pref_key_backup_service_enabled), true);
                        e.putBoolean(getString(R.string.pref_key_backup_service_show_notification), true);
                        e.apply();
                        backupService.setChecked(true);
                        try {
//                            ServiceStarter.startServicesDirect(BackupRestorePreferenceFragment.this.getActivity(), ConstantValues.SERVICE_STARTER_START_BACKUP_SERVICE);
                            Utils.setBackupNextRun(getActivity(), AndiCar.getDefaultSharedPreferences().getBoolean(getString(R.string.pref_key_backup_service_enabled), false));
                        } catch (Exception ex) {
                            AndiCarCrashReporter.sendCrash(ex);
                            Log.d(LogTag, ex.getMessage(), ex);
                        }
                        backupServiceShowNotification.setChecked(true);
                        BackupRestorePreferenceFragment.this.setBackupServiceScheduleSummary(null);
                    }
                    backupServiceSchedule.setEnabled(true);
                    backupServiceShowNotification.setEnabled(true);
                } else {
                    backupServiceSchedule.setEnabled(false);
                    backupServiceShowNotification.setEnabled(false);
                }
                secureBkCategory.setEnabled(true);
                secureBkPreference.setSummary(R.string.pref_securebackup_description);
            }

            if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_secure_backup_gdrive_folder_name), "").length() > 0)
                secureBkGDriveFolderPreference.setSummary(getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_secure_backup_gdrive_folder_name), ""));

            if (secureBkCategory != null) {
                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_secure_backup_method), "0").equals("0")) {
                    secureBkCategory.removePreference(secureBkEmailToPreference);
                }
                else {
                    secureBkCategory.removePreference(secureBkGDriveFolderPreference);
                }
            }


            //no access given to read google accounts => disable the secure backup
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_DENIED) {
                secureBkPreference.setChecked(false);
                secureBkGoogleAccountPreference.setEnabled(false);
                secureBkMethodPreference.setEnabled(false);
                secureBkGDriveFolderPreference.setEnabled(false);
                secureBkOnlyWiFiPreference.setEnabled(false);
                secureBkShowNotification.setEnabled(false);
                secureBkEmailToPreference.setEnabled(false);
                secureBkSendTrackFilesPreference.setEnabled(false);
                revalidateAccountPreference.setEnabled(false);
            } else {
                secureBkPreference.setSummary(R.string.pref_securebackup_description);

                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_google_account), "").length() == 0 && !isAccessToAccountsJustAsked) {
                    secureBkPreference.setChecked(false);
                    SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
                    editor.putBoolean(getString(R.string.pref_key_secure_backup_enabled), false);
                    editor.apply();
                }


                if (isAccessToAccountsJustAsked) {
                    isAccessToAccountsJustAsked = false;
                    if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_google_account), "").length() == 0) {
                        showGoogleAccountChooser();
                    }
                } else {
                    if (getPreferenceManager().getSharedPreferences().getBoolean(getString(R.string.pref_key_secure_backup_enabled), false)) {
                        secureBkGoogleAccountPreference.setEnabled(true);
                        secureBkMethodPreference.setEnabled(true);
                        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                            secureBkGDriveFolderPreference.setEnabled(true);
                        secureBkOnlyWiFiPreference.setEnabled(true);
                        secureBkShowNotification.setEnabled(true);
                        if (isGMailAccessGranted) {
                            secureBkEmailToPreference.setEnabled(true);
                        }
                        secureBkSendTrackFilesPreference.setEnabled(true);
                        revalidateAccountPreference.setEnabled(true);
                    } else {
                        SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
                        editor.putString(getString(R.string.pref_key_google_account), null);
                        editor.apply();
                        secureBkMethodPreference.setEnabled(false);
                        secureBkGDriveFolderPreference.setEnabled(false);
                        secureBkGoogleAccountPreference.setSummary(R.string.pref_google_account_description);
                        secureBkGoogleAccountPreference.setEnabled(false);
                        secureBkOnlyWiFiPreference.setEnabled(false);
                        secureBkShowNotification.setEnabled(false);
                        secureBkEmailToPreference.setEnabled(false);
                        secureBkSendTrackFilesPreference.setEnabled(false);
                        revalidateAccountPreference.setEnabled(false);
                    }
                }
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_secure_backup_method), "0").equals("0")) {
                if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
                    secureBkPreference.setChecked(false);
                }

            }
            else {
                if (!isGMailAccessGranted) {
                    secureBkPreference.setChecked(false);
                }
            }
        }

        private void showGoogleAccountChooser() {
            if (isAccountChooserIsShown)
                return;

            isAccountChooserIsShown = true;
            BackupRestorePreferenceFragment.this.startActivityForResult(
                    mGoogleCredential.newChooseAccountIntent(), ConstantValues.REQUEST_ACCOUNT_PICKER);
        }

        private void fillBKFileList() {
            ArrayList<String> bkFiles = FileUtils.listBkFiles(getActivity(), true);
            if (bkFiles.size() == 1) { //just the choose other
                listBackupsPreference.setSummary(R.string.pref_restore_backup_not_found_summary);
                listBackupsPreference.setEnabled(false);
            } else {
                listBackupsPreference.setSummary(R.string.pref_backup_list_description);
                listBackupsPreference.setEnabled(true);
//                restorePreference.setSummary(R.string.pref_restore_description);
//                restorePreference.setEnabled(true);
            }

            CharSequence bkEntries[] = bkFiles.toArray(new CharSequence[bkFiles.size()]);
            restorePreference.setEntries(bkEntries);
            restorePreference.setEntryValues(bkEntries);
            restorePreference.setDialogTitle(R.string.pref_restore_list_title);
        }

        /**
         * construct a string representation of the backup service schedule
         *
         * @param newValue used when called from onPreferenceChangeListener because the call happen before the change of the preference value
         */
        private void setBackupServiceScheduleSummary(Boolean newValue) {
            if ((newValue != null && newValue)
                    || (newValue == null && AndiCar.getDefaultSharedPreferences().getBoolean(getString(R.string.pref_key_backup_service_enabled), true))) {

                if (AndiCar.getDefaultSharedPreferences().getString(
                        getString(R.string.pref_key_backup_service_schedule_type), ConstantValues.BACKUP_SERVICE_DAILY).equals(ConstantValues.BACKUP_SERVICE_DAILY)) {

                    backupServiceSchedule.setSummary(
                            String.format(
                                    AndiCar.getAppResources().getString(R.string.pref_backup_service_schedule_summary_daily),
                                    Utils.getTimeString(getActivity(),
                                            AndiCar.getDefaultSharedPreferences().getInt(getString(R.string.pref_key_backup_service_exec_hour), 21),
                                            AndiCar.getDefaultSharedPreferences().getInt(getString(R.string.pref_key_backup_service_exec_minute), 0)))
                    );
                } else {
                    StringBuilder dayList = new StringBuilder();
                    String prefDays = AndiCar.getDefaultSharedPreferences().getString(getString(R.string.pref_key_backup_service_backup_days), "1111111");
                    for (int i = 0; i < 7; i++) {
                        if (prefDays.charAt(i) == '0') //not selected day
                        {
                            continue;
                        }

                        switch (i) {
                            case 0:
                                dayList.append(dayList.length() == 0 ? getString(R.string.day_of_week_0) : ", " + getString(R.string.day_of_week_0));
                                break;
                            case 1:
                                dayList.append(dayList.length() == 0 ? getString(R.string.day_of_week_1) : ", " + getString(R.string.day_of_week_1));
                                break;
                            case 2:
                                dayList.append(dayList.length() == 0 ? getString(R.string.day_of_week_2) : ", " + getString(R.string.day_of_week_2));
                                break;
                            case 3:
                                dayList.append(dayList.length() == 0 ? getString(R.string.day_of_week_3) : ", " + getString(R.string.day_of_week_3));
                                break;
                            case 4:
                                dayList.append(dayList.length() == 0 ? getString(R.string.day_of_week_4) : ", " + getString(R.string.day_of_week_4));
                                break;
                            case 5:
                                dayList.append(dayList.length() == 0 ? getString(R.string.day_of_week_5) : ", " + getString(R.string.day_of_week_5));
                                break;
                            case 6:
                                dayList.append(dayList.length() == 0 ? getString(R.string.day_of_week_6) : ", " + getString(R.string.day_of_week_6));
                                break;
                        }

                    }

                    backupServiceSchedule.setSummary(String.format(
                            AndiCar.getAppResources().getString(R.string.pref_backup_service_schedule_summary_weekly),
                            dayList.toString(),
                            Utils.getTimeString(getActivity(),
                                    AndiCar.getDefaultSharedPreferences().getInt(getString(R.string.pref_key_backup_service_exec_hour), 21),
                                    AndiCar.getDefaultSharedPreferences().getInt(getString(R.string.pref_key_backup_service_exec_minute), 0))));
                }
            } else {
                backupServiceSchedule.setSummary("");
            }

        }

        /*
        listen for google account verification gmail task
         */
        @Override
        public void onTaskCompleted(String successMessage) {
            if (mProgress != null) {
                mProgress.dismiss();
            }
            Toast.makeText(getActivity(), R.string.gen_done, Toast.LENGTH_SHORT).show();
        }

        /*
        listen for google account verification gmail task
         */
        @Override
        public void onCancelled(String errorMsg, Exception e) {
            if (mProgress != null) {
                mProgress.dismiss();
            }
            if (e != null) {
                if (e instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) e).getConnectionStatusCode());
                } else if (e instanceof UserRecoverableAuthIOException) {
                    getActivity().startActivityForResult(
                            ((UserRecoverableAuthIOException) e).getIntent(), ConstantValues.REQUEST_GMAIL_AUTHORIZATION);
                } else {
                    AndiCarCrashReporter.sendCrash(e);
                    Log.d("SendGMailTask", "The following error occurred:\n" + e.getMessage(), e);
                }
            } else {
                Log.d("SendGMailTask", "Request cancelled.");
            }

        }

        /**
         * Display an error dialog showing that Google Play Services is missing
         * or out of date.
         *
         * @param connectionStatusCode code describing the presence (or lack of)
         *                             Google Play Services on this device.
         */
        void showGooglePlayServicesAvailabilityErrorDialog(
                final int connectionStatusCode) {
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }

            try {
                GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
                Dialog dialog = apiAvailability.getErrorDialog(
                        getActivity(),
                        connectionStatusCode,
                        ConstantValues.REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            } catch (Exception e) {
                Utils.showNotReportableErrorDialog(getActivity(), e.getMessage(), "", false);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == ConstantValues.REQUEST_GET_ACCOUNTS) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_google_account), "").length() == 0) {
                        showGoogleAccountChooser();
                    }
                }
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                if (!isLargeScreen(getActivity())) {
                    startActivity(new Intent(getActivity(), PreferenceActivity.class));
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }

        /**
         * Check that Google Play services APK is installed and up to date.
         *
         * @return true if Google Play Services is available and up to
         * date on this device; false otherwise.
         */
        private boolean isGooglePlayServicesAvailable() {
            GoogleApiAvailability apiAvailability =
                    GoogleApiAvailability.getInstance();
            final int connectionStatusCode =
                    apiAvailability.isGooglePlayServicesAvailable(getActivity());
            return connectionStatusCode == ConnectionResult.SUCCESS;
        }

        /**
         * Attempt to resolve a missing, out-of-date, invalid or disabled Google
         * Play Services installation via a user dialog, if possible.
         */
        private void acquireGooglePlayServices() {
            GoogleApiAvailability apiAvailability =
                    GoogleApiAvailability.getInstance();
            final int connectionStatusCode =
                    apiAvailability.isGooglePlayServicesAvailable(getActivity());
            if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
                SharedPreferences settings = getPreferenceManager().getSharedPreferences();
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(getString(R.string.pref_key_secure_backup_enabled), false);
                editor.apply();
                showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            SharedPreferences settings = getPreferenceManager().getSharedPreferences();
            SharedPreferences.Editor editor = settings.edit();
            switch (requestCode) {
                case ConstantValues.REQUEST_BACKUP_SERVICE_SCHEDULE:
                    try {
                        Utils.setBackupNextRun(BackupRestorePreferenceFragment.this.getActivity(), true);
                    } catch (Exception e) {
                        AndiCarCrashReporter.sendCrash(e);
                        Log.d(LogTag, e.getMessage(), e);
                    }
                    break;
                case ConstantValues.REQUEST_ACCOUNT_PICKER:
                    isAccountChooserIsShown = false;
                    if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                        String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                        //account changed
                        if (accountName != null &&
                                (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_google_account), null) == null ||
                                        !getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_google_account), "").equals(accountName))) {
                            editor.putString(getString(R.string.pref_key_google_account), accountName);
                            editor.putString(getString(R.string.pref_key_secure_backup_gdrive_folder_id), "");
                            editor.putString(getString(R.string.pref_key_secure_backup_gdrive_folder_name), "");
                            secureBkGDriveFolderPreference.setSummary("");
                            if (settings.getString(getString(R.string.pref_key_secure_backup_emailTo), "").length() == 0) {
                                editor.putString(getString(R.string.pref_key_secure_backup_emailTo), accountName);
                                secureBkEmailToPreference.setSummary(accountName);
                            }

                            editor.apply();
                            mGoogleCredential.setSelectedAccountName(accountName);
                            secureBkGoogleAccountPreference.setSummary(accountName);
                            if (!isGooglePlayServicesAvailable()) {
                                editor.putBoolean(getString(R.string.pref_key_secure_backup_enabled), false);
                                editor.apply();
                                secureBkPreference.setChecked(false);
                                acquireGooglePlayServices();
                                return;
                            }
                            //check the account: send a test email using it
                            validateGoogleAccount(getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_secure_backup_method), "0"));
                        }
                    }
                    break;
                case ConstantValues.REQUEST_GOOGLE_PLAY_SERVICES:
                    editor.putBoolean(getString(R.string.pref_key_secure_backup_enabled), false);
                    editor.apply();
                    secureBkPreference.setChecked(false);
                    break;
            }
        }

        public static void setGDriveAccessGranted(Context ctx, boolean access) {
            if (mProgress != null) {
                mProgress.dismiss();
            }
            secureBkGDriveFolderPreference.setEnabled(access);
            if (access) {
                isShouldShowGDriveFolderSelector = true;
                mGoogleApiClient.connect();
            }
            else {
                Toast.makeText(ctx, R.string.pref_securebackup_authorization_required, Toast.LENGTH_LONG).show();
            }
        }

        public static void setGMailAccessGranted(Context ctx, boolean access) {
            if (mProgress != null) {
                mProgress.dismiss();
            }

            isGMailAccessGranted = access;
            secureBkEmailToPreference.setEnabled(access);
            if (access) {
                secureBkEmailToPreference
                        .setText(AndiCar.getDefaultSharedPreferences().getString(ctx.getString(R.string.pref_key_google_account), ""));
                secureBkEmailToPreference
                        .setSummary(AndiCar.getDefaultSharedPreferences().getString(ctx.getString(R.string.pref_key_google_account), ""));
            }
            else {
                secureBkEmailToPreference.setText("");
                secureBkEmailToPreference.setSummary("");
                Toast.makeText(ctx, R.string.pref_securebackup_authorization_required, Toast.LENGTH_LONG).show();
            }
        }

        private void validateGoogleAccount(String method) {
            mProgress = new ProgressDialog(getActivity());
            mProgress.setMessage(getResources().getString(R.string.gen_validating_google_account));
            mProgress.show();
            if (method.equals("1")) {
                try {
                    new SendGMailTask(getActivity(), getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_google_account), null),
                            getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_google_account), null),
                            getResources().getString(R.string.gen_test_email_subject), getResources().getString(R.string.gen_test_email_body), null,
                            BackupRestorePreferenceFragment.this).execute();
                }
                catch (Exception e) {
                    if (mProgress != null) {
                        mProgress.dismiss();
                    }

                    if (!(e instanceof GoogleAuthException)) {
                        AndiCarCrashReporter.sendCrash(e);
                        Log.e("AndiCar", e.getMessage(), e);
                    }
                    else {
                        Utils.showReportableErrorDialog(getActivity(), null, e.getMessage(), e, false);
                    }
                }
            }
            else {
                try {
                    mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                            .addApi(Drive.API)
                            .addScope(Drive.SCOPE_FILE)
                            .addConnectionCallbacks(BackupRestorePreferenceFragment.this)
                            .addOnConnectionFailedListener(BackupRestorePreferenceFragment.this)
                            .setAccountName(mGoogleCredential.getSelectedAccountName())
                            .build();
                    isShouldShowGDriveDoneToast = true;
                    mGoogleApiClient.connect();
                }
                catch (Exception e) {
                    if (mProgress != null) {
                        mProgress.dismiss();
                    }
                }
            }
        }

        @Override
        public void run() {
            //called when other backup is restored using the file picker

            Message msg = new Message();
            Bundle msgBundle = new Bundle();

            DBAdapter db = new DBAdapter(BackupRestorePreferenceFragment.this.getActivity());
            String dbPath = db.getDatabase().getPath();
            db.close();
            //create a safe backup before overwriting the existing database
            if (FileUtils.backupDb(BackupRestorePreferenceFragment.this.getActivity(), dbPath, "sbk_", true, null) == null) { //error
                msgBundle.putString(ERROR_MSG_KEY, FileUtils.mLastErrorMessage);
                msg.setData(msgBundle);
                handler.sendMessage(msg);
                return;
            }
            if (FileUtils.restoreDb(BackupRestorePreferenceFragment.this.getActivity(), fPath, dbPath)) {
                try {
//                    ServiceStarter.startServicesDirect(BackupRestorePreferenceFragment.this.getActivity(), ConstantValues.SERVICE_STARTER_START_ALL);
                    Utils.setToDoNextRun(getActivity());
                } catch (Exception e) {
                    AndiCarCrashReporter.sendCrash(e);
                    Log.d(LogTag, e.getMessage(), e);
                }
                msgBundle.putString(SUCCESS_MSG_KEY, getString(R.string.pref_restore_success_message));
                msg.setData(msgBundle);
                handler.sendMessage(msg);
//                Utils.showInfoDialog(getActivity(), getString(R.string.pref_restore_success_message), null);
            } else {
                msgBundle.putString(ERROR_MSG_KEY, FileUtils.mLastErrorMessage);
                msg.setData(msgBundle);
                handler.sendMessage(msg);
//                Utils.showNotReportableErrorDialog(getActivity(), FileUtils.mLastErrorMessage, null, false);
            }
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            if (mProgress != null) {
                mProgress.dismiss();
            }
            secureBkGDriveFolderPreference.setEnabled(true);
            if (isShouldShowGDriveFolderSelector
                    || AndiCar.getDefaultSharedPreferences().getString(getString(R.string.pref_key_secure_backup_gdrive_folder_id), "").equals("")) {
                showGDriveFolderSelector(getActivity());
                isShouldShowGDriveFolderSelector = false;
            }
            if (isShouldShowGDriveDoneToast) {
                Toast.makeText(getActivity(), R.string.gen_done, Toast.LENGTH_SHORT).show();
                isShouldShowGDriveDoneToast = false;
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            if (mProgress != null) {
                mProgress.dismiss();
            }
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            if (mProgress != null) {
                mProgress.dismiss();
            }

            secureBkGDriveFolderPreference.setEnabled(false);
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(BackupRestorePreferenceFragment.this.getActivity(), ConstantValues.REQUEST_GDRIVE_AUTHORIZATION);
                } catch (IntentSender.SendIntentException e) {
                    // Unable to resolve, message user appropriately
                }
            } else {
                GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), getActivity(), 0).show();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GPSTrackPreferenceFragment extends PreferenceFragment {

        Preference gpsTrackFileLocation;
        Preference btCarLink;
//        ListPreference minTrackTime;
//        ListPreference minAccuracy;
//        ListPreference onBTDisconnect;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_gps_track);
            setHasOptionsMenu(true);

            gpsTrackFileLocation = findPreference(getString(R.string.pref_key_gps_track_file_location));
            gpsTrackFileLocation.setSummary(String.format(getString(R.string.pref_gps_track_file_location), ConstantValues.TRACK_FOLDER));

            btCarLink = findPreference(getString(R.string.pref_key_bt_device_link));
            btCarLink.setSummary(getString(R.string.pref_bt_link_description));
            btCarLink.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(GPSTrackPreferenceFragment.this.getActivity(), CommonListActivity.class);
                    i.putExtra(CommonListActivity.ACTIVITY_TYPE_KEY, CommonListActivity.ACTIVITY_TYPE_BT_CAR_LINK);
                    i.putExtra(CommonListActivity.SCROLL_TO_POSITION_KEY, 0);
                    i.putExtra(CommonListActivity.IS_SHOW_SEARCH_MENU_KEY, false);
                    GPSTrackPreferenceFragment.this.startActivity(i);
                    return true;
                }
            });


//            minTrackTime = (ListPreference) findPreference(getString(R.string.pref_key_gps_track_min_time));
            if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_gps_track_min_time), null) != null) {
                setDefaultPreferenceChangeListener(findPreference(getString(R.string.pref_key_gps_track_min_time)));
            }

//            minAccuracy = (ListPreference) findPreference(getString(R.string.pref_key_gps_track_min_accuracy));
            if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_gps_track_min_accuracy), null) != null) {
                setDefaultPreferenceChangeListener(findPreference(getString(R.string.pref_key_gps_track_min_accuracy)));
            }

//            onBTDisconnect = (ListPreference) findPreference(getString(R.string.pref_key_bt_on_disconnect));
            if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_bt_on_disconnect), null) != null) {
                setDefaultPreferenceChangeListener(findPreference(getString(R.string.pref_key_bt_on_disconnect)));
            }

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                if (!isLargeScreen(getActivity())) {
                    startActivity(new Intent(getActivity(), PreferenceActivity.class));
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MainScreenPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
//        Preference mainAddBtn;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main_screen);
            setHasOptionsMenu(true);

            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            Preference mainAddBtn = findPreference(getString(R.string.pref_key_main_addbtn));
            if (mainAddBtn != null) {
                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_main_addbtn), null) != null) {
                    setDefaultPreferenceChangeListener(mainAddBtn);
                }
            }

            Preference mainZone1Content = findPreference(getString(R.string.pref_key_main_zone1_content));
            if (mainZone1Content != null) {
                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_main_zone1_content), null) != null) {
                    setDefaultPreferenceChangeListener(mainZone1Content);
                }
            }

            Preference mainZone2Content = findPreference(getString(R.string.pref_key_main_zone2_content));
            if (mainZone2Content != null) {
                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_main_zone2_content), null) != null) {
                    setDefaultPreferenceChangeListener(mainZone2Content);
                }
            }

            Preference mainZone3Content = findPreference(getString(R.string.pref_key_main_zone3_content));
            if (mainZone3Content != null) {
                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_main_zone3_content), null) != null) {
                    setDefaultPreferenceChangeListener(mainZone3Content);
                }
            }

            Preference mainZone4Content = findPreference(getString(R.string.pref_key_main_zone4_content));
            if (mainZone4Content != null) {
                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_main_zone4_content), null) != null) {
                    setDefaultPreferenceChangeListener(mainZone4Content);
                }
            }

            Preference mainZone5Content = findPreference(getString(R.string.pref_key_main_zone5_content));
            if (mainZone5Content != null) {
                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_main_zone5_content), null) != null) {
                    setDefaultPreferenceChangeListener(mainZone5Content);
                }
            }

            Preference mainZone6Content = findPreference(getString(R.string.pref_key_main_zone6_content));
            if (mainZone6Content != null) {
                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_main_zone6_content), null) != null) {
                    setDefaultPreferenceChangeListener(mainZone6Content);
                }
            }

            Preference mainZone7Content = findPreference(getString(R.string.pref_key_main_zone7_content));
            if (mainZone7Content != null) {
                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_main_zone7_content), null) != null) {
                    setDefaultPreferenceChangeListener(mainZone7Content);
                }
            }

            Preference mainZone8Content = findPreference(getString(R.string.pref_key_main_zone8_content));
            if (mainZone8Content != null) {
                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_main_zone8_content), null) != null) {
                    setDefaultPreferenceChangeListener(mainZone8Content);
                }
            }

            Preference mainZone9Content = findPreference(getString(R.string.pref_key_main_zone9_content));
            if (mainZone9Content != null) {
                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_main_zone9_content), null) != null) {
                    setDefaultPreferenceChangeListener(mainZone9Content);
                }
            }

            Preference mainZone10Content = findPreference(getString(R.string.pref_key_main_zone10_content));
            if (mainZone10Content != null) {
                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_main_zone10_content), null) != null) {
                    setDefaultPreferenceChangeListener(mainZone10Content);
                }
            }

            Preference mainZone11Content = findPreference(getString(R.string.pref_key_main_zone11_content));
            if (mainZone11Content != null) {
                if (getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_main_zone11_content), null) != null) {
                    setDefaultPreferenceChangeListener(mainZone11Content);
                }
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                if (!isLargeScreen(getActivity())) {
                    startActivity(new Intent(getActivity(), PreferenceActivity.class));
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            try {
                if (s.equals(getString(R.string.pref_key_main_addbtn))) {
                    if (!sharedPreferences.getString(s, "").equals("0")) {
                        Toast.makeText(getActivity(), R.string.pref_main_screen_addbtn_hint, Toast.LENGTH_LONG).show();
                    }
                }
            }
            catch (IllegalStateException e) {
                Log.e("AndiCar", e.getMessage(), e);
            }
        }
    }

}
