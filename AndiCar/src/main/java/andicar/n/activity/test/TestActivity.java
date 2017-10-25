package andicar.n.activity.test;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import andicar.n.persistence.AndiCarFileProvider;
import andicar.n.utils.ConstantValues;

@SuppressLint("SetTextI18n")
public class TestActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_CODE_RESOLVE_CONNECTION = 1;
    private static final int REQUEST_CODE_OPEN_DRIVE_FILE = 1000;
    private static final String TAG = "AndiCar";
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(DriveFolder.DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.d(TAG, "Error while trying to create the file");
                return;
            }
            Log.d(TAG, "Created a file with content: " + result.getDriveFile().getDriveId());
        }
    };
    GoogleAccountCredential mGoogleCredential;
    private SharedPreferences mPref = AndiCar.getDefaultSharedPreferences();
    private GoogleApiClient mGoogleApiClient;
    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new ResultCallback<DriveApi.DriveContentsResult>() {

        @Override
        public void onResult(DriveApi.DriveContentsResult result) {

            if (!result.getStatus().isSuccess()) {
                Log.d(TAG, "Error while trying to create new file contents");
                return;
            }

            final DriveContents driveContents = result.getDriveContents();

            // Perform I/O off the UI thread.
            new Thread() {
                @Override
                public void run() {
                    OutputStream outputStream = driveContents.getOutputStream();
                    try {
                        //getting image from the local storage
//                /sdcard/andicar/backups/abk_2017-10-17-092529637.db
                        InputStream inputStream = getContentResolver().openInputStream(
                                AndiCarFileProvider.getUriForFile(getApplicationContext(), "org.andicar2.provider", new File("/sdcard/andicar/backups/abk_2017-10-17-092529637.db")));

                        if (inputStream != null) {
                            byte[] data = new byte[1024];
                            while (inputStream.read(data) != -1) {
                                //Reading data from local storage and writing to google drive
                                outputStream.write(data);
                            }
                            inputStream.close();
                        }

                        outputStream.close();
                    }
                    catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }


                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("abk_2017-10-17-092529637.db")
                            .setMimeType("application/octet-stream")
                            .setStarred(true).build();


                    DriveApi.DriveIdResult exFolderResult = Drive.DriveApi
                            .fetchDriveId(mGoogleApiClient, mPref.getString(getString(R.string.pref_key_google_drive_folder_id), "")) //existing folder id = 0B_cMuo4-XwcAZ3IzSG1jajFlWk0
                            .await();

                    if (!exFolderResult.getStatus().isSuccess()) {
                        Log.d(TAG, "Cannot find DriveId. Are you authorized to view this file?");
                        return;
                    }

                    final DriveFolder folder = exFolderResult.getDriveId().asDriveFolder();

                    // create a file on root folder
                    folder.createFile(mGoogleApiClient, changeSet, driveContents)
                            .setResultCallback(fileCallback);

                }
            }.start();

        }
    };
    private TextView text1;
    private TextView text2;
    private TextView text3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        text1 = (TextView) findViewById(R.id.text1);
        text2 = (TextView) findViewById(R.id.text2);
        text3 = (TextView) findViewById(R.id.text3);

        mGoogleCredential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(ConstantValues.GOOGLE_SCOPES)).setBackOff(new ExponentialBackOff());
        mGoogleCredential.setSelectedAccountName(mPref.getString(getString(R.string.pref_key_google_account), ""));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .setAccountName(mGoogleCredential.getSelectedAccountName())
                .build();

        Button btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
//                    mGoogleApiClient.clearDefaultAccountAndReconnect();
//                }
                text1.setText("");
                startActivityForResult(
                        mGoogleCredential.newChooseAccountIntent(), ConstantValues.REQUEST_ACCOUNT_PICKER);
            }
        });

        Button btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    IntentSender intentSender = Drive.DriveApi
                            .newOpenFileActivityBuilder()
                            .setMimeType(new String[]{DriveFolder.MIME_TYPE})
                            .build(mGoogleApiClient);
                    try {
                        startIntentSenderForResult(
                                intentSender, REQUEST_CODE_OPEN_DRIVE_FILE, null, 0, 0, 0);
                    }
                    catch (IntentSender.SendIntentException e) {
                        Log.i("AndiCar", "Failed to launch file chooser.");
                    }
                }
            }
        });

        Button btn3 = (Button) findViewById(R.id.btn3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(driveContentsCallback);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_CONNECTION);
            }
            catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        }
        else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        text1.setText("Connected to: " + mGoogleCredential.getSelectedAccountName());
        text2.setText(mPref.getString(getString(R.string.pref_key_google_drive_folder_name), ""));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case ConstantValues.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK) {
                    mGoogleCredential.setSelectedAccountName(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addApi(Drive.API)
                            .addScope(Drive.SCOPE_FILE)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .setAccountName(mGoogleCredential.getSelectedAccountName())
                            .build();
                    mGoogleApiClient.connect();
                }
                break;
            case REQUEST_CODE_RESOLVE_CONNECTION:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
            case REQUEST_CODE_OPEN_DRIVE_FILE:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);//this extra contains the drive id of the selected file
                    Log.i(TAG, "Selected folder's ID: " + driveId.encodeToString());
                    Log.i(TAG, "Selected folder's Resource ID: " + driveId.getResourceId());
                    SharedPreferences.Editor e = mPref.edit();
                    e.putString(getString(R.string.pref_key_google_drive_folder_id), driveId.getResourceId());
                    e.apply();

////                    selected file (can also be a folder)
                    final DriveFolder selectedFolder = driveId.asDriveFolder();
                    final PendingResult selectedFolderMetadata = selectedFolder.getMetadata(mGoogleApiClient);

//                    fetch the selected item's metadata asynchronously using a pending result
                    selectedFolderMetadata.setResultCallback(new ResultCallback() {
                        @Override
                        public void onResult(@NonNull Result result) {
                            Metadata fileMetadata = ((DriveResource.MetadataResult) result).getMetadata();
                            text2.setText(fileMetadata.getTitle());
                            SharedPreferences.Editor e = mPref.edit();
                            e.putString(getString(R.string.pref_key_google_drive_folder_name), fileMetadata.getTitle());
                            e.apply();
//                            get the details out of the metadata object
                            Log.i(TAG, "File title: " + fileMetadata.getTitle());
                            Log.i(TAG, "File size: " + fileMetadata.getFileSize());
                            Log.i(TAG, "File mime type: " + fileMetadata.getMimeType());
                        }
                    });
                }
                break;
        }
    }
}
