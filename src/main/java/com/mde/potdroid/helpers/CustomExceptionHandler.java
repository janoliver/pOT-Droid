package com.mde.potdroid.helpers;

import android.os.Environment;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class writes an exception to the SDCard and then forwards the Exception to
 * the usual exception handler.
 */
public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    // forward to the one before.
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public CustomExceptionHandler() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date date = new Date();
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        String filename = dateFormat.format(date) + ".stacktrace";

        File ext_root = Environment.getExternalStorageDirectory();
        File path = new File(ext_root, "Android/data/com.mde.potdroid/files/log/");
        path.mkdirs();

        try {
            BufferedWriter bos = new BufferedWriter(new FileWriter(new File(path, filename)));
            bos.write(stacktrace);
            bos.flush();
            bos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        mDefaultHandler.uncaughtException(t, e);
    }
}