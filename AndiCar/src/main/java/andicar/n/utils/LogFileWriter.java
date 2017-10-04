package andicar.n.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import org.andicar2.activity.AndiCar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by Miklos Keresztes on 04.10.2017.
 */

public class LogFileWriter extends FileWriter {
    public LogFileWriter(@NonNull File file, boolean append) throws IOException {
        super(file, append);
        try {
            append("Log File: ").append(file.getAbsolutePath());
            appendnl("App version: ").append(Integer.toString(AndiCar.getAppVersion()))
                    .append("; Android version: ").append(Utils.getAndroidVersion())
                    .append("\n");
            flush();
        } catch (Exception e) {
            Log.e("AndiCar LogFileWriter", e.getMessage(), e);
        }

    }

    public Writer appendnl(CharSequence csq) throws IOException {
        return super.append("\n").append(Utils.getCurrentDateTimeForLog()).append(" ").append(csq);
    }
}
