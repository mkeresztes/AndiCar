package andicar.n.service;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import andicar.n.persistence.AndiCarFileProvider;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.LogFileWriter;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 25.10.2017.
 */

public class GDriveUploaderTask extends AsyncTask<Void, Void, List<String>> {
    private static final String TAG = "AndiCar";

    private GoogleApiClient mGoogleApiClient;
    private Context mCtx;
    private String mDriveFolderID;
    private String mFile;
    private String mMimeType;
    private ResultCallback<DriveFolder.DriveFileResult> mFileUploadCallback;

    private Exception mLastException = null;
    private LogFileWriter debugLogFileWriter = null;

    public GDriveUploaderTask(Context ctx, GoogleApiClient googleApiClient, String driveFolderID, String file, String mimeType,
                              ResultCallback<DriveFolder.DriveFileResult> fileUploadCallback) throws Exception {
        try {
            if (FileUtils.isFileSystemAccessGranted(ctx)) {
                FileUtils.createFolderIfNotExists(ctx, ConstantValues.LOG_FOLDER);
                File debugLogFile = new File(ConstantValues.LOG_FOLDER + "GDRiveUploaderTask.log");
                debugLogFileWriter = new LogFileWriter(debugLogFile, false);
                debugLogFileWriter.appendnl("SendGMailTask begin");
                debugLogFileWriter.flush();
            }
            mGoogleApiClient = googleApiClient;
            mCtx = ctx;
            mDriveFolderID = driveFolderID;
            mFile = file;
            mMimeType = mimeType;
            mFileUploadCallback = fileUploadCallback;
        }
        catch (Exception e) {
            if (debugLogFileWriter != null) {
                try {
                    debugLogFileWriter.appendnl("An error occured: ").append(e.getMessage()).append(Utils.getStackTrace(e));
                    debugLogFileWriter.flush();
                }
                catch (IOException ignored) {
                }
            }
            throw e;
        }
    }

    @Override
    protected List<String> doInBackground(Void... params) {
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(mDriveContentsCallback);
        return null;
    }

    final private ResultCallback<DriveApi.DriveContentsResult> mDriveContentsCallback = new ResultCallback<DriveApi.DriveContentsResult>() {

        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult result) {

            if (!result.getStatus().isSuccess()) {
                Log.d(TAG, "Error while trying to create new file contents");
                return;
            }

            final DriveContents driveContents = result.getDriveContents();

            OutputStream outputStream = driveContents.getOutputStream();
            try {
                File fileToUpload = new File(mFile);
                InputStream inputStream = mCtx.getContentResolver().openInputStream(
                        AndiCarFileProvider.getUriForFile(mCtx, "org.andicar2.provider", fileToUpload));

                if (inputStream != null) {
                    byte[] data = new byte[1024];
                    while (inputStream.read(data) != -1) {
                        //Reading data from local storage and writing to google drive
                        outputStream.write(data);
                    }
                    inputStream.close();
                }

                outputStream.close();

                final MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(fileToUpload.getName())
                        .setMimeType(mMimeType)
                        .setStarred(false).build();

//                DriveApi.DriveIdResult exFolderResult = Drive.DriveApi
//                        .fetchDriveId(mGoogleApiClient, mDriveFolderID) //existing folder id = 0B_cMuo4-XwcAZ3IzSG1jajFlWk0
//                        .await();
//
//                if (!exFolderResult.getStatus().isSuccess()) {
//                    Log.d(TAG, "Cannot find DriveId. Are you authorized to view this file?");
//                    return;
//                }
//
//                DriveId driveId = exFolderResult.getDriveId();
//                //showMessage("driveid" + driveId.getResourceId());
//                final DriveFolder folder = driveId.asDriveFolder();
//
//
//                // create a file on root folder
//                folder.createFile(mGoogleApiClient, changeSet, driveContents)
//                        .setResultCallback(mFileUploadCallback);

                Drive.DriveApi.fetchDriveId(mGoogleApiClient, mDriveFolderID).setResultCallback(new ResultCallback<DriveApi.DriveIdResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveIdResult driveIdResult) {
                        if (!driveIdResult.getStatus().isSuccess()) {
                            Log.d(TAG, "Cannot find DriveId. Are you authorized to view this file?");
                            return;
                        }

                        final DriveFolder folder = driveIdResult.getDriveId().asDriveFolder();

                        // create a file on root folder
                        folder.createFile(mGoogleApiClient, changeSet, driveContents)
                                .setResultCallback(mFileUploadCallback);
                    }
                });
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    };

//    final private ResultCallback<DriveFolder.DriveFileResult> mFileUploadCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
//        @Override
//        public void onResult(@NonNull DriveFolder.DriveFileResult result) {
//            if (!result.getStatus().isSuccess()) {
//                Log.d(TAG, "Error while trying to create the file");
//                return;
//            }
//            Log.d(TAG, "Created a file with content: " + result.getDriveFile().getDriveId());
//        }
//    };
}
