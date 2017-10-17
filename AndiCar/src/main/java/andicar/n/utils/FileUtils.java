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

package andicar.n.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.andicar2.activity.AndiCar;
import org.andicar2.activity.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import andicar.n.persistence.AndiCarFileProvider;
import andicar.n.service.JobStarter;
import andicar.n.service.SecureBackupJob;

//import org.andicar.andicar.n.activity.dialog.AndiCarDialogBuilder;

/**
 * @author miki
 */
@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored"})
public class FileUtils {
    private static final Resources mResources = AndiCar.getAppResources();
    private static final String LogTag = "FileUtils";
    private static final SharedPreferences mPreferences = AndiCar.getDefaultSharedPreferences();
    public static String mLastErrorMessage = null;
    public static Exception mLastException = null;

    /**
     * delete a file
     *
     * @param pathToFile to delete
     * @return null on success; error message on error
     */
    @SuppressWarnings("UnusedReturnValue")
    public static String deleteFile(String pathToFile) {
        try {
            File file = new File(pathToFile);
            if (!file.delete()) {
                return String.format(mResources.getString(R.string.error_105), pathToFile);
            }
        }
        catch (Exception e) {
            mLastErrorMessage = e.getMessage();
            mLastException = e;
            return e.getMessage();
        }
        return null;
    }

    public static ArrayList<String> listBkFiles(Context ctx, boolean addOtherChoice) {
        ArrayList<String> fileNames = FileUtils.getFileNames(ctx, ConstantValues.BACKUP_FOLDER, null);
        if (fileNames != null) {
            Collections.sort(fileNames, String.CASE_INSENSITIVE_ORDER);
            Collections.reverse(fileNames);
            if (addOtherChoice) {
                fileNames.add(0, ctx.getString(R.string.pref_restore_other));
            }
        }
        else if (addOtherChoice) {
            fileNames = new ArrayList<>();
            fileNames.add(0, ctx.getString(R.string.pref_restore_other));
        }
        return fileNames;
    }

    public static ArrayList<String> listLogFiles(Context ctx) {
        return FileUtils.getFileNames(ctx, ConstantValues.LOG_FOLDER, null);
    }

    @Nullable
    public static ArrayList<String> getFileNames(Context ctx, String folder, @Nullable String fileNameFilterPattern) {
        ArrayList<String> myData = new ArrayList<>();
        File fileDir = new File(folder);
        Pattern p = null;
        Matcher m;

//        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
        if (!FileUtils.isFileSystemAccessGranted(ctx)) {
            return null;
        }
        if (!fileDir.exists() || !fileDir.isDirectory()) {
            return null;
        }

        String[] files = fileDir.list();

        if (files == null || files.length == 0) {
            return null;
        }

        if (fileNameFilterPattern != null) {
            p = Pattern.compile(fileNameFilterPattern);
        }

        for (String file : files) {
            if (fileNameFilterPattern == null) {
                myData.add(file);
            }
            else {
                m = p.matcher(file);
                if (m.matches()) {
                    myData.add(file);
                }
            }
        }
        return myData;
    }

    public static File createGpsTrackDetailFile(String fileFormat, String fileName) {
        return new File(ConstantValues.TRACK_FOLDER + fileName + "." + fileFormat);
    }

    /**
     * Return an Uri to the zip file created
     *
     * @param inputFiles a list of files to be zipped
     * @param outZipFile the destination file name
     * @return the zip file Uri or null on error
     */
    public static Uri zipFiles(Context ctx, Bundle inputFiles, String outZipFile) {
        byte[] buf = new byte[1024];
        ZipOutputStream out;
        try {
            out = new ZipOutputStream(new FileOutputStream(outZipFile));
            Set<String> inputFileNames = inputFiles.keySet();
            String inputFileKey;
            for (String inputFileName : inputFileNames) {
                inputFileKey = inputFileName;
                try {
                    String t = inputFiles.getString(inputFileKey);
                    if (t == null) {
                        return null;
                    }
                    FileInputStream in = new FileInputStream(t);
                    //zip entry name
                    out.putNextEntry(new ZipEntry(inputFileKey));
                    // Transfer bytes from the file to the ZIP file
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    // Complete the entry
                    out.closeEntry();
                    in.close();
                }
                catch (FileNotFoundException ignored) {
                }
            }

            out.close();
//            return Uri.parse("file://" + outZipFile);
            return AndiCarFileProvider.getUriForFile(ctx, "org.andicar2.provider", new File(outZipFile));
        }
        catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            mLastException = ex;
            mLastErrorMessage = ex.getMessage();
            return null;
        }
    }

    //credits: https://stackoverflow.com/a/27050680/6008542
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public static void unzipFile(File zipFile, File targetDirectory) throws IOException {

        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                }
                if (ze.isDirectory()) {
                    continue;
                }
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1) {
                        fout.write(buffer, 0, count);
                    }
                }
                finally {
                    fout.close();
                }
            }
        }
        finally {
            zis.close();
        }
    }
    /**
     * Backup the database to the external storage
     *
     * @param bkPrefix Default: ConstantValues.BACKUP_PREFIX (bk). Prefix for the backup filename (<prefix>filename.db).
     * @param dbPath the path to the database
     * @param skipSecureBk if true the secure backup step will be skipped, even if in the preference is enabled
     * @return The backup file name on success or null on error. See mLastErrorMessage for error details
     */
    public static String backupDb(Context ctx, String dbPath, @Nullable String bkPrefix, boolean skipSecureBk) {

        String bkFile = ConstantValues.BACKUP_FOLDER;
        String bkFileName;
        File debugLogFile;
        LogFileWriter debugLogFileWriter = null;

        try {
//            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            if (!FileUtils.isFileSystemAccessGranted(ctx)) {
                mLastErrorMessage = "External Storage Access denied";
                return null;
            }
            FileUtils.createFolderIfNotExists(ctx, ConstantValues.LOG_FOLDER);

            debugLogFile = new File(ConstantValues.LOG_FOLDER + "backupDB.log");
            debugLogFileWriter = null;

            debugLogFileWriter = new LogFileWriter(debugLogFile, false);

            bkFileName = Utils.appendDateTime(bkPrefix == null ? ConstantValues.BACKUP_PREFIX : bkPrefix, true, true, "-") + ConstantValues.BACKUP_SUFIX;
            bkFile = bkFile + bkFileName;

            debugLogFileWriter.appendnl("db backup started to: ").append(bkFile);

            mLastErrorMessage = null;
            mLastException = null;

            mLastErrorMessage = FileUtils.createFolderIfNotExists(ctx, ConstantValues.BACKUP_FOLDER);
            if (mLastErrorMessage != null) {
                return null;
            }

            if (dbPath == null) {
                mLastErrorMessage = "Invalid database (null)";
                debugLogFileWriter.append("\nInvalid database (null)");
                debugLogFileWriter.flush();
                debugLogFileWriter.close();
                return null;
            }

            mLastErrorMessage = FileUtils.copyFile(ctx, dbPath, bkFile, false);
            if (mLastErrorMessage != null) {
                debugLogFileWriter.append("\nError: ").append(mLastErrorMessage);
                debugLogFileWriter.append("\n\n").append(Utils.getStackTrace(mLastException));
                debugLogFileWriter.flush();
                debugLogFileWriter.close();
                return null;
            }
            else { // if secure backup enabled send the backup file as email attachment
                debugLogFileWriter.appendnl("Backup terminated with success.");

                if (mPreferences.getBoolean(mResources.getString(R.string.pref_key_secure_backup_enabled), false) && !skipSecureBk) {
                    debugLogFileWriter.appendnl("Secure backup enabled. Calling FirebaseJobDispatcher for SecureBackup");
                    Bundle serviceParams = new Bundle();
//                    serviceParams.putString(FBJobService.JOB_TYPE_KEY, FBJobService.JOB_TYPE_SECURE_BACKUP);
                    serviceParams.putString(SecureBackupJob.BK_FILE_KEY, bkFile);

                    JobStarter.startServicesUsingFBJobDispacher(ctx, JobStarter.SERVICE_STARTER_START_SECURE_BACKUP, serviceParams);

                    debugLogFileWriter.appendnl("Calling FirebaseJobDispatcher terminated");
                }
                else {
                    debugLogFileWriter.appendnl("Secure backup is not enabled.");
                }
                debugLogFileWriter.flush();
                debugLogFileWriter.close();
            }
        }
        catch (Exception e) {
            if (debugLogFileWriter != null) {
                try {
                    debugLogFileWriter.appendnl("backup terminated with error: ")
                            .append(e.getMessage()).append("\n");
                    debugLogFileWriter.append(Utils.getStackTrace(e));
                    debugLogFileWriter.flush();
                    debugLogFileWriter.close();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            mLastException = e;
            mLastErrorMessage = e.getMessage();
            AndiCarCrashReporter.sendCrash(e);
            Log.e(LogTag, e.getMessage(), e);
        }
        return bkFile;
    }

    /**
     * creates one or all AndiCar folders
     *
     * @param what ALL; REPORT_FOLDER; BACKUP_FOLDER; TRACK_FOLDER; TEMP_FOLDER; LOG_FOLDER
     * @return null on success an error message on error.
     */
    public static String createFolderIfNotExists(Context ctx, String what) {
        File file;

//        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
        if (!FileUtils.isFileSystemAccessGranted(ctx)) {
            return mResources.getString(R.string.error_102);
        }

        try {
//			file = Environment.getExternalStorageDirectory();
//            if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//                return mResources.getString(R.string.error_020);
//            }
//			if (!file.exists() || !file.isDirectory()) {
//				return mResources.getString(R.string.error_020);
//			}

            if (what.equals("ALL") || what.equals(ConstantValues.REPORT_FOLDER)) {
                file = new File(ConstantValues.REPORT_FOLDER);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        return String.format(mResources.getString(R.string.error_021), ConstantValues.REPORT_FOLDER);
                    }
                }
            }

            if (what.equals("ALL") || what.equals(ConstantValues.BACKUP_FOLDER)) {
                file = new File(ConstantValues.BACKUP_FOLDER);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        return String.format(mResources.getString(R.string.error_024), ConstantValues.BACKUP_FOLDER);
                    }
                }
            }

            if (what.equals("ALL") || what.equals(ConstantValues.TRACK_FOLDER)) {
                file = new File(ConstantValues.TRACK_FOLDER);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        return String.format(mResources.getString(R.string.error_033), ConstantValues.TRACK_FOLDER);
                    }
                }
            }

            if (what.equals("ALL") || what.equals(ConstantValues.TEMP_FOLDER)) {
                file = new File(ConstantValues.TEMP_FOLDER);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        return String.format(mResources.getString(R.string.error_058), ConstantValues.TEMP_FOLDER);
                    }
                }
            }

            if (what.equals("ALL") || what.equals(ConstantValues.LOG_FOLDER)) {
                file = new File(ConstantValues.LOG_FOLDER);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        return String.format(mResources.getString(R.string.error_103), ConstantValues.LOG_FOLDER);
                    }
                }
            }

        }
        catch (SecurityException e) {
            Toast toast = Toast.makeText(ctx, "Error: " + e.getMessage(), Toast.LENGTH_LONG);
            toast.show();
            mLastException = e;
            mLastErrorMessage = e.getMessage();
            return e.getMessage();
        }
        return null;
    }

    /**
     * copy a file to a new location
     *
     * @param sourceFile        source file
     * @param destinationFile   destination file
     * @param overwriteExisting overwrite if the destination exists
     * @return null on success; error message on error
     */
    private static String copyFile(Context ctx, String sourceFile, String destinationFile, boolean overwriteExisting) {

//        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
        if (!FileUtils.isFileSystemAccessGranted(ctx)) {
            return mResources.getString(R.string.error_102);
        }

        try {
            File fromFile = new File(sourceFile);
            if (!fromFile.exists()) {
                return String.format(mResources.getString(R.string.error_100), "");
            }
            File toFile = new File(destinationFile);
            if (overwriteExisting && toFile.exists()) {
                if (!toFile.delete()) {
                    return String.format(mResources.getString(R.string.error_105), destinationFile);
                }
            }
            return copyFile(ctx, fromFile, toFile);
        }
        catch (SecurityException e) {
            mLastException = e;
            mLastErrorMessage = e.getMessage();
            return e.getMessage();
        }
    }

    /**
     * copy a file
     *
     * @param source source file
     * @param dest   destination file
     * @return null on success; error message on failure
     */
    private static String copyFile(Context ctx, File source, File dest) {

//        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
        if (!FileUtils.isFileSystemAccessGranted(ctx)) {
            return mResources.getString(R.string.error_102);
        }

        FileChannel in;
        FileChannel out;
        try {
            FileInputStream fileInputStream = new FileInputStream(source);
            in = fileInputStream.getChannel();
            FileOutputStream fileOutputStream = new FileOutputStream(dest);
            out = fileOutputStream.getChannel();

            long size = in.size();
            MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);

            out.write(buf);

            fileInputStream.close();
            fileOutputStream.close();
            in.close();
            out.close();
        }
        catch (IOException e) {
            Toast toast = Toast.makeText(ctx, "Error: " + e.getMessage(), Toast.LENGTH_LONG);
            toast.show();
            mLastException = e;
            mLastErrorMessage = e.getMessage();
            return e.getMessage();
        }
        return null;
    }

    /**
     * Copy directory method.
     *
     * @param srcDir         the validated source directory, must not be <code>null</code>
     * @param destDir        the validated destination directory, must not be <code>null</code>
     * @param backupIfExists create a backup copy of the destination directory if exists
     * @throws IOException if an error occurs
     * @since Commons IO 1.1
     */
    //adapted from http://grepcode.com/file_/repo1.maven.org/maven2/commons-io/commons-io/1.4/org/apache/commons/io/FileUtils.java/?v=source doCopyDirectory(...)
    public static String copyDirectory(Context ctx, File srcDir, File destDir, boolean backupIfExists) throws IOException {
        if (destDir.exists()) {
            if (backupIfExists && destDir.isDirectory()) {
                if (!destDir.renameTo(new File(destDir.getAbsolutePath() + "_" + System.currentTimeMillis()))) {
                    return mResources.getString(R.string.error_077);
                }
            }
            mLastErrorMessage = deleteDirectory(destDir);
            if (mLastErrorMessage != null) {
                return mLastErrorMessage;
            }
        }
        if (!destDir.mkdirs()) {
            return String.format(mResources.getString(R.string.error_078), destDir);
        }

        if (!destDir.canWrite()) {
            return String.format(mResources.getString(R.string.error_079), destDir);
        }

        // recurse
        File[] files = srcDir.listFiles();
        if (files == null) {  // null if security restricted
            return String.format(mResources.getString(R.string.error_075), srcDir);
        }
        for (File file : files) {
            File copiedFile = new File(destDir, file.getName());
            if (file.isDirectory()) {
                mLastErrorMessage = copyDirectory(ctx, file, copiedFile, false);
                if (mLastErrorMessage != null) {
                    return mLastErrorMessage;
                }
            }
            else {
                mLastErrorMessage = copyFile(ctx, file, copiedFile);
                if (mLastErrorMessage != null) {
                    return mLastErrorMessage;
                }
            }
        }
        return null;
    }


    /**
     * Deletes a directory recursively.
     *
     * @param directory directory to delete
     * @throws IOException in case deletion is unsuccessful
     */
    //adapted from http://grepcode.com/file_/repo1.maven.org/maven2/commons-io/commons-io/1.4/org/apache/commons/io/FileUtils.java/?v=source deleteDirectory(...)
    public static String deleteDirectory(File directory) throws IOException {

        if (!directory.exists()) {
            return null;
        }

        mLastErrorMessage = cleanDirectory(directory);
        if (mLastErrorMessage != null) {
            return mLastErrorMessage;
        }
        if (!directory.delete()) {
            return String.format(mResources.getString(R.string.error_076), directory);
        }

        return null;
    }


    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     */
    //adapted from http://grepcode.com/file_/repo1.maven.org/maven2/commons-io/commons-io/1.4/org/apache/commons/io/FileUtils.java/?v=source cleanDirectory(...)
    public static String cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return String.format(mResources.getString(R.string.error_073), directory);
        }

        if (!directory.isDirectory()) {
            return String.format(mResources.getString(R.string.error_074), directory);
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            return String.format(mResources.getString(R.string.error_075), directory);
        }

        for (File file : files) {
            //noinspection ResultOfMethodCallIgnored
            if (file.isDirectory()) {
                mLastErrorMessage = deleteDirectory(file);
                if (mLastErrorMessage != null) {
                    return mLastErrorMessage;
                }
            }
            else {
                file.delete();
            }
        }

        return null;
    }


    /**
     * restore the database from an existing backup
     *
     * @param restoreFileWithPath backup file
     * @param dbPath          database to restore
     * @return true on success, false on error. See mLastErrorMessage for error details
     */
    public static boolean restoreDb(Context ctx, String restoreFileWithPath, String dbPath) {

        mLastErrorMessage = null;
        mLastException = null;

        if (!(restoreFileWithPath.endsWith(".db") || restoreFileWithPath.endsWith(".zip") || restoreFileWithPath.endsWith(".zi_"))) {
            mLastErrorMessage = ctx.getString(R.string.error_072);
            return false;
        }

//        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
        if (!FileUtils.isFileSystemAccessGranted(ctx)) {
            mLastErrorMessage = ctx.getString(R.string.error_070);
            return false;
        }

        if (dbPath == null) {
            mLastErrorMessage = ctx.getString(R.string.error_071);
            return false;
        }

        //TODO check if this db file is an AndiCar db
        if (restoreFileWithPath.endsWith(".db"))
            mLastErrorMessage = FileUtils.copyFile(ctx, restoreFileWithPath, dbPath, true);
        else if (restoreFileWithPath.endsWith(".zip") || restoreFileWithPath.endsWith(".zi_")) {
            createFolderIfNotExists(ctx, ConstantValues.TEMP_FOLDER);
            String unzipPath = ConstantValues.TEMP_FOLDER + System.currentTimeMillis() + "/";
            try {
                unzipFile(new File(restoreFileWithPath), new File(unzipPath));

                //see what was in the zip file
                ArrayList<String> bkFiles = getFileNames(ctx, unzipPath, null);
                if (bkFiles == null) {
                    mLastErrorMessage = mResources.getString(R.string.error_080);
                    return false;
                }

                boolean dbFileExists = false;
                for (String file : bkFiles) {
                    if (file.endsWith(".db")) { //check for database file
                        dbFileExists = true;
                        mLastErrorMessage = FileUtils.copyFile(ctx, unzipPath + file, dbPath, true);
                        if (mLastErrorMessage != null) {
                            return false;
                        }
                        break;
                    }
                }

                if (!dbFileExists) {
                    mLastErrorMessage = mResources.getString(R.string.error_081);
                    return false;
                }

                for (String file : bkFiles) {
                    if (file.endsWith(ConstantValues.TRACK_FOLDER_NAME)) { //check for gps track folder
                        mLastErrorMessage = FileUtils.copyDirectory(ctx, new File(unzipPath + file), new File(ConstantValues.TRACK_FOLDER), true);
                        if (mLastErrorMessage != null) {
                            return false;
                        }
                        break;
                    }
                }

                //clean up
                deleteDirectory(new File(unzipPath));

            }
            catch (IOException e) {
                mLastException = e;
                mLastErrorMessage = e.getMessage();
                Utils.showReportableErrorDialog(ctx, e.getMessage(), null, e, false);
            }
        }
        //update background services if need (scheduled tasks, etc.)
//        Intent intent = new Intent(ctx, ToDoNotificationService.class);
//        intent.putExtra(ToDoNotificationJob.SET_JUST_NEXT_RUN_KEY, false);
//        ctx.startService(intent);
        Utils.setToDoNextRun(ctx);

        return mLastErrorMessage == null;
    }


    public static int writeReportFile(Context ctx, String content, String fileName) {

//        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
        if (!isFileSystemAccessGranted(ctx)) {
            mLastErrorMessage = mResources.getString(R.string.error_070);
            return R.string.error_070;
        }

        try {
            mLastErrorMessage = null;
            mLastException = null;
            File file = new File(ConstantValues.REPORT_FOLDER + fileName);
            if (!file.createNewFile()) {
                return R.string.error_022;
            }
            FileWriter fw = new FileWriter(file);
            fw.append(content);
            fw.flush();
            fw.close();
        }
        catch (IOException e) {
            mLastErrorMessage = e.getMessage();
            mLastException = e;
            return R.string.error_023;
        }
        return -1;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public static boolean isFileSystemAccessGranted(Context ctx) {
        if (ConstantValues.BASE_FOLDER.equals(ctx.getFilesDir().getAbsolutePath())) //internal storage used => access granted
        {
            return true;
        }

        //external storage
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

    }
}
