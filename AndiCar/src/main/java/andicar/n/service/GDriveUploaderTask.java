package andicar.n.service;

import android.content.Context;
import android.support.annotation.NonNull;

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

import andicar.n.interfaces.AndiCarAsyncTaskListener;
import andicar.n.persistence.AndiCarFileProvider;
import andicar.n.utils.ConstantValues;
import andicar.n.utils.FileUtils;
import andicar.n.utils.LogFileWriter;
import andicar.n.utils.Utils;

/**
 * Created by Miklos Keresztes on 25.10.2017.
 */

public class GDriveUploaderTask {
//    private static final String TAG = "AndiCar";

    private GoogleApiClient mGoogleApiClient;
    private Context mCtx;
    private String mDriveFolderID;
    private String mFile;
    private String mMimeType;
    private AndiCarAsyncTaskListener mTaskListener;
    private LogFileWriter debugLogFileWriter = null;

    public GDriveUploaderTask(Context ctx, GoogleApiClient googleApiClient, String driveFolderID, String file, String mimeType,
                              AndiCarAsyncTaskListener taskListener) throws Exception {
        try {
            if (FileUtils.isFileSystemAccessGranted(ctx)) {
                FileUtils.createFolderIfNotExists(ctx, ConstantValues.LOG_FOLDER);
                File debugLogFile = new File(ConstantValues.LOG_FOLDER + "GDriveUploaderTask.log");
                debugLogFileWriter = new LogFileWriter(debugLogFile, false);
                debugLogFileWriter.appendnl("GDriveUploaderTask begin");
                debugLogFileWriter.flush();
            }
            mGoogleApiClient = googleApiClient;
            mCtx = ctx;
            mDriveFolderID = driveFolderID;
            mFile = file;
            mMimeType = mimeType;
            mTaskListener = taskListener;
        }
        catch (Exception e) {
            if (mTaskListener != null)
                mTaskListener.onCancelled(null, e);

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

    public void startUpload() {
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(mDriveContentsCallback);
    }

    final private ResultCallback<DriveApi.DriveContentsResult> mDriveContentsCallback = new ResultCallback<DriveApi.DriveContentsResult>() {

        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult result) {

            if (!result.getStatus().isSuccess()) {
                if (debugLogFileWriter != null) {
                    try {
                        debugLogFileWriter.appendnl("Error while trying to create new file contents: ").append(result.toString());
                        debugLogFileWriter.flush();
                    } catch (IOException ignored) {
                    }
                }
                if (mTaskListener != null)
                    mTaskListener.onCancelled("Error while trying to create new file contents", null);
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

                Drive.DriveApi.fetchDriveId(mGoogleApiClient, mDriveFolderID).setResultCallback(new ResultCallback<DriveApi.DriveIdResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveIdResult driveIdResult) {
                        if (!driveIdResult.getStatus().isSuccess()) {
                            if (mTaskListener != null)
                                mTaskListener.onCancelled("Cannot find drive folder. Are you authorized to view this file?", null);

                            try {
                                debugLogFileWriter.appendnl("Cannot find DriveId. ").append(driveIdResult.toString());
                                debugLogFileWriter.flush();
                            } catch (IOException ignored) {
                            }
                            return;
                        }

                        final DriveFolder folder = driveIdResult.getDriveId().asDriveFolder();

                        // create a file on root folder
                        folder.createFile(mGoogleApiClient, changeSet, driveContents)
                                .setResultCallback(mFileUploadCallback);
                    }
                });
            } catch (Exception e) {
                try {
                    debugLogFileWriter.appendnl("Unexpected error!\n").append(Utils.getStackTrace(e));
                    debugLogFileWriter.flush();
                } catch (IOException ignored) {
                }

                if (mTaskListener != null)
                    mTaskListener.onCancelled(null, e);
            }
        }
    };

    final private ResultCallback<DriveFolder.DriveFileResult> mFileUploadCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                if (debugLogFileWriter != null) {
                    try {
                        debugLogFileWriter.appendnl("Error while trying to create the file: ").append(result.toString());
                        debugLogFileWriter.flush();
                    } catch (IOException ignored) {
                    }
                }
                if (mTaskListener != null)
                    mTaskListener.onCancelled("Error while trying to create the file", null);
                return;
            }
            if (debugLogFileWriter != null) {
                try {
                    debugLogFileWriter.appendnl("Created a file with content: ").append(result.getDriveFile().getDriveId().toString());
                    debugLogFileWriter.flush();
                } catch (IOException ignored) {
                }
            }
            if (mTaskListener != null)
                mTaskListener.onTaskCompleted("Created a file with content: " + result.getDriveFile().getDriveId());
        }
    };
}
