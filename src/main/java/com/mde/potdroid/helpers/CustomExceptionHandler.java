package com.mde.potdroid.helpers;

import android.os.Environment;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by oli on 12/10/13.
 */
public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

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

        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(t, e);
    }
}