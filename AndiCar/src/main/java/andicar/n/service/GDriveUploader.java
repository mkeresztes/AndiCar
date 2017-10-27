package andicar.n.service;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

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

public class GDriveUploader {
//    private static final String TAG = "AndiCar";

    private GoogleApiClient mGoogleApiClient;
    private Context mCtx;
    private String mDriveFolderID;
    private String mFile;
    private String mMimeType;
    private AndiCarAsyncTaskListener mTaskListener;
    private LogFileWriter debugLogFileWriter = null;
    private Resources mResource;

    public GDriveUploader(Context ctx, GoogleApiClient googleApiClient, String driveFolderID, String file, String mimeType,
                          AndiCarAsyncTaskListener taskListener) throws Exception {
        try {
            if (FileUtils.isFileSystemAccessGranted(ctx)) {
                FileUtils.createFolderIfNotExists(ctx, ConstantValues.LOG_FOLDER);
                File debugLogFile = new File(ConstantValues.LOG_FOLDER + "GDriveUploader.log");
                debugLogFileWriter = new LogFileWriter(debugLogFile, false);
                debugLogFileWriter.appendnl("GDriveUploader started for file: ").append(file);
                debugLogFileWriter.flush();
            }
            mGoogleApiClient = googleApiClient;
            mCtx = ctx;
            mDriveFolderID = driveFolderID;
            mFile = file;
            mMimeType = mimeType;
            mTaskListener = taskListener;
            mResource = ctx.getResources();
        }
        catch (Exception e) {
            if (mTaskListener != null) {
                mTaskListener.onAndiCarTaskCancelled(null, e);
            }

            if (debugLogFileWriter != null) {
                try {
                    debugLogFileWriter.appendnl("An error occurred: ").append(e.getMessage()).append(Utils.getStackTrace(e));
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
                        debugLogFileWriter.appendnl("Error while trying to upload the backup: ").append(result.toString());
                        debugLogFileWriter.flush();
                    } catch (IOException ignored) {
                    }
                }
                if (mTaskListener != null) {
                    mTaskListener.onAndiCarTaskCancelled(String.format(mResource.getString(R.string.error_116), result.toString()), null);
                }
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
                            if (mTaskListener != null) {
                                mTaskListener.onAndiCarTaskCancelled(String.format(mResource.getString(R.string.error_117),
                                        AndiCar.getDefaultSharedPreferences().getString(mCtx.getResources().getString(R.string.pref_key_secure_backup_gdrive_folder_name), "N/A")), null);
                            }

                            try {
                                debugLogFileWriter.appendnl("Cannot find folder: ")
                                        .append(AndiCar.getDefaultSharedPreferences().getString(mCtx.getResources().getString(R.string.pref_key_secure_backup_gdrive_folder_name), "N/A"));
                                debugLogFileWriter.flush();
                            } catch (IOException ignored) {
                            }
                            return;
                        }

                        final DriveFolder folder = driveIdResult.getDriveId().asDriveFolder();
                        folder.getMetadata(mGoogleApiClient).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                            @Override
                            public void onResult(@NonNull DriveResource.MetadataResult metadataResult) {
                                Metadata folderMetadata = metadataResult.getMetadata();

                                if (!folderMetadata.isFolder()) {
                                    if (mTaskListener != null) {
                                        mTaskListener.onAndiCarTaskCancelled(String.format(mResource.getString(R.string.error_118), folderMetadata.getTitle()), null);
                                    }

                                    try {
                                        debugLogFileWriter.appendnl("Selected drive resource (" + folderMetadata.getTitle() + ") is not a folder!");
                                        debugLogFileWriter.flush();
                                    } catch (IOException ignored) {
                                    }
                                } else if (folderMetadata.isTrashed() || folderMetadata.isExplicitlyTrashed()) {
                                    if (mTaskListener != null) {
                                        mTaskListener.onAndiCarTaskCancelled(String.format(mResource.getString(R.string.error_119), folderMetadata.getTitle()), null);
                                    }

                                    try {
                                        debugLogFileWriter.appendnl("Selected drive folder (" + folderMetadata.getTitle() + ")  was deleted!");
                                        debugLogFileWriter.flush();
                                    } catch (IOException ignored) {
                                    }
                                } else if (folderMetadata.isRestricted()) {
                                    if (mTaskListener != null) {
                                        mTaskListener.onAndiCarTaskCancelled(String.format(mResource.getString(R.string.error_120), folderMetadata.getTitle()), null);
                                    }

                                    try {
                                        debugLogFileWriter.appendnl("Selected drive folder (" + folderMetadata.getTitle() + ")  is restricted!");
                                        debugLogFileWriter.flush();
                                    } catch (IOException ignored) {
                                    }
                                } else {
                                    // create a file on root folder
                                    folder.createFile(mGoogleApiClient, changeSet, driveContents)
                                            .setResultCallback(mFileUploadCallback);
                                }
                            }
                        });

                    }
                });
            } catch (Exception e) {
                try {
                    debugLogFileWriter.appendnl("Unexpected error!\n").append(Utils.getStackTrace(e));
                    debugLogFileWriter.flush();
                } catch (IOException ignored) {
                }

                if (mTaskListener != null) {
                    mTaskListener.onAndiCarTaskCancelled(null, e);
                }
            }
        }
    };

    final private ResultCallback<DriveFolder.DriveFileResult> mFileUploadCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                if (debugLogFileWriter != null) {
                    try {
                        debugLogFileWriter.appendnl("mFileUploadCallback: Error while trying to create the file: ").append(result.toString());
                        debugLogFileWriter.flush();
                    } catch (IOException ignored) {
                    }
                }
                if (mTaskListener != null) {
                    mTaskListener.onAndiCarTaskCancelled(String.format(mResource.getString(R.string.error_116), result.toString()), null);
                }
                return;
            }
            if (debugLogFileWriter != null) {
                try {
                    debugLogFileWriter.appendnl("Success. Created a file with content: ").append(result.getDriveFile().getDriveId().toString());
                    debugLogFileWriter.flush();
                } catch (IOException ignored) {
                }
            }
            if (mTaskListener != null) {
                mTaskListener.onAndiCarTaskCompleted(mResource.getString(R.string.secure_backup_success_message));
            }
        }
    };
}
